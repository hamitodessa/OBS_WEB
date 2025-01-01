async function etiketListele() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none"; // Hata mesajını gizle

	try {
		const response = await fetchWithSessionCheck("adres/etiketliste", {
			method: 'GET',
			headers: {
				'Content-Type': 'text/html',
			},
		});

		if (!response.ok) {
			const errorText = await response.text();
			throw new Error(errorText || "Bir hata oluştu.");
		}

		const html = await response.text();
		document.getElementById("gonderilmisMailler").innerHTML = html;

	} catch (error) {
		errorDiv.style.display = "block"; // Hata mesajını göster
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	}
}

function toggleCheckboxes(source) {
	const checkboxes = document.querySelectorAll('tbody input[type="checkbox"]');
	checkboxes.forEach(checkbox => {
		checkbox.checked = source.checked;
	});
}

async function etiketyazdir() {

	const rows = document.querySelectorAll('tbody input[type="checkbox"]:checked');
	const selectedData = Array.from(rows).map(row => {
		const tr = row.closest('tr'); // Checkbox'ın bulunduğu satır
		const data = {
			Adi: tr.querySelector('td:nth-child(2)').textContent.trim(),
			Adres_1: tr.querySelector('td:nth-child(3)').textContent.trim(),
			Adres_2: tr.querySelector('td:nth-child(4)').textContent.trim(),
			Tel_1: tr.querySelector('td:nth-child(5)').textContent.trim(),
			Semt: tr.querySelector('td:nth-child(6)').textContent.trim(),
			Sehir: tr.querySelector('td:nth-child(7)').textContent.trim()
		};
		return data;
	});
	if (selectedData.length === 0) {
		alert('Lütfen en az bir satır seçin.');
		return;
	}
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";

	document.body.style.cursor = "wait";
	const $indirButton = $('#etiketyazdirButton');
	$indirButton.prop('disabled', true).text('İşleniyor...');

	try {
		const response = await fetchWithSessionCheckForDownload('adres/etiket_download', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({ selectedRows: selectedData })
		});

		if (response.blob) {
			const disposition = response.headers.get('Content-Disposition');
			const fileName = disposition.match(/filename="(.+)"/)[1];
			const url = window.URL.createObjectURL(response.blob);
			const a = document.createElement("a");
			a.href = url;
			a.download = fileName;
			document.body.appendChild(a);
			a.click();
			a.remove();
			window.URL.revokeObjectURL(url);
		} else {
			throw new Error("Dosya indirilemedi.");
		}

	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Bilinmeyen bir hata oluştu.";
	} finally {
		$indirButton.prop('disabled', false).text('Yazdir');
		document.body.style.cursor = "default";
	}
}

function aramaYap() {
   const input = document.getElementById("arama");
   const filter = input.value.toLowerCase();
   const table = document.getElementById("myTable");
   const rows = table.getElementsByTagName("tr");

   for (let i = 1; i < rows.length; i++) { // İlk satır başlık olduğu için 1'den başlıyoruz
     const cells = rows[i].getElementsByTagName("td");
     let shouldDisplay = false;

     if (cells.length > 1) {
       const col1 = cells[1].textContent.toLowerCase();

       if (col1.includes(filter)) {
         shouldDisplay = true;
       }
     }
     rows[i].style.display = shouldDisplay ? "" : "none";
   }
 }
 
