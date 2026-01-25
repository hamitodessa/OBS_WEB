/* =========================
   OBS Namespace
   ========================= */
window.OBS = window.OBS || {};
OBS.ENVANTER = OBS.ENVANTER || {};

(() => {
  const E = OBS.ENVANTER;

  /* ---------- state ---------- */
  E.currentPage = 0;
  E.totalPages  = 0;
  E.pageSize    = 250;

  /* ---------- helpers ---------- */
  E.byId = (id) => document.getElementById(id);

  E.errClear = () => {
    const e = E.byId("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
  };

  E.errShow = (msg) => {
    const e = E.byId("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Beklenmeyen bir hata oluştu.";
  };

  E.cursor = (c) => { document.body.style.cursor = c || "default"; };

  E.disableBtn = (id, yes, textYes, textNo) => {
    const b = E.byId(id);
    if (!b) return;
    b.disabled = !!yes;
    if (yes && textYes != null) b.innerText = textYes;
    if (!yes && textNo != null) b.innerText = textNo;
  };

  /* =========================
     ANA GRUP -> ALT GRUP (parametreli)
     usage:
       onchange="OBS.ENVANTER.anagrpChanged(this,'altgrp')"
       onchange="OBS.ENVANTER.anagrpChanged(this,'uraltgrp')"
     ========================= */
  E.anagrpChanged = async function (anagrpElement, altgrpElementId) {
    const anagrup = anagrpElement?.value || "";
    const selectElement = E.byId(altgrpElementId);

    if (!selectElement) return;

    selectElement.innerHTML = "";

    if (anagrup === "") {
      selectElement.disabled = true;
      return;
    }

    E.cursor("wait");
    E.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/altgrup", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ anagrup }),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      (response.altKodlari || []).forEach((kod) => {
        const opt = document.createElement("option");
        opt.value = kod.ALT_GRUP;
        opt.textContent = kod.ALT_GRUP;
        selectElement.appendChild(opt);
      });

      selectElement.disabled = selectElement.options.length === 0;
    } catch (error) {
      selectElement.disabled = true;
      E.errShow(error?.message);
    } finally {
      E.cursor("default");
    }
  };

  /* =========================
     MODAL OPEN (jQuery yok)
     ========================= */
  E.openenvanterModal = async function (modalRef) {
    const modalEl =
      (typeof modalRef === "string")
        ? (modalRef.startsWith("#") ? document.querySelector(modalRef) : E.byId(modalRef))
        : modalRef;

    if (!modalEl) return;

    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    modal.show();

    E.cursor("wait");
    E.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/anadepo", {
        method: "POST",
        headers: { "Content-Type": "application/json" }
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const ana = response.anaKodlari || [];
      const dpo = response.depoKodlari || [];

      const uranaSelect = E.byId("uranagrp");
      const anaSelect   = E.byId("anagrp");
      const dpoSelect   = E.byId("depo");

      if (uranaSelect) uranaSelect.innerHTML = "";
      if (anaSelect)   anaSelect.innerHTML = "";
      if (dpoSelect)   dpoSelect.innerHTML = "";

      // ortak doldurucu (unique)
      const fillSelect = (sel, arr, key) => {
        if (!sel) return;
        sel.add(new Option("", ""));
        sel.add(new Option("Bos Olanlar", "Bos Olanlar"));

        const seen = new Set();
        for (const it of arr) {
          const v = (it[key] || "").trim();
          if (!v || seen.has(v)) continue;
          seen.add(v);
          sel.add(new Option(v, v));
        }
      };

      fillSelect(anaSelect,   ana, "ANA_GRUP");
      fillSelect(uranaSelect, ana, "ANA_GRUP");
      fillSelect(dpoSelect,   dpo, "DEPO");

    } catch (error) {
      E.errShow(`Bir hata oluştu: ${error.message}`);
    } finally {
      E.cursor("default");
    }
  };

  /* =========================
     2. TABLO GÖSTER/GİZLE
     ========================= */
  E.setSecondTableVisible = function (isVisible) {
    const container = document.querySelector("#ara_content .container");
    const secondWrap = E.byId("second-table-container");
    if (!container || !secondWrap) return;

    if (isVisible) {
      container.classList.add("two-tables");
      secondWrap.style.display = "block";
    } else {
      container.classList.remove("two-tables");
      secondWrap.style.display = "none";
      const sb = E.byId("secondTableBody");
      if (sb) sb.innerHTML = "";
    }
  };

  /* =========================
     DTO (jQuery yok)
     ========================= */
  E.getenvanterDTO = function () {
    const hf = E.byId("envanterBilgi");
    const hiddenFieldValue = hf ? (hf.value || "") : "";
    const p = hiddenFieldValue.split(",");

    return {
      tar1: p[0],
      tar2: p[1],
      uranagrp: p[2],
      ukod1: p[3],
      ukod2: p[4],
      uraltgrp: p[5],
      evrno1: p[6],
      evrno2: p[7],
      anagrp: p[8],
      gruplama: p[9],
      altgrp: p[10],
      depo: p[11],
      fiatlama: p[12],
      depohardahil: p[13],
      uretfisdahil: p[14],
    };
  };

  /* =========================
     MAIN FETCH
     ========================= */
  E.envfetchTableData = async function () {
    const dto = E.getenvanterDTO();

    E.cursor("wait");
    E.errClear();

    E.disableBtn("envyenileButton", true, "İşleniyor...", "Yenile");

    const mainTableBody = E.byId("mainTableBody");
    if (mainTableBody) mainTableBody.innerHTML = "";

    const secondTableBody = E.byId("secondTableBody");
    if (secondTableBody) secondTableBody.innerHTML = "";

    E.clearTfoot();

    try {
      const response = await fetchWithSessionCheck("stok/envanterdoldur", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      // raporturu: normal | fifo
      const raporturu = response.raporturu;

      // headerlar
      if (raporturu === "normal") {
        const h1 = ["KODU", "ADI", "SIMGE", "GIRIS MIKTARI", "GIRIS TUTARI", "CIKIS MIKTARI", "CIKIS TUTARI", "CIKIS MALIYET", "STOK MIKTARI", "MALIYET", "TUTAR"];
        E.updateTableHeadersnormal(h1);
      } else {
        // fifo: mail & download disable
        const mailButton = E.byId("envmailButton");
        const reportFormat = E.byId("envDownloadButton");
        if (mailButton) mailButton.disabled = true;
        if (reportFormat) reportFormat.disabled = true;

        const h1 = ["KODU", "ADI", "SIMGE", "GIRIS MIKTARI", "GIRIS TUTARI", "CIKIS MIKTARI", "CIKIS TUTARI", "CIKIS MALIYET", "STOK MIKTARI", "MALIYET", "TUTAR"];
        E.updateTableHeadersfifofirst(h1);

        const h2 = ["URUN KODU", "EVRAK NO", "HES_KODU", "EVR CINS", "TARIH", "MIKTAR", "BIRIM", "FIAT", "MIKTAR BAKIYE", "TUTAR", "DOVIZ", "_TUTAR", "TUTAR BAKIYE", "USER"];
        E.updateTableHeadersfifo(h2);
      }

      // totals (normal)
      let totalmiktar = 0;
      let totalcmiktar = 0;
      let totalgtutar = 0;
      let totaltutar = 0;
      let totalstok = 0;
      let totalctutar = 0;

      if (raporturu === "normal") {
        E.setSecondTableVisible(false);

        (response.data || []).forEach(rowData => {
          const row = document.createElement("tr");
          row.classList.add("expandable", "table-row-height");

          row.innerHTML = `
            <td>${rowData.Kodu || ""}</td>
            <td>${rowData.Adi || ""}</td>
            <td>${rowData.Simge || ""}</td>
            <td class="double-column">${rowData.Giris_Miktari}</td>
            <td class="double-column">${formatNumber2(rowData.Giris_Tutar)}</td>
            <td class="double-column">${rowData.Cikis_Miktari}</td>
            <td class="double-column">${formatNumber2(rowData.Cikis_Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Cikis_Maliyet)}</td>
            <td class="double-column">${rowData.Stok_Miktari}</td>
            <td class="double-column">${formatNumber2(rowData.Maliyet)}</td>
            <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
          `;

          totalmiktar += parseLocaleNumber(rowData.Giris_Miktari);
          totalcmiktar += parseLocaleNumber(rowData.Cikis_Miktari);
          totalstok   += parseLocaleNumber(rowData.Stok_Miktari);
          totaltutar  += (rowData.Tutar || 0);
          totalctutar += (rowData.Cikis_Tutar || 0);
          totalgtutar += (rowData.Giris_Tutar || 0);

          mainTableBody.appendChild(row);
        });

        // tfoot doldur
        const t3  = E.byId("toplam-3");
        const t4  = E.byId("toplam-4");
        const t5  = E.byId("toplam-5");
        const t6  = E.byId("toplam-6");
        const t8  = E.byId("toplam-8");
        const t10 = E.byId("toplam-10");

        if (t3)  t3.innerText  = formatNumber3(totalmiktar);
        if (t4)  t4.innerText  = formatNumber2(totalgtutar);
        if (t5)  t5.innerText  = formatNumber3(totalcmiktar);
        if (t6)  t6.innerText  = formatNumber3(totalctutar);
        if (t8)  t8.innerText  = formatNumber3(totalstok);
        if (t10) t10.innerText = formatNumber2(totaltutar);

      } else if (raporturu === "fifo") {
        E.setSecondTableVisible(true);

        // 1. tablo
        (response.fifo || []).forEach(rowData => {
          const row = document.createElement("tr");
          row.classList.add("expandable", "table-row-height");
          row.innerHTML = `
            <td>${rowData.Urun_Kodu || ""}</td>
            <td>${rowData.Adi || ""}</td>
            <td>${rowData.Simge || ""}</td>
            <td class="double-column">${formatNumber3(rowData.Giris_Miktari)}</td>
            <td class="double-column">${formatNumber2(rowData.Giris_Tutar)}</td>
            <td class="double-column">${formatNumber3(rowData.Cikis_Miktari)}</td>
            <td class="double-column">${formatNumber2(rowData.Cikis_Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Cikis_Maliyet)}</td>
            <td class="double-column">${formatNumber3(rowData.Stok_Miktari)}</td>
            <td class="double-column">${formatNumber2(rowData.Maliyet)}</td>
            <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
          `;
          mainTableBody.appendChild(row);
        });

        // 2. tablo (MIKTAR'ı araya insert mantığı korunarak)
        (response.fifo2 || []).forEach(rowData => {
          const row = document.createElement("tr");
          row.classList.add("expandable", "table-row-height");

          row.innerHTML = `
            <td>${rowData.Urun_Kodu || ""}</td>
            <td>${rowData.Evrak_No || ""}</td>
            <td>${rowData.Hes_Kodu || ""}</td>
            <td>${rowData.Evrak_Cins || ""}</td>
            <td>${formatDate(rowData.Tarih)}</td>
            <td class="double-column">${formatNumber2(rowData.Fiat)}</td>
            <td>${rowData.Birim || ""}</td>
            <td class="double-column">${formatNumber3(rowData.Miktar_Bakiye)}</td>
            <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
            <td>${rowData.Doviz || ""}</td>
            <td class="double-column">${formatNumber2(E.findTutarField(rowData) || 0)}</td>
            <td class="double-column">${formatNumber2(rowData.Tutar_Bakiye)}</td>
            <td>${rowData.USER || ""}</td>
          `;

          // MIKTAR hücresini (TARIH'ten sonra / FIAT'tan önce) sok
          const miktarCell = document.createElement("td");
          miktarCell.classList.add("double-column");
          miktarCell.textContent = formatNumber3(rowData.Miktar);

          if (rowData.Miktar < 0) {
            miktarCell.style.backgroundColor = "red";
            miktarCell.style.color = "white";
          }

          const fiatTd = row.querySelector("td:nth-child(6)"); // FIAT
          row.insertBefore(miktarCell, fiatTd);

          secondTableBody.appendChild(row);
        });
      }

    } catch (error) {
      E.errShow((error && error.message) ? error.message : String(error));
    } finally {
      E.disableBtn("envyenileButton", false, "İşleniyor...", "Yenile");
      E.cursor("default");
    }
  };

  /* =========================
     _Tutar alanı bul
     ========================= */
  E.findTutarField = function (rowData) {
    for (const key in rowData) {
      if (key.includes("_Tutar")) return rowData[key];
    }
    return null;
  };

  /* =========================
     TFOOT temizle
     ========================= */
  E.clearTfoot = function () {
    const table = document.querySelector("#main-table");
    const tfoot = table?.querySelector("tfoot");
    if (!tfoot) return;
    tfoot.querySelectorAll("td").forEach(td => td.textContent = "");
  };

  /* =========================
     HEADERS - NORMAL (tfoot td)
     ========================= */
  E.updateTableHeadersnormal = function (headers) {
    const table = document.querySelector("#main-table");
    const thead = table?.querySelector("thead");
    if (!table || !thead) return;

    let tfoot = table.querySelector("tfoot");
    if (!tfoot) {
      tfoot = document.createElement("tfoot");
      table.appendChild(tfoot);
    }

    // THEAD
    thead.innerHTML = "";
    const trHead = document.createElement("tr");
    headers.forEach((header, index) => {
      const th = document.createElement("th");
      th.textContent = header;
      if (index >= headers.length - 8) th.classList.add("double-column");
      trHead.appendChild(th);
    });
    thead.appendChild(trHead);

    // TFOOT (TD)
    tfoot.innerHTML = "";
    const trFoot = document.createElement("tr");

    headers.forEach((_, index) => {
      const td = document.createElement("td");
      td.id = "toplam-" + index;

      if ([3, 5, 8].includes(index)) {
        td.textContent = "0.000";
        td.classList.add("double-column");
      } else if ([4, 6, 10].includes(index)) {
        td.textContent = "0.00";
        td.classList.add("double-column");
      } else {
        td.textContent = "";
      }

      trFoot.appendChild(td);
    });

    tfoot.appendChild(trFoot);
  };

  /* =========================
     HEADERS - FIFO FIRST TABLE
     ========================= */
  E.updateTableHeadersfifofirst = function (headers) {
    const table = document.querySelector("#main-table");
    const thead = table?.querySelector("thead");
    if (!table || !thead) return;

    let tfoot = table.querySelector("tfoot");
    if (!tfoot) {
      tfoot = document.createElement("tfoot");
      table.appendChild(tfoot);
    }

    thead.innerHTML = "";
    const trHead = document.createElement("tr");
    headers.forEach((header, index) => {
      const th = document.createElement("th");
      th.textContent = header;
      if (index >= headers.length - 8) th.classList.add("double-column");
      trHead.appendChild(th);
    });
    thead.appendChild(trHead);

    tfoot.innerHTML = "";
    const trFoot = document.createElement("tr");
    headers.forEach((_, index) => {
      const td = document.createElement("td");
      td.id = "toplam-" + index;
      td.textContent = "";
      trFoot.appendChild(td);
    });
    tfoot.appendChild(trFoot);
  };

  /* =========================
     HEADERS - FIFO SECOND TABLE
     ========================= */
  E.updateTableHeadersfifo = function (headers) {
    const table = document.querySelector("#second-table");
    const thead = table?.querySelector("thead");
    if (!table || !thead) return;

    let tfoot = table.querySelector("tfoot");
    if (!tfoot) {
      tfoot = document.createElement("tfoot");
      table.appendChild(tfoot);
    }

    thead.innerHTML = "";
    const trHead = document.createElement("tr");

    headers.forEach((header, index) => {
      const th = document.createElement("th");
      th.textContent = header;

      if ([5, 7, 8, 9, 11, 12].includes(index)) th.classList.add("double-column");
      trHead.appendChild(th);
    });

    thead.appendChild(trHead);

    tfoot.innerHTML = "";
    const trFoot = document.createElement("tr");

    headers.forEach((_, index) => {
      const td = document.createElement("td");
      td.id = "second-toplam-" + index;
      td.textContent = "";

      if ([5, 7, 8, 9, 11, 12].includes(index)) td.classList.add("double-column");
      trFoot.appendChild(td);
    });

    tfoot.appendChild(trFoot);
  };

  /* =========================
     DOWNLOAD (jQuery yok)
     ========================= */
  E.envdownloadReport = async function () {
    E.errClear();

    E.cursor("wait");
    E.disableBtn("envDownloadButton", true, "İşleniyor...", "Rapor İndir");

    const yenile = E.byId("envyenileButton");
    if (yenile) yenile.disabled = true;

    const rows = E.extractTableData("main-table");

    try {
      const response = await fetchWithSessionCheckForDownload("stok/env_download", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(rows),
      });

      if (response?.blob) {
        const disposition = response.headers.get("Content-Disposition");
        const fileName = disposition?.match(/filename="(.+)"/)?.[1] || "envanter.xlsx";

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
      E.errShow(error?.message);
    } finally {
      E.disableBtn("envDownloadButton", false, "İşleniyor...", "Rapor İndir");
      if (yenile) yenile.disabled = false;
      E.cursor("default");
    }
  };

  /* =========================
     MAIL
     ========================= */
  E.envmailAt = function () {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    E.cursor("wait");

    const rows = E.extractTableData("main-table");
    localStorage.setItem("tableData", JSON.stringify({ rows }));

    const degerler = "envanter";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);

    E.cursor("default");
  };

  /* =========================
     TABLE EXTRACT (tfoot td dahil)
     ========================= */
  E.extractTableData = function (tableId) {
    const table = document.querySelector(`#${tableId}`);
    if (!table) return [];

    const headers = [];
    const rows = [];

    table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));

    table.querySelectorAll("tbody tr").forEach(tr => {
      const rowData = {};
      let nonEmptyCount = 0;

      tr.querySelectorAll("td").forEach((td, index) => {
        const value = td.innerText.trim();
        if (value !== "") nonEmptyCount++;
        rowData[headers[index]] = value;
      });

      if (nonEmptyCount > 0) rows.push(rowData);
    });

    const tfoot = table.querySelector("tfoot");
    if (tfoot) {
      const tfootRowData = {};
      let nonEmptyCount = 0;

      tfoot.querySelectorAll("td").forEach((td, index) => {
        const value = td.innerText.trim();
        if (value !== "") nonEmptyCount++;
        tfootRowData[headers[index]] = value;
      });

      if (nonEmptyCount > 0) rows.push(tfootRowData);
    }

    return rows;
  };

  /* =========================
     FIATLAMA CHANGED
     ========================= */
  E.fiatlamaChanged = function () {
    const fiatlama = E.byId("fiatlama")?.value;
    const ukod2 = E.byId("ukod2");
    if (!ukod2) return;

    ukod2.style.visibility = (fiatlama === "agort") ? "visible" : "hidden";
  };

})();
