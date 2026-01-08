async function fetchDetails(evrakNo, cins) {
	try {
		let tah_ted = 0;
		if (cins === "Tahsilat") {
			tah_ted = 0;
		} else {
			tah_ted = 1;
		}
		const response = await fetchWithSessionCheck("cari/tahcekdokum", {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({ evrakNo: evrakNo, tah_ted: tah_ted }),
		});
		data = await response;
		if (!data.success) {
			throw new Error(`Hata: ${data.errorMessage}`);
		}
		return await response;
	} catch (error) {
		throw error;
	}
}

async function tahrapfetchTableData() {
  const errorDiv = document.getElementById("errorDiv");
  errorDiv.style.display = "none";
  errorDiv.innerText = "";

  const hiddenFieldValue = $('#tahrapBilgi').val() || "";
  const parsedValues = hiddenFieldValue.split(",");

  const tahrapDTO = {
    tah_ted:   parsedValues[0] || "",
    hangi_tur: parsedValues[1] || "",
    pos:       parsedValues[2] || "",
    hkodu1:    parsedValues[3] || "",
    hkodu2:    parsedValues[4] || "",
    startDate: parsedValues[5] || "",
    endDate:   parsedValues[6] || "",
    evrak1:    parsedValues[7] || "",
    evrak2:    parsedValues[8] || ""
  };

  document.body.style.cursor = "wait";
  const $btn = $('#tahrapyenileButton');
  $btn.prop('disabled', true).text('İşleniyor...');

  const body = document.getElementById("mainTableBody");
  body.innerHTML = "";

  const thCount = document.querySelectorAll("#main-table thead th").length;

  try {
    const response = await fetchWithSessionCheck("cari/tahrapdoldur", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(tahrapDTO),
    });

    const res = response;

    if (!res || !res.success) {
      errorDiv.style.display = "block";
      errorDiv.innerText = (res && res.errorMessage) ? res.errorMessage : "Bir hata oluştu.";
      return;
    }

    (res.data || []).forEach(rowData => {
      const hasDetails = (rowData.TUR === "Cek");

      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td><span class="toggle-button">${hasDetails ? "+" : ""}</span></td>
        <td>${rowData.EVRAK || ""}</td>
        <td>${formatDate(rowData.TARIH)}</td>
        <td>${rowData.CARI_HESAP || ""}</td>
        <td>${rowData.UNVAN || ""}</td>
        <td>${rowData.ADRES_HESAP || ""}</td>
        <td>${rowData.ADRES_UNVAN || ""}</td>
        <td>${rowData.CINS || ""}</td>
        <td>${rowData.TUR || ""}</td>
        <td>${rowData.POS_BANKA || ""}</td>
        <td>${rowData.DVZ_CINS || ""}</td>
        <td class="double-column">${formatNumber2(rowData.TUTAR)}</td>
      `;
      body.appendChild(tr);

      const detailsTr = document.createElement("tr");
      detailsTr.className = "details-row";
      detailsTr.style.display = "none";
      detailsTr.innerHTML = `<td colspan="${thCount}"></td>`;
      body.appendChild(detailsTr);

      if (!hasDetails) return;

      const toggle = tr.querySelector(".toggle-button");

      toggle.addEventListener("click", async (e) => {
        e.stopPropagation();

        const isOpen = (detailsTr.style.display === "table-row");

        // ✅ tek satır açık kalsın + temizle
        document.querySelectorAll("#main-table tr.details-row").forEach(r => r.style.display = "none");
        document.querySelectorAll("#main-table tbody tr").forEach(r => r.classList.remove("selected"));
        document.querySelectorAll("#main-table .toggle-button").forEach(x => {
          if (x.textContent === "-") x.textContent = "+";
        });

        if (isOpen) {
          detailsTr.style.display = "none";
          toggle.textContent = "+";
          return;
        }

        // ✅ seçimi sadece ana satıra uygula
        tr.classList.add("selected");
        detailsTr.style.display = "table-row";
        toggle.textContent = "-";

        // daha önce yüklendiyse tekrar fetch yapma
        if (detailsTr.dataset.loaded === "1") return;

        document.body.style.cursor = "wait";
        try {
          const detResp = await fetchDetails(rowData.EVRAK, rowData.CINS);
          const det = (detResp && detResp.data) ? detResp.data : [];

          // ✅ KESİN GENİŞLİK: colgroup + fixed layout + min-width
          let html = `
            <div class="details-wrap">
              <table class="t-details" style="table-layout:fixed; width:100%; min-width:1200px;">
                <colgroup>
                  <col style="width:150px;">  <!-- BANKA -->
                  <col style="width:150px;">  <!-- SUBE -->
                  <col style="width:120px;">  <!-- SERI -->
                  <col style="width:140px;">  <!-- HESAP -->
                  <col style="width:150px;">  <!-- BORCLU -->
                  <col style="width:70px;">  <!-- TARIH -->
                  <col style="width:100px;">  <!-- TUTAR -->
                </colgroup>
                <thead>
                  <tr>
                    <th>BANKA</th>
                    <th>SUBE</th>
                    <th>SERI</th>
                    <th>HESAP</th>
                    <th>BORCLU</th>
                    <th>TARIH</th>
                    <th class="double-column">TUTAR</th>
                  </tr>
                </thead>
                <tbody>
          `;

          det.forEach(item => {
            html += `
              <tr>
                <td>${item.BANKA || ""}</td>
                <td>${item.SUBE || ""}</td>
                <td>${item.SERI || ""}</td>
                <td>${item.HESAP || ""}</td>
                <td>${item.BORCLU || ""}</td>
                <td>${formatDate(item.TARIH)}</td>
                <td class="double-column">${formatNumber2(item.TUTAR)}</td>
              </tr>
            `;
          });

          html += `
                </tbody>
              </table>
            </div>
          `;

          detailsTr.children[0].innerHTML = html;
          detailsTr.dataset.loaded = "1";

        } catch (err) {
          detailsTr.children[0].innerHTML =
            `<div class="details-wrap"><b>Hata:</b> Detaylar alınamadı.</div>`;
        } finally {
          document.body.style.cursor = "default";
        }
      });
    });

  } catch (err) {
    errorDiv.style.display = "block";
    errorDiv.innerText = err;
  } finally {
    $btn.prop('disabled', false).text('Filtre');
    document.body.style.cursor = "default";
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
		const data = result.data; 
		const posSelect = document.getElementById("pos");
		posSelect.innerHTML = ""; 
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

async function tahrapdownloadReport() {
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    document.body.style.cursor = "wait";
    const $indirButton = $('#tahrapreportFormat');
    $indirButton.prop('disabled', true).text('İşleniyor...');
    const $yenileButton = $('#tahrapyenileButton');
    $yenileButton.prop('disabled', true);
    let rows = extractTableData("main-table");
    try {
        const response = await fetchWithSessionCheckForDownload('cari/tahrap_download', {
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

async function tahrapmailAt() {
    document.body.style.cursor = "wait";
	localStorage.removeItem("tableData");
	localStorage.removeItem("grprapor");
	localStorage.removeItem("tablobaslik");
    
	let rows = extractTableData("main-table");
    localStorage.setItem("tableData", JSON.stringify({ rows: rows }));
    const degerler = "tahrap";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);
}

function extractTableData(tableId) {
    let table = document.querySelector(`#${tableId}`);
    let headers = [];
    let rows = [];
    table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));
    table.querySelectorAll("tbody tr").forEach(tr => {
        let rowData = {};
        let nonEmptyCount = 0;
        tr.querySelectorAll("td").forEach((td, index) => {
            let value = td.innerText.trim();
            if (value !== "") {
                nonEmptyCount++;
            }
            rowData[headers[index]] = value;
        });
        if (nonEmptyCount > 0) {
            rows.push(rowData);
        }
    });
    let tfoot = table.querySelector("tfoot");
    if (tfoot) {
        let tfootRowData = {};
        let nonEmptyCount = 0;
        tfoot.querySelectorAll("th").forEach((th, index) => {
            let value = th.innerText.trim();
            if (value !== "") {
                nonEmptyCount++;
            }
            tfootRowData[headers[index]] = value;
        });
        if (nonEmptyCount > 0) {
            rows.push(tfootRowData);
        }
    }
    return rows;
}