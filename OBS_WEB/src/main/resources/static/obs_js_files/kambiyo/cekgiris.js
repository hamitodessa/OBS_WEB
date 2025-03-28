async function fetchBankaSubeOnce() {
	const errorDiv2 = document.getElementById("errorDiv2").textContent;
	if (errorDiv2 != "") {
		return;
	}
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.innerText = "";
	errorDiv.style.display = "none";
	rowCounter = 0;
	bankaIsimleri = [];
	subeIsimleri = [];
	ilkBorclu = [];
	try {
		const response = await fetchWithSessionCheck("kambiyo/kamgetDegiskenler", {
			method: "GET",
			headers: {
				"Content-Type": "application/json",
			},
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		bankaIsimleri = response.bankaIsmi || [];
		subeIsimleri = response.subeIsmi || [];
		ilkBorclu = response.ilkBorclu || [];
		initializeRows();
	} catch (error) {
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
		errorDiv.style.display = "block";
	}
}

function initializeRows() {
	rowCounter = 0;
	for (let i = 0; i < 5; i++) {
		cekgiraddRow();
	}
}

function cekgiraddRow() {
	const table = document.getElementById("gbTable").getElementsByTagName("tbody")[0];
	const newRow = table.insertRow();
	incrementRowCounter();
	let optionsHTML = bankaIsimleri.map(kod => `<option value="${kod.Banka}">${kod.Banka}</option>`).join("");
	let subeHTML = subeIsimleri.map(kod => `<option value="${kod.Sube}">${kod.Sube}</option>`).join("");
	let ilkborcluHTML = ilkBorclu.map(kod => `<option value="${kod.Ilk_Borclu}">${kod.Ilk_Borclu}</option>`).join("");
	newRow.innerHTML = `
		<td >
			<button id="bsatir_${rowCounter}" type="button" class="btn btn-secondary ml-2" onclick="cekgirremoveRow(this)"><i class="fa fa-trash"></i></button>
		</td>
		<td>
	        <div style="position: relative; width: 100%;">
	           <input class="form-control cins_bold" maxlength="10" id="cekno_${rowCounter}" onkeydown="focusNextCell(event,this)" onchange="cekkontrol(this)">
	        </div>
	    </td>
		<td>
			<input type="date" class="form-control" onkeydown="focusNextCell(event, this)">
		</td>
        <td>
            <div style="position: relative; width: 100%;">
                <input class="form-control cins_bold" list="bankaOptions_${rowCounter}" maxlength="25" id="banka_${rowCounter}" onkeydown="focusNextCell(event, this)">
                <datalist id="bankaOptions_${rowCounter}">${optionsHTML}</datalist>
                <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
            </div>
        </td>
        <td>
            <div style="position: relative; width: 100%;">
                <input class="form-control cins_bold" list="subeOptions_${rowCounter}" maxlength="25" id="sube_${rowCounter}" onkeydown="focusNextCell(event, this)">
                <datalist id="subeOptions_${rowCounter}">${subeHTML}</datalist>
                <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
            </div>
        </td>
		<td>
			<input class="form-control cins_bold"  maxlength="15" id="serino_${rowCounter}" onkeydown="focusNextCell(event, this)">
		</td>
		<td>
		    <div style="position: relative; width: 100%;">
		        <input class="form-control cins_bold" list="ilkborcluOptions_${rowCounter}" maxlength="30" id="ilkborclu_${rowCounter}" 
		            onkeydown="focusNextCell(event, this)">
		            <datalist id="ilkborcluOptions_${rowCounter}">${ilkborcluHTML}</datalist>
		            <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
		    </div>
		</td>
		<td>
        	<input class="form-control cins_bold"  maxlength="15" id="hesap_${rowCounter}" onkeydown="focusNextCell(event, this)">
		</td>
		<td>
			<input class="form-control cins_bold"  maxlength="3" id="cins_${rowCounter}" onkeydown="focusNextCell(event, this)">
		</td>
		<td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)"  
			     onkeydown="focusNextRow(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
		</td>
		<td style="display: none;"></td>
		<td style="display: none;">1900-01-01</td>
		<td style="display: none;"></td>
		<td style="display: none;"></td>
		<td style="display: none;">1900-01-01</td>
		<td style="display: none;"></td>
    `;
}

function handleBlur(input) {
	input.value = formatNumber2(parseLocaleNumber(input.value));
	updateColumnTotal();
}
function cekgirremoveRow(button) {
	const row = button.parentElement.parentElement;
	row.remove();
	updateColumnTotal();
}

function selectAllContent(element) {
	const range = document.createRange();
	const selection = window.getSelection();
	range.selectNodeContents(element);
	selection.removeAllRanges();
	selection.addRange(range);
}

function updateColumnTotal() {
	const cells = document.querySelectorAll('tr td:nth-child(10) input');
	const totalTutarCell = document.getElementById("totalTutar");
	const totalceksayisi = document.getElementById("ceksayisi");
	let total = 0;
	let totaladet = 0;

	cells.forEach(input => {
		const value = parseFloat(input.value.replace(/,/g, '').trim());
		if (!isNaN(value) && value > 0) {
			total += value;
			totaladet += 1;
		}
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
			const secondInput = nextRow.querySelector("td:nth-child(2) input");
			if (secondInput) {
				secondInput.focus();
				secondInput.select();
			}
		} else {
			cekgiraddRow();
			const table = currentRow.parentElement;
			const newRow = table.lastElementChild;
			const secondInput = newRow.querySelector("td:nth-child(2) input");
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

function clearInputs() {
	document.getElementById("ozelkod").value = "";
	document.getElementById("dvzcinsi").value = "";
	document.getElementById("bcheskod").value = "";
	document.getElementById("lblcheskod").innerText = "";
	document.getElementById("lblfaiztutari").innerText = "0.00";
	document.getElementById("faiz").value = "";
	document.getElementById("lblortgun").innerText = "0";
	document.getElementById("aciklama1").value = "";
	document.getElementById("aciklama2").value = "";
	const tableBody = document.getElementById("tbody");
	tableBody.innerHTML = "";
	rowCounter = 0;
	initializeRows();
	const totalTutarCell = document.getElementById("totalTutar");
	totalTutarCell.textContent = formatNumber2(0);
}

async function songirisBordro() {
	try {
		const response = await fetchWithSessionCheck('kambiyo/sonbordroNo', {
			method: 'GET',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
		});
		const bordroNo = await response.bordroNo;
		const bordronoInput = document.getElementById('bordrono');
		const errorDiv = document.getElementById('errorDiv');
		bordronoInput.value = bordroNo;
		if (bordroNo.errorMessage) {
			errorDiv.innerText = bordroNo.errorMessage;
			errorDiv.style.display = 'block';
		} else {
			errorDiv.innerText = "";
			errorDiv.style.display = 'none';
			clearInputs();
			bordroOku();
		}
	} catch (error) {
		const errorDiv = document.getElementById('errorDiv');
		errorDiv.style.display = 'block';
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	}
}

function bordroOkuma(event) {
	if (event.key === "Enter" || event.keyCode === 13) {
		bordroOku();
	}
}

async function bordroOku() {
	const bordroNo = document.getElementById("bordrono").value;
	if (!bordroNo) {
		return;
	}
	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("kambiyo/bordroOku", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ bordroNo: bordroNo }),
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

		const tableBody = document.getElementById("tbody");
		tableBody.innerHTML = "";
		const table = document.getElementById('gbTable');
		const rows = table.querySelectorAll('tbody tr');
		if (data.data.length > rows.length) {
			const additionalRows = data.data.length - rows.length;
			for (let i = 0; i < additionalRows + 1; i++) {
				cekgiraddRow();
			}
		}
		const rowss = table.querySelectorAll('tbody tr');
		data.data.forEach((item, index) => {
			const cells = rowss[index].cells;

			const ceknoInput = cells[1]?.querySelector('input');
			if (ceknoInput) ceknoInput.value = item.Cek_No || "";

			const vadeInput = cells[2]?.querySelector('input');
			if (vadeInput) vadeInput.value = item.Vade;

			const bankaInput = cells[3]?.querySelector('input');
			if (bankaInput) bankaInput.value = item.Banka || "";

			const subeInput = cells[4]?.querySelector('input');
			if (subeInput) subeInput.value = item.Sube || "";

			const serinoInput = cells[5]?.querySelector('input');
			if (serinoInput) serinoInput.value = item.Seri_No;

			const ilkborcluSelect = cells[6]?.querySelector('input');
			if (ilkborcluSelect) ilkborcluSelect.value = item.Ilk_Borclu || "";

			const cekhspnoInput = cells[7]?.querySelector('input');
			if (cekhspnoInput) cekhspnoInput.value = item.Cek_Hesap_No || "";

			const cinsInput = cells[8]?.querySelector('input');
			if (cinsInput) cinsInput.value = item.Cins || "";

			const tutarInput = cells[9]?.querySelector('input');
			if (tutarInput) tutarInput.value = formatNumber2(item.Tutar);

			cells[10].innerText = item.Cikis_Bordro || '';
			cells[11].innerText = item.Cikis_Tarihi;
			cells[12].innerText = item.Cikis_Musteri || '';
			cells[13].innerText = item.Durum || '';
			cells[14].innerText = item.T_Tarih;
			cells[15].innerText = item.Cikis_Ozel_Kod || '';

		});

		getTableData();
		updateColumnTotal();
		document.getElementById("ozelkod").value = data.data[0].Giris_Ozel_Kod;
		document.getElementById("dvzcinsi").value = data.data[0].Cins;
		document.getElementById("bcheskod").value = data.data[0].Giris_Musteri;
		document.getElementById("bordroTarih").value = data.data[0].Giris_Tarihi;
		const ches = document.getElementById("bcheskod");
		ches.oninput();
		const responseAciklama = await fetchWithSessionCheck("kambiyo/kamgetAciklama", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ bordroNo: bordroNo }),
		});
		const dataAciklama = responseAciklama;
		document.getElementById("aciklama1").value = dataAciklama.aciklama1;
		document.getElementById("aciklama2").value = dataAciklama.aciklama2;
		if (data.errorMessage) {
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage;
			return;
		}
		errorDiv.style.display = "none";
		errorDiv.innerText = "";
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function prepareBordroKayit() {
	const girisbordroDTO = {
		bordroNo: document.getElementById("bordrono").value,
		aciklama1: document.getElementById("aciklama1").value || "",
		aciklama2: document.getElementById("aciklama2").value || "",
	};
	tableData = getTableData();
	return { girisbordroDTO, tableData, };
}

function getTableData() {
	const table = document.getElementById('gbTable'); // Tablo ID'sini değiştirin
	const rows = table.querySelectorAll('tbody tr');
	const data = [];
	rows.forEach((row) => {
		const cells = row.querySelectorAll('td');
		const firstColumnValue = cells[1]?.querySelector('input')?.value || "";
		const ninthColumnValue = parseFloat(cells[9]?.querySelector('input')?.value || "0");
		if (!firstColumnValue || ninthColumnValue <= 0) {
			return;
		}
		const rowData = {
			cekNo: firstColumnValue,
			girisBordro: document.getElementById('bordrono').value,
			girisTarihi: document.getElementById('bordroTarih').value,
			girisOzelKod: document.getElementById('ozelkod').value,
			girisMusteri: document.getElementById('bcheskod').value,
			vade: cells[2]?.querySelector('input')?.value || "",
			banka: cells[3]?.querySelector('input')?.value || "",
			sube: cells[4]?.querySelector('input')?.value || "",
			seriNo: cells[5]?.querySelector('input')?.value || "",
			ilkBorclu: cells[6]?.querySelector('input')?.value || "",
			cekHesapNo: cells[7]?.querySelector('input')?.value || "",
			cins: cells[8]?.querySelector('input')?.value || "",
			tutar: parseLocaleNumber(cells[9]?.querySelector('input')?.value || "0"),
			cikisBordro: cells[10]?.textContent || "",
			cikisTarihi: cells[11]?.textContent || "",
			cikisMusteri: cells[12]?.textContent || "",
			durum: cells[13]?.textContent || "",
			ttarih: cells[14]?.textContent || "",
			cikisOzelKod: cells[15]?.textContent || "",
		};
		data.push(rowData);
	});
	return data;
}

async function gbKayit() {
	const bordroNo = document.getElementById("bordrono").value.trim();
	if (!bordroNo || bordroNo === "0") {
		alert("Geçerli bir evrak numarası giriniz.");
		return;
	}
	const bordroKayitDTO = prepareBordroKayit();
	const errorDiv = document.getElementById('errorDiv');
	document.body.style.cursor = 'wait';
	try {
		const response = await fetchWithSessionCheck('kambiyo/gbordroKayit', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify(bordroKayitDTO),
		});
		if (response.errorMessage.trim() !== "") {
			throw new Error(response.errorMessage);
		}
		clearInputs();
		document.getElementById("bordrono").value = "";
		document.getElementById("errorDiv").innerText = "";
		errorDiv.style.display = 'none';
	} catch (error) {
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
		errorDiv.style.display = 'block';
	} finally {
		document.body.style.cursor = 'default';
	}
}
//************************evrak sil ***********************************************
async function gbYoket() {
	const bordroNo = document.getElementById("bordrono").value.trim(); // Boşlukları temizle
	if (!bordroNo || bordroNo === "0") {
		alert("Geçerli bir evrak numarası giriniz.");
		return;
	}

	const confirmDelete = confirm("Bu Bordroyu silmek istediğinize emin misiniz?");
	if (!confirmDelete) {
		return;
	}
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("kambiyo/bordroYoket", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ bordroNo: bordroNo }),
		});
		if (response.errorMessage.trim() !== "") {
			throw new Error(response.errorMessage);
		}
		clearInputs();
		document.getElementById("bordrono").value = "";
		document.getElementById("errorDiv").style.display = "none";
		document.getElementById("errorDiv").innerText = "";
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function ortgun() {
	const table = document.getElementById('gbTable');
	const rows = table.querySelectorAll('tbody tr');
	const gunlukTarih = document.getElementById("bordroTarih").value;
	const faizoran = parseLocaleNumber(document.getElementById("faiz").value);
	let toppara = 0;
	let tfaiz = 0;
	let orgun = 0;
	rows.forEach((row) => {
		const cells = row.querySelectorAll('td');
		const tutar = parseLocaleNumber(cells[9]?.querySelector('input')?.value || "0");
		const vade = cells[2]?.querySelector('input')?.value;
		if (tutar > 0 && vade && gunlukTarih) {
			const gunfarki = tarihGunFarki(gunlukTarih, vade);
			const faiz = (((tutar * faizoran) / 365) * gunfarki) / 100;
			toppara += tutar;
			tfaiz += faiz;
			orgun = ((toppara * faizoran) / 365) / 100;
		}
	});
	document.getElementById("lblortgun").innerText = (tfaiz / orgun).toLocaleString(undefined, {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	});
	document.getElementById("lblfaiztutari").innerText = tfaiz.toLocaleString(undefined, {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	});
}

