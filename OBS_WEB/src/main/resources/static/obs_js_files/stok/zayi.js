/* =========================
   OBS Namespace (çakışmasız)
   ========================= */
window.OBS = window.OBS || {};
OBS.ZAI = OBS.ZAI || {};

(() => {
  const Z = OBS.ZAI;

  /* ---------- state ---------- */
  Z.rowCounter = 0;
  Z.depolar = [];
  Z.urnkodlar = [];
  Z.tableData = [];

  /* ---------- helpers ---------- */
  Z.byId = (id) => document.getElementById(id);

  Z.errClear = () => {
    const e = Z.byId("errorDiv");
    if (!e) return;
    e.innerText = "";
    e.style.display = "none";
  };

  Z.errShow = (msg) => {
    const e = Z.byId("errorDiv");
    if (!e) return;
    e.innerText = msg || "Beklenmeyen bir hata oluştu.";
    e.style.display = "block";
  };

  Z.cursor = (mode) => { document.body.style.cursor = mode || "default"; };

  Z.incrementRowCounter = () => { Z.rowCounter++; };

  Z.setLabelContent = (cell, content) => {
    const span = cell?.querySelector("label span");
    if (span) span.textContent = content ? content : "\u00A0";
  };

  Z.disableBtn = (id, yes, textYes, textNo) => {
    const b = Z.byId(id);
    if (!b) return;
    b.disabled = !!yes;
    if (yes && textYes != null) b.innerText = textYes;
    if (!yes && textNo != null) b.innerText = textNo;
  };

  /* =========================
     FETCH / INIT
     ========================= */

  Z.fetchkod = async function () {
    Z.errClear();
    Z.rowCounter = 0;
    Z.depolar = [];
    Z.urnkodlar = [];

    try {
      const response = await fetchWithSessionCheck("stok/stkgeturndepo", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      Z.urnkodlar = response.urnkodlar || [];
      Z.depolar  = response.depolar  || [];
      Z.initializeRows();
    } catch (error) {
      Z.errShow(error?.message);
    }
  };

  Z.anagrpChanged = async function (anagrpElement) {
    const anagrup = anagrpElement?.value || "";
    const selectElement = Z.byId("altgrp");
    if (!selectElement) return;

    selectElement.innerHTML = "";

    if (anagrup === "") {
      selectElement.disabled = true;
      return;
    }

    Z.cursor("wait");
    Z.errClear();

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
    } catch (error) {
      selectElement.disabled = true;
      Z.errShow(error?.message);
    } finally {
      Z.cursor("default");
    }
  };

  /* =========================
     TABLE
     ========================= */

  Z.initializeRows = function () {
    Z.rowCounter = 0;
    const tbody = Z.byId("zaiTable")?.querySelector("tbody");
    if (tbody) tbody.innerHTML = "";
    for (let i = 0; i < 5; i++) Z.satirekle();
  };

  Z.satirekle = function () {
    const tbody = Z.byId("zaiTable")?.querySelector("tbody");
    if (!tbody) {
      console.error("zaiTable tbody bulunamadı!");
      return null;
    }

    const newRow = tbody.insertRow();
    Z.incrementRowCounter();

    const ukoduoptionsHTML = (Z.urnkodlar || [])
      .map(kod => `<option value="${kod.Kodu}">${kod.Kodu}</option>`)
      .join("");

    const depoOptionsHTML = (Z.depolar || [])
      .map(kod => `<option value="${kod.DEPO}">${kod.DEPO}</option>`)
      .join("");

	  newRow.innerHTML = `
	    <td>
	      <button id="bsatir_${Z.rowCounter}"
	        type="button"
	        class="btn btn-secondary cam-icon"
	        onclick="OBS.ZAI.satirsil(this)">
	        <i class="fa fa-trash"></i>
	      </button>
	    </td>

	    <td>
	      <div class="zai-rel">
	        <input class="cam-input"
	          list="barkodOptions_${Z.rowCounter}"
	          maxlength="20"
	          id="barkod_${Z.rowCounter}"
	          onkeydown="OBS.ZAI.focusNextCell(event, this)"
	          ondblclick="openurunkodlariModal('barkod_${Z.rowCounter}','fatsatir','barkodkod')"
	          onchange="OBS.ZAI.updateRowValues(this,'Barkod')">
	        <datalist id="barkodOptions_${Z.rowCounter}"></datalist>
	        <span class="zai-arrow">▼</span>
	      </div>
	    </td>

	    <td>
	      <div class="zai-rel">
	        <input class="cam-input"
	          list="ukoduOptions_${Z.rowCounter}"
	          maxlength="12"
	          id="ukodu_${Z.rowCounter}"
	          onkeydown="OBS.ZAI.focusNextCell(event, this)"
	          ondblclick="openurunkodlariModal('ukodu_${Z.rowCounter}','recetesatir','ukodukod')"
	          onchange="OBS.ZAI.updateRowValues(this,'Kodu')">
	        <datalist id="ukoduOptions_${Z.rowCounter}">
	          ${ukoduoptionsHTML}
	        </datalist>
	        <span class="zai-arrow">▼</span>
	      </div>
	    </td>

	    <td>
	      <div class="zai-rel">
	        <select class="cam-select" id="depo_${Z.rowCounter}">
	          ${depoOptionsHTML}
	        </select>
	        <span class="zai-arrow">▼</span>
	      </div>
	    </td>

	    <td>
	      <input class="cam-input ta-right"
	        value="${formatNumber2(0)}"
	        onfocus="OBS.ZAI.selectAllContent(this)"
	        onblur="OBS.ZAI.handleBlur(this)"
	        onkeydown="OBS.ZAI.focusNextCell(event, this)">
	    </td>

	    <td>
	      <input class="cam-input ta-right"
	        value="${formatNumber3(0)}"
	        onfocus="OBS.ZAI.selectAllContent(this)"
	        onblur="OBS.ZAI.handleBlur3(this)"
	        onkeydown="OBS.ZAI.focusNextCell(event, this)">
	    </td>

	    <td>
	      <label class="cam-input cam-label"><span>&nbsp;</span></label>
	    </td>

	    <td>
	      <input class="cam-input ta-right"
	        value="${formatNumber2(0)}"
	        onfocus="OBS.ZAI.selectAllContent(this)"
	        onblur="OBS.ZAI.handleBlur(this)"
	        onkeydown="OBS.ZAI.focusNextCell(event, this)">
	    </td>

	    <td>
	      <input class="cam-input"
	        onfocus="OBS.ZAI.selectAllContent(this)"
	        onkeydown="OBS.ZAI.focusNextRow(event, this)">
	    </td>
	  `;


    return newRow;
  };

  /* ---------- format / blur ---------- */
  Z.handleBlur3 = function (input) {
    input.value = formatNumber3(input.value);
    Z.updateColumnTotal();
  };

  Z.handleBlur = function (input) {
    input.value = formatNumber2(input.value);
    Z.updateColumnTotal();
  };

  Z.selectAllContent = function (el) { el?.select?.(); };

  /* ---------- delete row ---------- */
  Z.satirsil = function (button) {
    const row = button?.closest("tr");
    if (row) row.remove();
    Z.updateColumnTotal();
  };

  /* ---------- navigation ---------- */
  Z.focusNextRow = function (event, element) {
    if (event.key !== "Enter") return;
    event.preventDefault();

    const currentRow = element.closest("tr");
    const nextRow = currentRow?.nextElementSibling;

    if (nextRow) {
      const secondInput = nextRow.querySelector("td:nth-child(3) input");
      if (secondInput) { secondInput.focus(); secondInput.select?.(); }
      return;
    }

    Z.satirekle();
    const tbody = currentRow?.parentElement;
    const newRow = tbody?.lastElementChild;
    const secondInput = newRow?.querySelector("td:nth-child(3) input");
    if (secondInput) { secondInput.focus(); secondInput.select?.(); }
  };

  Z.focusNextCell = function (event, element) {
    if (event.key !== "Enter") return;
    event.preventDefault();

    let currentCell = element.closest("td");
    let nextCell = currentCell?.nextElementSibling;

    while (nextCell) {
      const focusable = nextCell.querySelector("input, select");
      if (focusable) {
        focusable.focus();
        focusable.select?.();
        break;
      }
      nextCell = nextCell.nextElementSibling;
    }
  };

  /* ---------- row info fetch ---------- */
  Z.updateRowValues = async function (inputElement, kodbarkod) {
    const selectedValue = inputElement?.value || "";

    Z.cursor("wait");
    Z.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/urnbilgiArama", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ deger: selectedValue, kodbarkod }),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const row = inputElement.closest("tr");
      const cells = row.querySelectorAll("td");

      Z.setLabelContent(cells[6], response?.urun?.birim);

      Z.byId("adil").innerText    = response?.urun?.adi     || "";
      Z.byId("anagrpl").innerText = response?.urun?.anagrup || "";
      Z.byId("altgrpl").innerText = response?.urun?.altgrup || "";
    } catch (error) {
      Z.errShow(error?.message);
    } finally {
      Z.cursor("default");
    }
  };

  /* ---------- totals ---------- */
  Z.updateColumnTotal = function () {
    const rows = document.querySelectorAll("table tr");
    const totalTutarCell = Z.byId("totalTutar");
    let total = 0;
    let totalmiktar = 0;

    if (totalTutarCell) totalTutarCell.textContent = "0.00";

    rows.forEach((row) => {
      const input5 = row.querySelector("td:nth-child(5) input");
      const input6 = row.querySelector("td:nth-child(6) input");
      const input8 = row.querySelector("td:nth-child(8) input");

      if (input5 && input6 && input8) {
        const value5 = parseLocaleNumber(input5.value) || 0;
        const value6 = parseLocaleNumber(input6.value) || 0;
        const result = value5 * value6;

        input8.value = result.toLocaleString(undefined, {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2,
        });

        if (result > 0) total += result;
        totalmiktar += value6;
      }
    });

    if (totalTutarCell) {
      totalTutarCell.textContent = total.toLocaleString(undefined, {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      });
    }

    const tm = Z.byId("totalMiktar");
    if (tm) {
      tm.textContent = totalmiktar.toLocaleString(undefined, {
        minimumFractionDigits: 3,
        maximumFractionDigits: 3,
      });
    }
  };

  /* =========================
     CLEAR / RESET
     ========================= */

  Z.clearInputs = function () {
    const anagrp = Z.byId("anagrp");
    const altgrp = Z.byId("altgrp");

    if (anagrp) anagrp.value = "";
    if (altgrp) {
      altgrp.innerHTML = "";
      altgrp.disabled = true;
    }

    const a1 = Z.byId("a1");
    const a2 = Z.byId("a2");
    if (a1) a1.value = "";
    if (a2) a2.value = "";

    if (Z.byId("adil"))    Z.byId("adil").innerText = "";
    if (Z.byId("anagrpl")) Z.byId("anagrpl").innerText = "";
    if (Z.byId("altgrpl")) Z.byId("altgrpl").innerText = "";

    if (Z.byId("totalMiktar")) Z.byId("totalMiktar").textContent = "0.000";
    if (Z.byId("totalTutar"))  Z.byId("totalTutar").textContent = "0.00";

    Z.initializeRows();
  };

  /* =========================
     SON FİŞ / YENİ FİŞ
     ========================= */

  Z.sonfis = async function () {
    Z.errClear();
    Z.cursor("wait");

    try {
      const response = await fetchWithSessionCheck("stok/zaisonfis", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const fisNoInput = Z.byId("fisno");
      if (fisNoInput) fisNoInput.value = response.fisno;

      if (response.fisNo === 0) { // senin eski kod mantığı
        Z.errShow(response.errorMessage || "Evrak numarası bulunamadı.");
        return;
      }

      await Z.zaiOku();
    } catch (error) {
      Z.errShow(error?.message);
    } finally {
      Z.cursor("default");
    }
  };

  Z.yeniFis = async function () {
    Z.cursor("wait");
    Z.errClear();
    Z.clearInputs();

    try {
      const response = await fetchWithSessionCheck("stok/zaiyenifis", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const fisNoInput = Z.byId("fisno");
      if (fisNoInput) fisNoInput.value = response.fisno;
    } catch (error) {
      Z.errShow(error?.message);
    } finally {
      Z.cursor("default");
    }
  };

  /* =========================
     OKU
     ========================= */

  Z.zaiOku = async function () {
    const fisno = Z.byId("fisno")?.value;
    if (!fisno) return;

    Z.errClear();
    Z.cursor("wait");

    try {
      const response = await fetchWithSessionCheck("stok/zaiOku", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ fisno }),
      });

      const data = response;
      Z.clearInputs();

      const table = Z.byId("zaiTable");
      const rowss = table?.querySelectorAll("tbody tr") || [];
      if ((data?.data?.length || 0) > rowss.length) {
        const additional = data.data.length - rowss.length;
        for (let i = 0; i < additional; i++) Z.satirekle();
      }

      const rows = table.querySelectorAll("tbody tr");
      (data.data || []).forEach((item, index) => {
        const cells = rows[index].cells;

        const barkodInput = cells[1]?.querySelector("input");
        if (barkodInput) barkodInput.value = item.Barkod || "";

        const urunKoduInput = cells[2]?.querySelector("input");
        if (urunKoduInput) urunKoduInput.value = item.Urun_Kodu || "";

        const depoSelect = cells[3]?.querySelector("select");
        if (depoSelect) depoSelect.value = item.Depo || "";

        const fiatInput = cells[4]?.querySelector("input");
        if (fiatInput) fiatInput.value = formatNumber2(item.Fiat);

        const miktarInput = cells[5]?.querySelector("input");
        if (miktarInput) miktarInput.value = formatNumber3(item.Miktar * -1);

        Z.setLabelContent(cells[6], item.Birim || "");

        const tutarInput = cells[7]?.querySelector("input");
        if (tutarInput) tutarInput.value = formatNumber2(item.Tutar * -1);

        const izahatInput = cells[8]?.querySelector("input");
        if (izahatInput) izahatInput.value = item.Izahat || "";
      });

      // üst alanlar
      if (data?.data?.length) {
        const first = data.data[0];
        if (Z.byId("fisTarih")) Z.byId("fisTarih").value = formatdateSaatsiz(first.Tarih);
        if (Z.byId("anagrp"))   Z.byId("anagrp").value = first.Ana_Grup || "";

        await Z.anagrpChanged(Z.byId("anagrp"));

        if (Z.byId("altgrp")) Z.byId("altgrp").value = first.Alt_Grup || "";
      }

      if (Z.byId("a1")) Z.byId("a1").value = data.aciklama1 || "";
      if (Z.byId("a2")) Z.byId("a2").value = data.aciklama2 || "";

      Z.updateColumnTotal();
    } catch (error) {
      Z.errShow(error?.message);
    } finally {
      Z.cursor("default");
    }
  };

  /* =========================
     KAYIT
     ========================= */

  Z.prepareureKayit = function () {
    const zaiDTO = {
      fisno:  Z.byId("fisno")?.value || "",
      tarih:  Z.byId("fisTarih")?.value || "",
      anagrup: Z.byId("anagrp")?.value || "",
      altgrup: Z.byId("altgrp")?.value || "",
      acik1:  Z.byId("a1")?.value || "",
      acik2:  Z.byId("a2")?.value || "",
    };

    const tableData = Z.getTableData();
    return { zaiDTO, tableData };
  };

  Z.getTableData = function () {
    const table = Z.byId("zaiTable");
    const rows = table?.querySelectorAll("tbody tr") || [];
    const data = [];

    rows.forEach((row) => {
      const cells = row.querySelectorAll("td");
      const ukodu = cells[2]?.querySelector("input")?.value || "";

      if (ukodu.trim()) {
        data.push({
          ukodu,
          depo:   cells[3]?.querySelector("select")?.value || "",
          fiat:   parseLocaleNumber(cells[4]?.querySelector("input")?.value),
          miktar: parseLocaleNumber(cells[5]?.querySelector("input")?.value),
          tutar:  parseLocaleNumber(cells[7]?.querySelector("input")?.value),
          izahat: cells[8]?.querySelector("input")?.value || "",
        });
      }
    });

    return data;
  };

  Z.zaiKayit = async function () {
    const fisno = Z.byId("fisno")?.value || "";
    const rowsCount = Z.byId("zaiTable")?.rows?.length || 0;

    if (!fisno || fisno === "0" || rowsCount === 0) {
      alert("Geçerli bir evrak numarası giriniz.");
      return;
    }

    const dto = Z.prepareureKayit();

    Z.disableBtn("zaikaydetButton", true, "İşleniyor...", "Kaydet");
    Z.cursor("wait");
    Z.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/zaiKayit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
      });

      // senin eski kod: response.errorMessage.trim() !== ""
      if ((response?.errorMessage || "").trim() !== "") {
        throw new Error(response.errorMessage);
      }

      Z.clearInputs();
      if (Z.byId("fisno")) Z.byId("fisno").value = "";
      Z.errClear();
    } catch (error) {
      Z.errShow(error?.message);
    } finally {
      Z.cursor("default");
      Z.disableBtn("zaikaydetButton", false, "İşleniyor...", "Kaydet");
    }
  };

  /* =========================
     YOKET
     ========================= */

  Z.zaiYoket = async function () {
    const fisNoInput = Z.byId("fisno");
    if (!fisNoInput || ["0", ""].includes(fisNoInput.value)) return;

    const ok = confirm("Bu Uretim fisi silinecek ?");
    if (!ok) return;

    Z.disableBtn("zaisilButton", true, "Siliniyor...", "Sil");
    Z.cursor("wait");
    Z.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/zaiYoket", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ fisno: fisNoInput.value }),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      Z.clearInputs();
      fisNoInput.value = "";
      Z.errClear();
    } catch (error) {
      Z.errShow(error?.message);
    } finally {
      Z.cursor("default");
      Z.disableBtn("zaisilButton", false, "Siliniyor...", "Sil");
    }
  };

})();
