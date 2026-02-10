/* =========================================================
   KERFATRAPOR (jQuery YOK) - OBS Namespace
   - fkodu’da 1. sütun daralmasın: table class has-toggle/no-toggle
   - toggle td class kullanımın (td.toggle-button) korunuyor
   ========================================================= */

window.OBS = window.OBS || {};
OBS.KERFATRAPOR = OBS.KERFATRAPOR || {};

(() => {
    const M = OBS.KERFATRAPOR;

    /* ---------------- state ---------------- */
    M.currentPage = 0;
    M.totalPages = 0;
    M.pageSize = 250;
    M.data = null;

    /* ---------------- helpers ---------------- */
    M.el = (id) => document.getElementById(id);

    M.setCursor = (wait) => {
        document.body.style.cursor = wait ? "wait" : "default";
    };

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

    M.formUrlEncoded = (obj) => new URLSearchParams(obj);

    M.setText = (id, val) => {
        const el = M.el(id);
        if (el) el.innerText = val ?? "";
    };

    /* ---------------- DTO ---------------- */
    M.getfatraporDTO = () => {
        const hidden = M.el("fatrapBilgi");
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
            gruplama: p[26],
            caradr: p[27],
            gircik: p[28]
        };
    };

    /* ---------------- select change ---------------- */
    M.anagrpChanged = async (anagrpElement) => {
        const anagrup = anagrpElement?.value || "";
        const selectElement = M.el("altgrp");
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
                body: M.formUrlEncoded({ anagrup })
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
            M.showError(err?.message || String(err));
        } finally {
            M.setCursor(false);
        }
    };

    /* ---------------- pagination ---------------- */
    M.ilksayfa = () => M.kerfetchTableData(0);

    M.oncekisayfa = () => {
        if (M.currentPage > 0) M.kerfetchTableData(M.currentPage - 1);
    };

    M.sonrakisayfa = () => {
        if (M.currentPage < M.totalPages - 1) M.kerfetchTableData(M.currentPage + 1);
    };

    M.sonsayfa = () => M.kerfetchTableData(Math.max(0, M.totalPages - 1));

    /* ---------------- page size calc ---------------- */
    M.toplampagesize = async () => {
        try {
            M.clearError();
            const dto = M.getfatraporDTO();

            const response = await fetchWithSessionCheck("kereste/fatdoldursize", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dto)
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            const totalRecords = Number(response?.totalRecords || 0);
            M.totalPages = Math.ceil(totalRecords / M.pageSize);
        } catch (err) {
            M.showError(err?.message || String(err));
        }
    };

    M.kerfatdoldur = async () => {
        M.setCursor(true);
        try {
            await M.toplampagesize();          // ✅ await
            await M.kerfetchTableData(0);
        } finally {
            M.setCursor(false);
        }
    };

    /* ---------------- table utils ---------------- */
    M.clearTfoot = () => {
        const table = document.querySelector("#main-table");
        if (!table) return;
        const tfoot = table.querySelector("tfoot");
        if (!tfoot) return;
        tfoot.querySelectorAll("th").forEach((th) => (th.textContent = ""));
    };

    M._ensureTfoot = () => {
        const table = document.querySelector("#main-table");
        if (!table) return null;
        let tfoot = table.querySelector("tfoot");
        if (!tfoot) {
            tfoot = document.createElement("tfoot");
            table.appendChild(tfoot);
        }
        return tfoot;
    };

    /* ---------------- details fetch ---------------- */
    M.fetchDetails = async (evrakNo, cins) => {
        const gircik = (cins === "Alis") ? "G" : "C";

        const response = await fetchWithSessionCheck("kereste/fatdetay", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: M.formUrlEncoded({ evrakNo, gircik })
        });

        if (response?.errorMessage) throw new Error(response.errorMessage);
        return response;
    };

    /* ---------------- header builders ---------------- */
    M.updateTableHeadersfno = (headers) => {
        const thead = document.querySelector("#main-table thead");
        const tfoot = M._ensureTfoot();
        if (!thead || !tfoot) return;

        thead.innerHTML = "";
        const trHead = document.createElement("tr");
        trHead.classList.add("thead-dark");

        headers.forEach((header, index) => {
            const th = document.createElement("th");
            th.textContent = header;
            if (index >= headers.length - 5) th.classList.add("double-column");
            trHead.appendChild(th);
        });
        thead.appendChild(trHead);

        tfoot.innerHTML = "";
        const trFoot = document.createElement("tr");

        headers.forEach((_, index) => {
            const th = document.createElement("th");
            if (index === 7) {
                th.textContent = "0.000";
                th.id = "toplam-" + index;
                th.classList.add("double-column");
            } else if (index === 11) {
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

    M.updateTableHeadersfkodu = (headers) => {
        const thead = document.querySelector("#main-table thead");
        const tfoot = M._ensureTfoot();
        if (!thead || !tfoot) return;

        thead.innerHTML = "";
        const trHead = document.createElement("tr");
        trHead.classList.add("thead-dark");

        headers.forEach((header, index) => {
            const th = document.createElement("th");
            th.textContent = header;
            if (index >= headers.length - 5) th.classList.add("double-column");
            trHead.appendChild(th);
        });
        thead.appendChild(trHead);

        tfoot.innerHTML = "";
        const trFoot = document.createElement("tr");

        headers.forEach((_, index) => {
            const th = document.createElement("th");
            if (index === 4) {
                th.textContent = "0.000";
                th.id = "toplam-" + index;
                th.classList.add("double-column");
            } else if (index === 8) {
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

    M.updateTableHeadersfnotar = (headers) => {
        const thead = document.querySelector("#main-table thead");
        const tfoot = M._ensureTfoot();
        if (!thead || !tfoot) return;

        thead.innerHTML = "";
        const trHead = document.createElement("tr");
        trHead.classList.add("thead-dark");

        headers.forEach((header, index) => {
            const th = document.createElement("th");
            th.textContent = header;
            if (index >= headers.length - 5) th.classList.add("double-column");
            trHead.appendChild(th);
        });
        thead.appendChild(trHead);

        tfoot.innerHTML = "";
        const trFoot = document.createElement("tr");

        headers.forEach((_, index) => {
            const th = document.createElement("th");
            if (index === 6) {
                th.textContent = "0.000";
                th.id = "toplam-" + index;
                th.classList.add("double-column");
            } else if (index === 10) {
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

    /* ---------------- main fetch table ---------------- */
    M.kerfetchTableData = async (page) => {
        const dto = M.getfatraporDTO();
        dto.page = page;
        dto.pageSize = M.pageSize;
        M.currentPage = page;

        M.clearError();
        M.setCursor(true);

        M.setBtn("fatrapyenileButton", true, "İşleniyor...");

        const mainTableBody = M.el("mainTableBody");
        if (mainTableBody) mainTableBody.innerHTML = "";
        M.clearTfoot();

        try {
            const response = await fetchWithSessionCheck("kereste/fatrapdoldur", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dto)
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            M.data = response;

            const raporturu = response.raporturu;
            const table = document.querySelector("#main-table");

            // ✅ KRİTİK: fkodu’da toggle yok => ilk sütunu daraltma
            if (table) {
                if (raporturu === "fkodu") {
                    table.classList.remove("has-toggle");
                    table.classList.add("no-toggle");
                } else {
                    table.classList.add("has-toggle");
                    table.classList.remove("no-toggle");
                }
            }

            // headers
            if (raporturu === "fno") {
                M.updateTableHeadersfno(["", "EVRAK NO", "HAREKET", "TARIH", "CARI_HESAP", "ADRES_HESAP", "DOVIZ", "M3", "TUTAR", "ISK. TUTAR", "KDV TUTAR", "TOPLAM TUTAR"]);
            } else if (raporturu === "fkodu") {
                M.updateTableHeadersfkodu(["CARI_HESAP", "HAREKET", "UNVAN", "VERGI NO", "M3", "TUTAR", "ISK. TUTAR", "KDV TUTAR", "TOPLAM TUTAR"]);
            } else if (raporturu === "fnotar") {
                M.updateTableHeadersfnotar(["", "EVRAK NO", "HAREKET", "TARIH", "UNVAN", "VERGI NO", "M3", "TUTAR", "ISK. TUTAR", "KDV TUTAR", "TOPLAM TUTAR"]);
            }

            const thCount = document.querySelectorAll("#main-table thead th").length;

            let totalmiktar = 0;
            let totaltutar = 0;

            const rows = response?.data || [];
            rows.forEach((rowData) => {
                const row = document.createElement("tr");
                row.classList.add("expandable", "table-row-height");

                if (raporturu === "fno") {
                    row.innerHTML = `
            <td class="toggle-button">+</td>
            <td>${rowData.Fatura_No || ""}</td>
            <td>${rowData.Hareket || ""}</td>
            <td>${formatDate(rowData.Tarih)}</td>
            <td>${rowData.Unvan || ""}</td>
            <td>${rowData.Adres_Firma || ""}</td>
            <td>${rowData.Doviz || ""}</td>
            <td class="double-column">${formatNumber3(rowData.m3)}</td>
            <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Iskontolu_Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Kdv_Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Toplam_Tutar)}</td>
          `;
                    totalmiktar += (rowData.m3 || 0);
                    totaltutar += (rowData.Toplam_Tutar || 0);

                } else if (raporturu === "fkodu") {
                    row.innerHTML = `
            <td>${rowData.Firma_Kodu || ""}</td>
            <td>${rowData.Hareket || ""}</td>
            <td>${rowData.Unvan || ""}</td>
            <td>${rowData.Vergi_No || ""}</td>
            <td class="double-column">${formatNumber3(rowData.m3)}</td>
            <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Iskontolu_Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Kdv_Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Toplam_Tutar)}</td>
          `;
                    totalmiktar += (rowData.m3 || 0);
                    totaltutar += (rowData.Toplam_Tutar || 0);

                } else if (raporturu === "fnotar") {
                    row.innerHTML = `
            <td class="toggle-button">+</td>
            <td>${rowData.Fatura_No || ""}</td>
            <td>${rowData.Hareket || ""}</td>
            <td>${formatDate(rowData.Tarih)}</td>
            <td>${rowData.Unvan || ""}</td>
            <td>${rowData.Vergi_No || ""}</td>
            <td class="double-column">${formatNumber3(rowData.m3)}</td>
            <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Iskontolu_Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Kdv_Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData.Toplam_Tutar)}</td>
          `;
                    totalmiktar += (rowData.m3 || 0);
                    totaltutar += (rowData.Toplam_Tutar || 0);
                }

                if (mainTableBody) mainTableBody.appendChild(row);

                // details row
                const detailsRow = document.createElement("tr");
                detailsRow.classList.add("details-row");
                detailsRow.style.display = "none";
                detailsRow.innerHTML = `<td colspan="${thCount}"></td>`;
                if (mainTableBody) mainTableBody.appendChild(detailsRow);

                // fkodu detay yok
                if (raporturu === "fkodu") return;

                const toggle = row.querySelector(".toggle-button");
                if (!toggle) return;

                // row select
                row.addEventListener("click", () => {
                    document.querySelectorAll("#main-table tbody tr.selected")
                        .forEach((r) => r.classList.remove("selected"));
                    row.classList.add("selected");
                });

                toggle.addEventListener("click", async (e) => {
                    e.stopPropagation();

                    const isOpen = (detailsRow.style.display === "table-row");

                    // tek satır açık kalsın
                    document.querySelectorAll("#main-table tr.details-row").forEach((r) => (r.style.display = "none"));
                    document.querySelectorAll("#main-table tbody tr").forEach((r) => r.classList.remove("selected"));
                    document.querySelectorAll("#main-table .toggle-button").forEach((x) => {
                        if (x.textContent === "-") x.textContent = "+";
                    });

                    if (isOpen) {
                        detailsRow.style.display = "none";
                        toggle.textContent = "+";
                        return;
                    }

                    row.classList.add("selected");
                    detailsRow.style.display = "table-row";
                    toggle.textContent = "-";

                    if (detailsRow.dataset.loaded === "1") return;

                    M.setCursor(true);
                    try {
                        const detResp = await M.fetchDetails(rowData.Fatura_No, rowData.Hareket);
                        const det = (detResp && detResp.data) ? detResp.data : [];

                        let html = `
              <div class="details-wrap">
                <table class="t-details">
                  <thead>
                    <tr>
                      <th>Fatura_No</th><th>Barkod</th><th>Kodu</th><th>Paket_No</th>
                      <th class="double-column">Miktar</th><th class="double-column">m3</th>
                      <th class="double-column">Kdv</th><th>Doviz</th>
                      <th class="double-column">Fiat</th><th class="double-column">Tutar</th>
                      <th class="double-column">Kur</th><th>Cari_Firma</th><th>Adres_Firma</th>
                      <th class="double-column">Iskonto</th><th class="double-column">Tevkifat</th>
                      <th>Ana Grup</th><th>Alt Grup</th><th>Mensei</th><th>Depo</th>
                      <th>Ozel Kod</th><th>Izahat</th><th>Nakliyeci</th><th>User</th>
                    </tr>
                  </thead>
                  <tbody>
            `;

                        det.forEach((item) => {
                            html += `
                <tr>
                  <td style="min-width:80px;">${item.Fatura_No || ""}</td>
                  <td>${item.Barkod || ""}</td>
                  <td>${item.Kodu || ""}</td>
                  <td>${item.Paket_No || ""}</td>
                  <td class="double-column">${formatNumber0(item.Miktar)}</td>
                  <td class="double-column">${formatNumber3(item.m3)}</td>
                  <td class="double-column">${formatNumber2(item.Kdv)}</td>
                  <td>${item.Doviz || ""}</td>
                  <td class="double-column">${formatNumber2(item.Fiat)}</td>
                  <td class="double-column">${formatNumber2(item.Tutar)}</td>
                  <td class="double-column">${formatNumber2(item.Kur)}</td>
                  <td>${item.Cari_Firma || ""}</td>
                  <td>${item.Adres_Firma || ""}</td>
                  <td class="double-column">${formatNumber2(item.Iskonto)}</td>
                  <td class="double-column">${formatNumber2(item.Tevkifat)}</td>
                  <td>${item.Ana_Grup || ""}</td>
                  <td>${item.Alt_Grup || ""}</td>
                  <td>${item.Mensei || ""}</td>
                  <td>${item.Depo || ""}</td>
                  <td>${item.Ozel_Kod || ""}</td>
                  <td>${item.Izahat || ""}</td>
                  <td>${item.Nakliyeci || ""}</td>
                  <td>${item.USER || ""}</td>
                </tr>
              `;
                        });

                        html += `</tbody></table></div>`;
                        detailsRow.children[0].innerHTML = html;
                        detailsRow.dataset.loaded = "1";
                    } catch (err) {
                        detailsRow.children[0].innerHTML =
                            `<div class="details-wrap"><b>Hata:</b> Detaylar alınamadı.</div>`;
                    } finally {
                        M.setCursor(false);
                    }
                });
            });

            // totals
            if (raporturu === "fno") {
                M.setText("toplam-7", formatNumber3(totalmiktar));
                M.setText("toplam-11", formatNumber2(totaltutar));
            } else if (raporturu === "fkodu") {
                M.setText("toplam-4", formatNumber3(totalmiktar));
                M.setText("toplam-8", formatNumber2(totaltutar));
            } else if (raporturu === "fnotar") {
                M.setText("toplam-6", formatNumber3(totalmiktar));
                M.setText("toplam-10", formatNumber2(totaltutar));
            }

        } catch (err) {
            M.showError(err?.message || String(err));
        } finally {
            M.setBtn("fatrapyenileButton", false, "Yenile");
            M.setCursor(false);
        }
    };

    /* ---------------- modal open (Bootstrap 5 jQuery'siz) ---------------- */
    M.openkfatrapModal = async (modalSelectorOrEl) => {
        M.setCursor(true);
        M.clearError();

        try {
            const modalEl = (typeof modalSelectorOrEl === "string")
                ? document.querySelector(modalSelectorOrEl)
                : modalSelectorOrEl;

            if (modalEl && window.bootstrap?.Modal) {
                const instance = bootstrap.Modal.getOrCreateInstance(modalEl);
                instance.show();
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

            [anaSelect, canaSelect, dpoSelect, cdpoSelect, ozSelect, cozSelect].forEach(s => { if (s) s.innerHTML = ""; });

            ana.forEach((item) => {
                if (anaSelect) anaSelect.appendChild(new Option(item.ANA_GRUP, item.ANA_GRUP));
                if (canaSelect) canaSelect.appendChild(new Option(item.ANA_GRUP, item.ANA_GRUP));
            });

            dpo.forEach((item) => {
                if (dpoSelect) dpoSelect.appendChild(new Option(item.DEPO, item.DEPO));
                if (cdpoSelect) cdpoSelect.appendChild(new Option(item.DEPO, item.DEPO));
            });

            oz.forEach((item) => {
                if (ozSelect) ozSelect.appendChild(new Option(item.OZEL_KOD_1, item.OZEL_KOD_1));
                if (cozSelect) cozSelect.appendChild(new Option(item.OZEL_KOD_1, item.OZEL_KOD_1));
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

    /* ---------------- download report (jQuery yok) ---------------- */
    M.fatrapdownloadReport = async () => {
        M.clearError();
        M.setCursor(true);

        M.setBtn("indirButton", true, "İşleniyor...");
        M.setBtn("yenileButton", true);

        try {
            const table = document.querySelector("#main-table");
            if (!table) throw new Error("Tablo bulunamadı.");

            const headers = [];
            const rows = [];

            table.querySelectorAll("thead th").forEach((th) => headers.push(th.innerText.trim()));

            // details-row dahil etme
            table.querySelectorAll("tbody tr").forEach((tr) => {
                if (tr.classList.contains("details-row")) return;

                const rowData = {};
                let isEmpty = true;

                tr.querySelectorAll("td").forEach((td, index) => {
                    const value = td.innerText.trim();
                    if (value !== "") isEmpty = false;
                    rowData[headers[index]] = value;
                });

                if (!isEmpty) rows.push(rowData);
            });

            const response = await fetchWithSessionCheckForDownload("kereste/fatrap_download", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(rows)
            });

            if (response?.blob) {
                const disposition = response.headers.get("Content-Disposition") || "";
                const m = disposition.match(/filename="(.+)"/);
                const fileName = m ? m[1] : "rapor.xlsx";

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
            M.showError(err?.message || String(err));
        } finally {
            M.setBtn("indirButton", false, "Rapor İndir");
            M.setBtn("yenileButton", false);
            M.setCursor(false);
        }
    };

    /* ---------------- mail (jQuery yok) ---------------- */
    M.fatrapmailAt = () => {
        localStorage.removeItem("tableData");
        localStorage.removeItem("grprapor");
        localStorage.removeItem("tablobaslik");

        const table = document.querySelector("#main-table");
        if (!table) {
            M.showError("Tablo bulunamadı.");
            return;
        }

        const headers = [];
        const rows = [];

        table.querySelectorAll("thead th").forEach((th) => headers.push(th.innerText.trim()));

        table.querySelectorAll("tbody tr").forEach((tr) => {
            if (tr.classList.contains("details-row")) return;

            const rowData = {};
            let isEmpty = true;

            tr.querySelectorAll("td").forEach((td, index) => {
                const value = td.innerText.trim();
                if (value !== "") isEmpty = false;
                rowData[headers[index]] = value;
            });

            if (!isEmpty) rows.push(rowData);
        });

        localStorage.setItem("tableData", JSON.stringify({ rows }));

        const degerler = "kerfatrapor";
        const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
        mailsayfasiYukle(url);
    };

    /* ---------------- expose (HTML onclick için) ---------------- */
    window.anagrpChanged = M.anagrpChanged;
    window.ilksayfa = M.ilksayfa;
    window.oncekisayfa = M.oncekisayfa;
    window.sonrakisayfa = M.sonrakisayfa;
    window.sonsayfa = M.sonsayfa;
    window.kerfatdoldur = M.kerfatdoldur;
    window.fatrapdownloadReport = M.fatrapdownloadReport;
    window.fatrapmailAt = M.fatrapmailAt;
    window.openkfatrapModal = M.openkfatrapModal;

})();
