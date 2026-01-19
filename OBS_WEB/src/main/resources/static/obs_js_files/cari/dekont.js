/* ==========================
   DEKONT (jQuery YOK) – 2026
   - fetchWithSessionCheck kullanır
   - Dinamik loader ile uyumlu
   - sayfaYukle isim çakışması YOK
   ========================== */

/* ---------- küçük yardımcılar ---------- */
function $(id) { return document.getElementById(id); }

function setText(id, val) {
  const el = $(id);
  if (el) el.innerText = val ?? "";
}

function setValue(id, val) {
  const el = $(id);
  if (el) el.value = val ?? "";
}

function getValue(id) {
  const el = $(id);
  return el ? (el.value ?? "") : "";
}

function setDisabled(id, disabled) {
  const el = $(id);
  if (!el) return;
  el.disabled = !!disabled;
}

function showError(msg) {
  const err = $("errorDiv");
  if (!err) return;
  err.style.display = "block";
  err.innerText = msg ?? "Beklenmeyen hata";
}

function clearError() {
  const err = $("errorDiv");
  if (!err) return;
  err.style.display = "none";
  err.innerText = "";
}

function setBusy(isBusy) {
  document.body.style.cursor = isBusy ? "wait" : "default";
}

function setBtn(id, disabled, text) {
  const b = $(id);
  if (!b) return;
  b.disabled = !!disabled;
  if (typeof text === "string") b.innerText = text;
}

function toFloatSafe(raw, defVal) {
  const x = parseFloat(String(raw ?? "").trim().replace(/,/g, "").replace(/\s/g, ""));
  return Number.isFinite(x) ? x : defVal;
}

/* ==========================
   HESAP ADI ÖĞREN
   ========================== */
async function dekhesapAdiOgren(inputId, targetLabelId1, targetLabelId2) {
  const inputEl = $(inputId);
  const l1 = $(targetLabelId1);
  const l2 = $(targetLabelId2);
  const inputValue = inputEl ? inputEl.value : "";

  setBusy(true);

  if (!inputValue) {
    if (l1) l1.innerText = "";
    if (l2) l2.innerText = "";
    setBusy(false);
    return;
  }

  try {
    const dto = await fetchWithSessionCheck("cari/hesapadi", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ hesapkodu: inputValue }),
    });

    if (dto?.errorMessage) throw new Error(dto.errorMessage);

    if (l1) l1.innerText = dto.hesapAdi || "Hesap adı bulunamadı";
    if (l2) l2.innerText = dto.hesapCinsi || "";
  } catch (e) {
    if (l1) l1.innerText = "Hata oluştu!";
    if (l2) l2.innerText = "";
  } finally {
    setBusy(false);
  }
}

/* ==========================
   INPUT TEMİZLEME / ENABLE-DISABLE
   ========================== */
function dekclearInput(input) {
  if (input) input.value = "";
}

function dekclearInputs() {
  // Borç
  dekclearInput($("borcHesap"));
  setText("borcluAdi", "");
  setText("borcluHesapCins", "");
  setValue("borcKurCins", "");
  setValue("borcKur", "1.0000");
  setValue("borcTutar", "0.00");

  // Alacak
  dekclearInput($("alacakHesap"));
  setText("alacakliAdi", "");
  setText("alacakliHesapCins", "");
  setValue("alacakKurCins", "");
  setValue("alacakKur", "1.0000");
  setValue("alacakTutar", "0.00");

  // Diğer
  dekclearInput($("aciklama"));
  dekclearInput($("kodu"));
}

function dekenableInputs() {
  setDisabled("borcHesap", false);
  setDisabled("borcKurCins", false);
  setDisabled("borcKur", false);
  setDisabled("borcTutar", false);

  setDisabled("alacakHesap", false);
  setDisabled("alacakKurCins", false);
  setDisabled("alacakKur", false);
  setDisabled("alacakTutar", false);

  setDisabled("aciklama", false);
  setDisabled("kodu", false);
}

function dekdisableInputs() {
  setDisabled("borcHesap", true);
  setDisabled("borcKurCins", true);
  setDisabled("borcKur", true);
  setDisabled("borcTutar", true);

  setDisabled("alacakHesap", true);
  setDisabled("alacakKurCins", true);
  setDisabled("alacakKur", true);
  setDisabled("alacakTutar", true);

  setDisabled("aciklama", true);
  setDisabled("kodu", true);
}

/* ==========================
   TUTAR/KUR HESAPLARI
   ========================== */
