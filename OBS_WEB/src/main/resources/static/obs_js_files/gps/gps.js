fileselect();

async function fileselect() {
    document.body.style.cursor = "wait";
    const sel = document.getElementById("fileSelect");
    sel.innerHTML = "";

    try {
        const files = await fetchWithSessionCheck("gps/files");

        files.forEach(file => {
            const opt = document.createElement("option");
            opt.value = file;
            opt.text = file;
            sel.appendChild(opt);
        });

        // varsa ilk dosyayı seçince otomatik gösterme istersen:
        // if (files.length) sel.value = files[0];

    } catch (error) {
        alert("📁 Dosya listesi alınamadı 😢\n\n" + error.message);
    } finally {
        document.body.style.cursor = "default";
    }
}

/* ==========================
   GPS MAP PLAYER – UPGRADE
   ========================== */
let currentIndex = 0;
let coords = [];

let map;
let polyline;
let marker;
let markerHalo;

let osm, satellite, dark;
let layerGroupTiles;

let playTimer = null;
let playSpeedMs = 800; // default hız

// UI elementleri (varsa bağlar)
const UI = {
    info: () => document.getElementById("gpsInfo"),         // <div id="gpsInfo"></div>
    slider: () => document.getElementById("gpsSlider"),     // <input id="gpsSlider" type="range">
    idx: () => document.getElementById("gpsIndex"),         // <span id="gpsIndex"></span>
    total: () => document.getElementById("gpsTotal"),       // <span id="gpsTotal"></span>
    speed: () => document.getElementById("gpsSpeed"),       // <span id="gpsSpeed"></span>
    btnPlay: () => document.getElementById("btnPlay"),      // <button id="btnPlay">
    btnPause: () => document.getElementById("btnPause"),    // <button id="btnPause">
};

function initMapLayers() {
    osm = L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: "© OpenStreetMap"
    });

    // Not: Google tile policy konusu var; çalışıyorsa devam, yoksa uydu için Esri vs kullanırız.
    satellite = L.tileLayer("https://{s}.google.com/vt/lyrs=s&x={x}&y={y}&z={z}", {
        maxZoom: 20,
        subdomains: ["mt0", "mt1", "mt2", "mt3"],
        attribution: "© Google"
    });

    dark = L.tileLayer("https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png", {
        attribution: "© Carto",
        subdomains: "abcd",
        maxZoom: 19
    });

    layerGroupTiles = {
        osm,
        satellite,
        dark
    };
}

async function goster() {
    if (!osm) initMapLayers();

    const file = document.getElementById("fileSelect").value;
    const start = document.getElementById("startDate").value;
    const end = document.getElementById("endDate").value;

    document.body.style.cursor = "wait";

    try {
        const response = await fetchWithSessionCheck(
            `gps/data?file=${encodeURIComponent(file)}&start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}`
        );

        coords = Array.isArray(response) ? response : [];
        currentIndex = 0;

        stopPlay();

        if (!coords.length) {
            alert("Seçilen tarih aralığında konum verisi bulunamadı tatlım 😢");
            return;
        }

        normalizeCoords(coords);

        drawMap(coords);
        setupPlayerUI(coords.length);
        updateMarker(currentIndex, true);

    } catch (error) {
        alert("Veri alınırken hata oluştu 😢\n\n" + error.message);
    } finally {
        document.body.style.cursor = "default";
    }
}

/* ---- veriyi sağlamlaştır ---- */
function normalizeCoords(list) {
    // lat/lng string gelirse number’a çevir
    for (const c of list) {
        c.lat = Number(c.lat);
        c.lng = Number(c.lng);
        // time alanı yoksa fallback
        if (!c.time) c.time = "";
    }
}

