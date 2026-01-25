/* =========================
   USER DETAILS (çakışmasız)
   Namespace: OBS.USERDETAIL
   ========================= */
window.OBS ||= {};
OBS.USERDETAIL ||= {};

/* ---------- helpers ---------- */
OBS.USERDETAIL._root = () =>
  document.getElementById("userdetail_content") || // varsa wrapper
  document.getElementById("ara_content") ||         // OBS içerik
  document;

OBS.USERDETAIL.byId = (id) => OBS.USERDETAIL._root().querySelector("#" + id);

OBS.USERDETAIL._setError = function (msg) {
  const e = OBS.USERDETAIL.byId("errorDiv");
  if (!e) return;
  if (msg) {
    e.style.display = "block";
    e.innerText = msg;
  } else {
    e.style.display = "none";
    e.innerText = "";
  }
};

OBS.USERDETAIL._tableBody = () => OBS.USERDETAIL.byId("tableBody") || OBS.USERDETAIL._root().querySelector("#moduleTable tbody");

OBS.USERDETAIL.clearTable = function () {
  const tb = OBS.USERDETAIL._tableBody();
  if (tb) tb.innerHTML = "";
};

OBS.USERDETAIL.clearForm = function (modulsuz = false) {
  const r = OBS.USERDETAIL._root();
  const setVal = (id, v) => { const el = r.querySelector("#" + id); if (el) el.value = v; };
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

  OBS.USERDETAIL._setError("");
};

OBS.USERDETAIL.sqlchanged = function () {
  const hangi_sql = OBS.USERDETAIL.byId("hangi_sql")?.value ?? "";
  const supDiv = OBS.USERDETAIL.byId("superviserdiv");
  if (!supDiv) return;
  supDiv.style.visibility = (hangi_sql === "PG SQL") ? "visible" : "hidden";
};

/* ---------- ui helpers ---------- */
OBS.USERDETAIL._getPwdValue = function () {
  const val = OBS.USERDETAIL.byId("user_pwd_server")?.value ?? "";
  return (val === "******") ? null : val;
};

OBS.USERDETAIL._fillFormFromItem = function (item) {
  const r = OBS.USERDETAIL._root();
  const setVal = (id, v) => { const el = r.querySelector("#" + id); if (el) el.value = v ?? ""; };
  const setChk = (id, v) => { const el = r.querySelector("#" + id); if (el) el.checked = !!v; };

  setVal("hiddenId", item?.id ?? "");
  setVal("user_prog_kodu", item?.user_prog_kodu ?? "");
  setVal("user_server", item?.user_server ?? "");
  setVal("user_pwd_server", item?.user_pwd_server ? "******" : ""); // güvenlik
  setVal("user_ip", item?.user_ip ?? "");
  if (item?.user_modul != null) setVal("user_modul", item.user_modul);
  if (item?.hangi_sql != null) setVal("hangi_sql", item.hangi_sql);

  setChk("izinlimi", !!item?.izinlimi);
  setChk("calisanmi", !!item?.calisanmi);
  setChk("log", !!item?.log);
  setVal("superviser", item?.superviser ?? "");

  OBS.USERDETAIL.sqlchanged();

  const dbButton = OBS.USERDETAIL.byId("savebutton");
  if (dbButton) dbButton.disabled = true;

  OBS.USERDETAIL._setError("");
};

