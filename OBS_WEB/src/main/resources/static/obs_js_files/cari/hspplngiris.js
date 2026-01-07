function hspenableInputs() {
	hspclearInputs();
	const inputs = document.querySelectorAll('.form-control');
	inputs.forEach(input => {
		input.disabled = false;
	});
}

function hspenableDuzeltmeInputs() {
	const inputs = document.querySelectorAll('.form-control');
	inputs.forEach(input => {
		if (input.id !== "kodu") {
			input.disabled = false;
		}
	});
}

function hspdisableInputs() {
	const inputs = document.querySelectorAll('.form-control');
	inputs.forEach(input => {
		if (input.id !== "arama") {
			input.disabled = true;
		}
	});
}

function hspclearInputs() {
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
	const imgElement = document.getElementById("resimGoster");
	imgElement.src = "";
	imgElement.style.display = "none";
	document.getElementById("kodKontrol").innerText = "" ;
	
	const fileInput = document.getElementById("resim");
	    fileInput.value = "";  
}

async function hsplnKayit() {
	const koduInput = document.getElementById('kodu');
	if (["0", ""].includes(koduInput.value)) {
		return;
	}
	const hesapplaniDTO = getHesapPlaniDTO();
	const formData = new FormData();
	for (const key in hesapplaniDTO) {
		formData.append(key, hesapplaniDTO[key]);
	}
	const fileInput = document.getElementById("resim");
	const file = fileInput.files[0];
	if (file) {
		formData.append("resim", file);
	}
	const imgElement = document.getElementById("resimGoster");
	let base64Data = imgElement.src.startsWith("data:image") ? imgElement.src.split(",")[1]	: null;
	if (base64Data) {
		const byteCharacters = atob(base64Data);
		const byteNumbers = new Array(byteCharacters.length);
		for (let i = 0; i < byteCharacters.length; i++) {
			byteNumbers[i] = byteCharacters.charCodeAt(i);
		}
		const byteArray = new Uint8Array(byteNumbers);
		const blob = new Blob([byteArray], { type: "image/jpeg" });
		formData.append("resimGoster", blob, "base64Resim.jpg");
	}
	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("cari/hsplnkayit", {
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

function getHesapPlaniDTO() {
	return {
		kodu: document.getElementById("kodu").value,
		adi: document.getElementById("adi").value,
		karton: document.getElementById("karton").value,
		hcins: document.getElementById("hcins").value,
		yetkili: document.getElementById("yetkili").value,
		ad1: document.getElementById("ad1").value,
		ad2: document.getElementById("ad2").value,
		semt: document.getElementById("semt").value,
		seh: document.getElementById("seh").value,
		vd: document.getElementById("vd").value,
		vn: document.getElementById("vn").value,
		t1: document.getElementById("t1").value,
		t2: document.getElementById("t2").value,
		t3: document.getElementById("t3").value,
		fx: document.getElementById("fx").value,
		o1: document.getElementById("o1").value,
		o2: document.getElementById("o2").value,
		o3: document.getElementById("o3").value,
		web: document.getElementById("web").value,
		mail: document.getElementById("mail").value,
		kim: document.getElementById("kim").value,
		acik: document.getElementById("acik").value,
		sms: document.getElementById("sms").checked
	};
}

async function hsparamaYap() {
	document.getElementById("kodKontrol").innerText = "" ;
	const aramaInput = document.getElementById("arama").value;
	if (!aramaInput || aramaInput === "") {
		return;
	}
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("cari/hsplnArama", {
			method: "POST",
			headers: {
				"Content-Type": "application/x-www-form-urlencoded"
			},
			body: new URLSearchParams({ arama: aramaInput })
		});
		const dto = await response;
		if (dto.errorMessage === "Bu Numarada Kayıtlı Hesap Yok") {
			document.getElementById("errorDiv").innerText = dto.errorMessage;
			return;
		}
		document.getElementById("kodu").value = dto.kodu;
		document.getElementById("adi").value = dto.adi || "";
		document.getElementById("karton").value = dto.karton || "";
		document.getElementById("hcins").value = dto.hcins || "";
		document.getElementById("yetkili").value = dto.yetkili || "";
		document.getElementById("ad1").value = dto.ad1 || "";
		document.getElementById("ad2").value = dto.ad2 || "";
		document.getElementById("semt").value = dto.semt || "";
		document.getElementById("seh").value = dto.seh || "";
		document.getElementById("vd").value = dto.vd || "";
		document.getElementById("vn").value = dto.vn || "";
		document.getElementById("t1").value = dto.t1 || "";
		document.getElementById("t2").value = dto.t2 || "";
		document.getElementById("t3").value = dto.t3 || "";
		document.getElementById("fx").value = dto.fx || "";
		document.getElementById("o1").value = dto.o1 || "";
		document.getElementById("o2").value = dto.o2 || "";
		document.getElementById("o3").value = dto.o3 || "";
		document.getElementById("web").value = dto.web || "";
		document.getElementById("mail").value = dto.mail || "";
		document.getElementById("kim").value = dto.kim || "";
		document.getElementById("acik").value = dto.acik || "";
		document.getElementById("sms").checked = dto.sms;

		const imgElement = document.getElementById("resimGoster");
		
		const fileInput = document.getElementById("resim");
		    fileInput.value = "";  
		if (dto.base64Resim && dto.base64Resim.trim() !== "") {
			const base64String = 'data:image/jpeg;base64,' + dto.base64Resim.trim();
			imgElement.src = base64String;
			imgElement.style.display = "block";
		} else {
			imgElement.src = "";
			imgElement.style.display = "none";
		}
		hspdisableInputs();
		hspenableDuzeltmeInputs();

		document.getElementById("errorDiv").style.display = "none";
		document.getElementById("errorDiv").innerText = "";
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function hsplnGeri() {
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
			hsparamaYap();
			document.getElementById("arama").value = "";
		} else {
			return null;
		}
	} else {
		return null;
	}
}

function hsplnIleri() {
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
		if (index < options.length - 1) {
			const nextValue = options[index + 1].value;
			document.getElementById("arama").value = nextValue;
			hsparamaYap();
			document.getElementById("arama").value = "";
		} else {
			return null;
		}
	} else {
		return null;
	}
}

function hsplnIlk() {
	const datalist = document.getElementById("hesapOptions");
	const options = datalist.getElementsByTagName("option"); 
	if (options.length === 0) {
		hspclearInputs();
		hspdisableInputs();
		return null;
	}
	const firstValue = options[0].value;
	document.getElementById("arama").value = firstValue;
	hsparamaYap();
	document.getElementById("arama").value = "";
}

async function hsphesapSil() {
	const hesapKodu = document.getElementById('kodu').value;
	if (["0", ""].includes(hesapKodu.value)) {
		return;
	}
	const message = "Kayit Dosyadan Silinecek ..?\n\n" +
		"Oncelikle Bu Hesaba Ait Fisleri Silmeniz\n\n" +
		"Tavsiye Olunur ....";
	const confirmDelete = confirm(message);
	if (!confirmDelete) {
		return;
	}
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("cari/hspplnSil", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ hesapKodu: hesapKodu }),
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
	const url = "cari/hspplngiris";
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
			cariBaslik();
			hsparamaYap();
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