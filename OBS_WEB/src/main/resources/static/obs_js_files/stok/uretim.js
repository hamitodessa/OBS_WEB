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
			throw new Error(responseBanka.errorMessage);
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

async function urnaramaYap() {

	const aramaInput = document.getElementById("girenurnkod").value;
	if (!aramaInput || aramaInput === "") {
		return;
	}
	document.body.style.cursor = "wait";
	document.getElementById("errorDiv").style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("stok/urnArama", {
			method: "POST",
			headers: {
				"Content-Type": "application/x-www-form-urlencoded"
			},
			body: new URLSearchParams({ arama: aramaInput })
		});
		const dto = response;
		if (dto.errorMessage === "Bu Numarada Urun Yok") {
			document.getElementById("errorDiv").innerText = dto.errorMessage;
			return;
		}
		document.getElementById("recetekod").value = dto.recete;
		document.getElementById("adi").innerText = dto.adi;
		document.getElementById("birim").innerText = dto.birim;
		document.getElementById("anagrpl").innerText = dto.anagrup;
		document.getElementById("altgrpl").innerText = dto.altgrup;
		document.getElementById("agirlik").innerText = dto.agirlik;
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
		//clearInputs();
		const dataSize = data.data.length;
		if (dataSize === 0) return;
		if (data.errorMessage) {
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage;
			return;
		}
		const tableBody = document.getElementById("tbody");

		let ukoduoptionsHTML = urnkodlar;

		tableBody.innerHTML = "";
		rowCounter = 0;
		rowCounter = 0;
		data.data.forEach(item => {
			if (item.Hareket === "C") {
				incrementRowCounter();
				const row = document.createElement("tr");
				row.innerHTML = `
			<td >
				<button id="bsatir_${rowCounter}" type="button" class="btn btn-secondary ml-2" onclick="satirsil(this)"><i class="fa fa-trash"></i></button>
			</td>
			<td contenteditable="false">CIKAN</td>
			<td>
			    <div style="position: relative; width: 100%;">
			        <input class="form-control cins_bold" list="ukoduOptions_${rowCounter}"  id="ukodu_${rowCounter}" 
		            onkeydown="focusNextCell(event, this)" value="${item.Urun_Kodu || ''}"  onchange="updateRowValues(this, ${rowCounter})">
			        <datalist id="ukoduOptions_${rowCounter}">${ukoduoptionsHTML}</datalist>
			        <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
			    </div>
			</td>
			<td contenteditable="false">${item.Adi || ''}</td>
			<td class="editable-cell" contenteditable="true" 
				    onfocus="selectAllContent(this)" 
				    onkeydown="focusNextRow(event, this)">${item.Izahat || ''}
			</td>
			<td>
				<div style="position: relative; width: 100%;">
				    <select class="form-control cins_bold" id="depo_${rowCounter}">
				        ${depolar.map(kod => `
				            <option value="${kod.DEPO}" ${item.Depo === kod.DEPO ? 'selected' : ''}>
				                ${kod.DEPO}
				            </option>
				        `).join('')}
				    </select>
				    <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
				</div>
			</td>
			<td class="editable-cell double-column" contenteditable="true" 
					    onfocus="selectAllContent(this)" 
					    onblur="formatInputTable3(this); updateColumnTotal()" 
					    onkeydown="focusNextRow(event, this)">${formatNumber2(item.Miktar)}
			</td>
			<td contenteditable="false" >${item.Birim || ''}</td>
			<td class="editable-cell double-column" contenteditable="true" 
					    onfocus="selectAllContent(this)" 
					    onblur="formatInputTable2(this); updateColumnTotal()" 
					    onkeydown="focusNextRow(event, this)">${formatNumber2(item.Fiat)}
			</td>
			<td class="editable-cell double-column" contenteditable="true" 
						   onfocus="selectAllContent(this)" 
						  onblur="formatInputTable2(this); updateColumnTotal()" 
						  onkeydown="focusNextRow(event, this)">${formatNumber2(item.Tutar)}
			</td>
		    `;
				tableBody.appendChild(row);
			}
		});

		for (let i = 0; i < data.data.length; i++) {
			const item = data.data[i];
			if (item.Hareket === "G") {
				document.getElementById("fisTarih").value = formatdateSaatsiz(item.Tarih);
				document.getElementById("uretmiktar").value = item.Miktar;
				document.getElementById("girenurnkod").value = item.Urun_Kodu;
				document.getElementById("anagrp").value = item.Ana_Grup || '';
				await anagrpChanged(document.getElementById("anagrp"));

				const selectElement = document.getElementById("altgrp");
				if (Array.from(selectElement.options).some(option => option.value.trim() === (item.Alt_Grup || '').trim())) {
					selectElement.value = (item.Alt_Grup || '').trim();
					selectElement.disabled = false;
				} else {
					selectElement.value = ''; // Geçerli bir değer değilse boş bırak
					selectElement.disabled = true;
				}
				const selectElementd = document.getElementById("depo");
				if (Array.from(selectElementd.options).some(option => option.value.trim() === (item.Depo || '').trim())) {
					selectElementd.value = (item.Depo || '').trim();
				} else {
					selectElementd.value = ''; // Geçerli bir değer değilse boş bırak
				}
				break; // İlk eşleşmeden sonra döngüden çık
			}
		}
		console.info(data);
		document.getElementById("aciklama").value = data.aciklama;
		urnaramaYap();
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
	for (let i = 0; i < 5; i++) {
		satirekle();
	}
}
function satirekle() {
	const table = document.getElementById("gbTable").getElementsByTagName("tbody")[0];
	const newRow = table.insertRow();
	incrementRowCounter();

	let ukoduoptionsHTML = urnkodlar.map(kod => `<option value="${kod.Kodu}">${kod.Kodu}</option>`).join("");
	//	let depoHTML = depolar.map(kod => `<option value="${kod.DEPO}">${kod.DEPO}</option>`).join("");
	newRow.innerHTML = `
		<td >
			<button id="bsatir_${rowCounter}" type="button" class="btn btn-secondary ml-2" onclick="satirsil(this)"><i class="fa fa-trash"></i></button>
		</td>
		<td contenteditable="false">CIKAN</td>
		<td>
		    <div style="position: relative; width: 100%;">
		        <input class="form-control cins_bold" list="ukoduOptions_${rowCounter}" maxlength="12" id="ukodu_${rowCounter}" 
		            onkeydown="focusNextCell(event, this)"  onchange="updateRowValues(this, ${rowCounter})">
		        <datalist id="ukoduOptions_${rowCounter}">${ukoduoptionsHTML}</datalist>
		        <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
		    </div>
		</td>
		<td contenteditable="false"></td>
		<td class="editable-cell" contenteditable="true" 
				    onfocus="selectAllContent(this)" 
				    onkeydown="focusNextRow(event, this)">
		</td>
		<td>
		<div style="position: relative; width: 100%;">
		    <select class="form-control cins_bold" id="depo_${rowCounter}">
		        ${depolar.map(kod => `
		            <option value="${kod.DEPO}" >
		                ${kod.DEPO}
		            </option>
		        `).join('')}
		    </select>
		    <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
		</div>
		</td>
		<td class="editable-cell double-column" contenteditable="true" 
				    onfocus="selectAllContent(this)" 
				    onblur="formatInputTable3(this); updateColumnTotal()" 
				    onkeydown="focusNextRow(event, this)">${formatNumber3(0)}
		</td>
		<td contenteditable="false" ></td>
		<td class="editable-cell double-column" contenteditable="true" 
		    onfocus="selectAllContent(this)" 
		    onblur="formatInputTable2(this); updateColumnTotal()" 
		    onkeydown="focusNextRow(event, this)">${formatNumber2(0)}
		</td>
		<td class="editable-cell double-column" contenteditable="true" 
			   onfocus="selectAllContent(this)" 
			  onblur="formatInputTable2(this); updateColumnTotal()" 
			  onkeydown="focusNextRow(event, this)">${formatNumber2(0)}
		</td>
	    `;
}

