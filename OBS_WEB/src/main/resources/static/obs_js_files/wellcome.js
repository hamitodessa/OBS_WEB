

async function getWellcomeData() {
	try {
		document.body.style.cursor = "wait";
		const response = await fetchWithSessionCheck("wellcomecalismadizini", {
			method: "GET",
		});
		
		const data = response;
		if (data.errorMessage) {
			throw new Error(data.errorMessage);
		}
		const responseData = data.data;
		responseData.forEach(item => {
			if (item.modul === "Cari Hesap") {
				document.getElementById("ckod").innerText = item.progkodu;
				document.getElementById("cserver").innerText = item.server;
				document.getElementById("chsql").innerText = item.hangi_sql;
			} else if (item.modul === "Adres") {
				document.getElementById("akod").innerText = item.progkodu;
				document.getElementById("aserver").innerText = item.server;
				document.getElementById("ahsql").innerText = item.hangi_sql;
			} else if (item.modul === "Kur") {
				document.getElementById("kkod").innerText = item.progkodu;
				document.getElementById("kserver").innerText = item.server;
				document.getElementById("khsql").innerText = item.hangi_sql;
			} else if (item.modul === "Kambiyo") {
				document.getElementById("kakod").innerText = item.progkodu;
				document.getElementById("kaserver").innerText = item.server;
				document.getElementById("kahsql").innerText = item.hangi_sql;
			} else if (item.modul === "Fatura") {
				document.getElementById("fkod").innerText = item.progkodu;
				document.getElementById("fserver").innerText = item.server;
				document.getElementById("fhsql").innerText = item.hangi_sql;
			} else if (item.modul === "Kereste") {
				document.getElementById("kerkod").innerText = item.progkodu;
				document.getElementById("kerserver").innerText = item.server;
				document.getElementById("kerhsql").innerText = item.hangi_sql;
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