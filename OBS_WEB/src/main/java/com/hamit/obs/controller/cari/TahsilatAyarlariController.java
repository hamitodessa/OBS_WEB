package com.hamit.obs.controller.cari;

import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.cari.tahayarDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.cari.CariService;

@Controller
public class TahsilatAyarlariController {

	@Autowired
	private CariService cariservice;


	@GetMapping("cari/tahsilatdegerleri")
	public String register() {
		return "cari/tahsilatayar";
	}	

	@PostMapping("cari/tahayarYukle")
	@ResponseBody
	public ResponseEntity<tahayarDTO> tahayarYukle() {
		tahayarDTO tahayarDTO = new tahayarDTO();
		try {
			tahayarDTO = cariservice.tahayaroku();
			if (tahayarDTO.getImagelogo() != null) {
				String base64ImageLogo = Base64.getEncoder().encodeToString(tahayarDTO.getImagelogo());
				tahayarDTO.setBase64Resimlogo(base64ImageLogo);
				tahayarDTO.setImagelogo(null);
			}
			if (tahayarDTO.getImagekase() != null) {
				String base64ImageKase = Base64.getEncoder().encodeToString(tahayarDTO.getImagekase());
				tahayarDTO.setBase64Resimkase(base64ImageKase);
				tahayarDTO.setImagekase(null);
			}
			tahayarDTO.setErrorMessage(""); 
			return ResponseEntity.ok(tahayarDTO); 
		} catch (ServiceException e) {
			tahayarDTO.setErrorMessage(e.getMessage());
		} catch (Exception e) {
			tahayarDTO.setErrorMessage("Hata: " + e.getMessage());
		}
		return ResponseEntity.badRequest().body(tahayarDTO);
	}

	@PostMapping("cari/tahayarkayit")
	@ResponseBody
	public ResponseEntity<?> tahayarkayit(@ModelAttribute tahayarDTO tahayarDTO) {
		try {
			if (tahayarDTO.getResimlogo() != null) {
				byte[] resimBytes = tahayarDTO.getResimlogo().getBytes();
				tahayarDTO.setImagelogo(resimBytes);
			} else if (tahayarDTO.getResimgosterlogo() != null) {
				byte[] resimGosterBytes = tahayarDTO.getResimgosterlogo().getBytes();
				tahayarDTO.setImagelogo(resimGosterBytes);
			} else {
				tahayarDTO.setImagelogo(null);
			}
			if (tahayarDTO.getResimkase() != null) {
				byte[] resimBytes = tahayarDTO.getResimkase().getBytes();
				tahayarDTO.setImagekase(resimBytes);
			} else if (tahayarDTO.getResimgosterkase() != null) {
				byte[] resimGosterBytes = tahayarDTO.getResimgosterkase().getBytes();
				tahayarDTO.setImagekase(resimGosterBytes);
			} else {
				tahayarDTO.setImagekase(null);
			}
			cariservice.tahayar_kayit(tahayarDTO);
			return ResponseEntity.ok(Map.of("errorMessage", ""));
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", e.getMessage()));
		}
	}
}