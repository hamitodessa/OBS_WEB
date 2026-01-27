/* =========================================================
   KONSACIKLAMA (jQuery TEMİZ) - OBS Namespace
   window.OBS = window.OBS || {};
   OBS.KONSACIKLAMA = ...
   ========================================================= */

window.OBS = window.OBS || {};
OBS.KONSACIKLAMA = OBS.KONSACIKLAMA || {};

(() => {
  const M = OBS.KONSACIKLAMA;

  /* ---------------- helpers ---------------- */
  M.el = (id) => document.getElementById(id);

  M.setCursor = (wait) => { document.body.style.cursor = wait ? "wait" : "default"; };

  M.clearError = () => {
    const e = M.el("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
  };

  M.showError = (msg) => {
    const e = M.el("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Bir hata oluştu.";
  };

  M.clearForm = () => {
    const kons = M.el("kons");
    const acik = M.el("aciklama");
    if (kons) kons.value = "";
    if (acik) acik.value = "";
  };

  M.clearTable = () => {
    const tb = M.el("tableBody");
    if (tb) tb.innerHTML = "";
  };

  /* ---------------- row click -> form ---------------- */
  M.setFormValues = (row) => {
    const cells = row?.cells;
    if (!cells) return;
    M.el("kons").value = cells[0]?.textContent?.trim() || "";
    M.el("aciklama").value = cells[1]?.textContent?.trim() || "";
  };

  /* ---------------- load list ---------------- */
  M.konsacikyukle = async () => {
    M.clearError();
    M.clearTable();
    M.setCursor(true);

    try {
      const data = await fetchWithSessionCheck("kereste/konsaciklamadoldur", { method: "GET" });

      if (data?.errorMessage) {
        throw new Error(data.errorMessage);
      }

      const list = data?.data || [];
      const tableBody = M.el("tableBody");

      list.forEach((item) => {
        const row = document.createElement("tr");
        row.classList.add("table-row-height");
        row.innerHTML = `
          <td>${item.KONS || ""}</td>
          <td>${item.ACIKLAMA || ""}</td>
        `;
        row.addEventListener("click", () => M.setFormValues(row));
        tableBody.appendChild(row);
      });

      M.clearForm();
    } catch (err) {
      M.showError(err?.message || String(err));
    } finally {
      M.setCursor(false);
    }
  };

  /* ---------------- save ---------------- */
  M.saveKons = async () => {
    M.clearError();
    M.clearTable();
    M.setCursor(true);

    try {
      const kons = (M.el("kons")?.value || "").trim();
      const aciklama = (M.el("aciklama")?.value || "").trim();

      if (!kons) { alert("Konsimento alanı boş bırakılamaz."); return; }
      if (!aciklama) { alert("Açıklama alanı boş bırakılamaz."); return; }

      const resp = await fetchWithSessionCheck("kereste/konskaydet", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ kons, aciklama })
      });

      if (resp?.errorMessage) throw new Error(resp.errorMessage);

      await M.konsacikyukle();
    } catch (err) {
      M.showError(err?.message || "Bir hata oluştu.");
    } finally {
      M.setCursor(false);
    }
  };

  /* ---------------- delete ---------------- */
  M.deleteKons = async () => {
    const ok = confirm("Bu Konsimento silinecek ?");
    if (!ok) return;

    M.clearError();
    M.clearTable();
    M.setCursor(true);

    try {
      const kons = (M.el("kons")?.value || "").trim();
      if (!kons) { alert("Konsimento alani boş birakilamaz."); return; }

      const resp = await fetchWithSessionCheck("kereste/konsdelete", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ kons })
      });

      if (resp?.errorMessage) throw new Error(resp.errorMessage);

      await M.konsacikyukle();
    } catch (err) {
      M.showError(err?.message || "Bir hata oluştu.");
    } finally {
      M.setCursor(false);
    }
  };

  /* ---------------- expose (HTML onclick çağrıları için) ---------------- */
  window.konsacikyukle = M.konsacikyukle;
  window.saveKons = M.saveKons;
  window.deleteKons = M.deleteKons;
  window.setFormValues = M.setFormValues;

})();
