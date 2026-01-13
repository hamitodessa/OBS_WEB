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

		const newOption = document.createElement("option");
		newOption.value = "Bos Olanlar";
		newOption.textContent = "Bos Olanlar";

		const newOption1 = document.createElement("option");
		newOption1.value = "Bos Olanlar";
		newOption1.textContent = "Bos Olanlar";

		const newOption2 = document.createElement("option");
		newOption2.value = "Bos Olanlar";
		newOption2.textContent = "Bos Olanlar";

		anaSelect.insertBefore(newOption, anaSelect.options[1]);
		uranaSelect.insertBefore(newOption1, uranaSelect.options[1]);
		dpoSelect.insertBefore(newOption2, dpoSelect.options[1]);
	} catch (error) {
		const modalError = document.getElementById("errorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}

function setSecondTableVisible(isVisible){
  const container = document.querySelector("#ara_content .container");
  const secondWrap = document.getElementById("second-table-container");

  if(isVisible){
    console.info("Showing second table");
    container.classList.add("two-tables");
    secondWrap.style.display = "block";
  }else{
    console.info("Hiding second table");
    container.classList.remove("two-tables");
    secondWrap.style.display = "none";
    document.getElementById("secondTableBody").innerHTML = "";
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
	const secondTableBody = document.getElementById("secondTableBody");
	secondTableBody.innerHTML = "";
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
		if (response.raporturu === 'normal') {
			sqlHeaders = ["KODU", "ADI", "SIMGE", "GIRIS MIKTARI", "GIRIS TUTARI", "CIKIS MIKTARI", "CIKIS TUTARI", "CIKIS MALIYET", "STOK MIKTARI", "MALIYET", "TUTAR"];
			updateTableHeadersnormal(sqlHeaders);
		} else {
			const mailButton = document.getElementById("envmailButton");
			mailButton.disabled = true;
			const reportFormat = document.getElementById("envDownloadButton");
			reportFormat.disabled = true;
			sqlHeaders = ["KODU", "ADI", "SIMGE", "GIRIS MIKTARI", "GIRIS TUTARI", "CIKIS MIKTARI", "CIKIS TUTARI", "CIKIS MALIYET", "STOK MIKTARI", "MALIYET", "TUTAR"];
			updateTableHeadersfifofirst(sqlHeaders);
			sqlHeaders = ["URUN KODU", "EVRAK NO", "HES_KODU", "EVR CINS", "TARIH", "MIKTAR", "BIRIM", "FIAT", "MIKTAR BAKIYE", "TUTAR", "DOVIZ", "_TUTAR", "TUTAR BAKIYE", "USER"];
			updateTableHeadersfifo(sqlHeaders);
		}
		let totalmiktar = 0;
		let totalcmiktar = 0;
		let totalgtutar = 0;
		let totaltutar = 0;
		let totalstok = 0;
		let totalctutar = 0;
		console.info("Rapor Turu: " + response.raporturu);
		if (response.raporturu === 'normal') {
			setSecondTableVisible(false);
			data.data.forEach(rowData => {
				const row = document.createElement('tr');
				row.classList.add('expandable');
				row.classList.add("table-row-height");
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
				mainTableBody.appendChild(row);
			});
		}
		else if (response.raporturu === 'fifo') {
			setSecondTableVisible(true);
			data.fifo.forEach(rowData => {
				const row = document.createElement('tr');
				row.classList.add('expandable');
				row.classList.add("table-row-height");
				row.innerHTML = `
                    <td>${rowData.Urun_Kodu || ''}</td>
                    <td>${rowData.Adi || ''}</td>
                    <td>${rowData.Simge || ''}</td>
                    <td class="double-column">${formatNumber3(rowData.Giris_Miktari)}</td>
					<td class="double-column">${formatNumber2(rowData.Giris_Tutar)}</td>
					<td class="double-column">${formatNumber3(rowData.Cikis_Miktari)}</td>
					<td class="double-column">${formatNumber2(rowData.Cikis_Tutar)}</td>
					<td class="double-column">${formatNumber2(rowData.Cikis_Maliyet)}</td>
                    <td class="double-column">${formatNumber3(rowData.Stok_Miktari)}</td>
                    <td class="double-column">${formatNumber2(rowData.Maliyet)}</td>
                    <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
                `;
				mainTableBody.appendChild(row);
			});
			/// 2 Tablo Doldur///////////////////////////////////////////////////////////////////////
			data.fifo2.forEach(rowData => {
			  const row = document.createElement('tr');
			  row.classList.add('expandable', 'table-row-height');

			  // önce normal kolonları bas
			  row.innerHTML = `
			    <td>${rowData.Urun_Kodu || ''}</td>
			    <td>${rowData.Evrak_No || ''}</td>
			    <td>${rowData.Hes_Kodu || ''}</td> 
			    <td>${rowData.Evrak_Cins || ''}</td> 
			    <td>${formatDate(rowData.Tarih)}</td>
			    <td class="double-column">${formatNumber2(rowData.Fiat)}</td>
			    <td>${rowData.Birim || ''}</td> 
			    <td class="double-column">${formatNumber3(rowData.Miktar_Bakiye)}</td>
			    <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
			    <td>${rowData.Doviz || ''}</td> 
			    <td class="double-column">${formatNumber2(findTutarField(rowData) || 0)}</td>
			    <td class="double-column">${formatNumber2(rowData.Tutar_Bakiye)}</td>
			    <td>${rowData.USER || ''}</td> 
			  `;

			  // sonra MIKTAR hücresini doğru yere ekle (TARIH'ten sonra, FIAT'tan önce)
			  const miktarCell = document.createElement('td');
			  miktarCell.classList.add('double-column');
			  miktarCell.textContent = formatNumber3(rowData.Miktar);

			  if (rowData.Miktar < 0) {
			    miktarCell.style.backgroundColor = 'red';
			    miktarCell.style.color = 'white';
			  }

			  const fiatTd = row.querySelector("td:nth-child(6)"); // FIAT (1-based)
			  row.insertBefore(miktarCell, fiatTd);

			  secondTableBody.appendChild(row);
			});

		}
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

function findTutarField(rowData) {
	for (let key in rowData) {
		if (key.includes("_Tutar")) {
			return rowData[key];
		}
	}
	return null;
}

function clearTfoot() {
  const table = document.querySelector("#main-table");
  const tfoot = table.querySelector("tfoot");
  if (!tfoot) return;

  tfoot.querySelectorAll("td").forEach(td => td.textContent = "");
}


function updateTableHeadersnormal(headers) {
  const table = document.querySelector("#main-table");
  const thead = table.querySelector("thead");
  let tfoot = table.querySelector("tfoot");

  if (!tfoot) {
    tfoot = document.createElement("tfoot");
    table.appendChild(tfoot);
  }

  // ===== THEAD =====
  thead.innerHTML = "";
  const trHead = document.createElement("tr");
  headers.forEach((header, index) => {
    const th = document.createElement("th");
    th.textContent = header;
    if (index >= headers.length - 8) th.classList.add("double-column");
    trHead.appendChild(th);
  });
  thead.appendChild(trHead);

  // ===== TFOOT =====
  tfoot.innerHTML = "";
  const trFoot = document.createElement("tr");

  headers.forEach((_, index) => {
    const td = document.createElement("td"); // <-- TH DEĞİL, TD!

    td.id = "toplam-" + index;

    // numeric kolonlar
    if ([3, 5, 8].includes(index)) {
      td.textContent = "0.000";
      td.classList.add("double-column");
    } else if ([4, 6, 10].includes(index)) {
      td.textContent = "0.00";
      td.classList.add("double-column");
    } else {
      td.textContent = "";
    }

    trFoot.appendChild(td);
  });

  tfoot.appendChild(trFoot);
}


function updateTableHeadersfifofirst(headers) {

  const table = document.querySelector("#main-table");
  const thead = table.querySelector("thead");
  let tfoot = table.querySelector("tfoot");

  if (!tfoot) {
    tfoot = document.createElement("tfoot");
    table.appendChild(tfoot);
  }

  // ===== THEAD =====
  thead.innerHTML = "";
  const trHead = document.createElement("tr");
  headers.forEach((header, index) => {
    const th = document.createElement("th");
    th.textContent = header;

    if (index >= headers.length - 8) th.classList.add("double-column");
    trHead.appendChild(th);
  });
  thead.appendChild(trHead);

  // ===== TFOOT =====
  tfoot.innerHTML = "";
  const trFoot = document.createElement("tr");

  headers.forEach((_, index) => {
    const td = document.createElement("td"); // <-- TD

    td.textContent = "";
    td.id = "toplam-" + index; // istersen kalsın (sonra doldurursun)
    trFoot.appendChild(td);
  });

  tfoot.appendChild(trFoot);
}

function updateTableHeadersfifo(headers) {

  const table = document.querySelector("#second-table");
  const thead = table.querySelector("thead");
  let tfoot = table.querySelector("tfoot");

  if (!tfoot) {
    tfoot = document.createElement("tfoot");
    table.appendChild(tfoot);
  }

  // ===== THEAD =====
  thead.innerHTML = "";
  const trHead = document.createElement("tr");

  headers.forEach((header, index) => {
    const th = document.createElement("th");
    th.textContent = header;

    if (index === 5 || index === 7 || index === 8 || 
        index === 9 || index === 11 || index === 12) {
      th.classList.add("double-column");
    }

    trHead.appendChild(th);
  });

  thead.appendChild(trHead);

  // ===== TFOOT =====
  tfoot.innerHTML = "";
  const trFoot = document.createElement("tr");

  headers.forEach((_, index) => {
    const td = document.createElement("td"); // <-- TD kullanıyoruz

    td.id = "second-toplam-" + index;
    td.textContent = "";

    if (index === 5 || index === 7 || index === 8 ||
        index === 9 || index === 11 || index === 12) {
      td.classList.add("double-column");
    }

    trFoot.appendChild(td);
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
	localStorage.removeItem("tableData");
	localStorage.removeItem("grprapor");
	localStorage.removeItem("tablobaslik");
	document.body.style.cursor = "wait";
	let rows = extractTableData("main-table");
	localStorage.setItem("tableData", JSON.stringify({ rows: rows }));
	const degerler = "envanter";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}

function extractTableData(tableId) {
  const table = document.querySelector(`#${tableId}`);
  const headers = [];
  const rows = [];

  table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));

  table.querySelectorAll("tbody tr").forEach(tr => {
    const rowData = {};
    let nonEmptyCount = 0;

    tr.querySelectorAll("td").forEach((td, index) => {
      const value = td.innerText.trim();
      if (value !== "") nonEmptyCount++;
      rowData[headers[index]] = value;
    });

    if (nonEmptyCount > 0) rows.push(rowData);
  });

  const tfoot = table.querySelector("tfoot");
  if (tfoot) {
    const tfootRowData = {};
    let nonEmptyCount = 0;

    tfoot.querySelectorAll("td").forEach((td, index) => {   // <-- TD!
      const value = td.innerText.trim();
      if (value !== "") nonEmptyCount++;
      tfootRowData[headers[index]] = value;
    });

    if (nonEmptyCount > 0) rows.push(tfootRowData);
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