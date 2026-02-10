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
    document.getElementById("backup_content") ||
    document.getElementById("ara_content") ||
    document;

OBS.BACKUPLOG.byId = (id) => OBS.BACKUPLOG._root().querySelector("#" + id);

OBS.BACKUPLOG._setError = function(msg) {
    const errorDiv = OBS.BACKUPLOG.byId("errorDiv") || document.getElementById("errorDiv");
    if (!errorDiv) return;
    if (msg) {
        errorDiv.style.display = "block";
        errorDiv.innerText = msg;
    } else {
        errorDiv.style.display = "none";
        errorDiv.innerText = "";
    }
};

OBS.BACKUPLOG._clearTable = function() {
    const tableBody = OBS.BACKUPLOG.byId("tableBody") || document.getElementById("tableBody");
    if (tableBody) tableBody.innerHTML = "";
};

OBS.BACKUPLOG._setCursor = function(wait) {
    document.body.style.cursor = wait ? "wait" : "default";
};

OBS.BACKUPLOG._readBodySmart = async function(resp) {
    const ct = (resp.headers.get("content-type") || "").toLowerCase();
    const raw = await resp.text();
    const t = (raw || "").trim();
    const looksJson = ct.includes("application/json") || t.startsWith("{") || t.startsWith("[");
    if (looksJson) {
        try {
            return { kind: "json", raw: t, data: JSON.parse(t) };
        } catch {
            return { kind: "text", raw: t, data: t };
        }
    }
    return { kind: "text", raw: t, data: t };
};

OBS.BACKUPLOG._extractErrorMessage = function(payload, status) {
    // status’a göre güzel mesaj
    if (status === 503) return "Şu anda yedekleme çalışıyor. Lütfen biraz sonra tekrar deneyin.";
    if (status === 401) return "Yetkisiz / Geçersiz anahtar.";

    if (!payload) return "Sunucu hatası (" + status + ").";

    if (typeof payload === "string") {
        return payload || ("Sunucu hatası (" + status + ").");
    }

    if (payload && typeof payload === "object") {
        // wrapper
        if (payload.message) return String(payload.message);
        if (payload.error) return String(payload.error);
        if (payload.hata) return String(payload.hata);

        // bazı durumlarda backend direkt {ok:false, data:"..."} gibi
        if (payload.ok === false && payload.data) return String(payload.data);
    }

    return "Sunucu hatası (" + status + ").";
};

/* ---------- fetch wrapper (tek tip) ---------- */
OBS.BACKUPLOG.requestJson = async function(url) {
    const resp = await fetch(url, { cache: "no-store" });
    const body = await OBS.BACKUPLOG._readBodySmart(resp);
    const payload = body.data;

    // 1) Controller wrapper döndürüyorsa: {ok,status,message,data}
    if (payload && typeof payload === "object" && !Array.isArray(payload) && ("ok" in payload || "status" in payload)) {
        if (payload.ok === false) {
            const msg = OBS.BACKUPLOG._extractErrorMessage(payload, payload.status || resp.status || 500);
            const e = new Error(msg);
            e.status = payload.status || resp.status;
            throw e;
        }

        // ok:true -> payload.data bazen string JSON olabilir
        let data = payload.data;

        if (typeof data === "string") {
            const s = data.trim();
            if (s.startsWith("{") || s.startsWith("[")) {
                try { data = JSON.parse(s); } catch { /* kalsın */ }
            }
        }

        return data;
    }

    // 2) Eski sistem: status code ile yönetilen normal response
    if (!resp.ok) {
        const msg = OBS.BACKUPLOG._extractErrorMessage(payload, resp.status);
        const e = new Error(msg);
        e.status = resp.status;
        throw e;
    }

    // 3) ok:true ama payload text ise (garip durum)
    return payload;
};

