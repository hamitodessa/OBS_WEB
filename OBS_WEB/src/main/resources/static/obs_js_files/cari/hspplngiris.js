/* =========================
   HSP PLAN (çakışmasız)
   Namespace: OBS.HSPPLN
   ========================= */
window.OBS ||= {};
OBS.HSPPLN ||= {};

/* ---------- helpers ---------- */
OBS.HSPPLN._root = () =>
  document.getElementById("hsp_content") ||
  document.getElementById("ara_content") ||
  document;

OBS.HSPPLN.byId = (id) => OBS.HSPPLN._root().querySelector("#" + id);

OBS.HSPPLN._inputs = () =>
  OBS.HSPPLN._root().querySelectorAll(".form-control, .form-check-input");

/* ---------- enable / disable / clear ---------- */
OBS.HSPPLN.enableInputs = function () {
  OBS.HSPPLN.clearInputs();
  OBS.HSPPLN._inputs().forEach((el) => { el.disabled = false; });
};

OBS.HSPPLN.enableDuzeltmeInputs = function () {
  OBS.HSPPLN._inputs().forEach((el) => {
    if (el.id !== "kodu") el.disabled = false;
  });
};

OBS.HSPPLN.disableInputs = function () {
  OBS.HSPPLN._inputs().forEach((el) => {
    if (el.id !== "arama") el.disabled = true;
  });
};

OBS.HSPPLN.clearInputs = function () {
  OBS.HSPPLN._inputs().forEach((el) => {
    // arama kalsın
    //if (el.id === "arama") return;

    if (el.type === "checkbox") el.checked = false;
    else el.value = ""; // text + file dahil
  });

  const imgElement = OBS.HSPPLN.byId("resimGoster");
  if (imgElement) {
    imgElement.src = "";
    imgElement.style.display = "none";
  }

  const kk = OBS.HSPPLN.byId("kodKontrol");
  if (kk) kk.innerText = "";

  const errorDiv = OBS.HSPPLN.byId("errorDiv");
  if (errorDiv) { errorDiv.style.display = "none"; errorDiv.innerText = ""; }
};

/* ---------- dto ---------- */
OBS.HSPPLN.getHesapPlaniDTO = function () {
  return {
    kodu: OBS.HSPPLN.byId("kodu").value,
    adi: OBS.HSPPLN.byId("adi").value,
    karton: OBS.HSPPLN.byId("karton").value,
    hcins: OBS.HSPPLN.byId("hcins").value,
    yetkili: OBS.HSPPLN.byId("yetkili").value,
    ad1: OBS.HSPPLN.byId("ad1").value,
    ad2: OBS.HSPPLN.byId("ad2").value,
    semt: OBS.HSPPLN.byId("semt").value,
    seh: OBS.HSPPLN.byId("seh").value,
    vd: OBS.HSPPLN.byId("vd").value,
    vn: OBS.HSPPLN.byId("vn").value,
    t1: OBS.HSPPLN.byId("t1").value,
    t2: OBS.HSPPLN.byId("t2").value,
    t3: OBS.HSPPLN.byId("t3").value,
    fx: OBS.HSPPLN.byId("fx").value,
    o1: OBS.HSPPLN.byId("o1").value,
    o2: OBS.HSPPLN.byId("o2").value,
    o3: OBS.HSPPLN.byId("o3").value,
    web: OBS.HSPPLN.byId("web").value,
    mail: OBS.HSPPLN.byId("mail").value,
    kim: OBS.HSPPLN.byId("kim").value,
    acik: OBS.HSPPLN.byId("acik").value,
    sms: OBS.HSPPLN.byId("sms").checked
  };
};

