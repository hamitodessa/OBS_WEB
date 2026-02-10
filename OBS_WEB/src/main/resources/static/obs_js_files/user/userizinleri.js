/* =========================
   USER IZINLER (çakışmasız)
   Namespace: OBS.USERIZIN
   ========================= */
window.OBS ||= {};
OBS.USERIZIN ||= {};

/* ---------- helpers ---------- */
OBS.USERIZIN._root = () =>
    document.getElementById("userizin_content") ||
    document.getElementById("ara_content") ||
    document;

OBS.USERIZIN.byId = (id) => OBS.USERIZIN._root().querySelector("#" + id);

OBS.USERIZIN._tableBody = () =>
    OBS.USERIZIN.byId("tableBody") || OBS.USERIZIN._root().querySelector("#moduleTable tbody");

OBS.USERIZIN._setError = function(msg) {
    const e = OBS.USERIZIN.byId("errorDiv");
    if (!e) return;
    if (msg) { e.style.display = "block"; e.innerText = msg; }
    else { e.style.display = "none"; e.innerText = ""; }
};

OBS.USERIZIN.clearTable = function() {
    const tb = OBS.USERIZIN._tableBody();
    if (tb) tb.innerHTML = "";
};

OBS.USERIZIN.clearForm = function(modulsuz = false) {
    const r = OBS.USERIZIN._root();
    const setVal = (id, v) => { const el = r.querySelector("#" + id); if (el) el.value = v ?? ""; };
    const setChk = (id, v) => { const el = r.querySelector("#" + id); if (el) el.checked = !!v; };

    setVal("hiddenId", "");
    setVal("user_prog_kodu", "");
    setVal("user_server", "");
    setVal("user_pwd_server", "");
    setVal("user_ip", "");

    if (!modulsuz) {
        const um = r.querySelector("#user_modul");
        if (um) um.selectedIndex = 0;
    }

    const hs = r.querySelector("#hangi_sql");
    if (hs) hs.selectedIndex = 0;

    setChk("izinlimi", false);
    setChk("calisanmi", false);
    setChk("log", false);
    setVal("superviser", "");

    OBS.USERIZIN._setError("");
};

OBS.USERIZIN._getPwdValue = function() {
    const val = OBS.USERIZIN.byId("user_pwd_server")?.value ?? "";
    return (val === "******") ? null : val;
};

OBS.USERIZIN.sqlchanged = function() {
    const hangi_sql = OBS.USERIZIN.byId("hangi_sql")?.value ?? "";
    const div = OBS.USERIZIN.byId("superviserdiv");
    if (!div) return;
    div.style.visibility = (hangi_sql === "PG SQL") ? "visible" : "hidden";
};

OBS.USERIZIN._fillForm = function(item) {
    const r = OBS.USERIZIN._root();
    const setVal = (id, v) => { const el = r.querySelector("#" + id); if (el) el.value = v ?? ""; };
    const setChk = (id, v) => { const el = r.querySelector("#" + id); if (el) el.checked = !!v; };

    setVal("hiddenId", item?.id ?? "");
    setVal("user_prog_kodu", item?.user_prog_kodu ?? "");
    setVal("user_server", item?.user_server ?? "");
    setVal("user_pwd_server", item?.user_pwd_server ? "******" : "");
    setVal("user_ip", item?.user_ip ?? "");
    setVal("user_modul", item?.user_modul ?? "");
    setVal("hangi_sql", item?.hangi_sql ?? "");

    setChk("izinlimi", !!item?.izinlimi);
    setChk("calisanmi", !!item?.calisanmi);
    setChk("log", !!item?.log);
    setVal("superviser", item?.superviser ?? "");

    OBS.USERIZIN.sqlchanged();
    OBS.USERIZIN._setError("");

    // savebutton kilit (senin mantık)
    const dbButton = OBS.USERIZIN.byId("savebutton");
    if (dbButton) dbButton.disabled = true;
};

