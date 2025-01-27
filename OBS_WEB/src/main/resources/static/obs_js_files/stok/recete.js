async function fetchkod() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.innerText = "";
	errorDiv.style.display = "none";
	rowCounter = 0;
	depolar = "";
	urnkodlar = "";
	try {
		const response = await fetchWithSessionCheck("stok/stkgeturn", {
			method: "GET",
			headers: {
				"Content-Type": "application/json",
			},
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		urnkodlar = response.urnkodlar || [];
		initializeRows();
	} catch (error) {
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
		errorDiv.style.display = "block";
	}
}
async function sonfis() {
	const errorDiv = document.getElementById('errorDiv');
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck('stok/recsonfis', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			}
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const data = response;
		document.getElementById('recno').value = data.recno;
		receteOku();
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		errorDiv.style.display = 'none';
		document.body.style.cursor = "default";
	}
}

async function yeniFis() {
	const errorDiv = document.getElementById('errorDiv');
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	clearInputs();
	try {
		const response = await fetchWithSessionCheck('stok/recyenifis', {
			method: "GET",
			headers: {
				"Content-Type": "application/json",
			},
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const fisNoInput = document.getElementById('recno');
		fisNoInput.value = response.recno;
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		errorDiv.style.display = 'none';
		document.body.style.cursor = "default";
	}
}

function initializeRows() {

	rowCounter = 0;
	for (let i = 0; i < 5; i++) {
		satirekle();
	}
}
function satirekle() {
	const table = document.getElementById("recTable").getElementsByTagName("tbody")[0];
	const newRow = table.insertRow();
	incrementRowCounter();

	let ukoduoptionsHTML = urnkodlar.map(kod => `<option value="${kod.Kodu}">${kod.Kodu}</option>`).join("");
	newRow.innerHTML = `
		<td >
			<button id="bsatir_${rowCounter}" type="button" class="btn btn-secondary ml-2" onclick="satirsil(this)"><i class="fa fa-trash"></i></button>
		</td>
		<td>
			<label class="form-control" >CIKAN</label>
		</td>
		<td>
		    <div style="position: relative; width: 100%;">
		        <input class="form-control cins_bold" list="ukoduOptions_${rowCounter}" maxlength="12" id="ukodu_${rowCounter}" 
		            onkeydown="focusNextCell(event, this)" ondblclick="openurunkodlariModal('ukodu_${rowCounter}', 'recetesatir','ukodukod')" onchange="updateRowValues(this)">
		        <datalist id="ukoduOptions_${rowCounter}">${ukoduoptionsHTML}</datalist>
		        <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
		    </div>
		</td>
		<td>
			<label class="form-control"style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label>
		</td>
		<td>
			<label class="form-control" style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label>
		</td>
        <td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur3(this)" 
			  onkeydown="focusNextRow(event, this)" value="${formatNumber3(0)}" style="text-align:right;">
		</td>
	    `;
}

function handleBlur3(input) {
	input.value = formatNumber3(input.value);
	updateColumnTotal();
}

function selectAllContent(element) {
	if (element && element.select) {
		element.select();
	}
}

function satirsil(button) {
	const row = button.parentElement.parentElement;
	row.remove();
	updateColumnTotal();
}

function setLabelContent(cell, content) {
	const span = cell.querySelector('label span');
	if (span) {
		span.textContent = content ? content : '\u00A0';
	}
}

function focusNextRow(event, element) {
	if (event.key === "Enter") {
		event.preventDefault();
		const currentRow = element.closest('tr');
		const nextRow = currentRow.nextElementSibling;
		if (nextRow) {
			const secondInput = nextRow.querySelector("td:nth-child(3) input");
			if (secondInput) {
				secondInput.focus();
				secondInput.select();
			}
		} else {
			satirekle();
			const table = currentRow.parentElement;
			const newRow = table.lastElementChild;
			const secondInput = newRow.querySelector("td:nth-child(3) input");
			if (secondInput) {
				secondInput.focus();
				secondInput.select();
			}
		}
	}
}

function focusNextCell(event, element) {
	if (event.key === "Enter") {
		event.preventDefault();
		let currentCell = element.closest('td');
		let nextCell = currentCell.nextElementSibling;
		while (nextCell) {
			const focusableElement = nextCell.querySelector('input');
			if (focusableElement) {
				focusableElement.focus();
				if (focusableElement.select) {
					focusableElement.select();
				}
				break;
			} else {
				nextCell = nextCell.nextElementSibling;
			}
		}
	}
}

async function updateRowValues(inputElement) {
	const selectedValue = inputElement.value;
	document.body.style.cursor = "wait";
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("stok/recetecikan", {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ kodu: selectedValue, cins: "Kodu" }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const row = inputElement.closest('tr');
		const cells = row.querySelectorAll('td');
		const adiCell = cells[3];
		const birimCell = cells[4];
		setLabelContent(adiCell, response.urun.adi);
		setLabelContent(birimCell, response.urun.birim);
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function updateurunValues(inputElement) {

	const selectedValue = inputElement.value;
	document.body.style.cursor = "wait";
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("stok/recetecikan", {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ kodu: selectedValue, cins: "Kodu" }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		document.getElementById('agirlik').innerText = response.urun.agirlik;
		document.getElementById('anagrpl').innerText = response.urun.anagrup;
		document.getElementById('altgrpl').innerText = response.urun.altgrup;
		document.getElementById('barkod').innerText = response.urun.barkod;
		document.getElementById('birim').innerText = response.urun.birim;
		document.getElementById('adi').innerText = response.urun.adi;
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function receteOku() {
	const recno = document.getElementById("recno").value;
	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("stok/receteOku", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ recno: recno }),
		});
		const data = response;
		clearInputs();

		if (data.errorMessage) {
			throw new Error(data.errorMessage);
		}
		const table = document.getElementById('recTable');
		const rows = table.querySelectorAll('tbody tr');
		if (data.data.length > rows.length) {
			const additionalRows = data.data.length - rows.length;
			for (let i = 0; i < additionalRows; i++) {
				satirekle();
			}
		}
		data.data.forEach((item, index) => {
			if (item.Tur === "Cikan" && index < rows.length) {
				const cells = rows[index].cells;
				setLabelContent(cells[1], item.Tur);
				const urunKoduInput = cells[2]?.querySelector('input');
				if (urunKoduInput) urunKoduInput.value = item.Kodu || "";
				setLabelContent(cells[3], item.Adi || '');
				setLabelContent(cells[4], item.Birim || '');
				const miktarInput = cells[5]?.querySelector('input');
				if (miktarInput) miktarInput.value = formatNumber3(item.Miktar);
			}
		});
		for (let i = 0; i < data.data.length; i++) {
			const item = data.data[i];
			if (item.Tur === "Giren") {
				document.getElementById("anagrp").value = item.Ana_Grup || '';
				await anagrpChanged(document.getElementById("anagrp"));
				document.getElementById("altgrp").value = item.Alt_Grup || ''
				document.getElementById("aciklama").value = data.aciklama;
				document.getElementById("ukodu").value = item.Kodu || ''
				document.getElementById("durum").value = item.Durum ? "A" : "P";
				break;
			}
		}
		updateurunValues(document.getElementById("ukodu"));

		errorDiv.style.display = "none";
		errorDiv.innerText = "";
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function clearInputs() {

	document.getElementById("anagrp").value = '';
	document.getElementById("altgrp").innerHTML = '';
	document.getElementById("altgrp").disabled = true;
	document.getElementById("aciklama").value = '';

	document.getElementById("ukodu").value = '';
	document.getElementById('agirlik').innerText = '0.000';
	document.getElementById('anagrpl').innerText = '';
	document.getElementById('altgrpl').innerText = '';
	document.getElementById('barkod').innerText = '';
	document.getElementById('birim').innerText = '';
	document.getElementById('adi').innerText = '';


	const tableBody = document.getElementById("tbody");
	tableBody.innerHTML = "";
	rowCounter = 0;
	initializeRows();
}

async function anagrpChanged(anagrpElement) {
	const anagrup = anagrpElement.value;
	const selectElement = document.getElementById("altgrp");
	const errorDiv = document.getElementById("errorDiv");
	selectElement.innerHTML = '';
	if (anagrup === "") {
		selectElement.disabled = true;
		return;
	}
	document.body.style.cursor = "wait";
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("stok/altgrup", {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ anagrup: anagrup }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		response.altKodlari.forEach(kod => {
			const option = document.createElement("option");
			option.value = kod.ALT_GRUP;
			option.textContent = kod.ALT_GRUP;
			selectElement.appendChild(option);
		});
		selectElement.disabled = selectElement.options.length === 0;
	} catch (error) {
		selectElement.disabled = true;
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function recYoket() {
	const fisNoInput = document.getElementById('recno');
	const girenurunkod = document.getElementById('ukodu');
	if (["0", ""].includes(fisNoInput.value)) {
		return;
	}
	const confirmDelete = confirm("Bu recete silinecek ?");
	if (!confirmDelete) {
		return;
	}
	document.body.style.cursor = "wait";
	const $silButton = $('#recsilButton');
	$silButton.prop('disabled', true).text('Siliniyor...');

	try {
		const response = await fetchWithSessionCheck("stok/recYoket", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ recno: fisNoInput.value, kodu: girenurunkod }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		clearInputs();
		document.getElementById("recno").value = "";
		document.getElementById("errorDiv").style.display = "none";
		document.getElementById("errorDiv").innerText = "";
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
		$silButton.prop('disabled', false).text('Sil');
	}
}

function prepareureKayit() {
	const receteDTO = {
		recno: document.getElementById("recno").value || "",
		anagrup: document.getElementById("anagrp").value || "",
		altgrup: document.getElementById("altgrp").value || "",
		aciklama: document.getElementById("aciklama").value || "",
		girenurkodu: document.getElementById("ukodu").value || "",
		durum: document.getElementById("durum").value || "",
	};
	tableData = getTableData();
	return { receteDTO, tableData, };
}

function getTableData() {
	const table = document.getElementById('recTable');
	const rows = table.querySelectorAll('tbody tr');
	const data = [];
	rows.forEach((row) => {
		const cells = row.querySelectorAll('td');
		const firstColumnValue = cells[2]?.querySelector('input')?.value || "";
		if (firstColumnValue.trim()) {
			const rowData = {
				ukodu: firstColumnValue,
				miktar: parseLocaleNumber(cells[5]?.querySelector('input')?.value),
			};
			data.push(rowData);
		}
	});
	return data;
}

async function recKayit() {
	const recno = document.getElementById("recno").value;

	const table = document.getElementById('recTable');
	const rows = table.rows;
	if (!recno || recno === "0" || rows.length === 0) {
		alert("Geçerli bir evrak numarası giriniz.");
		return;
	}
	const recetekayitDTO = prepareureKayit();
	const errorDiv = document.getElementById('errorDiv');
	const $kaydetButton = $('#reckaydetButton');
	$kaydetButton.prop('disabled', true).text('İşleniyor...');
	document.body.style.cursor = 'wait';
	try {
		const response = await fetchWithSessionCheck('stok/recKayit', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify(recetekayitDTO),
		});
		if (response.errorMessage.trim() !== "") {
			throw new Error(response.errorMessage);
		}
		clearInputs();
		document.getElementById("recno").value = "";
		document.getElementById("errorDiv").innerText = "";
		errorDiv.style.display = 'none';
	} catch (error) {
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
		errorDiv.style.display = 'block';
	} finally {
		document.body.style.cursor = 'default';
		$kaydetButton.prop('disabled', false).text('Kaydet');
	}
}

