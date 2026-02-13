window.OBS ||= {};
OBS.SHORTCUT_SETTINGS ||= {};

(function(M) {
    function fillSelect(id, current) {
        const sel = document.getElementById(id);
        if (!sel) return;
        const letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
        sel.innerHTML = letters.map(ch => `<option value="${ch}">${ch}</option>`).join("");
        sel.value = (current || "S").toUpperCase();
    }
    function showMessage(msg, type) {
        const errorDiv = document.getElementById("errorDiv");
        if (!errorDiv) return;

        const cls = type === "error" ? "cam-msg-error" : "cam-msg-success";
        errorDiv.innerHTML = `<div class="cam-msg ${cls}">${msg}</div>`;
        // 4 saniye sonra kaybolsun
        setTimeout(() => {
            errorDiv.innerHTML = "";
        }, 4000);
    }

    M.init = function() {
        document.body.style.cursor = "wait";
        fillSelect("sc_SAVE", OBS?.CONFIG?.shortcuts?.["SAVE"] || "S");
        fillSelect("sc_DELETE", OBS?.CONFIG?.shortcuts?.["DELETE"] || "D");
        fillSelect("sc_REFRESH", OBS?.CONFIG?.shortcuts?.["REFRESH"] || "R");
        fillSelect("sc_NEW", OBS?.CONFIG?.shortcuts?.["NEW"] || "N");
        fillSelect("sc_SEARCH", OBS?.CONFIG?.shortcuts?.["SEARCH"] || "F");
        document.body.style.cursor = "default";
    };

    M.save = async function(actionCode, selectId) {
        const sel = document.getElementById(selectId);
        if (!sel) return;
        document.body.style.cursor = "wait";
        const letter = (sel.value || "S").toUpperCase();
        try {
            const res = await fetch("/user/shortcutsave", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ actionCode, hotkey: letter })
            });
            if (!res.ok) {
                document.body.style.cursor = "default";
                showMessage("Kayıt hatası: " + res.status, "error");
                return;
            }
            // config güncelle
            OBS.CONFIG.shortcuts[actionCode] = letter;
						
            // anında aktif et
            OBS.SHORTCUTS.setHotkey(actionCode, letter);
            document.body.style.cursor = "default";
            showMessage("Kısayol kaydedildi: Ctrl+" + letter, "success");
        } catch (e) {
            document.body.style.cursor = "default";
            showMessage("Sunucu hatası: " + e.message, "error");
        }
    };

})(OBS.SHORTCUT_SETTINGS);

/*
if (typeof OBS?.SHORTCUT_SETTINGS?.init === "function")
    OBS.SHORTCUT_SETTINGS.init();
*/