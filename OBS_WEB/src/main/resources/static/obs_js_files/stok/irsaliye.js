document.querySelector('table').addEventListener('focusin', (event) => {
  let currentRow = event.target.closest('tr');
  if (currentRow && currentRow !== lastFocusedRow) {
    lastFocusedRow = currentRow;
    let cells = currentRow.cells;
    document.getElementById("urunadi").innerText = cells[11]?.textContent;
    document.getElementById("anaalt").innerText = cells[12]?.textContent;

   
  }
});

async function fetchkoddepo() {
  const errorDiv = document.getElementById("errorDiv");
  errorDiv.innerText = "";
  errorDiv.style.display = "none";
  rowCounter = 0;
  depolar = "";
  urnkodlar = "";
  try {
    const response = await fetchWithSessionCheck("stok/stkgeturndepo", {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });
    if (response.errorMessage) {
      throw new Error(responseBanka.errorMessage);
    }
    urnkodlar = response.urnkodlar || [];
    depolar = response.depolar || [];
    initializeRows();
  } catch (error) {
    errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
    errorDiv.style.display = "block";
  }
}

function initializeRows() {

  rowCounter = 0;
  for (let i = 0; i < 5; i++) {
    satirekle();
  }
}
function satirekle() {
  const table = document.getElementById("irsTable").getElementsByTagName("tbody")[0];
  const newRow = table.insertRow();
  incrementRowCounter();

  let ukoduoptionsHTML = urnkodlar.map(kod => `<option value="${kod.Kodu}">${kod.Kodu}</option>`).join("");
  newRow.innerHTML = `
    <td >
      <button id="bsatir_${rowCounter}" type="button" class="btn btn-secondary ml-2" onclick="satirsil(this)"><i class="fa fa-trash"></i></button>
    </td>
      <td>
        <div style="position: relative; width: 100%;">
            <input class="form-control cins_bold" list="barkodOptions_${rowCounter}" maxlength="20" id="barkod_${rowCounter}" 
                onkeydown="focusNextCell(event, this)" ondblclick="openurunkodlariModal('barkod_${rowCounter}', 'fatsatir','barkodkod')" onchange="updateRowValues(this)">
            <datalist id="barkodOptions_${rowCounter}"></datalist>
            <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
        </div>
    </td>

    <td>
        <div style="position: relative; width: 100%;">
            <input class="form-control cins_bold" list="ukoduOptions_${rowCounter}" maxlength="12" id="ukodu_${rowCounter}" 
                onkeydown="focusNextCell(event, this)" ondblclick="openurunkodlariModal('ukodu_${rowCounter}', 'imalatsatir','ukodukod')" onchange="updateRowValues(this)">
            <datalist id="ukoduOptions_${rowCounter}">${ukoduoptionsHTML}</datalist>
            <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
        </div>
    </td>
    <td>
    <div style="position: relative; width: 100%;">
        <select class="form-control" id="depo_${rowCounter}">
            ${depolar.map(kod => `
                <option value="${kod.DEPO}" >
                    ${kod.DEPO}
                </option>
            `).join('')}
        </select>
        <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
    </div>
    </td>
    <td>
         <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)" 
        onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
    </td>
        <td>
         <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)"
        onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
    </td>
        <td>
         <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur3(this)"
        onkeydown="focusNextCell(event, this)" value="${formatNumber3(0)}" style="text-align:right;">
    </td>
    <td>
      <label class="form-control" style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label>
    </td>
    <td>
      <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)" 
      onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;" >
    </td>
    <td>
         <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)"  
         onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
    </td>
        <td>
         <input class="form-control" onfocus="selectAllContent(this)" 
         onkeydown="focusNextRow(event, this)" value="" style="text-align:right;">
    </td>
    <td style="display: none;"></td>
    <td style="display: none;"></td>
   
      `;
}

function satirsil(button) {
  const row = button.parentElement.parentElement;
  row.remove();
  updateColumnTotal();
}

