/* =========================
   STOK – URETIM (çakışmasız, jQuery yok)
   Namespace: OBS.URETIM
   File: /obs_js_files/stok/uretim.js
   ========================= */
window.OBS ||= {};
OBS.URETIM ||= {};

/* ---------- helpers ---------- */
OBS.URETIM._el = (id) => document.getElementById(id);

OBS.URETIM._showError = (msg) => {
  const e = OBS.URETIM._el("errorDiv");
  if (!e) return;
  e.style.display = "block";
  e.innerText = msg || "Beklenmeyen bir hata oluştu.";
};

OBS.URETIM._hideError = () => {
  const e = OBS.URETIM._el("errorDiv");
  if (!e) return;
  e.style.display = "none";
  e.innerText = "";
};

OBS.URETIM._busy = (yes) => {
  document.body.style.cursor = yes ? "wait" : "default";
};

OBS.URETIM._btnBusy = (btnId, yes, busyText, idleText) => {
  const btn = OBS.URETIM._el(btnId);
  if (!btn) return;
  btn.disabled = !!yes;
  if (busyText && idleText) btn.textContent = yes ? busyText : idleText;
};

/* ---------- state ---------- */
OBS.URETIM.rowCounter = 0;
OBS.URETIM.depolar = [];
OBS.URETIM.urnkodlar = [];

/* ---------- init ---------- */
OBS.URETIM.init = () => {
  OBS.URETIM._hideError();

  // resim input max size kontrol (sayfa varsa)
  const fileInput = OBS.URETIM._el("resim");
  if (fileInput && !fileInput.dataset.bound) {
    fileInput.dataset.bound = "1";
    fileInput.addEventListener("change", (event) => {
      const file = event.target.files?.[0];
      const maxSizeInKB = 500;
      const maxSizeInBytes = maxSizeInKB * 1024;

      if (file && file.size > maxSizeInBytes) {
        OBS.URETIM._showError(`Dosya boyutu ${maxSizeInKB} KB'ı geçemez!`);
        event.target.value = "";
      } else {
        OBS.URETIM._hideError();
      }
    });
  }
	OBS.URETIM.fetchkoddepo();
};

/* =========================================================
   1) KOD+DEPO ÇEK
   ========================================================= */
OBS.URETIM.fetchkoddepo = async () => {
  OBS.URETIM._hideError();
  OBS.URETIM.rowCounter = 0;
  OBS.URETIM.depolar = [];
  OBS.URETIM.urnkodlar = [];

  OBS.URETIM._busy(true);
  try {
    const response = await fetchWithSessionCheck("stok/stkgeturndepo", {
      method: "GET",
      headers: { "Content-Type": "application/json" }
    });
    if (response?.errorMessage) throw new Error(response.errorMessage);

    OBS.URETIM.urnkodlar = response.urnkodlar || [];
    OBS.URETIM.depolar = response.depolar || [];
    OBS.URETIM.initializeRows();
  } catch (err) {
    OBS.URETIM._showError(err?.message);
  } finally {
    OBS.URETIM._busy(false);
  }
};

/* =========================================================
   2) ANA GRUP -> ALT GRUP
   ========================================================= */
OBS.URETIM.anagrpChanged = async (anagrpElement) => {
  const anagrup = anagrpElement?.value || "";
  const selectElement = OBS.URETIM._el("altgrp");
  if (!selectElement) return;

  OBS.URETIM._hideError();
  selectElement.innerHTML = "";

  if (anagrup === "") {
    selectElement.disabled = true;
    return;
  }

  OBS.URETIM._busy(true);
  try {
    const response = await fetchWithSessionCheck("stok/altgrup", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ anagrup })
    });
    if (response?.errorMessage) throw new Error(response.errorMessage);

    (response.altKodlari || []).forEach((kod) => {
      const option = document.createElement("option");
      option.value = kod.ALT_GRUP;
      option.textContent = kod.ALT_GRUP;
      selectElement.appendChild(option);
    });

    selectElement.disabled = selectElement.options.length === 0;
  } catch (err) {
    selectElement.disabled = true;
    OBS.URETIM._showError(err?.message);
  } finally {
    OBS.URETIM._busy(false);
  }
};

