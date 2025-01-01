package com.hamit.obs.controller.cari;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.hamit.obs.dto.cari.hesapplaniDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.user.UserService;

@Controller
public class HesapPlaniController {

	@Autowired
	private CariService cariservice;

	@Autowired
	private UserService userService;

	@GetMapping("cari/hspplngiris")
	public Model hesapPlani(Model model) {
	    try {
	        List<Map<String, Object>> hesapKodlari = cariservice.hesap_kodlari();
	        model.addAttribute("hesapKodlari", (hesapKodlari != null) ? hesapKodlari : new ArrayList<>());
	        model.addAttribute("message", "");
	        String qwString = (hesapKodlari != null && !hesapKodlari.isEmpty()) 
	                ? hesapKodlari.get(0).get("HESAP").toString() 
	                : "";
	        model.addAttribute("hesapKodu", qwString);
	    } catch (ServiceException e) {
	        model.addAttribute("message", e.getMessage());
	    } catch (Exception e) {
	        model.addAttribute("message", "Hata: " + e.getMessage());
	    }
	    return model;
	}

	@PostMapping("cari/hsplnkayit")
	@ResponseBody
	public Map<String, String> hsplnKayit(@ModelAttribute hesapplaniDTO hesapplaniDTO) {
		Map<String, String> response = new HashMap<>();
		User user = userService.getCurrentUser();
		String usrString = user.getFirstName().length() > 15 
				? user.getFirstName().substring(0, 15) 
						: user.getFirstName();
		hesapplaniDTO.setUsr(usrString);
		try {
			if (hesapplaniDTO.getResim() != null) {
			    byte[] resimBytes = hesapplaniDTO.getResim().getBytes();
			    hesapplaniDTO.setImage(resimBytes);
			} else if (hesapplaniDTO.getResimGoster() != null) {
			    byte[] resimGosterBytes = hesapplaniDTO.getResimGoster().getBytes();
			    hesapplaniDTO.setImage(resimGosterBytes);
			} else {
			    hesapplaniDTO.setImage(null);
			}
			cariservice.hsp_sil(hesapplaniDTO.getKodu());
			cariservice.hpln_kayit(hesapplaniDTO);
			cariservice.hpln_detay_kayit(hesapplaniDTO);
			response.put("errorMessage", "");
			return response;
		} catch (Exception e) {
			response.put("errorMessage", e.getMessage());
			return response;
		}
	}

	@PostMapping("cari/hsplnArama")
	public ResponseEntity<?> hsplnArama(@RequestParam String arama) {
		hesapplaniDTO hesapplaniDTO = new hesapplaniDTO();
		try {
			hesapplaniDTO =  cariservice.hsp_pln(arama);
	        if (hesapplaniDTO.getKodu().equals("")) {
	            throw new ServiceException("Bu Numarada Kayıtlı Hesap Yok");
	        }
	        if (hesapplaniDTO.getImage() != null) {
	            String base64Image = Base64.getEncoder().encodeToString(hesapplaniDTO.getImage());
	            hesapplaniDTO.setBase64Resim(base64Image);
	            hesapplaniDTO.setImage(null) ;
	        }
	        hesapplaniDTO.setErrorMessage(""); 
	    } catch (ServiceException e) {
	        hesapplaniDTO.setErrorMessage(e.getMessage());
	    } catch (Exception e) {
	        hesapplaniDTO.setErrorMessage("Hata: " + e.getMessage());
	    }
	    return ResponseEntity.ok(hesapplaniDTO);
	}
	
	@PostMapping("cari/hspplnSil")
	@ResponseBody
	public Map<String, String> hspplnSil(@RequestParam String hesapKodu) {
		Map<String, String> response = new HashMap<>();
		try {
			cariservice.hsp_sil(hesapKodu);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}