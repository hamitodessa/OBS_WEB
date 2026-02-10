/* =========================
   KAMBİYO – CEK GIRIS (çakışmasız)
   Namespace: OBS.CEKGIR
   File: /obs_js_files/kambiyo/cekgiris.js
   ========================= */
window.OBS ||= {};
OBS.CEKGIR ||= {};

/* ---------- helpers ---------- */
OBS.CEKGIR._root = () =>
    document.getElementById("kmb_content") ||
    document.getElementById("ara_content") ||
    document;

OBS.CEKGIR.byId = (id) => OBS.CEKGIR._root().querySelector("#" + id);

OBS.CEKGIR._cursor = (wait) => { document.body.style.cursor = wait ? "wait" : "default"; };

OBS.CEKGIR._setErr = (msg) => {
    const e = OBS.CEKGIR.byId("errorDiv") || document.getElementById("errorDiv");
    if (!e) return;
    if (msg) { e.style.display = "block"; e.innerText = msg; }
    else { e.style.display = "none"; e.innerText = ""; }
};

OBS.CEKGIR._btnBusy = (id, busy, busyText, idleText) => {
    const b = OBS.CEKGIR.byId(id) || document.getElementById(id);
    if (!b) return;
    b.disabled = !!busy;
    if (busy && busyText) b.textContent = busyText;
    if (!busy && idleText) b.textContent = idleText;
};

/* ---------- state (global yerine) ---------- */
OBS.CEKGIR.state = {
    rowCounter: 0,
    bankaIsimleri: [],
    subeIsimleri: [],
    ilkBorclu: [],
    topPara: 0
};

/* =========================
   INIT
   ========================= */
OBS.CEKGIR.init = function() {
    // İstersen sayfa açılınca otomatik çağır:
    OBS.CEKGIR.fetchBankaSubeOnce();

};

/* ---------- counter ---------- */
OBS.CEKGIR.incrementRowCounter = function() {
    OBS.CEKGIR.state.rowCounter++;
};

/* =========================
   fetchBankaSubeOnce
   ========================= */
OBS.CEKGIR.fetchBankaSubeOnce = async function() {
    const errorDiv2Text = (OBS.CEKGIR.byId("errorDiv2") || document.getElementById("errorDiv2"))?.textContent || "";
    if (errorDiv2Text.trim() !== "") return;

    OBS.CEKGIR._setErr("");

    OBS.CEKGIR.state.rowCounter = 0;
    OBS.CEKGIR.state.bankaIsimleri = [];
    OBS.CEKGIR.state.subeIsimleri = [];
    OBS.CEKGIR.state.ilkBorclu = [];

    try {
        const response = await fetchWithSessionCheck("kambiyo/kamgetDegiskenler", {
            method: "GET",
            headers: { "Content-Type": "application/json" }
        });
        if (response?.errorMessage) throw new Error(response.errorMessage);

        OBS.CEKGIR.state.bankaIsimleri = response.bankaIsmi || [];
        OBS.CEKGIR.state.subeIsimleri = response.subeIsmi || [];
        OBS.CEKGIR.state.ilkBorclu = response.ilkBorclu || [];

        OBS.CEKGIR.initializeRows();
    } catch (error) {
        OBS.CEKGIR._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
    }
};

OBS.CEKGIR.initializeRows = function() {
    OBS.CEKGIR.state.rowCounter = 0;
    for (let i = 0;i < 5;i++) {
        OBS.CEKGIR.cekgiraddRow();
    }
};