/* =========================================================
   3) GİREN ÜRÜN BİLGİ OKU (urnbilgiArama)
   ========================================================= */
OBS.URETIM.urunAramaYap = async (kodbarkod = "Kodu") => {
  const aramaInput = OBS.URETIM._el("girenurnkod")?.value || "";
  if (!aramaInput) return;

  OBS.URETIM._hideError();
  OBS.URETIM._busy(true);

  try {
    const response = await fetchWithSessionCheck("stok/urnbilgiArama", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ deger: aramaInput, kodbarkod })
    });

    if (response?.errorMessage === "Bu Numarada Urun Yok") {
      OBS.URETIM._showError(response.errorMessage);
      return;
    }
    if (response?.errorMessage) throw new Error(response.errorMessage);

    const dto = response.urun || {};

    const recete = OBS.URETIM._el("recetekod");
    if (recete) recete.value = dto.recete ?? "";

    const setText = (id, val) => {
      const el = OBS.URETIM._el(id);
      if (el) el.innerText = val ?? "";
    };

    setText("adi", dto.adi);
    setText("birim", dto.birim);
    setText("anagrpl", dto.anagrup);
    setText("altgrpl", dto.altgrup);
    setText("agirlik", formatNumber3(dto.agirlik ?? 0));
    setText("barkod", dto.barkod);
    setText("sinif", dto.sinif);

    const img = OBS.URETIM._el("resimGoster");
    if (img) {
      if (dto.base64Resim && String(dto.base64Resim).trim() !== "") {
        img.src = "data:image/jpeg;base64," + String(dto.base64Resim).trim();
        img.style.display = "block";
      } else {
        img.src = "";
        img.style.display = "none";
      }
    }
	
	const urtmmiktar =  OBS.URETIM._el("uretmiktar");
	urtmmiktar.focus();
	urtmmiktar.select();
  } catch (err) {
    OBS.URETIM._showError(err?.message);
  } finally {
    OBS.URETIM._busy(false);
  }
};

/* =========================================================
   4) SON FİŞ
   ========================================================= */
OBS.URETIM.sonfis = async () => {
  OBS.URETIM._hideError();
  OBS.URETIM._busy(true);

  try {
    const response = await fetchWithSessionCheck("stok/sonfis", {
      method: "POST",
      headers: { "Content-Type": "application/json" }
    });
    if (response?.errorMessage) throw new Error(response.errorMessage);

    const fisNoInput = OBS.URETIM._el("fisno");
    if (fisNoInput) fisNoInput.value = response.fisno ?? "";

    // kodunda data.fisNo diye kontrol vardı, muhtemelen typo -> fisno kontrolü yeter
    if (!response.fisno || Number(response.fisno) === 0) {
      OBS.URETIM._showError("Hata: Evrak numarası bulunamadı.");
      return;
    }

    await OBS.URETIM.uretimOku();
  } catch (err) {
    OBS.URETIM._showError(err?.message);
  } finally {
    OBS.URETIM._busy(false);
  }
};

/* =========================================================
   5) ÜRETİM OKU
   ========================================================= */
