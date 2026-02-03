/* =========================
   USER UPDATE (VANILLA)
   Image preview + submit
   Namespace: OBS.USERUPD
   ========================= */
window.OBS ||= {};
OBS.USERUPD ||= {};

OBS.USERUPD._root = () =>
  document.getElementById("userupd_content") ||  // varsa wrapper
  document.getElementById("ara_content")     ||
  document;

OBS.USERUPD.byId = (id) => OBS.USERUPD._root().querySelector("#" + id);

OBS.USERUPD._state = {
  currentObjectUrl: null
};

/* ---------- msg box ---------- */
OBS.USERUPD.showMsg = function (text, type) {
  const el = OBS.USERUPD.byId("msgBox") || document.getElementById("msgBox");
  if (!el) return;
  el.classList.remove("success", "error", "visible");
  el.classList.add(type === "success" ? "success" : "error");
  el.textContent = text || (type === "success" ? "Başarılı" : "Hata");
  void el.offsetWidth;
  el.classList.add("visible");
};

OBS.USERUPD.hideMsg = function () {
  const el = OBS.USERUPD.byId("msgBox") || document.getElementById("msgBox");
  if (!el) return;
  el.classList.remove("success", "error", "visible");
  el.textContent = "";
};

/* ---------- image preview ---------- */
OBS.USERUPD.resetPreview = function () {
  const fileInput  = OBS.USERUPD.byId("image");
  const imgPreview = OBS.USERUPD.byId("imgPreview");
  const imgPh      = OBS.USERUPD.byId("imgPh");
  const fileNameEl = OBS.USERUPD.byId("fileName");
  const clearBtn   = OBS.USERUPD.byId("clearImgBtn");

  if (OBS.USERUPD._state.currentObjectUrl) {
    URL.revokeObjectURL(OBS.USERUPD._state.currentObjectUrl);
    OBS.USERUPD._state.currentObjectUrl = null;
  }

  if (imgPreview) { imgPreview.style.display = "none"; imgPreview.src = ""; }
  if (imgPh) imgPh.style.display = "block";
  if (fileNameEl) fileNameEl.textContent = "Dosya seçilmedi";
  if (clearBtn) clearBtn.style.display = "none";
  if (fileInput) fileInput.value = "";
};

OBS.USERUPD._onImageChange = function (event) {
  const fileInput  = OBS.USERUPD.byId("image");
  const imgPreview = OBS.USERUPD.byId("imgPreview");
  const imgPh      = OBS.USERUPD.byId("imgPh");
  const fileNameEl = OBS.USERUPD.byId("fileName");
  const clearBtn   = OBS.USERUPD.byId("clearImgBtn");

  const file = event.target?.files?.[0] || null;

  if (!file) {
    OBS.USERUPD.resetPreview();
    OBS.USERUPD.hideMsg();
    return;
  }

  // 500KB kontrol
  const maxSizeInKB = 500;
  const maxBytes = maxSizeInKB * 1024;

  if (file.size > maxBytes) {
    OBS.USERUPD.showMsg(`Dosya boyutu ${maxSizeInKB} KB'ı geçemez!`, "error");
    if (fileInput) fileInput.value = "";
    OBS.USERUPD.resetPreview();
    return;
  }

  OBS.USERUPD.hideMsg();

  if (fileNameEl) fileNameEl.textContent = file.name;

  if (OBS.USERUPD._state.currentObjectUrl) {
    URL.revokeObjectURL(OBS.USERUPD._state.currentObjectUrl);
  }
  OBS.USERUPD._state.currentObjectUrl = URL.createObjectURL(file);

  if (imgPreview) {
    imgPreview.src = OBS.USERUPD._state.currentObjectUrl;
    imgPreview.style.display = "block";
  }
  if (imgPh) imgPh.style.display = "none";
  if (clearBtn) clearBtn.style.display = "inline-block";
};

OBS.USERUPD._onClearClick = function () {
  OBS.USERUPD.resetPreview();
  OBS.USERUPD.hideMsg();
};

/* ---------- submit (delegation) ---------- */
OBS.USERUPD._onSubmit = async function (event) {
  const form = event.target;
  if (!form || form.id !== "updateForm2026") return;

  event.preventDefault();

  const btn  = OBS.USERUPD.byId("submitBtn");
  const spin = OBS.USERUPD.byId("btnSpin");
  const text = OBS.USERUPD.byId("btnText");

  OBS.USERUPD.hideMsg();

  if (btn) btn.disabled = true;
  if (spin) spin.style.display = "inline-block";
  if (text) text.textContent = "İşleniyor...";

  try {
    const formData = new FormData(form);

    const result = await fetchWithSessionCheck("user/user_update", {
      method: "POST",
      body: formData
    });

    const err = result?.errorMessage?.trim();
    const okm = result?.message?.trim();

    if (err) OBS.USERUPD.showMsg(err, "error");
    else if (okm) OBS.USERUPD.showMsg(okm, "success");
    else OBS.USERUPD.showMsg("Kaydedildi.", "success");
     
		window.location.href = '/index';
  } catch (error) {
    OBS.USERUPD.showMsg(error?.message || "Beklenmeyen bir hata oluştu.", "error");
  } finally {
    if (btn) btn.disabled = false;
    if (spin) spin.style.display = "none";
    if (text) text.textContent = "Kaydet";
  }
};

/* ---------- init / destroy ---------- */
OBS.USERUPD.init = function () {

  // image listeners (direct)
  const fileInput = OBS.USERUPD.byId("image");
  const clearBtn  = OBS.USERUPD.byId("clearImgBtn");

  if (fileInput) {
    fileInput.removeEventListener("change", OBS.USERUPD._onImageChange);
    fileInput.addEventListener("change", OBS.USERUPD._onImageChange);
  }
  if (clearBtn) {
    clearBtn.removeEventListener("click", OBS.USERUPD._onClearClick);
    clearBtn.addEventListener("click", OBS.USERUPD._onClearClick);
  }

  // submit delegation (dynamic safe)
  document.removeEventListener("submit", OBS.USERUPD._onSubmit, true);
  document.addEventListener("submit", OBS.USERUPD._onSubmit, true);

  // sayfa ilk açılış temiz başlasın
  OBS.USERUPD.resetPreview();
  OBS.USERUPD.hideMsg();
};

OBS.USERUPD.destroy = function () {
  const fileInput = OBS.USERUPD.byId("image");
  const clearBtn  = OBS.USERUPD.byId("clearImgBtn");

  if (fileInput) fileInput.removeEventListener("change", OBS.USERUPD._onImageChange);
  if (clearBtn) clearBtn.removeEventListener("click", OBS.USERUPD._onClearClick);

  document.removeEventListener("submit", OBS.USERUPD._onSubmit, true);

  if (OBS.USERUPD._state.currentObjectUrl) {
    URL.revokeObjectURL(OBS.USERUPD._state.currentObjectUrl);
    OBS.USERUPD._state.currentObjectUrl = null;
  }
};
