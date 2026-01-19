window.OBS = window.OBS || {};
OBS.DVZ = OBS.DVZ || {};

/* =========================
   DVZ MODULE STATE
   ========================= */
OBS.DVZ.state = OBS.DVZ.state || {
  currentPage: 0,
  totalPages: 0,
  pageSize: 500
};

/* =========================
   HELPERS
   ========================= */
OBS.DVZ._el = function (id) { return document.getElementById(id); };

OBS.DVZ._setDisabled = function (id, yes) {
  const el = OBS.DVZ._el(id);
  if (el) el.disabled = !!yes;
};

OBS.DVZ._setBtn = function (id, disabled, text) {
  const b = OBS.DVZ._el(id);
  if (!b) return;
  b.disabled = !!disabled;
  if (text !== undefined) b.innerText = text;
};

OBS.DVZ._clearError = function () {
  const err = OBS.DVZ._el("errorDiv");
  if (!err) return;
  err.style.display = "none";
  err.innerText = "";
};

OBS.DVZ._showError = function (msg) {
  const err = OBS.DVZ._el("errorDiv");
  if (!err) return;
  err.style.display = "block";
  err.innerText = msg || "Beklenmeyen hata.";
};

OBS.DVZ._getBilgi = function () {
  const raw = OBS.DVZ._el("dvzcevirmeBilgi")?.value ?? "";
  const p = raw.split(",");
  return {
    hesapKodu: p[0] || "",
    startDate: p[1] || "",
    endDate: p[2] || "",
    dvz_tur: p[3] || "",
    dvz_cins: p[4] || ""
  };
};

OBS.DVZ.updatePaginationUI = function (disableAllWhileLoading = false) {
  const s = OBS.DVZ.state;

  if (disableAllWhileLoading) {
    OBS.DVZ._setDisabled("ilksayfa", true);
    OBS.DVZ._setDisabled("oncekisayfa", true);
    OBS.DVZ._setDisabled("sonrakisayfa", true);
    OBS.DVZ._setDisabled("sonsayfa", true);
    return;
  }

  const noData = s.totalPages === 0;
  OBS.DVZ._setDisabled("ilksayfa", noData || s.currentPage <= 0);
  OBS.DVZ._setDisabled("oncekisayfa", noData || s.currentPage <= 0);
  OBS.DVZ._setDisabled("sonrakisayfa", noData || s.currentPage >= s.totalPages - 1);
  OBS.DVZ._setDisabled("sonsayfa", noData || s.currentPage >= s.totalPages - 1);
};

/* =========================
   PAGINATION ACTIONS
   ========================= */
OBS.DVZ.ilksayfa = function () {
  if (OBS.DVZ.state.currentPage > 0) OBS.DVZ.dvzfetchTableData(0);
};
OBS.DVZ.oncekisayfa = function () {
  if (OBS.DVZ.state.currentPage > 0) OBS.DVZ.dvzfetchTableData(OBS.DVZ.state.currentPage - 1);
};
OBS.DVZ.sonrakisayfa = function () {
  if (OBS.DVZ.state.currentPage < OBS.DVZ.state.totalPages - 1) OBS.DVZ.dvzfetchTableData(OBS.DVZ.state.currentPage + 1);
};
OBS.DVZ.sonsayfa = function () {
  if (OBS.DVZ.state.totalPages > 0) OBS.DVZ.dvzfetchTableData(OBS.DVZ.state.totalPages - 1);
};

/* =========================
   TOTAL PAGE SIZE
   ========================= */
OBS.DVZ.toplampagesize = async function () {
  OBS.DVZ._clearError();

  const s = OBS.DVZ.state;
  const { hesapKodu, startDate, endDate, dvz_tur, dvz_cins } = OBS.DVZ._getBilgi();

  try {
    const res = await fetchWithSessionCheck("cari/dvzsize", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hesapKodu, startDate, endDate, dvz_tur, dvz_cins }),
    });

    const totalRecords = res?.totalRecords ?? 0;
    s.totalPages = Math.max(0, Math.ceil(totalRecords / s.pageSize));
  } catch (e) {
    OBS.DVZ._showError(e?.message || e || "Beklenmeyen hata.");
    s.totalPages = 0;
  } finally {
    OBS.DVZ.updatePaginationUI();
  }
};

/* =========================
   INITIAL LOAD
   ========================= */
OBS.DVZ.dvzdoldur = async function () {
  document.body.style.cursor = "wait";
  OBS.DVZ.updatePaginationUI(true);

  await OBS.DVZ.toplampagesize();
  await OBS.DVZ.dvzfetchTableData(0);

  document.body.style.cursor = "default";
};

/* =========================
   TABLE FETCH
   ========================= */