function tutarKontrolAlacak() {
  const borcTutar = toFloatSafe(getValue("borcTutar"), 0);
  const borcKur = toFloatSafe(getValue("borcKur"), 1);
  const alacakKur = toFloatSafe(getValue("alacakKur"), 1);

  const alacakTutarEl = $("alacakTutar");
  if (!alacakTutarEl) return;

  if (!Number.isFinite(borcTutar) || !Number.isFinite(borcKur) || !Number.isFinite(alacakKur) || borcKur <= 0 || alacakKur <= 0) {
    alert("Lütfen geçerli ve sıfırdan büyük değerler girin.");
    return;
  }

  let sonuc;
  if (alacakKur !== 1) {
    sonuc = (borcKur !== 1) ? ((borcTutar * borcKur) / alacakKur) : (borcTutar / alacakKur);
  } else {
    sonuc = borcTutar * borcKur;
  }

  alacakTutarEl.value = sonuc.toFixed(2);
}

function tutarKontrolBorc() {
  const borcTutarEl = $("borcTutar");
  if (!borcTutarEl) return;

  const borcKur = toFloatSafe(getValue("borcKur"), 1);
  const alacakTutar = toFloatSafe(getValue("alacakTutar"), 0);
  const alacakKur = toFloatSafe(getValue("alacakKur"), 1);

  if (!Number.isFinite(borcKur) || !Number.isFinite(alacakTutar) || !Number.isFinite(alacakKur) || borcKur <= 0 || alacakKur <= 0) {
    alert("Lütfen geçerli ve sıfırdan büyük değerler girin.");
    return;
  }

  let sonuc;
  if (borcKur !== 1) {
    sonuc = (alacakKur !== 1) ? ((alacakTutar * alacakKur) / borcKur) : (alacakTutar / borcKur);
  } else {
    sonuc = alacakTutar * alacakKur;
  }

  borcTutarEl.value = sonuc.toFixed(2);
}

/* ==========================
   YENİ FİŞ
   ========================== */
function dekyenifis() {
  dekclearInputs();
  dekenableInputs();
  dekyenifisNo();
}

/* ==========================
   SON FİŞ NO
   ========================== */
async function deksonfisNo() {
  try {
    setBusy(true);
    clearError();

    const data = await fetchWithSessionCheck("cari/sonfisNo", {
      method: "POST",
      headers: { "Content-Type": "application/json" }
    });

    if (data?.errorMessage) throw new Error(data.errorMessage);

    const fisNoInput = $("evrakNo");
    if (fisNoInput) fisNoInput.value = data.fisNo;

    if (data.fisNo === 0) {
      alert("Hata: Evrak numarası bulunamadı.");
      showError(data.errorMessage || "Evrak numarası bulunamadı.");
      return;
    }

    clearError();
    await dekevrakOku();
  } catch (e) {
    showError(e?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    setBusy(false);
  }
}

/* ==========================
   GERİ/İLERİ FİŞ
   ========================== */
function dekgerifisNo() {
  const fisNoInput = $("evrakNo");
  if (!fisNoInput) return;
  const currentValue = parseInt(fisNoInput.value, 10) || 0;
  if (currentValue <= 0) return;
  fisNoInput.value = currentValue - 1;
  dekevrakOku();
}

function dekilerifisNo() {
  const fisNoInput = $("evrakNo");
  if (!fisNoInput) return;
  const currentValue = parseInt(fisNoInput.value, 10) || 0;
  fisNoInput.value = currentValue + 1;
  dekevrakOku();
}

/* ==========================
   FİŞ KAYIT
   ========================== */
async function dekfisKayit() {
  const fisNoInput = $("evrakNo");
  if (!fisNoInput || ["0", ""].includes(fisNoInput.value)) return;

  clearError();
  setBusy(true);
  setBtn("dekkaydetButton", true, "İşleniyor...");

  try {
    const dekontDTO = {
      fisNo: fisNoInput.value,
      tar: getFullDateWithTimeAndMilliseconds(getValue("dekontTarih")),
      bhes: getValue("borcHesap"),
      bcins: getValue("borcKurCins"),
      bkur: parseLocaleNumber(getValue("borcKur")),
      borc: parseLocaleNumber(getValue("borcTutar")),
      ahes: getValue("alacakHesap"),
      acins: getValue("alacakKurCins"),
      akur: parseLocaleNumber(getValue("alacakKur")),
      alacak: parseLocaleNumber(getValue("alacakTutar")),
      izahat: getValue("aciklama"),
      kod: getValue("kodu"),
    };

    const dto = await fetchWithSessionCheck("cari/fiskayit", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dekontDTO),
    });

    if (dto?.errorMessage) throw new Error(dto.errorMessage);

    dekclearInputs();
    dekdisableInputs();
    fisNoInput.value = "0";
  } catch (e) {
    showError(e?.message || "Bir hata oluştu.");
  } finally {
    setBusy(false);
    setBtn("dekkaydetButton", false, "Kaydet");
  }
}

/* ==========================
   YENİ FİŞ NO
   ========================== */
