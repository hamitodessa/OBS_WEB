package com.hamit.obs.controller.kereste.raporlar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.kereste.kerestedetayraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.kereste.KeresteService;

@Controller
public class DetayController {

	@Autowired
	private KeresteService keresteService;

	@GetMapping("/kereste/detay")
	public String kodaciklama() {
		return "kereste/detay";
	}
	
	@PostMapping("kereste/kerestedetaydoldur")
	@ResponseBody
	public Map<String, Object> stokdetaydoldur(@RequestBody kerestedetayraporDTO kerestedetayraporDTO ) {
		Map<String, Object> response = new HashMap<>();
		try {
			String turuString[] = grup_cevir(kerestedetayraporDTO.getGana(),kerestedetayraporDTO.getGalt(),kerestedetayraporDTO.getGdepo(),kerestedetayraporDTO.getGozkod(),
					kerestedetayraporDTO.getCana(),kerestedetayraporDTO.getCalt(),kerestedetayraporDTO.getCdepo(),kerestedetayraporDTO.getCozkod());
			kerestedetayraporDTO.setGana(turuString[0]);
			kerestedetayraporDTO.setGalt(turuString[1]);
			kerestedetayraporDTO.setGdepo(turuString[2]);
			kerestedetayraporDTO.setGozkod(turuString[3]);
			kerestedetayraporDTO.setCana(turuString[4]);
			kerestedetayraporDTO.setCalt(turuString[5]);
			kerestedetayraporDTO.setCdepo(turuString[6]);
			kerestedetayraporDTO.setCozkod(turuString[7]);
			Pageable pageable = PageRequest.of(kerestedetayraporDTO.getPage(), kerestedetayraporDTO.getPageSize());
			List<Map<String, Object>> kerdetay = keresteService.stok_rapor(kerestedetayraporDTO,pageable);
			response.put("data", (kerdetay != null) ? kerdetay : new ArrayList<>());
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("kereste/kerestedetaydoldursize")
	@ResponseBody
	public Map<String, Object> stokdetaydoldursize(@RequestBody kerestedetayraporDTO kerestedetayraporDTO ) {
		Map<String, Object> response = new HashMap<>();
		try {
			String turuString[] = grup_cevir(kerestedetayraporDTO.getGana(),kerestedetayraporDTO.getGalt(),kerestedetayraporDTO.getGdepo(),kerestedetayraporDTO.getGozkod(),
					kerestedetayraporDTO.getCana(),kerestedetayraporDTO.getCalt(),kerestedetayraporDTO.getCdepo(),kerestedetayraporDTO.getCozkod());
			kerestedetayraporDTO.setGana(turuString[0]);
			kerestedetayraporDTO.setGalt(turuString[1]);
			kerestedetayraporDTO.setGdepo(turuString[2]);
			kerestedetayraporDTO.setGozkod(turuString[3]);
			kerestedetayraporDTO.setCana(turuString[4]);
			kerestedetayraporDTO.setCalt(turuString[5]);
			kerestedetayraporDTO.setCdepo(turuString[6]);
			kerestedetayraporDTO.setCozkod(turuString[7]);
			double kerdetaysize = keresteService.stok_raporsize(kerestedetayraporDTO);
			response.put("totalRecords", kerdetaysize);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	private String[] grup_cevir(String ana,String alt,String dpo,String ozkod,String cana,String calt,String cdpo,String cozkod)
	{
		String deger[] = {"","","","","","","",""};
		String qwq1 = "", qwq2="", qwq3="",qwq4 = "",qwq5 = "", qwq6="",qwq7 = "",qwq8 = "";
		//***********************ANA GRUP
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
		//***********************ALT GRUP
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
		//***********************DEPO
		if (dpo.equals(""))
			qwq3 = " Like  '%' " ;
		else if  (dpo.equals("Bos Olanlar"))
			qwq3 = " = '' " ;
		else
		{
			String dpos = keresteService.urun_kod_degisken_ara("DPID_Y", "DEPO", "DEPO_DEGISKEN", dpo);
			qwq3 = "=" + dpos;
		}
		deger[2] = qwq3; 

		//***********************OZKOD
		if (ozkod.equals(""))
			qwq4 = " Like  '%' " ;
		else if  (ozkod.equals("Bos Olanlar"))
			qwq4 = " = '' " ;
		else
		{
			String anas = keresteService.urun_kod_degisken_ara("OZ1ID_Y", "OZEL_KOD_1", "OZ_KOD_1_DEGISKEN", ozkod);
			qwq4 = "=" + anas;
		}
		deger[3] = qwq4; 
		//***********************cANA GRUP
		if (cana.equals(""))
			qwq5 = " Like  '%' " ;
		else if  (ana.equals("Bos Olanlar"))
			qwq5 = " = '' " ;
		else
		{
			String canas = keresteService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", cana);
			qwq5 = "=" + canas;
		}
		deger[4] = qwq5; 
		//***********************cALT GRUP
		if (calt.equals(""))
			qwq6 = " Like  '%' " ;
		else if  (calt.equals("Bos Olanlar"))
			qwq6 = " = '' " ;
		else
		{
			String calts = keresteService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", calt);
			qwq6 ="=" + calts;
		}
		deger[5] = qwq6; 
		//***********************cDEPO
		if (cdpo.equals(""))
			qwq7 = " Like  '%' " ;
		else if  (cdpo.equals("Bos Olanlar"))
			qwq7 = " = '' " ;
		else
		{
			String cdpos = keresteService.urun_kod_degisken_ara("DPID_Y", "DEPO", "DEPO_DEGISKEN", cdpo);
			qwq7 = "=" + cdpos;
		}
		deger[6] = qwq7; 

		//***********************cOZKOD
		if (cozkod.equals(""))
			qwq8 = " Like  '%' " ;
		else if  (cozkod.equals("Bos Olanlar"))
			qwq8 = " = '' " ;
		else
		{
			String cozs = keresteService.urun_kod_degisken_ara("OZ1ID_Y", "OZEL_KOD_1", "OZ_KOD_1_DEGISKEN", cozkod);
			qwq8 = "=" + cozs;
		}
		deger[7] = qwq8; 
		return deger;
	}
}
