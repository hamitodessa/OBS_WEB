function turChange() {
	const cins = document.getElementById("tur").value;
	if (cins === "Cek") {
		document.getElementById("cekbilgidiv").style.display = "block";
		document.getElementById("divcekbilgi").style.display = "block";

		document.getElementById("posbilgidiv").style.display = "none";
		document.getElementById("divposbilgi").style.display = "none";

		document.getElementById("tab2-tab").disabled = false;
	}
	else if (cins === "Kredi Karti") {
		document.getElementById("cekbilgidiv").style.display = "none";
		document.getElementById("divcekbilgi").style.display = "none";

		document.getElementById("posbilgidiv").style.display = "block";
		document.getElementById("divposbilgi").style.display = "block";

		document.getElementById("tab2-tab").disabled = true;
	}
	else {
		document.getElementById("cekbilgidiv").style.display = "none";
		document.getElementById("divcekbilgi").style.display = "none";

		document.getElementById("posbilgidiv").style.display = "none";
		document.getElementById("divposbilgi").style.display = "none";

		document.getElementById("tab2-tab").disabled = true;
	}
}

async function fetchHesapKodlariOnce() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.innerText = "";
	errorDiv.style.display = "none";
	rowCounter = 0;
	bankaIsimleri = "";
	subeIsimleri = "";
	try {
		const response = await fetchWithSessionCheck('/getBankaIsmi');
		//if (!response.ok) throw new Error('Banka isimlerini çekerken bir hata oluştu.');
		bankaIsimleri = response;
		const responsesube = await fetchWithSessionCheck('/getSubeIsmi');
		//if (!responsesube.ok) throw new Error('Şube isimlerini çekerken bir hata oluştu.');
		subeIsimleri = responsesube;
		initializeRows();
	} catch (error) {
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
		errorDiv.style.display = "block";
	}
}

function initializeRows() {
	for (let i = 0; i < 10; i++) {
		addRow();
	}

}

function addRow() {
	const table = document.getElementById("cekTable").getElementsByTagName("tbody")[0];
	const newRow = table.insertRow();

	incrementRowCounter();
	let optionsHTML = bankaIsimleri.map(kod => `<option value="${kod.BANKA}">${kod.BANKA}</option>`).join("");
	let subeHTML = subeIsimleri.map(kod => `<option value="${kod.SUBE}">${kod.SUBE}</option>`).join("");

	newRow.innerHTML = `
        <td>
            <div style="position: relative; width: 100%;">
                <input class="form-control cins_bold" 
                    list="bankaOptions_${rowCounter}" 
                    maxlength="40" id="banka_${rowCounter}" 
                    onkeydown="focusNextCell(event, this)">
                <datalist id="bankaOptions_${rowCounter}">${optionsHTML}</datalist>
                <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
            </div>
        </td>
        <td>
            <div style="position: relative; width: 100%;">
                <input class="form-control cins_bold" 
                    list="subeOptions_${rowCounter}" 
                    maxlength="40" id="sube_${rowCounter}" 
                    onkeydown="focusNextCell(event, this)">
                <datalist id="subeOptions_${rowCounter}">${subeHTML}</datalist>
                <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
            </div>
        </td>
        <td class="editable-cell" contenteditable="true" onkeydown="focusNextCell(event, this)"></td>
        <td class="editable-cell" contenteditable="true" onkeydown="focusNextCell(event, this)"></td>
        <td class="editable-cell" contenteditable="true" onkeydown="focusNextCell(event, this)"></td>
        <td><input type="date" class="form-control" onkeydown="focusNextCell(event, this)"></td>
        <td class="editable-cell double-column" contenteditable="true" 
            onfocus="selectAllContent(this)" 
            onblur="formatInputTable2(this); updateColumnTotal()" 
            onkeydown="focusNextRow(event, this)">0.00</td>
    `;
}

function selectAllContent(element) {
	const range = document.createRange();
	const selection = window.getSelection();

	range.selectNodeContents(element);
	selection.removeAllRanges();
	selection.addRange(range);
}

