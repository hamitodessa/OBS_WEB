currentPage = 0;
totalPages = 0;
pageSize = 500;


async function emirismidoldur() {
  const server = document.getElementById("server").value;
  const apiKey = document.getElementById("sifre").value;
  const hangi_emir = document.getElementById("hangi_emir");
  const errorDiv = document.getElementById("errorDiv");
  const user = document.getElementById("kullaniciAdi").innerText.trim();
  const tableBody = document.getElementById("tableBody");

  if (!server || !apiKey) {
    errorDiv.style.display = "block";
    errorDiv.innerText = "⚠️ Lütfen sunucu ve şifre bilgilerini girin.";
    return;
  }

  document.body.style.cursor = "wait";
  errorDiv.style.display = "none";
  errorDiv.innerText = "";
  tableBody.innerHTML = "";
  hangi_emir.innerHTML = "";

  const url = `/backup/emirliste?server=${encodeURIComponent(server)}&key=${encodeURIComponent(apiKey)}&user=${encodeURIComponent(user)}`;

  try {
    const data = await fetchWithSessionCheck(url);
    if (!data) return; // logout olduysa dur

    // Default seçenekleri ekle
    const defaultOption = new Option("Lütfen seçiniz", "");
    const defaultOption1 = new Option("Hepsi", "Hepsi");
    const systemOption = new Option("System", "System");
    [defaultOption, defaultOption1, systemOption].forEach(opt => hangi_emir.appendChild(opt));

    // Emir isimlerini doldur
    data.forEach(item => {
      if (item.EMIR_ISMI) {
        const option = new Option(item.EMIR_ISMI, item.EMIR_ISMI);
        hangi_emir.appendChild(option);
      }
    });
  } catch (error) {
    console.error("Emir ismi doldurma hatası:", error);
    errorDiv.style.display = "block";
    errorDiv.innerText = "Hata: " + (error.message || "Bilinmeyen hata oluştu.");
  } finally {
    document.body.style.cursor = "default";
  }
}


async function logliste(page = 0) {
  const emir_ismi = document.getElementById("hangi_emir").value;
  const errorDiv = document.getElementById("errorDiv");
  const tableBody = document.getElementById("tableBody");
  const startDate = document.getElementById("startDate").value;
  const endDate = document.getElementById("endDate").value;
  const user = document.getElementById("kullaniciAdi").innerText.trim();
  const server = document.getElementById("server").value;
  const apiKey = document.getElementById("sifre").value;

  errorDiv.style.display = "none";
  errorDiv.innerText = "";
  tableBody.innerHTML = "";

  if (!emir_ismi) return;

  if (!startDate || !endDate) {
    errorDiv.innerText = "Lütfen tarih aralığı seçin.";
    errorDiv.style.display = "block";
    return;
  }

  if (!server || !apiKey) {
    errorDiv.style.display = "block";
    errorDiv.innerText = "⚠️ Lütfen sunucu ve şifre bilgilerini girin.";
    return;
  }

  document.body.style.cursor = "wait";
  const url = `/backup/logliste?server=${encodeURIComponent(server)}&key=${encodeURIComponent(apiKey)}&emir=${encodeURIComponent(emir_ismi)}&start=${encodeURIComponent(startDate)}&end=${encodeURIComponent(endDate)}&page=${page}&user=${encodeURIComponent(user)}`;

  try {
    const data = await fetchWithSessionCheck(url);
    if (!data) return; // logout olmuşsa işlemi durdur

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
		pageSize = 500;
    currentPage = page;
    document.getElementById("prevPage").disabled = currentPage === 0;
    document.getElementById("nextPage").disabled = tableBody.rows.length < pageSize;
  } catch (error) {
    console.error("Log listeleme hatası:", error);
    errorDiv.style.display = "block";
    errorDiv.innerText = "Hata: " + (error.message || "Bilinmeyen hata oluştu.");
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
