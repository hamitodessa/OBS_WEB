package com.hamit.obs.controller.cari;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.cari.ekstreDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.cari.CariService;

import jakarta.mail.util.ByteArrayDataSource;

@Controller
public class EkstreController {

	@Autowired
	private CariService cariservice;

	@Autowired
	private RaporOlustur raporOlustur;


	@GetMapping("/cari/ekstre")
	public String ekstre() {
		return "cari/ekstre";
	}


	@PostMapping("cari/ekstre")
	@ResponseBody
	public Map<String, Object> sorgula(@RequestBody Map<String, Object> params) {
		Map<String, Object> response = new HashMap<>();
		try {
			String hesapKodu = (String) params.get("hesapKodu");
			String startDate = (String) params.get("startDate");
			String endDate = (String) params.get("endDate");
			int page = (int) params.get("page");
			int pageSize = (int) params.get("pageSize");
			Pageable pageable = PageRequest.of(page, pageSize);
			List<Map<String, Object>> ekstre = cariservice.ekstre(hesapKodu, startDate, endDate,pageable);
			response.put("success", true);
			response.put("data", (ekstre != null) ? ekstre : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("cari/ekssize")
	@ResponseBody
	public Map<String, Object> ekssize(@RequestBody Map<String, Object> params) {
		Map<String, Object> response = new HashMap<>();
		try {
			String hesapKodu = (String) params.get("hesapKodu");
			String startDate = (String) params.get("startDate");
			String endDate = (String) params.get("endDate");
			double ekssize = cariservice.eks_raporsize(hesapKodu, startDate, endDate);
			response.put("totalRecords", ekssize);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("cari/ekstre_download")
	public ResponseEntity<byte[]> downloadReport(@RequestBody ekstreDTO ekstreDTO) {
		ByteArrayDataSource dataSource ;
		try {
			dataSource =  raporOlustur.cari_ekstre(ekstreDTO.getKodu(), ekstreDTO.getStartDate(), ekstreDTO.getEndDate(), ekstreDTO.getFormat());
			if (dataSource == null) {
				throw new ServiceException("Rapor oluşturulamadı: veri bulunamadı.");
			}
			byte[] fileContent = dataSource.getInputStream().readAllBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			String fileName = "Cari_Ekstre." + ekstreDTO.getFormat().toLowerCase();
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