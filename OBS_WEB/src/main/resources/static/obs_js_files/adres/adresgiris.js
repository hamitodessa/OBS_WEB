/* =========================
   ADR (çakışmasız)
   Namespace: OBS.ADR
   Scope: #ara_content (fallback document)
   ========================= */
window.OBS ||= {};
OBS.ADR ||= {};

/* ---------- helpers ---------- */
OBS.ADR._root = () =>
    document.getElementById("adr_content") ||
    document.getElementById("ara_content") ||
    document;

OBS.ADR.byId = (id) => document.getElementById(id);
OBS.ADR.qsa = (sel) => Array.from(OBS.ADR._root().querySelectorAll(sel));

OBS.ADR._setErr = (msg) => {
    const e = OBS.ADR.byId("errorDiv");
    if (!e) return;
    if (msg) { e.style.display = "block"; e.innerText = msg; }
    else { e.style.display = "none"; e.innerText = ""; }
};

OBS.ADR._setCursor = (wait) => { document.body.style.cursor = wait ? "wait" : "default"; };

/* ---------- form helpers ---------- */
OBS.ADR.clearInputs = function() {
    const inputs = OBS.ADR.qsa(".form-control");
    inputs.forEach((input) => {
        if (input.type === "checkbox") input.checked = false;
        else if (input.type === "file") input.value = "";
        else if (input.name !== "arama" && input.id !== "arama") input.value = "";
    });

    const idEl = OBS.ADR.byId("adrid");
    if (idEl) idEl.value = 0;

    const img = OBS.ADR.byId("resimGoster");
    if (img) { img.src = ""; img.style.display = "none"; }

    const kk = OBS.ADR.byId("kodKontrol");
    if (kk) kk.innerText = "";
};

OBS.ADR.enableInputs = function() {
    OBS.ADR.clearInputs();
    OBS.ADR.qsa(".form-control").forEach((input) => { input.disabled = false; });
};

OBS.ADR.enableDuzeltmeInputs = function() {
    OBS.ADR.qsa(".form-control").forEach((input) => {
        if (input.id !== "kodu") input.disabled = false;
    });
};

OBS.ADR.disableInputs = function() {
    OBS.ADR.qsa(".form-control").forEach((input) => {
        if (input.id !== "arama" && input.name !== "arama") input.disabled = true;
    });
};

/* ---------- DTO ---------- */
OBS.ADR.getDTO = function() {
    return {
        kodu: OBS.ADR.byId("kodu")?.value,
        unvan: OBS.ADR.byId("unvan")?.value,
        ad1: OBS.ADR.byId("ad1")?.value,
        semt: OBS.ADR.byId("semt")?.value,
        ad2: OBS.ADR.byId("ad2")?.value,
        seh: OBS.ADR.byId("seh")?.value,
        pkodu: OBS.ADR.byId("pkodu")?.value,
        smsgon: !!OBS.ADR.byId("smsgon")?.checked,
        mailgon: !!OBS.ADR.byId("mailgon")?.checked,
        vd: OBS.ADR.byId("vd")?.value,
        vn: OBS.ADR.byId("vn")?.value,
        t1: OBS.ADR.byId("t1")?.value,
        t2: OBS.ADR.byId("t2")?.value,
        t3: OBS.ADR.byId("t3")?.value,
        fx: OBS.ADR.byId("fx")?.value,
        o1: OBS.ADR.byId("o1")?.value,
        o2: OBS.ADR.byId("o2")?.value,
        web: OBS.ADR.byId("web")?.value,
        ozel: OBS.ADR.byId("ozel")?.value,
        mail: OBS.ADR.byId("mail")?.value,
        acik: OBS.ADR.byId("acik")?.value,
        not1: OBS.ADR.byId("not1")?.value,
        not2: OBS.ADR.byId("not2")?.value,
        not3: OBS.ADR.byId("not3")?.value,
        yetkili: OBS.ADR.byId("yetkili")?.value,
        id: OBS.ADR.byId("adrid")?.value
    };
};

/* ---------- sayfa yukle ---------- */
OBS.ADR.sayfaYukle = async function() {
    const url = "adres/adresgiris";
    try {
        OBS.ADR._setCursor(true);
        const response = await fetchWithSessionCheck(url, { method: "GET" });


        const ara = document.getElementById("ara_content");
        if (ara) ara.innerHTML = response.data;

        // eski akışın aynısı:
        if (typeof window.adresBaslik === "function") window.adresBaslik();
        if (typeof OBS.ADR.aramaYap === "function") OBS.ADR.aramaYap();
        const arama = OBS.ADR.byId("arama");
        if (arama) arama.value = "";
    } catch (error) {
        const ara = document.getElementById("ara_content");
        if (ara) ara.innerHTML = `<h2>${error.message}</h2>`;
    } finally {
        OBS.ADR._setCursor(false);
    }
};

