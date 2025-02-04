async function anagrpChanged(anagrpElement, altgrpElement) {
	const anagrup = anagrpElement.value;
	const errorDiv = document.getElementById("errorDiv");
	const selectElement = document.getElementById(altgrpElement);
	selectElement.innerHTML = '';
	if (anagrup === "") {
		selectElement.disabled = true;
		return;
	}
	document.body.style.cursor = "wait";
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("stok/altgrup", {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ anagrup: anagrup }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		response.altKodlari.forEach(kod => {
			const option = document.createElement("option");
			option.value = kod.ALT_GRUP;
			option.textContent = kod.ALT_GRUP;
			selectElement.appendChild(option);
		});
		selectElement.disabled = selectElement.options.length === 0;
	} catch (error) {
		selectElement.disabled = true;
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function openenvModal(modal) {
	$(modal).modal('show');
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("stok/anadepo", {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			}
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const ana = response.anaKodlari;
		const dpo = response.depoKodlari;
		const uranaSelect = document.getElementById("uranagrp");
		const anaSelect = document.getElementById("anagrp");
		const dpoSelect = document.getElementById("depo");
		anaSelect.innerHTML = "";
		uranaSelect.innerHTML = "";
		dpoSelect.innerHTML = "";
		ana.forEach(item => {
			const optionAna = document.createElement("option");
			optionAna.value = item.ANA_GRUP;
			optionAna.textContent = item.ANA_GRUP;
			anaSelect.appendChild(optionAna);
			const optionUrana = document.createElement("option");
			optionUrana.value = item.ANA_GRUP;
			optionUrana.textContent = item.ANA_GRUP;
			uranaSelect.appendChild(optionUrana);
		});
		dpo.forEach(item => {
			const option = document.createElement("option");
			option.value = item.DEPO;
			option.textContent = item.DEPO;
			dpoSelect.appendChild(option);
		});
	} catch (error) {
		const modalError = document.getElementById("errorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}


async function envfetchTableData() {
	const hiddenFieldValue = $('#envanterBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	const envanterDTO = {
		tar1: parsedValues[0],
		tar2: parsedValues[1],
		uranagrp: parsedValues[2],
		ukod1: parsedValues[3],
		ukod2: parsedValues[4],
		uraltgrp: parsedValues[5],
		evrno1: parsedValues[6],
		evrno2: parsedValues[7],
		anagrp: parsedValues[8],
		gruplama: parsedValues[9],
		altgrp: parsedValues[10],
		depo: parsedValues[11],
		fiatlama: parsedValues[12],
		depohardahil: parsedValues[13],
		uretfisdahil: parsedValues[14],
	};
	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";
	const $yenileButton = $('#envyenileButton');
	$yenileButton.prop('disabled', true).text('İşleniyor...');
	const mainTableBody = document.getElementById("mainTableBody");
	mainTableBody.innerHTML = "";
	clearTfoot();
	try {
		const response = await fetchWithSessionCheck("stok/envanterdoldur", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(envanterDTO),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		data = response;
		let sqlHeaders = "";
		sqlHeaders = ["KODU", "ADI", "SIMGE", "GIRIS MIKTARI", "GIRIS TUTARI", "CIKIS MIKTARI", "CIKIS TUTARI", "CIKIS MALIYET", "STOK MIKTARI", "MALIYET", "TUTAR"];
		updateTableHeadersnormal(sqlHeaders);

		if (response.raporturu === 'normal') {
			
		}
		let totalmiktar = 0;
		let totalcmiktar = 0;
		let totalgtutar = 0;
		let totaltutar = 0;
		let totalstok = 0;
		let totalctutar = 0;
		data.data.forEach(rowData => {
			const row = document.createElement('tr');
			row.classList.add('expandable');
			row.classList.add("table-row-height");
			if (response.raporturu === 'normal') {
				row.innerHTML = `
                    <td>${rowData.Kodu || ''}</td>
                    <td>${rowData.Adi || ''}</td>
                    <td>${rowData.Simge || ''}</td>
                    <td class="double-column">${rowData.Giris_Miktari}</td>
					<td class="double-column">${formatNumber2(rowData.Giris_Tutar)}</td>
					<td class="double-column">${rowData.Cikis_Miktari}</td>
					<td class="double-column">${formatNumber2(rowData.Cikis_Tutar)}</td>
					<td class="double-column">${formatNumber2(rowData.Cikis_Maliyet)}</td>
                    <td class="double-column">${rowData.Stok_Miktari}</td>
                    <td class="double-column">${formatNumber2(rowData.Maliyet)}</td>
                    <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
                `;
				totalmiktar += parseLocaleNumber(rowData.Giris_Miktari);
				totalcmiktar += parseLocaleNumber(rowData.Cikis_Miktari);
				totalstok += parseLocaleNumber(rowData.Stok_Miktari);
				totaltutar += rowData.Tutar;
				totalctutar += rowData.Cikis_Tutar;
				totalgtutar += rowData.Giris_Tutar;
			}
			mainTableBody.appendChild(row);
		});

		if (response.raporturu === 'normal') {
			document.getElementById("toplam-3").innerText = formatNumber3(totalmiktar);
			document.getElementById("toplam-4").innerText = formatNumber2(totalgtutar);
			document.getElementById("toplam-5").innerText = formatNumber3(totalcmiktar);
			document.getElementById("toplam-6").innerText = formatNumber3(totalctutar);
			document.getElementById("toplam-8").innerText = formatNumber3(totalstok);
			document.getElementById("toplam-10").innerText = formatNumber2(totaltutar);
		}
		document.body.style.cursor = "default";
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error;
	} finally {
		$yenileButton.prop('disabled', false).text('Yenile');
		document.body.style.cursor = "default";
	}
}

function clearTfoot() {
	let table = document.querySelector("#main-table");
	let tfoot = table.querySelector("tfoot");
	if (tfoot) {
		let cells = tfoot.querySelectorAll("th");
		for (let i = 0; i < cells.length; i++) {
			cells[i].textContent = "";
		}
	}
}


function updateTableHeadersnormal(headers) {

	let thead = document.querySelector("#main-table thead");
	let table = document.querySelector("#main-table");
	let tfoot = table.querySelector("tfoot");
	if (!tfoot) {
		tfoot = document.createElement("tfoot");
		table.appendChild(tfoot);
	}
	thead.innerHTML = "";
	let trHead = document.createElement("tr");
	trHead.classList.add("thead-dark");
	headers.forEach((header, index) => {
		let th = document.createElement("th");
		th.textContent = header;

		if (index >= headers.length - 8) {
			th.classList.add("double-column");
		}
		trHead.appendChild(th);
	});
	thead.appendChild(trHead);
	tfoot.innerHTML = "";
	let trFoot = document.createElement("tr");
	headers.forEach((_, index) => {
		let th = document.createElement("th");
		if (index === 3) {
			th.textContent = "0.000";
			th.id = "toplam-" + index;
			th.classList.add("double-column");
		} else if (index === 4) {
			th.textContent = "0.00";
			th.id = "toplam-" + index;
			th.classList.add("double-column");
		} else if (index === 5) {
			th.textContent = "0.000";
			th.id = "toplam-" + index;
			th.classList.add("double-column");
		} else if (index === 6) {
			th.textContent = "0.00";
			th.id = "toplam-" + index;
			th.classList.add("double-column");
		} else if (index === 8) {
			th.textContent = "0.000";
			th.id = "toplam-" + index;
			th.classList.add("double-column");
		} else if (index === 10) {
			th.textContent = "0.00";
			th.id = "toplam-" + index;
			th.classList.add("double-column");
		} else {
			th.textContent = "";
		}
		trFoot.appendChild(th);
	});
	tfoot.appendChild(trFoot);
}

async function envdownloadReport() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	const $indirButton = $('#envDownloadButton');
	$indirButton.prop('disabled', true).text('İşleniyor...');
	const $yenileButton = $('#envyenileButton');
	$yenileButton.prop('disabled', true);
	let rows = extractTableData("main-table");
	try {
		const response = await fetchWithSessionCheckForDownload('stok/env_download', {
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

async function envmailAt() {
	document.body.style.cursor = "wait";
	let rows = extractTableData("main-table");
	localStorage.setItem("tableData", JSON.stringify({ rows: rows }));
	const degerler = "envanter";
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

function fiatlamaChanged() {
	const fiatlama = document.getElementById("fiatlama").value;
	if (fiatlama === "agort") {
		document.getElementById("ukod2").style.visibility = "visible";
	} else {
		document.getElementById("ukod2").style.visibility = "hidden";
	}
}