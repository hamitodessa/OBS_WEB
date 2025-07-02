let currentPage = 0;
const pageSize = 500;


async function emirismidoldur() {
    
    const server = document.getElementById("server").value;
    const apiKey = document.getElementById("sifre").value;
    const hangi_emir = document.getElementById("hangi_emir");
    const errorDiv = document.getElementById("errorDiv");
	
	if (!server || !apiKey) {
	    errorDiv.style.display = "block";
	    errorDiv.innerText = "⚠️ Lütfen sunucu ve şifre bilgilerini girin.";
	    return;
	}
	document.body.style.cursor = "wait";
    const user = document.getElementById("kullaniciAdi").innerText.trim();
    const url = `/backup/emirliste?server=${encodeURIComponent(server)}&key=${encodeURIComponent(apiKey)}&user=${encodeURIComponent(user)}`;

    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    const tableBody = document.getElementById("tableBody");
    tableBody.innerHTML = "";

    try {
        const response = await fetch(url);
        if (!response.ok) {
            let hataMesaji = "";
            try {
                const hataData = await response.json();
                hataMesaji = hataData.message || JSON.stringify(hataData);
            } catch {
                hataMesaji = await response.text();
            }
            throw new Error(`Sunucu Hatası (${response.status}): ${hataMesaji}`);
        }
        const data = await response.json();
        const defaultOption = new Option("Lütfen seçiniz", "");
        const defaultOption1 = new Option("Hepsi", "Hepsi");
        const systemOption = new Option("System", "System");
        [defaultOption, defaultOption1, systemOption].forEach(opt => hangi_emir.appendChild(opt));

        data.forEach(item => {
            if (item.EMIR_ISMI) {
                const option = new Option(item.EMIR_ISMI, item.EMIR_ISMI);
                hangi_emir.appendChild(option);
            }
        });
    } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = "Hata: " + (error.message || "Bilinmeyen hata");
    } finally {
        document.body.style.cursor = "default";
    }
}

async function logliste(page = 0) {
   
    const emir_ismi = document.getElementById("hangi_emir").value;
    const errorDiv = document.getElementById("errorDiv");

		
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    const tableBody = document.getElementById("tableBody");
    tableBody.innerHTML = "";
    if (emir_ismi === "") return;

    const startDate = document.getElementById("startDate").value;
    const endDate = document.getElementById("endDate").value;
    const user = document.getElementById("kullaniciAdi").innerText.trim();
    if (!startDate || !endDate) {
        errorDiv.innerText = "Lütfen tarih aralığı seçin.";
        errorDiv.style.display = "block";
        return;
    }
    const server = document.getElementById("server").value;
    const apiKey = document.getElementById("sifre").value;
	if (!server || !apiKey) {
		    errorDiv.style.display = "block";
		    errorDiv.innerText = "⚠️ Lütfen sunucu ve şifre bilgilerini girin.";
		    return;
		}
	document.body.style.cursor = "wait";
    const url = `/backup/logliste?server=${encodeURIComponent(server)}&key=${encodeURIComponent(apiKey)}&emir=${encodeURIComponent(emir_ismi)}&start=${encodeURIComponent(startDate)}&end=${encodeURIComponent(endDate)}&page=${page}&user=${encodeURIComponent(user)}`;

    try {
        const response = await fetch(url);
        if (!response.ok) {
            let hataMesaji = "";
            try {
                const hataData = await response.json();
                hataMesaji = hataData.message || JSON.stringify(hataData);
            } catch {
                hataMesaji = await response.text();
            }
            throw new Error(`Sunucu Hatası (${response.status}): ${hataMesaji}`);
        }
        const data = await response.json();
        data.forEach(row => {
            const tr = document.createElement("tr");
            tr.classList.add("table-row-height");
            [row.TARIH, row.ACIKLAMA, row.EMIR_ISMI].forEach(text => {
                const td = document.createElement("td");
                td.innerText = text;
                tr.appendChild(td);
            });
            tableBody.appendChild(tr);
        });
        currentPage = page;
        document.getElementById("prevPage").disabled = currentPage === 0;
        document.getElementById("nextPage").disabled = tableBody.rows.length < pageSize;
    } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = "Hata: " + (error.message || "Bilinmeyen hata");
    } finally {
        document.body.style.cursor = "default";
    }
}

function ileriSayfa() {
    logliste(currentPage + 1);
}

function geriSayfa() {
    if (currentPage > 0) logliste(currentPage - 1);
}
