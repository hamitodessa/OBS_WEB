window.OBS = window.OBS || {};
OBS.KUR = OBS.KUR || {};

OBS.KUR.byId = (id) => document.getElementById(id);

OBS.KUR.showError = function (msg) {
  const e = OBS.KUR.byId("errorDiv");
  if (!e) return;
  e.style.display = "block";
  e.innerText = msg || "Beklenmeyen bir hata oluştu.";
};

OBS.KUR.clearError = function () {
  const e = OBS.KUR.byId("errorDiv");
  if (!e) return;
  e.style.display = "none";
  e.innerText = "";
};

OBS.KUR.setBusy = function (yes) {
  document.body.style.cursor = yes ? "wait" : "default";
};

OBS.KUR.parseLocaleNumber = function (val) {
	if (val == null) return 0;

	  let s = String(val).trim();
	  if (!s) return 0;

	  const hasDot = s.includes(".");
	  const hasComma = s.includes(",");

	  if (hasDot && hasComma) {
	    if (s.lastIndexOf(",") > s.lastIndexOf(".")) {
	      s = s.replace(/\./g, "").replace(",", ".");
	    } else {
	      s = s.replace(/,/g, "");
	    }
	  } else if (hasComma) {
	    s = s.replace(",", ".");
	  }
	  const n = Number(s);
	  return Number.isFinite(n) ? n : 0;
};

OBS.KUR.formatNumber4 = function (value) {
  if (value == null || value === "") return "";
  const n = typeof value === "number" ? value : OBS.KUR.parseLocaleNumber(value);
  return n.toLocaleString(undefined, { minimumFractionDigits: 4, maximumFractionDigits: 4 });
};

OBS.KUR.setFormValuesFromCells = function (cells) {
  OBS.KUR.byId("doviz_tur").value = (cells[0]?.textContent || "").trim();
  OBS.KUR.byId("ma").value = (cells[1]?.textContent || "").trim();
  OBS.KUR.byId("ms").value = (cells[2]?.textContent || "").trim();
  OBS.KUR.byId("sa").value = (cells[3]?.textContent || "").trim();
  OBS.KUR.byId("ss").value = (cells[4]?.textContent || "").trim();
  OBS.KUR.byId("ba").value = (cells[5]?.textContent || "").trim();
  OBS.KUR.byId("bs").value = (cells[6]?.textContent || "").trim();
};

OBS.KUR.setZeros = function () {
  ["ma", "ms", "sa", "ss", "ba", "bs"].forEach(id => {
    const el = OBS.KUR.byId(id);
    if (el) el.value = OBS.KUR.formatNumber4(0);
  });
};

OBS.KUR.kuroku = async function () {
  const tarih = OBS.KUR.byId("tarih")?.value;
  OBS.KUR.clearError();

  if (!tarih) {
    OBS.KUR.showError("Lütfen tüm alanları doldurun.");
    return;
  }

  const tableBody = OBS.KUR.byId("tableBody");
  if (tableBody) tableBody.innerHTML = "";

  OBS.KUR.setBusy(true);
  try {
    const data = await fetchWithSessionCheck("kur/kurgunluk", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ tarih }),
    });

    if (data?.errorMessage) throw new Error(data.errorMessage);

    if (!data?.success) {
      OBS.KUR.showError("İşlem başarısız. Lütfen tekrar deneyin.");
      return;
    }

    if (!Array.isArray(data.data) || data.data.length === 0) {
      OBS.KUR.setZeros();
      return;
    }

    data.data.forEach(item => {
      const row = document.createElement("tr");
      row.classList.add("table-row-height");
      row.innerHTML = `
        <td>${item.Kur || ""}</td>
        <td class="double-column">${OBS.KUR.formatNumber4(item.MA)}</td>
        <td class="double-column">${OBS.KUR.formatNumber4(item.MS)}</td>
        <td class="double-column">${OBS.KUR.formatNumber4(item.SA)}</td>
        <td class="double-column">${OBS.KUR.formatNumber4(item.SS)}</td>
        <td class="double-column">${OBS.KUR.formatNumber4(item.BA)}</td>
        <td class="double-column">${OBS.KUR.formatNumber4(item.BS)}</td>
      `;
      row.addEventListener("click", () => OBS.KUR.setFormValuesFromCells(row.cells));
      tableBody.appendChild(row);
    });

    // ilk satırı forma bas
    const firstRow = tableBody.rows[0];
    if (firstRow) OBS.KUR.setFormValuesFromCells(firstRow.cells);

  } catch (e) {
    OBS.KUR.showError(`Beklenmeyen bir hata oluştu: ${e?.message || e}`);
  } finally {
    OBS.KUR.setBusy(false);
  }
};

OBS.KUR.tarihGeri = function () {
  const el = OBS.KUR.byId("tarih");
  if (!el?.value) return;
  const d = new Date(el.value);
  d.setDate(d.getDate() - 1);
  el.value = d.toISOString().split("T")[0];
  OBS.KUR.kuroku();
};

OBS.KUR.tarihIleri = function () {
  const el = OBS.KUR.byId("tarih");
  if (!el?.value) return;
  const d = new Date(el.value);
  d.setDate(d.getDate() + 1);
  el.value = d.toISOString().split("T")[0];
  OBS.KUR.kuroku();
};

