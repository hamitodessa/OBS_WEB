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
		const response = await fetchWithSessionCheck("stok/anaoz1", {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			}
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const ana = response.anaKodlari;
		const oz1 = response.ozKodlari;
		const uranaSelect = document.getElementById("uranagrp");
		uranaSelect.innerHTML = "";
		const ozSelect = document.getElementById("urozkod");
		ozSelect.innerHTML = "";
		ana.forEach(item => {
			const optionUrana = document.createElement("option");
			optionUrana.value = item.ANA_GRUP;
			optionUrana.textContent = item.ANA_GRUP;
			uranaSelect.appendChild(optionUrana);
		});
		oz1.forEach(item => {
			const optionOz1 = document.createElement("option");
			optionOz1.value = item.OZEL_KOD_1;
			optionOz1.textContent = item.OZEL_KOD_1;
			ozSelect.appendChild(optionOz1);
		});
	} catch (error) {
		const modalError = document.getElementById("errorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}

function istenenayChanged() {
	const istenenay = document.getElementById("istenenaychc").checked;
	if (istenenay) {
		document.getElementById("istenenay").style.visibility = "visible";
	} else {
		document.getElementById("istenenay").style.visibility = "hidden";
	}
}

function dvzcevirChanged() {
	const dvzcevir = document.getElementById("dvzcevirchc").checked;
	if (dvzcevir) {
		document.getElementById("dvzcins").style.visibility = "visible";
		document.getElementById("dvzturu").style.visibility = "visible";
	} else {
		document.getElementById("dvzcins").style.visibility = "hidden";
		document.getElementById("dvzturu").style.visibility = "hidden";
	}
}

async function grpfetchTableData() {
	const hiddenFieldValue = $('#grpBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	const grupraporDTO = {
		tar1: parsedValues[0],
		tar2: parsedValues[1],
		uranagrp: parsedValues[2],
		ukod1: parsedValues[3],
		ukod2: parsedValues[4],
		uraltgrp: parsedValues[5],
		ckod1: parsedValues[6],
		ckod2: parsedValues[7],
		urozkod: parsedValues[8],
		birim: parsedValues[9],
		istenenay: parsedValues[10],
		gruplama: parsedValues[11],
		dvzcevirchc: parsedValues[12],
		doviz: parsedValues[13],
		stunlar: parsedValues[14],
		dvzturu: parsedValues[15],
		turu: parsedValues[16],
		istenenaychc: parsedValues[17],
		sinif1: parsedValues[18],
		sinif2: parsedValues[19],
	};
	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";
	const $yenileButton = $('#grpyenileButton');
	$yenileButton.prop('disabled', true).text('İşleniyor...');
	const mainTableBody = document.getElementById("mainTableBody");
	mainTableBody.innerHTML = "";
	let table = document.querySelector("#main-table");
	let tfoot = table.querySelector("tfoot");
	clearTfoot();
	try {
		const response = await fetchWithSessionCheck("stok/grpdoldur", {
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
		rowCounter = data.sabitkolonsayisi;
		let sqlHeaders = "";
		sqlHeaders = data.baslik.split(',').map(header => header.trim().replace(/\[|\]/g, ""));
		const sabitKolonlar = sqlHeaders.slice(0, data.sabitkolonsayisi);
		const tumKolonlar = data.data.length > 0 ? Object.keys(data.data[0]) : [];
		const dinamikKolonlar = tumKolonlar.filter(kolon => !sabitKolonlar.includes(kolon));
		const headers = [...sabitKolonlar, ...dinamikKolonlar];
		const kolonbaslangic = sabitKolonlar.length;

		updateTableHeaders(headers, data.sabitkolonsayisi);

		let kolonToplamlari = new Array(headers.length + 1).fill(0);
		data.data.forEach(rowData => {
			const row = document.createElement('tr');
			row.classList.add('expandable', 'table-row-height');
			let satirToplami = 0;
			let rowContent = headers.map((key, index) => {
				let cellValue = rowData[key] !== null ? rowData[key] : 0;
				let numericValue = isNaN(cellValue) ? 0 : parseFloat(cellValue);
				let td = "";
				if (index >= kolonbaslangic) {
					if (response.format === 2) {
						td = `<td class="double-column">${formatNumber2(numericValue)}</td>`;
					} else if (response.format === 3) {
						td = `<td class="double-column">${formatNumber3(numericValue)}</td>`;
					} else {
						td = `<td class="double-column">${numericValue}</td>`;
					}
					satirToplami += numericValue;
					kolonToplamlari[index] += numericValue; 
				} else {
					td = `<td>${cellValue}</td>`;
				}
				return td;
			}).join("");

			rowContent += `<td class="double-column">${formatNumber2(satirToplami)}</td>`;
			kolonToplamlari[kolonToplamlari.length - 1] += satirToplami;
			row.innerHTML = rowContent;
			mainTableBody.appendChild(row);
		});
		let tableHeaderRow = document.querySelector("#main-table thead tr");
		let toplamTh = document.createElement("th");
		toplamTh.classList.add("double-column");
		toplamTh.textContent = "TOPLAM";
		tableHeaderRow.appendChild(toplamTh);
		let footerRow = document.createElement('tr');
		footerRow.classList.add('table-footer');
		let footerContent = headers.map((_, index) => {
			if (index >= kolonbaslangic) {
				return `<td class="double-column" id="toplam-${index}">${formatNumber2(kolonToplamlari[index])}</td>`;
			} else {
				return `<td></td>`;
			}
		}).join("");
		footerContent += `<td class="double-column">${formatNumber2(kolonToplamlari[kolonToplamlari.length - 1])}</td>`;
		footerRow.innerHTML = footerContent;

		tfoot.innerHTML = ""; 
		tfoot.appendChild(footerRow);

		document.body.style.cursor = "default";
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error;
	} finally {
		$yenileButton.prop('disabled', false).text('Yenile');
		document.body.style.cursor = "default";
	}
}

function updateTableHeaders(headers, kolonbaslangic) {
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
		if (index >= kolonbaslangic) {

			th.classList.add("double-column");
		}
		trHead.appendChild(th);
	});
	thead.appendChild(trHead);
}

async function grpdownloadReport() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	const $indirButton = $('#grpDownloadButton');
	$indirButton.prop('disabled', true).text('İşleniyor...');
	const $yenileButton = $('#grpyenileButton');
	$yenileButton.prop('disabled', true);
	let rows = extractTableData("main-table", rowCounter);
	try {
		const response = await fetchWithSessionCheckForDownload('stok/grp_download', {
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

async function grpmailAt() {
	document.body.style.cursor = "wait";
	let rows = extractTableData("main-table");
	localStorage.setItem("tableData", JSON.stringify({ rows: rows }));
	const degerler = "gruprapor";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}

function extractTableData(tableId) {
	let table = document.querySelector(`#${tableId}`);
	let headers = Array.from(table.querySelectorAll("thead th")).map(th => th.innerText.trim());
	let rows = [];

	console.log("Başlıklar:", headers); // 📌 Kontrol için ekledik

	table.querySelectorAll("tbody tr").forEach(tr => {
		let rowData = {};
		let tds = Array.from(tr.querySelectorAll("td"));

		headers.forEach((header, index) => {
			let td = tds[index]; // Doğru `td` hücresini al
			let cellValue = td ? td.innerText.trim() : ""; // Boşsa boş string ata
			rowData[header] = cellValue; // 📌 Key olarak başlık ismini kullan
		});

		rows.push(rowData);
	});

	// 📌 FOOTER (Toplam satırlarını ekleyelim)
	let tfoot = table.querySelector("tfoot");
	if (tfoot) {
		let footerData = {};
		let ths = Array.from(tfoot.querySelectorAll("th"));

		headers.forEach((header, index) => {
			let th = ths[index]; // Doğru `th` hücresini al
			let footerValue = th ? th.innerText.trim() : "";
			footerData[header] = footerValue;
		});

		rows.push(footerData);
	}

	console.log("Dönüştürülen JSON:", rows); // 📌 Kontrol için JSON'u yazdır

	return rows;
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