OBS.URETIM.uretimOku = async () => {
  const fisno = OBS.URETIM._el("fisno")?.value || "";
  if (!fisno) return;

  OBS.URETIM._hideError();
  OBS.URETIM._busy(true);

  try {
    const response = await fetchWithSessionCheck("stok/uretimOku", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ fisno })
    });

    if (response?.errorMessage) {
      OBS.URETIM._showError(response.errorMessage);
      return;
    }

    const data = response;
    OBS.URETIM.clearInputs(); // tabloyu ve alanları sıfırla

    const list = data.data || [];
    if (list.length === 0) return;

    // tablo satırı yetmezse artır
    const table = OBS.URETIM._el("imaTable");
    const rowss = table?.querySelectorAll("tbody tr") || [];
    if (list.length > rowss.length) {
      const add = list.length - rowss.length;
      for (let i = 0; i < add; i++) OBS.URETIM.satirekle();
    }

    const rows = table.querySelectorAll("tbody tr");

    // C (ÇIKAN) satırlarını doldur
    list.forEach((item, index) => {
      if (item.Hareket === "C" && index < rows.length) {
        const cells = rows[index].cells;

        OBS.URETIM.setLabelContent(cells[1], "CIKAN");
        OBS.URETIM.setLabelContent(cells[3], item.Adi || "");
        OBS.URETIM.setLabelContent(cells[7], item.Birim || "");

        const urunKoduInput = cells[2]?.querySelector("input");
        if (urunKoduInput) urunKoduInput.value = item.Urun_Kodu || "";

        const izahatInput = cells[4]?.querySelector("input");
        if (izahatInput) izahatInput.value = item.Izahat || "";

        const miktarInput = cells[6]?.querySelector("input");
        if (miktarInput) miktarInput.value = formatNumber3((item.Miktar || 0) * -1);

        const fiatInput = cells[8]?.querySelector("input");
        if (fiatInput) fiatInput.value = formatNumber2(item.Fiat || 0);

        const tutarInput = cells[9]?.querySelector("input");
        if (tutarInput) tutarInput.value = formatNumber2((item.Tutar || 0) * -1);

        const depoSelect = cells[5]?.querySelector("select");
        if (depoSelect) depoSelect.value = item.Depo || "";
      }
    });

    // G (GİREN) satırından üst bilgileri doldur
    for (let i = 0; i < list.length; i++) {
      const item = list[i];
      if (item.Hareket === "G") {
        const setVal = (id, v) => { const el = OBS.URETIM._el(id); if (el) el.value = v ?? ""; };
        const setText = (id, v) => { const el = OBS.URETIM._el(id); if (el) el.innerText = v ?? ""; };

        setVal("fisTarih", formatdateSaatsiz(item.Tarih));
        setVal("uretmiktar", item.Miktar ?? "");
        setVal("girenurnkod", item.Urun_Kodu ?? "");

        setVal("anagrp", item.Ana_Grup || "");
        setText("mikbirim", item.Birim || "");
        setVal("dvzcins", item.Doviz || "");

        await OBS.URETIM.anagrpChanged(OBS.URETIM._el("anagrp"));

        setVal("altgrp", item.Alt_Grup || "");

        const depoSel = OBS.URETIM._el("depo");
        if (depoSel) {
          const wanted = (item.Depo || "").trim();
          const exists = Array.from(depoSel.options).some(o => o.value.trim() === wanted);
          depoSel.value = exists ? wanted : "";
        }
        break;
      }
    }

    const aciklama = OBS.URETIM._el("aciklama");
    if (aciklama) aciklama.value = data.aciklama ?? "";

    await OBS.URETIM.urunAramaYap("Kodu");
    OBS.URETIM.updateColumnTotal();
    OBS.URETIM._hideError();
  } catch (err) {
    OBS.URETIM._showError(err?.message);
  } finally {
    OBS.URETIM._busy(false);
  }
};

/* =========================================================
   6) TABLO / SATIR
   ========================================================= */
OBS.URETIM.initializeRows = () => {
  OBS.URETIM.rowCounter = 0;
  // önce tbody temizle (varsa)
  const table = OBS.URETIM._el("imaTable");
  const tbody = table?.getElementsByTagName("tbody")?.[0];
  if (tbody) tbody.innerHTML = "";

  for (let i = 0; i < 5; i++) OBS.URETIM.satirekle();
};

OBS.URETIM.incrementRowCounter = () => { OBS.URETIM.rowCounter++; };

