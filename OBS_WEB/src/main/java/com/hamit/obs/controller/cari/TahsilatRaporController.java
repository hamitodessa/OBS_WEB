package com.hamit.obs.controller.cari;

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

import com.hamit.obs.dto.cari.tahrapDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.cari.CariService;

@Controller
public class TahsilatRaporController {

	@Autowired
	private CariService cariservice;

	@GetMapping("/cari/tahsilatrapor")
	public String tahsilatRapor() {
		return "cari/tahsilatrapor";
	}


	@PostMapping("cari/tahsilatrappos")
	@ResponseBody
	public Map<String, Object> sorgula() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> posbilgi =  cariservice.banka_sube("BANKA");
			response.put("data", (posbilgi != null) ? posbilgi : new ArrayList<>());
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("cari/tahrapdoldur")
	@ResponseBody
	public Map<String, Object> tah_listele(@RequestBody tahrapDTO tahrapDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> tah_listele = cariservice.tah_listele(tahrapDTO);
			response.put("success", true);
			response.put("data", (tah_listele != null) ? tah_listele : new ArrayList<>());
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