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
    const hiddenFieldValue = $('#kerestedetayBilgi').val();
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
    const mainTableBody = document.getElementById("tbody");
    mainTableBody.innerHTML = "";
    clearTfoot();
    currentPage = page;
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

        
     } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = error;
    } finally {
        $yenileButton.prop('disabled', false).text('Yenile');
        document.body.style.cursor = "default";
    }
}
