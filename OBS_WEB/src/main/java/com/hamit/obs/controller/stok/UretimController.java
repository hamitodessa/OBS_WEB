package com.hamit.obs.controller.stok;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hamit.obs.service.fatura.FaturaService;

@Controller
public class UretimController {

	@Autowired
	private FaturaService faturaService;
	
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


}
