async function openadrkodlariModal(inputId, secondnerden) {
    activeNestedInputId = inputId;
    $('#adrsecondModal').modal('show');
    const modalError = document.getElementById("adrsecond-errorDiv");
    modalError.style.display = "none";
    modalError.innerText = "";
    document.body.style.cursor = "wait";
    try {
        const response = await fetchWithSessionCheck("modal/adrhsppln");
        if (response.errorMessage) {
            throw new Error(response.errorMessage);
        }
        const data = response;
        const tableBody = document.getElementById("adrsecond-modalTableBody");
        tableBody.innerHTML = "";
        if (data.length === 0) {
            if (modalError) {
                modalError.style.display = "block";
                modalError.innerText = "Hiç veri bulunamadı.";
            }
            return;
        }
        tableBody.classList.add("table-row-height");
        data.forEach((row) => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                   <td style="min-width:20%;">${row.M_Kodu || ""}</td>
                   <td>${row.Adi || ""}</td>
               `;
            tr.onclick = () => adrselectValue(inputId, row.M_Kodu, secondnerden);
            tableBody.appendChild(tr);
        });
    } catch (error) {
        const modalError = document.getElementById("adrsecond-errorDiv");
        modalError.style.display = "block";
        modalError.innerText = `Bir hata oluştu: ${error.message}`;
    } finally {
        document.body.style.cursor = "default";
    }
}

function adrselectValue(inputId, selectedKodu, secondnerden) {
    $('#adrsecondModal').modal('hide');
    const inputElementm = document.getElementById(inputId);
    document.getElementById("adrsecond-modalSearch").value = "";
     inputElementm.value = selectedKodu;
     if (secondnerden === "fatura") {
         adrhesapAdiOgren(inputId, 'adresadilbl');
    }
}

function adrfilterTable() {
    const searchValue = document.getElementById("adrsecond-modalSearch").value.toLowerCase();
    const rows = document.querySelectorAll("#adrsecond-modalTable tbody tr");
    rows.forEach((row) => {
        const rowText = Array.from(row.cells)
            .map((cell) => cell.textContent.toLowerCase())
            .join(" ");
        row.style.display = rowText.includes(searchValue) ? "" : "none";
    });
}