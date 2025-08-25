package com.hamit.obs.controller.stok.raporlar;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.util.ByteArrayDataSource;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.stok.raporlar.fatraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.fatura.FaturaService;

@Controller
public class IrsRaporController {

	@Autowired
	private RaporOlustur raporOlustur;

	@Autowired
	private FaturaService faturaService;

	@GetMapping("/stok/irsrapor")
	public String irsrapor() {
		return "stok/raporlar/irsrapor";
	}

	@PostMapping("stok/irsrapdoldur")
	@ResponseBody
	public Map<String, Object> irsrapdoldur(@RequestBody fatraporDTO fatraporDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			String turuString[] = grup_cevir(fatraporDTO.getAnagrp(), fatraporDTO.getAltgrp(),
					fatraporDTO.getTuru());
			fatraporDTO.setAnagrp(turuString[0]);
			fatraporDTO.setAltgrp(turuString[1]);
			fatraporDTO.setTuru(turuString[2]);
			List<Map<String, Object>> irs_listele = new ArrayList<>();
			Pageable pageable = PageRequest.of(fatraporDTO.getPage(), fatraporDTO.getPageSize());
			irs_listele = faturaService.irs_rapor(fatraporDTO, pageable);
			response.put("data", (irs_listele != null) ? irs_listele : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/irsdoldursize")
	@ResponseBody
	public Map<String, Object> irsdoldursize(@RequestBody fatraporDTO fatraporDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			String turuString[] = grup_cevir(fatraporDTO.getAnagrp(), fatraporDTO.getAltgrp(),
					fatraporDTO.getTuru());
			fatraporDTO.setAnagrp(turuString[0]);
			fatraporDTO.setAltgrp(turuString[1]);

			fatraporDTO.setTuru(turuString[2]);
			double fatdetaysize = faturaService.irs_raporsize(fatraporDTO);
			response.put("totalRecords", fatdetaysize);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/irsdetay")
	@ResponseBody
	public Map<String, Object> fatdetay(@RequestParam String evrakNo, @RequestParam String gircik) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> irsdetay = faturaService.irs_detay_rapor(evrakNo, gircik);
			response.put("data", (irsdetay != null) ? irsdetay : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/irsrap_download")
	public ResponseEntity<byte[]> downloadReport(@RequestBody List<Map<String, String>> tableData) {
		ByteArrayDataSource dataSource;
		try {
			dataSource = raporOlustur.irsrap(tableData);
			if (dataSource == null) {
				throw new ServiceException("Rapor oluşturulamadı: veri bulunamadı.");
			}
			byte[] fileContent = dataSource.getInputStream().readAllBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			String fileName = "Irsaliye_Rapor.xlsx";
			headers.setContentDispositionFormData("attachment", fileName);
			return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage().getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Beklenmeyen bir hata oluştu.".getBytes(StandardCharsets.UTF_8));
		} finally {
			dataSource = null;
		}
	}

	private String[] grup_cevir(String ana, String alt, String turu) {
		String deger[] = { "", "", "", "" };
		String qwq1 = "", qwq2 = "", tur = "";
		// ***********************ANA GRUP
		if (ana.equals(""))
			qwq1 = " Like  '%' ";
		else if (ana.equals("Bos Olanlar"))
			qwq1 = " = '' ";
		else {
			String anas = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", ana);
			qwq1 = "=" + anas;
		}
		deger[0] = qwq1;
		// ***********************ALT GRUP
		if (alt.equals(""))
			qwq2 = " Like  '%' ";
		else if (alt.equals("Bos Olanlar"))
			qwq2 = " = '' ";
		else {
			String alts = faturaService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", alt);
			qwq2 = "=" + alts;
		}
		deger[1] = qwq2;
		// ***********************TUR
		if (turu.equals("GIREN"))
			tur = "G";
		else if (turu.equals("CIKAN"))
			tur = "C";
		else if (turu.equals(""))
			tur = "";
		deger[2] = tur;
		return deger;
	}

}