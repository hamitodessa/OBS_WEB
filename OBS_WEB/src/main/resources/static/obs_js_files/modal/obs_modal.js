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
	else if (nerdenGeldi === "fatura") {
		const fisno = document.getElementById("fisno").value.trim();
		if (!fisno || fisno === "0") {
			return;
		}
	}
	else if (nerdenGeldi === "kerestegiris") {
		const fisno = document.getElementById("fisno").value.trim();
		if (!fisno || fisno === "0") {
			return;
		}
	}

	const modal = document.getElementById('firstModal');
	nerden = nerdenGeldi;
	if (nerden === "tahsilatrapor") {
		opentahrapModal(modal);
	}
	else if (nerden === "fatrapor") {
		openfatrapModal(modal);
	}
	else if (nerden === "imarapor") {
		openimarapModal(modal);
	}
	else if (nerden === "envanter") {
		openenvModal(modal);
	}
	else if (nerden === "stokrapor") {
		openenvModal(modal);
	}
	else if (nerden === "grprapor") {
		openenvModal(modal);
	}
	else if (nerden === "imagrprapor") {
		openenvModal(modal);
	}
	else if (nerden === "stokdetayrapor") {
		openenvModal(modal);
	}
	else if (nerden === "kerestedetayrapor") {
		openenvModal(modal);
	}
	else if (nerden === "kergrprapor") {
		openenvModal(modal);
	}
	else if (nerden === "kerfatrapor") {
		openenvModal(modal);
	}
	else if (nerden === "kerenvanter") {
		openenvModal(modal);
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
		else if (secondnerden === "fatura") {
			inputElement.oninput();
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
		const tah_ted = $('#tah_ted').prop('selectedIndex');
		const hangi_tur = $('#hangi_tur').prop('selectedIndex');
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
	else if (nerden === "fatura") {
		const hesapKodu = $('#ckodu').val() || "";
		const hiddenField = $('#ara_content #faturaBilgi');
		hiddenField.val(hesapKodu);
	}
	else if (nerden === "fatrapor") {
		const fatno1 = $('#fatno1').val() || "";
		const fatno2 = $('#fatno2').val() || "";
		const anagrp = $('#anagrp').val() || "";
		const tar1 = $('#tar1').val() || "";
		const tar2 = $('#tar2').val() || "";
		const altgrp = $('#altgrp').val() || "";
		const ckod1 = $('#ckod1').val() || "";
		const ckod2 = $('#ckod2').val() || "";
		const depo = $('#depo').val() || "";
		const adr1 = $('#adr1').val() || "";
		const adr2 = $('#adr2').val() || "";
		const turu = $('#turu').val() || "";
		const ukod1 = $('#ukod1').val() || "";
		const ukod2 = $('#ukod2').val() || "";
		const tev1 = $('#tev1').val() || "";
		const tev2 = $('#tev2').val() || "";
		const okod1 = $('#okod1').val() || "";
		const okod2 = $('#okod2').val() || "";
		const gruplama = $('#gruplama').val() || "";
		const dvz1 = $('#dvz1').val() || "";
		const dvz2 = $('#dvz2').val() || "";
		const caradr = $('#caradr').val() || "";
		const degerler = [fatno1, fatno2, anagrp, tar1, tar2, altgrp, ckod1, ckod2, depo, adr1, adr2, turu, ukod1, ukod2, tev1, tev2, okod1, okod2
			, gruplama, dvz1, dvz2, caradr].join(",");
		const hiddenField = $('#ara_content #fatrapBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "imarapor") {
		const evrno1 = $('#evrno1').val() || "";
		const evrno2 = $('#evrno2').val() || "";
		const uranagrp = $('#uranagrp').val() || "";
		const tar1 = $('#tar1').val() || "";
		const tar2 = $('#tar2').val() || "";
		const uraltgrp = $('#uraltgrp').val() || "";
		const bkod1 = $('#bkod1').val() || "";
		const bkod2 = $('#bkod2').val() || "";
		const depo = $('#depo').val() || "";
		const ukod1 = $('#ukod1').val() || "";
		const ukod2 = $('#ukod2').val() || "";
		const rec1 = $('#rec1').val() || "";
		const rec2 = $('#rec2').val() || "";
		const anagrp = $('#anagrp').val() || "";
		const altgrp = $('#altgrp').val() || "";
		const degerler = [evrno1, evrno2, uranagrp, tar1, tar2, uraltgrp, bkod1, bkod2, depo, ukod1, ukod2, rec1, rec2, anagrp, altgrp].join(",");
		const hiddenField = $('#ara_content #imarapBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "envanter") {
		const tar1 = $('#tar1').val() || "";
		const tar2 = $('#tar2').val() || "";
		const uranagrp = $('#uranagrp').val() || "";
		const ukod1 = $('#ukod1').val() || "";
		const ukod2 = $('#ukod2').val() || "";
		const uraltgrp = $('#uraltgrp').val() || "";
		const evrno1 = $('#evrno1').val() || "";
		const evrno2 = $('#evrno2').val() || "";
		const anagrp = $('#anagrp').val() || "";
		const gruplama = $('#gruplama').val() || "";
		const altgrp = $('#altgrp').val() || "";
		const depo = $('#depo').val() || "";
		const fiatlama = $('#fiatlama').val() || "";
		const depohardahil = $('#depohardahil').prop('checked');
		const uretfisdahil = $('#uretfisdahil').prop('checked');

		const degerler = [tar1, tar2, uranagrp, ukod1, ukod2, uraltgrp, evrno1, evrno2, anagrp, gruplama, altgrp, depo, fiatlama, depohardahil, uretfisdahil].join(",");
		const hiddenField = $('#ara_content #envanterBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "stokrapor") {
		const tar1 = $('#tar1').val() || "";
		const tar2 = $('#tar2').val() || "";
		const uranagrp = $('#uranagrp').val() || "";
		const ukod1 = $('#ukod1').val() || "";
		const ukod2 = $('#ukod2').val() || "";
		const uraltgrp = $('#uraltgrp').val() || "";
		const evrno1 = $('#evrno1').val() || "";
		const evrno2 = $('#evrno2').val() || "";
		const anagrp = $('#anagrp').val() || "";
		const gruplama = $('#gruplama').val() || "";
		const altgrp = $('#altgrp').val() || "";
		const depo = $('#depo').val() || "";
		const oncekitarih = $('#oncekitarih').prop('checked');
		const depohardahil = $('#depohardahil').prop('checked');
		const uretfisdahil = $('#uretfisdahil').prop('checked');
		const degerler = [tar1, tar2, uranagrp, ukod1, ukod2, uraltgrp, evrno1, evrno2, anagrp, gruplama, altgrp, depo, oncekitarih, depohardahil, uretfisdahil].join(",");
		const hiddenField = $('#ara_content #stokBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "grprapor") {
		const tar1 = $('#tar1').val() || "";
		const tar2 = $('#tar2').val() || "";
		const uranagrp = $('#uranagrp').val() || "";
		const ukod1 = $('#ukod1').val() || "";
		const ukod2 = $('#ukod2').val() || "";
		const uraltgrp = $('#uraltgrp').val() || "";
		const ckod1 = $('#ckod1').val() || "";
		const ckod2 = $('#ckod2').val() || "";
		const urozkod = $('#urozkod').val() || "";
		const birim = $('#birim').val() || "";
		const istenenay = $('#istenenay').val() || "";
		const gruplama = $('#gruplama').val() || "";
		const dvzcevirchc = $('#dvzcevirchc').prop('checked');
		const doviz = $('#dvzcins').val() || "";
		const stunlar = $('#stunlar').val() || "";
		const dvzturu = $('#dvzturu').val() || "";
		const turu = $('#turu').val() || "";
		const istenenaychc = $('#istenenaychc').prop('checked');
		const sinif1 = $('#sinif1').val() || "";
		const sinif2 = $('#sinif2').val() || "";
		const degerler = [tar1, tar2, uranagrp, ukod1, ukod2, uraltgrp, ckod1, ckod2, urozkod, birim, istenenay, gruplama, dvzcevirchc, doviz,
			stunlar, dvzturu, turu, istenenaychc, sinif1, sinif2].join(",");
		const hiddenField = $('#ara_content #grpBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "imagrprapor") {
		const ukod1 = $('#ukod1').val() || "";
		const ukod2 = $('#ukod2').val() || "";
		const tar1 = $('#tar1').val() || "";
		const tar2 = $('#tar2').val() || "";
		const sinif1 = $('#sinif1').val() || "";
		const sinif2 = $('#sinif2').val() || "";
		const uranagrp = $('#uranagrp').val() || "";
		const uraltgrp = $('#uraltgrp').val() || "";
		const birim = $('#birim').val() || "";
		const gruplama = $('#gruplama').val() || "";
		const stunlar = $('#stunlar').val() || "";
		const turu = $('#turu').val() || "";
		const anagrp = $('#anagrp').val() || "";
		const altgrp = $('#altgrp').val() || "";
		const degerler = [ukod1, ukod2, tar1, tar2, sinif1, sinif2, uranagrp, uraltgrp, birim, gruplama, stunlar, turu, anagrp, altgrp].join(",");
		const hiddenField = $('#ara_content #imagrpBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "stokdetayrapor") {
		const tar1 = $('#tar1').val() || "";
		const tar2 = $('#tar2').val() || "";
		const uranagrp = $('#uranagrp').val() || "";
		const ukod1 = $('#ukod1').val() || "";
		const ukod2 = $('#ukod2').val() || "";
		const uraltgrp = $('#uraltgrp').val() || "";
		const evrno1 = $('#evrno1').val() || "";
		const evrno2 = $('#evrno2').val() || "";
		const ckod1 = $('#ckod1').val() || "";
		const ckod2 = $('#ckod2').val() || "";
		const anagrp = $('#anagrp').val() || "";
		const altgrp = $('#altgrp').val() || "";
		const depo = $('#depo').val() || "";
		const depohardahil = $('#depohardahil').prop('checked');
		const uretfisdahil = $('#uretfisdahil').prop('checked');
		const turu = $('#turu').val() || "";
		const degerler = [tar1, tar2, uranagrp, ukod1, ukod2, uraltgrp, evrno1, evrno2, ckod1, ckod2, anagrp, altgrp, depo, depohardahil, uretfisdahil, turu].join(",");
		const hiddenField = $('#ara_content #stokdetayBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "kerestegiris") {
		const hesapKodu = $('#ckodu').val() || "";
		const hiddenField = $('#ara_content #kerBilgi');
		hiddenField.val(hesapKodu);
	}
	else if (nerden === "kerestedetayrapor") {
		const tar1 = $('#tar1').val() || "";
		const tar2 = $('#tar2').val() || "";
		const ctar1 = $('#ctar1').val() || "";
		const ctar2 = $('#ctar2').val() || "";
		const ukod1 = $('#ukod1').val() || "";
		const ukod2 = $('#ukod2').val() || "";
		const ckod1 = $('#chkod1').val() || "";
		const ckod2 = $('#chkod2').val() || "";
		const pak1 = $('#pak1').val() || "";
		const pak2 = $('#pak2').val() || ""; 
		const cevr1 = $('#cevr1').val() || "";
		const cevr2 = $('#cevr2').val() || "";
		const hes1 = $('#hes1').val() || "";
		const hes2 = $('#hes2').val() || "";
		const canagrp = $('#canagrp').val() || "";
		const evr1 = $('#evr1').val() || "";
		const evr2 = $('#evr2').val() || "";
		const caltgrp = $('#caltgrp').val() || "";
		const anagrp = $('#anagrp').val() || "";
		const altgrp = $('#altgrp').val() || "";
		const depo = $('#depo').val() || "";
		const ozkod = $('#ozkod').val() || "";
		const kons1 = $('#kons1').val() || "";
		const kons2 = $('#kons2').val() || "";
		const cozkod = $('#cozkod').val() || "";
		const cdepo = $('#cdepo').val() || "";
		const degerler = [tar1, tar2, ctar1, ctar2, ukod1, ukod2, ckod1, ckod2, pak1, pak2,
			cevr1, cevr2, hes1, hes2, canagrp, evr1, evr2,caltgrp, anagrp, altgrp,
			depo, ozkod, kons1, kons2, cozkod, cdepo].join(",");
		const hiddenField = $('#ara_content #kerestedetayBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "kerenvanter") {
		const tar1 = $('#tar1').val() || "";
		const tar2 = $('#tar2').val() || "";
		const ctar1 = $('#ctar1').val() || "";
		const ctar2 = $('#ctar2').val() || "";
		const ukod1 = $('#ukod1').val() || "";
		const ukod2 = $('#ukod2').val() || "";
		const ckod1 = $('#chkod1').val() || "";
		const ckod2 = $('#chkod2').val() || "";
		const pak1 = $('#pak1').val() || "";
		const pak2 = $('#pak2').val() || "";
		const cevr1 = $('#cevr1').val() || "";
		const cevr2 = $('#cevr2').val() || "";
		const hes1 = $('#hes1').val() || "";
		const hes2 = $('#hes2').val() || "";
		const canagrp = $('#canagrp').val() || "";
		const evr1 = $('#evr1').val() || "";
		const evr2 = $('#evr2').val() || "";
		const caltgrp = $('#caltgrp').val() || "";
		const anagrp = $('#anagrp').val() || "";
		const altgrp = $('#altgrp').val() || "";
		const depo = $('#depo').val() || "";
		const ozkod = $('#ozkod').val() || "";
		const kons1 = $('#kons1').val() || "";
		const kons2 = $('#kons2').val() || "";
		const cozkod = $('#cozkod').val() || "";
		const cdepo = $('#cdepo').val() || "";
		const gruplama = $('#gruplama').val() || "";
		const degerler = [tar1, tar2, ctar1, ctar2, ukod1, ukod2, ckod1, ckod2, pak1, pak2,
			cevr1, cevr2, hes1, hes2, canagrp, evr1, evr2, caltgrp, anagrp, altgrp,
			depo, ozkod, kons1, kons2, cozkod, cdepo,gruplama].join(",");
		const hiddenField = $('#ara_content #envanterBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "kergrprapor") {
		const tar1 = $('#tar1').val() || "";
		const tar2 = $('#tar2').val() || "";
		const anagrp = $('#anagrp').val() || "";
		const ukod1 = $('#ukod1').val() || "";
		const ukod2 = $('#ukod2').val() || "";
		const altgrp = $('#altgrp').val() || "";
		const ckod1 = $('#ckod1').val() || "";
		const ckod2 = $('#ckod2').val() || "";
		const ozkod = $('#ozkod').val() || "";
		const kons1 = $('#kons1').val() || "";
		const kons2 = $('#kons2').val() || "";
		const depo = $('#depo').val() || "";
		const evr1 = $('#evr1').val() || "";
		const evr2 = $('#evr2').val() || "";
		const birim = $('#birim').val() || "";
		const gruplama = $('#gruplama').val() || "";
		const stunlar = $('#stunlar').val() || "";
		const turu = $('#turu').val() || "";
		const dvzcevirchc = $('#dvzcevirchc').prop('checked');
		const doviz = $('#dvzcins').val() || "";
		const dvzturu = $('#dvzturu').val() || "";
		const degerler = [tar1, tar2, anagrp, ukod1, ukod2, altgrp, ckod1, ckod2, ozkod,kons1,kons2,depo,evr1,evr2, birim,gruplama,
			stunlar, turu, dvzcevirchc,doviz,dvzturu].join(",");
		const hiddenField = $('#ara_content #grpBilgi');
		hiddenField.val(degerler);
	}
	else if (nerden === "kerfatrapor") {
		const tar1 = $('#tar1').val() || "";
		const tar2 = $('#tar2').val() || "";
		const ctar1 = $('#ctar1').val() || "";
		const ctar2 = $('#ctar2').val() || "";
		const ukod1 = $('#ukod1').val() || "";
		const ukod2 = $('#ukod2').val() || "";
		const ckod1 = $('#chkod1').val() || "";
		const ckod2 = $('#chkod2').val() || "";
		const pak1 = $('#pak1').val() || "";
		const pak2 = $('#pak2').val() || "";
		const cevr1 = $('#cevr1').val() || "";
		const cevr2 = $('#cevr2').val() || "";
		const hes1 = $('#hes1').val() || "";
		const hes2 = $('#hes2').val() || "";
		const canagrp = $('#canagrp').val() || "";
		const evr1 = $('#evr1').val() || "";
		const evr2 = $('#evr2').val() || "";
		const caltgrp = $('#caltgrp').val() || "";
		const anagrp = $('#anagrp').val() || "";
		const altgrp = $('#altgrp').val() || "";
		const depo = $('#depo').val() || "";
		const ozkod = $('#ozkod').val() || "";
		const kons1 = $('#kons1').val() || "";
		const kons2 = $('#kons2').val() || "";
		const cozkod = $('#cozkod').val() || "";
		const cdepo = $('#cdepo').val() || "";
		const gruplama = $('#gruplama').val() || "";
		const caradr = $('#caradr').val() || "";
		const gircik = $('#turu').val() || "";
		const degerler = [tar1, tar2, ctar1, ctar2, ukod1, ukod2, ckod1, ckod2, pak1, pak2,
			cevr1, cevr2, hes1, hes2, canagrp, evr1, evr2, caltgrp, anagrp, altgrp,
			depo, ozkod, kons1, kons2, cozkod, cdepo, gruplama, caradr,gircik].join(",");
		const hiddenField = $('#ara_content #fatrapBilgi');
		hiddenField.val(degerler);
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
		eksdoldur();
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
		dvzdoldur();
		const mailButton = document.getElementById("mailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("reportFormat");
		reportFormat.disabled = false;
	}
	else if (nerden === "cekrapor") {
		cekfetchTableData();
		const reportFormat = document.getElementById("cekrapreportFormat");
		reportFormat.disabled = false;
		const mailButton = document.getElementById("cekrapmailButton");
		mailButton.disabled = false;

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
	else if (nerden === "fatura") {
		fatcariIsle();
	}
	else if (nerden === "kerestegiris") {
		kercariIsle();
	}
	else if (nerden === "fatrapor") {
		fatdoldur();
		const mailButton = document.getElementById("fatrapmailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("fatrapreportFormat");
		reportFormat.disabled = false;
	}
	else if (nerden === "imarapor") {
		imafetchTableData();
		const mailButton = document.getElementById("imarapmailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("imarapdownloadButton");
		reportFormat.disabled = false;
	}
	else if (nerden === "envanter") {
		envfetchTableData();
		const mailButton = document.getElementById("envmailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("envDownloadButton");
		reportFormat.disabled = false;
	}
	else if (nerden === "stokrapor") {
		stokfetchTableData();
		const mailButton = document.getElementById("stokrapmailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("stokrapreportDownload");
		reportFormat.disabled = false;
	}
	else if (nerden === "grprapor") {
		grpfetchTableData();
		const mailButton = document.getElementById("grprapmailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("grprapreportDownload");
		reportFormat.disabled = false;
	}
	else if (nerden === "imagrprapor") {
		imagrpfetchTableData();
		const mailButton = document.getElementById("imagrprapmailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("imagrprapreportDownload");
		reportFormat.disabled = false;
	}
	else if (nerden === "stokdetayrapor") {
		stokdetayfetchTableData();
		const mailButton = document.getElementById("stokdetaymailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("stokdetayreportDownload");
		reportFormat.disabled = false;
	}
	else if (nerden === "kerestedetayrapor") {
		kerestedetaydoldur();
		const mailButton = document.getElementById("kerestedetaymailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("kerestedetayreportDownload");
		reportFormat.disabled = false;
	}
	else if (nerden === "kergrprapor") {
		grpfetchTableData();
		const mailButton = document.getElementById("grprapmailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("grprapreportDownload");
		reportFormat.disabled = false;
	}
	else if (nerden === "kerfatrapor") {
		kerfatdoldur();
		const mailButton = document.getElementById("fatrapmailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("fatrapreportFormat");
		reportFormat.disabled = false;
	}
	else if (nerden === "kerenvanter") {
		kerenvanterdoldur();
		const mailButton = document.getElementById("envantermailButton");
		mailButton.disabled = false;
		const reportFormat = document.getElementById("envanterDownload");
		reportFormat.disabled = false;
	}
}