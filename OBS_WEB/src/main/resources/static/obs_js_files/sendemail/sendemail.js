window.OBS = window.OBS || {};
OBS.REPORTMAIL = OBS.REPORTMAIL || {};

OBS.REPORTMAIL.byId = (id) => document.getElementById(id);

OBS.REPORTMAIL.init = function () {
  OBS.REPORTMAIL.tabloyukle();
};

OBS.REPORTMAIL.tabloyukle = function () {
  document.body.style.cursor = "wait";

  const extra   = OBS.REPORTMAIL.byId("extraValue");
  const format  = OBS.REPORTMAIL.byId("format");
  const degerler= OBS.REPORTMAIL.byId("degerler");

  if (extra) extra.value = "";

  const tableData = localStorage.getItem("tableData");
  const grprapor  = localStorage.getItem("grprapor");
  const baslik    = localStorage.getItem("tablobaslik");

  if (tableData && extra && format) {
    const parsed = JSON.parse(tableData);
    extra.value = JSON.stringify(parsed.rows);
    format.value = "xlsx";
    format.disabled = true;
  }

  if (grprapor) {
    const g = OBS.REPORTMAIL.byId("grprapor");
    const t = OBS.REPORTMAIL.byId("tablobaslik");
    if (g) g.value = grprapor;
    if (t) t.value = baslik || "";
    if (format) {
      format.value = "xlsx";
      format.disabled = true;
    }
  }

  if (!degerler || ["kercikis", "kergiris", ""].includes(degerler.value)) {
    if (format) {
      format.value = "xlsx";
      format.disabled = true;
    }
  }

  document.body.style.cursor = "default";
};

OBS.REPORTMAIL.getModel = function () {
  const v = (id) => (OBS.REPORTMAIL.byId(id)?.value || "").trim();
  return {
    hesap: v("hesap"),
    isim: v("isim"),
    too: v("too"),
    ccc: v("ccc"),
    konu: v("konu"),
    aciklama: v("aciklama"),
    nerden: v("nerden"),
    degerler: v("degerler"),
    baslik: v("tablobaslik"),
    format: v("format")
  };
};

OBS.REPORTMAIL.validate = function (m) {
  const err = OBS.REPORTMAIL.byId("errorDiv");
  const required = {
    hesap: "Hesap",
    isim: "İsim",
    too: "Alıcı (To)",
    konu: "Konu",
    aciklama: "Açıklama"
  };

  for (const k in required) {
    if (!m[k]) {
      if (err) {
        err.style.display = "block";
        err.style.color = "red";
        err.innerText = `${required[k]} alanı boş olamaz!`;
      }
      OBS.REPORTMAIL.byId(k)?.focus();
      return false;
    }
  }

  if (m.too && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(m.too)) {
    if (err) {
      err.style.display = "block";
      err.style.color = "red";
      err.innerText = "Alıcı (To) geçerli bir e-posta değil.";
    }
    OBS.REPORTMAIL.byId("too")?.focus();
    return false;
  }

  if (err) {
    err.style.display = "none";
    err.innerText = "";
  }
  return true;
};

OBS.REPORTMAIL.send = async function () {
  const model = OBS.REPORTMAIL.getModel();
  const err = OBS.REPORTMAIL.byId("errorDiv");
  const btn = OBS.REPORTMAIL.byId("mailButton");

  if (!OBS.REPORTMAIL.validate(model)) return;

  if (["kercikis", "kergiris"].includes(model.degerler)) {
    model.keresteyazdirDTO = JSON.parse(localStorage.getItem("keresteyazdirDTO") || "null");
  }

  const extraVal = OBS.REPORTMAIL.byId("extraValue")?.value;
  if (extraVal) model.exceList = JSON.parse(extraVal);

  const grpVal = OBS.REPORTMAIL.byId("grprapor")?.value;
  if (grpVal) model.tableString = grpVal;

  document.body.style.cursor = "wait";
  if (btn) { btn.disabled = true; btn.innerText = "Gönderiliyor..."; }

  try {
    const res = await fetchWithSessionCheck("send_email_gonder", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(model)
    });

    if (res?.errorMessage) throw new Error(res.errorMessage);

    if (err) {
      err.style.display = "block";
      err.style.color = "lime";
      err.innerText = res.success || "Mail gönderildi.";
    }

  } catch (e) {
    if (err) {
      err.style.display = "block";
      err.style.color = "red";
      err.innerText = e?.message || "Bir hata oluştu.";
    }
  } finally {
    ["tableData", "grprapor", "tablobaslik", "keresteyazdirDTO"]
      .forEach(k => localStorage.removeItem(k));

    document.body.style.cursor = "default";
    if (btn) { btn.disabled = false; btn.innerText = "Gönder"; }
  }
};

/* =========================
   GLOBAL KÖPRÜLER (HTML ve pageModules için)
   ========================= */

// HTML: onclick="sendmailAt()" aynen kalsın
window.sendmailAt = function () {
  return OBS.REPORTMAIL.send();
};

// pageModules init: DOM basıldıktan sonra çağır
window.sendEmailInit = function () {
  OBS.REPORTMAIL.init();
};