/* ---- map çiz ---- */
function drawMap(coords) {
    if (map) {
        map.off();
        map.remove();
        map = null;
    }

    map = L.map("map", {
        center: [coords[0].lat, coords[0].lng],
        zoom: 15,
        layers: [osm],
        zoomControl: true
    });

    const latlngs = coords.map(c => [c.lat, c.lng]);

    polyline = L.polyline(latlngs, {
        color: "#38bdf8",
        weight: 4,
        opacity: 0.9,
        smoothFactor: 1
    }).addTo(map);

    // rota içine padding’li sığdır
    map.fitBounds(polyline.getBounds(), { padding: [24, 24] });
}

/* ---- marker + info ---- */
function updateMarker(index, fly = false) {
    const c = coords[index];
    if (!c || !map) return;

    // eski markerları kaldır
    if (marker) map.removeLayer(marker);
    if (markerHalo) map.removeLayer(markerHalo);

    // halo (sexy)
    markerHalo = L.circleMarker([c.lat, c.lng], {
        radius: 14,
        weight: 2,
        opacity: 0.8,
        fillOpacity: 0.15
    }).addTo(map);

    // marker icon (daha şık)
    const icon = L.divIcon({
        className: "gps-pin",
        html: `<div class="gps-pin-dot"></div>`,
        iconSize: [18, 18],
        iconAnchor: [9, 9]
    });

    marker = L.marker([c.lat, c.lng], { icon }).addTo(map);

    // istersen popup KAPALI olsun, info panel kullanalım
    // marker.bindPopup(c.time).openPopup();

    if (fly) map.flyTo([c.lat, c.lng], Math.max(map.getZoom(), 15), { duration: 0.6 });

    updateInfoPanel(index);
    updateSlider(index);
}

/* ---- info panel ---- */
function updateInfoPanel(index) {
    const el = UI.info();
    if (!el) return;

    const c = coords[index];
    const prev = coords[index - 1];

    let distM = 0;
    let speedKmh = 0;

    if (prev) {
        distM = haversineMeters(prev.lat, prev.lng, c.lat, c.lng);
        // time parse edebilirsek hız çıkar
        const dtSec = timeDiffSeconds(prev.time, c.time);
        if (dtSec > 0) speedKmh = (distM / dtSec) * 3.6;
    }

    el.innerHTML = `
    <div class="gps-info-row">
      <div class="gps-k">Zaman</div><div class="gps-v">${escapeHtml(c.time)}</div>
    </div>
    <div class="gps-info-row">
      <div class="gps-k">Koordinat</div><div class="gps-v">${c.lat.toFixed(6)}, ${c.lng.toFixed(6)}</div>
    </div>
    <div class="gps-info-row">
      <div class="gps-k">Adım</div><div class="gps-v">${index + 1} / ${coords.length}</div>
    </div>
    <div class="gps-info-row">
      <div class="gps-k">Mesafe</div><div class="gps-v">${distM ? distM.toFixed(0) + " m" : "-"}</div>
    </div>
    <div class="gps-info-row">
      <div class="gps-k">Hız</div><div class="gps-v">${speedKmh ? speedKmh.toFixed(1) + " km/h" : "-"}</div>
    </div>
  `;

    const idxEl = UI.idx();
    const totalEl = UI.total();
    if (idxEl) idxEl.textContent = String(index + 1);
    if (totalEl) totalEl.textContent = String(coords.length);

    const sp = UI.speed();
    if (sp) sp.textContent = `${playSpeedMs} ms`;
}

/* ---- slider ---- */
function setupPlayerUI(total) {
    const sl = UI.slider();
    if (sl) {
        sl.min = "0";
        sl.max = String(Math.max(0, total - 1));
        sl.value = "0";
        sl.oninput = () => {
            stopPlay();
            currentIndex = Number(sl.value);
            updateMarker(currentIndex, false);
        };
    }
}

function updateSlider(index) {
    const sl = UI.slider();
    if (sl) sl.value = String(index);
}

/* ---- ileri/geri ---- */
function ileri() {
    if (!coords.length) return;
    if (currentIndex < coords.length - 1) {
        currentIndex++;
        updateMarker(currentIndex, true);
    }
}

