window.OBS = window.OBS || {};
OBS.HSPPLN = OBS.HSPPLN || {};

/* =========================
   HELPERS
   ========================= */
OBS.HSPPLN._el = (id) => document.getElementById(id);

OBS.HSPPLN._showError = function (msg) {
  const e = OBS.HSPPLN._el("errorDiv");
  if (!e) return;
  e.style.display = "block";
  e.innerText = msg || "Beklenmeyen hata.";
};

OBS.HSPPLN._clearError = function () {
  const e = OBS.HSPPLN._el("errorDiv");
  if (!e) return;
  e.style.display = "none";
  e.innerText = "";
};

OBS.HSPPLN._setFileName = function (text) {
  const el = OBS.HSPPLN._el("resimFileName");
  if (!el) return;
  el.textContent = text || "Dosya seçilmedi";
};

OBS.HSPPLN._resetFileUI = function () {
  const inp = OBS.HSPPLN._el("resim");
  if (inp) inp.value = "";

  const img = OBS.HSPPLN._el("resimGoster");
  if (img) {
    img.src = "";
    img.style.display = "none";
  }

  OBS.HSPPLN._setFileName("Dosya seçilmedi");
};

OBS.HSPPLN._setImgBase64 = function (base64) {
  const img = OBS.HSPPLN._el("resimGoster");
  if (!img) return;

  if (base64 && base64.trim() !== "") {
    img.src = "data:image/jpeg;base64," + base64.trim();
    img.style.display = "block";
  } else {
    img.src = "";
    img.style.display = "none";
  }
};

OBS.HSPPLN._appendBase64ImgAsBlob = function (formData, imgId, formKey, filename) {
  const img = OBS.HSPPLN._el(imgId);
  if (!img) return;

  const src = img.src || "";
  if (!src.startsWith("data:image")) return;

  const base64 = src.split(",")[1];
  if (!base64) return;

  const byteCharacters = atob(base64);
  const byteNumbers = new Array(byteCharacters.length);
  for (let i = 0; i < byteCharacters.length; i++) byteNumbers[i] = byteCharacters.charCodeAt(i);
  const byteArray = new Uint8Array(byteNumbers);

  const blob = new Blob([byteArray], { type: "image/jpeg" });
  formData.append(formKey, blob, filename);
};

/* =========================
   FILE (500KB + preview)
   ========================= */
OBS.HSPPLN._MAX_KB = 500;

OBS.HSPPLN.onFileChanged = function () {
  const input = OBS.HSPPLN._el("resim");
  if (!input) return;

  const file = input.files?.[0];
  if (!file) {
    OBS.HSPPLN._setFileName("Dosya seçilmedi");
    return;
  }

  if (file.type && !file.type.startsWith("image/")) {
    OBS.HSPPLN._showError("Lütfen bir resim dosyası seçin!");
    OBS.HSPPLN._resetFileUI();
    return;
  }

  const maxBytes = OBS.HSPPLN._MAX_KB * 1024;
  if (file.size > maxBytes) {
    OBS.HSPPLN._showError(`Dosya boyutu ${OBS.HSPPLN._MAX_KB} KB'ı geçemez!`);
    OBS.HSPPLN._resetFileUI();
    return;
  }

  OBS.HSPPLN._clearError();
  OBS.HSPPLN._setFileName(file.name);

  // preview
  const reader = new FileReader();
  reader.onload = function (e) {
    const img = OBS.HSPPLN._el("resimGoster");
    if (!img) return;
    img.src = e.target.result;
    img.style.display = "block";

    /*
    img.style.objectFit = "contain";
    img.style.mixBlendMode = "normal";
    img.style.visibility = "visible";
    img.style.opacity = "1";
		*/
  };
  reader.readAsDataURL(file);
};

OBS.HSPPLN.resimSil = function () {
  OBS.HSPPLN._clearError();
  OBS.HSPPLN._resetFileUI();
};

/* =========================
   DTO
   ========================= */
