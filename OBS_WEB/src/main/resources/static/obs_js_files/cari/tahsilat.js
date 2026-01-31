window.OBS = window.OBS || {};
OBS.TAHSILAT = OBS.TAHSILAT || {};

(function (M) {
  /* =========================
     helpers
     ========================= */
  M.byId = (id) => document.getElementById(id);

  M._showError = function (msg) {
    const e = M.byId("errorDiv");
    if (!e) return;
    e.innerText = msg || "Beklenmeyen bir hata oluştu.";
    e.style.display = "block";
  };

  M._clearError = function () {
    const e = M.byId("errorDiv");
    if (!e) return;
    e.innerText = "";
    e.style.display = "none";
  };

  M._setCursorWait = () =>
    requestAnimationFrame(() => (document.body.style.cursor = "wait"));

  M._setCursorDefault = () =>
    requestAnimationFrame(() => (document.body.style.cursor = "default"));

  M._toggle = function (id, show) {
    const el = M.byId(id);
    if (el) el.style.display = show ? "block" : "none";
  };

  /* =========================
     state
     ========================= */
  M.state = {
    rowCounter: 0,
    bankaIsimleri: [],
    subeIsimleri: [],
  };

  M._setRowCounter = function (v) {
    M.state.rowCounter = v || 0;
  };

  M._incRowCounter = function () {
    M.state.rowCounter += 1;
    return M.state.rowCounter;
  };

  M._rc = function () {
    return M.state.rowCounter;
  };

  /* =========================
     focus handlers (TAHSILAT'a özel)
     ========================= */
  M.focusNextCell = function (event, element) {
    if (event.key === "Enter") {
      event.preventDefault();

      const currentCell = element.closest("td");
      const nextCell = currentCell?.nextElementSibling;

      if (nextCell) {
        const focusable = nextCell.querySelector("input, [contenteditable='true']");
        if (focusable) {
          focusable.focus();
          focusable.select?.();
        } else {
          nextCell.focus?.();
        }
      }
    }
  };

  M.focusNextRow = function (event, element) {
    if (event.key === "Enter") {
      event.preventDefault();

      const currentRow = element.closest("tr");
      const nextRow = currentRow?.nextElementSibling;

      if (nextRow) {
        const secondInput = nextRow.querySelector("td:nth-child(2) input");
        if (secondInput) {
          secondInput.focus();
          secondInput.select?.();
        }
      } else {
        M.addRow();
        const tbody = currentRow?.parentElement; // tbody
        const newRow = tbody?.lastElementChild;
        const secondInput = newRow?.querySelector("td:nth-child(2) input");
        if (secondInput) {
          secondInput.focus();
          secondInput.select?.();
        }
      }
    }
  };

  /* =========================
     UI: tür değişimi
     ========================= */
  M.turChange = function () {
    const cins = M.byId("tur")?.value || "";

    if (cins === "Cek") {
      M._toggle("cekbilgidiv", true);
      M._toggle("divcekbilgi", true);
      M._toggle("posbilgidiv", false);
      M._toggle("divposbilgi", false);
      const tab2 = M.byId("tab2-tab");
      if (tab2) tab2.disabled = false;
    } else if (cins === "Kredi Karti") {
      M._toggle("cekbilgidiv", false);
      M._toggle("divcekbilgi", false);
      M._toggle("posbilgidiv", true);
      M._toggle("divposbilgi", true);
      const tab2 = M.byId("tab2-tab");
      if (tab2) tab2.disabled = true;
    } else {
      M._toggle("cekbilgidiv", false);
      M._toggle("divcekbilgi", false);
      M._toggle("posbilgidiv", false);
      M._toggle("divposbilgi", false);
      const tab2 = M.byId("tab2-tab");
      if (tab2) tab2.disabled = true;
    }
  };

  /* =========================
     Çek tablo: değişkenleri al
     ========================= */
  M.fetchHesapKodlariOnce = async function () {
    M._clearError();
    M._setRowCounter(0);
    M.state.bankaIsimleri = [];
    M.state.subeIsimleri = [];

    try {
      const response = await fetchWithSessionCheck("cari/getDegiskenler", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      M.state.bankaIsimleri = response?.bankaIsmi || [];
      M.state.subeIsimleri = response?.subeIsmi || [];

      M.initializeRows();
    } catch (e) {
      M._showError(e?.message || "Beklenmeyen bir hata oluştu.");
    }
  };

  M.initializeRows = function () {
    const tbody = document.querySelector("#cekTable tbody");
    if (tbody) tbody.innerHTML = "";
    M._setRowCounter(0);

    for (let i = 0; i < 10; i++) M.addRow();
    M.updateColumnTotal();
  };

  M.addRow = function () {
    const tb = M.byId("cekTable")?.getElementsByTagName("tbody")?.[0];
    if (!tb) return;

    const newRow = tb.insertRow();
    const rc = M._incRowCounter();

    const optionsHTML = (M.state.bankaIsimleri || [])
      .map((kod) => `<option value="${kod.BANKA}">${kod.BANKA}</option>`)
      .join("");

    const subeHTML = (M.state.subeIsimleri || [])
      .map((kod) => `<option value="${kod.SUBE}">${kod.SUBE}</option>`)
      .join("");

    newRow.innerHTML = `
      <td>
        <button id="bsatir_${rc}" type="button" class="btn btn-secondary ml-2"
          onclick="OBS.TAHSILAT.cekgirremoveRow(this)">
          <i class="fa fa-trash"></i>
        </button>
      </td>

      <td>
        <div style="position: relative; width: 100%;">
          <input class="form-control" list="bankaOptions_${rc}"
            maxlength="40" id="banka_${rc}"
            onkeydown="OBS.TAHSILAT.focusNextCell(event, this)">
          <datalist id="bankaOptions_${rc}">${optionsHTML}</datalist>
          <span style="position:absolute; top:50%; right:10px; transform:translateY(-50%); pointer-events:none;"> ▼ </span>
        </div>
      </td>

      <td>
        <div style="position: relative; width: 100%;">
          <input class="form-control" list="subeOptions_${rc}"
            maxlength="40" id="sube_${rc}"
            onkeydown="OBS.TAHSILAT.focusNextCell(event, this)">
          <datalist id="subeOptions_${rc}">${subeHTML}</datalist>
          <span style="position:absolute; top:50%; right:10px; transform:translateY(-50%); pointer-events:none;"> ▼ </span>
        </div>
      </td>

      <td><input class="form-control" onkeydown="OBS.TAHSILAT.focusNextCell(event, this)"></td>
      <td><input class="form-control" onkeydown="OBS.TAHSILAT.focusNextCell(event, this)"></td>
      <td><input class="form-control" onkeydown="OBS.TAHSILAT.focusNextCell(event, this)"></td>
      <td><input type="date" class="form-control" onkeydown="OBS.TAHSILAT.focusNextCell(event, this)"></td>

      <td>
        <input class="form-control"
          onfocus="OBS.TAHSILAT.selectAllContent(this)"
          onblur="OBS.TAHSILAT.handleBlur(this)"
          onkeydown="OBS.TAHSILAT.focusNextRow(event, this)"
          value="${formatNumber2(0)}" style="text-align:right;">
      </td>
    `;
  };

  M.cekgirremoveRow = function (button) {
    const row = button?.parentElement?.parentElement;
    if (row) row.remove();
    M.updateColumnTotal();
  };

  M.handleBlur = function (input) {
    input.value = formatNumber2(parseLocaleNumber(input.value));
    M.updateColumnTotal();
  };

  M.selectAllContent = function (el) {
    if (el && typeof el.select === "function") el.select();
  };

  M.updateColumnTotal = function () {
    const cells = document.querySelectorAll("tr td:nth-child(8) input");
    const tutarToplam = M.byId("tutar");
    const totalTutarCell = M.byId("totalTutar");
    const totalceksayisi = M.byId("ceksayisi");

    let total = 0;
    let totaladet = 0;

    cells.forEach((cell) => {
      const value = parseFloat((cell.value || "").replace(/,/g, "").trim());
      if (!isNaN(value) && value > 0) {
        total += value;
        totaladet += 1;
      }
    });

    if (tutarToplam) {
      tutarToplam.value = total.toLocaleString(undefined, {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      });
    }

    if (totalceksayisi) totalceksayisi.innerText = String(totaladet);

    if (totalTutarCell) {
      totalTutarCell.textContent = total.toLocaleString(undefined, {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      });
    }
  };

  /* =========================
     evrak okuma
     ========================= */
  M.tahevrakOkuma = function (event) {
    if (event.key === "Enter" || event.keyCode === 13) M.tahevrakOku();
  };

  M.tahevrakOku = async function () {
    const evrakNo = M.byId("tahevrakNo")?.value;
    const tah_ted = (M.byId("tah_ted")?.value === "Tahsilat") ? 0 : 1;

    if (!evrakNo || evrakNo === "0") return;

    M._setCursorWait();

    try {
      const dto = await fetchWithSessionCheck("/cari/tahevrakOku", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ evrakNo, tah_ted }),
      });

      M.tahclearInputs();

      if (dto?.errorMessage) {
        M._showError(dto.errorMessage);
        return;
      }
      if (dto?.fisNo === null) return;

      // tur set
      if (dto.tur === 0) M.byId("tur").value = "Nakit";
      else if (dto.tur === 1) M.byId("tur").value = "Cek";
      else M.byId("tur").value = "Kredi Karti";

      // çek döküm
      if (dto.tur === 1) {
        const r = await fetchWithSessionCheck("cari/tahcekdokum", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: new URLSearchParams({ evrakNo, tah_ted }),
        });

        const table = M.byId("cekTable");
        const rows = table?.querySelectorAll("tbody tr") || [];

        if (r?.data?.length > rows.length) {
          const add = r.data.length - rows.length;
          for (let i = 0; i < add + 1; i++) M.addRow();
        }

        if (r?.success) {
          const rowss = table.querySelectorAll("tbody tr");
          r.data.forEach((item, index) => {
            const cells = rowss[index].cells;

            const bankaInput = cells[1]?.querySelector("input");
            if (bankaInput) bankaInput.value = item.BANKA || "";

            const subeInput = cells[2]?.querySelector("input");
            if (subeInput) subeInput.value = item.SUBE || "";

            const serinoInput = cells[3]?.querySelector("input");
            if (serinoInput) serinoInput.value = item.SERI || "";

            const hesapInput = cells[4]?.querySelector("input");
            if (hesapInput) hesapInput.value = item.HESAP || "";

            const borcluInput = cells[5]?.querySelector("input");
            if (borcluInput) borcluInput.value = item.BORCLU || "";

            const vadeInput = cells[6]?.querySelector("input");
            if (vadeInput) vadeInput.value = formatdateSaatsiz(item.TARIH) || "";

            const tutarInput = cells[7]?.querySelector("input");
            if (tutarInput) tutarInput.value = formatNumber2(item.TUTAR);
          });
        }

        M.updateColumnTotal();
      }

      M.turChange();

      // diğer alanlar
      M.byId("tahTarih").value = dto.tahTarih;
      M.byId("tutar").value = formatNumber2(dto.tutar);
      M.byId("tcheskod").value = dto.tcheskod || "";
      M.byId("adresheskod").value = dto.adresheskod || "";
      M.byId("dvz_cins").text = dto.dvz_cins || "TL";
      M.byId("posBanka").value = dto.posBanka || "";

      // senin mevcut oninput tetikleri
      M.byId("tcheskod")?.oninput?.();
      M.byId("adresheskod")?.oninput?.();

      M._clearError();
    } catch (e) {
      M._showError(e?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
      M._setCursorDefault();
    }
  };

  /* =========================
     clear / reset
     ========================= */
  M.tahclearInputs = function () {
    M.byId("tutar").value = formatNumber2(0);
    M.byId("tcheskod").value = "";
    M.byId("adresheskod").value = "";
    M.byId("posBanka").value = "";
    M.byId("dvz_cins").value = "TL";
    M.byId("lblcheskod").innerText = "";
    M.byId("lbladrheskod").innerText = "";
    M.byId("ceksayisi").innerText = "0";

    const tableBody = M.byId("tableBody");
    if (tableBody) tableBody.innerHTML = "";

    M.initializeRows();

    const totalTutarCell = M.byId("totalTutar");
    if (totalTutarCell) totalTutarCell.textContent = formatNumber2(0);
  };

  M.tah_ted_cins_clear = function () {
    M.byId("tah_ted").value = "Tahsilat";
    M.byId("tur").value = "Nakit";
  };

  /* =========================
     fis navigation
     ========================= */
  M.tahgerifisNo = function () {
    const fisNoInput = M.byId("tahevrakNo");
    if (!fisNoInput) return;

    const currentValue = parseInt(fisNoInput.value, 10) || 0;
    if (currentValue <= 0) return;

    fisNoInput.value = currentValue - 1;
    M.tah_ted_cins_clear();
    M.turChange();
    M.tahevrakOku();
  };

  M.tahilerifisNo = function () {
    const fisNoInput = M.byId("tahevrakNo");
    if (!fisNoInput) return;

    const currentValue = parseInt(fisNoInput.value, 10) || 0;
    fisNoInput.value = currentValue + 1;

    M.tah_ted_cins_clear();
    M.turChange();
    M.tahevrakOku();
  };

  M.tahsonfisNo = async function () {
    const tah_ted = (M.byId("tah_ted")?.value === "Tahsilat") ? 0 : 1;

    try {
      const data = await fetchWithSessionCheck("/cari/tahsonfisNo", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ tah_ted }),
      });

      const fisNoInput = M.byId("tahevrakNo");
      if (fisNoInput) fisNoInput.value = data.fisNo;

      if (data.fisNo === 0) {
        alert("Hata: Evrak numarası bulunamadı.");
        M._showError(data.errorMessage || "Bilinmeyen bir hata.");
        return;
      }

      if (data?.errorMessage) {
        M._showError(data.errorMessage);
      } else {
        M._clearError();
        M.tah_ted_cins_clear();
        M.turChange();
        M.tahevrakOku();
      }
    } catch (e) {
      M._showError(e?.message || "Beklenmeyen bir hata oluştu.");
    }
  };

  M.tahyenifis = async function () {
    const tah_ted = (M.byId("tah_ted")?.value === "Tahsilat") ? "GIR" : "CIK";

    try {
      const data = await fetchWithSessionCheck("/cari/tahyenifisNo", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ tah_ted }),
      });

      const fisNoInput = M.byId("tahevrakNo");
      if (fisNoInput) fisNoInput.value = data.fisNo;

      if (data.fisNo === 0) {
        M._showError(data.errorMessage || "Bilinmeyen bir hata.");
        return;
      }

      if (data?.errorMessage) {
        M._showError(data.errorMessage);
      } else {
        M._clearError();
        M.tahclearInputs();
        M.turChange();
      }
    } catch (e) {
      M._showError(e?.message || "Beklenmeyen bir hata oluştu.");
    }
  };

  /* =========================
     table data / payload
     ========================= */
  M.getTableData = function () {
    const table = M.byId("cekTable");
    const rows = table?.querySelectorAll("tbody tr") || [];
    const data = [];

    rows.forEach((row) => {
      const cells = row.querySelectorAll("td");
      const banka = cells[1]?.querySelector("input")?.value || "";
      const tutar = parseLocaleNumber(cells[7]?.querySelector("input")?.value || "0");

      if (!banka.trim() || tutar <= 0) return;

      data.push({
        banka,
        sube: cells[2]?.querySelector("input")?.value || "",
        seri: cells[3]?.querySelector("input")?.value || "",
        hesap: cells[4]?.querySelector("input")?.value || "",
        borclu: cells[5]?.querySelector("input")?.value || "",
        tarih: cells[6]?.querySelector("input")?.value || "",
        tutar,
      });
    });

    return data;
  };

  M.prepareRequestPayload = function () {
    let turu = 2;
    const turVal = M.byId("tur")?.value;
    if (turVal === "Nakit") turu = 0;
    else if (turVal === "Cek") turu = 1;

    let tah_ted = 0;
    const tt = M.byId("tah_ted")?.value;
    if (tt === "Tahsilat") tah_ted = 0;
    else if (tt === "Tediye") tah_ted = 1;

    const tahsilatDTO = {
      tah_ted,
      fisNo: M.byId("tahevrakNo")?.value || "",
      tahTarih: getFullDateWithTimeAndMilliseconds(M.byId("tahTarih")?.value),
      tutar: parseLocaleNumber(M.byId("tutar")?.value),
      tcheskod: M.byId("tcheskod")?.value || "",
      adresheskod: M.byId("adresheskod")?.value || "",
      posBanka: M.byId("posBanka")?.value || "",
      dvz_cins: M.byId("dvz_cins")?.value || "",
      tur: turu,
      fisnoyazdir: !!M.byId("fisnoyazdir")?.checked,
    };

    const tableData = (turVal === "Cek") ? M.getTableData() : null;
    return { tahsilatDTO, tableData };
  };

  /* =========================
     save / delete / download / cari
     ========================= */
  M.tahfisKayit = async function () {
    const payload = M.prepareRequestPayload();
    M._clearError();

    try {
      const response = await fetchWithSessionCheck("cari/tahsilatKayit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (response?.errorMessage?.trim() !== "") throw new Error(response.errorMessage);

      M.tahclearInputs();
      M.tah_ted_cins_clear();
      M.turChange();
      M.byId("tahevrakNo").value = "0";
      M._clearError();
    } catch (e) {
      M._showError(e?.message || "Beklenmeyen bir hata oluştu.");
    }
  };

  M.tahfisYoket = async function () {
    const evrakNo = M.byId("tahevrakNo")?.value;
    const silBtn = M.byId("tahsilButton");
    const tah_ted = (M.byId("tah_ted")?.value === "Tahsilat") ? 0 : 1;

    if (!evrakNo || evrakNo === "0") {
      alert("Geçerli bir evrak numarası giriniz.");
      return;
    }
    if (!confirm("Bu evrak numarasını silmek istediğinize emin misiniz?")) return;

    M._clearError();
    M._setCursorWait();
    if (silBtn) { silBtn.disabled = true; silBtn.innerText = "Siliniyor..."; }

    try {
      const dto = await fetchWithSessionCheck("/cari/tahfisYoket", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ evrakNo, tah_ted }),
      });

      if (dto?.errorMessage?.trim() !== "") throw new Error(dto.errorMessage);

      M.tahclearInputs();
      M.tah_ted_cins_clear();
      M.turChange();
      M.byId("tahevrakNo").value = "0";
      M._clearError();
    } catch (e) {
      M._showError(e?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
      M._setCursorDefault();
      if (silBtn) { silBtn.disabled = false; silBtn.innerText = "Sil"; }
    }
  };

  M.tahsilatdownloadReport = async function () {
    const payload = M.prepareRequestPayload();
    const indirBtn = M.byId("indirButton");

    M._clearError();
    M._setCursorWait();
    if (indirBtn) { indirBtn.disabled = true; indirBtn.innerText = "İşleniyor..."; }

    try {
      const response = await fetchWithSessionCheckForDownload("cari/tahsilat_download", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!response?.blob) throw new Error("Dosya indirilemedi.");

      const disposition = response.headers.get("Content-Disposition") || "";
      const m = disposition.match(/filename="(.+)"/);
      const fileName = m ? m[1] : "rapor.bin";

      const url = window.URL.createObjectURL(response.blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (e) {
      M._showError(e?.message || "Bilinmeyen bir hata oluştu.");
    } finally {
      if (indirBtn) { indirBtn.disabled = false; indirBtn.innerText = "Rapor İndir"; }
      M._setCursorDefault();
    }
  };

  M.tahcariIsle = async function () {
    const hesapKodu = M.byId("tahsilatBilgi")?.value ?? "";
    const payload = M.prepareRequestPayload();
    payload.tahsilatDTO.borc_alacak = hesapKodu;

    M._clearError();

    try {
      const dto = await fetchWithSessionCheck("cari/tahsilatCariKayit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (dto?.errorMessage?.trim() !== "") throw new Error(dto.errorMessage);
      M._clearError();
    } catch (e) {
      M._showError(e?.message || "Beklenmeyen bir hata oluştu.");
    }
  };
})(OBS.TAHSILAT);

/* =========================================================
   Geriye dönük uyumluluk (sayfanda eski isimler çağrılıyorsa)
   NOT: focusNextCell/focusNextRow GLOBAL YOK -> çakışma bitti
   ========================================================= */
function turChange() { return OBS.TAHSILAT.turChange(); }
async function fetchHesapKodlariOnce() { return OBS.TAHSILAT.fetchHesapKodlariOnce(); }
function initializeRows() { return OBS.TAHSILAT.initializeRows(); }
function addRow() { return OBS.TAHSILAT.addRow(); }
function cekgirremoveRow(btn) { return OBS.TAHSILAT.cekgirremoveRow(btn); }
function handleBlur(inp) { return OBS.TAHSILAT.handleBlur(inp); }
function selectAllContent(el) { return OBS.TAHSILAT.selectAllContent(el); }
function updateColumnTotal() { return OBS.TAHSILAT.updateColumnTotal(); }
function tahevrakOkuma(e) { return OBS.TAHSILAT.tahevrakOkuma(e); }
async function tahevrakOku() { return OBS.TAHSILAT.tahevrakOku(); }
function tahclearInputs() { return OBS.TAHSILAT.tahclearInputs(); }
function tah_ted_cins_clear() { return OBS.TAHSILAT.tah_ted_cins_clear(); }
function tahgerifisNo() { return OBS.TAHSILAT.tahgerifisNo(); }
function tahilerifisNo() { return OBS.TAHSILAT.tahilerifisNo(); }
async function tahsonfisNo() { return OBS.TAHSILAT.tahsonfisNo(); }
async function tahyenifis() { return OBS.TAHSILAT.tahyenifis(); }
function getTableData() { return OBS.TAHSILAT.getTableData(); }
function prepareRequestPayload() { return OBS.TAHSILAT.prepareRequestPayload(); }
async function tahfisKayit() { return OBS.TAHSILAT.tahfisKayit(); }
async function tahfisYoket() { return OBS.TAHSILAT.tahfisYoket(); }
async function tahsilatdownloadReport() { return OBS.TAHSILAT.tahsilatdownloadReport(); }
async function tahcariIsle() { return OBS.TAHSILAT.tahcariIsle(); }