function handleBlur3(input) {
  input.value = formatNumber3(parseLocaleNumber(input.value));
  updateColumnTotal();
}
function handleBlur(input) {
  input.value = formatNumber2(parseLocaleNumber(input.value));
  updateColumnTotal();
}

function selectAllContent(element) {
  if (element && element.select) {
    element.select();
  }
}

function updateColumnTotal() {
  const rows = document.querySelectorAll('table tr');
  const totalSatirCell = document.getElementById("totalSatir");
  const totalTutarCell = document.getElementById("totalTutar");
  const totalMiktarCell = document.getElementById("totalMiktar");
  const tevoran = document.getElementById("tevoran");

  let total = 0;
  let totalmik = 0;
  let double_1 = 0;
  let double_2 = 0;
  let double_5 = 0;
  let double_4 = 0;
  let totalsatir = 0;

  totalSatirCell.textContent = "0";
  totalTutarCell.textContent = "0.00";
  totalMiktarCell.textContent = "0.000";
  rows.forEach(row => {
    const secondColumn = row.querySelector('td:nth-child(3) input');

    const fiat = row.querySelector('td:nth-child(5) input');
    const iskonto = row.querySelector('td:nth-child(6) input');
    const miktar = row.querySelector('td:nth-child(7) input');
    const kdvv = row.querySelector('td:nth-child(9) input');
    const tutar = row.querySelector('td:nth-child(10) input');

    if (secondColumn && secondColumn.value.trim() !== '') {
      totalsatir += 1;
    }
    if (fiat && miktar) {
      const isk = parseLocaleNumber(iskonto.value) || 0;
      const kdv = parseLocaleNumber(kdvv.value) || 0;
      const fia = parseLocaleNumber(fiat.value) || 0;
      const mik = parseLocaleNumber(miktar.value) || 0;
      const result = fia * mik;

      tutar.value = formatNumber2(result);
      totalmik += mik;
      if (result > 0) {
        total += result;
        double_5 += result;
        double_1 += (result * isk) / 100;
        double_2 += ((result - (result * isk / 100)) * kdv) / 100;
      }
    }
  });
  document.getElementById("iskonto").innerText = formatNumber2(double_1);
  document.getElementById("bakiye").innerText = formatNumber2(double_5 - double_1);
  document.getElementById("kdv").innerText = formatNumber2(double_2);

  const tev = parseLocaleNumber(tevoran.value) || 0;

  double_4 = tev;
  document.getElementById("tevedkdv").innerText = formatNumber2((double_2 / 10) * double_4);

  double_0 = (double_5 - double_1) + double_2;
  document.getElementById("tevdahtoptut").innerText = formatNumber2(double_0);


  document.getElementById("beyedikdv").innerText = formatNumber2((double_2 - (double_2 / 10) * double_4));
  document.getElementById("tevhartoptut").innerText = formatNumber2((double_5 - double_1) + (double_2 - (double_2 / 10) * double_4));

  totalTutarCell.textContent = total.toLocaleString(undefined, {
    minimumFractionDigits: 2, maximumFractionDigits: 2
  });
  totalMiktarCell.textContent = totalmik.toLocaleString(undefined, {
    minimumFractionDigits: 3, maximumFractionDigits: 3
  });

  totalSatirCell.textContent = totalsatir.toLocaleString(undefined, {
    minimumFractionDigits: 0, maximumFractionDigits: 0
  });
}

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

