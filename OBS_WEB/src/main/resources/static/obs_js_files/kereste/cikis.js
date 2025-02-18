console.info("Kereste Çıkış İşlemleri");

async function fetchpakdepo() {
	const errorDiv = document.getElementById("errorDiv");
	errorDiv.innerText = "";
	errorDiv.style.display = "none";
	rowCounter = 0;
	depolar = "";
	urnkodlar = "";
	try {
		const response = await fetchWithSessionCheck("kereste/getpakdepo", {
			method: "GET",
			headers: {
				"Content-Type": "application/json",
			},
		});
		if (response.errorMessage) {
			throw new Error(responseBanka.errorMessage);
		}
		urnkodlar = response.paknolar || [];
		depolar = response.depolar || [];
		initializeRows();
	} catch (error) {
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
		errorDiv.style.display = "block";
	}
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
		alert("En fazla 250 satir ekleyebilirsiniz.");
		return
	}
	const newRow = table.insertRow();
	incrementRowCounter();

	let paknooptionsHTML = urnkodlar.map(kod => `<option value="${kod.Paket_No}">${kod.Paket_No}</option>`).join("");

	newRow.innerHTML = `
		<td >
			<button id="bsatir_${rowCounter}" type="button" class="btn btn-secondary ml-2" onclick="satirsil(this)"><i class="fa fa-trash"></i></button>
		</td>
        <td>
	        <div style="position: relative; width: 100%;">
	            <input class="form-control cins_bold" list="pakOptions_${rowCounter}"  id="pakno_${rowCounter}" onkeydown="if(event.key === 'Enter') paketkontrol(event,this)">
				<datalist id="pakOptions_${rowCounter}">${paknooptionsHTML}</datalist>
		        <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
	        </div>
	    </td>
   		<td><label class="form-control"style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label></td>
		<td><label class="form-control"style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label></td>
		<td><label class="form-control"style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label></td>
        <td>
		    <label class="form-control" style="display: block;width:100%;height:100%;text-align:right;font-weight:bold;"><span>&nbsp;</span></label>
		</td>
        <td>
			<label class="form-control" style="display: block;width:100%;height:100%;text-align:right;font-weight:bold; color:darkgreen;"><span>&nbsp;</span></label>
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
	    `;

}

async function paketkontrol(event, input) {
	const fisno = document.getElementById("fisno").value;
	const errorDiv = document.getElementById('errorDiv');
	errorDiv.style.display = 'none';
	errorDiv.innerText = "";

	const row = input.closest('tr');
	if (!row) return;
	const pakno = input.value;
	console.info(pakno);
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("kereste/paket_oku", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ pno: pakno, cins: 'CIKIS', fisno: fisno }),
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
		console.info(data.paket);
		///
		const currentRow = input.closest('tr'); // Tıklanan inputun bulunduğu satırı al
		const table = currentRow.closest('table'); // Tabloyu bul
		let rows = Array.from(table.querySelectorAll('tr')); // Mevcut tüm satırları al
		let rowIndex = currentRow.rowIndex; // currentRow'un kaçıncı satırda olduğunu al
		
		data.paket.forEach((item) => {
			
			if (rowIndex >= rows.length) {
				rows.push(satirEkle());
			}
		
			const cells = rows[rowIndex].cells; 
			cells[2]?.querySelector('label span').textContent = item.Barkod || " "; 
		
			cells[3]?.querySelector('label span').textContent = item.Kodu;
		
			rowIndex++;
		});		///
		//updateValues(input);
		//focusNextCell(event, input)
	} catch (error) {
		const errorDiv = document.getElementById('errorDiv');
		errorDiv.style.display = 'block';
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function pakkonsayir(pakkons) {
	const pakno = pakkons.split("-")[0];
	const kons = pakkons.split("-")[1];
	return { pakno, kons };
}
