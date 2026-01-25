/* =========================
   KAMBİYO – CEK CIKIS (çakışmasız)
   Namespace: OBS.CEKCIK
   File: /obs_js_files/kambiyo/cekcikis.js
   ========================= */
window.OBS ||= {};
OBS.CEKCIK ||= {};

/* ---------- helpers ---------- */
OBS.CEKCIK._root = () =>
  document.getElementById("kmb_content") ||
  document.getElementById("ara_content") ||
  document;

OBS.CEKCIK.byId = (id) => OBS.CEKCIK._root().querySelector("#" + id);

OBS.CEKCIK._cursor = (wait) => { document.body.style.cursor = wait ? "wait" : "default"; };

OBS.CEKCIK._setErr = (msg) => {
  const e = OBS.CEKCIK.byId("errorDiv") || document.getElementById("errorDiv");
  if (!e) return;
  if (msg) { e.style.display = "block"; e.innerText = msg; }
  else { e.style.display = "none"; e.innerText = ""; }
};

OBS.CEKCIK._btnBusy = (id, busy, busyText, idleText) => {
  const b = OBS.CEKCIK.byId(id) || document.getElementById(id);
  if (!b) return;
  b.disabled = !!busy;
  if (busy && busyText) b.textContent = busyText;
  if (!busy && idleText) b.textContent = idleText;
};

/* ---------- state ---------- */
OBS.CEKCIK.state = {
  rowCounter: 0,
  cekListesi: [],   // (sen bankaIsimleri diyordun ama içerik cek liste)
  topPara: 0
};

/* =========================
   INIT
   ========================= */
OBS.CEKCIK.init = function () {
  // İstersen sayfa açılınca otomatik:
   OBS.CEKCIK.fetchCekNoOnce();
};

/* =========================
   fetchcekno (Cek Listesi)
   ========================= */
OBS.CEKCIK.fetchCekNoOnce = async function () {
  OBS.CEKCIK._setErr("");
  OBS.CEKCIK.state.rowCounter = 0;
  OBS.CEKCIK.state.cekListesi = [];

  try {
    const response = await fetchWithSessionCheck("kambiyo/kamgetCekListe", {
      method: "GET",
      headers: { "Content-Type": "application/json" }
    });

    if (response?.errorMessage) throw new Error(response.errorMessage);

    OBS.CEKCIK.state.cekListesi = response.cekListe || [];
    OBS.CEKCIK.initializeRows();
  } catch (error) {
    OBS.CEKCIK._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
  }
};

OBS.CEKCIK.initializeRows = function () {
  for (let i = 0; i < 5; i++) OBS.CEKCIK.cekcikaddRow();
};

OBS.CEKCIK.incrementRowCounter = function () {
  OBS.CEKCIK.state.rowCounter++;
};

/* boş hücre üretici */
OBS.CEKCIK.emptyCell = function (textAlignRight = false, value = "") {
  return `
    <td>
      <label class="form-control">
        <span class="${textAlignRight ? "cell-right" : "cell-left"}">
          ${value || "&nbsp;"}
        </span>
      </label>
    </td>`;
};

OBS.CEKCIK.cekcikaddRow = function () {
  const tbody = document.querySelector("#gbTable tbody");
  if (!tbody) return;

  const row = tbody.insertRow();
  OBS.CEKCIK.incrementRowCounter();
  const rc = OBS.CEKCIK.state.rowCounter;

  const optionsHTML = OBS.CEKCIK.state.cekListesi
    .map(k => `<option value="${k.Cek_No}">${k.Cek_No}</option>`)
    .join("");

  row.innerHTML = `
    <td>
      <button class="btn btn-secondary" onclick="OBS.CEKCIK.cekcikremoveRow(this)">
        <i class="fa fa-trash"></i>
      </button>
    </td>

    <td>
      <div style="position:relative;">
        <input class="form-control cins_bold"
          id="cekno_${rc}"
          list="cekOptions_${rc}"
          maxlength="10"
          onchange="OBS.CEKCIK.cekkontrol(this)">
        <datalist id="cekOptions_${rc}">
          ${optionsHTML}
        </datalist>
        <span style="position:absolute;right:10px;top:50%;transform:translateY(-50%);pointer-events:none;">▼</span>
      </div>
    </td>

    ${OBS.CEKCIK.emptyCell()}
    ${OBS.CEKCIK.emptyCell()}
    ${OBS.CEKCIK.emptyCell()}
    ${OBS.CEKCIK.emptyCell()}
    ${OBS.CEKCIK.emptyCell()}
    ${OBS.CEKCIK.emptyCell()}
    ${OBS.CEKCIK.emptyCell()}
    ${OBS.CEKCIK.emptyCell(true, "0.00")}
  `;
};

