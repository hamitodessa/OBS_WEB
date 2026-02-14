/* =========================
   OBS – Dynamic Page Loader (çakışmasız)  ✅ MULTI-JS
   - data-url ile sayfa yükler
   - data-xxx parametrelerini query olarak taşır
   - pageModules key = PATH (query yok)
   - js: "string" veya ["a.js","b.js"] ✅
   - init: function veya "string" (window fonksiyonu)
   - init(params, urlObj) parametre alır
   ========================= */

const ROOT = {
    ara: document.querySelector("#appShell #ara_content"),
    baslik: document.getElementById("baslik")
};

const loadedScripts = new Set();

function loadScriptOnce(src) {
    return new Promise((resolve, reject) => {
        if (!src) return resolve();
        if (loadedScripts.has(src) || document.querySelector(`script[data-dyn="${src}"]`)) {
            loadedScripts.add(src);
            return resolve();
        }
        const s = document.createElement("script");
        s.src = src;
        s.async = false;
        s.dataset.dyn = src;
        s.onload = () => {
            loadedScripts.add(src);
            resolve();
        };
        s.onerror = () => reject(new Error("JS dosyası yüklenemedi: " + src));
        document.head.appendChild(s);
    });
}

/* -------------------------
   PAGE MODULES
   Key: pathname (query yok)
   js: string | string[]
   ------------------------- */
