currentPage = 0;
totalPages = 0;
pageSize = 500;

function setDisabled(el, yes) { el.disabled = !!yes; }
function updatePaginationUI(disableAllWhileLoading = false) {
  const first = document.getElementById("ilksayfa");
  const prev  = document.getElementById("oncekisayfa");
  const next  = document.getElementById("sonrakisayfa");
  const last  = document.getElementById("sonsayfa");

  if (disableAllWhileLoading) {
    setDisabled(first, true); setDisabled(prev, true);
    setDisabled(next, true);  setDisabled(last, true);
    return;
  }

  const noData = totalPages === 0;
  setDisabled(first, noData || currentPage <= 0);
  setDisabled(prev,  noData || currentPage <= 0);
  setDisabled(next,  noData || currentPage >= totalPages - 1);
  setDisabled(last,  noData || currentPage >= totalPages - 1);
}

function ilksayfa()     { if (currentPage > 0)            dvzfetchTableData(0); }
function oncekisayfa()  { if (currentPage > 0)            dvzfetchTableData(currentPage - 1); }
function sonrakisayfa() { if (currentPage < totalPages-1) dvzfetchTableData(currentPage + 1); }
function sonsayfa()     { if (totalPages > 0)             dvzfetchTableData(totalPages - 1); }

async function toplampagesize() {
  const errorDiv = document.getElementById("errorDiv");
  try {
    errorDiv.style.display = "none";
    errorDiv.innerText = "";

    const hiddenFieldValue = $('#dvzcevirmeBilgi').val();
    const [hesapKodu, startDate, endDate, dvz_tur, dvz_cins] = (hiddenFieldValue || "").split(",");

    const response = await fetchWithSessionCheck("cari/dvzsize", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hesapKodu, startDate, endDate, dvz_tur, dvz_cins }),
    });
    const totalRecords = response?.totalRecords ?? 0;
    totalPages = Math.max(0, Math.ceil(totalRecords / pageSize));
  } catch (error) {
    errorDiv.style.display = "block";
    errorDiv.innerText = error?.message || error || "Beklenmeyen hata.";
    totalPages = 0;
  } finally {
    updatePaginationUI();
  }
}

async function dvzdoldur() {
  document.body.style.cursor = "wait";
  updatePaginationUI(true);         // yükleme süresince kilit
  await toplampagesize();           // <-- mutlaka bekle
  await dvzfetchTableData(0);
  document.body.style.cursor = "default";
}

async function dvzfetchTableData(page) {
  const hiddenFieldValue = $('#dvzcevirmeBilgi').val();
  const [hesapKodu, startDate, endDate, dvz_tur, dvz_cins] = (hiddenFieldValue || "").split(",");
  currentPage = page;

  const errorDiv = document.getElementById("errorDiv");
  errorDiv.style.display = "none";
  errorDiv.innerText = "";
  document.getElementById("dvzaciklama").innerText = "";

  if (!hesapKodu || !startDate || !endDate) {
    errorDiv.style.display = "block";
    errorDiv.innerText = "Lütfen tüm alanları doldurun.";
    return;
  }

  const tableBody = document.getElementById("tableBody");
  tableBody.innerHTML = "";

  document.body.style.cursor = "wait";
  updatePaginationUI(true);         // istek sırasında butonları kapat
  const $yenileButton = $('#dvzyenileButton');
  $yenileButton.prop('disabled', true).text('İşleniyor...');

  try {
    const data = await fetchWithSessionCheck("cari/dvzcevirme", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hesapKodu, startDate, endDate, dvz_tur, dvz_cins, page, pageSize }),
    });

    if (!data.success) throw new Error(data.errorMessage || "Bir hata oluştu.");

    let boskur = 0;
    data.data.forEach(item => {
      const row = document.createElement("tr");
      row.classList.add("table-row-height");
      row.innerHTML = `
        <td>${formatDate(item.TARIH)}</td>
        <td>${item.EVRAK || ''}</td>
        <td>${item.IZAHAT || ''}</td>
        <td class="double-column" style="color: ${item.CEV_KUR == 1 ? 'red' : 'black'};">
          ${formatNumber4(item.CEV_KUR)}
        </td>
        <td class="double-column">${formatNumber2(item.DOVIZ_TUTAR)}</td>
        <td class="double-column">${formatNumber2(item.DOVIZ_BAKIYE)}</td>
        <td class="double-column">${formatNumber2(item.BAKIYE)}</td>
        <td class="double-column">${formatNumber4(item.KUR)}</td>
        <td class="double-column">${formatNumber2(item.BORC)}</td>
        <td class="double-column">${formatNumber2(item.ALACAK)}</td>
        <td>${item.USER || ''}</td>
      `;
      if (item.CEV_KUR === 1.0) boskur += 1;
      tableBody.appendChild(row);
    });

    hesapAdiOgren(hesapKodu, 'hesapAdi');
    document.getElementById("dvzaciklama").innerText = "Bos Kur :" + boskur;

  } catch (error) {
    errorDiv.style.display = "block";
    errorDiv.innerText = error?.message || "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.";
  } finally {
    $yenileButton.prop('disabled', false).text('Yenile');
    document.body.style.cursor = "default";
    updatePaginationUI();          // sayfa durumuna göre aç/kapat
  }
}

