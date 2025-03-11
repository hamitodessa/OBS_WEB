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


        const newOption = document.createElement("option");
        newOption.value = "Bos Olanlar";
        newOption.textContent = "Bos Olanlar";

        const newOption1 = document.createElement("option");
        newOption1.value = "Bos Olanlar";
        newOption1.textContent = "Bos Olanlar";

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
function getkergrupraporDTO() {
    const hiddenFieldValue = $('#ortfiatBilgi').val();
    const parsedValues = hiddenFieldValue.split(",");
    return {
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
        gruplama: parsedValues[11],
        turu: parsedValues[12],
        dvzcevirchc: parsedValues[13],
        doviz: parsedValues[14],
        dvzturu: parsedValues[15]
    };
}
async function kerortfiatdoldur() {
    const kergrupraporDTO = getkergrupraporDTO();
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    document.body.style.cursor = "wait";
    const $yenileButton = $('#ortfiatyenileButton');
    $yenileButton.prop('disabled', true).text('İşleniyor...');
    const mainTableBody = document.getElementById("mainTableBody");
    mainTableBody.innerHTML = "";
    clearTfoot();
    console.log(kergrupraporDTO);
    try {
        const response = await fetchWithSessionCheck("kereste/ortfiatdoldur", {
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
