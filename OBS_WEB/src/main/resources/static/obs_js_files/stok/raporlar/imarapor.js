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

async function openimarapModal(modal) {
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

async function imafetchTableData() {
    const hiddenFieldValue = $('#imarapBilgi').val();
    const parsedValues = hiddenFieldValue.split(",");
    const imaraporDTO = {
        evrno1: parsedValues[0],
        evrno2: parsedValues[1],
        uranagrp: parsedValues[2],
        tar1: parsedValues[3],
        tar2: parsedValues[4],
        uraltgrp: parsedValues[5],
        bkod1: parsedValues[6],
        bkod2: parsedValues[7],
        depo: parsedValues[8],
        ukod1: parsedValues[9],
        ukod2: parsedValues[10],
        rec1: parsedValues[11],
        rec2: parsedValues[12],
        anagrp: parsedValues[13],
        altgrp: parsedValues[14],
    };
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    const tableBody = document.getElementById("tableBody");
    tableBody.innerHTML = "";
	
	document.getElementById("totalMiktar").textContent = '0.000';
	document.getElementById("totalAgirlik").textContent = '0.000';
			
    document.body.style.cursor = "wait";
    const $yenileButton = $('#imarapyenileButton');
    $yenileButton.prop('disabled', true).text('İşleniyor...');
    try {
        const response = await fetchWithSessionCheck("stok/imarapdoldur", {
            method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(imaraporDTO),
        });
        if (response.errorMessage) {
            throw new Error(response.errorMessage);
        }
        let totalmiktar = 0;
        let totalagirlik = 0;
        response.data.forEach(item => {
            const row = document.createElement("tr");
            row.classList.add("table-row-height");
            row.innerHTML = `
									<td>${item.Evrak_No || ''}</td>
				                    <td>${formatDate(item.Tarih)}</td>
				                    <td>${item.Urun_Kodu || ''}</td>
				                    <td>${item.Adi || ''}</td>
                                    <td class="double-column">${formatNumber3(item.Miktar)}</td>
                                    <td>${item.Birim || ''}</td>
                                    <td class="double-column">${formatNumber3(item.Agirlik)}</td>
									<td>${item.Depo || ''}</td>
                                    <td>${item.Ana_Grup || ''}</td>
									<td>${item.Alt_Grup || ''}</td>
									<td>${item.Barkod || ''}</td>
									<td>${item.Recete || ''}</td>
				                    <td>${item.USER || ''}</td>
				                `;
            totalmiktar += item.Miktar;
            totalagirlik += item.Agirlik
            tableBody.appendChild(row);
        });
        const totalMiktarCell = document.getElementById("totalMiktar");
        const totalAgirlikCell = document.getElementById("totalAgirlik");
        totalMiktarCell.textContent = totalmiktar.toLocaleString(undefined, {
            minimumFractionDigits: 3, maximumFractionDigits: 3
        });
        totalAgirlikCell.textContent = totalagirlik.toLocaleString(undefined, {
            minimumFractionDigits: 3, maximumFractionDigits: 3
        });
    } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = error.message;
    } finally {
        $yenileButton.prop('disabled', false).text('Yenile');
        document.body.style.cursor = "default";
    }
}

async function imarapdownloadReport() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";

	document.body.style.cursor = "wait";
	const $indirButton = $('#imarapreportFormatbutton');
	$indirButton.prop('disabled', true).text('İşleniyor...');
	const $yenileButton = $('#imarapyenileButton');
	$yenileButton.prop('disabled', true);

	let table = document.querySelector("#main-table");
	let headers = [];
	let rows = [];
	table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));
	table.querySelectorAll("tbody tr").forEach(tr => {
		let rowData = {};
		let isEmpty = true;
		tr.querySelectorAll("td").forEach((td, index) => {
			let value = td.innerText.trim();
			if (value !== "") {
				isEmpty = false;
			}
			rowData[headers[index]] = value;
		});
		if (!isEmpty) {
			rows.push(rowData);
		}
	});
	try {
		const response = await fetchWithSessionCheckForDownload('stok/imarap_download', {
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

async function imarapmailAt() {
	document.body.style.cursor = "wait";
	let table = document.querySelector("#main-table");
		let headers = [];
		let rows = [];
		table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));
		table.querySelectorAll("tbody tr").forEach(tr => {
			let rowData = {};
			let isEmpty = true;
			tr.querySelectorAll("td").forEach((td, index) => {
				let value = td.innerText.trim();
				if (value !== "") {
					isEmpty = false;
				}
				rowData[headers[index]] = value;
			});
			if (!isEmpty) {
				rows.push(rowData);
			}
		});
	localStorage.setItem("tableData", JSON.stringify({ rows: rows }));
	const degerler = "imarapor";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}