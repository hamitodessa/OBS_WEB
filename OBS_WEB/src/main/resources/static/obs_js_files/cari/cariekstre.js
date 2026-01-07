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

// Buton clickleri aynı kalsın, sadece guard'lar:
function ilksayfa()     { if (currentPage > 0)            eksfetchTableData(0); }
function oncekisayfa()  { if (currentPage > 0)            eksfetchTableData(currentPage - 1); }
function sonrakisayfa() { if (currentPage < totalPages-1) eksfetchTableData(currentPage + 1); }
function sonsayfa()     { if (totalPages > 0)             eksfetchTableData(totalPages - 1); }

async function toplampagesize() {
  const errorDiv = document.getElementById("errorDiv");
  try {
    errorDiv.style.display = "none";
    errorDiv.innerText = "";

    const hiddenFieldValue = $('#ekstreBilgi').val() || "";
    const [hesapKodu, startDate, endDate] = hiddenFieldValue.split(",");

    const response = await fetchWithSessionCheck("cari/ekssize", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hesapKodu, startDate, endDate }),
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

async function eksdoldur() {
  document.body.style.cursor = "wait";
  updatePaginationUI(true);           // yükleme sırasında tümünü kilitle
  await toplampagesize();             // <-- ÖNEMLİ: bekle
  await eksfetchTableData(0);
  document.body.style.cursor = "default";
}

async function eksfetchTableData(page) {
  const hiddenFieldValue = $('#ekstreBilgi').val() || "";
  const [hesapKodu, startDate, endDate] = hiddenFieldValue.split(",");
  currentPage = page;

  const errorDiv = document.getElementById("errorDiv");
  errorDiv.style.display = "none";
  errorDiv.innerText = "";

  const tableBody = document.getElementById("tableBody");
  tableBody.innerHTML = "";
  document.getElementById("totalBorc").textContent = "";
  document.getElementById("totalAlacak").textContent = "";

  document.body.style.cursor = "wait";
  updatePaginationUI(true);           // istek sırasında tıklamayı kapat
  const $yenileButton = $('#eksyenileButton');
  $yenileButton.prop('disabled', true).text('İşleniyor...');

  try {
    const data = await fetchWithSessionCheck("cari/ekstre", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hesapKodu, startDate, endDate, page, pageSize }),
    });

    if (!data.success) {
      throw new Error(data.errorMessage || "Bir hata oluştu.");
    }

    let totalBorc = 0, totalAlacak = 0;
    data.data.forEach((item) => {
      const row = document.createElement("tr");
    
      row.innerHTML = `
        <td>${formatDate(item.TARIH)}</td>
        <td>${item.EVRAK || ''}</td>
        <td>${item.IZAHAT || ''}</td>
        <td>${item.KOD || ''}</td>
        <td class="double-column">${formatNumber4(item.KUR)}</td>
        <td class="double-column">${formatNumber2(item.BORC)}</td>
        <td class="double-column">${formatNumber2(item.ALACAK)}</td>
        <td class="double-column">${formatNumber2(item.BAKIYE)}</td>
        <td>${item.USER || ''}</td>
      `;
      tableBody.appendChild(row);
      totalBorc   += item.BORC   || 0;
      totalAlacak += item.ALACAK || 0;
    });

    // Toplamları şimdilik boş bırakıyorsun, bıraktım.
    // document.getElementById("totalBorc").textContent = formatNumber2(totalBorc);
    // document.getElementById("totalAlacak").textContent = formatNumber2(totalAlacak);

    hesapAdiOgren(hesapKodu, 'hesapAdi');
  } catch (error) {
    errorDiv.style.display = "block";
    errorDiv.innerText = error?.message || "Beklenmeyen bir hata oluştu.";
  } finally {
    $yenileButton.prop('disabled', false).text('Yenile');
    document.body.style.cursor = "default";
    updatePaginationUI();            // her yüklemeden sonra duruma göre aç/kapat
  }
}

async function ekstredownloadReport(format) {
	const hiddenFieldValue = $('#ekstreBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	const hesapKodu = parsedValues[0] || "";
	const startDateField = parsedValues[1] || "";
	const endDateField = parsedValues[2] || "";
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	if (!hesapKodu || !startDateField || !endDateField || !format) {
		errorDiv.style.display = "block";
		errorDiv.innerText = "Lütfen tüm alanları doldurun.";
		return;
	}
	document.body.style.cursor = "wait";
	const $indirButton = $('#indirButton');
	$indirButton.prop('disabled', true).text('İşleniyor...');
	const $yenileButton = $('#yenileButton');
	$yenileButton.prop('disabled', true);
	try {
		const response = await fetchWithSessionCheckForDownload('cari/ekstre_download', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({
				format: format,
				kodu: hesapKodu,
				startDate: startDateField,
				endDate: endDateField,
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

async function ekstremailAt() {
	localStorage.removeItem("tableData");
	localStorage.removeItem("grprapor");
	localStorage.removeItem("tablobaslik");
	const hiddenFieldValue = $('#ekstreBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	const hesapKodu = parsedValues[0];
	const startDateField = parsedValues[1];
	const endDateField = parsedValues[2];
	if (!hesapKodu) {
		alert("Lütfen geçerli bir hesap kodu girin!");
		return;
	}
	const degerler = hesapKodu + "," + startDateField + "," + endDateField + ",cariekstre";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}