/* ================================================================================================================
   SECOND MODAL OPEN (HSP PLAN)
   ========================= */
async function openSecondModal(inputId, secondnerden) {
    activeNestedInputId = inputId;
    modalShow("secondModal");
    const modalError = byId("hsperrorDiv");
    if (modalError) {
        modalError.style.display = "none";
        modalError.innerText = "";
    }
    document.body.style.cursor = "wait";
    try {
        const response = await fetchWithSessionCheck("modal/hsppln");
        if (response?.errorMessage) throw new Error(response.errorMessage);
        const data = response;
        const tableBody = byId("modalTableBody");
        if (!tableBody) return;
        tableBody.innerHTML = "";
        if (!data || data.length === 0) {
            if (modalError) {
                modalError.style.display = "block";
                modalError.innerText = "Hiç veri bulunamadı.";
            }
            return;
        }

        data.forEach((row) => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
        <td>${row.HESAP || ""}</td>
        <td>${row.UNVAN || ""}</td>
        <td>${row.HESAP_CINSI || ""}</td>
        <td>${row.KARTON || ""}</td>
      `;
            tr.addEventListener("click", () => selectValue(inputId, row.HESAP, secondnerden));
            tableBody.appendChild(tr);
        });
        bindHspModalKeyboard(inputId, secondnerden);
        const modalsearch = byId("modalSearch");
        modalsearch.value = '';
        modalsearch.focus();
    } catch (error) {
        if (modalError) {
            modalError.style.display = "block";
            modalError.innerText = `Bir hata oluştu: ${error.message}`;
        }
    } finally {
        document.body.style.cursor = "default";
    }
}

// =========================
// Modal keyboard navigation
// =========================
function bindHspModalKeyboard(inputId, secondnerden) {
    const search = byId("modalSearch");
    const tbody = byId("modalTableBody");
    if (!search || !tbody) return;
    const key = "kbdBound_" + inputId + "_" + (secondnerden ?? "");
    if (search.dataset[key] === "1") return;
    search.dataset[key] = "1";
    let activeIndex = -1;
    const rows = () => Array.from(tbody.querySelectorAll("tr"));
    const clearActive = () => {
        rows().forEach((tr) => tr.classList.remove("is-active-row"));
    };
    const setActive = (idx) => {
        const r = rows();
        if (!r.length) return;
        if (idx < 0) idx = 0;
        if (idx >= r.length) idx = r.length - 1;
        activeIndex = idx;
        clearActive();
        const tr = r[activeIndex];
        tr.classList.add("is-active-row");
        tr.scrollIntoView({ block: "nearest" });
    };

    const clickActive = () => {
        const r = rows();
        if (!r.length) return;
        if (activeIndex < 0) setActive(0);
        const tr = r[activeIndex];
        if (!tr) return;
        const hesap = tr.querySelector("td:nth-child(1)")?.textContent?.trim() || "";
        if (hesap) selectValue(inputId, hesap, secondnerden);
    };

    search.addEventListener("keydown", (e) => {
        if (e.key === "ArrowDown") {
            const r = rows();
            if (!r.length) return;
            e.preventDefault();
            setActive(activeIndex >= 0 ? activeIndex : 0);
            // focus'u tabloya ver (tbody focus alamaz; ilk tr'ye tabindex veriyoruz)
            r[activeIndex].focus();
        } else if (e.key === "Enter") {
            // istersen: search'te Enter => ilk satırı seçsin
            const r = rows();
            if (!r.length) return;
            e.preventDefault();
            setActive(activeIndex >= 0 ? activeIndex : 0);
            clickActive();
        }
    });

    // Tablo satırlarına klavye desteği
    const enhanceRowsForKeyboard = () => {
        const r = rows();
        r.forEach((tr, idx) => {
            tr.tabIndex = 0; // focus alabilsin
            tr.dataset.idx = String(idx);

            // mouse hover olunca da aktif çizgi
            tr.addEventListener("mouseenter", () => setActive(idx));

            tr.addEventListener("keydown", (e) => {
                if (e.key === "ArrowDown") {
                    e.preventDefault();
                    setActive(activeIndex + 1);
                    rows()[activeIndex]?.focus();
                } else if (e.key === "ArrowUp") {
                    e.preventDefault();
                    setActive(activeIndex - 1);
                    rows()[activeIndex]?.focus();
                } else if (e.key === "Enter") {
                    e.preventDefault();
                    clickActive();
                } else if (e.key === "Escape") {
                    e.preventDefault();
                    e.stopPropagation();
                    clearActive();
                    activeIndex = -1;
                    requestAnimationFrame(() => {
                        tbody.parentElement?.scrollTo({ top: 0, behavior: "smooth" });
                        search.focus();
                        search.select(); // istersen text de seçilsin
                    });
                }
            });
        });
    };

    // tbody her dolduğunda tekrar "tabIndex + keydown" bağlamak lazım.
    // Sen tabloyu her openSecondModal'da yeniden dolduruyorsun.
    // Bu yüzden küçük bir observer:
    const obs = new MutationObserver(() => {
        // row sayısı değişince indeksleri resetle
        activeIndex = -1;
        enhanceRowsForKeyboard();
    });

    // bir kez bağla
    obs.observe(tbody, { childList: true });
    // ilk durum için
    enhanceRowsForKeyboard();
}

// satır seçimi için ufak stil (istersen CSS dosyana ekle)
(function injectModalRowStyle() {
    if (document.getElementById("hspModalRowStyle")) return;
    const s = document.createElement("style");
    s.id = "hspModalRowStyle";
    s.textContent = `
    #secondModal tr.is-active-row td{
      outline: none;
      box-shadow: inset 0 0 0 9999px rgba(255,255,255,.08);
    }
  `;
    document.head.appendChild(s);
})();


function filterTable() {
    const searchValue = valRaw("modalSearch").toLowerCase();
    document.querySelectorAll("#modalTable tbody tr").forEach((row) => {
        const rowText = Array.from(row.cells).map((c) => c.textContent.toLowerCase()).join(" ");
        row.style.display = rowText.includes(searchValue) ? "" : "none";
    });
}

function selectValue(inputId, selectedValue, secondnerden) {
    const inputElement = byId(inputId);
    if (!inputElement) return;
    inputElement.value = selectedValue;
    if (
        ["dekont", "tahsilat", "cekgir", "cekcik", "tahsilatckaydet", "carikoddegis", "fatura", "irsaliye"]
            .includes(secondnerden)
    ) {
        if (typeof inputElement.oninput === "function") inputElement.oninput();
    } else if (secondnerden === "gunlukkontrol") {
        if (typeof inputElement.oninput === "function") inputElement.oninput();
        if (typeof belgeoku === "function") belgeoku();
    }

    const ms = byId("modalSearch");
    if (ms) ms.value = "";

    modalHide("secondModal");
}