package com.hamit.obs.controller.gunluk;

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

import com.hamit.obs.dto.gunluk.AyRequest;
import com.hamit.obs.dto.gunluk.GunlukOkuRequest;
import com.hamit.obs.dto.gunluk.IsimOkuRequest;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.gunluk.GunlukService;

@Controller
public class GunlukController {

	@Autowired 
	private GunlukService gunlukService;

	@GetMapping("/gunluk/gunluk")
	public String lograpor() {
		return "gunluk/gunluk";
	}

	@GetMapping("gunluk/getBaslik")
	@ResponseBody
	public Map<String, String> getBaslik() {
		String[] detay  = gunlukService.conn_detail();
		Map<String, String> response = new HashMap<>();
		try {
			response.put("baslik", gunlukService.gunluk_firma_adi() + " / " + detay[1]);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("baslik", ""); 
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("gunluk/gunlukoku")
	@ResponseBody
	public Map<String, Object> sorgula(@RequestBody GunlukOkuRequest req) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> gunlukokuma = gunlukService.gorevsayi(req.start,req.end);
			response.put("success", true);
			response.put("data", (gunlukokuma != null) ? gunlukokuma : new ArrayList<>());
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("gunluk/gunlukoku_ay")
	@ResponseBody
	public Map<String, Object> sorgulaay(@RequestBody AyRequest request) {
		String ay = request.getAy();   // ✅ "2026-02"
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> gunlukokuma = gunlukService.gorev_oku_aylik_grup(ay);
			response.put("success", true);
			response.put("data", (gunlukokuma != null) ? gunlukokuma : new ArrayList<>());
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	

	@PostMapping("gunluk/isimoku")
	@ResponseBody
	public Map<String, Object> isimoku(@RequestBody IsimOkuRequest req) {
		System.out.println("GunlukController - isimoku: " + req.tarih + " " + req.saat); // Loglama
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> isimokuma = gunlukService.gorev_oku_tarih(req.tarih,req.saat);
			response.put("success", true);
			response.put("data", (isimokuma != null) ? isimokuma : new ArrayList<>());
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("gunluk/isimoku_gun")
	@ResponseBody
	public Map<String, Object> isimoku_gun(@RequestBody IsimOkuRequest req) {
		System.out.println("GunlukController - isimoku: " + req.tarih + " " + req.saat); // Loglama
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> isimokuma = gunlukService.gorev_oku_gun(req.tarih);
			response.put("success", true);
			response.put("data", (isimokuma != null) ? isimokuma : new ArrayList<>());
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}
