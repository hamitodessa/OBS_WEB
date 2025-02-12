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
	const hiddenFieldValue = $('#tahrapBilgi').val();
	const parsedValues = hiddenFieldValue.split(","); 
	const tah_ted = parsedValues[0];
	const hangi_tur = parsedValues[1];
	const pos = parsedValues[2];
	const hkodu1 = parsedValues[3];
	const hkodu2 = parsedValues[4];
	const startDate = parsedValues[5];
	const endDate = parsedValues[6];
	const evrak1 = parsedValues[7];
	const evrak2 = parsedValues[8];

	const tahrapDTO = {
		tah_ted: tah_ted,
		hangi_tur: hangi_tur,
		pos: pos,
		hkodu1: hkodu1,
		hkodu2: hkodu2,
		startDate: startDate,
		endDate: endDate,
		evrak1: evrak1,
		evrak2: evrak2
	}
	const errorDiv = document.getElementById("errorDiv");
	document.body.style.cursor = "wait";
	const $yenileButton = $('#tahrapyenileButton');
	$yenileButton.prop('disabled', true).text('İşleniyor...');
	const mainTableBody = document.getElementById("mainTableBody");
	mainTableBody.innerHTML = "";
	try {
		const response = await fetchWithSessionCheck("cari/tahrapdoldur", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(tahrapDTO),
		});
		data = response;
		if (data.success) {
			data.data.forEach(rowData => {
				const row = document.createElement('tr');
				row.classList.add('expandable');
				row.classList.add("table-row-height");
				row.innerHTML = `
					<td class="toggle-button">
			       		${rowData.TUR === "Cek" ? "+" : ''}
			   		</td>
		            <td>${rowData.EVRAK || ''}</td>
		            <td>${formatDate(rowData.TARIH)}</td>
		            <td>${rowData.CARI_HESAP || ''}</td>
		            <td>${rowData.UNVAN || ''}</td>
		            <td>${rowData.ADRES_HESAP || ''}</td>
		            <td>${rowData.ADRES_UNVAN || ''}</td>
		            <td>${rowData.CINS || ''}</td>
		            <td>${rowData.TUR || ''}</td>
		            <td>${rowData.POS_BANKA || ''}</td>
		            <td>${rowData.DVZ_CINS || ''}</td>
		            <td class="double-column">${formatNumber2(rowData.TUTAR)}</td>
		        `;
				mainTableBody.appendChild(row);
				const detailsRow = document.createElement('tr');
				detailsRow.classList.add('details-row');
				detailsRow.innerHTML = `<td colspan="12"></td>`;
				mainTableBody.appendChild(detailsRow);
				if (rowData.TUR === "Cek") {
					row.addEventListener('click', async () => {
						const toggleButton = row.querySelector('.toggle-button');
						const isVisible = detailsRow.style.display === 'table-row';
						detailsRow.style.display = isVisible ? 'none' : 'table-row';
						toggleButton.textContent = isVisible ? '+' : '-';
						document.body.style.cursor = "wait";
						if (!isVisible) {
							try {
								const details = await fetchDetails(rowData.EVRAK, rowData.CINS);
								const data = details.data;
								let detailsTable = `
							<div class="table-container" >
                            <table class="details-table table table-bordered table-hover">
                                <thead class="thead-dark">
                                    <tr>
                                        <th>BANKA</th>
                                        <th>SUBE</th>
                                        <th>SERI</th>
                                        <th>HESAP</th>
										<th>BORCLU</th>
										<th>TARIH</th>
										<th style="text-align: right;">TUTAR</th>
                                    </tr>
                                </thead>
                                <tbody>
                        `;
								data.forEach(item => {
									detailsTable += `
                                <tr>
                                    <td>${item.BANKA || ''}</td>
                                    <td>${item.SUBE || ''}</td>
                                    <td>${item.SERI || ''}</td>
                                    <td>${item.HESAP || ''}</td>
									<td>${item.BORCLU || ''}</td>
									<td>${formatDate(item.TARIH)}</td>
									<td style="text-align: right;">${formatNumber2(item.TUTAR)}</td>
                                </tr>
                            `;
								});
								detailsTable += `
                                </tbody>
                            </table>
							</div>
                        `;
								detailsRow.children[0].classList.add("table-row-height");
								detailsRow.children[0].innerHTML = detailsTable;
								document.body.style.cursor = "default";
							} catch (error) {
								detailsRow.children[0].innerHTML = `
                            <strong>Hata:</strong> Detay bilgileri alınamadı.
                        `;
								document.body.style.cursor = "default";
							}
						}
						document.body.style.cursor = "default";
					});
				}
			});
		} else {
			errorDiv.style.display = "block";
			errorDiv.innerText = data.errorMessage || "Bir hata oluştu.";
		}
		document.body.style.cursor = "default";
	} catch (error) {
		errorDiv.style.display = "block";
		errorDiv.innerText = error;
	} finally {
		$yenileButton.prop('disabled', false).text('Yenile');
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