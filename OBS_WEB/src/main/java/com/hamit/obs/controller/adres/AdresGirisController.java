package com.hamit.obs.controller.adres;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.adres.adresDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.adres.AdresService;

@Controller
public class AdresGirisController {

	@Autowired
	private AdresService adresService;

	@GetMapping("adres/adresgiris")
	public Model hesapPlani(Model model) {
		try {
			List<Map<String, Object>> hesapKodlari = adresService.hesap_kodlari();
			model.addAttribute("hesapKodlari", (hesapKodlari != null) ? hesapKodlari : new ArrayList<>());
			model.addAttribute("message", "");
			String qwString = (hesapKodlari != null && !hesapKodlari.isEmpty()) 
					? hesapKodlari.get(0).get("M_Kodu").toString() 
							: "";
			model.addAttribute("hesapKodu", qwString);
		} catch (ServiceException e) {
			model.addAttribute("message", e.getMessage());
		} catch (Exception e) {
			model.addAttribute("message", "Hata: " + e.getMessage());
		}
		return model;
	}

	@PostMapping("adres/adresArama")
	public ResponseEntity<?> hsplnArama(@RequestParam String arama) {
		adresDTO adresDTO = new adresDTO();
		try {
			adresDTO =  adresService.hsp_pln(arama);
			if (adresDTO.getKodu().equals("")) {
				throw new ServiceException("Bu Numarada Kayıtlı Hesap Yok");
			}
			if (adresDTO.getImage() != null) {
				String base64Image = Base64.getEncoder().encodeToString(adresDTO.getImage());
				adresDTO.setBase64Resim(base64Image);
				adresDTO.setImage(null) ;
			}
			adresDTO.setErrorMessage(""); 
		} catch (ServiceException e) {
			adresDTO.setErrorMessage(e.getMessage());
		} catch (Exception e) {
			adresDTO.setErrorMessage("Hata: " + e.getMessage());
		}
		return ResponseEntity.ok(adresDTO);
	}

	@GetMapping("adres/getBaslik")
	@ResponseBody
	public Map<String, String> getBaslik() {
		Map<String, String> response = new HashMap<>();
		try {
			response.put("baslik", adresService.adres_firma_adi());
			response.put("errorMessage","");
		} catch (ServiceException e) {
			response.put("baslik", ""); 
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}


	@PostMapping("adres/adrkayit")
	@ResponseBody
	public ResponseEntity<?> adres_kayit(@ModelAttribute adresDTO adresDTO) {
		try {
			if (adresDTO.getResim() != null) {
				byte[] resimBytes = adresDTO.getResim().getBytes();
				adresDTO.setImage(resimBytes);
			} else if (adresDTO.getResimGoster() != null) {
				byte[] resimGosterBytes = adresDTO.getResimGoster().getBytes();
				adresDTO.setImage(resimGosterBytes);
			} else {
				adresDTO.setImage(null);
			}
			if( adresDTO.getId() != 0)
				adresService.adres_sil(adresDTO.getId());
			adresService.adres_kayit(adresDTO);
			return ResponseEntity.ok(Map.of("errorMessage", ""));
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", e.getMessage()));
		}
	}

	@PostMapping("adr/hesapadi")
	@ResponseBody
	public Map<String, String> hesapadiOgren(@RequestParam String hesapkodu) {
		Map<String, String> response = new HashMap<>();
		try {
			String hesAdiString = adresService.kod_ismi(hesapkodu);
			if (hesAdiString != null ) {
				response.put("hesapAdi", hesAdiString);
				response.put("errorMessage", ""); 
			}else {
				response.put("hesapAdi", "");
				response.put("errorMessage", ""); 
			}
		} catch (ServiceException e) {
			response.put("hesapAdi", ""); 
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("adres/adrSil")
	@ResponseBody
	public Map<String, String> adrSil(@RequestParam int adrId) {
		Map<String, String> response = new HashMap<>();
		try {
			adresService.adres_sil(adrId);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	
}