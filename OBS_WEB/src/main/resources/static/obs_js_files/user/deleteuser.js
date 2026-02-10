window.OBS = window.OBS || {};
OBS.ACCDEL = OBS.ACCDEL || {};

OBS.ACCDEL.byId = (id) => document.getElementById(id);

OBS.ACCDEL.showError = function(msg) {
    const e = OBS.ACCDEL.byId("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Beklenmeyen bir hata oluştu.";
};

OBS.ACCDEL.clearError = function() {
    const e = OBS.ACCDEL.byId("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
};

OBS.ACCDEL.openConfirm = function() {
    OBS.ACCDEL.clearError();
    const box = OBS.ACCDEL.byId("confirmBox");
    const txt = OBS.ACCDEL.byId("confirmText");
    const btn = OBS.ACCDEL.byId("confirmDeleteBtn");

    if (box) box.style.display = "block";
    if (txt) { txt.value = ""; txt.focus(); }
    if (btn) btn.disabled = true;
};

OBS.ACCDEL.closeConfirm = function() {
    const box = OBS.ACCDEL.byId("confirmBox");
    const txt = OBS.ACCDEL.byId("confirmText");
    const btn = OBS.ACCDEL.byId("confirmDeleteBtn");

    if (box) box.style.display = "none";
    if (txt) txt.value = "";
    if (btn) btn.disabled = true;
};

OBS.ACCDEL.onConfirmInput = function() {
    const txt = OBS.ACCDEL.byId("confirmText");
    const btn = OBS.ACCDEL.byId("confirmDeleteBtn");
    if (!txt || !btn) return;

    const ok = txt.value.trim().toUpperCase() === "SIL";
    btn.disabled = !ok;
};

OBS.ACCDEL.deleteAccount = async function() {
    const btnMain = OBS.ACCDEL.byId("hesapyoketButton");
    const btnConf = OBS.ACCDEL.byId("confirmDeleteBtn");

    OBS.ACCDEL.clearError();
    document.body.style.cursor = "wait";
    if (btnMain) btnMain.disabled = true;
    if (btnConf) { btnConf.disabled = true; btnConf.innerText = "Siliniyor..."; }

    try {
        const res = await fetchWithSessionCheck("user/accountdelete", {
            method: "POST"
        });

        if (res?.errorMessage) {
            throw new Error(res.errorMessage);
        }

        // başarı → çıkış
        window.location.href = "/logout";

    } catch (err) {
        OBS.ACCDEL.showError(err?.message || "Beklenmeyen bir hata oluştu.");
        if (btnMain) btnMain.disabled = false;
        if (btnConf) { btnConf.disabled = false; btnConf.innerText = "Evet, Sil"; }
    } finally {
        document.body.style.cursor = "default";
    }
};


window.accountDeleteInit = function() {
    OBS.ACCDEL.byId("hesapyoketButton")
        ?.addEventListener("click", OBS.ACCDEL.openConfirm);

    OBS.ACCDEL.byId("confirmCloseBtn")
        ?.addEventListener("click", OBS.ACCDEL.closeConfirm);

    OBS.ACCDEL.byId("confirmText")
        ?.addEventListener("input", OBS.ACCDEL.onConfirmInput);

    OBS.ACCDEL.byId("confirmDeleteBtn")
        ?.addEventListener("click", OBS.ACCDEL.deleteAccount);
};

