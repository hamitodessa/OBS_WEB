bankaIsimleri = [];
subeIsimleri = [];
ilkBorclu = [];
allSubjects = [];
depolar = [];
ozelkodlar = [];
urnkodlar = [];
rowCounter = 0;
lastFocusedRow = null;
tablobaslik = "";
currentPage = 0 ;
pageSize = 0;
totalPages = 1;





incrementRowCounter = function () {
	rowCounter++;
};

formatInputBox2 = function (input) {
	input.value = formatNumber2(parseLocaleNumber(input.value));
}

formatInputBox4 = function (input) {
	input.value = formatNumber4(parseLocaleNumber(input.value));
}

formatNumber0 = function (value) {
	if (value == null) return '';
	return parseFloat(value).toLocaleString(undefined, {
		minimumFractionDigits: 0,
		maximumFractionDigits: 0
	});
}

formatNumber2 = function (value) {
	if (value == null) return '';
	return parseFloat(value).toLocaleString(undefined, {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	});
}

formatNumber3 = function (value) {
	if (value == null) return '';
	return parseFloat(value).toLocaleString(undefined, {
		minimumFractionDigits: 3,
		maximumFractionDigits: 3
	});
}

formatNumber4 = function (value) {
	if (value == null) return '';
	return parseFloat(value).toLocaleString(undefined, {
		minimumFractionDigits: 4,
		maximumFractionDigits: 4
	});
}

formatDate = function (dateString) {
	if (!dateString) return '';
	const date = new Date(dateString);
	const day = String(date.getDate()).padStart(2, '0');
	const month = String(date.getMonth() + 1).padStart(2, '0');
	const year = date.getFullYear();
	return `${day}.${month}.${year}`;
}

function formatTarihsqlite(tarihStr) {
    if (!tarihStr) return "";
    try {
        const tarih = new Date(tarihStr.replace(" ", "T"));
        const gun = String(tarih.getDate()).padStart(2, "0");
        const ay = String(tarih.getMonth() + 1).padStart(2, "0");
        const yil = tarih.getFullYear();
        const saat = String(tarih.getHours()).padStart(2, "0");
        const dakika = String(tarih.getMinutes()).padStart(2, "0");
        const saniye = String(tarih.getSeconds()).padStart(2, "0");
        return `${gun}.${ay}.${yil} ${saat}:${dakika}:${saniye}`;
    } catch (e) {
        return tarihStr;
    }
}

getFullDateWithTimeAndMilliseconds = function (dateInput) {
	const now = new Date();
	const hours = now.getHours().toString().padStart(2, '0');
	const minutes = now.getMinutes().toString().padStart(2, '0');
	const seconds = now.getSeconds().toString().padStart(2, '0');
	const milliseconds = now.getMilliseconds().toString().padStart(3, '0');
	const fullDate = `${dateInput} ${hours}:${minutes}:${seconds}.${milliseconds}`;
	return fullDate;
}

formatdateSaatsiz = function (dateString) {
	const isoDate = dateString;
	const formattedDate = new Date(isoDate).toISOString().split('T')[0];
	return formattedDate;
}

validateAutoSearch = function (inputId, datalistId) {
	const inputElement = document.getElementById(inputId);
	const datalist = document.getElementById(datalistId);
	const inputValue = inputElement.value;
	const options = Array.from(datalist.options).map(option => option.value);
	if (!options.some(option => option.startsWith(inputValue))) {
		inputElement.setCustomValidity("Bu değer geçerli değil.");
	} else {
		inputElement.setCustomValidity("");
	}
	if (!options.includes(inputValue)) {
		inputElement.addEventListener('blur', function () {
			if (!options.includes(inputElement.value)) {
				inputElement.value = "";
			}
		});
	}
}

hesapAdiOgren = async function (inputId, targetLabelId1) {
	const inputValue = inputId;
	document.body.style.cursor = "wait";
	if (!inputValue) {
		document.getElementById(targetLabelId1).innerText = "";
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
	} catch (error) {
		document.getElementById(targetLabelId1).innerText = "Hata oluştu!";
	} finally {
		document.body.style.cursor = "default";
	}
}