/* ---------- emir dropdown doldur ---------- */
OBS.BACKUPLOG.emirIsmiDoldur = async function() {
    const server = (OBS.BACKUPLOG.byId("server")?.value ?? "").trim();
    const apiKey = (OBS.BACKUPLOG.byId("sifre")?.value ?? "").trim();
    const hangi_emir = OBS.BACKUPLOG.byId("hangi_emir") || document.getElementById("hangi_emir");
    const user = (OBS.BACKUPLOG.byId("kullaniciAdi")?.innerText ?? "").trim();

    if (!server || !apiKey) {
        OBS.BACKUPLOG._setError("⚠️ Lütfen sunucu ve şifre bilgilerini girin.");
        return;
    }

    OBS.BACKUPLOG._setCursor(true);
    OBS.BACKUPLOG._setError("");
    OBS.BACKUPLOG._clearTable();
    if (hangi_emir) hangi_emir.innerHTML = "";

    const url =
        `/backup/emirliste?server=${encodeURIComponent(server)}` +
        `&key=${encodeURIComponent(apiKey)}` +
        `&user=${encodeURIComponent(user)}`;

    try {
        const res = await OBS.BACKUPLOG.requestJson(url);

        // res wrapper da olabilir, array de olabilir
        let arr = res;

        // wrapper geldiyse
        if (arr && typeof arr === "object" && !Array.isArray(arr) && ("ok" in arr || "status" in arr)) {
            if (arr.ok === false) {
                throw new Error(arr.message || "Bilinmeyen hata");
            }
            arr = arr.data;
        }

        // data string ise parse et
        if (typeof arr === "string") {
            try { arr = JSON.parse(arr); } catch { arr = []; }
        }

        // garanti array
        arr = Array.isArray(arr) ? arr : [];

        hangi_emir.appendChild(new Option("Lütfen seçiniz", ""));
        hangi_emir.appendChild(new Option("Hepsi", "Hepsi"));
        hangi_emir.appendChild(new Option("System", "System"));

        arr.forEach(item => {
            if (item?.EMIR_ISMI) hangi_emir.appendChild(new Option(item.EMIR_ISMI, item.EMIR_ISMI));
        });
    } catch (error) {
        console.error("Emir ismi doldurma hatası:", error);
        OBS.BACKUPLOG._setError(error?.message || "Bilinmeyen hata oluştu.");
    } finally {
        OBS.BACKUPLOG._setCursor(false);
    }
};

/* ---------- log liste ---------- */
OBS.BACKUPLOG.logListe = async function(page = 0) {
    const emir_ismi = (OBS.BACKUPLOG.byId("hangi_emir")?.value ?? "").trim();
    const startDate = (OBS.BACKUPLOG.byId("startDate")?.value ?? "").trim();
    const endDate = (OBS.BACKUPLOG.byId("endDate")?.value ?? "").trim();
    const user = (OBS.BACKUPLOG.byId("kullaniciAdi")?.innerText ?? "").trim();
    const server = (OBS.BACKUPLOG.byId("server")?.value ?? "").trim();
    const apiKey = (OBS.BACKUPLOG.byId("sifre")?.value ?? "").trim();

    const tableBody = OBS.BACKUPLOG.byId("tableBody") || document.getElementById("tableBody");
    const prevBtn = OBS.BACKUPLOG.byId("prevPage") || document.getElementById("prevPage");
    const nextBtn = OBS.BACKUPLOG.byId("nextPage") || document.getElementById("nextPage");

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

    OBS.BACKUPLOG._setCursor(true);

    const url =
        `/backup/logliste?server=${encodeURIComponent(server)}` +
        `&key=${encodeURIComponent(apiKey)}` +
        `&emir=${encodeURIComponent(emir_ismi)}` +
        `&start=${encodeURIComponent(startDate)}` +
        `&end=${encodeURIComponent(endDate)}` +
        `&page=${encodeURIComponent(page)}` +
        `&limit=${encodeURIComponent(OBS.BACKUPLOG.pageSize)}` +
        `&user=${encodeURIComponent(user)}`;

    try {
        const data = await OBS.BACKUPLOG.requestJson(url);

        const arr = Array.isArray(data) ? data : [];
        arr.forEach(row => {
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
        if (nextBtn) nextBtn.disabled = (tableBody?.rows?.length ?? 0) < OBS.BACKUPLOG.pageSize;

    } catch (error) {
        console.error("Log listeleme hatası:", error);
        OBS.BACKUPLOG._setError(error?.message || "Bilinmeyen hata oluştu.");
        if (prevBtn) prevBtn.disabled = true;
        if (nextBtn) nextBtn.disabled = true;

    } finally {
        OBS.BACKUPLOG._setCursor(false);
    }
};

/* ---------- pagination ---------- */
OBS.BACKUPLOG.ileriSayfa = function() {
    OBS.BACKUPLOG.logListe(OBS.BACKUPLOG.currentPage + 1);
};

OBS.BACKUPLOG.geriSayfa = function() {
    if (OBS.BACKUPLOG.currentPage > 0) {
        OBS.BACKUPLOG.logListe(OBS.BACKUPLOG.currentPage - 1);
    }
};

/* ---------- init ---------- */
OBS.BACKUPLOG.init = function() {
    // istersen sayfa açılınca emir dropdown'u doldur:
    // OBS.BACKUPLOG.emirIsmiDoldur();
};
