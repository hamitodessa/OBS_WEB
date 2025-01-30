async function dekhesapAdiOgren(inputId, targetLabelId1, targetLabelId2) {
	const inputValue = document.getElementById(inputId).value;
	document.body.style.cursor = "wait";

	if (!inputValue) {
		document.getElementById(targetLabelId1).innerText = "";
		document.getElementById(targetLabelId2).innerText = "";
		document.body.style.cursor = "default";
		return;
	}

	try {
		const response = await fetchWithSessionCheck("cari/hesapadi", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ hesapkodu: inputValue }),
		});

		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		document.getElementById(targetLabelId1).innerText = response.hesapAdi || "Hesap adı bulunamadı";
		document.getElementById(targetLabelId2).innerText = response.hesapCinsi || "";
	} catch (error) {
		document.getElementById(targetLabelId1).innerText = "Hata oluştu!";
		document.getElementById(targetLabelId2).innerText = "";
	} finally {
		document.body.style.cursor = "default";
	}
}

function dekclearInput(input) {
	input.value = ""; // Kullanıcı odaklandığında alanı temizle
}
function dekclearInputs() {
	const borcHesap = document.getElementById('borcHesap');
	dekclearInput(borcHesap);
	const borcluAdi = document.getElementById('borcluAdi');
	borcluAdi.innerText = "";

	const borcluHesapCins = document.getElementById('borcluHesapCins');
	borcluHesapCins.innerText = "";

	const borcKurCins = document.getElementById('borcKurCins');
	borcKurCins.value = "";
	const borcKur = document.getElementById('borcKur');
	borcKur.value = "1.0000";
	const borcTutar = document.getElementById('borcTutar');
	borcTutar.value = "0.00";

	const alacakHesap = document.getElementById('alacakHesap');
	dekclearInput(alacakHesap);
	const alacakliAdi = document.getElementById('alacakliAdi');
	alacakliAdi.innerText = "";

	const alacakliHesapCins = document.getElementById('alacakliHesapCins');
	alacakliHesapCins.innerText = "";

	const alacakKurCins = document.getElementById('alacakKurCins');
	alacakKurCins.value = "";
	const alacakKur = document.getElementById('alacakKur');
	alacakKur.value = "1.0000";
	const alacakTutar = document.getElementById('alacakTutar');
	alacakTutar.value = "0.00";

	const aciklama = document.getElementById('aciklama');
	dekclearInput(aciklama);
	const kodu = document.getElementById('kodu');
	dekclearInput(kodu);
}

function dekenableInputs() {
	const borcHesap = document.getElementById('borcHesap');
	borcHesap.removeAttribute('disabled');
	const borcKurCins = document.getElementById('borcKurCins');
	borcKurCins.removeAttribute('disabled');
	const borcKur = document.getElementById('borcKur');
	borcKur.removeAttribute('disabled');
	const borcTutar = document.getElementById('borcTutar');
	borcTutar.removeAttribute('disabled');

	const alacakHesap = document.getElementById('alacakHesap');
	alacakHesap.removeAttribute('disabled');
	const alacakKurCins = document.getElementById('alacakKurCins');
	alacakKurCins.removeAttribute('disabled');
	const alacakKur = document.getElementById('alacakKur');
	alacakKur.removeAttribute('disabled');
	const alacakTutar = document.getElementById('alacakTutar');
	alacakTutar.removeAttribute('disabled');

	const aciklama = document.getElementById('aciklama');
	aciklama.removeAttribute('disabled');
	const kodu = document.getElementById('kodu');
	kodu.removeAttribute('disabled');
}

