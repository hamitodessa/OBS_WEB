/* =========================
   STOK – IRSALIYE (çakışmasız)
   Namespace: OBS.IRSALIYE
   File: /obs_js_files/stok/irsaliye.js
   ========================= */
window.OBS ||= {};
OBS.IRSALIYE ||= {};

(() => {
  const M = OBS.IRSALIYE;

  /* ---------- state ---------- */
  M.rowCounter = 0;
  M.urnkodlar = [];
  M.depolar = [];
  M.lastFocusedRow = null;

  /* ---------- helpers ---------- */
  M._root = () =>
    document.getElementById("stok_content") ||
    document.getElementById("irs_content") ||
    document.getElementById("ara_content") ||
    document;

  M._el = (id) => M._root().querySelector(`#${CSS.escape(id)}`) || document.getElementById(id);

  M._setBusy = (yes) => { document.body.style.cursor = yes ? "wait" : "default"; };

  M._clearError = () => {
    const e = M._el("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
  };

  M._error = (msg) => {
    const e = M._el("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Beklenmeyen bir hata oluştu.";
  };

  M._incRow = () => { M.rowCounter++; };

  /* sayı format helper'ların zaten globalde var diye varsayıyorum:
     formatNumber2 / formatNumber3 / parseLocaleNumber / formatdateSaatsiz
     Yoksa söyle tatlım, bunları da namespace'e taşırız. */

  M.selectAllContent = (el) => { if (el && el.select) el.select(); };

  M.setLabelContent = (cell, content) => {
    const span = cell?.querySelector?.("label span");
    if (span) span.textContent = content ? content : "\u00A0";
  };

  /* ---------- init ---------- */
  M.init = async function init() {
    M._clearError();

    // tablo focus info (urunadi / anaalt / resim)
    M._bindTableFocusInfo();

    // ilk veri yükle (urnkodlar + depolar) ve 5 satır bas
    await M.fetchkoddepo();
  };

  M._bindTableFocusInfo = function () {
    const table = M._el("irsTable");
    if (!table) return;

    table.addEventListener("focusin", (event) => {
      const currentRow = event.target.closest("tr");
      if (!currentRow || currentRow === M.lastFocusedRow) return;

      M.lastFocusedRow = currentRow;
      const cells = currentRow.cells;

      const urunadi = M._el("urunadi");
      const anaalt = M._el("anaalt");
      if (urunadi) urunadi.innerText = cells[11]?.textContent || "";
      if (anaalt) anaalt.innerText = cells[12]?.textContent || "";

      const img = M._el("resimGoster");
      if (!img) return;

      const b64 = (cells[13]?.textContent || "").trim();
      if (b64) {
        img.src = "data:image/jpeg;base64," + b64;
        img.style.display = "block";
      } else {
        img.src = "";
        img.style.display = "none";
      }
    });
  };

  /* ---------- data preload ---------- */
  M.fetchkoddepo = async function fetchkoddepo() {
    M._clearError();
    M.rowCounter = 0;
    M.urnkodlar = [];
    M.depolar = [];

    try {
      const response = await fetchWithSessionCheck("stok/stkgeturndepo", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      });
      if (response.errorMessage) throw new Error(response.errorMessage);

      M.urnkodlar = response.urnkodlar || [];
      M.depolar = response.depolar || [];

      M.initializeRows();
    } catch (err) {
      M._error(err.message);
    }
  };

  /* ---------- rows ---------- */
  M.initializeRows = function initializeRows() {
    M.rowCounter = 0;

    const tbody = M._el("irsTable")?.getElementsByTagName("tbody")?.[0];
    if (tbody) tbody.innerHTML = "";

    for (let i = 0; i < 5; i++) M.satirekle();
    M.updateColumnTotal();
  };

  M.satirekle = function satirekle() {
    const tbody = M._el("irsTable")?.getElementsByTagName("tbody")?.[0];
    if (!tbody) {
      console.error("irsTable tbody bulunamadı!");
      return null;
    }

    const row = tbody.insertRow();
    M._incRow();

    const ukoduoptionsHTML = (M.urnkodlar || [])
      .map(k => `<option value="${k.Kodu}">${k.Kodu}</option>`)
      .join("");

    const depoOptionsHTML = (M.depolar || [])
      .map(k => `<option value="${k.DEPO}">${k.DEPO}</option>`)
      .join("");

    row.innerHTML = `
      <td>
        <button id="bsatir_${M.rowCounter}" type="button"
          class="btn btn-secondary"
          onclick="OBS.IRSALIYE.satirsil(this)">
          <i class="fa fa-trash"></i>
        </button>
      </td>

      <td>
        <div class="irs-rel">
          <input class="form-control cins_bold"
            list="barkodOptions_${M.rowCounter}"
            maxlength="20"
            id="barkod_${M.rowCounter}"
            onkeydown="OBS.IRSALIYE.focusNextCell(event, this)"
            ondblclick="openurunkodlariModal('barkod_${M.rowCounter}','fatsatir','barkodkod')"
            onchange="OBS.IRSALIYE.updateRowValues(this)">
          <datalist id="barkodOptions_${M.rowCounter}"></datalist>
          <span class="irs-arrow">▼</span>
        </div>
      </td>

      <td>
        <div class="irs-rel">
          <input class="form-control cins_bold"
            list="ukoduOptions_${M.rowCounter}"
            maxlength="12"
            id="ukodu_${M.rowCounter}"
            onkeydown="OBS.IRSALIYE.focusNextCell(event, this)"
            ondblclick="openurunkodlariModal('ukodu_${M.rowCounter}','imalatsatir','ukodukod')"
            onchange="OBS.IRSALIYE.updateRowValues(this)">
          <datalist id="ukoduOptions_${M.rowCounter}">${ukoduoptionsHTML}</datalist>
          <span class="irs-arrow">▼</span>
        </div>
      </td>

      <td>
        <div class="irs-rel">
          <select class="form-control" id="depo_${M.rowCounter}">
            ${depoOptionsHTML}
          </select>
          <span class="irs-arrow">▼</span>
        </div>
      </td>

      <td>
        <input class="form-control ta-right"
          value="${formatNumber2(0)}"
          onfocus="OBS.IRSALIYE.selectAllContent(this)"
          onblur="OBS.IRSALIYE.handleBlur(this)"
          onkeydown="OBS.IRSALIYE.focusNextCell(event, this)">
      </td>

      <td>
        <input class="form-control ta-right"
          value="${formatNumber2(0)}"
          onfocus="OBS.IRSALIYE.selectAllContent(this)"
          onblur="OBS.IRSALIYE.handleBlur(this)"
          onkeydown="OBS.IRSALIYE.focusNextCell(event, this)">
      </td>

      <td>
        <input class="form-control ta-right"
          value="${formatNumber3(0)}"
          onfocus="OBS.IRSALIYE.selectAllContent(this)"
          onblur="OBS.IRSALIYE.handleBlur3(this)"
          onkeydown="OBS.IRSALIYE.focusNextCell(event, this)">
      </td>

      <td>
        <label class="form-control">
          <span>&nbsp;</span>
        </label>
      </td>

      <td>
        <input class="form-control ta-right"
          value="${formatNumber2(0)}"
          onfocus="OBS.IRSALIYE.selectAllContent(this)"
          onblur="OBS.IRSALIYE.handleBlur(this)"
          onkeydown="OBS.IRSALIYE.focusNextCell(event, this)">
      </td>

      <td>
        <input class="form-control ta-right"
          value="${formatNumber2(0)}"
          onfocus="OBS.IRSALIYE.selectAllContent(this)"
          onblur="OBS.IRSALIYE.handleBlur(this)"
          onkeydown="OBS.IRSALIYE.focusNextCell(event, this)">
      </td>

      <td>
        <input class="form-control"
          value=""
          onfocus="OBS.IRSALIYE.selectAllContent(this)"
          onkeydown="OBS.IRSALIYE.focusNextRow(event, this)">
      </td>

      <td style="display:none;"></td>
      <td style="display:none;"></td>
      <td style="display:none;"></td>
    `;

    return row;
  };

  M.satirsil = function satirsil(btn) {
    const row = btn?.closest("tr");
    if (row) row.remove();
    M.updateColumnTotal();
  };

  M.handleBlur3 = function handleBlur3(input) {
    input.value = formatNumber3(parseLocaleNumber(input.value));
    M.updateColumnTotal();
  };

  M.handleBlur = function handleBlur(input) {
    input.value = formatNumber2(parseLocaleNumber(input.value));
    M.updateColumnTotal();
  };

  /* ---------- totals ---------- */
  M.updateColumnTotal = function updateColumnTotal() {
    const table = M._el("irsTable");
    if (!table) return;

    const rows = table.querySelectorAll("tbody tr");
    const totalSatirCell = M._el("totalSatir");
    const totalTutarCell = M._el("totalTutar");
    const totalMiktarCell = M._el("totalMiktar");
    const tevoran = M._el("tevoran");

    let total = 0;
    let totalmik = 0;
    let iskTop = 0;
    let kdvTop = 0;
    let brutTop = 0;
    let totalsatir = 0;

    rows.forEach(row => {
      const ukodu = row.querySelector("td:nth-child(3) input");

      const fiat = row.querySelector("td:nth-child(5) input");
      const iskonto = row.querySelector("td:nth-child(6) input");
      const miktar = row.querySelector("td:nth-child(7) input");
      const kdvv = row.querySelector("td:nth-child(9) input");
      const tutar = row.querySelector("td:nth-child(10) input");

      if (ukodu && ukodu.value.trim() !== "") totalsatir += 1;

      if (fiat && miktar && tutar && iskonto && kdvv) {
        const isk = parseLocaleNumber(iskonto.value) || 0;
        const kdv = parseLocaleNumber(kdvv.value) || 0;
        const fia = parseLocaleNumber(fiat.value) || 0;
        const mik = parseLocaleNumber(miktar.value) || 0;

        const line = fia * mik;
        tutar.value = formatNumber2(line);

        totalmik += mik;

        if (line > 0) {
          brutTop += line;
          iskTop += (line * isk) / 100;
          kdvTop += ((line - (line * isk / 100)) * kdv) / 100;
          total += line;
        }
      }
    });

    if (M._el("iskonto")) M._el("iskonto").innerText = formatNumber2(iskTop);
    if (M._el("bakiye")) M._el("bakiye").innerText = formatNumber2(brutTop - iskTop);
    if (M._el("kdv")) M._el("kdv").innerText = formatNumber2(kdvTop);

    const tev = parseLocaleNumber(tevoran?.value || 0) || 0;
    if (M._el("tevedkdv")) M._el("tevedkdv").innerText = formatNumber2((kdvTop / 10) * tev);

    const tevDahil = (brutTop - iskTop) + kdvTop;
    if (M._el("tevdahtoptut")) M._el("tevdahtoptut").innerText = formatNumber2(tevDahil);

    const beyanEdilecek = (kdvTop - (kdvTop / 10) * tev);
    if (M._el("beyedikdv")) M._el("beyedikdv").innerText = formatNumber2(beyanEdilecek);

    const tevHaric = (brutTop - iskTop) + beyanEdilecek;
    if (M._el("tevhartoptut")) M._el("tevhartoptut").innerText = formatNumber2(tevHaric);

    if (totalTutarCell) totalTutarCell.textContent = total.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    if (totalMiktarCell) totalMiktarCell.textContent = totalmik.toLocaleString(undefined, { minimumFractionDigits: 3, maximumFractionDigits: 3 });
    if (totalSatirCell) totalSatirCell.textContent = totalsatir.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  };

  /* ---------- navigation with Enter ---------- */
  M.focusNextRow = function focusNextRow(event, element) {
    if (event.key !== "Enter") return;
    event.preventDefault();

    const currentRow = element.closest("tr");
    const nextRow = currentRow?.nextElementSibling;

    if (nextRow) {
      const focusEl = nextRow.querySelector("td:nth-child(3) input");
      if (focusEl) { focusEl.focus(); focusEl.select?.(); }
      return;
    }

    M.satirekle();
    const tbody = currentRow?.parentElement;
    const newRow = tbody?.lastElementChild;
    const focusEl = newRow?.querySelector("td:nth-child(3) input");
    if (focusEl) { focusEl.focus(); focusEl.select?.(); }
  };

  M.focusNextCell = function focusNextCell(event, element) {
    if (event.key !== "Enter") return;
    event.preventDefault();

    let currentCell = element.closest("td");
    let nextCell = currentCell?.nextElementSibling;

    while (nextCell) {
      // input veya select bul (select de var)
      const input = nextCell.querySelector("input, select");
      if (input) {
        input.focus();
        input.select?.();
        break;
      }
      nextCell = nextCell.nextElementSibling;
    }
  };

  /* ---------- anagrup -> altgrup ---------- */
  M.anagrpChanged = async function anagrpChanged(anagrpElement) {
    const anagrup = anagrpElement?.value || "";
    const selectAlt = M._el("altgrp");
    if (!selectAlt) return;

    selectAlt.innerHTML = "";
    if (!anagrup) { selectAlt.disabled = true; return; }

    M._setBusy(true);
    M._clearError();

    try {
      const response = await fetchWithSessionCheck("stok/altgrup", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ anagrup }),
      });
      if (response.errorMessage) throw new Error(response.errorMessage);

      (response.altKodlari || []).forEach(k => {
        const opt = document.createElement("option");
        opt.value = k.ALT_GRUP;
        opt.textContent = k.ALT_GRUP;
        selectAlt.appendChild(opt);
      });

      selectAlt.disabled = selectAlt.options.length === 0;
    } catch (err) {
      selectAlt.disabled = true;
      M._error(err.message);
    } finally {
      M._setBusy(false);
    }
  };

  /* ---------- row product fetch (urunoku) ---------- */
  M.updateRowValues = async function updateRowValues(inputElement) {
    const selectedValue = inputElement?.value || "";
    if (!selectedValue) return;

    const uygulananfiat = M._el("uygulananfiat")?.value || "";
    const gircik = M._el("gircik")?.value || "";
    const carikod = M._el("carikod")?.value || "";

    M._setBusy(true);
    M._clearError();

    try {
      const response = await fetchWithSessionCheck("stok/urunoku", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
          ukodu: selectedValue,
          barkod: "",
          fiatlama: uygulananfiat,
          gircik: gircik,
          ckod: carikod
        }),
      });
      if (response.errorMessage) throw new Error(response.errorMessage);

      const row = inputElement.closest("tr");
      const cells = row.querySelectorAll("td");

      const barkodInput = cells[1]?.querySelector("input");
      const fiatInput = cells[4]?.querySelector("input");
      const birimCell = cells[7];

      cells[11].innerText = response.dto?.adi || "";
      cells[12].innerText = (response.dto?.anagrup || "") + " / " + (response.dto?.altgrup || "");
      cells[13].innerText = response.dto?.base64Resim || "";

      M.setLabelContent(birimCell, response.dto?.birim || "");

      if (barkodInput) barkodInput.value = response.dto?.barkod || "";
      if (fiatInput) fiatInput.value = formatNumber2(response.fiat || 0);

      const urunadi = M._el("urunadi");
      const anaalt = M._el("anaalt");
      if (urunadi) urunadi.innerText = response.dto?.adi || "";
      if (anaalt) anaalt.innerText = (response.dto?.anagrup || "") + " / " + (response.dto?.altgrup || "");

      const img = M._el("resimGoster");
      if (img) {
        const b64 = (response.dto?.base64Resim || "").trim();
        if (b64) {
          img.src = "data:image/jpeg;base64," + b64;
          img.style.display = "block";
        } else {
          img.src = "";
          img.style.display = "none";
        }
      }
    } catch (err) {
      M._error(err.message);
    } finally {
      M._setBusy(false);
    }
  };

  /* ---------- clear ---------- */
  M.clearInputs = function clearInputs() {
    const setVal = (id, v) => { const el = M._el(id); if (el) el.value = v; };
    const setText = (id, v) => { const el = M._el(id); if (el) el.innerText = v; };

    setVal("barkod", "");
    setText("urunadi", "");
    setText("anaalt", "");

    setText("iskonto", "0.00");
    setText("bakiye", "0.00");
    setText("kdv", "0.00");
    setText("tevedkdv", "0.00");
    setText("tevdahtoptut", "0.00");
    setText("beyedikdv", "0.00");
    setText("tevhartoptut", "0.00");
    setVal("tevoran", "0.00");

    setVal("ozelkod", "");
    setVal("anagrp", "");

    const alt = M._el("altgrp");
    if (alt) { alt.innerHTML = ""; alt.disabled = true; }

    setVal("carikod", "");
    setVal("adreskod", "");
    setText("cariadilbl", "");
    setText("adresadilbl", "");

    const uyg = M._el("uygulananfiat");
    if (uyg) uyg.selectedIndex = 0;

    const dvz = M._el("dovizcins");
    const def = M._el("defaultdvzcinsi")?.value || "TL";
    if (dvz) dvz.value = def;

    setVal("kur", "0.0000");

    setVal("not1", "");
    setVal("not2", "");
    setVal("not3", "");

    const chk = M._el("fatmikyazdir");
    if (chk) chk.checked = false;

    setVal("a1", "");
    setVal("a2", "");
    setVal("fatno", "");

    // tablo reset
    M.initializeRows();

    // img reset
    const img = M._el("resimGoster");
    if (img) { img.src = ""; img.style.display = "none"; }

    // totals reset
    const tT = M._el("totalTutar");
    const tS = M._el("totalSatir");
    const tM = M._el("totalMiktar");
    if (tT) tT.textContent = formatNumber2(0);
    if (tS) tS.textContent = formatNumber0(0);
    if (tM) tM.textContent = formatNumber3(0);

    M._clearError();
  };

  /* ---------- read (irsOku) ---------- */
  M.irsOku = async function irsOku() {
    const fisno = M._el("fisno")?.value || "";
    if (!fisno) return;

    const cins = M._el("gircik")?.value || "";
    M._setBusy(true);
    M._clearError();

    try {
      const response = await fetchWithSessionCheck("stok/irsOku", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ fisno, cins }),
      });

      if (response.errorMessage) throw new Error(response.errorMessage);

      const data = response;
      M.clearInputs();

      if (!data?.data || data.data.length === 0) return;

      // satır sayısını garanti et
      const table = M._el("irsTable");
      const need = data.data.length;
      while (table.querySelectorAll("tbody tr").length < need) M.satirekle();

      const rows = table.querySelectorAll("tbody tr");
      data.data.forEach((item, i) => {
        const cells = rows[i].cells;
        const barkodInput = cells[1]?.querySelector("input");
        const ukoduInput  = cells[2]?.querySelector("input");
        const depoSel     = cells[3]?.querySelector("select");
        const fiatInput   = cells[4]?.querySelector("input");
        const iskInput    = cells[5]?.querySelector("input");
        const mikInput    = cells[6]?.querySelector("input");
        const kdvInput    = cells[8]?.querySelector("input");
        const tutInput    = cells[9]?.querySelector("input");
        const izaInput    = cells[10]?.querySelector("input");

        if (barkodInput) barkodInput.value = item.Barkod || "";
        if (ukoduInput)  ukoduInput.value  = item.Kodu || "";
        if (depoSel)     depoSel.value     = item.Depo || "";
        if (fiatInput)   fiatInput.value   = formatNumber2(item.Fiat);
        if (iskInput)    iskInput.value    = formatNumber2(item.Iskonto);
        if (mikInput)    mikInput.value    = formatNumber3(item.Miktar);
        if (kdvInput)    kdvInput.value    = formatNumber2(item.Kdv);
        if (tutInput)    tutInput.value    = formatNumber2(item.Tutar);
        if (izaInput)    izaInput.value    = item.Izahat || "";

        M.setLabelContent(cells[7], item.Birim || "");
        cells[11].innerText = item.Adi || "";
        cells[12].innerText = (item.Ur_AnaGrup || "") + " / " + (item.Ur_AltGrup || "");
        cells[13].innerText = item.base64Resim || "";
      });

      // üst bilgiler (ilk kayıttan)
      const first = data.data[0];
      if (first) {
        if (M._el("fisTarih")) M._el("fisTarih").value = formatdateSaatsiz(first.Tarih);
        if (M._el("sevkTarih")) M._el("sevkTarih").value = formatdateSaatsiz(first.Sevk_Tarihi);
        if (M._el("anagrp")) M._el("anagrp").value = first.Ana_Grup || "";
        if (M._el("kur")) M._el("kur").value = first.Kur || "0.0000";

        await M.anagrpChanged(M._el("anagrp"));
        if (M._el("altgrp")) M._el("altgrp").value = first.Alt_Grup || "";

        if (M._el("ozelkod")) M._el("ozelkod").value = first.Ozel_Kod || "";
        if (M._el("tevoran")) M._el("tevoran").value = first.Tevkifat || "0";
        if (M._el("carikod")) M._el("carikod").value = first.Cari_Hesap_Kodu || "";
        if (M._el("adreskod")) M._el("adreskod").value = first.Firma || "";
        if (M._el("dovizcins")) M._el("dovizcins").value = first.Doviz || "";
        if (M._el("fatno")) M._el("fatno").value = first.Fatura_No || "";
      }

      if (M._el("a1")) M._el("a1").value = data.a1 || "";
      if (M._el("a2")) M._el("a2").value = data.a2 || "";
      if (M._el("not1")) M._el("not1").value = (data.dipnot?.[0] ?? "");
      if (M._el("not2")) M._el("not2").value = (data.dipnot?.[1] ?? "");
      if (M._el("not3")) M._el("not3").value = (data.dipnot?.[2] ?? "");

      M.updateColumnTotal();

      // bunlar sende global fonksiyon; varsa çağır
      if (typeof window.hesapAdiOgren === "function") window.hesapAdiOgren(M._el("carikod")?.value, "cariadilbl");
      if (typeof window.adrhesapAdiOgren === "function") window.adrhesapAdiOgren("adreskod", "adresadilbl");
    } catch (err) {
      M._error(err.message);
    } finally {
      M._setBusy(false);
    }
  };

  /* ---------- sonfis / yenifis / yoket ---------- */
  M.sonfis = async function sonfis() {
    const cins = M._el("gircik")?.value || "";
    M._setBusy(true);
    M._clearError();

    try {
      const response = await fetchWithSessionCheck("stok/sonirsfis", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ cins }),
      });
      if (response.errorMessage) throw new Error(response.errorMessage);

      const fisnoEl = M._el("fisno");
      if (fisnoEl) fisnoEl.value = response.fisno;

      if (response.errorMessage) {
        M._error(response.errorMessage);
        return;
      }

      await M.irsOku();
    } catch (err) {
      M._error(err.message);
    } finally {
      M._setBusy(false);
    }
  };

  M.yeniFis = async function yeniFis() {
    const cins = M._el("gircik")?.value || "";
    M.clearInputs();
    M._setBusy(true);
    M._clearError();

    try {
      const response = await fetchWithSessionCheck("stok/irsyenifis", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ cins }),
      });
      if (response.errorMessage) throw new Error(response.errorMessage);

      const fisnoEl = M._el("fisno");
      if (fisnoEl) fisnoEl.value = response.fisno;
    } catch (err) {
      M._error(err.message);
    } finally {
      M._setBusy(false);
    }
  };

  M.irsYoket = async function irsYoket() {
    const fisno = M._el("fisno")?.value || "";
    const cins = M._el("gircik")?.value || "";
    if (!fisno || fisno === "0") return;

    if (!confirm("Bu İrsaliye silinecek ?")) return;

    M._setBusy(true);
    M._clearError();

    try {
      const response = await fetchWithSessionCheck("stok/irsYoket", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ fisno, cins }),
      });
      if (response.errorMessage) throw new Error(response.errorMessage);

      M.clearInputs();
      const fisnoEl = M._el("fisno");
      if (fisnoEl) fisnoEl.value = "";
    } catch (err) {
      M._error(err.message);
    } finally {
      M._setBusy(false);
    }
  };

  /* ---------- prepare + save ---------- */
  M.prepareKayit = function prepareKayit() {
    const dto = {
      fisno: M._el("fisno")?.value || "",
      tarih: M._el("fisTarih")?.value || "",
      sevktarih: M._el("sevkTarih")?.value || "",
      ozelkod: M._el("ozelkod")?.value || "",
      anagrup: M._el("anagrp")?.value || "",
      altgrup: M._el("altgrp")?.value || "",
      carikod: M._el("carikod")?.value || "",
      adreskod: M._el("adreskod")?.value || "",
      dvzcins: M._el("dovizcins")?.value || "",
      kur: parseLocaleNumber(M._el("kur")?.value || 0) || 0,
      tevoran: parseLocaleNumber(M._el("tevoran")?.value || 0) || 0,
      not1: M._el("not1")?.value || "",
      not2: M._el("not2")?.value || "",
      not3: M._el("not3")?.value || "",
      acik1: M._el("a1")?.value || "",
      acik2: M._el("a2")?.value || "",
      fatno: M._el("fatno")?.value || "",
      fatcins: M._el("gircik")?.value || "",
    };

    const tableData = M.getTableData();
    return { faturaDTO: dto, tableData }; // backend aynı isim bekliyorsa diye
  };

  M.getTableData = function getTableData() {
    const table = M._el("irsTable");
    if (!table) return [];

    const rows = table.querySelectorAll("tbody tr");
    const data = [];

    rows.forEach((row) => {
      const cells = row.querySelectorAll("td");
      const ukodu = cells[2]?.querySelector("input")?.value || "";
      if (!ukodu.trim()) return;

      data.push({
        barkod: cells[1]?.querySelector("input")?.value || "",
        ukodu: ukodu,
        depo: cells[3]?.querySelector("select")?.value || "",
        fiat: parseLocaleNumber(cells[4]?.querySelector("input")?.value || 0),
        iskonto: parseLocaleNumber(cells[5]?.querySelector("input")?.value || 0),
        miktar: parseLocaleNumber(cells[6]?.querySelector("input")?.value || 0),
        kdv: parseLocaleNumber(cells[8]?.querySelector("input")?.value || 0),
        tutar: parseLocaleNumber(cells[9]?.querySelector("input")?.value || 0),
        izahat: cells[10]?.querySelector("input")?.value || "",
      });
    });

    return data;
  };

  M.irsKayit = async function irsKayit() {
    const fisno = M._el("fisno")?.value || "";
    const table = M._el("irsTable");
    if (!fisno || fisno === "0" || !table) return;

    const payload = M.prepareKayit();

    const btn = M._el("irskaydetButton");
    if (btn) { btn.disabled = true; btn.innerText = "İşleniyor..."; }

    M._setBusy(true);
    M._clearError();

    try {
      const response = await fetchWithSessionCheck("stok/irsKayit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if ((response.errorMessage || "").trim() !== "") throw new Error(response.errorMessage);

      M.clearInputs();
      const fisnoEl = M._el("fisno");
      if (fisnoEl) fisnoEl.value = "";
    } catch (err) {
      M._error(err.message);
    } finally {
      if (btn) { btn.disabled = false; btn.innerText = "Kaydet"; }
      M._setBusy(false);
    }
  };

  /* ---------- cari işle ---------- */
  M.irscariIsle = async function irscariIsle() {
    const fisno = M._el("fisno")?.value || "";
    const table = M._el("irsTable");
    if (!fisno || fisno === "0" || !table) return;

    // jquery yok -> select'i direkt al
    const hesapKodu = M._el("irsaliyeBilgi")?.value || "";

    const btn = M._el("carkaydetButton");
    if (btn) { btn.disabled = true; btn.innerText = "İşleniyor..."; }

    const faturaDTO = {
      fisno: fisno,
      tarih: M._el("fisTarih")?.value || "",
      carikod: M._el("carikod")?.value || "",
      miktar: parseLocaleNumber(M._el("totalMiktar")?.textContent || 0),
      tutar: parseLocaleNumber(M._el("tevhartoptut")?.innerText || 0),
      fatcins: M._el("gircik")?.value || "",
      karsihesapkodu: hesapKodu,
    };

    M._setBusy(true);
    M._clearError();

    try {
      const response = await fetchWithSessionCheck("stok/irscariKayit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(faturaDTO),
      });
      if ((response.errorMessage || "").trim() !== "") throw new Error(response.errorMessage);

      M._clearError();
    } catch (err) {
      M._error(err.message);
    } finally {
      if (btn) { btn.disabled = false; btn.innerText = "Cari Kaydet"; }
      M._setBusy(false);
    }
  };

})();
