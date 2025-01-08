package com.hamit.obs.controller.cari;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.cari.dvzcevirmeDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.cari.CariService;

@Controller
public class DovizeCevirmeController {


	@Autowired
	private CariService cariservice;

	@Autowired
	private RaporOlustur raporOlustur;

	@GetMapping("/cari/dvzcevirme")
	public String dvzcevirme() {
		return "/cari/dvzcevirme";
	}

	@PostMapping("cari/dvzcevirme")
	@ResponseBody
	public Map<String, Object> sorgula(@RequestBody dvzcevirmeDTO dvzcevirmeDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> dvzcevirme = cariservice.dvzcevirme(dvzcevirmeDTO);
			response.put("success", true);
			response.put("data", (dvzcevirme != null) ? dvzcevirme : new ArrayList<>());
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("cari/dvzcevirme_download")
	public ResponseEntity<byte[]> downloadReport(@RequestBody dvzcevirmeDTO dvzcevirmeDTO) {
		ByteArrayDataSource dataSource ;
		try {
			dataSource =  raporOlustur.dvzcevirme(dvzcevirmeDTO);
			if (dataSource == null) {
				throw new ServiceException("Rapor oluşturulamadı: veri bulunamadı.");
			}
			byte[] fileContent = dataSource.getInputStream().readAllBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			String fileName = "Cari_Dovize_Cevirme." + dvzcevirmeDTO.getFormat().toLowerCase();
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