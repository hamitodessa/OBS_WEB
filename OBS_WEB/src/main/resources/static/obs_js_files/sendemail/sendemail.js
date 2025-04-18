tabloyukle();
function tabloyukle() {
	document.body.style.cursor = "wait"
	document.getElementById("extraValue").value = '';
	let storedData = localStorage.getItem("tableData");
	let data = localStorage.getItem("grprapor");
	let tablobaslik = localStorage.getItem("tablobaslik");
	if (storedData) {
		let parsedData = JSON.parse(storedData);
		document.getElementById("extraValue").value = JSON.stringify(parsedData.rows);
		document.getElementById("format").value = "xlsx" ;
		document.getElementById("format").disabled = true;
	}
	if (data) {
		document.getElementById("grprapor").value = data;
		document.getElementById("tablobaslik").value = tablobaslik;
		document.getElementById("format").value = "xlsx" ;
		document.getElementById("format").disabled = true;
	}
	if (document.getElementById("degerler").value === "kercikis" || document.getElementById("degerler").value === "kergiris"
				|| document.getElementById("degerler").value === ""){
		document.getElementById("format").value = "xlsx" ;
		document.getElementById("format").disabled = true;
    }
	document.body.style.cursor = "default"
};

async function sendmailAt() {
	const RaporEmailDegiskenler = {
		hesap: document.getElementById("hesap").value || "",
		isim: document.getElementById("isim").value || "",
		too: document.getElementById("too").value || "",
		ccc: document.getElementById("ccc").value || "",
		konu: document.getElementById("konu").value || "",
		aciklama: document.getElementById("aciklama").value || "",
		nerden: document.getElementById("nerden").value || "",
		degerler: document.getElementById("degerler").value || "",
		baslik: document.getElementById("tablobaslik").value || "",
		format: document.getElementById("format") ? document.getElementById("format").value : ""
	}

	if (document.getElementById("degerler").value === "kercikis" || document.getElementById("degerler").value === "kergiris"){
		const keresteyazdirDTO = localStorage.getItem("keresteyazdirDTO");
		RaporEmailDegiskenler.keresteyazdirDTO = JSON.parse(keresteyazdirDTO);
    }

	if (document.getElementById("extraValue").value != "") {
		let extraValue = document.getElementById("extraValue").value;
		RaporEmailDegiskenler.exceList = JSON.parse(extraValue);
	}
	if (document.getElementById("grprapor").value != "") {
		let extraValue = document.getElementById("grprapor").value;
		RaporEmailDegiskenler.tableString = extraValue;
	}

	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	const fields = { hesap, isim, too, konu, aciklama };
	for (const [key, value] of Object.entries(fields)) {
		if (!value) {
			errorDiv.style.display = "block";
			errorDiv.innerText = `${key} alanı boş olamaz!`;
			return;
		}
	}
	const $mailButton = $('#mailButton');
	document.body.style.cursor = "wait";
	$mailButton.prop('disabled', true).text('Gönderiliyor...');
	try {
		const response = await fetchWithSessionCheck('send_email_gonder', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(RaporEmailDegiskenler)
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const data = response;
		if (data.success) {
			errorDiv.style.display = "block";
			errorDiv.style.color = "green";
			errorDiv.innerText = data.success;
		} else {
			errorDiv.style.display = "block";
			errorDiv.style.color = "red";
			errorDiv.innerText = data.error;
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.style.color = "red";
		errorDiv.innerText = error.message || "Bir hata oluştu.";
	} finally {
		localStorage.removeItem("tableData");
		localStorage.removeItem("grprapor");
		localStorage.removeItem("tablobaslik");
		localStorage.removeItem("keresteyazdirDTO");
		document.body.style.cursor = "default";
		$mailButton.prop('disabled', false).text('Gönder');
	}
}