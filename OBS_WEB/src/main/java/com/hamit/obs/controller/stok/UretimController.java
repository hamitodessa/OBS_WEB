package com.hamit.obs.controller.stok;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.custom.yardimci.KusurYuvarla;
import com.hamit.obs.dto.stok.uretimDTO;
import com.hamit.obs.dto.stok.uretimdetayDTO;
import com.hamit.obs.dto.stok.uretimkayitDTO;
import com.hamit.obs.dto.stok.urunDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.fatura.FaturaService;
import com.hamit.obs.service.user.UserService;

@Controller
public class UretimController {

	@Autowired
	private FaturaService faturaService;

	@Autowired
	private UserService userService;

	@GetMapping("stok/uretim")
	public Model uretim(Model model) {
		List<Map<String, Object>> anaKodlari = faturaService.stk_kod_degisken_oku("ANA_GRUP", "AGID_Y", "ANA_GRUP_DEGISKEN") ;
		Map<String, Object> anaDeger = new HashMap<>();
		anaDeger.put("ANA_GRUP", ""); 
		anaKodlari.add(0, anaDeger);
		model.addAttribute("anaKodlari", (anaKodlari != null) ? anaKodlari : new ArrayList<>());

		List<Map<String, Object>> depoKodlari = faturaService.stk_kod_degisken_oku("DEPO", "DPID_Y", "DEPO_DEGISKEN") ;
		Map<String, Object> depoDeger = new HashMap<>();
		depoDeger.put("DEPO", ""); 
		depoKodlari.add(0, depoDeger);
		model.addAttribute("depoKodlari", (depoKodlari != null) ? depoKodlari : new ArrayList<>());
		LocalDate today = LocalDate.now(); 
		model.addAttribute("tarih", today); 
		return model;
	}

