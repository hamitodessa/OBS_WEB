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

async function stokfetchTableData() {
    const hiddenFieldValue = $('#stokBilgi').val();
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
        oncekitarih: parsedValues[12],
        depohardahil: parsedValues[13],
        uretfisdahil: parsedValues[14],
    };
    const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
    document.body.style.cursor = "wait";
    const $yenileButton = $('#stokyenileButton');
    $yenileButton.prop('disabled', true).text('İşleniyor...');
    const mainTableBody = document.getElementById("mainTableBody");
    mainTableBody.innerHTML = "";
    clearTfoot();
    try {
        const response = await fetchWithSessionCheck("stok/stokdoldur", {
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
        if (response.raporturu === 'stokkodu') {
            sqlHeaders = ["KODU", "ADI", "SIMGE", "GIRIS MIKTARI", "GIRIS AGIRLIK", "CIKIS MIKTARI", "CIKIS AGIRLIK", "STOK MIKTARI", "STOK AGIRLIK"];
            updateTableHeadersstokkodu(sqlHeaders);
        } else if (response.raporturu === 'stokkoduonceki') {
            sqlHeaders = ["KODU", "ADI", "SIMGE", "ONCEKI BAKIYE", "PERY. GIRIS AGIRLIK", "PERY. CIKIS AGIRLIK", "PERY. STOK AGIRLIK", "BAKIYE"];
            updateTableHeadersstokkoduonceki(sqlHeaders);
        } else if (response.raporturu === 'anaalt') {
            sqlHeaders = ["ANA GRUP", "ALT GRUP", "GIRIS MIKTARI", "GIRIS AGIRLIK", "CIKIS MIKTARI", "CIKIS AGIRLIK", "STOK MIKTARI", "STOK AGIRLIK"];
            updateTableHeadersanaalt(sqlHeaders);
        }
        let totalmiktar = 0;
        let totalgagirlik = 0;
        let totalcmiktar = 0;
        let totalcagirlik = 0;
        let totalstokmik = 0;
        let totalstokagi = 0;
        if (response.raporturu === 'stokkodu') {
            data.data.forEach(rowData => {
                const row = document.createElement('tr');
                row.classList.add('expandable');
                row.classList.add("table-row-height");
                row.innerHTML = `
                    <td>${rowData.Kodu || ''}</td>
                    <td>${rowData.Adi || ''}</td>
                    <td>${rowData.Simge || ''}</td>
                    <td class="double-column">${formatNumber3(rowData.Giris_Miktari)}</td>
					<td class="double-column">${formatNumber3(rowData.Giris_Agirlik)}</td>
					<td class="double-column">${formatNumber3(rowData.Cikis_Miktari)}</td>
					<td class="double-column">${formatNumber3(rowData.Cikis_Agirlik)}</td>
					<td class="double-column">${formatNumber3(rowData.Stok_Miktari)}</td>
                    <td class="double-column">${formatNumber3(rowData.Stok_Agirlik)}</td>
                `;
                totalmiktar += rowData.Giris_Miktari;
                totalgagirlik += rowData.Giris_Agirlik;
                totalcmiktar += rowData.Cikis_Miktari;
                totalcagirlik += rowData.Cikis_Agirlik;
                totalstokmik += rowData.Stok_Miktari;
                totalstokagi += rowData.Stok_Agirlik;
                mainTableBody.appendChild(row);
            });
        }
        else if (response.raporturu === 'stokkoduonceki') {

            data.data.forEach(rowData => {
                const row = document.createElement('tr');
                row.classList.add('expandable');
                row.classList.add("table-row-height");
                row.innerHTML = `
                    <td>${rowData.Kodu || ''}</td>
                    <td>${rowData.Adi || ''}</td>
                    <td>${rowData.Simge || ''}</td>
                    <td class="double-column">${formatNumber3(rowData.Onceki_Bakiye)}</td>
					<td class="double-column">${formatNumber3(rowData.Periyot_Giris_Agirlik)}</td>
					<td class="double-column">${formatNumber3(rowData.Periyot_Cikis_Agirlik)}</td>
					<td class="double-column">${formatNumber3(rowData.Periyot_Stok_Agirlik)}</td>
                    <td class="double-column">${formatNumber3(rowData.BAKIYE)}</td>
                 `;
                totalmiktar += rowData.Onceki_Bakiye;
                totalgagirlik += rowData.Periyot_Giris_Agirlik;
                totalcmiktar += rowData.Periyot_Cikis_Agirlik;
                totalcagirlik += rowData.Periyot_Stok_Agirlik;
                totalstokmik += rowData.BAKIYE;
                mainTableBody.appendChild(row);
            });
        }
        else if (response.raporturu === 'anaalt') {
            data.data.forEach(rowData => {
                const row = document.createElement('tr');
                row.classList.add('expandable');
                row.classList.add("table-row-height");
                row.innerHTML = `
                    <td>${rowData.ANA_GRUP || ''}</td>
                    <td>${rowData.ALT_GRUP || ''}</td>
                    <td class="double-column">${formatNumber3(rowData.Giris_Miktar)}</td>
					<td class="double-column">${formatNumber3(rowData.Giris_Agirlik)}</td>
					<td class="double-column">${formatNumber3(rowData.Cikis_Miktar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Cikis_Agirlik)}</td>
                    <td class="double-column">${formatNumber3(rowData.Stok_Miktar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Stok_Agirlik)}</td>
                `;
                totalmiktar += rowData.Giris_Miktar;
                totalgagirlik += rowData.Giris_Agirlik;
                totalcmiktar += rowData.Cikis_Miktar;
                totalcagirlik += rowData.Cikis_Agirlik;
                totalstokmik += rowData.Stok_Miktar;
                totalstokagi += rowData.Stok_Agirlik;
                mainTableBody.appendChild(row);
            });
        }
        if (response.raporturu === 'stokkodu') {
            document.getElementById("toplam-3").innerText = formatNumber3(totalmiktar);
            document.getElementById("toplam-4").innerText = formatNumber3(totalgagirlik);
            document.getElementById("toplam-5").innerText = formatNumber3(totalcmiktar);
            document.getElementById("toplam-6").innerText = formatNumber3(totalcagirlik);
            document.getElementById("toplam-7").innerText = formatNumber3(totalstokmik);
            document.getElementById("toplam-8").innerText = formatNumber3(totalstokagi);
        } else if (response.raporturu === 'stokkodu') {
            document.getElementById("toplam-3").innerText = formatNumber3(totalmiktar);
            document.getElementById("toplam-4").innerText = formatNumber3(totalgagirlik);
            document.getElementById("toplam-5").innerText = formatNumber3(totalcmiktar);
            document.getElementById("toplam-6").innerText = formatNumber3(totalcagirlik);
            document.getElementById("toplam-7").innerText = formatNumber3(totalstokmik);
        } else if (response.raporturu === 'anaalt') {
            document.getElementById("toplam-2").innerText = formatNumber3(totalmiktar);
            document.getElementById("toplam-3").innerText = formatNumber3(totalgagirlik);
            document.getElementById("toplam-4").innerText = formatNumber3(totalcmiktar);
            document.getElementById("toplam-5").innerText = formatNumber3(totalcagirlik);
            document.getElementById("toplam-6").innerText = formatNumber3(totalstokmik);
            document.getElementById("toplam-7").innerText = formatNumber3(totalstokagi);
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

function updateTableHeadersstokkodu(headers) {
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
        if (index >= headers.length - 6) {
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
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else if (index === 5) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else if (index === 6) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else if (index === 7) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else if (index === 8) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else {
            th.textContent = "";
        }
        trFoot.appendChild(th);
    });
    tfoot.appendChild(trFoot);
}


function updateTableHeadersstokkoduonceki(headers) {
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
        if (index === 3) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else if (index === 4) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else if (index === 5) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else if (index === 6) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else if (index === 7) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else {
            th.textContent = "";
        }
        trFoot.appendChild(th);
    });
    tfoot.appendChild(trFoot);
}

function updateTableHeadersanaalt(headers) {
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
        if (index >= headers.length - 6) {
            th.classList.add("double-column");
        }
        trHead.appendChild(th);
    });
    thead.appendChild(trHead);
    tfoot.innerHTML = "";
    let trFoot = document.createElement("tr");
    headers.forEach((_, index) => {
        let th = document.createElement("th");
        if (index === 2) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else if (index === 3) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else if (index === 4) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else if (index === 5) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else if (index === 6) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else if (index === 7) {
            th.textContent = "0.000";
            th.id = "toplam-" + index;
            th.classList.add("double-column");
        } else {
            th.textContent = "";
        }
        trFoot.appendChild(th);
    });
    tfoot.appendChild(trFoot);

}


async function stokdownloadReport() {
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    document.body.style.cursor = "wait";
    const $indirButton = $('#stokrapreportDownload');
    $indirButton.prop('disabled', true).text('İşleniyor...');
    const $yenileButton = $('#stokyenileButton');
    $yenileButton.prop('disabled', true);
    let rows = extractTableData("main-table");
    try {
        const response = await fetchWithSessionCheckForDownload('stok/stk_download', {
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

async function stokmailAt() {
    document.body.style.cursor = "wait";
    let rows = extractTableData("main-table");
    localStorage.setItem("tableData", JSON.stringify({ rows: rows }));
    const degerler = "stok";
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

function gruplamaChanged() {
    const gruplama = document.getElementById("gruplama").value;
    const oncekitarih = document.getElementById("oncekitarih"); // Checkbox'ı al
    if (gruplama === "Urun Kodu") {
        oncekitarih.disabled = false; // Checkbox'ı aktif yap
    } else {
        oncekitarih.disabled = true; // Checkbox'ı devre dışı bırak
    }
}
