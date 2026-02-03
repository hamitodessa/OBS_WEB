/* =========================
   KAMBİYO – CEK RAPOR (çakışmasız)
   Namespace: OBS.CEKRAP
   ========================= */
window.OBS ||= {};
OBS.CEKRAP ||= {};

/* ---------- helpers ---------- */
OBS.CEKRAP._root = () =>
  document.getElementById("kmb_content") ||
  document.getElementById("ara_content") ||
  document;

OBS.CEKRAP.byId = (id) => OBS.CEKRAP._root().querySelector("#" + id);

OBS.CEKRAP._cursor = (wait) => { document.body.style.cursor = wait ? "wait" : "default"; };

OBS.CEKRAP._setErr = (msg) => {
  const e = OBS.CEKRAP.byId("errorDiv") || document.getElementById("errorDiv");
  if (!e) return;
  if (msg) { e.style.display = "block"; e.innerText = msg; }
  else { e.style.display = "none"; e.innerText = ""; }
};

OBS.CEKRAP._btn = (id) => OBS.CEKRAP.byId(id) || document.getElementById(id);

OBS.CEKRAP._setBtn = (id, disabled, text) => {
  const b = OBS.CEKRAP._btn(id);
  if (!b) return;
  b.disabled = !!disabled;
  if (typeof text === "string") b.textContent = text;
};

OBS.CEKRAP._getHiddenParsed = () => {
  const el = OBS.CEKRAP.byId("cekrapBilgi") || document.getElementById("cekrapBilgi");
  const raw = (el?.value ?? "").trim();
  const parts = raw.split(",");
  // eksik gelirse boş string ile tamamlayalım
  while (parts.length < 25) parts.push("");
  return parts;
};

OBS.CEKRAP._buildDTO = () => {
  const p = OBS.CEKRAP._getHiddenParsed();
  return {
    cekno1: p[0],  cekno2: p[1],
    durum1: p[2],  durum2: p[3],
    vade1:  p[4],  vade2:  p[5],
    ttar1:  p[6],  ttar2:  p[7],
    gbor1:  p[8],  gbor2:  p[9],
    gozel:  p[10], cozel:  p[11],
    gtar1:  p[12], gtar2:  p[13],
    cins1:  p[14], cins2:  p[15],
    cbor1:  p[16], cbor2:  p[17],
    ches1:  p[18], ches2:  p[19],
    ctar1:  p[20], ctar2:  p[21],
    hangi_tur: p[22],
    ghes1:  p[23], ghes2:  p[24],
  };
};

OBS.CEKRAP._clearTable = () => {
  const body = OBS.CEKRAP.byId("tableBody") || document.getElementById("tableBody");
  if (body) body.innerHTML = "";
};

OBS.CEKRAP._appendRow = (item) => {
  const body = OBS.CEKRAP.byId("tableBody") || document.getElementById("tableBody");
  if (!body) return;

  const tr = document.createElement("tr");
  tr.classList.add("table-row-height");
  tr.innerHTML = `
    <td>${item.Cek_No || ""}</td>
    <td>${item.Vade ? formatDate(item.Vade) : ""}</td>
    <td>${item.Giris_Bordro || ""}</td>
    <td>${item.Giris_Tarihi ? formatDate(item.Giris_Tarihi) : ""}</td>
    <td>${item.Giris_Musteri || ""}</td>
    <td>${item.Banka || ""}</td>
    <td>${item.Sube || ""}</td>
    <td>${item.Cins || ""}</td>
    <td class="double-column">${formatNumber2(item.Tutar)}</td>
    <td>${item.Durum || ""}</td>
    <td>${item.T_Tarih || ""}</td>
    <td>${item.Giris_Ozel_Kod || ""}</td>
    <td>${item.Cikis_Bordro || ""}</td>
    <td>${item.Cikis_Tarihi || ""}</td>
    <td>${item.Cikis_Musteri || ""}</td>
    <td>${item.Cikis_Ozel_Kod || ""}</td>
    <td>${item.USER || ""}</td>
  `;
  body.appendChild(tr);
};

/* =========================
   INIT
   ========================= */