OBS.CEKCIK.cekcikremoveRow = function (button) {
  button.closest("tr")?.remove();
  OBS.CEKCIK.updateColumnTotal();
};

OBS.CEKCIK.selectAllContent = function (element) {
  const range = document.createRange();
  const selection = window.getSelection();
  range.selectNodeContents(element);
  selection.removeAllRanges();
  selection.addRange(range);
};

OBS.CEKCIK.updateColumnTotal = function () {
  const spans = document.querySelectorAll("tr td:nth-child(10) span");
  const totalTutarCell = OBS.CEKCIK.byId("totalTutar") || document.getElementById("totalTutar");
  const totalceksayisi = OBS.CEKCIK.byId("ceksayisi") || document.getElementById("ceksayisi");

  let total = 0;
  let totaladet = 0;

  spans.forEach(span => {
    const value = parseFloat(String(span.textContent).replace(/,/g, "").trim());
    if (!isNaN(value) && value > 0) { total += value; totaladet += 1; }
  });

  if (totalceksayisi) {
    totalceksayisi.innerText = totaladet.toLocaleString(undefined, {
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    });
  }
  if (totalTutarCell) {
    totalTutarCell.textContent = total.toLocaleString(undefined, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }
};

OBS.CEKCIK.focusNextRow = function (event, element) {
  if (event.key !== "Enter") return;
  event.preventDefault();

  const currentRow = element.closest("tr");
  const nextRow = currentRow?.nextElementSibling;

  if (nextRow) {
    const secondInput = nextRow.querySelector("td:nth-child(2) input");
    if (secondInput) { secondInput.focus(); secondInput.select?.(); }
  } else {
    OBS.CEKCIK.cekcikaddRow();
    const tbody = currentRow?.parentElement;
    const newRow = tbody?.lastElementChild;
    const secondInput = newRow?.querySelector("td:nth-child(2) input");
    if (secondInput) { secondInput.focus(); secondInput.select?.(); }
  }
};

/* =========================
   cekkontrol (ccekkontrol)
   ========================= */
OBS.CEKCIK.cekkontrol = async function (element) {
  const cekNo = element?.value || "";
  OBS.CEKCIK._setErr("");
  OBS.CEKCIK._cursor(true);

  try {
    const response = await fetchWithSessionCheck("kambiyo/ccekkontrol", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ cekNo })
    });

    const dto = response;
    if (dto?.errorMessage) {
      OBS.CEKCIK._setErr(dto.errorMessage);
      return;
    }

    const gelenbordro = dto.cikisBordro || "";
    const bordrono = (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono"))?.value || "";

    if (gelenbordro !== "" && gelenbordro !== bordrono) {
      alert("Bu cek daha onceden cikis yapilmis Bordro No:" + gelenbordro);
      return;
    }

    const currentRow = element.closest("tr");
    const cells = currentRow?.getElementsByTagName("td");
    if (!cells) return;

    OBS.CEKCIK.setLabelContent(cells[2], formatDate(dto.vade) || "");
    OBS.CEKCIK.setLabelContent(cells[3], dto.banka || "");
    OBS.CEKCIK.setLabelContent(cells[4], dto.sube || "");
    OBS.CEKCIK.setLabelContent(cells[5], dto.seriNo || "");
    OBS.CEKCIK.setLabelContent(cells[6], dto.ilkBorclu || "");
    OBS.CEKCIK.setLabelContent(cells[7], dto.cekHesapNo || "");
    OBS.CEKCIK.setLabelContent(cells[8], dto.cins || "");
    OBS.CEKCIK.setLabelContent(cells[9], formatNumber2(dto.tutar));

    OBS.CEKCIK.updateColumnTotal();
    OBS.CEKCIK._setErr("");
  } catch (error) {
    OBS.CEKCIK._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    OBS.CEKCIK._cursor(false);
  }
};

