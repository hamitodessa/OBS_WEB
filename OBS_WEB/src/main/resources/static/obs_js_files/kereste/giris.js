
$(document).ready(function() {
	applyMask();
});

function applyMask() {
	$("input[id^='ukodu_']").inputmask({
		mask: "AA-999-9999-9999",
		placeholder: "_",
		definitions: {
			'A': {
				validator: "[A-Za-z0-9]",
				cardinality: 1
			}
		}
	});
}

function updatePaketM3() {
	const rows = document.querySelectorAll('table tr');
	let paketMap = new Map();
	let paketadet = 0 ;
	rows.forEach(row => {
		const paketnoCell = row.querySelector('td:nth-child(4) input');
		const m3Cell = row.querySelector('td:nth-child(6) label span');
		const paketm3Span = row.querySelector('td:nth-child(7) span');
		if (!paketnoCell || !m3Cell || !paketm3Span) return;
		const paketNo = paketnoCell.value.trim();
		const m3Value = parseLocaleNumber(m3Cell.textContent.trim()) || 0;
		if (!paketNo) return;
		if (!paketMap.has(paketNo)) {
			paketMap.set(paketNo, { total: 0, rows: [] });
		}
		paketMap.get(paketNo).total += m3Value;
		paketMap.get(paketNo).rows.push(paketm3Span);
	});
	paketMap.forEach((data) => {
		const rowCount = data.rows.length;
		data.rows.forEach((cell, index) => {
			if (index === rowCount - 1) {
				cell.textContent = data.total ? data.total.toFixed(3) : '\u00A0';
				paketadet += 1 ;
			} else {
				cell.textContent = '\u00A0';
			}
		});
	});
	document.getElementById("totalPakadet").textContent = "Paket:" + formatNumber0(paketadet);
}

function initializeRows() {

	rowCounter = 0;
	for (let i = 0; i < 5; i++) {
		satirekle();
	}
}
function satirekle() {
	const table = document.getElementById("kerTable").getElementsByTagName("tbody")[0];
	const rowCount = table.rows.length;
	if (rowCount == 250) {
		alert("En fazla 250 satır ekleyebilirsiniz.");
		return
	}
	const newRow = table.insertRow();
	incrementRowCounter();

	newRow.innerHTML = `
		<td >
			<button id="bsatir_${rowCounter}" type="button" class="btn btn-secondary ml-2" onclick="satirsil(this)"><i class="fa fa-trash"></i></button>
		</td>
   		<td>
		    <input class="form-control" maxlength="20" id="barkod_${rowCounter}" 
		        onkeydown="focusNextCell(event, this)" onchange="updateRowValues(this)">
		</td>
		<td>
		    <input class="form-control" maxlength="16" id="ukodu_${rowCounter}"  style="font-weight:bold;"
		        onkeydown="focusNextCell(event, this)" placeholder="XX-XXX-XXXX-XXXX" onchange="updateValues(this)">
		</td>
		<td>
		    <input class="form-control" maxlength="16" id="pakno_${rowCounter}" 
		        onkeydown="if(event.key === 'Enter') paketkontrol(event,this)"  >
		</td>
		<td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur0(this)" 
			  onkeydown="focusNextCell(event, this)" value="${formatNumber0(0)}" style="text-align:right;">
		</td>
        <td>
		    <label class="form-control" style="display: block;width:100%;height:100%;text-align:right;font-weight:bold;"><span>&nbsp;</span></label>
		</td>
        <td>
			<label class="form-control" style="display: block;width:100%;height:100%;text-align:right;font-weight:bold; color:darkgreen;"><span>&nbsp;</span></label>
		</td>
		<td>
			<input class="form-control" onfocus="selectAllContent(this)" onkeydown="focusNextCell(event, this)">
		</td>
		<td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)"  
		     onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
		</td>
        <td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)"  
		     onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
		</td>
        <td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)"  
		     onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
		</td>
        <td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)"  
		     onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;font-weight:bold;">
		</td>
        <td>
			<input class="form-control" onfocus="selectAllContent(this)" onkeydown="focusNextRow(event, this)">
		</td>
		<td style="display: none;"></td>
		<td style="display: none;">1900-01-01 00:00:00.000</td>
		<td style="display: none;"></td>
		<td style="display: none;"></td>
		<td style="display: none;"></td>
		<td style="display: none;"></td>
		<td style="display: none;"></td>
		<td style="display: none;"></td>
		<td style="display: none;"></td>
		<td style="display: none;"></td>
		<td style="display: none;"></td>
		<td style="display: none;">0</td>
		<td style="display: none;">0</td>
		<td style="display: none;">0</td>
		<td style="display: none;">0</td>
		<td style="display: none;"></td>
		<td style="display: none;">0</td>
		<td style="display: none;"></td>
		<td style="display: none;"></td>
	    `;
	applyMask(); // Yeni eklenen inputa maskeyi uygula
}


