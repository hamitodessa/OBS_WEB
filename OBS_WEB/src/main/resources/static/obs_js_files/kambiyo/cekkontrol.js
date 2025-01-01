async function aramaYap() {
	if (document.getElementById("arama").value.trim() === "") {
		clearform();
		return;
	}
	const cekNo = document.getElementById("arama").value;
	document.body.style.cursor = "wait";
	clearform();
	try {
		const response = await fetchWithSessionCheck("kambiyo/cektakipkontrol", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ cekNo: cekNo }),
		});
		if (response.errorMessage.trim() !== "") {
			throw new Error(response.errorMessage);
		}
		document.getElementById("cekno").innerText = response.data.cekNo;
		document.getElementById("banka").innerText = response.data.banka;
		document.getElementById("cikisbordro").innerText = response.data.cikisBordro;
		document.getElementById("cikishesapkod").innerText = response.data.cikisMusteri;
		if (response.data.cikisTarihi === "1900-01-01") {
			document.getElementById("cikistarihi").innerText = "";
		}
		else {
			document.getElementById("cikistarihi").innerText = formatDate(response.data.cikisTarihi);
		}
		document.getElementById("vade").innerText = formatDate(response.data.vade);
		document.getElementById("girisbordro").innerText = response.data.girisBordro;
		document.getElementById("girishesapkod").innerText = response.data.girisMusteri;
		document.getElementById("giristarihi").innerText = formatDate(response.data.girisTarihi);
		document.getElementById("sube").innerText = response.data.sube;
		document.getElementById("serino").innerText = response.data.seriNo;
		document.getElementById("ilkborclu").innerText = response.data.ilkBorclu;
		document.getElementById("cekhesapno").innerText = response.data.cekHesapNo;
		document.getElementById("tutar").innerText = formatNumber2(response.data.tutar);
		document.getElementById("gozelkod").innerText = response.data.girisOzelKod;
		document.getElementById("cozelkod").innerText = response.data.cikisOzelKod;
		document.getElementById("ttarih").value = response.data.ttarih;
		const durum = parseInt(response.data.durum, 10);
		if (durum === "")
			document.getElementById("islem").selectedIndex = 0;
		else if (durum === 1)
			document.getElementById("islem").selectedIndex = 1;
		else if (durum === 2)
			document.getElementById("islem").selectedIndex = 2;
		else if (durum === 3)
			document.getElementById("islem").selectedIndex = 3;
		document.getElementById("errorDiv").style.display = "none";
		document.getElementById("errorDiv").innerText = "";
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

async function cekdurumKayit() {
	if (document.getElementById("cekno").innerText.trim() === "") {
		return;
	}
	const cekno = document.getElementById("cekno").innerText;
	const durum = document.getElementById("islem").selectedIndex;
	const ttarih = document.getElementById("ttarih").value;
	document.body.style.cursor = "wait";
	try {
		const response = await fetchWithSessionCheck("kambiyo/cektakipkaydet", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ cekno: cekno, durum: durum, ttarih: ttarih }),
		});
		if (response.errorMessage.trim() !== "") {
			throw new Error(response.errorMessage);
		}
		clearform();
		document.getElementById("arama").value = "";
		document.getElementById("errorDiv").style.display = "none";
		document.getElementById("errorDiv").innerText = "";
	} catch (error) {
		document.getElementById("errorDiv").style.display = "block";
		document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
	} finally {
		document.body.style.cursor = "default";
	}
}

function clearform() {
	document.getElementById("cekno").innerText = "";
	document.getElementById("banka").innerText = "";
	document.getElementById("cikisbordro").innerText = "";
	document.getElementById("cikishesapkod").innerText = "";
	document.getElementById("cikistarihi").innerText = "";
	document.getElementById("vade").innerText = "";
	document.getElementById("girisbordro").innerText = "";
	document.getElementById("girishesapkod").innerText = "";
	document.getElementById("giristarihi").innerText = "";
	document.getElementById("sube").innerText = "";
	document.getElementById("serino").innerText = "";
	document.getElementById("ilkborclu").innerText = "";
	document.getElementById("cekhesapno").innerText = "";
	document.getElementById("tutar").innerText = formatNumber2(0);
	document.getElementById("gozelkod").innerText = "";
	document.getElementById("cozelkod").innerText = "";
	const today = new Date();
	const formattedDate = today.toISOString().split('T')[0];
	document.getElementById("ttarih").value = formattedDate;
	document.getElementById("islem").selectedIndex = 0;
}