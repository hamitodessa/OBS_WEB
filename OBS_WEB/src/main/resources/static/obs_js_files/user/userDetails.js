document.getElementById("createdbMenuLink").addEventListener("click", function () {
	document.querySelector('.changeLink[data-url="/user/createdb"]').click();
});

async function detailoku() {
	const modul = document.getElementById("user_modul").value;
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	const tableBody = document.getElementById("tableBody");
	tableBody.innerHTML = "";
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("user/detailoku", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ modul }),
		});
		const data = response;
		clearTable();
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		if (data.success) {
			clearFormModulsuz();
			data.data.forEach(item => {
				const row = document.createElement("tr");
				row.classList.add("table-row-height");
				row.innerHTML = `
                    <td style="display: none;">${item.id || ""}</td>
                    <td>${item.user_prog_kodu || ""}</td>
                    <td>${item.user_server || ""}</td>
                    <td>${item.user_pwd_server ? "******" : ""}</td> 
                    <td>${item.user_ip || ""}</td>
                    <td>${item.user_modul || ""}</td>
                    <td>${item.hangi_sql || ""}</td>
                    <td>${item.izinlimi ? "Evet" : "Hayır"}</td>
                    <td>${item.calisanmi ? "Evet" : "Hayır"}</td>
                    <td>${item.log ? "Evet" : "Hayır"}</td>
					<td>${item.superviser || ""}</td>
                `;
				row.addEventListener("click", () => setFormValues(row));
				tableBody.appendChild(row);
			});
			if (tableBody.rows.length > 0) {
				const firstRow = tableBody.rows[0];
				const cells = firstRow.cells;
				document.getElementById("hiddenId").value = cells[0].textContent.trim() || "";
				document.getElementById("user_prog_kodu").value = cells[1].textContent.trim() || "";
				document.getElementById("user_server").value = cells[2].textContent.trim() || "";
				document.getElementById("user_pwd_server").value = cells[3].textContent.trim() || "";
				document.getElementById("user_ip").value = cells[4].textContent.trim() || "";
				document.getElementById("user_modul").value = cells[5].textContent.trim() || "";
				document.getElementById("hangi_sql").value = cells[6].textContent.trim() || "";
				document.getElementById("izinlimi").checked = cells[7].textContent.trim() === "Evet";
				document.getElementById("calisanmi").checked = cells[8].textContent.trim() === "Evet";
				document.getElementById("log").checked = cells[9].textContent.trim() === "Evet";
				document.getElementById("superviser").value = cells[10].textContent.trim() || "";
			} else {
				clearFormModulsuz();
			}
			sqlchanged();
			if (data.roleName === "ADMIN") {
				const logiznidiv = document.getElementById("logiznidiv");
				logiznidiv.style.display = "block";
				const izinlimidiv = document.getElementById("izinlimidiv");
				izinlimidiv.style.display = "block";
			}
		} else {
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage || "Bir hata oluştu.";
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message;
	} finally {
		document.body.style.cursor = "default";
		const dbButton = document.getElementById("savebutton");
		dbButton.disabled = true;
	}
}

function setFormValues(row) {
	const cells = row.cells;
	document.getElementById("hiddenId").value = cells[0].textContent.trim() || "";
	document.getElementById("user_prog_kodu").value = cells[1].textContent.trim() || "";
	document.getElementById("user_server").value = cells[2].textContent.trim() || "";
	document.getElementById("user_pwd_server").value = cells[3].textContent.trim() || "";
	document.getElementById("user_ip").value = cells[4].textContent.trim() || "";
	document.getElementById("user_modul").value = cells[5].textContent.trim() || "";
	document.getElementById("hangi_sql").value = cells[6].textContent.trim() || "";
	document.getElementById("izinlimi").checked = cells[7].textContent.trim() === "Evet";
	document.getElementById("calisanmi").checked = cells[8].textContent.trim() === "Evet";
	document.getElementById("log").checked = cells[9].textContent.trim() === "Evet";
	document.getElementById("superviser").value = cells[10].textContent.trim() || "";
	sqlchanged();
	const dbButton = document.getElementById("savebutton");
	dbButton.disabled = true;
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
}

