fileselect();

async function fileselect() {
    const sel = document.getElementById("fileSelect");
    const response = await fetch("gps/files");
    const files = await response.json();
    files.forEach(file => {
        const opt = document.createElement("option");
        opt.value = file;
        opt.text = file;
        sel.appendChild(opt);
    });
};



let currentIndex = 0;
let coords = [];
let map;

async function goster() {
    const file = document.getElementById("fileSelect").value;
    const start = document.getElementById("startDate").value;
    const end = document.getElementById("endDate").value;

    try {
        const response = await fetch(`gps/data?file=${file}&start=${start}&end=${end}`);
        coords = await response.json();
        currentIndex = 0;

        if (!coords || coords.length === 0) {
            alert("Seçilen tarih aralığında konum verisi bulunamadı tatlım 😢");
            return;
        }

        drawMap(coords);
        updateMarker(currentIndex);
    } catch (error) {
        console.error("Hata oluştu:", error);
        alert("Veri alınırken hata oluştu.");
    }
}

function drawMap(coords) {
    if (map) {
        map.remove(); // eski haritayı kaldır
    }

    map = L.map('map').setView([coords[0].lat, coords[0].lng], 15);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
}

function updateMarker(index) {
    if (!coords[index]) return;

    // Haritadaki eski marker'ları temizle
    map.eachLayer(layer => {
        if (layer instanceof L.Marker) {
            map.removeLayer(layer);
        }
    });

    // Yeni marker ekle
    const marker = L.marker([coords[index].lat, coords[index].lng]).addTo(map);
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
