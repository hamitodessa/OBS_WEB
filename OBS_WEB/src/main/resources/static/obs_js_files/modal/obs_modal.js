/* =========================
   MODAL + FORM HELPERS (NO JQUERY)
   Bootstrap 5
   ========================= */

let activeNestedInputId = null;
let nerden = null;

/* ---------- DOM helpers ---------- */
const byId = (id) => document.getElementById(id);
const val = (id) => (byId(id)?.value ?? "").trim();
const valRaw = (id) => (byId(id)?.value ?? "");
const checked = (id) => !!byId(id)?.checked;
const selIndex = (id) => byId(id)?.selectedIndex ?? 0;

function setHiddenInAraContent(hiddenId, value) {
  // ara_content içindeki hidden inputları yakala
  const el = document.querySelector(`#ara_content #${hiddenId}`);
  if (el) el.value = value ?? "";
}

/* ---------- Bootstrap modal helpers ---------- */
function modalShowByEl(el) {
  if (!el) return;
  bootstrap.Modal.getOrCreateInstance(el).show();
}
function modalHideByEl(el) {
  if (!el) return;
  bootstrap.Modal.getInstance(el)?.hide();
}
function modalShow(id) { modalShowByEl(byId(id)); }
function modalHide(id) { modalHideByEl(byId(id)); }

/* =========================
   FIRST MODAL OPEN
   ========================= */
function openFirstModal(nerdenGeldi) {
  // bazı ekranlarda fis/bordro no yoksa açma
  if (nerdenGeldi === "cekgir" || nerdenGeldi === "cekcik") {
    const bordroNo = val("bordrono");
    if (!bordroNo || bordroNo === "0") return;
  } else if (nerdenGeldi === "tahsilatckaydet") {
    const bordroNo = val("tahevrakNo");
    if (!bordroNo || bordroNo === "0") return;
  } else if (nerdenGeldi === "fatura") {
    const fisno = val("fisno");
    if (!fisno || fisno === "0") return;
  } else if (nerdenGeldi === "irsaliye") {
    const fisno = val("fisno");
    if (!fisno || fisno === "0") return;
  } else if (nerdenGeldi === "kerestegiris") {
    const fisno = val("fisno");
    if (!fisno || fisno === "0") return;
  }

  const modal = byId("firstModal");
  nerden = nerdenGeldi;

  // senin özel modal açıcıların
  if (nerden === "tahsilatrapor") {
    OBS.TAHRAP.opentahrapModal(modal);
  } else if (nerden === "fatrapor") {
    openfatrapModal(modal);
  } else if (nerden === "irsrapor") {
    openirsrapModal(modal);
  } else if (nerden === "imarapor") {
    openimarapModal(modal);
  } else if (
    nerden === "envanter" ||
    nerden === "stokrapor" ||
    nerden === "grprapor" ||
    nerden === "imagrprapor" ||
    nerden === "stokdetayrapor" ||
    nerden === "kerestedetayrapor" ||
    nerden === "kergrprapor" ||
    nerden === "kerfatrapor" ||
    nerden === "kerenvanter" ||
    nerden === "kerortfiat"
  ) {
    openenvModal(modal);
  } else {
    // default: modal göster
    modalShowByEl(modal);
  }
}

/* =========================
   SECOND MODAL OPEN (HSP PLAN)
   ========================= */
