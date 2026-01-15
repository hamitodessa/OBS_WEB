/* =========================
   KERESTE CIKIS - TABLE JS
   tbody id: kerTbody
   ========================= */



/* rowCounter artir */
function incrementRowCounter(){
  rowCounter++;
}

/* bos hucre üretici (label+span) */
function emptyCellKer(textAlignRight=false, value=""){
  return `
    <td>
      <label class="form-control ker-cell">
        <span class="${textAlignRight ? 'cell-right' : 'cell-left'}">
          ${value || "&nbsp;"}
        </span>
      </label>
    </td>`;
}

/* kerTbody altindaki satirlari getir */
function getKerRows(){
  return document.querySelectorAll('#kerTable #kerTbody tr');
}

/* ============ DATA GET ============ */
async function fetchpakdepo() {
  const errorDiv = document.getElementById("errorDiv");
  if (errorDiv){
    errorDiv.innerText = "";
    errorDiv.style.display = "none";
  }

  rowCounter = 0;
  depolar = [];
  urnkodlar = [];

  try {
    const response = await fetchWithSessionCheck("kereste/getpakdepo", {
      method: "GET",
      headers: { "Content-Type": "application/json" },
    });

    if (response?.errorMessage) {
      throw new Error(response.errorMessage);
    }

    urnkodlar = response.paknolar || [];
    depolar   = response.depolar  || [];

    initializeRows();
  } catch (error) {
    if (errorDiv){
      errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
      errorDiv.style.display = "block";
    }
  }
}

/* 5 satir baslat */
function initializeRows() {
  rowCounter = 0;
  for (let i = 0; i < 5; i++) satirekle();
}

/* ============ ROW ADD ============ */
function satirekle() {
  const tbody = document.getElementById("kerTbody");
  if (!tbody){
    console.error("kerTbody bulunamadi!");
    return;
  }

  const rowCount = tbody.rows.length;
  if (rowCount >= 250) {
    alert("En fazla 250 satir ekleyebilirsiniz.");
    return;
  }

  const newRow = tbody.insertRow();
  incrementRowCounter();

  const paknooptionsHTML = (urnkodlar || [])
    .map(kod => `<option value="${kod.Paket_No}">${kod.Paket_No}</option>`)
    .join("");

  const depOptionsHTML = (depolar || [])
    .map(kod => `<option value="${kod.DEPO}">${kod.DEPO}</option>`)
    .join("");

  newRow.innerHTML = `
    <!-- SIL -->
    <td>
      <button id="bsatir_${rowCounter}" type="button"
        class="btn btn-secondary ker-rowbtn" onclick="satirsil(this)">
        <i class="fa fa-trash"></i>
      </button>
    </td>

    <!-- PAKET_NO (datalist) -->
    <td>
      <div class="ker-dd">
        <input class="form-control cins_bold ker-cell"
          list="pakOptions_${rowCounter}"
          id="pakno_${rowCounter}"
          maxlength="30"
          onkeydown="if(event.key === 'Enter') paketkontrol(event,this)">
        <datalist id="pakOptions_${rowCounter}">${paknooptionsHTML}</datalist>
        <span class="ker-dd-arrow">▼</span>
      </div>
    </td>

    <!-- LABEL SUTUNLARI -->
    ${emptyCellKer(false,"")}  <!-- BARKOD -->
    ${emptyCellKer(false,"")}  <!-- URUN KODU -->
    ${emptyCellKer(true,"")}   <!-- MIKTAR -->
    ${emptyCellKer(true,"")}   <!-- M3 -->
    ${emptyCellKer(true,"")}   <!-- PAK_M3 -->

    <!-- DEPO -->
    <td>
      <div class="ker-dd">
        <select class="form-select ker-cell" id="depo_${rowCounter}"
          onkeydown="focusNextCell(event, this)">
          ${depOptionsHTML}
        </select>
        <span class="ker-dd-arrow">▼</span>
      </div>
    </td>

    <!-- FIAT -->
    <td>
      <input class="form-control ker-cell double-column"
        onfocus="selectAllContent(this)" onblur="handleBlur(this)"
        onkeydown="focusNextCell(event, this)"
        value="${formatNumber2(0)}">
    </td>

    <!-- ISK -->
    <td>
      <input class="form-control ker-cell double-column"
        onfocus="selectAllContent(this)" onblur="handleBlur(this)"
        onkeydown="focusNextCell(event, this)"
        value="${formatNumber2(0)}">
    </td>

    <!-- KDV -->
    <td>
      <input class="form-control ker-cell double-column"
        onfocus="selectAllContent(this)" onblur="handleBlur(this)"
        onkeydown="focusNextCell(event, this)"
        value="${formatNumber2(0)}">
    </td>

    <!-- TUTAR -->
    <td>
      <input class="form-control ker-cell ker-cell--bold double-column"
        onfocus="selectAllContent(this)" onblur="handleBlur(this)"
        onkeydown="focusNextCell(event, this)"
        value="${formatNumber2(0)}">
    </td>

    <!-- IZAHAT -->
    <td>
      <input class="form-control ker-cell"
        onfocus="selectAllContent(this)"
        onkeydown="focusNextRow(event, this)">
    </td>

    <!-- gizli SATIR -->
    <td style="display:none;"></td>
  `;

  return newRow;
}

