pageSize = 250;

async function anagrpChanged(anagrpElement) {
  const anagrup = anagrpElement.value;
  const errorDiv = document.getElementById("errorDiv");
  const selectElement = document.getElementById("altgrp");
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

function ilksayfa() {
  irsfetchTableData(0);
}

function oncekisayfa() {
  if (currentPage > 0) {
    irsfetchTableData(currentPage - 1);
  }
}

function sonrakisayfa() {
  if (currentPage < totalPages - 1) {
    irsfetchTableData(currentPage + 1);
  }
}

async function sonsayfa() {
  irsfetchTableData(totalPages - 1);
}

async function toplampagesize() {
  try {
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    const fatraporDTO = getirsraporDTO();
    const response = await fetchWithSessionCheck("stok/irsdoldursize", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(fatraporDTO),
    });
    const totalRecords = response.totalRecords;
    totalPages = Math.ceil(totalRecords / pageSize);
  } catch (error) {
    errorDiv.style.display = "block";
    errorDiv.innerText = error;
    document.body.style.cursor = "default";
  }
}

async function irsdoldur() {
  document.body.style.cursor = "wait";
  toplampagesize();
  irsfetchTableData(0);
}

function getirsraporDTO() {
  const hiddenFieldValue = $('#irsrapBilgi').val();
  const parsedValues = hiddenFieldValue.split(",");
  return {
    irsno1: parsedValues[0],
    irsno2: parsedValues[1],
    anagrp: parsedValues[2],
    tar1: parsedValues[3],
    tar2: parsedValues[4],
    altgrp: parsedValues[5],
    ckod1: parsedValues[6],
    ckod2: parsedValues[7],
    turu: parsedValues[8],
    ukod1: parsedValues[9],
    ukod2: parsedValues[10],
    okod1: parsedValues[11],
    okod2: parsedValues[12],
		dvz1: parsedValues[13],
		dvz2: parsedValues[14],
    fatno1: parsedValues[15],
    fatno2: parsedValues[16],
		adr1: parsedValues[17],
		adr2: parsedValues[18]
  };
}

