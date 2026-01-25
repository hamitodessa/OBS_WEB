/* =========================
   OBS Namespace
   ========================= */
window.OBS = window.OBS || {};
OBS.IRSRAPOR = OBS.IRSRAPOR || {};

(() => {
  const I = OBS.IRSRAPOR;

  /* ---------- state ---------- */
  I.currentPage = 0;
  I.totalPages = 0;
  I.pageSize = 250;

  /* ---------- helpers ---------- */
  I.byId = (id) => document.getElementById(id);

  I.errClear = () => {
    const e = I.byId("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
  };

  I.errShow = (msg) => {
    const e = I.byId("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Beklenmeyen bir hata oluştu.";
  };

  I.cursor = (mode) => { document.body.style.cursor = mode || "default"; };

  I.disableBtn = (id, yes, textYes, textNo) => {
    const b = I.byId(id);
    if (!b) return;
    b.disabled = !!yes;
    if (yes && textYes != null) b.innerText = textYes;
    if (!yes && textNo != null) b.innerText = textNo;
  };

  /* =========================
     ANA GRUP -> ALT GRUP
     ========================= */
  I.anagrpChanged = async function (anagrpElement) {
    const anagrup = anagrpElement?.value || "";
    const selectElement = I.byId("altgrp");
    if (!selectElement) return;

    selectElement.innerHTML = "";

    if (anagrup === "") {
      selectElement.disabled = true;
      return;
    }

    I.cursor("wait");
    I.errClear();

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
      I.errShow(error?.message);
    } finally {
      I.cursor("default");
    }
  };

  /* =========================
     PAGINATION
     ========================= */
  I.ilksayfa = () => I.irsfetchTableData(0);

  I.oncekisayfa = () => {
    if (I.currentPage > 0) I.irsfetchTableData(I.currentPage - 1);
  };

  I.sonrakisayfa = () => {
    if (I.currentPage < I.totalPages - 1) I.irsfetchTableData(I.currentPage + 1);
  };

  I.sonsayfa = () => I.irsfetchTableData(Math.max(I.totalPages - 1, 0));

  /* =========================
     DTO (jQuery yok)
     ========================= */
  I.getirsraporDTO = function () {
    const hf = I.byId("irsrapBilgi");
    const hiddenFieldValue = hf ? (hf.value || "") : "";
    const p = hiddenFieldValue.split(",");

    return {
      irsno1: p[0],
      irsno2: p[1],
      anagrp: p[2],
      tar1: p[3],
      tar2: p[4],
      altgrp: p[5],
      ckod1: p[6],
      ckod2: p[7],
      turu: p[8],
      ukod1: p[9],
      ukod2: p[10],
      okod1: p[11],
      okod2: p[12],
      dvz1: p[13],
      dvz2: p[14],
      fatno1: p[15],
      fatno2: p[16],
      adr1: p[17],
      adr2: p[18]
    };
  };

  /* =========================
     PAGE COUNT
     ========================= */
  I.toplampagesize = async function () {
    try {
      I.errClear();

      const dto = I.getirsraporDTO();
      const response = await fetchWithSessionCheck("stok/irsdoldursize", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
      });

      const totalRecords = response?.totalRecords || 0;
      I.totalPages = Math.ceil(totalRecords / I.pageSize);
    } catch (error) {
      I.errShow((error && error.message) ? error.message : String(error));
      I.cursor("default");
    }
  };

  I.irsdoldur = async function () {
    I.cursor("wait");
    await I.toplampagesize();         // ✅ await
    await I.irsfetchTableData(0);
  };

  /* =========================
     TABLE DATA
     ========================= */
  I.irsfetchTableData = async function (page) {
    const dto = I.getirsraporDTO();
    dto.page = page;
    dto.pageSize = I.pageSize;
    I.currentPage = page;

    I.errClear();
    I.cursor("wait");
    I.disableBtn("irsrapyenileButton", true, "İşleniyor...", "Yenile");

    const mainTableBody = I.byId("mainTableBody");
    if (mainTableBody) mainTableBody.innerHTML = "";
    I.clearTfoot();

    try {
      const response = await fetchWithSessionCheck("stok/irsrapdoldur", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const data = response;

      const sqlHeaders = ["", "IRSALIYE NO", "HAREKET", "TARIH", "CARI_HESAP", "ADRES", "BIRIM", "MIKTAR", "TUTAR"];
      I.updateTableHeaders(sqlHeaders);

      const thCount = document.querySelectorAll("#main-table thead th").length;

      let totalmiktar = 0;
      let totaltutar = 0;

      (data.data || []).forEach((rowData) => {
        const row = document.createElement("tr");
        row.classList.add("expandable", "table-row-height");

        row.innerHTML = `
          <td><span class="toggle-button">+</span></td>
          <td>${rowData.Irsaliye_No || ""}</td>
          <td>${rowData.Hareket || ""}</td>
          <td>${formatDate(rowData.Tarih)}</td>
          <td>${rowData.Cari_Hesap_Kodu || ""}</td>
          <td>${rowData.Adres_Firma || ""}</td>
          <td>${rowData.Birim || ""}</td>
          <td class="double-column">${formatNumber3(rowData.Miktar)}</td>
          <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
        `;

        totalmiktar += (rowData.Miktar || 0);
        totaltutar += (rowData.Tutar || 0);

        mainTableBody.appendChild(row);

        const detailsRow = document.createElement("tr");
        detailsRow.className = "details-row";
        detailsRow.style.display = "none";
        detailsRow.innerHTML = `<td colspan="${thCount}"></td>`;
        mainTableBody.appendChild(detailsRow);

        // satıra tıklayınca sadece seçili olsun
        row.addEventListener("click", () => {
          document.querySelectorAll("#main-table tbody tr.selected").forEach(r => r.classList.remove("selected"));
          row.classList.add("selected");
        });

        const toggle = row.querySelector(".toggle-button");
        if (!toggle) return;

        toggle.addEventListener("click", async (e) => {
          e.stopPropagation();

          const isOpen = (detailsRow.style.display === "table-row");

          // tek satır açık kalsın
          document.querySelectorAll("#main-table tr.details-row").forEach(r => r.style.display = "none");
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

          // colspan garanti
          detailsRow.innerHTML = `<td colspan="${thCount}"></td>`;

          if (detailsRow.dataset.loaded === "1") return;

          I.cursor("wait");
          try {
            const detResp = await I.fetchDetails(rowData.Irsaliye_No, rowData.Hareket);
            const det = (detResp && detResp.data) ? detResp.data : [];

            let html = `
              <div class="details-wrap">
                <table class="t-details">
                  <thead>
                    <tr>
                      <th>Kodu</th>
                      <th>Adi</th>
                      <th class="double-column">Miktar</th>
                      <th>Birim</th>
                      <th class="double-column">Fiat</th>
                      <th class="double-column">Tutar</th>
                      <th class="double-column">Kdv</th>
                      <th>Doviz</th>
                      <th class="double-column">Iskonto</th>
                      <th>Ana Grup</th>
                      <th>Alt Grup</th>
                      <th>Ozel Kod</th>
                      <th>Fatura No</th>
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
                  <td class="double-column">${formatNumber2(item.Tutar)}</td>
                  <td class="double-column">${formatNumber2(item.Kdv)}</td>
                  <td>${item.Doviz || ""}</td>
                  <td class="double-column">${formatNumber2(item.Iskonto)}</td>
                  <td>${item.Ana_Grup || ""}</td>
                  <td>${item.Alt_Grup || ""}</td>
                  <td>${item.Ozel_Kod || ""}</td>
                  <td>${item.Fatura_No || ""}</td>
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
            I.cursor("default");
          }
        });
      });

      // TOPLAM
      const t7 = I.byId("toplam-7");
      const t8 = I.byId("toplam-8");
      if (t7) t7.innerText = formatNumber3(totalmiktar);
      if (t8) t8.innerText = formatNumber2(totaltutar);

    } catch (err) {
      I.errShow((err && err.message) ? err.message : String(err));
    } finally {
      I.disableBtn("irsrapyenileButton", false, "İşleniyor...", "Yenile");
      I.cursor("default");
    }
  };

  /* =========================
     TFOOT
     ========================= */
  I.clearTfoot = function () {
    const table = document.querySelector("#main-table");
    const tfoot = table?.querySelector("tfoot");
    if (!tfoot) return;
    tfoot.querySelectorAll("th").forEach(th => th.textContent = "");
  };

  /* =========================
     DETAILS FETCH
     ========================= */
  I.fetchDetails = async function (evrakNo, cins) {
    try {
      const gircik = (cins === "Alis") ? "G" : "C";
      const response = await fetchWithSessionCheck("stok/irsdetay", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ evrakNo, gircik }),
      });
      return response;
    } catch (error) {
      throw error;
    }
  };

  /* =========================
     HEADERS
     ========================= */
  I._ensureTfoot = function () {
    const table = document.querySelector("#main-table");
    if (!table) return null;
    let tfoot = table.querySelector("tfoot");
    if (!tfoot) {
      tfoot = document.createElement("tfoot");
      table.appendChild(tfoot);
    }
    return tfoot;
  };

  I.updateTableHeaders = function (headers) {
    const thead = document.querySelector("#main-table thead");
    const tfoot = I._ensureTfoot();
    const table = document.querySelector("#main-table");
    if (!thead || !tfoot || !table) return;

    thead.innerHTML = "";
    const trHead = document.createElement("tr");
    trHead.classList.add("thead-dark");

    headers.forEach((header, index) => {
      const th = document.createElement("th");
      th.textContent = header;
      if (index >= headers.length - 2) th.classList.add("double-column"); // son 2 sütun
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

  /* =========================
     MODAL OPEN (jQuery yok)
     ========================= */
  I.openirsrapModal = async function (modalRef) {
    const modalEl =
      (typeof modalRef === "string")
        ? (modalRef.startsWith("#") ? document.querySelector(modalRef) : I.byId(modalRef))
        : modalRef;

    if (!modalEl) return;

    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    modal.show();

    I.cursor("wait");
    try {
      const response = await fetchWithSessionCheck("stok/anadepo", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const anaSelect = I.byId("anagrp");
      if (!anaSelect) return;

      anaSelect.innerHTML = "";
      anaSelect.add(new Option("", ""));
      anaSelect.add(new Option("Bos Olanlar", "Bos Olanlar"));

      const seen = new Set();
      for (const it of (response.anaKodlari || [])) {
        const v = (it.ANA_GRUP || "").trim();
        if (!v || seen.has(v)) continue;
        seen.add(v);
        anaSelect.add(new Option(v, v));
      }

    } catch (error) {
      I.errShow(`Bir hata oluştu: ${error.message}`);
    } finally {
      I.cursor("default");
    }
  };

  /* =========================
     DOWNLOAD (jQuery yok)
     ========================= */
  I.irsrapdownloadReport = async function () {
    I.errClear();

    I.cursor("wait");
    I.disableBtn("indirButton", true, "İşleniyor...", "Rapor İndir");

    const yenile = I.byId("yenileButton");
    if (yenile) yenile.disabled = true;

    const table = document.querySelector("#main-table");
    const headers = [];
    const rows = [];

    table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));

    // ✅ details-row satırlarını atla
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
      const response = await fetchWithSessionCheckForDownload("stok/irsrap_download", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(rows),
      });

      if (response?.blob) {
        const disposition = response.headers.get("Content-Disposition");
        const fileName = disposition?.match(/filename="(.+)"/)?.[1] || "irsrapor.xlsx";

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
      I.errShow(error?.message);
    } finally {
      I.disableBtn("indirButton", false, "İşleniyor...", "Rapor İndir");
      if (yenile) yenile.disabled = false;
      I.cursor("default");
    }
  };

  /* =========================
     MAIL (zaten jQuery yok)
     ========================= */
  I.irsrapmailAt = function () {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    I.cursor("wait");

    const table = document.querySelector("#main-table");
    const headers = [];
    const rows = [];

    table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));

    // ✅ details-row satırlarını atla
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

    const degerler = "irsrapor";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);

    I.cursor("default");
  };

})();
