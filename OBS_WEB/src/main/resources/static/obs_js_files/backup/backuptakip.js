/* =========================
   EMIR LISTE (çakışmasız)
   Namespace: OBS.EMIRLISTE
   ========================= */
window.OBS ||= {};
OBS.EMIRLISTE ||= {};

/* ---------- helpers ---------- */
OBS.EMIRLISTE._root = () =>
  document.getElementById("emir_content") || // varsa sayfa wrapper'ı
  document.getElementById("ara_content")  || // OBS içerik alanın
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
      <td>${typeof formatTarihsqlite === "function" ? formatTarihsqlite(row?.SON_YUKLEME) : (row?.SON_YUKLEME ?? "")}</td>
      <td>${row?.GELECEK_YEDEKLEME ?? ""}</td>
    `;

    if (row?.DURUM != 1) {
      tr.querySelectorAll("td").forEach((td) => (td.style.color = "red"));
    }

    tableBody.appendChild(tr);
  });
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

    if (!resp.ok) {
      let msg = "Sunucu hatası.";
      try {
        const j = await resp.json();
        msg = j?.message || msg;
      } catch {
        const t = await resp.text();
        if (t) msg = t;
      }
      OBS.EMIRLISTE._setError(msg);
      OBS.EMIRLISTE._clearTable();
      return;
    }

    const data = await resp.json();
    OBS.EMIRLISTE._renderRows(data);

  } catch (error) {
    console.error("Fetch hatası:", error);
    OBS.EMIRLISTE._setError("Ağ hatası. Lütfen tekrar deneyin.");
    OBS.EMIRLISTE._clearTable();
  } finally {
    document.body.style.cursor = "default";
  }
};


