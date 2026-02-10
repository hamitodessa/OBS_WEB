/* =========================================================
   ADR – ETIKET AYAR
   Namespace: OBS.ADR
   Scope: #ara_content
   ========================================================= */
window.OBS ||= {};
OBS.ADR ||= {};

/* ---------- helpers ---------- */
OBS.ADR._root = () =>
  document.getElementById("adr_content") ||
  document.getElementById("ara_content") ||
  document;

OBS.ADR.byId = (id) => OBS.ADR._root().querySelector("#" + id);

OBS.ADR._setErr = (msg) => {
  const e = OBS.ADR.byId("errorDiv");
  if (!e) return;
  if (msg) {
    e.style.display = "block";
    e.innerText = msg;
  } else {
    e.style.display = "none";
    e.innerText = "";
  }
};

OBS.ADR._cursor = (wait) => {
  document.body.style.cursor = wait ? "wait" : "default";
};

/* ---------- ETIKET AYAR KAYDET ---------- */
OBS.ADR.ayarKayit = async function () {
  OBS.ADR._setErr("");
  OBS.ADR._cursor(true);

  const dto = {
    id: OBS.ADR.byId("id")?.value,
    altbosluk: OBS.ADR.byId("altbosluk")?.value,
    ustbosluk: OBS.ADR.byId("ustbosluk")?.value,
    sagbosluk: OBS.ADR.byId("sagbosluk")?.value,
    solbosluk: OBS.ADR.byId("solbosluk")?.value,
    dikeyarabosluk: OBS.ADR.byId("dikeyarabosluk")?.value,
    genislik: OBS.ADR.byId("genislik")?.value,
    yataydikey: OBS.ADR.byId("yataydikey")?.value,
    yukseklik: OBS.ADR.byId("yukseklik")?.value
  };

  const btn = OBS.ADR.byId("kaydetButton");
  if (btn) {
    btn.disabled = true;
    btn.textContent = "İşleniyor...";
  }

  try {
    const response = await fetchWithSessionCheck(
      "adres/etiketsettings_save",
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto)
      }
    );

    if (response?.errorMessage) {
      throw new Error(response.errorMessage);
    }

    await OBS.ADR.sayfaYukle();
  } catch (err) {
    OBS.ADR._setErr(err?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.textContent = "Kaydet";
    }
    OBS.ADR._cursor(false);
  }
};

/* ---------- SAYFA YUKLE ---------- */
OBS.ADR.sayfaYukle = async function () {
  try {
    OBS.ADR._cursor(true);

    const response = await fetchWithSessionCheck("adres/etiketayar", { method: "GET" });
    const html = response.data
    const ara = document.getElementById("ara_content");
    if (ara) ara.innerHTML = html;

  } catch (err) {
    const ara = document.getElementById("ara_content");
    if (ara) ara.innerHTML = `<h2>${err.message}</h2>`;
  } finally {
    OBS.ADR._cursor(false);
  }
};

/* ---------- INIT ---------- */
OBS.ADR.init = function () {
  // Şimdilik özel init yok, ileride eklenir
};