/* ---------- main: detail oku ---------- */
OBS.USERDETAIL.detailoku = async function () {
  const modul = OBS.USERDETAIL.byId("user_modul")?.value ?? "";
  const tb = OBS.USERDETAIL._tableBody();
  OBS.USERDETAIL._setError("");
  OBS.USERDETAIL.clearTable();

  document.body.style.cursor = "wait";

  try {
    const response = await fetchWithSessionCheck("user/detailoku", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ modul }),
    });

    if (response?.errorMessage) throw new Error(response.errorMessage);

    if (response?.success) {
      OBS.USERDETAIL.clearForm(true);

      const arr = Array.isArray(response.data) ? response.data : [];
      arr.forEach((item) => {
        const tr = document.createElement("tr");
        tr.classList.add("table-row-height");

        // satırdan item id'si ve index'i okunabilir olsun (delegation için)
        tr.dataset.item = JSON.stringify(item);

        const durumPwd = item?.user_pwd_server ? "******" : "";

        tr.innerHTML = `
          <td style="display:none;">${item?.id ?? ""}</td>
          <td>${item?.user_prog_kodu ?? ""}</td>
          <td>${item?.user_server ?? ""}</td>
          <td>${durumPwd}</td>
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

      if (arr.length > 0) {
        OBS.USERDETAIL._fillFormFromItem(arr[0]);
      } else {
        OBS.USERDETAIL.clearForm(true);
      }

      // admin ise divleri aç
      if (response?.roleName === "ADMIN") {
        const logiznidiv = OBS.USERDETAIL.byId("logiznidiv");
        const izinlimidiv = OBS.USERDETAIL.byId("izinlimidiv");
        if (logiznidiv) logiznidiv.style.display = "block";
        if (izinlimidiv) izinlimidiv.style.display = "block";
      }

    } else {
      OBS.USERDETAIL._setError(response?.errorMessage || "Bir hata oluştu.");
    }

  } catch (e) {
    OBS.USERDETAIL._setError(e?.message || "Beklenmeyen hata");
  } finally {
    document.body.style.cursor = "default";
    const dbButton = OBS.USERDETAIL.byId("savebutton");
    if (dbButton) dbButton.disabled = true;
  }
};

/* ---------- row click (delegation) ---------- */
OBS.USERDETAIL._onRowClick = function (event) {
  const tr = event.target.closest("tr");
  const tb = OBS.USERDETAIL._tableBody();
  if (!tr || !tb || !tb.contains(tr)) return;

  try {
    const item = tr.dataset.item ? JSON.parse(tr.dataset.item) : null;
    if (item) OBS.USERDETAIL._fillFormFromItem(item);
  } catch {
    // fallback: eski hücre okuma (çok gerekmez ama dursun)
    const cells = tr.cells;
    if (!cells || cells.length < 11) return;

    const item = {
      id: cells[0].textContent.trim(),
      user_prog_kodu: cells[1].textContent.trim(),
      user_server: cells[2].textContent.trim(),
      user_pwd_server: cells[3].textContent.trim() ? true : false,
      user_ip: cells[4].textContent.trim(),
      user_modul: cells[5].textContent.trim(),
      hangi_sql: cells[6].textContent.trim(),
      izinlimi: cells[7].textContent.trim() === "Evet",
      calisanmi: cells[8].textContent.trim() === "Evet",
      log: cells[9].textContent.trim() === "Evet",
      superviser: cells[10].textContent.trim(),
    };
    OBS.USERDETAIL._fillFormFromItem(item);
  }
};

/* ---------- save ---------- */
OBS.USERDETAIL.saveUserDetails = async function () {
  OBS.USERDETAIL._setError("");

  try {
    const userDetails = {
      id: OBS.USERDETAIL.byId("hiddenId")?.value ?? "",
      user_prog_kodu: OBS.USERDETAIL.byId("user_prog_kodu")?.value ?? "",
      user_server: OBS.USERDETAIL.byId("user_server")?.value ?? "",
      user_pwd_server: OBS.USERDETAIL._getPwdValue(),
      user_ip: OBS.USERDETAIL.byId("user_ip")?.value ?? "",
      user_modul: OBS.USERDETAIL.byId("user_modul")?.value ?? "",
      hangi_sql: OBS.USERDETAIL.byId("hangi_sql")?.value ?? "",
      izinlimi: !!OBS.USERDETAIL.byId("izinlimi")?.checked,
      calisanmi: !!OBS.USERDETAIL.byId("calisanmi")?.checked,
      log: !!OBS.USERDETAIL.byId("log")?.checked,
      superviser: OBS.USERDETAIL.byId("superviser")?.value ?? "",
    };

    const response = await fetchWithSessionCheck("user/user_details_save", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(userDetails)
    });

    if (response?.errorMessage) throw new Error(response.errorMessage);

    OBS.USERDETAIL.detailoku();
  } catch (e) {
    OBS.USERDETAIL._setError(e?.message || "Bir hata oluştu.");
  }
};

/* ---------- delete ---------- */
OBS.USERDETAIL.confirmDeleteus = function () {
  if (confirm("Bu kaydı silmek istediğinizden emin misiniz?")) {
    OBS.USERDETAIL.deleteUserDetailsus();
  }
};

OBS.USERDETAIL.deleteUserDetailsus = async function () {
  const idd = OBS.USERDETAIL.byId("hiddenId")?.value ?? "";
  if (!idd || isNaN(idd) || Number(idd) <= 0) return;

  const id = Number(idd);
  const modul = OBS.USERDETAIL.byId("user_modul")?.value ?? "";

  document.body.style.cursor = "wait";
  try {
    const response = await fetchWithSessionCheck("user/delete_user_details", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id, modul }),
    });
    if (response?.errorMessage) throw new Error(response.errorMessage);

    OBS.USERDETAIL.detailoku();
  } catch (e) {
    OBS.USERDETAIL._setError(e?.message || "Beklenmeyen bir hata oluştu.");
  } finally {
    document.body.style.cursor = "default";
  }
};

/* ---------- checkFile ---------- */
OBS.USERDETAIL.checkFile = async function () {
  const serverBilgiDTO = {
    user_modul: OBS.USERDETAIL.byId("user_modul")?.value ?? "",
    hangi_sql: OBS.USERDETAIL.byId("hangi_sql")?.value ?? "",
    user_prog_kodu: OBS.USERDETAIL.byId("user_prog_kodu")?.value ?? "",
    user_server: OBS.USERDETAIL.byId("user_server")?.value ?? "",
    user_pwd_server: OBS.USERDETAIL._getPwdValue(),
    user_ip: OBS.USERDETAIL.byId("user_ip")?.value ?? "",
    superviser: OBS.USERDETAIL.byId("superviser")?.value ?? "",
    id: OBS.USERDETAIL.byId("hiddenId")?.value ?? "",
  };

  const dbButton = OBS.USERDETAIL.byId("savebutton");
  OBS.USERDETAIL._setError("");

  document.body.style.cursor = "wait";
  try {
    const response = await fetchWithSessionCheck("user/checkfile", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(serverBilgiDTO),
    });

    if (response?.dosyaDurum === "true") {
      if (dbButton) dbButton.disabled = false;
    } else {
      alert("Baglanti saglanamadi veya Veritabanı Mevcut Değil");
    }
  } catch (e) {
    OBS.USERDETAIL._setError(e?.message || "Bilinmeyen bir hata oluştu.");
  } finally {
    document.body.style.cursor = "default";
  }
};

/* ---------- menu link ---------- */
OBS.USERDETAIL.gotoCreateDb = function () {
  if (window.sayfaYukle) window.sayfaYukle("/user/createdb");
  // veya:
  // OBS.NAV.go("/user/createdb");
};

/* ---------- init / destroy ---------- */
OBS.USERDETAIL.init = function () {
  // menu click
  const menu = OBS.USERDETAIL.byId("createdbMenuLink");
  if (menu) {
    menu.removeEventListener("click", OBS.USERDETAIL.gotoCreateDb);
    menu.addEventListener("click", OBS.USERDETAIL.gotoCreateDb);
  }

  // table row click delegation
  document.removeEventListener("click", OBS.USERDETAIL._onRowClick, true);
  document.addEventListener("click", OBS.USERDETAIL._onRowClick, true);

  // ilk yüklemede
  // OBS.USERDETAIL.detailoku(); // istersen otomatik
};

OBS.USERDETAIL.destroy = function () {
  const menu = OBS.USERDETAIL.byId("createdbMenuLink");
  if (menu) menu.removeEventListener("click", OBS.USERDETAIL.gotoCreateDb);
  document.removeEventListener("click", OBS.USERDETAIL._onRowClick, true);
};
