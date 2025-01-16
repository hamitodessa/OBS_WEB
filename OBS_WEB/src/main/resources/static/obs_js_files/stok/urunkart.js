function urnenableInputs() {
	urnclearInputs();
	const inputs = document.querySelectorAll('.form-control');
	inputs.forEach(input => {
		input.disabled = false;
	});

}

function urnenableDuzeltmeInputs() {
	const inputs = document.querySelectorAll('.form-control');
	inputs.forEach(input => {
		if (input.id !== "kodu") {
			input.disabled = false;
		}

	});
}

function urndisableInputs() {
	const inputs = document.querySelectorAll('.form-control');
	inputs.forEach(input => {
		if (input.id !== "arama") {
			input.disabled = true;
		}

	});
}

function urnclearInputs() {
	const inputs = document.querySelectorAll('.form-control');
	inputs.forEach(input => {
		if (input.type === 'file') {
			input.value = '';
		} else if (input.name !== 'arama') {
			input.value = '';
		}
	});
	const imgElement = document.getElementById("resimGoster");
	imgElement.src = "";
	imgElement.style.display = "none";
	document.getElementById("kodKontrol").innerText = "" ;
}

async function urnKayit() {
	const koduInput = document.getElementById('kodu');
	if (["0", ""].includes(koduInput.value)) {
		return;
	}
	const urunDTO = geturnDTO;
	const formData = new FormData();

	for (const key in urunDTO) {
		formData.append(key, urunDTO[key]);
	}

	const fileInput = document.getElementById("resim");
	const file = fileInput.files[0];
	if (file) {
		formData.append("resim", file);
	}

	const imgElement = document.getElementById("resimGoster");
	let base64Data = imgElement.src.startsWith("data:image")
		? imgElement.src.split(",")[1]
		: null;

	if (base64Data) {
		const byteCharacters = atob(base64Data); // Base64'ü çöz
		const byteNumbers = new Array(byteCharacters.length);
		for (let i = 0; i < byteCharacters.length; i++) {
			byteNumbers[i] = byteCharacters.charCodeAt(i);
		}
		const byteArray = new Uint8Array(byteNumbers);
		const blob = new Blob([byteArray], { type: "image/jpeg" });
		formData.append("resimGoster", blob, "base64Resim.jpg"); // Blob olarak ekle
	}

	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";
	errorDiv.style.display = "none";
	errorDiv.innerText = "";

	try {
		const response = await fetchWithSessionCheck("stok/urnkayit", {
			method: "POST",
			body: formData
		});

		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		sayfaYukle();
		document.body.style.cursor = "default";
	} catch (error) {
		document.body.style.cursor = "default";
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message;
	}
}

function geturnDTO() {
	return {
		kodu: document.getElementById("kodu").value,
		adi: document.getElementById("adi").value,
		birim: document.getElementById("birim").value,
		kusurat: document.getElementById("kusurat").value,
		sinif: document.getElementById("sinif").value,
		anagrup: document.getElementById("anagrup").value,
		altgrup: document.getElementById("altgrup").value,
		aciklama1: document.getElementById("aciklama1").value,
		aciklama2: document.getElementById("aciklama2").value,
		ozelkod1: document.getElementById("ozelkod1").value,
		ozelkod2: document.getElementById("ozelkod2").value,
		barkod: document.getElementById("barkod").value,
		mensei: document.getElementById("mensei").value,
		agirlik: document.getElementById("agirlik").value,
		fiat1: document.getElementById("fiat1").value,
		fiat2: document.getElementById("fiat2").value,
		fiat3: document.getElementById("fiat3").value,
		recete: document.getElementById("recete").value
	};
}

