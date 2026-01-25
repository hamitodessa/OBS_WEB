/* =========================
   OBS Namespace (çakışmasız)
   ========================= */
window.OBS = window.OBS || {};
OBS.RECETE = OBS.RECETE || {};

(() => {
  const R = OBS.RECETE;

  /* ---------- state ---------- */
  R.rowCounter = 0;
  R.urnkodlar = [];
  R.tableData = [];

  /* ---------- helpers ---------- */
  R.byId = (id) => document.getElementById(id);

  R.errClear = () => {
    const e = R.byId("errorDiv");
    if (!e) return;
    e.innerText = "";
    e.style.display = "none";
  };

  R.errShow = (msg) => {
    const e = R.byId("errorDiv");
    if (!e) return;
    e.innerText = msg || "Beklenmeyen bir hata oluştu.";
    e.style.display = "block";
  };

  R.cursor = (mode) => { document.body.style.cursor = mode || "default"; };

  R.incrementRowCounter = () => { R.rowCounter++; };

  R.setLabelContent = (cell, content) => {
    const span = cell?.querySelector("label span");
    if (span) span.textContent = content ? content : "\u00A0";
  };

  R.selectAllContent = (el) => { el?.select?.(); };

  R.disableBtn = (id, yes, textYes, textNo) => {
    const b = R.byId(id);
    if (!b) return;
    b.disabled = !!yes;
    if (yes && textYes != null) b.innerText = textYes;
    if (!yes && textNo != null) b.innerText = textNo;
  };

  /* =========================
     FETCH / INIT
     ========================= */

  R.fetchkod = async function () {
    R.errClear();
    R.rowCounter = 0;
    R.urnkodlar = [];

    try {
      const response = await fetchWithSessionCheck("stok/stkgeturn", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      R.urnkodlar = response.urnkodlar || [];
      R.initializeRows();
    } catch (error) {
      R.errShow(error?.message);
    }
  };

  /* =========================
     SON FİŞ / YENİ FİŞ
     ========================= */

  R.sonfis = async function () {
    R.errClear();
    R.cursor("wait");

    try {
      const response = await fetchWithSessionCheck("stok/recsonfis", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      if (R.byId("recno")) R.byId("recno").value = response.recno;
      await R.receteOku();
    } catch (error) {
      R.errShow(error?.message);
    } finally {
      R.cursor("default");
      // eski kod: finally'de errorDiv gizliyordu ama bu hata mesajını da yok ediyordu
      // biz HATA varsa kalsın istiyoruz => burada saklamıyoruz.
    }
  };

  R.yeniFis = async function () {
    R.errClear();
    R.cursor("wait");
    R.clearInputs();

    try {
      const response = await fetchWithSessionCheck("stok/recyenifis", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      if (R.byId("recno")) R.byId("recno").value = response.recno;
    } catch (error) {
      R.errShow(error?.message);
    } finally {
      R.cursor("default");
    }
  };

  /* =========================
     ROWS
     ========================= */

  R.initializeRows = function () {
    R.rowCounter = 0;

    // tbody temizle
    const tbody = R.byId("recTable")?.querySelector("tbody");
    if (tbody) tbody.innerHTML = "";

    for (let i = 0; i < 5; i++) R.satirekle();
  };

  R.satirekle = function () {
    const tbody = R.byId("recTable")?.querySelector("tbody");

    if (!tbody) {
      console.error("recTable tbody bulunamadı!");
      return null;
    }

    const newRow = tbody.insertRow();
    R.incrementRowCounter();

    const ukoduoptionsHTML = (R.urnkodlar || [])
      .map(kod => `<option value="${kod.Kodu}">${kod.Kodu}</option>`)
      .join("");

    newRow.innerHTML = `
      <td>
        <button id="bsatir_${R.rowCounter}"
          type="button"
          class="btn btn-secondary"
          onclick="OBS.RECETE.satirsil(this)">
          <i class="fa fa-trash"></i>
        </button>
      </td>

      <td>
        <label class="form-control"><span>CIKAN</span></label>
      </td>

      <td>
        <div class="rec-rel">
          <input class="form-control cins_bold"
            list="ukoduOptions_${R.rowCounter}"
            maxlength="12"
            id="ukodu_${R.rowCounter}"
            onkeydown="OBS.RECETE.focusNextCell(event, this)"
            ondblclick="openurunkodlariModal('ukodu_${R.rowCounter}','recetesatir','ukodukod')"
            onchange="OBS.RECETE.updateRowValues(this)">
          <datalist id="ukoduOptions_${R.rowCounter}">
            ${ukoduoptionsHTML}
          </datalist>
          <span class="rec-arrow">▼</span>
        </div>
      </td>

      <td>
        <label class="form-control"><span>&nbsp;</span></label>
      </td>

      <td>
        <label class="form-control"><span>&nbsp;</span></label>
      </td>

      <td>
        <input class="form-control ta-right"
          value="${formatNumber3(0)}"
          onfocus="OBS.RECETE.selectAllContent(this)"
          onblur="OBS.RECETE.handleBlur3(this)"
          onkeydown="OBS.RECETE.focusNextRow(event, this)">
      </td>
    `;

    return newRow;
  };

  /* =========================
     FORMAT / TOTAL
     ========================= */

  R.handleBlur3 = function (input) {
    input.value = formatNumber3(parseLocaleNumber(input.value));
    R.updateColumnTotal();
  };

  // senin kodda updateColumnTotal() var ama burada yoktu
  // Recete tabloda toplam istiyorsan buraya koy (şimdilik boş bırakıyorum, çağrılınca patlamasın)
  R.updateColumnTotal = function () {
    // burada recete için toplam hesaplıyorsan yazarsın
  };

  /* =========================
     DELETE ROW
     ========================= */

  R.satirsil = function (button) {
    const row = button?.closest("tr");
    if (row) row.remove();
    R.updateColumnTotal();
  };

  /* =========================
     NAVIGATION
     ========================= */

  R.focusNextRow = function (event, element) {
    if (event.key !== "Enter") return;
    event.preventDefault();

    const currentRow = element.closest("tr");
    const nextRow = currentRow?.nextElementSibling;

    if (nextRow) {
      const secondInput = nextRow.querySelector("td:nth-child(3) input");
      if (secondInput) {
        secondInput.focus();
        secondInput.select?.();
      }
    } else {
      R.satirekle();
      const tbody = currentRow?.parentElement;
      const newRow = tbody?.lastElementChild;
      const secondInput = newRow?.querySelector("td:nth-child(3) input");
      if (secondInput) {
        secondInput.focus();
        secondInput.select?.();
      }
    }
  };

  R.focusNextCell = function (event, element) {
    if (event.key !== "Enter") return;
    event.preventDefault();

    let currentCell = element.closest("td");
    let nextCell = currentCell?.nextElementSibling;

    while (nextCell) {
      const focusableElement = nextCell.querySelector("input");
      if (focusableElement) {
        focusableElement.focus();
        focusableElement.select?.();
        break;
      }
      nextCell = nextCell.nextElementSibling;
    }
  };

  /* =========================
     ROW VALUES
     ========================= */

  R.updateRowValues = async function (inputElement) {
    const selectedValue = inputElement?.value || "";
    R.cursor("wait");
    R.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/recetecikan", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ kodu: selectedValue, cins: "Kodu" }),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const row = inputElement.closest("tr");
      const cells = row.querySelectorAll("td");

      R.setLabelContent(cells[3], response?.urun?.adi);
      R.setLabelContent(cells[4], response?.urun?.birim);
    } catch (error) {
      R.errShow(error?.message);
    } finally {
      R.cursor("default");
    }
  };

  R.updateurunValues = async function (inputElement) {
    const selectedValue = inputElement?.value || "";
    R.cursor("wait");
    R.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/recetecikan", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ kodu: selectedValue, cins: "Kodu" }),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      if (R.byId("agirlik")) R.byId("agirlik").innerText = response?.urun?.agirlik ?? "";
      if (R.byId("anagrpl")) R.byId("anagrpl").innerText = response?.urun?.anagrup ?? "";
      if (R.byId("altgrpl")) R.byId("altgrpl").innerText = response?.urun?.altgrup ?? "";
      if (R.byId("barkod"))  R.byId("barkod").innerText  = response?.urun?.barkod ?? "";
      if (R.byId("birim"))   R.byId("birim").innerText   = response?.urun?.birim ?? "";
      if (R.byId("adi"))     R.byId("adi").innerText     = response?.urun?.adi ?? "";
    } catch (error) {
      R.errShow(error?.message);
    } finally {
      R.cursor("default");
    }
  };

  /* =========================
     OKU
     ========================= */

  R.receteOku = async function () {
    const recno = R.byId("recno")?.value;
    if (!recno) return;

    const errorDiv = R.byId("errorDiv");
    R.cursor("wait");
    R.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/receteOku", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ recno }),
      });

      const data = response;
      R.clearInputs();

      if (data?.errorMessage) throw new Error(data.errorMessage);

      const table = R.byId("recTable");
      const rows = table.querySelectorAll("tbody tr");

      if ((data?.data?.length || 0) > rows.length) {
        const additionalRows = data.data.length - rows.length;
        for (let i = 0; i < additionalRows; i++) R.satirekle();
      }

      const rows2 = table.querySelectorAll("tbody tr");

      (data.data || []).forEach((item, index) => {
        if (item.Tur === "Cikan" && index < rows2.length) {
          const cells = rows2[index].cells;

          R.setLabelContent(cells[1], item.Tur);

          const urunKoduInput = cells[2]?.querySelector("input");
          if (urunKoduInput) urunKoduInput.value = item.Kodu || "";

          R.setLabelContent(cells[3], item.Adi || "");
          R.setLabelContent(cells[4], item.Birim || "");

          const miktarInput = cells[5]?.querySelector("input");
          if (miktarInput) miktarInput.value = formatNumber3(item.Miktar);
        }
      });

      // giren satır bilgileri
      for (let i = 0; i < (data.data || []).length; i++) {
        const item = data.data[i];
        if (item.Tur === "Giren") {
          if (R.byId("anagrp")) R.byId("anagrp").value = item.Ana_Grup || "";
          await R.anagrpChanged(R.byId("anagrp"));
          if (R.byId("altgrp")) R.byId("altgrp").value = item.Alt_Grup || "";
          if (R.byId("aciklama")) R.byId("aciklama").value = data.aciklama || "";
          if (R.byId("ukodu")) R.byId("ukodu").value = item.Kodu || "";
          if (R.byId("durum")) R.byId("durum").value = item.Durum ? "A" : "P";
          break;
        }
      }

      // ürün detaylarını doldur
      await R.updateurunValues(R.byId("ukodu"));

      if (errorDiv) {
        errorDiv.style.display = "none";
        errorDiv.innerText = "";
      }
    } catch (error) {
      R.errShow(error?.message);
    } finally {
      R.cursor("default");
    }
  };

  /* =========================
     CLEAR
     ========================= */

  R.clearInputs = function () {
    if (R.byId("anagrp")) R.byId("anagrp").value = "";
    const altgrp = R.byId("altgrp");
    if (altgrp) {
      altgrp.innerHTML = "";
      altgrp.disabled = true;
    }
    if (R.byId("aciklama")) R.byId("aciklama").value = "";

    if (R.byId("ukodu")) R.byId("ukodu").value = "";
    if (R.byId("agirlik")) R.byId("agirlik").innerText = "0.000";
    if (R.byId("anagrpl")) R.byId("anagrpl").innerText = "";
    if (R.byId("altgrpl")) R.byId("altgrpl").innerText = "";
    if (R.byId("barkod"))  R.byId("barkod").innerText = "";
    if (R.byId("birim"))   R.byId("birim").innerText = "";
    if (R.byId("adi"))     R.byId("adi").innerText = "";

    // tbody temizle (senin kod: id="tbody")
    const tableBody = R.byId("tbody");
    if (tableBody) tableBody.innerHTML = "";

    R.rowCounter = 0;
    R.initializeRows();
  };

  /* =========================
     ALT GRUP (aynı)
     ========================= */

  R.anagrpChanged = async function (anagrpElement) {
    const anagrup = anagrpElement?.value || "";
    const selectElement = R.byId("altgrp");
    if (!selectElement) return;

    selectElement.innerHTML = "";

    if (anagrup === "") {
      selectElement.disabled = true;
      return;
    }

    R.cursor("wait");
    R.errClear();

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
      R.errShow(error?.message);
    } finally {
      R.cursor("default");
    }
  };

  /* =========================
     YOKET
     ========================= */

  R.recYoket = async function () {
    const recNoInput = R.byId("recno");
    const girenurunkodEl = R.byId("ukodu");

    if (!recNoInput || ["0", ""].includes(recNoInput.value)) return;

    const ok = confirm("Bu recete silinecek ?");
    if (!ok) return;

    R.disableBtn("recsilButton", true, "Siliniyor...", "Sil");
    R.cursor("wait");
    R.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/recYoket", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
          recno: recNoInput.value,
          kodu: (girenurunkodEl?.value || "")
        }),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      R.clearInputs();
      recNoInput.value = "";
      R.errClear();
    } catch (error) {
      R.errShow(error?.message);
    } finally {
      R.cursor("default");
      R.disableBtn("recsilButton", false, "Siliniyor...", "Sil");
    }
  };

  /* =========================
     KAYIT DTO
     ========================= */

  R.prepareureKayit = function () {
    const receteDTO = {
      recno: R.byId("recno")?.value || "",
      anagrup: R.byId("anagrp")?.value || "",
      altgrup: R.byId("altgrp")?.value || "",
      aciklama: R.byId("aciklama")?.value || "",
      girenurkodu: R.byId("ukodu")?.value || "",
      durum: R.byId("durum")?.value || "",
    };

    const tableData = R.getTableData();
    return { receteDTO, tableData };
  };

  R.getTableData = function () {
    const table = R.byId("recTable");
    const rows = table?.querySelectorAll("tbody tr") || [];
    const data = [];

    rows.forEach((row) => {
      const cells = row.querySelectorAll("td");
      const ukodu = cells[2]?.querySelector("input")?.value || "";

      if (ukodu.trim()) {
        data.push({
          ukodu,
          miktar: parseLocaleNumber(cells[5]?.querySelector("input")?.value),
        });
      }
    });

    return data;
  };

  /* =========================
     KAYIT
     ========================= */

  R.recKayit = async function () {
    const recno = R.byId("recno")?.value || "";

    const table = R.byId("recTable");
    const rowsLen = table?.rows?.length || 0;

    if (!recno || recno === "0" || rowsLen === 0) {
      alert("Geçerli bir evrak numarası giriniz.");
      return;
    }

    const recetekayitDTO = R.prepareureKayit();

    R.disableBtn("reckaydetButton", true, "İşleniyor...", "Kaydet");
    R.cursor("wait");
    R.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/recKayit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(recetekayitDTO),
      });

      if ((response?.errorMessage || "").trim() !== "") {
        throw new Error(response.errorMessage);
      }

      R.clearInputs();
      if (R.byId("recno")) R.byId("recno").value = "";
      R.errClear();
    } catch (error) {
      R.errShow(error?.message);
    } finally {
      R.cursor("default");
      R.disableBtn("reckaydetButton", false, "İşleniyor...", "Kaydet");
    }
  };

})();
