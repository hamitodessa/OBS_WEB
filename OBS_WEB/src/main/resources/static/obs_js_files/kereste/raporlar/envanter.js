/* =========================================================
   KERENVANTER (jQuery TEMİZ) - OBS Namespace
   window.OBS = window.OBS || {};
   OBS.KERENVANTER = ...
   ========================================================= */

window.OBS = window.OBS || {};
OBS.KERENVANTER = OBS.KERENVANTER || {};

(() => {
    const M = OBS.KERENVANTER;

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
    M.openkenvModal = async (modalSelectorOrEl) => {
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
            const dpo = response.depoKodlari || [];
            const oz = response.oz1Kodlari || [];

            const anaSelect = M.el("anagrp");
            const canaSelect = M.el("canagrp");
            const dpoSelect = M.el("depo");
            const cdpoSelect = M.el("cdepo");
            const ozSelect = M.el("ozkod");
            const cozSelect = M.el("cozkod");

            if (anaSelect) anaSelect.innerHTML = "";
            if (canaSelect) canaSelect.innerHTML = "";
            if (dpoSelect) dpoSelect.innerHTML = "";
            if (cdpoSelect) cdpoSelect.innerHTML = "";
            if (ozSelect) ozSelect.innerHTML = "";
            if (cozSelect) cozSelect.innerHTML = "";

            ana.forEach((item) => {
                if (anaSelect) {
                    const o = document.createElement("option");
                    o.value = item.ANA_GRUP;
                    o.textContent = item.ANA_GRUP;
                    anaSelect.appendChild(o);
                }
                if (canaSelect) {
                    const o2 = document.createElement("option");
                    o2.value = item.ANA_GRUP;
                    o2.textContent = item.ANA_GRUP;
                    canaSelect.appendChild(o2);
                }
            });

            dpo.forEach((item) => {
                if (dpoSelect) {
                    const o = document.createElement("option");
                    o.value = item.DEPO;
                    o.textContent = item.DEPO;
                    dpoSelect.appendChild(o);
                }
                if (cdpoSelect) {
                    const o2 = document.createElement("option");
                    o2.value = item.DEPO;
                    o2.textContent = item.DEPO;
                    cdpoSelect.appendChild(o2);
                }
            });

            oz.forEach((item) => {
                if (ozSelect) {
                    const o = document.createElement("option");
                    o.value = item.OZEL_KOD_1;
                    o.textContent = item.OZEL_KOD_1;
                    ozSelect.appendChild(o);
                }
                if (cozSelect) {
                    const o2 = document.createElement("option");
                    o2.value = item.OZEL_KOD_1;
                    o2.textContent = item.OZEL_KOD_1;
                    cozSelect.appendChild(o2);
                }
            });

            const insertBos = (sel) => {
                if (!sel) return;
                const o = document.createElement("option");
                o.value = "Bos Olanlar";
                o.textContent = "Bos Olanlar";
                sel.insertBefore(o, sel.options[1] || null);
            };

            insertBos(anaSelect);
            insertBos(canaSelect);
            insertBos(dpoSelect);
            insertBos(cdpoSelect);

        } catch (err) {
            M.showError(`Bir hata oluştu: ${err?.message || String(err)}`);
        } finally {
            M.setCursor(false);
        }
    };

    /* ---------------- action ---------------- */
    M.kerenvanterdoldur = async () => {
        // toplampagesize(); yok
        await M.envanterfetchTableData();
    };

    /* ---------------- DTO (jQuery yok) ---------------- */
    M.getKeresteDetayRaporDTO = () => {
        const hidden = M.el("envanterBilgi");
        const raw = hidden ? (hidden.value || "") : "";
        const p = raw.split(",");

        return {
            gtar1: p[0],
            gtar2: p[1],
            ctar1: p[2],
            ctar2: p[3],
            ukodu1: p[4],
            ukodu2: p[5],
            cfirma1: p[6],
            cfirma2: p[7],
            pak1: p[8],
            pak2: p[9],
            cevr1: p[10],
            cevr2: p[11],
            gfirma1: p[12],
            gfirma2: p[13],
            cana: p[14],
            evr1: p[15],
            evr2: p[16],
            calt: p[17],
            gana: p[18],
            galt: p[19],
            gdepo: p[20],
            gozkod: p[21],
            kons1: p[22],
            kons2: p[23],
            cozkod: p[24],
            cdepo: p[25],
            gruplama: p[26]
        };
    };

    /* ---------------- tfoot helpers ---------------- */
    M.clearTfoot = () => {
        const table = document.querySelector("#main-table");
        if (!table) return;
        const tfoot = table.querySelector("tfoot");
        if (!tfoot) return;
        tfoot.querySelectorAll("th").forEach((th) => (th.textContent = ""));
    };

    /* ---------------- headers (senin birebir mantık) ---------------- */
    M.updateTableHeaderskodu = (headers) => {
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
            if (index >= headers.length - 9) th.classList.add("double-column");
            trHead.appendChild(th);
        });

        thead.appendChild(trHead);

        tfoot.innerHTML = "";
        const trFoot = document.createElement("tr");

        headers.forEach((_, index) => {
            const th = document.createElement("th");
            if (index === 1 || index === 4) {
                th.textContent = "0";
                th.id = "toplam-" + index;
                th.classList.add("double-column");
            } else if (index === 2 || index === 5 || index === 7) {
                th.textContent = "0.000";
                th.id = "toplam-" + index;
                th.classList.add("double-column");
            } else if (index === 3 || index === 6 || index === 9) {
                th.textContent = "0.00";
                th.id = "toplam-" + index;
                th.classList.add("double-column");
            } else {
                th.textContent = "";
            }
            trFoot.appendChild(th);
        });

        tfoot.appendChild(trFoot);
    };

    M.updateTableHeaders = (headers) => {
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
            if (index >= headers.length - 9) th.classList.add("double-column");
            trHead.appendChild(th);
        });

        thead.appendChild(trHead);

        tfoot.innerHTML = "";
        const trFoot = document.createElement("tr");

        headers.forEach((_, index) => {
            const th = document.createElement("th");
            if (index === 2 || index === 5) {
                th.textContent = "0";
                th.id = "toplam-" + index;
                th.classList.add("double-column");
            } else if (index === 3 || index === 6 || index === 8) {
                th.textContent = "0.000";
                th.id = "toplam-" + index;
                th.classList.add("double-column");
            } else if (index === 4 || index === 7 || index === 10) {
                th.textContent = "0.00";
                th.id = "toplam-" + index;
                th.classList.add("double-column");
            } else {
                th.textContent = "";
            }
            trFoot.appendChild(th);
        });

        tfoot.appendChild(trFoot);
    };

    /* ---------------- fetch + render ---------------- */
    M.envanterfetchTableData = async () => {
        const dto = M.getKeresteDetayRaporDTO();

        M.clearError();
        M.setCursor(true);
        M.setBtn("envanteryenileButton", true, "İşleniyor...");

        const mainTableBody = M.el("mainTableBody");
        if (mainTableBody) mainTableBody.innerHTML = "";
        M.clearTfoot();

        try {
            const response = await fetchWithSessionCheck("kereste/kerenvanter", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dto)
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            const data = response;
            const raporturu = data.raporturu;

            let sqlHeaders = [];
            if (raporturu === "Urun Kodu") {
                sqlHeaders = ["KODU", "GIRIS MIKTAR", "GIRIS M3", "GIRIS TUTARI", "CIKIS MIKTARI", "CIKIS M3", "CIKIS TUTARI", "STOK M3", "ORT FIAT", "STOK TUTAR"];
                M.updateTableHeaderskodu(sqlHeaders);
            } else if (raporturu === "Konsimento") {
                sqlHeaders = ["KONSIMENTO", "ACIKLAMA", "GIRIS MIKTAR", "GIRIS M3", "GIRIS TUTARI", "CIKIS MIKTARI", "CIKIS M3", "CIKIS TUTARI", "STOK M3", "ORT FIAT", "STOK TUTAR"];
                M.updateTableHeaders(sqlHeaders);
            } else if (raporturu === "Hesap-Kodu") {
                sqlHeaders = ["CARI FIRMA", "UNVAN", "GIRIS MIKTAR", "GIRIS M3", "GIRIS TUTARI", "CIKIS MIKTARI", "CIKIS M3", "CIKIS TUTARI", "STOK M3", "ORT FIAT", "STOK TUTAR"];
                M.updateTableHeaders(sqlHeaders);
            } else if (raporturu === "Ana_Grup-Alt_Grup") {
                sqlHeaders = ["ANA GRUP", "ALT GRUP", "GIRIS MIKTAR", "GIRIS M3", "GIRIS TUTARI", "CIKIS MIKTARI", "CIKIS M3", "CIKIS TUTARI", "STOK M3", "ORT FIAT", "STOK TUTAR"];
                M.updateTableHeaders(sqlHeaders);
            }

            // totals
            let totalgmiktar = 0, totalgm3 = 0, totalgtutar = 0;
            let totalcmiktar = 0, totalcm3 = 0, totalctutar = 0;
            let totalstokm3 = 0, totaltutar = 0;

            (data.data || []).forEach((rowData) => {
                const row = document.createElement("tr");
                row.classList.add("expandable", "table-row-height");

                if (raporturu === "Urun Kodu") {
                    row.innerHTML = `
            <td>${rowData.Kodu || ""}</td>
            <td class="double-column">${formatNumber0(rowData.Giris_Miktar)}</td>
            <td class="double-column">${formatNumber3(rowData.Giris_m3)}</td>
            <td class="double-column">${formatNumber2(rowData.Giris_Tutar)}</td>
            <td class="double-column">${formatNumber0(rowData.Cikis_Miktar)}</td>
            <td class="double-column">${formatNumber3(rowData.Cikis_m3)}</td>
            <td class="double-column">${formatNumber2(rowData.Cikis_Tutar)}</td>
            <td class="double-column">${formatNumber3(rowData.Stok_M3)}</td>
            <td class="double-column">${formatNumber2(rowData.Ort_Fiat)}</td>
            <td class="double-column">${formatNumber2(rowData.Stok_Tutar)}</td>
          `;
                } else if (raporturu === "Konsimento") {
                    row.innerHTML = `
            <td>${rowData.Konsimento || ""}</td>
            <td>${rowData.Aciklama || ""}</td>
            <td class="double-column">${formatNumber0(rowData.Giris_Miktar)}</td>
            <td class="double-column">${formatNumber3(rowData.Giris_m3)}</td>
            <td class="double-column">${formatNumber2(rowData.Giris_Tutar)}</td>
            <td class="double-column">${formatNumber0(rowData.Cikis_Miktar)}</td>
            <td class="double-column">${formatNumber3(rowData.Cikis_m3)}</td>
            <td class="double-column">${formatNumber2(rowData.Cikis_Tutar)}</td>
            <td class="double-column">${formatNumber3(rowData.Stok_M3)}</td>
            <td class="double-column">${formatNumber2(rowData.Ort_Fiat)}</td>
            <td class="double-column">${formatNumber2(rowData.Stok_Tutar)}</td>
          `;
                } else if (raporturu === "Hesap-Kodu") {
                    row.innerHTML = `
            <td>${rowData.Cari_Firma || ""}</td>
            <td>${rowData.Unvan || ""}</td>
            <td class="double-column">${formatNumber0(rowData.Giris_Miktar)}</td>
            <td class="double-column">${formatNumber3(rowData.Giris_m3)}</td>
            <td class="double-column">${formatNumber2(rowData.Giris_Tutar)}</td>
            <td class="double-column">${formatNumber0(rowData.Cikis_Miktar)}</td>
            <td class="double-column">${formatNumber3(rowData.Cikis_m3)}</td>
            <td class="double-column">${formatNumber2(rowData.Cikis_Tutar)}</td>
            <td class="double-column">${formatNumber3(rowData.Stok_M3)}</td>
            <td class="double-column">${formatNumber2(rowData.Ort_Fiat)}</td>
            <td class="double-column">${formatNumber2(rowData.Stok_Tutar)}</td>
          `;
                } else if (raporturu === "Ana_Grup-Alt_Grup") {
                    row.innerHTML = `
            <td>${rowData.Ana_Grup || ""}</td>
            <td>${rowData.Alt_Grup || ""}</td>
            <td class="double-column">${formatNumber0(rowData.Giris_Miktar)}</td>
            <td class="double-column">${formatNumber3(rowData.Giris_m3)}</td>
            <td class="double-column">${formatNumber2(rowData.Giris_Tutar)}</td>
            <td class="double-column">${formatNumber0(rowData.Cikis_Miktar)}</td>
            <td class="double-column">${formatNumber3(rowData.Cikis_m3)}</td>
            <td class="double-column">${formatNumber2(rowData.Cikis_Tutar)}</td>
            <td class="double-column">${formatNumber3(rowData.Stok_M3)}</td>
            <td class="double-column">${formatNumber2(rowData.Ort_Fiat)}</td>
            <td class="double-column">${formatNumber2(rowData.Stok_Tutar)}</td>
          `;
                }

                if (mainTableBody) mainTableBody.appendChild(row);

                // totals (hepsi aynı alanlar)
                totalgmiktar += (rowData.Giris_Miktar || 0);
                totalgm3 += (rowData.Giris_m3 || 0);
                totalgtutar += (rowData.Giris_Tutar || 0);
                totalcmiktar += (rowData.Cikis_Miktar || 0);
                totalcm3 += (rowData.Cikis_m3 || 0);
                totalctutar += (rowData.Cikis_Tutar || 0);
                totalstokm3 += (rowData.Stok_M3 || 0);
                totaltutar += (rowData.Stok_Tutar || 0);
            });

            // footer yaz (senin eski index mapping birebir)
            if (raporturu === "Urun Kodu") {
                M.el("toplam-1").innerText = formatNumber0(totalgmiktar);
                M.el("toplam-2").innerText = formatNumber3(totalgm3);
                M.el("toplam-3").innerText = formatNumber2(totalgtutar);
                M.el("toplam-4").innerText = formatNumber0(totalcmiktar);
                M.el("toplam-5").innerText = formatNumber3(totalcm3);
                M.el("toplam-6").innerText = formatNumber2(totalctutar);
                M.el("toplam-7").innerText = formatNumber3(totalstokm3);
                M.el("toplam-9").innerText = formatNumber2(totaltutar);
            } else {
                M.el("toplam-2").innerText = formatNumber0(totalgmiktar);
                M.el("toplam-3").innerText = formatNumber3(totalgm3);
                M.el("toplam-4").innerText = formatNumber2(totalgtutar);
                M.el("toplam-5").innerText = formatNumber0(totalcmiktar);
                M.el("toplam-6").innerText = formatNumber3(totalcm3);
                M.el("toplam-7").innerText = formatNumber2(totalctutar);
                M.el("toplam-8").innerText = formatNumber3(totalstokm3);
                M.el("toplam-10").innerText = formatNumber2(totaltutar);
            }

        } catch (err) {
            M.showError(err?.message || String(err));
        } finally {
            M.setBtn("envanteryenileButton", false, "Yenile");
            M.setCursor(false);
        }
    };

    /* ---------------- download (jQuery yok) ---------------- */
    M.envanterDownload = async () => {
        M.clearError();
        M.setCursor(true);

        M.setBtn("envanterDownload", true, "İşleniyor...");
        M.setBtn("envanteryenileButton", true);

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
            const response = await fetchWithSessionCheckForDownload("kereste/envanter_download", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(rows)
            });

            if (response?.blob) {
                const disposition = response.headers.get("Content-Disposition") || "";
                const m = disposition.match(/filename="(.+)"/);
                const fileName = m ? m[1] : "kerenvanter.xlsx";

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
            M.setBtn("envanterDownload", false, "Rapor İndir");
            M.setBtn("envanteryenileButton", false);
            M.setCursor(false);
        }
    };

    /* ---------------- mail (aynı mantık: tableData JSON) ---------------- */
    M.envantermailAt = () => {
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

            const degerler = "kerenvanter";
            const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
            mailsayfasiYukle(url);
        } finally {
            M.setCursor(false);
        }
    };

    /* ---------------- expose (HTML onclick için) ---------------- */
    window.anagrpChanged = M.anagrpChanged;
    window.openenvModal = M.openenvModal;

    window.kerenvanterdoldur = M.kerenvanterdoldur;
    window.envanterfetchTableData = M.envanterfetchTableData;

    window.envanterDownload = M.envanterDownload;
    window.envantermailAt = M.envantermailAt;

})();
