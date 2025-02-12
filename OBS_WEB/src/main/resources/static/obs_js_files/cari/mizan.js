async function mizfetchTableData() {
	const hiddenFieldValue = $('#mizanBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	const hkodu1 = parsedValues[0];
	const hkodu2 = parsedValues[1];
	const startDate = parsedValues[2];
	const endDate = parsedValues[3];
	const cins1 = parsedValues[4];
	const cins2 = parsedValues[5];
	const karton1 = parsedValues[6];
	const karton2 = parsedValues[7];
	const hangi_tur = parsedValues[8];
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";

	const Mizan_Request = {
		hkodu1: hkodu1,
		hkodu2: hkodu2,
		startDate: startDate,
		endDate: endDate,
		cins1: cins1,
		cins2: cins2,
		karton1: karton1,
		karton2: karton2,
		hangi_tur: hangi_tur
	};
	const tableBody = document.getElementById("tableBody");
	tableBody.innerHTML = "";
	document.getElementById("totalBorc").textContent = "";
	document.getElementById("totalAlacak").textContent = "";
	document.getElementById("totalBakiye").textContent = "";
	document.body.style.cursor = "wait";
	const $yenileButton = $('#mizyenileButton');
	$yenileButton.prop('disabled', true).text('İşleniyor...');
	try {
		const data = await fetchWithSessionCheck("cari/mizanrapor", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(Mizan_Request),
		});
		if (data.success) {
			let totalBorc = 0;
			let totalAlacak = 0;
			let totalBakiye = 0;
			data.data.forEach(item => {
				const row = document.createElement("tr");
				row.classList.add("table-row-height");
				row.innerHTML = `
                	<td>${item.HESAP || ''}</td>
                    <td>${item.UNVAN || ''}</td>
                    <td>${item.H_CINSI || ''}</td>
                    <td class="double-column">${formatNumber2(item.BORC)}</td>
                    <td class="double-column">${formatNumber2(item.ALACAK)}</td>
                    <td class="double-column">${formatNumber2(item.BAKIYE)}</td>
                `;
				tableBody.appendChild(row);
				totalBorc += item.BORC || 0;
				totalAlacak += item.ALACAK || 0;
				totalBakiye += item.BAKIYE || 0;
			});
			document.getElementById("totalBorc").textContent = formatNumber2(totalBorc);
			document.getElementById("totalAlacak").textContent = formatNumber2(totalAlacak);
			document.getElementById("totalBakiye").textContent = formatNumber2(totalBakiye);
		} else {
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage || "Bir hata oluştu.";
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.";
	} finally {
		$yenileButton.prop('disabled', false).text('Yenile');
		document.body.style.cursor = "default";
	}
}

async function mizdownloadReport(format) {
    const hiddenFieldValue = $('#mizanBilgi').val();
    const parsedValues = hiddenFieldValue.split(",");
    const Mizan_Request = {
        format: format,
        hkodu1: parsedValues[0] || "",
        hkodu2: parsedValues[1] || "",
        startDate: parsedValues[2] || "",
        endDate: parsedValues[3] || "",
        cins1: parsedValues[4] || "",
        cins2: parsedValues[5] || "",
        karton1: parsedValues[6] || "",
        karton2: parsedValues[7] || "",
        hangi_tur: parsedValues[8] || "",
    };
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    document.body.style.cursor = "wait";
    const $indirButton = $('#indirButton');
    $indirButton.prop('disabled', true).text('İşleniyor...');
    const $yenileButton = $('#yenileButton');
    $yenileButton.prop('disabled', true);
    try {
		const response = await fetchWithSessionCheckForDownload('cari/mizan_download', {
		    method: 'POST',
		    headers: { 'Content-Type': 'application/json' },
		    body: JSON.stringify(Mizan_Request),
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
		}else {
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

function mizmailAt() {
	localStorage.removeItem("tableData");
	localStorage.removeItem("grprapor");
	localStorage.removeItem("tablobaslik");
	const hiddenFieldValue = $('#mizanBilgi').val(); 
	const parsedValues = hiddenFieldValue.split(",");
	const hkodu1 = parsedValues[0];
	const hkodu2 = parsedValues[1];
	const startDate = parsedValues[2];
	const endDate = parsedValues[3];
	const cins1 = parsedValues[4];
	const cins2 = parsedValues[5];
	const karton1 = parsedValues[6];
	const karton2 = parsedValues[7];
	const hangi_tur = parsedValues[8];
	const degerler = hkodu1 + "," + hkodu2 + "," + startDate + "," + endDate + "," + cins1 + "," + cins2 + "," + karton1 + "," + karton2 + "," + hangi_tur + ",carimizan";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}