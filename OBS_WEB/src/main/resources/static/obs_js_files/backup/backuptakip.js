
async function emirliste() {
    const server = document.getElementById("server").value.trim();
    const apiKey = document.getElementById("sifre").value.trim();
    const errorDiv = document.getElementById("errorDiv");
    const tableBody = document.getElementById("tableBody");
    const user = document.getElementById("kullaniciAdi").innerText.trim();

    if (!server || !apiKey) {
        errorDiv.style.display = "block";
        errorDiv.innerText = "⚠️ Lütfen sunucu ve şifre bilgilerini girin.";
        return;
    }

    document.body.style.cursor = "wait";
    errorDiv.style.display = "none";
    errorDiv.innerText = "";

    const url = `/backup/emirliste?server=${encodeURIComponent(server)}&key=${encodeURIComponent(apiKey)}&user=${encodeURIComponent(user)}`;

    try {
        const data = await fetchWithSessionCheck(url);
        if (!data) return; // logout durumunda devam etme

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
            if (row.DURUM != 1) {
                tr.querySelectorAll("td").forEach(td => td.style.color = "red");
            }
            tableBody.appendChild(tr);
        });
    } catch (error) {
        console.error("Fetch hatası:", error);
        errorDiv.style.display = "block";
        errorDiv.innerText = "Hata: " + (error.message || "Bilinmeyen hata oluştu.");
    } finally {
        document.body.style.cursor = "default";
    }
}
