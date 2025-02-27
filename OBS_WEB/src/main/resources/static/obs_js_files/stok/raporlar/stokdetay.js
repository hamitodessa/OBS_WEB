async function anagrpChanged(anagrpElement, altgrpElement) {
	const anagrup = anagrpElement.value;
	const errorDiv = document.getElementById("errorDiv");
	const selectElement = document.getElementById(altgrpElement);
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


async function openenvModal(modal) {
	$(modal).modal('show');
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("stok/anadepo", {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			}
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const ana = response.anaKodlari;
		const dpo = response.depoKodlari;
		const uranaSelect = document.getElementById("uranagrp");
		const anaSelect = document.getElementById("anagrp");
		const dpoSelect = document.getElementById("depo");
		anaSelect.innerHTML = "";
		uranaSelect.innerHTML = "";
		dpoSelect.innerHTML = "";
		ana.forEach(item => {
			const optionAna = document.createElement("option");
			optionAna.value = item.ANA_GRUP;
			optionAna.textContent = item.ANA_GRUP;
			anaSelect.appendChild(optionAna);
			const optionUrana = document.createElement("option");
			optionUrana.value = item.ANA_GRUP;
			optionUrana.textContent = item.ANA_GRUP;
			uranaSelect.appendChild(optionUrana);
		});
		dpo.forEach(item => {
			const option = document.createElement("option");
			option.value = item.DEPO;
			option.textContent = item.DEPO;
			dpoSelect.appendChild(option);
		});
		const newOption = document.createElement("option");
		newOption.value = "Bos Olanlar";
		newOption.textContent = "Bos Olanlar";

		const newOption1 = document.createElement("option");
		newOption1.value = "Bos Olanlar";
		newOption1.textContent = "Bos Olanlar";

		const newOption2 = document.createElement("option");
		newOption2.value = "Bos Olanlar";
		newOption2.textContent = "Bos Olanlar";

		anaSelect.insertBefore(newOption, anaSelect.options[1]);
		uranaSelect.insertBefore(newOption1, uranaSelect.options[1]);
		dpoSelect.insertBefore(newOption2, dpoSelect.options[1]);
	} catch (error) {
		const modalError = document.getElementById("errorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}

async function stokdetayfetchTableData() {
	const hiddenFieldValue = $('#stokdetayBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	const stokdetayDTO = {
		tar1: parsedValues[0],
		tar2: parsedValues[1],
		uranagrp: parsedValues[2],
		ukod1: parsedValues[3],
		ukod2: parsedValues[4],
		uraltgrp: parsedValues[5],
		evrno1: parsedValues[6],
		evrno2: parsedValues[7],
		ckod1: parsedValues[8],
		ckod2: parsedValues[9],
		anagrp: parsedValues[10],
		altgrp: parsedValues[11],
		depo: parsedValues[12],
		depohardahil: parsedValues[13],
		uretfisdahil: parsedValues[14],
		turu: parsedValues[15]
	};
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	const $yenileButton = $('#stokdetayyenileButton');
	$yenileButton.prop('disabled', true).text('İşleniyor...');
	const mainTableBody = document.getElementById("mainTableBody");
	mainTableBody.innerHTML = "";
	try {
		const response = await fetchWithSessionCheck("stok/stokdetaydoldur", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(stokdetayDTO),
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		data = response;
		data.data.forEach(rowData => {
			const row = document.createElement('tr');
			row.classList.add('expandable');
			row.classList.add("table-row-height");
			row.innerHTML = `
                    <td>${rowData.Urun_Kodu || ''}</td>
                    <td>${rowData.Barkod || ''}</td>
                    <td>${rowData.Adi || ''}</td>
                    <td>${rowData.Izahat || ''}</td>
                    <td>${rowData.Evrak_No || ''}</td>
                    <td>${rowData.Hesap_Kodu || ''}</td>
                    <td>${rowData.Evrak_Cins || ''}</td>
                    <td>${formatDate(rowData.Tarih) || ''}</td>
                    <td class="double-column">${formatNumber3(rowData.Miktar)}</td>
                    <td>${rowData.Birim || ''}</td>
					          <td class="double-column">${formatNumber2(rowData.Fiat)}</td>
                    <td>${rowData.Doviz || ''}</td>
					          <td class="double-column">${formatNumber3(rowData.Miktar_Bakiye)}</td>
					          <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
					          <td class="double-column">${formatNumber2(rowData.Tutar_Bakiye)}</td>
                    <td>${rowData.Ana_Grup || ''}</td>
                    <td>${rowData.Alt_Grup || ''}</td>
                    <td>${rowData.Depo || ''}</td>
                    <td>${rowData.USER || ''}</td>
                `;
			mainTableBody.appendChild(row);
		});
		document.body.style.cursor = "default";
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error;
	} finally {
		$yenileButton.prop('disabled', false).text('Yenile');
		document.body.style.cursor = "default";
	}
}

async function stokdetaydownloadReport() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	document.body.style.cursor = "wait";
	const $indirButton = $('#stokdetayreportDownload');
	$indirButton.prop('disabled', true).text('İşleniyor...');
	const $yenileButton = $('#stokyenileButton');
	$yenileButton.prop('disabled', true);
	let rows = extractTableData("main-table");
	try {
		const response = await fetchWithSessionCheckForDownload('stok/stokdetay_download', {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(rows)
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
		$indirButton.prop('disabled', false).text('Rapor İndir');
		$yenileButton.prop('disabled', false);
		document.body.style.cursor = "default";
	}
}

async function stokdetaymailAt() {
	localStorage.removeItem("tableData");
	localStorage.removeItem("grprapor");
	localStorage.removeItem("tablobaslik");
	document.body.style.cursor = "wait";
	let rows = extractTableData("main-table");
	localStorage.setItem("tableData", JSON.stringify({ rows: rows }));
	const degerler = "stokdetay";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}

function extractTableData(tableId) {
	let table = document.querySelector(`#${tableId}`);
	let headers = [];
	let rows = [];
	table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));
	table.querySelectorAll("tbody tr").forEach(tr => {
		let rowData = {};
		let nonEmptyCount = 0;
		tr.querySelectorAll("td").forEach((td, index) => {
			let value = td.innerText.trim();
			if (value !== "") {
				nonEmptyCount++;
			}
			rowData[headers[index]] = value;
		});
		if (nonEmptyCount > 0) {
			rows.push(rowData);
		}
	});
	return rows;
}