function updateColumnTotal() {
	const cells = document.querySelectorAll('.double-column');
	const tutarToplam = document.getElementById("tutar");
	const totalTutarCell = document.getElementById("totalTutar");
	const totalceksayisi = document.getElementById("ceksayisi");

	let total = 0;
	let totaladet = 0;
	cells.forEach(cell => {
		const value = parseFloat(cell.textContent.replace(/,/g, '').trim());
		if (!isNaN(value) && value > 0) {
			total += value;
			totaladet += 1;
		}
	});
	tutarToplam.value = total.toLocaleString(undefined, {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	});

	totalceksayisi.innerText = totaladet.toLocaleString(undefined, {
		minimumFractionDigits: 0,
		maximumFractionDigits: 0
	});

	totalTutarCell.textContent = total.toLocaleString(undefined, {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	});
}

function focusNextRow(event, element) {
	if (event.key === "Enter") {
		event.preventDefault();
		const currentRow = element.closest('tr');
		const nextRow = currentRow.nextElementSibling;

		if (nextRow) {
			const firstInput = nextRow.querySelector("td:first-child input");
			if (firstInput) {
				firstInput.focus();
				firstInput.select();
			}
		} else {
			addRow();
			const table = currentRow.parentElement;
			const newRow = table.lastElementChild;
			const firstInput = newRow.querySelector("td:first-child input");
			if (firstInput) {
				firstInput.focus();
				firstInput.select();
			}
		}
	}
}

function focusNextCell(event, element) {
	if (event.key === "Enter") {
		event.preventDefault();
		const currentCell = element.closest('td');
		const nextCell = currentCell.nextElementSibling;
		if (nextCell) {
			const focusableElement = nextCell.querySelector('input, [contenteditable="true"]');
			if (focusableElement) {
				focusableElement.focus();
				if (focusableElement.select) {
					focusableElement.select();
				}
			} else {
				nextCell.focus();
			}
		}
	}
}

//*****************************evrak kontrol **********************************************
function tahevrakOkuma(event) {
	if (event.key === "Enter" || event.keyCode === 13) {
		tahevrakOku();
	}
}

