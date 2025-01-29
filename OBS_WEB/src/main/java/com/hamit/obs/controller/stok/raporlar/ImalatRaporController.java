package com.hamit.obs.controller.stok.raporlar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.stok.raporlar.imaraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.fatura.FaturaService;

@Controller
public class ImalatRaporController {
	
	@Autowired
	private FaturaService faturaService;

	@GetMapping("/stok/imarapor")
	public String fatrapor() {
		return "stok/raporlar/imarapor";
	}

	@PostMapping("stok/imarapdoldur")
	@ResponseBody
	public Map<String, Object> imarapordoldur(@RequestBody imaraporDTO imaraporDTO) {
		System.out.println("34");
		Map<String, Object> response = new HashMap<>();
		try {
			String turuString[] =  grup_cevir(imaraporDTO.getAnagrp(),imaraporDTO.getAltgrp(),imaraporDTO.getDepo(),imaraporDTO.getUranagrp(),imaraporDTO.getUraltgrp());
			imaraporDTO.setAnagrp(turuString[0]);
			imaraporDTO.setAltgrp(turuString[1]);
			imaraporDTO.setDepo(turuString[2]);
			imaraporDTO.setUranagrp(turuString[3]);
			imaraporDTO.setUraltgrp(turuString[4]);
			List<Map<String, Object>> fat_listele = faturaService.imalat_rapor(imaraporDTO);
			response.put("data", (fat_listele != null) ? fat_listele : new ArrayList<>());
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
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
