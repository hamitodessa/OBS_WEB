window.OBS = window.OBS || {};
OBS.FIRMA = OBS.FIRMA || {};

/* =========================
   helpers
   ========================= */
OBS.FIRMA._el = (id) => document.getElementById(id);

OBS.FIRMA._clearError = function () {
  const e = OBS.FIRMA._el("errorDiv");
  if (!e) return;
  e.style.display = "none";
  e.innerText = "";
};

OBS.FIRMA._showError = function (msg) {
  const e = OBS.FIRMA._el("errorDiv");
  if (!e) return;
  e.style.display = "block";
  e.innerText = msg || "Beklenmeyen bir hata oluştu.";
};

/* =========================
   KAYDET
   ========================= */
OBS.FIRMA.firmaIsmiKaydet = async function () {
  const fismi = OBS.FIRMA._el("firmaismi")?.value?.trim() || "";
  const modul = OBS.FIRMA._el("modul")?.value?.trim() || "";

  if (!fismi || !modul) {
    alert("Firma adı ve modül zorunludur.");
    return;
  }

  OBS.FIRMA._clearError();
  document.body.style.cursor = "wait";

  try {
    const res = await fetchWithSessionCheck("obs/firmaismiKayit", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ fismi, modul }),
    });

    if (res?.errorMessage) throw new Error(res.errorMessage);

  } catch (err) {
    OBS.FIRMA._showError(err?.message);
  } finally {
    document.body.style.cursor = "default";
  }
};

/* =========================
   OKU
   ========================= */
OBS.FIRMA.firmaIsmiOku = async function (modul) {
  OBS.FIRMA._clearError();

  try {
    const res = await fetchWithSessionCheck("obs/firmaismioku", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(modul),
    });

    if (res?.errorMessage) throw new Error(res.errorMessage);

    const input = OBS.FIRMA._el("firmaismi");
    if (input) input.value = res.firmaismi || "";

  } catch (err) {
    OBS.FIRMA._showError(err?.message);
  }
};

/* =========================
   SAYFAYI AÇ
   ========================= */
OBS.FIRMA.firmaIsmiSayfa = async function (modul) {
  

  document.body.style.cursor = "wait";
  const baslik = OBS.FIRMA._el("baslik");
  if (baslik) baslik.innerText = "";

  try {
    const res = await fetchWithSessionCheck("obs/firmaismi", { method: "GET" });

    const html = res.data;
    const ara = OBS.FIRMA._el("ara_content");
    if (ara) ara.innerHTML = html;

    const modulInput = OBS.FIRMA._el("modul");
    if (modulInput) modulInput.value = "";

    await OBS.FIRMA.firmaIsmiOku(modul);

    if (modulInput) modulInput.value = modul;

    const lbl = OBS.FIRMA._el("lblfismi");
    if (lbl) {
      const map = {
        cari: "Cari Firma Adi",
        adres: "Adres Firma Adi",
        kambiyo: "Kambiyo Firma Adi",
        fatura: "Stok Firma Adi",
        kereste: "Kereste Firma Adi",
      };
      lbl.innerText = map[modul] || "Firma Adi";
    }

  } catch (err) {
    const ara = OBS.FIRMA._el("ara_content");
    if (ara) ara.innerHTML = `<h2>Bir hata oluştu: ${err.message}</h2>`;
  } finally {
    document.body.style.cursor = "default";
  }
};