async function updateRowValues(inputElement) {
  const selectedValue = inputElement.value;
  const uygulananfiat = document.getElementById("uygulananfiat").value;
  const barkod = "";
  const gircikdeger = document.getElementById("gircik").value;
  const carikod = document.getElementById("carikod").value;

  document.body.style.cursor = "wait";
  errorDiv.style.display = "none";
  errorDiv.innerText = "";
  try {
    const response = await fetchWithSessionCheck("stok/urunoku", {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ ukodu: selectedValue, barkod: barkod, fiatlama: uygulananfiat, gircik: gircikdeger, ckod: carikod }),
    });
    if (response.errorMessage) {
      throw new Error(response.errorMessage);
    }
    const row = inputElement.closest('tr');
    const cells = row.querySelectorAll('td');

    const barkodCell = cells[1]?.querySelector('input');
    const fiatCell = cells[4]?.querySelector('input');
    const birimCell = cells[7];
    cells[11].innerText = response.dto.adi || '';
    cells[12].innerText = response.dto.anagrup + " / " + response.dto.altgrup || '';
    
    setLabelContent(birimCell, response.dto.birim);

    barkodCell.value = response.dto.barkod;
    fiatCell.value = formatNumber2(response.fiat);

    document.getElementById("urunadi").innerText = response.dto.adi;
    document.getElementById("anaalt").innerText = response.dto.anagrup + " / " + response.dto.altgrup;

    const imgElement = document.getElementById("resimGoster");
    if (response.dto.base64Resim && response.dto.base64Resim.trim() !== "") {
      const base64String = 'data:image/jpeg;base64,' + response.dto.base64Resim.trim();
      imgElement.src = base64String;
      imgElement.style.display = "block";
    } else {
      imgElement.src = "";
      imgElement.style.display = "none";
    }
  } catch (error) {
    errorDiv.style.display = "block";
    errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
  } finally {
    document.body.style.cursor = "default";
  }
}

function setLabelContent(cell, content) {
  const span = cell.querySelector('label span');
  if (span) {
    span.textContent = content ? content : '\u00A0';
  }
}
function focusNextRow(event, element) {
  if (event.key === "Enter") {
    event.preventDefault();
    const currentRow = element.closest('tr');
    const nextRow = currentRow.nextElementSibling;
    if (nextRow) {
      const secondInput = nextRow.querySelector("td:nth-child(3) input");
      if (secondInput) {
        secondInput.focus();
        secondInput.select();
      }
    } else {
      satirekle();
      const table = currentRow.parentElement;
      const newRow = table.lastElementChild;
      const secondInput = newRow.querySelector("td:nth-child(3) input");
      if (secondInput) {
        secondInput.focus();
        secondInput.select();
      }
    }
  }
}

function focusNextCell(event, element) {
  if (event.key === "Enter") {
    event.preventDefault();
    let currentCell = element.closest('td');
    let nextCell = currentCell.nextElementSibling;
    while (nextCell) {
      const focusableElement = nextCell.querySelector('input');
      if (focusableElement) {
        focusableElement.focus();
        if (focusableElement.select) {
          focusableElement.select();
        }
        break;
      } else {
        nextCell = nextCell.nextElementSibling;
      }
    }
  }
}

function clearInputs() {

  document.getElementById("barkod").value = '';
  document.getElementById("urunadi").innerText = '';
  document.getElementById("anaalt").innerText = '';
  document.getElementById("iskonto").innerText = "0.00";
  document.getElementById("bakiye").innerText = "0.00";
  document.getElementById("kdv").innerText = "0.00";
  document.getElementById("tevedkdv").innerText = "0.00";
  document.getElementById("tevdahtoptut").innerText = "0.00";
  document.getElementById("beyedikdv").innerText = "0.00";
  document.getElementById("tevhartoptut").innerText = "0.00";
  document.getElementById("tevoran").value = "0.00";

  document.getElementById("ozelkod").value = '';
  document.getElementById("anagrp").value = '';
  document.getElementById("altgrp").innerHTML = '';
  document.getElementById("altgrp").disabled = true;

  document.getElementById("carikod").value = '';
  document.getElementById("adreskod").value = '';
  document.getElementById("cariadilbl").innerText = "";
  document.getElementById("adresadilbl").innerText = "";

  document.getElementById("uygulananfiat").selectedIndex = 0;
  document.getElementById("adreskod").value = '';

  document.getElementById("dovizcins").value = document.getElementById("defaultdvzcinsi").value || 'TL';
  document.getElementById("kur").value = '0.0000';

  document.getElementById("not1").value = '';
  document.getElementById("not2").value = '';
  document.getElementById("not3").value = '';
  document.getElementById("fatmikyazdir").checked = false;

  document.getElementById("a1").value = '';
  document.getElementById("a2").value = '';


  const tableBody = document.getElementById("tbody");
  tableBody.innerHTML = "";
  rowCounter = 0;
  initializeRows();
  document.getElementById("totalTutar").textContent = formatNumber2(0);
  document.getElementById("totalSatir").textContent = formatNumber0(0);
  document.getElementById("totalMiktar").textContent = formatNumber3(0);

  const imgElement = document.getElementById("resimGoster");
  imgElement.src = "";
  imgElement.style.display = "none";

}

