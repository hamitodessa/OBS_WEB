/* =========================
   KAMBİYO – CEK TAKIP (çakışmasız)
   Namespace: OBS.CEKTAKIP
   ========================= */
window.OBS ||= {};
OBS.CEKTAKIP ||= {};

/* ---------- helpers ---------- */
OBS.CEKTAKIP._root = () =>
    document.getElementById("kmb_content") ||
    document.getElementById("ara_content") ||
    document;

OBS.CEKTAKIP.byId = (id) => OBS.CEKTAKIP._root().querySelector("#" + id);

OBS.CEKTAKIP._cursor = (wait) => { document.body.style.cursor = wait ? "wait" : "default"; };

OBS.CEKTAKIP._setErr = (msg) => {
    const e = OBS.CEKTAKIP.byId("errorDiv") || document.getElementById("errorDiv");
    if (!e) return;
    if (msg) { e.style.display = "block"; e.innerText = msg; }
    else { e.style.display = "none"; e.innerText = ""; }
};

OBS.CEKTAKIP._setText = (id, val) => {
    const el = OBS.CEKTAKIP.byId(id) || document.getElementById(id);
    if (el) el.innerText = (val ?? "");
};

OBS.CEKTAKIP._setValue = (id, val) => {
    const el = OBS.CEKTAKIP.byId(id) || document.getElementById(id);
    if (el) el.value = (val ?? "");
};

/* =========================
   INIT (opsiyonel)
   ========================= */
OBS.CEKTAKIP.init = function() {
    // Sayfa açılınca default tarih:
    OBS.CEKTAKIP.clearForm();

    // Enter ile arama:
    const arama = OBS.CEKTAKIP.byId("arama") || document.getElementById("arama");
    if (arama) {
        arama.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                OBS.CEKTAKIP.aramaYap();
            }
        });
    }
};

/* =========================
   aramaYap
   ========================= */
OBS.CEKTAKIP.aramaYap = async function() {
    const aramaEl = OBS.CEKTAKIP.byId("arama") || document.getElementById("arama");
    const aramaVal = (aramaEl?.value || "").trim();

    if (aramaVal === "") {
        OBS.CEKTAKIP.clearForm();
        return;
    }

    const cekNo = aramaVal;

    OBS.CEKTAKIP._cursor(true);
    OBS.CEKTAKIP.clearForm();

    try {
        const response = await fetchWithSessionCheck("kambiyo/cektakipkontrol", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ cekNo })
        });

        if (response?.errorMessage?.trim?.() !== "") {
            throw new Error(response.errorMessage);
        }

        const d = response?.data || {};

        OBS.CEKTAKIP._setText("cekno", d.cekNo);
        OBS.CEKTAKIP._setText("banka", d.banka);
        OBS.CEKTAKIP._setText("cikisbordro", d.cikisBordro);
        OBS.CEKTAKIP._setText("cikishesapkod", d.cikisMusteri);

        // cikis tarihi 1900-01-01 ise boş
        if (d.cikisTarihi === "1900-01-01") OBS.CEKTAKIP._setText("cikistarihi", "");
        else OBS.CEKTAKIP._setText("cikistarihi", d.cikisTarihi ? formatDate(d.cikisTarihi) : "");

        OBS.CEKTAKIP._setText("vade", d.vade ? formatDate(d.vade) : "");
        OBS.CEKTAKIP._setText("girisbordro", d.girisBordro);
        OBS.CEKTAKIP._setText("girishesapkod", d.girisMusteri);
        OBS.CEKTAKIP._setText("giristarihi", d.girisTarihi ? formatDate(d.girisTarihi) : "");
        OBS.CEKTAKIP._setText("sube", d.sube);
        OBS.CEKTAKIP._setText("serino", d.seriNo);
        OBS.CEKTAKIP._setText("ilkborclu", d.ilkBorclu);
        OBS.CEKTAKIP._setText("cekhesapno", d.cekHesapNo);
        OBS.CEKTAKIP._setText("tutar", formatNumber2(d.tutar));
        OBS.CEKTAKIP._setText("gozelkod", d.girisOzelKod);
        OBS.CEKTAKIP._setText("cozelkod", d.cikisOzelKod);

        OBS.CEKTAKIP._setValue("ttarih", d.ttarih || "");

        // durum -> select index
        const islem = OBS.CEKTAKIP.byId("islem") || document.getElementById("islem");
        const durum = parseInt(d.durum, 10);

        if (islem) {
            if (Number.isNaN(durum)) islem.selectedIndex = 0;
            else if (durum === 1) islem.selectedIndex = 1;
            else if (durum === 2) islem.selectedIndex = 2;
            else if (durum === 3) islem.selectedIndex = 3;
            else islem.selectedIndex = 0;
        }

        OBS.CEKTAKIP._setErr("");
    } catch (error) {
        OBS.CEKTAKIP._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
        OBS.CEKTAKIP._cursor(false);
    }
};

/* =========================
   cekdurumKayit
   ========================= */
OBS.CEKTAKIP.cekdurumKayit = async function() {
    const ceknoTxt = (OBS.CEKTAKIP.byId("cekno") || document.getElementById("cekno"))?.innerText || "";
    if (ceknoTxt.trim() === "") return;

    const cekno = ceknoTxt.trim();
    const islem = OBS.CEKTAKIP.byId("islem") || document.getElementById("islem");
    const durum = islem ? islem.selectedIndex : 0;
    const ttarih = (OBS.CEKTAKIP.byId("ttarih") || document.getElementById("ttarih"))?.value || "";

    OBS.CEKTAKIP._cursor(true);

    try {
        const response = await fetchWithSessionCheck("kambiyo/cektakipkaydet", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ cekno, durum, ttarih })
        });

        if (response?.errorMessage?.trim?.() !== "") {
            throw new Error(response.errorMessage);
        }

        OBS.CEKTAKIP.clearForm();

        const arama = OBS.CEKTAKIP.byId("arama") || document.getElementById("arama");
        if (arama) arama.value = "";

        OBS.CEKTAKIP._setErr("");
    } catch (error) {
        OBS.CEKTAKIP._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
        OBS.CEKTAKIP._cursor(false);
    }
};

/* =========================
   clearForm
   ========================= */
OBS.CEKTAKIP.clearForm = function() {
    OBS.CEKTAKIP._setText("cekno", "");
    OBS.CEKTAKIP._setText("banka", "");
    OBS.CEKTAKIP._setText("cikisbordro", "");
    OBS.CEKTAKIP._setText("cikishesapkod", "");
    OBS.CEKTAKIP._setText("cikistarihi", "");
    OBS.CEKTAKIP._setText("vade", "");
    OBS.CEKTAKIP._setText("girisbordro", "");
    OBS.CEKTAKIP._setText("girishesapkod", "");
    OBS.CEKTAKIP._setText("giristarihi", "");
    OBS.CEKTAKIP._setText("sube", "");
    OBS.CEKTAKIP._setText("serino", "");
    OBS.CEKTAKIP._setText("ilkborclu", "");
    OBS.CEKTAKIP._setText("cekhesapno", "");
    OBS.CEKTAKIP._setText("tutar", formatNumber2(0));
    OBS.CEKTAKIP._setText("gozelkod", "");
    OBS.CEKTAKIP._setText("cozelkod", "");

    // bugün
    const today = new Date();
    const formattedDate = today.toISOString().split("T")[0];
    OBS.CEKTAKIP._setValue("ttarih", formattedDate);

    const islem = OBS.CEKTAKIP.byId("islem") || document.getElementById("islem");
    if (islem) islem.selectedIndex = 0;

    OBS.CEKTAKIP._setErr("");
};
