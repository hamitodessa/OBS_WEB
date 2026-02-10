package com.hamit.obs.controller.cari;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.dto.cari.dekontDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.cari.CariService;

@Controller
public class DekontController {

	@Autowired
	private CariService cariservice;
	
	@GetMapping("cari/dekont")
	public Model register(Model model) {
	    try {
	        model.addAttribute("hesapKodlari", (cariservice.hesap_kodlari() != null) ? cariservice.hesap_kodlari() : new ArrayList<>());
	        LocalDate today = LocalDate.now(); 
	        model.addAttribute("evrakTarih", today); 
	        model.addAttribute("errorMessage", "");
	    } catch (ServiceException e) {
	        model.addAttribute("errorMessage", e.getMessage());
	    } catch (Exception e) {
	        model.addAttribute("errorMessage", "Hata: " + e.getMessage());
	    }
	    return model;
	}
	
	@PostMapping("cari/sonfisNo")
	@ResponseBody
	public dekontDTO sonfisNo() {
		dekontDTO response = new dekontDTO();
	    try {
	        int evrakNo = cariservice.sonfisNo();
	        if (evrakNo == 0) {
	            throw new ServiceException("Dosyada Kayit Yok");
	        }
	        response.setFisNo(evrakNo);
	        response.setErrorMessage(""); // Hata yoksa boş mesaj
	    } catch (ServiceException e) {
	        response.setFisNo(0);
	        response.setErrorMessage(e.getMessage()); // ServiceException hatası
	    } catch (Exception e) {
	        response.setFisNo(0);
	        response.setErrorMessage("Hata: " + e.getMessage()); // Diğer hatalar
	    }
	    return response;
	}
	
	@PostMapping("cari/evrakOku")
	@ResponseBody
	public ResponseEntity<List<dekontDTO>> evrakOku(@RequestParam int evrakNo) {
	    List<dekontDTO> fiskon = new ArrayList<>();
	    try {
	        fiskon = cariservice.fiskon(evrakNo);
	        if (fiskon.isEmpty()) {
	            fiskon.add(new dekontDTO());
		        fiskon.get(0).setErrorMessage("Bu Numarada Kayıtlı Fiş Yok");
	        }
	        else
	        	fiskon.forEach(dto -> dto.setErrorMessage("")); // Hata mesajlarını temizle
	        return ResponseEntity.ok(fiskon); // Başarılı yanıt
	    } catch (ServiceException e) {
            fiskon.add(new dekontDTO());
	        fiskon.get(0).setErrorMessage(e.getMessage());
	    } catch (Exception e) {
            fiskon.add(new dekontDTO());
	        fiskon.get(0).setErrorMessage("Hata: " + e.getMessage());
	    }
	    return ResponseEntity.badRequest().body(fiskon); // Hata yanıtı
	}

	@PostMapping("cari/yenifisNo")
	@ResponseBody
	public dekontDTO yenifisNo() {
		dekontDTO response = new dekontDTO();
	    try {
	        int evrakNo = cariservice.yenifisno();
	        response.setFisNo(evrakNo);
	        response.setErrorMessage("");
	    } catch (ServiceException e) {
	        response.setFisNo(0);
	        response.setErrorMessage(e.getMessage());
	    } catch (Exception e) {
	        response.setFisNo(0);
	        response.setErrorMessage("Hata: " + e.getMessage());
	    }
	    return response;
	}
	
	@PostMapping("cari/fiskayit")
	@ResponseBody
	public Map<String, Object> fiskayit(@RequestBody dekontDTO dekontDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			String usrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			dekontDTO.setUser(usrString);
			//cariservice.evrak_yoket(dekontDTO.getFisNo(),usrString);
			boolean status = cariservice.cari_dekont_kaydet(dekontDTO);
			if (status) {
				response.put("message", "Kayıt başarılı");
				response.put("errorMessage", "");
			} else {
				response.put("message", "Kayıt işlemi başarısız");
				response.put("errorMessage", "Kayıt işlemi başarısız");
			}
		} catch (ServiceException e) {
			response.put("message", "Kayıt işlemi başarısız");
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("message", "Kayıt işlemi başarısız");
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("cari/fisYoket")
	@ResponseBody
	public ResponseEntity<Map<String, String>> evrakSil(@RequestParam int evrakNo) {
		Map<String, String> response = new HashMap<>();
		try {
			String usrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			cariservice.evrak_yoket(evrakNo,usrString);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage",  e.getMessage());
		}
		return ResponseEntity.ok(response); // HTTP 200
	}
}