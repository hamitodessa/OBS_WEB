
async function emirliste() {
    document.body.style.cursor = "wait";

    const server = document.getElementById("server").value.trim();
    const tableBody = document.getElementById("tableBody");
    const errorDiv = document.getElementById("errorDiv");
    const apiKey = document.getElementById("sifre").value.trim();
		const user = document.getElementById("kullaniciAdi").innerText.trim();
    const modul = "emirliste";
    const url = `https://${server}/loglar?key=${apiKey}&emir=${encodeURIComponent(modul)}&user=${encodeURIComponent(user)}`;

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
								<td>${row.GELECEK_YEDEKLEME || ""}</td>
            `;
            tableBody.appendChild(tr);
        });

    } catch (error) {
        console.error("Fetch hatası:", error);
        errorDiv.style.display = "block";
        if (error.message.includes("TypeError: Failed to fetch") || error.message.includes("fetch")) {
            errorDiv.innerHTML = `
                ❌ Sunucuya bağlantı sağlanamadı.<br>
                🔐 Muhtemelen sertifika geçersiz (ERR_CERT_AUTHORITY_INVALID).<br><br>
                📌 Lütfen aşağıdaki adımları uygulayın:<br>
                1. <a href="${url}" target="_blank">Bu bağlantıya tıklayın</a><br>
                2. Açılan sayfada "Advanced" → "Proceed" tıklayın<br>
                3. Ardından bu sayfayı yenileyin.
            `;
        } else {
            errorDiv.innerText = "Hata: " + (error.message || "Bilinmeyen hata");
        }
    } finally {
        document.body.style.cursor = "default";
    }
}