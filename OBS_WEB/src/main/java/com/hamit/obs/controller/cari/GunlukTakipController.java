package com.hamit.obs.controller.cari;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.cari.CariService;

@Controller
public class GunlukTakipController {

	@Autowired
	private CariService cariservice;

	@GetMapping("cari/gunlukkontrol")
	public Model gunlukkontrol(Model model) {
		try {
			model.addAttribute("hesapKodlari", (cariservice.hesap_kodlari() != null) ? cariservice.hesap_kodlari() : new ArrayList<>());
			LocalDate today = LocalDate.now(); 
			model.addAttribute("evrakTarih", today); 
			model.addAttribute("errorMessage", "");
		} catch (ServiceException e) {
			model.addAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Hata: " + e.getMessage());
		}
		return model;
	}

	@PostMapping("cari/gunluktakip")
	@ResponseBody
	public Map<String, Object> gunluktakip(@RequestParam String tarih,@RequestParam String kodu) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> kasa_kontrol = cariservice.kasa_kontrol(kodu, tarih);
			List<Map<String, Object>> kasa_mizan = cariservice.kasa_mizan(kodu,"1900-01-01" ,tarih);
			response.put("data", (kasa_kontrol != null) ? kasa_kontrol : new ArrayList<>());
			response.put("onceki", (kasa_mizan != null) ? kasa_mizan : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}