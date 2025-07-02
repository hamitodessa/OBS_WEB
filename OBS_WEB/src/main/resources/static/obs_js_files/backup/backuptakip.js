
async function emirliste() {
   
    const server = document.getElementById("server").value.trim();
	const apiKey = document.getElementById("sifre").value.trim();
	if (!server || !apiKey) {
	    errorDiv.style.display = "block";
	    errorDiv.innerText = "⚠️ Lütfen sunucu ve şifre bilgilerini girin.";
	    return;
	}
	document.body.style.cursor = "wait";
    const tableBody = document.getElementById("tableBody");
    const errorDiv = document.getElementById("errorDiv");
   
    const user = document.getElementById("kullaniciAdi").innerText.trim();
    const url = `/backup/emirliste?server=${encodeURIComponent(server)}&key=${encodeURIComponent(apiKey)}&user=${encodeURIComponent(user)}`;
    errorDiv.style.display = "none";
    errorDiv.innerText = "";

    try {
        const response = await fetch(url);
		if (!response.ok) {
		    let hataMesaji = "";
		    try {
		        const rawText = await response.text();
		        try {
		            const hataData = JSON.parse(rawText);
		            hataMesaji = hataData.message || rawText;
		        } catch {
		            hataMesaji = rawText;
		        }
		    } catch (e) {
		        hataMesaji = "Hata mesajı alınamadı.";
		    }
		    throw new Error(`Sunucu Hatası (${response.status}): ${hataMesaji}`);
		}

        const data = await response.json();
        tableBody.innerHTML = "";
		data.forEach(row => {
		    const tr = document.createElement("tr");
		    tr.classList.add("table-row-height");
		    const durumText = row.DURUM == 1 ? "Aktif" : "Pasif";
		    if (durumText === "Pasif") {
		        tr.style.color = "red";
		    }
		    tr.innerHTML = `
		        <td>${row.EMIR_ISMI}</td>
		        <td>${durumText}</td>
		        <td>${row.MESAJ || ""}</td>
		        <td>${row.INSTANCE || ""}</td>
		        <td>${formatTarihsqlite(row.SON_YUKLEME)}</td>
		        <td>${row.GELECEK_YEDEKLEME || ""}</td>
		    `;
		    tableBody.appendChild(tr);
		});


    } catch (error) {
        console.error("Fetch hatası:", error);
        errorDiv.style.display = "block";
        errorDiv.innerText = "Hata: " + (error.message || "Bilinmeyen hata");
    } finally {
        document.body.style.cursor = "default";
    }
}