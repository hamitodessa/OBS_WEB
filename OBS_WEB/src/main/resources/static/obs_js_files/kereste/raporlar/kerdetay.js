currentPage = 0;
totalPages = 0;
pageSize = 250;

function setDisabled(el, yes) { el.disabled = !!yes; }
function updatePaginationUI(disableAllWhileLoading = false) {
  const first = document.getElementById("ilksayfa");
  const prev  = document.getElementById("oncekisayfa");
  const next  = document.getElementById("sonrakisayfa");
  const last  = document.getElementById("sonsayfa");

  if (disableAllWhileLoading) {
    setDisabled(first, true); setDisabled(prev, true);
    setDisabled(next, true);  setDisabled(last, true);
    return;
  }
  const noData = totalPages === 0;
  setDisabled(first, noData || currentPage <= 0);
  setDisabled(prev,  noData || currentPage <= 0);
  setDisabled(next,  noData || currentPage >= totalPages - 1);
  setDisabled(last,  noData || currentPage >= totalPages - 1);
}

// Buton clickleri (guard'lı)
function ilksayfa()     { if (currentPage > 0)            kerestedetayfetchTableData(0); }
function oncekisayfa()  { if (currentPage > 0)            kerestedetayfetchTableData(currentPage - 1); }
function sonrakisayfa() { if (currentPage < totalPages-1) kerestedetayfetchTableData(currentPage + 1); }
function sonsayfa()     { if (totalPages > 0)             kerestedetayfetchTableData(totalPages - 1); }

async function toplampagesize() { 
  const errorDiv = document.getElementById("errorDiv");
  try {
    errorDiv.style.display = "none";
    errorDiv.innerText = "";

    const dto = getKeresteDetayRaporDTO();
    const response = await fetchWithSessionCheck("kereste/kerestedetaydoldursize", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dto),
    });

    const totalRecords = response?.totalRecords ?? 0;
    totalPages = Math.max(0, Math.ceil(totalRecords / pageSize));
  } catch (err) {
    totalPages = 0;
    errorDiv.style.display = "block";
    errorDiv.innerText = err?.message || err || "Beklenmeyen bir hata oluştu.";
  } finally {
    updatePaginationUI();
  }
}

async function kerestedetaydoldur() {
  document.body.style.cursor = "wait";
  updatePaginationUI(true);                 // yükleme sırasında kilitle
  await toplampagesize();                   // <-- ÖNEMLİ: bekle
  await kerestedetayfetchTableData(0);
  document.body.style.cursor = "default";
}

