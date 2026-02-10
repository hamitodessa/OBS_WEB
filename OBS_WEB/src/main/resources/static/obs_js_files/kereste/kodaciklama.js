/* =========================================================
   KODACIKLAMA (jQuery TEMİZ) - OBS Namespace
   window.OBS = window.OBS || {};
   OBS.KODACIKLAMA = ...
   ========================================================= */

window.OBS = window.OBS || {};
OBS.KODACIKLAMA = OBS.KODACIKLAMA || {};

(() => {
    const M = OBS.KODACIKLAMA;

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
        const kod = M.el("kod");
        const acik = M.el("aciklama");
        if (kod) kod.value = "";
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
        const kod = cells[0]?.textContent?.trim() || "";
        const acik = cells[1]?.textContent?.trim() || "";
        M.el("kod").value = kod;
        M.el("aciklama").value = acik;
    };

    /* ---------------- load list ---------------- */
    M.kodacikyukle = async () => {
        M.clearError();
        M.clearTable();
        M.setCursor(true);

        try {
            const data = await fetchWithSessionCheck("kereste/kodaciklamadoldur", { method: "GET" });

            if (data?.errorMessage) {
                throw new Error(data.errorMessage);
            }

            const list = data?.data || [];
            const tableBody = M.el("tableBody");

            list.forEach((item) => {
                const row = document.createElement("tr");
                row.classList.add("table-row-height");
                row.innerHTML = `
          <td>${item.KOD || ""}</td>
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
    M.saveKod = async () => {
        M.clearError();
        M.clearTable();
        M.setCursor(true);

        try {
            const kod = (M.el("kod")?.value || "").trim();
            const aciklama = (M.el("aciklama")?.value || "").trim();

            if (!kod) { alert("Kod alanı boş bırakılamaz."); return; }
            if (!aciklama) { alert("Açıklama alanı boş bırakılamaz."); return; }

            const resp = await fetchWithSessionCheck("kereste/kodkaydet", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ kod, aciklama })
            });

            if (resp?.errorMessage) throw new Error(resp.errorMessage);

            await M.kodacikyukle();
        } catch (err) {
            M.showError(err?.message || "Bir hata oluştu.");
        } finally {
            M.setCursor(false);
        }
    };

    /* ---------------- delete ---------------- */
    M.deleteKod = async () => {
        const ok = confirm("Bu Kod silinecek ?");
        if (!ok) return;

        M.clearError();
        M.clearTable();
        M.setCursor(true);

        try {
            const kod = (M.el("kod")?.value || "").trim();
            if (!kod) { alert("Kod alani boş birakilamaz."); return; }

            const resp = await fetchWithSessionCheck("kereste/koddelete", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ kod })
            });

            if (resp?.errorMessage) throw new Error(resp.errorMessage);

            await M.kodacikyukle();
        } catch (err) {
            M.showError(err?.message || "Bir hata oluştu.");
        } finally {
            M.setCursor(false);
        }
    };

    /* ---------------- expose (HTML onclick çağrıları için) ---------------- */
    window.kodacikyukle = M.kodacikyukle;
    window.saveKod = M.saveKod;
    window.deleteKod = M.deleteKod;
    window.setFormValues = M.setFormValues;

})();
