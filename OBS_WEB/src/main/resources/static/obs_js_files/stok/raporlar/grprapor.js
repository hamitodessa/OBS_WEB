/* =========================
   OBS Namespace
   ========================= */
window.OBS = window.OBS || {};
OBS.GRPRAP = OBS.GRPRAP || {};

(() => {
  const G = OBS.GRPRAP;

  /* ---------- helpers ---------- */
  G.byId = (id) => document.getElementById(id);

  G.errClear = () => {
    const e = G.byId("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
  };

  G.errShow = (msg) => {
    const e = G.byId("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Beklenmeyen bir hata oluştu.";
  };

  G.cursor = (c) => { document.body.style.cursor = c || "default"; };

  G.disableBtn = (id, yes, textYes, textNo) => {
    const b = G.byId(id);
    if (!b) return;
    b.disabled = !!yes;
    if (yes && textYes != null) b.innerText = textYes;
    if (!yes && textNo != null) b.innerText = textNo;
  };

  /* =========================================================
     ANA GRUP -> ALT GRUP
     ========================================================= */
  G.anagrpChanged = async function (anagrpElement, altgrpElementId) {
    const anagrup = anagrpElement?.value || "";
    const errorDiv = G.byId("errorDiv");
    const selectElement = G.byId(altgrpElementId);
    if (!selectElement) return;

    selectElement.innerHTML = "";

    if (anagrup === "") {
      selectElement.disabled = true;
      return;
    }

    G.cursor("wait");
    if (errorDiv) { errorDiv.style.display = "none"; errorDiv.innerText = ""; }

    try {
      const response = await fetchWithSessionCheck("stok/altgrup", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ anagrup }),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      (response.altKodlari || []).forEach(kod => {
        const opt = document.createElement("option");
        opt.value = kod.ALT_GRUP;
        opt.textContent = kod.ALT_GRUP;
        selectElement.appendChild(opt);
      });

      selectElement.disabled = selectElement.options.length === 0;
    } catch (error) {
      selectElement.disabled = true;
      G.errShow(error?.message);
    } finally {
      G.cursor("default");
    }
  };

  /* =========================================================
     MODAL OPEN (jQuery YOK)
     endpoint: stok/anaoz1
     doldur: #uranagrp, #urozkod
     + Bos Olanlar
     + birimChanged() çağır
     ========================================================= */
  G.opengrpModal = async function (modalRef) {
    const modalEl =
      (typeof modalRef === "string")
        ? (modalRef.startsWith("#") ? document.querySelector(modalRef) : G.byId(modalRef))
        : modalRef;

    if (!modalEl) return;

    bootstrap.Modal.getOrCreateInstance(modalEl).show();

    G.cursor("wait");
    G.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/anaoz1", {
        method: "POST",
        headers: { "Content-Type": "application/json" }
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const ana = response.anaKodlari || [];
      const oz1 = response.ozKodlari || [];

      const uranaSelect = G.byId("uranagrp");
      const ozSelect = G.byId("urozkod");
      if (uranaSelect) uranaSelect.innerHTML = "";
      if (ozSelect) ozSelect.innerHTML = "";

      const addBos = (sel) => {
        if (!sel) return;
        sel.add(new Option("", ""));
        sel.add(new Option("Bos Olanlar", "Bos Olanlar"));
      };

      if (uranaSelect) {
        addBos(uranaSelect);
        const seen = new Set();
        for (const it of ana) {
          const v = (it.ANA_GRUP || "").trim();
          if (!v || seen.has(v)) continue;
          seen.add(v);
          uranaSelect.add(new Option(v, v));
        }
      }

      if (ozSelect) {
        addBos(ozSelect);
        const seen = new Set();
        for (const it of oz1) {
          const v = (it.OZEL_KOD_1 || "").trim();
          if (!v || seen.has(v)) continue;
          seen.add(v);
          ozSelect.add(new Option(v, v));
        }
      }

      // senin akış: modal açılınca birim değişince gizle/göster
      if (typeof G.birimChanged === "function") G.birimChanged();
      else if (typeof window.birimChanged === "function") window.birimChanged();

    } catch (error) {
      G.errShow(`Bir hata oluştu: ${error.message}`);
    } finally {
      G.cursor("default");
    }
  };

  /* =========================================================
     birimChanged / dvzcevirChanged / istenenayChanged
     (aynı davranış, sadece namespace'e aldım)
     ========================================================= */
  G.birimChanged = function () {
    const birim = G.byId("birim")?.value;

    const dvzLbl = G.byId("dvzcevlbl");
    const dvzChc = G.byId("dvzcevirchc");
    const dvzSpn = G.byId("dvzcvrspn");

    const dvzcins = G.byId("dvzcins");
    const dvzturu = G.byId("dvzturu");
    const dvzcinsspan = G.byId("dvzcinsspan");
    const dvzturuspan = G.byId("dvzturuspan");

    if (birim === "Tutar") {
      dvzChc?.classList.toggle("is-hidden", false);
      dvzSpn?.classList.toggle("is-hidden", false);
      dvzLbl?.classList.toggle("is-hidden", false);
    } else {
      if (dvzChc) dvzChc.checked = false;

      dvzLbl?.classList.toggle("is-hidden", true);
      dvzChc?.classList.toggle("is-hidden", true);
      dvzSpn?.classList.toggle("is-hidden", true);

      dvzcins?.classList.toggle("is-hidden", true);
      dvzturu?.classList.toggle("is-hidden", true);
      dvzcinsspan?.classList.toggle("is-hidden", true);
      dvzturuspan?.classList.toggle("is-hidden", true);
    }
  };

  G.dvzcevirChanged = function () {
    const checked = !!G.byId("dvzcevirchc")?.checked;

    G.byId("dvzcins")?.classList.toggle("is-hidden", !checked);
    G.byId("dvzturu")?.classList.toggle("is-hidden", !checked);
    G.byId("dvzcinsspan")?.classList.toggle("is-hidden", !checked);
    G.byId("dvzturuspan")?.classList.toggle("is-hidden", !checked);
  };

  G.istenenayChanged = function () {
    const checked = !!G.byId("istenenaychc")?.checked;
    G.byId("istenenay")?.classList.toggle("is-hidden", !checked);
  };

  /* =========================================================
     DTO (hidden input #grpBilgi) — jQuery yok
     ========================================================= */
  G.getDTO = function () {
    const hf = G.byId("grpBilgi");
    const v = hf ? (hf.value || "") : "";
    const p = v.split(",");

    return {
      tar1: p[0],
      tar2: p[1],
      uranagrp: p[2],
      ukod1: p[3],
      ukod2: p[4],
      uraltgrp: p[5],
      ckod1: p[6],
      ckod2: p[7],
      urozkod: p[8],
      birim: p[9],
      istenenay: p[10],
      gruplama: p[11],
      dvzcevirchc: p[12],
      doviz: p[13],
      stunlar: p[14],
      dvzturu: p[15],
      turu: p[16],
      istenenaychc: p[17],
      sinif1: p[18],
      sinif2: p[19],
    };
  };

  /* =========================================================
     TABLE HEADERS (dinamik)
     ========================================================= */
  G.updateTableHeaders = function (baslikString, kolonbaslangic) {
    const thead = document.querySelector("#main-table thead");
    if (!thead) return;

    thead.innerHTML = "";
    const trHead = document.createElement("tr");
    trHead.classList.add("thead-dark");

    const headers = (baslikString || "")
      .split(",")
      .map(h => h.trim().replace(/\[|\]/g, ""));

    headers.forEach((h, i) => {
      const th = document.createElement("th");
      th.textContent = h;
      if (i >= kolonbaslangic) th.classList.add("double-column");
      trHead.appendChild(th);
    });

    thead.appendChild(trHead);
  };

  /* =========================================================
     TABLE (dinamik data + tfoot toplam)
     format: 2 => formatNumber2, else formatNumber3
     ========================================================= */
  G.updateTable = function (data, headers, format, kolonbaslangic) {
    const tbody = document.querySelector("#main-table tbody");
    const table = document.querySelector("#main-table");
    if (!tbody || !table) return;

    let tfoot = table.querySelector("tfoot");
    tbody.innerHTML = "";

    if (!tfoot) {
      tfoot = document.createElement("tfoot");
      table.appendChild(tfoot);
    }
    tfoot.innerHTML = "";

    const kolonToplamlari = new Array(headers.length).fill(0);

    (data || []).forEach(rowData => {
      const tr = document.createElement("tr");
      tr.classList.add("table-row-height");

      headers.forEach((header, index) => {
        const td = document.createElement("td");
        const raw = (rowData && rowData[header] != null) ? rowData[header] : "";
        const cellValue = (raw === null) ? "" : raw;

        if (index >= kolonbaslangic) {
          const numericValue = parseFloat(cellValue);
          if (!isNaN(numericValue)) {
            td.textContent = (format == 2) ? formatNumber2(numericValue) : formatNumber3(numericValue);
            td.classList.add("double-column");
            kolonToplamlari[index] += numericValue;
          } else {
            td.textContent = cellValue;
          }
        } else {
          td.textContent = cellValue;
        }

        tr.appendChild(td);
      });

      tbody.appendChild(tr);
    });

    const footerRow = document.createElement("tr");
    footerRow.classList.add("table-footer");

    headers.forEach((_, index) => {
      const th = document.createElement("th");
      if (index >= kolonbaslangic) {
        th.textContent = (format == 2) ? formatNumber2(kolonToplamlari[index]) : formatNumber3(kolonToplamlari[index]);
        th.classList.add("double-column");
      } else {
        th.textContent = "";
      }
      footerRow.appendChild(th);
    });

    tfoot.appendChild(footerRow);
  };

  /* =========================================================
     CLEAR TFOOT
     ========================================================= */
  G.clearTfoot = function () {
    const table = document.querySelector("#main-table");
    const tfoot = table?.querySelector("tfoot");
    if (!tfoot) return;
    tfoot.querySelectorAll("th").forEach(th => th.textContent = "");
  };

  /* =========================================================
     FETCH (stok/grpdoldur)
     global değişkenler: data, tablobaslik, rowCounter
     ========================================================= */
  G.grpfetchTableData = async function () {
    const dto = G.getDTO();

    G.errClear();
    G.cursor("wait");
    G.disableBtn("grpyenileButton", true, "İşleniyor...", "Yenile");

    const mainTableBody = G.byId("mainTableBody");
    if (mainTableBody) mainTableBody.innerHTML = "";
    G.clearTfoot();

    try {
      const response = await fetchWithSessionCheck("stok/grpdoldur", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      // senin eski akış: global data/tablobaslik/rowCounter
      window.data = response;

      G.updateTableHeaders(response.baslik, response.sabitkolonsayisi);

      const headers = (response.baslik || "")
        .split(",")
        .map(h => h.trim().replace(/\[|\]/g, ""));

      window.tablobaslik = headers;
      window.rowCounter = response.sabitkolonsayisi;

      G.updateTable(response.data, headers, response.format, response.sabitkolonsayisi);

    } catch (error) {
      G.errShow(error?.message || String(error));
    } finally {
      G.disableBtn("grpyenileButton", false, "İşleniyor...", "Yenile");
      G.cursor("default");
    }
  };

  /* =========================================================
     extractTableData(headers) -> string (senin format)
     ========================================================= */
  G.extractTableData = function (headers) {
    let rowsString = "";

    const tbody = document.querySelector("#main-table tbody");
    if (!tbody) return rowsString;

    const tbodyRows = tbody.querySelectorAll("tr");
    tbodyRows.forEach(tr => {
      let rowString = "";
      const tds = Array.from(tr.querySelectorAll("td"));

      headers.forEach((_, index) => {
        const td = tds[index];
        const v = td ? td.innerText.trim() : "";
        rowString += v + "||";
      });

      rowsString += rowString.slice(0, -2) + "\n";
    });

    const footer = document.querySelector("#main-table tfoot");
    if (footer) {
      let footerString = "";
      const ths = Array.from(footer.querySelectorAll("th"));

      headers.forEach((_, index) => {
        const th = ths[index];
        const v = th ? th.innerText.trim() : "";
        footerString += v + "||";
      });

      rowsString += footerString.slice(0, -2) + "\n";
    }

    return rowsString;
  };

  /* =========================================================
     DOWNLOAD
     ========================================================= */
  G.grpdownloadReport = async function () {
    G.errClear();
    G.cursor("wait");

    G.disableBtn("grpDownloadButton", true, "İşleniyor...", "Rapor İndir");
    const yenile = G.byId("grpyenileButton");
    if (yenile) yenile.disabled = true;

    try {
      const headers = window.tablobaslik || [];
      const tableString = G.extractTableData(headers);

      const response = await fetchWithSessionCheckForDownload("stok/grp_download", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ headers, data: tableString, sabitkolon: window.rowCounter }),
      });

      if (response?.blob) {
        const disposition = response.headers.get("Content-Disposition");
        const fileName = disposition?.match(/filename="(.+)"/)?.[1] || "gruprapor.xlsx";

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
      G.errShow(error?.message);
    } finally {
      G.disableBtn("grpDownloadButton", false, "İşleniyor...", "Rapor İndir");
      if (yenile) yenile.disabled = false;
      G.cursor("default");
    }
  };

  /* =========================================================
     MAIL
     ========================================================= */
  G.grpmailAt = function () {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    G.cursor("wait");

    const headers = window.tablobaslik || [];
    const rows = G.extractTableData(headers);

    localStorage.setItem("grprapor", rows);
    localStorage.setItem("tablobaslik", JSON.stringify(headers));

    const degerler = (window.rowCounter || 0) + "," + "gruprapor";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);

    G.cursor("default");
  };

})();