async function irsOku() {
  const fisno = document.getElementById("fisno").value;
  if (!fisno) {
    return;
  }
  const gircikdeger = document.getElementById("gircik").value;
  const errorDiv = document.getElementById("errorDiv");
  document.body.style.cursor = "wait";
  try {
    const response = await fetchWithSessionCheck("stok/irsOku", {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({ fisno: fisno, cins: gircikdeger }),
    });
    const data = response;
		console.info(data);
    clearInputs();
    if (response.errorMessage) {
      throw new Error(response.errorMessage);
    }
    const dataSize = data.data.length;
    if (dataSize === 0) return;

    const table = document.getElementById('irsTable');
    const rowss = table.querySelectorAll('tbody tr');
    if (data.data.length > rowss.length) {
      const additionalRows = data.data.length - rowss.length;
      for (let i = 0; i < additionalRows; i++) {
        satirekle();
      }
    }
    const rows = table.querySelectorAll('tbody tr');
    data.data.forEach((item, index) => {
      const cells = rows[index].cells;
      const barkodInput = cells[1]?.querySelector('input');
      if (barkodInput) barkodInput.value = item.Barkod || "";
      const urunKoduInput = cells[2]?.querySelector('input');
      if (urunKoduInput) urunKoduInput.value = item.Kodu || "";
      const depoSelect = cells[3]?.querySelector('select');
      if (depoSelect) depoSelect.value = item.Depo || "";
      const fiatInput = cells[4]?.querySelector('input');
      if (fiatInput) fiatInput.value = formatNumber2(item.Fiat);
      const iskontoInput = cells[5]?.querySelector('input');
      if (iskontoInput) iskontoInput.value = formatNumber2(item.Iskonto);
      const miktarInput = cells[6]?.querySelector('input');
      if (miktarInput) miktarInput.value = formatNumber3(item.Miktar);
      setLabelContent(cells[7], item.Birim || '');
      const kdvInput = cells[8]?.querySelector('input');
      if (kdvInput) kdvInput.value = formatNumber2(item.Kdv);
      const tutarInput = cells[9]?.querySelector('input');
      if (tutarInput) tutarInput.value = formatNumber2(item.Tutar);
      const izahatInput = cells[10]?.querySelector('input');
      if (izahatInput) izahatInput.value = item.Izahat || "";
      cells[11].innerText = item.Adi || '';
      cells[12].innerText = item.Ur_AnaGrup + " / " + item.Ur_AltGrup;
     
    });

    for (let i = 0; i < data.data.length; i++) {
      const item = data.data[i];
      document.getElementById("fisTarih").value = formatdateSaatsiz(item.Tarih);
			document.getElementById("sevkTarih").value = formatdateSaatsiz(item.Sevk_Tarihi);
      document.getElementById("anagrp").value = item.Ana_Grup || '';
      document.getElementById("kur").value = item.Kur;
      await anagrpChanged(document.getElementById("anagrp"));
      document.getElementById("altgrp").value = item.Alt_Grup || ''
      document.getElementById("ozelkod").value = item.Ozel_Kod || ''
      document.getElementById("tevoran").value = item.Tevkifat || '0'
      document.getElementById("carikod").value = item.Cari_Hesap_Kodu || '';
      document.getElementById("adreskod").value = item.Firma || '';
      document.getElementById("dovizcins").value = item.Doviz || '';
			document.getElementById("fatno").value = item.Fatura_No || '';
      break;
    }
    document.getElementById("a1").value = data.a1;
    document.getElementById("a2").value = data.a2;

    document.getElementById("not1").value = data.dipnot[0];
    document.getElementById("not2").value = data.dipnot[1];
    document.getElementById("not3").value = data.dipnot[2];


    updateColumnTotal();

    hesapAdiOgren(document.getElementById("carikod").value, 'cariadilbl')
    adrhesapAdiOgren('adreskod', 'adresadilbl');
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
  } catch (error) {
    errorDiv.style.display = "block";
    errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
  } finally {
    document.body.style.cursor = "default";
  }
}

