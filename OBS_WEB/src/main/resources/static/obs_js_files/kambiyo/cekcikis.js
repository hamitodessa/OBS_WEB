async function fetchcekno() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.innerText = "";
	errorDiv.style.display = "none";
	rowCounter = 0;
	bankaIsimleri = "";
	try {
		const responseBanka = await fetchWithSessionCheck("kambiyo/kamgetCekListe", {
			method: "GET",
			headers: {
				"Content-Type": "application/json",
			},
		});
		if (responseBanka.errorMessage) {
			throw new Error(responseBanka.errorMessage);
		}
		bankaIsimleri = responseBanka.cekListe || [];
		initializeRows();
	} catch (error) {
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
		errorDiv.style.display = "block";
	}
}

function initializeRows() {
	for (let i = 0; i < 5; i++) {
		cekcikaddRow();
	}
}
function cekcikaddRow() {
	const table = document.getElementById("gbTable").getElementsByTagName("tbody")[0];
	const newRow = table.insertRow();
	incrementRowCounter();
	let optionsHTML = bankaIsimleri.map(kod => `<option value="${kod.Cek_No}">${kod.Cek_No}</option>`).join("");
	newRow.innerHTML = `
		<td >
		<button id="bsatir_${rowCounter}" type="button" class="btn btn-secondary ml-2" onclick="cekcikremoveRow(this)"><i class="fa fa-trash"></i></button>
		</td>
		<td>
	        <div style="position: relative; width: 100%;">
	            <input class="form-control cins_bold" list="cekOptions_${rowCounter}"
	               maxlength="10" id="cekno_${rowCounter}" 
	               onchange="cekkontrol(this)">
				<datalist id="cekOptions_${rowCounter}">${optionsHTML}</datalist>
		        <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>

	        </div>
	    </td>
		<td><label class="form-control"style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label></td>
        <td><label class="form-control"style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label></td>
        <td><label class="form-control"style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label></td>
		<td><label class="form-control"style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label></td>
		<td><label class="form-control"style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label></td>
		<td><label class="form-control"style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label></td>
		<td><label class="form-control"style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label></td>
        <td><label class="form-control"style="display: block;width:100%;height:100%;text-align:right;"><span>0.00</span></label></td>
    `;
}

