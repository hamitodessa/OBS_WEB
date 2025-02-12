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
		const response = await fetchWithSessionCheck("stok/ana", {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			}
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const ana = response.anaKodlari;

		const uranaSelect = document.getElementById("uranagrp");
		const anaSelect = document.getElementById("anagrp");

		anaSelect.innerHTML = "";
		uranaSelect.innerHTML = "";


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

		const newOption = document.createElement("option");
		newOption.value = "Bos Olanlar";
		newOption.textContent = "Bos Olanlar"; 

		const newOption1 = document.createElement("option");
		newOption1.value = "Bos Olanlar";
		newOption1.textContent = "Bos Olanlar";

		anaSelect.insertBefore(newOption, anaSelect.options[1]);
		uranaSelect.insertBefore(newOption1, uranaSelect.options[1]);

	} catch (error) {
		const modalError = document.getElementById("errorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}
async function imagrpfetchTableData() {
	const hiddenFieldValue = $('#imagrpBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	const grupraporDTO = {
		ukod1: parsedValues[0],
		ukod2: parsedValues[1],
		tar1: parsedValues[2],
		tar2: parsedValues[3],
		sinif1: parsedValues[4],
		sinif2: parsedValues[5],
		uranagrp: parsedValues[6],
		uraltgrp: parsedValues[7],
		birim: parsedValues[8],
		gruplama: parsedValues[9],
		stunlar: parsedValues[10],
		turu: parsedValues[11],
		anagrp: parsedValues[12],
		altgrp: parsedValues[13],
	};
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";

	document.body.style.cursor = "wait";
	const $yenileButton = $('#imagrpyenileButton');
	$yenileButton.prop('disabled', true).text('İşleniyor...');
	const mainTableBody = document.getElementById("mainTableBody");
	mainTableBody.innerHTML = "";
	clearTfoot();
	try {
		const response = await fetchWithSessionCheck("stok/imagrpdoldur", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(grupraporDTO),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		data = response;
		updateTableHeaders(data.baslik, data.sabitkolonsayisi);
		let headers = data.baslik
			.split(',')
			.map(header => header.trim().replace(/\[|\]/g, ""));
		tablobaslik = headers;
		rowCounter = data.sabitkolonsayisi;
		updateTable(data.data, headers, data.format, data.sabitkolonsayisi);
		document.body.style.cursor = "default";
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error;
	} finally {
		$yenileButton.prop('disabled', false).text('Yenile');
		document.body.style.cursor = "default";
	}
}

function updateTable(data, headers, format, kolonbaslangic) {
	let table = document.querySelector("#main-table tbody");
	let tfoot = document.querySelector("#main-table tfoot");
	table.innerHTML = "";
	if (!tfoot) {
		tfoot = document.createElement("tfoot");
		document.querySelector("#main-table").appendChild(tfoot);
	}
	tfoot.innerHTML = "";
	let kolonToplamlari = new Array(headers.length).fill(0);
	data.forEach(rowData => {
		let row = document.createElement("tr");
		row.classList.add("table-row-height");
		headers.forEach((header, index) => {
			let td = document.createElement("td");
			let cellValue = rowData[header] !== null ? rowData[header] : "";
			if (index >= kolonbaslangic) {
				let numericValue = parseFloat(cellValue);
				if (!isNaN(numericValue)) {
					td.textContent = format == 2 ? formatNumber2(numericValue) : formatNumber3(numericValue);
					td.classList.add("double-column");
					kolonToplamlari[index] += numericValue;
				} else {
					td.textContent = cellValue;
				}
			} else {
				td.textContent = cellValue;
			}
			row.appendChild(td);
		});
		table.appendChild(row);
	});

	let footerRow = document.createElement("tr");
	footerRow.classList.add("table-footer");
	headers.forEach((header, index) => {
		let th = document.createElement("th");
		if (index >= kolonbaslangic) {
			th.textContent = format == 2 ? formatNumber2(kolonToplamlari[index]) : formatNumber3(kolonToplamlari[index]);
			th.classList.add("double-column");
		} else {
			th.textContent = "";
		}
		footerRow.appendChild(th);
	});
	tfoot.appendChild(footerRow);
}

function updateTableHeaders(baslikString, kolonbaslangic) {
	let thead = document.querySelector("#main-table thead");
	thead.innerHTML = "";
	let trHead = document.createElement("tr");
	trHead.classList.add("thead-dark");
	let headers = baslikString
		.split(',')
		.map(header => header.trim().replace(/\[|\]/g, ""));
	headers.forEach((header, index) => {
		let th = document.createElement("th");
		th.textContent = header;
		if (index >= kolonbaslangic) {
			th.classList.add("double-column");
		}
		trHead.appendChild(th);
	});
	thead.appendChild(trHead);
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

async function imagrpdownloadReport() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	const $indirButton = $('#imagrprapreportDownload');
	$indirButton.prop('disabled', true).text('İşleniyor...');
	const $yenileButton = $('#imagrpyenileButton');
	$yenileButton.prop('disabled', true);
	let tableString = extractTableData(tablobaslik);
	try {
		const response = await fetchWithSessionCheckForDownload('stok/imagrp_download', {
			method: "POST",
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ headers: tablobaslik, data: tableString, sabitkolon: rowCounter }),
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

async function imagrpmailAt() {
	localStorage.removeItem("tableData");
	localStorage.removeItem("grprapor");
	localStorage.removeItem("tablobaslik");
	document.body.style.cursor = "wait";
	let rows = extractTableData(tablobaslik);
	localStorage.setItem("grprapor", rows);
	localStorage.setItem("tablobaslik", tablobaslik);
	const degerler = rowCounter + "," + "imagruprapor";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}

function extractTableData(headers) {
	let rowsString = "";
	let table = document.querySelector("#main-table tbody");
	let tbodyRows = table.querySelectorAll("tr");
	tbodyRows.forEach(tr => {
		let rowString = "";
		let tds = Array.from(tr.querySelectorAll("td"));
		headers.forEach((header, index) => {
			let td = tds[index];
			let cellValue = td ? td.innerText.trim() : "";
			rowString += cellValue + "||";
		});
		rowsString += rowString.slice(0, -2) + "\n";
	});
	let footer = document.querySelector("#main-table tfoot");
	if (footer) {
		let footerString = "";
		let ths = Array.from(footer.querySelectorAll("th"));
		headers.forEach((header, index) => {
			let th = ths[index];
			let footerValue = th ? th.innerText.trim() : "";
			footerString += footerValue + "||";
		});
		rowsString += footerString.slice(0, -2) + "\n";
	}
	return rowsString;
}