function satirsil(button) {
	const row = button.parentElement.parentElement;
	row.remove();
	updateColumnTotal();
}

function handleBlur3(input) {
	updateValues(input);
	input.value = formatNumber3(parseLocaleNumber(input.value));
	updateColumnTotal();
}
function handleBlur(input) {
	updateValues(input);
	input.value = formatNumber2(parseLocaleNumber(input.value));
	updateColumnTotal();
}

function handleBlur0(input) {
	updateValues(input);
	input.value = formatNumber0(parseLocaleNumber(input.value));
	updateColumnTotal();
}

function selectAllContent(element) {
	if (element && element.select) {
		element.select();
	}
}

function updateColumnTotal() {
	const rows = document.querySelectorAll('table tr');
	const totalSatirCell = document.getElementById("totalSatir");
	const totalTutarCell = document.getElementById("totalTutar");
	const totalMiktarCell = document.getElementById("totalMiktar");
	const totalM3Cell = document.getElementById("totalM3");
	const totalPaketM3Cell = document.getElementById("totalPaketM3");

	const tevoran = document.getElementById("tevoran");

	let total = 0;
	let totalm3 = 0;
	let totalpakm3 = 0;
	let totalMiktar = 0;
	let double_1 = 0;
	let double_2 = 0;
	let double_5 = 0;
	let double_4 = 0;
	let totalsatir = 0;

	totalSatirCell.textContent = "0";
	totalTutarCell.textContent = "0.00";
	totalMiktarCell.textContent = "0";
	totalM3Cell.textContent = "0.000";
	totalPaketM3Cell.textContent = "0.000";

	rows.forEach(row => {
		const secondColumn = row.querySelector('td:nth-child(3) input');
		const mik = row.querySelector('td:nth-child(5) input');
		const m3 = row.querySelector('td:nth-child(6) label span');
		const pm3 = row.querySelector('td:nth-child(7) label span');
		const fiat = row.querySelector('td:nth-child(9) input');
		const iskonto = row.querySelector('td:nth-child(10) input');

		const kdvv = row.querySelector('td:nth-child(11) input');
		const tutar = row.querySelector('td:nth-child(12) input');

		if (secondColumn && secondColumn.value.trim() !== '') {
			totalsatir += 1;
		}
		if (fiat && m3) {
			const isk = parseLocaleNumber(iskonto.value) || 0;
			const kdv = parseLocaleNumber(kdvv.value) || 0;
			const fia = parseLocaleNumber(fiat.value) || 0;
			const m33 = parseLocaleNumber(m3.textContent.trim() || 0);
			const result = fia * m33;

			tutar.value = formatNumber2(result);
			totalm3 += m33;
			totalpakm3 += parseLocaleNumber(pm3.textContent.trim() || 0);
			totalMiktar += mik.value ? parseLocaleNumber(mik.value) : 0;
			if (result > 0) {
				total += result;
				double_5 += result;
				double_1 += (result * isk) / 100;
				double_2 += ((result - (result * isk / 100)) * kdv) / 100;
			}
		}
	});
	document.getElementById("iskonto").innerText = formatNumber2(double_1);
	document.getElementById("bakiye").innerText = formatNumber2(double_5 - double_1);
	document.getElementById("kdv").innerText = formatNumber2(double_2);

	const tev = parseLocaleNumber(tevoran.value) || 0;

	double_4 = tev;
	document.getElementById("tevedkdv").innerText = formatNumber2((double_2 / 10) * double_4);

	double_0 = (double_5 - double_1) + double_2;
	document.getElementById("tevdahtoptut").innerText = formatNumber2(double_0);


	document.getElementById("beyedikdv").innerText = formatNumber2((double_2 - (double_2 / 10) * double_4));
	document.getElementById("tevhartoptut").innerText = formatNumber2((double_5 - double_1) + (double_2 - (double_2 / 10) * double_4));

	totalTutarCell.textContent = total.toLocaleString(undefined, {
		minimumFractionDigits: 2, maximumFractionDigits: 2
	});
	totalMiktarCell.textContent = totalMiktar.toLocaleString(undefined, {
		minimumFractionDigits: 0, maximumFractionDigits: 0
	});
	totalM3Cell.textContent = totalm3.toLocaleString(undefined, {
		minimumFractionDigits: 3, maximumFractionDigits: 3
	});

	totalPaketM3Cell.textContent = totalpakm3.toLocaleString(undefined, {
		minimumFractionDigits: 3, maximumFractionDigits: 3
	});

	totalSatirCell.textContent = totalsatir.toLocaleString(undefined, {
		minimumFractionDigits: 0, maximumFractionDigits: 0
	});
}