	@PostMapping("stok/sonfis")
	@ResponseBody
	public Map<String, String> sorgula() {
		Map<String, String> response = new HashMap<>();
		try {
			response.put("fisno", faturaService.uret_son_bordro_no_al());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/uretimOku")
	@ResponseBody
	public Map<String, Object> uretimOku(@RequestParam String fisno) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> uretim = faturaService.stok_oku(fisno,"URE");
			response.put("data", (uretim != null) ? uretim : new ArrayList<>());
			response.put("aciklama",faturaService.aciklama_oku("URE", 1, fisno, "G"));
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@GetMapping("stok/stkgeturndepo")
	@ResponseBody
	public Map<String, Object> stkgeturndepo() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> urunKodlari = faturaService.urun_kodlari() ;
			Map<String, Object> urnDeger = new HashMap<>();
			urnDeger.put("Kodu", ""); 
			urunKodlari.add(0, urnDeger);
			response.put("urnkodlar", (urunKodlari != null) ? urunKodlari : new ArrayList<>());
			List<Map<String, Object>> depoKodlari = faturaService.stk_kod_degisken_oku("DEPO", "DPID_Y", "DEPO_DEGISKEN") ;
			Map<String, Object> depoDeger = new HashMap<>();
			depoDeger.put("DEPO", ""); 
			depoKodlari.add(0, depoDeger);
			response.put("depolar", (depoKodlari != null) ? depoKodlari : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}	

	@PostMapping("stok/imalatcikan")
	@ResponseBody
	public Map<String, Object> imalatcikan(@RequestParam String ukodu, @RequestParam String fiatlama, @RequestParam String fisTarih, @RequestParam String fiatTarih) {
		Map<String, Object> response = new HashMap<>();
		try {
			urunDTO urunDTO = new urunDTO();
			urunDTO =  faturaService.urun_adi_oku(ukodu,"Kodu");
			if (urunDTO.getKodu().equals("")) {
				throw new ServiceException("Bu Kodda Urun Yok");
			}
			response.put("urun", urunDTO);

			if (! fiatlama.equals(""))
			{
				if ( fiatlama.equals("fiat1"))
					response.put("fiat", urunDTO.getFiat1());
				else  if (fiatlama.equals("fiat2"))
					response.put("fiat", urunDTO.getFiat2());
				else  if (fiatlama.equals("fiat3"))
					response.put("fiat", urunDTO.getFiat3());
				else  if (fiatlama.equals("Sonimalat"))
					response.put("fiat",(faturaService.son_imalat_fiati_oku(ukodu)));
				else  if (fiatlama.equals("ortfiat"))
				{
					String i_tar = faturaService.uret_ilk_tarih(fiatTarih, fisTarih,ukodu);
					double qwee = faturaService.gir_ort_fiati_oku(ukodu,i_tar, fisTarih);
					response.put("fiat", qwee);
				}
			}
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@GetMapping("stok/uretimyenifis")
	@ResponseBody
	public Map<String, Object> uretimyenifis() {
		Map<String, Object> response = new HashMap<>();
		try {
			int evrakNo = faturaService.uretim_fisno_al();
			int kj = 10 - Integer.toString(evrakNo).length();
			String str_ = "0".repeat(kj) + evrakNo;
			response.put("fisno", str_);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/hesapla")
	@ResponseBody
	public Map<String, Object> hesapla(@RequestParam String recetekod) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> uretim = faturaService.recete_oku(recetekod);
			response.put("data", (uretim != null) ? uretim : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/ureKayit")
	@ResponseBody
	public  Map<String, Object>  ureKayit(@RequestBody uretimkayitDTO uretimkayitDTO ) {
		Map<String, Object> response = new HashMap<>();
		try {
			uretimDTO dto = uretimkayitDTO.getUretimDTO();
			List<uretimdetayDTO> tableData = uretimkayitDTO.getTableData();
			faturaService.stok_sil(dto.getFisno(), "URE", "C");
			String userrString = Global_Yardimci.user_log(userService.getCurrentUser().getEmail());
			int dpo = 0 ;
			int ana = 0 ;
			int alt = 0;
			for (uretimdetayDTO row : tableData) {
				dpo = 0 ;
				ana = 0 ;
				alt = 0;
				if( ! row.getDepo().equals("")) {
					String dpos = faturaService.urun_kod_degisken_ara("DPID_Y", "DEPO", "DEPO_DEGISKEN",  row.getDepo());
					dpo = Integer.parseInt(dpos);
				}
				if(! dto.getAnagrup().equals("")) {
					String anas = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", dto.getAnagrup());
					ana = Integer.parseInt(anas);
				}
				if(! dto.getAltgrup().equals("")) {
					String alts = faturaService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", dto.getAltgrup());
					alt = Integer.parseInt(alts);
				}
				faturaService.stk_kaydet(dto.getFisno(),"URE", dto.getTarih(), dpo, row.getUkodu(),
						row.getMiktar() *-1, row.getFiat(), KusurYuvarla.round(row.getTutar() *-1,2), KusurYuvarla.round(row.getTutar() *-1,2), 
						"C",row.getIzahat(), ana, alt, 0, "", dto.getDvzcins(), "", userrString);
			}
			//GIRIS YAZ
			faturaService.stok_sil(dto.getFisno(), "URE", "G");
			if( ! dto.getDepo().equals("")) {
				String dpos = faturaService.urun_kod_degisken_ara("DPID_Y", "DEPO", "DEPO_DEGISKEN",  dto.getDepo());
				dpo = Integer.parseInt(dpos);
			}
			double miktar = Double.valueOf(dto.getUremiktar());
			double tutar = Double.valueOf(dto.getToptutar())  ;
			double fiat =tutar  / (miktar == 0 ? 1 :miktar);
			faturaService.stk_kaydet(dto.getFisno(),"URE", dto.getTarih(), dpo, dto.getGirenurkodu(),
					dto.getUremiktar(), fiat, KusurYuvarla.round(tutar,2), KusurYuvarla.round(tutar,2), 
					"G",dto.getFisno().trim() + " Nolu Fis Ile Uretim " , ana, alt, 0, "", dto.getDvzcins(), "", userrString);
			faturaService.aciklama_sil("URE", dto.getFisno().trim(), "G");
			faturaService.aciklama_yaz("URE", 1, dto.getFisno().trim(), dto.getAciklama(), "G");
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/ureYoket")
	@ResponseBody
	public ResponseEntity<Map<String, String>> evrakSil(@RequestParam String fisno) {
		Map<String, String> response = new HashMap<>();
		try {
			faturaService.stok_sil(fisno.trim(),  "URE", "G");
			faturaService.stok_sil(fisno.trim(),  "URE", "C");
			faturaService.aciklama_sil("URE", fisno.trim(), "G");
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage",  e.getMessage());
		}
		return ResponseEntity.ok(response);
	}
}