/* =========================================================
   KERGRPRAPOR (jQuery YOK) - OBS Namespace
   window.OBS = window.OBS || {};
   OBS.KERGRPRAPOR = ...
   ========================================================= */

window.OBS = window.OBS || {};
OBS.KERGRPRAPOR = OBS.KERGRPRAPOR || {};

(() => {
    const M = OBS.KERGRPRAPOR;

    /* ---------------- state ---------------- */
    M.data = null;
    M.tablobaslik = "";   // ✅ STRING
    M.rowCounter = 0;

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
        e.innerText = msg || "Beklenmeyen bir hata oluştu.";
    };

    M.setBtn = (id, disabled, text) => {
        const b = M.el(id);
        if (!b) return;
        b.disabled = !!disabled;
        if (typeof text === "string") b.textContent = text;
    };

    /* ---------------- UI toggles ---------------- */
    M.dvzcevirChanged = () => {
        const checked = !!M.el("dvzcevirchc")?.checked;
        M.el("dvzcins")?.classList.toggle("is-hidden", !checked);
        M.el("dvzturu")?.classList.toggle("is-hidden", !checked);
        M.el("dvzcinsspan")?.classList.toggle("is-hidden", !checked);
        M.el("dvzturuspan")?.classList.toggle("is-hidden", !checked);
    };

    M.birimChanged = () => {
        const birim = M.el("birim")?.value || "";
        const ch = M.el("dvzcevirchc");
        const sp = M.el("dvzcvrspn");

        if (birim === "Tutar") {
            ch?.classList.toggle("is-hidden", false);
            sp?.classList.toggle("is-hidden", false);
        } else {
            if (ch) ch.checked = false;

            ch?.classList.toggle("is-hidden", true);
            sp?.classList.toggle("is-hidden", true);

            M.el("dvzcins")?.classList.toggle("is-hidden", true);
            M.el("dvzcinsspan")?.classList.toggle("is-hidden", true);

            M.el("dvzturuspan")?.classList.toggle("is-hidden", true);
            M.el("dvzturu")?.classList.toggle("is-hidden", true);
        }
    };

    /* ---------------- anagrp -> altgrp ---------------- */
    M.anagrpChanged = async (anagrpElement, altgrpElementId) => {
        const anagrup = anagrpElement.value;
        const selectElement = M.el(altgrpElementId);
        if (!selectElement) return;

        selectElement.innerHTML = "";
        if (anagrup === "") {
            selectElement.disabled = true;
            return;
        }

        M.setCursor(true);
        M.clearError();

        try {
            const response = await fetchWithSessionCheck("kereste/altgrup", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ anagrup })
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            (response?.altKodlari || []).forEach((kod) => {
                const option = document.createElement("option");
                option.value = kod.ALT_GRUP;
                option.textContent = kod.ALT_GRUP;
                selectElement.appendChild(option);
            });

            selectElement.disabled = selectElement.options.length === 0;
        } catch (err) {
            selectElement.disabled = true;
            M.showError(err?.message);
        } finally {
            M.setCursor(false);
        }
    };

    /* ---------------- modal open (Bootstrap 5 jQuery yok) ---------------- */
    M.openkgrpModal = async (modalSelectorOrEl) => {
        M.setCursor(true);
        M.clearError();

        try {
            const modalEl = (typeof modalSelectorOrEl === "string")
                ? document.querySelector(modalSelectorOrEl)
                : modalSelectorOrEl;

            if (modalEl && window.bootstrap?.Modal) {
                bootstrap.Modal.getOrCreateInstance(modalEl).show();
            }

            const response = await fetchWithSessionCheck("kereste/anadepo", {
                method: "POST",
                headers: { "Content-Type": "application/json" }
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            const ana = response.anaKodlari || [];
            const oz1 = response.oz1Kodlari || [];
            const dpo = response.depoKodlari || [];

            const dpoSelect = M.el("depo");
            const uranaSelect = M.el("anagrp");
            const ozSelect = M.el("ozkod");

            if (dpoSelect) dpoSelect.innerHTML = "";
            if (uranaSelect) uranaSelect.innerHTML = "";
            if (ozSelect) ozSelect.innerHTML = "";

            ana.forEach((item) => {
                if (!uranaSelect) return;
                const opt = document.createElement("option");
                opt.value = item.ANA_GRUP;
                opt.textContent = item.ANA_GRUP;
                uranaSelect.appendChild(opt);
            });

            oz1.forEach((item) => {
                if (!ozSelect) return;
                const opt = document.createElement("option");
                opt.value = item.OZEL_KOD_1;
                opt.textContent = item.OZEL_KOD_1;
                ozSelect.appendChild(opt);
            });

            dpo.forEach((item) => {
                if (!dpoSelect) return;
                const opt = document.createElement("option");
                opt.value = item.DEPO;
                opt.textContent = item.DEPO;
                dpoSelect.appendChild(opt);
            });

            const insertBos = (sel) => {
                if (!sel) return;
                const o = document.createElement("option");
                o.value = "Bos Olanlar";
                o.textContent = "Bos Olanlar";
                sel.insertBefore(o, sel.options[1] || null);
            };

            insertBos(dpoSelect);
            insertBos(uranaSelect);
            insertBos(ozSelect);

        } catch (err) {
            M.showError(`Bir hata oluştu: ${err?.message || String(err)}`);
        } finally {
            M.setCursor(false);
        }
    };

    /* ---------------- DTO (jQuery yok) ---------------- */
    M.getKerGrupRaporDTO = () => {
        const hidden = M.el("grpBilgi");
        const raw = hidden ? (hidden.value || "") : "";
        const p = raw.split(",");

        return {
            tar1: p[0],
            tar2: p[1],
            anagrp: p[2],
            ukod1: p[3],
            ukod2: p[4],
            altgrp: p[5],
            ckod1: p[6],
            ckod2: p[7],
            ozkod: p[8],
            kons1: p[9],
            kons2: p[10],
            depo: p[11],
            evr1: p[12],
            evr2: p[13],
            birim: p[14],
            gruplama: p[15],
            stunlar: p[16],
            turu: p[17],
            dvzcevirchc: p[18],
            doviz: p[19],
            dvzturu: p[20]
        };
    };

    /* ---------------- table helpers ---------------- */
    M.clearTfoot = () => {
        const table = document.querySelector("#main-table");
        if (!table) return;
        const tfoot = table.querySelector("tfoot");
        if (!tfoot) return;
        tfoot.querySelectorAll("th").forEach((th) => (th.textContent = ""));
    };

    M.updateTableHeaders = (baslikString, kolonbaslangic) => {
        const thead = document.querySelector("#main-table thead");
        if (!thead) return;

        thead.innerHTML = "";

        const tr = document.createElement("tr");
        tr.classList.add("thead-dark");

        const headers = (baslikString || "")
            .split(",")
            .map(h => h.trim().replace(/\[|\]/g, ""));

        headers.forEach((header, index) => {
            const th = document.createElement("th");
            th.textContent = header;
            if (index >= kolonbaslangic) th.classList.add("double-column");
            tr.appendChild(th);
        });

        thead.appendChild(tr);
    };

    M.updateTable = (data, headers, format, kolonbaslangic) => {
        const tbody = document.querySelector("#main-table tbody");
        if (!tbody) return;

        let tfoot = document.querySelector("#main-table tfoot");
        if (!tfoot) {
            tfoot = document.createElement("tfoot");
            document.querySelector("#main-table").appendChild(tfoot);
        }

        tbody.innerHTML = "";
        tfoot.innerHTML = "";

        const kolonToplamlari = new Array(headers.length).fill(0);

        data.forEach((rowData) => {
            const tr = document.createElement("tr");
            tr.classList.add("table-row-height");

            headers.forEach((header, index) => {
                const td = document.createElement("td");
                const cellValue = (rowData?.[header] != null) ? rowData[header] : "";

                if (index >= kolonbaslangic) {
                    const n = parseFloat(cellValue);
                    if (!Number.isNaN(n)) {
                        if (format == 2) td.textContent = formatNumber2(n);
                        else if (format == 3) td.textContent = formatNumber3(n);
                        else if (format == 0) td.textContent = formatNumber0(n);
                        else td.textContent = String(n);

                        td.classList.add("double-column");
                        kolonToplamlari[index] += n;
                    } else {
                        td.textContent = cellValue;
                    }
                } else {
                    td.textContent = cellValue;
                }

                tr.appendChild(td);
            });

            tbody.appendChild(tr);
        });

        const footerRow = document.createElement("tr");
        footerRow.classList.add("table-footer");

        headers.forEach((_, index) => {
            const th = document.createElement("th");

            if (index >= kolonbaslangic) {
                const sum = kolonToplamlari[index];
                if (format == 2) th.textContent = formatNumber2(sum);
                else if (format == 3) th.textContent = formatNumber3(sum);
                else if (format == 0) th.textContent = formatNumber0(sum);
                else th.textContent = String(sum);

                th.classList.add("double-column");
            } else {
                th.textContent = "";
            }

            footerRow.appendChild(th);
        });

        tfoot.appendChild(footerRow);
    };

    /* ---------------- fetch + render ---------------- */
    M.grpfetchTableData = async () => {
        M.clearError();
        M.setCursor(true);

        M.setBtn("grpyenileButton", true, "İşleniyor...");

        const mainTableBody = M.el("mainTableBody");
        if (mainTableBody) mainTableBody.innerHTML = "";

        M.clearTfoot();

        try {
            const dto = M.getKerGrupRaporDTO();

            const response = await fetchWithSessionCheck("kereste/grpdoldur", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dto)
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            M.data = response;

            M.updateTableHeaders(M.data.baslik, M.data.sabitkolonsayisi);

            const headersArr = (M.data.baslik || "")
                .split(",")
                .map(h => h.trim().replace(/\[|\]/g, ""));

            // ✅ STRING: "A,B,C"
            M.tablobaslik = headersArr.join(",");

            M.rowCounter = Number(M.data.sabitkolonsayisi || 0);

            M.updateTable(M.data.data || [], headersArr, M.data.format, M.rowCounter);

        } catch (err) {
            M.showError(err?.message || String(err));
        } finally {
            M.setBtn("grpyenileButton", false, "Yenile");
            M.setCursor(false);
        }
    };

    /* ---------------- export helpers ---------------- */
    M.extractTableData = (headersString) => {
        const headers = (headersString || "").split(",").map(s => s.trim()).filter(Boolean);

        let rowsString = "";
        const tbody = document.querySelector("#main-table tbody");
        if (!tbody) return rowsString;

        const tbodyRows = tbody.querySelectorAll("tr");
        tbodyRows.forEach((tr) => {
            let rowString = "";
            const tds = Array.from(tr.querySelectorAll("td"));

            headers.forEach((_, index) => {
                const td = tds[index];
                const cellValue = td ? td.innerText.trim() : "";
                rowString += cellValue + "||";
            });

            rowsString += rowString.slice(0, -2) + "\n";
        });

        const footer = document.querySelector("#main-table tfoot");
        if (footer) {
            let footerString = "";
            const ths = Array.from(footer.querySelectorAll("th"));

            headers.forEach((_, index) => {
                const th = ths[index];
                const footerValue = th ? th.innerText.trim() : "";
                footerString += footerValue + "||";
            });

            rowsString += footerString.slice(0, -2) + "\n";
        }

        return rowsString;
    };

    /* ---------------- download (jQuery yok) ---------------- */
    M.grpdownloadReport = async () => {
        M.clearError();
        M.setCursor(true);

        M.setBtn("grpDownloadButton", true, "İşleniyor...");
        M.setBtn("grpyenileButton", true);

        try {
            const tableString = M.extractTableData(M.tablobaslik);

            const response = await fetchWithSessionCheckForDownload("kereste/grp_download", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    headers: M.tablobaslik,        // ✅ STRING
                    data: tableString,
                    sabitkolon: M.rowCounter
                })
            });

            if (response?.blob) {
                const disposition = response.headers.get("Content-Disposition") || "";
                const m = disposition.match(/filename="(.+)"/);
                const fileName = m ? m[1] : "kergruprapor.xlsx";

                const url = window.URL.createObjectURL(response.blob);
                const a = document.createElement("a");
                a.href = url;
                a.download = fileName;
                document.body.appendChild(a);
                a.click();
                a.remove();
                window.URL.revokeObjectURL(url);
            } else {
                throw new Error("Dosya indirilemedi.");
            }
        } catch (err) {
            M.showError(err?.message || "Bilinmeyen bir hata oluştu.");
        } finally {
            M.setBtn("grpDownloadButton", false, "Rapor İndir");
            M.setBtn("grpyenileButton", false);
            M.setCursor(false);
        }
    };

    /* ---------------- mail (jQuery yok) ---------------- */
    M.grpmailAt = () => {
        localStorage.removeItem("tableData");
        localStorage.removeItem("grprapor");
        localStorage.removeItem("tablobaslik");

        M.setCursor(true);

        try {
            const rows = M.extractTableData(M.tablobaslik);

            localStorage.setItem("grprapor", rows);
            localStorage.setItem("tablobaslik", M.tablobaslik); // ✅ STRING

            const degerler = M.rowCounter + "," + "kergruprapor";
            const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
            mailsayfasiYukle(url);
        } finally {
            M.setCursor(false);
        }
    };

    /* ---------------- expose (HTML onclick için) ---------------- */
    window.dvzcevirChanged = M.dvzcevirChanged;
    window.birimChanged = M.birimChanged;

    window.anagrpChanged = M.anagrpChanged;
    window.openenvModal = M.openenvModal;

    window.grpfetchTableData = M.grpfetchTableData;
    window.grpdownloadReport = M.grpdownloadReport;
    window.grpmailAt = M.grpmailAt;

})();
