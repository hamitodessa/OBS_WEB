/* =========================
   OBS Namespace
   ========================= */
window.OBS = window.OBS || {};
OBS.STKRAP = OBS.STKRAP || {};

(() => {
  const S = OBS.STKRAP;

  /* ---------- helpers ---------- */
  S.byId = (id) => document.getElementById(id);

  S.errClear = () => {
    const e = S.byId("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
  };

  S.errShow = (msg) => {
    const e = S.byId("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Beklenmeyen bir hata oluştu.";
  };

  S.cursor = (c) => { document.body.style.cursor = c || "default"; };

  S.disableBtn = (id, yes, textYes, textNo) => {
    const b = S.byId(id);
    if (!b) return;
    b.disabled = !!yes;
    if (yes && textYes != null) b.innerText = textYes;
    if (!yes && textNo !=null) b.innerText = textNo;
  };

  /* =========================
     ANA GRUP -> ALT GRUP (parametreli)
     ========================= */
  S.anagrpChanged = async function (anagrpElement, altgrpElementId) {
    const anagrup = anagrpElement?.value || "";
    const selectElement = S.byId(altgrpElementId);
    if (!selectElement) return;

    selectElement.innerHTML = "";

    if (anagrup === "") {
      selectElement.disabled = true;
      return;
    }

    S.cursor("wait");
    S.errClear();

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
      S.errShow(error?.message);
    } finally {
      S.cursor("default");
    }
  };

  /* =========================
     MODAL OPEN (jQuery yok)
     endpoint: stok/anadepo
     doldur: #uranagrp, #anagrp, #depo
     ========================= */
  S.openstkModal = async function (modalRef) {
    const modalEl =
      (typeof modalRef === "string")
        ? (modalRef.startsWith("#") ? document.querySelector(modalRef) : S.byId(modalRef))
        : modalRef;

    if (!modalEl) return;

    bootstrap.Modal.getOrCreateInstance(modalEl).show();

    S.cursor("wait");
    S.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/anadepo", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const ana = response.anaKodlari || [];
      const dpo = response.depoKodlari || [];

      const uranaSelect = S.byId("uranagrp");
      const anaSelect = S.byId("anagrp");
      const dpoSelect = S.byId("depo");

      const fillAna = (sel) => {
        if (!sel) return;
        sel.innerHTML = "";
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

      const fillDepo = (sel) => {
        if (!sel) return;
        sel.innerHTML = "";
        sel.add(new Option("", ""));
        sel.add(new Option("Bos Olanlar", "Bos Olanlar"));

        const seen = new Set();
        for (const it of dpo) {
          const v = (it.DEPO || "").trim();
          if (!v || seen.has(v)) continue;
          seen.add(v);
          sel.add(new Option(v, v));
        }
      };

      fillAna(anaSelect);
      fillAna(uranaSelect);
      fillDepo(dpoSelect);

    } catch (error) {
      S.errShow(`Bir hata oluştu: ${error.message}`);
    } finally {
      S.cursor("default");
    }
  };

  /* =========================
     DTO (jQuery yok) -> hidden input #stokBilgi
     ========================= */
  S.getStokDTO = function () {
    const hf = S.byId("stokBilgi");
    const v = hf ? (hf.value || "") : "";
    const p = v.split(",");

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
      oncekitarih: p[12],
      depohardahil: p[13],
      uretfisdahil: p[14],
    };
  };

  /* =========================
     CLEAR TFOOT (th)
     ========================= */
  S.clearTfoot = function () {
    const table = document.querySelector("#main-table");
    const tfoot = table?.querySelector("tfoot");
    if (!tfoot) return;
    tfoot.querySelectorAll("th").forEach(th => th.textContent = "");
  };

  /* =========================
     HEADERS builders (3 tür)
     ========================= */
  S._ensureTfoot = function () {
    const table = document.querySelector("#main-table");
    if (!table) return null;
    let tfoot = table.querySelector("tfoot");
    if (!tfoot) {
      tfoot = document.createElement("tfoot");
      table.appendChild(tfoot);
    }
    return tfoot;
  };

  S.updateTableHeadersstokkodu = function (headers) {
    const table = document.querySelector("#main-table");
    const thead = table?.querySelector("thead");
    const tfoot = S._ensureTfoot();
    if (!thead || !tfoot) return;

    // THEAD
    thead.innerHTML = "";
    const trHead = document.createElement("tr");
    trHead.classList.add("thead-dark");

    headers.forEach((h, i) => {
      const th = document.createElement("th");
      th.textContent = h;
      if (i >= headers.length - 6) th.classList.add("double-column");
      trHead.appendChild(th);
    });
    thead.appendChild(trHead);

    // TFOOT
    tfoot.innerHTML = "";
    const trFoot = document.createElement("tr");

    headers.forEach((_, i) => {
      const th = document.createElement("th");
      th.id = "toplam-" + i;

      if ([3,4,5,6,7,8].includes(i)) {
        th.textContent = "0.000";
        th.classList.add("double-column");
      } else {
        th.textContent = "";
      }

      trFoot.appendChild(th);
    });

    tfoot.appendChild(trFoot);
  };

  S.updateTableHeadersstokkoduonceki = function (headers) {
    const table = document.querySelector("#main-table");
    const thead = table?.querySelector("thead");
    const tfoot = S._ensureTfoot();
    if (!thead || !tfoot) return;

    thead.innerHTML = "";
    const trHead = document.createElement("tr");
    trHead.classList.add("thead-dark");

    headers.forEach((h, i) => {
      const th = document.createElement("th");
      th.textContent = h;
      if (i >= headers.length - 5) th.classList.add("double-column");
      trHead.appendChild(th);
    });
    thead.appendChild(trHead);

    tfoot.innerHTML = "";
    const trFoot = document.createElement("tr");

    headers.forEach((_, i) => {
      const th = document.createElement("th");
      th.id = "toplam-" + i;

      if ([3,4,5,6,7].includes(i)) {
        th.textContent = "0.000";
        th.classList.add("double-column");
      } else {
        th.textContent = "";
      }

      trFoot.appendChild(th);
    });

    tfoot.appendChild(trFoot);
  };

  S.updateTableHeadersanaalt = function (headers) {
    const table = document.querySelector("#main-table");
    const thead = table?.querySelector("thead");
    const tfoot = S._ensureTfoot();
    if (!thead || !tfoot) return;

    thead.innerHTML = "";
    const trHead = document.createElement("tr");
    trHead.classList.add("thead-dark");

    headers.forEach((h, i) => {
      const th = document.createElement("th");
      th.textContent = h;
      if (i >= headers.length - 6) th.classList.add("double-column");
      trHead.appendChild(th);
    });
    thead.appendChild(trHead);

    tfoot.innerHTML = "";
    const trFoot = document.createElement("tr");

    headers.forEach((_, i) => {
      const th = document.createElement("th");
      th.id = "toplam-" + i;

      if ([2,3,4,5,6,7].includes(i)) {
        th.textContent = "0.000";
        th.classList.add("double-column");
      } else {
        th.textContent = "";
      }

      trFoot.appendChild(th);
    });

    tfoot.appendChild(trFoot);
  };

  /* =========================
     FETCH + TABLE DOLDUR
     ========================= */
  S.stokfetchTableData = async function () {
    const dto = S.getStokDTO();

    S.errClear();
    S.cursor("wait");
    S.disableBtn("stokyenileButton", true, "İşleniyor...", "Yenile");

    const mainTableBody = S.byId("mainTableBody");
    if (mainTableBody) mainTableBody.innerHTML = "";
    S.clearTfoot();

    try {
      const response = await fetchWithSessionCheck("stok/stokdoldur", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const raporturu = response.raporturu;
      let sqlHeaders = [];

      if (raporturu === "stokkodu") {
        sqlHeaders = ["KODU", "ADI", "SIMGE", "GIRIS MIKTARI", "GIRIS AGIRLIK", "CIKIS MIKTARI", "CIKIS AGIRLIK", "STOK MIKTARI", "STOK AGIRLIK"];
        S.updateTableHeadersstokkodu(sqlHeaders);
      } else if (raporturu === "stokkoduonceki") {
        sqlHeaders = ["KODU", "ADI", "SIMGE", "ONCEKI BAKIYE", "PERY. GIRIS AGIRLIK", "PERY. CIKIS AGIRLIK", "PERY. STOK AGIRLIK", "BAKIYE"];
        S.updateTableHeadersstokkoduonceki(sqlHeaders);
      } else if (raporturu === "anaalt") {
        sqlHeaders = ["ANA GRUP", "ALT GRUP", "GIRIS MIKTARI", "GIRIS AGIRLIK", "CIKIS MIKTARI", "CIKIS AGIRLIK", "STOK MIKTARI", "STOK AGIRLIK"];
        S.updateTableHeadersanaalt(sqlHeaders);
      }

      let totalmiktar = 0;
      let totalgagirlik = 0;
      let totalcmiktar = 0;
      let totalcagirlik = 0;
      let totalstokmik = 0;
      let totalstokagi = 0;

      const rows = response.data || [];
      const tbody = mainTableBody || document.querySelector("#main-table tbody");

      rows.forEach((rowData) => {
        const tr = document.createElement("tr");
        tr.classList.add("expandable", "table-row-height");

        if (raporturu === "stokkodu") {
          tr.innerHTML = `
            <td>${rowData.Kodu || ""}</td>
            <td>${rowData.Adi || ""}</td>
            <td>${rowData.Simge || ""}</td>
            <td class="double-column">${formatNumber3(rowData.Giris_Miktari)}</td>
            <td class="double-column">${formatNumber3(rowData.Giris_Agirlik)}</td>
            <td class="double-column">${formatNumber3(rowData.Cikis_Miktari)}</td>
            <td class="double-column">${formatNumber3(rowData.Cikis_Agirlik)}</td>
            <td class="double-column">${formatNumber3(rowData.Stok_Miktari)}</td>
            <td class="double-column">${formatNumber3(rowData.Stok_Agirlik)}</td>
          `;

          totalmiktar += (rowData.Giris_Miktari || 0);
          totalgagirlik += (rowData.Giris_Agirlik || 0);
          totalcmiktar += (rowData.Cikis_Miktari || 0);
          totalcagirlik += (rowData.Cikis_Agirlik || 0);
          totalstokmik += (rowData.Stok_Miktari || 0);
          totalstokagi += (rowData.Stok_Agirlik || 0);

        } else if (raporturu === "stokkoduonceki") {
          tr.innerHTML = `
            <td>${rowData.Kodu || ""}</td>
            <td>${rowData.Adi || ""}</td>
            <td>${rowData.Simge || ""}</td>
            <td class="double-column">${formatNumber3(rowData.Onceki_Bakiye)}</td>
            <td class="double-column">${formatNumber3(rowData.Periyot_Giris_Agirlik)}</td>
            <td class="double-column">${formatNumber3(rowData.Periyot_Cikis_Agirlik)}</td>
            <td class="double-column">${formatNumber3(rowData.Periyot_Stok_Agirlik)}</td>
            <td class="double-column">${formatNumber3(rowData.BAKIYE)}</td>
          `;

          totalmiktar += (rowData.Onceki_Bakiye || 0);
          totalgagirlik += (rowData.Periyot_Giris_Agirlik || 0);
          totalcmiktar += (rowData.Periyot_Cikis_Agirlik || 0);
          totalcagirlik += (rowData.Periyot_Stok_Agirlik || 0);
          totalstokmik += (rowData.BAKIYE || 0);

        } else if (raporturu === "anaalt") {
          tr.innerHTML = `
            <td>${rowData.ANA_GRUP || ""}</td>
            <td>${rowData.ALT_GRUP || ""}</td>
            <td class="double-column">${formatNumber3(rowData.Giris_Miktar)}</td>
            <td class="double-column">${formatNumber3(rowData.Giris_Agirlik)}</td>
            <td class="double-column">${formatNumber3(rowData.Cikis_Miktar)}</td>
            <td class="double-column">${formatNumber3(rowData.Cikis_Agirlik)}</td>
            <td class="double-column">${formatNumber3(rowData.Stok_Miktar)}</td>
            <td class="double-column">${formatNumber3(rowData.Stok_Agirlik)}</td>
          `;

          totalmiktar += (rowData.Giris_Miktar || 0);
          totalgagirlik += (rowData.Giris_Agirlik || 0);
          totalcmiktar += (rowData.Cikis_Miktar || 0);
          totalcagirlik += (rowData.Cikis_Agirlik || 0);
          totalstokmik += (rowData.Stok_Miktar || 0);
          totalstokagi += (rowData.Stok_Agirlik || 0);
        }

        tbody.appendChild(tr);
      });

      // totals bas (id’ler senin kurala göre)
      if (raporturu === "stokkodu") {
        S.byId("toplam-3").innerText = formatNumber3(totalmiktar);
        S.byId("toplam-4").innerText = formatNumber3(totalgagirlik);
        S.byId("toplam-5").innerText = formatNumber3(totalcmiktar);
        S.byId("toplam-6").innerText = formatNumber3(totalcagirlik);
        S.byId("toplam-7").innerText = formatNumber3(totalstokmik);
        S.byId("toplam-8").innerText = formatNumber3(totalstokagi);
      } else if (raporturu === "stokkoduonceki") {
        S.byId("toplam-3").innerText = formatNumber3(totalmiktar);
        S.byId("toplam-4").innerText = formatNumber3(totalgagirlik);
        S.byId("toplam-5").innerText = formatNumber3(totalcmiktar);
        S.byId("toplam-6").innerText = formatNumber3(totalcagirlik);
        S.byId("toplam-7").innerText = formatNumber3(totalstokmik);
      } else if (raporturu === "anaalt") {
        S.byId("toplam-2").innerText = formatNumber3(totalmiktar);
        S.byId("toplam-3").innerText = formatNumber3(totalgagirlik);
        S.byId("toplam-4").innerText = formatNumber3(totalcmiktar);
        S.byId("toplam-5").innerText = formatNumber3(totalcagirlik);
        S.byId("toplam-6").innerText = formatNumber3(totalstokmik);
        S.byId("toplam-7").innerText = formatNumber3(totalstokagi);
      }

    } catch (error) {
      S.errShow(error?.message || String(error));
    } finally {
      S.disableBtn("stokyenileButton", false, "İşleniyor...", "Yenile");
      S.cursor("default");
    }
  };

  /* =========================
     DOWNLOAD
     ========================= */
  S.extractTableData = function (tableId) {
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

      // tfoot TH (senin footer’lar th)
      tfoot.querySelectorAll("th").forEach((th, index) => {
        const value = th.innerText.trim();
        if (value !== "") nonEmptyCount++;
        tfootRowData[headers[index]] = value;
      });

      if (nonEmptyCount > 0) rows.push(tfootRowData);
    }

    return rows;
  };

  S.stokdownloadReport = async function () {
    S.errClear();
    S.cursor("wait");

    S.disableBtn("stokrapreportDownload", true, "İşleniyor...", "Rapor İndir");
    const yenile = S.byId("stokyenileButton");
    if (yenile) yenile.disabled = true;

    try {
      const rows = S.extractTableData("main-table");

      const response = await fetchWithSessionCheckForDownload("stok/stk_download", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(rows),
      });

      if (response?.blob) {
        const disposition = response.headers.get("Content-Disposition");
        const fileName = disposition?.match(/filename="(.+)"/)?.[1] || "stokrapor.xlsx";

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
      S.errShow(error?.message);
    } finally {
      S.disableBtn("stokrapreportDownload", false, "İşleniyor...", "Rapor İndir");
      if (yenile) yenile.disabled = false;
      S.cursor("default");
    }
  };

  /* =========================
     MAIL
     ========================= */
  S.stokmailAt = function () {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    S.cursor("wait");

    const rows = S.extractTableData("main-table");
    localStorage.setItem("tableData", JSON.stringify({ rows }));

    const degerler = "stok";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);

    S.cursor("default");
  };

  /* =========================
     GRUPLAMA changed
     ========================= */
  S.gruplamaChanged = function () {
    const gruplama = S.byId("gruplama")?.value || "";
    const oncekitarih = S.byId("oncekitarih"); // checkbox
    if (!oncekitarih) return;

    if (gruplama === "Urun Kodu") {
      oncekitarih.disabled = false;
    } else {
      oncekitarih.disabled = true;
    }
  };

})();
