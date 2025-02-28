async function fetchkod() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.innerText = "";
	errorDiv.style.display = "none";
	rowCounter = 0;
	depolar = "";
	urnkodlar = "";
	try {
		const response = await fetchWithSessionCheck("stok/stkgeturndepo", {
			method: "GET",
			headers: {
				"Content-Type": "application/json",
			},
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		urnkodlar = response.urnkodlar || [];
		depolar = response.depolar || [];
		initializeRows();
	} catch (error) {
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
		errorDiv.style.display = "block";
	}
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

function initializeRows() {

	rowCounter = 0;
	for (let i = 0; i < 5; i++) {
		satirekle();
	}
}
function satirekle() {
	const table = document.getElementById("zaiTable").getElementsByTagName("tbody")[0];
	const newRow = table.insertRow();
	incrementRowCounter();

	let ukoduoptionsHTML = urnkodlar.map(kod => `<option value="${kod.Kodu}">${kod.Kodu}</option>`).join("");
	newRow.innerHTML = `
		<td >
			<button id="bsatir_${rowCounter}" type="button" class="btn btn-secondary ml-2" onclick="satirsil(this)"><i class="fa fa-trash"></i></button>
		</td>
		<td>
		<div style="position: relative; width: 100%;">
		       <input class="form-control cins_bold" list="barkodOptions_${rowCounter}" maxlength="20" id="barkod_${rowCounter}" 
		        onkeydown="focusNextCell(event, this)" ondblclick="openurunkodlariModal('barkod_${rowCounter}', 'fatsatir','barkodkod')" onchange="updateRowValues(this,'Barkod')">
		       <datalist id="barkodOptions_${rowCounter}"></datalist>
		        <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
		</div>
		</td>
		<td>
		    <div style="position: relative; width: 100%;">
		        <input class="form-control cins_bold" list="ukoduOptions_${rowCounter}" maxlength="12" id="ukodu_${rowCounter}" 
		            onkeydown="focusNextCell(event, this)" ondblclick="openurunkodlariModal('ukodu_${rowCounter}', 'recetesatir','ukodukod')" onchange="updateRowValues(this,'Kodu')">
		        <datalist id="ukoduOptions_${rowCounter}">${ukoduoptionsHTML}</datalist>
		        <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
		    </div>
		</td>
		<td>
		<div style="position: relative; width: 100%;">
		    <select class="form-control" id="depo_${rowCounter}">
		        ${depolar.map(kod => `
		            <option value="${kod.DEPO}" >
		                ${kod.DEPO}
		            </option>
		        `).join('')}
		    </select>
		    <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
		</div>
		</td>
		<td>
			<input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)" 
			onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;" >
		</td>
		<td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur3(this)" 
			  onkeydown="focusNextCell(event, this)" value="${formatNumber3(0)}" style="text-align:right;">
		</td>		
		<td>
			<label class="form-control"style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label>
		</td>
		<td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)"  
		     onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
		</td>
		<td>
		     <input class="form-control" onfocus="selectAllContent(this)" onkeydown="focusNextRow(event, this)" >
		</td>
	    `;
}

function handleBlur3(input) {
	input.value = formatNumber3(input.value);
	updateColumnTotal();
}

function handleBlur(input) {
	input.value = formatNumber2(input.value);
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

async function updateRowValues(inputElement, kodbarkod) {
	const selectedValue = inputElement.value;
	document.body.style.cursor = "wait";
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("stok/urnbilgiArama", {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ deger: selectedValue, kodbarkod: kodbarkod }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const row = inputElement.closest('tr');
		const cells = row.querySelectorAll('td');


		const birimCell = cells[6];
		setLabelContent(birimCell, response.urun.birim);
		document.getElementById("adil").innerText = response.urun.adi || '';
		document.getElementById("anagrpl").innerText = response.urun.anagrup || '';
		document.getElementById("altgrpl").innerText = response.urun.altgrup || '';

	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function updateColumnTotal() {
	const rows = document.querySelectorAll('table tr');
	const totalTutarCell = document.getElementById("totalTutar");
	let total = 0;
	let totalmiktar = 0;
	totalTutarCell.textContent = "0.00";
	rows.forEach(row => {
		const input5 = row.querySelector('td:nth-child(5) input');
		const input6 = row.querySelector('td:nth-child(6) input');
		const input8 = row.querySelector('td:nth-child(8) input');
		if (input5 && input6) {
			const value5 = parseLocaleNumber(input5.value) || 0;
			const value6 = parseLocaleNumber(input6.value) || 0;
			const result = value5 * value6;
			input8.value = result.toLocaleString(undefined, {
				minimumFractionDigits: 2, maximumFractionDigits: 2
			});
			if (result > 0) {
				total += result;
			}
			totalmiktar += value6;
		}
	});
	totalTutarCell.textContent = total.toLocaleString(undefined, {
		minimumFractionDigits: 2, maximumFractionDigits: 2
	});
	document.getElementById("totalMiktar").textContent = totalmiktar.toLocaleString(undefined, {
		minimumFractionDigits: 3, maximumFractionDigits: 3
	});
}

function clearInputs() {

	document.getElementById("anagrp").value = '';
	document.getElementById("altgrp").innerHTML = '';
	document.getElementById("altgrp").disabled = true;

	document.getElementById("a1").value = '';
	document.getElementById("a2").value = '';

	document.getElementById("adil").innerText = '';
	document.getElementById("anagrpl").innerText = '';
	document.getElementById("altgrpl").innerText = '';

	document.getElementById("totalMiktar").textContent = "0.000";
	document.getElementById("totalTutar").textContent = "0.00";

	const tableBody = document.getElementById("tbody");
	tableBody.innerHTML = "";
	rowCounter = 0;
	initializeRows();
}

async function sonfis() {
	document.getElementById("errorDiv").style.display = "none";
	document.getElementById("errorDiv").innerText = '';
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck('stok/zaisonfis', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			}
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const data = response;
		const fisNoInput = document.getElementById('fisno');
		const errorDiv = document.getElementById('errorDiv');

		fisNoInput.value = data.fisno;
		if (data.fisNo === 0) {
			alert('Hata: Evrak numarası bulunamadı.');
			errorDiv.innerText = data.errorMessage;
			return;
		}
		zaiOku();
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function yeniFis() {
	document.body.style.cursor = "wait";
	const errorDiv = document.getElementById('errorDiv');
	errorDiv.innerText = "";
	clearInputs();
	try {
		const response = await fetchWithSessionCheck('stok/zaiyenifis', {
			method: "GET",
			headers: {
				"Content-Type": "application/json",
			},
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const fisNoInput = document.getElementById('fisno');
		fisNoInput.value = response.fisno;
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		errorDiv.style.display = 'none';
		document.body.style.cursor = "default";
	}
}

async function zaiOku() {
	const fisno = document.getElementById("fisno").value;
	if (!fisno) {
		return;
	}
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = '';

	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("stok/zaiOku", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ fisno: fisno }),
		});
		const data = response;
		clearInputs();

		const table = document.getElementById('zaiTable');
		const rowss = table.querySelectorAll('tbody tr');
		if (data.data.length > rowss.length) {
			const additionalRows = data.data.length - rowss.length;
			for (let i = 0; i < additionalRows; i++) {
				satirekle();
			}
		}
		const rows = table.querySelectorAll('tbody tr');
		data.data.forEach((item, index) => {
			const cells = rows[index].cells;
			const barkodInput = cells[1]?.querySelector('input');
			if (barkodInput) barkodInput.value = item.Barkod || "";
			const urunKoduInput = cells[2]?.querySelector('input');
			if (urunKoduInput) urunKoduInput.value = item.Urun_Kodu || "";
			const depoSelect = cells[3]?.querySelector('select');
			if (depoSelect) depoSelect.value = item.Depo || "";
			const fiatInput = cells[4]?.querySelector('input');
			if (fiatInput) fiatInput.value = formatNumber2(item.Fiat);
			const miktarInput = cells[5]?.querySelector('input');
			if (miktarInput) miktarInput.value = formatNumber3(item.Miktar * -1);
			setLabelContent(cells[6], item.Birim || '');
			const tutarInput = cells[7]?.querySelector('input');
			if (tutarInput) tutarInput.value = formatNumber2(item.Tutar * -1);
			const izahatInput = cells[8]?.querySelector('input');
			if (izahatInput) izahatInput.value = item.Izahat || "";
		});
		for (let i = 0; i < data.data.length; i++) {
			const item = data.data[i];
			document.getElementById("fisTarih").value = formatdateSaatsiz(item.Tarih);
			document.getElementById("anagrp").value = item.Ana_Grup || '';
			await anagrpChanged(document.getElementById("anagrp"));
			document.getElementById("altgrp").value = item.Alt_Grup || ''
			break;
		}
		document.getElementById("a1").value = data.aciklama1;
		document.getElementById("a2").value = data.aciklama2;

		updateColumnTotal();
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function prepareureKayit() {
	const zaiDTO = {
		fisno: document.getElementById("fisno").value || "",
		tarih: document.getElementById("fisTarih").value || "",
		anagrup: document.getElementById("anagrp").value || "",
		altgrup: document.getElementById("altgrp").value || "",
		acik1: document.getElementById("a1").value || "",
		acik2: document.getElementById("a2").value || "",
	};
	tableData = getTableData();
	return { zaiDTO, tableData, };
}

function getTableData() {
	const table = document.getElementById('zaiTable');
	const rows = table.querySelectorAll('tbody tr');
	const data = [];
	rows.forEach((row) => {
		const cells = row.querySelectorAll('td');
		const firstColumnValue = cells[2]?.querySelector('input')?.value || "";
		if (firstColumnValue.trim()) {
			const rowData = {
				ukodu: firstColumnValue,
				depo: cells[3]?.querySelector('select')?.value || "",
				fiat: parseLocaleNumber(cells[4]?.querySelector('input')?.value),
				miktar: parseLocaleNumber(cells[5]?.querySelector('input')?.value),
				tutar: parseLocaleNumber(cells[7]?.querySelector('input')?.value),
				izahat: cells[8]?.querySelector('input')?.value || "",
			};
			data.push(rowData);
		}
	});
	return data;
}

async function zaiKayit() {
	const fisno = document.getElementById("fisno").value;

	const table = document.getElementById('zaiTable');
	const rows = table.rows;
	if (!fisno || fisno === "0" || rows.length === 0) {
		alert("Geçerli bir evrak numarası giriniz.");
		return;
	}
	const zaikayitDTO = prepareureKayit();
	const errorDiv = document.getElementById('errorDiv');
	const $kaydetButton = $('#zaikaydetButton');
	$kaydetButton.prop('disabled', true).text('İşleniyor...');

	document.body.style.cursor = 'wait';
	try {
		const response = await fetchWithSessionCheck('stok/zaiKayit', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify(zaikayitDTO),
		});
		if (response.errorMessage.trim() !== "") {
			throw new Error(response.errorMessage);
		}
		clearInputs();
		document.getElementById("fisno").value = "";
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

async function zaiYoket() {
	const fisNoInput = document.getElementById('fisno');
	if (["0", ""].includes(fisNoInput.value)) {
		return;
	}
	const confirmDelete = confirm("Bu Uretim fisi silinecek ?");
	if (!confirmDelete) {
		return;
	}
	document.body.style.cursor = "wait";
	const $silButton = $('#zaisilButton');
	$silButton.prop('disabled', true).text('Siliniyor...');
	try {
		const response = await fetchWithSessionCheck("stok/zaiYoket", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ fisno: fisNoInput.value }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		clearInputs();
		document.getElementById("fisno").value = "";
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