/* ============ INPUT HELPERS ============ */
function handleBlur(input) {
  input.value = formatNumber2(parseLocaleNumber(input.value));
  updateColumnTotal();
}

function selectAllContent(element) {
  if (element && element.select) element.select();
}

/* ============ PAKET KONTROL ============ */
async function paketkontrol(event, input) {
  const fisno = document.getElementById("fisno")?.value || "";
  const errorDiv = document.getElementById('errorDiv');
  if (errorDiv){
    errorDiv.style.display = 'none';
    errorDiv.innerText = "";
  }

  const row = input.closest('tr');
  if (!row) return;

  const pakno = input.value;
  document.body.style.cursor = "wait";

  try {
    const response = await fetchWithSessionCheck("kereste/paket_oku", {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ pno: pakno, cins: 'CIKIS', fisno }),
    });

    if (response?.errorMessage) throw new Error(response.errorMessage);

    const data = response;

    if (data.mesaj && data.mesaj !== "") {
      document.body.style.cursor = "default";
      setTimeout(() => alert(data.mesaj), 100);
      return;
    }

    if (!data.paket || data.paket.length === 0) {
      focusNextCell(event, input);
      return;
    }

    const table = document.getElementById('kerTable');
    const currentRow = input.closest('tr');
    let rowIndex = currentRow.rowIndex; // table icinde index (thead dahil)
    // bizim tbody satirlarindan dolduracagiz
    const rowsNow = getKerRows();
    // rowIndex - 1 -> thead satirini dus
    let targetIndex = rowIndex - 1;

    // gerekli satir sayisi kadar ekle
    if (data.paket.length + targetIndex > rowsNow.length) {
      const needed = (data.paket.length + targetIndex) - rowsNow.length;
      for (let i = 0; i < needed; i++) satirekle();
    }

    const rows = getKerRows();

    data.paket.forEach((item) => {
      const cells = rows[targetIndex]?.cells;
      if (!cells) { targetIndex++; return; }

      const pakInput = cells[1]?.querySelector('input');
      if (pakInput) pakInput.value = input.value;

      const barkodSpan = cells[2]?.querySelector('label span');
      if (barkodSpan) barkodSpan.textContent = item.Barkod || "\u00A0";

      const koduSpan = cells[3]?.querySelector('label span');
      if (koduSpan) koduSpan.textContent = item.Kodu || "\u00A0";

      const miktarSpan = cells[4]?.querySelector('label span');
      if (miktarSpan) miktarSpan.textContent = formatNumber0(item.Miktar || 0);

      const m3Span = cells[5]?.querySelector('label span');
      if (m3Span) m3Span.textContent = formatNumber3(hesaplaM3(item.Kodu, item.Miktar) || 0);

      // gizli satir no
      if (cells[13]) cells[13].innerText = item.Satir || 0;

      targetIndex++;
    });

    updatePaketM3();
    updateColumnTotal();

  } catch (error) {
    if (errorDiv){
      errorDiv.style.display = 'block';
      errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
    }
  } finally {
    document.body.style.cursor = "default";
  }
}

