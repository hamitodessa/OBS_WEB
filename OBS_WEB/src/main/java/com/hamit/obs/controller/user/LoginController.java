package com.hamit.obs.controller.user;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.hamit.obs.dto.user.CalismaDiziniDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.RolEnum;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.adres.AdresService;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.kambiyo.KambiyoService;
import com.hamit.obs.service.kur.KurService;
import com.hamit.obs.service.user.UserService;


@Controller
public class LoginController {

	@Autowired
	private UserService userService;
	
	@Autowired
	CariService cariService ;
	@Autowired
	KurService kurService;
	@Autowired
	AdresService adresService;
	@Autowired
	KambiyoService kambiyoService;
	

	@GetMapping("/login")
	public String login(@RequestParam(required = false) String timeout, Model model) {
	    if ("true".equals(timeout)) {
	        model.addAttribute("message", "Oturum süresi doldu. Lütfen tekrar giriş yapınız.");
	    }
	    return "login"; 
	}
	
	@GetMapping("/index")
	public Model index(Model model) {
		User user = userService.getCurrentUser();
		model.addAttribute("profileImage", getBase64Image(user != null ? user.getImage() : null));
		boolean isAdmin = user != null && user.getRoles().stream()
				.anyMatch(role -> role.getName() == RolEnum.ADMIN);
		model.addAttribute("menuitemvisible", isAdmin);
		String apiUrl = "https://api.ipify.org?format=text";
		RestTemplate restTemplate = new RestTemplate();
		model.addAttribute("ip", restTemplate.getForObject(apiUrl, String.class));
		return model;
	}

	private String getBase64Image(byte[] image) {
	    return image != null ? Base64.getEncoder().encodeToString(image) : null;
	}
	
	@GetMapping("/wellcome")
	public String deneme() {
		return "wellcome"; 
	}

	
	@GetMapping("/wellcomecalismadizini")
	public ResponseEntity<Map<String, Object>> wellcome() {
	    Map<String, Object> response = new HashMap<>();
	    try {
	        List<CalismaDiziniDTO> calismaDiziniDTO = new ArrayList<>();
	        
	        CalismaDiziniDTO dto = new CalismaDiziniDTO();
	        dto.setFirma(cariService.cari_firma_adi());
	        dto.setModul("Cari Hesap");
	        dto.setHangi_sql(cariService.cariConnDetails.getHangisql());
	        dto.setProgkodu(cariService.cariConnDetails.getDatabaseName());
	        dto.setServer(cariService.cariConnDetails.getServerIp());
	        calismaDiziniDTO.add(dto);
	        dto = new CalismaDiziniDTO();
	        dto.setFirma("");
	        dto.setModul("Kur");
	        dto.setHangi_sql(kurService.kurConnDetails.getHangisql());
	        dto.setProgkodu(kurService.kurConnDetails.getDatabaseName());
	        dto.setServer(kurService.kurConnDetails.getServerIp());
	        calismaDiziniDTO.add(dto);
	        dto = new CalismaDiziniDTO();
	        dto.setFirma(kambiyoService.kambiyo_firma_adi());
	        dto.setModul("Kambiyo");
	        dto.setHangi_sql(kambiyoService.kambiyoConnDetails.getHangisql());
	        dto.setProgkodu(kambiyoService.kambiyoConnDetails.getDatabaseName());
	        dto.setServer(kambiyoService.kambiyoConnDetails.getServerIp());
	        calismaDiziniDTO.add(dto);
	        dto = new CalismaDiziniDTO();
	        dto.setFirma(adresService.adres_firma_adi());
	        dto.setModul("Adres");
	        dto.setHangi_sql(adresService.adresConnDetails.getHangisql());
	        dto.setProgkodu(adresService.adresConnDetails.getDatabaseName());
	        dto.setServer(adresService.adresConnDetails.getServerIp());
	        calismaDiziniDTO.add(dto);
	        response.put("data", calismaDiziniDTO);
	        response.put("errorMessage", "");
	        
	        return ResponseEntity.ok(response);
	    } catch (ServiceException e) {
	        response.put("errorMessage", "Hata: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    } catch (Exception e) {
	        response.put("errorMessage", "Hata: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
}