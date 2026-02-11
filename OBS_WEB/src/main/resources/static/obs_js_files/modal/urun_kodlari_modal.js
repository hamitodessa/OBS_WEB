

async function openurunkodlariModal(inputId, secondnerden, barkodurunkodu) {
    modalShow("urnsecondModal");

    const modalError = document.getElementById("urnsecond-errorDiv");
    if (modalError) {
        modalError.style.display = "none";
        modalError.innerText = "";
    }

    document.body.style.cursor = "wait";

    try {
        const response = await fetchWithSessionCheck("modal/urunkodlari");
        if (response?.errorMessage) throw new Error(response.errorMessage);

        const data = Array.isArray(response) ? response : [];

        const tableBody = document.getElementById("urnsecond-modalTableBody");
        if (!tableBody) throw new Error("urnsecond-modalTableBody bulunamadı");

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
        <td>${row?.Barkod ?? ""}</td>
        <td>${row?.Kodu ?? ""}</td>
        <td>${row?.Adi ?? ""}</td>
        <td>${row?.Sinif ?? ""}</td>
        <td>${row?.Birim ?? ""}</td>
        <td>${row?.Agirlik ?? ""}</td>
        <td>${row?.Mensei ?? ""}</td>
        <td>${row?.Ana_Grup ?? ""}</td>
        <td>${row?.Alt_Grup ?? ""}</td>
      `;
            tr.addEventListener("click", () =>
                urnselectValue(
                    inputId,
                    row?.Barkod ?? "",
                    row?.Kodu ?? "",
                    secondnerden,
                    barkodurunkodu
                )
            );
            tableBody.appendChild(tr);
        }
				
				const modalsearch = byId("urnsecond-modalSearch");
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

function stkfilterTable() {
    // Senin modal id’lerin "urnsecond-modalSearch" / "urnsecond-modalTable" ise buna göre düzelt
    // (Eski kodda modalSearch/modalTable kalmış)
    const searchValue = (document.getElementById("urnsecond-modalSearch")?.value ?? "").toLowerCase();
    const rows = document.querySelectorAll("#urnsecond-modalTableBody tbody tr");

    rows.forEach((row) => {
        const rowText = Array.from(row.cells)
            .map((cell) => (cell.textContent || "").toLowerCase())
            .join(" ");
        row.style.display = rowText.includes(searchValue) ? "" : "none";
    });
}

function urnselectValue(inputId, selectedBarkod, selectedKodu, secondnerden, barkodurunkodu) {
    const inputEl = document.getElementById(inputId);
    const searchEl = document.getElementById("urnsecond-modalSearch");
    if (searchEl) searchEl.value = "";

    modalHide("urnsecondModal");

    if (inputEl) {
        if (barkodurunkodu === "ukodukod") inputEl.value = selectedKodu;
        else if (barkodurunkodu === "barkod") inputEl.value = selectedBarkod;
    }

    if (secondnerden === "imalat") {
        urnaramaYap("Kodu");
        return;
    }

    if (secondnerden === "imalatsatir" || secondnerden === "recetealt" || secondnerden === "recetesatir") {
        if (inputEl) inputEl.dispatchEvent(new Event("change", { bubbles: true }));
        return;
    }
}
