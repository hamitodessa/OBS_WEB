/* =========================
   EMIR LISTE (çakışmasız)
   Namespace: OBS.EMIRLISTE
   ========================= */
window.OBS ||= {};
OBS.EMIRLISTE ||= {};

/* ---------- helpers ---------- */
OBS.EMIRLISTE._root = () =>
  document.getElementById("emir_content") ||
  document.getElementById("ara_content")  ||
  document;

OBS.EMIRLISTE.byId = (id) => OBS.EMIRLISTE._root().querySelector("#" + id);

OBS.EMIRLISTE._setError = function (msg) {
  const errorDiv = OBS.EMIRLISTE.byId("errorDiv") || document.getElementById("errorDiv");
  if (!errorDiv) return;
  if (msg) {
    errorDiv.style.display = "block";
    errorDiv.innerText = msg;
  } else {
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
  }
};

OBS.EMIRLISTE._clearTable = function () {
  const tableBody = OBS.EMIRLISTE.byId("tableBody") || document.getElementById("tableBody");
  if (tableBody) tableBody.innerHTML = "";
};

OBS.EMIRLISTE._renderRows = function (data) {
  const tableBody = OBS.EMIRLISTE.byId("tableBody") || document.getElementById("tableBody");
  if (!tableBody) return;

  tableBody.innerHTML = "";

  (Array.isArray(data) ? data : []).forEach((row) => {
    const tr = document.createElement("tr");
    tr.classList.add("table-row-height");

    const durumText = row?.DURUM == 1 ? "Aktif" : "Pasif";

    tr.innerHTML = `
      <td>${row?.EMIR_ISMI ?? ""}</td>
      <td>${durumText}</td>
      <td>${row?.MESAJ ?? ""}</td>
      <td>${row?.INSTANCE ?? ""}</td>
      <td>${typeof formatTarihsqlite === "function"
            ? formatTarihsqlite(row?.SON_YUKLEME)
            : (row?.SON_YUKLEME ?? "")}</td>
			<td>${typeof formatTarihsqlite === "function"
			      ? formatTarihsqlite(row?.GELECEK_YUKLEME)
			      : (row?.GELECEK_YUKLEME ?? "")}</td>
    `;

    if (row?.DURUM != 1) {
      tr.querySelectorAll("td").forEach((td) => (td.style.color = "red"));
    }

    tableBody.appendChild(tr);
  });
};

// ✅ Tek bir parser: JSON da olsa text de olsa hatayı yakalar
OBS.EMIRLISTE._readBodySmart = async function (resp) {
  const ct = (resp.headers.get("content-type") || "").toLowerCase();

  // önce text oku (her halükarda güvenli)
  const raw = await resp.text();
  const t = (raw || "").trim();

  // JSON gibi görünüyorsa parse dene
  const looksJson = ct.includes("application/json") || t.startsWith("{") || t.startsWith("[");
  if (looksJson) {
    try {
      return { kind: "json", raw: t, data: JSON.parse(t) };
    } catch {
      // json sandık ama değil -> text
      return { kind: "text", raw: t, data: t };
    }
  }
  return { kind: "text", raw: t, data: t };
};

// ✅ Hata mesajını her formatta çıkar
OBS.EMIRLISTE._extractErrorMessage = function (payload) {
  if (!payload) return "Sunucu hatası.";

  // text geldiyse direkt bas
  if (typeof payload === "string") {
    return payload || "Sunucu hatası.";
  }

  // wrapper format: {ok:false,status:401,message:"...",data:"..."}
  if (payload && typeof payload === "object") {
    if (payload.message) return String(payload.message);
    if (payload.error) return String(payload.error);
    if (payload.hata) return String(payload.hata);
    // {hata:"..."} gibi değil de "{hata: ...}" gibi geldiyse zaten string olur
  }

  return "Sunucu hatası.";
};

/* ---------- main ---------- */
OBS.EMIRLISTE.emirliste = async function () {
  const serverEl = OBS.EMIRLISTE.byId("server") || document.getElementById("server");
  const keyEl    = OBS.EMIRLISTE.byId("sifre")  || document.getElementById("sifre");
  const userEl   = OBS.EMIRLISTE.byId("kullaniciAdi") || document.getElementById("kullaniciAdi");

  const server = (serverEl?.value ?? "").trim();
  const apiKey = (keyEl?.value ?? "").trim();
  const user   = (userEl?.innerText ?? "").trim();

  if (!server || !apiKey) {
    OBS.EMIRLISTE._setError("⚠️ Lütfen sunucu ve şifre bilgilerini girin.");
    return;
  }

  document.body.style.cursor = "wait";
  OBS.EMIRLISTE._setError("");

  const url =
    `/backup/emirliste?server=${encodeURIComponent(server)}` +
    `&key=${encodeURIComponent(apiKey)}` +
    `&user=${encodeURIComponent(user)}`;

  try {
    const resp = await fetch(url, { cache: "no-store" });
    const body = await OBS.EMIRLISTE._readBodySmart(resp);
    const payload = body.data;

    if (payload && typeof payload === "object" && !Array.isArray(payload) && ("ok" in payload || "status" in payload)) {
      if (payload.ok === false) {
        const msg = OBS.EMIRLISTE._extractErrorMessage(payload);
        OBS.EMIRLISTE._setError(msg);
        OBS.EMIRLISTE._clearTable();
        return;
      }
      let arr = payload.data;
      if (typeof arr === "string") {
        try { arr = JSON.parse(arr); } catch { /* kalsın */ }
      }
      if (!Array.isArray(arr)) {
        OBS.EMIRLISTE._setError("Beklenmeyen veri formatı.");
        OBS.EMIRLISTE._clearTable();
        return;
      }

      OBS.EMIRLISTE._renderRows(arr);
      return;
    }

    if (Array.isArray(payload)) {
      OBS.EMIRLISTE._renderRows(payload);
      return;
    }

    if (!resp.ok) {
      const msg = OBS.EMIRLISTE._extractErrorMessage(payload);
      OBS.EMIRLISTE._setError(msg);
      OBS.EMIRLISTE._clearTable();
      return;
    }
    OBS.EMIRLISTE._setError("Beklenmeyen veri formatı.");
    OBS.EMIRLISTE._clearTable();

  } catch (error) {
    console.error("Fetch hatası:", error);
    OBS.EMIRLISTE._setError("Ağ hatası. Lütfen tekrar deneyin.");
    OBS.EMIRLISTE._clearTable();
  } finally {
    document.body.style.cursor = "default";
  }
};
