window.OBS = window.OBS || {};
OBS.KODDEGIS = OBS.KODDEGIS || {};

OBS.KODDEGIS._el = (id) => document.getElementById(id);

OBS.KODDEGIS._clearError = function () {
  const e = OBS.KODDEGIS._el("errorDiv");
  if (!e) return;
  e.style.display = "none";
  e.innerText = "";
};

OBS.KODDEGIS._showError = function (msg) {
  const e = OBS.KODDEGIS._el("errorDiv");
  if (!e) return;
  e.style.display = "block";
  e.innerText = msg || "Beklenmeyen bir hata oluştu.";
};

OBS.KODDEGIS._setBtn = function (btnId, disabled, text) {
  const btn = OBS.KODDEGIS._el(btnId);
  if (!btn) return;
  btn.disabled = !!disabled;
  if (typeof text === "string") btn.textContent = text;
};

OBS.KODDEGIS.kodDegis = async function () {
  const eskiKod = OBS.KODDEGIS._el("tcheskodeski")?.value?.trim() || "";
  const yeniKod = OBS.KODDEGIS._el("tcheskodyeni")?.value?.trim() || "";

  if (!eskiKod || !yeniKod) {
    alert("Lütfen her iki alanı da doldurun.");
    return;
  }
  if (eskiKod === yeniKod) {
    alert("Eski kod ile yeni kod aynı olamaz.");
    return;
  }

  OBS.KODDEGIS._clearError();

  document.body.style.cursor = "wait";
  OBS.KODDEGIS._setBtn("koddegisButton", true, "Islem Yapiliyor...");

  try {
    const res = await fetchWithSessionCheck("cari/koddegiskaydet", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ eskiKod, yeniKod }),
    });

    if (res?.errorMessage) throw new Error(res.errorMessage);

    setTimeout(() => alert("Islem Basari ile gerceklestirildi"), 100);
  } catch (err) {
    OBS.KODDEGIS._showError(err?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    OBS.KODDEGIS._setBtn("koddegisButton", false, "Kaydet");
    document.body.style.cursor = "default";
  }
};