/* ---------- kayit ---------- */
OBS.ADR.kayit = async function() {
    const koduEl = OBS.ADR.byId("kodu");
    if (!koduEl || ["0", ""].includes(koduEl.value)) return;

    const adresDTO = OBS.ADR.getDTO();
    const formData = new FormData();
    for (const key in adresDTO) formData.append(key, adresDTO[key] ?? "");

    const fileInput = OBS.ADR.byId("resim");
    const file = fileInput?.files?.[0];
    if (file) formData.append("resim", file);

    const imgElement = OBS.ADR.byId("resimGoster");
    const base64Data = (imgElement?.src && imgElement.src.startsWith("data:image"))
        ? imgElement.src.split(",")[1]
        : null;

    if (base64Data) {
        const byteCharacters = atob(base64Data);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0;i < byteCharacters.length;i++) byteNumbers[i] = byteCharacters.charCodeAt(i);
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: "image/jpeg" });
        formData.append("resimGoster", blob, "base64Resim.jpg");
    }

    OBS.ADR._setErr("");
    OBS.ADR._setCursor(true);

    try {
        // fetchWithSessionCheck global kalabilir (senin ortak helper)
        const response = await fetchWithSessionCheck("adres/adrkayit", {
            method: "POST",
            body: formData
        });

        if (response?.errorMessage) throw new Error(response.errorMessage);

        await OBS.ADR.sayfaYukle();
    } catch (error) {
        OBS.ADR._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
        OBS.ADR._setCursor(false);
    }
};

/* ---------- arama ---------- */
OBS.ADR.aramaYap = async function() {
    const aramaEl = OBS.ADR.byId("arama");
    const aramaInput = (aramaEl?.value || "").trim();

    const kk = OBS.ADR.byId("kodKontrol");
    if (kk) kk.innerText = "";

    if (aramaInput === "") { OBS.ADR.ilk(); return; }

    OBS.ADR._setCursor(true);
    OBS.ADR._setErr("");

    try {
        const dto = await fetchWithSessionCheck("adres/adresArama", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ arama: aramaInput })
        });

        if (dto?.errorMessage) {
            OBS.ADR._setErr(dto.errorMessage);
            return;
        }

        const setVal = (id, v) => { const el = OBS.ADR.byId(id); if (el) el.value = v ?? ""; };
        const setChk = (id, v) => { const el = OBS.ADR.byId(id); if (el) el.checked = !!v; };

        setVal("kodu", dto.kodu);
        setVal("unvan", dto.unvan);
        setVal("ad1", dto.ad1);
        setVal("semt", dto.semt);
        setVal("ad2", dto.ad2);
        setVal("seh", dto.seh);
        setVal("pkodu", dto.pkodu);
        setChk("smsgon", dto.smsgon);
        setChk("mailgon", dto.mailgon);
        setVal("vd", dto.vd);
        setVal("vn", dto.vn);
        setVal("t1", dto.t1);
        setVal("t2", dto.t2);
        setVal("t3", dto.t3);
        setVal("fx", dto.fx);
        setVal("o1", dto.o1);
        setVal("o2", dto.o2);
        setVal("web", dto.web);
        setVal("ozel", dto.ozel);
        setVal("mail", dto.mail);
        setVal("acik", dto.acik);
        setVal("not1", dto.not1);
        setVal("not2", dto.not2);
        setVal("not3", dto.not3);
        setVal("yetkili", dto.yetkili);
        setVal("adrid", dto.id);

        const img = OBS.ADR.byId("resimGoster");
        if (img) {
            if (dto.base64Resim && dto.base64Resim.trim() !== "") {
                img.src = "data:image/jpeg;base64," + dto.base64Resim.trim();
                img.style.display = "block";
            } else {
                img.src = "";
                img.style.display = "none";
            }
        }

        OBS.ADR.disableInputs();
        OBS.ADR.enableDuzeltmeInputs();
        OBS.ADR._setErr("");
    } catch (error) {
        OBS.ADR._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
        OBS.ADR._setCursor(false);
    }
};

/* ---------- ileri / geri / ilk ---------- */
OBS.ADR._options = () => {
    const dl = OBS.ADR.byId("hesapOptions");
    return dl ? Array.from(dl.getElementsByTagName("option")) : [];
};

OBS.ADR.geri = function() {
    const kod = OBS.ADR.byId("kodu")?.value;
    const opts = OBS.ADR._options();
    if (!opts.length) return null;

    const idx = opts.findIndex(o => o.value === kod);
    if (idx > 0) {
        const arama = OBS.ADR.byId("arama");
        if (arama) arama.value = opts[idx - 1].value;
        OBS.ADR.aramaYap();
        if (arama) arama.value = "";
    }
    return null;
};