/* =========================
   clearInputs
   ========================= */
OBS.CEKCIK.clearInputs = function () {
  (OBS.CEKCIK.byId("ozelkod") || document.getElementById("ozelkod")).value = "";
  (OBS.CEKCIK.byId("dvzcinsi") || document.getElementById("dvzcinsi")).value = "";
  (OBS.CEKCIK.byId("bcheskod") || document.getElementById("bcheskod")).value = "";

  const lblcheskod = OBS.CEKCIK.byId("lblcheskod") || document.getElementById("lblcheskod");
  if (lblcheskod) lblcheskod.innerText = "";

  (OBS.CEKCIK.byId("faiz") || document.getElementById("faiz")).value = "";

  const lblortgun = OBS.CEKCIK.byId("lblortgun") || document.getElementById("lblortgun");
  if (lblortgun) lblortgun.innerText = "0";

  const lblfaiztutari = OBS.CEKCIK.byId("lblfaiztutari") || document.getElementById("lblfaiztutari");
  if (lblfaiztutari) lblfaiztutari.innerText = "0.00";

  (OBS.CEKCIK.byId("aciklama1") || document.getElementById("aciklama1")).value = "";
  (OBS.CEKCIK.byId("aciklama2") || document.getElementById("aciklama2")).value = "";

  const tbody = OBS.CEKCIK.byId("tbody") || document.getElementById("tbody");
  if (tbody) tbody.innerHTML = "";

  OBS.CEKCIK.state.rowCounter = 0;
  OBS.CEKCIK.initializeRows();

  const totalTutarCell = OBS.CEKCIK.byId("totalTutar") || document.getElementById("totalTutar");
  if (totalTutarCell) totalTutarCell.textContent = formatNumber2(0);
};

/* =========================
   soncikisBordro
   ========================= */
OBS.CEKCIK.soncikisBordro = async function () {
  try {
    const response = await fetchWithSessionCheck("kambiyo/csonbordroNo", {
      method: "GET",
      headers: { "Content-Type": "application/x-www-form-urlencoded" }
    });

    const bordroNo = response?.bordroNo;
    const bordronoInput = OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono");
    if (bordronoInput) bordronoInput.value = bordroNo;

    if (bordroNo?.errorMessage) {
      OBS.CEKCIK._setErr(bordroNo.errorMessage);
    } else {
      OBS.CEKCIK._setErr("");
      OBS.CEKCIK.clearInputs();
      await OBS.CEKCIK.bordroOku();
    }
  } catch (error) {
    OBS.CEKCIK._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
  }
};

OBS.CEKCIK.bordroOkuma = function (event) {
  if (event.key === "Enter" || event.keyCode === 13) OBS.CEKCIK.bordroOku();
};

/* =========================
   bordroOku (cbordroOku)
   ========================= */
