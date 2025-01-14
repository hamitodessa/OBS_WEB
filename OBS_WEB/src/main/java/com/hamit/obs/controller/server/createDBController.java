package com.hamit.obs.controller.server;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.dto.server.serverBilgiDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.server.ServerService;
import com.hamit.obs.service.user.UserService;

@Controller
public class createDBController {

	@Autowired
	private ServerService serverService ;

	@Autowired
	private UserService userService;
	
	@GetMapping("/user/createdb")
	public String createdb() {
		return "user/createdb";
	}

	@PostMapping("server/serverkontrol")
	@ResponseBody
	public Map<String, String> serverKontrol(@RequestBody serverBilgiDTO serverBilgiDTO) {
		Map<String, String> response = new HashMap<>();
		boolean result = false;
		try {
			result = serverService.serverKontrol(serverBilgiDTO);
			String serverDurum = result ? "true" : "false";
			response.put("serverDurum", serverDurum);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("serverDurum", "false");
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("serverDurum", "false");
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("server/dosyakontrol")
	@ResponseBody
	public Map<String, String> dosyakontrol(@RequestBody serverBilgiDTO serverBilgiDTO) {
		Map<String, String> response = new HashMap<>();
		boolean result = false;
		String drm  = "false" ;
		try {
			switch (serverBilgiDTO.getUser_modul()) {
			case "Cari Hesap": {
				serverBilgiDTO.setUser_modul_baslik("OK_Car");
				break;
			}
			case "Kur": {
				serverBilgiDTO.setUser_modul_baslik("OK_Kur");
				break;
			}
			case "Adres": {
				serverBilgiDTO.setUser_modul_baslik("OK_Adr");
				break;
			}
			case "Kambiyo": {
				serverBilgiDTO.setUser_modul_baslik("OK_Kam");
				break;
			}
			}
			result = serverService.dosyakontrol(serverBilgiDTO);
			drm = result != false ? "true" : "false" ;
			response.put("dosyaDurum",drm ); 
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("dosyaDurum",drm ); 
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("dosyaDurum",drm ); 
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("server/dosyaolustur")
	@ResponseBody
	public Map<String, String> dosyaolustur(@RequestBody serverBilgiDTO serverBilgiDTO) {
		Map<String, String> response = new HashMap<>();
		boolean result = false;
		String drm  = "false" ;
		String usrString =  Global_Yardimci.user_log(userService.getCurrentUser().getEmail());
		try {
			switch (serverBilgiDTO.getUser_modul()) {
			case "Cari Hesap": {
				serverBilgiDTO.setUser_name(usrString);
				serverBilgiDTO.setUser_modul_baslik("OK_Car");
				break;
			}
			case "Kur": {
				serverBilgiDTO.setUser_name(usrString);
				serverBilgiDTO.setUser_modul_baslik("OK_Kur");
				break;
			}
			case "Adres": {
				serverBilgiDTO.setUser_name(usrString);
				serverBilgiDTO.setUser_modul_baslik("OK_Adres");
				break;
			}
			case "Kambiyo": {
				serverBilgiDTO.setUser_name(usrString);
				serverBilgiDTO.setUser_modul_baslik("OK_Kam");
				break;
			}
			}
			result = serverService.dosyaolustur(serverBilgiDTO);
			drm = result != false ? "true" : "false" ;			response.put("olustuDurum",drm ); 
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("olustuDurum",drm ); 
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("olustuDurum",drm ); 
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}