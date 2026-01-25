/* =========================
   OBS Namespace
   ========================= */
window.OBS = window.OBS || {};
OBS.STKDETAY = OBS.STKDETAY || {};

(() => {
  const S = OBS.STKDETAY;

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
    if (!yes && textNo != null) b.innerText = textNo;
  };

  /* =========================================================
     ANA GRUP -> ALT GRUP  (kereste/altgrup)
     ========================================================= */
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
      const response = await fetchWithSessionCheck("kereste/altgrup", {
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
      S.errShow(error?.message);
    } finally {
      S.cursor("default");
    }
  };

  /* =========================================================
     MODAL OPEN (jQuery YOK)
     endpoint: stok/anadepo
     doldur: #uranagrp, #anagrp, #depo  + Bos Olanlar
     ========================================================= */
  S.openstkdtyModal = async function (modalRef) {
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
        headers: { "Content-Type": "application/json" }
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const ana = response.anaKodlari || [];
      const dpo = response.depoKodlari || [];

      const uranaSelect = S.byId("uranagrp");
      const anaSelect   = S.byId("anagrp");
      const dpoSelect   = S.byId("depo");

      if (uranaSelect) uranaSelect.innerHTML = "";
      if (anaSelect) anaSelect.innerHTML = "";
      if (dpoSelect) dpoSelect.innerHTML = "";

      const addBos = (sel) => {
        if (!sel) return;
        sel.add(new Option("", ""));
        sel.add(new Option("Bos Olanlar", "Bos Olanlar"));
      };

      // ana gruplar
      addBos(anaSelect);
      addBos(uranaSelect);

      const seen = new Set();
      for (const it of ana) {
        const v = (it.ANA_GRUP || "").trim();
        if (!v || seen.has(v)) continue;
        seen.add(v);

        if (anaSelect)   anaSelect.add(new Option(v, v));
        if (uranaSelect) uranaSelect.add(new Option(v, v));
      }

      // depolar
      addBos(dpoSelect);
      const seenD = new Set();
      for (const it of dpo) {
        const v = (it.DEPO || "").trim();
        if (!v || seenD.has(v)) continue;
        seenD.add(v);
        if (dpoSelect) dpoSelect.add(new Option(v, v));
      }

    } catch (error) {
      S.errShow(`Bir hata oluştu: ${error.message}`);
    } finally {
      S.cursor("default");
    }
  };

  /* =========================================================
     DTO (hidden input #stokdetayBilgi) — jQuery yok
     ========================================================= */
  S.getDTO = function () {
    const hf = S.byId("stokdetayBilgi");
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
      ckod1: p[8],
      ckod2: p[9],
      anagrp: p[10],
      altgrp: p[11],
      depo: p[12],
      depohardahil: p[13],
      uretfisdahil: p[14],
      turu: p[15],
    };
  };

  /* =========================================================
     FETCH (stok/stokdetaydoldur)
     ========================================================= */
  S.stokdetayfetchTableData = async function () {
    const dto = S.getDTO();

    S.errClear();
    S.cursor("wait");
    S.disableBtn("stokdetayyenileButton", true, "İşleniyor...", "Yenile");

    const mainTableBody = S.byId("mainTableBody");
    if (mainTableBody) mainTableBody.innerHTML = "";

    try {
      const response = await fetchWithSessionCheck("stok/stokdetaydoldur", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      window.data = response;

      (response.data || []).forEach(rowData => {
        const row = document.createElement("tr");
        row.classList.add("expandable", "table-row-height");
        row.innerHTML = `
          <td>${rowData.Urun_Kodu || ""}</td>
          <td>${rowData.Barkod || ""}</td>
          <td>${rowData.Adi || ""}</td>
          <td>${rowData.Izahat || ""}</td>
          <td>${rowData.Evrak_No || ""}</td>
          <td>${rowData.Hesap_Kodu || ""}</td>
          <td>${rowData.Evrak_Cins || ""}</td>
          <td>${formatDate(rowData.Tarih) || ""}</td>
          <td class="double-column">${formatNumber3(rowData.Miktar)}</td>
          <td>${rowData.Birim || ""}</td>
          <td class="double-column">${formatNumber2(rowData.Fiat)}</td>
          <td>${rowData.Doviz || ""}</td>
          <td class="double-column">${formatNumber3(rowData.Miktar_Bakiye)}</td>
          <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
          <td class="double-column">${formatNumber2(rowData.Tutar_Bakiye)}</td>
          <td>${rowData.Ana_Grup || ""}</td>
          <td>${rowData.Alt_Grup || ""}</td>
          <td>${rowData.Depo || ""}</td>
          <td>${rowData.USER || ""}</td>
        `;
        mainTableBody.appendChild(row);
      });

    } catch (error) {
      S.errShow(error?.message || String(error));
    } finally {
      S.disableBtn("stokdetayyenileButton", false, "İşleniyor...", "Yenile");
      S.cursor("default");
    }
  };

  /* =========================================================
     TABLE EXPORT DATA (rows[])
     ========================================================= */
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

    return rows;
  };

  /* =========================================================
     DOWNLOAD
     (düzeltme: yanlış yenile butonu değil, stokdetayyenileButton)
     ========================================================= */
  S.stokdetaydownloadReport = async function () {
    S.errClear();
    S.cursor("wait");

    S.disableBtn("stokdetayreportDownload", true, "İşleniyor...", "Rapor İndir");
    const yenileBtn = S.byId("stokdetayyenileButton");
    if (yenileBtn) yenileBtn.disabled = true;

    try {
      const rows = S.extractTableData("main-table");

      const response = await fetchWithSessionCheckForDownload("stok/stokdetay_download", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(rows),
      });

      if (response?.blob) {
        const disposition = response.headers.get("Content-Disposition");
        const fileName = disposition?.match(/filename="(.+)"/)?.[1] || "stokdetay.xlsx";

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
      S.disableBtn("stokdetayreportDownload", false, "İşleniyor...", "Rapor İndir");
      if (yenileBtn) yenileBtn.disabled = false;
      S.cursor("default");
    }
  };

  /* =========================================================
     MAIL
     ========================================================= */
  S.stokdetaymailAt = function () {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    S.cursor("wait");

    const rows = S.extractTableData("main-table");
    localStorage.setItem("tableData", JSON.stringify({ rows }));

    const degerler = "stokdetay";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);

    S.cursor("default");
  };

})();
