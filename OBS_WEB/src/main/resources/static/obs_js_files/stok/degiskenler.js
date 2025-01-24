
document.getElementById("arama").value = "";
anagrpdoldur();

async function degiskenchange(grpElement) {
	document.getElementById("aciklama").value = "";
	document.getElementById("arama").value = "";
	document.getElementById("idacik").value = "";

	const grup = grpElement.value;
	const altgrpdiv = document.getElementById("altgrpdiv");
	document.getElementById("arama").value = "";

	if (grup === "altgrp") {
		altgrpdiv.style.display = "block";
		altgrpdoldur()
	} else {
		altgrpdiv.style.display = "none";
	}

	if (grup === "anagrp") {
		anagrpdoldur();
	}
	else if (grup === "mensei") {
		menseidoldur();
	}
	else if (grup === "depo") {
		depodoldur();
	}
	else if (grup === "oz1") {
		oz1doldur();
	}
	else if (grup === "oz2") {
		oz2doldur();
	}
}

async function anagrpdoldur() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("stok/anagrpOku");
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const data = response.anagrp;
		const tableBody = document.getElementById("degiskenTableBody");
		tableBody.innerHTML = "";
		tableBody.classList.add("table-row-height");
		data.forEach((row) => {
			const tr = document.createElement("tr");
			tr.innerHTML = `
				<td style="display: none;">${row.KOD || ""}</td>
                <td>${row.ANA_GRUP || ""}</td>
               `;
			tr.onclick = () => selectValue(row.ANA_GRUP, row.KOD);
			tableBody.appendChild(tr);
		});

		const firstRow = tableBody.rows[0];
		if (firstRow) {
			const cells = firstRow.cells;
			selectValue(cells[1].textContent.trim(), cells[0].textContent.trim());
		}

	} catch (error) {
		const modalError = document.getElementById("errorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}

async function menseidoldur() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("stok/menseiOku");
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const data = response.mensei;
		const tableBody = document.getElementById("degiskenTableBody");
		tableBody.innerHTML = "";
		if (data.length === 0) {
			return;
		}
		tableBody.classList.add("table-row-height");
		data.forEach((row) => {
			const tr = document.createElement("tr");
			tr.innerHTML = `
				<td style="display: none;">${row.KOD || ""}</td>
                <td>${row.MENSEI || ""}</td>
               `;
			tr.onclick = () => selectValue(row.MENSEI, row.KOD);
			tableBody.appendChild(tr);
		});
		const firstRow = tableBody.rows[0];
		if (firstRow) {
			const cells = firstRow.cells;
			selectValue(cells[1].textContent.trim(), cells[0].textContent.trim());
		}
	} catch (error) {
		const modalError = document.getElementById("errorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}

async function depodoldur() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("stok/depoOku");
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const data = response.depo;
		const tableBody = document.getElementById("degiskenTableBody");
		tableBody.innerHTML = "";
		if (data.length === 0) {
			return;
		}
		tableBody.classList.add("table-row-height");
		data.forEach((row) => {
			const tr = document.createElement("tr");
			tr.innerHTML = `
				<td style="display: none;">${row.KOD || ""}</td>
                <td>${row.DEPO || ""}</td>
               `;
			tr.onclick = () => selectValue(row.DEPO, row.KOD);
			tableBody.appendChild(tr);
		});
		const firstRow = tableBody.rows[0];
		if (firstRow) {
			const cells = firstRow.cells;
			selectValue(cells[1].textContent.trim(), cells[0].textContent.trim());
		}
	} catch (error) {
		const modalError = document.getElementById("errorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}

async function oz1doldur() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("stok/oz1Oku");
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}

		const data = response.oz1;
		const tableBody = document.getElementById("degiskenTableBody");
		tableBody.innerHTML = "";
		if (data.length === 0) {
			return;
		}
		tableBody.classList.add("table-row-height");
		data.forEach((row) => {
			const tr = document.createElement("tr");
			tr.innerHTML = `
				<td style="display: none;">${row.KOD || ""}</td>
                <td>${row.OZEL_KOD_1 || ""}</td>
               `;
			tr.onclick = () => selectValue(row.OZEL_KOD_1, row.KOD);
			tableBody.appendChild(tr);
		});
		const firstRow = tableBody.rows[0];
		if (firstRow) {
			const cells = firstRow.cells;
			selectValue(cells[1].textContent.trim(), cells[0].textContent.trim());
		}
	} catch (error) {
		const modalError = document.getElementById("errorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}

async function oz2doldur() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("stok/oz2Oku");
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const data = response.oz2;
		const tableBody = document.getElementById("degiskenTableBody");
		tableBody.innerHTML = "";
		if (data.length === 0) {
			return;
		}
		tableBody.classList.add("table-row-height");
		data.forEach((row) => {
			const tr = document.createElement("tr");
			tr.innerHTML = `
				<td style="display: none;">${row.KOD || ""}</td>
                <td>${row.OZEL_KOD_2 || ""}</td>
               `;
			tr.onclick = () => selectValue(row.OZEL_KOD_2, row.KOD);
			tableBody.appendChild(tr);
		});
		const firstRow = tableBody.rows[0];
		if (firstRow) {
			const cells = firstRow.cells;
			selectValue(cells[1].textContent.trim(), cells[0].textContent.trim());
		}
	} catch (error) {
		const modalError = document.getElementById("errorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}

function filterTable() {
	const searchValue = document.getElementById("arama").value.toLowerCase();
	const rows = document.querySelectorAll("#degiskenTable tbody tr");
	rows.forEach((row) => {
		const rowText = Array.from(row.cells)
			.map((cell) => cell.textContent.toLowerCase())
			.join(" ");
		row.style.display = rowText.includes(searchValue) ? "" : "none";
	});
}

async function altgrpdoldur() {
	document.getElementById("arama").value = "";

	const anagrup = document.getElementById("altgrpAna").value;
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("stok/altgrupdeg", {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ anagrup: anagrup }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const data = response.altKodlari

		const tableBody = document.getElementById("degiskenTableBody");
		tableBody.innerHTML = "";
		if (data.length === 0) {
			return;
		}
		tableBody.classList.add("table-row-height");
		data.forEach((row) => {
			if (row.ALT_GRUP != "") {
				const tr = document.createElement("tr");
				tr.innerHTML = `
				<td style="display: none;">${row.ALID_Y || ""}</td>
                <td>${row.ALT_GRUP || ""}</td>
               `;
				tr.onclick = () => selectValue(row.ALT_GRUP, row.ALID_Y);
				tableBody.appendChild(tr);
			}
		});
		const firstRow = tableBody.rows[0];
		if (firstRow) {
			const cells = firstRow.cells;
			selectValue(cells[1].textContent.trim(), cells[0].textContent.trim());
		}
	} catch (error) {
		const modalError = document.getElementById("errorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}

function selectValue(selectedaciklama, selectedid) {
	const inputElement = document.getElementById("aciklama");
	const idacik = document.getElementById("idacik");
	inputElement.value = selectedaciklama;
	idacik.value = selectedid;
}

function degyeni() {
	document.getElementById("aciklama").value = "";
	document.getElementById("idacik").value = "";
}

async function degKayit() {
	const aciklama = document.getElementById("aciklama").value;
	const idacik = document.getElementById("idacik").value || "";
	const degisken = document.getElementById("degiskenler").value;
	const altgrpAna = document.getElementById("altgrpAna").value;

	if (!aciklama) {
		alert('Lütfen  açıklama alanlarını doldurun.');
		return;
	}
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";

	const saveButton = document.getElementById('degkaydetButton');
	saveButton.textContent = "İşlem yapılıyor...";
	saveButton.disabled = true;
	document.body.style.cursor = "wait";

	try {
		const response = await fetchWithSessionCheck('stok/degkayit', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({ aciklama, idacik, degisken, altgrpAna }),
		});
		if (!response) {
			return;
		}
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const inputElement = document.getElementById("degiskenler");
		degiskenchange(inputElement);
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Bir hata oluştu. Daha sonra tekrar deneyin.";
	} finally {
		document.body.style.cursor = "default";
		saveButton.textContent = "Kaydet";
		saveButton.disabled = false;
	}
}

async function degYoket() {
	const confirmDelete = confirm(
	    "Alt Grup Degisken Silinecek ..?\n" +
	    "Silme operasyonu butun dosyayi etkileyecek...\n" +
	    "Ilk once Degisken Yenileme Bolumunden degistirip sonra siliniz...."
	);
	if (!confirmDelete) {
		return;
	}
	const aciklama = document.getElementById("aciklama").value;
	const idacik = document.getElementById("idacik").value || "";
	const degisken = document.getElementById("degiskenler").value;
	const altgrpAna = document.getElementById("altgrpAna").value;

	if (!idacik) {
		alert('Lütfen  açıklama alanlarını doldurun.');
		return;
	}
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";

	const saveButton = document.getElementById('degsilButton');
	saveButton.textContent = "İşlem yapılıyor...";
	saveButton.disabled = true;
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck('stok/degsil', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({ aciklama, idacik, degisken, altgrpAna }),
		});
		if (!response) {
			return;
		}
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const inputElement = document.getElementById("degiskenler");
		degiskenchange(inputElement);
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Bir hata oluştu. Daha sonra tekrar deneyin.";
	} finally {
		document.body.style.cursor = "default";
		saveButton.textContent = "Sil";
		saveButton.disabled = false;
	}
}