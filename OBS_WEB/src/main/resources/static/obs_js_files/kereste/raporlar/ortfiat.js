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
        const response = await fetchWithSessionCheck("kereste/anadepo", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            }
        });
        if (response.errorMessage) {
            throw new Error(response.errorMessage);
        }
        const ana = response.anaKodlari;
        const oz1 = response.oz1Kodlari;

        const uranaSelect = document.getElementById("anagrp");
        uranaSelect.innerHTML = "";
        const ozSelect = document.getElementById("ozkod");
        ozSelect.innerHTML = "";

        ana.forEach(item => {
            const optionUrana = document.createElement("option");
            optionUrana.value = item.ANA_GRUP;
            optionUrana.textContent = item.ANA_GRUP;
            uranaSelect.appendChild(optionUrana);
        });
        oz1.forEach(item => {
            const optionOz1 = document.createElement("option");
            optionOz1.value = item.OZEL_KOD_1;
            optionOz1.textContent = item.OZEL_KOD_1;
            ozSelect.appendChild(optionOz1);
        });
        const newOption = document.createElement("option");
        newOption.value = "Bos Olanlar";
        newOption.textContent = "Bos Olanlar";

        const newOption1 = document.createElement("option");
        newOption1.value = "Bos Olanlar";
        newOption1.textContent = "Bos Olanlar";

        uranaSelect.insertBefore(newOption, uranaSelect.options[1]);
        ozSelect.insertBefore(newOption1, ozSelect.options[1]);
    } catch (error) {
        const modalError = document.getElementById("errorDiv");
        modalError.style.display = "block";
        modalError.innerText = `Bir hata oluştu: ${error.message}`;
    } finally {
        document.body.style.cursor = "default";
    }
}