async function tahevrakOku() {
	const evrakNo = document.getElementById("tahevrakNo").value;
	let tah_ted;
	if (document.getElementById("tah_ted").value === "Tahsilat") {
		tah_ted = 0;
	}
	else {
		tah_ted = 1;
	}
	if (!evrakNo || evrakNo === "0") {
		return;
	}
	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";

	try {
		const response = await fetchWithSessionCheck("/cari/tahevrakOku", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ evrakNo: evrakNo, tah_ted: tah_ted }),
		});
		const data = response;
		tahclearInputs();
		const dto = data;
		if (dto.errorMessage) {
			errorDiv.style.display = "block";
			errorDiv.innerText = dto.errorMessage;
			return;
		}
		if (data.fisNo === null) {
			return;
		}
		if (dto.tur === 0) {
			document.getElementById("tur").value = "Nakit";
		}
		else if (dto.tur === 1) {
			document.getElementById("tur").value = "Cek";
		}
		else {
			document.getElementById("tur").value = "Kredi Karti";
		}
		if (dto.tur === 1) {
			const response = await fetchWithSessionCheck("cari/tahcekdokum", {
				method: 'POST',
				headers: {
					'Content-Type': 'application/x-www-form-urlencoded',
				},
				body: new URLSearchParams({ evrakNo: evrakNo, tah_ted: tah_ted }),
			});

			const data = response;
			const tableBody = document.getElementById("tableBody");
			tableBody.innerHTML = "";
			let optionsHTML = bankaIsimleri.map(kod => `<option value="${kod.BANKA}">${kod.BANKA}</option>`).join("");
			let subeHTML = subeIsimleri.map(kod => `<option value="${kod.SUBE}">${kod.SUBE}</option>`).join("");
			if (data.success) {
				rowCounter = 0;
				data.data.forEach(item => {
					incrementRowCounter();
					const row = document.createElement("tr");
					row.innerHTML = `
								        <td>
								            <div style="position: relative; width: 100%;">
								                <input class="form-control cins_bold" 
								                    list="bankaOptions_${rowCounter}" 
								                    maxlength="40" id="banka_${rowCounter}" 
								                    onkeydown="focusNextCell(event, this)" value="${item.BANKA || ''}" >
								                <datalist id="bankaOptions_${rowCounter}">${optionsHTML}</datalist>
								                <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
								            </div>
								        </td>
								        <td>
								            <div style="position: relative; width: 100%;">
								                <input class="form-control cins_bold" 
								                    list="subeOptions_${rowCounter}" 
								                    maxlength="40" id="sube_${rowCounter}" 
								                    onkeydown="focusNextCell(event, this)" value="${item.SUBE || ''}">
								                <datalist id="subeOptions_${rowCounter}">${subeHTML}</datalist>
								                <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
								            </div>
								        </td>
								        <td class="editable-cell" contenteditable="true" onkeydown="focusNextCell(event, this)">${item.SERI || ""}</td>
								        <td class="editable-cell" contenteditable="true" onkeydown="focusNextCell(event, this)">${item.HESAP || ""}</td>
								        <td class="editable-cell" contenteditable="true" onkeydown="focusNextCell(event, this)">${item.BORCLU || ""}</td>
								        <td><input type="date" class="form-control" onkeydown="focusNextCell(event, this)" value="${formatTableDate(item.TARIH)}"></td>
								        <td class="editable-cell double-column" contenteditable="true" 
								            onfocus="selectAllContent(this)" 
								            onblur="formatInputTable2(this); updateColumnTotal()" 
								            onkeydown="focusNextRow(event, this)">${formatNumber2(item.TUTAR)}</td>
								    `;
					tableBody.appendChild(row);
				});
			}
			updateColumnTotal();
		}
		turChange();

		document.getElementById("tahTarih").value = dto.tahTarih;
		document.getElementById("tutar").value = formatNumber2(dto.tutar);
		document.getElementById("tcheskod").value = dto.tcheskod || "";
		document.getElementById("adresheskod").value = dto.adresheskod || "";
		document.getElementById("dvz_cins").text = dto.dvz_cins || "TL";
		document.getElementById("posBanka").value = dto.posBanka || "";

		const ches = document.getElementById("tcheskod");
		ches.oninput();
		const ahes = document.getElementById("adresheskod");
		ahes.oninput();
		errorDiv.style.display = "none";
		errorDiv.innerText = "";
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function tahclearInputs() {

	document.getElementById("tutar").value = formatNumber2(0);
	document.getElementById("tcheskod").value = "";
	document.getElementById("adresheskod").value = "";
	document.getElementById("posBanka").value = "";
	document.getElementById("dvz_cins").value = "TL";
	document.getElementById("lblcheskod").innerText = "";
	document.getElementById("lbladrheskod").innerText = "";
	document.getElementById("ceksayisi").innerText = "0";

	const tableBody = document.getElementById("tableBody");
	tableBody.innerHTML = "";
	initializeRows();
	const totalTutarCell = document.getElementById("totalTutar");
	totalTutarCell.textContent = formatNumber2(0);
}
function tah_ted_cins_clear() {

	document.getElementById("tah_ted").value = "Tahsilat";
	document.getElementById("tur").value = "Nakit";
}

//********************************gerifisno ***********************************************
function tahgerifisNo() {
	const fisNoInput = document.getElementById('tahevrakNo');
	const errorDiv = document.getElementById('errorDiv');
	if (fisNoInput) {
		const currentValue = parseInt(fisNoInput.value, 10) || 0; // Eğer değer geçersizse 0 kabul edilir

		if (currentValue <= 0) {
			return;
		}
		fisNoInput.value = currentValue - 1;
		tah_ted_cins_clear();
		turChange();
		tahevrakOku();
	} else {
		errorDiv.innerText = "Hata: 'evrakNo' elemanı bulunamadı.";
	}
}

//******************************** ileri fis **********************************************
function tahilerifisNo() {
	const fisNoInput = document.getElementById('tahevrakNo');
	const errorDiv = document.getElementById('errorDiv');
	if (fisNoInput) {
		const currentValue = parseInt(fisNoInput.value, 10) || 0;
		fisNoInput.value = currentValue + 1;
		tah_ted_cins_clear();
		turChange();
		tahevrakOku();

	} else {
		errorDiv.innerText = "Hata: 'evrakNo' elemanı bulunamadı.";
	}
}

