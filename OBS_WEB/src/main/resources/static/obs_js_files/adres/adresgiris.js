function adrenableInputs() {
	adrclearInputs();
	const inputs = document.querySelectorAll('.form-control');
	inputs.forEach(input => {
		input.disabled = false;
	});

}

function adrenableDuzeltmeInputs() {
	const inputs = document.querySelectorAll('.form-control');
	inputs.forEach(input => {
		if (input.id !== "kodu") {
			input.disabled = false;
		}

	});
}

function adrdisableInputs() {
	const inputs = document.querySelectorAll('.form-control');
	inputs.forEach(input => {
		if (input.id !== "arama") {
			input.disabled = true;
		}

	});
}

function adrclearInputs() {
	const inputs = document.querySelectorAll('.form-control');
	inputs.forEach(input => {
		if (input.type === 'checkbox') {
			input.checked = false;
		} else if (input.type === 'file') {
			input.value = '';
		} else if (input.name !== 'arama') {
			input.value = '';
		}
	});
	document.getElementById("adrid").value = 0;
	const imgElement = document.getElementById("resimGoster");
	imgElement.src = "";
	imgElement.style.display = "none";
	const kodKontrol = document.getElementById("kodKontrol") ;
	kodKontrol.innerText = ""
}

async function adrKayit() {
	const koduInput = document.getElementById('kodu');
	if (["0", ""].includes(koduInput.value)) {
		return;
	}
	const adresDTO = adrgetDTO();
	const formData = new FormData();

	for (const key in adresDTO) {
		formData.append(key, adresDTO[key]);
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
		const response = await fetchWithSessionCheck("adres/adrkayit", {
			method: "POST",
			body: formData
		});

		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		document.body.style.cursor = "default";
		sayfaYukle();
	} catch (error) {
		document.body.style.cursor = "default";
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message;
	}
}

async function sayfaYukle() {
	const url = "adres/adresgiris";
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
			adresBaslik();
			adraramaYap();
			document.getElementById("arama").value = "";
		}
	} catch (error) {
		document.getElementById('ara_content').innerHTML = `<h2>${error.message}</h2>`;
	} finally {
		document.body.style.cursor = "default"; // İşaretçiyi sıfırla
	}
}
function adrgetDTO() {
	return {
		kodu: document.getElementById("kodu").value,
		unvan: document.getElementById("unvan").value,
		ad1: document.getElementById("ad1").value,
		semt: document.getElementById("semt").value,
		ad2: document.getElementById("ad2").value,
		seh: document.getElementById("seh").value,
		pkodu: document.getElementById("pkodu").value,
		smsgon: document.getElementById("smsgon").checked,
		mailgon: document.getElementById("mailgon").checked,
		vd: document.getElementById("vd").value,
		vn: document.getElementById("vn").value,
		t1: document.getElementById("t1").value,
		t2: document.getElementById("t2").value,
		t3: document.getElementById("t3").value,
		fx: document.getElementById("fx").value,
		o1: document.getElementById("o1").value,
		o2: document.getElementById("o2").value,
		web: document.getElementById("web").value,
		ozel: document.getElementById("ozel").value,
		mail: document.getElementById("mail").value,
		acik: document.getElementById("acik").value,
		not1: document.getElementById("not1").value,
		not2: document.getElementById("not2").value,
		not3: document.getElementById("not3").value,
		yetkili: document.getElementById("yetkili").value,
		id: document.getElementById("adrid").value
	};
}

async function adraramaYap() {
	const aramaInput = document.getElementById("arama").value;
	document.getElementById("kodKontrol").innerText = "" ;
	if (!aramaInput || aramaInput === "") {
		return;
	}
	document.body.style.cursor = "wait";
	try {

		const response = await fetchWithSessionCheck("adres/adresArama", {
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
		document.getElementById("unvan").value = dto.unvan || "";
		document.getElementById("ad1").value = dto.ad1 || "";
		document.getElementById("semt").value = dto.semt || "";
		document.getElementById("ad2").value = dto.ad2 || "";
		document.getElementById("seh").value = dto.seh || "";
		document.getElementById("pkodu").value = dto.pkodu || "";
		document.getElementById("smsgon").checked = dto.smsgon;
		document.getElementById("mailgon").checked = dto.mailgon;
		document.getElementById("vd").value = dto.vd || "";
		document.getElementById("vn").value = dto.vn || "";
		document.getElementById("t1").value = dto.t1 || "";
		document.getElementById("t2").value = dto.t2 || "";
		document.getElementById("t3").value = dto.t3 || "";
		document.getElementById("fx").value = dto.fx || "";
		document.getElementById("o1").value = dto.o1 || "";
		document.getElementById("o2").value = dto.o2 || "";
		document.getElementById("web").value = dto.web || "";
		document.getElementById("ozel").value = dto.ozel || "";
		document.getElementById("mail").value = dto.mail || "";
		document.getElementById("acik").value = dto.acik || "";
		document.getElementById("not1").value = dto.not1 || "";
		document.getElementById("not2").value = dto.not2 || "";
		document.getElementById("not3").value = dto.not3 || "";
		document.getElementById("yetkili").value = dto.yetkili || "";
		document.getElementById("adrid").value = dto.id || "";

		const imgElement = document.getElementById("resimGoster");
		if (dto.base64Resim && dto.base64Resim.trim() !== "") {
			const base64String = 'data:image/jpeg;base64,' + dto.base64Resim.trim();
			imgElement.src = base64String;
			imgElement.style.display = "block";
		} else {
			imgElement.src = "";
			imgElement.style.display = "none";
		}
		adrdisableInputs();
		adrenableDuzeltmeInputs();

		
		document.getElementById("errorDiv").style.display = "none";
		document.getElementById("errorDiv").innerText = "";
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function adrGeri() {
	const arama = document.getElementById("kodu").value;
	const datalist = document.getElementById("hesapOptions");
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
			adraramaYap();
			document.getElementById("arama").value = "";
		} else {
			return null; // İlk index ise geri null döndür
		}
	} else {
		return null; // Değer bulunamazsa null döndür
	}
}

function adrIleri() {
	const arama = document.getElementById("kodu").value; // Input alanının değerini al
	const datalist = document.getElementById("hesapOptions"); // Datalist öğesini seç
	const options = datalist.getElementsByTagName("option"); // Datalist içindeki option'ları al

	if (options.length === 0) {
		return null;
	}
	let index = -1; // Varsayılan olarak bulunamadı
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
			adraramaYap();
			document.getElementById("arama").value = "";
		} else {
			return null; // Son index ise geri null döndür
		}
	} else {
		return null; // Değer bulunamazsa null döndür
	}
}

function adrIlk() {
	const datalist = document.getElementById("hesapOptions");
	const options = datalist.getElementsByTagName("option");

	if (options.length === 0) {
		adrclearInputs();
		adrdisableInputs();
		return null;
	}
	const firstValue = options[0].value;
	document.getElementById("arama").value = firstValue;
	adraramaYap();
	document.getElementById("arama").value = "";
}

//************************evrak sil ***********************************************
async function adrSil() {
	const adrId = document.getElementById("adrid").value
	if (["0", ""].includes(adrId.value)) {
		return;
	}
	const message = "Kayit Dosyadan Silinecek ..?";
	const confirmDelete = confirm(message);
	if (!confirmDelete) {
		return;
	}
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("adres/adrSil", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ adrId: adrId }),
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

function resimSil() {
	const imgElementLogo = document.getElementById("resimGoster");
	imgElementLogo.src = "";
	imgElementLogo.style.display = "none";

}