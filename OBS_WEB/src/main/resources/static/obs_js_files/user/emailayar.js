window.OBS = window.OBS || {};
OBS.EMAILAYAR = OBS.EMAILAYAR || {};

OBS.EMAILAYAR.byId = (id) => document.getElementById(id);

OBS.EMAILAYAR.showMsg = function(msg, ok) {
    const e = OBS.EMAILAYAR.byId("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.classList.remove("psuccess", "perror", "pvisible");
    e.classList.add(ok ? "psuccess" : "perror", "pvisible");
    e.textContent = msg || (ok ? "Kayıt yapıldı." : "Beklenmeyen hata.");
};

OBS.EMAILAYAR.clearMsg = function() {
    const e = OBS.EMAILAYAR.byId("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.textContent = "";
    e.classList.remove("psuccess", "perror", "pvisible");
};

OBS.EMAILAYAR.load = async function() {
    OBS.EMAILAYAR.clearMsg();

    try {
        const data = await fetchWithSessionCheck("user/emailsettings_data", { method: "GET" });

        OBS.EMAILAYAR.byId("id").value = data?.id ?? "";
        OBS.EMAILAYAR.byId("hesap").value = data?.hesap ?? "";
        OBS.EMAILAYAR.byId("host").value = data?.host ?? "";
        OBS.EMAILAYAR.byId("port").value = data?.port ?? "";
        OBS.EMAILAYAR.byId("sifre").value = data?.sifre ?? "";
        OBS.EMAILAYAR.byId("gon_mail").value = data?.gon_mail ?? "";
        OBS.EMAILAYAR.byId("gon_isim").value = data?.gon_isim ?? "";
        OBS.EMAILAYAR.byId("bssl").checked = !!data?.bssl;
        OBS.EMAILAYAR.byId("btsl").checked = !!data?.btsl;

    } catch (err) {
        OBS.EMAILAYAR.showMsg(err?.message || "Ayarlar yüklenemedi.", false);
    }
};

OBS.EMAILAYAR.save = async function() {
    const btn = OBS.EMAILAYAR.byId("submitBtn");
    if (btn) { btn.disabled = true; btn.textContent = "İşleniyor..."; }

    OBS.EMAILAYAR.clearMsg();

    try {
        const form = OBS.EMAILAYAR.byId("updateForm2026");
        const fd = new FormData(form);

        const result = await fetchWithSessionCheck("user/emailsettings_save", {
            method: "POST",
            body: fd
        });

        if (result?.errorMessage && result.errorMessage.trim() !== "") {
            OBS.EMAILAYAR.showMsg(result.errorMessage, false);
        } else {
            OBS.EMAILAYAR.showMsg(result?.message || "Kayıt yapıldı.", true);
        }

    } catch (err) {
        OBS.EMAILAYAR.showMsg(err?.message || "Bir hata oluştu.", false);
    } finally {
        if (btn) { btn.disabled = false; btn.textContent = "Kaydet"; }
    }
};

// ✅ senin init çağrın bunu çalıştıracak
window.emailSettingsInit = function() {
    OBS.EMAILAYAR.load();
};

// ✅ buton onclick burayı çağıracak
window.emailSettingsSave = function() {
    OBS.EMAILAYAR.save();
};
