/* =========================
   OBS Namespace
   ========================= */
window.OBS = window.OBS || {};
OBS.DEGISKENLER = OBS.DEGISKENLER || {};

(() => {
  const D = OBS.DEGISKENLER;

  /* ---------- helpers ---------- */
  D.byId = (id) => document.getElementById(id);

  D.cursor = (c) => { document.body.style.cursor = c || "default"; };

  D.errClear = () => {
    const e = D.byId("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
  };

  D.errShow = (msg) => {
    const e = D.byId("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Beklenmeyen bir hata oluştu.";
  };

  D.clearInputs = () => {
    const aciklama = D.byId("aciklama");
    const arama = D.byId("arama");
    const idacik = D.byId("idacik");
    if (aciklama) aciklama.value = "";
    if (arama) arama.value = "";
    if (idacik) idacik.value = "";
  };

  D.selectFirstRow = (tableBody) => {
    if (!tableBody) return;
    const firstRow = tableBody.rows?.[0];
    if (!firstRow) return;
    const cells = firstRow.cells;
    if (!cells || cells.length < 2) return;
    D.selectValue(cells[1].textContent.trim(), cells[0].textContent.trim());
  };

  /* =========================================================
     INIT
     ========================================================= */
  D.init = function () {
    const arama = D.byId("arama");
    if (arama) arama.value = "";
    D.anagrpdoldur();
  };

  /* =========================================================
     GRUP DEĞİŞTİ
     ========================================================= */
  D.degiskenchange = async function (grpElement) {
    D.clearInputs();

    const grup = grpElement?.value || "";
    const altgrpdiv = D.byId("altgrpdiv");

    if (grup === "altgrp") {
      if (altgrpdiv) altgrpdiv.style.display = "grid";
      await D.altgrpdoldur();
    } else {
      if (altgrpdiv) altgrpdiv.style.display = "none";
    }

    if (grup === "anagrp") return D.anagrpdoldur();
    if (grup === "mensei") return D.menseidoldur();
    if (grup === "depo") return D.depodoldur();
    if (grup === "oz1") return D.oz1doldur();
    if (grup === "oz2") return D.oz2doldur();
  };

  /* =========================================================
     DOLDUR HELPER (tek yerden)
     labelKey: gösterilecek alan
     idKey: gizli id alanı
     endpoint: GET
     listKey: response içindeki array adı
     ========================================================= */
  D._fillTable = async function ({ endpoint, listKey, labelKey, idKey }) {
    D.errClear();
    D.cursor("wait");

    try {
      const response = await fetchWithSessionCheck(endpoint);
      if (response?.errorMessage) throw new Error(response.errorMessage);

      const data = response[listKey] || [];
      const tableBody = D.byId("degiskenTableBody");
      if (!tableBody) return;

      tableBody.innerHTML = "";
      tableBody.classList.add("table-row-height");

      if (!data.length) return;

      data.forEach((row) => {
        const tr = document.createElement("tr");
        const idVal = row[idKey] ?? "";
        const labelVal = row[labelKey] ?? "";

        tr.innerHTML = `
          <td style="display:none;">${idVal}</td>
          <td>${labelVal}</td>
        `;
        tr.onclick = () => D.selectValue(String(labelVal), String(idVal));
        tableBody.appendChild(tr);
      });

      D.selectFirstRow(tableBody);

    } catch (error) {
      D.errShow(`Bir hata oluştu: ${error.message}`);
    } finally {
      D.cursor("default");
    }
  };

  /* =========================================================
     ANAGRUP / MENSEI / DEPO / OZ1 / OZ2
     ========================================================= */
  D.anagrpdoldur = () => D._fillTable({
    endpoint: "stok/anagrpOku",
    listKey: "anagrp",
    labelKey: "ANA_GRUP",
    idKey: "KOD",
  });

  D.menseidoldur = () => D._fillTable({
    endpoint: "stok/menseiOku",
    listKey: "mensei",
    labelKey: "MENSEI",
    idKey: "KOD",
  });

  D.depodoldur = () => D._fillTable({
    endpoint: "stok/depoOku",
    listKey: "depo",
    labelKey: "DEPO",
    idKey: "KOD",
  });

  D.oz1doldur = () => D._fillTable({
    endpoint: "stok/oz1Oku",
    listKey: "oz1",
    labelKey: "OZEL_KOD_1",
    idKey: "KOD",
  });

  D.oz2doldur = () => D._fillTable({
    endpoint: "stok/oz2Oku",
    listKey: "oz2",
    labelKey: "OZEL_KOD_2",
    idKey: "KOD",
  });

  /* =========================================================
     ALT GRUP DOLDUR (POST + anagrup)
     ========================================================= */
  D.altgrpdoldur = async function () {
    const arama = D.byId("arama");
    if (arama) arama.value = "";

    const anagrup = (D.byId("altgrpAna")?.value || "");
    D.errClear();
    D.cursor("wait");

    try {
      const response = await fetchWithSessionCheck("stok/altgrupdeg", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ anagrup }),
      });

      if (response?.errorMessage) throw new Error(response.errorMessage);

      const data = response.altKodlari || [];
      const tableBody = D.byId("degiskenTableBody");
      if (!tableBody) return;

      tableBody.innerHTML = "";
      tableBody.classList.add("table-row-height");

      const filtered = data.filter(r => (r.ALT_GRUP || "").trim() !== "");
      if (!filtered.length) return;

      filtered.forEach((row) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td style="display:none;">${row.ALID_Y || ""}</td>
          <td>${row.ALT_GRUP || ""}</td>
        `;
        tr.onclick = () => D.selectValue(row.ALT_GRUP, row.ALID_Y);
        tableBody.appendChild(tr);
      });

      D.selectFirstRow(tableBody);

    } catch (error) {
      D.errShow(`Bir hata oluştu: ${error.message}`);
    } finally {
      D.cursor("default");
    }
  };

  /* =========================================================
     FILTER
     ========================================================= */
  D.filterTable = function () {
    const searchValue = (D.byId("arama")?.value || "").toLowerCase();
    const rows = document.querySelectorAll("#degiskenTable tbody tr");

    rows.forEach((row) => {
      const rowText = Array.from(row.cells)
        .map((cell) => cell.textContent.toLowerCase())
        .join(" ");
      row.style.display = rowText.includes(searchValue) ? "" : "none";
    });
  };

  /* =========================================================
     SELECT / YENİ
     ========================================================= */
  D.selectValue = function (selectedaciklama, selectedid) {
    const inputElement = D.byId("aciklama");
    const idacik = D.byId("idacik");
    if (inputElement) inputElement.value = selectedaciklama || "";
    if (idacik) idacik.value = selectedid || "";
  };

  D.degyeni = function () {
    const a = D.byId("aciklama");
    const i = D.byId("idacik");
    if (a) a.value = "";
    if (i) i.value = "";
  };

  /* =========================================================
     KAYIT
     ========================================================= */
  D.degKayit = async function () {
    const aciklama = (D.byId("aciklama")?.value || "").trim();
    const idacik = (D.byId("idacik")?.value || "").trim();
    const degisken = (D.byId("degiskenler")?.value || "").trim();
    const altgrpAna = (D.byId("altgrpAna")?.value || "").trim();

    if (!aciklama) {
      alert("Lütfen açıklama alanlarını doldurun.");
      return;
    }

    D.errClear();

    const saveButton = D.byId("degkaydetButton");
    if (saveButton) { saveButton.textContent = "İşlem yapılıyor..."; saveButton.disabled = true; }
    D.cursor("wait");

    try {
      const response = await fetchWithSessionCheck("stok/degkayit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ aciklama, idacik, degisken, altgrpAna }),
      });

      if (!response) return;
      if (response?.errorMessage) throw new Error(response.errorMessage);

      const inputElement = D.byId("degiskenler");
      await D.degiskenchange(inputElement);

    } catch (error) {
      D.errShow(error?.message || "Bir hata oluştu. Daha sonra tekrar deneyin.");
    } finally {
      D.cursor("default");
      if (saveButton) { saveButton.textContent = "Kaydet"; saveButton.disabled = false; }
    }
  };

  /* =========================================================
     SİL
     ========================================================= */
  D.degYoket = async function () {
    const confirmDelete = confirm(
      "Alt Grup Degisken Silinecek ..?\n" +
      "Silme operasyonu butun dosyayi etkileyecek...\n" +
      "Ilk once Degisken Yenileme Bolumunden degistirip sonra siliniz...."
    );
    if (!confirmDelete) return;

    const aciklama = (D.byId("aciklama")?.value || "").trim();
    const idacik = (D.byId("idacik")?.value || "").trim();
    const degisken = (D.byId("degiskenler")?.value || "").trim();
    const altgrpAna = (D.byId("altgrpAna")?.value || "").trim();

    if (!idacik) {
      alert("Lütfen açıklama alanlarını doldurun.");
      return;
    }

    D.errClear();

    const btn = D.byId("degsilButton");
    if (btn) { btn.textContent = "İşlem yapılıyor..."; btn.disabled = true; }
    D.cursor("wait");

    try {
      const response = await fetchWithSessionCheck("stok/degsil", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ aciklama, idacik, degisken, altgrpAna }),
      });

      if (!response) return;
      if (response?.errorMessage) throw new Error(response.errorMessage);

      const inputElement = D.byId("degiskenler");
      await D.degiskenchange(inputElement);

    } catch (error) {
      D.errShow(error?.message || "Bir hata oluştu. Daha sonra tekrar deneyin.");
    } finally {
      D.cursor("default");
      if (btn) { btn.textContent = "Sil"; btn.disabled = false; }
    }
  };

})();