/* ============ TOTALS ============ */
function updateColumnTotal() {
  const rows = getKerRows();

  const totalSatirCell   = document.getElementById("totalSatir");
  const totalTutarCell   = document.getElementById("totalTutar");
  const totalMiktarCell  = document.getElementById("totalMiktar");
  const totalM3Cell      = document.getElementById("totalM3");
  const totalPaketM3Cell = document.getElementById("totalPaketM3");
  const tevoran          = document.getElementById("tevoran");

  let total = 0;
  let totalm3 = 0;
  let totalpakm3 = 0;
  let totalMiktar = 0;
  let totalsatir = 0;

  let iskTop = 0;
  let kdvTop = 0;
  let brutTop = 0;

  if (totalSatirCell) totalSatirCell.textContent = "0";
  if (totalTutarCell) totalTutarCell.textContent = "0.00";
  if (totalMiktarCell) totalMiktarCell.textContent = "0";
  if (totalM3Cell) totalM3Cell.textContent = "0.000";
  if (totalPaketM3Cell) totalPaketM3Cell.textContent = "0.000";

  rows.forEach(row => {
    const firstColumn = row.querySelector('td:nth-child(2) input');
    const mikSpan = row.querySelector('td:nth-child(5) label span');
    const m3Span  = row.querySelector('td:nth-child(6) label span');
    const pm3Span = row.querySelector('td:nth-child(7) label span');

    const fiat = row.querySelector('td:nth-child(9) input');
    const iskonto = row.querySelector('td:nth-child(10) input');
    const kdvv = row.querySelector('td:nth-child(11) input');
    const tutar = row.querySelector('td:nth-child(12) input');

    if (firstColumn && firstColumn.value.trim() !== '') totalsatir += 1;

    if (fiat && m3Span && tutar && iskonto && kdvv) {
      const isk = parseLocaleNumber(iskonto.value) || 0;
      const kdv = parseLocaleNumber(kdvv.value) || 0;
      const fia = parseLocaleNumber(fiat.value) || 0;
      const m33 = parseLocaleNumber(m3Span.textContent.trim() || 0);

      const result = fia * m33; // brut
      tutar.value = formatNumber2(result);

      totalm3 += m33;
      totalpakm3 += parseLocaleNumber(pm3Span?.textContent?.trim() || 0);
      totalMiktar += mikSpan?.textContent?.trim() ? parseLocaleNumber(mikSpan.textContent.trim()) : 0;

      if (result > 0) {
        brutTop += result;
        iskTop += (result * isk) / 100;
        kdvTop += ((result - (result * isk / 100)) * kdv) / 100;
        total += result;
      }
    }
  });

  // sag bloklar (varsa)
  if (document.getElementById("iskonto")) document.getElementById("iskonto").innerText = formatNumber2(iskTop);
  if (document.getElementById("bakiye"))  document.getElementById("bakiye").innerText  = formatNumber2(brutTop - iskTop);
  if (document.getElementById("kdv"))     document.getElementById("kdv").innerText     = formatNumber2(kdvTop);

  const tev = tevoran ? (parseLocaleNumber(tevoran.value) || 0) : 0;

  if (document.getElementById("tevedkdv"))    document.getElementById("tevedkdv").innerText    = formatNumber2((kdvTop / 10) * tev);
  const genelTop = (brutTop - iskTop) + kdvTop;
  if (document.getElementById("tevdahtoptut")) document.getElementById("tevdahtoptut").innerText = formatNumber2(genelTop);

  if (document.getElementById("beyedikdv"))   document.getElementById("beyedikdv").innerText   = formatNumber2((kdvTop - (kdvTop / 10) * tev));
  if (document.getElementById("tevhartoptut")) document.getElementById("tevhartoptut").innerText = formatNumber2((brutTop - iskTop) + (kdvTop - (kdvTop / 10) * tev));

  if (totalTutarCell) totalTutarCell.textContent = brutTop.toLocaleString(undefined,{ minimumFractionDigits:2, maximumFractionDigits:2 });
  if (totalMiktarCell) totalMiktarCell.textContent = totalMiktar.toLocaleString(undefined,{ minimumFractionDigits:0, maximumFractionDigits:0 });
  if (totalM3Cell) totalM3Cell.textContent = totalm3.toLocaleString(undefined,{ minimumFractionDigits:3, maximumFractionDigits:3 });
  if (totalPaketM3Cell) totalPaketM3Cell.textContent = totalpakm3.toLocaleString(undefined,{ minimumFractionDigits:3, maximumFractionDigits:3 });
  if (totalSatirCell) totalSatirCell.textContent = totalsatir.toLocaleString(undefined,{ minimumFractionDigits:0, maximumFractionDigits:0 });
}

/* ============ M3 ============ */
function hesaplaM3(kodu, miktar) {
  if (!kodu) return 0;
  const token = kodu.split("-");
  if (token.length !== 4) return 0;

  let m3 = 0;
  const d1 = token[1]?.trim();
  const d2 = token[2]?.trim();
  const d3 = token[3]?.trim();

  if (d1 && d2 && d3) {
    m3 = ((parseFloat(d1) * parseFloat(d2) * parseFloat(d3)) * (miktar || 0)) / 1000000000;
  }
  return m3;
}

