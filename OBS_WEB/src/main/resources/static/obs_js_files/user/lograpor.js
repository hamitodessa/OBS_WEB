async function lograpor() {
	const modul = document.getElementById("user_modul").value;
	const startDate = document.getElementById("startDate").value;
	const endDate = document.getElementById("endDate").value;
	const aciklama = document.getElementById("aciklama").value;
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none"; // Hata mesajını gizle
	try {
		const response = await fetchWithSessionCheck("user/loglistele", {
			method: 'POST',
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({ modul, startDate, endDate, aciklama }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const data = response;
		const tableBody = document.getElementById("tableBody");
		tableBody.innerHTML = "";
		if (data.success) {
			data.data.forEach(item => {
				const row = document.createElement("tr");
				row.classList.add("table-row-height");
				row.innerHTML = `
		                	<td>${item.TARIH || ''}</td>
		                    <td>${item.MESAJ || ''}</td>
		                    <td>${item.EVRAK || ''}</td>
		                    <td>${item.USER_NAME || ''}</td>
		                `;
				tableBody.appendChild(row);
			});
		}
	} catch (error) {
		errorDiv.style.display = "block"; // Hata mesajını göster
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	}
}