//************************************sonfisno *****************************************************
async function tahsonfisNo() {
	let tah_ted;
	if (document.getElementById("tah_ted").value === "Tahsilat") {
		tah_ted = 0;
	} else {
		tah_ted = 1;
	}

	try {
		const response = await fetchWithSessionCheck('/cari/tahsonfisNo', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ tah_ted: tah_ted }),
		});

		const data = response;
		const fisNoInput = document.getElementById('tahevrakNo');
		const errorDiv = document.getElementById('errorDiv');

		fisNoInput.value = data.fisNo;

		if (data.fisNo === 0) {
			alert('Hata: Evrak numarası bulunamadı.');
			errorDiv.innerText = data.errorMessage || "Bilinmeyen bir hata.";
			errorDiv.style.display = 'block';
			return;
		}

		if (data.errorMessage) {
			errorDiv.innerText = data.errorMessage;
			errorDiv.style.display = 'block';
		} else {
			errorDiv.style.display = 'none';
			tah_ted_cins_clear();
			turChange();
			tahevrakOku();
		}
	} catch (error) {
		const errorDiv = document.getElementById('errorDiv');
		errorDiv.style.display = 'block';
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	}
}

//************************************sonfisno *****************************************************
async function tahyenifis() {
	let tah_ted;
	if (document.getElementById("tah_ted").value === "Tahsilat") {
		tah_ted = "GIR";
	} else {
		tah_ted = "CIK";
	}

	try {
		const response = await fetchWithSessionCheck('/cari/tahyenifisNo', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ tah_ted: tah_ted }),
		});

		if (!response.ok) {
			throw new Error(`HTTP error! Status: ${response.status}`);
		}

		const data = response;
		const fisNoInput = document.getElementById('tahevrakNo');
		const errorDiv = document.getElementById('errorDiv');

		fisNoInput.value = data.fisNo;

		if (data.fisNo === 0) {
			errorDiv.innerText = data.errorMessage || "Bilinmeyen bir hata.";
			errorDiv.style.display = 'block';
			return;
		}

		if (data.errorMessage) {
			errorDiv.innerText = data.errorMessage;
			errorDiv.style.display = 'block';
		}
		else {
			errorDiv.style.display = 'none';
			tahclearInputs();
			turChange();
		}
	} catch (error) {
		const errorDiv = document.getElementById('errorDiv');
		errorDiv.style.display = 'block';
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	}
}

function getTableData() {
	const table = document.getElementById('cekTable'); // Tablo ID'sini değiştirin
	const rows = table.querySelectorAll('tbody tr');
	const data = [];

	rows.forEach((row) => {
		const cells = row.querySelectorAll('td');
		const firstColumnValue = cells[0]?.querySelector('input')?.value || "";
		const lastColumnValue = parseLocaleNumber(cells[6]?.textContent || "0");
		if (!firstColumnValue.trim() || lastColumnValue <= 0) {
			return;
		}

		const rowData = {
			banka: firstColumnValue,
			sube: cells[1]?.querySelector('input')?.value || "",
			seri: cells[2]?.textContent || "",
			hesap: cells[3]?.textContent || "",
			borclu: cells[4]?.textContent || "",
			tarih: cells[5]?.querySelector('input')?.value || "",
			tutar: lastColumnValue, // Sayıya çevrilmiş hali
		};

		data.push(rowData);
	});

	return data;
}