OBS.URETIM.satirekle = () => {
  const tbody = OBS.URETIM._el("imaTable")?.getElementsByTagName("tbody")?.[0];
  if (!tbody) return;

  const newRow = tbody.insertRow();
  OBS.URETIM.incrementRowCounter();
  const rc = OBS.URETIM.rowCounter;

  const ukoduoptionsHTML = (OBS.URETIM.urnkodlar || [])
    .map(kod => `<option value="${kod.Kodu}">${kod.Kodu}</option>`)
    .join("");

  const depoOptionsHTML = (OBS.URETIM.depolar || [])
    .map(kod => `<option value="${kod.DEPO}">${kod.DEPO}</option>`)
    .join("");

  newRow.innerHTML = `
    <td>
      <button id="bsatir_${rc}" type="button" class="btn btn-secondary"
              onclick="OBS.URETIM.satirsil(this)">
        <i class="fa fa-trash"></i>
      </button>
    </td>

    <td>
      <label class="form-control"><span>CIKAN</span></label>
    </td>

    <td>
      <div class="ima-rel">
        <input class="form-control cins_bold"
          list="ukoduOptions_${rc}"
          maxlength="12"
          id="ukodu_${rc}"
          onkeydown="OBS.URETIM.focusNextCell(event, this)"
          ondblclick="openurunkodlariModal('ukodu_${rc}','imalatsatir','ukodukod')"
          onchange="OBS.URETIM.updateRowValues(this)">
        <datalist id="ukoduOptions_${rc}">
          ${ukoduoptionsHTML}
        </datalist>
        <span class="ima-arrow">▼</span>
      </div>
    </td>

    <td>
      <label class="form-control"><span>&nbsp;</span></label>
    </td>

    <td>
      <input class="form-control"
        onkeydown="OBS.URETIM.focusNextCell(event, this)"
        onfocus="OBS.URETIM.selectAllContent(this)">
    </td>

    <td>
      <div class="ima-rel">
        <select class="form-control" id="depo_${rc}">
          ${depoOptionsHTML}
        </select>
        <span class="ima-arrow">▼</span>
      </div>
    </td>

    <td>
      <input class="form-control"
        value="${formatNumber3(0)}"
        style="text-align:right;"
        onfocus="OBS.URETIM.selectAllContent(this)"
        onblur="OBS.URETIM.handleBlur3(this)"
        onkeydown="OBS.URETIM.focusNextCell(event, this)">
    </td>

    <td>
      <label class="form-control"><span>&nbsp;</span></label>
    </td>

    <td>
      <input class="form-control"
        value="${formatNumber2(0)}"
        style="text-align:right;"
        onfocus="OBS.URETIM.selectAllContent(this)"
        onblur="OBS.URETIM.handleBlur2(this)"
        onkeydown="OBS.URETIM.focusNextCell(event, this)">
    </td>

    <td>
      <input class="form-control"
        value="${formatNumber2(0)}"
        style="text-align:right;"
        onfocus="OBS.URETIM.selectAllContent(this)"
        onblur="OBS.URETIM.handleBlur2(this)"
        onkeydown="OBS.URETIM.focusNextRow(event, this)">
    </td>
  `;
};

OBS.URETIM.handleBlur3 = (input) => {
  input.value = formatNumber3(parseLocaleNumber(input.value));
  OBS.URETIM.updateColumnTotal();
};
OBS.URETIM.handleBlur2 = (input) => {
  input.value = formatNumber2(parseLocaleNumber(input.value));
  OBS.URETIM.updateColumnTotal();
};

OBS.URETIM.selectAllContent = (el) => { el?.select?.(); };

OBS.URETIM.satirsil = (btn) => {
  btn?.closest("tr")?.remove();
  OBS.URETIM.updateColumnTotal();
};

OBS.URETIM.setLabelContent = (cell, content) => {
  const span = cell?.querySelector("label span");
  if (span) span.textContent = content ? content : "\u00A0";
};

