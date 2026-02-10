function tarihGeri() {
    const tarih = document.getElementById("tarih").value;
    const date = new Date(tarih);
    date.setDate(date.getDate() - 1);
    const formattedDate = date.toISOString().split('T')[0];
    document.getElementById("tarih").value = formattedDate;
    belgeoku();
}

function tarihIleri() {
    const tarih = document.getElementById("tarih").value;
    const date = new Date(tarih);
    date.setDate(date.getDate() + 1);
    const formattedDate = date.toISOString().split('T')[0];
    document.getElementById("tarih").value = formattedDate;
    belgeoku();
}

function gunlukTarih() {
    const date = new Date();
    const formattedDate = date.toISOString().split('T')[0];
    document.getElementById("tarih").value = formattedDate;
    belgeoku();
}

function checkEnter(event) {
    if (event.key === "Enter" || event.keyCode === 13) {
        belgeoku();
    }
}

async function belgeoku() {
    const tarih = document.getElementById("tarih").value;
    const kodu = document.getElementById("tcheskod").value;
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    if (!kodu) {
        errorDiv.style.display = "block";
        errorDiv.innerText = "Lütfen tüm alanları doldurun.";
        return;
    }
    const tableBody = document.getElementById("tableBody");
    tableBody.innerHTML = "";
    document.getElementById("gborc").textContent = formatNumber2(0);
    document.getElementById("galacak").textContent = formatNumber2(0);
    document.getElementById("gbakiye").textContent = formatNumber2(0);
    document.getElementById("eborc").textContent = formatNumber2(0);
    document.getElementById("ealacak").textContent = formatNumber2(0);
    document.getElementById("ebakiye").textContent = formatNumber2(0);
    document.getElementById("bborc").textContent = formatNumber2(0);
    document.getElementById("balacak").textContent = formatNumber2(0);
    document.getElementById("bbakiye").textContent = formatNumber2(0);

    document.body.style.cursor = "wait";
    try {
        const data = await fetchWithSessionCheck("cari/gunluktakip", {
            method: "POST",
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({ tarih: tarih, kodu: kodu }),
        });
        if (data.errorMessage) {
            errorDiv.style.display = "block";
            errorDiv.innerText = data.errorMessage;
            return;
        }
        let totalBorc = 0;
        let totalAlacak = 0;
        if (Array.isArray(data.data) && data.data.length > 0) {

            data.data.forEach(item => {
                const row = document.createElement("tr");
                row.classList.add("table-row-height");
                row.innerHTML = `
						<td>${item.EVRAK || ''}</td>
						<td>${item.IZAHAT || ''}</td>
						<td>${item.KOD || ''}</td>
						<td class="double-column">${formatNumber2(item.BORC)}</td>
						<td class="double-column">${formatNumber2(item.ALACAK)}</td>
						<td>${item.USER || ''}</td>
					`;
                tableBody.appendChild(row);
                totalBorc += item.BORC || 0;
                totalAlacak += item.ALACAK || 0;
            });
        }
        document.getElementById("gborc").textContent = formatNumber2(totalBorc);
        document.getElementById("galacak").textContent = formatNumber2(totalAlacak);
        document.getElementById("gbakiye").textContent = formatNumber2(totalAlacak - totalBorc);
        data.onceki.forEach(item => {
            document.getElementById("eborc").textContent = formatNumber2(item.islem);
            document.getElementById("ealacak").textContent = formatNumber2(item.islem2);
            document.getElementById("ebakiye").textContent = formatNumber2(item.islem2 - item.islem);

            document.getElementById("bborc").textContent = formatNumber2(totalBorc + item.islem);
            document.getElementById("balacak").textContent = formatNumber2(totalAlacak + item.islem2);
            document.getElementById("bbakiye").textContent = formatNumber2((totalAlacak + item.islem2) - (totalBorc + item.islem));
        });

    } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = error.message;
    } finally {
        document.body.style.cursor = "default";
    }
}