/* =========================
   STOK – FATURA (çakışmasız)
   Namespace: OBS.FATURA
   File: /obs_js_files/stok/fatura.js
   ========================= */
window.OBS ||= {};
OBS.FATURA ||= {};

(() => {
  const N = OBS.FATURA;

  /* ---------- state ---------- */
  N.rowCounter = 0;
  N.depolar = [];
  N.urnkodlar = [];
  N.lastFocusedRow = null;

  /* ---------- helpers ---------- */
  N._root = () =>
    document.getElementById("ara_content") ||
    document.getElementById("stk_content") ||
    document;

  N.$ = (id) => N._root().querySelector(`#${CSS.escape(id)}`);
  N.qs = (sel) => N._root().querySelector(sel);
  N.qsa = (sel) => Array.from(N._root().querySelectorAll(sel));

  N._err = () => N.$("errorDiv") || document.getElementById("errorDiv");
  N._showErr = (msg) => {
    const e = N._err();
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Beklenmeyen bir hata oluştu.";
  };
  N._clearErr = () => {
    const e = N._err();
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
  };

  N._setWait = (yes) => {
    document.body.style.cursor = yes ? "wait" : "default";
  };

  N._table = () => N.$("fatTable") || document.getElementById("fatTable");
  N._tbody = () => N._table()?.getElementsByTagName("tbody")?.[0];

  N._ensureHiddenCells = (row) => {
    // 11,12,13 indeksleri için td lazım (0-based: 11..13 => 12..14. hücre)
    const need = 14; // en az 14 hücre (0..13)
    while (row.cells.length < need) {
      const td = row.insertCell(-1);
      td.style.display = "none";
      td.innerText = "";
    }
  };

  N.incrementRowCounter = () => { N.rowCounter++; };

  /* ---------- init ---------- */
  N.init = async function init() {
    // eventler tek sefer bağlansın
    N.bindEvents();

    // ilk açılış
    await N.fetchkoddepo();
    N.initializeRows();
    N.updateColumnTotal();
  };

  N.bindEvents = function bindEvents() {
    const table = N._table();
    if (table && !table.dataset.obsBound) {
      table.dataset.obsBound = "1";

      // satır focus’unda ürün info + resim göster
      table.addEventListener("focusin", (event) => {
        const tr = event.target.closest("tr");
        if (!tr || tr === N.lastFocusedRow) return;

        N.lastFocusedRow = tr;
        N._ensureHiddenCells(tr);

        const urunAdi = tr.cells[11]?.textContent || "";
        const anaalt = tr.cells[12]?.textContent || "";
        const base64 = tr.cells[13]?.textContent || "";

        const lblUrun = N.$("urunadi") || document.getElementById("urunadi");
        const lblAnaAlt = N.$("anaalt") || document.getElementById("anaalt");
        if (lblUrun) lblUrun.innerText = urunAdi;
        if (lblAnaAlt) lblAnaAlt.innerText = anaalt;

        const img = N.$("resimGoster") || document.getElementById("resimGoster");
        if (img) {
          if (base64 && base64.trim() !== "") {
            img.src = "data:image/jpeg;base64," + base64.trim();
            img.style.display = "block";
          } else {
            img.src = "";
            img.style.display = "none";
          }
        }
      });

      // table input değişince totals
      table.addEventListener("input", (e) => {
        const el = e.target;
        if (!(el instanceof HTMLInputElement)) return;
        // fiyat/iskonto/miktar/kdv/tutar alanları değişince toplamı güncelle
        N.updateColumnTotal();
      });

      // Enter ile gezinme (delegation)
      table.addEventListener("keydown", (e) => {
        if (e.key !== "Enter") return;
        const el = e.target;
        if (!(el instanceof HTMLElement)) return;
        if (el.closest("td") && (el.tagName === "INPUT" || el.tagName === "SELECT")) {
          // son input (izahat) ise yeni satıra geç
          // burada basit: mevcut hücrede input varsa bir sonraki input’a git, yoksa satır sonuysa yeni satır
          // senin eski fonksiyonları kullanacağız:
          // - miktar/kdv vs hücrelerde focusNextCell / focusNextRow çağırmak zaten inline var, bu sadece yedek.
        }
      });
    }
  };

  /* ---------- data load ---------- */
  N.fetchkoddepo = async function fetchkoddepo() {
    N._clearErr();
    N.rowCounter = 0;
    N.depolar = [];
    N.urnkodlar = [];
    try {
      const response = await fetchWithSessionCheck("stok/stkgeturndepo", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      });
      if (response?.errorMessage) throw new Error(response.errorMessage);

      N.urnkodlar = response.urnkodlar || [];
      N.depolar = response.depolar || [];
    } catch (err) {
      N._showErr(err?.message);
    }
  };

  /* ---------- rows ---------- */
  N.initializeRows = function initializeRows() {
    const tbody = N._tbody();
    if (!tbody) return;
    tbody.innerHTML = "";
    N.rowCounter = 0;
    for (let i = 0; i < 5; i++) N.satirekle();
  };

  N.satirekle = function satirekle() {
    const tbody = N._tbody();
    if (!tbody) {
      console.error("fatTable tbody bulunamadı!");
      return null;
    }

    const newRow = tbody.insertRow();
    N.incrementRowCounter();

    const ukoduoptionsHTML = (N.urnkodlar || [])
      .map(kod => `<option value="${kod.Kodu}">${kod.Kodu}</option>`)
      .join("");

    const depoOptionsHTML = (N.depolar || [])
      .map(kod => `<option value="${kod.DEPO}">${kod.DEPO}</option>`)
      .join("");

    newRow.innerHTML = `
      <td>
        <button id="bsatir_${N.rowCounter}" type="button"
          class="btn btn-secondary"
          onclick="OBS.FATURA.satirsil(this)">
          <i class="fa fa-trash"></i>
        </button>
      </td>

      <td>
        <div class="fat-rel">
          <input class="form-control cins_bold"
            list="barkodOptions_${N.rowCounter}"
            maxlength="20"
            id="barkod_${N.rowCounter}"
            onkeydown="OBS.FATURA.focusNextCell(event, this)"
            ondblclick="openurunkodlariModal('barkod_${N.rowCounter}', 'fatsatir','barkodkod')"
            onchange="OBS.FATURA.updateRowValues(this)">
          <datalist id="barkodOptions_${N.rowCounter}"></datalist>
          <span class="fat-arrow">▼</span>
        </div>
      </td>

      <td>
        <div class="fat-rel">
          <input class="form-control cins_bold"
            list="ukoduOptions_${N.rowCounter}"
            maxlength="12"
            id="ukodu_${N.rowCounter}"
            onkeydown="OBS.FATURA.focusNextCell(event, this)"
            ondblclick="openurunkodlariModal('ukodu_${N.rowCounter}', 'fatsatir','ukodukod')"
            onchange="OBS.FATURA.updateRowValues(this)">
          <datalist id="ukoduOptions_${N.rowCounter}">${ukoduoptionsHTML}</datalist>
          <span class="fat-arrow">▼</span>
        </div>
      </td>

      <td>
        <div class="fat-rel">
          <select class="form-control" id="depo_${N.rowCounter}" onkeydown="OBS.FATURA.focusNextCell(event, this)">
            ${depoOptionsHTML}
          </select>
          <span class="fat-arrow">▼</span>
        </div>
      </td>

      <td>
        <input class="form-control ta-right"
          value="${formatNumber2(0)}"
          onfocus="OBS.FATURA.selectAllContent(this)"
          onblur="OBS.FATURA.handleBlur(this)"
          onkeydown="OBS.FATURA.focusNextCell(event, this)">
      </td>

      <td>
        <input class="form-control ta-right"
          value="${formatNumber2(0)}"
          onfocus="OBS.FATURA.selectAllContent(this)"
          onblur="OBS.FATURA.handleBlur(this)"
          onkeydown="OBS.FATURA.focusNextCell(event, this)">
      </td>

      <td>
        <input class="form-control ta-right"
          value="${formatNumber3(0)}"
          onfocus="OBS.FATURA.selectAllContent(this)"
          onblur="OBS.FATURA.handleBlur3(this)"
          onkeydown="OBS.FATURA.focusNextCell(event, this)">
      </td>

      <td>
        <label class="form-control"><span>&nbsp;</span></label>
      </td>

      <td>
        <input class="form-control ta-right"
          value="${formatNumber2(0)}"
          onfocus="OBS.FATURA.selectAllContent(this)"
          onblur="OBS.FATURA.handleBlur(this)"
          onkeydown="OBS.FATURA.focusNextCell(event, this)">
      </td>

      <td>
        <input class="form-control ta-right"
          value="${formatNumber2(0)}"
          onfocus="OBS.FATURA.selectAllContent(this)"
          onblur="OBS.FATURA.handleBlur(this)"
          onkeydown="OBS.FATURA.focusNextCell(event, this)">
      </td>

      <td>
        <input class="form-control"
          value=""
          onfocus="OBS.FATURA.selectAllContent(this)"
          onkeydown="OBS.FATURA.focusNextRow(event, this)">
      </td>

      <td style="display:none;"></td>
      <td style="display:none;"></td>
      <td style="display:none;"></td>
    `;

    return newRow;
  };

  N.satirsil = function satirsil(btn) {
    const tr = btn?.closest("tr");
    if (tr) tr.remove();
    N.updateColumnTotal();
  };

  /* ---------- formatting ---------- */
  N.handleBlur3 = function handleBlur3(input) {
    input.value = formatNumber3(parseLocaleNumber(input.value));
    N.updateColumnTotal();
  };
  N.handleBlur = function handleBlur(input) {
    input.value = formatNumber2(parseLocaleNumber(input.value));
    N.updateColumnTotal();
  };
  N.selectAllContent = function selectAllContent(el) {
    if (el && el.select) el.select();
  };

  /* ---------- totals ---------- */
  N.updateColumnTotal = function updateColumnTotal() {
    const table = N._table();
    if (!table) return;

    const rows = Array.from(table.querySelectorAll("tbody tr"));
    const totalSatirCell = N.$("totalSatir") || document.getElementById("totalSatir");
    const totalTutarCell = N.$("totalTutar") || document.getElementById("totalTutar");
    const totalMiktarCell = N.$("totalMiktar") || document.getElementById("totalMiktar");
    const tevoranEl = N.$("tevoran") || document.getElementById("tevoran");

    let total = 0;
    let totalmik = 0;
    let totalsatir = 0;

    let iskTop = 0;
    let araTop = 0;     // (fiat*mik) toplamı
    let kdvTop = 0;

    // reset
    if (totalSatirCell) totalSatirCell.textContent = "0";
    if (totalTutarCell) totalTutarCell.textContent = "0.00";
    if (totalMiktarCell) totalMiktarCell.textContent = "0.000";

    rows.forEach((row) => {
      const ukoduInput = row.querySelector("td:nth-child(3) input"); // 3. sütun
      const fiat = row.querySelector("td:nth-child(5) input");
      const iskonto = row.querySelector("td:nth-child(6) input");
      const miktar = row.querySelector("td:nth-child(7) input");
      const kdvv = row.querySelector("td:nth-child(9) input");
      const tutar = row.querySelector("td:nth-child(10) input");

      if (ukoduInput && ukoduInput.value.trim() !== "") totalsatir += 1;

      const fia = parseLocaleNumber(fiat?.value) || 0;
      const mik = parseLocaleNumber(miktar?.value) || 0;
      const isk = parseLocaleNumber(iskonto?.value) || 0;
      const kdv = parseLocaleNumber(kdvv?.value) || 0;

      const satirAra = fia * mik;
      if (tutar) tutar.value = formatNumber2(satirAra);

      totalmik += mik;

      if (satirAra > 0) {
        araTop += satirAra;
        iskTop += (satirAra * isk) / 100;
        kdvTop += ((satirAra - (satirAra * isk) / 100) * kdv) / 100;
        total += satirAra; // ekranda satır toplamı istersen araTop’u kullan, sende böyleydi
      }
    });

    // senin label’lar
    const setText = (id, val) => {
      const el = N.$(id) || document.getElementById(id);
      if (el) el.innerText = val;
    };

    setText("iskonto", formatNumber2(iskTop));
    setText("bakiye", formatNumber2(araTop - iskTop));
    setText("kdv", formatNumber2(kdvTop));

    const tev = parseLocaleNumber(tevoranEl?.value) || 0;
    setText("tevedkdv", formatNumber2((kdvTop / 10) * tev));

    const tevDahilTop = (araTop - iskTop) + kdvTop;
    setText("tevdahtoptut", formatNumber2(tevDahilTop));

    setText("beyedikdv", formatNumber2(kdvTop - (kdvTop / 10) * tev));
    setText("tevhartoptut", formatNumber2((araTop - iskTop) + (kdvTop - (kdvTop / 10) * tev)));

    if (totalTutarCell) totalTutarCell.textContent = (araTop).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    if (totalMiktarCell) totalMiktarCell.textContent = (totalmik).toLocaleString(undefined, { minimumFractionDigits: 3, maximumFractionDigits: 3 });
    if (totalSatirCell) totalSatirCell.textContent = (totalsatir).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  };

  /* ---------- combo dependent ---------- */
  N.anagrpChanged = async function anagrpChanged(anagrpElement) {
    const anagrup = anagrpElement?.value || "";
    const errorDiv = N._err();
    const selectElement = N.$("altgrp") || document.getElementById("altgrp");

    if (!selectElement) return;
    selectElement.innerHTML = "";

    if (anagrup === "") {
      selectElement.disabled = true;
      return;
    }

    N._setWait(true);
    N._clearErr();
    try {
      const response = await fetchWithSessionCheck("stok/altgrup", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ anagrup }),
      });
      if (response?.errorMessage) throw new Error(response.errorMessage);

      (response.altKodlari || []).forEach((kod) => {
        const option = document.createElement("option");
        option.value = kod.ALT_GRUP;
        option.textContent = kod.ALT_GRUP;
        selectElement.appendChild(option);
      });

      selectElement.disabled = selectElement.options.length === 0;
    } catch (err) {
      selectElement.disabled = true;
      if (errorDiv) {
        errorDiv.style.display = "block";
        errorDiv.innerText = err?.message || "Beklenmeyen bir hata oluştu.";
      }
    } finally {
      N._setWait(false);
    }
  };

  /* ---------- row fill (urun oku) ---------- */
  N.updateRowValues = async function updateRowValues(inputElement) {
    const selectedValue = inputElement?.value || "";
    if (!selectedValue) return;

    const uygulananfiat = (N.$("uygulananfiat") || document.getElementById("uygulananfiat"))?.value || "";
    const gircikdeger = (N.$("gircik") || document.getElementById("gircik"))?.value || "";
    const carikod = (N.$("carikod") || document.getElementById("carikod"))?.value || "";

    N._setWait(true);
    N._clearErr();
    try {
      const response = await fetchWithSessionCheck("stok/urunoku", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
          ukodu: selectedValue,
          barkod: "",                 // sende böyle
          fiatlama: uygulananfiat,
          gircik: gircikdeger,
          ckod: carikod
        }),
      });
      if (response?.errorMessage) throw new Error(response.errorMessage);

      const row = inputElement.closest("tr");
      if (!row) return;
      N._ensureHiddenCells(row);

      const cells = row.querySelectorAll("td");

      const barkodInput = cells[1]?.querySelector("input");
      const fiatInput = cells[4]?.querySelector("input");
      const birimCell = cells[7];

      // hidden info
      row.cells[11].innerText = response.dto?.adi || "";
      row.cells[12].innerText = (response.dto?.anagrup || "") + " / " + (response.dto?.altgrup || "");
      row.cells[13].innerText = response.dto?.base64Resim || "";

      N.setLabelContent(birimCell, response.dto?.birim || "");

      if (barkodInput) barkodInput.value = response.dto?.barkod || "";
      if (fiatInput) fiatInput.value = formatNumber2(response.fiat || 0);

      // üst bilgi paneli
      const lblUrun = N.$("urunadi") || document.getElementById("urunadi");
      const lblAnaAlt = N.$("anaalt") || document.getElementById("anaalt");
      if (lblUrun) lblUrun.innerText = response.dto?.adi || "";
      if (lblAnaAlt) lblAnaAlt.innerText = (response.dto?.anagrup || "") + " / " + (response.dto?.altgrup || "");

      const img = N.$("resimGoster") || document.getElementById("resimGoster");
      if (img) {
        const b64 = response.dto?.base64Resim || "";
        if (b64 && b64.trim() !== "") {
          img.src = "data:image/jpeg;base64," + b64.trim();
          img.style.display = "block";
        } else {
          img.src = "";
          img.style.display = "none";
        }
      }

      N.updateColumnTotal();
    } catch (err) {
      N._showErr(err?.message);
    } finally {
      N._setWait(false);
    }
  };

  N.setLabelContent = function setLabelContent(cell, content) {
    const span = cell?.querySelector("label span");
    if (span) span.textContent = content ? content : "\u00A0";
  };

  /* ---------- navigation by Enter ---------- */
  N.focusNextRow = function focusNextRow(event, element) {
    if (event.key !== "Enter") return;
    event.preventDefault();

    const currentRow = element.closest("tr");
    const nextRow = currentRow?.nextElementSibling;

    if (nextRow) {
      const secondInput = nextRow.querySelector("td:nth-child(3) input");
      if (secondInput) { secondInput.focus(); secondInput.select?.(); }
    } else {
      N.satirekle();
      const tbody = currentRow?.parentElement;
      const newRow = tbody?.lastElementChild;
      const secondInput = newRow?.querySelector("td:nth-child(3) input");
      if (secondInput) { secondInput.focus(); secondInput.select?.(); }
    }
  };

  N.focusNextCell = function focusNextCell(event, element) {
    if (event.key !== "Enter") return;
    event.preventDefault();

    let currentCell = element.closest("td");
    let nextCell = currentCell?.nextElementSibling;

    while (nextCell) {
      const focusable = nextCell.querySelector("input, select, textarea");
      if (focusable) {
        focusable.focus();
        focusable.select?.();
        break;
      }
      nextCell = nextCell.nextElementSibling;
    }
  };

  /* ---------- clear ---------- */
  N.clearInputs = function clearInputs() {
    const setVal = (id, v) => { const el = N.$(id) || document.getElementById(id); if (el) el.value = v; };
    const setTxt = (id, v) => { const el = N.$(id) || document.getElementById(id); if (el) el.innerText = v; };

    setVal("barkod", "");
    setTxt("urunadi", "");
    setTxt("anaalt", "");

    setTxt("iskonto", "0.00");
    setTxt("bakiye", "0.00");
    setTxt("kdv", "0.00");
    setTxt("tevedkdv", "0.00");
    setTxt("tevdahtoptut", "0.00");
    setTxt("beyedikdv", "0.00");
    setTxt("tevhartoptut", "0.00");
    setVal("tevoran", "0.00");

    setVal("ozelkod", "");
    setVal("anagrp", "");
    const alt = N.$("altgrp") || document.getElementById("altgrp");
    if (alt) { alt.innerHTML = ""; alt.disabled = true; }

    setVal("carikod", "");
    setVal("adreskod", "");
    setTxt("cariadilbl", "");
    setTxt("adresadilbl", "");

    const uyg = N.$("uygulananfiat") || document.getElementById("uygulananfiat");
    if (uyg) uyg.selectedIndex = 0;

    setVal("dovizcins", (N.$("defaultdvzcinsi") || document.getElementById("defaultdvzcinsi"))?.value || "TL");
    setVal("kur", "0.0000");

    setVal("not1", "");
    setVal("not2", "");
    setVal("not3", "");
    const chk = N.$("fatmikyazdir") || document.getElementById("fatmikyazdir");
    if (chk) chk.checked = false;

    setVal("a1", "");
    setVal("a2", "");

    // tablo sıfırla
    N.initializeRows();

    const totalTutar = N.$("totalTutar") || document.getElementById("totalTutar");
    const totalSatir = N.$("totalSatir") || document.getElementById("totalSatir");
    const totalMiktar = N.$("totalMiktar") || document.getElementById("totalMiktar");
    if (totalTutar) totalTutar.textContent = formatNumber2(0);
    if (totalSatir) totalSatir.textContent = formatNumber0(0);
    if (totalMiktar) totalMiktar.textContent = formatNumber3(0);

    const img = N.$("resimGoster") || document.getElementById("resimGoster");
    if (img) { img.src = ""; img.style.display = "none"; }

    N._clearErr();
  };

  /* ---------- read invoice ---------- */
  N.fatOku = async function fatOku() {
    const fisno = (N.$("fisno") || document.getElementById("fisno"))?.value;
    if (!fisno) return;

    const gircik = (N.$("gircik") || document.getElementById("gircik"))?.value || "";
    const errorDiv = N._err();

    N._setWait(true);
    N._clearErr();
    try {
      const response = await fetchWithSessionCheck("stok/fatOku", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ fisno, cins: gircik }),
      });
      if (response?.errorMessage) throw new Error(response.errorMessage);

      const data = response;
      N.clearInputs();

      if (!data?.data || data.data.length === 0) return;

      // satır sayısını garanti et
      const table = N._table();
      const existing = table.querySelectorAll("tbody tr").length;
      const need = data.data.length;
      for (let i = existing; i < need; i++) N.satirekle();

      const rows = table.querySelectorAll("tbody tr");
      data.data.forEach((item, idx) => {
        const cells = rows[idx].cells;
        N._ensureHiddenCells(rows[idx]);

        cells[1]?.querySelector("input") && (cells[1].querySelector("input").value = item.Barkod || "");
        cells[2]?.querySelector("input") && (cells[2].querySelector("input").value = item.Kodu || "");
        cells[3]?.querySelector("select") && (cells[3].querySelector("select").value = item.Depo || "");

        cells[4]?.querySelector("input") && (cells[4].querySelector("input").value = formatNumber2(item.Fiat));
        cells[5]?.querySelector("input") && (cells[5].querySelector("input").value = formatNumber2(item.Iskonto));
        cells[6]?.querySelector("input") && (cells[6].querySelector("input").value = formatNumber3(item.Miktar));

        N.setLabelContent(cells[7], item.Birim || "");

        cells[8]?.querySelector("input") && (cells[8].querySelector("input").value = formatNumber2(item.Kdv));
        cells[9]?.querySelector("input") && (cells[9].querySelector("input").value = formatNumber2(item.Tutar));
        cells[10]?.querySelector("input") && (cells[10].querySelector("input").value = item.Izahat || "");

        rows[idx].cells[11].innerText = item.Adi || "";
        rows[idx].cells[12].innerText = (item.Ur_AnaGrup || "") + " / " + (item.Ur_AltGrup || "");
        rows[idx].cells[13].innerText = item.base64Resim || "";
      });

      // üst header alanları (ilk item’dan)
      const first = data.data[0];
      (N.$("fisTarih") || document.getElementById("fisTarih")).value = formatdateSaatsiz(first.Tarih);
      (N.$("anagrp") || document.getElementById("anagrp")).value = first.Ana_Grup || "";
      (N.$("kur") || document.getElementById("kur")).value = first.Kur;

      await N.anagrpChanged(N.$("anagrp") || document.getElementById("anagrp"));
      (N.$("altgrp") || document.getElementById("altgrp")).value = first.Alt_Grup || "";
      (N.$("ozelkod") || document.getElementById("ozelkod")).value = first.Ozel_Kod || "";
      (N.$("tevoran") || document.getElementById("tevoran")).value = first.Tevkifat || "0";
      (N.$("carikod") || document.getElementById("carikod")).value = first.Cari_Firma || "";
      (N.$("adreskod") || document.getElementById("adreskod")).value = first.Adres_Firma || "";
      (N.$("dovizcins") || document.getElementById("dovizcins")).value = first.Doviz || "";

      (N.$("a1") || document.getElementById("a1")).value = data.a1 || "";
      (N.$("a2") || document.getElementById("a2")).value = data.a2 || "";

      (N.$("not1") || document.getElementById("not1")).value = data.dipnot?.[0] || "";
      (N.$("not2") || document.getElementById("not2")).value = data.dipnot?.[1] || "";
      (N.$("not3") || document.getElementById("not3")).value = data.dipnot?.[2] || "";

      N.updateColumnTotal();

      // cari/adres isim
      try { hesapAdiOgren((N.$("carikod") || document.getElementById("carikod")).value, "cariadilbl"); } catch {}
      try { adrhesapAdiOgren("adreskod", "adresadilbl"); } catch {}

      if (errorDiv) { errorDiv.style.display = "none"; errorDiv.innerText = ""; }
    } catch (err) {
      N._showErr(err?.message);
    } finally {
      N._setWait(false);
    }
  };

  /* ---------- navigation functions ---------- */
  N.sonfis = async function sonfis() {
    const gircik = (N.$("gircik") || document.getElementById("gircik"))?.value || "";
    N._setWait(true);
    N._clearErr();
    try {
      const response = await fetchWithSessionCheck("stok/sonfatfis", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ cins: gircik }),
      });
      if (response?.errorMessage) throw new Error(response.errorMessage);

      const fisNoInput = N.$("fisno") || document.getElementById("fisno");
      if (fisNoInput) fisNoInput.value = response.fisno;

      await N.fatOku();
    } catch (err) {
      N._showErr(err?.message);
    } finally {
      N._setWait(false);
    }
  };

  N.yeniFis = async function yeniFis() {
    const gircik = (N.$("gircik") || document.getElementById("gircik"))?.value || "";
    N.clearInputs();
    N._setWait(true);
    N._clearErr();
    try {
      const response = await fetchWithSessionCheck("stok/yenifis", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ cins: gircik }),
      });
      if (response?.errorMessage) throw new Error(response.errorMessage);

      const fisNoInput = N.$("fisno") || document.getElementById("fisno");
      if (fisNoInput) fisNoInput.value = response.fisno;
    } catch (err) {
      N._showErr(err?.message);
    } finally {
      N._setWait(false);
    }
  };

  N.fatYoket = async function fatYoket() {
    const fisnoEl = N.$("fisno") || document.getElementById("fisno");
    const fisno = fisnoEl?.value || "";
    const gircik = (N.$("gircik") || document.getElementById("gircik"))?.value || "";

    if (["0", ""].includes(fisno)) return;
    if (!confirm("Bu Fatura silinecek ?")) return;

    N._setWait(true);
    N._clearErr();
    try {
      const response = await fetchWithSessionCheck("stok/fatYoket", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ fisno, cins: gircik }),
      });
      if (response?.errorMessage) throw new Error(response.errorMessage);

      N.clearInputs();
      if (fisnoEl) fisnoEl.value = "";
    } catch (err) {
      N._showErr(err?.message);
    } finally {
      N._setWait(false);
    }
  };

  /* ---------- save ---------- */
  N.prepareFatKayit = function prepareFatKayit() {
    const getVal = (id) => (N.$(id) || document.getElementById(id))?.value || "";
    const faturaDTO = {
      fisno: getVal("fisno"),
      tarih: getVal("fisTarih"),
      ozelkod: getVal("ozelkod"),
      anagrup: getVal("anagrp"),
      altgrup: getVal("altgrp"),
      carikod: getVal("carikod"),
      adreskod: getVal("adreskod"),
      dvzcins: getVal("dovizcins"),
      kur: parseLocaleNumber(getVal("kur")) || 0,
      tevoran: parseLocaleNumber(getVal("tevoran")) || 0,
      not1: getVal("not1"),
      not2: getVal("not2"),
      not3: getVal("not3"),
      acik1: getVal("a1"),
      acik2: getVal("a2"),
      fatcins: getVal("gircik"),
    };
    const tableData = N.getTableData();
    return { faturaDTO, tableData };
  };

  N.getTableData = function getTableData() {
    const table = N._table();
    if (!table) return [];

    const rows = table.querySelectorAll("tbody tr");
    const data = [];

    rows.forEach((row) => {
      const cells = row.querySelectorAll("td");
      const ukodu = cells[2]?.querySelector("input")?.value || "";
      if (ukodu.trim()) {
        data.push({
          barkod: cells[1]?.querySelector("input")?.value || "",
          ukodu,
          depo: cells[3]?.querySelector("select")?.value || "",
          fiat: parseLocaleNumber(cells[4]?.querySelector("input")?.value || 0),
          iskonto: parseLocaleNumber(cells[5]?.querySelector("input")?.value || 0),
          miktar: parseLocaleNumber(cells[6]?.querySelector("input")?.value || 0),
          kdv: parseLocaleNumber(cells[8]?.querySelector("input")?.value || 0),
          tutar: parseLocaleNumber(cells[9]?.querySelector("input")?.value || 0),
          izahat: cells[10]?.querySelector("input")?.value || "",
        });
      }
    });

    return data;
  };

  N.fatKayit = async function fatKayit() {
    const fisno = (N.$("fisno") || document.getElementById("fisno"))?.value || "";
    const table = N._table();
    if (!fisno || fisno === "0" || !table) {
      alert("Geçerli bir evrak numarası giriniz.");
      return;
    }

    const dto = N.prepareFatKayit();
    N._setWait(true);
    N._clearErr();
    try {
      const response = await fetchWithSessionCheck("stok/fatKayit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
      });
      if ((response?.errorMessage || "").trim() !== "") throw new Error(response.errorMessage);

      N.clearInputs();
      const fisnoEl = N.$("fisno") || document.getElementById("fisno");
      if (fisnoEl) fisnoEl.value = "";
    } catch (err) {
      N._showErr(err?.message);
    } finally {
      N._setWait(false);
    }
  };

  /* ---------- cari işle ---------- */
  N.fatcariIsle = async function fatcariIsle() {
    const fisno = (N.$("fisno") || document.getElementById("fisno"))?.value || "";
    const table = N._table();
    if (!fisno || fisno === "0" || !table) {
      alert("Geçerli bir evrak numarası giriniz.");
      return;
    }

    const hesapKodu = (N.$("faturaBilgi") || document.getElementById("faturaBilgi"))?.value || "";
    const faturaDTO = {
      fisno,
      tarih: (N.$("fisTarih") || document.getElementById("fisTarih"))?.value || "",
      carikod: (N.$("carikod") || document.getElementById("carikod"))?.value || "",
      miktar: parseLocaleNumber((N.$("totalMiktar") || document.getElementById("totalMiktar"))?.textContent || 0),
      tutar: parseLocaleNumber((N.$("tevhartoptut") || document.getElementById("tevhartoptut"))?.innerText || 0),
      fatcins: (N.$("gircik") || document.getElementById("gircik"))?.value || "",
      karsihesapkodu: hesapKodu,
    };

    N._setWait(true);
    N._clearErr();
    try {
      const response = await fetchWithSessionCheck("stok/fatcariKayit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(faturaDTO),
      });
      if ((response?.errorMessage || "").trim() !== "") throw new Error(response.errorMessage);
    } catch (err) {
      N._showErr(err?.message);
    } finally {
      N._setWait(false);
    }
  };

})(); // IIFE end