OBS.CEKCIK.bordroOku = async function () {
  const bordroNo = (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono"))?.value;
  if (!bordroNo) return;

  const errorDiv = OBS.CEKCIK.byId("errorDiv") || document.getElementById("errorDiv");

  OBS.CEKCIK._cursor(true);
  try {
    const response = await fetchWithSessionCheck("kambiyo/cbordroOku", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ bordroNo })
    });

    const data = response;
    OBS.CEKCIK.clearInputs();

    const dataSize = data?.data?.length || 0;
    if (dataSize === 0) return;

    if (data?.errorMessage) {
      OBS.CEKCIK._setErr(data.errorMessage);
      return;
    }

    const table = OBS.CEKCIK.byId("gbTable") || document.getElementById("gbTable");
    const rows = table?.querySelectorAll("tbody tr") || [];

    if (data.data.length > rows.length) {
      const additionalRows = data.data.length - rows.length;
      for (let i = 0; i < additionalRows + 1; i++) OBS.CEKCIK.cekcikaddRow();
    }

    const rowss = table?.querySelectorAll("tbody tr") || [];
    data.data.forEach((item, index) => {
      const cells = rowss[index]?.cells;
      if (!cells) return;

      const ceknoInput = cells[1]?.querySelector("input");
      if (ceknoInput) ceknoInput.value = item.Cek_No || "";

      OBS.CEKCIK.setLabelContent(cells[2], formatDate(item.Vade) || "");
      OBS.CEKCIK.setLabelContent(cells[3], item.Banka || "");
      OBS.CEKCIK.setLabelContent(cells[4], item.Sube || "");
      OBS.CEKCIK.setLabelContent(cells[5], item.Seri_No || "");
      OBS.CEKCIK.setLabelContent(cells[6], item.Ilk_Borclu || "");
      OBS.CEKCIK.setLabelContent(cells[7], item.Cek_Hesap_No || "");
      OBS.CEKCIK.setLabelContent(cells[8], item.Cins || "");
      OBS.CEKCIK.setLabelContent(cells[9], formatNumber2(item.Tutar));
    });

    OBS.CEKCIK.updateColumnTotal();

    (OBS.CEKCIK.byId("ozelkod") || document.getElementById("ozelkod")).value = data.data[0].Cikis_Ozel_Kod;
    (OBS.CEKCIK.byId("dvzcinsi") || document.getElementById("dvzcinsi")).value = data.data[0].Cins;
    (OBS.CEKCIK.byId("bcheskod") || document.getElementById("bcheskod")).value = data.data[0].Cikis_Musteri;
    (OBS.CEKCIK.byId("bordroTarih") || document.getElementById("bordroTarih")).value = data.data[0].Cikis_Tarihi;

    const ches = OBS.CEKCIK.byId("bcheskod") || document.getElementById("bcheskod");
    ches?.oninput?.();

    const responseAciklama = await fetchWithSessionCheck("kambiyo/ckamgetAciklama", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ bordroNo })
    });

    const dataAciklama = responseAciklama;
    (OBS.CEKCIK.byId("aciklama1") || document.getElementById("aciklama1")).value = dataAciklama.aciklama1;
    (OBS.CEKCIK.byId("aciklama2") || document.getElementById("aciklama2")).value = dataAciklama.aciklama2;

    OBS.CEKCIK._setErr("");
  } catch (error) {
    OBS.CEKCIK._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    OBS.CEKCIK._cursor(false);
  }
};

/* =========================
   prepare + tableData
   ========================= */
