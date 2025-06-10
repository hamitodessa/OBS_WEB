let currentPage = 0;
const pageSize = 500;


async function emirismidoldur() {
    const server = document.getElementById("server").value;
		const apiKey = document.getElementById("sifre").value;
		const hangi_emir = document.getElementById("hangi_emir");
    const errorDiv = document.getElementById("errorDiv");
    const modul = "emirliste" ;
    
    const url = `https://${server}/loglar?key=${apiKey}&emir=${encodeURIComponent(modul)}`;
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
				const defaultOption = document.createElement("option");
				const defaultOption1 = document.createElement("option");
				        defaultOption.text = "Lütfen seçiniz";
				        defaultOption.value = "";
				        hangi_emir.appendChild(defaultOption);
								defaultOption1.text = "Hepsi";
								defaultOption1.value = "Hepsi";
								hangi_emir.appendChild(defaultOption1);
				        data.forEach(item => {
				            if (item.EMIR_ISMI) {
				                const option = document.createElement("option");
				                option.value = item.EMIR_ISMI;
				                option.text = item.EMIR_ISMI;
				                hangi_emir.appendChild(option);
				            }
				        });
				
    } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = "Hata: " + error.message;
    }
}

async function logliste(page = 0) {
    const emir_ismi = document.getElementById("hangi_emir").value;
		const errorDiv = document.getElementById("errorDiv");
		errorDiv.style.display = "none";
		errorDiv.innerText = "";
		const tableBody = document.getElementById("tableBody");
		tableBody.innerHTML = "";
		if (emir_ismi == "") return;		
		
		const startDate = document.getElementById("startDate").value;
		const endDate = document.getElementById("endDate").value;
		const server = document.getElementById("server").value;
		const apiKey = document.getElementById("sifre").value;

		const url = `https://${server}/loglar?key=${apiKey}&emir=${encodeURIComponent(emir_ismi)}&start=${encodeURIComponent(startDate)}&end=${encodeURIComponent(endDate)}&page=${page}&limit=${pageSize}`;

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
              tr.innerHTML = `
                    <td>${row.TARIH}</td>
                    <td>${row.ACIKLAMA}</td>
                    <td>${row.EMIR_ISMI}</td>
                `;
                tableBody.appendChild(tr);
            });
        currentPage = page;
        document.getElementById("prevPage").disabled = currentPage === 0;
        document.getElementById("nextPage").disabled = tableBody.rows.length < pageSize;

    } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = "Hata: " + error.message;
    }
}

function ileriSayfa() {
    lograpor(currentPage + 1);
}

function geriSayfa() {
    if (currentPage > 0) lograpor(currentPage - 1);
}
