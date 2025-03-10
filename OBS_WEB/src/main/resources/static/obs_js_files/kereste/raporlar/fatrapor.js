pageSize = 250;
async function anagrpChanged(anagrpElement) {
	const anagrup = anagrpElement.value;
	const errorDiv = document.getElementById("errorDiv");
	const selectElement = document.getElementById("altgrp");
	selectElement.innerHTML = '';
	if (anagrup === "") {
		selectElement.disabled = true;
		return;
	}
	document.body.style.cursor = "wait";
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("kereste/altgrup", {
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

function ilksayfa() {
	kerfetchTableData(0);
}

function oncekisayfa() {
	if (currentPage > 0) {
		kerfetchTableData(currentPage - 1);
	}
}

function sonrakisayfa() {
	if (currentPage < totalPages - 1) {
		kerfetchTableData(currentPage + 1);
	}
}

async function sonsayfa() {
	kerfetchTableData(totalPages - 1);
}

async function toplampagesize() {
	try {
		const errorDiv = document.getElementById("errorDiv");
		errorDiv.style.display = "none";
		errorDiv.innerText = "";
		const kerestedetayraporDTO = getfatraporDTO();
		const response = await fetchWithSessionCheck("kereste/fatdoldursize", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(kerestedetayraporDTO),
		});
		const totalRecords = response.totalRecords;
		totalPages = Math.ceil(totalRecords / pageSize);
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error;
		document.body.style.cursor = "default";
	}
}

async function kerfatdoldur() {
	document.body.style.cursor = "wait";
	toplampagesize();
	kerfetchTableData(0);
}

function getfatraporDTO() {
	const hiddenFieldValue = $('#fatrapBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	return {
		gtar1: parsedValues[0],
		gtar2: parsedValues[1],
		ctar1: parsedValues[2],
		ctar2: parsedValues[3],
		ukodu1: parsedValues[4],
		ukodu2: parsedValues[5],
		cfirma1: parsedValues[6],
		cfirma2: parsedValues[7],
		pak1: parsedValues[8],
		pak2: parsedValues[9],
		cevr1: parsedValues[10],
		cevr2: parsedValues[11],
		gfirma1: parsedValues[12],
		gfirma2: parsedValues[13],
		cana: parsedValues[14],
		evr1: parsedValues[15],
		evr2: parsedValues[16],
		calt: parsedValues[17],
		gana: parsedValues[18],
		galt: parsedValues[19],
		gdepo: parsedValues[20],
		gozkod: parsedValues[21],
		kons1: parsedValues[22],
		kons2: parsedValues[23],
		cozkod: parsedValues[24],
		cdepo: parsedValues[25],
		gruplama: parsedValues[26],
		caradr: parsedValues[27],
		gircik: parsedValues[28]
	};
}

async function kerfetchTableData(page) {
	const kerestedetayraporDTO = getfatraporDTO();
	kerestedetayraporDTO.page = page;
	kerestedetayraporDTO.pageSize = pageSize;
	currentPage = page;
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	
	document.body.style.cursor = "wait";
	const $yenileButton = $('#fatrapyenileButton');
	$yenileButton.prop('disabled', true).text('İşleniyor...');
	const mainTableBody = document.getElementById("mainTableBody");
	mainTableBody.innerHTML = "";
	clearTfoot();
	try {
		const response = await fetchWithSessionCheck("kereste/fatrapdoldur", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(kerestedetayraporDTO),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		data = response;
		let sqlHeaders = "";
		if (response.raporturu === 'fno') {
			sqlHeaders = ["", "EVRAK NO", "HAREKET", "TARIH", "CARI_HESAP", "ADRES_HESAP", "DOVIZ", "M3", "TUTAR", "ISK. TUTAR", "KDV TUTAR", "TOPLAM TUTAR"];
			updateTableHeadersfno(sqlHeaders);
		} else if (response.raporturu === 'fkodu') {
			sqlHeaders = ["CARI_HESAP", "HAREKET", "UNVAN", "VERGI NO", "M3", "TUTAR", "ISK. TUTAR", "KDV TUTAR", "TOPLAM TUTAR"];
			updateTableHeadersfkodu(sqlHeaders);
		} else if (response.raporturu === 'fnotar') {
			sqlHeaders = ["", "EVRAK NO", "HAREKET", "TARIH", "UNVAN", "VERGI NO", "M3", "TUTAR", "ISK. TUTAR", "KDV TUTAR", "TOPLAM TUTAR"];
			updateTableHeadersfnotar(sqlHeaders);
		}
		let totalmiktar = 0;
		let totaltutar = 0;
		data.data.forEach(rowData => {
			const row = document.createElement('tr');
			row.classList.add('expandable');
			row.classList.add("table-row-height");
			if (response.raporturu === 'fno') {
				row.innerHTML = `
                    <td class="toggle-button">+</td>
                    <td>${rowData.Fatura_No || ''}</td>
                    <td>${rowData.Hareket || ''}</td>
                    <td>${formatDate(rowData.Tarih)}</td>
                    <td>${rowData.Unvan || ''}</td>
                    <td>${rowData.Adres_Firma || ''}</td>
                    <td>${rowData.Doviz || ''}</td>
                    <td class="double-column">${formatNumber3(rowData.m3)}</td>
                    <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Iskontolu_Tutar)}</td>
					<td class="double-column">${formatNumber2(rowData.Kdv_Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Toplam_Tutar)}</td>
                `;
				totalmiktar += rowData.m3;
				totaltutar += rowData.Toplam_Tutar;
			}
			else if (response.raporturu === 'fkodu') {
				row.innerHTML = `
                    <td>${rowData.Firma_Kodu || ''}</td>
                    <td>${rowData.Hareket || ''}</td>
                    <td>${rowData.Unvan || ''}</td>
                    <td>${rowData.Vergi_No || ''}</td>
                    <td class="double-column">${formatNumber3(rowData.m3)}</td>
                    <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Iskontolu_Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Kdv_Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Toplam_Tutar)}</td>
                `;
				totalmiktar += rowData.m3;
				totaltutar += rowData.Toplam_Tutar;
			} if (response.raporturu === 'fnotar') {
				row.innerHTML = `
                    <td class="toggle-button">+</td>
                    <td>${rowData.Fatura_No || ''}</td>
                    <td>${rowData.Hareket || ''}</td>
                    <td>${formatDate(rowData.Tarih)}</td>
                    <td>${rowData.Unvan || ''}</td>
                    <td>${rowData.Vergi_No || ''}</td>
                    <td class="double-column">${formatNumber3(rowData.m3)}</td>
                    <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Iskontolu_Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Kdv_Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Toplam_Tutar)}</td>
                `;
				totalmiktar += rowData.m3;
				totaltutar += rowData.Toplam_Tutar;
			}
			mainTableBody.appendChild(row);
			const detailsRow = document.createElement('tr');
			detailsRow.classList.add('details-row');
			detailsRow.innerHTML = `<td colspan="24"></td>`;
			mainTableBody.appendChild(detailsRow);
			if (response.raporturu != 'fkodu') {
				row.addEventListener('click', async () => {
					const toggleButton = row.querySelector('.toggle-button');
					const isVisible = detailsRow.style.display === 'table-row';
					detailsRow.style.display = isVisible ? 'none' : 'table-row';
					toggleButton.textContent = isVisible ? '+' : '-';
					document.body.style.cursor = "wait";
					if (!isVisible) {
						try {
							const details = await fetchDetails(rowData.Fatura_No, rowData.Hareket);
							const data = details.data;
							let detailsTable = `
                                <table class="details-table table table-bordered table-hover">
                                    <thead class="thead-dark">
                                        <tr>
                                            <th>Fatura_No</th>
                                            <th>Barkod</th>
											<th>Kodu</th>
											<th>Paket_No</th>
                                            <th style="text-align: right;">Miktar</th>
											<th style="text-align: right;">m3</th>
                                            <th style="text-align: right;">Kdv</th>
											<th>Doviz</th>
                                            <th style="text-align: right;">Fiat</th>
                                            <th style="text-align: right;">Tutar</th>
											<th style="text-align: right;">Kur</th>
											<th>Cari_Firma</th>
											<th>Adres_Firma</th>
                                            <th style="text-align: right;">Iskonto</th>
											<th style="text-align: right;">Tevkifat</th>
                                            <th>Ana Grup</th>
                                            <th>Alt Grup</th>
											<th>Mensei</th>
                                            <th>Depo</th>
                                            <th>Ozel Kod</th>
                                            <th>Izahat</th>
                                            <th>Nakliyeci</th>
                                            <th>User</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                            `;
							data.forEach(item => {
								detailsTable += `
                                    <tr>
                                        <td style="min-width:80px;">${item.Fatura_No || ''}</td>
                                        <td >${item.Barkod || ''}</td>
										<td >${item.Kodu || ''}</td>
										<td >${item.Paket_No || ''}</td>
										<td style="text-align: right;min-width:80px;">${formatNumber0(item.Miktar)}</td>
										<td style="text-align: right;min-width:80px;">${formatNumber3(item.m3)}</td>
                                        <td style="text-align: right;min-width:80px;">${formatNumber2(item.Kdv)}</td>
                                        <td>${item.Doviz || ''}</td>
										<td style="text-align: right;min-width:80px;">${formatNumber2(item.Fiat)}</td>
                                        <td style="text-align: right;min-width:80px;">${formatNumber2(item.Tutar)}</td>
										<td style="text-align: right;min-width:80px;">${formatNumber2(item.Kur)}</td>
										<td>${item.Cari_Firma || ''}</td>
										<td>${item.Adres_Firma || ''}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Iskonto)}</td>
										<td style="text-align: right;">${formatNumber2(item.Tevkifat)}</td>
										<td >${item.Ana_Grup}</td>
                                        <td >${item.Alt_Grup}</td>
										<td >${item.Mensei}</td>
										<td >${item.Depo}</td>
                                        <td >${item.Ozel_Kod}</td>
										<td >${item.Izahat}</td>
										<td >${item.Nakliyeci}</td>
                                        <td >${item.USER}</td>
                                    </tr>
                                `;
							});
							detailsTable += `
                                    </tbody>
                                </table>
                            `;
							detailsRow.children[0].classList.add("table-row-height");
							detailsRow.children[0].innerHTML = detailsTable;
							document.body.style.cursor = "default";
						} catch (error) {
							detailsRow.children[0].innerHTML = `
                                <strong>Hata:</strong> Detay bilgileri alınamadı.
                            `;
							document.body.style.cursor = "default";
						}
					}
					document.body.style.cursor = "default";
				});
			}
		});
		if (response.raporturu === 'fno') {
			document.getElementById("toplam-7").innerText = formatNumber3(totalmiktar);
			document.getElementById("toplam-11").innerText = formatNumber2(totaltutar);

		}
		else if (response.raporturu === 'fkodu') {
			document.getElementById("toplam-4").innerText = formatNumber3(totalmiktar);
			document.getElementById("toplam-8").innerText = formatNumber2(totaltutar);
		}
		else if (response.raporturu === 'fnotar') {
			document.getElementById("toplam-6").innerText = formatNumber3(totalmiktar);
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

async function fetchDetails(evrakNo, cins) {
	try {
		let gircik = "";
		if (cins === "Alis") {
			gircik = "G";
		} else {
			gircik = "C";
		}
		const response = await fetchWithSessionCheck("kereste/fatdetay", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ evrakNo: evrakNo, gircik: gircik }),
		});
		data = await response;
		return await response;
	} catch (error) {
		throw error;
	}
}

function updateTableHeadersfno(headers) {
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

		if (index >= headers.length - 5) {
			th.classList.add("double-column");
		}
		trHead.appendChild(th);
	});
	thead.appendChild(trHead);
	tfoot.innerHTML = "";
	let trFoot = document.createElement("tr");
	headers.forEach((_, index) => {
		let th = document.createElement("th");
		if (index === 7) {
			th.textContent = "0.000";
			th.id = "toplam-" + index;
			th.classList.add("double-column");
		} else if (index === 11) {
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

function updateTableHeadersfkodu(headers) {
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
		if (index >= headers.length - 5) {
			th.classList.add("double-column");
		}
		trHead.appendChild(th);
	});
	thead.appendChild(trHead);
	tfoot.innerHTML = "";
	let trFoot = document.createElement("tr");
	headers.forEach((_, index) => {
		let th = document.createElement("th");
		if (index === 4) {
			th.textContent = "0.000";
			th.id = "toplam-" + index;
			th.classList.add("double-column");
		} else if (index === 8) {
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
function updateTableHeadersfnotar(headers) {
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
		if (index >= headers.length - 5) {
			th.classList.add("double-column");
		}
		trHead.appendChild(th);
	});
	thead.appendChild(trHead);
	tfoot.innerHTML = "";
	let trFoot = document.createElement("tr");
	headers.forEach((_, index) => {
		let th = document.createElement("th");
		if (index === 6) {
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

async function openenvModal(modal) {
	$(modal).modal('show');
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("kereste/anadepo", {
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
		const oz = response.oz1Kodlari;
		const anaSelect = document.getElementById("anagrp");
		const canaSelect = document.getElementById("canagrp");
		const dpoSelect = document.getElementById("depo");
		const cdpoSelect = document.getElementById("cdepo");
		const ozSelect = document.getElementById("ozkod");
		const cozSelect = document.getElementById("cozkod");
		anaSelect.innerHTML = "";
		canaSelect.innerHTML = "";
		dpoSelect.innerHTML = "";
		cdpoSelect.innerHTML = "";
		ozSelect.innerHTML = "";
		cozSelect.innerHTML = "";
		ana.forEach(item => {
			const optionAna = document.createElement("option");
			optionAna.value = item.ANA_GRUP;
			optionAna.textContent = item.ANA_GRUP;
			anaSelect.appendChild(optionAna);
			const optionUrana = document.createElement("option");
			optionUrana.value = item.ANA_GRUP;
			optionUrana.textContent = item.ANA_GRUP;
			canaSelect.appendChild(optionUrana);
		});
		dpo.forEach(item => {
			const option = document.createElement("option");
			option.value = item.DEPO;
			option.textContent = item.DEPO;
			dpoSelect.appendChild(option);

			const optioncdpo = document.createElement("option");
			optioncdpo.value = item.DEPO;
			optioncdpo.textContent = item.DEPO;
			cdpoSelect.appendChild(optioncdpo);

		});

		oz.forEach(item => {
			const optionOz = document.createElement("option");
			optionOz.value = item.OZEL_KOD_1;
			optionOz.textContent = item.OZEL_KOD_1;
			ozSelect.appendChild(optionOz);

			const optioncoz = document.createElement("option");
			optioncoz.value = item.OZEL_KOD_1;
			optioncoz.textContent = item.OZEL_KOD_1;
			cozSelect.appendChild(optioncoz);
		});

		const newOption = document.createElement("option");
		newOption.value = "Bos Olanlar";
		newOption.textContent = "Bos Olanlar";

		const newOption1 = document.createElement("option");
		newOption1.value = "Bos Olanlar";
		newOption1.textContent = "Bos Olanlar";

		const newOption2 = document.createElement("option");
		newOption2.value = "Bos Olanlar";
		newOption2.textContent = "Bos Olanlar";

		const newOption3 = document.createElement("option");
		newOption3.value = "Bos Olanlar";
		newOption3.textContent = "Bos Olanlar";

		anaSelect.insertBefore(newOption, anaSelect.options[1]);
		canaSelect.insertBefore(newOption1, canaSelect.options[1]);
		dpoSelect.insertBefore(newOption2, dpoSelect.options[1]);
		cdpoSelect.insertBefore(newOption3, cdpoSelect.options[1]);
	} catch (error) {
		const modalError = document.getElementById("errorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}

async function fatrapdownloadReport() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";

	document.body.style.cursor = "wait";
	const $indirButton = $('#indirButton');
	$indirButton.prop('disabled', true).text('İşleniyor...');
	const $yenileButton = $('#yenileButton');
	$yenileButton.prop('disabled', true);

	let table = document.querySelector("#main-table");
	let headers = [];
	let rows = [];
	table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));
	table.querySelectorAll("tbody tr").forEach(tr => {
		let rowData = {};
		let isEmpty = true;
		tr.querySelectorAll("td").forEach((td, index) => {
			let value = td.innerText.trim();
			if (value !== "") {
				isEmpty = false;
			}
			rowData[headers[index]] = value;
		});
		if (!isEmpty) {
			rows.push(rowData);
		}
	});
	try {
		const response = await fetchWithSessionCheckForDownload('kereste/fatrap_download', {
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

async function fatrapmailAt() {
	localStorage.removeItem("tableData");
	localStorage.removeItem("grprapor");
	localStorage.removeItem("tablobaslik");
	document.body.style.cursor = "wait";
	let table = document.querySelector("#main-table");
		let headers = [];
		let rows = [];
		table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));
		table.querySelectorAll("tbody tr").forEach(tr => {
			let rowData = {};
			let isEmpty = true;
			tr.querySelectorAll("td").forEach((td, index) => {
				let value = td.innerText.trim();
				if (value !== "") {
					isEmpty = false;
				}
				rowData[headers[index]] = value;
			});
			if (!isEmpty) {
				rows.push(rowData);
			}
		});
	localStorage.setItem("tableData", JSON.stringify({ rows: rows }));
	const degerler = "kerfatrapor";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}