async function dvzdownloadReport(format) {
	const hiddenFieldValue = $('#dvzcevirmeBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	const hesapKodu = parsedValues[0] || "";
	const startDateField = parsedValues[1] || "";
	const endDateField = parsedValues[2] || "";
	const dvz_tur = parsedValues[3];
	const dvz_cins = parsedValues[4];
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	if (!hesapKodu || !startDateField || !endDateField || !format) {
		errorDiv.style.display = "block";
		errorDiv.innerText = "Lütfen tüm alanlari doldurun.";
		return;
	}
	document.body.style.cursor = "wait";
	const $indirButton = $('#dvzindirButton');
	$indirButton.prop('disabled', true).text('İşleniyor...');
	const $yenileButton = $('#dvzyenileButton');
	$yenileButton.prop('disabled', true);

	try {
		const response = await fetchWithSessionCheckForDownload('cari/dvzcevirme_download', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({
				format: format,
				hesapKodu: hesapKodu,
				startDate: startDateField,
				endDate: endDateField,
				dvz_tur: dvz_tur,
				dvz_cins: dvz_cins
			}),
		});
		if (response.blob) {
			const disposition = response.headers.get('Content-Disposition');
			const fileName = disposition.match(/filename="(.+)"/)[1];
			const url = window.URL.createObjectURL(response.blob);
			const a = document.createElement("a");
			a.href = url;
			a.download = fileName;
			document.body.appendChild(a);
			a.click();
			a.remove();
			window.URL.revokeObjectURL(url);
		} else {
			throw new Error("Dosya indirilemedi.");
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Bilinmeyen bir hata oluştu.";
	} finally {
		$indirButton.prop('disabled', false).text('Rapor İndir');
		$yenileButton.prop('disabled', false);
		document.body.style.cursor = "default";
	}
}

function dvzmailAt() {
	localStorage.removeItem("tableData");
	localStorage.removeItem("grprapor");
	localStorage.removeItem("tablobaslik");
	const hiddenFieldValue = $('#dvzcevirmeBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	const hesapKodu = parsedValues[0];
	const startDateField = parsedValues[1];
	const endDateField = parsedValues[2];
	const dvz_tur = parsedValues[3];
	const dvz_cins = parsedValues[4];
	if (!hesapKodu) {
		alert("Lütfen geçerli bir hesap kodu girin!");
		return;
	}
	const degerler = hesapKodu + "," + startDateField + "," + endDateField + "," + dvz_tur + "," + dvz_cins + ",dvzcevir";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}