function prepareRequestPayload() {

	let turu = "";
	if (document.getElementById("tur").value === "Nakit") {
		turu = 0;
	} else if (document.getElementById("tur").value === "Cek") {
		turu = 1;
	} else {
		turu = 2;   //Kredi KArti
	}
	let tah_ted = 0;
	if (document.getElementById("tah_ted").value === "Tahsilat") {
		tah_ted = 0;
	} else if (document.getElementById("tah_ted").value === "Tediye") {
		tah_ted = 1;
	}

	const tahsilatDTO = {
		tah_ted: tah_ted,
		fisNo: document.getElementById("tahevrakNo").value || "",
		tahTarih: getFullDateWithTimeAndMilliseconds(document.getElementById("tahTarih").value),
		tutar: parseLocaleNumber(document.getElementById("tutar").value),
		tcheskod: document.getElementById("tcheskod").value || "",
		adresheskod: document.getElementById("adresheskod").value || "",
		posBanka: document.getElementById("posBanka").value || "",
		dvz_cins: document.getElementById("dvz_cins").value || "",
		tur: turu,
		fisnoyazdir: document.getElementById("fisnoyazdir").checked,
	};
	let tableData = null;
	if (document.getElementById("tur").value === "Cek") {
		tableData = getTableData();
	}
	return { tahsilatDTO, tableData, };
}


async function tahfisKayit() {
	const tahsilatKayitDTO = prepareRequestPayload();
	const errorDiv = document.getElementById('errorDiv');
	try {
		const response = await fetchWithSessionCheck('cari/tahsilatKayit', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify(tahsilatKayitDTO),
		});
		if (response.errorMessage.trim() !== "") {
				throw new Error(response.errorMessage);
		}
		tahclearInputs();
		tah_ted_cins_clear();
		turChange();
		document.getElementById("tahevrakNo").value = "0";
		document.getElementById("errorDiv").innerText = "";
		errorDiv.style.display = 'none'; // Hata mesajını gizle
	} catch (error) {
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
		errorDiv.style.display = 'block'; // Hata mesajını görünür yap
	}
}

//************************evrak sil ***********************************************
async function tahfisYoket() {
	const evrakNo = document.getElementById("tahevrakNo").value;
	let tah_ted;

	if (document.getElementById("tah_ted").value === "Tahsilat") {
		tah_ted = 0;
	} else {
		tah_ted = 1;
	}
	if (!evrakNo || evrakNo === "0") {
		alert("Geçerli bir evrak numarası giriniz.");
		return;
	}
	const confirmDelete = confirm("Bu evrak numarasını silmek istediğinize emin misiniz?");
	if (!confirmDelete) {
		return;
	}
	document.body.style.cursor = "wait";
	const $silButton = $('#tahsilButton');
	$silButton.prop('disabled', true).text('Siliniyor...');
	try {
		const response = await fetchWithSessionCheck("/cari/tahfisYoket", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ evrakNo: evrakNo, tah_ted: tah_ted }),
		});
		if (response.errorMessage.trim() !== "") {
				throw new Error(response.errorMessage);
		}
		tahclearInputs();
		tah_ted_cins_clear();
		turChange();
		document.getElementById("tahevrakNo").value = "0";
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

async function tahsilatdownloadReport() {
	const tahsilatKayitDTO = prepareRequestPayload();
	const errorDiv = document.getElementById("errorDiv"); // Hata mesajını göstermek için
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	const $indirButton = $('#indirButton');
	$indirButton.prop('disabled', true).text('İşleniyor...');
	try {
		const response = await fetchWithSessionCheckForDownload('cari/tahsilat_download', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(tahsilatKayitDTO),
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
		errorDiv.style.display = "block"; // Hata mesajını göster
		errorDiv.innerText = error.message || "Bilinmeyen bir hata oluştu.";
	} finally {
		$indirButton.prop('disabled', false).text('Rapor İndir');
		document.body.style.cursor = "default";
	}
}

async function tahcariIsle() {
	const hesapKodu = $('#tahsilatBilgi').val();
	const tahsilatKayitDTO = prepareRequestPayload();
	tahsilatKayitDTO.tahsilatDTO.borc_alacak = hesapKodu;
	const errorDiv = document.getElementById('errorDiv');
	try {
		const response = await fetchWithSessionCheck('cari/tahsilatCariKayit', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify(tahsilatKayitDTO),
		});
		if (response.errorMessage.trim() !== "") {
					throw new Error(response.errorMessage);
		}
		document.getElementById("errorDiv").innerText = "";
		errorDiv.style.display = 'none';
	} catch (error) {
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
		errorDiv.style.display = 'block';
	}
}