/* ============ PAKET M3 TOPLA ============ */
function updatePaketM3() {
  const rows = getKerRows();
  let paketMap = new Map();
  let paketadet = 0;

  rows.forEach(row => {
    const paketnoInput = row.querySelector('td:nth-child(2) input');
    const m3Span       = row.querySelector('td:nth-child(6) label span');
    const paketm3Span  = row.querySelector('td:nth-child(7) label span');

    if (!paketnoInput || !m3Span || !paketm3Span) return;

    const paketNo = paketnoInput.value.trim();
    const m3Value = parseLocaleNumber(m3Span.textContent.trim()) || 0;
    if (!paketNo) return;

    if (!paketMap.has(paketNo)) paketMap.set(paketNo, { total: 0, rows: [] });
    paketMap.get(paketNo).total += m3Value;
    paketMap.get(paketNo).rows.push(paketm3Span);
  });

  paketMap.forEach((data) => {
    const rowCount = data.rows.length;
    data.rows.forEach((cell, index) => {
      if (index === rowCount - 1) {
        cell.textContent = data.total ? data.total.toFixed(3) : '\u00A0';
        paketadet += 1;
      } else {
        cell.textContent = '\u00A0';
      }
    });
  });

  const tp = document.getElementById("totalPakadet");
  if (tp) tp.textContent = "Paket:" + formatNumber0(paketadet);
}

/* ============ NAV ENTER ============ */
function focusNextRow(event, element) {
  if (event.key !== "Enter") return;
  event.preventDefault();

  const currentRow = element.closest('tr');
  const nextRow = currentRow?.nextElementSibling;

  if (nextRow) {
    const secondInput = nextRow.querySelector("td:nth-child(2) input");
    if (secondInput) { secondInput.focus(); secondInput.select(); }
  } else {
    satirekle();
    const rows = getKerRows();
    const lastRow = rows[rows.length - 1];
    const secondInput = lastRow?.querySelector("td:nth-child(2) input");
    if (secondInput) { secondInput.focus(); secondInput.select(); }
  }
}

function focusNextCell(event, element) {
  if (event.key !== "Enter") return;
  event.preventDefault();

  let currentCell = element.closest('td');
  let nextCell = currentCell?.nextElementSibling;

  while (nextCell) {
    const focusableElement = nextCell.querySelector('input,select');
    if (focusableElement) {
      focusableElement.focus();
      if (focusableElement.select) focusableElement.select();
      break;
    }
    nextCell = nextCell.nextElementSibling;
  }
}

/* ============ CLEAR ============ */
function clearInputs() {
  // label bloklari (varsa)
  if (document.getElementById("urunadi")) document.getElementById("urunadi").innerText = '';
  if (document.getElementById("anaalt"))  document.getElementById("anaalt").innerText = '';

  if (document.getElementById("iskonto")) document.getElementById("iskonto").innerText = "0.00";
  if (document.getElementById("bakiye"))  document.getElementById("bakiye").innerText  = "0.00";
  if (document.getElementById("kdv"))     document.getElementById("kdv").innerText     = "0.00";
  if (document.getElementById("tevedkdv")) document.getElementById("tevedkdv").innerText = "0.00";
  if (document.getElementById("tevdahtoptut")) document.getElementById("tevdahtoptut").innerText = "0.00";
  if (document.getElementById("beyedikdv")) document.getElementById("beyedikdv").innerText = "0.00";
  if (document.getElementById("tevhartoptut")) document.getElementById("tevhartoptut").innerText = "0.00";

  if (document.getElementById("tevoran")) document.getElementById("tevoran").value = "0.00";

  if (document.getElementById("ozelkod")) document.getElementById("ozelkod").value = '';
  if (document.getElementById("anagrp"))  document.getElementById("anagrp").value = '';
  if (document.getElementById("altgrp"))  { document.getElementById("altgrp").innerHTML = ''; document.getElementById("altgrp").disabled = true; }
  if (document.getElementById("nakliyeci")) document.getElementById("nakliyeci").value = '';

  if (document.getElementById("carikod")) document.getElementById("carikod").value = '';
  if (document.getElementById("adreskod")) document.getElementById("adreskod").value = '';
  if (document.getElementById("cariadilbl")) document.getElementById("cariadilbl").innerText = "";
  if (document.getElementById("adresadilbl")) document.getElementById("adresadilbl").innerText = "";

  if (document.getElementById("dovizcins")) document.getElementById("dovizcins").value = (document.getElementById("defaultdvzcinsi")?.value || 'TL');
  if (document.getElementById("kur")) document.getElementById("kur").value = '0.0000';

  if (document.getElementById("not1")) document.getElementById("not1").value = '';
  if (document.getElementById("not2")) document.getElementById("not2").value = '';
  if (document.getElementById("not3")) document.getElementById("not3").value = '';

  if (document.getElementById("fatmikyazdir")) document.getElementById("fatmikyazdir").checked = false;

  if (document.getElementById("a1")) document.getElementById("a1").value = '';
  if (document.getElementById("a2")) document.getElementById("a2").value = '';

  // tablo temizle
  const tableBody = document.getElementById("kerTbody");
  if (tableBody) tableBody.innerHTML = "";

  rowCounter = 0;
  initializeRows();

  if (document.getElementById("totalSatir")) document.getElementById("totalSatir").textContent = formatNumber0(0);
  if (document.getElementById("totalMiktar")) document.getElementById("totalMiktar").textContent = formatNumber0(0);
  if (document.getElementById("totalM3")) document.getElementById("totalM3").textContent = formatNumber3(0);
  if (document.getElementById("totalPaketM3")) document.getElementById("totalPaketM3").textContent = formatNumber3(0);
  if (document.getElementById("totalTutar")) document.getElementById("totalTutar").textContent = formatNumber2(0);
  if (document.getElementById("totalPakadet")) document.getElementById("totalPakadet").textContent = '';
}