/* ---------- kayit ---------- */
OBS.HSPPLN.kayit = async function () {
  const koduInput = OBS.HSPPLN.byId("kodu");
  if (!koduInput || ["0", ""].includes(koduInput.value)) return;

  const hesapplaniDTO = OBS.HSPPLN.getHesapPlaniDTO();
  const formData = new FormData();
  for (const key in hesapplaniDTO) formData.append(key, hesapplaniDTO[key]);

  const fileInput = OBS.HSPPLN.byId("resim");
  const file = fileInput?.files?.[0];
  if (file) formData.append("resim", file);

  const imgElement = OBS.HSPPLN.byId("resimGoster");
  const src = imgElement?.src || "";
  const base64Data = src.startsWith("data:image") ? src.split(",")[1] : null;

  if (base64Data) {
    const byteCharacters = atob(base64Data);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) byteNumbers[i] = byteCharacters.charCodeAt(i);
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: "image/jpeg" });
    formData.append("resimGoster", blob, "base64Resim.jpg");
  }

  const errorDiv = OBS.HSPPLN.byId("errorDiv");
  document.body.style.cursor = "wait";
  if (errorDiv) { errorDiv.style.display = "none"; errorDiv.innerText = ""; }

  try {
    const response = await fetchWithSessionCheck("cari/hsplnkayit", {
      method: "POST",
      body: formData
    });
    if (response?.errorMessage) throw new Error(response.errorMessage);

    window.sayfaYukle("/cari/hspplngiris");
  } catch (error) {
    if (errorDiv) {
      errorDiv.style.display = "block";
      errorDiv.innerText = error?.message || "Beklenmeyen hata.";
    }
  } finally {
    document.body.style.cursor = "default";
  }
};

/* ---------- arama ---------- */
OBS.HSPPLN.aramaYap = async function () {
  const kk = OBS.HSPPLN.byId("kodKontrol");
  if (kk) kk.innerText = "";

  const aramaVal = OBS.HSPPLN.byId("arama")?.value ?? "";
  if (!aramaVal) return;

  document.body.style.cursor = "wait";

  try {
    const dto = await fetchWithSessionCheck("cari/hsplnArama", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ arama: aramaVal })
    });

    if (dto?.errorMessage === "Bu Numarada Kayıtlı Hesap Yok") {
      const err = OBS.HSPPLN.byId("errorDiv");
      if (err) { err.style.display = "block"; err.innerText = dto.errorMessage; }
      return;
    }

    OBS.HSPPLN.byId("kodu").value = dto.kodu;
    OBS.HSPPLN.byId("adi").value = dto.adi || "";
    OBS.HSPPLN.byId("karton").value = dto.karton || "";
    OBS.HSPPLN.byId("hcins").value = dto.hcins || "";
    OBS.HSPPLN.byId("yetkili").value = dto.yetkili || "";
    OBS.HSPPLN.byId("ad1").value = dto.ad1 || "";
    OBS.HSPPLN.byId("ad2").value = dto.ad2 || "";
    OBS.HSPPLN.byId("semt").value = dto.semt || "";
    OBS.HSPPLN.byId("seh").value = dto.seh || "";
    OBS.HSPPLN.byId("vd").value = dto.vd || "";
    OBS.HSPPLN.byId("vn").value = dto.vn || "";
    OBS.HSPPLN.byId("t1").value = dto.t1 || "";
    OBS.HSPPLN.byId("t2").value = dto.t2 || "";
    OBS.HSPPLN.byId("t3").value = dto.t3 || "";
    OBS.HSPPLN.byId("fx").value = dto.fx || "";
    OBS.HSPPLN.byId("o1").value = dto.o1 || "";
    OBS.HSPPLN.byId("o2").value = dto.o2 || "";
    OBS.HSPPLN.byId("o3").value = dto.o3 || "";
    OBS.HSPPLN.byId("web").value = dto.web || "";
    OBS.HSPPLN.byId("mail").value = dto.mail || "";
    OBS.HSPPLN.byId("kim").value = dto.kim || "";
    OBS.HSPPLN.byId("acik").value = dto.acik || "";
    OBS.HSPPLN.byId("sms").checked = !!dto.sms;

    const img = OBS.HSPPLN.byId("resimGoster");
    const fileInput = OBS.HSPPLN.byId("resim");
    if (fileInput) fileInput.value = "";

    if (dto.base64Resim && dto.base64Resim.trim() !== "") {
      img.src = "data:image/jpeg;base64," + dto.base64Resim.trim();
      img.style.display = "block";
    } else {
      img.src = "";
      img.style.display = "none";
    }

    OBS.HSPPLN.disableInputs();
    OBS.HSPPLN.enableDuzeltmeInputs();

    const err = OBS.HSPPLN.byId("errorDiv");
    if (err) { err.style.display = "none"; err.innerText = ""; }
  } catch (error) {
    const err = OBS.HSPPLN.byId("errorDiv");
    if (err) {
      err.style.display = "block";
      err.innerText = error?.message || "Beklenmeyen bir hata oluştu.";
    }
  } finally {
    document.body.style.cursor = "default";
  }
};