function dvzcevirChanged() {
    const checked = document.getElementById("dvzcevirchc").checked;
   
        document.getElementById("dvzcinsWrap").classList.toggle("is-hidden", !checked);
        document.getElementById("dvzturuWrap").classList.toggle("is-hidden", !checked);
    
}
function getkergrupraporDTO() {
    const hiddenFieldValue = $('#ortfiatBilgi').val();
    const parsedValues = hiddenFieldValue.split(",");
    return {
        tar1: parsedValues[0],
        tar2: parsedValues[1],
        anagrp: parsedValues[2],
        ukod1: parsedValues[3],
        ukod2: parsedValues[4],
        altgrp: parsedValues[5],
        ckod1: parsedValues[6],
        ckod2: parsedValues[7],
        ozkod: parsedValues[8],
        kons1: parsedValues[9],
        kons2: parsedValues[10],
        gruplama: parsedValues[11],
        turu: parsedValues[12],
        dvzcevirchc: parsedValues[13],
        doviz: parsedValues[14],
        dvzturu: parsedValues[15]
    };
}
async function kerortfiatdoldur() {
    const kergrupraporDTO = getkergrupraporDTO();
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    document.body.style.cursor = "wait";
    const $yenileButton = $('#ortfiatyenileButton');
    $yenileButton.prop('disabled', true).text('İşleniyor...');
    const mainTableBody = document.getElementById("mainTableBody");
    mainTableBody.innerHTML = "";
    try {
        const response = await fetchWithSessionCheck("kereste/ortfiatdoldur", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(kergrupraporDTO),
        });
        if (response.errorMessage) {
            throw new Error(response.errorMessage);
        }
        data = response;
        console.log(data);
        let sqlHeaders = "";
        let extraColumns = "";
        if (response.raporturu === 'Sinif') {
            sqlHeaders = ["SINIF", "ACIKLAMA", "TUTAR", data.dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + data.dvz];
            updateTableHeaderssinif(sqlHeaders);
        }
        else if (response.raporturu === 'Sinif-Kal') {
            sqlHeaders = ["SINIF", "KAL", "TUTAR", data.dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + data.dvz];
            updateTableHeaderssinif(sqlHeaders);
        }
        else if (response.raporturu === 'Sinif-Boy') {
            sqlHeaders = ["SINIF", "BOY", "TUTAR", data.dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + data.dvz];
            updateTableHeaderssinif(sqlHeaders);
        }
        else if (response.raporturu === 'Sinif-Gen') {
            sqlHeaders = ["SINIF", "GEN", "TUTAR", data.dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + data.dvz];
            updateTableHeaderssinif(sqlHeaders);
        }
        else if (response.raporturu === 'Kodu') {
            sqlHeaders = ["KODU", "ACIKLAMA", "TUTAR", data.dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + data.dvz];
            updateTableHeaderssinif(sqlHeaders);
        }
        else if (response.raporturu === 'Konsimento') {
            sqlHeaders = ["KONSIMENTO", "ACIKLAMA", "TUTAR", data.dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + data.dvz];
            updateTableHeaderssinif(sqlHeaders);
        }
        else if (response.raporturu === 'Hesap Kodu') {
            sqlHeaders = ["CARI FIRMA", "UNVAN", "TUTAR", data.dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + data.dvz];
            updateTableHeaderssinif(sqlHeaders);
        }
        else if (response.raporturu === 'Hesap Kodu-Ana_Alt_Grup') {
            sqlHeaders = ["CARI FIRMA", "ANA GRUP", "ALT GRUP", "TUTAR", data.dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + data.dvz];
            updateTableHeaders3(sqlHeaders);
        }
        else if (response.raporturu === 'Yil') {
            sqlHeaders = ["YIL", "TUTAR", data.dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + data.dvz];
            updateTableHeaders1(sqlHeaders);
        }
        else if (response.raporturu === 'Yil_Ay') {
            sqlHeaders = ["YIL", "AY", "TUTAR", data.dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + data.dvz];
            updateTableHeaderssinif(sqlHeaders);
        }
        else if (response.raporturu === 'Ana Grup') {
            sqlHeaders = ["ANA GRUP", "ALT GRUP", "TUTAR", data.dvz + "_TUTAR", "MIKTAR", "M3", "M3_ORT_FIAT", "M3_ORT_FIAT_" + data.dvz];
            updateTableHeaderssinif(sqlHeaders);
        }
        data.data.forEach(rowData => {
            const row = document.createElement('tr');
            row.classList.add('expandable');
            row.classList.add("table-row-height");
            if (response.raporturu === 'Sinif') {
                extraColumns = `
                    <td>${rowData.Sinif || ''}</td>
                    <td>${rowData.Adi || ''}</td>
            `;
            }
            else if (response.raporturu === 'Sinif-Kal') {
                extraColumns = `
                    <td>${rowData.Sinif || ''}</td>
                    <td>${rowData.Kal || ''}</td>
            `;
            }
            else if (response.raporturu === 'Sinif-Boy') {
                extraColumns = `
                    <td>${rowData.Sinif || ''}</td>
                    <td>${rowData.Boy || ''}</td>
                `;
            }
            else if (response.raporturu === 'Sinif-Gen') {
                extraColumns = `
                    <td>${rowData.Sinif || ''}</td>
                    <td>${rowData.Gen || ''}</td>
                `;
            }
            else if (response.raporturu === 'Kodu') {
                extraColumns = `
                    <td>${rowData.Kodu || ''}</td>
                    <td>${rowData.Adi || ''}</td>
                `;
            }
            else if (response.raporturu === 'Konsimento') {
                extraColumns = `
                    <td>${rowData.Konsimento || ''}</td>
                    <td>${rowData.Aciklama || ''}</td>
                `;
            }
            else if (response.raporturu === 'Hesap Kodu') {
                extraColumns = `
                    <td>${rowData.Cari_Firma || ''}</td>
                    <td>${rowData.Cari_Adi || ''}</td>
                `;
            }
            else if (response.raporturu === 'Hesap Kodu-Ana_Alt_Grup') {
                extraColumns = `
                    <td>${rowData.Cari_Firma || ''}</td>
                    <td>${rowData.Ana_Grup || ''}</td>
                    <td>${rowData.Alta_Grup || ''}</td>
                `;
            }
            else if (response.raporturu === 'Yil') {
                extraColumns = `
                    <td>${rowData.Yil || ''}</td>
                `;
            }
            else if (response.raporturu === 'Yil_Ay') {
                extraColumns = `
                    <td>${rowData.Yil || ''}</td>
                    <td>${rowData.Ay || ''}</td>
                `;
            }
            else if (response.raporturu === 'Ana Grup') {
                extraColumns = `
                    <td>${rowData.Ana_Grup || ''}</td>
                    <td>${rowData.Alt_Grup || ''}</td>
                `;
            }
        row.innerHTML = `
            ${extraColumns}
            <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
            <td class="double-column">${formatNumber2(rowData[data.dvz + "_Tutar"])}</td>
            <td class="double-column">${formatNumber0(rowData.Miktar)}</td>
            <td class="double-column">${formatNumber3(rowData.m3)}</td>
            <td class="double-column">${formatNumber2(rowData.m3_Ort_Fiat)}</td>
            <td class="double-column">${formatNumber2(rowData["m3_Ort_Fiat_" + data.dvz])}</td>
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

function updateTableHeaders1(headers) {
    let thead = document.querySelector("#main-table thead");
    let table = document.querySelector("#main-table");
    let tfoot = table.querySelector("tfoot");
    if (!tfoot) {
        tfoot = document.createElement("tfoot");
        table.appendChild(tfoot);
    }
    thead.innerHTML = "";
    let trHead = document.createElement("tr");
    trHead.classList.add("thead-dark");
    headers.forEach((header, index) => {
        let th = document.createElement("th");
        th.textContent = header;

        if (index >= headers.length - 6) {
            th.classList.add("double-column");
        }
        trHead.appendChild(th);
    });
    thead.appendChild(trHead);
}
function updateTableHeaderssinif(headers) {
    let thead = document.querySelector("#main-table thead");
    let table = document.querySelector("#main-table");
    let tfoot = table.querySelector("tfoot");
    if (!tfoot) {
        tfoot = document.createElement("tfoot");
        table.appendChild(tfoot);
    }
    thead.innerHTML = "";
    let trHead = document.createElement("tr");
    trHead.classList.add("thead-dark");
    headers.forEach((header, index) => {
        let th = document.createElement("th");
        th.textContent = header;

        if (index >= headers.length - 6) {
            th.classList.add("double-column");
        }
        trHead.appendChild(th);
    });
    thead.appendChild(trHead);
}

function updateTableHeaders3(headers) {
    let thead = document.querySelector("#main-table thead");
    let table = document.querySelector("#main-table");
    let tfoot = table.querySelector("tfoot");
    if (!tfoot) {
        tfoot = document.createElement("tfoot");
        table.appendChild(tfoot);
    }
    thead.innerHTML = "";
    let trHead = document.createElement("tr");
    trHead.classList.add("thead-dark");
    headers.forEach((header, index) => {
        let th = document.createElement("th");
        th.textContent = header;
        if (index >= headers.length - 6) {
            th.classList.add("double-column");
        }
        trHead.appendChild(th);
    });
    thead.appendChild(trHead);
}

async function ortfiatDownload() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";

	document.body.style.cursor = "wait";
	const $indirButton = $('#ortfiatDownload');
	$indirButton.prop('disabled', true).text('İşleniyor...');
	const $yenileButton = $('#ortfiatyenileButton');
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
		const response = await fetchWithSessionCheckForDownload('kereste/ortfiat_download', {
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

async function ortfiatmailAt() {
	localStorage.removeItem("tableData");
	localStorage.removeItem("grprapor");
	localStorage.removeItem("tablobaslik");
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
	const degerler = "kerortfiat";
	const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
	mailsayfasiYukle(url);
}