package com.hamit.obs.controller.adres;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.adres.AdresService;

@Controller
public class EtiketListeController {

	@Autowired
	AdresService adresService;
	
	@Autowired
	private RaporOlustur raporOlustur;
	
	@GetMapping("/adres/etiketliste")
	public Map<String, Object>  etiketliste() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> etiketler = adresService.adr_etiket("Adi");
			response.put("etiketler",etiketler);
			response.put("errorMessage","");
			return response;
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
			return response;
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
			return response;
		}	
	}

	@PostMapping("adres/etiket_download")
	public ResponseEntity<byte[]> downloadReport(@RequestBody Map<String, List<Map<String, String>>> request) {
		ByteArrayDataSource dataSource ;
		try {
			dataSource =  raporOlustur.etiket(request.get("selectedRows"));
			if (dataSource == null) {
				throw new ServiceException("Rapor oluşturulamadı: veri bulunamadı.");
			}
			byte[] fileContent = dataSource.getInputStream().readAllBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			String fileName = "Etiket.pdf" ;
			headers.setContentDispositionFormData("attachment", fileName);
			return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage().getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Beklenmeyen bir hata oluştu.".getBytes(StandardCharsets.UTF_8));
		} finally {
			dataSource = null;
		}	
	}

}