async function anagrpChanged(anagrpElement) {
	const anagrup = anagrpElement.value;
	const errorDiv = document.getElementById("errorDiv");
	const selectElement = document.getElementById("altgrp");
	selectElement.innerHTML = '';
	if (anagrup === "") {
		selectElement.disabled = true;
		return;
	}
	document.body.style.cursor = "wait";
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	try {
		const response = await fetchWithSessionCheck("kereste/altgrup", {
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

function hesaplaM3(inputElement) {
	if (!inputElement) return;
	const row = inputElement.closest('tr');
	if (!row) return;
	const cells = row.querySelectorAll('td');
	const urkodCell = cells[2]?.querySelector('input')?.value?.trim();
	const miktarCell = cells[4]?.querySelector('input');
	if (!urkodCell || !miktarCell) return;
	let miktar = parseFloat(miktarCell.value) || 0;
	let token = urkodCell.split("-");
	if (token.length !== 4) return;
	let m3 = 0;
	let deger1 = token[1]?.trim();
	let deger2 = token[2]?.trim();
	let deger3 = token[3]?.trim();
	if (deger1 && deger2 && deger3) {
		m3 = ((parseFloat(deger1) * parseFloat(deger2) * parseFloat(deger3)) * miktar) / 1000000000;
	}
	const M3Cell = cells[5];
	if (M3Cell) {
		const span = M3Cell.querySelector('label span');
		span.textContent = m3.toFixed(3);
	}
}

async function updateValues(inputElement) {
	hesaplaM3(inputElement)
	updatePaketM3();
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

function clearInputs() {

	document.getElementById("urunadi").innerText = '';
	document.getElementById("anaalt").innerText = '';
	document.getElementById("iskonto").innerText = "0.00";
	document.getElementById("bakiye").innerText = "0.00";
	document.getElementById("kdv").innerText = "0.00";
	document.getElementById("tevedkdv").innerText = "0.00";
	document.getElementById("tevdahtoptut").innerText = "0.00";
	document.getElementById("beyedikdv").innerText = "0.00";
	document.getElementById("tevhartoptut").innerText = "0.00";
	document.getElementById("tevoran").value = "0.00";

	document.getElementById("ozelkod").value = '';
	document.getElementById("anagrp").value = '';
	document.getElementById("altgrp").innerHTML = '';
	document.getElementById("mensei").value = '';
	document.getElementById("depo").value = '';
	document.getElementById("nakliyeci").value = '';
	document.getElementById("altgrp").disabled = true;

	document.getElementById("carikod").value = '';
	document.getElementById("adreskod").value = '';
	document.getElementById("cariadilbl").innerText = "";
	document.getElementById("adresadilbl").innerText = "";

	document.getElementById("adreskod").value = '';

	document.getElementById("dovizcins").value = document.getElementById("defaultdvzcinsi").value || 'TL';
	document.getElementById("kur").value = '0.0000';

	document.getElementById("not1").value = '';
	document.getElementById("not2").value = '';
	document.getElementById("not3").value = '';
	document.getElementById("fatmikyazdir").checked = false;

	document.getElementById("a1").value = '';
	document.getElementById("a2").value = '';

	const tableBody = document.getElementById("tbody");
	tableBody.innerHTML = "";
	rowCounter = 0;
	initializeRows();
	document.getElementById("totalSatir").textContent = formatNumber0(0);
	document.getElementById("totalMiktar").textContent = formatNumber0(0);
	document.getElementById("totalM3").textContent = formatNumber3(0);
	document.getElementById("totalPaketM3").textContent = formatNumber3(0);
	document.getElementById("totalTutar").textContent = formatNumber2(0);
	document.getElementById("totalPakadet").textContent = "" ;
}

async function kerOku() {
	const fisno = document.getElementById("fisno").value.trim();
	if (!fisno) {
		return;
	}
	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("kereste/kerOku", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ fisno: fisno, cins: 'GIRIS' }),
		});
		const data = response;
		clearInputs();
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const dataSize = data.data.length;
		if (dataSize === 0) return;

		const table = document.getElementById('kerTable');
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
			if (urunKoduInput) urunKoduInput.value = item.Kodu || "";

			const paknoInput = cells[3]?.querySelector('input');
			if (paknoInput) paknoInput.value = item.Paket_No || "";

			const mikInput = cells[4]?.querySelector('input');
			if (mikInput) mikInput.value = formatNumber0(item.Miktar || 0);

			const m3Label = cells[5]?.querySelector('label span');
			if (m3Label) m3Label.textContent = formatNumber3(hesapM3(item.Kodu, item.Miktar));

			const pm3Label = cells[6]?.querySelector('label span');
			if (pm3Label) pm3Label.textContent = '';

			const konsInput = cells[7]?.querySelector('input');
			if (konsInput) konsInput.value = item.Konsimento || "";

			const fiatInput = cells[8]?.querySelector('input');
			if (fiatInput) fiatInput.value = formatNumber2(item.Fiat || 0);

			const iskInput = cells[9]?.querySelector('input');
			if (iskInput) iskInput.value = formatNumber2(item.Iskonto || 0);

			const kdvInput = cells[10]?.querySelector('input');
			if (kdvInput) kdvInput.value = formatNumber2(item.Kdv || 0);

			const tutarInput = cells[11]?.querySelector('input');
			if (tutarInput) tutarInput.value = formatNumber2(item.Tutar || 0);

			const izahatInput = cells[12]?.querySelector('input');
			if (izahatInput) izahatInput.value = item.Izahat || "";

			cells[13].innerText = item.Cikis_Evrak || '';
			cells[14].innerText = item.CTarih || '';
			cells[15].innerText = item.CKdv || '';
			cells[16].innerText = item.CDoviz || '';
			cells[17].innerText = item.CFiat || '';
			cells[18].innerText = item.CTutar || '';
			cells[19].innerText = item.CKur || '';
			cells[20].innerText = item.CCari_Firma || '';
			cells[21].innerText = item.CAdres_Firma || '';
			cells[22].innerText = item.CIskonto || '';
			cells[23].innerText = item.CTevkifat || '';
			cells[24].innerText = item.CAna_Grup || '';
			cells[25].innerText = item.CAlt_Grup || '';
			cells[26].innerText = item.CDepo || '';
			cells[27].innerText = item.COzel_Kod || '';
			cells[28].innerText = item.CIzahat || '';
			cells[29].innerText = item.CNakliyeci || '';
			cells[30].innerText = item.CUser || '';
			cells[31].innerText = item.CSatir || '';
		});
		for (let i = 0; i < data.data.length; i++) {
			const item = data.data[i];
			document.getElementById("fisTarih").value = formatdateSaatsiz(item.Tarih);
			document.getElementById("anagrp").value = item.Ana_Grup || '';
			document.getElementById("kur").value = item.Kur;
			await anagrpChanged(document.getElementById("anagrp"));
			document.getElementById("altgrp").value = item.Alt_Grup || '';
			document.getElementById("ozelkod").value = item.Ozel_Kod || '';
			document.getElementById("mensei").value = item.Mensei || '';
			document.getElementById("depo").value = item.Depo || '';
			document.getElementById("nakliyeci").value = item.Nakliyeci || ''
			document.getElementById("tevoran").value = item.Tevkifat || '0';
			document.getElementById("carikod").value = item.Cari_Firma || '';
			document.getElementById("adreskod").value = item.Adres_Firma || '';
			document.getElementById("dovizcins").value = item.Doviz || '';
			break;
		}
		document.getElementById("a1").value = data.a1;
		document.getElementById("a2").value = data.a2;
		document.getElementById("not1").value = data.dipnot[0];
		document.getElementById("not2").value = data.dipnot[1];
		document.getElementById("not3").value = data.dipnot[2];
		updatePaketM3();
		updateColumnTotal();

		hesapAdiOgren(document.getElementById("carikod").value, 'cariadilbl')
		adrhesapAdiOgren('adreskod', 'adresadilbl');
		errorDiv.style.display = "none";
		errorDiv.innerText = "";
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function hesapM3(ukodu, miktar) {

	let token = ukodu.split("-");
	if (token.length !== 4) return 0;
	let m3 = 0;
	let deger1 = token[1]?.trim();
	let deger2 = token[2]?.trim();
	let deger3 = token[3]?.trim();
	if (deger1 && deger2 && deger3) {
		m3 = ((parseFloat(deger1) * parseFloat(deger2) * parseFloat(deger3)) * miktar) / 1000000000;
	}
	return m3;

}

async function sonfis() {
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck('kereste/sonfis', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ cins: 'GIRIS' }),
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
			kerOku();
		}
	} catch (error) {
		const errorDiv = document.getElementById('errorDiv');
		errorDiv.style.display = 'block';
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function yeniFis() {
	const errorDiv = document.getElementById('errorDiv');
	errorDiv.innerText = "";
	clearInputs();
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck('kereste/yenifis', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ cins: 'GIRIS' }),
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

		document.body.style.cursor = "default";
	}
}

