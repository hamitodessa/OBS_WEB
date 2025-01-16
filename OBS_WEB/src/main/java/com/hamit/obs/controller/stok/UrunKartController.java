package com.hamit.obs.controller.stok;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.dto.cari.dekontDTO;
import com.hamit.obs.dto.stok.urunDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.fatura.FaturaService;
import com.hamit.obs.service.user.UserService;

@Controller
public class UrunKartController {

	@Autowired
    private UserService userService;
	
	@Autowired
	private FaturaService faturaService;
	
	@GetMapping("/stok/urunkart")
	public Model urunkart(Model model) {
		try {
			List<Map<String, Object>> urunKodlari = faturaService.urun_kodlari() ;
			model.addAttribute("urunKodlari", (urunKodlari != null) ? urunKodlari : new ArrayList<>());
			List<Map<String, Object>> menseiKodlari = faturaService.stk_kod_degisken_oku("MENSEI", "MEID_Y", "MENSEI_DEGISKEN") ;

			Map<String, Object> yeniDeger = new HashMap<>();
			yeniDeger.put("MENSEI", ""); 
			menseiKodlari.add(0, yeniDeger);
			model.addAttribute("menseiKodlari", (menseiKodlari != null) ? menseiKodlari : new ArrayList<>());
			
			List<Map<String, Object>> anaKodlari = faturaService.stk_kod_degisken_oku("ANA_GRUP", "AGID_Y", "ANA_GRUP_DEGISKEN") ;
			Map<String, Object> anaDeger = new HashMap<>();
			anaDeger.put("ANA_GRUP", ""); 
			anaKodlari.add(0, anaDeger);
			model.addAttribute("anaKodlari", (anaKodlari != null) ? anaKodlari : new ArrayList<>());
			
			if(anaKodlari.size() >1)
			{
				String qwe = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", anaKodlari.get(1).get("ANA_GRUP").toString());
				List<Map<String, Object>> altKodlari = faturaService.stk_kod_alt_grup_degisken_oku(Integer.parseInt(qwe)) ;
				Map<String, Object> altDeger = new HashMap<>();
				altDeger.put("ALT_GRUP", ""); 
				altKodlari.add(0, altDeger);
				model.addAttribute("altKodlari", (altKodlari != null) ? altKodlari : new ArrayList<>());
			}
			else {
				List<Map<String, Object>> altKodlari = new ArrayList<>() ;
				Map<String, Object> altDeger = new HashMap<>();
				altDeger.put("ALT_GRUP", ""); 
				altKodlari.add(0, altDeger);
				model.addAttribute("altKodlari", altKodlari);
			}
			List<Map<String, Object>> oz1Kodlari = faturaService.stk_kod_degisken_oku("OZEL_KOD_1", "OZ1ID_Y", "OZ_KOD_1_DEGISKEN") ;
			Map<String, Object> oz1Deger = new HashMap<>();
			oz1Deger.put("OZEL_KOD_1", ""); 
			oz1Kodlari.add(0, oz1Deger);
			model.addAttribute("oz1Kodlari", (oz1Kodlari != null) ? oz1Kodlari : new ArrayList<>());
			List<Map<String, Object>> oz2Kodlari = faturaService.stk_kod_degisken_oku("OZEL_KOD_2", "OZ2ID_Y", "OZ_KOD_2_DEGISKEN") ;
			Map<String, Object> oz2Deger = new HashMap<>();
			oz2Deger.put("OZEL_KOD_2", ""); 
			oz2Kodlari.add(0, oz2Deger);
			model.addAttribute("oz2Kodlari", (oz2Kodlari != null) ? oz2Kodlari : new ArrayList<>());

	        String qwString = (urunKodlari != null && ! urunKodlari.isEmpty()) 
	                ? urunKodlari.get(0).get("Kodu").toString() : "";
	        model.addAttribute("urunKodu", qwString);
	        model.addAttribute("errorMessage", "");
	    } catch (ServiceException e) {
	        model.addAttribute("errorMessage", e.getMessage());
	    } catch (Exception e) {
	    	e.printStackTrace();
	        model.addAttribute("errorMessage", "Hata: " + e.getMessage());
	    }
	    return model;
	}
	
