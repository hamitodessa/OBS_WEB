window.OBS = window.OBS || {};
OBS.TAHRAP = OBS.TAHRAP || {};

/* =========================
   HELPERS
   ========================= */
OBS.TAHRAP._el = (id) => document.getElementById(id);

OBS.TAHRAP._getVal = (id) => (OBS.TAHRAP._el(id)?.value ?? "");

OBS.TAHRAP._setBtn = (id, disabled, text) => {
    const b = OBS.TAHRAP._el(id);
    if (!b) return;
    b.disabled = !!disabled;
    if (text !== undefined) b.innerText = text;
};

OBS.TAHRAP._clearError = () => {
    const e = OBS.TAHRAP._el("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
};

OBS.TAHRAP._showError = (msg) => {
    const e = OBS.TAHRAP._el("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Beklenmeyen hata.";
};

OBS.TAHRAP._getBilgi = () => {
    const raw = OBS.TAHRAP._getVal("tahrapBilgi");
    const p = (raw || "").split(",");
    return {
        tah_ted: p[0] || "",
        hangi_tur: p[1] || "",
        pos: p[2] || "",
        hkodu1: p[3] || "",
        hkodu2: p[4] || "",
        startDate: p[5] || "",
        endDate: p[6] || "",
        evrak1: p[7] || "",
        evrak2: p[8] || ""
    };
};

/* =========================
   DETAILS FETCH
   ========================= */
OBS.TAHRAP.fetchDetails = async function(evrakNo, cins) {
    let tah_ted = (cins === "Tahsilat") ? 0 : 1;

    const dto = await fetchWithSessionCheck("cari/tahcekdokum", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ evrakNo, tah_ted }),
    });

    if (!dto?.success) throw new Error(dto?.errorMessage || "Detay alınamadı.");
    return dto; // { success, data, ... }
};

/* =========================
   MAIN TABLE
   ========================= */
OBS.TAHRAP.tahrapfetchTableData = async function() {
    OBS.TAHRAP._clearError();

    const dto = OBS.TAHRAP._getBilgi();

    document.body.style.cursor = "wait";
    OBS.TAHRAP._setBtn("tahrapyenileButton", true, "İşleniyor...");

    const body = OBS.TAHRAP._el("mainTableBody");
    if (body) body.innerHTML = "";

    const thCount = document.querySelectorAll("#main-table thead th").length;

    try {
        const res = await fetchWithSessionCheck("cari/tahrapdoldur", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dto),
        });

        if (!res?.success) {
            OBS.TAHRAP._showError(res?.errorMessage || "Bir hata oluştu.");
            return;
        }

        (res.data || []).forEach(rowData => {
            const hasDetails = (rowData.TUR === "Cek");

            const tr = document.createElement("tr");
            tr.innerHTML = `
        <td><span class="toggle-button">${hasDetails ? "+" : ""}</span></td>
        <td>${rowData.EVRAK || ""}</td>
        <td>${formatDate(rowData.TARIH)}</td>
        <td>${rowData.CARI_HESAP || ""}</td>
        <td>${rowData.UNVAN || ""}</td>
        <td>${rowData.ADRES_HESAP || ""}</td>
        <td>${rowData.ADRES_UNVAN || ""}</td>
        <td>${rowData.CINS || ""}</td>
        <td>${rowData.TUR || ""}</td>
        <td>${rowData.POS_BANKA || ""}</td>
        <td>${rowData.DVZ_CINS || ""}</td>
        <td class="double-column">${formatNumber2(rowData.TUTAR)}</td>
      `;
            body.appendChild(tr);

            const detailsTr = document.createElement("tr");
            detailsTr.className = "details-row";
            detailsTr.style.display = "none";
            detailsTr.innerHTML = `<td colspan="${thCount}"></td>`;
            body.appendChild(detailsTr);

            if (!hasDetails) return;

            const toggle = tr.querySelector(".toggle-button");
            toggle.addEventListener("click", async (e) => {
                e.stopPropagation();

                const isOpen = (detailsTr.style.display === "table-row");

                // tek satır açık kalsın + reset
                document.querySelectorAll("#main-table tr.details-row").forEach(r => r.style.display = "none");
                document.querySelectorAll("#main-table tbody tr").forEach(r => r.classList.remove("selected"));
                document.querySelectorAll("#main-table .toggle-button").forEach(x => {
                    if (x.textContent === "-") x.textContent = "+";
                });

                if (isOpen) {
                    detailsTr.style.display = "none";
                    toggle.textContent = "+";
                    return;
                }

                tr.classList.add("selected");
                detailsTr.style.display = "table-row";
                toggle.textContent = "-";

                if (detailsTr.dataset.loaded === "1") return;

                document.body.style.cursor = "wait";
                try {
                    const detResp = await OBS.TAHRAP.fetchDetails(rowData.EVRAK, rowData.CINS);
                    const det = detResp?.data || [];

                    let html = `
            <div class="details-wrap">
              <table class="t-details" style="table-layout:fixed; width:100%; min-width:1200px;">
                <colgroup>
                  <col style="width:150px;">
                  <col style="width:150px;">
                  <col style="width:120px;">
                  <col style="width:140px;">
                  <col style="width:150px;">
                  <col style="width:70px;">
                  <col style="width:100px;">
                </colgroup>
                <thead>
                  <tr>
                    <th>BANKA</th><th>SUBE</th><th>SERI</th><th>HESAP</th>
                    <th>BORCLU</th><th>TARIH</th><th class="double-column">TUTAR</th>
                  </tr>
                </thead>
                <tbody>
          `;

                    det.forEach(item => {
                        html += `
              <tr>
                <td>${item.BANKA || ""}</td>
                <td>${item.SUBE || ""}</td>
                <td>${item.SERI || ""}</td>
                <td>${item.HESAP || ""}</td>
                <td>${item.BORCLU || ""}</td>
                <td>${formatDate(item.TARIH)}</td>
                <td class="double-column">${formatNumber2(item.TUTAR)}</td>
              </tr>
            `;
                    });

                    html += `</tbody></table></div>`;

                    detailsTr.children[0].innerHTML = html;
                    detailsTr.dataset.loaded = "1";
                } catch (err) {
                    detailsTr.children[0].innerHTML =
                        `<div class="details-wrap"><b>Hata:</b> Detaylar alınamadı.</div>`;
                } finally {
                    document.body.style.cursor = "default";
                }
            });
        });

    } catch (err) {
        OBS.TAHRAP._showError(err?.message || String(err));
    } finally {
        OBS.TAHRAP._setBtn("tahrapyenileButton", false, "Filtre");
        document.body.style.cursor = "default";
    }
};