async function sonfis() {
  const gircikdeger = document.getElementById("gircik").value;
  document.body.style.cursor = "wait";
  try {
    const response = await fetchWithSessionCheck('stok/sonirsfis', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({ cins: gircikdeger }),
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
      irsOku();
    }
  } catch (error) {
    const errorDiv = document.getElementById('errorDiv');
    errorDiv.style.display = 'block';
    errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
  } finally {
    document.body.style.cursor = "default";
  }
}

async function yeniFis() {
  const gircikdeger = document.getElementById("gircik").value;
  const errorDiv = document.getElementById('errorDiv');
  errorDiv.innerText = "";
  clearInputs();
  document.body.style.cursor = "wait";
  try {
    const response = await fetchWithSessionCheck('stok/yenifis', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({ cins: gircikdeger }),
    });
    if (response.errorMessage) {
      throw new Error(response.errorMessage);
    }
    const fisNoInput = document.getElementById('fisno');
    fisNoInput.value = response.fisno;
  } catch (error) {
    errorDiv.style.display = "block";
    errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
  } finally {

    document.body.style.cursor = "default";
  }
}

async function irsYoket() {
  const fisNoInput = document.getElementById('fisno').value;
  const gircikdeger = document.getElementById("gircik").value;
  if (["0", ""].includes(fisNoInput.value)) {
    return;
  }
  const confirmDelete = confirm("Bu Fatura silinecek ?");
  if (!confirmDelete) {
    return;
  }
  document.body.style.cursor = "wait";
  const $silButton = $('#fatsilButton');
  $silButton.prop('disabled', true).text('Siliniyor...');
  try {
    const response = await fetchWithSessionCheck("stok/fatYoket", {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({ fisno: fisNoInput.value, cins: gircikdeger }),
    });
    if (response.errorMessage) {
      throw new Error(response.errorMessage);
    }
    clearInputs();
    document.getElementById("fisno").value = "";
    document.getElementById("errorDiv").style.display = "none";
    document.getElementById("errorDiv").innerText = "";
  } catch (error) {
    document.getElementById("errorDiv").style.display = "block";
    document.getElementById("errorDiv").innerText = error.message || "Beklenmeyen bir hata oluştu.";
  } finally {
    document.body.style.cursor = "default";
    $silButton.prop('disabled', false).text('Sil');
  }
}

function prepareureKayit() {
  const faturaDTO = {
    fisno: document.getElementById("fisno").value || "",
    tarih: document.getElementById("fisTarih").value || "",
		sevktarih: document.getElementById("sevkTarih").value || "",
    ozelkod: document.getElementById("ozelkod").value || "",
    anagrup: document.getElementById("anagrp").value || "",
    altgrup: document.getElementById("altgrp").value || "",

    carikod: document.getElementById("carikod").value || "",
    adreskod: document.getElementById("adreskod").value || "",

    dvzcins: document.getElementById("dovizcins").value || "",
    kur: parseLocaleNumber(document.getElementById("kur").value) || 0,
    tevoran: parseLocaleNumber(document.getElementById("tevoran").value) || 0,

    not1: document.getElementById("not1").value || "",
    not2: document.getElementById("not2").value || "",
    not3: document.getElementById("not3").value || "",

    acik1: document.getElementById("a1").value || "",
    acik2: document.getElementById("a2").value || "",
		fatno: document.getElementById("fatno").value || "",

    fatcins: document.getElementById("gircik").value,

  };
	console.info(document.getElementById("fatno").value);
  tableData = getTableData();
  return { faturaDTO, tableData, };
}