	@PostMapping("stok/urnArama")
	public ResponseEntity<?> hsplnArama(@RequestParam String arama) {
		urunDTO urunDTO = new urunDTO();
		try {
			urunDTO =  faturaService.stk_urun("Kodu",arama);
	        if (urunDTO.getKodu().equals("")) {
	            throw new ServiceException("Bu Kodda Urun Yok");
	        }
	        if (urunDTO.getImage() != null) {
	            String base64Image = Base64.getEncoder().encodeToString(urunDTO.getImage());
	            urunDTO.setBase64Resim(base64Image);
	            urunDTO.setImage(null) ;
	        }
	        urunDTO.setErrorMessage(""); 
	    } catch (ServiceException e) {
	    	urunDTO.setErrorMessage(e.getMessage());
	    } catch (Exception e) {
	    	urunDTO.setErrorMessage("Hata: " + e.getMessage());
	    }
	    return ResponseEntity.ok(urunDTO);
	}

	@PostMapping("stok/altgrup")
	@ResponseBody
	public Map<String, Object> altgrup(@RequestParam String anagrup) {
		Map<String, Object> response = new HashMap<>();
		try {
			String qwe = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", anagrup);
			List<Map<String, Object>> altKodlari = faturaService.stk_kod_alt_grup_degisken_oku(Integer.parseInt(qwe)) ;
			Map<String, Object> altDeger = new HashMap<>();
			altDeger.put("ALT_GRUP", ""); 
			altKodlari.add(0, altDeger);
			response.put("altKodlari", altKodlari);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/urnadi")
	@ResponseBody
	public Map<String, String> urnadiOgren(@RequestParam String urnkodu) {
		Map<String, String> response = new HashMap<>();
		try {
			String urnAdiString = faturaService.ur_kod_bak(urnkodu);
			if (urnAdiString != null ) {
				response.put("urnAdi", urnAdiString);
				response.put("errorMessage", ""); 
			}else {
				response.put("urnAdi", "");
				response.put("errorMessage", ""); 
			}
		} catch (ServiceException e) {
			response.put("urnAdi", ""); 
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("stok/urnSil")
	@ResponseBody
	public Map<String, String> urnSil(@RequestParam String urnkodu) {
		Map<String, String> response = new HashMap<>();
		try {
			faturaService.stk_ur_sil(urnkodu);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("stok/urnkayit")
	@ResponseBody
	public Map<String, Object> fiskayit(@ModelAttribute urunDTO urunDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			
			faturaService.stk_ur_sil(urunDTO.getKodu());
			System.out.println(urunDTO);
			String usrString = Global_Yardimci.user_log(userService.getCurrentUser().getEmail());
			urunDTO.setUsr(usrString);
			
			String anagrp = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN",urunDTO.getAnagrup());
			String altgrp = faturaService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", urunDTO.getAltgrup());
			String mensei = faturaService.urun_kod_degisken_ara("MEID_Y", "MENSEI", "MENSEI_DEGISKEN", urunDTO.getMensei());
			String oz1 = faturaService.urun_kod_degisken_ara("OZ1ID_Y", "OZEL_KOD_1", "OZ_KOD_1_DEGISKEN", urunDTO.getOzelkod1());
			String oz2 = faturaService.urun_kod_degisken_ara("OZ2ID_Y", "OZEL_KOD_2", "OZ_KOD_2_DEGISKEN", urunDTO.getOzelkod2());

			urunDTO.setAnagrup(anagrp.equals("") ? "" : anagrp) ;
			urunDTO.setAltgrup(altgrp.equals("") ? "" : altgrp);
			urunDTO.setMensei(mensei.equals("") ? "" : mensei);
			urunDTO.setOzelkod1(oz2.equals("") ? "" : oz1);
			urunDTO.setOzelkod2(oz2.equals("") ? "" : oz2);
			faturaService.stk_ur_kayit(urunDTO);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

}
