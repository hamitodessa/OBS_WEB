package com.hamit.obs.controller.stok.raporlar;

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

import com.hamit.obs.dto.stok.raporlar.envanterDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.fatura.FaturaService;
import com.hamit.obs.service.user.UserService;

@Controller
public class StokRaporController {

	@Autowired
	private FaturaService faturaService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RaporOlustur raporOlustur;
	
	@GetMapping("/stok/stokrapor")
	public String stokrapor() {
		return "stok/raporlar/stokrapor";
	}
	
	@PostMapping("stok/stokdoldur")
	@ResponseBody
	public Map<String, Object> stokdoldur(@RequestBody envanterDTO envanterDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			String turuString[] =  grup_cevir(envanterDTO.getAnagrp(),envanterDTO.getAltgrp(),envanterDTO.getDepo(),envanterDTO.getUranagrp(),envanterDTO.getUraltgrp());
			envanterDTO.setAnagrp(turuString[0]);
			envanterDTO.setAltgrp(turuString[1]);
			envanterDTO.setDepo(turuString[2]);
			envanterDTO.setUranagrp(turuString[3]);
			envanterDTO.setUraltgrp(turuString[4]);
			envanterDTO.setDoviz(userService.getCurrentUser().getCalisandvzcins());
			if(envanterDTO.getGruplama().equals("Urun Kodu"))
			{
				if(envanterDTO.isOncekitarih()) {
					List<Map<String, Object>> u_kodu = faturaService.envanter_rapor_u_kodu_oncekitarih(envanterDTO);
					response.put("data", (u_kodu != null) ? u_kodu : new ArrayList<>());
					response.put("raporturu","stokkoduonceki");
				}
				else {
					List<Map<String, Object>> u_kodu = faturaService.envanter_rapor_u_kodu(envanterDTO);
					response.put("data", (u_kodu != null) ? u_kodu : new ArrayList<>());
					response.put("raporturu","stokkodu");
				}
			}
			else if(envanterDTO.getFiatlama().equals("Ana_Grup_Alt_Grup"))
			{
				List<Map<String, Object>> fifo = faturaService.envanter_rapor_ana_grup_alt_grup(envanterDTO);
				response.put("data", (fifo != null) ? fifo : new ArrayList<>());
				response.put("raporturu","anaalt");
			}
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/env_download")
	public ResponseEntity<byte[]> downloadReport(@RequestBody List<Map<String, String>> tableData) {
		ByteArrayDataSource dataSource ;
		try {
			dataSource =  raporOlustur.envanter(tableData);
			if (dataSource == null) {
				throw new ServiceException("Rapor oluşturulamadı: veri bulunamadı.");
			}
			byte[] fileContent = dataSource.getInputStream().readAllBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			String fileName = "Envanter_Rapor.xlsx";
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

	private String[] grup_cevir(String ana,String alt,String dpo,String urana,String uralt)
	{
		String deger[] = {"","","","",""};
		String qwq1 = "", qwq2="", qwq3="",qwq4 = "",qwq5 = "";
		//***********************ANA GRUP
		if (ana.equals(""))
			qwq1 = " Like  '%' " ;
		else if  (ana.equals("Bos Olanlar"))
			qwq1 = " = '' " ;
		else
		{
			String anas = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", ana);
			qwq1 = "=" + anas;
		}
		deger[0] = qwq1; 
		//***********************ALT GRUP
		if (alt.equals(""))
			qwq2 = " Like  '%' " ;
		else if  (alt.equals("Bos Olanlar"))
			qwq2 = " = '' " ;
		else
		{
			String alts = faturaService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", alt);
			qwq2 ="=" + alts;
		}
		deger[1] = qwq2; 
		//***********************DEPO
		if (dpo.equals(""))
			qwq3 = " Like  '%' " ;
		else if  (dpo.equals("Bos Olanlar"))
			qwq3 = " = '' " ;
		else
		{
			String dpos = faturaService.urun_kod_degisken_ara("DPID_Y", "DEPO", "DEPO_DEGISKEN", dpo);
			qwq3 = "=" + dpos;
		}
		deger[2] = qwq3; 
		
		//***********************URUN ANA GRUP
		if (urana.equals(""))
			qwq4 = " Like  '%' " ;
		else if  (urana.equals("Bos Olanlar"))
			qwq4 = " = '' " ;
		else
		{
			String anas = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", urana);
			qwq4 = "=" + anas;
		}
		deger[3] = qwq4; 
		//*********************** URUN ALT GRUP
		if (uralt.equals(""))
			qwq5 = " Like  '%' " ;
		else if  (uralt.equals("Bos Olanlar"))
			qwq5 = " = '' " ;
		else
		{
			String alts = faturaService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", uralt);
			qwq5 ="=" + alts;
		}
		deger[4] = qwq5; 
		return deger;
	}

}
