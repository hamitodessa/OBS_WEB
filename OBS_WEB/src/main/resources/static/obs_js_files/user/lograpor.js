window.OBS = window.OBS || {};
OBS.LOG = OBS.LOG || {};

OBS.LOG.currentPage = 0;
OBS.LOG.totalPages = 0;
OBS.LOG.pageSize = 500;

OBS.LOG.byId = (id) => document.getElementById(id);
OBS.LOG.setDisabled = (el, yes) => { if (el) el.disabled = !!yes; };

OBS.LOG.updatePaginationUI = function() {
    const first = OBS.LOG.byId("ilksayfa");
    const prev = OBS.LOG.byId("oncekisayfa");
    const next = OBS.LOG.byId("sonrakisayfa");
    const last = OBS.LOG.byId("sonsayfa");

    const noData = OBS.LOG.totalPages === 0;

    OBS.LOG.setDisabled(first, noData || OBS.LOG.currentPage <= 0);
    OBS.LOG.setDisabled(prev, noData || OBS.LOG.currentPage <= 0);
    OBS.LOG.setDisabled(next, noData || OBS.LOG.currentPage >= OBS.LOG.totalPages - 1);
    OBS.LOG.setDisabled(last, noData || OBS.LOG.currentPage >= OBS.LOG.totalPages - 1);
};

OBS.LOG.getFilters = function() {
    return {
        modul: OBS.LOG.byId("user_modul")?.value || "",
        startDate: OBS.LOG.byId("startDate")?.value || "",
        endDate: OBS.LOG.byId("endDate")?.value || "",
        aciklama: OBS.LOG.byId("aciklama")?.value || ""
    };
};

OBS.LOG.clearError = function() {
    const e = OBS.LOG.byId("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
};

OBS.LOG.showError = function(msg) {
    const e = OBS.LOG.byId("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Beklenmeyen hata.";
};

OBS.LOG.toplamPageSize = async function() {
    OBS.LOG.clearError();

    try {
        const f = OBS.LOG.getFilters();

        const resp = await fetchWithSessionCheck("user/logsize", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ ...f })
        });

        const totalRecords = Number(resp?.totalRecords || 0);
        OBS.LOG.totalPages = Math.max(0, Math.ceil(totalRecords / OBS.LOG.pageSize));

    } catch (err) {
        OBS.LOG.showError(err?.message || "Beklenmeyen hata.");
        OBS.LOG.totalPages = 0;
    } finally {
        OBS.LOG.updatePaginationUI();
    }
};

OBS.LOG.logdoldur = async function() {
    document.body.style.cursor = "wait";
    try {
        await OBS.LOG.toplamPageSize();
        await OBS.LOG.lograpor(0);
    } finally {
        document.body.style.cursor = "default";
    }
};

OBS.LOG.lograpor = async function(page) {
    OBS.LOG.clearError();

    // sayfa sınırı
    if (OBS.LOG.totalPages > 0) {
        page = Math.max(0, Math.min(page, OBS.LOG.totalPages - 1));
    } else {
        page = 0;
    }

    const f = OBS.LOG.getFilters();

    try {
        const response = await fetchWithSessionCheck("user/loglistele", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ ...f, page, pageSize: OBS.LOG.pageSize })
        });

        if (response?.errorMessage) throw new Error(response.errorMessage);

        // ✅ ancak başarılıysa currentPage güncelle
        OBS.LOG.currentPage = page;

        const tableBody = OBS.LOG.byId("tableBody");
        if (tableBody) tableBody.innerHTML = "";

        const rows = Array.isArray(response?.data) ? response.data : [];
        rows.forEach(item => {
            const tr = document.createElement("tr");
            tr.classList.add("table-row-height");
            tr.innerHTML = `
        <td>${item?.TARIH ?? ""}</td>
        <td>${item?.MESAJ ?? ""}</td>
        <td>${item?.EVRAK ?? ""}</td>
        <td>${item?.USER_NAME ?? ""}</td>
      `;
            tableBody?.appendChild(tr);
        });

    } catch (err) {
        OBS.LOG.showError(err?.message || "Beklenmeyen hata oluştu.");
    } finally {
        OBS.LOG.updatePaginationUI();
    }
};

// === dışarıdan çağırdığın isimleri koruyalım ===
window.ilksayfa = () => { if (OBS.LOG.currentPage > 0) OBS.LOG.lograpor(0); };
window.oncekisayfa = () => { if (OBS.LOG.currentPage > 0) OBS.LOG.lograpor(OBS.LOG.currentPage - 1); };
window.sonrakisayfa = () => { if (OBS.LOG.currentPage < OBS.LOG.totalPages - 1) OBS.LOG.lograpor(OBS.LOG.currentPage + 1); };
window.sonsayfa = () => { if (OBS.LOG.totalPages > 0) OBS.LOG.lograpor(OBS.LOG.totalPages - 1); };

window.toplampagesize = () => OBS.LOG.toplamPageSize();
window.logdoldur = () => OBS.LOG.logdoldur();
window.lograpor = (p) => OBS.LOG.lograpor(p);