async function urnaramaYap() {
	
	document.getElementById("kodKontrol").innerText = "" ;
	const aramaInput = document.getElementById("arama").value;
	if (!aramaInput || aramaInput === "") {
		return;
	}
	document.body.style.cursor = "wait";
	document.getElementById("errorDiv").style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("stok/urnArama", {
			method: "POST",
			headers: {
				"Content-Type": "application/x-www-form-urlencoded"
			},
			body: new URLSearchParams({ arama: aramaInput })
		});
		const dto = response;
		if (dto.errorMessage === "Bu Numarada Kayıtlı Hesap Yok") {
			document.getElementById("errorDiv").innerText = dto.errorMessage;
			return;
		}
		document.getElementById("kodu").value = dto.kodu; 
		document.getElementById("adi").value = dto.adi;
		document.getElementById("birim").value = dto.birim; 
		document.getElementById("kusurat").value = dto.kusurat;
		document.getElementById("sinif").value = dto.sinif;
		document.getElementById("anagrup").value = dto.anagrup;
		document.getElementById("altgrup").value = dto.altgrup;
		document.getElementById("aciklama1").value = dto.aciklama1;
		document.getElementById("aciklama2").value = dto.aciklama2;
		document.getElementById("ozelkod1").value = dto.ozelkod1;
		document.getElementById("ozelkod2").value = dto.ozelkod2;
		document.getElementById("barkod").value = dto.barkod;
		document.getElementById("mensei").value = dto.mensei;
		document.getElementById("agirlik").value = dto.agirlik;
		document.getElementById("fiat1").value = dto.fiat1;
		document.getElementById("fiat2").value = dto.fiat2;
		document.getElementById("fiat3").value = dto.fiat3;
		document.getElementById("recete").value = dto.recete;
		const imgElement = document.getElementById("resimGoster");
		if (dto.base64Resim && dto.base64Resim.trim() !== "") {
			const base64String = 'data:image/jpeg;base64,' + dto.base64Resim.trim();
			imgElement.src = base64String;
			imgElement.style.display = "block";
		} else {
			imgElement.src = "";
			imgElement.style.display = "none";
		}
		urndisableInputs();
		urnenableDuzeltmeInputs();
		document.getElementById("errorDiv").style.display = "none";
		document.getElementById("errorDiv").innerText = "";
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function urnGeri() {
	const arama = document.getElementById("kodu").value;
	const datalist = document.getElementById("urnOptions");
	const options = datalist.getElementsByTagName("option");
	if (options.length === 0) {
		return null;
	}
	let index = -1;
	for (let i = 0; i < options.length; i++) {
		if (options[i].value === arama) {
			index = i;
			break;
		}
	}
	if (index !== -1) {
		if (index > 0) {
			const previousValue = options[index - 1].value;
			document.getElementById("arama").value = previousValue;
			urnaramaYap();
			document.getElementById("arama").value = "";
		} else {
			return null;
		}
	} else {
		return null;
	}
}

function urnIleri() {
	const arama = document.getElementById("kodu").value; // Input alanının değerini al
	const datalist = document.getElementById("urnOptions"); // Datalist öğesini seç
	const options = datalist.getElementsByTagName("option"); // Datalist içindeki option'ları al
	if (options.length === 0) {
		return null;
	}
	let index = -1;
	for (let i = 0; i < options.length; i++) {
		if (options[i].value === arama) {
			index = i;
			break;
		}
	}

	if (index !== -1) {
		if (index < options.length - 1) {
			const nextValue = options[index + 1].value; // Bir sonraki indexin değeri
			document.getElementById("arama").value = nextValue;
			urnaramaYap();
			document.getElementById("arama").value = "";
		} else {
			return null; // Son index ise geri null döndür
		}
	} else {
		return null; // Değer bulunamazsa null döndür
	}
}

function urnIlk() {
	const datalist = document.getElementById("urnOptions");
	const options = datalist.getElementsByTagName("option"); 

	if (options.length === 0) {
		urnclearInputs();
		urndisableInputs();
		return null;
	}
	const firstValue = options[0].value;
	document.getElementById("arama").value = firstValue;
	urnaramaYap();
	document.getElementById("arama").value = "";
}

//************************evrak sil ***********************************************
async function urnSil() {
	const urnKodu = document.getElementById('kodu').value;
	if (["0", ""].includes(hesapKodu.value)) {
		return;
	}
	const message = "Kayit Dosyadan Silinecek ..?" ;
	const confirmDelete = confirm(message);
	if (!confirmDelete) {
		return;
	}
	document.getElementById("errorDiv").style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("stok/urnSil", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ urnkodu: urnKodu }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		sayfaYukle();
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function sayfaYukle() {
	const url = "stok/urunkart";
	try {
		document.body.style.cursor = "wait";
		const response = await fetch(url, { method: "GET" });

		if (!response.ok) {
			throw new Error(`Bir hata oluştu: ${response.statusText}`);
		}
		const data = await response.text();
		if (data.includes('<form') && data.includes('name="username"')) {
			window.location.href = "/login";
		} else {
			document.getElementById('ara_content').innerHTML = data;
			stokBaslik();
			urnaramaYap();
			document.getElementById("arama").value = "";
		}
	} catch (error) {
		document.getElementById('ara_content').innerHTML = `<h2>${error.message}</h2>`;
	} finally {
		document.body.style.cursor = "default";
	}
}

function resimSil() {
	const imgElementLogo = document.getElementById("resimGoster");
	imgElementLogo.src = "";
	imgElementLogo.style.display = "none";
}

async function anaChanged(selectElement) {
	const selectedValue = selectElement.value; // Seçilen değeri al
	document.body.style.cursor = "wait";
	document.getElementById("errorDiv").style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("stok/altgrup", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ anagrup: selectedValue }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}

		const selectElement = document.getElementById("altgrup");
		selectElement.innerHTML = '';

		response.altKodlari.forEach(kod => {
			const option = document.createElement("option");
			option.value = kod.ALT_GRUP; // Option value
			option.textContent = kod.ALT_GRUP; // Option görünen metni
			selectElement.appendChild(option); // Option'ı dropdown'a ekle
		});


	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function birimtipChanged(selectElement) {
	const selectedValue = selectElement.value; // Seçilen değeri al
	const imgElementLogo = document.getElementById("birim");
	imgElementLogo.value = selectedValue ;
	
}

async function urnAdiOgren(inputElement, targetId) {
	const inputValue = inputElement.value;
	document.body.style.cursor = "wait";
	if (!inputValue) {
		document.getElementById(targetId).value = "";
		document.body.style.cursor = "default";
		return;
	}
	document.getElementById("errorDiv").style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("stok/urnadi", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ urnkodu: inputValue }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		document.getElementById(targetId).value = response.urnAdi;
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}