function dekdisableInputs() {
	const borcHesap = document.getElementById('borcHesap');
	if (borcHesap) borcHesap.setAttribute('disabled', 'disabled');

	const borcKurCins = document.getElementById('borcKurCins');
	if (borcKurCins) borcKurCins.setAttribute('disabled', 'disabled');

	const borcKur = document.getElementById('borcKur');
	if (borcKur) borcKur.setAttribute('disabled', 'disabled');

	const borcTutar = document.getElementById('borcTutar');
	if (borcTutar) borcTutar.setAttribute('disabled', 'disabled');

	const alacakHesap = document.getElementById('alacakHesap');
	if (alacakHesap) alacakHesap.setAttribute('disabled', 'disabled');

	const alacakKurCins = document.getElementById('alacakKurCins');
	if (alacakKurCins) alacakKurCins.setAttribute('disabled', 'disabled');

	const alacakKur = document.getElementById('alacakKur');
	if (alacakKur) alacakKur.setAttribute('disabled', 'disabled');

	const alacakTutar = document.getElementById('alacakTutar');
	if (alacakTutar) alacakTutar.setAttribute('disabled', 'disabled');

	const aciklama = document.getElementById('aciklama');
	if (aciklama) aciklama.setAttribute('disabled', 'disabled');

	const kodu = document.getElementById('kodu');
	if (kodu) kodu.setAttribute('disabled', 'disabled');
}
function tutarKontrolAlacak() {
	const borcTutarRaw = document.getElementById('borcTutar').value.trim();
	const borcKurRaw = document.getElementById('borcKur').value.trim();
	const alacakKurRaw = document.getElementById('alacakKur').value.trim();

	const borcTutar = parseFloat(borcTutarRaw.replace(/,/g, '').replace(/\s/g, '')) || 0;
	const borcKur = parseFloat(borcKurRaw.replace(/,/g, '').replace(/\s/g, '')) || 1;
	const alacakKur = parseFloat(alacakKurRaw.replace(/,/g, '').replace(/\s/g, '')) || 1;
	const alacakTutar = document.getElementById('alacakTutar');

	if (isNaN(borcTutar) || isNaN(borcKur) || isNaN(alacakKur) || borcKur <= 0 || alacakKur <= 0) {
		alert("Lütfen geçerli ve sıfırdan büyük değerler girin.");
		return;
	}

	if (alacakKur !== 1) {
		if (borcKur !== 1) {
			alacakTutar.value = ((borcTutar * borcKur) / alacakKur).toFixed(2); // Sonuç 2 ondalık basamak
		} else {
			alacakTutar.value = (borcTutar / alacakKur).toFixed(2); // Sonuç 2 ondalık basamak
		}
	} else {
		alacakTutar.value = (borcTutar * borcKur).toFixed(2); // Sonuç 2 ondalık basamak
	}
}

function tutarKontrolBorc() {
	const borcTutar = document.getElementById('borcTutar');
	const borcKurRaw = document.getElementById('borcKur').value.trim();
	const alacakTutarRaw = document.getElementById('alacakTutar').value.trim();
	const alacakKurRaw = document.getElementById('alacakKur').value.trim();

	const borcKur = parseFloat(borcKurRaw.replace(/,/g, '').replace(/\s/g, '')) || 1;
	const alacakTutar = parseFloat(alacakTutarRaw.replace(/,/g, '').replace(/\s/g, '')) || 0;
	const alacakKur = parseFloat(alacakKurRaw.replace(/,/g, '').replace(/\s/g, '')) || 1;

	if (isNaN(borcKur) || isNaN(alacakTutar) || isNaN(alacakKur) || borcKur <= 0 || alacakKur <= 0) {
		alert("Lütfen geçerli ve sıfırdan büyük değerler girin.");
		return;
	}
	if (borcKur !== 1) {
		if (alacakKur !== 1) {
			borcTutar.value = ((alacakTutar * alacakKur) / borcKur).toFixed(2); // Sonuç 2 ondalık basamak
		} else {
			borcTutar.value = (alacakTutar / borcKur).toFixed(2); // Sonuç 2 ondalık basamak
		}
	} else {
		borcTutar.value = (alacakTutar * alacakKur).toFixed(2); // Sonuç 2 ondalık basamak
	}
}

function dekyenifis() {
	dekclearInputs();
	dekenableInputs();
	dekyenifisNo();
}

