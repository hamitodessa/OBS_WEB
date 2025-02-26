$(document).ready(function () {
	$("#kodu").inputmask({
		mask: "AA-999-9999-9999",
		placeholder: "-",
		definitions: {
			'A': {
				validator: "[A-Za-z0-9]",
				cardinality: 1
			}
		}
	});
});

function toggleCheckboxes(source) {
	const checkboxes = document.querySelectorAll('tbody input[type="checkbox"]');
	checkboxes.forEach(checkbox => {
		checkbox.checked = source.checked;
	});
	satirsay();
}

function satirsay() {
	const checkboxes = document.querySelectorAll('tbody input[type="checkbox"]:checked');
	let totalsatir = checkboxes.length;
	document.getElementById("totalSatir").innerText = formatNumber0(totalsatir);

}

async function fetchTable() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";

	document.body.style.cursor = "wait";
	const tableBody = document.getElementById("tableBody");
	tableBody.innerHTML = "";

	const kons = document.getElementById("kons").value;
	const kodu = document.getElementById("kodu").value;
	const evrak = document.getElementById("gevrak").value;
	const pakno = document.getElementById("pakno").value;
	if (
		(!kons || kons.trim() === "") &&
		(!evrak || evrak.trim() === "") &&
		(!pakno || pakno.trim() === "") &&
		(kodu === "00-000-0000-0000")
	) {
		document.body.style.cursor = "default";
		return;
	}
	document.getElementById("totalSatir").innerText = "0";
	try {
		const response = await fetchWithSessionCheck("kereste/koddegisload", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({ pakno, kons, kodu, evrak }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		data = response;
		data.data.forEach(rowData => {
			const row = document.createElement('tr');
			row.classList.add("table-row-height");
			row.innerHTML = `
                <td style="width: 40px;"><input type="checkbox" onclick="satirsay()"></td>
				<td>${rowData.Evrak_No}</td>
				<td>${rowData.Barkod}</td>
                <td>${rowData.Kodu}</td>
                <td>${rowData.Paket_No}</td>
                <td>${rowData.Konsimento}</td>
                <td class="double-column">${formatNumber0(rowData.Miktar)}</td>
                <td class="double-column">${formatNumber3(rowData.m3)}</td>
                <td>${formatDate(rowData.Tarih)}</td>
                <td class="double-column">${formatNumber2(rowData.Kdv)}</td>
                <td>${rowData.Doviz}</td>
                <td class="double-column">${formatNumber2(rowData.Fiat)}</td>
                <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
                <td class="double-column">${formatNumber2(rowData.Kur)}</td>
                <td>${rowData.Cari_Firma}</td>
                <td>${rowData.Adres_Firma}</td>
                <td class="double-column">${formatNumber2(rowData.Iskonto)}</td>
                <td class="double-column">${formatNumber2(rowData.Tevkifat)}</td>
                <td>${rowData.Ana_Grup}</td>
                <td>${rowData.Alt_Grup}</td>
                <td>${rowData.Mensei}</td>
				<td style="display: none;">${rowData.Satir}</td>
            `;
			tableBody.appendChild(row);
		});
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error;
	} finally {
		document.body.style.cursor = "default";
	}
}

async function kodKaydet() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	const ykod = document.getElementById("ykod").value.trim();
	const kodadi = document.getElementById('kodadi').innerText.trim();
	if (!ykod || !kodadi) {
		document.body.style.cursor = "default";
		return;
	}
	const selectedData = [];
	document.querySelectorAll("#tableBody tr").forEach(tr => {
		const checkbox = tr.querySelector('td input[type="checkbox"]');
		if (checkbox && checkbox.checked) {
			const cells = tr.querySelectorAll("td");
			selectedData.push({
				kodu: ykod,
				paket: cells[4]?.textContent.trim(),
				kons: cells[5]?.textContent.trim(),
				satir: parseInt(cells[21]?.textContent.trim(), 10) || 0
			});
		}
	});
	if (selectedData.length === 0) {
		document.body.style.cursor = "default";
		alert('Lütfen en az bir satır seçin.');
		return;
	}
	try {
		const response = await fetchWithSessionCheck("kereste/ykodkaydet", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({ selectedRows: selectedData }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error;
	} finally {
		const tableBody = document.getElementById("tableBody");
		tableBody.innerHTML = "";
		document.getElementById("totalSatir").innerText = '0';
		clearinput();
		document.body.style.cursor = "default";
	}
}

async function ykonsKaydet() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	const ykons = document.getElementById("ykons").value.trim();
	const konsadi = document.getElementById('konsadi').innerText.trim();
	if (!ykons || !konsadi) {
		document.body.style.cursor = "default";
		return;
	}
	const selectedData = [];
	document.querySelectorAll("#tableBody tr").forEach(tr => {
		const checkbox = tr.querySelector('td input[type="checkbox"]');
		if (checkbox && checkbox.checked) {
			const cells = tr.querySelectorAll("td");
			selectedData.push({
				ykons: ykons,
				kons: cells[5]?.textContent.trim(),
				satir: parseInt(cells[21]?.textContent.trim(), 10) || 0
			});
		}
	});
	if (selectedData.length === 0) {
		document.body.style.cursor = "default";
		alert('Lütfen en az bir satır seçin.');
		return;
	}
	try {
		const response = await fetchWithSessionCheck("kereste/ykonskaydet", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({ selectedRows: selectedData }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error;
	} finally {
		const tableBody = document.getElementById("tableBody");
		tableBody.innerHTML = "";
		document.getElementById("totalSatir").innerText = '0';
		clearinput();
		document.body.style.cursor = "default";
	}
}

async function kodadi() {
	document.body.style.cursor = "wait";
	const ukod = document.getElementById("ykod").value;
	document.getElementById('kodadi').innerText = '';
	try {
		const response = await fetchWithSessionCheck('kereste/kodadi', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ kod: ukod }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		document.getElementById('kodadi').innerText = response.urunAdi;
	} catch (error) {
		const errorDiv = document.getElementById('errorDiv');
		errorDiv.style.display = 'block';
		errorDiv.innerText = error.message;
	} finally {
		document.body.style.cursor = "default";
	}
}

async function konsadi() {
	document.body.style.cursor = "wait";
	const kons = document.getElementById("ykons").value;
	document.getElementById('konsadi').innerText = '';
	try {
		const response = await fetchWithSessionCheck('kereste/konsadi', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ kons: kons }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		document.getElementById('konsadi').innerText = response.konsAdi;
	} catch (error) {
		const errorDiv = document.getElementById('errorDiv');
		errorDiv.style.display = 'block';
		errorDiv.innerText = error.message;
	} finally {
		document.body.style.cursor = "default";
	}
}

function clearinput() {

	document.getElementById("ykod").value = '';
	document.getElementById('kodadi').innerText = '';

	document.getElementById("ykons").value = '';
	document.getElementById('konsadi').innerText = '';

	const kons = document.getElementById("kons").value = '';
	const kodu = document.getElementById("kodu").value = '00-000-0000-0000';
	const evrak = document.getElementById("gevrak").value = '';
	const pakno = document.getElementById("pakno").value = '';

}

