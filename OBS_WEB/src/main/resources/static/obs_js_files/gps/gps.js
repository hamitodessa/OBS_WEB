fileselect();

async function fileselect() {
    document.body.style.cursor = "wait";
    const sel = document.getElementById("fileSelect");
    sel.innerHTML = ""; 
    try {
        const response = await fetch("gps/files");
        const files = await response.json();
        files.forEach(file => {
            const opt = document.createElement("option");
            opt.value = file;
            opt.text = file;
            sel.appendChild(opt);
        });
    } catch (error) {
        console.error("Dosya listesi alınırken hata oluştu:", error);
        alert("Dosya listesi alınamadı tatlım 😢");
    } finally {
        document.body.style.cursor = "default";
    }
}
let currentIndex = 0;
let coords = [];
let map;
let polyline;
let marker;
let osm, satellite, dark;

function initMapLayers() {
    osm = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap'
    });

    satellite = L.tileLayer('https://{s}.google.com/vt/lyrs=s&x={x}&y={y}&z={z}', {
        maxZoom: 20,
        subdomains: ['mt0', 'mt1', 'mt2', 'mt3'],
        attribution: '© Google'
    });

    dark = L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
        attribution: '© Carto',
        subdomains: 'abcd',
        maxZoom: 19
    });
}

async function goster() {
    if (!osm) initMapLayers();
    const file = document.getElementById("fileSelect").value;
    const start = document.getElementById("startDate").value;
    const end = document.getElementById("endDate").value;
    document.body.style.cursor = "wait";
    try {
        const response = await fetch(`gps/data?file=${file}&start=${start}&end=${end}`);
        coords = await response.json();
        currentIndex = 0;
        if (!coords || coords.length === 0) {
					   document.body.style.cursor = "default";
            alert("Seçilen tarih aralığında konum verisi bulunamadı tatlım 😢");
            return;
        }
        drawMap(coords);
        updateMarker(currentIndex);
    } catch (error) {
        alert("Veri alınırken hata oluştu tatlım 😢\n\n" + error.message);
    } finally {
        document.body.style.cursor = "default";
    }
}


function drawMap(coords) {
    if (map) {
        map.off();
        map.remove();
    }

    map = L.map('map', {
        center: [coords[0].lat, coords[0].lng],
        zoom: 15,
        layers: [osm] // Başlangıçta normal görünüm
    });

    const latlngs = coords.map(c => [c.lat, c.lng]);
    if (polyline) {
        map.removeLayer(polyline);
    }
    polyline = L.polyline(latlngs, {
			    color: '#6fa8dc', // daha pastel ve tatlı mavi
			    weight: 4,
			    opacity: 0.9,
			    smoothFactor: 1
    }).addTo(map);
    map.fitBounds(polyline.getBounds());
}

function updateMarker(index) {
    if (!coords[index]) return;
    if (marker) {
        map.removeLayer(marker);
    }
    marker = L.marker([coords[index].lat, coords[index].lng]).addTo(map);
    marker.bindPopup(coords[index].time).openPopup();
}

function ileri() {
    if (currentIndex < coords.length - 1) {
        currentIndex++;
        updateMarker(currentIndex);
    }
}

function geri() {
    if (currentIndex > 0) {
        currentIndex--;
        updateMarker(currentIndex);
    }
}

function setLayer(type) {
    if (!map) return;
    map.eachLayer(layer => {
        if (layer instanceof L.TileLayer) {
            map.removeLayer(layer);
        }
    });
    switch (type) {
        case 'osm': osm.addTo(map); break;
        case 'satellite': satellite.addTo(map); break;
        case 'dark': dark.addTo(map); break;
    }
    if (polyline) polyline.addTo(map);
    if (marker) marker.addTo(map);
}