//************************************sonfisno *****************************************************
async function deksonfisNo() {
	try {
		const response = await fetchWithSessionCheck('cari/sonfisNo', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			}
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const data = response;
		const fisNoInput = document.getElementById('evrakNo');
		const errorDiv = document.getElementById('errorDiv');

		fisNoInput.value = data.fisNo;
		if (data.fisNo === 0) {
			alert('Hata: Evrak numarası bulunamadı.');
			errorDiv.innerText = data.errorMessage;
			return;
		}
		if (data.errorMessage) {
			errorDiv.innerText = data.errorMessage;
			errorDiv.style.display = 'block';
		} else {
			errorDiv.style.display = 'none';
			dekevrakOku();
		}
	} catch (error) {
		const errorDiv = document.getElementById('errorDiv');
		errorDiv.style.display = 'block';
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	}
}

//********************************gerifisno ***********************************************
function dekgerifisNo() {
	const fisNoInput = document.getElementById('evrakNo');
	const errorDiv = document.getElementById('errorDiv');
	if (fisNoInput) {
		const currentValue = parseInt(fisNoInput.value, 10) || 0; // Eğer değer geçersizse 0 kabul edilir
		if (currentValue <= 0) {
			return;
		}
		fisNoInput.value = currentValue - 1;
		dekevrakOku();
	} else {
		errorDiv.innerText = "Hata: 'evrakNo' elemanı bulunamadı.";
	}
}
//******************************** ileri fis **********************************************
function dekilerifisNo() {
	const fisNoInput = document.getElementById('evrakNo');
	const errorDiv = document.getElementById('errorDiv');
	if (fisNoInput) {
		const currentValue = parseInt(fisNoInput.value, 10) || 0;
		fisNoInput.value = currentValue + 1;
		dekevrakOku();
	} else {
		errorDiv.innerText = "Hata: 'evrakNo' elemanı bulunamadı.";
	}
}
//********************************** fis kayit ********************************************
async function dekfisKayit() {
	const fisNoInput = document.getElementById('evrakNo');
	const errorDiv = document.getElementById("errorDiv");
	const $kaydetButton = $('#dekkaydetButton');
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	if (["0", ""].includes(fisNoInput.value)) {
		return;
	}
	const dekontDTO = {
		fisNo: fisNoInput.value,
		tar: getFullDateWithTimeAndMilliseconds(document.getElementById("dekontTarih").value),
		bhes: document.getElementById("borcHesap").value,
		bcins: document.getElementById("borcKurCins").value,
		bkur: parseLocaleNumber(document.getElementById("borcKur").value),
		borc: parseLocaleNumber(document.getElementById("borcTutar").value),
		ahes: document.getElementById("alacakHesap").value,
		acins: document.getElementById("alacakKurCins").value,
		akur: parseLocaleNumber(document.getElementById("alacakKur").value),
		alacak: parseLocaleNumber(document.getElementById("alacakTutar").value),
		izahat: document.getElementById("aciklama").value,
		kod: document.getElementById("kodu").value,
	};
	document.body.style.cursor = "wait";
	$kaydetButton.prop('disabled', true).text('İşleniyor...');
	try {
		const response = await fetchWithSessionCheck("cari/fiskayit", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(dekontDTO),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		dekclearInputs();
		dekdisableInputs();
		fisNoInput.value = "0";
		//		setTimeout(() => {
		//			alert(response.message); // Başarı mesajını göster
		//		}, 100);

	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
		$kaydetButton.prop('disabled', false).text('Kaydet');
	}
}
//******************************** yeni fis no ********************************************
async function dekyenifisNo() {
	try {
		const response = await fetchWithSessionCheck('cari/yenifisNo', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			}
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const data = response;
		const fisNoInput = document.getElementById('evrakNo');
		const errorDiv = document.getElementById('errorDiv');
		fisNoInput.value = data.fisNo;
		errorDiv.style.display = 'none';
		errorDiv.innerText = "";
		dekclearInputs();
		dekenableInputs();
	} catch (error) {
		errorDiv.innerText = error;
		errorDiv.style.display = 'block';
	}
}
//*****************************evrak kontrol **********************************************
function checkEnter(event) {
	if (event.key === "Enter" || event.keyCode === 13) {
		dekevrakOku();
	}
}
async function dekevrakOku() {
	const evrakNo = document.getElementById("evrakNo").value;
	if (!evrakNo || evrakNo === "0") {
		alert("Lütfen geçerli bir evrak numarası girin!");
		return;
	}

	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";

	try {
		const response = await fetchWithSessionCheck("cari/evrakOku", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ evrakNo: evrakNo }),
		});
		const data = response;
		dekclearInputs();
		const dto = data[0];
		if (dto.errorMessage) {
			errorDiv.style.display = "block";
			errorDiv.innerText = dto.errorMessage;
			dekdisableInputs();
			return;
		}
		// İlk DTO'daki bilgileri doldur
		document.getElementById("dekontTarih").value = dto.tar || "";
		document.getElementById("borcHesap").value = dto.bhes || "";
		document.getElementById("borcKurCins").value = dto.bcins || "";
		document.getElementById("borcKur").value = formatNumber4(dto.bkur);
		document.getElementById("borcTutar").value = formatNumber2(dto.borc);

		// İkinci DTO'daki bilgileri doldur
		const dto2 = data[1];
		document.getElementById("alacakHesap").value = dto2?.ahes || "";
		document.getElementById("alacakKurCins").value = dto2?.acins || "";
		document.getElementById("alacakKur").value = formatNumber4(dto2.akur);
		document.getElementById("alacakTutar").value = formatNumber2(dto2.alacak);

		document.getElementById("aciklama").value = dto2?.izahat || "";
		document.getElementById("kodu").value = dto2?.kod || "";

		// Alanların yeniden etkinleştirilmesi
		const bhes = document.getElementById("borcHesap");
		bhes.oninput();
		const ahes = document.getElementById("alacakHesap");
		ahes.oninput();
		dekenableInputs();
		// Hata divini temizle
		errorDiv.style.display = "none";
		errorDiv.innerText = "";
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
		dekdisableInputs();
	} finally {
		document.body.style.cursor = "default";
	}
}
//************************evrak sil ***********************************************
async function dekfisYoket() {
	const fisNoInput = document.getElementById('evrakNo');
	if (["0", ""].includes(fisNoInput.value)) {
		return;
	}
	const confirmDelete = confirm("Bu evrak numarasını silmek istediğinize emin misiniz?");
	if (!confirmDelete) {
		return;
	}
	const dekontNo = parseInt(fisNoInput.value, 10);


	document.body.style.cursor = "wait";
	const $silButton = $('#deksilButton');
	$silButton.prop('disabled', true).text('Siliniyor...');
	try {
		const response = await fetchWithSessionCheck("cari/fisYoket", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ evrakNo: dekontNo }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		//		const data = response;

		dekclearInputs();
		dekdisableInputs();
		document.getElementById('evrakNo').value = "0";
		$silButton.prop('disabled', false).text('Sil');

		document.getElementById("errorDiv").style.display = "none";
		document.getElementById("errorDiv").innerText = "";

		// Başarı mesajı
		//		setTimeout(() => {
		//			alert(data.message || "Silme işlemi başarılı!");
		//		}, 100);
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
		$silButton.prop('disabled', false).text('Sil');
	}
}
//**************************************************************
function allowOnlyNumbers(event) {
	const allowedKeys = ["Backspace", "Tab", "ArrowLeft", "ArrowRight", "Delete", "Enter"];
	if (allowedKeys.includes(event.key)) {
		return;
	}
	if (!/^[0-9]$/.test(event.key)) {
		event.preventDefault();
	}
}

async function dekkurOku(dvz_turu, cinsElement, targetInput) {
	const dvz_tur = document.getElementById(dvz_turu)?.textContent;
	const cin = cinsElement.value;
	const yazilacakInput = document.getElementById(targetInput);

	const tar = document.getElementById("dekontTarih").value;
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	const kurgirisDTO = {
		dvz_cins: cin,
		dvz_turu: dvz_tur,
		tar: tar
	};
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("kur/kuroku", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(kurgirisDTO),
		});
		const data = response;
		if (data.success && data.data.length > 0) {
			const item = data.data[0];
			yazilacakInput.value = formatNumber4(item[cin]);
		} else {
			yazilacakInput.value = "1.0000";
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.";
	} finally {
		document.body.style.cursor = "default";
	}
}