async function irsfetchTableData(page) {
  const fatraporDTO = getirsraporDTO();
  fatraporDTO.page = page;
  fatraporDTO.pageSize = pageSize;
  currentPage = page;

  const errorDiv = document.getElementById("errorDiv");
  errorDiv.style.display = "none";
  errorDiv.innerText = "";

  document.body.style.cursor = "wait";
  const $yenileButton = $('#irsrapyenileButton');
  $yenileButton.prop('disabled', true).text('İşleniyor...');
  const mainTableBody = document.getElementById("mainTableBody");
  mainTableBody.innerHTML = "";

  clearTfoot();

  try {
    const response = await fetchWithSessionCheck("stok/irsrapdoldur", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(fatraporDTO),
    });
    if (response.errorMessage) {
      throw new Error(response.errorMessage);
    }
    data = response;
    let sqlHeaders = "";
    
    sqlHeaders = ["", "IRSALIYE NO", "HAREKET", "TARIH", "CARI_HESAP", "ADRES","BIRIM", "MIKTAR", "TUTAR"];
    updateTableHeadersfno(sqlHeaders);
   
    let totalmiktar = 0;
    let totaltutar = 0;
    data.data.forEach(rowData => {
      const row = document.createElement('tr');
      row.classList.add('expandable');
      row.classList.add("table-row-height");
      row.innerHTML = `
                    <td class="toggle-button">+</td>
                    <td>${rowData.Irsaliye_No || ''}</td>
                    <td>${rowData.Hareket || ''}</td>
                    <td>${formatDate(rowData.Tarih)}</td>
                    <td>${rowData.Cari_Hesap_Kodu || ''}</td>
										<td>${rowData.Adres_Firma || ''}</td>
                    <td>${rowData.Birim || ''}</td>
                    <td class="double-column">${formatNumber3(rowData.Miktar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
                `;
      totalmiktar += rowData.Miktar;
      totaltutar += rowData.Tutar;
      
      mainTableBody.appendChild(row);
      const detailsRow = document.createElement('tr');
      detailsRow.classList.add('details-row');
      detailsRow.innerHTML = `<td colspan="24"></td>`;
      mainTableBody.appendChild(detailsRow);
    
        row.addEventListener('click', async () => {
          const toggleButton = row.querySelector('.toggle-button');
          const isVisible = detailsRow.style.display === 'table-row';
          detailsRow.style.display = isVisible ? 'none' : 'table-row';
          toggleButton.textContent = isVisible ? '+' : '-';
          document.body.style.cursor = "wait";
          if (!isVisible) {
            try {
              const details = await fetchDetails(rowData.Irsaliye_No, rowData.Hareket);
              const data = details.data;
              let detailsTable = `
                                <table class="details-table table table-bordered table-hover">
                                    <thead class="thead-dark">
                                        <tr>
                                            <th>Kodu</th>
                                            <th>Adi</th>
                                            <th style="text-align: right;">Miktar</th>
                                            <th>Birim</th>
                                            <th style="text-align: right;">Fiat</th>
                                            <th style="text-align: right;">Tutar</th>
																						<th style="text-align: right;">Kdv</th>
																						<th>Doviz</th>
                                            <th style="text-align: right;">Iskonto</th>
                                            <th>Ana Grup</th>
                                            <th>Alt Grup</th>
                                            <th>Ozel Kod</th>
                                            <th>Fatura No</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                            `;
              data.forEach(item => {
                detailsTable += `
                                    <tr>
                                        <td style="min-width:80px;">${item.Kodu || ''}</td>
                                        <td style="min-width:250px;">${item.Adi || ''}</td>
                                        <td style="text-align: right;min-width:80px;">${formatNumber3(item.Miktar)}</td>
                                        <td>${item.Birim || ''}</td>
                                        <td style="text-align: right;min-width:80px;">${formatNumber2(item.Fiat)}</td>
                                        <td style="text-align: right;min-width:80px;">${formatNumber2(item.Tutar)}</td>
																				<td style="text-align: right;">${formatNumber2(item.Kdv)}</td>
																				<td>${item.Doviz || ''}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Iskonto)}</td>
                                        <td >${item.Ana_Grup}</td>
                                        <td >${item.Alt_Grup}</td>
                                        <td >${item.Ozel_Kod}</td>
                                        <td >${item.Fatura_No}</td>
                                    </tr>
                                `;
              });
              detailsTable += `
                                    </tbody>
                                </table>
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
    
    });
   
    document.getElementById("toplam-7").innerText = formatNumber3(totalmiktar);
    document.getElementById("toplam-8").innerText = formatNumber2(totaltutar);
    document.body.style.cursor = "default";
  } catch (error) {
    errorDiv.style.display = "block";
    errorDiv.innerText = error;
  } finally {
    $yenileButton.prop('disabled', false).text('Yenile');
    document.body.style.cursor = "default";
  }
}

function clearTfoot() {
  let table = document.querySelector("#main-table");
  let tfoot = table.querySelector("tfoot");
  if (tfoot) {
    let cells = tfoot.querySelectorAll("th");
    for (let i = 0; i < cells.length; i++) {
      cells[i].textContent = "";
    }
  }
}

async function fetchDetails(evrakNo, cins) {
  try {
    let gircik = "";
    if (cins === "Alis") {
      gircik = "G";
    } else {
      gircik = "C";
    }
    const response = await fetchWithSessionCheck("stok/irsdetay", {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({ evrakNo: evrakNo, gircik: gircik }),
    });
    data = await response;
    return await response;
  } catch (error) {
    throw error;
  }
}

function updateTableHeadersfno(headers) {
  let thead = document.querySelector("#main-table thead");
  let table = document.querySelector("#main-table");
  let tfoot = table.querySelector("tfoot");
  if (!tfoot) {
    tfoot = document.createElement("tfoot");
    table.appendChild(tfoot);
  }
  thead.innerHTML = "";
  let trHead = document.createElement("tr");
  trHead.classList.add("thead-dark");
  headers.forEach((header, index) => {
    let th = document.createElement("th");
    th.textContent = header;

    if (index >= headers.length - 2) {
      th.classList.add("double-column");
    }
		
    trHead.appendChild(th);
  });
  thead.appendChild(trHead);
  tfoot.innerHTML = "";
  let trFoot = document.createElement("tr");
  headers.forEach((_, index) => {
    let th = document.createElement("th");
    if (index === 7) {
      th.textContent = "0.000";
      th.id = "toplam-" + index;
      th.classList.add("double-column");
    } else if (index === 8) {
      th.textContent = "0.00";
      th.id = "toplam-" + index;
      th.classList.add("double-column");
    } else {
      th.textContent = "";
    }
    trFoot.appendChild(th);
  });
  tfoot.appendChild(trFoot);
}



async function openirsrapModal(modal) {
  $(modal).modal('show');
  document.body.style.cursor = "wait";
  try {
    const response = await fetchWithSessionCheck("stok/anadepo", {
      method: "POST",
      headers: { "Content-Type": "application/json" }
    });
    if (response.errorMessage) throw new Error(response.errorMessage);
    const anaSelect = document.getElementById("anagrp");
    anaSelect.innerHTML = "";
    anaSelect.add(new Option("", ""));
    anaSelect.add(new Option("Bos Olanlar", "Bos Olanlar"));
    const seen = new Set();
    for (const it of (response.anaKodlari || [])) {
      const v = (it.ANA_GRUP || "").trim();
      if (!v || seen.has(v)) continue;
      seen.add(v);
      anaSelect.add(new Option(v, v));
    }
  } catch (error) {
    const modalError = document.getElementById("errorDiv");
    modalError.style.display = "block";
    modalError.innerText = `Bir hata oluştu: ${error.message}`;
  } finally {
    document.body.style.cursor = "default";
  }
}


async function irsrapdownloadReport() {
  const errorDiv = document.getElementById("errorDiv");
  errorDiv.style.display = "none";
  errorDiv.innerText = "";

  document.body.style.cursor = "wait";
  const $indirButton = $('#indirButton');
  $indirButton.prop('disabled', true).text('İşleniyor...');
  const $yenileButton = $('#yenileButton');
  $yenileButton.prop('disabled', true);

  let table = document.querySelector("#main-table");
  let headers = [];
  let rows = [];
  table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));
  table.querySelectorAll("tbody tr").forEach(tr => {
    let rowData = {};
    let isEmpty = true;
    tr.querySelectorAll("td").forEach((td, index) => {
      let value = td.innerText.trim();
      if (value !== "") {
        isEmpty = false;
      }
      rowData[headers[index]] = value;
    });
    if (!isEmpty) {
      rows.push(rowData);
    }
  });
  try {
    const response = await fetchWithSessionCheckForDownload('stok/irsrap_download', {
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

async function irsrapmailAt() {
  localStorage.removeItem("tableData");
  localStorage.removeItem("grprapor");
  localStorage.removeItem("tablobaslik");
  document.body.style.cursor = "wait";
  let table = document.querySelector("#main-table");
  let headers = [];
  let rows = [];
  table.querySelectorAll("thead th").forEach(th => headers.push(th.innerText.trim()));
  table.querySelectorAll("tbody tr").forEach(tr => {
    let rowData = {};
    let isEmpty = true;
    tr.querySelectorAll("td").forEach((td, index) => {
      let value = td.innerText.trim();
      if (value !== "") {
        isEmpty = false;
      }
      rowData[headers[index]] = value;
    });
    if (!isEmpty) {
      rows.push(rowData);
    }
  });
  localStorage.setItem("tableData", JSON.stringify({ rows: rows }));
  const degerler = "irsrapor";
  const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
  mailsayfasiYukle(url);
}