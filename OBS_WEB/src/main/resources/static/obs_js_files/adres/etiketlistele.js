/* =========================
   ADR – ETIKET (çakışmasız)
   Namespace: OBS.ADR
   ========================= */
window.OBS ||= {};
OBS.ADR ||= {};

/* helpers varsa tekrar yazmana gerek yok; yoksa ekle */
OBS.ADR._root ||= () =>
    document.getElementById("adr_content") ||
    document.getElementById("ara_content") ||
    document;

OBS.ADR.byId ||= (id) => OBS.ADR._root().querySelector("#" + id);
OBS.ADR.qsa ||= (sel) => Array.from(OBS.ADR._root().querySelectorAll(sel));

OBS.ADR._setErr ||= (msg) => {
    const e = OBS.ADR.byId("errorDiv");
    if (!e) return;
    if (msg) { e.style.display = "block"; e.innerText = msg; }
    else { e.style.display = "none"; e.innerText = ""; }
};

OBS.ADR._btnState = (id, disabled, text) => {
    const b = OBS.ADR.byId(id) || document.getElementById(id);
    if (!b) return;
    b.disabled = !!disabled;
    if (typeof text === "string") b.textContent = text;
};

/* ---------- checkbox toplu seç ---------- */
OBS.ADR.toggleCheckboxes = function(source) {
    const checkboxes = OBS.ADR.qsa('tbody input[type="checkbox"]');
    checkboxes.forEach(cb => { cb.checked = !!source.checked; });
};

/* ---------- etiket yazdır (download) ---------- */
OBS.ADR.etiketyazdir = async function() {
    const rows = OBS.ADR.qsa('tbody input[type="checkbox"]:checked');

    const selectedData = rows.map((row) => {
        const tr = row.closest("tr");
        return {
            Adi: tr?.querySelector("td:nth-child(2)")?.textContent?.trim() || "",
            Adres_1: tr?.querySelector("td:nth-child(3)")?.textContent?.trim() || "",
            Adres_2: tr?.querySelector("td:nth-child(4)")?.textContent?.trim() || "",
            Tel_1: tr?.querySelector("td:nth-child(5)")?.textContent?.trim() || "",
            Semt: tr?.querySelector("td:nth-child(6)")?.textContent?.trim() || "",
            Sehir: tr?.querySelector("td:nth-child(7)")?.textContent?.trim() || ""
        };
    });

    if (selectedData.length === 0) {
        alert("Lütfen en az bir satır seçin.");
        return;
    }

    OBS.ADR._setErr("");
    document.body.style.cursor = "wait";
    OBS.ADR._btnState("etiketyazdirButton", true, "İşleniyor...");

    try {
        const response = await fetchWithSessionCheckForDownload("adres/etiket_download", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ selectedRows: selectedData })
        });

        if (!response?.blob) throw new Error("Dosya indirilemedi.");

        const disposition = response.headers?.get?.("Content-Disposition") || "";
        const match = disposition.match(/filename="(.+)"/);
        const fileName = match?.[1] || "etiket.xlsx"; // default

        const url = window.URL.createObjectURL(response.blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    } catch (error) {
        OBS.ADR._setErr(error?.message || "Bilinmeyen bir hata oluştu.");
    } finally {
        OBS.ADR._btnState("etiketyazdirButton", false, "Yazdir");
        document.body.style.cursor = "default";
    }
};

/* ---------- tablo içi filtre ---------- */
OBS.ADR.aramaYapTable = function() {
    const input = OBS.ADR.byId("arama");
    const table = OBS.ADR.byId("myTable");
    if (!input || !table) return;

    const filter = (input.value || "").toLowerCase();
    const rows = table.getElementsByTagName("tr");

    for (let i = 1;i < rows.length;i++) {
        const cells = rows[i].getElementsByTagName("td");
        let show = false;

        if (cells.length > 1) {
            const col1 = (cells[1].textContent || "").toLowerCase();
            if (col1.includes(filter)) show = true;
        }
        rows[i].style.display = show ? "" : "none";
    }
};