/* ---------- admin bağlı hesap oku ---------- */
OBS.USERIZIN.adminbaglihesapoku = async function() {
    OBS.USERIZIN._setError("");
    document.body.style.cursor = "wait";

    try {
        const data = await fetchWithSessionCheck("user/adminbaglihesapoku", {
            method: "GET",
            headers: { "Content-Type": "application/json" }
        });

        if (!data?.success) throw new Error(data?.errorMessage || "Bir hata oluştu.");

        const sel = OBS.USERIZIN.byId("kullanici");
        if (sel) {
            sel.innerHTML = "";
            (data.data || []).forEach((email) => {
                const opt = document.createElement("option");
                opt.value = email;
                opt.textContent = email;
                sel.appendChild(opt);
            });
        }

        // ilk kullanıcı ile listeyi çek
        await OBS.USERIZIN.detailoku();

    } catch (e) {
        OBS.USERIZIN._setError(e?.message || "Beklenmeyen hata");
    } finally {
        document.body.style.cursor = "default";
    }
};

/* ---------- izinleri oku ---------- */
OBS.USERIZIN.detailoku = async function() {
    const modul = OBS.USERIZIN.byId("user_modul")?.value ?? "";
    const hesap = OBS.USERIZIN.byId("kullanici")?.value ?? "";

    OBS.USERIZIN._setError("");
    OBS.USERIZIN.clearTable();

    document.body.style.cursor = "wait";

    try {
        const data = await fetchWithSessionCheck("user/izinlerioku", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ modul, hesap })
        });

        if (!data?.success) throw new Error(data?.errorMessage || "Bir hata oluştu.");

        OBS.USERIZIN.clearForm(true);

        const tb = OBS.USERIZIN._tableBody();
        const arr = Array.isArray(data.data) ? data.data : [];

        arr.forEach((item) => {
            const tr = document.createElement("tr");
            tr.classList.add("table-row-height");
            tr.dataset.item = JSON.stringify(item);

            tr.innerHTML = `
        <td style="display:none;">${item?.id ?? ""}</td>
        <td>${item?.user_prog_kodu ?? ""}</td>
        <td>${item?.user_server ?? ""}</td>
        <td>${item?.user_pwd_server ? "******" : ""}</td>
        <td>${item?.user_ip ?? ""}</td>
        <td>${item?.user_modul ?? ""}</td>
        <td>${item?.hangi_sql ?? ""}</td>
        <td>${item?.izinlimi ? "Evet" : "Hayır"}</td>
        <td>${item?.calisanmi ? "Evet" : "Hayır"}</td>
        <td>${item?.log ? "Evet" : "Hayır"}</td>
        <td>${item?.superviser ?? ""}</td>
      `;
            tb?.appendChild(tr);
        });

        if (arr.length > 0) OBS.USERIZIN._fillForm(arr[0]);
        else OBS.USERIZIN.clearForm(true);

        OBS.USERIZIN.sqlchanged();

        if (data?.roleName === "ADMIN") {
            const logiznidiv = OBS.USERIZIN.byId("logiznidiv");
            const izinlimidiv = OBS.USERIZIN.byId("izinlimidiv");
            if (logiznidiv) logiznidiv.style.display = "block";
            if (izinlimidiv) izinlimidiv.style.display = "block";
        }

    } catch (e) {
        OBS.USERIZIN._setError(e?.message || "Beklenmeyen hata");
    } finally {
        document.body.style.cursor = "default";
    }
};

/* ---------- row click (delegation) ---------- */
OBS.USERIZIN._onRowClick = function(event) {
    const tr = event.target.closest("tr");
    const tb = OBS.USERIZIN._tableBody();
    if (!tr || !tb || !tb.contains(tr)) return;

    try {
        const item = tr.dataset.item ? JSON.parse(tr.dataset.item) : null;
        if (item) OBS.USERIZIN._fillForm(item);
    } catch { /* ignore */ }
};

