window.OBS = window.OBS || {};
OBS.TAHAYAR = OBS.TAHAYAR || {};

/* =========================
   HELPERS (tek yer)
   ========================= */
OBS.TAHAYAR._el = (id) => document.getElementById(id);

OBS.TAHAYAR._clearError = function () {
  const e = OBS.TAHAYAR._el("errorDiv");
  if (!e) return;
  e.style.display = "none";
  e.innerText = "";
};

OBS.TAHAYAR._showError = function (msg) {
  const e = OBS.TAHAYAR._el("errorDiv");
  if (!e) return;
  e.style.display = "block";
  e.innerText = msg || "Beklenmeyen hata.";
};

OBS.TAHAYAR._setImg = function (imgId, base64) {
  const img = OBS.TAHAYAR._el(imgId);
  if (!img) return;

  if (base64 && base64.trim() !== "") {
    img.src = "data:image/jpeg;base64," + base64.trim();
    img.style.display = "block";
  } else {
    img.src = "";
    img.style.display = "none";
  }
};

OBS.TAHAYAR._setFileName = function (nameSpanId, text) {
  const el = OBS.TAHAYAR._el(nameSpanId);
  if (!el) return;
  el.textContent = text || "Dosya seçilmedi";
};

OBS.TAHAYAR._resetFile = function (inputId, previewImgId, nameSpanId) {
  const inp = OBS.TAHAYAR._el(inputId);
  if (inp) inp.value = "";

  const img = OBS.TAHAYAR._el(previewImgId);
  if (img) {
    img.src = "";
    img.style.display = "none";
  }

  OBS.TAHAYAR._setFileName(nameSpanId, "Dosya seçilmedi");
};

OBS.TAHAYAR._appendBase64ImgAsBlob = function (formData, imgId, formKey, filename) {
  const img = OBS.TAHAYAR._el(imgId);
  if (!img) return;

  const src = img.src || "";
  if (!src.startsWith("data:image")) return;

  const base64 = src.split(",")[1];
  if (!base64) return;

  const byteCharacters = atob(base64);
  const byteNumbers = new Array(byteCharacters.length);
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }
  const byteArray = new Uint8Array(byteNumbers);
  const blob = new Blob([byteArray], { type: "image/jpeg" });

  formData.append(formKey, blob, filename);
};

/* =========================
   FILE CHANGE (tek akış)
   ========================= */
OBS.TAHAYAR._MAX_KB = 500;

OBS.TAHAYAR._onFileChanged = function (inputId, previewImgId, nameSpanId) {
  const input = OBS.TAHAYAR._el(inputId);
  if (!input) return;

  const file = input.files?.[0];
  if (!file) {
    OBS.TAHAYAR._setFileName(nameSpanId, "Dosya seçilmedi");
    return;
  }

  const maxBytes = OBS.TAHAYAR._MAX_KB * 1024;
  if (file.size > maxBytes) {
    OBS.TAHAYAR._showError(`Dosya boyutu ${OBS.TAHAYAR._MAX_KB} KB'ı geçemez!`);
    // her şeyi sıfırla
    OBS.TAHAYAR._resetFile(inputId, previewImgId, nameSpanId);
    return;
  }

  OBS.TAHAYAR._clearError();
  OBS.TAHAYAR._setFileName(nameSpanId, file.name);

  // preview
  const reader = new FileReader();
  reader.onload = function (e) {
    const img = OBS.TAHAYAR._el(previewImgId);
    if (!img) return;
    img.src = e.target.result;
    img.style.display = "block";
  };
  reader.readAsDataURL(file);
};

OBS.TAHAYAR._bindFileEvents = function () {
  const logoInput = OBS.TAHAYAR._el("imageLogo");
  if (logoInput && logoInput.dataset.bound !== "1") {
    logoInput.dataset.bound = "1";
    logoInput.addEventListener("change", () =>
      OBS.TAHAYAR._onFileChanged("imageLogo", "resimLogo", "logoFileName")
    );
  }

  const kaseInput = OBS.TAHAYAR._el("imageKase");
  if (kaseInput && kaseInput.dataset.bound !== "1") {
    kaseInput.dataset.bound = "1";
    kaseInput.addEventListener("change", () =>
      OBS.TAHAYAR._onFileChanged("imageKase", "resimKase", "kaseFileName")
    );
  }
};

/* =========================
   DTO
   ========================= */
OBS.TAHAYAR.gettahayarDTO = function () {
  return {
    adi:   OBS.TAHAYAR._el("adi")?.value || "",
    ad1:   OBS.TAHAYAR._el("adr1")?.value || "",
    ad2:   OBS.TAHAYAR._el("adr2")?.value || "",
    vdvn:  OBS.TAHAYAR._el("vdvn")?.value || "",
    mail:  OBS.TAHAYAR._el("mail")?.value || "",
    diger: OBS.TAHAYAR._el("diger")?.value || "",
  };
};

