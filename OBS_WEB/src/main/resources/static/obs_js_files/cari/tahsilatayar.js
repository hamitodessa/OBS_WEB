window.OBS = window.OBS || {};
OBS.TAHAYAR = OBS.TAHAYAR || {};

/* =========================
   helpers
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
   file size validation
   ========================= */
OBS.TAHAYAR.handleFileChange = function (event) {
  const input = event?.target;
  if (!input) return;

  const file = input.files?.[0];
  const maxSizeInKB = 500;
  const maxSizeInBytes = maxSizeInKB * 1024;

  // errorDiv yoksa da patlamasın
  const errorDiv = OBS.TAHAYAR._el("errorDiv");

  if (file && file.size > maxSizeInBytes) {
    if (errorDiv) {
      errorDiv.innerText = `Dosya boyutu ${maxSizeInKB} KB'ı geçemez!`;
      errorDiv.style.display = "block";
    }
    input.value = ""; // seçimi sıfırla
  } else {
    if (errorDiv) {
      errorDiv.style.display = "none";
      errorDiv.innerText = "";
    }
  }
};

OBS.TAHAYAR._bindFileEvents = function () {
  const logoInput = OBS.TAHAYAR._el("imageLogo");
  if (logoInput) {
    logoInput.addEventListener("change", (e) =>
      OBS.TAHAYAR.handleFileChangeWithPreview(e, "resimLogo")
    );
  }

  const kaseInput = OBS.TAHAYAR._el("imageKase");
  if (kaseInput) {
    kaseInput.addEventListener("change", (e) =>
      OBS.TAHAYAR.handleFileChangeWithPreview(e, "resimKase")
    );
  }
};


/* =========================
   dto
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
   clear
   ========================= */
OBS.TAHAYAR.clearInputs = function () {
  const root = OBS.TAHAYAR._el("tahayar_content") || document;
  root.querySelectorAll(".form-control").forEach(inp => { inp.value = ""; });

  const imgLogo = OBS.TAHAYAR._el("resimLogo");
  if (imgLogo) { imgLogo.src = ""; imgLogo.style.display = "none"; }

  const imgKase = OBS.TAHAYAR._el("resimKase");
  if (imgKase) { imgKase.src = ""; imgKase.style.display = "none"; }
};

/* =========================
   save
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
   load first
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

    OBS.TAHAYAR.clearInputs();

    OBS.TAHAYAR._el("adi").value   = dto.adi || "";
    OBS.TAHAYAR._el("adr1").value  = dto.ad1 || "";
    OBS.TAHAYAR._el("adr2").value  = dto.ad2 || "";
    OBS.TAHAYAR._el("vdvn").value  = dto.vdvn || "";
    OBS.TAHAYAR._el("mail").value  = dto.mail || "";
    OBS.TAHAYAR._el("diger").value = dto.diger || "";

    OBS.TAHAYAR._setImg("resimLogo", dto.base64Resimlogo);
    OBS.TAHAYAR._setImg("resimKase", dto.base64Resimkase);

  } catch (err) {
    OBS.TAHAYAR._showError(err?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    document.body.style.cursor = "default";
  }
};

/* =========================
   delete img preview
   ========================= */
OBS.TAHAYAR.logoSil = function () {
  const img = OBS.TAHAYAR._el("resimLogo");
  if (!img) return;
  img.src = "";
  img.style.display = "none";
};

OBS.TAHAYAR.kaseSil = function () {
  const img = OBS.TAHAYAR._el("resimKase");
  if (!img) return;
  img.src = "";
  img.style.display = "none";
};


/* =========================
   file change + preview
   ========================= */
OBS.TAHAYAR.handleFileChangeWithPreview = function (event, previewImgId) {
  const input = event?.target;
  if (!input) return;

  const file = input.files?.[0];
  const maxSizeInKB = 500;
  const maxSizeInBytes = maxSizeInKB * 1024;
  const errorDiv = OBS.TAHAYAR._el("errorDiv");

  if (file && file.size > maxSizeInBytes) {
    if (errorDiv) {
      errorDiv.innerText = `Dosya boyutu ${maxSizeInKB} KB'ı geçemez!`;
      errorDiv.style.display = "block";
    }
    input.value = "";
    return;
  }

  if (errorDiv) {
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
  }

  // preview
  if (!file) return;
  const reader = new FileReader();
  reader.onload = function (e) {
    const img = OBS.TAHAYAR._el(previewImgId);
    if (!img) return;
    img.src = e.target.result;
    img.style.display = "block";
  };
  reader.readAsDataURL(file);
};

/* =========================
   sayfa init
   çağır: OBS.TAHAYAR.init();
   ========================= */
OBS.TAHAYAR.init = function () {
  // input change eventlerini bağla (dinamik sayfa geldiğinde)
  OBS.TAHAYAR._bindFileEvents();

  // sayfa açılınca ilk yükle
  OBS.TAHAYAR.tahayarIlk();
};