/* ============ KER OKU ============ */
async function kerOku() {
  const fisno = document.getElementById("fisno")?.value?.trim() || "";
  if (!fisno) return;

  const errorDiv = document.getElementById("errorDiv");
  document.body.style.cursor = "wait";

  try {
    const response = await fetchWithSessionCheck("kereste/kerOku", {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ fisno, cins: 'CIKIS' }),
    });

    const data = response;
    clearInputs();

    if (response?.errorMessage) throw new Error(response.errorMessage);
    if (!data?.data || data.data.length === 0) return;

    // gerekirse satir ekle
    const rowsNow = getKerRows();
    if (data.data.length > rowsNow.length) {
      const add = data.data.length - rowsNow.length;
      for (let i = 0; i < add; i++) satirekle();
    }

    const rows = getKerRows();

    data.data.forEach((item, index) => {
      const cells = rows[index]?.cells;
      if (!cells) return;

      const paknoInput = cells[1]?.querySelector('input');
      if (paknoInput) paknoInput.value = (item.Paket_No || "") + "-" + (item.Konsimento || "");

      const barkodSpan = cells[2]?.querySelector('label span');
      if (barkodSpan) barkodSpan.textContent = (item.Barkod && item.Barkod.trim()) ? item.Barkod : "\u00A0";

      const urunSpan = cells[3]?.querySelector('label span');
      if (urunSpan) urunSpan.textContent = item.Kodu || "\u00A0";

      const mikSpan = cells[4]?.querySelector('label span');
      if (mikSpan) mikSpan.textContent = formatNumber0(item.Miktar || 0);

      const m3Span = cells[5]?.querySelector('label span');
      if (m3Span) m3Span.textContent = formatNumber3(hesaplaM3(item.Kodu, item.Miktar) || 0);

      const pm3Span = cells[6]?.querySelector('label span');
      if (pm3Span) pm3Span.textContent = "\u00A0";

      const depoSel = cells[7]?.querySelector('select');
      if (depoSel) depoSel.value = item.CDepo || "";

      const fiatInput = cells[8]?.querySelector('input');
      if (fiatInput) fiatInput.value = formatNumber2(item.CFiat || 0);

      const iskInput = cells[9]?.querySelector('input');
      if (iskInput) iskInput.value = formatNumber2(item.CIskonto || 0);

      const kdvInput = cells[10]?.querySelector('input');
      if (kdvInput) kdvInput.value = formatNumber2(item.CKdv || 0);

      const tutarInput = cells[11]?.querySelector('input');
      if (tutarInput) tutarInput.value = formatNumber2(item.CTutar || 0);

      const izahatInput = cells[12]?.querySelector('input');
      if (izahatInput) izahatInput.value = item.CIzahat || "";

      if (cells[13]) cells[13].innerText = item.Satir || '';
    });

    // ust alanlar
    const first = data.data[0];
    if (first){
      if (document.getElementById("fisTarih")) document.getElementById("fisTarih").value = formatdateSaatsiz(first.CTarih);
      if (document.getElementById("anagrp"))  document.getElementById("anagrp").value = first.Ana_Grup || '';
      if (document.getElementById("kur"))     document.getElementById("kur").value = first.CKur || "";
      await anagrpChanged(document.getElementById("anagrp"));
      if (document.getElementById("altgrp"))  document.getElementById("altgrp").value = first.Alt_Grup || '';
      if (document.getElementById("ozelkod")) document.getElementById("ozelkod").value = first.Ozel_Kod || '';
      if (document.getElementById("nakliyeci")) document.getElementById("nakliyeci").value = first.Nakliyeci || '';
      if (document.getElementById("tevoran")) document.getElementById("tevoran").value = first.CTevkifat || '0';
      if (document.getElementById("carikod")) document.getElementById("carikod").value = first.CCari_Firma || '';
      if (document.getElementById("adreskod")) document.getElementById("adreskod").value = first.CAdres_Firma || '';
      if (document.getElementById("dovizcins")) document.getElementById("dovizcins").value = first.CDoviz || '';
    }

    if (document.getElementById("a1")) document.getElementById("a1").value = data.a1 || "";
    if (document.getElementById("a2")) document.getElementById("a2").value = data.a2 || "";
    if (document.getElementById("not1")) document.getElementById("not1").value = data?.dipnot?.[0] || "";
    if (document.getElementById("not2")) document.getElementById("not2").value = data?.dipnot?.[1] || "";
    if (document.getElementById("not3")) document.getElementById("not3").value = data?.dipnot?.[2] || "";

    updatePaketM3();
    updateColumnTotal();

    if (typeof hesapAdiOgren === "function") hesapAdiOgren(document.getElementById("carikod")?.value || "", 'cariadilbl');
    if (typeof adrhesapAdiOgren === "function") adrhesapAdiOgren('adreskod', 'adresadilbl');

    if (errorDiv){
      errorDiv.style.display = "none";
      errorDiv.innerText = "";
    }

  } catch (error) {
    if (errorDiv){
      errorDiv.style.display = "block";
      errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
    }
  } finally {
    document.body.style.cursor = "default";
  }
}

