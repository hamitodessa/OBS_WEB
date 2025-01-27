package com.hamit.obs.controller.stok;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.stok.urunDTO;
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

	@PostMapping("stok/urnbilgiArama")
	@ResponseBody
	public Map<String, Object> urnbilgiArama(@RequestParam String deger,@RequestParam String kodbarkod) {
		urunDTO urunDTO = new urunDTO();
		Map<String, Object> response = new HashMap<>();
		try {
			urunDTO =  faturaService.stk_urun(kodbarkod,deger);
			if (urunDTO.getKodu() == null || urunDTO.getKodu().isEmpty()) {
	            throw new ServiceException("Bu Kodda Urun Yok");
	        }
	        if (urunDTO.getImage() != null) {
	            String base64Image = Base64.getEncoder().encodeToString(urunDTO.getImage());
	            urunDTO.setBase64Resim(base64Image);
	            urunDTO.setImage(null) ;
	        }
	    	response.put("urun", urunDTO);
			response.put("errorMessage","");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}
