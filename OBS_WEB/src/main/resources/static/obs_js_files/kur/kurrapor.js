window.OBS = window.OBS || {};
OBS.KURRAPOR = OBS.KURRAPOR || {};

(function(M) {
    M.byId = (id) => document.getElementById(id);

    M._showError = function(msg) {
        const e = M.byId("errorDiv");
        if (!e) return;
        e.style.display = "block";
        e.innerText = msg || "Beklenmeyen hata.";
    };

    M._clearError = function() {
        const e = M.byId("errorDiv");
        if (!e) return;
        e.style.display = "none";
        e.innerText = "";
    };

    M._setCursorWait = () => requestAnimationFrame(() => {
        document.body.style.cursor = "wait";
    });

    M._setCursorDefault = () => requestAnimationFrame(() => {
        document.body.style.cursor = "default";
    });

    M._setButtonBusy = function(busy) {
        const btn = M.byId("yenileButton");
        if (!btn) return;
        btn.disabled = !!busy;
        btn.innerText = busy ? "İşleniyor..." : "Yenile";
    };

    M._readParams = function() {
        const raw = M.byId("kurraporBilgi")?.value || "";
        const p = raw.split(",");
        return {
            startDate: p[0] || "",
            endDate: p[1] || "",
            cins1: p[2] || "",
            cins2: p[3] || ""
        };
    };

    M._clearTable = function() {
        const tb = M.byId("tableBody");
        if (tb) tb.innerHTML = "";
    };

    M._appendRow = function(tb, item) {
        const row = document.createElement("tr");
        row.classList.add("table-row-height");
        row.innerHTML = `
      <td>${formatDate(item.Tarih)}</td>
      <td>${item.Kur || ""}</td>
      <td class="double-column">${formatNumber4(item.MA)}</td>
      <td class="double-column">${formatNumber4(item.MS)}</td>
      <td class="double-column">${formatNumber4(item.SA)}</td>
      <td class="double-column">${formatNumber4(item.SS)}</td>
      <td class="double-column">${formatNumber4(item.BA)}</td>
      <td class="double-column">${formatNumber4(item.BS)}</td>
    `;
        tb.appendChild(row);
    };

    M.fetchTableData = async function() {
        const dto = M._readParams();

        M._clearError();
        M._clearTable();
        M._setCursorWait();
        M._setButtonBusy(true);

        try {
            const data = await fetchWithSessionCheck("kur/kurrapor", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dto)
            });

            if (data?.errorMessage) throw new Error(data.errorMessage);

            if (data?.success && Array.isArray(data.data)) {
                const tb = M.byId("tableBody");
                if (!tb) return;
                data.data.forEach(item => M._appendRow(tb, item));
            } else {
                M._showError(data?.errorMessage || "Bir hata oluştu.");
            }
        } catch (err) {
            M._showError(err?.message || "Beklenmeyen hata.");
        } finally {
            M._setCursorDefault();
            M._setButtonBusy(false);
        }
    };

})(OBS.KURRAPOR);
