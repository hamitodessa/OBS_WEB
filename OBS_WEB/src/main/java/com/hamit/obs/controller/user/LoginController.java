package com.hamit.obs.controller.user;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.dto.user.CalismaDiziniDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.RolEnum;
import com.hamit.obs.service.adres.AdresService;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.fatura.FaturaService;
import com.hamit.obs.service.forum.ForumService;
import com.hamit.obs.service.kambiyo.KambiyoService;
import com.hamit.obs.service.kereste.KeresteService;
import com.hamit.obs.service.kur.KurService;
import com.hamit.obs.service.user.UserService;


@Controller
public class LoginController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private CariService cariService ;
	
	@Autowired
	private KurService kurService;
	
	@Autowired
	private AdresService adresService;
	
	@Autowired
	private KambiyoService kambiyoService;
	
	@Autowired
	private FaturaService faturaService;
	
	@Autowired
	private KeresteService keresteService;
	
	@Autowired
	private ForumService forumService;
	

	@GetMapping("/login")
	public String login(@RequestParam(required = false) String timeout, Model model) {
	    if ("true".equals(timeout)) {
	        model.addAttribute("message", "Oturum süresi doldu. Lütfen tekrar giriş yapınız.");
	    }
	    return "login"; 
	}
	
	@GetMapping("/index")
	public Model index(Model model) {
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		byte[] image = userService.getImage(useremail);
		model.addAttribute("profileImage", getBase64Image(image));
		List<RolEnum> roleNames =userService.getRoleNamesByEmail(useremail);
		boolean isAdmin = roleNames.contains(RolEnum.ADMIN);
		model.addAttribute("menuitemvisible", isAdmin);
		String apiUrl = "https://api.ipify.org?format=text";
		RestTemplate restTemplate = new RestTemplate();
		model.addAttribute("ip", restTemplate.getForObject(apiUrl, String.class));
		int mesajsayi = forumService.getmesajsayi(useremail);
		model.addAttribute("mesajadet", mesajsayi == 0 ? "":mesajsayi);
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
	       //dto.setFirma(cariService.cari_firma_adi());
	        dto.setFirma("");
	        dto.setModul(modulTipi.CARI_HESAP.getDbValue());
	        dto.setHangi_sql(cariService.conn_detail()[0]);
	        dto.setProgkodu(cariService.conn_detail()[1]);
	        dto.setServer(cariService.conn_detail()[2]);
	        calismaDiziniDTO.add(dto);
	        dto = new CalismaDiziniDTO();
	        dto.setFirma("");
	        dto.setModul(modulTipi.KUR.getDbValue());
	        dto.setHangi_sql(kurService.conn_detail()[0]);
	        dto.setProgkodu(kurService.conn_detail()[1]);
	        dto.setServer(kurService.conn_detail()[2]);
	        calismaDiziniDTO.add(dto);
	        dto = new CalismaDiziniDTO();
	        //dto.setFirma(kambiyoService.kambiyo_firma_adi());
	        dto.setFirma("");
	        dto.setModul(modulTipi.KAMBIYO.getDbValue());
	        dto.setHangi_sql(kambiyoService.conn_detail()[0]);
	        dto.setProgkodu(kambiyoService.conn_detail()[1]);
	        dto.setServer(kambiyoService.conn_detail()[2]);
	        calismaDiziniDTO.add(dto);
	        dto = new CalismaDiziniDTO();
	        //dto.setFirma(adresService.adres_firma_adi());
	        dto.setFirma("");
	        dto.setModul(modulTipi.ADRES.getDbValue());
	        dto.setHangi_sql(adresService.conn_detail()[0]);
	        dto.setProgkodu(adresService.conn_detail()[1]);
	        dto.setServer(adresService.conn_detail()[2]);
	        calismaDiziniDTO.add(dto);
	        dto = new CalismaDiziniDTO();
	        //dto.setFirma(kambiyoService.fatura_firma_adi());
	        dto.setFirma("");
	        dto.setModul(modulTipi.FATURA.getDbValue());
	        dto.setHangi_sql(faturaService.conn_detail()[0]);
	        dto.setProgkodu(faturaService.conn_detail()[1]);
	        dto.setServer(faturaService.conn_detail()[2]);
	        calismaDiziniDTO.add(dto);
	        dto = new CalismaDiziniDTO();
	      //dto.setFirma(keresteService.kereste_firma_adi());
	        dto.setFirma("");
	        dto.setModul(modulTipi.KERESTE.getDbValue());
	        dto.setHangi_sql(keresteService.conn_detail()[0]);
	        dto.setProgkodu(keresteService.conn_detail()[1]);
	        dto.setServer(keresteService.conn_detail()[2]);
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