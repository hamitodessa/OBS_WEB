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
        
    } catch (error) {
        const modalError = document.getElementById("errorDiv");
        modalError.style.display = "block";
        modalError.innerText = `Bir hata oluştu: ${error.message}`;
    } finally {
        document.body.style.cursor = "default";
    }
}