OBS.CEKGIR.cekgiraddRow = function() {
    const table = OBS.CEKGIR.byId("gbTable") || document.getElementById("gbTable");
    const tbody = table?.getElementsByTagName("tbody")?.[0];
    if (!tbody) return;

    const newRow = tbody.insertRow();
    OBS.CEKGIR.incrementRowCounter();
    const rc = OBS.CEKGIR.state.rowCounter;

    const optionsHTML = OBS.CEKGIR.state.bankaIsimleri.map(kod => `<option value="${kod.Banka}">${kod.Banka}</option>`).join("");
    const subeHTML = OBS.CEKGIR.state.subeIsimleri.map(kod => `<option value="${kod.Sube}">${kod.Sube}</option>`).join("");
    const ilkHTML = OBS.CEKGIR.state.ilkBorclu.map(kod => `<option value="${kod.Ilk_Borclu}">${kod.Ilk_Borclu}</option>`).join("");

    newRow.innerHTML = `
    <td>
      <button id="bsatir_${rc}" type="button" class="btn btn-secondary ml-2"
              onclick="OBS.CEKGIR.cekgirremoveRow(this)">
        <i class="fa fa-trash"></i>
      </button>
    </td>
    <td>
      <div style="position: relative; width: 100%;">
        <input class="form-control cins_bold" maxlength="10" id="cekno_${rc}"
               onkeydown="OBS.CEKGIR.focusNextCell(event,this)"
               onchange="OBS.CEKGIR.cekkontrol(this)">
      </div>
    </td>
    <td>
      <input type="date" class="form-control" onkeydown="OBS.CEKGIR.focusNextCell(event, this)">
    </td>
    <td>
      <div style="position: relative; width: 100%;">
        <input class="form-control cins_bold" list="bankaOptions_${rc}" maxlength="25" id="banka_${rc}"
               onkeydown="OBS.CEKGIR.focusNextCell(event, this)">
        <datalist id="bankaOptions_${rc}">${optionsHTML}</datalist>
        <span style="position:absolute; top:50%; right:10px; transform:translateY(-50%); pointer-events:none;"> ▼ </span>
      </div>
    </td>
    <td>
      <div style="position: relative; width: 100%;">
        <input class="form-control cins_bold" list="subeOptions_${rc}" maxlength="25" id="sube_${rc}"
               onkeydown="OBS.CEKGIR.focusNextCell(event, this)">
        <datalist id="subeOptions_${rc}">${subeHTML}</datalist>
        <span style="position:absolute; top:50%; right:10px; transform:translateY(-50%); pointer-events:none;"> ▼ </span>
      </div>
    </td>
    <td>
      <input class="form-control cins_bold" maxlength="15" id="serino_${rc}" onkeydown="OBS.CEKGIR.focusNextCell(event, this)">
    </td>
    <td>
      <div style="position: relative; width: 100%;">
        <input class="form-control cins_bold" list="ilkborcluOptions_${rc}" maxlength="30" id="ilkborclu_${rc}"
               onkeydown="OBS.CEKGIR.focusNextCell(event, this)">
        <datalist id="ilkborcluOptions_${rc}">${ilkHTML}</datalist>
        <span style="position:absolute; top:50%; right:10px; transform:translateY(-50%); pointer-events:none;"> ▼ </span>
      </div>
    </td>
    <td>
      <input class="form-control cins_bold" maxlength="15" id="hesap_${rc}" onkeydown="OBS.CEKGIR.focusNextCell(event, this)">
    </td>
    <td>
      <input class="form-control cins_bold" maxlength="3" id="cins_${rc}" onkeydown="OBS.CEKGIR.focusNextCell(event, this)">
    </td>
    <td>
      <input class="form-control" onfocus="OBS.CEKGIR.selectAllContent(this)" onblur="OBS.CEKGIR.handleBlur(this)"
             onkeydown="OBS.CEKGIR.focusNextRow(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
    </td>
    <td style="display: none;"></td>
    <td style="display: none;">1900-01-01</td>
    <td style="display: none;"></td>
    <td style="display: none;"></td>
    <td style="display: none;">1900-01-01</td>
    <td style="display: none;"></td>
  `;
};

OBS.CEKGIR.handleBlur = function(input) {
    input.value = formatNumber2(parseLocaleNumber(input.value));
    OBS.CEKGIR.updateColumnTotal();
};

OBS.CEKGIR.cekgirremoveRow = function(button) {
    button.closest("tr")?.remove();
    OBS.CEKGIR.updateColumnTotal();
};

OBS.CEKGIR.selectAllContent = function(element) {
    const range = document.createRange();
    const selection = window.getSelection();
    range.selectNodeContents(element);
    selection.removeAllRanges();
    selection.addRange(range);
};

