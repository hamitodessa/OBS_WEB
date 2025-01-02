let activeNestedInputId = null;
let nerden = null;
function openFirstModal(nerdenGeldi) {
	if (nerdenGeldi === "cekgir" || nerdenGeldi === "cekcik") {
		const bordroNo = document.getElementById("bordrono").value.trim();
		if (!bordroNo || bordroNo === "0") {
			return;
		}
	} else if (nerdenGeldi === "tahsilatckaydet") {
		const bordroNo = document.getElementById("tahevrakNo").value.trim();
		if (!bordroNo || bordroNo === "0") {
			return;
		}
	}

	const modal = document.getElementById('firstModal');
	nerden = nerdenGeldi;
	if (nerden === "tahsilatrapor") {
		opentahrapModal(modal);
	}
	else {
		$(modal).modal('show');
	}
}

async function openSecondModal(inputId, secondnerden) {
	activeNestedInputId = inputId;
	$('#secondModal').modal('show');
	const modalError = document.getElementById("hsperrorDiv");
	modalError.style.display = "none";
	modalError.innerText = "";
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("modal/hsppln");
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const data = response;
		const tableBody = document.getElementById("modalTableBody");
		tableBody.innerHTML = "";
		if (data.length === 0) {
			if (modalError) {
				modalError.style.display = "block";
				modalError.innerText = "Hiç veri bulunamadı.";
			}
			return;
		}
		tableBody.classList.add("table-row-height");
		data.forEach((row) => {
			const tr = document.createElement("tr");
			tr.innerHTML = `
                   <td>${row.HESAP || ""}</td>
                   <td>${row.UNVAN || ""}</td>
                   <td>${row.HESAP_CINSI || ""}</td>
                   <td>${row.KARTON || ""}</td>
               `;
			tr.onclick = () => selectValue(inputId, row.HESAP, secondnerden);
			tableBody.appendChild(tr);
		});
	} catch (error) {
		const modalError = document.getElementById("hsperrorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}

function filterTable() {
	const searchValue = document.getElementById("modalSearch").value.toLowerCase();
	const rows = document.querySelectorAll("#modalTable tbody tr");
	rows.forEach((row) => {
		const rowText = Array.from(row.cells)
			.map((cell) => cell.textContent.toLowerCase())
			.join(" ");
		row.style.display = rowText.includes(searchValue) ? "" : "none";
	});
}

function selectValue(inputId, selectedValue, secondnerden) {
	const inputElement = document.getElementById(inputId);
	if (inputElement) {
		inputElement.value = selectedValue;
		if (secondnerden === "dekont") {
			inputElement.oninput();
		}
		else if (secondnerden === "tahsilat") {
			inputElement.oninput();
		}
		else if (secondnerden === "cekgir") {
			inputElement.oninput();
		}
		else if (secondnerden === "cekcik") {
			inputElement.oninput();
		}
		else if (secondnerden === "tahsilatckaydet") {
			inputElement.oninput();
		}
		else if (secondnerden === "carikoddegis") {
			inputElement.oninput();
		}
		else if (secondnerden === "gunlukkontrol") {
			inputElement.oninput();
			belgeoku();
		}
		document.getElementById("modalSearch").value = "";
		$('#secondModal').modal('hide');
	}

}
function saveToMain() {
	if (nerden === "mizan") {
		const hkodu1 = $('#fhkodu1').val() || "";
		const hkodu2 = $('#fhkodu2').val() || "";
		const startDate = $('#startDate').val() || "";
		const endDate = $('#endDate').val() || "";
		const cins1 = $('#cins1').val() || "";
		const cins2 = $('#cins2').val() || "";
		const karton1 = $('#karton1').val() || "";
		const karton2 = $('#karton2').val() || "";
		const hangi_tur = $('#hangi_tur').val() || "";
		const degerler = [hkodu1, hkodu2, startDate, endDate, cins1, cins2, karton1, karton2, hangi_tur].join(",");
		const hiddenField = $('#ara_content #mizanBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "ekstre") {
		const hkodu1 = $('#fhkodu1').val() || "";
		const startDate = $('#startDate').val() || "";
		const endDate = $('#endDate').val() || "";
		const degerler = [hkodu1, startDate, endDate].join(",");
		const hiddenField = $('#ara_content #ekstreBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "dvzcevirme") {
		const hkodu1 = $('#fhkodu1').val() || "";
		const startDate = $('#startDate').val() || "";
		const endDate = $('#endDate').val() || "";
		const dvz_tur = $('#dvz_tur').val() || "";
		const dvz_cins = $('#dvz_cins').val() || "";
		const degerler = [hkodu1, startDate, endDate, dvz_tur, dvz_cins].join(",");
		const hiddenField = $('#ara_content #dvzcevirmeBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "ozelmizan") {
		const hkodu1 = $('#fhkodu1').val() || "";
		const hkodu2 = $('#fhkodu2').val() || "";
		const startDate = $('#startDate').val() || "";
		const endDate = $('#endDate').val() || "";
		const cins1 = $('#cins1').val() || "";
		const cins2 = $('#cins2').val() || "";
		const karton1 = $('#karton1').val() || "";
		const karton2 = $('#karton2').val() || "";
		const hangi_tur = $('#hangi_tur').val() || "";
		const degerler = [hkodu1, hkodu2, startDate, endDate, cins1, cins2, karton1, karton2, hangi_tur].join(",");
		const hiddenField = $('#ara_content #mizanBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "tahsilatrapor") {
		const tah_ted = $('#tah_ted').selectedIndex;
		const hangi_tur = $('#hangi_tur').selectedIndex;
		const pos = $('#pos').val() || "";
		const hkodu1 = $('#fhkodu1').val() || "";
		const hkodu2 = $('#fhkodu2').val() || "";
		const startDate = $('#startDate').val() || "";
		const endDate = $('#endDate').val() || "";
		const fhevrak1 = $('#fhevrak1').val() || "";
		const fhevrak2 = $('#fhevrak2').val() || "";
		const degerler = [tah_ted, hangi_tur, pos, hkodu1, hkodu2, startDate, endDate, fhevrak1, fhevrak2].join(",");
		const hiddenField = $('#ara_content #tahrapBilgi');
		hiddenField.val(degerler);
	}

	else if (nerden === "kurrapor") {
		const startDate = $('#startDate').val() || "";
		const endDate = $('#endDate').val() || "";
		const cins1 = $('#cins1').val() || "";
		const cins2 = $('#cins2').val() || "";
		const degerler = [startDate, endDate, cins1, cins2].join(",");
		const hiddenField = $('#ara_content #kurraporBilgi');
		hiddenField.val(degerler);
	}

	else if (nerden === "cekrapor") {
		const cekno1 = $('#cekno1').val() || "";
		const cekno2 = $('#cekno2').val() || "";
		const durum1 = $('#durum1').val() || "";
		const durum2 = $('#durum2').val() || "";
		const vade1 = $('#vade1').val() || "";
		const vade2 = $('#vade2').val() || "";
		const ttar1 = $('#ttar1').val() || "";
		const ttar2 = $('#ttar2').val() || "";
		const gbor1 = $('#gbor1').val() || "";
		const gbor2 = $('#gbor2').val() || "";
		const gozel = $('#gozel').val() || "";
		const cozel = $('#cozel').val() || "";
		const gtar1 = $('#gtar1').val() || "";
		const gtar2 = $('#gtar2').val() || "";
		const cins1 = $('#cins1').val() || "";
		const cins2 = $('#cins2').val() || "";
		const cbor1 = $('#cbor1').val() || "";
		const cbor2 = $('#cbor2').val() || "";
		const ches1 = $('#ches1').val() || "";
		const ches2 = $('#ches2').val() || "";
		const ctar1 = $('#ctar1').val() || "";
		const ctar2 = $('#ctar2').val() || "";
		const hangi_tur = $('#hangi_tur').val() || "";
		const ghes1 = $('#ghes1').val() || "";
		const ghes2 = $('#ghes2').val() || "";
		const degerler = [cekno1, cekno2, durum1, durum2, vade1, vade2, ttar1, ttar2, gbor1, gbor2, gozel, cozel, gtar1, gtar2
			, cins1, cins2, cbor1, cbor2, ches1, ches2, ctar1, ctar2, hangi_tur, ghes1, ghes2].join(",");
		const hiddenField = $('#ara_content #cekrapBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "cekgir") {
		const hesapKodu = $('#ckodu').val() || "";
		const hiddenField = $('#ara_content #cekgirBilgi');
		hiddenField.val(hesapKodu);
	}
	else if (nerden === "cekcik") {
		const hesapKodu = $('#ckodu').val() || "";
		const hiddenField = $('#ara_content #cekcikBilgi');
		hiddenField.val(hesapKodu);
	}
	else if (nerden === "tahsilatckaydet") {
		const hesapKodu = $('#ckodu').val() || "";
		const hiddenField = $('#ara_content #tahsilatBilgi');
		hiddenField.val(hesapKodu);
	}



	$('#firstModal').modal('hide');

	if (nerden === "mizan") {
		mizfetchTableData();
		const mailButton = document.getElementById("mailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("reportFormat");
		reportFormat.disabled = false;
	}
	else if (nerden === "ekstre") {
		eksfetchTableData();
		const mailButton = document.getElementById("mailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("reportFormat");
		reportFormat.disabled = false;
	}
	else if (nerden === "ozelmizan") {
		ozmizfetchTableData();
		const mailButton = document.getElementById("mailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("reportFormat");
		reportFormat.disabled = false;
	}
	else if (nerden === "tahsilatrapor") {
		tahrapfetchTableData();
		const mailButton = document.getElementById("tahrapmailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("tahrapreportFormat");
		reportFormat.disabled = false;
	}
	else if (nerden === "dvzcevirme") {
		dvzfetchTableData();
		const mailButton = document.getElementById("mailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("reportFormat");
		reportFormat.disabled = false;
	}
	else if (nerden === "cekrapor") {
		cekfetchTableData();
		const reportFormat = document.getElementById("reportFormat");
		reportFormat.disabled = false;
	}
	else if (nerden === "cekgir") {
		cekcariIsle();
	}
	else if (nerden === "cekcik") {
		cekcariIsle();
	}
	else if (nerden === "kurrapor") {
		kurrapfetchTableData();
	}
	else if (nerden === "tahsilatckaydet") {
		tahcariIsle();
	}
}

async function opentahrapModal(modal) {
	$(modal).modal('show');
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("cari/tahsilatrappos", {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			}
		});
		if (response.errorMessage) {
			throw new Error(response.errorMessage);
		}
		const result = response;
		const data = result.data; // Gelen liste
		const posSelect = document.getElementById("pos");
		posSelect.innerHTML = ""; // Önce eski seçenekleri temizle
		if (data.length === 0) {
			const modalError = document.getElementById("errorDiv");
			if (modalError) {
				modalError.style.display = "block";
				modalError.innerText = "Hiç veri bulunamadı.";
			}
			return;
		}
		const optionbos = document.createElement("option");
		optionbos.value = "";
		optionbos.textContent = "";
		posSelect.appendChild(optionbos);
		data.forEach(item => {
			const option = document.createElement("option");
			option.value = item.BANKA;
			option.textContent = item.BANKA;
			posSelect.appendChild(option);
		});
	} catch (error) {
		const modalError = document.getElementById("errorDiv");
		modalError.style.display = "block";
		modalError.innerText = `Bir hata oluştu: ${error.message}`;
	} finally {
		document.body.style.cursor = "default";
	}
}