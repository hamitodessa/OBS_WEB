/* =========================
   OBS Namespace (çakışmasız)
   ========================= */
window.OBS = window.OBS || {};
OBS.FATRAPOR = OBS.FATRAPOR || {};

(() => {
  const F = OBS.FATRAPOR;

  /* ---------- state ---------- */
  F.currentPage = 0;
  F.totalPages = 0;
  F.pageSize = 250;

  /* ---------- helpers ---------- */
  F.byId = (id) => document.getElementById(id);

  F.errClear = () => {
    const e = F.byId("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
  };

  F.errShow = (msg) => {
    const e = F.byId("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Beklenmeyen bir hata oluştu.";
  };

  F.cursor = (mode) => {
    document.body.style.cursor = mode || "default";
  };

  F.disableBtn = (id, yes, textYes, textNo) => {
    const b = F.byId(id);
    if (!b) return;
    b.disabled = !!yes;
    if (yes && textYes != null) b.innerText = textYes;
    if (!yes && textNo != null) b.innerText = textNo;
  };

  F.setText = (id, val) => {
    const el = F.byId(id);
    if (!el) return;
    el.innerText = val ?? "";
  };

  /* =========================
     ANA GRUP -> ALT GRUP
     ========================= */
  F.anagrpChanged = async function (anagrpElement) {
    const anagrup = anagrpElement?.value || "";
    const selectElement = F.byId("altgrp");
    if (!selectElement) return;

    selectElement.innerHTML = "";

    if (anagrup === "") {
      selectElement.disabled = true;
      return;
    }

    F.cursor("wait");
    F.errClear();

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
      F.errShow(error?.message);
    } finally {
      F.cursor("default");
    }
  };

  /* =========================
     PAGINATION
     ========================= */
  F.ilksayfa = () => F.fatfetchTableData(0);

  F.oncekisayfa = () => {
    if (F.currentPage > 0) F.fatfetchTableData(F.currentPage - 1);
  };

  F.sonrakisayfa = () => {
    if (F.currentPage < F.totalPages - 1) F.fatfetchTableData(F.currentPage + 1);
  };

  F.sonsayfa = () => F.fatfetchTableData(Math.max(F.totalPages - 1, 0));

  /* =========================
     DTO (jQuery yok)
     ========================= */
  F.getfatraporDTO = function () {
    const hf = F.byId("fatrapBilgi");
    const hiddenFieldValue = hf ? (hf.value || "") : "";
    const p = hiddenFieldValue.split(",");

    return {
      fatno1: p[0],
      fatno2: p[1],
      anagrp: p[2],
      tar1: p[3],
      tar2: p[4],
      altgrp: p[5],
      ckod1: p[6],
      ckod2: p[7],
      depo: p[8],
      adr1: p[9],
      adr2: p[10],
      turu: p[11],
      ukod1: p[12],
      ukod2: p[13],
      tev1: p[14],
      tev2: p[15],
      okod1: p[16],
      okod2: p[17],
      gruplama: p[18],
      dvz1: p[19],
      dvz2: p[20],
      caradr: p[21]
    };
  };

  /* =========================
     PAGE COUNT
     ========================= */
  F.toplampagesize = async function () {
    try {
      F.errClear();

      const fatraporDTO = F.getfatraporDTO();
      const response = await fetchWithSessionCheck("stok/fatdoldursize", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(fatraporDTO),
      });

      const totalRecords = response?.totalRecords || 0;
      F.totalPages = Math.ceil(totalRecords / F.pageSize);
    } catch (error) {
      F.errShow(error?.message || String(error));
    } finally {
      F.cursor("default");
    }
  };

  F.fatdoldur = async function () {
    F.cursor("wait");
    await F.toplampagesize();      // ✅ await
    await F.fatfetchTableData(0);
  };

  /* =========================
     TABLE DATA
     ========================= */
  F.fatfetchTableData = async function (page) {
    const fatraporDTO = F.getfatraporDTO();
    fatraporDTO.page = page;
    fatraporDTO.pageSize = F.pageSize;

    F.currentPage = page;

    F.errClear();
    F.cursor("wait");

    // UI
    F.disableBtn("fatrapyenileButton", true, "İşleniyor...", "Filtre");

    const mainTableBody = F.byId("mainTableBody");
    if (mainTableBody) mainTableBody.innerHTML = "";
    F.clearTfoot();

    try {
      const response = await fetchWithSessionCheck("stok/fatrapdoldur", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(fatraporDTO),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const res = response;
      const raporturu = res.raporturu;

      // ✅ fkodu’da 1. ve 2. sütun birbirine girmesin: tabloya class bağla
      const table = document.querySelector("#main-table");
      if (table) {
        if (raporturu === "fkodu") {
          table.classList.remove("has-toggle");
          table.classList.add("no-toggle");
        } else {
          table.classList.add("has-toggle");
          table.classList.remove("no-toggle");
        }
      }

      // HEADER
      let sqlHeaders = [];
      if (raporturu === "fno") {
        sqlHeaders = ["", "FATURA NO", "HAREKET", "TARIH", "CARI_HESAP", "ADRES_HESAP", "DOVIZ", "MIKTAR", "TUTAR", "ISK. TUTAR"];
        F.updateTableHeadersfno(sqlHeaders);
      } else if (raporturu === "fkodu") {
        sqlHeaders = ["FATURA NO", "HAREKET", "UNVAN", "VERGI NO", "MIKTAR", "TUTAR", "ISK. TUTAR", "KDV TUTAR", "TOPLAM TUTAR"];
        F.updateTableHeadersfkodu(sqlHeaders);
      } else if (raporturu === "fnotar") {
        sqlHeaders = ["", "FATURA NO", "HAREKET", "TARIH", "UNVAN", "VERGI NO", "MIKTAR", "TUTAR", "ISK. TUTAR", "KDV TUTAR", "TOPLAM TUTAR"];
        F.updateTableHeadersfnotar(sqlHeaders);
      }

      const thCount = document.querySelectorAll("#main-table thead th").length;

      let totalmiktar = 0;
      let totaltutar = 0;

      (res.data || []).forEach((rowData) => {
        const row = document.createElement("tr");
        row.classList.add("expandable", "table-row-height");

        if (raporturu === "fno") {
          row.innerHTML = `
            <td><span class="toggle-button">+</span></td>
            <td>${rowData.Fatura_No || ""}</td>
            <td>${rowData.Hareket || ""}</td>
            <td>${formatDate(rowData.Tarih)}</td>
            <td>${rowData.Cari_Firma || ""}</td>
            <td>${rowData.Adres_Firma || ""}</td>
            <td>${rowData.Doviz || ""}</td>
            <td class="double-column">${formatNumber3(rowData.Miktar)}</td>
            <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Iskontolu_Tutar)}</td>
          `;
          totalmiktar += (rowData.Miktar || 0);
          totaltutar += (rowData.Iskontolu_Tutar || 0);

        } else if (raporturu === "fkodu") {
          row.innerHTML = `
            <td>${rowData.Fatura_No || ""}</td>
            <td>${rowData.Hareket || ""}</td>
            <td>${rowData.Unvan || ""}</td>
            <td>${rowData.Vergi_No || ""}</td>
            <td class="double-column">${formatNumber3(rowData.Miktar)}</td>
            <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Iskontolu_Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Kdv_Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Toplam_Tutar)}</td>
          `;
          totalmiktar += (rowData.Miktar || 0);
          totaltutar += (rowData.Toplam_Tutar || 0);

        } else if (raporturu === "fnotar") {
          row.innerHTML = `
            <td><span class="toggle-button">+</span></td>
            <td>${rowData.Fatura_No || ""}</td>
            <td>${rowData.Hareket || ""}</td>
            <td>${formatDate(rowData.Tarih)}</td>
            <td>${rowData.Unvan || ""}</td>
            <td>${rowData.Vergi_No || ""}</td>
            <td class="double-column">${formatNumber3(rowData.Miktar)}</td>
            <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Iskontolu_Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Kdv_Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Toplam_Tutar)}</td>
          `;
          totalmiktar += (rowData.Miktar || 0);
          totaltutar += (rowData.Toplam_Tutar || 0);
        }

        if (mainTableBody) mainTableBody.appendChild(row);

        // details row (fkodu’da da koyuyoruz ama detaysız kalacak)
        const detailsRow = document.createElement("tr");
        detailsRow.classList.add("details-row");
        detailsRow.style.display = "none";
        detailsRow.innerHTML = `<td colspan="${thCount}"></td>`;
        if (mainTableBody) mainTableBody.appendChild(detailsRow);

        // fkodu detaysız
        if (raporturu === "fkodu") return;

        const toggle = row.querySelector(".toggle-button");
        if (!toggle) return;

        // selected satır
        row.addEventListener("click", () => {
          document.querySelectorAll("#main-table tbody tr.selected").forEach(r => r.classList.remove("selected"));
          row.classList.add("selected");
        });

        toggle.addEventListener("click", async (e) => {
          e.stopPropagation();

          const isOpen = (detailsRow.style.display === "table-row");

          // tek satır açık kalsın
          document.querySelectorAll("#main-table tr.details-row").forEach(r => (r.style.display = "none"));
          document.querySelectorAll("#main-table tbody tr").forEach(r => r.classList.remove("selected"));
          document.querySelectorAll("#main-table .toggle-button").forEach(x => {
            if (x.textContent === "-") x.textContent = "+";
          });

          if (isOpen) {
            detailsRow.style.display = "none";
            toggle.textContent = "+";
            return;
          }

          row.classList.add("selected");
          detailsRow.style.display = "table-row";
          toggle.textContent = "-";

          if (detailsRow.dataset.loaded === "1") return;

          F.cursor("wait");
          try {
            const detResp = await F.fetchDetails(rowData.Fatura_No, rowData.Hareket);
            const det = detResp?.data ? detResp.data : [];

            let html = `
              <div class="details-wrap">
                <table class="t-details">
                  <thead>
                    <tr>
                      <th>Kodu</th><th>Adi</th><th class="double-column">Miktar</th><th>Birim</th>
                      <th class="double-column">Fiat</th><th>Doviz</th><th class="double-column">Tutar</th>
                      <th class="double-column">Iskonto</th><th class="double-column">Iskonto_Tutar</th>
                      <th class="double-column">Iskontolu_Tutar</th><th class="double-column">Kdv</th>
                      <th class="double-column">Kdv Tutar</th><th class="double-column">Tevkifat</th>
                      <th class="double-column">Tev Edi.Kdv.</th><th class="double-column">Tev Dah Top. Tut.</th>
                      <th class="double-column">Beyan Edilen Kdv</th><th class="double-column">Tev Har TopTutar</th>
                      <th>Ana Grup</th><th>Alt Grup</th><th>Depo</th><th>Ozel Kod</th><th>Izahat</th><th>Hareket</th><th>User</th>
                    </tr>
                  </thead>
                  <tbody>
            `;

            det.forEach(item => {
              html += `
                <tr class="drow">
                  <td>${item.Kodu || ""}</td>
                  <td>${item.Adi || ""}</td>
                  <td class="double-column">${formatNumber3(item.Miktar)}</td>
                  <td>${item.Birim || ""}</td>
                  <td class="double-column">${formatNumber2(item.Fiat)}</td>
                  <td>${item.Doviz || ""}</td>
                  <td class="double-column">${formatNumber2(item.Tutar)}</td>
                  <td class="double-column">${formatNumber2(item.Iskonto)}</td>
                  <td class="double-column">${formatNumber2(item.Iskonto_Tutar)}</td>
                  <td class="double-column">${formatNumber2(item.Iskontolu_Tutar)}</td>
                  <td class="double-column">${formatNumber2(item.Kdv)}</td>
                  <td class="double-column">${formatNumber2(item.Kdv_Tutar)}</td>
                  <td class="double-column">${formatNumber2(item.Tevkifat)}</td>
                  <td class="double-column">${formatNumber2(item.Tev_Edilen_KDV)}</td>
                  <td class="double-column">${formatNumber2(item.Tev_Dah_Top_Tutar)}</td>
                  <td class="double-column">${formatNumber2(item.Beyan_Edilen_KDV)}</td>
                  <td class="double-column">${formatNumber2(item.Tev_Har_Top_Tutar)}</td>
                  <td>${item.Ana_Grup || ""}</td>
                  <td>${item.Alt_Grup || ""}</td>
                  <td>${item.Depo || ""}</td>
                  <td>${item.Ozel_Kod || ""}</td>
                  <td>${item.Izahat || ""}</td>
                  <td>${item.Hareket || ""}</td>
                  <td>${item.USER || ""}</td>
                </tr>
              `;
            });

            html += `</tbody></table></div>`;

            detailsRow.children[0].innerHTML = html;
            detailsRow.dataset.loaded = "1";
          } catch (err) {
            detailsRow.children[0].innerHTML =
              `<div class="details-wrap"><b>Hata:</b> Detaylar alınamadı.</div>`;
          } finally {
            F.cursor("default");
          }
        });
      });

      // TOPLAM (null guard)
      if (raporturu === "fno") {
        F.setText("toplam-7", formatNumber3(totalmiktar));
        F.setText("toplam-9", formatNumber2(totaltutar));
      } else if (raporturu === "fkodu") {
        F.setText("toplam-4", formatNumber3(totalmiktar));
        F.setText("toplam-8", formatNumber2(totaltutar));
      } else if (raporturu === "fnotar") {
        F.setText("toplam-6", formatNumber3(totalmiktar));
        F.setText("toplam-10", formatNumber2(totaltutar));
      }

      // Excel + Mail butonlarını aktif et (data geldiyse)
      const hasData = (res.data || []).length > 0;
      const excelBtn = F.byId("fatrapreportFormat");
      const mailBtn = F.byId("fatrapmailButton");
      if (excelBtn) excelBtn.disabled = !hasData;
      if (mailBtn) mailBtn.disabled = !hasData;

    } catch (error) {
      F.errShow(error?.message || String(error));
    } finally {
      F.disableBtn("fatrapyenileButton", false, "İşleniyor...", "Filtre");
      F.cursor("default");
    }
  };

  /* =========================
     TFOOT
     ========================= */
  F.clearTfoot = function () {
    const table = document.querySelector("#main-table");
    const tfoot = table?.querySelector("tfoot");
    if (!tfoot) return;
    tfoot.querySelectorAll("th").forEach(th => (th.textContent = ""));
  };

  /* =========================
     DETAILS FETCH
     ========================= */
  F.fetchDetails = async function (evrakNo, cins) {
    const gircik = (cins === "Alis") ? "G" : "C";
    const response = await fetchWithSessionCheck("stok/fatdetay", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ evrakNo, gircik }),
    });
    return response;
  };

  /* =========================
     HEADERS
     ========================= */
  F._ensureTfoot = function () {
    const table = document.querySelector("#main-table");
    if (!table) return null;
    let tfoot = table.querySelector("tfoot");
    if (!tfoot) {
      tfoot = document.createElement("tfoot");
      table.appendChild(tfoot);
    }
    return tfoot;
  };

  F.updateTableHeadersfno = function (headers) {
    const thead = document.querySelector("#main-table thead");
    const tfoot = F._ensureTfoot();
    if (!thead || !tfoot) return;

    thead.innerHTML = "";
    const trHead = document.createElement("tr");
    trHead.classList.add("thead-dark");

    headers.forEach((header, index) => {
      const th = document.createElement("th");
      th.textContent = header;
      if (index >= headers.length - 3) th.classList.add("double-column");
      trHead.appendChild(th);
    });
    thead.appendChild(trHead);

    tfoot.innerHTML = "";
    const trFoot = document.createElement("tr");

    headers.forEach((_, index) => {
      const th = document.createElement("th");
      if (index === 7) {
        th.textContent = "0.000";
        th.id = "toplam-" + index;
        th.classList.add("double-column");
      } else if (index === 9) {
        th.textContent = "0.00";
        th.id = "toplam-" + index;
        th.classList.add("double-column");
      } else {
        th.textContent = "";
      }
      trFoot.appendChild(th);
    });

    tfoot.appendChild(trFoot);
  };

  F.updateTableHeadersfkodu = function (headers) {
    const thead = document.querySelector("#main-table thead");
    const tfoot = F._ensureTfoot();
    if (!thead || !tfoot) return;

    thead.innerHTML = "";
    const trHead = document.createElement("tr");
    trHead.classList.add("thead-dark");

    headers.forEach((header, index) => {
      const th = document.createElement("th");
      th.textContent = header;
      if (index >= headers.length - 5) th.classList.add("double-column");
      trHead.appendChild(th);
    });
    thead.appendChild(trHead);

    tfoot.innerHTML = "";
    const trFoot = document.createElement("tr");

    headers.forEach((_, index) => {
      const th = document.createElement("th");
      if (index === 4) {
        th.textContent = "0.000";
        th.id = "toplam-" + index;
        th.classList.add("double-column");
      } else if (index === 8) {
        th.textContent = "0.00";
        th.id = "toplam-" + index;
        th.classList.add("double-column");
      } else {
        th.textContent = "";
      }
      trFoot.appendChild(th);
    });

    tfoot.appendChild(trFoot);
  };

  F.updateTableHeadersfnotar = function (headers) {
    const thead = document.querySelector("#main-table thead");
    const tfoot = F._ensureTfoot();
    if (!thead || !tfoot) return;

    thead.innerHTML = "";
    const trHead = document.createElement("tr");
    trHead.classList.add("thead-dark");

    headers.forEach((header, index) => {
      const th = document.createElement("th");
      th.textContent = header;
      if (index >= headers.length - 5) th.classList.add("double-column");
      trHead.appendChild(th);
    });
    thead.appendChild(trHead);

    tfoot.innerHTML = "";
    const trFoot = document.createElement("tr");

    headers.forEach((_, index) => {
      const th = document.createElement("th");
      if (index === 6) {
        th.textContent = "0.000";
        th.id = "toplam-" + index;
        th.classList.add("double-column");
      } else if (index === 10) {
        th.textContent = "0.00";
        th.id = "toplam-" + index;
        th.classList.add("double-column");
      } else {
        th.textContent = "";
      }
      trFoot.appendChild(th);
    });

    tfoot.appendChild(trFoot);
  };

  /* =========================
     MODAL OPEN (jQuery yok)
     ========================= */
  F.openfatrapModal = async function (modalRef) {
    // modalRef: "#fatrapModal" ya da element ya da id
    const modalEl =
      (typeof modalRef === "string")
        ? (modalRef.startsWith("#") ? document.querySelector(modalRef) : F.byId(modalRef))
        : modalRef;

    if (!modalEl) return;

    // Bootstrap 5
    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    modal.show();

    F.cursor("wait");
    F.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/anadepo", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const ana = response.anaKodlari || [];
      const dpo = response.depoKodlari || [];

      const anaSelect = F.byId("anagrp");
      const dpoSelect = F.byId("depo");
      if (!anaSelect || !dpoSelect) return;

      anaSelect.innerHTML = "";
      dpoSelect.innerHTML = "";

      const fillSelect = (selectEl, arr, key) => {
        selectEl.add(new Option("", ""));
        selectEl.add(new Option("Bos Olanlar", "Bos Olanlar"));
        const seen = new Set();
        for (const it of arr) {
          const v = (it[key] || "").trim();
          if (!v || seen.has(v)) continue;
          seen.add(v);
          selectEl.add(new Option(v, v));
        }
      };

      fillSelect(anaSelect, ana, "ANA_GRUP");
      fillSelect(dpoSelect, dpo, "DEPO");

    } catch (error) {
      F.errShow(`Bir hata oluştu: ${error?.message || error}`);
    } finally {
      F.cursor("default");
    }
  };

  /* =========================
     DOWNLOAD (jQuery yok)
     - buton id: fatrapreportFormat
     ========================= */
  F.fatrapdownloadReport = async function () {
    F.errClear();

    F.cursor("wait");
    F.disableBtn("fatrapreportFormat", true, "İşleniyor...", "Excel Indir");
    const filtreBtn = F.byId("fatrapyenileButton");
    if (filtreBtn) filtreBtn.disabled = true;

    const table = document.querySelector("#main-table");
    if (!table) {
      F.errShow("Tablo bulunamadı.");
      F.cursor("default");
      return;
    }

    const headers = [];
    const rows = [];

    table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));

    // details-row satırlarını mail/excel’e katma
    table.querySelectorAll("tbody tr").forEach(tr => {
      if (tr.classList.contains("details-row")) return;

      const rowData = {};
      let isEmpty = true;

      tr.querySelectorAll("td").forEach((td, index) => {
        const value = td.innerText.trim();
        if (value !== "") isEmpty = false;
        rowData[headers[index]] = value;
      });

      if (!isEmpty) rows.push(rowData);
    });

    try {
      const response = await fetchWithSessionCheckForDownload("stok/fatrap_download", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(rows),
      });

      if (response?.blob) {
        const disposition = response.headers.get("Content-Disposition");
        const fileName = disposition?.match(/filename="(.+)"/)?.[1] || "rapor.xlsx";

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
      F.errShow(error?.message || String(error));
    } finally {
      F.disableBtn("fatrapreportFormat", false, "İşleniyor...", "Excel Indir");
      if (filtreBtn) filtreBtn.disabled = false;
      F.cursor("default");
    }
  };

  /* =========================
     MAIL
     - buton id: fatrapmailButton
     ========================= */
  F.fatrapmailAt = function () {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    F.cursor("wait");

    const table = document.querySelector("#main-table");
    if (!table) {
      F.errShow("Tablo bulunamadı.");
      F.cursor("default");
      return;
    }

    const headers = [];
    const rows = [];

    table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));

    table.querySelectorAll("tbody tr").forEach(tr => {
      if (tr.classList.contains("details-row")) return;

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

    const degerler = "fatrapor";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);

    F.cursor("default");
  };

})();
