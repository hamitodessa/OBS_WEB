package com.hamit.obs.controller.kereste;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.kereste.KeresteService;

@Controller
public class KerDegiskenlerController {


	@Autowired
	private KeresteService keresteService;

	@GetMapping("/kereste/degiskenler")
	public Model uretim(Model model) {
		List<Map<String, Object>> anaKodlari = keresteService.ker_kod_degisken_oku("ANA_GRUP", "AGID_Y", "ANA_GRUP_DEGISKEN") ;
		model.addAttribute("anaKodlari", (anaKodlari != null) ? anaKodlari : new ArrayList<>());
		return model;
	}

	@GetMapping("kereste/anagrpOku")
	@ResponseBody
	public Map<String, Object> anagrpOku() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> anaKodlari = keresteService.ker_kod_degisken_oku("ANA_GRUP", "AGID_Y", "ANA_GRUP_DEGISKEN") ;
			response.put("anagrp", anaKodlari); 
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@GetMapping("kereste/menseiOku")
	@ResponseBody
	public Map<String, Object> menseiOku() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> menseiKodlari = keresteService.ker_kod_degisken_oku("MENSEI", "MEID_Y", "MENSEI_DEGISKEN") ;
			response.put("mensei", menseiKodlari); 
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@GetMapping("kereste/depoOku")
	@ResponseBody
	public Map<String, Object> depoOku() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> depoKodlari = keresteService.ker_kod_degisken_oku("DEPO", "DPID_Y", "DEPO_DEGISKEN") ;
			response.put("depo", depoKodlari); 
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@GetMapping("kereste/oz1Oku")
	@ResponseBody
	public Map<String, Object> oz1Oku() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> oz1OkuKodlari = keresteService.ker_kod_degisken_oku("OZEL_KOD_1", "OZ1ID_Y", "OZ_KOD_1_DEGISKEN") ;
			response.put("oz1", oz1OkuKodlari); 
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@GetMapping("kereste/nakOku")
	@ResponseBody
	public Map<String, Object> nakOku() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> nakOkuKodlari = keresteService.ker_kod_degisken_oku("UNVAN", "NAKID_Y", "NAKLIYECI") ;
			response.put("nak", nakOkuKodlari); 
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kereste/altgrupdeg")
	@ResponseBody
	public Map<String, Object> altgrupdeg(@RequestParam String anagrup) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> altKodlari = new ArrayList<Map<String,Object>>();
			String qwe = keresteService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", anagrup);
			altKodlari = keresteService.ker_kod_alt_grup_degisken_oku(Integer.parseInt(qwe)) ;
			response.put("altKodlari", altKodlari);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kereste/degkayit")
	@ResponseBody
	public Map<String, Object> degkayit(@RequestBody Map<String, String> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			String aciklama = request.get("aciklama");
			String idacik = request.get("idacik");
			String degisken = request.get("degisken");
			String altgrpAna = request.get("altgrpAna");
			if( ! idacik.equals(""))//  ' ESKI KAYIT
			{ 
				if (degisken.equals("mensei"))
					keresteService.urun_degisken_eski("MENSEI", aciklama, "MENSEI_DEGISKEN", "MEID_Y",  Integer.parseInt(idacik));
				else  if (degisken.equals("anagrp"))
					keresteService.urun_degisken_eski("ANA_GRUP",aciklama, "ANA_GRUP_DEGISKEN", "AGID_Y", Integer.parseInt(idacik));
				else if (degisken.equals("altgrp"))
				{
					int in1  = 0;
					String qwe =  keresteService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN",altgrpAna);
					in1 = Integer.parseInt(qwe);
					keresteService.urun_degisken_alt_grup_eski(aciklama.trim(), in1, Integer.parseInt(idacik));
				}
				else  if (degisken.equals("depo"))
					keresteService.urun_degisken_eski("DEPO", aciklama.trim(), "DEPO_DEGISKEN", "DPID_Y",  Integer.parseInt(idacik));
				else  if (degisken.equals("oz1"))
					keresteService.urun_degisken_eski("OZEL_KOD_1",aciklama.trim(), "OZ_KOD_1_DEGISKEN", "OZ1ID_Y",  Integer.parseInt(idacik));
				else  if (degisken.equals("nak"))
					keresteService.urun_degisken_eski("UNVAN", aciklama.trim(), "NAKLIYECI"	, "NAKID_Y",  Integer.parseInt(idacik));
			}
			else {
				if (degisken.equals("mensei"))
					keresteService.urun_degisken_kayit("MEID_Y","MENSEI_DEGISKEN", "MENSEI", aciklama.trim());
				else  if (degisken.equals("anagrp"))
					keresteService.urun_degisken_kayit("AGID_Y","ANA_GRUP_DEGISKEN", "ANA_GRUP",aciklama.trim());
				else  if (degisken.equals("altgrp"))
				{
					int in1  = 0;  
					String qwe = keresteService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN",altgrpAna);
					in1 = Integer.parseInt(qwe);
					keresteService.urun_degisken_alt_grup_kayit(aciklama.trim(), in1);
				}
				else  if (degisken.equals("depo"))
					keresteService.urun_degisken_kayit("DPID_Y","DEPO_DEGISKEN", "DEPO",aciklama.trim());
				else  if (degisken.equals("oz1"))
					keresteService.urun_degisken_kayit("OZ1ID_Y","OZ_KOD_1_DEGISKEN", "OZEL_KOD_1",aciklama.trim());
				else  if (degisken.equals("nak"))
					keresteService.urun_degisken_kayit("NAKID_Y","NAKLIYECI", "UNVAN", aciklama.trim());
			}
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kereste/degsil")
	@ResponseBody
	public Map<String, Object> degsil(@RequestBody Map<String, String> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			String aciklama = request.get("aciklama");
			String idacik = request.get("idacik");
			String degisken = request.get("degisken");
			String altgrpAna = request.get("altgrpAna");
			if (degisken.equals("mensei"))
				keresteService.urun_kod_degisken_sil( "MEID_Y", "MENSEI_DEGISKEN", Integer.parseInt(idacik));
			else  if (degisken.equals("anagrp"))
				keresteService.urun_kod_degisken_sil( "AGID_Y", "ANA_GRUP_DEGISKEN", Integer.parseInt(idacik));
			else if (degisken.equals("altgrp"))
			{
				String qwe = keresteService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", altgrpAna);
				int anaG = 0;
				if (! qwe.equals(""))
					anaG  = Integer.parseInt(qwe);
				int altG = 0;
				String ewq = keresteService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN",aciklama.trim());
				if (! ewq.equals(""))
					altG  = Integer.parseInt(ewq);
				if (keresteService.alt_grup_kontrol(anaG,altG) )
				{
					response.put("errorMessage", "Ilk once Degisken Yenileme Bolumunden degistirip sonra siliniz....");
					return response;
				}
				keresteService.urun_degisken_alt_grup_sil(Integer.parseInt(idacik));				}
			else  if (degisken.equals("depo"))
				keresteService.urun_kod_degisken_sil("DPID_Y", "DEPO_DEGISKEN", Integer.parseInt(idacik));
			else  if (degisken.equals("oz1"))
				keresteService.urun_kod_degisken_sil("OZ1ID_Y", "OZ_KOD_1_DEGISKEN", Integer.parseInt(idacik));
			else  if (degisken.equals("nak"))
				keresteService.urun_kod_degisken_sil("NAKID_Y","NAKLIYECI", Integer.parseInt(idacik));
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}