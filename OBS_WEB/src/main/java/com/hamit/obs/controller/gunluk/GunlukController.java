package com.hamit.obs.controller.gunluk;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