function selectAllContent(element) {
	const range = document.createRange();
	const selection = window.getSelection();
	range.selectNodeContents(element);
	selection.removeAllRanges();
	selection.addRange(range);
}

function satirsil(button) {
	const row = button.parentElement.parentElement;
	row.remove();
	updateColumnTotal();
}

function updateColumnTotal() {
	const cells = document.querySelectorAll('table tr td:nth-child(10)');
	const totalTutarCell = document.getElementById("totalTutar");
	let total = 0;
	totalTutarCell.textContent = "0.00";
	cells.forEach(cell => {
		const value = parseFloat(cell.textContent.replace(/,/g, '').trim());
		if (!isNaN(value) && value > 0) {
			total += value;
		}
	});
	totalTutarCell.textContent = total.toLocaleString(undefined, {
		minimumFractionDigits: 2, maximumFractionDigits: 2
	});

	const dbmik = parseFloat(document.getElementById("uretmiktar").value) || 0;
	const lblbirimfiati = document.getElementById("birimfiati");

	lblbirimfiati.innerText = (total / (dbmik === 0 ? 1 : dbmik)).toFixed(2);
}
async function updateRowValues(inputElement, rowCounter) {
	const selectedValue = inputElement.value;
	const uygulananfiat = document.getElementById("uygulananfiat").value;
	
	document.body.style.cursor = "wait";
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("stok/imalatcikan", {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ ukodu: selectedValue , fiatlama : uygulananfiat}),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}

		const row = inputElement.closest('tr');
		const cells = row.querySelectorAll('td');

		const turCell = cells[1];
		const adiCell = cells[3];
		const birimCell = cells[7];
		const fiatCell = cells[8];

	console.info(response);
		turCell.textContent = "CIKAN";
		adiCell.textContent = response.urun.adi;
		birimCell.textContent = response.urun.birim;
		fiatCell.textContent = response.fiat.toFixed(2);
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}
