/* =========================
   OBS – Scheduler (2026 CAM)
   Namespace: OBS.SCHEDULER
   ========================= */

window.OBS = window.OBS || {};
OBS.SCHEDULER = OBS.SCHEDULER || {};

(function () {
  // ---- config ----
  const CFG = {
    rootId: "camScheduler",
    apiBase: "/api/scheduler",
    hoursFrom: 6,
    hoursTo: 23,          // dahil
    weekStartsMonday: true
  };

  // ---- state ----
  OBS.SCHEDULER.state = OBS.SCHEDULER.state || {
    anchorDate: new Date(),
    selectedCell: null,
    inited: false
  };

  // ---- public init ----
  OBS.SCHEDULER.init = async function initScheduler() {
    const root = document.getElementById(CFG.rootId);
    if (!root) return;

    // aynı sayfa tekrar load olursa iki kez bağlanmasın
    if (OBS.SCHEDULER.state.inited) return;
    OBS.SCHEDULER.state.inited = true;

    OBS.SCHEDULER.els = mapEls(root);

    bindEvents();

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
      btnNext: root.querySelector("#camSch_next")
    };
  }

  function bindEvents() {
    const { btnRefresh, btnPrev, btnNext, viewMode } = OBS.SCHEDULER.els;

    btnRefresh.addEventListener("click", refreshNow);
    btnPrev.addEventListener("click", () => move(viewMode.value === "day" ? -1 : -7));
    btnNext.addEventListener("click", () => move(viewMode.value === "day" ? +1 : +7));

    viewMode.addEventListener("change", async () => {
      renderGridSkeleton();
      await loadCounts();
    });
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
    for (let h = CFG.hoursFrom; h <= CFG.hoursTo; h++) out.push(`${pad2(h)}:00`);
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

    const url = `${CFG.apiBase}/counts?start=${isoDate(start)}&end=${isoDate(end)}`;
    const res = await fetch(url);
    if (!res.ok) return;

    const rows = await res.json(); // [{tarih,saat,adet}] senin backend formatına göre aynı isimler olmalı

    // reset all to 0
    grid.querySelectorAll(".cell .badge").forEach(b => {
      b.textContent = "0";
      b.classList.remove("has");
    });

    // apply
    for (const r of rows) {
      const q = `.cell[data-date="${r.tarih}"][data-time="${r.saat}"]`;
      const cell = grid.querySelector(q);
      if (!cell) continue;
      const b = cell.querySelector(".badge");
      b.textContent = String(r.adet);
      if (Number(r.adet) > 0) b.classList.add("has");
    }
  }

  // ---- cell click -> load tasks ----
  async function onCellClick(e) {
    const cell = e.currentTarget;
    const { sideTitle, taskList } = OBS.SCHEDULER.els;

    // select ui
    if (OBS.SCHEDULER.state.selectedCell) OBS.SCHEDULER.state.selectedCell.classList.remove("selected");
    OBS.SCHEDULER.state.selectedCell = cell;
    cell.classList.add("selected");

    const tarih = cell.dataset.date;
    const saat = cell.dataset.time;

    sideTitle.textContent = `${tarih} / ${saat}`;
    taskList.innerHTML = `<div class="cam-muted">Yükleniyor…</div>`;

    const url = `${CFG.apiBase}/tasks?tarih=${tarih}&saat=${encodeURIComponent(saat)}`;
    const res = await fetch(url);
    if (!res.ok) {
      taskList.innerHTML = `<div class="cam-muted">Görevler alınamadı</div>`;
      return;
    }

    const list = await res.json();

    if (!list || !list.length) {
      taskList.innerHTML = `<div class="cam-muted">Bu saatte görev yok.</div>`;
      return;
    }

    taskList.innerHTML = list.map(x => `
      <div class="task">
        <div class="t1">#${x.gid ?? ""} • ${escapeHtml(x.isim)} • ${escapeHtml(x.gorev)}</div>
        <div class="t2">${escapeHtml(x.yer)}${x.mesaj ? " • " + escapeHtml(x.mesaj) : ""}</div>
      </div>
    `).join("");
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

})();
