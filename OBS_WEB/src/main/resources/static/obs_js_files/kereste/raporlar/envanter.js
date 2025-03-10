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

async function kerenvanterdoldur() {
    //toplampagesize();
    envanterfetchTableData();
}

function getKeresteDetayRaporDTO() {
    const hiddenFieldValue = $('#envanterBilgi').val();
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
        gruplama: parsedValues[26]
    };
}

async function envanterfetchTableData() {
    const kerestedetayraporDTO = getKeresteDetayRaporDTO();
  
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    document.body.style.cursor = "wait";
    const $yenileButton = $('#envanteryenileButton');
    $yenileButton.prop('disabled', true).text('İşleniyor...');
    const mainTableBody = document.getElementById("mainTableBody");
    mainTableBody.innerHTML = "";
    clearTfoot();
    try {
        const response = await fetchWithSessionCheck("kereste/kerenvanter", {
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
        if (response.raporturu === 'Urun Kodu') {
            sqlHeaders = ["KODU", "GIRIS MIKTAR", "GIRIS M3", "GIRIS TUTAR", "CIKIS MIKTAR", "CIKIS M3", "CIKIS TUTAR", "STOK M3", "ORT FIAT", "STOK TUTAR"];
            updateTableHeaderskodu(sqlHeaders);
        } else if (response.raporturu === 'Konsimento') {
            sqlHeaders = ["KONSIMENTO","ACIKLAMA" ,"GIRIS MIKTAR", "GIRIS M3", "GIRIS TUTAR", "CIKIS MIKTAR", "CIKIS M3", "CIKIS TUTAR", "STOK M3", "ORT FIAT", "STOK TUTAR"];
            updateTableHeaders(sqlHeaders);
        } else if (response.raporturu === 'Hesap-Kodu') {
            sqlHeaders = ["CARI FIRMA","UNVAN" ,"GIRIS MIKTAR", "GIRIS M3", "GIRIS TUTAR", "CIKIS MIKTAR", "CIKIS M3", "CIKIS TUTAR", "STOK M3", "ORT FIAT", "STOK TUTAR"];
            updateTableHeaders(sqlHeaders);
        } else if (response.raporturu === 'Ana_Grup-Alt_Grup') {
            sqlHeaders = ["ANA GRUP","ALT GRUP" ,"GIRIS MIKTAR", "GIRIS M3", "GIRIS TUTAR", "CIKIS MIKTAR", "CIKIS M3", "CIKIS TUTAR", "STOK M3", "ORT FIAT", "STOK TUTAR"];
            updateTableHeaders(sqlHeaders);
        }
        let totalgmiktar = 0;
        let totalgm3 = 0;
        let totalgtutar = 0;
        let totalcmiktar = 0;
        let totalcm3 = 0;
        let totalctutar = 0;
        let totalstokm3 = 0;
		let totaltutar = 0;

        console.log(data);
		data.data.forEach(rowData => {
			const row = document.createElement('tr');
			row.classList.add('expandable');
			row.classList.add("table-row-height");
			if (response.raporturu === 'Urun Kodu') {
				row.innerHTML = `
                    <td>${rowData.Kodu || ''}</td>
                    <td class="double-column">${formatNumber0(rowData.Giris_Miktar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Giris_m3)}</td>       
                    <td class="double-column">${formatNumber2(rowData.Giris_Tutar)}</td>
                    <td class="double-column">${formatNumber0(rowData.Cikis_Miktar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Cikis_m3)}</td>
                    <td class="double-column">${formatNumber2(rowData.Cikis_Tutar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Stok_M3)}</td>
                    <td class="double-column">${formatNumber2(rowData.Ort_Fiat)}</td>
                    <td class="double-column">${formatNumber2(rowData.Stok_Tutar)}</td>
                `;
				totalgmiktar += rowData.Giris_Miktar;
                totalgm3 += rowData.Giris_m3;
                totalgtutar += rowData.Giris_Tutar;
                totalcmiktar += rowData.Cikis_Miktar;
                totalcm3 += rowData.Cikis_m3;
                totalctutar += rowData.Cikis_Tutar;
                totalstokm3 += rowData.Stok_M3;
                totaltutar += rowData.Stok_Tutar;
			}
            else if (response.raporturu === 'Konsimento') {
				row.innerHTML = `
                    <td>${rowData.Konsimento || ''}</td>
                    <td>${rowData.Aciklama || ''}</td>
                    <td class="double-column">${formatNumber0(rowData.Giris_Miktar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Giris_m3)}</td>       
                    <td class="double-column">${formatNumber2(rowData.Giris_Tutar)}</td>
                    <td class="double-column">${formatNumber0(rowData.Cikis_Miktar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Cikis_m3)}</td>
                    <td class="double-column">${formatNumber2(rowData.Cikis_Tutar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Stok_M3)}</td>
                    <td class="double-column">${formatNumber2(rowData.Ort_Fiat)}</td>
                    <td class="double-column">${formatNumber2(rowData.Stok_Tutar)}</td>
                `;
				totalgmiktar += rowData.Giris_Miktar;
                totalgm3 += rowData.Giris_m3;
                totalgtutar += rowData.Giris_Tutar;
                totalcmiktar += rowData.Cikis_Miktar;
                totalcm3 += rowData.Cikis_m3;
                totalctutar += rowData.Cikis_Tutar;
                totalstokm3 += rowData.Stok_M3;
                totaltutar += rowData.Stok_Tutar;
			}
            else if (response.raporturu === 'Hesap-Kodu') {
				row.innerHTML = `
                    <td>${rowData.Cari_Firma || ''}</td>
                    <td>${rowData.Unvan || ''}</td>
                    <td class="double-column">${formatNumber0(rowData.Giris_Miktar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Giris_m3)}</td>       
                    <td class="double-column">${formatNumber2(rowData.Giris_Tutar)}</td>
                    <td class="double-column">${formatNumber0(rowData.Cikis_Miktar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Cikis_m3)}</td>
                    <td class="double-column">${formatNumber2(rowData.Cikis_Tutar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Stok_M3)}</td>
                    <td class="double-column">${formatNumber2(rowData.Ort_Fiat)}</td>
                    <td class="double-column">${formatNumber2(rowData.Stok_Tutar)}</td>
                `;
				totalgmiktar += rowData.Giris_Miktar;
                totalgm3 += rowData.Giris_m3;
                totalgtutar += rowData.Giris_Tutar;
                totalcmiktar += rowData.Cikis_Miktar;
                totalcm3 += rowData.Cikis_m3;
                totalctutar += rowData.Cikis_Tutar;
                totalstokm3 += rowData.Stok_M3;
                totaltutar += rowData.Stok_Tutar;
			}
            else if (response.raporturu === 'Ana_Grup-Alt_Grup') {
				row.innerHTML = `
                    <td>${rowData.Ana_Grup || ''}</td>
                    <td>${rowData.Alt_Grup || ''}</td>
                    <td class="double-column">${formatNumber0(rowData.Giris_Miktar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Giris_m3)}</td>       
                    <td class="double-column">${formatNumber2(rowData.Giris_Tutar)}</td>
                    <td class="double-column">${formatNumber0(rowData.Cikis_Miktar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Cikis_m3)}</td>
                    <td class="double-column">${formatNumber2(rowData.Cikis_Tutar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Stok_M3)}</td>
                    <td class="double-column">${formatNumber2(rowData.Ort_Fiat)}</td>
                    <td class="double-column">${formatNumber2(rowData.Stok_Tutar)}</td>
                `;
				totalgmiktar += rowData.Giris_Miktar;
                totalgm3 += rowData.Giris_m3;
                totalgtutar += rowData.Giris_Tutar;
                totalcmiktar += rowData.Cikis_Miktar;
                totalcm3 += rowData.Cikis_m3;
                totalctutar += rowData.Cikis_Tutar;
                totalstokm3 += rowData.Stok_M3;
                totaltutar += rowData.Stok_Tutar;
			}
            mainTableBody.appendChild(row);
        });
        if (response.raporturu === 'Urun Kodu') {
			document.getElementById("toplam-1").innerText = formatNumber0(totalgmiktar);
            document.getElementById("toplam-2").innerText = formatNumber3(totalgm3);
            document.getElementById("toplam-3").innerText = formatNumber2(totalgtutar);
            document.getElementById("toplam-4").innerText = formatNumber0(totalcmiktar);
            document.getElementById("toplam-5").innerText = formatNumber3(totalcm3);
            document.getElementById("toplam-6").innerText = formatNumber2(totalctutar);
            document.getElementById("toplam-7").innerText = formatNumber3(totalstokm3);
			document.getElementById("toplam-9").innerText = formatNumber2(totaltutar);
		}else{
            document.getElementById("toplam-2").innerText = formatNumber0(totalgmiktar);
            document.getElementById("toplam-3").innerText = formatNumber3(totalgm3);
            document.getElementById("toplam-4").innerText = formatNumber2(totalgtutar);
            document.getElementById("toplam-5").innerText = formatNumber0(totalcmiktar);
            document.getElementById("toplam-6").innerText = formatNumber3(totalcm3);
            document.getElementById("toplam-7").innerText = formatNumber2(totalctutar);
            document.getElementById("toplam-8").innerText = formatNumber3(totalstokm3);
            document.getElementById("toplam-10").innerText = formatNumber2(totaltutar);
        }
     } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = error;
    } finally {
        $yenileButton.prop('disabled', false).text('Yenile');
        document.body.style.cursor = "default";
    }
}

function updateTableHeaderskodu(headers) {
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

		if (index >= headers.length - 9) {
			th.classList.add("double-column");
		}
		trHead.appendChild(th);
	});
	thead.appendChild(trHead);
	tfoot.innerHTML = "";
	let trFoot = document.createElement("tr");
	headers.forEach((_, index) => {
		let th = document.createElement("th");
        if (index === 1 || index === 4) {
			th.textContent = "0";
			th.id = "toplam-" + index;
			th.classList.add("double-column");
		} else if (index === 2 || index === 5 || index === 7) {
			th.textContent = "0.000";
			th.id = "toplam-" + index;
			th.classList.add("double-column");
		} else if (index === 3 || index === 6 || index === 9) {
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

function updateTableHeaders(headers) {
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

		if (index >= headers.length - 9) {
			th.classList.add("double-column");
		}
		trHead.appendChild(th);
	});
	thead.appendChild(trHead);
	tfoot.innerHTML = "";
	let trFoot = document.createElement("tr");
	headers.forEach((_, index) => {
		let th = document.createElement("th");
        if (index === 2 || index === 5) {
			th.textContent = "0";
			th.id = "toplam-" + index;
			th.classList.add("double-column");
		} else if (index === 3 || index === 6 || index === 8) {
			th.textContent = "0.000";
			th.id = "toplam-" + index;
			th.classList.add("double-column");
		} else if (index === 4 || index === 7 || index === 10) {
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
