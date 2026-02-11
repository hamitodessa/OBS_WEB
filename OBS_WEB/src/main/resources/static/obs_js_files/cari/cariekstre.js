/* =========================
   EKSTRE (NO JQUERY) - NAMESPACE SAFE
   ========================= */

// tek namespace: çakışma yok
window.OBS = window.OBS || {};
OBS.EKSTRE = OBS.EKSTRE || {};

OBS.EKSTRE.currentPage = 0;
OBS.EKSTRE.totalPages = 0;
OBS.EKSTRE.pageSize = 500;

/* ---------- helpers (çakışmasız) ---------- */
OBS.EKSTRE.byId = (id) => document.getElementById(id);

OBS.EKSTRE.setDisabled = (el, yes) => { if (el) el.disabled = !!yes; };

OBS.EKSTRE.setBtnLoading = (btnId, yes, loadingText, normalText) => {
    const btn = OBS.EKSTRE.byId(btnId);
    if (!btn) return;
    btn.disabled = !!yes;
    if (yes && loadingText != null) btn.textContent = loadingText;
    if (!yes && normalText != null) btn.textContent = normalText;
};

OBS.EKSTRE.showError = (msg) => {
    const errorDiv = OBS.EKSTRE.byId("errorDiv");
    if (!errorDiv) return;
    errorDiv.style.display = "block";
    errorDiv.innerText = msg || "Beklenmeyen hata.";
};

OBS.EKSTRE.clearError = () => {
    const errorDiv = OBS.EKSTRE.byId("errorDiv");
    if (!errorDiv) return;
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
};

OBS.EKSTRE.getEkstreParams = () => {
    const hidden =
        document.querySelector("#ara_content #ekstreBilgi") ||
        document.getElementById("ekstreBilgi");

    const hiddenFieldValue = (hidden?.value ?? "").trim();
    const [hesapKodu = "", startDate = "", endDate = ""] = hiddenFieldValue.split(",");
    return { hesapKodu, startDate, endDate };
};

/* ---------- pagination UI ---------- */
OBS.EKSTRE.updatePaginationUI = (disableAllWhileLoading = false) => {
    const first = OBS.EKSTRE.byId("ilksayfa");
    const prev = OBS.EKSTRE.byId("oncekisayfa");
    const next = OBS.EKSTRE.byId("sonrakisayfa");
    const last = OBS.EKSTRE.byId("sonsayfa");

    if (disableAllWhileLoading) {
        OBS.EKSTRE.setDisabled(first, true); OBS.EKSTRE.setDisabled(prev, true);
        OBS.EKSTRE.setDisabled(next, true); OBS.EKSTRE.setDisabled(last, true);
        return;
    }

    const noData = OBS.EKSTRE.totalPages === 0;
    const cp = OBS.EKSTRE.currentPage;
    const tp = OBS.EKSTRE.totalPages;

    OBS.EKSTRE.setDisabled(first, noData || cp <= 0);
    OBS.EKSTRE.setDisabled(prev, noData || cp <= 0);
    OBS.EKSTRE.setDisabled(next, noData || cp >= tp - 1);
    OBS.EKSTRE.setDisabled(last, noData || cp >= tp - 1);
};

/* ---------- global button functions (HTML onclick aynı kalsın) ---------- */
OBS.EKSTRE.ilksayfa = function() {
    if (OBS.EKSTRE.currentPage > 0) OBS.EKSTRE.eksfetchTableData(0);
};
OBS.EKSTRE.oncekisayfa = function() {
    if (OBS.EKSTRE.currentPage > 0) OBS.EKSTRE.eksfetchTableData(OBS.EKSTRE.currentPage - 1);
};
OBS.EKSTRE.sonrakisayfa = function() {
    if (OBS.EKSTRE.currentPage < OBS.EKSTRE.totalPages - 1) OBS.EKSTRE.eksfetchTableData(OBS.EKSTRE.currentPage + 1);
};
OBS.EKSTRE.sonsayfa = function() {
    if (OBS.EKSTRE.totalPages > 0) OBS.EKSTRE.eksfetchTableData(OBS.EKSTRE.totalPages - 1);
};

/* =========================
   total page size
   ========================= */
OBS.EKSTRE.toplampagesize = async function() {
    try {
        OBS.EKSTRE.clearError();

        const { hesapKodu, startDate, endDate } = OBS.EKSTRE.getEkstreParams();

        const response = await fetchWithSessionCheck("cari/ekssize", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ hesapKodu, startDate, endDate }),
        });

        const totalRecords = response?.totalRecords ?? 0;
        OBS.EKSTRE.totalPages = Math.max(0, Math.ceil(totalRecords / OBS.EKSTRE.pageSize));
    } catch (error) {
        OBS.EKSTRE.totalPages = 0;
        OBS.EKSTRE.showError(error?.message || error || "Beklenmeyen hata.");
    } finally {
        OBS.EKSTRE.updatePaginationUI();
    }
};

