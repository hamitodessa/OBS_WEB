async function kurrapfetchTableData() {
	const hiddenFieldValue = $('#kurraporBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	const startDate = parsedValues[0];
	const endDate = parsedValues[1];
	const cins1 = parsedValues[2];
	const cins2 = parsedValues[3];
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	const kurraporDTO = {
		startDate: startDate,
		endDate: endDate,
		cins1: cins1,
		cins2: cins2
	};
	const tableBody = document.getElementById("tableBody");
	tableBody.innerHTML = "";
	document.body.style.cursor = "wait";
	const $yenileButton = $('#yenileButton');
	$yenileButton.prop('disabled', true).text('İşleniyor...');
	try {
		const response = await fetchWithSessionCheck("kur/kurrapor", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(kurraporDTO),
		});
		if (response.errorMessage.trim() !== "") {
			throw new Error(response.errorMessage);
		}
		const data = response;
		if (data.success) {
			data.data.forEach(item => {
				const row = document.createElement("tr");
				row.classList.add("table-row-height");
				row.innerHTML = `
                	<td>${formatDate(item.Tarih)}</td>
                    <td>${item.Kur || ''}</td>
                    <td class="double-column">${formatNumber4(item.MA)}</td>
                    <td class="double-column">${formatNumber4(item.MS)}</td>
					<td class="double-column">${formatNumber4(item.SA)}</td>
					<td class="double-column">${formatNumber4(item.SS)}</td>
					<td class="double-column">${formatNumber4(item.BA)}</td>
					<td class="double-column">${formatNumber4(item.BS)}</td>
                `;
				tableBody.appendChild(row);
			});
		} else {
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage || "Bir hata oluştu.";
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message;
	} finally {
		$yenileButton.prop('disabled', false).text('Yenile');
		document.body.style.cursor = "default";
	}
}