function geri() {
    if (!coords.length) return;
    if (currentIndex > 0) {
        currentIndex--;
        updateMarker(currentIndex, true);
    }
}

/* ---- play/pause ---- */
function play() {
    if (!coords.length) return;
    if (playTimer) return;

    playTimer = setInterval(() => {
        if (currentIndex >= coords.length - 1) {
            stopPlay();
            return;
        }
        currentIndex++;
        updateMarker(currentIndex, false);
    }, playSpeedMs);

    togglePlayButtons(true);
}

function stopPlay() {
    if (playTimer) {
        clearInterval(playTimer);
        playTimer = null;
    }
    togglePlayButtons(false);
}

function togglePlayButtons(isPlaying) {
    const bPlay = UI.btnPlay();
    const bPause = UI.btnPause();
    if (bPlay) bPlay.disabled = !!isPlaying;
    if (bPause) bPause.disabled = !isPlaying;
}

/* ---- hız ayarı (ör: select ile çağır) ---- */
function setPlaySpeed(ms) {
    playSpeedMs = Math.max(120, Number(ms) || 800);
    const sp = UI.speed();
    if (sp) sp.textContent = `${playSpeedMs} ms`;

    // oynuyorsa yeniden başlat
    if (playTimer) {
        stopPlay();
        play();
    }
}

/* ---- layer switch (tileları düzgün kaldır/ekle) ---- */
function setLayer(type) {
    if (!map || !layerGroupTiles) return;

    // sadece tile layerları kaldır
    Object.values(layerGroupTiles).forEach(tl => {
        if (map.hasLayer(tl)) map.removeLayer(tl);
    });

    const next = layerGroupTiles[type] || osm;
    next.addTo(map);

    // rota + marker zaten ayrı layer, dokunma
}

/* ==========================
   Helpers
   ========================== */
function haversineMeters(lat1, lon1, lat2, lon2) {
    const R = 6371000;
    const toRad = d => (d * Math.PI) / 180;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a =
        Math.sin(dLat / 2) ** 2 +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
    return 2 * R * Math.asin(Math.sqrt(a));
}

// time string parse edilemezse 0 döner (hız göstermeyiz)
function timeDiffSeconds(t1, t2) {
    // örnek formatlar: "2026-02-09 10:12:33" / "09.02.2026 10:12:33" vs...
    const d1 = parseAnyDate(t1);
    const d2 = parseAnyDate(t2);
    if (!d1 || !d2) return 0;
    const diff = (d2.getTime() - d1.getTime()) / 1000;
    return diff > 0 ? diff : 0;
}

function parseAnyDate(s) {
    if (!s) return null;

    // ISO gibi ise Date() bazen yer
    const dIso = new Date(s);
    if (!isNaN(dIso.getTime())) return dIso;

    // "DD.MM.YYYY HH:mm:ss" dene
    const m = String(s).match(/^(\d{2})\.(\d{2})\.(\d{4})\s+(\d{2}):(\d{2})(?::(\d{2}))?/);
    if (m) {
        const dd = Number(m[1]), MM = Number(m[2]) - 1, yyyy = Number(m[3]);
        const hh = Number(m[4]), mm = Number(m[5]), ss = Number(m[6] || 0);
        const d = new Date(yyyy, MM, dd, hh, mm, ss);
        if (!isNaN(d.getTime())) return d;
    }

    return null;
}

function escapeHtml(s) {
    return String(s ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

/* ==========================
   CSS (Leaflet divIcon) – ekle
   ========================== */
/*
.gps-pin { }
.gps-pin-dot{
  width: 14px; height: 14px;
  border-radius: 999px;
  background: rgba(56,189,248,.95);
  box-shadow: 0 0 18px rgba(56,189,248,.55), inset 0 0 0 2px rgba(255,255,255,.25);
}
*/
