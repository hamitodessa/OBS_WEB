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