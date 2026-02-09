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
  }	else if (nerdenGeldi === "kerestecikis") {
		    const fisno = val("fisno");
		    if (!fisno || fisno === "0") return;
	}

  const modal = byId("firstModal");
  nerden = nerdenGeldi;

  
  const setEkstreFocus = () => {
      const fhkodu1 = document.getElementById("fhkodu1");
      if (!fhkodu1) return;
      fhkodu1.value = "";
      fhkodu1.focus({ preventScroll: true });
      fhkodu1.select?.();
    };
	
  // senin özel modal açıcıların
  if (nerden === "tahsilatrapor") {
    OBS.TAHRAP.opentahrapModal(modal);
  } else if (nerden === "fatrapor") {
    OBS.FATRAPOR.openfatrapModal(modal);
  } else if (nerden === "irsrapor") {
    OBS.IRSRAPOR.openirsrapModal(modal);
	} else if (nerden === "envanter") {
	OBS.ENVANTER.openenvanterModal(modal);
    } else if (nerden === "imarapor") {
    OBS.IMARAPOR.openimarapModal(modal);
	} else if (nerden === "imagrprapor") {
	  OBS.IMAGRUP.openimagrpModal(modal);
	} else if (nerden === "stokrapor") {
	  OBS.STKRAP.openstkModal(modal);
	} else if (nerden === "grprapor") {
	  OBS.GRPRAP.opengrpModal(modal);
	} else if (nerden === "stokdetayrapor") {
	  OBS.STKDETAY.openstkdtyModal(modal);
	} else if (nerden === "kerfatrapor") {
	  OBS.KERFATRAPOR.openkfatrapModal(modal);
    } else if (nerden === "kerestedetayrapor") {
  	  OBS.KERDETAY.openkdetayModal(modal);	  
	} else if (nerden === "kergrprapor") {
   	  OBS.KERGRPRAPOR.openkgrpModal(modal);	
	} else if (nerden === "kerenvanter") {
	  OBS.KERENVANTER.openkenvModal(modal);	 
	} else if (nerden === "kerortfiat") {
	  OBS.KERORTFIAT.openkortModal(modal);	  
  } 
  else {
	const modalEl = (typeof modal === "string") ? document.querySelector(modal) : modal;

	    if (modalEl && window.bootstrap?.Modal) {
	      if (nerden === "ekstre") {
	        modalEl.addEventListener("shown.bs.modal", setEkstreFocus, { once: true });
	      }
	      bootstrap.Modal.getOrCreateInstance(modalEl).show();
	    }
  }
  /*---- */
  if (nerden === "ekstre") {
      requestAnimationFrame(() => setTimeout(setEkstreFocus, 50));
    }
};

/* =========================
   SAVE (FIRST MODAL -> MAIN)
   ========================= */
function saveToMain() {
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
	else if (nerden === "kerestecikis") {
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
    OBS.CEKRAP.fetchTableData();
    byId("cekrapreportFormat") && (byId("cekrapreportFormat").disabled = false);
    byId("cekrapmailButton") && (byId("cekrapmailButton").disabled = false);
  }
  else if (nerden === "cekcik") {
    OBS.CEKCIK.cekcariIsle();
  }
	else if (nerden === "cekgir") {
	  OBS.CEKGIR.cekcariIsle();
	}
  else if (nerden === "kurrapor") {
    OBS.KURRAPOR.fetchTableData();
  }
  else if (nerden === "tahsilatckaydet") {
    OBS.TAHSILAT.tahcariIsle();
  }
  else if (nerden === "fatura") {
    OBS.FATURA.fatcariIsle();
  }
  else if (nerden === "irsaliye") {
    OBS.IRSALIYE.irscariIsle();
  }
  else if (nerden === "kerestegiris") {
    OBS.KERGIRIS.kercariIsle();
  }
	else if (nerden === "kerestecikis") {
	  OBS.KERCIKIS.kercariIsle();
	}
  else if (nerden === "fatrapor") {
    OBS.FATRAPOR.fatdoldur();
    byId("fatrapmailButton") && (byId("fatrapmailButton").disabled = false);
    byId("fatrapreportFormat") && (byId("fatrapreportFormat").disabled = false);
  }
  else if (nerden === "irsrapor") {
    OBS.IRSRAPOR.irsdoldur();
    byId("irsrapmailButton") && (byId("irsrapmailButton").disabled = false);
    byId("irsrapreportFormat") && (byId("irsrapreportFormat").disabled = false);
  }
  else if (nerden === "imarapor") {
    OBS.IMARAPOR.imafetchTableData();
    byId("imarapmailButton") && (byId("imarapmailButton").disabled = false);
    byId("imarapdownloadButton") && (byId("imarapdownloadButton").disabled = false);
  }
  else if (nerden === "envanter") {
    OBS.ENVANTER.envfetchTableData();
    byId("envmailButton") && (byId("envmailButton").disabled = false);
    byId("envDownloadButton") && (byId("envDownloadButton").disabled = false);
  }
  else if (nerden === "stokrapor") {
    OBS.STKRAP.stokfetchTableData();
    byId("stokrapmailButton") && (byId("stokrapmailButton").disabled = false);
    byId("stokrapreportDownload") && (byId("stokrapreportDownload").disabled = false);
  }
  else if (nerden === "grprapor") {
    OBS.GRPRAP.grpfetchTableData();
    byId("grprapmailButton") && (byId("grprapmailButton").disabled = false);
    byId("grprapreportDownload") && (byId("grprapreportDownload").disabled = false);
  }
  else if (nerden === "imagrprapor") {
    OBS.IMAGRUP.imagrpfetchTableData();
    byId("imagrprapmailButton") && (byId("imagrprapmailButton").disabled = false);
    byId("imagrprapreportDownload") && (byId("imagrprapreportDownload").disabled = false);
  }
  else if (nerden === "stokdetayrapor") {
    OBS.STKDETAY.stokdetayfetchTableData();
    byId("stokdetaymailButton") && (byId("stokdetaymailButton").disabled = false);
    byId("stokdetayreportDownload") && (byId("stokdetayreportDownload").disabled = false);
  }
  else if (nerden === "kerestedetayrapor") {
    OBS.KERDETAY.kerestedetaydoldur();
    byId("kerestedetaymailButton") && (byId("kerestedetaymailButton").disabled = false);
    byId("kerestedetayreportDownload") && (byId("kerestedetayreportDownload").disabled = false);
  }
  else if (nerden === "kergrprapor") {
    OBS.KERGRPRAPOR.grpfetchTableData();
    byId("grprapmailButton") && (byId("grprapmailButton").disabled = false);
    byId("grprapreportDownload") && (byId("grprapreportDownload").disabled = false);
  }
  else if (nerden === "kerfatrapor") {
    OBS.KERFATRAPOR.kerfatdoldur();
    byId("fatrapmailButton") && (byId("fatrapmailButton").disabled = false);
    byId("fatrapreportFormat") && (byId("fatrapreportFormat").disabled = false);
  }
  else if (nerden === "kerenvanter") {
    OBS.KERENVANTER.kerenvanterdoldur();
    byId("envantermailButton") && (byId("envantermailButton").disabled = false);
    byId("envanterDownload") && (byId("envanterDownload").disabled = false);
  }
  else if (nerden === "kerortfiat") {
    OBS.KERORTFIAT.kerortfiatdoldur();
    byId("ortfiatmailButton") && (byId("ortfiatmailButton").disabled = false);
    byId("ortfiatDownload") && (byId("ortfiatDownload").disabled = false);
  }
}
