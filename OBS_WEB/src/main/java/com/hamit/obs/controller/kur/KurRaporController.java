package com.hamit.obs.controller.kur;

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

import com.hamit.obs.dto.kur.kurraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.kur.KurService;

@Controller
public class KurRaporController {

	@Autowired
	private KurService kurService;
	
	@GetMapping("kur/kurrapor")
	public String register() {
		return "/kur/kurrapor";
	}
	
	@PostMapping("kur/kurrapor")
	@ResponseBody
	public Map<String, Object> sorgula(@RequestBody kurraporDTO kurraporDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> kurrapor = kurService.kur_rapor(kurraporDTO);
			response.put("success", true);
			response.put("data", (kurrapor != null) ? kurrapor : new ArrayList<>());
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