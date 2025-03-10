package com.hamit.obs.controller.kereste;

import java.util.ArrayList;
import java.util.Collections;
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
public class KodKonsController {

	@Autowired
	private KeresteService keresteService;

	@GetMapping("/kereste/kodaciklama")
	public String kodaciklama() {
		return "kereste/kodaciklama";
	}

	@GetMapping("/kereste/konsimentoaciklama")
	public String konsimentoaciklama() {
		return "kereste/konsimentoaciklama";
	}

	@GetMapping("kereste/kodaciklamadoldur")
	@ResponseBody
	public Map<String, Object> kodaciklamadoldur() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> kodacik = keresteService.kod_pln();
			response.put("data", (kodacik != null) ? kodacik : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kereste/kodkaydet")
	@ResponseBody
	public Map<String, Object> kodkaydet(@RequestBody Map<String, Object> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			String kod = (String) request.get("kod");
			String aciklama = (String) request.get("aciklama");
			keresteService.kod_sil(kod);
			keresteService.kod_kayit(kod,aciklama);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kereste/koddelete")
	@ResponseBody
	public Map<String, Object> konsdelete(@RequestBody Map<String, Object> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			String kod = (String) request.get("kod");
			keresteService.kod_sil(kod);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@GetMapping("kereste/konsaciklamadoldur")
	@ResponseBody
	public Map<String, Object> konsaciklamadoldur() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> konsacik = keresteService.kons_pln();
			response.put("data", (konsacik != null) ? konsacik : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kereste/konskaydet")
	@ResponseBody
	public Map<String, Object> konskaydet(@RequestBody Map<String, Object> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			String kons = (String) request.get("kons");
			String aciklama = (String) request.get("aciklama");
			int pak_noString = keresteService.kons_sil(kons);
			keresteService.kons_kayit(kons, aciklama,pak_noString);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kereste/konsdelete")
	@ResponseBody
	public Map<String, Object> koddelete(@RequestBody Map<String, Object> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			String kons = (String) request.get("kons");
			keresteService.kons_sil(kons);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}