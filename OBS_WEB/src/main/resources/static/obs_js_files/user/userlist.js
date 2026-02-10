window.OBS = window.OBS || {};
OBS.USERS_LIST = OBS.USERS_LIST || {};

OBS.setCursorWait = function () {
  requestAnimationFrame(() => { document.body.style.cursor = "wait"; });
};
OBS.setCursorDefault = function () {
  requestAnimationFrame(() => { document.body.style.cursor = "default"; });
};

(function (M) {
  M.byId = (id) => document.getElementById(id);

  M._showError = function (msg) {
    const e = M.byId("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.textContent = msg || "Beklenmeyen hata.";
  };

  M._clearError = function () {
    const e = M.byId("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.textContent = "";
  };

  M._escape = function (s) {
    return (s ?? "").toString()
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  };

  /* API */
  M.API_LIST = "/user/list";

  /* render */
  M._renderRows = function (rows) {
    const tb = M.byId("users_tbody");
    const cnt = M.byId("users_count");
    if (!tb) return;

    const list = Array.isArray(rows) ? rows : [];
    if (cnt) cnt.textContent = String(list.length);

    if (list.length === 0) {
      tb.innerHTML = `<tr><td colspan="4" class="cam-td-muted">Kayıt bulunamadı.</td></tr>`;
      return;
    }

    tb.innerHTML = list.map(u => {
      const id = M._escape(u.id);
      const name = M._escape(((u.firstName || "") + " " + (u.lastName || "")).trim());
      const email = M._escape(u.email || "");

      return `
        <tr data-user-id="${id}">
          <td class="col-del" style="text-align:center">
            <button class="cam-iconbtn js-user-del" type="button" title="Sil" data-id="${id}">🗑️</button>
          </td>
          <td class="col-id">${id}</td>
          <td>${name}</td>
          <td>${email}</td>
        </tr>
      `;
    }).join("");
  };

  /* fetch */
  M._fetchJson = async function (url) {
    const r = await fetchWithSessionCheck(url, { headers: { "Accept": "application/json" } });
   
    return r;
  };

  M.load = async function () {
    M._clearError();
    OBS.setCursorWait();

    const tb = M.byId("users_tbody");
    if (tb) tb.innerHTML = `<tr><td colspan="4" class="cam-td-muted">Yükleniyor…</td></tr>`;

    try {
      const data = await M._fetchJson(M.API_LIST);
      M._allRows = Array.isArray(data) ? data : (data.data || []);
      M._renderRows(M._allRows);
    } catch (e) {
      M._renderRows([]);
      M._showError(e?.message);
    } finally {
      OBS.setCursorDefault();
    }
  };

  /* ✅ DOĞRU: fonksiyon tanımı */
  M.deleteUser = async function (id) {
    const message = "Kayıt silinecek. Emin misin?";
    const confirmDelete = confirm(message);
    if (!confirmDelete) return;

    OBS.setCursorWait();

    const errorDiv = document.getElementById("errorDiv"); // sende bu var
    if (errorDiv) {
      errorDiv.style.display = "none";
      errorDiv.innerText = "";
    }

    try {
      const response = await fetchWithSessionCheck("/user/userSil", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ id: id }),
      });

      if (response && response.errorMessage && response.errorMessage.trim() !== "") {
        if (errorDiv) {
          errorDiv.style.display = "block";
          errorDiv.innerText = response.errorMessage || "Bir hata oluştu.";
        }
        return;
      }
      await M.load();


    } catch (error) {
      if (errorDiv) {
        errorDiv.style.display = "block";
        errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
      }
    } finally {
      OBS.setCursorDefault();
    }
  };

  /* DELETE click */
  M._bindOnce = function () {
    if (M._bound) return;
    M._bound = true;

    const table = M.byId("users_table");
    if (!table) return;

    table.addEventListener("click", (ev) => {
      const btn = ev.target.closest(".js-user-del");
      if (!btn) return;

      const id = btn.getAttribute("data-id");
      if (!id) return;

      M.deleteUser(id); // ✅ modül içinden çağır
    });
  };

  M.init = function () {
    M._bindOnce();
    M.load();
  };

})(OBS.USERS_LIST);