async function kerYoket() {
	const fisno = document.getElementById("fisno").value.trim();
	const table = document.getElementById('kerTable');
	const rows = table.rows;
	
	if (!fisno || fisno === "0" || rows.length === 0) {
		alert("Geçerli bir evrak numarası giriniz.");
		return;
	}
	const confirmDelete = confirm("Bu Fis silinecek ?");
	if (!confirmDelete) {
		return;
	}
	document.body.style.cursor = "wait";
	const $silButton = $('#kersilButton');
	$silButton.prop('disabled', true).text('Siliniyor...');
	try {
		const response = await fetchWithSessionCheck("kereste/fisYoket", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ fisno }),
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

function prepareureKayit() {
	const keresteDTO = {
		fisno: document.getElementById("fisno").value || "",
		tarih: document.getElementById("fisTarih").value || "",
		ozelkod: document.getElementById("ozelkod").value || "",
		anagrup: document.getElementById("anagrp").value || "",
		altgrup: document.getElementById("altgrp").value || "",
		depo: document.getElementById("depo").value || "",
		mensei: document.getElementById("mensei").value || "",
		nakliyeci: document.getElementById("nakliyeci").value || "",

		carikod: document.getElementById("carikod").value || "",
		adreskod: document.getElementById("adreskod").value || "",

		dvzcins: document.getElementById("dovizcins").value || "",
		kur: parseLocaleNumber(document.getElementById("kur").value) || 0.0,
		tevoran: parseLocaleNumber(document.getElementById("tevoran").value) || 0.0,

		not1: document.getElementById("not1").value || "",
		not2: document.getElementById("not2").value || "",
		not3: document.getElementById("not3").value || "",

		acik1: document.getElementById("a1").value || "",
		acik2: document.getElementById("a2").value || "",

	};
	tableData = getTableData();
	return { keresteDTO, tableData, };
}

function getTableData() {
	const table = document.getElementById('kerTable');
	const rows = table.querySelectorAll('tbody tr');
	const data = [];
	rows.forEach((row) => {
		const cells = row.querySelectorAll('td');
		const firstColumnValue = cells[2]?.querySelector('input')?.value || "";
		if (firstColumnValue.trim()) {
			const rowData = {
				barkod: cells[1]?.querySelector('input')?.value || "",
				ukodu: firstColumnValue,
				paketno: cells[3]?.querySelector('input')?.value || "",
				miktar: parseLocaleNumber(cells[4]?.querySelector('input')?.value || 0),
				konsimento: cells[7]?.querySelector('input')?.value || "",
				fiat: parseLocaleNumber(cells[8]?.querySelector('input')?.value || 0),
				iskonto: parseLocaleNumber(cells[9]?.querySelector('input')?.value || 0),
				kdv: parseLocaleNumber(cells[10]?.querySelector('input')?.value || 0),
				tutar: parseLocaleNumber(cells[11]?.querySelector('input')?.value || 0),
				izahat: cells[12]?.querySelector('input')?.value || "",

				cevrak: cells[13]?.textContent || "",
				ctarih: cells[14]?.textContent || "1900-01-01 00:00:00.000",
				ckdv: parseFloat(cells[15]?.textContent.trim()) || 0.0,
				cdoviz: cells[16]?.textContent || "",
				cfiat: parseFloat(cells[17]?.textContent.trim()) || 0.0,
				ctutar: parseFloat(cells[18]?.textContent.trim()) || 0.0,
				ckur: parseFloat(cells[19]?.textContent.trim()) || 0.0,
				ccarifirma: cells[20]?.textContent || "",
				cadresfirma: cells[21]?.textContent || "",
				ciskonto: parseFloat(cells[22]?.textContent.trim()) || 0.0,
				ctevkifat: parseFloat(cells[23]?.textContent.trim()) || 0.0,
				canagrup: parseInt(cells[24]?.textContent.trim(), 10) || 0,
				caltgrup: parseInt(cells[25]?.textContent.trim(), 10) || 0,
				cdepo: parseInt(cells[26]?.textContent.trim(), 10) || 0,
				cozelkod: parseInt(cells[27]?.textContent.trim(), 10) || 0,
				cizahat: cells[28]?.textContent || "",
				cnakliyeci: parseInt(cells[29]?.textContent.trim(), 10) || 0,
				cuser: cells[30]?.textContent || "",
				csatir: parseInt(cells[31]?.textContent.trim(), 10) || 0,

				m3: parseFloat(cells[5]?.textContent.trim()) || 0.0,
				pakm3: parseFloat(cells[6]?.textContent.trim()) || 0.0,
			};
			data.push(rowData);
		}
	});
	return data;
}
async function kerKayit() {
	const fisno = document.getElementById("fisno").value;
	const table = document.getElementById('kerTable');
	const rows = table.rows;
	if (!fisno || fisno === "0" || rows.length === 0) {
		alert("Geçerli bir evrak numarası giriniz.");
		return;
	}
	const kodkontrol = kontrolEt();
	if (! kodkontrol) return ;

	const kerestekayitDTO = prepareureKayit();
	const errorDiv = document.getElementById('errorDiv');
	const $kaydetButton = $('#kerkaydetButton');
	$kaydetButton.prop('disabled', true).text('İşleniyor...');
	document.body.style.cursor = 'wait';
	try {
		const response = await fetchWithSessionCheck('kereste/girKayit', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify(kerestekayitDTO),
		});
		if (response.errorMessage) {
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

async function kercariIsle() {
	const hesapKodu = $('#kerBilgi').val();
	const fisno = document.getElementById("fisno").value;
	const table = document.getElementById('kerTable');
	const rows = table.rows;
	if (!fisno || fisno === "0" || rows.length === 0) {
		alert("Geçerli bir evrak numarası giriniz.");
		return;
	}
	const $carkaydetButton = $('#carkayitButton');
	$carkaydetButton.prop('disabled', true).text('İşleniyor...');

	const keresteDTO = {
		fisno: document.getElementById("fisno").value || "",
		tarih: document.getElementById("fisTarih").value || "",
		carikod: document.getElementById("carikod").value || "",
		miktar: document.getElementById("totalM3").textContent || 0,
		tutar: parseLocaleNumber(document.getElementById("tevhartoptut").innerText || 0),
		karsihesapkodu: hesapKodu,
	};
	const errorDiv = document.getElementById('errorDiv');
	document.body.style.cursor = 'wait';
	try {
		const response = await fetchWithSessionCheck('kereste/kergcariKayit', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify(keresteDTO),
		});
		if (response.errorMessage.trim() !== "") {
			throw new Error(response.errorMessage);
		}
		document.getElementById("errorDiv").innerText = "";
		errorDiv.style.display = 'none';
	} catch (error) {
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
		errorDiv.style.display = 'block';
	} finally {
		$carkaydetButton.prop('disabled', false).text('Cari Kaydet');
		document.body.style.cursor = 'default';
	}
}

async function paketkontrol(event, input) {
	const fisno = document.getElementById("fisno").value;
	const errorDiv = document.getElementById('errorDiv');
	errorDiv.style.display = 'none';
	errorDiv.innerText = "";

	const row = input.closest('tr');
	if (!row) return;
	const cells = row.querySelectorAll('td');
	const konsCell = cells[7]?.querySelector('input')?.value?.trim() ?? '';
	if (!konsCell) {
		alert("Kontrol icin once Konsimento No giriniz!");
		return;
	}

	const pakno = input.value + "-" + konsCell;
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("kereste/paket_oku", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ pno: pakno, cins: 'GIRIS', fisno: fisno }),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const data = response;
		if (data.mesaj != "") {
			document.body.style.cursor = "default";
			setTimeout(() => {
				alert(data.mesaj);
			}, 100);
			return;
		}
		updateValues(input);
		focusNextCell(event, input)
	} catch (error) {
		const errorDiv = document.getElementById('errorDiv');
		errorDiv.style.display = 'block';
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}


function kontrolEt() {
	const table = document.getElementById("kerTable").getElementsByTagName("tbody")[0];
	let isValid = true;
	for (let row of table.rows) {
		let kod = row.cells[2].innerText.trim();
		if(kod != ''){
			if (!kontrolFormat(kod)) {
			      isValid = false;
			      alert(`Geçersiz kod bulundu: ${kod}`);
			      break;
			    }
		}
		
	}
	if (isValid) {
		return true;
	} else {
		return false;
	}
}
function kontrolFormat(kod) {
	// Mask: "AA-999-9999-9999" (İlk iki karakter harf, geri kalanı rakam)
	const regex = /^[A-Z]{2}-\d{3}-\d{4}-\d{4}$/;
	return regex.test(kod);
}

async function downloadgiris() {
	const fisno = document.getElementById("fisno").value;
	const table = document.getElementById('kerTable');
	const rows = table.rows;
	if (!fisno || fisno === "0" || rows.length === 0) {
		alert("Geçerli bir evrak numarasi giriniz.");
		return;
	}

	const keresteyazdirDTO = {
		...prepareureKayit(),
		cikisbilgiDTO: cikisbilgiler()
	};
	const errorDiv = document.getElementById('errorDiv');
	const $indirButton = $('#girisdownloadButton');
	document.body.style.cursor = "wait";

	$indirButton.prop('disabled', true).text('İşleniyor...');
	try {
		const response = await fetchWithSessionCheckForDownload('kereste/giris_download', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify(keresteyazdirDTO),
		});
		if (response.blob) {
			const disposition = response.headers.get('Content-Disposition');
			const fileName = disposition.match(/filename="(.+)"/)[1];
			const url = window.URL.createObjectURL(response.blob);
			const a = document.createElement("a");
			a.href = url;
			a.download = fileName;
			document.body.appendChild(a);
			a.click();
			a.remove();
			window.URL.revokeObjectURL(url);
		} else {
			throw new Error("Dosya indirilemedi.");
		}
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Bilinmeyen bir hata oluştu.";
	} finally {
		$indirButton.prop('disabled', false).text('Yazdir');
		document.body.style.cursor = "default";
	}
}

