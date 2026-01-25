/* =========================
   OBS Namespace
   ========================= */
window.OBS = window.OBS || {};
OBS.IMARAPOR = OBS.IMARAPOR || {};

(() => {
  const I = OBS.IMARAPOR;

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

  I.cursor = (c) => { document.body.style.cursor = c || "default"; };

  I.disableBtn = (id, yes, textYes, textNo) => {
    const b = I.byId(id);
    if (!b) return;
    b.disabled = !!yes;
    if (yes && textYes != null) b.innerText = textYes;
    if (!yes && textNo != null) b.innerText = textNo;
  };

  I.val = (id) => (I.byId(id)?.value ?? "");

  /* =========================
     ANA GRUP -> ALT GRUP (parametreli)
     usage:
       onchange="OBS.IMARAPOR.anagrpChanged(this,'altgrp')"
       onchange="OBS.IMARAPOR.anagrpChanged(this,'uraltgrp')"
     ========================= */
  I.anagrpChanged = async function (anagrpElement, altgrpElementId) {
    const anagrup = anagrpElement?.value || "";
    const selectElement = I.byId(altgrpElementId);
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
        const opt = document.createElement("option");
        opt.value = kod.ALT_GRUP;
        opt.textContent = kod.ALT_GRUP;
        selectElement.appendChild(opt);
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
     MODAL OPEN (jQuery yok)
     ========================= */
  I.openimarapModal = async function (modalRef) {
    const modalEl =
      (typeof modalRef === "string")
        ? (modalRef.startsWith("#") ? document.querySelector(modalRef) : I.byId(modalRef))
        : modalRef;

    if (!modalEl) return;

    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    modal.show();

    I.cursor("wait");
    I.errClear();

    try {
      const response = await fetchWithSessionCheck("stok/anadepo", {
        method: "POST",
        headers: { "Content-Type": "application/json" }
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const ana = response.anaKodlari || [];
      const dpo = response.depoKodlari || [];

      const uranaSelect = I.byId("uranagrp");
      const anaSelect   = I.byId("anagrp");
      const dpoSelect   = I.byId("depo");

      const fillSelect = (sel, arr, key) => {
        if (!sel) return;
        sel.innerHTML = "";

        // boş + "Bos Olanlar" başa
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
      I.errShow(`Bir hata oluştu: ${error.message}`);
    } finally {
      I.cursor("default");
    }
  };

  /* =========================
     DTO (jQuery yok) -> hidden input #imarapBilgi
     ========================= */
  I.getimaraporDTO = function () {
    const hf = I.byId("imarapBilgi");
    const v = hf ? (hf.value || "") : "";
    const p = v.split(",");

    return {
      evrno1: p[0],
      evrno2: p[1],
      uranagrp: p[2],
      tar1: p[3],
      tar2: p[4],
      uraltgrp: p[5],
      bkod1: p[6],
      bkod2: p[7],
      depo: p[8],
      ukod1: p[9],
      ukod2: p[10],
      rec1: p[11],
      rec2: p[12],
      anagrp: p[13],
      altgrp: p[14],
    };
  };

  /* =========================
     TABLE EXTRACT (thead + tbody)
     ========================= */
  I.extractTableData = function (tableId) {
    const table = document.querySelector(`#${tableId}`);
    if (!table) return [];

    const headers = [];
    const rows = [];

    table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));

    table.querySelectorAll("tbody tr").forEach(tr => {
      const rowData = {};
      let nonEmpty = 0;

      tr.querySelectorAll("td").forEach((td, idx) => {
        const value = td.innerText.trim();
        if (value !== "") nonEmpty++;
        rowData[headers[idx]] = value;
      });

      if (nonEmpty > 0) rows.push(rowData);
    });

    return rows;
  };

  /* =========================
     FETCH + TABLE DOLDUR
     ========================= */
  I.imafetchTableData = async function () {
    const dto = I.getimaraporDTO();

    I.errClear();

    const tableBody = I.byId("tableBody");
    if (tableBody) tableBody.innerHTML = "";

    const totalMiktarCell  = I.byId("totalMiktar");
    const totalAgirlikCell = I.byId("totalAgirlik");
    if (totalMiktarCell)  totalMiktarCell.textContent  = "0.000";
    if (totalAgirlikCell) totalAgirlikCell.textContent = "0.000";

    I.cursor("wait");
    I.disableBtn("imarapyenileButton", true, "İşleniyor...", "Yenile");

    try {
      const response = await fetchWithSessionCheck("stok/imarapdoldur", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      let totalmiktar = 0;
      let totalagirlik = 0;

      (response.data || []).forEach(item => {
        const row = document.createElement("tr");
        row.classList.add("table-row-height");

        row.innerHTML = `
          <td>${item.Evrak_No || ""}</td>
          <td>${formatDate(item.Tarih)}</td>
          <td>${item.Urun_Kodu || ""}</td>
          <td>${item.Adi || ""}</td>
          <td class="double-column">${formatNumber3(item.Miktar)}</td>
          <td>${item.Birim || ""}</td>
          <td class="double-column">${formatNumber3(item.Agirlik)}</td>
          <td>${item.Depo || ""}</td>
          <td>${item.Ana_Grup || ""}</td>
          <td>${item.Alt_Grup || ""}</td>
          <td>${item.Barkod || ""}</td>
          <td>${item.Recete || ""}</td>
          <td>${item.USER || ""}</td>
        `;

        totalmiktar  += (item.Miktar || 0);
        totalagirlik += (item.Agirlik || 0);

        tableBody.appendChild(row);
      });

      if (totalMiktarCell) {
        totalMiktarCell.textContent = totalmiktar.toLocaleString(undefined, {
          minimumFractionDigits: 3, maximumFractionDigits: 3
        });
      }
      if (totalAgirlikCell) {
        totalAgirlikCell.textContent = totalagirlik.toLocaleString(undefined, {
          minimumFractionDigits: 3, maximumFractionDigits: 3
        });
      }

    } catch (error) {
      I.errShow(error?.message);
    } finally {
      I.disableBtn("imarapyenileButton", false, "İşleniyor...", "Yenile");
      I.cursor("default");
    }
  };

  /* =========================
     DOWNLOAD (jQuery yok)
     ========================= */
  I.imarapdownloadReport = async function () {
    I.errClear();

    I.cursor("wait");
    I.disableBtn("imarapreportFormatbutton", true, "İşleniyor...", "Rapor İndir");

    const yenile = I.byId("imarapyenileButton");
    if (yenile) yenile.disabled = true;

    const rows = I.extractTableData("main-table");

    try {
      const response = await fetchWithSessionCheckForDownload("stok/imarap_download", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(rows),
      });

      if (response?.blob) {
        const disposition = response.headers.get("Content-Disposition");
        const fileName = disposition?.match(/filename="(.+)"/)?.[1] || "imarapor.xlsx";

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
      I.disableBtn("imarapreportFormatbutton", false, "İşleniyor...", "Rapor İndir");
      if (yenile) yenile.disabled = false;
      I.cursor("default");
    }
  };

  /* =========================
     MAIL (jQuery yok)
     ========================= */
  I.imarapmailAt = function () {
    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    I.cursor("wait");

    const rows = I.extractTableData("main-table");
    localStorage.setItem("tableData", JSON.stringify({ rows }));

    const degerler = "imarapor";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);

    I.cursor("default");
  };

})();