OBS.DVZ.dvzfetchTableData = async function (page) {
  const s = OBS.DVZ.state;
  s.currentPage = page;

  OBS.DVZ._clearError();

  const acik = OBS.DVZ._el("dvzaciklama");
  if (acik) acik.innerText = "";

  const { hesapKodu, startDate, endDate, dvz_tur, dvz_cins } = OBS.DVZ._getBilgi();
  if (!hesapKodu || !startDate || !endDate) {
    OBS.DVZ._showError("Lütfen tüm alanları doldurun.");
    return;
  }

  const tableBody = OBS.DVZ._el("tableBody");
  if (tableBody) tableBody.innerHTML = "";

  document.body.style.cursor = "wait";
  OBS.DVZ.updatePaginationUI(true);
  OBS.DVZ._setBtn("dvzyenileButton", true, "İşleniyor...");

  try {
    const data = await fetchWithSessionCheck("cari/dvzcevirme", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        hesapKodu, startDate, endDate, dvz_tur, dvz_cins,
        page,
        pageSize: s.pageSize
      }),
    });

    if (!data?.success) throw new Error(data?.errorMessage || "Bir hata oluştu.");

    let boskur = 0;

    (data.data || []).forEach(item => {
      const row = document.createElement("tr");
      row.classList.add("table-row-height");
      row.innerHTML = `
        <td>${formatDate(item.TARIH)}</td>
        <td>${item.EVRAK || ""}</td>
        <td>${item.IZAHAT || ""}</td>
        <td class="double-column" style="color:${item.CEV_KUR == 1 ? "red" : "white"};">
          ${formatNumber4(item.CEV_KUR)}
        </td>
        <td class="double-column">${formatNumber2(item.DOVIZ_TUTAR)}</td>
        <td class="double-column">${formatNumber2(item.DOVIZ_BAKIYE)}</td>
        <td class="double-column">${formatNumber2(item.BAKIYE)}</td>
        <td class="double-column">${formatNumber4(item.KUR)}</td>
        <td class="double-column">${formatNumber2(item.BORC)}</td>
        <td class="double-column">${formatNumber2(item.ALACAK)}</td>
        <td>${item.USER || ""}</td>
      `;
      if (item.CEV_KUR === 1.0) boskur += 1;
      tableBody?.appendChild(row);
    });

    // sayfa butonlarını sayfa durumuna göre aç/kapat
    OBS.DVZ.updatePaginationUI();

    // ad bilgisi + açıklama
    if (typeof hesapAdiOgren === "function") hesapAdiOgren(hesapKodu, "hesapAdi");
    if (acik) acik.innerText = "Bos Kur :" + boskur;

  } catch (e) {
    OBS.DVZ._showError(e?.message || "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.");
  } finally {
    OBS.DVZ._setBtn("dvzyenileButton", false, "Yenile");
    document.body.style.cursor = "default";
    OBS.DVZ.updatePaginationUI(); // son durum
  }
};

/* =========================
   DOWNLOAD
   ========================= */
OBS.DVZ.dvzdownloadReport = async function (format) {
  OBS.DVZ._clearError();

  const { hesapKodu, startDate, endDate, dvz_tur, dvz_cins } = OBS.DVZ._getBilgi();
  if (!hesapKodu || !startDate || !endDate || !format) {
    OBS.DVZ._showError("Lütfen tüm alanlari doldurun.");
    return;
  }

  document.body.style.cursor = "wait";
  OBS.DVZ._setBtn("dvzindirButton", true, "İşleniyor...");
  OBS.DVZ._setDisabled("dvzyenileButton", true);

  try {
    const response = await fetchWithSessionCheckForDownload("cari/dvzcevirme_download", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ format, hesapKodu, startDate, endDate, dvz_tur, dvz_cins }),
    });

    if (!response?.blob) throw new Error("Dosya indirilemedi.");

    const disposition = response.headers.get("Content-Disposition") || "";
    const m = disposition.match(/filename="(.+)"/);
    const fileName = m ? m[1] : "dvzcevirme_rapor.bin";

    const url = window.URL.createObjectURL(response.blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);

  } catch (e) {
    OBS.DVZ._showError(e?.message || "Bilinmeyen bir hata oluştu.");
  } finally {
    OBS.DVZ._setBtn("dvzindirButton", false, "Rapor İndir");
    OBS.DVZ._setDisabled("dvzyenileButton", false);
    document.body.style.cursor = "default";
  }
};

/* =========================
   MAIL
   ========================= */
OBS.DVZ.dvzmailAt = function () {
  localStorage.removeItem("tableData");
  localStorage.removeItem("grprapor");
  localStorage.removeItem("tablobaslik");

  const { hesapKodu, startDate, endDate, dvz_tur, dvz_cins } = OBS.DVZ._getBilgi();
  if (!hesapKodu) {
    alert("Lütfen geçerli bir hesap kodu girin!");
    return;
  }

  const degerler = `${hesapKodu},${startDate},${endDate},${dvz_tur},${dvz_cins},dvzcevir`;
  const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
  mailsayfasiYukle(url);
};
