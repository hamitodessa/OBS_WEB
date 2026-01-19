window.OBS = window.OBS || {};
OBS.USERCOPY = OBS.USERCOPY || {};

OBS.USERCOPY.byId = (id) => document.getElementById(id);

OBS.USERCOPY.showError = function (msg) {
  const e = OBS.USERCOPY.byId("errorDiv");
  if (!e) return;
  e.style.display = "block";
  e.innerText = msg || "Beklenmeyen bir hata oluştu.";
};

OBS.USERCOPY.clearError = function () {
  const e = OBS.USERCOPY.byId("errorDiv");
  if (!e) return;
  e.style.display = "none";
  e.innerText = "";
};

OBS.USERCOPY.setBtnReady = function (ready) {
  const btn = OBS.USERCOPY.byId("kaydetButton");
  if (!btn) return;
  btn.disabled = !ready;
  btn.textContent = ready ? "Kaydet" : "Kaydet";
  btn.classList.toggle("btn-ready", ready);
};

OBS.USERCOPY.setBusy = function (yes) {
  document.body.style.cursor = yes ? "wait" : "default";
};

window.aramaYap = async function () {
  const hesap = OBS.USERCOPY.byId("hesap")?.value.trim();

  OBS.USERCOPY.clearError();
  OBS.USERCOPY.setBtnReady(false);

  if (!hesap) {
    OBS.USERCOPY.showError("Lütfen e-mail gir.");
    return;
  }

  OBS.USERCOPY.setBusy(true);

  try {
    const res = await fetchWithSessionCheck("user/checkuser", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ hesap })
    });

    if (res?.errorMessage) {
      throw new Error(res.errorMessage);
    }

    // ✅ her şey OK → kaydet aktif
    OBS.USERCOPY.setBtnReady(true);

  } catch (err) {
    OBS.USERCOPY.showError(err?.message);
    OBS.USERCOPY.setBtnReady(false);
  } finally {
    OBS.USERCOPY.setBusy(false);
  }
};

window.copyUser = async function () {
  const hesap = OBS.USERCOPY.byId("hesap")?.value.trim();

  OBS.USERCOPY.clearError();

  if (!hesap) {
    OBS.USERCOPY.showError("Lütfen kullanıcı alanını doldurun.");
    return;
  }

  OBS.USERCOPY.setBtnReady(false);
  OBS.USERCOPY.setBusy(true);

  try {
    const res = await fetchWithSessionCheck("user/savecopyuser", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ hesap })
    });

    if (res?.errorMessage) {
      throw new Error(res.errorMessage);
    }

    // ✅ başarı
    OBS.USERCOPY.showError("Kullanıcı oluşturuldu: " + (res.success || "OK"));
    OBS.USERCOPY.byId("errorDiv")?.classList.add("psuccess");

  } catch (err) {
    OBS.USERCOPY.showError(err?.message);
  } finally {
    OBS.USERCOPY.setBusy(false);
    OBS.USERCOPY.setBtnReady(true);
  }
};