function getTableData() {
  const table = document.getElementById('irsTable');
  const rows = table.querySelectorAll('tbody tr');
  const data = [];
  rows.forEach((row) => {
    const cells = row.querySelectorAll('td');
    const firstColumnValue = cells[2]?.querySelector('input')?.value || "";
    if (firstColumnValue.trim()) {
      const rowData = {
        barkod: cells[1]?.querySelector('input')?.value || "",
        ukodu: firstColumnValue,
        depo: cells[3]?.querySelector('select')?.value || "",
        fiat: parseLocaleNumber(cells[4]?.querySelector('input')?.value || 0),
        iskonto: parseLocaleNumber(cells[5]?.querySelector('input')?.value || 0),
        miktar: parseLocaleNumber(cells[6]?.querySelector('input')?.value || 0),
        kdv: parseLocaleNumber(cells[8]?.querySelector('input')?.value || 0),
        tutar: parseLocaleNumber(cells[9]?.querySelector('input')?.value || 0),
        izahat: cells[10]?.querySelector('input')?.value || "",
      };
      data.push(rowData);
    }
  });
  return data;
}
async function irsKayit() {
  const fisno = document.getElementById("fisno").value;
  const table = document.getElementById('irsTable');
  const rows = table.rows;
  if (!fisno || fisno === "0" || rows.length === 0) {
    alert("Geçerli bir evrak numarası giriniz.");
    return;
  }
  const faturakayitDTO = prepareureKayit();
  const errorDiv = document.getElementById('errorDiv');
  const $kaydetButton = $('#irskaydetButton');
  $kaydetButton.prop('disabled', true).text('İşleniyor...');

  document.body.style.cursor = 'wait';
  try {
    const response = await fetchWithSessionCheck('stok/irsKayit', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(faturakayitDTO),
    });
    if (response.errorMessage.trim() !== "") {
      throw new Error(response.errorMessage);
    }
    clearInputs();
    document.getElementById("fisno").value = "";
    document.getElementById("errorDiv").innerText = "";
    errorDiv.style.display = 'none';
  } catch (error) {
    errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
    errorDiv.style.display = 'block';
  } finally {
    document.body.style.cursor = 'default';
    $kaydetButton.prop('disabled', false).text('Kaydet');
  }
}

async function fatcariIsle() {
  const hesapKodu = $('#irsaliyeBilgi').val();
  const fisno = document.getElementById("fisno").value;
  const table = document.getElementById('irsTable');
  const rows = table.rows;
  if (!fisno || fisno === "0" || rows.length === 0) {
    alert("Geçerli bir evrak numarası giriniz.");
    return;
  }
  const $carkaydetButton = $('#carkaydetButton');
  $carkaydetButton.prop('disabled', true).text('İşleniyor...');

  const faturaDTO = {
    fisno: document.getElementById("fisno").value || "",
    tarih: document.getElementById("fisTarih").value || "",
    carikod: document.getElementById("carikod").value || "",
    miktar: parseLocaleNumber(document.getElementById("totalMiktar").textContent || 0),
    tutar: parseLocaleNumber(document.getElementById("tevhartoptut").innerText || 0),
    fatcins: document.getElementById("gircik").value,
    karsihesapkodu: hesapKodu,
  };
  const errorDiv = document.getElementById('errorDiv');
  document.body.style.cursor = 'wait';
  try {
    const response = await fetchWithSessionCheck('stok/irscariKayit', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(faturaDTO),
    });
    if (response.errorMessage.trim() !== "") {
      throw new Error(response.errorMessage);
    }
    document.getElementById("errorDiv").innerText = "";
    errorDiv.style.display = 'none';
  } catch (error) {
    errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
    errorDiv.style.display = 'block';
  } finally {
    $carkaydetButton.prop('disabled', false).text('Cari Kaydet');
    document.body.style.cursor = 'default';
  }
}