OBS.URETIM.updateColumnTotal = () => {
  const totalCell = OBS.URETIM._el("totalTutar");
  if (!totalCell) return;

  const rows = document.querySelectorAll("table tr");
  let total = 0;
  totalCell.textContent = "0.00";

  rows.forEach(row => {
    const input7 = row.querySelector("td:nth-child(7) input");
    const input9 = row.querySelector("td:nth-child(9) input");
    const input10 = row.querySelector("td:nth-child(10) input");
    if (!input7 || !input9 || !input10) return;

    const v7 = parseLocaleNumber(input7.value) || 0;
    const v9 = parseLocaleNumber(input9.value) || 0;
    const result = v7 * v9;

    input10.value = result.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    if (result > 0) total += result;
  });

  totalCell.textContent = total.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  const dbmik = parseLocaleNumber(OBS.URETIM._el("uretmiktar")?.value) || 0;
  const lbl = OBS.URETIM._el("birimfiati");
  if (lbl) {
    lbl.innerText = (total / (dbmik === 0 ? 1 : dbmik))
      .toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
};

/* =========================================================
   7) SATIRDA ÜRÜN SEÇİNCE (imalatcikan)
   ========================================================= */
OBS.URETIM.updateRowValues = async (inputElement) => {
  const selectedValue = inputElement?.value || "";
  if (!selectedValue) return;

  const uygulananfiat = OBS.URETIM._el("uygulananfiat")?.value || "";
  const fisTarih = OBS.URETIM._el("fisTarih")?.value || "";
  const fiatTarih = OBS.URETIM._el("fiatTarih")?.value || "";

  OBS.URETIM._hideError();
  OBS.URETIM._busy(true);

  try {
    const response = await fetchWithSessionCheck("stok/imalatcikan", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        ukodu: selectedValue,
        fiatlama: uygulananfiat,
        fisTarih,
        fiatTarih
      })
    });
    if (response?.errorMessage) throw new Error(response.errorMessage);

    const row = inputElement.closest("tr");
    const cells = row.querySelectorAll("td");

    OBS.URETIM.setLabelContent(cells[1], "CIKAN");
    OBS.URETIM.setLabelContent(cells[3], response.urun?.adi);
    OBS.URETIM.setLabelContent(cells[7], response.urun?.birim);

    const fiatInput = cells[8]?.querySelector("input");
    if (fiatInput) fiatInput.value = formatNumber2(response.urun?.fiat || 0);

    OBS.URETIM.updateColumnTotal();
  } catch (err) {
    OBS.URETIM._showError(err?.message);
  } finally {
    OBS.URETIM._busy(false);
  }
};

/* =========================================================
   8) ENTER İLE GEÇİŞ
   ========================================================= */
OBS.URETIM.focusNextRow = (event, element) => {
  if (event.key !== "Enter") return;
  event.preventDefault();

  const currentRow = element.closest("tr");
  const nextRow = currentRow.nextElementSibling;

  if (nextRow) {
    const secondInput = nextRow.querySelector("td:nth-child(3) input");
    if (secondInput) { secondInput.focus(); secondInput.select?.(); }
  } else {
    OBS.URETIM.satirekle();
    const tbody = currentRow.parentElement;
    const newRow = tbody.lastElementChild;
    const secondInput = newRow.querySelector("td:nth-child(3) input");
    if (secondInput) { secondInput.focus(); secondInput.select?.(); }
  }
};

OBS.URETIM.focusNextCell = (event, element) => {
  if (event.key !== "Enter") return;
  event.preventDefault();

  let currentCell = element.closest("td");
  let nextCell = currentCell.nextElementSibling;

  while (nextCell) {
    const focusable = nextCell.querySelector("input, select");
    if (focusable) {
      focusable.focus();
      focusable.select?.();
      break;
    }
    nextCell = nextCell.nextElementSibling;
  }
};

/* =========================================================
   9) FİYAT TİPİ DEĞİŞİNCE
   ========================================================= */
OBS.URETIM.uygulananfiatchange = () => {
  const uygfiat = OBS.URETIM._el("uygulananfiat")?.value || "";
  const datlabel = OBS.URETIM._el("datlabel");
  const fiatTarih = OBS.URETIM._el("fiatTarih");

  const visible = (uygfiat === "ortfiat");
  if (datlabel) datlabel.style.visibility = visible ? "visible" : "hidden";
  if (fiatTarih) fiatTarih.style.visibility = visible ? "visible" : "hidden";
};

/* =========================================================
   10) TEMİZLE
   ========================================================= */
