/* =========================
   OBS – Scheduler (2026 CAM)
   Day / Week / Month (unified)
   Namespace: OBS.SCHEDULER
   ========================= */

window.OBS = window.OBS || {};
OBS.SCHEDULER = OBS.SCHEDULER || {};

(function () {
  // ---- config ----
  const CFG = {
    rootId: "camScheduler",
    apiBase: "/gunluk/scheduler",

    // mevcutlar
    hoursFrom: 6,
    hoursTo: 23,
    weekStartsMonday: true,

    // ✅ yeni endpoint isimleri
    epCountsDayWeek: "gunluk/gunlukoku",       // {start,end} -> saat bazlı
    epCountsMonth:  "gunluk/gunlukoku_ay",     // {ay:"YYYY-MM"} -> gün bazlı
    epTasksCell:    "gunluk/isimoku",          // {tarih,saat}
    epTasksDay:     "gunluk/isimoku_gun"       // {tarih}
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

    if (OBS.SCHEDULER.state.abortCtrl) OBS.SCHEDULER.state.abortCtrl.abort();
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

    btnPrev.addEventListener("click", async () => {
      const mode = viewMode.value;
      if (mode === "day") await moveDays(-1);
      else if (mode === "week") await moveDays(-7);
      else await moveMonths(-1);
    }, { signal });

    btnNext.addEventListener("click", async () => {
      const mode = viewMode.value;
      if (mode === "day") await moveDays(+1);
      else if (mode === "week") await moveDays(+7);
      else await moveMonths(+1);
    }, { signal });

    viewMode.addEventListener("change", async () => {
      renderGridSkeleton();
      await loadCounts();
    }, { signal });
  }

  // ---- utils ----
  function pad2(n) { return String(n).padStart(2, "0"); }

  function isoDate(d) {
    const x = new Date(d);
    x.setHours(0, 0, 0, 0);
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

  function startOfMonth(d) {
    const x = new Date(d);
    x.setHours(0, 0, 0, 0);
    x.setDate(1);
    return x;
  }

  function addMonthsSafe(d, n) {
    // "aynı gün" taşarken ay sonuna patlamasın
    const x = new Date(d);
    const day = x.getDate();
    x.setDate(1);
    x.setMonth(x.getMonth() + n);
    const last = new Date(x.getFullYear(), x.getMonth() + 1, 0).getDate();
    x.setDate(Math.min(day, last));
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

  function fmtMonthTitle(d) {
    return new Date(d).toLocaleDateString("tr-TR", { month: "long", year: "numeric" });
  }

  function escapeHtml(s) {
    return String(s ?? "").replace(/[&<>"']/g, m => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[m]));
  }

  function hoursArray() {
    const out = [];
    for (let h = CFG.hoursFrom; h <= CFG.hoursTo; h++) out.push(`${pad2(h)}:00`);
    return out;
  }

  function getRange() {
    const mode = OBS.SCHEDULER.els.viewMode.value;
    const anchor = OBS.SCHEDULER.state.anchorDate;

    if (mode === "day") {
      const s = new Date(anchor); s.setHours(0, 0, 0, 0);
      return { mode, start: s, end: s, days: [s] };
    }

    if (mode === "week") {
      const s = startOfWeek(anchor);
      const days = Array.from({ length: 7 }, (_, i) => addDays(s, i));
      const e = addDays(s, 6);
      return { mode, start: s, end: e, days };
    }

    // month
    const m1 = startOfMonth(anchor);
    const gridStart = startOfWeek(m1);          // ayın 1'ini içeren haftanın başı
    const gridEnd = addDays(gridStart, 41);     // 6 hafta * 7 = 42 gün
    const days = Array.from({ length: 42 }, (_, i) => addDays(gridStart, i));
    return { mode, start: gridStart, end: gridEnd, days, monthRef: m1 };
  }

  // ---- render grid ----
  function renderGridSkeleton() {
    const { grid, title, sideTitle, taskList } = OBS.SCHEDULER.els;
    const R = getRange();

    grid.innerHTML = "";
    grid.classList.remove("month");

    if (R.mode === "day") {
      title.textContent = fmtHeader(R.start);
      renderDayWeekGrid(R.days);
    }
    else if (R.mode === "week") {
      title.textContent = `${fmtDay(R.start)} – ${fmtDay(R.end)} (Hafta)`;
      renderDayWeekGrid(R.days);
    }
    else {
      title.textContent = `${fmtMonthTitle(OBS.SCHEDULER.state.anchorDate)} (Ay)`;
      renderMonthGrid(R.days, R.monthRef);
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

  function renderDayWeekGrid(days) {
    const { grid } = OBS.SCHEDULER.els;
    const hrs = hoursArray();

    // columns: time + day count
    grid.style.gridTemplateColumns = `78px repeat(${days.length}, 1fr)`;

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
  }

  function renderMonthGrid(days42, monthRef) {
    const { grid } = OBS.SCHEDULER.els;

    grid.classList.add("month");
    grid.style.gridTemplateColumns = `repeat(7, 1fr)`;

    // header row (7 gün)
    const start = days42[0];
    for (let i = 0; i < 7; i++) {
      const d = addDays(start, i);
      grid.appendChild(hcell(dayName(d)));
    }

    const m = monthRef.getMonth();
    const y = monthRef.getFullYear();

    for (const d of days42) {
      const cell = document.createElement("div");
      const inMonth = (d.getMonth() === m && d.getFullYear() === y);

      cell.className = "mcell" + (inMonth ? "" : " out");
      cell.dataset.date = isoDate(d);
      cell.dataset.time = "00:00";   // backend saat istiyor diye sabit; month tık = gün detayı
      cell.innerHTML = `
        <div class="m-top">
          <div class="m-day">${d.getDate()}</div>
          <span class="badge">0</span>
        </div>
      `;
      cell.addEventListener("click", onCellClick);
      grid.appendChild(cell);
    }
  }

  // ---- load counts ----
  async function loadCounts() {
    const { grid } = OBS.SCHEDULER.els;
    const mode = OBS.SCHEDULER.els.viewMode.value;
    const R = getRange();

    try {
      clearError();
      setBusy(true);

      // reset
      grid.querySelectorAll(".cell, .mcell").forEach(c => c.classList.remove("has"));
      grid.querySelectorAll(".badge").forEach(b => {
        b.textContent = "0";
        b.classList.remove("has");
      });

      // ✅ MONTH: gün bazlı sayılar
      if (mode === "month") {
        const ay = isoMonth(OBS.SCHEDULER.state.anchorDate);

        const data = await fetchWithSessionCheck(CFG.epCountsMonth, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ ay })   // { "ay":"2026-02" }
        });

        if (data?.errorMessage) throw new Error(data.errorMessage);

        const rows = data.data || [];
        for (const r of rows) {
          const cell = grid.querySelector(`.mcell[data-date="${r.tarih}"]`);
          if (!cell) continue;

          const b = cell.querySelector(".badge");
          const adet = Number(r.adet) || 0;
          b.textContent = String(adet);

          const has = adet > 0;
          b.classList.toggle("has", has);
          cell.classList.toggle("has", has);
        }
        return;
      }

      // ✅ DAY/WEEK: saat bazlı sayılar (mevcut gibi)
      const data = await fetchWithSessionCheck(CFG.epCountsDayWeek, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ start: R.start, end: R.end })
      });

      if (data?.errorMessage) throw new Error(data.errorMessage);

      const rows = data.data || [];
      for (const r of rows) {
        const cell = grid.querySelector(`.cell[data-date="${r.tarih}"][data-time="${r.saat}"]`);
        if (!cell) continue;

        const b = cell.querySelector(".badge");
        const adet = Number(r.adet) || 0;
        b.textContent = String(adet);

        const has = adet > 0;
        b.classList.toggle("has", has);
        cell.classList.toggle("has", has);
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
    const { sideTitle, taskList, viewMode } = OBS.SCHEDULER.els;

    // selection
    if (OBS.SCHEDULER.state.selectedCell)
      OBS.SCHEDULER.state.selectedCell.classList.remove("selected");

    OBS.SCHEDULER.state.selectedCell = cell;
    cell.classList.add("selected");

    const mode = viewMode.value;

    const tarih = cell.dataset.date;
    const saat  = cell.dataset.time;

    // UI header
    taskList.innerHTML = `<div class="cam-muted">Yükleniyor…</div>`;
    if (mode === "month") {
      sideTitle.textContent = `${tarih} (Gün)`;
      resetDetail();
    } else {
      sideTitle.textContent = `${tarih} / ${saat}`;
    }

    // endpoint + payload
    const endpoint = (mode === "month") ? "gunluk/isimoku_gun" : "gunluk/isimoku";
    const payload  = (mode === "month") ? { tarih } : { tarih, saat };

    try {
      clearError();
      setBusy(true);

      const data = await fetchWithSessionCheck(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (data?.errorMessage) throw new Error(data.errorMessage);

      const list = data.data || [];

      if (!list.length) {
        taskList.innerHTML = `<div class="cam-muted">Görev yok.</div>`;
        resetDetail();
        return;
      }

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

      // ilkini otomatik seç
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

  async function moveDays(stepDays) {
    OBS.SCHEDULER.state.anchorDate = addDays(OBS.SCHEDULER.state.anchorDate, stepDays);
    renderGridSkeleton();
    await loadCounts();
  }

  async function moveMonths(stepMonths) {
    OBS.SCHEDULER.state.anchorDate = addMonthsSafe(OBS.SCHEDULER.state.anchorDate, stepMonths);
    renderGridSkeleton();
    await loadCounts();
  }

  // ---- detail ----
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

    detailTitle.textContent = `Detay • ${x.isim ?? ""}`;

    const rows = [
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

  // ---- busy / error ----
  function setBusy(on) {
    const { root } = OBS.SCHEDULER.els || {};
    if (!root) return;

    if (!root.style.position) root.style.position = "relative";

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

  function isoMonth(d){
    const x = new Date(d);
    return `${x.getFullYear()}-${pad2(x.getMonth()+1)}`; // "2026-02"
  }
  
})();