adrhesapAdiOgren = async function (inputId, targetLabelId1) {
	const inputValue = document.getElementById(inputId).value;
	document.body.style.cursor = "wait";
	if (!inputValue) {
		document.getElementById(targetLabelId1).innerText = "";
		document.body.style.cursor = "default";
		return;
	}
	try {
		const response = await fetchWithSessionCheck("adr/hesapadi", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ hesapkodu: inputValue }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		document.getElementById(targetLabelId1).innerText = response.hesapAdi;
	} catch (error) {
		document.getElementById(targetLabelId1).innerText = "Hata oluştu!";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function mailsayfasiYukle(url) {
	if (window.sayfaYukle) window.sayfaYukle(url);
	/*
	try {
		$('html, body').css('cursor', 'wait');
		$.ajax({
			url: url,
			type: "GET",
			success: function (data) {
				if (data.includes('<form') && data.includes('name="username"')) {
					window.location.href = "/login";
				} else {
					$('#ara_content').html(data);
				}
			},
			error: function (xhr) {
				if (xhr.status === 401) {
					window.location.href = "/login";
				} else {
					$('#ara_content').html('<h2>Bir hata oluştu: ' + xhr.status + ' - ' + xhr.statusText + '</h2>');
				}
			},
			complete: function () {
				//$('body').css('cursor', 'default');
			}
		});
	} catch (error) {
		$('#ara_content').html(
			`<h2>Bir hata oluştu: ${error.message}</h2>`
		);
	} finally {
		//$('body').css('cursor', 'default');
	}
	*/
}






async function cariBaslik() {
	try {
		const response = await fetchWithSessionCheck("cari/getBaslik");
		const data = response;
		if (data.errorMessage === "") {
			document.getElementById("baslik").innerText = data.baslik;
		} else {
			const errorDiv = document.getElementById("errorDiv");
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage;
		}
	} catch (error) {
		const errorDiv = document.getElementById("errorDiv");
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message;
	}
}

async function adresBaslik() {
	try {
		const response = await fetchWithSessionCheck("adres/getBaslik");
		const data = response;
		if (data.errorMessage === "") {
			document.getElementById("baslik").innerText = data.baslik;
		} else {
			const errorDiv = document.getElementById("errorDiv");
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage;
		}
	} catch (error) {
		const errorDiv = document.getElementById("errorDiv");
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message;
	}
}

async function kambiyoBaslik() {
	try {
		const response = await fetchWithSessionCheck("kambiyo/getBaslik");
		const data = response;
		if (data.errorMessage === "") {
			document.getElementById("baslik").innerText = data.baslik;
		} else {
			const errorDiv = document.getElementById("errorDiv");
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage;
		}
	} catch (error) {
		const errorDiv = document.getElementById("errorDiv");
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message;
	}
}

async function stokBaslik() {
	try {
		const response = await fetchWithSessionCheck("stok/getBaslik");
		const data = response;
		if (data.errorMessage === "") {
			document.getElementById("baslik").innerText = data.baslik;
		} else {
			const errorDiv = document.getElementById("errorDiv");
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage;
		}
	} catch (error) {
		const errorDiv = document.getElementById("errorDiv");
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message;
	}
}

async function keresteBaslik() {
	try {
		const response = await fetchWithSessionCheck("kereste/getBaslik");
		const data = response;
		if (data.errorMessage === "") {
			document.getElementById("baslik").innerText = data.baslik;
		} else {
			const errorDiv = document.getElementById("errorDiv");
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage;
		}
	} catch (error) {
		const errorDiv = document.getElementById("errorDiv");
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message;
	}
}

parseLocaleNumber = function (input) {
	if (!input) return 0;
	if (input.includes(",") && input.includes(".")) {
		if (input.indexOf(",") < input.indexOf(".")) {
			return parseFloat(input.replace(/,/g, ""));
		} else {
			return parseFloat(input.replace(/\./g, "").replace(/,/g, "."));
		}
	} else if (input.includes(",")) {
		return parseFloat(input.replace(/,/g, "."));
	} else {
		return parseFloat(input);
	}
}