/* ---------- save ---------- */
OBS.USERIZIN.save = async function() {
    OBS.USERIZIN._setError("");

    try {
        const dto = {
            id: OBS.USERIZIN.byId("hiddenId")?.value ?? "",
            user_prog_kodu: OBS.USERIZIN.byId("user_prog_kodu")?.value ?? "",
            user_server: OBS.USERIZIN.byId("user_server")?.value ?? "",
            user_pwd_server: OBS.USERIZIN._getPwdValue(),
            user_ip: OBS.USERIZIN.byId("user_ip")?.value ?? "",
            user_modul: OBS.USERIZIN.byId("user_modul")?.value ?? "",
            hangi_sql: OBS.USERIZIN.byId("hangi_sql")?.value ?? "",
            izinlimi: !!OBS.USERIZIN.byId("izinlimi")?.checked,
            calisanmi: !!OBS.USERIZIN.byId("calisanmi")?.checked,
            log: !!OBS.USERIZIN.byId("log")?.checked,
            email: OBS.USERIZIN.byId("kullanici")?.value ?? "",
            superviser: OBS.USERIZIN.byId("superviser")?.value ?? ""
        };

        const res = await fetchWithSessionCheck("user/userizinsave", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dto)
        });

        if (res?.errorMessage) throw new Error(res.errorMessage);

        OBS.USERIZIN.detailoku();
    } catch (e) {
        OBS.USERIZIN._setError(e?.message || "Bir hata oluştu.");
    }
};

/* ---------- delete ---------- */
OBS.USERIZIN.confirmDelete = function() {
    if (confirm("Bu kaydı silmek istediğinizden emin misiniz?")) {
        OBS.USERIZIN.delete();
    }
};

OBS.USERIZIN.delete = async function() {
    const id = OBS.USERIZIN.byId("hiddenId")?.value ?? "";
    const hesap = OBS.USERIZIN.byId("kullanici")?.value ?? "";
    if (!id) return;

    document.body.style.cursor = "wait";
    try {
        const res = await fetchWithSessionCheck("user/izinlerdelete", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ id, hesap })
        });

        if (res?.errorMessage) throw new Error(res.errorMessage);

        OBS.USERIZIN.detailoku();
    } catch (e) {
        OBS.USERIZIN._setError(e?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
        document.body.style.cursor = "default";
    }
};

/* ---------- init / destroy ---------- */
OBS.USERIZIN.init = function() {
    // satır tıklama
    document.removeEventListener("click", OBS.USERIZIN._onRowClick, true);
    document.addEventListener("click", OBS.USERIZIN._onRowClick, true);

    // kullanıcı değişince otomatik oku (istersen)
    const sel = OBS.USERIZIN.byId("kullanici");
    if (sel) {
        sel.removeEventListener("change", OBS.USERIZIN.detailoku);
        sel.addEventListener("change", OBS.USERIZIN.detailoku);
    }

    // modul değişince otomatik oku (istersen)
    const modulSel = OBS.USERIZIN.byId("user_modul");
    if (modulSel) {
        modulSel.removeEventListener("change", OBS.USERIZIN.detailoku);
        modulSel.addEventListener("change", OBS.USERIZIN.detailoku);
    }

    // hangi_sql değişince superviser aç/kapa
    const hs = OBS.USERIZIN.byId("hangi_sql");
    if (hs) {
        hs.removeEventListener("change", OBS.USERIZIN.sqlchanged);
        hs.addEventListener("change", OBS.USERIZIN.sqlchanged);
    }
};

OBS.USERIZIN.destroy = function() {
    document.removeEventListener("click", OBS.USERIZIN._onRowClick, true);

    const sel = OBS.USERIZIN.byId("kullanici");
    if (sel) sel.removeEventListener("change", OBS.USERIZIN.detailoku);

    const modulSel = OBS.USERIZIN.byId("user_modul");
    if (modulSel) modulSel.removeEventListener("change", OBS.USERIZIN.detailoku);

    const hs = OBS.USERIZIN.byId("hangi_sql");
    if (hs) hs.removeEventListener("change", OBS.USERIZIN.sqlchanged);
};
