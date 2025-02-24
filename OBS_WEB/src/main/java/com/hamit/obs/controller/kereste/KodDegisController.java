package com.hamit.obs.controller.kereste;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.kereste.KeresteService;

@Controller
public class KodDegisController {

	@Autowired
	private KeresteService keresteService;

	@GetMapping("/kereste/koddegis")
	public String kodaciklama() {
		return "kereste/koddegis";
	}
	
	@PostMapping("kereste/koddegisload")
	@ResponseBody
	public Map<String, Object> koddegisload(@RequestBody Map<String, String> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			String pakno = request.get("pakno");
			String kons = request.get("kons");
			String kodu = request.get("kodu");
			String evrak = request.get("evrak");
			
			List<Map<String, Object>> koddegis = keresteService.urun_detay(pakno,kons,kodu,evrak);
			response.put("data", (koddegis != null) ? koddegis : new ArrayList<>());
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	
	//pakno,kons, kodu,evrak
}
