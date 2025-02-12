function serversuperviserdurum() {
	const drm = document.getElementById("hangi_sql").value;
	const superviserDiv = document.getElementById("superviserdiv");
	if (drm === "PG SQL") {
		superviserDiv.style.visibility = "visible";
		superviserDiv.style.opacity = "1";
		superviserDiv.style.height = "auto";
		document.getElementById("superviser").value = "";
	} else {
		superviserDiv.style.visibility = "hidden";
		superviserDiv.style.opacity = "0";
		superviserDiv.style.height = "0";
	}
}

async function serverKontrol() {
	const serverBilgiDTO = {
		user_modul: document.getElementById("user_modul").value,
		hangi_sql: document.getElementById("hangi_sql").value,
		user_prog_kodu: document.getElementById("user_prog_kodu").value,
		user_server: document.getElementById("user_server").value,
		user_pwd_server: document.getElementById("user_pwd_server").value,
		user_ip: document.getElementById("user_ip").value,
		superviser: document.getElementById("superviser").value,
	};
	const errorDiv = document.getElementById("errorDiv");
	const dbButton = document.getElementById("dbKontrolButton");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	if (
		!serverBilgiDTO.user_prog_kodu ||
		!serverBilgiDTO.user_server ||
		!serverBilgiDTO.user_pwd_server ||
		!serverBilgiDTO.user_ip
	) {
		errorDiv.style.display = "block";
		errorDiv.innerText = "Lütfen tüm gerekli alanları doldurunuz.";
		return;
	}
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("server/serverkontrol", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(serverBilgiDTO),
		});
		const data = response;
		setTimeout(() => {
			if (data.serverDurum === "true") {
				setTimeout(() => {
					alert("Server Bağlantısı Sağlandı");
				}, 100);
				if (dbButton) dbButton.disabled = false;
			} else {
				setTimeout(() => {
					alert("Server Bağlantısı Sağlanamadı!");
				}, 100);
				if (dbButton) dbButton.disabled = true;
			}
		}, 100);
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Bilinmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function databaseKontrol() {
	const serverBilgiDTO = {
		user_modul: document.getElementById("user_modul").value,
		hangi_sql: document.getElementById("hangi_sql").value,
		user_prog_kodu: document.getElementById("user_prog_kodu").value,
		user_server: document.getElementById("user_server").value,
		user_pwd_server: document.getElementById("user_pwd_server").value,
		user_ip: document.getElementById("user_ip").value,
		superviser: document.getElementById("superviser").value,
	};
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	if (
		!serverBilgiDTO.user_prog_kodu ||
		!serverBilgiDTO.user_server ||
		!serverBilgiDTO.user_pwd_server ||
		!serverBilgiDTO.user_ip
	) {
		errorDiv.style.display = "block";
		errorDiv.innerText = "Lütfen tüm gerekli alanları doldurunuz.";
		return;
	}
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("server/dosyakontrol", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(serverBilgiDTO),
		});
		const data = response;
		if (data.dosyaDurum === "true") {
			document.body.style.cursor = "default";
			setTimeout(() => {
				alert("Dosya Veritabanında Mevcut");
			}, 100);
		} else {

			setTimeout(() => {
				document.body.style.cursor = "default";
			}, 100);
			const confirmCreate = confirm("Dosya Veritabanında Mevcut Değil. Oluşturulsun mu?");
			if (!confirmCreate) {
				return;
			} else {
				await createnewDB();
			}
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Bilinmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function createnewDB() {
	setTimeout(() => {
		document.body.style.cursor = "default";
	}, 100);
	let firmaadi = "";
	if (document.getElementById("user_modul").value != "Kur") {
		firmaadi = prompt("Firma Ismi Giriniz :");
		if (firmaadi === null) {
			firmaadi = "";
		}
	}
	setTimeout(() => {
		document.body.style.cursor = "wait";
	}, 100);
	const serverBilgiDTO = {
		user_modul: document.getElementById("user_modul").value,
		hangi_sql: document.getElementById("hangi_sql").value,
		user_prog_kodu: document.getElementById("user_prog_kodu").value,
		user_server: document.getElementById("user_server").value,
		user_pwd_server: document.getElementById("user_pwd_server").value,
		user_ip: document.getElementById("user_ip").value,
		firma_adi: firmaadi,
		superviser: document.getElementById("superviser").value
	};
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("server/dosyaolustur", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(serverBilgiDTO),
		});
		const data = response;
		await saveTo();
		if (data.olustuDurum === "true") {
			setTimeout(() => {
				alert("Dosya Oluşturuldu");
			}, 100);

			if (data.indexolustuDurum === "true") {
				setTimeout(() => {
					alert("Indexleme Oluşturuldu");
				}, 100);
			}
			else {
				setTimeout(() => {
					alert("Indexleme Oluştururken hata olustu");
				}, 100);
			}
		} else {
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage || "Dosya oluşturulamadı.";
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Bilinmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function saveTo() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	try {
		const userDetails = {
			user_prog_kodu: document.getElementById("user_prog_kodu").value,
			user_server: document.getElementById("user_server").value,
			user_pwd_server: document.getElementById("user_pwd_server").value,
			user_ip: document.getElementById("user_ip").value,
			user_modul: document.getElementById("user_modul").value,
			hangi_sql: document.getElementById("hangi_sql").value,
			izinlimi: true,
			calisanmi: true,
			log: false,
			superviser: document.getElementById("superviser").value,
		};
		const response = await fetchWithSessionCheck("user/user_details_save", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(userDetails),
		});
		if (response.errorMessage) {
			throw new Error(errorMessage);
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}