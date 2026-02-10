window.OBS = window.OBS || {};
OBS.MIZAN = OBS.MIZAN || {};

/* =========================
   HELPERS (MIZAN)
   ========================= */
OBS.MIZAN._el = function(id) { return document.getElementById(id); };

OBS.MIZAN._setBtn = function(btnId, disabled, text) {
    const b = OBS.MIZAN._el(btnId);
    if (!b) return;
    b.disabled = !!disabled;
    if (text !== undefined) b.innerText = text;
};

OBS.MIZAN._clearError = function() {
    const err = OBS.MIZAN._el("errorDiv");
    if (!err) return;
    err.style.display = "none";
    err.innerText = "";
};

OBS.MIZAN._showError = function(msg) {
    const err = OBS.MIZAN._el("errorDiv");
    if (!err) return;
    err.style.display = "block";
    err.innerText = msg || "Bir hata oluştu.";
};

OBS.MIZAN._parseMizanBilgi = function() {
    const raw = OBS.MIZAN._el("mizanBilgi")?.value ?? "";
    const p = raw.split(",");
    return {
        hkodu1: p[0] || "",
        hkodu2: p[1] || "",
        startDate: p[2] || "",
        endDate: p[3] || "",
        cins1: p[4] || "",
        cins2: p[5] || "",
        karton1: p[6] || "",
        karton2: p[7] || "",
        hangi_tur: p[8] || ""
    };
};

/* =========================
   TABLE FETCH
   ========================= */
OBS.MIZAN.mizfetchTableData = async function() {
    const req = OBS.MIZAN._parseMizanBilgi();

    OBS.MIZAN._clearError();

    const tableBody = OBS.MIZAN._el("tableBody");
    if (tableBody) tableBody.innerHTML = "";

    if (OBS.MIZAN._el("totalBorc")) OBS.MIZAN._el("totalBorc").textContent = "";
    if (OBS.MIZAN._el("totalAlacak")) OBS.MIZAN._el("totalAlacak").textContent = "";
    if (OBS.MIZAN._el("totalBakiye")) OBS.MIZAN._el("totalBakiye").textContent = "";

    document.body.style.cursor = "wait";
    OBS.MIZAN._setBtn("mizyenileButton", true, "İşleniyor...");

    try {
        const data = await fetchWithSessionCheck("cari/mizanrapor", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(req),
        });

        if (!data?.success) {
            OBS.MIZAN._showError(data?.errorMessage || "Bir hata oluştu.");
            return;
        }

        let totalBorc = 0, totalAlacak = 0, totalBakiye = 0;

        (data.data || []).forEach(item => {
            const row = document.createElement("tr");
            row.classList.add("table-row-height");
            row.innerHTML = `
        <td>${item.HESAP || ""}</td>
        <td>${item.UNVAN || ""}</td>
        <td>${item.H_CINSI || ""}</td>
        <td class="double-column">${formatNumber2(item.BORC)}</td>
        <td class="double-column">${formatNumber2(item.ALACAK)}</td>
        <td class="double-column">${formatNumber2(item.BAKIYE)}</td>
      `;
            tableBody?.appendChild(row);

            totalBorc += (item.BORC || 0);
            totalAlacak += (item.ALACAK || 0);
            totalBakiye += (item.BAKIYE || 0);
        });

        if (OBS.MIZAN._el("totalBorc")) OBS.MIZAN._el("totalBorc").textContent = formatNumber2(totalBorc);
        if (OBS.MIZAN._el("totalAlacak")) OBS.MIZAN._el("totalAlacak").textContent = formatNumber2(totalAlacak);
        if (OBS.MIZAN._el("totalBakiye")) OBS.MIZAN._el("totalBakiye").textContent = formatNumber2(totalBakiye);

    } catch (e) {
        OBS.MIZAN._showError(e?.message || "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.");
    } finally {
        OBS.MIZAN._setBtn("mizyenileButton", false, "Yenile");
        document.body.style.cursor = "default";
    }
};

/* =========================
   DOWNLOAD
   ========================= */
OBS.MIZAN.mizdownloadReport = async function(format) {
    const req = OBS.MIZAN._parseMizanBilgi();
    req.format = format;

    OBS.MIZAN._clearError();

    document.body.style.cursor = "wait";
    OBS.MIZAN._setBtn("indirButton", true, "İşleniyor...");
    // sende bazen farklı id kullanılıyor diye ikisini de kilitliyorum
    OBS.MIZAN._setBtn("yenileButton", true);
    OBS.MIZAN._setBtn("mizyenileButton", true);

    try {
        const response = await fetchWithSessionCheckForDownload("cari/mizan_download", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(req),
        });

        if (!response?.blob) throw new Error("Dosya indirilemedi.");

        const disposition = response.headers.get("Content-Disposition") || "";
        const m = disposition.match(/filename="(.+)"/);
        const fileName = m ? m[1] : "mizan_rapor.bin";

        const url = window.URL.createObjectURL(response.blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

    } catch (e) {
        OBS.MIZAN._showError(e?.message || "Bilinmeyen bir hata oluştu.");
    } finally {
        OBS.MIZAN._setBtn("indirButton", false, "Rapor İndir");
        OBS.MIZAN._setBtn("yenileButton", false);
        OBS.MIZAN._setBtn("mizyenileButton", false);
        document.body.style.cursor = "default";
    }
};

/* =========================
   MAIL
   ========================= */
OBS.MIZAN.mizmailAt = function() {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    const req = OBS.MIZAN._parseMizanBilgi();
    const degerler =
        `${req.hkodu1},${req.hkodu2},${req.startDate},${req.endDate},` +
        `${req.cins1},${req.cins2},${req.karton1},${req.karton2},${req.hangi_tur},carimizan`;

    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);
};