OBS.KUR.kurSatirOku = function () {
  const kur_turu = (OBS.KUR.byId("doviz_tur")?.value || "").trim();
  const tableBody = OBS.KUR.byId("tableBody");
  if (!tableBody) return;

  let matchedRow = null;
  for (let i = 0; i < tableBody.rows.length; i++) {
    const row = tableBody.rows[i];
    const first = (row.cells[0]?.textContent || "").trim();
    if (first === kur_turu) { matchedRow = row; break; }
  }

  if (matchedRow) OBS.KUR.setFormValuesFromCells(matchedRow.cells);
  else OBS.KUR.setZeros();
};

OBS.KUR.kurKayit = async function () {
  OBS.KUR.clearError();

	console.info(OBS.KUR.byId("ma").value);
	console.info(OBS.KUR.parseLocaleNumber(OBS.KUR.byId("ma")?.value));
  const dto = {
    dvz_turu: OBS.KUR.byId("doviz_tur")?.value || "",
    tar: OBS.KUR.byId("tarih")?.value || "",
    ma: OBS.KUR.parseLocaleNumber(OBS.KUR.byId("ma")?.value),
    ms: OBS.KUR.parseLocaleNumber(OBS.KUR.byId("ms")?.value),
    sa: OBS.KUR.parseLocaleNumber(OBS.KUR.byId("sa")?.value),
    ss: OBS.KUR.parseLocaleNumber(OBS.KUR.byId("ss")?.value),
    ba: OBS.KUR.parseLocaleNumber(OBS.KUR.byId("ba")?.value),
    bs: OBS.KUR.parseLocaleNumber(OBS.KUR.byId("bs")?.value)
  };

  const btn = OBS.KUR.byId("kaydetButton");
  OBS.KUR.setBusy(true);
  if (btn) { btn.disabled = true; btn.textContent = "İşleniyor..."; }

  try {
    const res = await fetchWithSessionCheck("kur/kurkayit", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dto)
    });
    if (res?.errorMessage) throw new Error(res.errorMessage);

    // ✅ ANA SİSTEM SAYFA YÜKLEYİCİ
    if (typeof window.sayfaYukle === "function") {
      window.sayfaYukle("/kur/kurgiris");
    } else {
      // fallback
      OBS.KUR.kuroku();
    }

  } catch (e) {
    OBS.KUR.showError(e?.message || "Bir hata oluştu.");
  } finally {
    OBS.KUR.setBusy(false);
    if (btn) { btn.disabled = false; btn.textContent = "Kaydet"; }
  }
};

OBS.KUR.kurYoket = async function () {
  OBS.KUR.clearError();

  const dto = {
    dvz_turu: OBS.KUR.byId("doviz_tur")?.value || "",
    tar: OBS.KUR.byId("tarih")?.value || ""
  };

  const ok = confirm("Bu kuru silmek istediğinize emin misiniz?");
  if (!ok) return;

  const btn = OBS.KUR.byId("silButton");
  OBS.KUR.setBusy(true);
  if (btn) { btn.disabled = true; btn.textContent = "Siliniyor..."; }

  try {
    const res = await fetchWithSessionCheck("kur/kurSil", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dto)
    });
    if (res?.errorMessage) throw new Error(res.errorMessage);

    if (typeof window.sayfaYukle === "function") {
      window.sayfaYukle("/kur/kurgiris");
    } else {
      OBS.KUR.kuroku();
    }

  } catch (e) {
    OBS.KUR.showError(e?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    OBS.KUR.setBusy(false);
    if (btn) { btn.disabled = false; btn.textContent = "Sil"; }
  }
};

OBS.KUR.merkezOku = async function () {
  OBS.KUR.clearError();

  const tarih = OBS.KUR.byId("tarih")?.value || "";
  const kurcins = OBS.KUR.byId("doviz_tur")?.value || "";

  OBS.KUR.setBusy(true);
  try {
    const res = await fetchWithSessionCheck("kur/merkezoku", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ tarih, kurcins }),
    });

    if (res?.errorMessage) throw new Error(res.errorMessage);

    const ma = OBS.KUR.byId("ma");
    const ms = OBS.KUR.byId("ms");
    if (ma) ma.value = OBS.KUR.formatNumber4(res.ma || 0);
    if (ms) ms.value = OBS.KUR.formatNumber4(res.ms || 0);

  } catch (e) {
    OBS.KUR.showError(e?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    OBS.KUR.setBusy(false);
  }
};

// ✅ Sayfa DOM'a basıldıktan sonra bağlanacak init
OBS.KUR.init = function () {
  // (İstersen burada butonlara onclick yerine event bağlarız.)
  // Şimdilik mevcut onclick’ler varsa sorun değil.

  // Sayfa açılınca otomatik oku istiyorsan:
  OBS.KUR.kuroku();
};

// Global köprüler (HTML onclick’ler bozulmasın diye)
window.kuroku = () => OBS.KUR.kuroku();
window.tarihGeri = () => OBS.KUR.tarihGeri();
window.tarihIleri = () => OBS.KUR.tarihIleri();
window.kurSatirOku = () => OBS.KUR.kurSatirOku();
window.kurKayit = () => OBS.KUR.kurKayit();
window.kurYoket = () => OBS.KUR.kurYoket();
window.merkezOku = () => OBS.KUR.merkezOku();
