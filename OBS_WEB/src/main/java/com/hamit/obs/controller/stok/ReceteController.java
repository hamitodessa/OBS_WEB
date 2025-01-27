package com.hamit.obs.controller.stok;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.dto.stok.receteDTO;
import com.hamit.obs.dto.stok.recetedetayDTO;
import com.hamit.obs.dto.stok.recetekayitDTO;
import com.hamit.obs.dto.stok.urunDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.fatura.FaturaService;

@Controller
public class ReceteController {

	@Autowired
	private FaturaService faturaService;
	
	
	@GetMapping("stok/recete")
	public Model fatura(Model model) {
		try {
			List<Map<String, Object>> anaKodlari = faturaService.stk_kod_degisken_oku("ANA_GRUP", "AGID_Y", "ANA_GRUP_DEGISKEN") ;
			Map<String, Object> anaDeger = new HashMap<>();
			anaDeger.put("ANA_GRUP", ""); 
			anaKodlari.add(0, anaDeger);
			model.addAttribute("anaKodlari", (anaKodlari != null) ? anaKodlari : new ArrayList<>());
			model.addAttribute("errorMessage", "");
		} catch (ServiceException e) {
			model.addAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Hata: " + e.getMessage());
		}
		return model;
	}
	
	@PostMapping("stok/recsonfis")
	@ResponseBody
	public Map<String, String> recsonfis() {
		Map<String, String> response = new HashMap<>();
		try {
			response.put("recno", faturaService.recete_son_bordro_no_al());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/recyenifis")
	@ResponseBody
	public Map<String, String> recyenifis(@RequestParam String cins) {
		Map<String, String> response = new HashMap<>();
		try {
			int sno = 0 ;
			sno =  faturaService.recete_no_al();
			int kj = 10 - Integer.toString(sno).length();
			StringBuilder strBuilder = new StringBuilder();
			for (int i = 0; i < kj; i++) {
			    strBuilder.append("0");
			}
			strBuilder.append(sno);
			String str_ = strBuilder.toString();
			response.put("recno", str_.equals("0000000000") ? "0000000001":str_);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@GetMapping("stok/stkgeturn")
	@ResponseBody
	public Map<String, Object> stkgeturndepo() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> urunKodlari = faturaService.urun_kodlari() ;
			Map<String, Object> urnDeger = new HashMap<>();
			urnDeger.put("Kodu", ""); 
			urunKodlari.add(0, urnDeger);
			response.put("urnkodlar", (urunKodlari != null) ? urunKodlari : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}	

	@PostMapping("stok/recetecikan")
	@ResponseBody
	public Map<String, Object> recetecikan(@RequestParam String kodu ,@RequestParam String cins) {
		Map<String, Object> response = new HashMap<>();
		try {
			urunDTO urunDTO = new urunDTO();
			urunDTO =  faturaService.urun_adi_oku(kodu,cins);
			if (urunDTO.getKodu() == null || urunDTO.getKodu().isEmpty()) {
				throw new ServiceException("Bu Kodda Urun Yok");
			}
			response.put("urun", urunDTO);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("stok/receteOku")
	@ResponseBody
	public Map<String, Object> receteOku(@RequestParam String recno) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> uretim = faturaService.recete_oku(recno);
			response.put("data", (uretim != null) ? uretim : new ArrayList<>());
			response.put("aciklama",faturaService.aciklama_oku("REC", 1,recno, "G"));
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("stok/recYoket")
	@ResponseBody
	public ResponseEntity<Map<String, String>> evrakSil(@RequestParam String recno, @RequestParam String kodu) {
		Map<String, String> response = new HashMap<>();
		try {
			faturaService.rec_sil(recno.trim());
			faturaService.aciklama_sil("REC", recno.trim(), "G");
			faturaService.kod_recete_yaz(kodu.trim(), "");
	        
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage",  e.getMessage());
		}
		return ResponseEntity.ok(response);
	}

	@PostMapping("stok/recKayit")
	@ResponseBody
	public  Map<String, Object>  recKayit(@RequestBody recetekayitDTO recetekayitDTO ) {
		Map<String, Object> response = new HashMap<>();
		try {
			receteDTO dto = recetekayitDTO.getReceteDTO();
			List<recetedetayDTO> tableData = recetekayitDTO.getTableData();
			faturaService.rec_sil(dto.getRecno());
			faturaService.aciklama_sil("REC", dto.getRecno().trim(), "G");
			
			boolean drm ;
			if (dto.getDurum().equals("A"))
				drm = true;
			else
				drm = false;
			String userrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			int ana = 0 ;
			int alt = 0;
			for (recetedetayDTO row : tableData) {
				ana = 0 ;
				alt = 0;
				if(! dto.getAnagrup().equals("")) {
					String anas = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", dto.getAnagrup());
					ana = Integer.parseInt(anas);
				}
				if(! dto.getAltgrup().equals("")) {
					String alts = faturaService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", dto.getAltgrup());
					alt = Integer.parseInt(alts);
				}
				faturaService.recete_kayit(dto.getRecno().trim(), drm,"Cikan",row.getUkodu()
						, row.getMiktar(), ana, alt, userrString);
			}
			
			faturaService.recete_kayit(dto.getRecno().trim().trim(), drm, "Giren",dto.getGirenurkodu().trim(),1
					, ana, alt,userrString);
			
			faturaService.kod_recete_yaz(dto.getGirenurkodu(), dto.getRecno().trim());
			
			faturaService.aciklama_yaz("REC", 1,dto.getRecno().trim(), dto.getAciklama(), "G");
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}
