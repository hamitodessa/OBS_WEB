package com.hamit.obs.controller.kereste.raporlar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.dto.kereste.kergrupraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.kereste.KeresteService;

@Controller
public class KerOrtFiatController {

	@Autowired
	private KeresteService keresteService;
	
	@GetMapping("/kereste/ortfiat")
	public String fatrapor() {
		return "kereste/ortfiat";
	}
	
	@PostMapping("kereste/ortfiatdoldur")
	@ResponseBody
	public Map<String, Object> grpdoldur(@RequestBody kergrupraporDTO kergrupraporDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kerConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			String turuString[] =  grup_cevir(kergrupraporDTO.getAnagrp(),kergrupraporDTO.getAltgrp(),kergrupraporDTO.getOzkod());

			kergrupraporDTO.setAnagrp(turuString[0]);
			kergrupraporDTO.setAltgrp(turuString[1]);
			kergrupraporDTO.setOzkod(turuString[2]);
			
			System.out.println(kergrupraporDTO.getTuru());
			
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	private String[] grup_cevir(String ana,String alt, String oz1)
	{
		String deger[] = {"","","",""};
		String qwq1 = "", qwq2="", qwq3="",qwq4="";
		if (ana.equals(""))
			qwq1 = " Like  '%' " ;
		else if  (ana.equals("Bos Olanlar"))
			qwq1 = " = '' " ;
		else
		{
			String anas = keresteService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", ana);
			qwq1 = "=" + anas;
		}
		deger[0] = qwq1; 
		if (alt.equals(""))
			qwq2 = " Like  '%' " ;
		else if  (alt.equals("Bos Olanlar"))
			qwq2 = " = '' " ;
		else
		{
			String alts = keresteService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", alt);
			qwq2 ="=" + alts;
		}
		deger[1] = qwq2; 
		if (oz1.equals(""))
			qwq3 = " Like  '%' " ;
		else if  (oz1.equals("Bos Olanlar"))
			qwq3 = " = '' " ;
		else
		{
			String ozs1 = keresteService.urun_kod_degisken_ara("OZ1ID_Y", "OZEL_KOD_1", "OZ_KOD_1_DEGISKEN", oz1);
			qwq3 = "=" + ozs1;
		}
		deger[2] = qwq3; 
		return deger;
	}
}