OBS.CEKGIR.updateColumnTotal = function() {
    const cells = document.querySelectorAll("tr td:nth-child(10) input");
    const totalTutarCell = OBS.CEKGIR.byId("totalTutar") || document.getElementById("totalTutar");
    const totalceksayisi = OBS.CEKGIR.byId("ceksayisi") || document.getElementById("ceksayisi");

    let total = 0;
    let totaladet = 0;

    cells.forEach(input => {
        const value = parseFloat(String(input.value).replace(/,/g, "").trim());
        if (!isNaN(value) && value > 0) {
            total += value;
            totaladet += 1;
        }
    });

    if (totalceksayisi) {
        totalceksayisi.innerText = totaladet.toLocaleString(undefined, {
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        });
    }

    if (totalTutarCell) {
        totalTutarCell.textContent = total.toLocaleString(undefined, {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }
};

OBS.CEKGIR.focusNextRow = function(event, element) {
    if (event.key !== "Enter") return;
    event.preventDefault();

    const currentRow = element.closest("tr");
    const nextRow = currentRow?.nextElementSibling;

    if (nextRow) {
        const secondInput = nextRow.querySelector("td:nth-child(2) input");
        if (secondInput) {
            secondInput.focus();
            secondInput.select?.();
        }
    } else {
        OBS.CEKGIR.cekgiraddRow();
        const tbody = currentRow?.parentElement;
        const newRow = tbody?.lastElementChild;
        const secondInput = newRow?.querySelector("td:nth-child(2) input");
        if (secondInput) {
            secondInput.focus();
            secondInput.select?.();
        }
    }
};

OBS.CEKGIR.focusNextCell = function(event, element) {
    if (event.key !== "Enter") return;
    event.preventDefault();

    const currentCell = element.closest("td");
    const nextCell = currentCell?.nextElementSibling;
    if (!nextCell) return;

    const focusableElement = nextCell.querySelector("input, [contenteditable='true']");
    if (focusableElement) {
        focusableElement.focus();
        if (focusableElement.select) focusableElement.select();
    }
};

/* =========================
   clearInputs
   ========================= */
OBS.CEKGIR.clearInputs = function() {
    (OBS.CEKGIR.byId("ozelkod") || document.getElementById("ozelkod"))?.value && ((OBS.CEKGIR.byId("ozelkod") || document.getElementById("ozelkod")).value = "");
    (OBS.CEKGIR.byId("dvzcinsi") || document.getElementById("dvzcinsi"))?.value && ((OBS.CEKGIR.byId("dvzcinsi") || document.getElementById("dvzcinsi")).value = "");
    (OBS.CEKGIR.byId("bcheskod") || document.getElementById("bcheskod"))?.value && ((OBS.CEKGIR.byId("bcheskod") || document.getElementById("bcheskod")).value = "");

    const lblcheskod = OBS.CEKGIR.byId("lblcheskod") || document.getElementById("lblcheskod");
    if (lblcheskod) lblcheskod.innerText = "";

    const lblfaiztutari = OBS.CEKGIR.byId("lblfaiztutari") || document.getElementById("lblfaiztutari");
    if (lblfaiztutari) lblfaiztutari.innerText = "0.00";

    const faiz = OBS.CEKGIR.byId("faiz") || document.getElementById("faiz");
    if (faiz) faiz.value = "";

    const lblortgun = OBS.CEKGIR.byId("lblortgun") || document.getElementById("lblortgun");
    if (lblortgun) lblortgun.innerText = "0";

    const aciklama1 = OBS.CEKGIR.byId("aciklama1") || document.getElementById("aciklama1");
    const aciklama2 = OBS.CEKGIR.byId("aciklama2") || document.getElementById("aciklama2");
    if (aciklama1) aciklama1.value = "";
    if (aciklama2) aciklama2.value = "";

    const tableBody = OBS.CEKGIR.byId("tbody") || document.getElementById("tbody");
    if (tableBody) tableBody.innerHTML = "";

    OBS.CEKGIR.state.rowCounter = 0;
    OBS.CEKGIR.initializeRows();

    const totalTutarCell = OBS.CEKGIR.byId("totalTutar") || document.getElementById("totalTutar");
    if (totalTutarCell) totalTutarCell.textContent = formatNumber2(0);
};

/* =========================
   songirisBordro
   ========================= */
OBS.CEKGIR.songirisBordro = async function() {
    try {
        const response = await fetchWithSessionCheck("kambiyo/sonbordroNo", {
            method: "GET",
            headers: { "Content-Type": "application/x-www-form-urlencoded" }
        });

        const bordroNo = response?.bordroNo;
        const bordronoInput = OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono");
        const errorDiv = OBS.CEKGIR.byId("errorDiv") || document.getElementById("errorDiv");

        if (bordronoInput) bordronoInput.value = bordroNo;

        if (bordroNo?.errorMessage) {
            if (errorDiv) { errorDiv.innerText = bordroNo.errorMessage; errorDiv.style.display = "block"; }
        } else {
            OBS.CEKGIR._setErr("");
            OBS.CEKGIR.clearInputs();
            await OBS.CEKGIR.bordroOku();
        }
    } catch (error) {
        OBS.CEKGIR._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
    }
};

OBS.CEKGIR.bordroOkuma = function(event) {
    if (event.key === "Enter" || event.keyCode === 13) {
        OBS.CEKGIR.bordroOku();
    }
};

/* =========================
   bordroOku
   ========================= */
OBS.CEKGIR.bordroOku = async function() {
    const bordronoEl = OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono");
    const bordroNo = bordronoEl?.value;

    if (!bordroNo) return;

    OBS.CEKGIR._cursor(true);

    try {
        const response = await fetchWithSessionCheck("kambiyo/bordroOku", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ bordroNo })
        });

        const data = response;
        OBS.CEKGIR.clearInputs();

        const dataSize = data?.data?.length || 0;
        if (dataSize === 0) return;

        if (data?.errorMessage) {
            OBS.CEKGIR._setErr(data.errorMessage);
            return;
        }

        const table = OBS.CEKGIR.byId("gbTable") || document.getElementById("gbTable");
        const tableBody = OBS.CEKGIR.byId("tbody") || document.getElementById("tbody");
        if (tableBody) tableBody.innerHTML = "";

        const rows0 = table?.querySelectorAll("tbody tr") || [];
        if (data.data.length > rows0.length) {
            const additionalRows = data.data.length - rows0.length;
            for (let i = 0;i < additionalRows + 1;i++) OBS.CEKGIR.cekgiraddRow();
        }

        const rowss = table?.querySelectorAll("tbody tr") || [];

        data.data.forEach((item, index) => {
            const cells = rowss[index]?.cells;
            if (!cells) return;

            const ceknoInput = cells[1]?.querySelector("input");
            if (ceknoInput) ceknoInput.value = item.Cek_No || "";

            const vadeInput = cells[2]?.querySelector("input");
            if (vadeInput) vadeInput.value = item.Vade;

            const bankaInput = cells[3]?.querySelector("input");
            if (bankaInput) bankaInput.value = item.Banka || "";

            const subeInput = cells[4]?.querySelector("input");
            if (subeInput) subeInput.value = item.Sube || "";

            const serinoInput = cells[5]?.querySelector("input");
            if (serinoInput) serinoInput.value = item.Seri_No;

            const ilkborcluInput = cells[6]?.querySelector("input");
            if (ilkborcluInput) ilkborcluInput.value = item.Ilk_Borclu || "";

            const cekhspnoInput = cells[7]?.querySelector("input");
            if (cekhspnoInput) cekhspnoInput.value = item.Cek_Hesap_No || "";

            const cinsInput = cells[8]?.querySelector("input");
            if (cinsInput) cinsInput.value = item.Cins || "";

            const tutarInput = cells[9]?.querySelector("input");
            if (tutarInput) tutarInput.value = formatNumber2(item.Tutar);

            // gizli kolonlar
            cells[10].innerText = item.Cikis_Bordro || "";
            cells[11].innerText = item.Cikis_Tarihi;
            cells[12].innerText = item.Cikis_Musteri || "";
            cells[13].innerText = item.Durum || "";
            cells[14].innerText = item.T_Tarih;
            cells[15].innerText = item.Cikis_Ozel_Kod || "";
        });

        OBS.CEKGIR.updateColumnTotal();

        // üst alanlar
        (OBS.CEKGIR.byId("ozelkod") || document.getElementById("ozelkod")).value = data.data[0].Giris_Ozel_Kod;
        (OBS.CEKGIR.byId("dvzcinsi") || document.getElementById("dvzcinsi")).value = data.data[0].Cins;
        (OBS.CEKGIR.byId("bcheskod") || document.getElementById("bcheskod")).value = data.data[0].Giris_Musteri;
        (OBS.CEKGIR.byId("bordroTarih") || document.getElementById("bordroTarih")).value = data.data[0].Giris_Tarihi;

        const ches = OBS.CEKGIR.byId("bcheskod") || document.getElementById("bcheskod");
        ches?.oninput?.();

        const responseAciklama = await fetchWithSessionCheck("kambiyo/kamgetAciklama", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ bordroNo })
        });

        const dataAciklama = responseAciklama;
        (OBS.CEKGIR.byId("aciklama1") || document.getElementById("aciklama1")).value = dataAciklama.aciklama1;
        (OBS.CEKGIR.byId("aciklama2") || document.getElementById("aciklama2")).value = dataAciklama.aciklama2;

        OBS.CEKGIR._setErr("");

    } catch (error) {
        OBS.CEKGIR._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
        OBS.CEKGIR._cursor(false);
    }
};

