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
					$('#ara_content').html('<h2>Bir hata oluştu: ' + xhr.statusText + '</h2>');
				},
				complete: function () {
					document.body.style.cursor = "default";
				},
			});
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}