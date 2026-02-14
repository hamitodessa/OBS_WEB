/* =========================
   OBS – Scheduler (2026 CAM)
   Namespace: OBS.SCHEDULER
   ========================= */

window.OBS = window.OBS || {};
OBS.SCHEDULER = OBS.SCHEDULER || {};

(function() {
    // ---- config ----
    const CFG = {
        rootId: "camScheduler",
        apiBase: "/gunluk/scheduler",
        hoursFrom: 6,
        hoursTo: 23,          // dahil
        weekStartsMonday: true
    };

    // ---- state ----
    OBS.SCHEDULER.state = OBS.SCHEDULER.state || {
        anchorDate: new Date(),
        selectedCell: null,
        lastRoot: null,
        abortCtrl: null
    };

    // ---- public init ----
    OBS.SCHEDULER.init = async function initScheduler() {
        const root = document.getElementById(CFG.rootId);
        if (!root) return;
        if (OBS.SCHEDULER.state.lastRoot === root && root.isConnected) return;

        if (OBS.SCHEDULER.state.abortCtrl) {
            OBS.SCHEDULER.state.abortCtrl.abort();
        }
        OBS.SCHEDULER.state.abortCtrl = new AbortController();
        OBS.SCHEDULER.state.lastRoot = root;

        OBS.SCHEDULER.els = mapEls(root);

        bindEvents(OBS.SCHEDULER.state.abortCtrl.signal);

        renderGridSkeleton();
        await loadCounts();
    };

    // ---- dom ----
    function mapEls(root) {
        return {
            root,
            title: root.querySelector("#camSch_title"),
            grid: root.querySelector("#camSch_grid"),
            sideTitle: root.querySelector("#camSch_sideTitle"),
            taskList: root.querySelector("#camSch_taskList"),
            viewMode: root.querySelector("#camSch_viewMode"),
            btnRefresh: root.querySelector("#camSch_refresh"),
            btnPrev: root.querySelector("#camSch_prev"),
            btnNext: root.querySelector("#camSch_next"),
            detailTitle: root.querySelector("#camSch_detailTitle"),
            detailTable: root.querySelector("#camSch_detailTable")
        };
    }

    function bindEvents(signal) {
        const { btnRefresh, btnPrev, btnNext, viewMode } = OBS.SCHEDULER.els;

        btnRefresh.addEventListener("click", refreshNow, { signal });
        btnPrev.addEventListener("click", () => move(viewMode.value === "day" ? -1 : -7), { signal });
        btnNext.addEventListener("click", () => move(viewMode.value === "day" ? +1 : +7), { signal });

        viewMode.addEventListener("change", async () => {
            renderGridSkeleton();
            await loadCounts();
        }, { signal });
    }


    // ---- utils ----
    function pad2(n) { return String(n).padStart(2, "0"); }

    function isoDate(d) {
        const x = new Date(d);
        return `${x.getFullYear()}-${pad2(x.getMonth() + 1)}-${pad2(x.getDate())}`;
    }

    function addDays(d, n) {
        const x = new Date(d);
        x.setDate(x.getDate() + n);
        return x;
    }

    function startOfWeek(d) {
        const x = new Date(d);
        x.setHours(0, 0, 0, 0);

        const dow = x.getDay(); // Sun=0
        const mondayBased = (dow + 6) % 7; // Mon=0
        const offset = CFG.weekStartsMonday ? mondayBased : dow;

        x.setDate(x.getDate() - offset);
        return x;
    }

    function fmtHeader(d) {
        return new Date(d).toLocaleDateString("tr-TR", {
            weekday: "long", day: "2-digit", month: "2-digit", year: "numeric"
        });
    }

    function fmtDay(d) {
        return new Date(d).toLocaleDateString("tr-TR", { day: "2-digit", month: "2-digit" });
    }

    function dayName(d) {
        return new Date(d).toLocaleDateString("tr-TR", { weekday: "short" });
    }

    function escapeHtml(s) {
        return String(s ?? "").replace(/[&<>"']/g, m => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
        }[m]));
    }

    function hoursArray() {
        const out = [];
        for (let h = CFG.hoursFrom;h <= CFG.hoursTo;h++) out.push(`${pad2(h)}:00`);
        return out;
    }

    function getRange() {
        const mode = OBS.SCHEDULER.els.viewMode.value;
        const anchor = OBS.SCHEDULER.state.anchorDate;

        if (mode === "day") {
            const s = new Date(anchor); s.setHours(0, 0, 0, 0);
            return { start: s, end: s, days: [s] };
        } else {
            const s = startOfWeek(anchor);
            const days = Array.from({ length: 7 }, (_, i) => addDays(s, i));
            const e = addDays(s, 6);
            return { start: s, end: e, days };
        }
    }

    // ---- render grid ----
    function renderGridSkeleton() {
        const { grid, title, sideTitle, taskList } = OBS.SCHEDULER.els;
        const { start, end, days } = getRange();
        const hrs = hoursArray();

        title.textContent = (OBS.SCHEDULER.els.viewMode.value === "day")
            ? fmtHeader(start)
            : `${fmtDay(start)} – ${fmtDay(end)} (Hafta)`;

        // columns: time + day count
        grid.style.gridTemplateColumns = `78px repeat(${days.length}, 1fr)`;
        grid.innerHTML = "";

        // header row
        grid.appendChild(hcell("Saat"));
        for (const d of days) grid.appendChild(hcell(`${fmtDay(d)} ${dayName(d)}`));

        // body
        for (const h of hrs) {
            grid.appendChild(tcell(h));
            for (const d of days) {
                const cell = document.createElement("div");
                cell.className = "cell";
                cell.dataset.date = isoDate(d);
                cell.dataset.time = h;
                cell.innerHTML = `<span class="badge">0</span>`;
                cell.addEventListener("click", onCellClick);
                grid.appendChild(cell);
            }
        }

        // reset selection + side
        if (OBS.SCHEDULER.state.selectedCell) {
            OBS.SCHEDULER.state.selectedCell.classList.remove("selected");
            OBS.SCHEDULER.state.selectedCell = null;
        }
        sideTitle.textContent = "Görevler";
        taskList.innerHTML = `<div class="cam-muted">Bir hücre seç…</div>`;

        resetDetail();
    }

    function hcell(txt) {
        const d = document.createElement("div");
        d.className = "hcell";
        d.textContent = txt;
        return d;
    }

    function tcell(txt) {
        const d = document.createElement("div");
        d.className = "tcell";
        d.textContent = txt;
        return d;
    }

    // ---- load counts ----
    async function loadCounts() {
        const { grid } = OBS.SCHEDULER.els;
        const { start, end } = getRange();

        try {
            clearError();
            setBusy(true);

            // tek reset (badge + cell.has)
            grid.querySelectorAll(".cell").forEach(c => c.classList.remove("has"));
            grid.querySelectorAll(".cell .badge").forEach(b => {
                b.textContent = "0";
                b.classList.remove("has");
            });

            const data = await fetchWithSessionCheck("gunluk/gunlukoku", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ start: start, end: end }),
            });

            if (data?.errorMessage) throw new Error(data.errorMessage);

            const rows = data.data || [];

            for (const r of rows) {
                const cell = grid.querySelector(`.cell[data-date="${r.tarih}"][data-time="${r.saat}"]`);
                if (!cell) continue;

                const b = cell.querySelector(".badge");
                const adet = Number(r.adet) || 0;

                b.textContent = String(adet);

                const has = adet > 0;              // ✅ TANIMLI
                b.classList.toggle("has", has);
                cell.classList.toggle("has", has); // ✅ artık çalışır
            }

        } catch (ex) {
            showError(ex.message);
        } finally {
            setBusy(false);
        }
    }


    // ---- cell click -> load tasks ----
    async function onCellClick(e) {
        const cell = e.currentTarget;
        const { sideTitle, taskList } = OBS.SCHEDULER.els;

        if (OBS.SCHEDULER.state.selectedCell) OBS.SCHEDULER.state.selectedCell.classList.remove("selected");
        OBS.SCHEDULER.state.selectedCell = cell;
        cell.classList.add("selected");

        const tarih = cell.dataset.date;
        const saat = cell.dataset.time;

        sideTitle.textContent = `${tarih} / ${saat}`;
        taskList.innerHTML = `<div class="cam-muted">Yükleniyor…</div>`;

        try {
            clearError();
            setBusy(true);

            const data = await fetchWithSessionCheck("gunluk/isimoku", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ tarih: tarih, saat: saat }),
            });

            if (data?.errorMessage) throw new Error(data.errorMessage);

            const list = data.data || [];

            if (!list.length) {
                taskList.innerHTML = `<div class="cam-muted">Bu saatte görev yok.</div>`;
                resetDetail();
                return;
            }

            /*
            taskList.innerHTML = list.map(x => `
          		<div class="task clickable" data-gid="${x.gid ?? ""}">
            		<div class="t1">#${x.gid ?? ""} • ${escapeHtml(x.isim)} • ${escapeHtml(x.gorev)}</div>
            		<div class="t2">${escapeHtml(x.yer)}${x.mesaj ? " • " + escapeHtml(x.mesaj) : ""}</div>
          		</div>
        	`).join("");
        	*/
            taskList.innerHTML = list.map(x => `
			      <div class="task clickable" data-gid="${x.gid ?? ""}">
			        <div class="t1">${escapeHtml(x.isim)} • ${escapeHtml(x.gorev)}</div>
			        <div class="t2">${escapeHtml(x.yer)}${x.mesaj ? " • " + escapeHtml(x.mesaj) : ""}</div>
			      </div>
			    `).join("");

            const cards = taskList.querySelectorAll(".task.clickable");
            cards.forEach((c) => {
                c.addEventListener("click", () => {
                    cards.forEach(z => z.classList.remove("active"));
                    c.classList.add("active");

                    const gid = c.dataset.gid;
                    const item = list.find(a => String(a.gid ?? "") === String(gid));
                    if (item) renderDetail(item);
                });
            });

            cards[0]?.click();

        } catch (ex) {
            showError(ex.message);
            taskList.innerHTML = `<div class="cam-muted">Hata oluştu.</div>`;
            resetDetail();
        } finally {
            setBusy(false);
        }
    }

    // ---- buttons ----
    async function refreshNow() {
        OBS.SCHEDULER.state.anchorDate = new Date();
        renderGridSkeleton();
        await loadCounts();
    }

    async function move(stepDays) {
        OBS.SCHEDULER.state.anchorDate = addDays(OBS.SCHEDULER.state.anchorDate, stepDays);
        renderGridSkeleton();
        await loadCounts();
    }

    function resetDetail() {
        const { detailTitle, detailTable } = OBS.SCHEDULER.els || {};
        if (!detailTitle || !detailTable) return;
        detailTitle.textContent = "Detay";
        const tds = detailTable.querySelectorAll("td");
        tds.forEach(td => { td.textContent = "—"; td.classList.add("cam-muted"); });
    }

    function renderDetail(x) {
        const { detailTitle, detailTable } = OBS.SCHEDULER.els || {};
        if (!detailTitle || !detailTable) return;
        //detailTitle.textContent = `Detay • #${x.gid ?? ""}`;
        detailTitle.textContent = `Detay • ${x.isim ?? ""}`;
        const rows = [
            //["GID", x.gid],
            ["Tarih", x.tarih],
            ["Saat", x.saat],
            ["İsim", x.isim],
            ["Görev", x.gorev],
            ["Yer", x.yer],
            ["Mesaj", x.mesaj]
        ];
        const tbody = detailTable.querySelector("tbody");
        tbody.innerHTML = rows.map(([k, v]) => `
      <tr>
        <th>${escapeHtml(k)}</th>
        <td>${v ? escapeHtml(v) : `<span class="cam-muted">—</span>`}</td>
      </tr>
    `).join("");
    }

    function setBusy(on) {
        const { root } = OBS.SCHEDULER.els || {};
        if (!root) return;

        // root relative olmalı ki overlay otursun
        if (!root.style.position) root.style.position = "relative";

        // overlay yoksa ekle
        let ov = root.querySelector(".cam-busy");
        if (!ov) {
            ov = document.createElement("div");
            ov.className = "cam-busy";
            ov.innerHTML = `<div class="cam-spinner">Yükleniyor…</div>`;
            root.appendChild(ov);
        }

        root.classList.toggle("busy", !!on);
    }

    function showError(msg) {
        const d = document.getElementById("errorDiv");
        if (!d) return;
        d.innerHTML = `
	    <div class="cam-alert">
	      <div class="a-title">Hata</div>
	      <div class="a-msg">${escapeHtml(msg || "Beklenmeyen hata")}</div>
	    </div>
	  `;
    }

    function clearError() {
        const d = document.getElementById("errorDiv");
        if (!d) return;
        d.innerHTML = "";
    }

})();
