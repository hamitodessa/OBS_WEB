package com.hamit.obs.controller.stok;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.stok.raporlar.fatraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.fatura.FaturaService;

@Controller
public class FaturaRaporController {

	@Autowired
	private FaturaService faturaService;

	@GetMapping("/stok/fatrapor")
	public String fatrapor() {
		return "stok/raporlar/fatrapor";
	}

	@PostMapping("stok/fatrapdoldur")
	@ResponseBody
	public Map<String, Object> fatrapdoldur(@RequestBody fatraporDTO fatraporDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			String turuString[] =  grup_cevir(fatraporDTO.getAnagrp(),fatraporDTO.getAltgrp(),fatraporDTO.getDepo(),fatraporDTO.getTuru());
			fatraporDTO.setAnagrp(turuString[0]);
			fatraporDTO.setAltgrp(turuString[1]);
			fatraporDTO.setDepo(turuString[2]);
			fatraporDTO.setTuru(turuString[3]);

			List<Map<String, Object>> fat_listele = faturaService.fat_rapor(fatraporDTO);
			response.put("data", (fat_listele != null) ? fat_listele : new ArrayList<>());
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			e.printStackTrace();
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("stok/fatdetay")
	@ResponseBody
	public Map<String, Object> fatdetay(@RequestParam String evrakNo,@RequestParam String gircik) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> fatdetay = faturaService.fat_detay_rapor(evrakNo,gircik);
			response.put("data", (fatdetay != null) ? fatdetay : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	private String[] grup_cevir(String ana,String alt,String dpo,String turu)
	{
		String deger[] = {"","","",""};
		String qwq1 = "", qwq2="", qwq3="",tur = "";
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
		//***********************TUR
		if (turu.equals("GIREN"))
			tur = "G" ;
		else if (turu.equals("CIKAN"))
			tur = "C" ;
		else if (turu.equals(""))
			tur = "" ;
		deger[3] = tur; 
		return deger;
	}
}