/* =========================
   main load
   ========================= */
OBS.EKSTRE.eksdoldur = async function() {
    document.body.style.cursor = "wait";
    OBS.EKSTRE.updatePaginationUI(true);

    await OBS.EKSTRE.toplampagesize();
    await OBS.EKSTRE.eksfetchTableData(0);

    document.body.style.cursor = "default";
};

/* =========================
   fetch table data
   ========================= */
OBS.EKSTRE.eksfetchTableData = async function(page) {
    const { hesapKodu, startDate, endDate } = OBS.EKSTRE.getEkstreParams();
    OBS.EKSTRE.currentPage = page;

    OBS.EKSTRE.clearError();

    const tableBody = OBS.EKSTRE.byId("tableBody");
    if (tableBody) tableBody.innerHTML = "";

    const totalBorcEl = OBS.EKSTRE.byId("totalBorc");
    const totalAlacakEl = OBS.EKSTRE.byId("totalAlacak");
    if (totalBorcEl) totalBorcEl.textContent = "";
    if (totalAlacakEl) totalAlacakEl.textContent = "";

    document.body.style.cursor = "wait";
    OBS.EKSTRE.updatePaginationUI(true);
    OBS.EKSTRE.setBtnLoading("eksyenileButton", true, "İşleniyor...", "Yenile");

    try {
        const data = await fetchWithSessionCheck("cari/ekstre", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                hesapKodu,
                startDate,
                endDate,
                page,
                pageSize: OBS.EKSTRE.pageSize
            }),
        });

        if (!data?.success) throw new Error(data?.errorMessage || "Bir hata oluştu.");

        (data.data || []).forEach((item) => {
            const row = document.createElement("tr");
            row.innerHTML = `
        <td>${formatDate(item.TARIH)}</td>
        <td>${item.EVRAK || ""}</td>
        <td>${item.IZAHAT || ""}</td>
        <td>${item.KOD || ""}</td>
        <td class="double-column">${formatNumber4(item.KUR)}</td>
        <td class="double-column">${formatNumber2(item.BORC)}</td>
        <td class="double-column">${formatNumber2(item.ALACAK)}</td>
        <td class="double-column">${formatNumber2(item.BAKIYE)}</td>
        <td>${item.USER || ""}</td>
      `;
            tableBody && tableBody.appendChild(row);
        });

        if (typeof hesapAdiOgren === "function") {
            hesapAdiOgren(hesapKodu, "hesapAdi");
        }
    } catch (error) {
        OBS.EKSTRE.showError(error?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
        OBS.EKSTRE.setBtnLoading("eksyenileButton", false, "İşleniyor...", "Yenile");
        document.body.style.cursor = "default";
        OBS.EKSTRE.updatePaginationUI();
    }
};

/* =========================
   download report
   ========================= */
OBS.EKSTRE.ekstredownloadReport = async function(format) {
    const { hesapKodu, startDate: startDateField, endDate: endDateField } = OBS.EKSTRE.getEkstreParams();

    OBS.EKSTRE.clearError();

    if (!hesapKodu || !startDateField || !endDateField || !format) {
        OBS.EKSTRE.showError("Lütfen tüm alanları doldurun.");
        return;
    }

    document.body.style.cursor = "wait";
    OBS.EKSTRE.setBtnLoading("indirButton", true, "İşleniyor...", "Rapor İndir");
    OBS.EKSTRE.setDisabled(OBS.EKSTRE.byId("yenileButton"), true);

    try {
        const response = await fetchWithSessionCheckForDownload("cari/ekstre_download", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                format,
                kodu: hesapKodu,
                startDate: startDateField,
                endDate: endDateField,
            }),
        });

        if (!response?.blob) throw new Error("Dosya indirilemedi.");

        const disposition = response.headers.get("Content-Disposition") || "";
        const m = disposition.match(/filename="(.+)"/);
        const fileName = (m && m[1]) ? m[1] : `ekstre.${format}`;

        const url = window.URL.createObjectURL(response.blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

    } catch (error) {
        OBS.EKSTRE.showError(error?.message || "Bilinmeyen bir hata oluştu.");
    } finally {
        OBS.EKSTRE.setBtnLoading("indirButton", false, "İşleniyor...", "Rapor İndir");
        OBS.EKSTRE.setDisabled(OBS.EKSTRE.byId("yenileButton"), false);
        document.body.style.cursor = "default";
    }
};

/* =========================
   mail
   ========================= */
OBS.EKSTRE.ekstremailAt = async function() {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    const { hesapKodu, startDate: startDateField, endDate: endDateField } = OBS.EKSTRE.getEkstreParams();

    if (!hesapKodu) {
        alert("Lütfen geçerli bir hesap kodu girin!");
        return;
    }

    const degerler = `${hesapKodu},${startDateField},${endDateField},cariekstre`;
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;

    if (typeof mailsayfasiYukle === "function") {
        mailsayfasiYukle(url);
    } else {
        window.location.href = url;
    }
};