OBS.URETIM.clearInputs = () => {
  const setVal = (id, v) => { const el = OBS.URETIM._el(id); if (el) el.value = v; };
  const setText = (id, v) => { const el = OBS.URETIM._el(id); if (el) el.innerText = v; };

  setVal("uygulananfiat", "");
  setVal("recetekod", "");
  setVal("anagrp", "");
  setVal("altgrp", "");
  setVal("depo", "");
  setVal("girenurnkod", "");
  setVal("uretmiktar", "0");
  setVal("aciklama", "");
  setVal("dvzcins", OBS.URETIM._el("defaultdvzcinsi")?.value || "TL");

  setText("birimfiati", "0.00");
  setText("adi", "");
  setText("birim", "");
  setText("anagrpl", "");
  setText("altgrpl", "");
  setText("agirlik", "");
  setText("barkod", "");
  setText("sinif", "");
  setText("mikbirim", "");

  const alt = OBS.URETIM._el("altgrp");
  if (alt) { alt.innerHTML = ""; alt.disabled = true; }

  const img = OBS.URETIM._el("resimGoster");
  if (img) { img.src = ""; img.style.display = "none"; }

  OBS.URETIM.initializeRows();
  const total = OBS.URETIM._el("totalTutar");
  if (total) total.textContent = formatNumber2(0);
};

/* =========================================================
   11) YENİ FİŞ
   ========================================================= */
OBS.URETIM.yeniFis = async () => {
  OBS.URETIM._hideError();
  OBS.URETIM.clearInputs();

  OBS.URETIM._busy(true);
  try {
    const response = await fetchWithSessionCheck("stok/uretimyenifis", {
      method: "GET",
      headers: { "Content-Type": "application/json" }
    });
    if (response?.errorMessage) throw new Error(response.errorMessage);

    const fisNoInput = OBS.URETIM._el("fisno");
    if (fisNoInput) fisNoInput.value = response.fisno ?? "";
  } catch (err) {
    OBS.URETIM._showError(err?.message);
  } finally {
    OBS.URETIM._busy(false);
  }
};

/* =========================================================
   12) URETIM YAP  ✅ (SENİN SORDUĞUN)
   ========================================================= */
OBS.URETIM.uretimYap = async () => {
  const recetekod = OBS.URETIM._el("recetekod")?.value || "";
  if (!recetekod) return;

  OBS.URETIM._hideError();
  OBS.URETIM._busy(true);

  try {
    const response = await fetchWithSessionCheck("stok/hesapla", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ recetekod })
    });
    if (response?.errorMessage) throw new Error(response.errorMessage);

    const data = response;
    const uretmiktar = parseLocaleNumber(OBS.URETIM._el("uretmiktar")?.value || 0) || 0;

    // tabloyu sıfırla + satırları hazırla
    OBS.URETIM.initializeRows();

    const table = OBS.URETIM._el("imaTable");
    const rows = table.querySelectorAll("tbody tr");

    // satır yetmezse artır
    if ((data.data || []).length > rows.length) {
      const add = (data.data || []).length - rows.length;
      for (let i = 0; i < add; i++) OBS.URETIM.satirekle();
    }

    const rows2 = table.querySelectorAll("tbody tr");
    let index = 0;

    (data.data || []).forEach((item) => {
      if (item.Tur === "Cikan" && index < rows2.length) {
        const cells = rows2[index].cells;

        OBS.URETIM.setLabelContent(cells[1], item.Tur);
        OBS.URETIM.setLabelContent(cells[3], item.Adi || "");
        OBS.URETIM.setLabelContent(cells[7], item.Birim || "");

        const urunKoduInput = cells[2]?.querySelector("input");
        if (urunKoduInput) urunKoduInput.value = item.Kodu || "";

        const izahatInput = cells[4]?.querySelector("input");
        if (izahatInput) izahatInput.value = "";

        const miktarInput = cells[6]?.querySelector("input");
        if (miktarInput) miktarInput.value = formatNumber3((item.Miktar || 0) * uretmiktar);

        const fiatInput = cells[8]?.querySelector("input");
        if (fiatInput) fiatInput.value = formatNumber2(0);

        const tutarInput = cells[9]?.querySelector("input");
        if (tutarInput) tutarInput.value = formatNumber2(0);

        const depoSelect = cells[5]?.querySelector("select");
        if (depoSelect) depoSelect.value = "";

        index++;
      } else {
        const mik = OBS.URETIM._el("mikbirim");
        if (mik) mik.innerText = item.Birim || "";
      }
    });

    OBS.URETIM.updateColumnTotal();
  } catch (err) {
    OBS.URETIM._showError(err?.message);
  } finally {
    OBS.URETIM._busy(false);
  }
};