OBS.CEKRAP.init = function () {
  // İstersen sayfa açılınca otomatik yenile:
  // OBS.CEKRAP.fetchTableData();

  // Yenile butonuna event bağlayalım (onclick yazmak istemezsen)
  
};

/* =========================
   TABLE DOLDUR
   ========================= */
OBS.CEKRAP.fetchTableData = async function () {
  const dto = OBS.CEKRAP._buildDTO();

  OBS.CEKRAP._setErr("");
  OBS.CEKRAP._clearTable();
  OBS.CEKRAP._cursor(true);

  // senin eski id: rapyenileButton (bazı sayfalarda cekrapyenileButton olabiliyor)
  const yenileId = (OBS.CEKRAP._btn("rapyenileButton") ? "rapyenileButton" : "cekrapyenileButton");
  OBS.CEKRAP._setBtn(yenileId, true, "İşleniyor...");

  try {
    const data = await fetchWithSessionCheck("kambiyo/cekraporlama", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dto),
    });

    if (data?.success) {
      (data.data || []).forEach((item) => OBS.CEKRAP._appendRow(item));
    } else {
      OBS.CEKRAP._setErr(data?.errorMessage || "Bir hata oluştu.");
    }
  } catch (error) {
    OBS.CEKRAP._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    OBS.CEKRAP._setBtn(yenileId, false, "Yenile");
    OBS.CEKRAP._cursor(false);
  }
};

/* =========================
   RAPOR İNDİR
   ========================= */
OBS.CEKRAP.downloadReport = async function () {
  OBS.CEKRAP._setErr("");
  OBS.CEKRAP._cursor(true);

  OBS.CEKRAP._setBtn("cekrapreportFormat", true, "İşleniyor...");
  OBS.CEKRAP._setBtn("cekrapyenileButton", true);

  try {
    const rows = OBS.CEKRAP.extractTableData("main-table");

    const response = await fetchWithSessionCheckForDownload("kambiyo/cekrap_download", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(rows)
    });

    if (response?.blob) {
      const disposition = response.headers.get("Content-Disposition") || "";
      const m = disposition.match(/filename="(.+)"/);
      const fileName = m ? m[1] : "cekrapor.xlsx";

      const url = window.URL.createObjectURL(response.blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } else {
      throw new Error("Dosya indirilemedi.");
    }
  } catch (error) {
    OBS.CEKRAP._setErr(error?.message || "Bilinmeyen bir hata oluştu.");
  } finally {
    OBS.CEKRAP._setBtn("cekrapreportFormat", false, "Rapor İndir");
    OBS.CEKRAP._setBtn("cekrapyenileButton", false);
    OBS.CEKRAP._cursor(false);
  }
};

/* =========================
   MAIL AT
   ========================= */
OBS.CEKRAP.mailAt = function () {
  OBS.CEKRAP._cursor(true);
  try {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    const rows = OBS.CEKRAP.extractTableData("main-table");
    localStorage.setItem("tableData", JSON.stringify({ rows }));

    const degerler = "cekrap";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);
  } finally {
    OBS.CEKRAP._cursor(false);
  }
};

/* =========================
   TABLO DATA ÇIKAR
   ========================= */
OBS.CEKRAP.extractTableData = function (tableId) {
  const table = document.querySelector(`#${tableId}`);
  if (!table) return [];

  const headers = [];
  const rows = [];

  table.querySelectorAll("thead th").forEach((th) => headers.push(th.innerText.trim()));

  table.querySelectorAll("tbody tr").forEach((tr) => {
    const rowData = {};
    let nonEmptyCount = 0;

    tr.querySelectorAll("td").forEach((td, index) => {
      const value = td.innerText.trim();
      if (value !== "") nonEmptyCount++;
      rowData[headers[index] || ("COL_" + index)] = value;
    });

    if (nonEmptyCount > 0) rows.push(rowData);
  });

  const tfoot = table.querySelector("tfoot");
  if (tfoot) {
    const tfootRowData = {};
    let nonEmptyCount = 0;

    tfoot.querySelectorAll("th").forEach((th, index) => {
      const value = th.innerText.trim();
      if (value !== "") nonEmptyCount++;
      tfootRowData[headers[index] || ("COL_" + index)] = value;
    });

    if (nonEmptyCount > 0) rows.push(tfootRowData);
  }

  return rows;
};
