currentPage = 0;
totalPages = 0;
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

function ilksayfa() {
	fatfetchTableData(0);
}

function oncekisayfa() {
	if (currentPage > 0) {
		fatfetchTableData(currentPage - 1);
	}
}

function sonrakisayfa() {
	if (currentPage < totalPages - 1) {
		fatfetchTableData(currentPage + 1);
	}
}

async function sonsayfa() {
	fatfetchTableData(totalPages - 1);
}

async function toplampagesize() {
	try {
		const errorDiv = document.getElementById("errorDiv");
		errorDiv.style.display = "none";
		errorDiv.innerText = "";
		const fatraporDTO = getfatraporDTO();
		const response = await fetchWithSessionCheck("stok/fatdoldursize", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(fatraporDTO),
		});
		const totalRecords = response.totalRecords;
		totalPages = Math.ceil(totalRecords / pageSize);
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error;
		document.body.style.cursor = "default";
	}
}

async function fatdoldur() {
	document.body.style.cursor = "wait";
	toplampagesize();
	fatfetchTableData(0);
}

function getfatraporDTO() {
	const hiddenFieldValue = $('#fatrapBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	return {
		fatno1: parsedValues[0],
		fatno2: parsedValues[1],
		anagrp: parsedValues[2],
		tar1: parsedValues[3],
		tar2: parsedValues[4],
		altgrp: parsedValues[5],
		ckod1: parsedValues[6],
		ckod2: parsedValues[7],
		depo: parsedValues[8],
		adr1: parsedValues[9],
		adr2: parsedValues[10],
		turu: parsedValues[11],
		ukod1: parsedValues[12],
		ukod2: parsedValues[13],
		tev1: parsedValues[14],
		tev2: parsedValues[15],
		okod1: parsedValues[16],
		okod2: parsedValues[17],
		gruplama: parsedValues[18],
		dvz1: parsedValues[19],
		dvz2: parsedValues[20],
		caradr: parsedValues[21]
	};
}

