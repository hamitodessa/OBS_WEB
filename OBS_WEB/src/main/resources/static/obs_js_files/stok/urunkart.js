/* =========================
   STOK – URUN KART (çakışmasız)
   Namespace: OBS.URUNKART
   File: /obs_js_files/stok/urunkart.js
   ========================= */
window.OBS ||= {};
OBS.URUNKART ||= {};

/* ---------- helpers ---------- */
OBS.URUNKART._root = () =>
  document.getElementById("hsp_content") ||
  document.getElementById("ara_content") ||
  document;

OBS.URUNKART._el = (id) => document.getElementById(id);

OBS.URUNKART._error = () => OBS.URUNKART._el("errorDiv");

OBS.URUNKART._showError = (msg) => {
  const e = OBS.URUNKART._error();
  if (!e) return;
  e.style.display = "block";
  e.innerText = msg || "Beklenmeyen bir hata oluştu.";
};

OBS.URUNKART._hideError = () => {
  const e = OBS.URUNKART._error();
  if (!e) return;
  e.style.display = "none";
  e.innerText = "";
};

OBS.URUNKART._setCursor = (wait) => {
  document.body.style.cursor = wait ? "wait" : "default";
};

OBS.URUNKART._setImg = (base64) => {
  const img = OBS.URUNKART._el("resimGoster");
  if (!img) return;

  if (base64 && String(base64).trim() !== "") {
    img.src = "data:image/jpeg;base64," + String(base64).trim();
    img.style.display = "block";
  } else {
    img.src = "";
    img.style.display = "none";
  }
};

OBS.URUNKART._getBase64BlobFromImg = () => {
  const img = OBS.URUNKART._el("resimGoster");
  if (!img) return null;

  if (!img.src || !img.src.startsWith("data:image")) return null;

  const base64Data = img.src.split(",")[1];
  if (!base64Data) return null;

  const byteCharacters = atob(base64Data);
  const byteNumbers = new Array(byteCharacters.length);
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }
  const byteArray = new Uint8Array(byteNumbers);
  return new Blob([byteArray], { type: "image/jpeg" });
};

/* ---------- enable/disable/clear ---------- */
OBS.URUNKART.clearInputs = () => {
  const root = OBS.URUNKART._root();

  // input + select + textarea
  root.querySelectorAll("input, select, textarea").forEach((el) => {
    const id = el.id || "";
    if (id === "arama") return;

    if (el.type === "file") el.value = "";
    else el.value = "";
  });

  OBS.URUNKART._setImg("");
  const kk = OBS.URUNKART._el("kodKontrol");
  if (kk) kk.innerText = "";
};

OBS.URUNKART.enableInputs = () => {
  OBS.URUNKART.clearInputs();
  const root = OBS.URUNKART._root();

  root.querySelectorAll(".form-control, .form-select, textarea.form-control").forEach((el) => {
    // arama inputu zaten açık olabilir ama sorun değil
    el.disabled = false;
  });
};

OBS.URUNKART.enableDuzeltmeInputs = () => {
  const root = OBS.URUNKART._root();

  root.querySelectorAll(".form-control, .form-select, textarea.form-control").forEach((el) => {
    if (el.id !== "kodu") el.disabled = false;
  });
};

OBS.URUNKART.disableInputs = () => {
  const root = OBS.URUNKART._root();

  root.querySelectorAll(".form-control, .form-select, textarea.form-control").forEach((el) => {
    if (el.id !== "arama") el.disabled = true;
  });
};

/* ---------- DTO ---------- */
OBS.URUNKART.getDTO = () => ({
  kodu: OBS.URUNKART._el("kodu")?.value || "",
  adi: OBS.URUNKART._el("adi")?.value || "",
  birim: OBS.URUNKART._el("birim")?.value || "",
  kusurat: parseInt(OBS.URUNKART._el("kusurat")?.value, 10) || 0,
  sinif: OBS.URUNKART._el("sinif")?.value || "",
  anagrup: OBS.URUNKART._el("anagrup")?.value || "",
  altgrup: OBS.URUNKART._el("altgrup")?.value || "",
  aciklama1: OBS.URUNKART._el("aciklama1")?.value || "",
  aciklama2: OBS.URUNKART._el("aciklama2")?.value || "",
  ozelkod1: OBS.URUNKART._el("ozelkod1")?.value || "",
  ozelkod2: OBS.URUNKART._el("ozelkod2")?.value || "",
  barkod: OBS.URUNKART._el("barkod")?.value || "",
  mensei: OBS.URUNKART._el("mensei")?.value || "",
  agirlik: parseFloat(OBS.URUNKART._el("agirlik")?.value) || 0.0,
  fiat1: parseFloat(OBS.URUNKART._el("fiat1")?.value) || 0.0,
  fiat2: parseFloat(OBS.URUNKART._el("fiat2")?.value) || 0.0,
  fiat3: parseFloat(OBS.URUNKART._el("fiat3")?.value) || 0.0,
  recete: OBS.URUNKART._el("recete")?.value || ""
});