/* ============ ROW DELETE ============ */
function satirsil(button) {
  const row = button.closest('tr');
  if (row) row.remove();
  updatePaketM3();
  updateColumnTotal();
}

/* ============ SON FIS / YENI FIS ============ */
async function sonfis() {
  document.body.style.cursor = "wait";
  try {
    const response = await fetchWithSessionCheck('kereste/sonfis', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ cins: 'CIKIS' }),
    });

    if (response?.errorMessage) throw new Error(response.errorMessage);

    const fisNoInput = document.getElementById('fisno');
    const errorDiv = document.getElementById('errorDiv');

    if (fisNoInput) fisNoInput.value = response.fisno || "";

    if (response.errorMessage) {
      if (errorDiv){ errorDiv.innerText = response.errorMessage; errorDiv.style.display = 'block'; }
    } else {
      if (errorDiv){ errorDiv.style.display = 'none'; errorDiv.innerText = ""; }
      clearInputs();
      kerOku();
    }
  } catch (error) {
    const errorDiv = document.getElementById('errorDiv');
    if (errorDiv){
      errorDiv.style.display = 'block';
      errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
    }
  } finally {
    document.body.style.cursor = "default";
  }
}

async function yeniFis() {
  const errorDiv = document.getElementById('errorDiv');
  if (errorDiv) errorDiv.innerText = "";

  clearInputs();
  document.body.style.cursor = "wait";

  try {
    const response = await fetchWithSessionCheck('kereste/yenifis', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ cins: 'CIKIS' }),
    });

    if (response?.errorMessage) throw new Error(response.errorMessage);

    const fisNoInput = document.getElementById('fisno');
    if (fisNoInput) fisNoInput.value = response.fisno || "";
  } catch (error) {
    if (errorDiv){
      errorDiv.style.display = "block";
      errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
    }
  } finally {
    document.body.style.cursor = "default";
  }
}

/* ============ ANA GRUP ============ */
async function anagrpChanged(anagrpElement) {
  const anagrup = anagrpElement?.value || "";
  const errorDiv = document.getElementById("errorDiv");
  const selectElement = document.getElementById("altgrp");

  if (selectElement) selectElement.innerHTML = '';

  if (anagrup === "") {
    if (selectElement) selectElement.disabled = true;
    return;
  }

  document.body.style.cursor = "wait";
  if (errorDiv){ errorDiv.style.display = "none"; errorDiv.innerText = ""; }

  try {
    const response = await fetchWithSessionCheck("kereste/altgrup", {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ anagrup }),
    });

    if (response?.errorMessage) throw new Error(response.errorMessage);

    (response.altKodlari || []).forEach(kod => {
      const option = document.createElement("option");
      option.value = kod.ALT_GRUP;
      option.textContent = kod.ALT_GRUP;
      selectElement.appendChild(option);
    });

    if (selectElement) selectElement.disabled = selectElement.options.length === 0;
  } catch (error) {
    if (selectElement) selectElement.disabled = true;
    if (errorDiv){
      errorDiv.style.display = "block";
      errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
    }
  } finally {
    document.body.style.cursor = "default";
  }
}

