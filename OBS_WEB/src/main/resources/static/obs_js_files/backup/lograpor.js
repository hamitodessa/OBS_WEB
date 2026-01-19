/* =========================
   BACKUP LOG (çakışmasız)
   Namespace: OBS.BACKUPLOG
   ========================= */
window.OBS ||= {};
OBS.BACKUPLOG ||= {};

/* ---------- state ---------- */
OBS.BACKUPLOG.currentPage = 0;
OBS.BACKUPLOG.pageSize = 500;

/* ---------- helpers ---------- */
OBS.BACKUPLOG._root = () =>
  document.getElementById("backup_content") ||   // varsa sayfa wrapper
  document.getElementById("ara_content")    ||   // OBS içerik alanın
  document;

OBS.BACKUPLOG.byId = (id) => OBS.BACKUPLOG._root().querySelector("#" + id);

OBS.BACKUPLOG._setError = function (msg) {
  const errorDiv = OBS.BACKUPLOG.byId("errorDiv");
  if (!errorDiv) return;
  if (msg) {
    errorDiv.style.display = "block";
    errorDiv.innerText = msg;
  } else {
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
  }
};

OBS.BACKUPLOG._clearTable = function () {
  const tableBody = OBS.BACKUPLOG.byId("tableBody");
  if (tableBody) tableBody.innerHTML = "";
};

/* ---------- fetch wrapper ---------- */
OBS.BACKUPLOG.requestJson = async function (url) {
  const resp = await fetch(url, { cache: "no-store" });

  if (!resp.ok) {
    let msg = "";
    try {
      const j = await resp.json();
      msg = j?.message || j?.hata || JSON.stringify(j);
    } catch {
      msg = await resp.text();
    }

    if (!msg) {
      if (resp.status === 503) msg = "Şu anda yedekleme çalışıyor. Lütfen biraz sonra tekrar deneyin.";
      else if (resp.status === 401) msg = "Yetkisiz / Geçersiz anahtar.";
      else msg = "Sunucu hatası (" + resp.status + ").";
    }

    const e = new Error(msg);
    e.status = resp.status;
    throw e;
  }

  return resp.json();
};

/* ---------- emir dropdown doldur ---------- */
OBS.BACKUPLOG.emirIsmiDoldur = async function () {
  const server = (OBS.BACKUPLOG.byId("server")?.value ?? "").trim();
  const apiKey = (OBS.BACKUPLOG.byId("sifre")?.value ?? "").trim();
  const hangi_emir = OBS.BACKUPLOG.byId("hangi_emir");
  const user = (OBS.BACKUPLOG.byId("kullaniciAdi")?.innerText ?? "").trim();

  if (!server || !apiKey) {
    OBS.BACKUPLOG._setError("⚠️ Lütfen sunucu ve şifre bilgilerini girin.");
    return;
  }

  document.body.style.cursor = "wait";
  OBS.BACKUPLOG._setError("");
  OBS.BACKUPLOG._clearTable();
  if (hangi_emir) hangi_emir.innerHTML = "";

  const url = `/backup/emirliste?server=${encodeURIComponent(server)}&key=${encodeURIComponent(apiKey)}&user=${encodeURIComponent(user)}`;

  try {
    const data = await OBS.BACKUPLOG.requestJson(url);

    if (!hangi_emir) return;

    hangi_emir.appendChild(new Option("Lütfen seçiniz", ""));
    hangi_emir.appendChild(new Option("Hepsi", "Hepsi"));
    hangi_emir.appendChild(new Option("System", "System"));

    (Array.isArray(data) ? data : []).forEach(item => {
      if (item?.EMIR_ISMI) hangi_emir.appendChild(new Option(item.EMIR_ISMI, item.EMIR_ISMI));
    });
  } catch (error) {
    console.error("Emir ismi doldurma hatası:", error);
    OBS.BACKUPLOG._setError(error?.message || "Bilinmeyen hata oluştu.");
  } finally {
    document.body.style.cursor = "default";
  }
};

/* ---------- log liste ---------- */
OBS.BACKUPLOG.logListe = async function (page = 0) {
  const emir_ismi = OBS.BACKUPLOG.byId("hangi_emir")?.value ?? "";
  const startDate = OBS.BACKUPLOG.byId("startDate")?.value ?? "";
  const endDate = OBS.BACKUPLOG.byId("endDate")?.value ?? "";
  const user = (OBS.BACKUPLOG.byId("kullaniciAdi")?.innerText ?? "").trim();
  const server = (OBS.BACKUPLOG.byId("server")?.value ?? "").trim();
  const apiKey = (OBS.BACKUPLOG.byId("sifre")?.value ?? "").trim();

  const tableBody = OBS.BACKUPLOG.byId("tableBody");
  const prevBtn = OBS.BACKUPLOG.byId("prevPage");
  const nextBtn = OBS.BACKUPLOG.byId("nextPage");

  OBS.BACKUPLOG._setError("");
  if (tableBody) tableBody.innerHTML = "";

  if (!emir_ismi) return;

  if (!startDate || !endDate) {
    OBS.BACKUPLOG._setError("Lütfen tarih aralığı seçin.");
    return;
  }

  if (!server || !apiKey) {
    OBS.BACKUPLOG._setError("⚠️ Lütfen sunucu ve şifre bilgilerini girin.");
    return;
  }

  document.body.style.cursor = "wait";

  const url =
    `/backup/logliste?server=${encodeURIComponent(server)}` +
    `&key=${encodeURIComponent(apiKey)}` +
    `&emir=${encodeURIComponent(emir_ismi)}` +
    `&start=${encodeURIComponent(startDate)}` +
    `&end=${encodeURIComponent(endDate)}` +
    `&page=${page}` +
    `&user=${encodeURIComponent(user)}`;

  try {
    const data = await OBS.BACKUPLOG.requestJson(url);

    (Array.isArray(data) ? data : []).forEach(row => {
      const tr = document.createElement("tr");
      tr.classList.add("table-row-height");

      [row?.TARIH, row?.MSJTYPE, row?.ACIKLAMA, row?.EMIR_ISMI].forEach(text => {
        const td = document.createElement("td");
        td.innerText = text ?? "";
        tr.appendChild(td);
      });

      tableBody?.appendChild(tr);
    });

    OBS.BACKUPLOG.currentPage = page;

    if (prevBtn) prevBtn.disabled = OBS.BACKUPLOG.currentPage === 0;
    if (nextBtn) nextBtn.disabled = (tableBody?.rows?.length ?? 0) < OBS.BACKUPLOG.pageSize; // son sayfa mı

  } catch (error) {
    console.error("Log listeleme hatası:", error);
    OBS.BACKUPLOG._setError(error?.message || "Bilinmeyen hata oluştu.");
    if (prevBtn) prevBtn.disabled = true;
    if (nextBtn) nextBtn.disabled = true;
  } finally {
    document.body.style.cursor = "default";
  }
};

/* ---------- pagination ---------- */
OBS.BACKUPLOG.ileriSayfa = function () {
  OBS.BACKUPLOG.logListe(OBS.BACKUPLOG.currentPage + 1);
};

OBS.BACKUPLOG.geriSayfa = function () {
  if (OBS.BACKUPLOG.currentPage > 0) {
    OBS.BACKUPLOG.logListe(OBS.BACKUPLOG.currentPage - 1);
  }
};

/* ---------- init ---------- */
OBS.BACKUPLOG.init = function () {
  // istersen sayfa açılınca emir dropdown'u doldur:
  // OBS.BACKUPLOG.emirIsmiDoldur();
};