async function kerestedetayfetchTableData(page) {
  const kerestedetayraporDTO = { ...getKeresteDetayRaporDTO(), page, pageSize };

  const errorDiv = document.getElementById("errorDiv");
  errorDiv.style.display = "none";
  errorDiv.innerText = "";

  document.body.style.cursor = "wait";
  updatePaginationUI(true);                 // istek sırasında butonları kapat

  const $yenileButton = $('#kerestedetayyenileButton');
  $yenileButton.prop('disabled', true).text('İşleniyor...');

  const mainTableBody = document.getElementById("tbody");
  mainTableBody.innerHTML = "";

  currentPage = page;

  try {
    const response = await fetchWithSessionCheck("kereste/kerestedetaydoldur", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(kerestedetayraporDTO),
    });

    if (response.errorMessage) throw new Error(response.errorMessage);

    const data = response.data || [];
    data.forEach(rowData => {
      const row = document.createElement('tr');
      row.classList.add("table-row-height");
      row.innerHTML = `
        <td>${rowData.Evrak_No}</td>
        <td>${rowData.Barkod}</td>
        <td>${rowData.Kodu}</td>
        <td>${rowData.Paket_No}</td>
        <td>${rowData.Konsimento}</td>
        <td class="double-column">${formatNumber0(rowData.Miktar)}</td>
        <td class="double-column">${formatNumber3(rowData.m3)}</td>
        <td>${formatDate(rowData.Tarih)}</td>
        <td class="double-column">${formatNumber2(rowData.Kdv)}</td>
        <td>${rowData.Doviz}</td>
        <td class="double-column">${formatNumber2(rowData.Fiat)}</td>
        <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
        <td class="double-column">${formatNumber2(rowData.Kur)}</td>
        <td>${rowData.Cari_Firma}</td>
        <td>${rowData.Adres_Firma}</td>
        <td class="double-column">${formatNumber2(rowData.Iskonto)}</td>
        <td class="double-column">${formatNumber2(rowData.Tevkifat)}</td>
        <td>${rowData.Ana_Grup}</td>
        <td>${rowData.Alt_Grup}</td>
        <td>${rowData.Mensei}</td>
        <td>${rowData.Depo}</td>
        <td>${rowData.Ozel_Kod}</td>
        <td>${rowData.Izahat}</td>
        <td>${rowData.Nakliyeci}</td>
        <td>${rowData.USER}</td>
        <td>${rowData.Cikis_Evrak}</td>
        <td>${rowData.CTarih}</td>
        <td class="double-column">${formatNumber2(rowData.CKdv)}</td>
        <td>${rowData.CDoviz}</td>
        <td class="double-column">${formatNumber2(rowData.CFiat)}</td>
        <td class="double-column">${formatNumber2(rowData.CTutar)}</td>
        <td class="double-column">${formatNumber2(rowData.CKur)}</td>
        <td>${rowData.CCari_Firma}</td>
        <td>${rowData.CAdres_Firma}</td>
        <td class="double-column">${formatNumber2(rowData.CIskonto)}</td>
        <td class="double-column">${formatNumber2(rowData.CTevkifat)}</td>
        <td>${rowData.C_Ana_Grup}</td>
        <td>${rowData.C_Alt_Grup}</td>
        <td>${rowData.C_Depo}</td>
        <td>${rowData.COzel_Kod}</td>
        <td>${rowData.CIzahat}</td>
        <td>${rowData.C_Nakliyeci}</td>
        <td>${rowData.CUSER}</td>
      `;
      mainTableBody.appendChild(row);
    });
  } catch (error) {
    errorDiv.style.display = "block";
    errorDiv.innerText = error?.message || "Beklenmeyen bir hata oluştu.";
  } finally {
    $yenileButton.prop('disabled', false).text('Yenile');
    document.body.style.cursor = "default";
    updatePaginationUI();                  // sayfa sınırlarına göre aç/kapat
  }
}
function getKeresteDetayRaporDTO() {
    const hiddenFieldValue = $('#kerestedetayBilgi').val();
    const parsedValues = hiddenFieldValue.split(",");
    return {
        gtar1: parsedValues[0],
        gtar2: parsedValues[1],
        ctar1: parsedValues[2],
        ctar2: parsedValues[3],
        ukodu1: parsedValues[4],
        ukodu2: parsedValues[5],
        cfirma1: parsedValues[6],
        cfirma2: parsedValues[7],
        pak1: parsedValues[8],
        pak2: parsedValues[9],
        cevr1: parsedValues[10],
        cevr2: parsedValues[11],
        gfirma1: parsedValues[12],
        gfirma2: parsedValues[13],
        cana: parsedValues[14],
        evr1: parsedValues[15],
        evr2: parsedValues[16],
        calt: parsedValues[17],
        gana: parsedValues[18],
        galt: parsedValues[19],
        gdepo: parsedValues[20],
        gozkod: parsedValues[21],
        kons1: parsedValues[22],
        kons2: parsedValues[23],
        cozkod: parsedValues[24],
        cdepo: parsedValues[25]
    };
}