const pageModules = {
    "/wellcome": { js: "/obs_js_files/wellcome.js", init: "getWellcomeData" },
    "/cari/ekstre": {
        js: "/obs_js_files/cari/cariekstre.js",
        init: () => {
            if (typeof window.cariBaslik === "function") window.cariBaslik();
        }
    },
    "/cari/hspplngiris": {
        js: "/obs_js_files/cari/hspplngiris.js",
        init: () => {
            if (typeof window.cariBaslik === "function") window.cariBaslik();
            if (typeof OBS?.HSPPLN?.init === "function") OBS.HSPPLN.init();
            if (typeof OBS?.HSPPLN?.aramaYap === "function") OBS.HSPPLN.aramaYap();
            const arama = document.getElementById("arama");
            if (arama) arama.value = "";
        }
    },
    "/cari/hspplnliste": {
        js: "/obs_js_files/cari/hspplnliste.js",
        init: () => {
            if (typeof window.cariBaslik === "function") window.cariBaslik();
        }
    },
    "/cari/dekont": {
        js: "/obs_js_files/cari/dekont.js",
        init: () => {
            if (typeof window.cariBaslik === "function") window.cariBaslik();
			if (typeof OBS?.DEKONT?.init === "function") OBS.DEKONT.init();
        }
    },
    "/cari/tahsilat": {
        js: "/obs_js_files/cari/tahsilat.js",
        init: () => {
            if (typeof window.cariBaslik === "function") window.cariBaslik();
            if (typeof window.fetchHesapKodlariOnce === "function") window.fetchHesapKodlariOnce();
        }
    },
    "/cari/mizan": { js: "/obs_js_files/cari/mizan.js", init: () => { window.cariBaslik() } },
    "/cari/ozelmizan": { js: "/obs_js_files/cari/ozelmizan.js", init: () => { window.cariBaslik() } },
    "/cari/dvzcevirme": { js: "/obs_js_files/cari/dvzcevirme.js", init: () => { window.cariBaslik() } },
    "/cari/gunlukkontrol": { js: "/obs_js_files/cari/gunluktakip.js", init: () => { window.cariBaslik() } },
    "/cari/tahsilatrapor": { js: "/obs_js_files/cari/tahsilatrapor.js", init: () => { window.cariBaslik() } },
    "/cari/tahsilatdegerleri": {
        js: "/obs_js_files/cari/tahsilatayar.js",
        init: () => {
            if (typeof window.cariBaslik === "function") window.cariBaslik();
            if (typeof OBS?.TAHAYAR?.init === "function") OBS.TAHAYAR.init();
        }
    },
    "/cari/ornekhesapplani": { js: "/obs_js_files/cari/ornhsppln.js", init: () => { window.cariBaslik() } },
    "/cari/carikoddegis": {
        js: "/obs_js_files/cari/koddegis.js", init: () => { window.cariBaslik() }
    },
    "/obs/firmaismi": {
        js: "/obs_js_files/ortak/firmaismi.js",
        init: (params) => {
            const modul = params?.get("modul") || "";
            if (typeof OBS?.FIRMA?.firmaIsmiSayfa === "function") OBS.FIRMA.firmaIsmiSayfa(modul);
        }
    },
    "/kur/kurgiris": {
        js: "/obs_js_files/kur/kurgiris.js",
        init: () => {
            if (typeof window.kurBaslik === "function") window.kurBaslik();
            OBS?.KUR?.init?.()
        }
    },
    "/kur/kurrapor": {
        js: "/obs_js_files/kur/kurrapor.js",
        init: () => {
            if (typeof window.kurBaslik === "function") window.kurBaslik();
        }
    },
    "/adres/adresgiris": {
        js: "/obs_js_files/adres/adresgiris.js",
        init: () => {
            if (typeof window.adresBaslik === "function") window.adresBaslik();
            if (typeof OBS?.ADR?.init === "function") OBS.ADR.init();
            if (typeof OBS?.ADR?.aramaYap === "function") OBS.ADR.aramaYap();
            const arama = document.getElementById("arama");
            if (arama) arama.value = "";
        }
    },
    "/adres/etiketliste": {
        js: "/obs_js_files/adres/etiketlistele.js",
        init: () => {
            if (typeof window.adresBaslik === "function") window.adresBaslik();
        }
    },
    "/adres/etiketayar": {
        js: "/obs_js_files/adres/etiketayar.js",
        init: () => {
            if (typeof window.adresBaslik === "function") window.adresBaslik();
            if (typeof OBS?.ADR?.init === "function") OBS.ADR.init();
        }
    },
    "/kambiyo/cekgiris": {
        js: "/obs_js_files/kambiyo/cekgiris.js",
        init: () => {
            if (typeof window.kambiyoBaslik === "function") window.kambiyoBaslik();
            if (typeof OBS?.CEKGIR?.init === "function") OBS.CEKGIR.init();
        }
    },
    "/kambiyo/cekcikis": {
        js: "/obs_js_files/kambiyo/cekcikis.js",
        init: () => {
            if (typeof window.kambiyoBaslik === "function") window.kambiyoBaslik();
            if (typeof OBS?.CEKCIK?.init === "function") OBS.CEKCIK.init();
        }
    },
    "/kambiyo/cektakip": {
        js: "/obs_js_files/kambiyo/cekkontrol.js",
        init: () => {
            if (typeof window.kambiyoBaslik === "function") window.kambiyoBaslik();
        }
    },
    "/kambiyo/cekrapor": {
        js: "/obs_js_files/kambiyo/cekrapor.js",
        init: () => {
            if (typeof window.kambiyoBaslik === "function") window.kambiyoBaslik();
            if (typeof OBS?.CEKRAP?.init === "function") OBS.CEKRAP.init();
        }
    },
    "/stok/urunkart": {
        js: "/obs_js_files/stok/urunkart.js",
        init: () => {
            if (typeof window.stokBaslik === "function") window.stokBaslik();
            if (typeof OBS?.URUNKART?.init === "function") OBS.URUNKART.init();
            if (typeof OBS?.URUNKART?.aramaYap === "function") OBS.URUNKART.aramaYap("Kodu");
            const arama = document.getElementById("arama");
            if (arama) arama.value = "";
        }
    },
    "/stok/uretim": {
        js: "/obs_js_files/stok/uretim.js",
        init: () => {
            if (typeof window.stokBaslik === "function") window.stokBaslik();
            if (typeof OBS?.URETIM?.init === "function") OBS.URETIM.init();
        }
    },
    "/stok/fatura": {
        js: "/obs_js_files/stok/fatura.js",
        init: () => {
            if (typeof window.stokBaslik === "function") window.stokBaslik();
            if (typeof OBS?.FATURA?.init === "function") OBS.FATURA.init();
        }
    },
    "/stok/irsaliye": {
        js: "/obs_js_files/stok/irsaliye.js",
        init: () => {
            if (typeof window.stokBaslik === "function") window.stokBaslik();
            if (typeof OBS?.IRSALIYE?.init === "function") OBS.IRSALIYE.init();
        }
    },
    "/stok/zayi": {
        js: "/obs_js_files/stok/zayi.js",
        init: () => {
            if (typeof window.stokBaslik === "function") window.stokBaslik();
            if (typeof OBS?.ZAI?.fetchkod === "function") OBS.ZAI.fetchkod();
        }
    },
    "/stok/recete": {
        js: "/obs_js_files/stok/recete.js",
        init: () => {
            if (typeof window.stokBaslik === "function") window.stokBaslik();
            if (typeof OBS?.RECETE?.fetchkod === "function") OBS.RECETE.fetchkod();
        }
    },
    "/stok/fatrapor": { js: "/obs_js_files/stok/raporlar/fatrapor.js", init: () => { if (typeof window.stokBaslik === "function") window.stokBaslik(); } },
    "/stok/irsrapor": { js: "/obs_js_files/stok/raporlar/irsrapor.js", init: () => { if (typeof window.stokBaslik === "function") window.stokBaslik(); } },
    "/stok/envanter": { js: "/obs_js_files/stok/raporlar/envanter.js", init: () => { if (typeof window.stokBaslik === "function") window.stokBaslik(); } },
    "/stok/imarapor": { js: "/obs_js_files/stok/raporlar/imarapor.js", init: () => { if (typeof window.stokBaslik === "function") window.stokBaslik(); } },
    "/stok/imagrprapor": { js: "/obs_js_files/stok/raporlar/imagrprapor.js", init: () => { if (typeof window.stokBaslik === "function") window.stokBaslik(); } },
    "/stok/stokrapor": { js: "/obs_js_files/stok/raporlar/stokrapor.js", init: () => { if (typeof window.stokBaslik === "function") window.stokBaslik(); } },
    "/stok/grprapor": { js: "/obs_js_files/stok/raporlar/grprapor.js", init: () => { if (typeof window.stokBaslik === "function") window.stokBaslik(); } },
    "/stok/stokdetay": { js: "/obs_js_files/stok/raporlar/stokdetay.js", init: () => { if (typeof window.stokBaslik === "function") window.stokBaslik(); } },
    "/stok/degiskenler": {
        js: "/obs_js_files/stok/degiskenler.js",
        init: () => {
            if (typeof window.stokBaslik === "function") window.stokBaslik();
            if (typeof OBS?.DEGISKENLER?.init === "function") OBS.DEGISKENLER.init();
        }
    },
    "/kereste/giris": {
        js: ["/obs_js_files/inputmask/inputmask.min.js",
            "/obs_js_files/kereste/giris.js"],
        init: () => {
            if (typeof window.keresteBaslik === "function") window.keresteBaslik();
            if (typeof OBS?.KERGIRIS?.init === "function") OBS.KERGIRIS.init();
        }
    },
    "/kereste/cikis": {
        js: ["/obs_js_files/inputmask/inputmask.min.js",
            "/obs_js_files/kereste/cikis.js"],
        init: () => {
            if (typeof window.keresteBaslik === "function") window.keresteBaslik();
            if (typeof OBS?.KERCIKIS?.init === "function") OBS.KERCIKIS.init();
        }
    },
    "/kereste/koddegis": {
        js: ["/obs_js_files/inputmask/inputmask.min.js",
            "/obs_js_files/kereste/koddegis.js"],
        init: () => {
            if (typeof window.keresteBaslik === "function") window.keresteBaslik();
            if (typeof OBS?.KODDEGIS?.init === "function") OBS.KODDEGIS.init();
        }
    },
    "/kereste/fatrapor": {
        js: "/obs_js_files/kereste/raporlar/fatrapor.js",
        init: () => {
            if (typeof window.keresteBaslik === "function") window.keresteBaslik();
        }
    },
    "/kereste/detay": { js: "/obs_js_files/kereste/raporlar/kerdetay.js", init: () => { if (typeof window.keresteBaslik === "function") window.keresteBaslik(); } },
    "/kereste/grprapor": { js: "/obs_js_files/kereste/raporlar/grprapor.js", init: () => { if (typeof window.keresteBaslik === "function") window.keresteBaslik(); } },
    "/kereste/envanter": { js: "/obs_js_files/kereste/raporlar/envanter.js", init: () => { if (typeof window.keresteBaslik === "function") window.keresteBaslik(); } },
    "/kereste/ortfiat": { js: "/obs_js_files/kereste/raporlar/ortfiat.js", init: () => { if (typeof window.keresteBaslik === "function") window.keresteBaslik(); } },
    "/kereste/kodaciklama": {
        js: "/obs_js_files/kereste/kodaciklama.js",
        init: () => {
            if (typeof window.keresteBaslik === "function") window.keresteBaslik();
            if (typeof window.kodacikyukle === "function") window.kodacikyukle();
        }
    },
    "/kereste/konsimentoaciklama": {
        js: "/obs_js_files/kereste/konsaciklama.js",
        init: () => {
            if (typeof window.keresteBaslik === "function") window.keresteBaslik();
            if (typeof window.konsacikyukle === "function") window.konsacikyukle();
        }
    },
    "/kereste/degiskenler": {
        js: "/obs_js_files/kereste/degiskenler.js",
        init: () => {
            if (typeof window.keresteBaslik === "function") window.keresteBaslik();
            if (typeof OBS?.KERDEGISKENLER?.init === "function") OBS.KERDEGISKENLER.init();
        }
    },
    "/user/gidenraporlar": { js: "/obs_js_files/giden_rapor/gidenrapor.js" },
    "/user/mailgonderme": {
        js: "/obs_js_files/sendemail/sendemail.js",
        init: () => { if (typeof OBS?.REPORTMAIL?.init === "function") OBS.REPORTMAIL.init(); }
    },
    "/send_email": {
        js: "/obs_js_files/sendemail/sendemail.js",
        init: () => { if (typeof OBS?.REPORTMAIL?.init === "function") OBS.REPORTMAIL.init(); }
    },
    "/user/lograpor": { js: "/obs_js_files/user/lograpor.js", init: () => OBS?.LOG?.updatePaginationUI?.() },
    "/backup/backuptakip": { js: "/obs_js_files/backup/backuptakip.js" },
    "/backup/lograpor": { js: "/obs_js_files/backup/lograpor.js" },
    "/user/user_pwd_change": { js: "/obs_js_files/user/sifredegis.js", init: () => OBS?.PWD?.init?.() },
    "/user/user_update": { js: "/obs_js_files/user/userupdate.js", init: () => OBS?.USERUPD?.init?.() },
    "/user/userdetails": {
        js: "/obs_js_files/user/userDetails.js",
        init: () => {
            OBS?.USERDETAIL?.init?.();
            OBS?.USERDETAIL?.detailoku?.();
        }
    },
    "/user/userizinler": {
        js: "/obs_js_files/user/userizinleri.js",
        init: () => {
            OBS?.USERIZIN?.init?.();
            OBS?.USERIZIN?.adminbaglihesapoku?.();
        }
    },
    "/user/userlist": {
        js: "/obs_js_files/user/userlist.js",
        init: () => { OBS?.USERS_LIST?.init?.(); }
    },
    "/user/emailsettings": {
        js: "/obs_js_files/user/emailayar.js",
        init: () => { if (window.emailSettingsInit) window.emailSettingsInit(); }
    },
    "/user/createdb": {
        js: "/obs_js_files/server/createdb.js",
        init: () => { if (window.createDbInit) window.createDbInit(); }
    },
    "/user/copyuser": { js: "/obs_js_files/user/copyuser.js" },
    "/user/deleteaccount": {
        js: "/obs_js_files/user/deleteuser.js",
        init: () => { if (window.accountDeleteInit) window.accountDeleteInit(); }
    },
    "/forum": {
        js: "/obs_js_files/forum/forum.js",
        init: () => { OBS?.FORUM?.init?.(); }
    },
		"/gunluk/gunluk": {js: "/obs_js_files/gunluk/gunluk.js",
			   init: () => {if (window.OBS?.SCHEDULER?.init) OBS.SCHEDULER.init();}
		 },
		
	"/user/shortcut": {
	     js: "/obs_js_files/user/shortcut/shortcuts_settings.js",
	     init: () => {if (typeof OBS?.SHORTCUT_SETTINGS?.init === "function") OBS.SHORTCUT_SETTINGS.init();
	}
	},
    "/gps/gps": {
        js: "/obs_js_files/gps/gps.js",
    }
};

