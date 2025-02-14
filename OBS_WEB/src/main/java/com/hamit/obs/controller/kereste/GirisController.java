package com.hamit.obs.controller.kereste;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.kereste.KeresteService;
import com.hamit.obs.service.user.UserService;

@Controller
public class GirisController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private KeresteService keresteService;
	
	@GetMapping("kereste/giris")
	public Model fatura(Model model) {
		try {
			List<Map<String, Object>> anaKodlari = keresteService.ker_kod_degisken_oku("ANA_GRUP", "AGID_Y", "ANA_GRUP_DEGISKEN") ;
			Map<String, Object> anaDeger = new HashMap<>();
			anaDeger.put("ANA_GRUP", ""); 
			anaKodlari.add(0, anaDeger);
			model.addAttribute("anaKodlari", (anaKodlari != null) ? anaKodlari : new ArrayList<>());

			List<Map<String, Object>> depoKodlari = keresteService.ker_kod_degisken_oku("DEPO", "DPID_Y", "DEPO_DEGISKEN") ;
			Map<String, Object> depoDeger = new HashMap<>();
			depoDeger.put("DEPO", ""); 
			depoKodlari.add(0, depoDeger);
			model.addAttribute("depoKodlari", (depoKodlari != null) ? depoKodlari : new ArrayList<>());
			
			List<Map<String, Object>> menseiKodlari = keresteService.ker_kod_degisken_oku("MENSEI", "MEID_Y", "MENSEI_DEGISKEN") ;
			Map<String, Object> menseiDeger = new HashMap<>();
			menseiDeger.put("MENSEI", ""); 
			menseiKodlari.add(0, menseiDeger);
			model.addAttribute("menseiKodlari", (menseiKodlari != null) ? menseiKodlari : new ArrayList<>());

			List<Map<String, Object>> nakKodlari = keresteService.ker_kod_degisken_oku("UNVAN", "NAKID_Y", "NAKLIYECI") ;
			Map<String, Object> nakDeger = new HashMap<>();
			nakDeger.put("UNVAN", ""); 
			nakKodlari.add(0, nakDeger);
			model.addAttribute("nakKodlari", (nakKodlari != null) ? nakKodlari : new ArrayList<>());

			List<Map<String, Object>> oz1Kodlari = keresteService.ker_kod_degisken_oku("OZEL_KOD_1", "OZ1ID_Y", "OZ_KOD_1_DEGISKEN") ;
			Map<String, Object> oz1Deger = new HashMap<>();
			oz1Deger.put("OZEL_KOD_1", ""); 
			oz1Kodlari.add(0, oz1Deger);
			model.addAttribute("oz1Kodlari", (oz1Kodlari != null) ? oz1Kodlari : new ArrayList<>());

			model.addAttribute("doviz", userService.getCurrentUser().getCalisandvzcins()); 
			LocalDate today = LocalDate.now(); 
			model.addAttribute("fisTarih", today); 
			model.addAttribute("errorMessage", "");
		} catch (ServiceException e) {
			model.addAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Hata: " + e.getMessage());
		}
		return model;
	}

}
