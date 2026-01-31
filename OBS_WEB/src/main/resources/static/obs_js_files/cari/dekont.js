/* ==========================
   DEKONT (jQuery YOK) – 2026
   - fetchWithSessionCheck kullanır
   - Dinamik loader ile uyumlu
   - global isim çakışması YOK
   ========================== */

window.OBS = window.OBS || {};
OBS.DEKONT = OBS.DEKONT || {};

(function (M) {
  "use strict";

  /* ---------- küçük yardımcılar ---------- */
  M.byId = (id) => document.getElementById(id);

  M.setText = function (id, val) {
    const el = M.byId(id);
    if (el) el.innerText = val ?? "";
  };

  M.setValue = function (id, val) {
    const el = M.byId(id);
    if (el) el.value = val ?? "";
  };

  M.getValue = function (id) {
    const el = M.byId(id);
    return el ? (el.value ?? "") : "";
  };

  M.setDisabled = function (id, disabled) {
    const el = M.byId(id);
    if (!el) return;
    el.disabled = !!disabled;
  };

  M.showError = function (msg) {
    const err = M.byId("errorDiv");
    if (!err) return;
    err.style.display = "block";
    err.innerText = msg ?? "Beklenmeyen hata";
  };

  M.clearError = function () {
    const err = M.byId("errorDiv");
    if (!err) return;
    err.style.display = "none";
    err.innerText = "";
  };

  M.setBusy = function (isBusy) {
    document.body.style.cursor = isBusy ? "wait" : "default";
  };

  M.setBtn = function (id, disabled, text) {
    const b = M.byId(id);
    if (!b) return;
    b.disabled = !!disabled;
    if (typeof text === "string") b.innerText = text;
  };

  M.toFloatSafe = function (raw, defVal) {
    const x = parseFloat(String(raw ?? "").trim().replace(/,/g, "").replace(/\s/g, ""));
    return Number.isFinite(x) ? x : defVal;
  };

  /* ==========================
     HESAP ADI ÖĞREN
     ========================== */
  M.dekhesapAdiOgren = async function (inputId, targetLabelId1, targetLabelId2) {
    const inputEl = M.byId(inputId);
    const l1 = M.byId(targetLabelId1);
    const l2 = M.byId(targetLabelId2);
    const inputValue = inputEl ? inputEl.value : "";

    M.setBusy(true);

    if (!inputValue) {
      if (l1) l1.innerText = "";
      if (l2) l2.innerText = "";
      M.setBusy(false);
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
      M.setBusy(false);
    }
  };

  /* ==========================
     INPUT TEMİZLEME / ENABLE-DISABLE
     ========================== */
  M.dekclearInput = function (input) {
    if (input) input.value = "";
  };

  M.dekclearInputs = function () {
    // Borç
    M.dekclearInput(M.byId("borcHesap"));
    M.setText("borcluAdi", "");
    M.setText("borcluHesapCins", "");
    M.setValue("borcKurCins", "");
    M.setValue("borcKur", "1.0000");
    M.setValue("borcTutar", "0.00");

    // Alacak
    M.dekclearInput(M.byId("alacakHesap"));
    M.setText("alacakliAdi", "");
    M.setText("alacakliHesapCins", "");
    M.setValue("alacakKurCins", "");
    M.setValue("alacakKur", "1.0000");
    M.setValue("alacakTutar", "0.00");

    // Diğer
    M.dekclearInput(M.byId("aciklama"));
    M.dekclearInput(M.byId("kodu"));
  };

  M.dekenableInputs = function () {
    M.setDisabled("borcHesap", false);
    M.setDisabled("borcKurCins", false);
    M.setDisabled("borcKur", false);
    M.setDisabled("borcTutar", false);

    M.setDisabled("alacakHesap", false);
    M.setDisabled("alacakKurCins", false);
    M.setDisabled("alacakKur", false);
    M.setDisabled("alacakTutar", false);

    M.setDisabled("aciklama", false);
    M.setDisabled("kodu", false);
  };

  M.dekdisableInputs = function () {
    M.setDisabled("borcHesap", true);
    M.setDisabled("borcKurCins", true);
    M.setDisabled("borcKur", true);
    M.setDisabled("borcTutar", true);

    M.setDisabled("alacakHesap", true);
    M.setDisabled("alacakKurCins", true);
    M.setDisabled("alacakKur", true);
    M.setDisabled("alacakTutar", true);

    M.setDisabled("aciklama", true);
    M.setDisabled("kodu", true);
  };

  /* ==========================
     TUTAR/KUR HESAPLARI
     ========================== */
  M.tutarKontrolAlacak = function () {
    const borcTutar = M.toFloatSafe(M.getValue("borcTutar"), 0);
    const borcKur = M.toFloatSafe(M.getValue("borcKur"), 1);
    const alacakKur = M.toFloatSafe(M.getValue("alacakKur"), 1);

    const alacakTutarEl = M.byId("alacakTutar");
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
  };

  M.tutarKontrolBorc = function () {
    const borcTutarEl = M.byId("borcTutar");
    if (!borcTutarEl) return;

    const borcKur = M.toFloatSafe(M.getValue("borcKur"), 1);
    const alacakTutar = M.toFloatSafe(M.getValue("alacakTutar"), 0);
    const alacakKur = M.toFloatSafe(M.getValue("alacakKur"), 1);

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
  };

  /* ==========================
     YENİ FİŞ
     ========================== */
  M.dekyenifis = function () {
    M.dekclearInputs();
    M.dekenableInputs();
    M.dekyenifisNo();
  };

  /* ==========================
     SON FİŞ NO
     ========================== */
  M.deksonfisNo = async function () {
    try {
      M.setBusy(true);
      M.clearError();

      const data = await fetchWithSessionCheck("cari/sonfisNo", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });

      if (data?.errorMessage) throw new Error(data.errorMessage);

      const fisNoInput = M.byId("evrakNo");
      if (fisNoInput) fisNoInput.value = data.fisNo;

      if (data.fisNo === 0) {
        alert("Hata: Evrak numarası bulunamadı.");
        M.showError(data.errorMessage || "Evrak numarası bulunamadı.");
        return;
      }

      M.clearError();
      await M.dekevrakOku();
    } catch (e) {
      M.showError(e?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
      M.setBusy(false);
    }
  };

  /* ==========================
     GERİ/İLERİ FİŞ
     ========================== */
  M.dekgerifisNo = function () {
    const fisNoInput = M.byId("evrakNo");
    if (!fisNoInput) return;
    const currentValue = parseInt(fisNoInput.value, 10) || 0;
    if (currentValue <= 0) return;
    fisNoInput.value = currentValue - 1;
    M.dekevrakOku();
  };

  M.dekilerifisNo = function () {
    const fisNoInput = M.byId("evrakNo");
    if (!fisNoInput) return;
    const currentValue = parseInt(fisNoInput.value, 10) || 0;
    fisNoInput.value = currentValue + 1;
    M.dekevrakOku();
  };

  /* ==========================
     FİŞ KAYIT
     ========================== */
  M.dekfisKayit = async function () {
    const fisNoInput = M.byId("evrakNo");
    if (!fisNoInput || ["0", ""].includes(fisNoInput.value)) return;

    M.clearError();
    M.setBusy(true);
    M.setBtn("dekkaydetButton", true, "İşleniyor...");

    try {
      const dekontDTO = {
        fisNo: fisNoInput.value,
        tar: getFullDateWithTimeAndMilliseconds(M.getValue("dekontTarih")),
        bhes: M.getValue("borcHesap"),
        bcins: M.getValue("borcKurCins"),
        bkur: parseLocaleNumber(M.getValue("borcKur")),
        borc: parseLocaleNumber(M.getValue("borcTutar")),
        ahes: M.getValue("alacakHesap"),
        acins: M.getValue("alacakKurCins"),
        akur: parseLocaleNumber(M.getValue("alacakKur")),
        alacak: parseLocaleNumber(M.getValue("alacakTutar")),
        izahat: M.getValue("aciklama"),
        kod: M.getValue("kodu"),
      };

      const dto = await fetchWithSessionCheck("cari/fiskayit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dekontDTO),
      });

      if (dto?.errorMessage) throw new Error(dto.errorMessage);

      M.dekclearInputs();
      M.dekdisableInputs();
      fisNoInput.value = "0";
    } catch (e) {
      M.showError(e?.message || "Bir hata oluştu.");
    } finally {
      M.setBusy(false);
      M.setBtn("dekkaydetButton", false, "Kaydet");
    }
  };

  /* ==========================
     YENİ FİŞ NO
     ========================== */
  M.dekyenifisNo = async function () {
    try {
      M.setBusy(true);
      M.clearError();

      const data = await fetchWithSessionCheck("cari/yenifisNo", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });

      if (data?.errorMessage) throw new Error(data.errorMessage);

      M.setValue("evrakNo", data.fisNo);
      M.dekclearInputs();
      M.dekenableInputs();
    } catch (e) {
      M.showError(e?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
      M.setBusy(false);
    }
  };

  /* ==========================
     EVRAK OKU (Enter ile)
     ========================== */
  M.checkEnter = function (event) {
    if (event.key === "Enter" || event.keyCode === 13) {
      M.dekevrakOku();
    }
  };

  M.dekevrakOku = async function () {
    const evrakNo = M.getValue("evrakNo");
    if (!evrakNo || evrakNo === "0") {
      alert("Lütfen geçerli bir evrak numarası girin!");
      return;
    }

    M.dekclearInputs();
    M.clearError();
    M.setBusy(true);

    try {
      const data = await fetchWithSessionCheck("cari/evrakOku", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ evrakNo }),
      });

      const dto1 = data?.[0];
      if (dto1?.errorMessage) {
        M.showError(dto1.errorMessage);
        M.dekdisableInputs();
        return;
      }

      M.dekclearInputs();

      M.setValue("dekontTarih", dto1?.tar || "");
      M.setValue("borcHesap", dto1?.bhes || "");
      M.setValue("borcKurCins", dto1?.bcins || "");
      M.setValue("borcKur", formatNumber4(dto1?.bkur));
      M.setValue("borcTutar", formatNumber2(dto1?.borc));

      const dto2 = data?.[1] || {};
      M.setValue("alacakHesap", dto2?.ahes || "");
      M.setValue("alacakKurCins", dto2?.acins || "");
      M.setValue("alacakKur", formatNumber4(dto2?.akur));
      M.setValue("alacakTutar", formatNumber2(dto2?.alacak));
      M.setValue("aciklama", dto2?.izahat || "");
      M.setValue("kodu", dto2?.kod || "");

      // input event’lerini tetikle (hesap adı label vs.)
      const bhes = M.byId("borcHesap");
      if (bhes && typeof bhes.oninput === "function") bhes.oninput();

      const ahes = M.byId("alacakHesap");
      if (ahes && typeof ahes.oninput === "function") ahes.oninput();

      M.dekenableInputs();
      M.clearError();
    } catch (e) {
      M.showError(e?.message || "Beklenmeyen bir hata oluştu.");
      M.dekdisableInputs();
    } finally {
      M.setBusy(false);
    }
  };

  /* ==========================
     EVRAK SİL
     ========================== */
  M.dekfisYoket = async function () {
    const fisNoInput = M.byId("evrakNo");
    if (!fisNoInput || ["0", ""].includes(fisNoInput.value)) return;

    if (!confirm("Bu evrak numarasını silmek istediğinize emin misiniz?")) return;

    M.clearError();
    M.setBusy(true);
    M.setBtn("deksilButton", true, "Siliniyor...");

    try {
      const dekontNo = parseInt(fisNoInput.value, 10);

      const dto = await fetchWithSessionCheck("cari/fisYoket", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ evrakNo: dekontNo }),
      });

      if (dto?.errorMessage) throw new Error(dto.errorMessage);

      M.dekclearInputs();
      M.dekdisableInputs();
      fisNoInput.value = "0";
      M.clearError();
    } catch (e) {
      M.showError(e?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
      M.setBusy(false);
      M.setBtn("deksilButton", false, "Sil");
    }
  };

  /* ==========================
     SADECE NUMARA
     ========================== */
  M.allowOnlyNumbers = function (event) {
    const allowedKeys = ["Backspace", "Tab", "ArrowLeft", "ArrowRight", "Delete", "Enter"];
    if (allowedKeys.includes(event.key)) return;
    if (!/^[0-9]$/.test(event.key)) event.preventDefault();
  };

  /* ==========================
     KUR OKU
     ========================== */
  M.dekkurOku = async function (dvz_turu, cinsElement, targetInput) {
    const dvz_tur = M.byId(dvz_turu)?.textContent ?? "";
    const cin = cinsElement?.value ?? "";
    const yazilacakInput = M.byId(targetInput);
    if (!yazilacakInput) return;

    M.clearError();
    M.setBusy(true);

    try {
      const kurgirisDTO = {
        dvz_cins: cin,
        dvz_turu: dvz_tur,
        tar: M.getValue("dekontTarih"),
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
      M.showError("Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.");
    } finally {
      M.setBusy(false);
    }
  };

  /* ==========================
     (İstersen init koyarız)
     ========================== */
  M.init = function () {
    // sayfa ilk açılışta gerekiyorsa buraya yaz
    // örn: M.dekdisableInputs();
  };
})(OBS.DEKONT);
