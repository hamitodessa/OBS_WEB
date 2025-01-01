package com.hamit.obs.controller.ortak;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.adres.AdresService;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.kambiyo.KambiyoService;

@Controller
public class ObsOrtakController {

	@Autowired
	private CariService cariservice;
	
	@Autowired
	private AdresService adresService;
	
	@Autowired
	private KambiyoService kambiyoService;

	@GetMapping("obs/firmaismi")
	public String firmaism() {
		return "ortak/firmaismidegistirme";
	}

	@PostMapping("obs/firmaismioku")
	@ResponseBody
	public Map<String, String> firmaIsmi(@RequestBody String modul) {
		Map<String, String> response = new HashMap<>();
		try {
			modul = modul.replace("\"", "");
			if(modul.trim().equals("cari")) {
				response.put("firmaismi", cariservice.cari_firma_adi());
			}
			else if(modul.trim().equals("adres")) {
				response.put("firmaismi", adresService.adres_firma_adi());
			}
			if(modul.trim().equals("kambiyo")) {
				response.put("firmaismi", kambiyoService.kambiyo_firma_adi());
			}
			response.put("errorMessage","");
		} catch (ServiceException e) {
			response.put("firmaismi", ""); 
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("obs/firmaismiKayit")
	@ResponseBody
	public Map<String, String> firmaismiKayit(@RequestParam String fismi,@RequestParam String modul) {
		Map<String, String> response = new HashMap<>();
		try {
			if(modul.trim().equals("cari"))
				cariservice.cari_firma_adi_kayit(fismi);
			else if(modul.trim().equals("adres"))
				adresService.adres_firma_adi_kayit(fismi);
			else if(modul.trim().equals("kambiyo"))
				kambiyoService.kambiyo_firma_adi_kayit(fismi);
			response.put("errorMessage","");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}