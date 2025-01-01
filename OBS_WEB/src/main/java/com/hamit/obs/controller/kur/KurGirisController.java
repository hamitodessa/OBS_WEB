package com.hamit.obs.controller.kur;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.kur.kurgirisDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.kur.KurService;

@Controller
public class KurGirisController {

	@Autowired
	private KurService kurService;

	@GetMapping("kur/kurgiris")
	public Model kurgiris(Model model) {
		try {
			LocalDate today = LocalDate.now(); 
			model.addAttribute("tarih", today); 
			model.addAttribute("errorMessage", "");
		} catch (ServiceException e) {
			model.addAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Hata: " + e.getMessage());
		}
		return model;
	}

	@PostMapping("/kur/kurgunluk")
	@ResponseBody
	public Map<String, Object> sorgula(@RequestBody Map<String, String> params) {
		Map<String, Object> response = new HashMap<>();
		try {
			String tarih = params.get("tarih");
			List<Map<String, Object>> kur_liste = kurService.kur_liste(tarih);
			response.put("success", true);
			response.put("data", (kur_liste != null) ? kur_liste : new ArrayList<>());
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

	@PostMapping("kur/kurkayit")
	@ResponseBody
	public Map<String, Object> kurkayit(@RequestBody kurgirisDTO kurgirisDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			kurService.kur_sil(kurgirisDTO.getTar(),kurgirisDTO.getDvz_turu());
			boolean status = kurService.kur_kayit(kurgirisDTO);
			if(status)
				response.put("errorMessage", "");
			else
				response.put("errorMessage", "Kayit Yaparken Hata Olustu");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kur/kurSil")
	@ResponseBody
	public Map<String, String> kurSil(@RequestBody kurgirisDTO kurgirisDTO) {
		Map<String, String> response = new HashMap<>();
		try {
			kurService.kur_sil(kurgirisDTO.getTar(), kurgirisDTO.getDvz_turu());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}
