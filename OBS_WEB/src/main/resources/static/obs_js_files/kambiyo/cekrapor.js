async function cekfetchTableData() {
	const hiddenFieldValue = $('#cekrapBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	const cekraporDTO = {
		cekno1: parsedValues[0],
		cekno2: parsedValues[1],
		durum1: parsedValues[2],
		durum2: parsedValues[3],
		vade1: parsedValues[4],
		vade2: parsedValues[5],
		ttar1: parsedValues[6],
		ttar2: parsedValues[7],
		gbor1: parsedValues[8],
		gbor2: parsedValues[9],
		gozel: parsedValues[10],
		cozel: parsedValues[11],
		gtar1: parsedValues[12],
		gtar2: parsedValues[13],
		cins1: parsedValues[14],
		cins2: parsedValues[15],
		cbor1: parsedValues[16],
		cbor2: parsedValues[17],
		ches1: parsedValues[18],
		ches2: parsedValues[19],
		ctar1: parsedValues[20],
		ctar2: parsedValues[21],
		hangi_tur: parsedValues[22],
		ghes1: parsedValues[23],
		ghes2: parsedValues[24],
	};
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	const tableBody = document.getElementById("tableBody");
	tableBody.innerHTML = "";
	document.body.style.cursor = "wait";
	const $yenileButton = $('#rapyenileButton');
	$yenileButton.prop('disabled', true).text('İşleniyor...');
	try {
		const data = await fetchWithSessionCheck("kambiyo/cekraporlama", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(cekraporDTO),
		});
		if (data.success) {
			data.data.forEach(item => {
				const row = document.createElement("tr");
				row.classList.add("table-row-height");
				row.innerHTML = `
									<td>${item.Cek_No || ''}</td>
				                    <td>${formatDate(item.Vade)}</td>
				                    <td>${item.Giris_Bordro || ''}</td>
				                    <td>${formatDate(item.Giris_Tarihi)}</td>
									<td>${item.Giris_Musteri || ''}</td>
									<td>${item.Banka || ''}</td>
									<td>${item.Sube || ''}</td>
									<td>${item.Cins || ''}</td>
				                    <td class="double-column">${formatNumber2(item.Tutar)}</td>
									<td>${item.Durum || ''}</td>
									<td>${item.T_Tarih || ''}</td>
									<td>${item.Giris_Ozel_Kod || ''}</td>
									<td>${item.Cikis_Bordro || ''}</td>
									<td>${item.Cikis_Tarihi || ''}</td>
									<td>${item.Cikis_Musteri || ''}</td>
									<td>${item.Cikis_Ozel_Kod || ''}</td>
				                    <td>${item.USER || ''}</td>
				                `;
				tableBody.appendChild(row);
			});
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

async function cekrapdownloadReport() {
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    document.body.style.cursor = "wait";
    const $indirButton = $('#cekrapreportFormat');
    $indirButton.prop('disabled', true).text('İşleniyor...');
    const $yenileButton = $('#cekrapyenileButton');
    $yenileButton.prop('disabled', true);
    let rows = extractTableData("main-table");
    try {
        const response = await fetchWithSessionCheckForDownload('kambiyo/cekrap_download', {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(rows)
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

async function cekrapmailAt() {
    document.body.style.cursor = "wait";
	localStorage.removeItem("tableData");
	localStorage.removeItem("grprapor");
	localStorage.removeItem("tablobaslik");
    
	let rows = extractTableData("main-table");
    localStorage.setItem("tableData", JSON.stringify({ rows: rows }));
    const degerler = "cekrap";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);
}

function extractTableData(tableId) {
    let table = document.querySelector(`#${tableId}`);
    let headers = [];
    let rows = [];
    table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));
    table.querySelectorAll("tbody tr").forEach(tr => {
        let rowData = {};
        let nonEmptyCount = 0;
        tr.querySelectorAll("td").forEach((td, index) => {
            let value = td.innerText.trim();
            if (value !== "") {
                nonEmptyCount++;
            }
            rowData[headers[index]] = value;
        });
        if (nonEmptyCount > 0) {
            rows.push(rowData);
        }
    });
    let tfoot = table.querySelector("tfoot");
    if (tfoot) {
        let tfootRowData = {};
        let nonEmptyCount = 0;
        tfoot.querySelectorAll("th").forEach((th, index) => {
            let value = th.innerText.trim();
            if (value !== "") {
                nonEmptyCount++;
            }
            tfootRowData[headers[index]] = value;
        });
        if (nonEmptyCount > 0) {
            rows.push(tfootRowData);
        }
    }
    return rows;
}