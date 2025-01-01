package com.hamit.obs.controller.kambiyo;

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

import com.hamit.obs.dto.kambiyo.cekraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.kambiyo.KambiyoService;

@Controller
public class CekRaporController {

	@Autowired 
	KambiyoService kambiyoService;
	
	@GetMapping("kambiyo/cekrapor")
	public String ekstre() {
		return "/kambiyo/cekraporlama";
	}

	
	@PostMapping("kambiyo/cekraporlama")
	@ResponseBody
	public Map<String, Object> sorgula(@RequestBody cekraporDTO cekraporDTO ) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> cek_rapor = kambiyoService.cek_rapor(cekraporDTO);
			response.put("success", true);
			response.put("data", (cek_rapor != null) ? cek_rapor : new ArrayList<>());
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