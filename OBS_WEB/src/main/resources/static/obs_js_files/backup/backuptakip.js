
async function emirliste() {
    const server = document.getElementById("server").value;
     const tableBody = document.getElementById("tableBody");
    const errorDiv = document.getElementById("errorDiv");
		const modul = "emirliste" ;
    const apiKey = document.getElementById("sifre").value;
    const url = `https://${server}/loglar?key=${apiKey}&emir=${encodeURIComponent(modul)}`;
		errorDiv.style.display = "none";
		errorDiv.innerText = "";
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
        tableBody.innerHTML = "";
				data.forEach(row => {
				    const tr = document.createElement("tr");
						tr.classList.add("table-row-height");
				    const durumText = row.DURUM == 1 ? "Aktif" : "Pasif";
				    tr.innerHTML = `
				        <td>${row.EMIR_ISMI}</td>
				        <td>${durumText}</td>
				        <td>${row.MESAJ || ""}</td>
				        <td>${row.INSTANCE || ""}</td>
				        <td>${formatTarihsqlite(row.SON_YUKLEME)}</td>
				    `;
				    tableBody.appendChild(tr);
				});
    } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = "Hata: " + error.message;
    }
}