/* ---------- kayit ---------- */
OBS.URUNKART.kayit = async () => {
  const koduVal = OBS.URUNKART._el("kodu")?.value || "";
  if (koduVal === "" || koduVal === "0") return;

  const dto = OBS.URUNKART.getDTO();
  const formData = new FormData();

  Object.keys(dto).forEach((k) => formData.append(k, dto[k]));

  // file
  const fileInput = OBS.URUNKART._el("resim");
  const file = fileInput?.files?.[0];
  if (file) formData.append("resim", file);

  // mevcut base64 img -> blob
  const blob = OBS.URUNKART._getBase64BlobFromImg();
  if (blob) formData.append("resimGoster", blob, "base64Resim.jpg");

  OBS.URUNKART._hideError();
  OBS.URUNKART._setCursor(true);

  try {
    const response = await fetchWithSessionCheck("stok/urnkayit", {
      method: "POST",
      body: formData
    });
    if (response?.errorMessage) throw new Error(response.errorMessage);

    await OBS.URUNKART.sayfaYukle(); // sayfayı yenile (senin akışın)
    OBS.URUNKART._setCursor(false);
  } catch (err) {
    OBS.URUNKART._setCursor(false);
    OBS.URUNKART._showError(err?.message);
  }
};

/* ---------- arama ---------- */
OBS.URUNKART.aramaYap = async (kodbarkod = "Kodu") => {
  const kk = OBS.URUNKART._el("kodKontrol");
  if (kk) kk.innerText = "";

  const aramaVal = OBS.URUNKART._el("arama")?.value || "";
  if (!aramaVal) return;

  OBS.URUNKART._hideError();
  OBS.URUNKART._setCursor(true);

  try {
    const response = await fetchWithSessionCheck("stok/urnbilgiArama", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ deger: aramaVal, kodbarkod })
    });

    if (response?.errorMessage === "Bu Numarada Kayıtlı Hesap Yok") {
      throw new Error(response.errorMessage);
    }
    if (response?.errorMessage) throw new Error(response.errorMessage);

    const dto = response.urun || {};

    OBS.URUNKART._el("kodu").value = dto.kodu ?? "";
    OBS.URUNKART._el("adi").value = dto.adi ?? "";
    OBS.URUNKART._el("birim").value = dto.birim ?? "";
    OBS.URUNKART._el("kusurat").value = dto.kusurat ?? "";
    OBS.URUNKART._el("sinif").value = dto.sinif ?? "";
    OBS.URUNKART._el("anagrup").value = dto.anagrup ?? "";
    OBS.URUNKART._el("altgrup").value = dto.altgrup ?? "";
    OBS.URUNKART._el("aciklama1").value = dto.aciklama1 ?? "";
    OBS.URUNKART._el("aciklama2").value = dto.aciklama2 ?? "";
    OBS.URUNKART._el("ozelkod1").value = dto.ozelkod1 ?? "";
    OBS.URUNKART._el("ozelkod2").value = dto.ozelkod2 ?? "";
    OBS.URUNKART._el("barkod").value = dto.barkod ?? "";
    OBS.URUNKART._el("mensei").value = dto.mensei ?? "";
    OBS.URUNKART._el("agirlik").value = dto.agirlik ?? "";
    OBS.URUNKART._el("fiat1").value = dto.fiat1 ?? "";
    OBS.URUNKART._el("fiat2").value = dto.fiat2 ?? "";
    OBS.URUNKART._el("fiat3").value = dto.fiat3 ?? "";
    OBS.URUNKART._el("recete").value = dto.recete ?? "";

    OBS.URUNKART._setImg(dto.base64Resim);

    // senin akış: disable -> sonra düzeltme enable
    OBS.URUNKART.disableInputs();
    OBS.URUNKART.enableDuzeltmeInputs();
    OBS.URUNKART._hideError();
  } catch (err) {
    OBS.URUNKART._showError(err?.message);
  } finally {
    OBS.URUNKART._setCursor(false);
  }
};

/* ---------- datalist gez ---------- */
OBS.URUNKART._getOptions = () => {
  const datalist = OBS.URUNKART._el("urnOptions");
  if (!datalist) return [];
  return Array.from(datalist.querySelectorAll("option")).map(o => o.value);
};

OBS.URUNKART.geri = () => {
  const current = OBS.URUNKART._el("kodu")?.value || "";
  const opts = OBS.URUNKART._getOptions();
  if (!opts.length) return;

  const idx = opts.indexOf(current);
  if (idx > 0) {
    const prev = opts[idx - 1];
    OBS.URUNKART._el("arama").value = prev;
    OBS.URUNKART.aramaYap("Kodu");
    OBS.URUNKART._el("arama").value = "";
  }
};

OBS.URUNKART.ileri = () => {
  const current = OBS.URUNKART._el("kodu")?.value || "";
  const opts = OBS.URUNKART._getOptions();
  if (!opts.length) return;

  const idx = opts.indexOf(current);
  if (idx !== -1 && idx < opts.length - 1) {
    const next = opts[idx + 1];
    OBS.URUNKART._el("arama").value = next;
    OBS.URUNKART.aramaYap("Kodu");
    OBS.URUNKART._el("arama").value = "";
  }
};