OBS.HSPPLN.getDTO = function () {
  const v = (id) => OBS.HSPPLN._el(id)?.value || "";
  const c = (id) => !!OBS.HSPPLN._el(id)?.checked;

  return {
    kodu: v("kodu"),
    adi: v("adi"),
    karton: v("karton"),
    hcins: v("hcins"),
    yetkili: v("yetkili"),
    ad1: v("ad1"),
    ad2: v("ad2"),
    semt: v("semt"),
    seh: v("seh"),
    vd: v("vd"),
    vn: v("vn"),
    t1: v("t1"),
    t2: v("t2"),
    t3: v("t3"),
    fx: v("fx"),
    o1: v("o1"),
    o2: v("o2"),
    o3: v("o3"),
    web: v("web"),
    mail: v("mail"),
    kim: v("kim"),
    acik: v("acik"),
    sms: c("sms")
  };
};

/* =========================
   INPUT STATE
   ========================= */
OBS.HSPPLN._inputs = () => document.querySelectorAll("#ara_content .form-control, #ara_content .form-check-input");

OBS.HSPPLN.clearInputs = function () {
  OBS.HSPPLN._inputs().forEach((el) => {
    if (el.type === "checkbox") el.checked = false;
    else el.value = "";
  });

  const kk = OBS.HSPPLN._el("kodKontrol");
  if (kk) kk.innerText = "";

  OBS.HSPPLN._clearError();
  OBS.HSPPLN._resetFileUI();
};

OBS.HSPPLN.disableInputs = function () {
  OBS.HSPPLN._inputs().forEach((el) => { if (el.id !== "arama") el.disabled = true; });
};

OBS.HSPPLN.enableInputs = function () {
  OBS.HSPPLN.clearInputs();
  OBS.HSPPLN._inputs().forEach((el) => (el.disabled = false));
};

OBS.HSPPLN.enableDuzeltmeInputs = function () {
  OBS.HSPPLN._inputs().forEach((el) => { if (el.id !== "kodu") el.disabled = false; });
};

/* =========================
   KAYIT
   ========================= */
OBS.HSPPLN.kayit = async function () {
  const kodu = OBS.HSPPLN._el("kodu")?.value || "";
  if (!kodu || kodu === "0") return;

  OBS.HSPPLN._clearError();

  const dto = OBS.HSPPLN.getDTO();
  const formData = new FormData();
  Object.keys(dto).forEach((k) => formData.append(k, dto[k]));

  const file = OBS.HSPPLN._el("resim")?.files?.[0];
  if (file) formData.append("resim", file);

  // dosya seçmediyse ama ekranda base64 resim varsa onu da gönder
  OBS.HSPPLN._appendBase64ImgAsBlob(formData, "resimGoster", "resimGoster", "base64Resim.jpg");

  document.body.style.cursor = "wait";
  try {
    const resp = await fetchWithSessionCheck("cari/hsplnkayit", {
      method: "POST",
      body: formData
    });
    if (resp?.errorMessage) throw new Error(resp.errorMessage);

    window.sayfaYukle("/cari/hspplngiris");
  } catch (e) {
    OBS.HSPPLN._showError(e?.message || "Beklenmeyen hata.");
  } finally {
    document.body.style.cursor = "default";
  }
};

/* =========================
   ARAMA
   ========================= */
OBS.HSPPLN.aramaYap = async function () {
  const kk = OBS.HSPPLN._el("kodKontrol");
  if (kk) kk.innerText = "";

  OBS.HSPPLN._clearError();

  const aramaVal = OBS.HSPPLN._el("arama")?.value || "";
  if (!aramaVal) return;

  document.body.style.cursor = "wait";
  try {
    const dto = await fetchWithSessionCheck("cari/hsplnArama", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ arama: aramaVal })
    });

    if (dto?.errorMessage) throw new Error(dto.errorMessage);

    const setVal = (id, val) => {
      const el = OBS.HSPPLN._el(id);
      if (el) el.value = val || "";
    };

    setVal("kodu", dto.kodu);
    setVal("adi", dto.adi);
    setVal("karton", dto.karton);
    setVal("hcins", dto.hcins);
    setVal("yetkili", dto.yetkili);
    setVal("ad1", dto.ad1);
    setVal("ad2", dto.ad2);
    setVal("semt", dto.semt);
    setVal("seh", dto.seh);
    setVal("vd", dto.vd);
    setVal("vn", dto.vn);
    setVal("t1", dto.t1);
    setVal("t2", dto.t2);
    setVal("t3", dto.t3);
    setVal("fx", dto.fx);
    setVal("o1", dto.o1);
    setVal("o2", dto.o2);
    setVal("o3", dto.o3);
    setVal("web", dto.web);
    setVal("mail", dto.mail);
    setVal("kim", dto.kim);
    setVal("acik", dto.acik);

    const sms = OBS.HSPPLN._el("sms");
    if (sms) sms.checked = !!dto.sms;

    // file input temiz kalsın
    const inp = OBS.HSPPLN._el("resim");
    if (inp) inp.value = "";
    OBS.HSPPLN._setFileName("Dosya seçilmedi");

    // resim (server base64)
    OBS.HSPPLN._setImgBase64(dto.base64Resim);

    OBS.HSPPLN.disableInputs();
    OBS.HSPPLN.enableDuzeltmeInputs();
  } catch (e) {
    OBS.HSPPLN._showError(e?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    document.body.style.cursor = "default";
  }
};