function cikisbilgiler() {
	return {
		totaltutar: document.getElementById("totalTutar").innerText.trim(),
		totalmiktar: document.getElementById("totalMiktar").innerText.trim(),
		totalm3: document.getElementById("totalM3").innerText.trim(),
		totalpaketm3: document.getElementById("totalPaketM3").innerText.trim(),
		paketsayi: document.getElementById("totalPakadet").textContent.replace("Paket:", "").trim() || "",

		iskonto: document.getElementById("iskonto").innerText.trim(),
		bakiye: document.getElementById("bakiye").innerText.trim(),
		kdv: document.getElementById("kdv").innerText.trim(),
		tevedkdv: document.getElementById("tevedkdv").innerText.trim(),
		tevdahtoptut: document.getElementById("tevdahtoptut").innerText.trim(),
		beyedikdv: document.getElementById("beyedikdv").innerText.trim(),
		tevhartoptut: document.getElementById("tevhartoptut").innerText.trim(),
	}

}

async function girismailAt() {
	localStorage.removeItem("tableData");
	localStorage.removeItem("grprapor");
	localStorage.removeItem("tablobaslik");
	const keresteyazdirDTO = {
		...prepareureKayit(),
		cikisbilgiDTO: cikisbilgiler()
	};
	localStorage.setItem("keresteyazdirDTO", JSON.stringify(keresteyazdirDTO));
	const degerler = "kergiris";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}