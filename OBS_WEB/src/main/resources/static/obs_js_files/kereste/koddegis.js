$(document).ready(function(){
    $("#kodu").inputmask({
      mask: "AA-999-9999-9999",
      placeholder: "-",
      definitions: {
        'A': {
          validator: "[A-Za-z0-9]",
          cardinality: 1
        }
      }
    });
  });

function toggleCheckboxes(source) {
	const checkboxes = document.querySelectorAll('tbody input[type="checkbox"]');
	let totalsatir = 0;
	checkboxes.forEach(checkbox => {
		checkbox.checked = source.checked;
		totalsatir += 1;
	});
	document.getElementById("totalSatir").innerText = formatNumber0(totalsatir);
}

async function fetchTable() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	
	document.body.style.cursor = "wait";
	const tableBody = document.getElementById("tableBody");
	tableBody.innerHTML = "";

    const kons = document.getElementById("kons").value;
    const kodu = document.getElementById("kodu").value;
    const evrak = document.getElementById("gevrak").value;
    const pakno = document.getElementById("pakno").value;
	if (
		(!kons || kons.trim() === "") &&
		(!evrak || evrak.trim() === "") &&
		(!pakno || pakno.trim() === "") &&
		(kodu === "00-000-0000-0000")
	  ) {
		document.body.style.cursor = "default";
		return;
	  }
	  document.getElementById("totalSatir").innerText = "0";
	try {
		const response = await fetchWithSessionCheck("kereste/koddegisload", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({pakno,kons, kodu,evrak}), 
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		data = response;
		data.data.forEach(rowData => {
			const row = document.createElement('tr');
			row.classList.add("table-row-height");
			row.innerHTML = `
                <td style="width: 40px;"><input type="checkbox"></td>
				<td>${rowData.Evrak_No}</td>
				<td>${rowData.Barkod}</td>
                <td>${rowData.Kodu}</td>
                <td>${rowData.Paket_No}</td>
                <td>${rowData.Konsimento}</td>
                <td class="double-column">${formatNumber0(rowData.Miktar)}</td>
                <td class="double-column">${formatNumber3(rowData.m3)}</td>
                <td>${formatDate(rowData.Tarih)}</td>
                <td class="double-column">${formatNumber2(rowData.Kdv)}</td>
                <td>${rowData.Doviz}</td>
                <td class="double-column">${formatNumber2(rowData.Fiat)}</td>
                <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
                <td class="double-column">${formatNumber2(rowData.Kur)}</td>
                <td>${rowData.Cari_Firma}</td>
                <td>${rowData.Adres_Firma}</td>
                <td class="double-column">${formatNumber2(rowData.Iskonto)}</td>
                <td class="double-column">${formatNumber2(rowData.Tevkifat)}</td>
                <td>${rowData.Ana_Grup}</td>
                <td>${rowData.Alt_Grup}</td>
                <td>${rowData.Mensei}</td>
				<td style="display: none;">${rowData.Satir}</td>
            `;
			tableBody.appendChild(row);
		});
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error;
	} finally {
		document.body.style.cursor = "default";
	}
}

async function kodKaydet() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.style.display = "none";
	errorDiv.innerText = "";
	
	document.body.style.cursor = "wait";
	const tableBody = document.getElementById("tableBody");
	tableBody.innerHTML = "";

    const ykod = document.getElementById("ykod").value;
    
	if (!ykod || ykod.trim() === "") {
		document.body.style.cursor = "default";
		return;
	}
	const checkedCheckboxes = document.querySelectorAll("table tr td:first-child input[type='checkbox']:checked");
	if (checkedCheckboxes.length === 0) {
  		document.body.style.cursor = "default";
  		return;
	}

	try {
		const response = await fetchWithSessionCheck("kereste/kodkaydet", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({ykod}), 
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		data = response;
		
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error;
	} finally {
		document.body.style.cursor = "default";
	}
}

