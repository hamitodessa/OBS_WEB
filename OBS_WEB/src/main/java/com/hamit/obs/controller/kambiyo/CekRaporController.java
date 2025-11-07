package com.hamit.obs.controller.kambiyo;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.mail.util.ByteArrayDataSource;
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

import com.hamit.obs.dto.kambiyo.cekraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.kambiyo.KambiyoService;

@Controller
public class CekRaporController {

	@Autowired
	private RaporOlustur raporOlustur;
	
	@Autowired 
	KambiyoService kambiyoService;
	
	@GetMapping("/kambiyo/cekrapor")
	public String ekstre() {
		return "kambiyo/cekraporlama";
	}

	
	@PostMapping("kambiyo/cekraporlama")
	@ResponseBody
	public Map<String, Object> sorgula(@RequestBody cekraporDTO cekraporDTO ) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> cek_rapor = kambiyoService.cek_rapor(cekraporDTO);
			response.put("success", true);
			response.put("data", (cek_rapor != null) ? cek_rapor : new ArrayList<>());
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
	
	@PostMapping("kambiyo/cekrap_download")
	public ResponseEntity<byte[]> cekrap_download(@RequestBody List<Map<String, String>> tableData) {
		ByteArrayDataSource dataSource ;
		try {
			dataSource =  raporOlustur.cekrap(tableData);
			if (dataSource == null) {
				throw new ServiceException("Rapor oluşturulamadı: veri bulunamadı.");
			}
			byte[] fileContent = dataSource.getInputStream().readAllBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			String fileName = "CekRapor_Rapor.xlsx";
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