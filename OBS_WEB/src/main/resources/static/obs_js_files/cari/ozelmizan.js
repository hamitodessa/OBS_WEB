window.OBS = window.OBS || {};
OBS.OZMIZAN = OBS.OZMIZAN || {};

/* =========================
   HELPERS (OZ MIZAN)
   ========================= */
OBS.OZMIZAN._el = function (id) { return document.getElementById(id); };

OBS.OZMIZAN._setBtn = function (btnId, disabled, text) {
  const b = OBS.OZMIZAN._el(btnId);
  if (!b) return;
  b.disabled = !!disabled;
  if (text !== undefined) b.innerText = text;
};

OBS.OZMIZAN._clearError = function () {
  const err = OBS.OZMIZAN._el("errorDiv");
  if (!err) return;
  err.style.display = "none";
  err.innerText = "";
};

OBS.OZMIZAN._showError = function (msg) {
  const err = OBS.OZMIZAN._el("errorDiv");
  if (!err) return;
  err.style.display = "block";
  err.innerText = msg || "Bir hata oluştu.";
};

OBS.OZMIZAN._parseMizanBilgi = function () {
  const raw = OBS.OZMIZAN._el("mizanBilgi")?.value ?? "";
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
OBS.OZMIZAN.ozmizfetchTableData = async function () {
  const req = OBS.OZMIZAN._parseMizanBilgi();

  OBS.OZMIZAN._clearError();

  const tableBody = OBS.OZMIZAN._el("tableBody");
  if (tableBody) tableBody.innerHTML = "";

  const tOnceki = OBS.OZMIZAN._el("totalOncekiBakiye");
  const tBorc = OBS.OZMIZAN._el("totalBorc");
  const tAlacak = OBS.OZMIZAN._el("totalAlacak");
  const tKv = OBS.OZMIZAN._el("totalBakKvartal");
  const tBakiye = OBS.OZMIZAN._el("totalBakiye");
  if (tOnceki) tOnceki.textContent = "";
  if (tBorc) tBorc.textContent = "";
  if (tAlacak) tAlacak.textContent = "";
  if (tKv) tKv.textContent = "";
  if (tBakiye) tBakiye.textContent = "";

  document.body.style.cursor = "wait";
  // senin kodda yenileButton idi
  OBS.OZMIZAN._setBtn("yenileButton", true, "İşleniyor...");

  try {
    const data = await fetchWithSessionCheck("cari/ozelmizan", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(req),
    });

    if (!data?.success) {
      OBS.OZMIZAN._showError(data?.errorMessage || "Bir hata oluştu.");
      return;
    }

    let totalOncekiBakiye = 0;
    let totalBorc = 0;
    let totalAlacak = 0;
    let totalBakKvartal = 0;
    let totalBakiye = 0;

    (data.data || []).forEach(item => {
      const row = document.createElement("tr");
      row.classList.add("table-row-height");
      row.innerHTML = `
        <td>${item.HESAP || ""}</td>
        <td>${item.UNVAN || ""}</td>
        <td>${item.H_CINSI || ""}</td>
        <td class="double-column">${formatNumber2(item.ONCEKI_BAKIYE)}</td>
        <td class="double-column">${formatNumber2(item.BORC)}</td>
        <td class="double-column">${formatNumber2(item.ALACAK)}</td>
        <td class="double-column">${formatNumber2(item.BAK_KVARTAL)}</td>
        <td class="double-column">${formatNumber2(item.BAKIYE)}</td>
      `;
      tableBody?.appendChild(row);

      totalOncekiBakiye += (item.ONCEKI_BAKIYE || 0);
      totalBorc += (item.BORC || 0);
      totalAlacak += (item.ALACAK || 0);
      totalBakKvartal += (item.BAK_KVARTAL || 0);
      totalBakiye += (item.BAKIYE || 0);
    });

    if (tOnceki) tOnceki.textContent = formatNumber2(totalOncekiBakiye);
    if (tBorc) tBorc.textContent = formatNumber2(totalBorc);
    if (tAlacak) tAlacak.textContent = formatNumber2(totalAlacak);
    if (tKv) tKv.textContent = formatNumber2(totalBakKvartal);
    if (tBakiye) tBakiye.textContent = formatNumber2(totalBakiye);

  } catch (e) {
    OBS.OZMIZAN._showError(e?.message || "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.");
  } finally {
    OBS.OZMIZAN._setBtn("yenileButton", false, "Yenile");
    document.body.style.cursor = "default";
  }
};

/* =========================
   DOWNLOAD
   ========================= */
OBS.OZMIZAN.ozmizdownloadReport = async function (format) {
  const req = OBS.OZMIZAN._parseMizanBilgi();
  req.format = format;

  OBS.OZMIZAN._clearError();

  document.body.style.cursor = "wait";
  // senin id’ler:
  OBS.OZMIZAN._setBtn("ozmizindirButton", true, "İşleniyor...");
  OBS.OZMIZAN._setBtn("ozmizyenileButton", true); // sadece disable

  try {
    const response = await fetchWithSessionCheckForDownload("cari/ozelmizan_download", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(req),
    });

    if (!response?.blob) throw new Error("Dosya indirilemedi.");

    const disposition = response.headers.get("Content-Disposition") || "";
    const m = disposition.match(/filename="(.+)"/);
    const fileName = m ? m[1] : "ozel_mizan_rapor.bin";

    const url = window.URL.createObjectURL(response.blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);

  } catch (e) {
    OBS.OZMIZAN._showError(e?.message || "Bilinmeyen bir hata oluştu.");
  } finally {
    OBS.OZMIZAN._setBtn("ozmizindirButton", false, "Rapor İndir");
    OBS.OZMIZAN._setBtn("ozmizyenileButton", false);
    document.body.style.cursor = "default";
  }
};

/* =========================
   MAIL
   ========================= */
OBS.OZMIZAN.ozmizmailAt = function () {
  localStorage.removeItem("tableData");
  localStorage.removeItem("grprapor");
  localStorage.removeItem("tablobaslik");

  const req = OBS.OZMIZAN._parseMizanBilgi();
  const degerler =
    `${req.hkodu1},${req.hkodu2},${req.startDate},${req.endDate},` +
    `${req.cins1},${req.cins2},${req.karton1},${req.karton2},${req.hangi_tur},cariozelmizan`;

  const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
  mailsayfasiYukle(url);
};
