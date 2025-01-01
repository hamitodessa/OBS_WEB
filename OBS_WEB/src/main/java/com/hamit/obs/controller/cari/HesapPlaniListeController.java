package com.hamit.obs.controller.cari;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.cari.CariService;

@Controller
public class HesapPlaniListeController {

	@Autowired
	private CariService cariservice;
	
	@GetMapping("cari/hspplnliste")
	public Model hspplnliste(Model model) {
		try {
			List<Map<String, Object>> hsppln = cariservice.hsppln_liste();
	        for (Map<String, Object> item : hsppln) {
	            byte[] imageBytes = (byte[]) item.get("RESIM"); // "resim" alanı veritabanındaki alan adı
	            if (imageBytes != null) {
	                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
	                item.put("RESIM", "data:image/jpeg;base64," + base64Image); // Resim URL'si olarak kaydet
	            }
	            else {
	            	 item.put("RESIM", ""); // Null yerine boş string
	            }
	        }
			model.addAttribute("hsppln", hsppln);
			model.addAttribute("errorMessage", "");
			return model;
		} catch (ServiceException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return model;
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Hata: " + e.getMessage());
			return model;
		}	
	}

}