async function openSecondModal(inputId, secondnerden) {
  activeNestedInputId = inputId;

  modalShow("secondModal");

  const modalError = byId("hsperrorDiv");
  if (modalError) {
    modalError.style.display = "none";
    modalError.innerText = "";
  }

  document.body.style.cursor = "wait";

  try {
    const response = await fetchWithSessionCheck("modal/hsppln");
    if (response?.errorMessage) throw new Error(response.errorMessage);

    const data = response;
    const tableBody = byId("modalTableBody");
    if (!tableBody) return;

    tableBody.innerHTML = "";

    if (!data || data.length === 0) {
      if (modalError) {
        modalError.style.display = "block";
        modalError.innerText = "Hiç veri bulunamadı.";
      }
      return;
    }

    data.forEach((row) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${row.HESAP || ""}</td>
        <td>${row.UNVAN || ""}</td>
        <td>${row.HESAP_CINSI || ""}</td>
        <td>${row.KARTON || ""}</td>
      `;
      tr.addEventListener("click", () => selectValue(inputId, row.HESAP, secondnerden));
      tableBody.appendChild(tr);
    });

  } catch (error) {
    if (modalError) {
      modalError.style.display = "block";
      modalError.innerText = `Bir hata oluştu: ${error.message}`;
    }
  } finally {
    document.body.style.cursor = "default";
    setTimeout(() => {
      const searchInput = byId("modalSearch");
      if (searchInput) searchInput.focus();
    }, 200);
  }
}

function filterTable() {
  const searchValue = valRaw("modalSearch").toLowerCase();
  document.querySelectorAll("#modalTable tbody tr").forEach((row) => {
    const rowText = Array.from(row.cells).map((c) => c.textContent.toLowerCase()).join(" ");
    row.style.display = rowText.includes(searchValue) ? "" : "none";
  });
}

function selectValue(inputId, selectedValue, secondnerden) {
  const inputElement = byId(inputId);
  if (!inputElement) return;

  inputElement.value = selectedValue;

  // eski mantık: bazı ekranlar oninput tetikliyor
  if (
    ["dekont", "tahsilat", "cekgir", "cekcik", "tahsilatckaydet", "carikoddegis", "fatura", "irsaliye"]
      .includes(secondnerden)
  ) {
    if (typeof inputElement.oninput === "function") inputElement.oninput();
  } else if (secondnerden === "gunlukkontrol") {
    if (typeof inputElement.oninput === "function") inputElement.oninput();
    if (typeof belgeoku === "function") belgeoku();
  }

  const ms = byId("modalSearch");
  if (ms) ms.value = "";

  modalHide("secondModal");
}

/* =========================
   SAVE (FIRST MODAL -> MAIN)
   ========================= */
function saveToMain() {

  // --- 1) Nereden’e göre hidden field doldur ---
  if (nerden === "mizan") {
    const degerler = [
      val("fhkodu1"), val("fhkodu2"),
      val("startDate"), val("endDate"),
      val("cins1"), val("cins2"),
      val("karton1"), val("karton2"),
      val("hangi_tur")
    ].join(",");
    setHiddenInAraContent("mizanBilgi", degerler);
  }

  else if (nerden === "ekstre") {
    const degerler = [val("fhkodu1"), val("startDate"), val("endDate")].join(",");
    setHiddenInAraContent("ekstreBilgi", degerler);
  }

  else if (nerden === "dvzcevirme") {
    const degerler = [
      val("fhkodu1"), val("startDate"), val("endDate"),
      val("dvz_tur"), val("dvz_cins")
    ].join(",");
    setHiddenInAraContent("dvzcevirmeBilgi", degerler);
  }

  else if (nerden === "ozelmizan") {
    const degerler = [
      val("fhkodu1"), val("fhkodu2"),
      val("startDate"), val("endDate"),
      val("cins1"), val("cins2"),
      val("karton1"), val("karton2"),
      val("hangi_tur")
    ].join(",");
    setHiddenInAraContent("mizanBilgi", degerler);
  }

  else if (nerden === "tahsilatrapor") {
    const degerler = [
      selIndex("tah_ted"),
      selIndex("hangi_tur"),
      val("pos"),
      val("fhkodu1"), val("fhkodu2"),
      val("startDate"), val("endDate"),
      val("fhevrak1"), val("fhevrak2")
    ].join(",");
    setHiddenInAraContent("tahrapBilgi", degerler);
  }

  else if (nerden === "kurrapor") {
    const degerler = [val("startDate"), val("endDate"), val("cins1"), val("cins2")].join(",");
    setHiddenInAraContent("kurraporBilgi", degerler);
  }

  else if (nerden === "cekrapor") {
    const degerler = [
      val("cekno1"), val("cekno2"),
      val("durum1"), val("durum2"),
      val("vade1"), val("vade2"),
      val("ttar1"), val("ttar2"),
      val("gbor1"), val("gbor2"),
      val("gozel"), val("cozel"),
      val("gtar1"), val("gtar2"),
      val("cins1"), val("cins2"),
      val("cbor1"), val("cbor2"),
      val("ches1"), val("ches2"),
      val("ctar1"), val("ctar2"),
      val("hangi_tur"),
      val("ghes1"), val("ghes2")
    ].join(",");
    setHiddenInAraContent("cekrapBilgi", degerler);
  }

  else if (nerden === "cekgir") {
    setHiddenInAraContent("cekgirBilgi", val("ckodu"));
  }

  else if (nerden === "cekcik") {
    setHiddenInAraContent("cekcikBilgi", val("ckodu"));
  }

  else if (nerden === "tahsilatckaydet") {
    setHiddenInAraContent("tahsilatBilgi", val("ckodu"));
  }

  else if (nerden === "fatura") {
    setHiddenInAraContent("faturaBilgi", val("ckodu"));
  }

  else if (nerden === "irsaliye") {
    setHiddenInAraContent("irsaliyeBilgi", val("ckodu"));
  }

  else if (nerden === "fatrapor") {
    const degerler = [
      val("fatno1"), val("fatno2"),
      val("anagrp"),
      val("tar1"), val("tar2"),
      val("altgrp"),
      val("ckod1"), val("ckod2"),
      val("depo"),
      val("adr1"), val("adr2"),
      val("turu"),
      val("ukod1"), val("ukod2"),
      val("tev1"), val("tev2"),
      val("okod1"), val("okod2"),
      val("gruplama"),
      val("dvz1"), val("dvz2"),
      val("caradr")
    ].join(",");
    setHiddenInAraContent("fatrapBilgi", degerler);
  }

  else if (nerden === "irsrapor") {
    const degerler = [
      val("irsno1"), val("irsno2"),
      val("anagrp"),
      val("tar1"), val("tar2"),
      val("altgrp"),
      val("ckod1"), val("ckod2"),
      val("turu"),
      val("ukod1"), val("ukod2"),
      val("okod1"), val("okod2"),
      val("dvz1"), val("dvz2"),
      val("fatno1"), val("fatno2"),
      val("adr1"), val("adr2")
    ].join(",");
    setHiddenInAraContent("irsrapBilgi", degerler);
  }

  else if (nerden === "imarapor") {
    const degerler = [
      val("evrno1"), val("evrno2"),
      val("uranagrp"),
      val("tar1"), val("tar2"),
      val("uraltgrp"),
      val("bkod1"), val("bkod2"),
      val("depo"),
      val("ukod1"), val("ukod2"),
      val("rec1"), val("rec2"),
      val("anagrp"), val("altgrp")
    ].join(",");
    setHiddenInAraContent("imarapBilgi", degerler);
  }

  else if (nerden === "envanter") {
    const degerler = [
      val("tar1"), val("tar2"),
      val("uranagrp"),
      val("ukod1"), val("ukod2"),
      val("uraltgrp"),
      val("evrno1"), val("evrno2"),
      val("anagrp"),
      val("gruplama"),
      val("altgrp"),
      val("depo"),
      val("fiatlama"),
      checked("depohardahil"),
      checked("uretfisdahil")
    ].join(",");
    setHiddenInAraContent("envanterBilgi", degerler);
  }

  else if (nerden === "stokrapor") {
    const degerler = [
      val("tar1"), val("tar2"),
      val("uranagrp"),
      val("ukod1"), val("ukod2"),
      val("uraltgrp"),
      val("evrno1"), val("evrno2"),
      val("anagrp"),
      val("gruplama"),
      val("altgrp"),
      val("depo"),
      checked("oncekitarih"),
      checked("depohardahil"),
      checked("uretfisdahil")
    ].join(",");
    setHiddenInAraContent("stokBilgi", degerler);
  }

  else if (nerden === "grprapor") {
    const degerler = [
      val("tar1"), val("tar2"),
      val("uranagrp"),
      val("ukod1"), val("ukod2"),
      val("uraltgrp"),
      val("ckod1"), val("ckod2"),
      val("urozkod"),
      val("birim"),
      val("istenenay"),
      val("gruplama"),
      checked("dvzcevirchc"),
      val("dvzcins"),
      val("stunlar"),
      val("dvzturu"),
      val("turu"),
      checked("istenenaychc"),
      val("sinif1"),
      val("sinif2")
    ].join(",");
    setHiddenInAraContent("grpBilgi", degerler);
  }

  else if (nerden === "imagrprapor") {
    const degerler = [
      val("ukod1"), val("ukod2"),
      val("tar1"), val("tar2"),
      val("sinif1"), val("sinif2"),
      val("uranagrp"),
      val("uraltgrp"),
      val("birim"),
      val("gruplama"),
      val("stunlar"),
      val("turu"),
      val("anagrp"),
      val("altgrp")
    ].join(",");
    setHiddenInAraContent("imagrpBilgi", degerler);
  }

  else if (nerden === "stokdetayrapor") {
    const degerler = [
      val("tar1"), val("tar2"),
      val("uranagrp"),
      val("ukod1"), val("ukod2"),
      val("uraltgrp"),
      val("evrno1"), val("evrno2"),
      val("ckod1"), val("ckod2"),
      val("anagrp"), val("altgrp"),
      val("depo"),
      checked("depohardahil"),
      checked("uretfisdahil"),
      val("turu")
    ].join(",");
    setHiddenInAraContent("stokdetayBilgi", degerler);
  }

  else if (nerden === "kerestegiris") {
    setHiddenInAraContent("kerBilgi", val("ckodu"));
  }

  else if (nerden === "kerestedetayrapor") {
    const degerler = [
      val("tar1"), val("tar2"),
      val("ctar1"), val("ctar2"),
      val("ukod1"), val("ukod2"),
      val("chkod1"), val("chkod2"),
      val("pak1"), val("pak2"),
      val("cevr1"), val("cevr2"),
      val("hes1"), val("hes2"),
      val("canagrp"),
      val("evr1"), val("evr2"),
      val("caltgrp"),
      val("anagrp"), val("altgrp"),
      val("depo"),
      val("ozkod"),
      val("kons1"), val("kons2"),
      val("cozkod"),
      val("cdepo")
    ].join(",");
    setHiddenInAraContent("kerestedetayBilgi", degerler);
  }

  else if (nerden === "kerenvanter") {
    const degerler = [
      val("tar1"), val("tar2"),
      val("ctar1"), val("ctar2"),
      val("ukod1"), val("ukod2"),
      val("chkod1"), val("chkod2"),
      val("pak1"), val("pak2"),
      val("cevr1"), val("cevr2"),
      val("hes1"), val("hes2"),
      val("canagrp"),
      val("evr1"), val("evr2"),
      val("caltgrp"),
      val("anagrp"), val("altgrp"),
      val("depo"),
      val("ozkod"),
      val("kons1"), val("kons2"),
      val("cozkod"),
      val("cdepo"),
      val("gruplama")
    ].join(",");
    setHiddenInAraContent("envanterBilgi", degerler);
  }

  else if (nerden === "kergrprapor") {
    const degerler = [
      val("tar1"), val("tar2"),
      val("anagrp"),
      val("ukod1"), val("ukod2"),
      val("altgrp"),
      val("ckod1"), val("ckod2"),
      val("ozkod"),
      val("kons1"), val("kons2"),
      val("depo"),
      val("evr1"), val("evr2"),
      val("birim"),
      val("gruplama"),
      val("stunlar"),
      val("turu"),
      checked("dvzcevirchc"),
      val("dvzcins"),
      val("dvzturu")
    ].join(",");
    setHiddenInAraContent("grpBilgi", degerler);
  }

  else if (nerden === "kerfatrapor") {
    const degerler = [
      val("tar1"), val("tar2"),
      val("ctar1"), val("ctar2"),
      val("ukod1"), val("ukod2"),
      val("chkod1"), val("chkod2"),
      val("pak1"), val("pak2"),
      val("cevr1"), val("cevr2"),
      val("hes1"), val("hes2"),
      val("canagrp"),
      val("evr1"), val("evr2"),
      val("caltgrp"),
      val("anagrp"), val("altgrp"),
      val("depo"),
      val("ozkod"),
      val("kons1"), val("kons2"),
      val("cozkod"),
      val("cdepo"),
      val("gruplama"),
      val("caradr"),
      val("turu")
    ].join(",");
    setHiddenInAraContent("fatrapBilgi", degerler);
  }

  else if (nerden === "kerortfiat") {
    const degerler = [
      val("tar1"), val("tar2"),
      val("anagrp"),
      val("ukod1"), val("ukod2"),
      val("altgrp"),
      val("ckod1"), val("ckod2"),
      val("ozkod"),
      val("kons1"), val("kons2"),
      val("gruplama"),
      val("turu"),
      checked("dvzcevirchc"),
      val("dvzcins"),
      val("dvzturu")
    ].join(",");
    setHiddenInAraContent("ortfiatBilgi", degerler);
  }

  // --- 2) modal kapat ---
  modalHide("firstModal");

  // --- 3) Nereden’e göre tablo doldur / butonları aç ---
  // (senin eski fonksiyon isimlerini aynen kullandım)
  if (nerden === "mizan") {
    OBS.MIZAN.mizfetchTableData();
    byId("mailButton") && (byId("mailButton").disabled = false);
    byId("reportFormat") && (byId("reportFormat").disabled = false);
  }
  else if (nerden === "ekstre") {
    OBS.EKSTRE.eksdoldur();
    byId("mailButton") && (byId("mailButton").disabled = false);
    byId("reportFormat") && (byId("reportFormat").disabled = false);
  }
  else if (nerden === "ozelmizan") {
    OBS.OZMIZAN.ozmizfetchTableData();
    byId("mailButton") && (byId("mailButton").disabled = false);
    byId("reportFormat") && (byId("reportFormat").disabled = false);
  }
  else if (nerden === "tahsilatrapor") {
    OBS.TAHRAP.tahrapfetchTableData();
    byId("tahrapmailButton") && (byId("tahrapmailButton").disabled = false);
    byId("tahrapreportFormat") && (byId("tahrapreportFormat").disabled = false);
  }
  else if (nerden === "dvzcevirme") {
    OBS.DVZ.dvzdoldur();
    byId("mailButton") && (byId("mailButton").disabled = false);
    byId("reportFormat") && (byId("reportFormat").disabled = false);
  }
  else if (nerden === "cekrapor") {
    cekfetchTableData();
    byId("cekrapreportFormat") && (byId("cekrapreportFormat").disabled = false);
    byId("cekrapmailButton") && (byId("cekrapmailButton").disabled = false);
  }
  else if (nerden === "cekgir" || nerden === "cekcik") {
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
  else if (nerden === "irsaliye") {
    irscariIsle();
  }
  else if (nerden === "kerestegiris") {
    kercariIsle();
  }
  else if (nerden === "fatrapor") {
    fatdoldur();
    byId("fatrapmailButton") && (byId("fatrapmailButton").disabled = false);
    byId("fatrapreportFormat") && (byId("fatrapreportFormat").disabled = false);
  }
  else if (nerden === "irsrapor") {
    irsdoldur();
    byId("irsrapmailButton") && (byId("irsrapmailButton").disabled = false);
    byId("irsrapreportFormat") && (byId("irsrapreportFormat").disabled = false);
  }
  else if (nerden === "imarapor") {
    imafetchTableData();
    byId("imarapmailButton") && (byId("imarapmailButton").disabled = false);
    byId("imarapdownloadButton") && (byId("imarapdownloadButton").disabled = false);
  }
  else if (nerden === "envanter") {
    envfetchTableData();
    byId("envmailButton") && (byId("envmailButton").disabled = false);
    byId("envDownloadButton") && (byId("envDownloadButton").disabled = false);
  }
  else if (nerden === "stokrapor") {
    stokfetchTableData();
    byId("stokrapmailButton") && (byId("stokrapmailButton").disabled = false);
    byId("stokrapreportDownload") && (byId("stokrapreportDownload").disabled = false);
  }
  else if (nerden === "grprapor") {
    grpfetchTableData();
    byId("grprapmailButton") && (byId("grprapmailButton").disabled = false);
    byId("grprapreportDownload") && (byId("grprapreportDownload").disabled = false);
  }
  else if (nerden === "imagrprapor") {
    imagrpfetchTableData();
    byId("imagrprapmailButton") && (byId("imagrprapmailButton").disabled = false);
    byId("imagrprapreportDownload") && (byId("imagrprapreportDownload").disabled = false);
  }
  else if (nerden === "stokdetayrapor") {
    stokdetayfetchTableData();
    byId("stokdetaymailButton") && (byId("stokdetaymailButton").disabled = false);
    byId("stokdetayreportDownload") && (byId("stokdetayreportDownload").disabled = false);
  }
  else if (nerden === "kerestedetayrapor") {
    kerestedetaydoldur();
    byId("kerestedetaymailButton") && (byId("kerestedetaymailButton").disabled = false);
    byId("kerestedetayreportDownload") && (byId("kerestedetayreportDownload").disabled = false);
  }
  else if (nerden === "kergrprapor") {
    grpfetchTableData();
    byId("grprapmailButton") && (byId("grprapmailButton").disabled = false);
    byId("grprapreportDownload") && (byId("grprapreportDownload").disabled = false);
  }
  else if (nerden === "kerfatrapor") {
    kerfatdoldur();
    byId("fatrapmailButton") && (byId("fatrapmailButton").disabled = false);
    byId("fatrapreportFormat") && (byId("fatrapreportFormat").disabled = false);
  }
  else if (nerden === "kerenvanter") {
    kerenvanterdoldur();
    byId("envantermailButton") && (byId("envantermailButton").disabled = false);
    byId("envanterDownload") && (byId("envanterDownload").disabled = false);
  }
  else if (nerden === "kerortfiat") {
    kerortfiatdoldur();
    byId("ortfiatmailButton") && (byId("ortfiatmailButton").disabled = false);
    byId("ortfiatDownload") && (byId("ortfiatDownload").disabled = false);
  }
}
