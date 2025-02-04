async function fetchkoddepo() {
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

async function urnaramaYap(kodbarkod) {

	const aramaInput = document.getElementById("girenurnkod").value;
	if (!aramaInput || aramaInput === "") {
		return;
	}
	document.body.style.cursor = "wait";
	document.getElementById("errorDiv").style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("stok/urnbilgiArama", {
			method: "POST",
			headers: {
				"Content-Type": "application/x-www-form-urlencoded"
			},
			body: new URLSearchParams({ deger: aramaInput , kodbarkod:kodbarkod })
		});
		
		if (response.errorMessage === "Bu Numarada Urun Yok") {
			document.getElementById("errorDiv").innerText = response.errorMessage;
			return;
		}
		const dto = response.urun;
		document.getElementById("recetekod").value = dto.recete;
		document.getElementById("adi").innerText = dto.adi;
		document.getElementById("birim").innerText = dto.birim;
		document.getElementById("anagrpl").innerText = dto.anagrup;
		document.getElementById("altgrpl").innerText = dto.altgrup;
		document.getElementById("agirlik").innerText = formatNumber3(dto.agirlik);
		document.getElementById("barkod").innerText = dto.barkod;
		document.getElementById("sinif").innerText = dto.sinif;

		const imgElement = document.getElementById("resimGoster");
		if (dto.base64Resim && dto.base64Resim.trim() !== "") {
			const base64String = 'data:image/jpeg;base64,' + dto.base64Resim.trim();
			imgElement.src = base64String;
			imgElement.style.display = "block";
		} else {
			imgElement.src = "";
			imgElement.style.display = "none";
		}
		document.getElementById("errorDiv").style.display = "none";
		document.getElementById("errorDiv").innerText = "";
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function sonfis() {
	try {
		const response = await fetchWithSessionCheck('stok/sonfis', {
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
		if (data.errorMessage) {
			errorDiv.innerText = data.errorMessage;
			errorDiv.style.display = 'block';
		} else {
			errorDiv.style.display = 'none';
			uretimOku();
		}
	} catch (error) {
		const errorDiv = document.getElementById('errorDiv');
		errorDiv.style.display = 'block';
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	}
}

async function uretimOku() {
	const fisno = document.getElementById("fisno").value;
	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("stok/uretimOku", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ fisno: fisno }),
		});
		const data = response;
		clearInputs();
		const dataSize = data.data.length;
		if (dataSize === 0) return;
		if (data.errorMessage) {
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage;
			return;
		}
		const table = document.getElementById('imaTable');
		const rowss = table.querySelectorAll('tbody tr');
		if (data.data.length > rowss.length) {
			const additionalRows = data.data.length - rowss.length;
			for (let i = 0; i < additionalRows; i++) {
				satirekle();
			}
		}
		const rows = table.querySelectorAll('tbody tr');
		data.data.forEach((item, index) => {
			if (item.Hareket === "C" && index < rows.length) {
				const cells = rows[index].cells;
				setLabelContent(cells[1], "CIKAN");
				setLabelContent(cells[3], item.Adi || '');
				setLabelContent(cells[7], item.Birim || '');
				const urunKoduInput = cells[2]?.querySelector('input');
				if (urunKoduInput) urunKoduInput.value = item.Urun_Kodu || "";

				const izahatInput = cells[4]?.querySelector('input');
				if (izahatInput) izahatInput.value = item.Izahat || "";

				const miktarInput = cells[6]?.querySelector('input');
				if (miktarInput) miktarInput.value = formatNumber3(item.Miktar * -1);

				const fiatInput = cells[8]?.querySelector('input');
				if (fiatInput) fiatInput.value = formatNumber2(item.Fiat);

				const tutarInput = cells[9]?.querySelector('input');
				if (tutarInput) tutarInput.value = formatNumber2(item.Tutar * -1);

				const depoSelect = cells[5]?.querySelector('select');
				if (depoSelect) depoSelect.value = item.Depo || "";
			}
		});

		for (let i = 0; i < data.data.length; i++) {
			const item = data.data[i];
			if (item.Hareket === "G") {
				document.getElementById("fisTarih").value = formatdateSaatsiz(item.Tarih);
				document.getElementById("uretmiktar").value = item.Miktar;
				document.getElementById("girenurnkod").value = item.Urun_Kodu;

				document.getElementById("anagrp").value = item.Ana_Grup || '';
				document.getElementById("mikbirim").innerText = item.Birim;
				document.getElementById("dvzcins").value = item.Doviz;

				await anagrpChanged(document.getElementById("anagrp"));

				document.getElementById("altgrp").value = item.Alt_Grup || ''
				const selectElementd = document.getElementById("depo");
				if (Array.from(selectElementd.options).some(option => option.value.trim() === (item.Depo || '').trim())) {
					selectElementd.value = (item.Depo || '').trim();
				} else {
					selectElementd.value = '';
				}
				break;
			}
		}
		document.getElementById("aciklama").value = data.aciklama;
		await urnaramaYap("Kodu");
		updateColumnTotal();
		errorDiv.style.display = "none";
		errorDiv.innerText = "";
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function initializeRows() {

	rowCounter = 0; // Satır sayacını sıfırla
	for (let i = 0; i < 5; i++) {
		satirekle();
	}
}
function satirekle() {
	const table = document.getElementById("imaTable").getElementsByTagName("tbody")[0];
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
		            onkeydown="focusNextCell(event, this)" ondblclick="openurunkodlariModal('ukodu_${rowCounter}', 'imalatsatir','ukodukod')" onchange="updateRowValues(this)">
		        <datalist id="ukoduOptions_${rowCounter}">${ukoduoptionsHTML}</datalist>
		        <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
		    </div>
		</td>
		<td>
			<label class="form-control"style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label>
		</td>
		<td>
		      <input class="form-control"  onkeydown="focusNextCell(event, this)" onfocus="selectAllContent(this)"  >
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
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur3(this)" 
			  onkeydown="focusNextCell(event, this)" value="${formatNumber3(0)}" style="text-align:right;">
		</td>
		<td>
				<label class="form-control" style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label>
		</td>
		<td>
			<input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)" 
			onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;" >
		</td>
		<td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)"  
		     onkeydown="focusNextRow(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
		</td>
	    `;
}

function handleBlur3(input) {
	input.value = formatNumber3(parseLocaleNumber(input.value));
	updateColumnTotal();
}
function handleBlur(input) {
	input.value = formatNumber2(parseLocaleNumber(input.value));
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

function updateColumnTotal() {
	const rows = document.querySelectorAll('table tr');
	const totalTutarCell = document.getElementById("totalTutar");
	let total = 0;
	totalTutarCell.textContent = "0.00";
	rows.forEach(row => {
		const input7 = row.querySelector('td:nth-child(7) input');
		const input9 = row.querySelector('td:nth-child(9) input');
		const input10 = row.querySelector('td:nth-child(10) input');

		if (input7 && input9 && input10) {
			const value7 = parseLocaleNumber(input7.value) || 0;
			const value9 = parseLocaleNumber(input9.value) || 0;
			const result = value7 * value9;
			input10.value = result.toLocaleString(undefined, {
				minimumFractionDigits: 2, maximumFractionDigits: 2
			});
			if (result > 0) {
				total += result;
			}
		}
	});
	totalTutarCell.textContent = total.toLocaleString(undefined, {
		minimumFractionDigits: 2, maximumFractionDigits: 2
	});
	const dbmik = parseLocaleNumber(document.getElementById("uretmiktar").value) || 0;
	const lblbirimfiati = document.getElementById("birimfiati");
	lblbirimfiati.innerText = (total / (dbmik === 0 ? 1 : dbmik)).toLocaleString(undefined, {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	});
}

async function updateRowValues(inputElement) {
	const selectedValue = inputElement.value;
	const uygulananfiat = document.getElementById("uygulananfiat").value;
	const fisTarih = document.getElementById("fisTarih").value;
	const fiatTarih = document.getElementById("fiatTarih").value;
	document.body.style.cursor = "wait";
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("stok/imalatcikan", {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ ukodu: selectedValue, fiatlama: uygulananfiat, fisTarih: fisTarih, fiatTarih: fiatTarih }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const row = inputElement.closest('tr');
		const cells = row.querySelectorAll('td');

		const turCell = cells[1];
		const adiCell = cells[3];
		const birimCell = cells[7];
		const fiatCell = cells[8]?.querySelector('input');
		setLabelContent(turCell, "CIKAN");
		setLabelContent(adiCell, response.urun.adi);
		setLabelContent(birimCell, response.urun.birim);
		fiatCell.value = formatNumber2(response.urun.fiat || 0);
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}
function setLabelContent(cell, content) {
	const span = cell.querySelector('label span');
	if (span) {
		span.textContent = content ? content : '\u00A0'; // Eğer içerik boşsa, boşluk karakteri eklenir
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
				break; // Döngüden çık
			} else {
				nextCell = nextCell.nextElementSibling;
			}
		}
	}
}

function uygulananfiatchange() {
	const uygfiat = document.getElementById("uygulananfiat").value;
	const datlabel = document.getElementById("datlabel");
	const fiatTarih = document.getElementById("fiatTarih");
	if (uygfiat == "ortfiat") {
		datlabel.style.visibility = "visible";
		fiatTarih.style.visibility = "visible";
	}
	else {
		datlabel.style.visibility = "hidden";
		fiatTarih.style.visibility = "hidden";
	}
}

function clearInputs() {
	document.getElementById("uygulananfiat").value = "";
	document.getElementById("recetekod").value = "";
	document.getElementById("anagrp").value = "";
	document.getElementById("altgrp").value = "";
	document.getElementById("depo").value = "";
	document.getElementById("girenurnkod").value = "";
	document.getElementById("uretmiktar").value = "0";
	document.getElementById("birimfiati").innerText = "0.00";
	document.getElementById("aciklama").value = "";
	document.getElementById("dvzcins").value = document.getElementById("defaultdvzcinsi").value || 'TL';      

	document.getElementById("adi").innerText = "";
	document.getElementById("birim").innerText = "";
	document.getElementById("anagrpl").innerText = "";
	document.getElementById("altgrpl").innerText = "";
	document.getElementById("agirlik").innerText = "";
	document.getElementById("barkod").innerText = "";
	document.getElementById("sinif").innerText = "";
	document.getElementById("mikbirim").innerText = "";

	const selectElement = document.getElementById("altgrp");
	selectElement.disabled = true;

	const tableBody = document.getElementById("tbody");
	tableBody.innerHTML = "";

	const imgElement = document.getElementById("resimGoster");
	imgElement.src = "";
	imgElement.style.display = "none";

	rowCounter = 0;
	initializeRows();
	const totalTutarCell = document.getElementById("totalTutar");
	totalTutarCell.textContent = formatNumber2(0);
}

async function yeniFis() {
	const errorDiv = document.getElementById('errorDiv');
	errorDiv.innerText = "";
	clearInputs();
	try {
		const response = await fetchWithSessionCheck('stok/uretimyenifis', {
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

async function uretimYap() {

	const recetekod = document.getElementById("recetekod").value;
	if (!recetekod || recetekod === "") {
		return;
	}
	document.body.style.cursor = "wait";
	document.getElementById("errorDiv").style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("stok/hesapla", {
			method: "POST",
			headers: {
				"Content-Type": "application/x-www-form-urlencoded"
			},
			body: new URLSearchParams({ recetekod: recetekod })
		});
		const data = response;
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const uretmiktar = document.getElementById("uretmiktar").value;
		const tableBody = document.getElementById("tbody");
		tableBody.innerHTML = "";
		initializeRows();
		const table = document.getElementById('imaTable');
		const rows = table.querySelectorAll('tbody tr');
		rowCounter = 0;
		if (data.data.length > rows.length) {
			const additionalRows = data.data.length - rows.length;
			for (let i = 0; i < additionalRows; i++) {
				satirekle();
			}
		}
		let index = 0;
		data.data.forEach((item) => {
			if (item.Tur === "Cikan" && index < rows.length) {
				const cells = rows[index].cells;
				setLabelContent(cells[1], item.Tur);
				setLabelContent(cells[3], item.Adi || '');
				setLabelContent(cells[7], item.Birim || '');

				const urunKoduInput = cells[2]?.querySelector('input');
				if (urunKoduInput) urunKoduInput.value = item.Kodu || "";

				const izahatInput = cells[4]?.querySelector('input');
				if (izahatInput) izahatInput.value = "";

				const miktarInput = cells[6]?.querySelector('input');
				if (miktarInput) miktarInput.value = formatNumber3(item.Miktar * uretmiktar) || "0";

				const fiatInput = cells[8]?.querySelector('input');
				if (fiatInput) fiatInput.value = formatNumber2(0);

				const tutarInput = cells[9]?.querySelector('input');
				if (tutarInput) tutarInput.value = formatNumber2(0);

				const depoSelect = cells[5]?.querySelector('select');
				if (depoSelect) depoSelect.value = "";

				index++;
			}
			else {
				document.getElementById("mikbirim").innerText = item.Birim;
			}
		});
		updateColumnTotal();
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function prepareureKayit() {
	const uretimDTO = {
		fisno: document.getElementById("fisno").value || "",
		tarih: document.getElementById("fisTarih").value || "",
		anagrup: document.getElementById("anagrp").value || "",
		altgrup: document.getElementById("altgrp").value || "",
		depo: document.getElementById("depo").value || "",
		girenurkodu: document.getElementById("girenurnkod").value || "",

		aciklama: document.getElementById("aciklama").value || "",
		dvzcins: document.getElementById("dvzcins").value || "",
		uremiktar: parseLocaleNumber(document.getElementById("uretmiktar")?.value || 0),
		toptutar: parseLocaleNumber(document.getElementById("totalTutar")?.value || 0),

	};
	tableData = getTableData();
	return { uretimDTO, tableData, };
}

function getTableData() {
	const table = document.getElementById('imaTable');
	const rows = table.querySelectorAll('tbody tr');
	const data = [];
	rows.forEach((row) => {
		const cells = row.querySelectorAll('td');
		const firstColumnValue = cells[2]?.querySelector('input')?.value || "";
		if (firstColumnValue.trim()) {
			const rowData = {
				ukodu: firstColumnValue,
				izahat: cells[4]?.querySelector('input')?.value || "",
				depo: cells[5]?.querySelector('select')?.value || "",
				miktar: parseLocaleNumber(cells[6]?.querySelector('input')?.value || 0),
				fiat: parseLocaleNumber(cells[8]?.querySelector('input')?.value || 0),
				tutar: parseLocaleNumber(cells[9]?.querySelector('input')?.value || 0),
			};
			data.push(rowData);
		}
	});
	return data;
}

async function ureKayit() {
	const fisno = document.getElementById("fisno").value;

	const table = document.getElementById('imaTable');
	const rows = table.rows;
	if (!fisno || fisno === "0" || rows.length === 0) {
		alert("Geçerli bir evrak numarası giriniz.");
		return;
	}
	const uretimkayitDTO = prepareureKayit();
	const errorDiv = document.getElementById('errorDiv');
	const $kaydetButton = $('#urekaydetButton');
	$kaydetButton.prop('disabled', true).text('İşleniyor...');

	document.body.style.cursor = 'wait';
	try {
		const response = await fetchWithSessionCheck('stok/ureKayit', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify(uretimkayitDTO),
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

async function ureYoket() {
	const fisNoInput = document.getElementById('fisno');
	if (["0", ""].includes(fisNoInput.value)) {
		return;
	}
	const confirmDelete = confirm("Bu Uretim fisi silinecek ?");
	if (!confirmDelete) {
		return;
	}
	document.body.style.cursor = "wait";
	const $silButton = $('#uresilButton');
	$silButton.prop('disabled', true).text('Siliniyor...');
	try {
		const response = await fetchWithSessionCheck("stok/ureYoket", {
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