async function saveUserDetails() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	try {
		const userPwdServerInput = document.getElementById("user_pwd_server").value;
		const userPwdServer = userPwdServerInput === "******" ? null : userPwdServerInput;
		const userDetails = {
			id: document.getElementById("hiddenId").value,
			user_prog_kodu: document.getElementById("user_prog_kodu").value,
			user_server: document.getElementById("user_server").value,
			user_pwd_server: userPwdServer,
			user_ip: document.getElementById("user_ip").value,
			user_modul: document.getElementById("user_modul").value,
			hangi_sql: document.getElementById("hangi_sql").value,
			izinlimi: document.getElementById("izinlimi").checked,
			calisanmi: document.getElementById("calisanmi").checked,
			log: document.getElementById("log").checked,
			superviser: document.getElementById("superviser").value,
		};
		const response = await fetchWithSessionCheck("user/user_details_save", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(userDetails)
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		detailoku();
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Bir hata oluştu.";
	}
}

function clearForm() {
	document.getElementById("hiddenId").value = "";
	document.getElementById("user_prog_kodu").value = "";
	document.getElementById("user_server").value = "";
	document.getElementById("user_pwd_server").value = "";
	document.getElementById("user_ip").value = "";
	document.getElementById("user_modul").selectedIndex = 0;
	document.getElementById("hangi_sql").selectedIndex = 0;
	document.getElementById("izinlimi").checked = false;
	document.getElementById("calisanmi").checked = false;
	document.getElementById("log").checked = false;
	document.getElementById("superviser").value = "";
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
}
function clearFormModulsuz() {
	document.getElementById("hiddenId").value = "";
	document.getElementById("user_prog_kodu").value = "";
	document.getElementById("user_server").value = "";
	document.getElementById("user_pwd_server").value = "";
	document.getElementById("user_ip").value = "";
	document.getElementById("hangi_sql").selectedIndex = 0;
	document.getElementById("izinlimi").checked = false;
	document.getElementById("calisanmi").checked = false;
	document.getElementById("log").checked = false;
	document.getElementById("superviser").value = "";
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
}
function clearTable() {
	const tableBody = document.querySelector("#moduleTable tbody");
	tableBody.innerHTML = "";
}

//************************evrak sil ***********************************************
function confirmDeleteus() {
	if (confirm("Bu kaydı silmek istediğinizden emin misiniz?")) {
		deleteUserDetailsus(); // Onay verilirse silme işlemi başlatılır
	}
}

async function deleteUserDetailsus() {
	const idd = document.getElementById("hiddenId").value; 
	if (!idd || isNaN(idd) || idd <= 0) {
		return;
	}
	const id = Number(idd); 
	const modul = document.getElementById("user_modul").value;
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("user/delete_user_details", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ id , modul }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		detailoku();
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}
//****************************************DOSYA KONTROL ************************************************/
async function checkFile() {
	const userPwdServerInput = document.getElementById("user_pwd_server").value;
	const userPwdServer = userPwdServerInput === "******" ? null : userPwdServerInput;
	const serverBilgiDTO = {
		user_modul: document.getElementById("user_modul").value,
		hangi_sql: document.getElementById("hangi_sql").value,
		user_prog_kodu: document.getElementById("user_prog_kodu").value,
		user_server: document.getElementById("user_server").value,
		user_pwd_server: userPwdServer,
		user_ip: document.getElementById("user_ip").value,
		superviser: document.getElementById("superviser").value,
		id: document.getElementById("hiddenId").value,
	};
	const dbButton = document.getElementById("savebutton");
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("user/checkfile", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(serverBilgiDTO),
		});
		const data = response;
		if (data.dosyaDurum === "true") {
			document.body.style.cursor = "default";
			dbButton.disabled = false;
		} else {
			document.body.style.cursor = "default";
			alert("Baglanti saglanamadi veya Veritabanı Mevcut Değil");
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Bilinmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function sqlchanged() {
  const hangi_sql = document.getElementById("hangi_sql").value;
  if (hangi_sql === "PG SQL") {
    document.getElementById("superviserdiv").style.visibility = "visible";
  } else {
    document.getElementById("superviserdiv").style.visibility = "hidden";
  }
}