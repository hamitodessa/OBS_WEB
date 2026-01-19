window.OBS = window.OBS || {};
OBS.SERVER = OBS.SERVER || {};

OBS.SERVER.byId = (id) => document.getElementById(id);

OBS.SERVER.showError = function (msg) {
  const e = OBS.SERVER.byId("errorDiv");
  if (!e) return;
  e.style.display = "block";
  e.innerText = msg || "Bilinmeyen bir hata oluştu.";
};

OBS.SERVER.clearError = function () {
  const e = OBS.SERVER.byId("errorDiv");
  if (!e) return;
  e.style.display = "none";
  e.innerText = "";
};

OBS.SERVER.collectDTO = function (extra = {}) {
  return {
    user_modul: OBS.SERVER.byId("user_modul")?.value || "",
    hangi_sql: OBS.SERVER.byId("hangi_sql")?.value || "",
    user_prog_kodu: OBS.SERVER.byId("user_prog_kodu")?.value || "",
    user_server: OBS.SERVER.byId("user_server")?.value || "",
    user_pwd_server: OBS.SERVER.byId("user_pwd_server")?.value || "",
    user_ip: OBS.SERVER.byId("user_ip")?.value || "",
    superviser: OBS.SERVER.byId("superviser")?.value || "",
    ...extra
  };
};

OBS.SERVER.validateRequired = function (dto) {
  if (!dto.user_prog_kodu || !dto.user_server || !dto.user_pwd_server || !dto.user_ip) {
    OBS.SERVER.showError("Lütfen tüm gerekli alanları doldurunuz.");
    return false;
  }
  return true;
};

OBS.SERVER.setBusy = function (yes) {
  document.body.style.cursor = yes ? "wait" : "default";
};

OBS.SERVER.postJSON = async function (url, dto) {
  return await fetchWithSessionCheck(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto),
  });
};

// ===== UI =====
window.serversuperviserdurum = function () {
  const drm = OBS.SERVER.byId("hangi_sql")?.value;
  const div = OBS.SERVER.byId("superviserdiv");
  const inp = OBS.SERVER.byId("superviser");
  if (!div) return;

  const show = (drm === "PG SQL");
  div.style.visibility = show ? "visible" : "hidden";
  div.style.opacity = show ? "1" : "0";
  div.style.height = show ? "auto" : "0";
  if (show && inp) inp.value = "";
};

// ===== actions =====
window.serverKontrol = async function () {
  OBS.SERVER.clearError();

  const dto = OBS.SERVER.collectDTO();
  if (!OBS.SERVER.validateRequired(dto)) return;

  const dbButton = OBS.SERVER.byId("dbKontrolButton");
  OBS.SERVER.setBusy(true);

  try {
    const data = await OBS.SERVER.postJSON("server/serverkontrol", dto);

    if (data?.serverDurum === "true") {
      alert("Server Bağlantısı Sağlandı");
      if (dbButton) dbButton.disabled = false;
    } else {
      alert("Server Bağlantısı Sağlanamadı!");
      if (dbButton) dbButton.disabled = true;
    }
  } catch (err) {
    OBS.SERVER.showError(err?.message);
  } finally {
    OBS.SERVER.setBusy(false);
  }
};

window.databaseKontrol = async function () {
  OBS.SERVER.clearError();

  const dto = OBS.SERVER.collectDTO();
  if (!OBS.SERVER.validateRequired(dto)) return;

  OBS.SERVER.setBusy(true);
  try {
    const data = await OBS.SERVER.postJSON("server/dosyakontrol", dto);

    if (data?.dosyaDurum === "true") {
      alert("Dosya Veritabanında Mevcut");
      return;
    }

    OBS.SERVER.setBusy(false);
    const ok = confirm("Dosya Veritabanında Mevcut Değil. Oluşturulsun mu?");
    if (!ok) return;

    await window.createnewDB();
  } catch (err) {
    OBS.SERVER.showError(err?.message);
  } finally {
    OBS.SERVER.setBusy(false);
  }
};

window.createnewDB = async function () {
  OBS.SERVER.clearError();

  let firmaadi = "";
  if (OBS.SERVER.byId("user_modul")?.value !== "Kur") {
    firmaadi = prompt("Firma Ismi Giriniz :") || "";
  }

  const dto = OBS.SERVER.collectDTO({ firma_adi: firmaadi });

  OBS.SERVER.setBusy(true);
  try {
    const data = await OBS.SERVER.postJSON("server/dosyaolustur", dto);

    await window.saveTo(); // kullanıcı detayını kaydet

    if (data?.olustuDurum === "true") {
      alert("Dosya Oluşturuldu");
      if (data?.indexolustuDurum === "true") alert("Indexleme Oluşturuldu");
      else alert("Indexleme Oluştururken hata oluştu");
    } else {
      OBS.SERVER.showError(data?.errorMessage || "Dosya oluşturulamadı.");
    }
  } catch (err) {
    OBS.SERVER.showError(err?.message);
  } finally {
    OBS.SERVER.setBusy(false);
  }
};

window.saveTo = async function () {
  OBS.SERVER.clearError();

  const dto = OBS.SERVER.collectDTO({
    izinlimi: true,
    calisanmi: true,
    log: false
  });

  if (!OBS.SERVER.validateRequired(dto)) return;

  OBS.SERVER.setBusy(true);
  try {
    const res = await OBS.SERVER.postJSON("user/user_details_save", dto);
    if (res?.errorMessage && res.errorMessage.trim() !== "") {
      throw new Error(res.errorMessage);
    }
  } catch (err) {
    OBS.SERVER.showError(err?.message || "Bir hata oluştu.");
  } finally {
    OBS.SERVER.setBusy(false);
  }
};

// ✅ sayfa init (butonları bağla + superviser durumunu ayarla)
window.createDbInit = function () {
  const serverBtn = OBS.SERVER.byId("serverButton");
  const dbBtn     = OBS.SERVER.byId("dbKontrolButton");
  const hangiSql  = OBS.SERVER.byId("hangi_sql");

  if (serverBtn) serverBtn.onclick = window.serverKontrol;
  if (dbBtn) dbBtn.onclick = window.databaseKontrol;

  // select değişince superviser div güncelle
  if (hangiSql) hangiSql.onchange = window.serversuperviserdurum;

  // sayfa ilk açılışta da uygula
  window.serversuperviserdurum();
};