/* ============ YOKET ============ */
async function kerYoket() {
  const fisno = document.getElementById("fisno")?.value?.trim() || "";
  const table = document.getElementById('kerTable');
  const rows = table ? table.rows : [];

  if (!fisno || fisno === "0" || rows.length === 0) {
    alert("Geçerli bir evrak numarası giriniz.");
    return;
  }

  if (!confirm("Bu Fis silinecek ?")) return;

  document.body.style.cursor = "wait";
  const $silButton = $('#kersilButton');
  $silButton.prop('disabled', true).text('Siliniyor...');

  try {
    const response = await fetchWithSessionCheck("kereste/fiscYoket", {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ fisno }),
    });

    if (response?.errorMessage) throw new Error(response.errorMessage);

    clearInputs();
    const fisInput = document.getElementById("fisno");
    if (fisInput) fisInput.value = "";

    const errorDiv = document.getElementById("errorDiv");
    if (errorDiv){ errorDiv.style.display = "none"; errorDiv.innerText = ""; }

  } catch (error) {
    const errorDiv = document.getElementById("errorDiv");
    if (errorDiv){
      errorDiv.style.display = "block";
      errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
    }
  } finally {
    document.body.style.cursor = "default";
    $silButton.prop('disabled', false).text('Sil');
  }
}

/* ============ SAVE DTO ============ */
function prepareureKayit() {
  const keresteDTO = {
    fisno: document.getElementById("fisno")?.value || "",
    tarih: document.getElementById("fisTarih")?.value || "",
    ozelkod: document.getElementById("ozelkod")?.value || "",
    anagrup: document.getElementById("anagrp")?.value || "",
    altgrup: document.getElementById("altgrp")?.value || "",
    nakliyeci: document.getElementById("nakliyeci")?.value || "",

    carikod: document.getElementById("carikod")?.value || "",
    adreskod: document.getElementById("adreskod")?.value || "",

    dvzcins: document.getElementById("dovizcins")?.value || "",
    kur: parseLocaleNumber(document.getElementById("kur")?.value || 0) || 0.0,
    tevoran: parseLocaleNumber(document.getElementById("tevoran")?.value || 0) || 0.0,

    not1: document.getElementById("not1")?.value || "",
    not2: document.getElementById("not2")?.value || "",
    not3: document.getElementById("not3")?.value || "",

    acik1: document.getElementById("a1")?.value || "",
    acik2: document.getElementById("a2")?.value || "",
  };

  const tableData = getTableData();
  return { keresteDTO, tableData };
}

function getTableData() {
  const rows = getKerRows();
  const data = [];

  rows.forEach((row) => {
    const cells = row.querySelectorAll('td');
    const paketNo = cells[1]?.querySelector('input')?.value || "";

    if (paketNo.trim()) {
      const rowData = {
        paketno: paketNo,
        cdepostring: cells[7]?.querySelector('select')?.value || "",
        cfiat: parseLocaleNumber(cells[8]?.querySelector('input')?.value || 0),
        ciskonto: parseLocaleNumber(cells[9]?.querySelector('input')?.value || 0),
        ckdv: parseLocaleNumber(cells[10]?.querySelector('input')?.value || 0),
        ctutar: parseLocaleNumber(cells[11]?.querySelector('input')?.value || 0),
        cizahat: cells[12]?.querySelector('input')?.value || "",
        satir: parseInt(cells[13]?.textContent?.trim() || "0", 10) || 0,

        ukodu: cells[3]?.textContent?.trim() || "",
        miktar: parseInt(cells[4]?.textContent?.trim() || "0", 10) || 0,
        m3: parseFloat(cells[5]?.textContent?.trim() || "0") || 0.0,
        pakm3: parseFloat(cells[6]?.textContent?.trim() || "0") || 0.0,
      };
      data.push(rowData);
    }
  });

  return data;
}

/* ============ KAYIT ============ */
async function kerKayit() {
  const fisno = document.getElementById("fisno")?.value || "";
  const table = document.getElementById('kerTable');
  const rows = table ? table.rows : [];

  if (!fisno || fisno === "0" || rows.length === 0) {
    alert("Geçerli bir evrak numarası giriniz.");
    return;
  }

  const kerestekayitDTO = prepareureKayit();
  const errorDiv = document.getElementById('errorDiv');
  const $kaydetButton = $('#kerkaydetButton');

  $kaydetButton.prop('disabled', true).text('İşleniyor...');
  document.body.style.cursor = 'wait';

  try {
    const response = await fetchWithSessionCheck('kereste/cikKayit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(kerestekayitDTO),
    });

    if (response?.errorMessage) throw new Error(response.errorMessage);

    clearInputs();
    const fisInput = document.getElementById("fisno");
    if (fisInput) fisInput.value = "";

    if (errorDiv){ errorDiv.innerText = ""; errorDiv.style.display = 'none'; }
  } catch (error) {
    if (errorDiv){ errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu."; errorDiv.style.display = 'block'; }
  } finally {
    document.body.style.cursor = 'default';
    $kaydetButton.prop('disabled', false).text('Kaydet');
  }
}

