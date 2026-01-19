window.OBS = window.OBS || {};
OBS.HPLAN = OBS.HPLAN || {};

OBS.HPLAN._el = (id) => document.getElementById(id);

OBS.HPLAN._setBtn = function (btnId, disabled, text) {
  const btn = OBS.HPLAN._el(btnId);
  if (!btn) return;
  btn.disabled = !!disabled;
  if (typeof text === "string") btn.textContent = text;
};

OBS.HPLAN._clearError = function () {
  const e = OBS.HPLAN._el("errorDiv");
  if (!e) return;
  e.style.display = "none";
  e.innerText = "";
};

OBS.HPLAN._showError = function (msg) {
  const e = OBS.HPLAN._el("errorDiv");
  if (!e) return;
  e.style.display = "block";
  e.innerText = msg || "Beklenmeyen bir hata oluştu.";
};

OBS.HPLAN.hplnkaydet = async function () {
  const ok = confirm("Ornek Hesap Plani Kayit Islemi Baslayacak ?");
  if (!ok) return;

  OBS.HPLAN._clearError();

  document.body.style.cursor = "wait";
  OBS.HPLAN._setBtn("hplkayitButton", true, "İşleniyor...");

  try {
    const res = await fetchWithSessionCheck("cari/ornekhesapplanikayit", {
      method: "GET",
      headers: { "Content-Type": "application/json" },
    });

    if (res?.errorMessage) throw new Error(res.errorMessage);

    alert("Ornek Hesap Plani Olusturuldu");
  } catch (err) {
    OBS.HPLAN._showError(err?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    OBS.HPLAN._setBtn("hplkayitButton", false, "Hesap Plani Kaydet");
    document.body.style.cursor = "default";
  }
};
