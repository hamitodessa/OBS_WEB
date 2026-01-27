/* =========================================================
   KERDETAY (jQuery YOK) - OBS Namespace
   window.OBS = window.OBS || {};
   OBS.KERDETAY = ...
   ========================================================= */

window.OBS = window.OBS || {};
OBS.KERDETAY = OBS.KERDETAY || {};

(() => {
  const M = OBS.KERDETAY;

  /* ---------------- state ---------------- */
  M.currentPage = 0;
  M.totalPages = 0;
  M.pageSize = 250;

  /* ---------------- helpers ---------------- */
  M.el = (id) => document.getElementById(id);

  M.setCursor = (wait) => {
    document.body.style.cursor = wait ? "wait" : "default";
  };

  M.setDisabled = (el, yes) => { if (el) el.disabled = !!yes; };

  M.clearError = () => {
    const e = M.el("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
  };

  M.showError = (msg) => {
    const e = M.el("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Beklenmeyen bir hata oluştu.";
  };

  M.setBtn = (id, disabled, text) => {
    const b = M.el(id);
    if (!b) return;
    b.disabled = !!disabled;
    if (typeof text === "string") b.textContent = text;
  };

  M.updatePaginationUI = (disableAllWhileLoading = false) => {
    const first = M.el("ilksayfa");
    const prev  = M.el("oncekisayfa");
    const next  = M.el("sonrakisayfa");
    const last  = M.el("sonsayfa");

    if (disableAllWhileLoading) {
      M.setDisabled(first, true); M.setDisabled(prev, true);
      M.setDisabled(next, true);  M.setDisabled(last, true);
      return;
    }

    const noData = M.totalPages === 0;
    M.setDisabled(first, noData || M.currentPage <= 0);
    M.setDisabled(prev,  noData || M.currentPage <= 0);
    M.setDisabled(next,  noData || M.currentPage >= M.totalPages - 1);
    M.setDisabled(last,  noData || M.currentPage >= M.totalPages - 1);
  };

  /* ---------------- DTO ---------------- */
  M.getKeresteDetayRaporDTO = () => {
    const hidden = M.el("kerestedetayBilgi");
    const raw = hidden ? (hidden.value || "") : "";
    const p = raw.split(",");

    return {
      gtar1: p[0],
      gtar2: p[1],
      ctar1: p[2],
      ctar2: p[3],
      ukodu1: p[4],
      ukodu2: p[5],
      cfirma1: p[6],
      cfirma2: p[7],
      pak1: p[8],
      pak2: p[9],
      cevr1: p[10],
      cevr2: p[11],
      gfirma1: p[12],
      gfirma2: p[13],
      cana: p[14],
      evr1: p[15],
      evr2: p[16],
      calt: p[17],
      gana: p[18],
      galt: p[19],
      gdepo: p[20],
      gozkod: p[21],
      kons1: p[22],
      kons2: p[23],
      cozkod: p[24],
      cdepo: p[25]
    };
  };

  /* ---------------- pagination buttons ---------------- */
  M.ilksayfa = () => { if (M.currentPage > 0) M.kerestedetayfetchTableData(0); };
  M.oncekisayfa = () => { if (M.currentPage > 0) M.kerestedetayfetchTableData(M.currentPage - 1); };
  M.sonrakisayfa = () => { if (M.currentPage < M.totalPages - 1) M.kerestedetayfetchTableData(M.currentPage + 1); };
  M.sonsayfa = () => { if (M.totalPages > 0) M.kerestedetayfetchTableData(M.totalPages - 1); };

  /* ---------------- page count ---------------- */
  M.toplampagesize = async () => {
    try {
      M.clearError();

      const dto = M.getKeresteDetayRaporDTO();
      const response = await fetchWithSessionCheck("kereste/kerestedetaydoldursize", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto)
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const totalRecords = response?.totalRecords ?? 0;
      M.totalPages = Math.max(0, Math.ceil(Number(totalRecords) / M.pageSize));
    } catch (err) {
      M.totalPages = 0;
      M.showError(err?.message || String(err));
    } finally {
      M.updatePaginationUI();
    }
  };

  M.kerestedetaydoldur = async () => {
    M.setCursor(true);
    M.updatePaginationUI(true);      // yükleme sırasında kilitle
    try {
      await M.toplampagesize();      // ✅ bekle
      await M.kerestedetayfetchTableData(0);
    } finally {
      M.setCursor(false);
    }
  };

  /* ---------------- fetch table ---------------- */
  M.kerestedetayfetchTableData = async (page) => {
    const dto = { ...M.getKeresteDetayRaporDTO(), page, pageSize: M.pageSize };

    M.clearError();
    M.setCursor(true);
    M.updatePaginationUI(true);

    // jQuery yok: direkt buton
    M.setBtn("kerestedetayyenileButton", true, "İşleniyor...");

    const mainTableBody = M.el("tbody");
    if (mainTableBody) mainTableBody.innerHTML = "";

    M.currentPage = page;

    try {
      const response = await fetchWithSessionCheck("kereste/kerestedetaydoldur", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto)
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const data = response?.data || [];
      data.forEach((rowData) => {
        const row = document.createElement("tr");
        row.classList.add("table-row-height");
        row.innerHTML = `
          <td>${rowData.Evrak_No ?? ""}</td>
          <td>${rowData.Barkod ?? ""}</td>
          <td>${rowData.Kodu ?? ""}</td>
          <td>${rowData.Paket_No ?? ""}</td>
          <td>${rowData.Konsimento ?? ""}</td>
          <td class="double-column">${formatNumber0(rowData.Miktar)}</td>
          <td class="double-column">${formatNumber3(rowData.m3)}</td>
          <td>${formatDate(rowData.Tarih)}</td>
          <td class="double-column">${formatNumber2(rowData.Kdv)}</td>
          <td>${rowData.Doviz ?? ""}</td>
          <td class="double-column">${formatNumber2(rowData.Fiat)}</td>
          <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
          <td class="double-column">${formatNumber2(rowData.Kur)}</td>
          <td>${rowData.Cari_Firma ?? ""}</td>
          <td>${rowData.Adres_Firma ?? ""}</td>
          <td class="double-column">${formatNumber2(rowData.Iskonto)}</td>
          <td class="double-column">${formatNumber2(rowData.Tevkifat)}</td>
          <td>${rowData.Ana_Grup ?? ""}</td>
          <td>${rowData.Alt_Grup ?? ""}</td>
          <td>${rowData.Mensei ?? ""}</td>
          <td>${rowData.Depo ?? ""}</td>
          <td>${rowData.Ozel_Kod ?? ""}</td>
          <td>${rowData.Izahat ?? ""}</td>
          <td>${rowData.Nakliyeci ?? ""}</td>
          <td>${rowData.USER ?? ""}</td>
          <td>${rowData.Cikis_Evrak ?? ""}</td>
          <td>${rowData.CTarih ?? ""}</td>
          <td class="double-column">${formatNumber2(rowData.CKdv)}</td>
          <td>${rowData.CDoviz ?? ""}</td>
          <td class="double-column">${formatNumber2(rowData.CFiat)}</td>
          <td class="double-column">${formatNumber2(rowData.CTutar)}</td>
          <td class="double-column">${formatNumber2(rowData.CKur)}</td>
          <td>${rowData.CCari_Firma ?? ""}</td>
          <td>${rowData.CAdres_Firma ?? ""}</td>
          <td class="double-column">${formatNumber2(rowData.CIskonto)}</td>
          <td class="double-column">${formatNumber2(rowData.CTevkifat)}</td>
          <td>${rowData.C_Ana_Grup ?? ""}</td>
          <td>${rowData.C_Alt_Grup ?? ""}</td>
          <td>${rowData.C_Depo ?? ""}</td>
          <td>${rowData.COzel_Kod ?? ""}</td>
          <td>${rowData.CIzahat ?? ""}</td>
          <td>${rowData.C_Nakliyeci ?? ""}</td>
          <td>${rowData.CUSER ?? ""}</td>
        `;
        mainTableBody && mainTableBody.appendChild(row);
      });
    } catch (err) {
      M.showError(err?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
      M.setBtn("kerestedetayyenileButton", false, "Yenile");
      M.setCursor(false);
      M.updatePaginationUI();
    }
  };

  /* ---------------- download report (jQuery yok) ---------------- */
  M.kerestedetaydownloadReport = async () => {
    M.clearError();
    M.setCursor(true);

    M.setBtn("kerestedetayreportDownload", true, "İşleniyor...");
    M.setBtn("keresteyenileButton", true); // senin id böyleydi (yanlışsa düzelt)

    try {
      const table = document.querySelector("#main-table");
      if (!table) throw new Error("Tablo bulunamadı.");

      const headers = [];
      const rows = [];

      table.querySelectorAll("thead th").forEach((th) => headers.push(th.innerText.trim()));

      table.querySelectorAll("tbody tr").forEach((tr) => {
        const rowData = {};
        let isEmpty = true;

        tr.querySelectorAll("td").forEach((td, index) => {
          const value = td.innerText.trim();
          if (value !== "") isEmpty = false;
          rowData[headers[index]] = value;
        });

        if (!isEmpty) rows.push(rowData);
      });

      const response = await fetchWithSessionCheckForDownload("kereste/kerdetay_download", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(rows)
      });

      if (response?.blob) {
        const disposition = response.headers.get("Content-Disposition") || "";
        const m = disposition.match(/filename="(.+)"/);
        const fileName = m ? m[1] : "kerdetay.xlsx";

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
    } catch (err) {
      M.showError(err?.message || "Bilinmeyen bir hata oluştu.");
    } finally {
      M.setBtn("kerestedetayreportDownload", false, "Rapor İndir");
      M.setBtn("keresteyenileButton", false);
      M.setCursor(false);
    }
  };

  /* ---------------- mail (jQuery yok) ---------------- */
  M.kerestedetaymailAt = () => {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    const table = document.querySelector("#main-table");
    if (!table) {
      M.showError("Tablo bulunamadı.");
      return;
    }

    const headers = [];
    const rows = [];

    table.querySelectorAll("thead th").forEach((th) => headers.push(th.innerText.trim()));
    table.querySelectorAll("tbody tr").forEach((tr) => {
      const rowData = {};
      let isEmpty = true;

      tr.querySelectorAll("td").forEach((td, index) => {
        const value = td.innerText.trim();
        if (value !== "") isEmpty = false;
        rowData[headers[index]] = value;
      });

      if (!isEmpty) rows.push(rowData);
    });

    localStorage.setItem("tableData", JSON.stringify({ rows }));
    const degerler = "kerdetay";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);
  };

  /* ---------------- anagrpChanged (genel) ---------------- */
  M.anagrpChanged = async (anagrpElement, altgrpElementId) => {
    const anagrup = anagrpElement.value;
    const selectElement = M.el(altgrpElementId);

    if (!selectElement) return;

    selectElement.innerHTML = "";
    if (anagrup === "") {
      selectElement.disabled = true;
      return;
    }

    M.setCursor(true);
    M.clearError();

    try {
      const response = await fetchWithSessionCheck("kereste/altgrup", {
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
    } catch (err) {
      selectElement.disabled = true;
      M.showError(err?.message);
    } finally {
      M.setCursor(false);
    }
  };

  /* ---------------- modal open (Bootstrap 5 jQuery yok) ---------------- */
  M.openkdetayModal = async (modalSelectorOrEl) => {
    M.setCursor(true);
    M.clearError();

    try {
      const modalEl = (typeof modalSelectorOrEl === "string")
        ? document.querySelector(modalSelectorOrEl)
        : modalSelectorOrEl;

      if (modalEl && window.bootstrap?.Modal) {
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
      }

      const response = await fetchWithSessionCheck("kereste/anadepo", {
        method: "POST",
        headers: { "Content-Type": "application/json" }
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const ana = response.anaKodlari || [];
      const dpo = response.depoKodlari || [];
      const oz  = response.oz1Kodlari || [];

      const anaSelect  = M.el("anagrp");
      const canaSelect = M.el("canagrp");
      const dpoSelect  = M.el("depo");
      const cdpoSelect = M.el("cdepo");
      const ozSelect   = M.el("ozkod");
      const cozSelect  = M.el("cozkod");

      [anaSelect, canaSelect, dpoSelect, cdpoSelect, ozSelect, cozSelect].forEach(s => { if (s) s.innerHTML = ""; });

      ana.forEach((item) => {
        if (anaSelect) {
          const o = document.createElement("option");
          o.value = item.ANA_GRUP;
          o.textContent = item.ANA_GRUP;
          anaSelect.appendChild(o);
        }
        if (canaSelect) {
          const o = document.createElement("option");
          o.value = item.ANA_GRUP;
          o.textContent = item.ANA_GRUP;
          canaSelect.appendChild(o);
        }
      });

      dpo.forEach((item) => {
        if (dpoSelect) {
          const o = document.createElement("option");
          o.value = item.DEPO;
          o.textContent = item.DEPO;
          dpoSelect.appendChild(o);
        }
        if (cdpoSelect) {
          const o = document.createElement("option");
          o.value = item.DEPO;
          o.textContent = item.DEPO;
          cdpoSelect.appendChild(o);
        }
      });

      oz.forEach((item) => {
        if (ozSelect) {
          const o = document.createElement("option");
          o.value = item.OZEL_KOD_1;
          o.textContent = item.OZEL_KOD_1;
          ozSelect.appendChild(o);
        }
        if (cozSelect) {
          const o = document.createElement("option");
          o.value = item.OZEL_KOD_1;
          o.textContent = item.OZEL_KOD_1;
          cozSelect.appendChild(o);
        }
      });

      // "Bos Olanlar" opsiyonlarını 2. sıraya koy
      const insertBos = (sel) => {
        if (!sel) return;
        const o = document.createElement("option");
        o.value = "Bos Olanlar";
        o.textContent = "Bos Olanlar";
        sel.insertBefore(o, sel.options[1] || null);
      };
      insertBos(anaSelect);
      insertBos(canaSelect);
      insertBos(dpoSelect);
      insertBos(cdpoSelect);

    } catch (err) {
      M.showError(`Bir hata oluştu: ${err?.message || String(err)}`);
    } finally {
      M.setCursor(false);
    }
  };

  /* ---------------- expose (HTML onclick için) ---------------- */
  window.updatePaginationUI = M.updatePaginationUI;

  window.ilksayfa = M.ilksayfa;
  window.oncekisayfa = M.oncekisayfa;
  window.sonrakisayfa = M.sonrakisayfa;
  window.sonsayfa = M.sonsayfa;

  window.toplampagesize = M.toplampagesize;
  window.kerestedetaydoldur = M.kerestedetaydoldur;
  window.kerestedetayfetchTableData = M.kerestedetayfetchTableData;

  window.kerestedetaydownloadReport = M.kerestedetaydownloadReport;
  window.kerestedetaymailAt = M.kerestedetaymailAt;

  window.anagrpChanged = M.anagrpChanged;
  window.openenvModal = M.openenvModal;

})();
