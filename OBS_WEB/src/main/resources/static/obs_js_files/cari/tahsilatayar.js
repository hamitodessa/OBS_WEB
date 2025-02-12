
function clearInputs() {
	const inputs = document.querySelectorAll('.form-control');
	inputs.forEach(input => {
		input.value = '';
	});
	const imgElementLogo = document.getElementById("resimLogo");
	imgElementLogo.src = "";
	imgElementLogo.style.display = "none";

	const imgElementKase = document.getElementById("resimKase");
	imgElementKase.src = "";
	imgElementKase.style.display = "none";
}

async function ayarKayit() {
	const adi = document.getElementById('adi');
	if ([""].includes(adi.value)) {
		return;
	}
	const tahayarDTO = gettahayarDTO();
	const formData = new FormData();

	for (const key in tahayarDTO) {
		formData.append(key, tahayarDTO[key]);
	}

	const fileInputLogo = document.getElementById("imageLogo");
	const fileLogo = fileInputLogo.files[0];
	if (fileLogo) {
		formData.append("resimlogo", fileLogo);
	}

	const imgElementLogo = document.getElementById("resimLogo");
	let base64DataLogo = imgElementLogo.src.startsWith("data:image")
		? imgElementLogo.src.split(",")[1]
		: null;

	if (base64DataLogo) {
		const byteCharacters = atob(base64DataLogo); // Base64'ü çöz
		const byteNumbers = new Array(byteCharacters.length);
		for (let i = 0; i < byteCharacters.length; i++) {
			byteNumbers[i] = byteCharacters.charCodeAt(i);
		}
		const byteArray = new Uint8Array(byteNumbers);
		const blob = new Blob([byteArray], { type: "image/jpeg" });
		formData.append("resimgosterlogo", blob, "base64ResimLogo.jpg"); // Blob olarak ekle
	}


	const fileInputKase = document.getElementById("imageKase");
	const fileKase = fileInputKase.files[0];
	if (fileKase) {
		formData.append("resimkase", fileKase);
	}

	const imgElementKase = document.getElementById("resimKase");
	let base64DataKase = imgElementKase.src.startsWith("data:image")
		? imgElementKase.src.split(",")[1]
		: null;

	if (base64DataKase) {
		const byteCharactersKase = atob(base64DataKase); // Base64'ü çöz
		const byteNumbersKase = new Array(byteCharactersKase.length);
		for (let i = 0; i < byteCharactersKase.length; i++) {
			byteNumbersKase[i] = byteCharactersKase.charCodeAt(i);
		}
		const byteArray = new Uint8Array(byteNumbersKase);
		const blob = new Blob([byteArray], { type: "image/jpeg" });
		formData.append("resimgosterkase", blob, "base64ResimKase.jpg"); // Blob olarak ekle
	}

	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";
	errorDiv.style.display = "none";
	errorDiv.innerText = "";

	try {
		const response = await fetchWithSessionCheck("cari/tahayarkayit", {
			method: "POST",
			body: formData
		});

		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		document.body.style.cursor = "default";
		tahayarIlk();
	} catch (error) {
		document.body.style.cursor = "default";
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message;
	}
}

function gettahayarDTO() {
	return {
		adi: document.getElementById("adi").value,
		ad1: document.getElementById("adr1").value,
		ad2: document.getElementById("adr2").value,
		vdvn: document.getElementById("vdvn").value,
		mail: document.getElementById("mail").value,
		diger: document.getElementById("diger").value
	};
}

async function tahayarIlk() {
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("cari/tahayarYukle", {
			method: "POST",
			headers: {
				"Content-Type": "application/x-www-form-urlencoded"
			},
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const dto = response;
		if (dto.errorMessage) {
			throw new Error(dto.errorMessage);
		}
		clearInputs();
		document.getElementById("adi").value = dto.adi;
		document.getElementById("adr1").value = dto.ad1 || "";
		document.getElementById("adr2").value = dto.ad2 || "";
		document.getElementById("vdvn").value = dto.vdvn || "";
		document.getElementById("mail").value = dto.mail || "";
		document.getElementById("diger").value = dto.diger || "";
		const imgElementlogo = document.getElementById("resimLogo");
		if (dto.base64Resimlogo && dto.base64Resimlogo.trim() !== "") {
			const base64Stringlogo = 'data:image/jpeg;base64,' + dto.base64Resimlogo.trim();
			imgElementlogo.src = base64Stringlogo;
			imgElementlogo.style.display = "block";
		} else {
			imgElementlogo.src = "";
			imgElementlogo.style.display = "none";
		}

		const imgElementkase = document.getElementById("resimKase");
		if (dto.base64Resimkase && dto.base64Resimkase.trim() !== "") {
			const base64Stringkase = 'data:image/jpeg;base64,' + dto.base64Resimkase.trim();
			imgElementkase.src = base64Stringkase;
			imgElementkase.style.display = "block";
		} else {
			imgElementkase.src = "";
			imgElementkase.style.display = "none";
		}
		document.getElementById("errorDiv").style.display = "none";
		document.getElementById("errorDiv").innerText = "";
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function logoSil() {
	const imgElementLogo = document.getElementById("resimLogo");
	imgElementLogo.src = "";
	imgElementLogo.style.display = "none";

}
function kaseSil() {
	const imgElementLogo = document.getElementById("resimKase");
	imgElementLogo.src = "";
	imgElementLogo.style.display = "none";

}