/* =========================
   CLEAR
   ========================= */
OBS.TAHAYAR.clearInputs = function () {
  const root = OBS.TAHAYAR._el("tahayar_content") || document;
  root.querySelectorAll(".form-control").forEach(inp => { inp.value = ""; });

  // preview + file ui temizle
  OBS.TAHAYAR._resetFile("imageLogo", "resimLogo", "logoFileName");
  OBS.TAHAYAR._resetFile("imageKase", "resimKase", "kaseFileName");
};

/* =========================
   SAVE
   ========================= */
OBS.TAHAYAR.ayarKayit = async function () {
  const adi = OBS.TAHAYAR._el("adi");
  if (!adi || adi.value.trim() === "") return;

  OBS.TAHAYAR._clearError();

  const dto = OBS.TAHAYAR.gettahayarDTO();
  const formData = new FormData();
  Object.keys(dto).forEach(k => formData.append(k, dto[k]));

  const fileLogo = OBS.TAHAYAR._el("imageLogo")?.files?.[0];
  if (fileLogo) formData.append("resimlogo", fileLogo);

  const fileKase = OBS.TAHAYAR._el("imageKase")?.files?.[0];
  if (fileKase) formData.append("resimkase", fileKase);

  // Eğer kullanıcı dosya seçmemiş ama preview'da base64 var ise onu da gönder
  OBS.TAHAYAR._appendBase64ImgAsBlob(formData, "resimLogo", "resimgosterlogo", "base64ResimLogo.jpg");
  OBS.TAHAYAR._appendBase64ImgAsBlob(formData, "resimKase", "resimgosterkase", "base64ResimKase.jpg");

  document.body.style.cursor = "wait";
  try {
    const res = await fetchWithSessionCheck("cari/tahayarkayit", {
      method: "POST",
      body: formData
    });

    if (res?.errorMessage) throw new Error(res.errorMessage);

    await OBS.TAHAYAR.tahayarIlk();
  } catch (err) {
    OBS.TAHAYAR._showError(err?.message || String(err));
  } finally {
    document.body.style.cursor = "default";
  }
};

/* =========================
   LOAD FIRST
   ========================= */
OBS.TAHAYAR.tahayarIlk = async function () {
  OBS.TAHAYAR._clearError();
  document.body.style.cursor = "wait";

  try {
    const dto = await fetchWithSessionCheck("cari/tahayarYukle", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
    });

    if (dto?.errorMessage) throw new Error(dto.errorMessage);

    // inputlar
    OBS.TAHAYAR._el("adi").value   = dto.adi || "";
    OBS.TAHAYAR._el("adr1").value  = dto.ad1 || "";
    OBS.TAHAYAR._el("adr2").value  = dto.ad2 || "";
    OBS.TAHAYAR._el("vdvn").value  = dto.vdvn || "";
    OBS.TAHAYAR._el("mail").value  = dto.mail || "";
    OBS.TAHAYAR._el("diger").value = dto.diger || "";

    // resimler
    OBS.TAHAYAR._setImg("resimLogo", dto.base64Resimlogo);
    OBS.TAHAYAR._setImg("resimKase", dto.base64Resimkase);

    // dosya adlarını resetle (serverdan geldiyse bile dosya seçilmedi say)
    OBS.TAHAYAR._setFileName("logoFileName", "Dosya seçilmedi");
    OBS.TAHAYAR._setFileName("kaseFileName", "Dosya seçilmedi");

    // input file temiz kalsın
    const il = OBS.TAHAYAR._el("imageLogo"); if (il) il.value = "";
    const ik = OBS.TAHAYAR._el("imageKase"); if (ik) ik.value = "";

  } catch (err) {
    OBS.TAHAYAR._showError(err?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    document.body.style.cursor = "default";
  }
};

/* =========================
   DELETE PREVIEW (UI ile beraber)
   ========================= */
OBS.TAHAYAR.logoSil = function () {
  OBS.TAHAYAR._clearError();
  OBS.TAHAYAR._resetFile("imageLogo", "resimLogo", "logoFileName");
};

OBS.TAHAYAR.kaseSil = function () {
  OBS.TAHAYAR._clearError();
  OBS.TAHAYAR._resetFile("imageKase", "resimKase", "kaseFileName");
};

/* =========================
   INIT
   çağır: OBS.TAHAYAR.init();
   ========================= */
OBS.TAHAYAR.init = function () {
  OBS.TAHAYAR._bindFileEvents();
  OBS.TAHAYAR.tahayarIlk();
};