async function fatfetchTableData(page) {
	const fatraporDTO = getfatraporDTO();
	fatraporDTO.page = page;
	fatraporDTO.pageSize = pageSize;
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
		const response = await fetchWithSessionCheck("stok/fatrapdoldur", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(fatraporDTO),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		data = response;
		let sqlHeaders = "";
		if (response.raporturu === 'fno') {
			sqlHeaders = ["", "FATURA NO", "HAREKET", "TARIH", "CARI_HESAP", "ADRES_HESAP", "DOVIZ", "MIKTAR", "TUTAR", "ISK. TUTAR"];
			updateTableHeadersfno(sqlHeaders);
		} else if (response.raporturu === 'fkodu') {
			sqlHeaders = ["FATURA NO", "HAREKET", "UNVAN", "VERGI NO", "MIKTAR", "TUTAR", "ISK. TUTAR", "KDV TUTAR", "TOPLAM TUTAR"];
			updateTableHeadersfkodu(sqlHeaders);
		} else if (response.raporturu === 'fnotar') {
			sqlHeaders = ["", "FATURA NO", "HAREKET", "TARIH", "UNVAN", "VERGI NO", "MIKTAR", "TUTAR", "ISK. TUTAR", "KDV TUTAR", "TOPLAM TUTAR"];
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
                    <td>${rowData.Cari_Firma || ''}</td>
                    <td>${rowData.Adres_Firma || ''}</td>
                    <td>${rowData.Doviz || ''}</td>
                    <td class="double-column">${formatNumber3(rowData.Miktar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Iskontolu_Tutar)}</td>
                `;
				totalmiktar += rowData.Miktar;
				totaltutar += rowData.Iskontolu_Tutar;
			}
			else if (response.raporturu === 'fkodu') {
				row.innerHTML = `
                    
                    <td>${rowData.Fatura_No || ''}</td>
                    <td>${rowData.Hareket || ''}</td>
                    <td>${rowData.Unvan || ''}</td>
                    <td>${rowData.Vergi_No || ''}</td>
                    <td class="double-column">${formatNumber3(rowData.Miktar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Iskontolu_Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Kdv_Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Toplam_Tutar)}</td>
                `;
				totalmiktar += rowData.Miktar;
				totaltutar += rowData.Toplam_Tutar;
			} if (response.raporturu === 'fnotar') {
				row.innerHTML = `
                    <td class="toggle-button">+</td>
                    <td>${rowData.Fatura_No || ''}</td>
                    <td>${rowData.Hareket || ''}</td>
                    <td>${formatDate(rowData.Tarih)}</td>
                    <td>${rowData.Unvan || ''}</td>
                    <td>${rowData.Vergi_No || ''}</td>
                    <td class="double-column">${formatNumber3(rowData.Miktar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Iskontolu_Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Kdv_Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Toplam_Tutar)}</td>
                `;
				totalmiktar += rowData.Miktar;
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
                                            <th>Kodu</th>
                                            <th>Adi</th>
                                            <th style="text-align: right;">Miktar</th>
                                            <th>Birim</th>
                                            <th style="text-align: right;">Fiat</th>
                                            <th>Doviz</th>
                                            <th style="text-align: right;">Tutar</th>
                                            <th style="text-align: right;">Iskonto</th>
                                            <th style="text-align: right;">Iskonto_Tutar</th>
                                            <th style="text-align: right;">Iskontolu_Tutar</th>
                                            <th style="text-align: right;">Kdv</th>
                                            <th style="text-align: right;">Kdv Tutar</th>
                                            <th style="text-align: right;">Tevkifat</th>
                                            <th style="text-align: right;">Tev Edi.Kdv.</th>
                                            <th style="text-align: right;">Tev Dah Top. Tut.</th>
                                            <th style="text-align: right;">Beyan Edilen Kdv</th>
                                            <th style="text-align: right;">Tev Har TopTutar</th>
                                            <th>Ana Grup</th>
                                            <th>Alt Grup</th>
                                            <th>Depo</th>
                                            <th>Ozel Kod</th>
                                            <th>Izahat</th>
                                            <th>Hareket</th>
                                            <th>User</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                            `;
							data.forEach(item => {
								detailsTable += `
                                    <tr>
                                        <td style="min-width:80px;">${item.Kodu || ''}</td>
                                        <td style="min-width:250px;">${item.Adi || ''}</td>
                                        <td style="text-align: right;min-width:80px;">${formatNumber3(item.Miktar)}</td>
                                        <td>${item.Birim || ''}</td>
                                        <td style="text-align: right;min-width:80px;">${formatNumber2(item.Fiat)}</td>
                                        <td>${item.Doviz || ''}</td>
                                        <td style="text-align: right;min-width:80px;">${formatNumber2(item.Tutar)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Iskonto)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Iskonto_Tutar)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Iskontolu_Tutar)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Kdv)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Kdv_Tutar)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Tevkifat)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Tev_Edilen_KDV)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Tev_Dah_Top_Tutar)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Beyan_Edilen_KDV)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Tev_Har_Top_Tutar)}</td>
                                        <td >${item.Ana_Grup}</td>
                                        <td >${item.Alt_Grup}</td>
                                        <td >${item.Depo}</td>
                                        <td >${item.Ozel_Kod}</td>
                                        <td >${item.Izahat}</td>
                                        <td >${item.Hareket}</td>
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
			document.getElementById("toplam-9").innerText = formatNumber2(totaltutar);

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
		const response = await fetchWithSessionCheck("stok/fatdetay", {
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

		if (index >= headers.length - 3) {
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
		} else if (index === 9) {
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

async function openfatrapModal(modal) {
  $(modal).modal('show');
  document.body.style.cursor = "wait";
  try {
    const response = await fetchWithSessionCheck("stok/anadepo", {
      method: "POST",
      headers: { "Content-Type": "application/json" }
    });
    if (response.errorMessage) throw new Error(response.errorMessage);

    const ana = response.anaKodlari || [];
    const dpo = response.depoKodlari || [];

    const anaSelect = document.getElementById("anagrp");
    const dpoSelect = document.getElementById("depo");
    anaSelect.innerHTML = "";
    dpoSelect.innerHTML = "";

    const fillSelect = (selectEl, arr, key) => {
      selectEl.add(new Option("", ""));
      selectEl.add(new Option("Bos Olanlar", "Bos Olanlar"));
      const seen = new Set();
      for (const it of arr) {
        const v = (it[key] || "").trim();
        if (!v || seen.has(v)) continue;
        seen.add(v);
        selectEl.add(new Option(v, v));
      }
    };
    fillSelect(anaSelect, ana, "ANA_GRUP");
    fillSelect(dpoSelect, dpo, "DEPO");
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
		const response = await fetchWithSessionCheckForDownload('stok/fatrap_download', {
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
	const degerler = "fatrapor";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}