OBS.ADR.ileri = function() {
    const kod = OBS.ADR.byId("kodu")?.value;
    const opts = OBS.ADR._options();
    if (!opts.length) return null;

    const idx = opts.findIndex(o => o.value === kod);
    if (idx !== -1 && idx < opts.length - 1) {
        const arama = OBS.ADR.byId("arama");
        if (arama) arama.value = opts[idx + 1].value;
        OBS.ADR.aramaYap();
        if (arama) arama.value = "";
    }
    return null;
};

OBS.ADR.ilk = function() {
    const opts = OBS.ADR._options();
    if (!opts.length) {
        OBS.ADR.clearInputs();
        OBS.ADR.disableInputs();
        return null;
    }
    const arama = OBS.ADR.byId("arama");
    if (arama) arama.value = opts[0].value;
    OBS.ADR.aramaYap();
    if (arama) arama.value = "";
    return null;
};

/* ---------- sil ---------- */
OBS.ADR.sil = async function() {
    const adrId = OBS.ADR.byId("adrid")?.value;
    if (["0", ""].includes(adrId)) return;

    if (!confirm("Kayit Dosyadan Silinecek ..?")) return;

    OBS.ADR._setCursor(true);
    OBS.ADR._setErr("");

    try {
        const response = await fetchWithSessionCheck("adres/adrSil", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ adrId })
        });

        if (response?.errorMessage) throw new Error(response.errorMessage);

        await OBS.ADR.sayfaYukle();
    } catch (error) {
        OBS.ADR._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
        OBS.ADR._setCursor(false);
    }
};

/* ---------- resim sil ---------- */
OBS.ADR.resimSil = function() {
    OBS.ADR._setErr("");
    OBS.ADR._resetFileUI();
};


OBS.ADR._setImgBase64 = function(base64) {
    const img = OBS.ADR.byId("resimGoster");
    if (!img) return;

    if (base64 && base64.trim() !== "") {
        img.src = "data:image/jpeg;base64," + base64.trim();
        img.style.display = "block";
    } else {
        img.src = "";
        img.style.display = "none";
    }
};

OBS.ADR._appendBase64ImgAsBlob = function(formData, imgId, formKey, filename) {
    const img = OBS.ADR.byId(imgId);
    if (!img) return;

    const src = img.src || "";
    if (!src.startsWith("data:image")) return;

    const base64 = src.split(",")[1];
    if (!base64) return;

    const byteCharacters = atob(base64);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0;i < byteCharacters.length;i++) byteNumbers[i] = byteCharacters.charCodeAt(i);
    const byteArray = new Uint8Array(byteNumbers);

    const blob = new Blob([byteArray], { type: "image/jpeg" });
    formData.append(formKey, blob, filename);
};


OBS.ADR._MAX_KB = 500;
OBS.ADR.onFileChanged = function() {
    const input = OBS.ADR.byId("resim");
    if (!input) return;

    const file = input.files?.[0];
    if (!file) {
        OBS.ADR._setFileName("Dosya seçilmedi");
        return;
    }

    if (file.type && !file.type.startsWith("image/")) {
        OBS.ADR._setErr("Lütfen bir resim dosyası seçin!");
        OBS.ADR._resetFileUI();
        return;
    }

    const maxBytes = OBS.ADR._MAX_KB * 1024;
    if (file.size > maxBytes) {
        OBS.ADR._setErr(`Dosya boyutu ${OBS.ADR._MAX_KB} KB'ı geçemez!`);
        OBS.ADR._resetFileUI();
        return;
    }

    OBS.ADR._setErr("");
    OBS.ADR._setFileName(file.name);

    // preview
    const reader = new FileReader();
    reader.onload = function(e) {
        const img = OBS.ADR.byId("resimGoster");
        if (!img) return;
        img.src = e.target.result;
        img.style.display = "block";

    };
    reader.readAsDataURL(file);
};

OBS.ADR._setFileName = function(text) {
    const el = OBS.ADR.byId("resimFileName");
    if (!el) return;
    el.textContent = text || "Dosya seçilmedi";
};

OBS.ADR._resetFileUI = function() {
    const inp = OBS.ADR.byId("resim");
    if (inp) inp.value = "";

    const img = OBS.ADR.byId("resimGoster");
    if (img) {
        img.src = "";
        img.style.display = "none";
    }

    OBS.ADR._setFileName("Dosya seçilmedi");
};

OBS.ADR._bindFileEvents = function() {
    const logoInput = OBS.ADR.byId("resim");
    if (logoInput && logoInput.dataset.bound !== "1") {
        logoInput.dataset.bound = "1";
        logoInput.addEventListener("change", OBS.ADR.onFileChanged);
    }

};

/* ---------- init (event bağla) ---------- */
OBS.ADR.init = function() {

    OBS.ADR._bindFileEvents();
};
