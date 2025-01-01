package com.hamit.obs.controller.kambiyo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.kambiyo.bordrodetayDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.kambiyo.KambiyoService;

@Controller
public class KambiyoController {

	@Autowired 
	KambiyoService kambiyoService;

	@GetMapping("kambiyo/getBaslik")
	@ResponseBody
	public Map<String, String> getBaslik() {
		Map<String, String> response = new HashMap<>();
		try {
			response.put("baslik", kambiyoService.kambiyo_firma_adi());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("baslik", ""); 
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@GetMapping("kambiyo/cektakip")
	public Model cekkontrol(Model model) {
		try {
			LocalDate today = LocalDate.now(); 
			model.addAttribute("gunlukTarih", today); 
			model.addAttribute("errorMessage", "");
		} catch (ServiceException e) {
			model.addAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Hata: " + e.getMessage());
		}
		return model;
	}
	
	@PostMapping("kambiyo/cektakipkontrol")
	@ResponseBody
	public Map<String, Object> cektakipkontrol(@RequestParam String cekNo) {
		bordrodetayDTO dto = new bordrodetayDTO();
		Map<String, Object> response = new HashMap<>();
		try {
			dto = kambiyoService.cektakipkontrol(cekNo);
			if (dto.getCekNo()== null) {
				response.put("errorMessage", "Bu Numarada Cek Yok");
			}
			else
			{
				response.put("errorMessage", "");
				response.put("data", dto);
			}
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response ;
	}
	
	@PostMapping("kambiyo/cektakipkaydet")
	@ResponseBody
	public Map<String, Object> cektakipkaydet(@RequestParam String cekno,@RequestParam int durum,@RequestParam String ttarih) {
		Map<String, Object> response = new HashMap<>();
		try {
			String drmString = "" ;
			if(durum == 1)
				drmString = "1";
			if(durum == 2)
				drmString = "2";
			if(durum == 3)
				drmString = "3";
			kambiyoService.kam_durum_yaz(cekno,"CEK", "Cek_No",drmString,drmString.equals("") ?  "1900-01-01" : ttarih );
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response ;
	}
}