OBS.URUNKART.ilk = () => {
  const opts = OBS.URUNKART._getOptions();
  if (!opts.length) {
    OBS.URUNKART.clearInputs();
    OBS.URUNKART.disableInputs();
    return;
  }
  OBS.URUNKART._el("arama").value = opts[0];
  OBS.URUNKART.aramaYap("Kodu");
  OBS.URUNKART._el("arama").value = "";
};

/* ---------- sil ---------- */
OBS.URUNKART.sil = async () => {
  const kod = OBS.URUNKART._el("kodu")?.value || "";
  if (kod === "" || kod === "0") return;

  if (!confirm("Kayit Dosyadan Silinecek ..?")) return;

  OBS.URUNKART._hideError();
  OBS.URUNKART._setCursor(true);

  try {
    const response = await fetchWithSessionCheck("stok/urnSil", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ urnkodu: kod })
    });
    if (response?.errorMessage) throw new Error(response.errorMessage);

    await OBS.URUNKART.sayfaYukle();
  } catch (err) {
    OBS.URUNKART._showError(err?.message);
  } finally {
    OBS.URUNKART._setCursor(false);
  }
};

/* ---------- sayfa yenile (senin mevcut düzenin) ---------- */
OBS.URUNKART.sayfaYukle = async () => {
  const url = "stok/urunkart";
  try {
    OBS.URUNKART._setCursor(true);
    const r = await fetch(url, { method: "GET" });
    if (!r.ok) throw new Error(`Bir hata oluştu: ${r.statusText}`);

    const html = await r.text();

    if (html.includes('<form') && html.includes('name="username"')) {
      window.location.href = "/login";
      return;
    }

    const host = document.getElementById("ara_content");
    if (host) host.innerHTML = html;

    if (typeof stokBaslik === "function") stokBaslik();

    // ilk yüklemede otomatik getir
    await OBS.URUNKART.aramaYap("Kodu");
    const arama = OBS.URUNKART._el("arama");
    if (arama) arama.value = "";
  } catch (err) {
    const host = document.getElementById("ara_content");
    if (host) host.innerHTML = `<h2>${err.message}</h2>`;
  } finally {
    OBS.URUNKART._setCursor(false);
  }
};

/* ---------- misc ---------- */

OBS.URUNKART.resimSil = function () {
   OBS.URUNKART._setImg("");
   const fileInput = OBS.URUNKART._el("resim");
   if (fileInput) fileInput.value = "";
};

OBS.URUNKART.anaChanged = async (selectEl) => {
  const selectedValue = selectEl?.value || "";

  OBS.URUNKART._hideError();
  OBS.URUNKART._setCursor(true);

  const alt = OBS.URUNKART._el("altgrup");
  if (!alt) { OBS.URUNKART._setCursor(false); return; }
  alt.innerHTML = "";

  if (selectedValue === "") {
    alt.disabled = true;
    OBS.URUNKART._setCursor(false);
    return;
  }

  try {
    const response = await fetchWithSessionCheck("stok/altgrup", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ anagrup: selectedValue })
    });
    if (response?.errorMessage) throw new Error(response.errorMessage);

    (response.altKodlari || []).forEach((kod) => {
      const opt = document.createElement("option");
      opt.value = kod.ALT_GRUP;
      opt.textContent = kod.ALT_GRUP;
      alt.appendChild(opt);
    });

    alt.disabled = false;
  } catch (err) {
    OBS.URUNKART._showError(err?.message);
  } finally {
    OBS.URUNKART._setCursor(false);
  }
};

OBS.URUNKART.birimtipChanged = (selectEl) => {
  const val = selectEl?.value || "";
  const birim = OBS.URUNKART._el("birim");
  if (birim) birim.value = val;
};

OBS.URUNKART.urnAdiOgren = async (inputEl, targetId) => {
  const val = inputEl?.value || "";
  const target = OBS.URUNKART._el(targetId);

  OBS.URUNKART._setCursor(true);
  OBS.URUNKART._hideError();

  if (!val) {
    if (target) target.value = "";
    OBS.URUNKART._setCursor(false);
    return;
  }

  try {
    const response = await fetchWithSessionCheck("stok/urnadi", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ urnkodu: val })
    });
    if (response?.errorMessage) throw new Error(response.errorMessage);

    if (target) target.value = response.urnAdi || "";
  } catch (err) {
    OBS.URUNKART._showError(err?.message);
  } finally {
    OBS.URUNKART._setCursor(false);
  }
};

/* ---------- init (event binding) ---------- */
OBS.URUNKART.init = () => {
  // file size kontrol + event
  const file = OBS.URUNKART._el("resim");
  if (file && !file.dataset.bound) {
    file.addEventListener("change", (event) => {
      const maxKB = 500;
      const maxBytes = maxKB * 1024;
      const f = event.target.files?.[0];

      if (f && f.size > maxBytes) {
        OBS.URUNKART._showError(`Dosya boyutu ${maxKB} KB'ı geçemez!`);
        event.target.value = "";
      } else {
        OBS.URUNKART._hideError();
      }
    });

    file.dataset.bound = "1";
  }
};