async function kerestedetaydownloadReport() {
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";

    document.body.style.cursor = "wait";
    const $indirButton = $('#kerestedetayreportDownload');
    $indirButton.prop('disabled', true).text('İşleniyor...');
    const $yenileButton = $('#keresteyenileButton');
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
        const response = await fetchWithSessionCheckForDownload('kereste/kerdetay_download', {
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

async function kerestedetaymailAt() {
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
    const degerler = "kerdetay";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);
}

async function anagrpChanged(anagrpElement, altgrpElement) {
    const anagrup = anagrpElement.value;
    const errorDiv = document.getElementById("errorDiv");
    const selectElement = document.getElementById(altgrpElement);
    selectElement.innerHTML = '';
    if (anagrup === "") {
        selectElement.disabled = true;
        return;
    }
    document.body.style.cursor = "wait";
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    try {
        const response = await fetchWithSessionCheck("kereste/altgrup", {
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

async function openenvModal(modal) {
    $(modal).modal('show');
    document.body.style.cursor = "wait";
    try {
        const response = await fetchWithSessionCheck("kereste/anadepo", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            }
        });
        if (response.errorMessage) {
            throw new Error(response.errorMessage);
        }
        const ana = response.anaKodlari;
        const dpo = response.depoKodlari;
        const oz = response.oz1Kodlari;
        const anaSelect = document.getElementById("anagrp");
        const canaSelect = document.getElementById("canagrp");
        const dpoSelect = document.getElementById("depo");
        const cdpoSelect = document.getElementById("cdepo");
        const ozSelect = document.getElementById("ozkod");
        const cozSelect = document.getElementById("cozkod");
        anaSelect.innerHTML = "";
        canaSelect.innerHTML = "";
        dpoSelect.innerHTML = "";
        cdpoSelect.innerHTML = "";
        ozSelect.innerHTML = "";
        cozSelect.innerHTML = "";
        ana.forEach(item => {
            const optionAna = document.createElement("option");
            optionAna.value = item.ANA_GRUP;
            optionAna.textContent = item.ANA_GRUP;
            anaSelect.appendChild(optionAna);
            const optionUrana = document.createElement("option");
            optionUrana.value = item.ANA_GRUP;
            optionUrana.textContent = item.ANA_GRUP;
            canaSelect.appendChild(optionUrana);
        });
        dpo.forEach(item => {
            const option = document.createElement("option");
            option.value = item.DEPO;
            option.textContent = item.DEPO;
            dpoSelect.appendChild(option);

            const optioncdpo = document.createElement("option");
            optioncdpo.value = item.DEPO;
            optioncdpo.textContent = item.DEPO;
            cdpoSelect.appendChild(optioncdpo);

        });

        oz.forEach(item => {
            const optionOz = document.createElement("option");
            optionOz.value = item.OZEL_KOD_1;
            optionOz.textContent = item.OZEL_KOD_1;
            ozSelect.appendChild(optionOz);

            const optioncoz = document.createElement("option");
            optioncoz.value = item.OZEL_KOD_1;
            optioncoz.textContent = item.OZEL_KOD_1;
            cozSelect.appendChild(optioncoz);
        });

        const newOption = document.createElement("option");
        newOption.value = "Bos Olanlar";
        newOption.textContent = "Bos Olanlar";

        const newOption1 = document.createElement("option");
        newOption1.value = "Bos Olanlar";
        newOption1.textContent = "Bos Olanlar";

        const newOption2 = document.createElement("option");
        newOption2.value = "Bos Olanlar";
        newOption2.textContent = "Bos Olanlar";

        const newOption3 = document.createElement("option");
        newOption3.value = "Bos Olanlar";
        newOption3.textContent = "Bos Olanlar";

        anaSelect.insertBefore(newOption, anaSelect.options[1]);
        canaSelect.insertBefore(newOption1, canaSelect.options[1]);
        dpoSelect.insertBefore(newOption2, dpoSelect.options[1]);
        cdpoSelect.insertBefore(newOption3, cdpoSelect.options[1]);
    } catch (error) {
        const modalError = document.getElementById("errorDiv");
        modalError.style.display = "block";
        modalError.innerText = `Bir hata oluştu: ${error.message}`;
    } finally {
        document.body.style.cursor = "default";
    }
}
