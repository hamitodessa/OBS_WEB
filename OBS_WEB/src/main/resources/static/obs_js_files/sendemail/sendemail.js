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
		format: document.getElementById("format") ? document.getElementById("format").value : ""
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
		document.body.style.cursor = "default";
		$mailButton.prop('disabled', false).text('Gönder');
	}
}