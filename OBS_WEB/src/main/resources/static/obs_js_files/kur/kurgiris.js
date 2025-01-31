async function kuroku() {
	const tarih = document.getElementById("tarih").value;
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	if (!tarih) {
		errorDiv.style.display = "block";
		errorDiv.innerText = "Lütfen tüm alanları doldurun.";
		return;
	}
	const tableBody = document.getElementById("tableBody");
	tableBody.innerHTML = "";
	document.body.style.cursor = "wait";
	try {
		const data = await fetchWithSessionCheck("kur/kurgunluk", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({ tarih }),
		});
		if (data.errorMessage) {
			throw new Error(data.errorMessage);
		}
		if (data.success) {
			if (Array.isArray(data.data) && data.data.length > 0) {
				data.data.forEach(item => {
					const row = document.createElement("tr");
					row.classList.add("table-row-height");
					row.innerHTML = `
						<td>${item.Kur || ''}</td>
						<td class="double-column">${formatNumber4(item.MA)}</td>
						<td class="double-column">${formatNumber4(item.MS)}</td>
						<td class="double-column">${formatNumber4(item.SA)}</td>
						<td class="double-column">${formatNumber4(item.SS)}</td>
						<td class="double-column">${formatNumber4(item.BA)}</td>
						<td class="double-column">${formatNumber4(item.BS)}</td>
					`;
					row.addEventListener("click", function() {
						setFormValues(row);
					});
					tableBody.appendChild(row);
				});
				const firstRow = tableBody.rows[0];
				const cells = firstRow.cells;
				document.getElementById("doviz_tur").value = cells[0].textContent.trim();
				document.getElementById("ma").value = cells[1].textContent.trim();
				document.getElementById("ms").value = cells[2].textContent.trim();
				document.getElementById("sa").value = cells[3].textContent.trim();
				document.getElementById("ss").value = cells[4].textContent.trim();
				document.getElementById("ba").value = cells[5].textContent.trim();
				document.getElementById("bs").value = cells[6].textContent.trim();
			} else {
				["ma", "ms", "sa", "ss", "ba", "bs"].forEach(id => {
					document.getElementById(id).value = "0.0000";
				});
			}
		} else {
			errorDiv.style.display = "block";
			errorDiv.innerText = "İşlem başarısız. Lütfen tekrar deneyin.";
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = `Beklenmeyen bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}

function tarihGeri() {
	const tarih = document.getElementById("tarih").value;
	const date = new Date(tarih);
	date.setDate(date.getDate() - 1);
	const formattedDate = date.toISOString().split('T')[0];
	document.getElementById("tarih").value = formattedDate;
	kuroku();
}

function tarihIleri() {
	const tarih = document.getElementById("tarih").value;
	const date = new Date(tarih);
	date.setDate(date.getDate() + 1);
	const formattedDate = date.toISOString().split('T')[0];
	document.getElementById("tarih").value = formattedDate;
	kuroku();
}

function checkEnter(event) {
	if (event.key === "Enter" || event.keyCode === 13) {
		kuroku();
	}
}

function setFormValues(row) {
	const cells = row.cells;
	document.getElementById("doviz_tur").value = cells[0].textContent.trim();
	document.getElementById("ma").value = cells[1].textContent.trim();
	document.getElementById("ms").value = cells[2].textContent.trim();  
	document.getElementById("sa").value = cells[3].textContent.trim(); 
	document.getElementById("ss").value = cells[4].textContent.trim(); 
	document.getElementById("ba").value = cells[5].textContent.trim();  
	document.getElementById("bs").value = cells[6].textContent.trim(); 
}

function kurSatirOku() {
	let kur_turu = document.getElementById("doviz_tur").value;
	const tableBody = document.getElementById("tableBody");
	let matchedRow = null; 
	for (let i = 0; i < tableBody.rows.length; i++) {
		const row = tableBody.rows[i]; 
		const firstColumnValue = row.cells[0].textContent.trim(); 
		if (firstColumnValue === kur_turu) { 
			matchedRow = row; 
			break; 
		}
	}
	if (matchedRow) {
		document.getElementById("ma").value = matchedRow.cells[1].textContent.trim();
		document.getElementById("ms").value = matchedRow.cells[2].textContent.trim(); 
		document.getElementById("sa").value = matchedRow.cells[3].textContent.trim(); 
		document.getElementById("ss").value = matchedRow.cells[4].textContent.trim(); 
		document.getElementById("ba").value = matchedRow.cells[5].textContent.trim(); 
		document.getElementById("bs").value = matchedRow.cells[6].textContent.trim();  
	} else {
	}
}
function formatNumber4(value) {
	if (value == null) return '';
	return parseFloat(value).toLocaleString(undefined, {
		minimumFractionDigits: 4,
		maximumFractionDigits: 4
	});
}
//**********************************kur kayit ********************************************
async function kurKayit() {
	const kurgirisDTO = {
		dvz_turu: document.getElementById("doviz_tur").value,
		tar: document.getElementById("tarih").value,
		ma: parseLocaleNumber(document.getElementById("ma").value),
		ms: parseLocaleNumber(document.getElementById("ms").value),
		sa: parseLocaleNumber(document.getElementById("sa").value),
		ss: parseLocaleNumber(document.getElementById("ss").value),
		ba: parseLocaleNumber(document.getElementById("ba").value),
		bs: parseLocaleNumber(document.getElementById("bs").value)
	};
	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";
	const $kaydetButton = $('#kaydetButton');
	$kaydetButton.prop('disabled', true).text('İşleniyor...');
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("kur/kurkayit", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(kurgirisDTO)
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		sayfaYukle()
	} catch (error) {
		document.body.style.cursor = "default";
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message; // Hata mesajını göster
	}
	finally {
		document.body.style.cursor = "default";
		$kaydetButton.prop('disabled', false).text('Kaydet');
	}
}
async function sayfaYukle() {
	const url = "kur/kurgiris";
	try {
		document.body.style.cursor = "wait";
		const response = await fetch(url, { method: "GET" });
		if (!response.ok) {
			throw new Error(`Bir hata oluştu: ${response.statusText}`);
		}
		const data = await response.text();
		if (data.includes('<form') && data.includes('name="username"')) {
			window.location.href = "/login";
		} else {
			document.getElementById('ara_content').innerHTML = data;
			kuroku();
		}
	} catch (error) {
		document.getElementById('ara_content').innerHTML = `<h2>${error.message}</h2>`;
	} finally {
		document.body.style.cursor = "default"; 
	}
}
//************************evrak sil ***********************************************
async function kurYoket() {
	const kurgirisDTO = {
		dvz_turu: document.getElementById("doviz_tur").value,
		tar: document.getElementById("tarih").value
	};
	const confirmDelete = confirm("Bu kuru silmek istediğinize emin misiniz?");
	if (!confirmDelete) {
		return;
	}
	document.body.style.cursor = "wait";
	const $silButton = $('#silButton');
	$silButton.prop('disabled', true).text('Siliniyor...');
	try {
		const response = await fetchWithSessionCheck("kur/kurSil", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(kurgirisDTO)
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		sayfaYukle()
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
		$silButton.prop('disabled', false).text('Sil');
	}
}

async function merkezOku() {
	const datePicker = document.getElementById("tarih").value;
	const currency = document.getElementById("doviz_tur").value;
	document.body.style.cursor = "wait";
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("kur/merkezoku", {
			method: "POST",
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ tarih: datePicker, kurcins: currency }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		document.getElementById("ma").value = formatNumber4(response.ma || 0);
		document.getElementById("ms").value = formatNumber4(response.ms || 0);
		
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}