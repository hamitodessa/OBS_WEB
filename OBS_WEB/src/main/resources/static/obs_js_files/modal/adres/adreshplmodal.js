

// open
async function openadrkodlariModal(inputId, secondnerden) {
    activeNestedInputId = inputId;

    modalShow("adrsecondModal");

    const modalError = document.getElementById("adrsecond-errorDiv");
    if (modalError) {
        modalError.style.display = "none";
        modalError.innerText = "";
    }

    document.body.style.cursor = "wait";

    try {
        const response = await fetchWithSessionCheck("modal/adrhsppln");
        if (response?.errorMessage) throw new Error(response.errorMessage);

        const data = Array.isArray(response) ? response : [];

        const tableBody = document.getElementById("adrsecond-modalTableBody");
        if (!tableBody) throw new Error("adrsecond-modalTableBody bulunamadı");

        tableBody.innerHTML = "";

        if (data.length === 0) {
            if (modalError) {
                modalError.style.display = "block";
                modalError.innerText = "Hiç veri bulunamadı.";
            }
            return;
        }

        tableBody.classList.add("table-row-height");

        for (const row of data) {
            const tr = document.createElement("tr");
            tr.innerHTML = `
        <td>${row?.M_Kodu ?? ""}</td>
        <td>${row?.Adi ?? ""}</td>
      `;
            tr.addEventListener("click", () => adrselectValue(inputId, row?.M_Kodu ?? "", secondnerden));
            tableBody.appendChild(tr);
        }
				const modalsearch = byId("adrsecond-modalSearch");
				               modalsearch.value = '';
				               modalsearch.focus();
    } catch (error) {
        if (modalError) {
            modalError.style.display = "block";
            modalError.innerText = `Bir hata oluştu: ${error?.message ?? error}`;
        }
    } finally {
        document.body.style.cursor = "default";
    }
}


function adrselectValue(inputId, selectedKodu, secondnerden) {
    modalHide("adrsecondModal");

    const inputEl = document.getElementById(inputId);
    if (inputEl) inputEl.value = selectedKodu;

    const searchEl = document.getElementById("adrsecond-modalSearch");
    if (searchEl) searchEl.value = "";

    if (secondnerden === "fatura" || secondnerden === "irsaliye" ) {
        adrhesapAdiOgren(inputId, "adresadilbl");
    }else if (secondnerden === "tahsilat") {
				        adrhesapAdiOgren(inputId, "lbladrheskod");
		}
}

// filter
function adrfilterTable() {
    const searchValue = (document.getElementById("adrsecond-modalSearch")?.value ?? "").toLowerCase();
    const rows = document.querySelectorAll("#adrsecond-modalTable tbody tr");

    rows.forEach((row) => {
        const rowText = Array.from(row.cells)
            .map((cell) => (cell.textContent || "").toLowerCase())
            .join(" ");
        row.style.display = rowText.includes(searchValue) ? "" : "none";
    });
}