/* -------------------------
   Module runner 
   ------------------------- */
async function runPageModule(url) {
    const u = new URL(url, window.location.origin);
    const path = u.pathname;
    const params = u.searchParams;
    const mod = pageModules[path];
    if (!mod) return;
    const jsList = Array.isArray(mod.js) ? mod.js : (mod.js ? [mod.js] : []);
    for (const src of jsList) {
        await loadScriptOnce(src);
    }
    const fn = (typeof mod.init === "function") ? mod.init : window[mod.init];
    if (typeof fn === "function") fn(params, u);
}

async function sayfaYukle(url) {
    const ara = ROOT.ara;
    const baslik = ROOT.baslik;
    if (!ara) return;
    try {
        if (baslik) baslik.innerText = "";
        const html = await fetchWithSessionCheckForm(url);
        if (!html) return;
        ara.innerHTML = html;
        try {
            await runPageModule(url);
        } catch (e) {
            ara.innerHTML += `<div class="cam-error">${e.message}</div>`;
        }
        const p = new URL(url, location.origin).pathname;
        if (window.urlActions?.[p]) {
            try { window.urlActions[p](); } catch {}
        }
    } catch (e) {
        ara.innerHTML = `<div class="cam-error">Bir hata: ${e?.message ?? "Bilinmeyen hata"}</div>`;
    }
}

document.addEventListener("click", (e) => {
    const link = e.target.closest(".changeLink");
    if (!link) return;
    e.preventDefault();
    const rawUrl = link.getAttribute("data-url");
    if (!rawUrl) return;
    const u = new URL(rawUrl, window.location.origin);

    for (const [k, v] of Object.entries(link.dataset)) {
        if (k === "url") continue;
        if (v != null && String(v).trim() !== "") u.searchParams.set(k, v);
    }
    sayfaYukle(u.pathname + u.search);
});

document.getElementById("sidebarCollapse")?.addEventListener("click", () => {
    document.getElementById("appShell")?.classList.toggle("is-collapsed");
});

window.addEventListener("load", () => {
    const trigger = new URLSearchParams(window.location.search).get("trigger");
    if (trigger === "userdetails") sayfaYukle("/user/userdetails");
    else sayfaYukle("/wellcome");
});


window.sayfaYukle = sayfaYukle;
