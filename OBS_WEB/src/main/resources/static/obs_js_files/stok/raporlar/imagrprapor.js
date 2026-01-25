/* =========================
   OBS Namespace
   ========================= */
window.OBS = window.OBS || {};
OBS.IMAGRUP = OBS.IMAGRUP || {};

(() => {
  const G = OBS.IMAGRUP;

  /* ---------- state ---------- */
  G.tablobaslik = [];
  G.rowCounter = 0; // sabit kolon sayısı
  G.data = null;

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

  /* =========================
     ANA GRUP -> ALT GRUP (parametreli)
     ========================= */
  G.anagrpChanged = async function (anagrpElement, altgrpElementId) {
    const anagrup = anagrpElement?.value || "";
    const selectElement = G.byId(altgrpElementId);
    if (!selectElement) return;

    selectElement.innerHTML = "";

    if (anagrup === "") {
      selectElement.disabled = true;
      return;
    }

    G.cursor("wait");
    G.errClear();

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
      G.errShow(error?.message);
    } finally {
      G.cursor("default");
    }
  };

  /* =========================
     MODAL OPEN (jQuery yok)
     endpoint: stok/ana
     doldur: #uranagrp, #anagrp
     ========================= */
  G.openimagrpModal = async function (modalRef) {
    const modalEl =
      (typeof modalRef === "string")
        ? (modalRef.startsWith("#") ? document.querySelector(modalRef) : G.byId(modalRef))
        : modalRef;

    if (!modalEl) return;

    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    modal.show();

    G.cursor("wait");
    G.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/ana", {
        method: "POST",
        headers: { "Content-Type": "application/json" }
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const ana = response.anaKodlari || [];

      const uranaSelect = G.byId("uranagrp");
      const anaSelect = G.byId("anagrp");

      const fillAna = (sel) => {
        if (!sel) return;
        sel.innerHTML = "";

        // başa boş + Bos Olanlar
        sel.add(new Option("", ""));
        sel.add(new Option("Bos Olanlar", "Bos Olanlar"));

        const seen = new Set();
        for (const it of ana) {
          const v = (it.ANA_GRUP || "").trim();
          if (!v || seen.has(v)) continue;
          seen.add(v);
          sel.add(new Option(v, v));
        }
      };

      fillAna(anaSelect);
      fillAna(uranaSelect);

    } catch (error) {
      G.errShow(`Bir hata oluştu: ${error.message}`);
    } finally {
      G.cursor("default");
    }
  };

  /* =========================
     DTO (jQuery yok) -> hidden input #imagrpBilgi
     ========================= */
  G.getGrupraporDTO = function () {
    const hf = G.byId("imagrpBilgi");
    const v = hf ? (hf.value || "") : "";
    const p = v.split(",");

    return {
      ukod1: p[0],
      ukod2: p[1],
      tar1: p[2],
      tar2: p[3],
      sinif1: p[4],
      sinif2: p[5],
      uranagrp: p[6],
      uraltgrp: p[7],
      birim: p[8],
      gruplama: p[9],
      stunlar: p[10],
      turu: p[11],
      anagrp: p[12],
      altgrp: p[13],
    };
  };

  /* =========================
     HEADERS
     ========================= */
  G.parseHeaders = (baslikString) =>
    (baslikString || "")
      .split(",")
      .map(h => h.trim().replace(/\[|\]/g, ""))
      .filter(h => h !== "");

  G.updateTableHeaders = function (baslikString, kolonbaslangic) {
    const thead = document.querySelector("#main-table thead");
    if (!thead) return;

    thead.innerHTML = "";
    const trHead = document.createElement("tr");
    trHead.classList.add("thead-dark");

    const headers = G.parseHeaders(baslikString);

    headers.forEach((header, index) => {
      const th = document.createElement("th");
      th.textContent = header;
      if (index >= kolonbaslangic) th.classList.add("double-column");
      trHead.appendChild(th);
    });

    thead.appendChild(trHead);
  };

  /* =========================
     TABLE (tbody + tfoot) ve kolon toplamları
     ========================= */
  G.updateTable = function (data, headers, format, kolonbaslangic) {
    const tbody = document.querySelector("#main-table tbody");
    if (!tbody) return;

    let tfoot = document.querySelector("#main-table tfoot");
    tbody.innerHTML = "";

    if (!tfoot) {
      tfoot = document.createElement("tfoot");
      document.querySelector("#main-table").appendChild(tfoot);
    }
    tfoot.innerHTML = "";

    const kolonToplamlari = new Array(headers.length).fill(0);

    (data || []).forEach((rowData) => {
      const tr = document.createElement("tr");
      tr.classList.add("table-row-height");

      headers.forEach((header, index) => {
        const td = document.createElement("td");
        const raw = (rowData && rowData[header] != null) ? rowData[header] : "";
        const cellValue = (raw == null) ? "" : String(raw);

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

    // footer
    const footerRow = document.createElement("tr");
    footerRow.classList.add("table-footer");

    headers.forEach((_, index) => {
      const th = document.createElement("th");
      if (index >= kolonbaslangic) {
        th.textContent = (format == 2)
          ? formatNumber2(kolonToplamlari[index])
          : formatNumber3(kolonToplamlari[index]);
        th.classList.add("double-column");
      } else {
        th.textContent = "";
      }
      footerRow.appendChild(th);
    });

    tfoot.appendChild(footerRow);
  };

  /* =========================
     CLEAR TFOOT (th)
     ========================= */
  G.clearTfoot = function () {
    const table = document.querySelector("#main-table");
    const tfoot = table?.querySelector("tfoot");
    if (!tfoot) return;
    tfoot.querySelectorAll("th").forEach(th => th.textContent = "");
  };

  /* =========================
     FETCH + TABLE DOLDUR
     ========================= */
  G.imagrpfetchTableData = async function () {
    const dto = G.getGrupraporDTO();

    G.errClear();
    G.cursor("wait");
    G.disableBtn("imagrpyenileButton", true, "İşleniyor...", "Yenile");

    const mainTableBody = G.byId("mainTableBody"); // sende bunu boşaltıyorsun, tbody zaten #main-table tbody ama kalsın
    if (mainTableBody) mainTableBody.innerHTML = "";

    G.clearTfoot();

    try {
      const response = await fetchWithSessionCheck("stok/imagrpdoldur", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      G.data = response;

      // header bas + state
      G.updateTableHeaders(response.baslik, response.sabitkolonsayisi);

      const headers = G.parseHeaders(response.baslik);
      G.tablobaslik = headers;
      G.rowCounter = response.sabitkolonsayisi;

      // tablo bas
      G.updateTable(response.data, headers, response.format, response.sabitkolonsayisi);

    } catch (error) {
      G.errShow(error?.message || String(error));
    } finally {
      G.disableBtn("imagrpyenileButton", false, "İşleniyor...", "Yenile");
      G.cursor("default");
    }
  };

  /* =========================
     TABLO STRING (download için)  "||" + "\n" formatı
     ========================= */
  G.extractTableDataToString = function (headers) {
    let rowsString = "";

    const tbody = document.querySelector("#main-table tbody");
    const tbodyRows = tbody ? tbody.querySelectorAll("tr") : [];

    tbodyRows.forEach(tr => {
      let rowString = "";
      const tds = Array.from(tr.querySelectorAll("td"));

      headers.forEach((_, index) => {
        const td = tds[index];
        const cellValue = td ? td.innerText.trim() : "";
        rowString += cellValue + "||";
      });

      rowsString += rowString.slice(0, -2) + "\n";
    });

    const footer = document.querySelector("#main-table tfoot");
    if (footer) {
      let footerString = "";
      const ths = Array.from(footer.querySelectorAll("th"));

      headers.forEach((_, index) => {
        const th = ths[index];
        const footerValue = th ? th.innerText.trim() : "";
        footerString += footerValue + "||";
      });

      rowsString += footerString.slice(0, -2) + "\n";
    }

    return rowsString;
  };

  /* =========================
     DOWNLOAD (jQuery yok)
     ========================= */
  G.imagrpdownloadReport = async function () {
    G.errClear();
    G.cursor("wait");

    G.disableBtn("imagrprapreportDownload", true, "İşleniyor...", "Rapor İndir");
    const yenile = G.byId("imagrpyenileButton");
    if (yenile) yenile.disabled = true;

    try {
      const tableString = G.extractTableDataToString(G.tablobaslik);

      const response = await fetchWithSessionCheckForDownload("stok/imagrp_download", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          headers: G.tablobaslik,
          data: tableString,
          sabitkolon: G.rowCounter
        }),
      });

      if (response?.blob) {
        const disposition = response.headers.get("Content-Disposition");
        const fileName = disposition?.match(/filename="(.+)"/)?.[1] || "imagrup.xlsx";

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
      G.disableBtn("imagrprapreportDownload", false, "İşleniyor...", "Rapor İndir");
      if (yenile) yenile.disabled = false;
      G.cursor("default");
    }
  };

  /* =========================
     MAIL (jQuery yok)
     ========================= */
  
  G.imagrpmailAt = function () {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    G.cursor("wait");

    const rowsString = G.extractTableDataToString(G.tablobaslik);

    // sen eski sistemde "grprapor" içine string basıyordun
    localStorage.setItem("grprapor", rowsString);
    localStorage.setItem("tablobaslik", JSON.stringify(G.tablobaslik));

    const degerler = `${G.rowCounter},imagruprapor`;
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);

    G.cursor("default");
  };

})();