/* =========================
   getTableData
   ========================= */
OBS.CEKGIR.getTableData = function() {
    const table = OBS.CEKGIR.byId("gbTable") || document.getElementById("gbTable");
    const rows = table?.querySelectorAll("tbody tr") || [];
    const data = [];

    rows.forEach((row) => {
        const cells = row.querySelectorAll("td");
        const cekNo = cells[1]?.querySelector("input")?.value || "";
        const tutarVal = parseFloat(cells[9]?.querySelector("input")?.value || "0");

        if (!cekNo || tutarVal <= 0) return;

        const rowData = {
            cekNo,
            girisBordro: (OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono")).value,
            girisTarihi: (OBS.CEKGIR.byId("bordroTarih") || document.getElementById("bordroTarih")).value,
            girisOzelKod: (OBS.CEKGIR.byId("ozelkod") || document.getElementById("ozelkod")).value,
            girisMusteri: (OBS.CEKGIR.byId("bcheskod") || document.getElementById("bcheskod")).value,
            vade: cells[2]?.querySelector("input")?.value || "",
            banka: cells[3]?.querySelector("input")?.value || "",
            sube: cells[4]?.querySelector("input")?.value || "",
            seriNo: cells[5]?.querySelector("input")?.value || "",
            ilkBorclu: cells[6]?.querySelector("input")?.value || "",
            cekHesapNo: cells[7]?.querySelector("input")?.value || "",
            cins: cells[8]?.querySelector("input")?.value || "",
            tutar: parseLocaleNumber(cells[9]?.querySelector("input")?.value || "0"),
            cikisBordro: cells[10]?.textContent || "",
            cikisTarihi: cells[11]?.textContent || "",
            cikisMusteri: cells[12]?.textContent || "",
            durum: cells[13]?.textContent || "",
            ttarih: cells[14]?.textContent || "",
            cikisOzelKod: cells[15]?.textContent || ""
        };

        data.push(rowData);
    });

    return data;
};

OBS.CEKGIR.prepareBordroKayit = function() {
    const girisbordroDTO = {
        bordroNo: (OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono")).value,
        aciklama1: (OBS.CEKGIR.byId("aciklama1") || document.getElementById("aciklama1")).value || "",
        aciklama2: (OBS.CEKGIR.byId("aciklama2") || document.getElementById("aciklama2")).value || ""
    };
    const tableData = OBS.CEKGIR.getTableData();
    return { girisbordroDTO, tableData };
};

/* =========================
   gbKayit
   ========================= */
OBS.CEKGIR.gbKayit = async function() {
    const bordroNo = (OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono"))?.value?.trim();
    if (!bordroNo || bordroNo === "0") { alert("Geçerli bir evrak numarası giriniz."); return; }

    const bordroKayitDTO = OBS.CEKGIR.prepareBordroKayit();

    OBS.CEKGIR._cursor(true);
    OBS.CEKGIR._setErr("");

    try {
        const response = await fetchWithSessionCheck("kambiyo/gbordroKayit", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(bordroKayitDTO)
        });

        if (response?.errorMessage?.trim() !== "") throw new Error(response.errorMessage);

        OBS.CEKGIR.clearInputs();
        (OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono")).value = "";
        OBS.CEKGIR._setErr("");

    } catch (error) {
        OBS.CEKGIR._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
        OBS.CEKGIR._cursor(false);
    }
};

/* =========================
   gbYoket
   ========================= */
OBS.CEKGIR.gbYoket = async function() {
    const bordroNo = (OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono"))?.value?.trim();
    if (!bordroNo || bordroNo === "0") { alert("Geçerli bir evrak numarası giriniz."); return; }

    if (!confirm("Bu Bordroyu silmek istediğinize emin misiniz?")) return;

    OBS.CEKGIR._cursor(true);
    OBS.CEKGIR._setErr("");

    try {
        const response = await fetchWithSessionCheck("kambiyo/bordroYoket", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ bordroNo })
        });

        if (response?.errorMessage?.trim() !== "") throw new Error(response.errorMessage);

        OBS.CEKGIR.clearInputs();
        (OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono")).value = "";
        OBS.CEKGIR._setErr("");

    } catch (error) {
        OBS.CEKGIR._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
        OBS.CEKGIR._cursor(false);
    }
};

/* =========================
   ortgun
   ========================= */
OBS.CEKGIR.ortgun = function() {
    const table = OBS.CEKGIR.byId("gbTable") || document.getElementById("gbTable");
    const rows = table?.querySelectorAll("tbody tr") || [];

    const gunlukTarih = (OBS.CEKGIR.byId("bordroTarih") || document.getElementById("bordroTarih")).value;
    const faizoran = parseLocaleNumber((OBS.CEKGIR.byId("faiz") || document.getElementById("faiz")).value);

    let topPara = 0;
    let tfaiz = 0;
    let orgun = 0;

    rows.forEach((row) => {
        const cells = row.querySelectorAll("td");
        const tutar = parseLocaleNumber(cells[9]?.querySelector("input")?.value || "0");
        const vade = cells[2]?.querySelector("input")?.value;

        if (tutar > 0 && vade && gunlukTarih) {
            const gunfarki = OBS.CEKGIR.tarihGunFarki(gunlukTarih, vade);
            const faiz = (((tutar * faizoran) / 365) * gunfarki) / 100;
            topPara += tutar;
            tfaiz += faiz;
            orgun = ((topPara * faizoran) / 365) / 100;
        }
    });

    const lblortgun = OBS.CEKGIR.byId("lblortgun") || document.getElementById("lblortgun");
    const lblfaiztutari = OBS.CEKGIR.byId("lblfaiztutari") || document.getElementById("lblfaiztutari");

    if (lblortgun) lblortgun.innerText = (tfaiz / orgun).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    if (lblfaiztutari) lblfaiztutari.innerText = tfaiz.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};

OBS.CEKGIR.tarihGunFarki = function(tarih1, tarih2) {
    const date1 = new Date(tarih1);
    const date2 = new Date(tarih2);
    const farkMs = date2 - date1;
    return Math.ceil(farkMs / (1000 * 60 * 60 * 24));
};

/* =========================
   yeniBordro
   ========================= */
OBS.CEKGIR.yeniBordro = async function() {
    const errorDiv = OBS.CEKGIR.byId("errorDiv") || document.getElementById("errorDiv");

    try {
        const response = await fetchWithSessionCheck("kambiyo/yeniBordro", {
            method: "GET",
            headers: { "Content-Type": "application/x-www-form-urlencoded" }
        });

        const bordronoInput = OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono");
        if (bordronoInput) bordronoInput.value = response?.bordroNo;

        if (response?.errorMessage?.trim() !== "") throw new Error(response.errorMessage);

        if (errorDiv) { errorDiv.innerText = ""; errorDiv.style.display = "none"; }
        OBS.CEKGIR.clearInputs();

    } catch (error) {
        OBS.CEKGIR._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
    }
};

/* =========================
   gbDownload (jQuery temiz)
   ========================= */
OBS.CEKGIR.gbDownload = async function() {
    const bordroNo = (OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono"))?.value?.trim();
    if (!bordroNo || bordroNo === "0") { alert("Geçerli bir evrak numarası giriniz."); return; }

    const table = OBS.CEKGIR.byId("gbTable") || document.getElementById("gbTable");
    const rows = table?.querySelectorAll("tbody tr") || [];

    let topPara = 0;
    rows.forEach((row) => {
        const tutar = parseLocaleNumber(row.querySelector("td:nth-child(10) input")?.value || "0");
        topPara += tutar;
    });

    const bordroPrinter = {
        girisBordro: (OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono")).value,
        girisTarihi: (OBS.CEKGIR.byId("bordroTarih") || document.getElementById("bordroTarih")).value,
        girisMusteri: (OBS.CEKGIR.byId("bcheskod") || document.getElementById("bcheskod")).value,
        unvan: (OBS.CEKGIR.byId("lblcheskod") || document.getElementById("lblcheskod")).innerText || "",
        dvzcins: (OBS.CEKGIR.byId("dvzcinsi") || document.getElementById("dvzcinsi")).value || "",
        tutar: topPara || 0
    };

    OBS.CEKGIR._setErr("");
    OBS.CEKGIR._cursor(true);
    OBS.CEKGIR._btnBusy("indirButton", true, "İşleniyor...", "Rapor İndir");

    try {
        const response = await fetchWithSessionCheckForDownload("kambiyo/bordro_download", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(bordroPrinter)
        });

        if (response?.blob) {
            const disposition = response.headers.get("Content-Disposition") || "";
            const m = disposition.match(/filename="(.+)"/);
            const fileName = m ? m[1] : "bordro.xlsx";

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
        OBS.CEKGIR._setErr(error?.message || "Bilinmeyen bir hata oluştu.");
    } finally {
        OBS.CEKGIR._btnBusy("indirButton", false, "İşleniyor...", "Rapor İndir");
        OBS.CEKGIR._cursor(false);
    }
};

/* =========================
   cekkontrol
   ========================= */
OBS.CEKGIR.cekkontrol = async function(element) {
    const cellValue = element?.value || "";
    OBS.CEKGIR._setErr("");
    OBS.CEKGIR._cursor(true);

    try {
        const response = await fetchWithSessionCheck("kambiyo/cekkontrol", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ cekNo: cellValue })
        });

        if (response?.errorMessage?.trim() !== "") throw new Error(response.errorMessage);

        const gelenbordro = (response?.bordroNo || "").trim();
        const bordrono = (OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono")).value;

        if (gelenbordro === "") {
            alert("Bu Numarada Cek Kaydi Bulunamadi");
            return;
        }
        if (gelenbordro !== bordrono) {
            alert("Bu cek daha onceden giris yapilmis Bordro No:" + gelenbordro);
        }
    } catch (error) {
        OBS.CEKGIR._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
        OBS.CEKGIR._cursor(false);
    }
};

/* =========================
   cekcariIsle (jQuery temiz)
   ========================= */
OBS.CEKGIR.cekcariIsle = async function() {
    const hesapKodu = (OBS.CEKGIR.byId("cekgirBilgi") || document.getElementById("cekgirBilgi"))?.value || "";
    const bordroNo = (OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono"))?.value?.trim();

    if (!bordroNo || bordroNo === "0") { alert("Geçerli bir evrak numarası giriniz."); return; }

    const bordroKayitDTO = OBS.CEKGIR.prepareBordroKayit();
    bordroKayitDTO.girisbordroDTO.hesapKodu = hesapKodu;

    OBS.CEKGIR._setErr("");
    OBS.CEKGIR._cursor(true);

    try {
        const response = await fetchWithSessionCheck("kambiyo/gbordroCariKayit", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(bordroKayitDTO)
        });

        if (response?.errorMessage?.trim() !== "") throw new Error(response.errorMessage);

        OBS.CEKGIR._setErr("");
    } catch (error) {
        OBS.CEKGIR._setErr(error?.message || "Beklenmeyen bir hata oluştu.");
    } finally {
        OBS.CEKGIR._cursor(false);
    }
};

/* =========================
   gbmailAt  (jQuery yoktu, aynı)
   ========================= */
OBS.CEKGIR.gbmailAt = function() {
    const bordroNo = (OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono"))?.value?.trim();
    if (!bordroNo || bordroNo === "0") { alert("Geçerli bir evrak numarası giriniz."); return; }

    const table = OBS.CEKGIR.byId("gbTable") || document.getElementById("gbTable");
    const rows = table?.querySelectorAll("tbody tr") || [];

    let topPara = 0;
    rows.forEach((row) => {
        const tutar = parseLocaleNumber(row.querySelector("td:nth-child(10) input")?.value || "0");
        topPara += tutar;
    });

    const girisBordro = (OBS.CEKGIR.byId("bordrono") || document.getElementById("bordrono")).value;
    const girisTarihi = (OBS.CEKGIR.byId("bordroTarih") || document.getElementById("bordroTarih")).value;
    const girisMusteri = (OBS.CEKGIR.byId("bcheskod") || document.getElementById("bcheskod")).value;
    const unvan = (OBS.CEKGIR.byId("lblcheskod") || document.getElementById("lblcheskod")).innerText || "";
    const dvzcins = (OBS.CEKGIR.byId("dvzcinsi") || document.getElementById("dvzcinsi")).value || "";
    const tutar = topPara || 0;

    const degerler = girisBordro + "," + girisTarihi + "," + girisMusteri + "," + unvan + "," + dvzcins + "," + tutar + ",cgbordro";
    const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;
    mailsayfasiYukle(url);
};
