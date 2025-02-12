async function eksfetchTableData() {
	const hiddenFieldValue = $('#ekstreBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	const hesapKodu = parsedValues[0];
	const startDate = parsedValues[1];
	const endDate = parsedValues[2];
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	const tableBody = document.getElementById("tableBody");
	tableBody.innerHTML = "";
	document.getElementById("totalBorc").textContent = "";
	document.getElementById("totalAlacak").textContent = "";

	document.body.style.cursor = "wait";
	const $yenileButton = $('#eksyenileButton');
	$yenileButton.prop('disabled', true).text('İşleniyor...');
	try {
		const data = await fetchWithSessionCheck("cari/ekstre", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({ hesapKodu, startDate, endDate }),
		});

		if (data.success) {
			let totalBorc = 0;
			let totalAlacak = 0;
			data.data.forEach(item => {
				const row = document.createElement("tr");
				row.classList.add("table-row-height");
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
				totalBorc += item.BORC || 0;
				totalAlacak += item.ALACAK || 0;
			});
			document.getElementById("totalBorc").textContent = formatNumber2(totalBorc);
			document.getElementById("totalAlacak").textContent = formatNumber2(totalAlacak);
			hesapAdiOgren(hesapKodu, 'hesapAdi');
		} else {
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage || "Bir hata oluştu.";
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message;
	} finally {
		$yenileButton.prop('disabled', false).text('Yenile');
		document.body.style.cursor = "default";
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