/* =========================
   MODAL OPEN (Bootstrap 5)
   ========================= */
OBS.TAHRAP.opentahrapModal = async function(modalSelectorOrEl) {
    OBS.TAHRAP._clearError();

    // modal göster (jQuery yok)
    try {
        const modalEl = (typeof modalSelectorOrEl === "string")
            ? document.querySelector(modalSelectorOrEl)
            : modalSelectorOrEl;

        if (modalEl && window.bootstrap?.Modal) {
            bootstrap.Modal.getOrCreateInstance(modalEl).show();
        }
    } catch (e) {}

    document.body.style.cursor = "wait";
    try {
        const result = await fetchWithSessionCheck("cari/tahsilatrappos", {
            method: "POST",
            headers: { "Content-Type": "application/json" }
        });

        if (result?.errorMessage) throw new Error(result.errorMessage);

        const data = result?.data || [];
        const posSelect = OBS.TAHRAP._el("pos");
        if (!posSelect) return;

        posSelect.innerHTML = "";

        const opt0 = document.createElement("option");
        opt0.value = "";
        opt0.textContent = "";
        posSelect.appendChild(opt0);

        if (data.length === 0) {
            OBS.TAHRAP._showError("Hiç veri bulunamadı.");
            return;
        }

        data.forEach(item => {
            const opt = document.createElement("option");
            opt.value = item.BANKA;
            opt.textContent = item.BANKA;
            posSelect.appendChild(opt);
        });

    } catch (err) {
        OBS.TAHRAP._showError(`Bir hata oluştu: ${err?.message || err}`);
    } finally {
        document.body.style.cursor = "default";
    }
};

OBS.TAHRAP._setDisabled = (id, disabled) => {
    const el = OBS.TAHRAP._el(id);
    if (!el) return;
    el.disabled = !!disabled;
};
/* =========================
   DOWNLOAD
   ========================= */
OBS.TAHRAP.tahrapdownloadReport = async function() {
    OBS.TAHRAP._clearError();

    document.body.style.cursor = "wait";
    OBS.TAHRAP._setDisabled("tahrapreportFormat", true);
    OBS.TAHRAP._setDisabled("tahrapmailAt", true);

    try {
        const rows = OBS.TAHRAP.extractTableData("main-table");

        const response = await fetchWithSessionCheckForDownload("cari/tahrap_download", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(rows),
        });

        if (!response?.blob) throw new Error("Dosya indirilemedi.");

        const disposition = response.headers.get("Content-Disposition") || "";
        const m = disposition.match(/filename="(.+)"/);
        const fileName = m ? m[1] : "tahrap_rapor.bin";

        const url = window.URL.createObjectURL(response.blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

    } catch (err) {
        OBS.TAHRAP._showError(err?.message || "Bilinmeyen bir hata oluştu.");
    } finally {
        OBS.TAHRAP._setDisabled("tahrapreportFormat", false);
        OBS.TAHRAP._setDisabled("tahrapmailAt", false);
        document.body.style.cursor = "default";
    }
};

/* =========================
   MAIL
   ========================= */
OBS.TAHRAP.tahrapmailAt = function() {
    document.body.style.cursor = "wait";

    localStorage.removeItem("tableData");
    localStorage.removeItem("grprapor");
    localStorage.removeItem("tablobaslik");

    const rows = OBS.TAHRAP.extractTableData("main-table");
    localStorage.setItem("tableData", JSON.stringify({ rows }));

    const url = `/send_email?degerler=${encodeURIComponent("tahrap")}`;
    mailsayfasiYukle(url);

    document.body.style.cursor = "default";
};

/* =========================
   TABLE EXTRACT (aynen)
   ========================= */
OBS.TAHRAP.extractTableData = function(tableId) {
    const table = document.querySelector(`#${tableId}`);
    if (!table) return [];

    const headers = [];
    const rows = [];

    table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));

    table.querySelectorAll("tbody tr").forEach(tr => {
        const rowData = {};
        let nonEmptyCount = 0;

        tr.querySelectorAll("td").forEach((td, index) => {
            const value = td.innerText.trim();
            if (value !== "") nonEmptyCount++;
            rowData[headers[index]] = value;
        });

        if (nonEmptyCount > 0) rows.push(rowData);
    });

    const tfoot = table.querySelector("tfoot");
    if (tfoot) {
        const tfootRowData = {};
        let nonEmptyCount = 0;

        tfoot.querySelectorAll("th").forEach((th, index) => {
            const value = th.innerText.trim();
            if (value !== "") nonEmptyCount++;
            tfootRowData[headers[index]] = value;
        });

        if (nonEmptyCount > 0) rows.push(tfootRowData);
    }

    return rows;
};
