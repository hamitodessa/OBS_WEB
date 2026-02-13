window.OBS ||= {};
OBS.CONFIG ||= {};
OBS.CONFIG.shortcuts ||= {};

OBS.SHORTCUTS ||= (function() {
    const hotkeys = new Map();   // SAVE -> s
    const handlers = new Map();  // SAVE -> fn
    let bound = false;

    const ACTIONS = ["SAVE", "DELETE", "REFRESH", "NEW", "SEARCH"];

    const normKey = (k) => (k || "").trim().toLowerCase();
    const normAct = (a) => (a || "").trim().toUpperCase();

    function loadFromConfig() {
        for (const a of ACTIONS) {
            const k = OBS?.CONFIG?.shortcuts?.[a];
            if (k) hotkeys.set(a, normKey(k));
        }
    }

    function bindOnce() {
        if (bound) return;
        bound = true;
        loadFromConfig();

        document.addEventListener("keydown", function(e) {
            if (!(e.ctrlKey || e.metaKey)) return;
            const k = normKey(e.key);
            if (!k) return;

            for (const [action, key] of hotkeys) {
                if (k !== key) continue;

                const fn = handlers.get(action);
                if (typeof fn !== "function") return;

                e.preventDefault();
                e.stopPropagation();
                try { fn(e); } catch (ex) { console.error(ex); }
                return;
            }
        }, true);
    }

    return {
        setHandler(actionCode, fn) {
            bindOnce();
            handlers.set(normAct(actionCode), fn);
        },
        setHotkey(actionCode, key) {
            bindOnce();
            hotkeys.set(normAct(actionCode), normKey(key));
        }
    };
})();
