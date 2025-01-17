//altgrup();
//
//function altgrup() {
//        const selectElement = document.getElementById('altgrp');
//        if (selectElement.options.length === 0) {
//            selectElement.disabled = true;
//        } else {
//            selectElement.disabled = false;
//        }
//    };


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
		
		let ukoduoptionsHTML = bankaIsimleri;
		let depoHTML = subeIsimleri;
				
		tableBody.innerHTML = "";
		rowCounter = 0;
		rowCounter = 0;
		data.data.forEach(item => {
			if(item.Hareket === "C"){
			incrementRowCounter();
			const row = document.createElement("tr");
			row.innerHTML = `
					<td contenteditable="false">CIKAN</td>
					<td>
					    <div style="position: relative; width: 100%;">
					        <input class="form-control cins_bold" list="ukoduOptions_${rowCounter}" maxlength="12" id="ukodu_${rowCounter}" 
					            onkeydown="focusNextCell(event, this)" value="${item.Urun_Kodu || ''}">
					        <datalist id="ukoduOptions_${rowCounter}">${ukoduoptionsHTML}</datalist>
					        <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
					    </div>
					</td>
					<td contenteditable="false">${item.Adi || ''}</td>
					<td contenteditable="false">${item.Izahat || ''}</td>
					<td>
					    <div style="position: relative; width: 100%;">
					        <input class="form-control cins_bold" list="depoOptions_${rowCounter}" maxlength="30" 
							id="depo_${rowCounter}"  value="${item.Depo || ''}">
					            <datalist id="depoOptions_${rowCounter}">${depoHTML}</datalist>
					            <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
					    </div>
					</td>
					<td class="double-column">${formatNumber3(item.Miktar *-1 || '0')}</td>
					<td contenteditable="false" >${item.Birim || ''}</td>
					<td class="editable-cell double-column" contenteditable="true" 
					    onfocus="selectAllContent(this)" 
					    onblur="formatInputTable2(this); updateColumnTotal()" 
					    onkeydown="focusNextRow(event, this)">${formatNumber2(item.Fiat)}
					</td>
					<td class="editable-cell double-column" contenteditable="false" 
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
		//updateColumnTotal();
		errorDiv.style.display = "none";
		errorDiv.innerText = "";
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}
