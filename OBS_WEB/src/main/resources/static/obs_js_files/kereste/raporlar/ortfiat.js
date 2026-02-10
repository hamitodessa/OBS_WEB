/* =========================================================
   KERORTFIAT (jQuery TEMİZ) - OBS Namespace
   window.OBS = window.OBS || {};
   OBS.KERORTFIAT = ...
   ========================================================= */

window.OBS = window.OBS || {};
OBS.KERORTFIAT = OBS.KERORTFIAT || {};

(() => {
    const M = OBS.KERORTFIAT;

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
    M.openkortModal = async (modalSelectorOrEl) => {
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

            const uranaSelect = M.el("anagrp");
            const ozSelect = M.el("ozkod");
            if (uranaSelect) uranaSelect.innerHTML = "";
            if (ozSelect) ozSelect.innerHTML = "";

            ana.forEach((item) => {
                if (!uranaSelect) return;
                const o = document.createElement("option");
                o.value = item.ANA_GRUP;
                o.textContent = item.ANA_GRUP;
                uranaSelect.appendChild(o);
            });

            oz1.forEach((item) => {
                if (!ozSelect) return;
                const o = document.createElement("option");
                o.value = item.OZEL_KOD_1;
                o.textContent = item.OZEL_KOD_1;
                ozSelect.appendChild(o);
            });

            const insertBos = (sel) => {
                if (!sel) return;
                const o = document.createElement("option");
                o.value = "Bos Olanlar";
                o.textContent = "Bos Olanlar";
                sel.insertBefore(o, sel.options[1] || null);
            };

            insertBos(uranaSelect);
            insertBos(ozSelect);

        } catch (err) {
            M.showError(`Bir hata oluştu: ${err?.message || String(err)}`);
        } finally {
            M.setCursor(false);
        }
    };

    /* ---------------- dvz toggle ---------------- */
    M.dvzcevirChanged = () => {
        const checked = !!M.el("dvzcevirchc")?.checked;
        M.el("dvzcinsWrap")?.classList.toggle("is-hidden", !checked);
        M.el("dvzturuWrap")?.classList.toggle("is-hidden", !checked);
    };

    /* ---------------- DTO (jQuery yok) ---------------- */
    M.getkergrupraporDTO = () => {
        const hidden = M.el("ortfiatBilgi");
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
            gruplama: p[11],
            turu: p[12],
            dvzcevirchc: p[13],
            doviz: p[14],
            dvzturu: p[15]
        };
    };

    /* ---------------- headers helpers ---------------- */
    M.updateTableHeaders1 = (headers) => {
        const thead = document.querySelector("#main-table thead");
        const table = document.querySelector("#main-table");
        if (!thead || !table) return;

        let tfoot = table.querySelector("tfoot");
        if (!tfoot) {
            tfoot = document.createElement("tfoot");
            table.appendChild(tfoot);
        }

        thead.innerHTML = "";
        const trHead = document.createElement("tr");
        trHead.classList.add("thead-dark");

        headers.forEach((header, index) => {
            const th = document.createElement("th");
            th.textContent = header;
            if (index >= headers.length - 6) th.classList.add("double-column");
            trHead.appendChild(th);
        });

        thead.appendChild(trHead);
    };

    M.updateTableHeaderssinif = (headers) => {
        const thead = document.querySelector("#main-table thead");
        const table = document.querySelector("#main-table");
        if (!thead || !table) return;

        let tfoot = table.querySelector("tfoot");
        if (!tfoot) {
            tfoot = document.createElement("tfoot");
            table.appendChild(tfoot);
        }

        thead.innerHTML = "";
        const trHead = document.createElement("tr");
        trHead.classList.add("thead-dark");

        headers.forEach((header, index) => {
            const th = document.createElement("th");
            th.textContent = header;
            if (index >= headers.length - 6) th.classList.add("double-column");
            trHead.appendChild(th);
        });

        thead.appendChild(trHead);
    };

    M.updateTableHeaders3 = (headers) => {
        const thead = document.querySelector("#main-table thead");
        const table = document.querySelector("#main-table");
        if (!thead || !table) return;

        let tfoot = table.querySelector("tfoot");
        if (!tfoot) {
            tfoot = document.createElement("tfoot");
            table.appendChild(tfoot);
        }

        thead.innerHTML = "";
        const trHead = document.createElement("tr");
        trHead.classList.add("thead-dark");

        headers.forEach((header, index) => {
            const th = document.createElement("th");
            th.textContent = header;
            if (index >= headers.length - 6) th.classList.add("double-column");
            trHead.appendChild(th);
        });

        thead.appendChild(trHead);
    };

    /* ---------------- main fetch/render ---------------- */
    M.kerortfiatdoldur = async () => {
        const dto = M.getkergrupraporDTO();

        M.clearError();
        M.setCursor(true);
        M.setBtn("ortfiatyenileButton", true, "İşleniyor...");

        const mainTableBody = M.el("mainTableBody");
        if (mainTableBody) mainTableBody.innerHTML = "";

        try {
            const response = await fetchWithSessionCheck("kereste/ortfiatdoldur", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dto)
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            const data = response;
            const raporturu = data.raporturu;
            const dvz = data.dvz;

            let sqlHeaders = [];

            if (raporturu === "Sinif") {
                sqlHeaders = ["SINIF", "ACIKLAMA", "TUTAR", dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + dvz];
                M.updateTableHeaderssinif(sqlHeaders);
            } else if (raporturu === "Sinif-Kal") {
                sqlHeaders = ["SINIF", "KAL", "TUTAR", dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + dvz];
                M.updateTableHeaderssinif(sqlHeaders);
            } else if (raporturu === "Sinif-Boy") {
                sqlHeaders = ["SINIF", "BOY", "TUTAR", dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + dvz];
                M.updateTableHeaderssinif(sqlHeaders);
            } else if (raporturu === "Sinif-Gen") {
                sqlHeaders = ["SINIF", "GEN", "TUTAR", dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + dvz];
                M.updateTableHeaderssinif(sqlHeaders);
            } else if (raporturu === "Kodu") {
                sqlHeaders = ["KODU", "ACIKLAMA", "TUTAR", dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + dvz];
                M.updateTableHeaderssinif(sqlHeaders);
            } else if (raporturu === "Konsimento") {
                sqlHeaders = ["KONSIMENTO", "ACIKLAMA", "TUTAR", dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + dvz];
                M.updateTableHeaderssinif(sqlHeaders);
            } else if (raporturu === "Hesap Kodu") {
                sqlHeaders = ["CARI FIRMA", "UNVAN", "TUTAR", dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + dvz];
                M.updateTableHeaderssinif(sqlHeaders);
            } else if (raporturu === "Hesap Kodu-Ana_Alt_Grup") {
                sqlHeaders = ["CARI FIRMA", "ANA GRUP", "ALT GRUP", "TUTAR", dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + dvz];
                M.updateTableHeaders3(sqlHeaders);
            } else if (raporturu === "Yil") {
                sqlHeaders = ["YIL", "TUTAR", dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + dvz];
                M.updateTableHeaders1(sqlHeaders);
            } else if (raporturu === "Yil_Ay") {
                sqlHeaders = ["YIL", "AY", "TUTAR", dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + dvz];
                M.updateTableHeaderssinif(sqlHeaders);
            } else if (raporturu === "Ana Grup") {
                sqlHeaders = ["ANA GRUP", "ALT GRUP", "TUTAR", dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + dvz];
                M.updateTableHeaderssinif(sqlHeaders);
            }

            (data.data || []).forEach((rowData) => {
                const row = document.createElement("tr");
                row.classList.add("expandable", "table-row-height");

                let extraColumns = "";

                if (raporturu === "Sinif") {
                    extraColumns = `<td>${rowData.Sinif || ""}</td><td>${rowData.Adi || ""}</td>`;
                } else if (raporturu === "Sinif-Kal") {
                    extraColumns = `<td>${rowData.Sinif || ""}</td><td>${rowData.Kal || ""}</td>`;
                } else if (raporturu === "Sinif-Boy") {
                    extraColumns = `<td>${rowData.Sinif || ""}</td><td>${rowData.Boy || ""}</td>`;
                } else if (raporturu === "Sinif-Gen") {
                    extraColumns = `<td>${rowData.Sinif || ""}</td><td>${rowData.Gen || ""}</td>`;
                } else if (raporturu === "Kodu") {
                    extraColumns = `<td>${rowData.Kodu || ""}</td><td>${rowData.Adi || ""}</td>`;
                } else if (raporturu === "Konsimento") {
                    extraColumns = `<td>${rowData.Konsimento || ""}</td><td>${rowData.Aciklama || ""}</td>`;
                } else if (raporturu === "Hesap Kodu") {
                    extraColumns = `<td>${rowData.Cari_Firma || ""}</td><td>${rowData.Cari_Adi || ""}</td>`;
                } else if (raporturu === "Hesap Kodu-Ana_Alt_Grup") {
                    extraColumns = `<td>${rowData.Cari_Firma || ""}</td><td>${rowData.Ana_Grup || ""}</td><td>${rowData.Alta_Grup || ""}</td>`;
                } else if (raporturu === "Yil") {
                    extraColumns = `<td>${rowData.Yil || ""}</td>`;
                } else if (raporturu === "Yil_Ay") {
                    extraColumns = `<td>${rowData.Yil || ""}</td><td>${rowData.Ay || ""}</td>`;
                } else if (raporturu === "Ana Grup") {
                    extraColumns = `<td>${rowData.Ana_Grup || ""}</td><td>${rowData.Alt_Grup || ""}</td>`;
                }

                row.innerHTML = `
          ${extraColumns}
          <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
          <td class="double-column">${formatNumber2(rowData[dvz + "_Tutar"])}</td>
          <td class="double-column">${formatNumber0(rowData.Miktar)}</td>
          <td class="double-column">${formatNumber3(rowData.m3)}</td>
          <td class="double-column">${formatNumber2(rowData.m3_Ort_Fiat)}</td>
          <td class="double-column">${formatNumber2(rowData["m3_Ort_Fiat_" + dvz])}</td>
        `;

                mainTableBody.appendChild(row);
            });

        } catch (err) {
            M.showError(err?.message || String(err));
        } finally {
            M.setBtn("ortfiatyenileButton", false, "Yenile");
            M.setCursor(false);
        }
    };

    /* ---------------- download (jQuery yok) ---------------- */
    M.ortfiatDownload = async () => {
        M.clearError();
        M.setCursor(true);

        M.setBtn("ortfiatDownload", true, "İşleniyor...");
        M.setBtn("ortfiatyenileButton", true);

        const table = document.querySelector("#main-table");
        let headers = [];
        let rows = [];

        if (table) {
            table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));
            table.querySelectorAll("tbody tr").forEach(tr => {
                let rowData = {};
                let isEmpty = true;
                tr.querySelectorAll("td").forEach((td, index) => {
                    const value = td.innerText.trim();
                    if (value !== "") isEmpty = false;
                    rowData[headers[index]] = value;
                });
                if (!isEmpty) rows.push(rowData);
            });
        }

        try {
            const response = await fetchWithSessionCheckForDownload("kereste/ortfiat_download", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(rows)
            });

            if (response?.blob) {
                const disposition = response.headers.get("Content-Disposition") || "";
                const m = disposition.match(/filename="(.+)"/);
                const fileName = m ? m[1] : "kerortfiat.xlsx";

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
            M.setBtn("ortfiatDownload", false, "Rapor İndir");
            M.setBtn("ortfiatyenileButton", false);
            M.setCursor(false);
        }
    };

    /* ---------------- mail (aynı mantık: tableData JSON) ---------------- */
    M.ortfiatmailAt = () => {
        localStorage.removeItem("tableData");
        localStorage.removeItem("grprapor");
        localStorage.removeItem("tablobaslik");

        M.setCursor(true);

        try {
            const table = document.querySelector("#main-table");
            let headers = [];
            let rows = [];

            if (table) {
                table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));
                table.querySelectorAll("tbody tr").forEach(tr => {
                    let rowData = {};
                    let isEmpty = true;
                    tr.querySelectorAll("td").forEach((td, index) => {
                        const value = td.innerText.trim();
                        if (value !== "") isEmpty = false;
                        rowData[headers[index]] = value;
                    });
                    if (!isEmpty) rows.push(rowData);
                });
            }

            localStorage.setItem("tableData", JSON.stringify({ rows }));

            const degerler = "kerortfiat";
            const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
            mailsayfasiYukle(url);
        } finally {
            M.setCursor(false);
        }
    };

    /* ---------------- expose (HTML onclick / onchange için) ---------------- */
    window.anagrpChanged = M.anagrpChanged;
    window.openenvModal = M.openenvModal;

    window.dvzcevirChanged = M.dvzcevirChanged;

    window.kerortfiatdoldur = M.kerortfiatdoldur;
    window.ortfiatDownload = M.ortfiatDownload;
    window.ortfiatmailAt = M.ortfiatmailAt;

})();