function cekcikremoveRow(button) {
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
    const cells = document.querySelectorAll('tr td:nth-child(10) span');
    const totalTutarCell = document.getElementById("totalTutar");
    const totalceksayisi = document.getElementById("ceksayisi");
    let total = 0;
    let totaladet = 0;

    cells.forEach(span => {
        const value = parseFloat(span.textContent.replace(/,/g, '').trim());
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
			// Yeni bir satır ekle ve onun ikinci kolonundaki input öğesine odaklan
			cekcikaddRow();
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

async function cekkontrol(element) {
	const currentCell = element.closest('td');
	const cellValue = currentCell.querySelector('input').value;
	try {
		const response = await fetchWithSessionCheck("kambiyo/ccekkontrol", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ cekNo: cellValue }),
		});
		const dto = response;
		if (dto.errorMessage) {
			errorDiv.style.display = "block";
			errorDiv.innerText = dto.errorMessage;
			return;
		}
		const gelenbordro = dto.cikisBordro;
		const bordrono = document.getElementById("bordrono").value;
		if (gelenbordro != "" && gelenbordro != bordrono) {
			alert("Bu cek daha onceden cikis yapilmis Bordro No:" + gelenbordro);
			return;
		}
		const currentRow = element.closest('tr');
		const cells = currentRow.getElementsByTagName('td');

		setLabelContent(cells[2], formatDate(dto.vade) || '');
		setLabelContent(cells[3], dto.banka || "");
		setLabelContent(cells[4], dto.sube || "");
		setLabelContent(cells[5], dto.seriNo || "");
		setLabelContent(cells[6], dto.ilkBorclu || "");
		setLabelContent(cells[7], dto.cekHesapNo || "");
		setLabelContent(cells[8], dto.cins || "");
		setLabelContent(cells[9], formatNumber2(dto.tutar));

		updateColumnTotal();
		document.getElementById("errorDiv").style.display = "none";
		document.getElementById("errorDiv").innerText = "";
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function clearInputs() {
	document.getElementById("ozelkod").value = "";
	document.getElementById("dvzcinsi").value = "";
	document.getElementById("bcheskod").value = "";
	document.getElementById("lblcheskod").innerText = "";
	document.getElementById("faiz").value = "";
	document.getElementById("lblortgun").innerText = "0";
	document.getElementById("lblfaiztutari").innerText = "0.00";
	document.getElementById("aciklama1").value = "";
	document.getElementById("aciklama2").value = "";
	const tableBody = document.getElementById("tbody");
	tableBody.innerHTML = "";
	rowCounter = 0;
	initializeRows();
	const totalTutarCell = document.getElementById("totalTutar");
	totalTutarCell.textContent = formatNumber2(0);
}

async function soncikisBordro() {
	try {
		const response = await fetchWithSessionCheck('kambiyo/csonbordroNo', {
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
	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("kambiyo/cbordroOku", {
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
		    for (let i = 0; i < additionalRows +1 ; i++) {
		        cekcikaddRow();
		    }
		}
		const rowss = table.querySelectorAll('tbody tr');
		data.data.forEach((item,index) => {
		        const cells = rowss[index].cells;
				
		        const ceknoInput = cells[1]?.querySelector('input');
		        if (ceknoInput) ceknoInput.value = item.Cek_No || "";
				
		        setLabelContent(cells[2], formatDate(item.Vade) || '');
				setLabelContent(cells[3], item.Banka || "");
		        setLabelContent(cells[4], item.Sube || "");
		        setLabelContent(cells[5], item.Seri_No || "");
		        setLabelContent(cells[6], item.Ilk_Borclu || "");
				setLabelContent(cells[7], item.Cek_Hesap_No || "");
				setLabelContent(cells[8], item.Cins || "");
				setLabelContent(cells[9], formatNumber2(item.Tutar));
		});
		getTableData();
		updateColumnTotal();
		document.getElementById("ozelkod").value = data.data[0].Cikis_Ozel_Kod;
		document.getElementById("dvzcinsi").value = data.data[0].Cins;
		document.getElementById("bcheskod").value = data.data[0].Cikis_Musteri;
		document.getElementById("bordroTarih").value = data.data[0].Cikis_Tarihi;
		const ches = document.getElementById("bcheskod");
		ches.oninput();
		const responseAciklama = await fetchWithSessionCheck("kambiyo/ckamgetAciklama", {
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
		if (!firstColumnValue.trim()) {
			return;
		}
		const rowData = {
			cekNo: firstColumnValue,
			vade: cells[2]?.textContent,
			tutar: parseLocaleNumber(cells[9]?.textContent),
			cikisBordro: document.getElementById('bordrono').value,
			cikisTarihi: document.getElementById('bordroTarih').value,
			cikisMusteri: document.getElementById('bcheskod').value,
			cikisOzelKod: document.getElementById('ozelkod').value,
		};
		data.push(rowData);
	});
	return data;
}

async function cbKayit() {
	const bordroNo = document.getElementById("bordrono").value.trim();
	if (!bordroNo || bordroNo === "0") {
		alert("Geçerli bir evrak numarası giriniz.");
		return;
	}
	const bordroKayitDTO = prepareBordroKayit();
	const errorDiv = document.getElementById('errorDiv');
	document.body.style.cursor = 'wait';
	try {
		const response = await fetchWithSessionCheck('kambiyo/cbordroKayit', {
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
async function cbYoket() {
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
		const response = await fetchWithSessionCheck("kambiyo/cbordroYoket", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ bordroNo: bordroNo }),
		});
		if (response.errorMessage.trim() !== "") {
			throw new Error(response.errorMessage);
		}
		document.querySelector('.changeLink[data-url="/kambiyo/cekcikis"]').click();
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
		const tutar = parseLocaleNumber(cells[9]?.textContent || "0");
		const vade = cells[2]?.textContent;
		if (vade && gunlukTarih) {
			const gunfarki = tarihGunFarki(gunlukTarih, vade);
			const faiz = (((tutar * faizoran) / 365) * gunfarki) / 100;
			toppara += tutar;
			tfaiz += faiz;
			orgun = ((toppara * faizoran) / 365) / 100;
		}
	});
	document.getElementById("lblortgun").innerText = (tfaiz / orgun).toLocaleString(undefined, {
		minimumFractionDigits: 2, maximumFractionDigits: 2
	});
	document.getElementById("lblfaiztutari").innerText = tfaiz.toLocaleString(undefined, {
		minimumFractionDigits: 2, maximumFractionDigits: 2
	});
}

function tarihGunFarki(tarih1, tarih2) {
	const date1 = new Date(tarih1);
	const [gun, ay, yil] = tarih2.split(".");
	const date2 = new Date(`${yil}-${ay}-${gun}`);
	const farkMs = date2 - date1;
	return Math.ceil(farkMs / (1000 * 60 * 60 * 24));
}

async function yeniBordro() {
	const errorDiv = document.getElementById('errorDiv');
	try {
		const response = await fetchWithSessionCheck('kambiyo/cyeniBordro', {
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

async function cbDownload() {
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
		const tutar = parseLocaleNumber(cells[9]?.textContent || "0");
		toppara += tutar; // Toplam parayı artır
	});
	const bordroPrinter = {
		cikisBordro: document.getElementById("bordrono").value,
		cikisTarihi: document.getElementById('bordroTarih').value,
		cikisMusteri: document.getElementById('bcheskod').value,
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
		const response = await fetchWithSessionCheckForDownload('kambiyo/cbordro_download', {
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

async function cekcariIsle() {
	const hesapKodu = $('#cekcikBilgi').val();
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
		const response = await fetchWithSessionCheck('kambiyo/cbordroCariKayit', {
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

function cbmailAt() {

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
		const tutar = parseLocaleNumber(cells[9]?.textContent || "0");
		toppara += tutar;
	});
	const cikisBordro = document.getElementById("bordrono").value;
	const cikisTarihi = document.getElementById('bordroTarih').value;
	const cikisMusteri = document.getElementById('bcheskod').value;
	const unvan = document.getElementById("lblcheskod").innerText || "";
	const dvzcins = document.getElementById("dvzcinsi").value || "";
	const tutar = toppara || 0;
	const degerler = cikisBordro + "," + cikisTarihi + "," + cikisMusteri + "," + unvan + "," + dvzcins + "," + tutar + ",ccbordro";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}

function setLabelContent(cell, content) {
    const span = cell.querySelector('label span');
    if (span) {
        span.textContent = content ? content : '\u00A0'; // Eğer içerik boşsa, boşluk karakteri eklenir
    }
}