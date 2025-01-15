package com.hamit.obs.controller.stok;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.fatura.FaturaService;

@Controller
public class StokController {

	@Autowired
	private FaturaService faturaService;
	
	@GetMapping("stok/getBaslik")
	@ResponseBody
	public Map<String, String> getBaslik() {
		Map<String, String> response = new HashMap<>();
		try {
			response.put("baslik", faturaService.fat_firma_adi());
			response.put("errorMessage","");
		} catch (ServiceException e) {
			response.put("baslik", ""); 
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

}