function tarihGunFarki(tarih1, tarih2) {
	const date1 = new Date(tarih1);
	const date2 = new Date(tarih2);
	const farkMs = date2 - date1; // Milisaniye cinsinden fark
	return Math.ceil(farkMs / (1000 * 60 * 60 * 24)); // Milisaniyeyi güne çevir
}

async function yeniBordro() {
	const errorDiv = document.getElementById('errorDiv');
	try {
		const response = await fetchWithSessionCheck('kambiyo/yeniBordro', {
			method: 'GET',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
		});
		const bordronoInput = document.getElementById('bordrono');
		bordronoInput.value = response.bordroNo;
		if (response.errorMessage.trim() !== "") {
			throw new Error(response.errorMessage);
		} else {
			errorDiv.innerText = "";
			errorDiv.style.display = 'none';
			clearInputs();
		}
	} catch (error) {
		errorDiv.style.display = 'block';
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	}
}

async function gbDownload() {
	const bordroNo = document.getElementById("bordrono").value.trim(); // Boşlukları temizle
	if (!bordroNo || bordroNo === "0") {
		alert("Geçerli bir evrak numarası giriniz.");
		return;
	}
	const table = document.getElementById('gbTable'); // Tablo ID'sini değiştirin
	const rows = table.querySelectorAll('tbody tr');
	toppara = 0;
	rows.forEach((row) => {
		const cells = row.querySelectorAll('td');
		const tutar = parseLocaleNumber(cells[9]?.querySelector('input')?.value || "0");
		toppara += tutar;
	});
	const bordroPrinter = {
		girisBordro: document.getElementById("bordrono").value,
		girisTarihi: document.getElementById('bordroTarih').value,
		girisMusteri: document.getElementById('bcheskod').value,
		unvan: document.getElementById("lblcheskod").innerText || "",
		dvzcins: document.getElementById("dvzcinsi").value || "",
		tutar: toppara || 0,
	};
	const errorDiv = document.getElementById("errorDiv"); // Hata mesajını göstermek için
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	const $indirButton = $('#indirButton');
	$indirButton.prop('disabled', true).text('İşleniyor...');
	try {
		const response = await fetchWithSessionCheckForDownload('kambiyo/bordro_download', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(bordroPrinter),
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

async function cekkontrol(element) {
	const currentCell = element.closest('td');
	const cellValue = currentCell.querySelector('input').value; // Hücrenin içindeki değeri al ve baştaki/sondaki boşlukları temizle
	document.getElementById("errorDiv").style.display = "none";
	document.getElementById("errorDiv").innerText = "";
	try {
		const response = await fetchWithSessionCheck("kambiyo/cekkontrol", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ cekNo: cellValue }),
		});
		if (response.errorMessage.trim() !== "") {
			throw new Error(response.errorMessage);
		}
		const gelenbordro = response.bordroNo;
		const bordrono = document.getElementById("bordrono").value;
		if (gelenbordro.trim() === "") {
			alert("Bu Numarada Cek Kaydi Bulunamadi");
			return;
		}
		if (gelenbordro != bordrono) {
			alert("Bu cek daha onceden giris yapilmis Bordro No:" + gelenbordro);
		}
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function cekcariIsle() {
	const hesapKodu = $('#cekgirBilgi').val();
	const bordroNo = document.getElementById("bordrono").value.trim();
	if (!bordroNo || bordroNo === "0") {
		alert("Geçerli bir evrak numarası giriniz.");
		return;
	}
	const bordroKayitDTO = prepareBordroKayit();
	bordroKayitDTO.girisbordroDTO.hesapKodu = hesapKodu;
	const errorDiv = document.getElementById('errorDiv');
	document.body.style.cursor = 'wait';
	try {
		const response = await fetchWithSessionCheck('kambiyo/gbordroCariKayit', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify(bordroKayitDTO),
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
		document.body.style.cursor = 'default';
	}
}

function gbmailAt() {
	const bordroNo = document.getElementById("bordrono").value.trim();
	if (!bordroNo || bordroNo === "0") {
		alert("Geçerli bir evrak numarası giriniz.");
		return;
	}
	const table = document.getElementById('gbTable');
	const rows = table.querySelectorAll('tbody tr');
	toppara = 0;
	rows.forEach((row) => {
		const cells = row.querySelectorAll('td');
		const tutar = parseLocaleNumber(cells[9]?.textContent || "0");
		toppara += tutar;
	});
	const girisBordro = document.getElementById("bordrono").value;
	const girisTarihi = document.getElementById('bordroTarih').value;
	const girisMusteri = document.getElementById('bcheskod').value;
	const unvan = document.getElementById("lblcheskod").innerText || "";
	const dvzcins = document.getElementById("dvzcinsi").value || "";
	const tutar = toppara || 0;
	const degerler = girisBordro + "," + girisTarihi + "," + girisMusteri + "," + unvan + "," + dvzcins + "," + tutar + ",cgbordro";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}