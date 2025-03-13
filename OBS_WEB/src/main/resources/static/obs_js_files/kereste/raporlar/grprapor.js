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
        const oz1 = response.oz1Kodlari;
        const dpo = response.depoKodlari;

        const dpoSelect = document.getElementById("depo");
        dpoSelect.innerHTML = "";
        const uranaSelect = document.getElementById("anagrp");
        uranaSelect.innerHTML = "";
        const ozSelect = document.getElementById("ozkod");
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

        dpoSelect.insertBefore(newOption2, dpoSelect.options[1]);
        uranaSelect.insertBefore(newOption, uranaSelect.options[1]);
        ozSelect.insertBefore(newOption1, ozSelect.options[1]);
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
function birimChanged() {
    const birim = document.getElementById("birim").value;
    if (birim === "Tutar") {
        document.getElementById("dvzcevirchc").style.visibility = "visible";
        document.getElementById("dvzcevirlbl").style.visibility = "visible";
    } else {
        document.getElementById("dvzcevirchc").style.visibility = "hidden";
        document.getElementById("dvzcevirlbl").style.visibility = "hidden";
        document.getElementById("dvzcins").style.visibility = "hidden";
        document.getElementById("dvzturu").style.visibility = "hidden";
    }
   
}

async function grpfetchTableData() {
    const hiddenFieldValue = $('#grpBilgi').val();
    const parsedValues = hiddenFieldValue.split(",");
    const kergrupraporDTO = {
        tar1: parsedValues[0],
        tar2: parsedValues[1],
        anagrp: parsedValues[2],
        ukod1: parsedValues[3],
        ukod2: parsedValues[4],
        altgrp: parsedValues[5],
        ckod1: parsedValues[6],
        ckod2: parsedValues[7],
        ozkod: parsedValues[8],
        kons1: parsedValues[9],
        kons2: parsedValues[10],
        depo: parsedValues[11],
        evr1: parsedValues[12],
        evr2: parsedValues[13],
        birim: parsedValues[14],
        gruplama: parsedValues[15],
        stunlar: parsedValues[16],
        turu: parsedValues[17],
        dvzcevirchc: parsedValues[18],
        doviz: parsedValues[19],
        dvzturu: parsedValues[20]
    };
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    document.body.style.cursor = "wait";
    const $yenileButton = $('#grpyenileButton');
    $yenileButton.prop('disabled', true).text('İşleniyor...');
    const mainTableBody = document.getElementById("mainTableBody");
    mainTableBody.innerHTML = "";
    clearTfoot();
    try {
        const response = await fetchWithSessionCheck("kereste/grpdoldur", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(kergrupraporDTO),
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
                    if(format == 2){
                        td.textContent = formatNumber2(numericValue);
                    } else if(format == 3){
                        td.textContent = formatNumber3(numericValue);   
                    } else if (format == 0) {
                        td.textContent = formatNumber0(numericValue);   
                    }
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
            if(format == 2){
                th.textContent = formatNumber2(kolonToplamlari[index]);
            } else if(format == 3){
                th.textContent = formatNumber3(kolonToplamlari[index]);   
            } else if (format == 0) {
                th.textContent = formatNumber0(kolonToplamlari[index]);   
            }
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

async function grpdownloadReport() {
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    document.body.style.cursor = "wait";
    const $indirButton = $('#grpDownloadButton');
    $indirButton.prop('disabled', true).text('İşleniyor...');
    const $yenileButton = $('#grpyenileButton');
    $yenileButton.prop('disabled', true);
    let tableString = extractTableData(tablobaslik);
    try {
        const response = await fetchWithSessionCheckForDownload('kereste/grp_download', {
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

async function grpmailAt() {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");
    document.body.style.cursor = "wait";
    let rows = extractTableData(tablobaslik);
    localStorage.setItem("grprapor", rows);
    localStorage.setItem("tablobaslik", tablobaslik);
    const degerler = rowCounter + "," + "kergruprapor";
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