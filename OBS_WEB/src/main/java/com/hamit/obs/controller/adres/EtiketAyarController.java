package com.hamit.obs.controller.adres;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import com.hamit.obs.dto.adres.etiketayarDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.Etiket_Ayarlari;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.user.EtiketAyarService;
import com.hamit.obs.service.user.UserService;

@Controller
public class EtiketAyarController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private EtiketAyarService etiketAyarService ;
	
	@GetMapping("adres/etiketayar")
	public Model getemailSettings(Model model) {
		try {
			User user = userService.getCurrentUser();
			etiketayarDTO etiketayarDTO = new etiketayarDTO();
			Etiket_Ayarlari eayar =  etiketAyarService.findByUserId(user.getId());
			if(eayar != null) {
				etiketayarDTO.setAltbosluk(eayar.getAltbosluk());
				etiketayarDTO.setDikeyarabosluk(eayar.getDikeyarabosluk());
				etiketayarDTO.setGenislik(eayar.getGenislik());
				etiketayarDTO.setId(eayar.getId());
				etiketayarDTO.setSagbosluk(eayar.getSagbosluk());
				etiketayarDTO.setSolbosluk(eayar.getSolbosluk());
				etiketayarDTO.setUstbosluk(eayar.getUstbosluk());
				etiketayarDTO.setYataydikey(eayar.getYataydikey() == 0 ?  "YATAY" : "DIKEY");
				etiketayarDTO.setYukseklik(eayar.getYukseklik());
				model.addAttribute("etiket", etiketayarDTO);
			}
			else {
				etiketayarDTO.setAltbosluk(0);
				etiketayarDTO.setDikeyarabosluk(0);
				etiketayarDTO.setGenislik(0);//240
				etiketayarDTO.setSagbosluk(0);
				etiketayarDTO.setSolbosluk(0);//60
				etiketayarDTO.setUstbosluk(0);//40
				etiketayarDTO.setYataydikey("YATAY");
				etiketayarDTO.setYukseklik(0);//85
				model.addAttribute("etiket", etiketayarDTO);
			}
			model.addAttribute("errorMessage","");
		} catch (Exception e) {
			model.addAttribute("errorMessage",e.getMessage());
		}
		return model;
	}
	
	@PostMapping("adres/etiketsettings_save")
	@ResponseBody
	public Map<String, String> updateetiketsettings(@RequestBody etiketayarDTO etiketayarDTO) {
		Map<String, String> response = new HashMap<>();
		try {
			User user = userService.getCurrentUser();
	        user.setEtiket_Ayarlari(null);
	        userService.saveUser(user);
			Etiket_Ayarlari etiket_Ayarlari = new Etiket_Ayarlari();
			etiket_Ayarlari.setAltbosluk(etiketayarDTO.getAltbosluk());
			etiket_Ayarlari.setDikeyarabosluk(etiketayarDTO.getDikeyarabosluk());
			etiket_Ayarlari.setGenislik(etiketayarDTO.getGenislik());
			etiket_Ayarlari.setSagbosluk(etiketayarDTO.getSagbosluk());
			etiket_Ayarlari.setSolbosluk(etiketayarDTO.getSolbosluk());
			etiket_Ayarlari.setUstbosluk(etiketayarDTO.getUstbosluk());
			etiket_Ayarlari.setYataydikey(etiketayarDTO.getYataydikey().equals("YATAY") ?  0 : 1);
			etiket_Ayarlari.setYukseklik(etiketayarDTO.getYukseklik());
			etiket_Ayarlari.setUser(user);
			user.setEtiket_Ayarlari(etiket_Ayarlari);
			userService.saveUser(user);
			response.put("errorMessage","");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}