/* =========================
   NAV
   ========================= */
OBS.HSPPLN._options = function () {
  const dl = OBS.HSPPLN._el("hesapOptions");
  return dl ? Array.from(dl.getElementsByTagName("option")) : [];
};

OBS.HSPPLN.ilk = function () {
  const opts = OBS.HSPPLN._options();
  if (opts.length === 0) return;
  const arama = OBS.HSPPLN._el("arama");
  if (!arama) return;
  arama.value = opts[0].value;
  OBS.HSPPLN.aramaYap();
  arama.value = "";
};

OBS.HSPPLN.geri = function () {
  const kodu = OBS.HSPPLN._el("kodu")?.value || "";
  const opts = OBS.HSPPLN._options();
  if (!kodu || opts.length === 0) return;

  const idx = opts.findIndex(o => o.value === kodu);
  if (idx > 0) {
    const arama = OBS.HSPPLN._el("arama");
    arama.value = opts[idx - 1].value;
    OBS.HSPPLN.aramaYap();
    arama.value = "";
  }
};

OBS.HSPPLN.ileri = function () {
  const kodu = OBS.HSPPLN._el("kodu")?.value || "";
  const opts = OBS.HSPPLN._options();
  if (!kodu || opts.length === 0) return;

  const idx = opts.findIndex(o => o.value === kodu);
  if (idx !== -1 && idx < opts.length - 1) {
    const arama = OBS.HSPPLN._el("arama");
    arama.value = opts[idx + 1].value;
    OBS.HSPPLN.aramaYap();
    arama.value = "";
  }
};

/* =========================
   SİL
   ========================= */
OBS.HSPPLN.hesapSil = async function () {
  const hesapKodu = OBS.HSPPLN._el("kodu")?.value || "";
  if (!hesapKodu || hesapKodu === "0") return;

  const msg =
    "Kayit Dosyadan Silinecek ..?\n\n" +
    "Oncelikle Bu Hesaba Ait Fisleri Silmeniz\n\n" +
    "Tavsiye Olunur ....";

  if (!confirm(msg)) return;

  OBS.HSPPLN._clearError();
  document.body.style.cursor = "wait";
  try {
    const dto = await fetchWithSessionCheck("cari/hspplnSil", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ hesapKodu })
    });
    if (dto?.errorMessage) throw new Error(dto.errorMessage);

    window.sayfaYukle("/cari/hspplngiris");
  } catch (e) {
    OBS.HSPPLN._showError(e?.message || "Beklenmeyen hata");
  } finally {
    document.body.style.cursor = "default";
  }
};


OBS.HSPPLN._bindFileEvents = function () {
  const logoInput = OBS.HSPPLN._el("resim");
  if (logoInput && logoInput.dataset.bound !== "1") {
    logoInput.dataset.bound = "1";
    logoInput.addEventListener("change", OBS.HSPPLN.onFileChanged);
  }

};

/* =========================
   INIT
   ========================= */
OBS.HSPPLN.init = function () {
  OBS.HSPPLN._bindFileEvents();
  OBS.HSPPLN._setFileName("Dosya seçilmedi");
};
