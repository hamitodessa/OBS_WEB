package com.hamit.obs.controller.stok;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.fatura.FaturaService;

@Controller
public class DegiskenlerController {

	@Autowired
	private FaturaService faturaService;
	
	@GetMapping("/stok/degiskenler")
	public Model uretim(Model model) {
		List<Map<String, Object>> anaKodlari = faturaService.stk_kod_degisken_oku("ANA_GRUP", "AGID_Y", "ANA_GRUP_DEGISKEN") ;
		model.addAttribute("anaKodlari", (anaKodlari != null) ? anaKodlari : new ArrayList<>());
		return model;
	}
    
    
    @GetMapping("stok/anagrpOku")
	@ResponseBody
	public Map<String, Object> anagrpOku() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> anaKodlari = faturaService.stk_kod_degisken_oku("ANA_GRUP", "AGID_Y", "ANA_GRUP_DEGISKEN") ;
			response.put("anagrp", anaKodlari); 
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

    @GetMapping("stok/menseiOku")
	@ResponseBody
	public Map<String, Object> menseiOku() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> menseiKodlari = faturaService.stk_kod_degisken_oku("MENSEI", "MEID_Y", "MENSEI_DEGISKEN") ;
			response.put("mensei", menseiKodlari); 
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

    @GetMapping("stok/depoOku")
	@ResponseBody
	public Map<String, Object> depoOku() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> depoKodlari = faturaService.stk_kod_degisken_oku("DEPO", "DPID_Y", "DEPO_DEGISKEN") ;
			response.put("depo", depoKodlari); 
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

    @GetMapping("stok/oz1Oku")
 	@ResponseBody
 	public Map<String, Object> oz1Oku() {
 		Map<String, Object> response = new HashMap<>();
 		try {
 			List<Map<String, Object>> oz1OkuKodlari = faturaService.stk_kod_degisken_oku("OZEL_KOD_1", "OZ1ID_Y", "OZ_KOD_1_DEGISKEN") ;
 			response.put("oz1", oz1OkuKodlari); 
 			response.put("errorMessage", "");
 		} catch (ServiceException e) {
 			response.put("errorMessage", e.getMessage());
 		} catch (Exception e) {
 			response.put("errorMessage", "Hata: " + e.getMessage());
 		}
 		return response;
 	}
    
    @GetMapping("stok/oz2Oku")
  	@ResponseBody
  	public Map<String, Object> oz2Oku() {
  		Map<String, Object> response = new HashMap<>();
  		try {
  			List<Map<String, Object>> oz2OkuKodlari = faturaService.stk_kod_degisken_oku("OZEL_KOD_2", "OZ2ID_Y", "OZ_KOD_2_DEGISKEN") ;
  			response.put("oz2", oz2OkuKodlari); 
  			response.put("errorMessage", "");
  		} catch (ServiceException e) {
  			response.put("errorMessage", e.getMessage());
  		} catch (Exception e) {
  			response.put("errorMessage", "Hata: " + e.getMessage());
  		}
  		return response;
  	}


	
}
