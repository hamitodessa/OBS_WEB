async function ayarKayit() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	const etiketayarDTO = {
		id: document.getElementById("id").value,
		altbosluk: document.getElementById("altbosluk").value,
		ustbosluk: document.getElementById("ustbosluk").value,
		sagbosluk: document.getElementById("sagbosluk").value,
		solbosluk: document.getElementById("solbosluk").value,
		dikeyarabosluk: document.getElementById("dikeyarabosluk").value,
		genislik: document.getElementById("genislik").value,
		yataydikey: document.getElementById("yataydikey").value,
		yukseklik: document.getElementById("yukseklik").value,
	};
	const submitButton = document.getElementById("kaydetButton");
	submitButton.disabled = true;
	submitButton.textContent = 'İşleniyor...';

	try {
		const response = await
			fetchWithSessionCheck("adres/etiketsettings_save", {
				method: "POST",
				headers: {
					"Content-Type": "application/json"
				},
				body: JSON.stringify(etiketayarDTO),
			});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		await etsayfaYukle();
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error;
	} finally {
		submitButton.disabled = false;
		submitButton.textContent = 'Kaydet';
		document.body.style.cursor = "default";
	}
}

async function etsayfaYukle() {
	const url = "adres/etiketayar";
	try {
		document.body.style.cursor = "wait"; // İşaretçiyi bekleme durumuna getir
		const response = await fetch(url, { method: "GET" });
		if (!response.ok) {
			throw new Error(`Bir hata oluştu: ${response.statusText}`);
		}
		const data = await response.text();
		if (data.includes('<form') && data.includes('name="username"')) {
			window.location.href = "/login";
		} else {
			document.getElementById('ara_content').innerHTML = data;
		}
	} catch (error) {
		const errorDiv = document.getElementById("errorDiv");
		errorDiv.innerText = error;
		document.getElementById('ara_content').innerHTML = `<h2>${error.message}</h2>`;
	} finally {
		document.body.style.cursor = "default"; // İşaretçiyi sıfırla
	}
}