/* ---------- nav ---------- */
OBS.HSPPLN.ilk = function () {
  const datalist = OBS.HSPPLN.byId("hesapOptions");
  const options = datalist?.getElementsByTagName("option") || [];
  if (options.length === 0) {
    OBS.HSPPLN.clearInputs();
    OBS.HSPPLN.disableInputs();
    return null;
  }
  const firstValue = options[0].value;
  OBS.HSPPLN.byId("arama").value = firstValue;
  OBS.HSPPLN.aramaYap();
  OBS.HSPPLN.byId("arama").value = "";
};

OBS.HSPPLN.geri = function () {
  const arama = OBS.HSPPLN.byId("kodu")?.value ?? "";
  const datalist = OBS.HSPPLN.byId("hesapOptions");
  const options = datalist?.getElementsByTagName("option") || [];
  if (options.length === 0) return null;

  let index = -1;
  for (let i = 0; i < options.length; i++) {
    if (options[i].value === arama) { index = i; break; }
  }
  if (index > 0) {
    const prevValue = options[index - 1].value;
    OBS.HSPPLN.byId("arama").value = prevValue;
    OBS.HSPPLN.aramaYap();
    OBS.HSPPLN.byId("arama").value = "";
  }
};

OBS.HSPPLN.ileri = function () {
  const arama = OBS.HSPPLN.byId("kodu")?.value ?? "";
  const datalist = OBS.HSPPLN.byId("hesapOptions");
  const options = datalist?.getElementsByTagName("option") || [];
  if (options.length === 0) return null;

  let index = -1;
  for (let i = 0; i < options.length; i++) {
    if (options[i].value === arama) { index = i; break; }
  }
  if (index !== -1 && index < options.length - 1) {
    const nextValue = options[index + 1].value;
    OBS.HSPPLN.byId("arama").value = nextValue;
    OBS.HSPPLN.aramaYap();
    OBS.HSPPLN.byId("arama").value = "";
  }
};

/* ---------- sil ---------- */
OBS.HSPPLN.hesapSil = async function () {
  const hesapKodu = OBS.HSPPLN.byId("kodu")?.value ?? "";
  if (["0", ""].includes(hesapKodu)) return;

  const message =
    "Kayit Dosyadan Silinecek ..?\n\n" +
    "Oncelikle Bu Hesaba Ait Fisleri Silmeniz\n\n" +
    "Tavsiye Olunur ....";

  if (!confirm(message)) return;

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
    const err = OBS.HSPPLN.byId("errorDiv");
    if (err) { err.style.display = "block"; err.innerText = e?.message || "Beklenmeyen hata"; }
  } finally {
    document.body.style.cursor = "default";
  }
};

/* ---------- resim ---------- */
OBS.HSPPLN.resimSil = function () {
  const img = OBS.HSPPLN.byId("resimGoster");
  if (img) { img.src = ""; img.style.display = "none"; }

  const fileInput = OBS.HSPPLN.byId("resim");
  if (fileInput) fileInput.value = "";
};

OBS.HSPPLN.initResimSizeCheck = function () {
  const input = OBS.HSPPLN.byId("resim");
  if (!input) return;

  input.addEventListener("change", function (event) {
    const file = event.target.files?.[0];
    const maxSizeInKB = 500;
    const maxSizeInBytes = maxSizeInKB * 1024;
    const errorDiv = OBS.HSPPLN.byId("errorDiv");

    if (file && file.size > maxSizeInBytes) {
      if (errorDiv) {
        errorDiv.innerText = `Dosya boyutu ${maxSizeInKB} KB'ı geçemez!`;
        errorDiv.style.display = "block";
      }
      event.target.value = "";
    } else if (errorDiv) {
      errorDiv.style.display = "none";
      errorDiv.innerText = "";
    }
  });
};

/* init (loader’dan çağır) */
OBS.HSPPLN.init = function () {
  OBS.HSPPLN.initResimSizeCheck();
};