async function dekyenifisNo() {
  try {
    setBusy(true);
    clearError();

    const data = await fetchWithSessionCheck("cari/yenifisNo", {
      method: "POST",
      headers: { "Content-Type": "application/json" }
    });

    if (data?.errorMessage) throw new Error(data.errorMessage);

    setValue("evrakNo", data.fisNo);
    dekclearInputs();
    dekenableInputs();
  } catch (e) {
    showError(e?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    setBusy(false);
  }
}

/* ==========================
   EVRAK OKU (Enter ile)
   ========================== */
function checkEnter(event) {
  if (event.key === "Enter" || event.keyCode === 13) {
    dekevrakOku();
  }
}

async function dekevrakOku() {
  const evrakNo = getValue("evrakNo");
  if (!evrakNo || evrakNo === "0") {
    alert("Lütfen geçerli bir evrak numarası girin!");
    return;
  }
	dekclearInputs();
  clearError();
  setBusy(true);

  try {
    const data = await fetchWithSessionCheck("cari/evrakOku", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ evrakNo }),
    });

    // beklenen: dizi [dto1, dto2]
    const dto1 = data?.[0];
    if (dto1?.errorMessage) {
      showError(dto1.errorMessage);
      dekdisableInputs();
      return;
    }

    dekclearInputs();

    setValue("dekontTarih", dto1?.tar || "");
    setValue("borcHesap", dto1?.bhes || "");
    setValue("borcKurCins", dto1?.bcins || "");
    setValue("borcKur", formatNumber4(dto1?.bkur));
    setValue("borcTutar", formatNumber2(dto1?.borc));

    const dto2 = data?.[1] || {};
    setValue("alacakHesap", dto2?.ahes || "");
    setValue("alacakKurCins", dto2?.acins || "");
    setValue("alacakKur", formatNumber4(dto2?.akur));
    setValue("alacakTutar", formatNumber2(dto2?.alacak));
    setValue("aciklama", dto2?.izahat || "");
    setValue("kodu", dto2?.kod || "");

    // input event’lerini tetikle (hesap adı label vs.)
    const bhes = $("borcHesap");
    if (bhes && typeof bhes.oninput === "function") bhes.oninput();

    const ahes = $("alacakHesap");
    if (ahes && typeof ahes.oninput === "function") ahes.oninput();

    dekenableInputs();
    clearError();
  } catch (e) {
    showError(e?.message || "Beklenmeyen bir hata oluştu.");
    dekdisableInputs();
  } finally {
    setBusy(false);
  }
}

/* ==========================
   EVRAK SİL
   ========================== */
async function dekfisYoket() {
  const fisNoInput = $("evrakNo");
  if (!fisNoInput || ["0", ""].includes(fisNoInput.value)) return;

  if (!confirm("Bu evrak numarasını silmek istediğinize emin misiniz?")) return;

  clearError();
  setBusy(true);
  setBtn("deksilButton", true, "Siliniyor...");

  try {
    const dekontNo = parseInt(fisNoInput.value, 10);

    const dto = await fetchWithSessionCheck("cari/fisYoket", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ evrakNo: dekontNo }),
    });

    if (dto?.errorMessage) throw new Error(dto.errorMessage);

    dekclearInputs();
    dekdisableInputs();
    fisNoInput.value = "0";
    clearError();
  } catch (e) {
    showError(e?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    setBusy(false);
    setBtn("deksilButton", false, "Sil");
  }
}

/* ==========================
   SADECE NUMARA
   ========================== */
function allowOnlyNumbers(event) {
  const allowedKeys = ["Backspace", "Tab", "ArrowLeft", "ArrowRight", "Delete", "Enter"];
  if (allowedKeys.includes(event.key)) return;
  if (!/^[0-9]$/.test(event.key)) event.preventDefault();
}

/* ==========================
   KUR OKU
   ========================== */
async function dekkurOku(dvz_turu, cinsElement, targetInput) {
  const dvz_tur = $(dvz_turu)?.textContent ?? "";
  const cin = cinsElement?.value ?? "";
  const yazilacakInput = $(targetInput);
  if (!yazilacakInput) return;

  clearError();
  setBusy(true);

  try {
    const kurgirisDTO = {
      dvz_cins: cin,
      dvz_turu: dvz_tur,
      tar: getValue("dekontTarih"),
    };

    const data = await fetchWithSessionCheck("kur/kuroku", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(kurgirisDTO),
    });

    if (data?.success && Array.isArray(data.data) && data.data.length > 0) {
      const item = data.data[0];
      yazilacakInput.value = formatNumber4(item?.[cin]);
    } else {
      yazilacakInput.value = "1.0000";
    }
  } catch (e) {
    showError("Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.");
  } finally {
    setBusy(false);
  }
}
