async function raporYoket(raporid) {
	const message = "Kayit Dosyadan Silinecek ..?";
	const confirmDelete = confirm(message);
	if (!confirmDelete) {
		return;
	}
	document.body.style.cursor = "wait";
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	try {
		const response = await fetchWithSessionCheck("user/raporSil", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ raporId: raporid }),
		});
		if (response && response.errorMessage && response.errorMessage.trim() !== "") {
			errorDiv.style.display = "block";
			errorDiv.innerText = response.errorMessage || "Bir hata oluştu.";
		} else {
			const url = "user/gidenraporlar";
			if (window.sayfaYukle) window.sayfaYukle(url);
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}