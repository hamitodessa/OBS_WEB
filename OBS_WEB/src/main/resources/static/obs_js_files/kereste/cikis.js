/* ==========================================================
   OBS.KERCIKIS  (Vanilla / jQuery YOK)  - CLEAN FULL
   ========================================================== */
window.OBS = window.OBS || {};
OBS.KERCIKIS = OBS.KERCIKIS || {};

(function (NS) {
  "use strict";

  /* ============ STATE ============ */
  NS.state = NS.state || {
    rowCounter: 0,
    depolar: [],
    urnkodlar: [],
  };

  /* ============ HELPERS (çakışmasız) ============ */
  const byId = (id) => document.getElementById(id);

  function showError(msg) {
    const errorDiv = byId("errorDiv");
    if (!errorDiv) return;
    errorDiv.innerText = msg || "Beklenmeyen bir hata oluştu.";
    errorDiv.style.display = "block";
  }

  function clearError() {
    const errorDiv = byId("errorDiv");
    if (!errorDiv) return;
    errorDiv.innerText = "";
    errorDiv.style.display = "none";
  }

  function setBtn(id, disabled, text) {
    const btn = byId(id);
    if (!btn) return;
    btn.disabled = !!disabled;
    if (typeof text === "string") btn.textContent = text;
  }

  function setCursor(wait) {
    document.body.style.cursor = wait ? "wait" : "default";
  }

  function incrementRowCounter() {
    NS.state.rowCounter++;
  }

  function emptyCellKer(textAlignRight = false, value = "") {
    return `
      <td>
        <label class="form-control ker-cell">
          <span class="${textAlignRight ? "cell-right" : "cell-left"}">
            ${value || "&nbsp;"}
          </span>
        </label>
      </td>`;
  }

  function getKerRows() {
    return document.querySelectorAll("#kerTable #kerTbody tr");
  }

  /* ============ DATA GET ============ */
  async function fetchpakdepo() {
    clearError();

    NS.state.rowCounter = 0;
    NS.state.depolar = [];
    NS.state.urnkodlar = [];

    try {
      const response = await fetchWithSessionCheck("kereste/getpakdepo", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      NS.state.urnkodlar = response.paknolar || [];
      NS.state.depolar = response.depolar || [];

      initializeRows();
    } catch (e) {
      showError(e.message);
    }
  }

  /* 5 satır başlat */
  function initializeRows() {
    const tbody = byId("kerTbody");
    if (tbody) tbody.innerHTML = "";

    NS.state.rowCounter = 0;

    for (let i = 0; i < 5; i++) satirekle();
    updatePaketM3();
    updateColumnTotal();
  }

  /* ============ ROW ADD ============ */
  function satirekle() {
    const tbody = byId("kerTbody");
    if (!tbody) {
      console.error("kerTbody bulunamadı!");
      return null;
    }

    const rowCount = tbody.rows.length;
    if (rowCount >= 250) {
      alert("En fazla 250 satır ekleyebilirsiniz.");
      return null;
    }

    const newRow = tbody.insertRow();
    incrementRowCounter();
    const rc = NS.state.rowCounter;

    const paknooptionsHTML = (NS.state.urnkodlar || [])
      .map((kod) => `<option value="${kod.Paket_No}">${kod.Paket_No}</option>`)
      .join("");

    const depOptionsHTML = (NS.state.depolar || [])
      .map((kod) => `<option value="${kod.DEPO}">${kod.DEPO}</option>`)
      .join("");

    newRow.innerHTML = `
      <!-- SIL -->
      <td>
        <button id="bsatir_${rc}" type="button"
          class="btn btn-secondary ker-rowbtn" onclick="OBS.KERCIKIS.satirsil(this)">
          <i class="fa fa-trash"></i>
        </button>
      </td>

      <!-- PAKET_NO (datalist) -->
      <td>
        <div class="ker-dd">
          <input class="form-control cins_bold ker-cell"
            list="pakOptions_${rc}"
            id="pakno_${rc}"
            maxlength="30"
            onkeydown="if(event.key === 'Enter') OBS.KERCIKIS.paketkontrol(event,this)">
          <datalist id="pakOptions_${rc}">${paknooptionsHTML}</datalist>
          <span class="ker-dd-arrow">▼</span>
        </div>
      </td>

      <!-- LABEL SUTUNLARI -->
      ${emptyCellKer(false, "")}  <!-- BARKOD -->
      ${emptyCellKer(false, "")}  <!-- URUN KODU -->
      ${emptyCellKer(true, "")}   <!-- MIKTAR -->
      ${emptyCellKer(true, "")}   <!-- M3 -->
      ${emptyCellKer(true, "")}   <!-- PAK_M3 -->

      <!-- DEPO -->
      <td>
        <div class="ker-dd">
          <select class="form-select ker-cell" id="depo_${rc}"
            onkeydown="OBS.KERCIKIS.focusNextCell(event, this)">
            ${depOptionsHTML}
          </select>
          <span class="ker-dd-arrow">▼</span>
        </div>
      </td>

      <!-- FIAT -->
      <td>
        <input class="form-control ker-cell double-column"
          onfocus="OBS.KERCIKIS.selectAllContent(this)"
          onblur="OBS.KERCIKIS.handleBlur(this)"
          onkeydown="OBS.KERCIKIS.focusNextCell(event, this)"
          value="${formatNumber2(0)}">
      </td>

      <!-- ISK -->
      <td>
        <input class="form-control ker-cell double-column"
          onfocus="OBS.KERCIKIS.selectAllContent(this)"
          onblur="OBS.KERCIKIS.handleBlur(this)"
          onkeydown="OBS.KERCIKIS.focusNextCell(event, this)"
          value="${formatNumber2(0)}">
      </td>

      <!-- KDV -->
      <td>
        <input class="form-control ker-cell double-column"
          onfocus="OBS.KERCIKIS.selectAllContent(this)"
          onblur="OBS.KERCIKIS.handleBlur(this)"
          onkeydown="OBS.KERCIKIS.focusNextCell(event, this)"
          value="${formatNumber2(0)}">
      </td>

      <!-- TUTAR -->
      <td>
        <input class="form-control ker-cell ker-cell--bold double-column"
          onfocus="OBS.KERCIKIS.selectAllContent(this)"
          onblur="OBS.KERCIKIS.handleBlur(this)"
          onkeydown="OBS.KERCIKIS.focusNextCell(event, this)"
          value="${formatNumber2(0)}">
      </td>

      <!-- IZAHAT -->
      <td>
        <input class="form-control ker-cell"
          onfocus="OBS.KERCIKIS.selectAllContent(this)"
          onkeydown="OBS.KERCIKIS.focusNextRow(event, this)">
      </td>

      <!-- gizli SATIR -->
      <td style="display:none;"></td>
    `;

    return newRow;
  }

  /* ============ INPUT HELPERS ============ */
  function handleBlur(input) {
    input.value = formatNumber2(parseLocaleNumber(input.value));
    updateColumnTotal();
  }

  function selectAllContent(el) {
    if (el && el.select) el.select();
  }

  /* ============ PAKET KONTROL ============ */
  async function paketkontrol(event, input) {
    const fisno = byId("fisno")?.value || "";
    clearError();

    const row = input.closest("tr");
    if (!row) return;

    const pakno = input.value;
    setCursor(true);

    try {
      const response = await fetchWithSessionCheck("kereste/paket_oku", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ pno: pakno, cins: "CIKIS", fisno }),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const data = response;

      if (data.mesaj && data.mesaj !== "") {
        setCursor(false);
        setTimeout(() => alert(data.mesaj), 100);
        return;
      }

      if (!data.paket || data.paket.length === 0) {
        focusNextCell(event, input);
        return;
      }

      const currentRow = input.closest("tr");
      let targetIndex = currentRow.rowIndex - 1; // thead varsayımı

      const rowsNow = getKerRows();
      if (data.paket.length + targetIndex > rowsNow.length) {
        const needed = (data.paket.length + targetIndex) - rowsNow.length;
        for (let i = 0; i < needed; i++) satirekle();
      }

      const rows = getKerRows();

      data.paket.forEach((item) => {
        const cells = rows[targetIndex]?.cells;
        if (!cells) {
          targetIndex++;
          return;
        }

        const pakInput = cells[1]?.querySelector("input");
        if (pakInput) pakInput.value = input.value;

        const barkodSpan = cells[2]?.querySelector("label span");
        if (barkodSpan) barkodSpan.textContent = item.Barkod || "\u00A0";

        const koduSpan = cells[3]?.querySelector("label span");
        if (koduSpan) koduSpan.textContent = item.Kodu || "\u00A0";

        const miktarSpan = cells[4]?.querySelector("label span");
        if (miktarSpan) miktarSpan.textContent = formatNumber0(item.Miktar || 0);

        const m3Span = cells[5]?.querySelector("label span");
        if (m3Span) m3Span.textContent = formatNumber3(hesaplaM3(item.Kodu, item.Miktar) || 0);

        const hiddenIdx = cells.length - 1;
        if (cells[hiddenIdx]) cells[hiddenIdx].innerText = item.Satir || 0;

        targetIndex++;
      });

      updatePaketM3();
      updateColumnTotal();
    } catch (e) {
      showError(e.message);
    } finally {
      setCursor(false);
    }
  }

  /* ============ TOTALS ============ */
  function updateColumnTotal() {
    const rows = getKerRows();

    const totalSatirCell = byId("totalSatir");
    const totalTutarCell = byId("totalTutar");
    const totalMiktarCell = byId("totalMiktar");
    const totalM3Cell = byId("totalM3");
    const totalPaketM3Cell = byId("totalPaketM3");
    const tevoranEl = byId("tevoran");

    let totalm3 = 0;
    let totalpakm3 = 0;
    let totalMiktar = 0;
    let totalsatir = 0;

    let iskTop = 0;
    let kdvTop = 0;
    let brutTop = 0;

    rows.forEach((row) => {
      const firstColumn = row.querySelector("td:nth-child(2) input"); // paket no
      const mikSpan = row.querySelector("td:nth-child(5) label span");
      const m3Span = row.querySelector("td:nth-child(6) label span");
      const pm3Span = row.querySelector("td:nth-child(7) label span");

      const fiat = row.querySelector("td:nth-child(9) input");
      const iskonto = row.querySelector("td:nth-child(10) input");
      const kdvv = row.querySelector("td:nth-child(11) input");
      const tutar = row.querySelector("td:nth-child(12) input");

      if (firstColumn && firstColumn.value.trim() !== "") totalsatir += 1;

      if (fiat && m3Span && tutar && iskonto && kdvv) {
        const isk = parseLocaleNumber(iskonto.value) || 0;
        const kdv = parseLocaleNumber(kdvv.value) || 0;
        const fia = parseLocaleNumber(fiat.value) || 0;
        const m33 = parseLocaleNumber(m3Span.textContent.trim() || 0);

        const result = fia * m33; // brut
        tutar.value = formatNumber2(result);

        totalm3 += m33;
        totalpakm3 += parseLocaleNumber(pm3Span?.textContent?.trim() || 0);
        totalMiktar += mikSpan?.textContent?.trim()
          ? parseLocaleNumber(mikSpan.textContent.trim())
          : 0;

        if (result > 0) {
          brutTop += result;
          iskTop += (result * isk) / 100;
          kdvTop += ((result - (result * isk) / 100) * kdv) / 100;
        }
      }
    });

    const tev = tevoranEl ? (parseLocaleNumber(tevoranEl.value) || 0) : 0;

    byId("iskonto") && (byId("iskonto").innerText = formatNumber2(iskTop));
    byId("bakiye") && (byId("bakiye").innerText = formatNumber2(brutTop - iskTop));
    byId("kdv") && (byId("kdv").innerText = formatNumber2(kdvTop));

    byId("tevedkdv") && (byId("tevedkdv").innerText = formatNumber2((kdvTop / 10) * tev));
    const genelTop = (brutTop - iskTop) + kdvTop;
    byId("tevdahtoptut") && (byId("tevdahtoptut").innerText = formatNumber2(genelTop));
    byId("beyedikdv") && (byId("beyedikdv").innerText = formatNumber2(kdvTop - (kdvTop / 10) * tev));
    byId("tevhartoptut") && (
      byId("tevhartoptut").innerText =
        formatNumber2((brutTop - iskTop) + (kdvTop - (kdvTop / 10) * tev))
    );

    if (totalTutarCell)
      totalTutarCell.textContent = brutTop.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    if (totalMiktarCell)
      totalMiktarCell.textContent = totalMiktar.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 });
    if (totalM3Cell)
      totalM3Cell.textContent = totalm3.toLocaleString(undefined, { minimumFractionDigits: 3, maximumFractionDigits: 3 });
    if (totalPaketM3Cell)
      totalPaketM3Cell.textContent = totalpakm3.toLocaleString(undefined, { minimumFractionDigits: 3, maximumFractionDigits: 3 });
    if (totalSatirCell)
      totalSatirCell.textContent = totalsatir.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  }

  function hesaplaM3(kodu, miktar) {
    if (!kodu) return 0;
    const token = kodu.split("-");
    if (token.length !== 4) return 0;

    const d1 = token[1]?.trim();
    const d2 = token[2]?.trim();
    const d3 = token[3]?.trim();
    if (!d1 || !d2 || !d3) return 0;

    return ((parseFloat(d1) * parseFloat(d2) * parseFloat(d3)) * (miktar || 0)) / 1000000000;
  }

  function updatePaketM3() {
    const rows = getKerRows();
    const paketMap = new Map();
    let paketadet = 0;

    rows.forEach((row) => {
      const paketnoInput = row.querySelector("td:nth-child(2) input");
      const m3Span = row.querySelector("td:nth-child(6) label span");
      const paketm3Span = row.querySelector("td:nth-child(7) label span");

      if (!paketnoInput || !m3Span || !paketm3Span) return;

      const paketNo = paketnoInput.value.trim();
      const m3Value = parseLocaleNumber(m3Span.textContent.trim()) || 0;
      if (!paketNo) return;

      if (!paketMap.has(paketNo)) paketMap.set(paketNo, { total: 0, rows: [] });
      paketMap.get(paketNo).total += m3Value;
      paketMap.get(paketNo).rows.push(paketm3Span);
    });

    paketMap.forEach((data) => {
      const rowCount = data.rows.length;
      data.rows.forEach((cell, index) => {
        if (index === rowCount - 1) {
          cell.textContent = data.total ? data.total.toFixed(3) : "\u00A0";
          paketadet += 1;
        } else {
          cell.textContent = "\u00A0";
        }
      });
    });

    const tp = byId("totalPakadet");
    if (tp) tp.textContent = "Paket:" + formatNumber0(paketadet);
  }

  function focusNextRow(event, element) {
    if (event.key !== "Enter") return;
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
      satirekle();
      const rows = getKerRows();
      const lastRow = rows[rows.length - 1];
      const secondInput = lastRow?.querySelector("td:nth-child(2) input");
      if (secondInput) {
        secondInput.focus();
        secondInput.select?.();
      }
    }
  }

  function focusNextCell(event, element) {
    if (event.key !== "Enter") return;
    event.preventDefault();

    let currentCell = element.closest("td");
    let nextCell = currentCell?.nextElementSibling;

    while (nextCell) {
      const focusable = nextCell.querySelector("input,select");
      if (focusable) {
        focusable.focus();
        focusable.select?.();
        break;
      }
      nextCell = nextCell.nextElementSibling;
    }
  }

  function clearInputs() {
    const tableBody = byId("kerTbody");
    if (tableBody) tableBody.innerHTML = "";

    NS.state.rowCounter = 0;
    initializeRows();

    byId("totalSatir") && (byId("totalSatir").textContent = formatNumber0(0));
    byId("totalMiktar") && (byId("totalMiktar").textContent = formatNumber0(0));
    byId("totalM3") && (byId("totalM3").textContent = formatNumber3(0));
    byId("totalPaketM3") && (byId("totalPaketM3").textContent = formatNumber3(0));
    byId("totalTutar") && (byId("totalTutar").textContent = formatNumber2(0));
    byId("totalPakadet") && (byId("totalPakadet").textContent = "");

    byId("iskonto") && (byId("iskonto").innerText = "0.00");
    byId("bakiye") && (byId("bakiye").innerText = "0.00");
    byId("kdv") && (byId("kdv").innerText = "0.00");
    byId("tevedkdv") && (byId("tevedkdv").innerText = "0.00");
    byId("tevdahtoptut") && (byId("tevdahtoptut").innerText = "0.00");
    byId("beyedikdv") && (byId("beyedikdv").innerText = "0.00");
    byId("tevhartoptut") && (byId("tevhartoptut").innerText = "0.00");
  }

  async function kerOku() {
    const fisno = byId("fisno")?.value?.trim() || "";
    if (!fisno) return;

    setCursor(true);
    clearError();

    try {
      const data = await fetchWithSessionCheck("kereste/kerOku", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ fisno, cins: "CIKIS" }),
      });

      clearInputs();

      if (data?.errorMessage) throw new Error(data.errorMessage);
      if (!data?.data || data.data.length === 0) return;

      const rowsNow = getKerRows();
      if (data.data.length > rowsNow.length) {
        const add = data.data.length - rowsNow.length;
        for (let i = 0; i < add; i++) satirekle();
      }

      const rows = getKerRows();

      data.data.forEach((item, index) => {
        const cells = rows[index]?.cells;
        if (!cells) return;

        const paknoInput = cells[1]?.querySelector("input");
        if (paknoInput) paknoInput.value = (item.Paket_No || "") + "-" + (item.Konsimento || "");

        const barkodSpan = cells[2]?.querySelector("label span");
        if (barkodSpan) barkodSpan.textContent = (item.Barkod && item.Barkod.trim()) ? item.Barkod : "\u00A0";

        const urunSpan = cells[3]?.querySelector("label span");
        if (urunSpan) urunSpan.textContent = item.Kodu || "\u00A0";

        const mikSpan = cells[4]?.querySelector("label span");
        if (mikSpan) mikSpan.textContent = formatNumber0(item.Miktar || 0);

        const m3Span = cells[5]?.querySelector("label span");
        if (m3Span) m3Span.textContent = formatNumber3(hesaplaM3(item.Kodu, item.Miktar) || 0);

        const pm3Span = cells[6]?.querySelector("label span");
        if (pm3Span) pm3Span.textContent = "\u00A0";

        const depoSel = cells[7]?.querySelector("select");
        if (depoSel) depoSel.value = item.CDepo || "";

        const fiatInput = cells[8]?.querySelector("input");
        if (fiatInput) fiatInput.value = formatNumber2(item.CFiat || 0);

        const iskInput = cells[9]?.querySelector("input");
        if (iskInput) iskInput.value = formatNumber2(item.CIskonto || 0);

        const kdvInput = cells[10]?.querySelector("input");
        if (kdvInput) kdvInput.value = formatNumber2(item.CKdv || 0);

        const tutarInput = cells[11]?.querySelector("input");
        if (tutarInput) tutarInput.value = formatNumber2(item.CTutar || 0);

        const izahatInput = cells[12]?.querySelector("input");
        if (izahatInput) izahatInput.value = item.CIzahat || "";

        const hiddenIdx = cells.length - 1;
        if (cells[hiddenIdx]) cells[hiddenIdx].innerText = item.Satir || "";
      });

      // üst alanlar (senin blok korunuyor)
      const first = data.data[0];
      if (first) {
        byId("fisTarih") && (byId("fisTarih").value = formatdateSaatsiz(first.CTarih));
        byId("anagrp") && (byId("anagrp").value = first.Ana_Grup || "");
        byId("kur") && (byId("kur").value = first.CKur || "");
        if (typeof anagrpChanged === "function" && byId("anagrp")) await anagrpChanged(byId("anagrp"));
        byId("altgrp") && (byId("altgrp").value = first.Alt_Grup || "");
        byId("ozelkod") && (byId("ozelkod").value = first.Ozel_Kod || "");
        byId("nakliyeci") && (byId("nakliyeci").value = first.Nakliyeci || "");
        byId("tevoran") && (byId("tevoran").value = first.CTevkifat || "0");
        byId("carikod") && (byId("carikod").value = first.CCari_Firma || "");
        byId("adreskod") && (byId("adreskod").value = first.CAdres_Firma || "");
        byId("dovizcins") && (byId("dovizcins").value = first.CDoviz || "");
      }

      byId("a1") && (byId("a1").value = data.a1 || "");
      byId("a2") && (byId("a2").value = data.a2 || "");
      byId("not1") && (byId("not1").value = data?.dipnot?.[0] || "");
      byId("not2") && (byId("not2").value = data?.dipnot?.[1] || "");
      byId("not3") && (byId("not3").value = data?.dipnot?.[2] || "");

      updatePaketM3();
      updateColumnTotal();

      if (typeof hesapAdiOgren === "function") {
        hesapAdiOgren(byId("carikod")?.value || "", "cariadilbl");
      }
      if (typeof adrhesapAdiOgren === "function") {
        adrhesapAdiOgren("adreskod", "adresadilbl");
      }

      clearError();
    } catch (e) {
      showError(e.message);
    } finally {
      setCursor(false);
    }
  }

  function satirsil(button) {
    const row = button.closest("tr");
    if (row) row.remove();
    updatePaketM3();
    updateColumnTotal();
  }

  async function sonfis() {
    setCursor(true);
    clearError();
    try {
      const response = await fetchWithSessionCheck("kereste/sonfis", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ cins: "CIKIS" }),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      byId("fisno") && (byId("fisno").value = response.fisno || "");
      clearInputs();
      kerOku();
    } catch (e) {
      showError(e.message);
    } finally {
      setCursor(false);
    }
  }

  async function yeniFis() {
    clearError();
    clearInputs();
    setCursor(true);

    try {
      const response = await fetchWithSessionCheck("kereste/yenifis", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ cins: "CIKIS" }),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      byId("fisno") && (byId("fisno").value = response.fisno || "");
    } catch (e) {
      showError(e.message);
    } finally {
      setCursor(false);
    }
  }

  async function kerYoket() {
    const fisno = byId("fisno")?.value?.trim() || "";
    const table = byId("kerTable");
    const rowsLen = table?.rows?.length || 0;

    if (!fisno || fisno === "0" || rowsLen === 0) {
      alert("Geçerli bir evrak numarası giriniz.");
      return;
    }
    if (!confirm("Bu Fiş silinecek ?")) return;

    setCursor(true);
    setBtn("kersilButton", true, "Siliniyor...");

    try {
      const response = await fetchWithSessionCheck("kereste/fiscYoket", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ fisno }),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      clearInputs();
      byId("fisno") && (byId("fisno").value = "");
      clearError();
    } catch (e) {
      showError(e.message);
    } finally {
      setCursor(false);
      setBtn("kersilButton", false, "Sil");
    }
  }

  function prepareureKayit() {
    const keresteDTO = {
      fisno: byId("fisno")?.value || "",
      tarih: byId("fisTarih")?.value || "",
      ozelkod: byId("ozelkod")?.value || "",
      anagrup: byId("anagrp")?.value || "",
      altgrup: byId("altgrp")?.value || "",
      nakliyeci: byId("nakliyeci")?.value || "",

      carikod: byId("carikod")?.value || "",
      adreskod: byId("adreskod")?.value || "",

      dvzcins: byId("dovizcins")?.value || "",
      kur: parseLocaleNumber(byId("kur")?.value || 0) || 0.0,
      tevoran: parseLocaleNumber(byId("tevoran")?.value || 0) || 0.0,

      not1: byId("not1")?.value || "",
      not2: byId("not2")?.value || "",
      not3: byId("not3")?.value || "",

      acik1: byId("a1")?.value || "",
      acik2: byId("a2")?.value || "",
    };

    const tableData = getTableData();
    return { keresteDTO, tableData };
  }

  function getTableData() {
    const rows = getKerRows();
    const data = [];

    rows.forEach((row) => {
      const cells = row.querySelectorAll("td");
      const paketNo = cells[1]?.querySelector("input")?.value || "";
      if (!paketNo.trim()) return;

      const hiddenIdx = cells.length - 1;

      data.push({
        paketno: paketNo,
        cdepostring: cells[7]?.querySelector("select")?.value || "",
        cfiat: parseLocaleNumber(cells[8]?.querySelector("input")?.value || 0),
        ciskonto: parseLocaleNumber(cells[9]?.querySelector("input")?.value || 0),
        ckdv: parseLocaleNumber(cells[10]?.querySelector("input")?.value || 0),
        ctutar: parseLocaleNumber(cells[11]?.querySelector("input")?.value || 0),
        cizahat: cells[12]?.querySelector("input")?.value || "",
        satir: parseInt(cells[hiddenIdx]?.textContent?.trim() || "0", 10) || 0,

        ukodu: cells[3]?.textContent?.trim() || "",
        miktar: parseInt(cells[4]?.textContent?.trim() || "0", 10) || 0,
        m3: parseFloat(cells[5]?.textContent?.trim() || "0") || 0.0,
        pakm3: parseFloat(cells[6]?.textContent?.trim() || "0") || 0.0,
      });
    });

    return data;
  }

  async function kerKayit() {
    const fisno = byId("fisno")?.value || "";
    const table = byId("kerTable");
    const rowsLen = table?.rows?.length || 0;

    if (!fisno || fisno === "0" || rowsLen === 0) {
      alert("Geçerli bir evrak numarası giriniz.");
      return;
    }

    const dto = prepareureKayit();
    setBtn("kerkaydetButton", true, "İşleniyor...");
    setCursor(true);
    clearError();

    try {
      const response = await fetchWithSessionCheck("kereste/cikKayit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      clearInputs();
      byId("fisno") && (byId("fisno").value = "");
      clearError();
    } catch (e) {
      showError(e.message);
    } finally {
      setCursor(false);
      setBtn("kerkaydetButton", false, "Kaydet");
    }
  }

  function cikisbilgiler() {
    return {
      totaltutar: byId("totalTutar")?.innerText?.trim() || "0.00",
      totalmiktar: byId("totalMiktar")?.innerText?.trim() || "0",
      totalm3: byId("totalM3")?.innerText?.trim() || "0.000",
      totalpaketm3: byId("totalPaketM3")?.innerText?.trim() || "0.000",
      paketsayi: (byId("totalPakadet")?.textContent || "").replace("Paket:", "").trim(),

      iskonto: byId("iskonto")?.innerText?.trim() || "0.00",
      bakiye: byId("bakiye")?.innerText?.trim() || "0.00",
      kdv: byId("kdv")?.innerText?.trim() || "0.00",
      tevedkdv: byId("tevedkdv")?.innerText?.trim() || "0.00",
      tevdahtoptut: byId("tevdahtoptut")?.innerText?.trim() || "0.00",
      beyedikdv: byId("beyedikdv")?.innerText?.trim() || "0.00",
      tevhartoptut: byId("tevhartoptut")?.innerText?.trim() || "0.00",
    };
  }

  async function downloadcikis() {
    const fisno = byId("fisno")?.value || "";
    const table = byId("kerTable");
    const rowsLen = table?.rows?.length || 0;

    if (!fisno || fisno === "0" || rowsLen === 0) {
      alert("Geçerli bir evrak numarası giriniz.");
      return;
    }

    const dto = { ...prepareureKayit(), cikisbilgiDTO: cikisbilgiler() };
    setBtn("cikisdownloadButton", true, "İşleniyor...");
    setCursor(true);
    clearError();

    try {
      const response = await fetchWithSessionCheckForDownload("kereste/cikis_download", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
      });

      if (response?.blob) {
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
      } else {
        throw new Error("Dosya indirilemedi.");
      }
    } catch (e) {
      showError(e.message);
    } finally {
      setCursor(false);
      setBtn("cikisdownloadButton", false, "Yazdir");
    }
  }

  async function cikismailAt() {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    const dto = { ...prepareureKayit(), cikisbilgiDTO: cikisbilgiler() };
    localStorage.setItem("keresteyazdirDTO", JSON.stringify(dto));

    const degerler = "kercikis";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;

    if (typeof mailsayfasiYukle === "function") mailsayfasiYukle(url);
    else console.warn("mailsayfasiYukle bulunamadı!");
  }

  /* ============ CARI ISLE (EKLENDI + EXPORT) ============ */
  async function kercariIsle() {
    const hesapKodu = byId("kerBilgi")?.value || "";
    const fisno = byId("fisno")?.value || "";
    const table = byId("kerTable");
    const rowsLen = table?.rows?.length || 0;

    if (!fisno || fisno === "0" || rowsLen === 0) {
      alert("Geçerli bir evrak numarasi giriniz.");
      return;
    }

    clearError();
    setCursor(true);
    setBtn("kercarikayitButton", true, "İşleniyor...");

    const keresteDTO = {
      fisno: fisno,
      tarih: byId("fisTarih")?.value || "",
      carikod: byId("carikod")?.value || "",
      miktar: byId("totalM3")?.textContent || 0,
      tutar: parseLocaleNumber(byId("tevhartoptut")?.innerText || 0),
      karsihesapkodu: hesapKodu,
    };

    try {
      const response = await fetchWithSessionCheck("kereste/kerccariKayit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(keresteDTO),
      });

      if (response?.errorMessage && response.errorMessage.trim() !== "") {
        throw new Error(response.errorMessage);
      }

      clearError();
    } catch (e) {
      showError(e?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
      setBtn("kercarikayitButton", false, "Cari Kaydet");
      setCursor(false);
    }
  }

	/* ========================= anagrpChanged ========================= */
	async function anagrpChanged (anagrpElement)  {
	    const anagrup = anagrpElement?.value || "";
	    const selectElement = byId("altgrp");
	    if (!selectElement) return;
	    selectElement.innerHTML = "";
	    if (anagrup === "") {
	      selectElement.disabled = true;
	      return;
	    }
	   setCursor(true);
	   clearError();
	    try {
	      const response = await window.fetchWithSessionCheck("kereste/altgrup", {
	        method: "POST",
	        headers: { "Content-Type": "application/x-www-form-urlencoded" },
	        body: new URLSearchParams({ anagrup })
	      });
	      if (response?.errorMessage) throw new Error(response.errorMessage);
	      (response?.altKodlari || []).forEach((kod) => {
	        const option = document.createElement("option");
	        option.value = kod.ALT_GRUP;
	        option.textContent = kod.ALT_GRUP;
	        selectElement.appendChild(option);
	      });

	      selectElement.disabled = selectElement.options.length === 0;
	    } catch (error) {
	      selectElement.disabled = true;
	      showError(error?.message || "Beklenmeyen bir hata oluştu.");
	    } finally {
	      setCursor(false);
	    }
	  }
  /* ============ INIT ============ */
  function init() {
    fetchpakdepo(); // bu zaten initializeRows çağırıyor
  }

  /* ============ EXPORTS ============ */
  NS.init = init;

  NS.fetchpakdepo = fetchpakdepo;
  NS.initializeRows = initializeRows;
  NS.satirekle = satirekle;
  NS.satirsil = satirsil;

  NS.handleBlur = handleBlur;
  NS.selectAllContent = selectAllContent;

  NS.paketkontrol = paketkontrol;

  NS.updateColumnTotal = updateColumnTotal;
  NS.updatePaketM3 = updatePaketM3;
  NS.hesaplaM3 = hesaplaM3;

  NS.focusNextRow = focusNextRow;
  NS.focusNextCell = focusNextCell;

  NS.clearInputs = clearInputs;
  NS.kerOku = kerOku;

  NS.sonfis = sonfis;
  NS.yeniFis = yeniFis;
  NS.kerYoket = kerYoket;

  NS.prepareureKayit = prepareureKayit;
  NS.getTableData = getTableData;

  NS.kerKayit = kerKayit;

  NS.cikisbilgiler = cikisbilgiler;
  NS.downloadcikis = downloadcikis;
  NS.cikismailAt = cikismailAt;

  NS.kercariIsle = kercariIsle;
	NS.anagrpChanged = anagrpChanged;
})(OBS.KERCIKIS);

