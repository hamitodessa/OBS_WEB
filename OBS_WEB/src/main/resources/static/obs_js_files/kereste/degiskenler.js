/* =========================================================
   KERDEGISKENLER (jQuery TEMİZ) - OBS Namespace + init
   window.OBS = window.OBS || {};
   OBS.KERDEGISKENLER = ...
   ========================================================= */

window.OBS = window.OBS || {};
OBS.KERDEGISKENLER = OBS.KERDEGISKENLER || {};

(() => {
    const M = OBS.KERDEGISKENLER;

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

    M.clearSearchAndForm = () => {
        const acik = M.el("aciklama");
        const arama = M.el("arama");
        const idacik = M.el("idacik");
        if (acik) acik.value = "";
        if (arama) arama.value = "";
        if (idacik) idacik.value = "";
    };

    M.selectValue = (selectedaciklama, selectedid) => {
        const inputElement = M.el("aciklama");
        const idacik = M.el("idacik");
        if (inputElement) inputElement.value = selectedaciklama || "";
        if (idacik) idacik.value = selectedid || "";
    };

    M._fillTableTwoCols = (rows, valueKey, idKey, onClickValueKey, onClickIdKey) => {
        const tableBody = M.el("degiskenTableBody");
        if (!tableBody) return;

        tableBody.innerHTML = "";
        tableBody.classList.add("table-row-height");

        rows.forEach((row) => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
        <td style="display:none;">${row[idKey] ?? ""}</td>
        <td>${row[valueKey] ?? ""}</td>
      `;
            tr.onclick = () => M.selectValue(row[onClickValueKey] ?? "", row[onClickIdKey] ?? "");
            tableBody.appendChild(tr);
        });

        const firstRow = tableBody.rows[0];
        if (firstRow) {
            const cells = firstRow.cells;
            M.selectValue(cells[1]?.textContent?.trim() || "", cells[0]?.textContent?.trim() || "");
        }
    };

    /* ---------------- main change ---------------- */
    M.degiskenchange = async (grpElement) => {
        M.clearSearchAndForm();

        const grup = grpElement?.value || "";
        const altgrpdiv = M.el("altgrpdiv");

        if (altgrpdiv) {
            if (grup === "altgrp") {
                altgrpdiv.style.display = "grid";
                await M.altgrpdoldur();
            } else {
                altgrpdiv.style.display = "none";
            }
        }

        if (grup === "anagrp") await M.anagrpdoldur();
        else if (grup === "mensei") await M.menseidoldur();
        else if (grup === "depo") await M.depodoldur();
        else if (grup === "oz1") await M.oz1doldur();
        else if (grup === "nak") await M.nakdoldur();
    };

    /* ---------------- loaders ---------------- */
    M.anagrpdoldur = async () => {
        M.clearError();
        M.setCursor(true);
        try {
            const response = await fetchWithSessionCheck("kereste/anagrpOku");
            if (response?.errorMessage) throw new Error(response.errorMessage);

            const data = response?.anagrp || [];
            M._fillTableTwoCols(data, "ANA_GRUP", "KOD", "ANA_GRUP", "KOD");
        } catch (err) {
            M.showError(`Bir hata oluştu: ${err?.message || err}`);
        } finally {
            M.setCursor(false);
        }
    };

    M.menseidoldur = async () => {
        M.clearError();
        M.setCursor(true);
        try {
            const response = await fetchWithSessionCheck("kereste/menseiOku");
            if (response?.errorMessage) throw new Error(response.errorMessage);

            const data = response?.mensei || [];
            if (data.length === 0) return;

            M._fillTableTwoCols(data, "MENSEI", "KOD", "MENSEI", "KOD");
        } catch (err) {
            M.showError(`Bir hata oluştu: ${err?.message || err}`);
        } finally {
            M.setCursor(false);
        }
    };

    M.depodoldur = async () => {
        M.clearError();
        M.setCursor(true);
        try {
            const response = await fetchWithSessionCheck("kereste/depoOku");
            if (response?.errorMessage) throw new Error(response.errorMessage);

            const data = response?.depo || [];
            if (data.length === 0) return;

            M._fillTableTwoCols(data, "DEPO", "KOD", "DEPO", "KOD");
        } catch (err) {
            M.showError(`Bir hata oluştu: ${err?.message || err}`);
        } finally {
            M.setCursor(false);
        }
    };

    M.oz1doldur = async () => {
        M.clearError();
        M.setCursor(true);
        try {
            const response = await fetchWithSessionCheck("kereste/oz1Oku");
            if (response?.errorMessage) throw new Error(response.errorMessage);

            const data = response?.oz1 || [];
            if (data.length === 0) return;

            M._fillTableTwoCols(data, "OZEL_KOD_1", "KOD", "OZEL_KOD_1", "KOD");
        } catch (err) {
            M.showError(`Bir hata oluştu: ${err?.message || err}`);
        } finally {
            M.setCursor(false);
        }
    };

    M.nakdoldur = async () => {
        M.clearError();
        M.setCursor(true);
        try {
            const response = await fetchWithSessionCheck("kereste/nakOku");
            if (response?.errorMessage) throw new Error(response.errorMessage);

            const data = response?.nak || [];
            if (data.length === 0) return;

            M._fillTableTwoCols(data, "UNVAN", "KOD", "UNVAN", "KOD");
        } catch (err) {
            M.showError(`Bir hata oluştu: ${err?.message || err}`);
        } finally {
            M.setCursor(false);
        }
    };

    M.altgrpdoldur = async () => {
        const arama = M.el("arama");
        if (arama) arama.value = "";

        const anagrup = (M.el("altgrpAna")?.value || "").trim();

        M.clearError();
        M.setCursor(true);

        try {
            const response = await fetchWithSessionCheck("kereste/altgrupdeg", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ anagrup }),
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            const data = response?.altKodlari || [];
            const temiz = data.filter(r => (r?.ALT_GRUP || "") !== "");

            // burada ID alanın ALID_Y, görünen ALT_GRUP
            const tableBody = M.el("degiskenTableBody");
            if (!tableBody) return;

            tableBody.innerHTML = "";
            tableBody.classList.add("table-row-height");

            temiz.forEach((row) => {
                const tr = document.createElement("tr");
                tr.innerHTML = `
          <td style="display:none;">${row.ALID_Y ?? ""}</td>
          <td>${row.ALT_GRUP ?? ""}</td>
        `;
                tr.onclick = () => M.selectValue(row.ALT_GRUP ?? "", row.ALID_Y ?? "");
                tableBody.appendChild(tr);
            });

            const firstRow = tableBody.rows[0];
            if (firstRow) {
                const cells = firstRow.cells;
                M.selectValue(cells[1]?.textContent?.trim() || "", cells[0]?.textContent?.trim() || "");
            }
        } catch (err) {
            M.showError(`Bir hata oluştu: ${err?.message || err}`);
        } finally {
            M.setCursor(false);
        }
    };

    /* ---------------- filter ---------------- */
    M.filterTable = () => {
        const searchValue = (M.el("arama")?.value || "").toLowerCase();
        const rows = document.querySelectorAll("#degiskenTable tbody tr");
        rows.forEach((row) => {
            const rowText = Array.from(row.cells).map(c => (c.textContent || "").toLowerCase()).join(" ");
            row.style.display = rowText.includes(searchValue) ? "" : "none";
        });
    };

    /* ---------------- buttons ---------------- */
    M.degyeni = () => {
        const acik = M.el("aciklama");
        const idacik = M.el("idacik");
        if (acik) acik.value = "";
        if (idacik) idacik.value = "";
    };

    M.degKayit = async () => {
        const aciklama = (M.el("aciklama")?.value || "").trim();
        const idacik = (M.el("idacik")?.value || "").trim();
        const degisken = (M.el("degiskenler")?.value || "").trim();
        const altgrpAna = (M.el("altgrpAna")?.value || "").trim();

        if (!aciklama) { alert("Lütfen  açıklama alanlarını doldurun."); return; }

        M.clearError();

        const saveButton = M.el("degkaydetButton");
        if (saveButton) { saveButton.textContent = "İşlem yapılıyor..."; saveButton.disabled = true; }
        M.setCursor(true);

        try {
            const response = await fetchWithSessionCheck("kereste/degkayit", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ aciklama, idacik: idacik || "", degisken, altgrpAna }),
            });

            if (!response) return;
            if (response?.errorMessage) throw new Error(response.errorMessage);

            const inputElement = M.el("degiskenler");
            await M.degiskenchange(inputElement);
        } catch (err) {
            M.showError(err?.message || "Bir hata oluştu. Daha sonra tekrar deneyin.");
        } finally {
            M.setCursor(false);
            if (saveButton) { saveButton.textContent = "Kaydet"; saveButton.disabled = false; }
        }
    };

    M.degYoket = async () => {
        const ok = confirm(
            "Alt Grup Degisken Silinecek ..?\n" +
            "Silme operasyonu butun dosyayi etkileyecek...\n" +
            "Ilk once Degisken Yenileme Bolumunden degistirip sonra siliniz...."
        );
        if (!ok) return;

        const aciklama = (M.el("aciklama")?.value || "").trim();
        const idacik = (M.el("idacik")?.value || "").trim();
        const degisken = (M.el("degiskenler")?.value || "").trim();
        const altgrpAna = (M.el("altgrpAna")?.value || "").trim();

        if (!idacik) { alert("Lütfen  açıklama alanlarını doldurun."); return; }

        M.clearError();

        const delButton = M.el("degsilButton");
        if (delButton) { delButton.textContent = "İşlem yapılıyor..."; delButton.disabled = true; }
        M.setCursor(true);

        try {
            const response = await fetchWithSessionCheck("kereste/degsil", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ aciklama, idacik, degisken, altgrpAna }),
            });

            if (!response) return;
            if (response?.errorMessage) throw new Error(response.errorMessage);

            const inputElement = M.el("degiskenler");
            await M.degiskenchange(inputElement);
        } catch (err) {
            M.showError(err?.message || "Bir hata oluştu. Daha sonra tekrar deneyin.");
        } finally {
            M.setCursor(false);
            if (delButton) { delButton.textContent = "Sil"; delButton.disabled = false; }
        }
    };

    /* ---------------- init ---------------- */
    M.init = async () => {
        // ilk açılış
        const arama = M.el("arama");
        if (arama) arama.value = "";

        await M.anagrpdoldur();
    };

    /* ---------------- expose (HTML onclick vs.) ---------------- */
    window.degiskenchange = M.degiskenchange;
    window.anagrpdoldur = M.anagrpdoldur;
    window.menseidoldur = M.menseidoldur;
    window.depodoldur = M.depodoldur;
    window.oz1doldur = M.oz1doldur;
    window.nakdoldur = M.nakdoldur;
    window.altgrpdoldur = M.altgrpdoldur;

    window.filterTable = M.filterTable;
    window.selectValue = M.selectValue;

    window.degyeni = M.degyeni;
    window.degKayit = M.degKayit;
    window.degYoket = M.degYoket;

    window.kerderegiskenlerInit = M.init;

})();