/* =========================================================
   13) KAYIT DTO
   ========================================================= */
OBS.URETIM.getTableData = () => {
  const table = OBS.URETIM._el("imaTable");
  const rows = table?.querySelectorAll("tbody tr") || [];
  const data = [];

  rows.forEach((row) => {
    const cells = row.querySelectorAll("td");
    const ukodu = cells[2]?.querySelector("input")?.value || "";
    if (!ukodu.trim()) return;

    data.push({
      ukodu,
      izahat: cells[4]?.querySelector("input")?.value || "",
      depo: cells[5]?.querySelector("select")?.value || "",
      miktar: parseLocaleNumber(cells[6]?.querySelector("input")?.value || 0),
      fiat: parseLocaleNumber(cells[8]?.querySelector("input")?.value || 0),
      tutar: parseLocaleNumber(cells[9]?.querySelector("input")?.value || 0)
    });
  });

  return data;
};

OBS.URETIM.prepareureKayit = () => {
  return {
    uretimDTO: {
      fisno: OBS.URETIM._el("fisno")?.value || "",
      tarih: OBS.URETIM._el("fisTarih")?.value || "",
      anagrup: OBS.URETIM._el("anagrp")?.value || "",
      altgrup: OBS.URETIM._el("altgrp")?.value || "",
      depo: OBS.URETIM._el("depo")?.value || "",
      girenurkodu: OBS.URETIM._el("girenurnkod")?.value || "",
      aciklama: OBS.URETIM._el("aciklama")?.value || "",
      dvzcins: OBS.URETIM._el("dvzcins")?.value || "",
      uremiktar: parseLocaleNumber(OBS.URETIM._el("uretmiktar")?.value || 0),
      toptutar: parseLocaleNumber(OBS.URETIM._el("totalTutar")?.textContent || 0)
    },
    tableData: OBS.URETIM.getTableData()
  };
};

/* =========================================================
   14) KAYIT / SİL  (jQuery yok)
   ========================================================= */
OBS.URETIM.ureKayit = async () => {
  const fisno = OBS.URETIM._el("fisno")?.value || "";
  const rowCount = OBS.URETIM._el("imaTable")?.rows?.length || 0;

  if (!fisno || fisno === "0" || rowCount === 0) {
    alert("Geçerli bir evrak numarası giriniz.");
    return;
  }

  const dto = OBS.URETIM.prepareureKayit();

  OBS.URETIM._hideError();
  OBS.URETIM._btnBusy("urekaydetButton", true, "İşleniyor...", "Kaydet");
  OBS.URETIM._busy(true);

  try {
    const response = await fetchWithSessionCheck("stok/ureKayit", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dto)
    });

    if (response?.errorMessage && response.errorMessage.trim() !== "") {
      throw new Error(response.errorMessage);
    }

    OBS.URETIM.clearInputs();
    const fis = OBS.URETIM._el("fisno");
    if (fis) fis.value = "";
    OBS.URETIM._hideError();
  } catch (err) {
    OBS.URETIM._showError(err?.message);
  } finally {
    OBS.URETIM._busy(false);
    OBS.URETIM._btnBusy("urekaydetButton", false, "İşleniyor...", "Kaydet");
  }
};

OBS.URETIM.ureYoket = async () => {
  const fisno = OBS.URETIM._el("fisno")?.value || "";
  if (!fisno || fisno === "0") return;
  if (!confirm("Bu Uretim fisi silinecek ?")) return;

  OBS.URETIM._hideError();
  OBS.URETIM._btnBusy("uresilButton", true, "Siliniyor...", "Sil");
  OBS.URETIM._busy(true);

  try {
    const response = await fetchWithSessionCheck("stok/ureYoket", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ fisno })
    });
    if (response?.errorMessage) throw new Error(response.errorMessage);

    OBS.URETIM.clearInputs();
    const fis = OBS.URETIM._el("fisno");
    if (fis) fis.value = "";
    OBS.URETIM._hideError();
  } catch (err) {
    OBS.URETIM._showError(err?.message);
  } finally {
    OBS.URETIM._busy(false);
    OBS.URETIM._btnBusy("uresilButton", false, "Siliniyor...", "Sil");
  }
};
