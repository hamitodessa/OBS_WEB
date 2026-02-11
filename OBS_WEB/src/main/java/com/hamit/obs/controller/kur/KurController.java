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

import com.hamit.obs.dto.kur.kurgirisDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.kur.KurService;

@Controller
public class KurController {

	@Autowired
	private KurService kurService;
	
	@PostMapping("kur/kuroku")
	@ResponseBody
	public Map<String, Object> sorgula(@RequestBody kurgirisDTO kurgirisDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> kuroku = kurService.kur_oku(kurgirisDTO);
			response.put("success", true);
			response.put("data", (kuroku != null) ? kuroku : new ArrayList<>());
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
	
	@GetMapping("kur/getBaslik")
	@ResponseBody
	public Map<String, String> getBaslik() {
		String[] detay  = kurService.conn_detail();
		Map<String, String> response = new HashMap<>();
		try {
			response.put("baslik", detay[1] );
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