OBS.CEKCIK.prepareBordroKayit = function () {
  const girisbordroDTO = {
    bordroNo: (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono")).value,
    aciklama1: (OBS.CEKCIK.byId("aciklama1") || document.getElementById("aciklama1")).value || "",
    aciklama2: (OBS.CEKCIK.byId("aciklama2") || document.getElementById("aciklama2")).value || ""
  };
  const tableData = OBS.CEKCIK.getTableData();
  return { girisbordroDTO, tableData };
};

OBS.CEKCIK.getTableData = function () {
  const table = OBS.CEKCIK.byId("gbTable") || document.getElementById("gbTable");
  const rows = table?.querySelectorAll("tbody tr") || [];
  const data = [];

  rows.forEach((row) => {
    const cells = row.querySelectorAll("td");
    const cekNo = cells[1]?.querySelector("input")?.value || "";
    if (!cekNo.trim()) return;

    const rowData = {
      cekNo,
      vade: cells[2]?.textContent,
      tutar: parseLocaleNumber(cells[9]?.textContent),
      cikisBordro: (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono")).value,
      cikisTarihi: (OBS.CEKCIK.byId("bordroTarih") || document.getElementById("bordroTarih")).value,
      cikisMusteri: (OBS.CEKCIK.byId("bcheskod") || document.getElementById("bcheskod")).value,
      cikisOzelKod: (OBS.CEKCIK.byId("ozelkod") || document.getElementById("ozelkod")).value
    };
    data.push(rowData);
  });

  return data;
};

/* =========================
   cbKayit / cbYoket
   ========================= */
OBS.CEKCIK.cbKayit = async function () {
  const bordroNo = (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono"))?.value?.trim();
  if (!bordroNo || bordroNo === "0") { alert("Geçerli bir evrak numarası giriniz."); return; }

  const bordroKayitDTO = OBS.CEKCIK.prepareBordroKayit();
  OBS.CEKCIK._cursor(true);
  OBS.CEKCIK._setErr("");

  try {
    const response = await fetchWithSessionCheck("kambiyo/cbordroKayit", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(bordroKayitDTO)
    });

    if (response?.errorMessage?.trim() !== "") throw new Error(response.errorMessage);

    OBS.CEKCIK.clearInputs();
    (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono")).value = "";
    OBS.CEKCIK._setErr("");
  } catch (error) {
    OBS.CEKCIK._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    OBS.CEKCIK._cursor(false);
  }
};

OBS.CEKCIK.cbYoket = async function () {
  const bordroNo = (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono"))?.value?.trim();
  if (!bordroNo || bordroNo === "0") { alert("Geçerli bir evrak numarası giriniz."); return; }

  if (!confirm("Bu Bordroyu silmek istediğinize emin misiniz?")) return;

  OBS.CEKCIK._cursor(true);
  OBS.CEKCIK._setErr("");

  try {
    const response = await fetchWithSessionCheck("kambiyo/cbordroYoket", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ bordroNo })
    });

    if (response?.errorMessage?.trim() !== "") throw new Error(response.errorMessage);

    // sayfa tekrar yükle (senin changeLink ile)
    document.querySelector('.changeLink[data-url="/kambiyo/cekcikis"]')?.click();

    (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono")).value = "";
    OBS.CEKCIK._setErr("");
  } catch (error) {
    OBS.CEKCIK._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    OBS.CEKCIK._cursor(false);
  }
};

/* =========================
   ortgun / tarih fark
   ========================= */
OBS.CEKCIK.ortgun = function () {
  const table = OBS.CEKCIK.byId("gbTable") || document.getElementById("gbTable");
  const rows = table?.querySelectorAll("tbody tr") || [];

  const gunlukTarih = (OBS.CEKCIK.byId("bordroTarih") || document.getElementById("bordroTarih")).value;
  const faizoran = parseLocaleNumber((OBS.CEKCIK.byId("faiz") || document.getElementById("faiz")).value);

  let topPara = 0, tfaiz = 0, orgun = 0;

  rows.forEach((row) => {
    const tutar = parseLocaleNumber(row.querySelector("td:nth-child(10) span")?.textContent || "0");
    const vade = row.querySelector("td:nth-child(3) span")?.textContent; // vade span

    if (tutar > 0 && vade && gunlukTarih) {
      const gunfarki = OBS.CEKCIK.tarihGunFarki(gunlukTarih, vade);
      const faiz = (((tutar * faizoran) / 365) * gunfarki) / 100;
      topPara += tutar;
      tfaiz += faiz;
      orgun = ((topPara * faizoran) / 365) / 100;
    }
  });

  (OBS.CEKCIK.byId("lblortgun") || document.getElementById("lblortgun")).innerText =
    (tfaiz / orgun).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  (OBS.CEKCIK.byId("lblfaiztutari") || document.getElementById("lblfaiztutari")).innerText =
    tfaiz.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};

OBS.CEKCIK.tarihGunFarki = function (tarih1, tarih2) {
  const date1 = new Date(tarih1); // yyyy-mm-dd
  const [gun, ay, yil] = String(tarih2).split(".");
  const date2 = new Date(`${yil}-${ay}-${gun}`);
  const farkMs = date2 - date1;
  return Math.ceil(farkMs / (1000 * 60 * 60 * 24));
};

/* =========================
   yeniBordro
   ========================= */
OBS.CEKCIK.yeniBordro = async function () {
  try {
    const response = await fetchWithSessionCheck("kambiyo/cyeniBordro", {
      method: "GET",
      headers: { "Content-Type": "application/x-www-form-urlencoded" }
    });

    (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono")).value = response.bordroNo;

    if (response?.errorMessage?.trim() !== "") throw new Error(response.errorMessage);

    OBS.CEKCIK._setErr("");
    OBS.CEKCIK.clearInputs();
  } catch (error) {
    OBS.CEKCIK._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
  }
};

/* =========================
   cbDownload (jQuery temiz)
   ========================= */
OBS.CEKCIK.cbDownload = async function () {
  const bordroNo = (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono"))?.value?.trim();
  if (!bordroNo || bordroNo === "0") { alert("Geçerli bir evrak numarası giriniz."); return; }

  const table = OBS.CEKCIK.byId("gbTable") || document.getElementById("gbTable");
  const rows = table?.querySelectorAll("tbody tr") || [];

  let topPara = 0;
  rows.forEach((row) => {
    const tutar = parseLocaleNumber(row.querySelector("td:nth-child(10) span")?.textContent || "0");
    topPara += tutar;
  });

  const bordroPrinter = {
    cikisBordro: (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono")).value,
    cikisTarihi: (OBS.CEKCIK.byId("bordroTarih") || document.getElementById("bordroTarih")).value,
    cikisMusteri: (OBS.CEKCIK.byId("bcheskod") || document.getElementById("bcheskod")).value,
    unvan: (OBS.CEKCIK.byId("lblcheskod") || document.getElementById("lblcheskod")).innerText || "",
    dvzcins: (OBS.CEKCIK.byId("dvzcinsi") || document.getElementById("dvzcinsi")).value || "",
    tutar: topPara || 0
  };

  OBS.CEKCIK._setErr("");
  OBS.CEKCIK._cursor(true);
  OBS.CEKCIK._btnBusy("indirButton", true, "İşleniyor...", "Rapor İndir");

  try {
    const response = await fetchWithSessionCheckForDownload("kambiyo/cbordro_download", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(bordroPrinter)
    });

    if (response?.blob) {
      const disposition = response.headers.get("Content-Disposition") || "";
      const m = disposition.match(/filename="(.+)"/);
      const fileName = m ? m[1] : "cbordro.xlsx";

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
    OBS.CEKCIK._setErr(error?.message || "Bilinmeyen bir hata oluştu.");
  } finally {
    OBS.CEKCIK._btnBusy("indirButton", false, "İşleniyor...", "Rapor İndir");
    OBS.CEKCIK._cursor(false);
  }
};

/* =========================
   cekcariIsle (jQuery temiz)
   ========================= */
OBS.CEKCIK.cekcariIsle = async function () {
  const hesapKodu = (OBS.CEKCIK.byId("cekcikBilgi") || document.getElementById("cekcikBilgi"))?.value || "";
  const bordroNo = (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono"))?.value?.trim();

  if (!bordroNo || bordroNo === "0") { alert("Geçerli bir evrak numarası giriniz."); return; }

  const bordroKayitDTO = OBS.CEKCIK.prepareBordroKayit();
  bordroKayitDTO.girisbordroDTO.hesapKodu = hesapKodu;

  OBS.CEKCIK._setErr("");
  OBS.CEKCIK._cursor(true);

  try {
    const response = await fetchWithSessionCheck("kambiyo/cbordroCariKayit", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(bordroKayitDTO)
    });

    if (response?.errorMessage?.trim() !== "") throw new Error(response.errorMessage);

    OBS.CEKCIK._setErr("");
  } catch (error) {
    OBS.CEKCIK._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    OBS.CEKCIK._cursor(false);
  }
};

/* =========================
   cbmailAt
   ========================= */
OBS.CEKCIK.cbmailAt = function () {
  const bordroNo = (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono"))?.value?.trim();
  if (!bordroNo || bordroNo === "0") { alert("Geçerli bir evrak numarası giriniz."); return; }

  const table = OBS.CEKCIK.byId("gbTable") || document.getElementById("gbTable");
  const rows = table?.querySelectorAll("tbody tr") || [];

  let topPara = 0;
  rows.forEach((row) => {
    const tutar = parseLocaleNumber(row.querySelector("td:nth-child(10) span")?.textContent || "0");
    topPara += tutar;
  });

  const cikisBordro = (OBS.CEKCIK.byId("bordrono") || document.getElementById("bordrono")).value;
  const cikisTarihi = (OBS.CEKCIK.byId("bordroTarih") || document.getElementById("bordroTarih")).value;
  const cikisMusteri = (OBS.CEKCIK.byId("bcheskod") || document.getElementById("bcheskod")).value;
  const unvan = (OBS.CEKCIK.byId("lblcheskod") || document.getElementById("lblcheskod")).innerText || "";
  const dvzcins = (OBS.CEKCIK.byId("dvzcinsi") || document.getElementById("dvzcinsi")).value || "";
  const tutar = topPara || 0;

  const degerler = cikisBordro + "," + cikisTarihi + "," + cikisMusteri + "," + unvan + "," + dvzcins + "," + tutar + ",ccbordro";
  const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
  mailsayfasiYukle(url);
};

/* =========================
   setLabelContent
   ========================= */
OBS.CEKCIK.setLabelContent = function (cell, content) {
  const span = cell?.querySelector("label span");
  if (span) span.textContent = content ? content : "\u00A0";
};
