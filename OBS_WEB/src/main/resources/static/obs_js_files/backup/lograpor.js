currentPage = 0;
totalPages = 0;
pageSize = 500;



async function requestJson(url) {
  const resp = await fetch(url, { cache: "no-store" });

  if (!resp.ok) {
    let msg = "";
    try {
      const j = await resp.json();
      msg = j?.message || j?.hata || JSON.stringify(j);
    } catch {
      msg = await resp.text();
    }
    if (!msg) {
      if (resp.status === 503) msg = "Şu anda yedekleme çalışıyor. Lütfen biraz sonra tekrar deneyin.";
      else if (resp.status === 401) msg = "Yetkisiz / Geçersiz anahtar.";
      else msg = "Sunucu hatası (" + resp.status + ").";
    }
    const e = new Error(msg);
    e.status = resp.status;
    throw e;
  }

  return resp.json();
}



async function emirismidoldur() {
  const server = document.getElementById("server").value.trim();
  const apiKey = document.getElementById("sifre").value.trim();
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
    const data = await requestJson(url);


    hangi_emir.appendChild(new Option("Lütfen seçiniz", ""));
    hangi_emir.appendChild(new Option("Hepsi", "Hepsi"));
    hangi_emir.appendChild(new Option("System", "System"));

    // Emir isimleri
    data.forEach(item => {
      if (item.EMIR_ISMI) hangi_emir.appendChild(new Option(item.EMIR_ISMI, item.EMIR_ISMI));
    });
  } catch (error) {
    console.error("Emir ismi doldurma hatası:", error);
    errorDiv.style.display = "block";
    errorDiv.innerText = error.message || "Bilinmeyen hata oluştu.";
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
  const server = document.getElementById("server").value.trim();
  const apiKey = document.getElementById("sifre").value.trim();
  const prevBtn = document.getElementById("prevPage");
  const nextBtn = document.getElementById("nextPage");

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
  const url =
    `/backup/logliste?server=${encodeURIComponent(server)}&key=${encodeURIComponent(apiKey)}&emir=${encodeURIComponent(emir_ismi)}&start=${encodeURIComponent(startDate)}&end=${encodeURIComponent(endDate)}&page=${page}&user=${encodeURIComponent(user)}`;

  try {
    const data = await requestJson(url);

    data.forEach(row => {
      const tr = document.createElement("tr");
      tr.classList.add("table-row-height");

      [row.TARIH, row.ACIKLAMA, row.EMIR_ISMI].forEach(text => {
        const td = document.createElement("td");
        td.innerText = text ?? "";
        tr.appendChild(td);
      });

      tableBody.appendChild(tr);
    });

    currentPage = page;

    if (prevBtn) prevBtn.disabled = currentPage === 0;
    if (nextBtn) nextBtn.disabled = tableBody.rows.length < pageSize; // son sayfa mı

  } catch (error) {
    console.error("Log listeleme hatası:", error);
    errorDiv.style.display = "block";
    errorDiv.innerText = error.message || "Bilinmeyen hata oluştu.";
    // hata halinde sayfalama kilit
    if (prevBtn) prevBtn.disabled = true;
    if (nextBtn) nextBtn.disabled = true;
  } finally {
    document.body.style.cursor = "default";
  }
}

function ileriSayfa()
 { 
	logliste(currentPage + 1); 
	
 }
 
  function geriSayfa() 
	{ if (currentPage > 0) 
		logliste(currentPage - 1); 
	
	}