/* ============ CARI ISLE ============ */
async function kercariIsle() {
  const hesapKodu = $('#kerBilgi').val();
  const fisno = document.getElementById("fisno")?.value || "";
  const table = document.getElementById('kerTable');
  const rows = table ? table.rows : [];

  if (!fisno || fisno === "0" || rows.length === 0) {
    alert("Geçerli bir evrak numarasi giriniz.");
    return;
  }

  const $carkaydetButton = $('#carkayitButton');
  $carkaydetButton.prop('disabled', true).text('İşleniyor...');

  const keresteDTO = {
    fisno,
    tarih: document.getElementById("fisTarih")?.value || "",
    carikod: document.getElementById("carikod")?.value || "",
    miktar: document.getElementById("totalM3")?.textContent || 0,
    tutar: parseLocaleNumber(document.getElementById("tevhartoptut")?.innerText || 0),
    karsihesapkodu: hesapKodu,
  };

  const errorDiv = document.getElementById('errorDiv');
  document.body.style.cursor = 'wait';

  try {
    const response = await fetchWithSessionCheck('kereste/kerccariKayit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(keresteDTO),
    });

    if (response?.errorMessage && response.errorMessage.trim() !== "") {
      throw new Error(response.errorMessage);
    }

    if (errorDiv){ errorDiv.innerText = ""; errorDiv.style.display = 'none'; }
  } catch (error) {
    if (errorDiv){ errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu."; errorDiv.style.display = 'block'; }
  } finally {
    $carkaydetButton.prop('disabled', false).text('Cari Kaydet');
    document.body.style.cursor = 'default';
  }
}

/* ============ DOWNLOAD ============ */
function cikisbilgiler(){
  return {
    totaltutar : document.getElementById("totalTutar")?.innerText?.trim() || "0.00",
    totalmiktar : document.getElementById("totalMiktar")?.innerText?.trim() || "0",
    totalm3 : document.getElementById("totalM3")?.innerText?.trim() || "0.000",
    totalpaketm3 : document.getElementById("totalPaketM3")?.innerText?.trim() || "0.000",
    paketsayi : (document.getElementById("totalPakadet")?.textContent || "").replace("Paket:", "").trim(),

    iskonto :  document.getElementById("iskonto")?.innerText?.trim() || "0.00",
    bakiye : document.getElementById("bakiye")?.innerText?.trim() || "0.00",
    kdv : document.getElementById("kdv")?.innerText?.trim() || "0.00",
    tevedkdv : document.getElementById("tevedkdv")?.innerText?.trim() || "0.00",
    tevdahtoptut : document.getElementById("tevdahtoptut")?.innerText?.trim() || "0.00",
    beyedikdv : document.getElementById("beyedikdv")?.innerText?.trim() || "0.00",
    tevhartoptut : document.getElementById("tevhartoptut")?.innerText?.trim() || "0.00",
  };
}

async function downloadcikis() {
  const fisno = document.getElementById("fisno")?.value || "";
  const table = document.getElementById('kerTable');
  const rows = table ? table.rows : [];

  if (!fisno || fisno === "0" || rows.length === 0) {
    alert("Geçerli bir evrak numarasi giriniz.");
    return;
  }

  const keresteyazdirDTO = {
    ...prepareureKayit(),
    cikisbilgiDTO: cikisbilgiler()
  };

  const errorDiv = document.getElementById('errorDiv');
  const $indirButton = $('#cikisdownloadButton');

  document.body.style.cursor = "wait";
  $indirButton.prop('disabled', true).text('İşleniyor...');

  try {
    const response = await fetchWithSessionCheckForDownload('kereste/cikis_download', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(keresteyazdirDTO),
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
    if (errorDiv){
      errorDiv.style.display = "block";
      errorDiv.innerText = error.message || "Bilinmeyen bir hata oluştu.";
    }
  } finally {
    $indirButton.prop('disabled', false).text('Yazdir');
    document.body.style.cursor = "default";
  }
}

/* ============ MAIL ============ */
async function cikismailAt() {
  localStorage.removeItem("tableData");
  localStorage.removeItem("grprapor");
  localStorage.removeItem("tablobaslik");

  const keresteyazdirDTO = {
    ...prepareureKayit(),
    cikisbilgiDTO: cikisbilgiler()
  };

  localStorage.setItem("keresteyazdirDTO", JSON.stringify(keresteyazdirDTO));

  const degerler = "kercikis";
  const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
  mailsayfasiYukle(url);
}
