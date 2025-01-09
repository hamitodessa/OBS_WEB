async function getWellcomeData() {
	try {
		document.body.style.cursor = "wait";
		const response = await fetch("wellcomecalismadizini", {
			method: "GET",
		});
		if (!response.ok) {
			throw new Error(`HTTP error! status: ${response.status}`);
		}
		const data = await response.json();
		if (data.errorMessage) {
			console.error("Error message from server:", data.errorMessage);
			throw new Error(data.errorMessage);
		}
		const responseData = data.data;
		responseData.forEach(item => {
			if (item.modul === "Cari Hesap") {
				document.getElementById("ckod").innerText = item.progkodu;
				document.getElementById("cserver").innerText = item.server;
				document.getElementById("cfadi").innerText = item.firma;
				document.getElementById("chsql").innerText = item.hangi_sql;
			} else if (item.modul === "Adres") {
				document.getElementById("akod").innerText = item.progkodu;
				document.getElementById("aserver").innerText = item.server;
				document.getElementById("afadi").innerText = item.firma;
				document.getElementById("ahsql").innerText = item.hangi_sql;
			} else if (item.modul === "Kur") {
				document.getElementById("kkod").innerText = item.progkodu;
				document.getElementById("kserver").innerText = item.server;
				document.getElementById("kfadi").innerText = item.firma;
				document.getElementById("khsql").innerText = item.hangi_sql;
			} else if (item.modul === "Kambiyo") {
				document.getElementById("kakod").innerText = item.progkodu;
				document.getElementById("kaserver").innerText = item.server;
				document.getElementById("kafadi").innerText = item.firma;
				document.getElementById("kahsql").innerText = item.hangi_sql;
			}
		});
	} catch (error) {
		const errorDiv = document.getElementById("errorDiv");
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}