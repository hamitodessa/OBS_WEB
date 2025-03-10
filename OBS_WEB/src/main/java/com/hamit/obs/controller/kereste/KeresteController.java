package com.hamit.obs.controller.kereste;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.kereste.KeresteService;

@Controller
public class KeresteController {

	@Autowired
	private KeresteService keresteService;

	@GetMapping("kereste/getBaslik")
	@ResponseBody
	public Map<String, String> getBaslik() {
		Map<String, String> response = new HashMap<>();
		try {
			response.put("baslik", keresteService.ker_firma_adi());
			response.put("errorMessage","");
		} catch (ServiceException e) {
			response.put("baslik", ""); 
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kereste/altgrup")
	@ResponseBody
	public Map<String, Object> altgrup(@RequestParam String anagrup) {
		Map<String, Object> response = new HashMap<>();
		List<Map<String, Object>> altKodlari = new ArrayList<Map<String,Object>>();
		try {
			if(anagrup.equals("")) {
				Map<String, Object> altDeger = new HashMap<>();
				altDeger.put("ALT_GRUP", ""); 
				altKodlari.add(0, altDeger);
			}
			else {
				String qwe = keresteService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", anagrup);
				altKodlari = keresteService.ker_kod_alt_grup_degisken_oku(Integer.parseInt(qwe)) ;
				Map<String, Object> altDeger = new HashMap<>();
				altDeger.put("ALT_GRUP", ""); 
				altKodlari.add(0, altDeger);
			}
			response.put("altKodlari", altKodlari);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kereste/sonfis")
	@ResponseBody
	public Map<String, String> sonfis(@RequestParam String cins) {
		Map<String, String> response = new HashMap<>();
		try {
			if (cins.toString().equals("CIKIS"))
				response.put("fisno", keresteService.son_no_al("C"));
			else
				response.put("fisno", keresteService.son_no_al("G"));
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kereste/yenifis")
	@ResponseBody
	public Map<String, String> yenifis(@RequestParam String cins) {
		Map<String, String> response = new HashMap<>();
		try {
			int sno = 0 ;
			if (cins.toString().equals("CIKIS"))
				sno =  keresteService.evrak_no_al("C");
			else
				sno =  keresteService.evrak_no_al("G");
			int kj = 10 - Integer.toString(sno).length();
			StringBuilder strBuilder = new StringBuilder();
			for (int i = 0; i < kj; i++)
				strBuilder.append("0");
			strBuilder.append(sno);
			String str_ = strBuilder.toString();
			response.put("fisno", str_.equals("0000000000") ? "0000000001":str_);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kereste/paket_oku")
	@ResponseBody
	public Map<String, Object> paket_oku(@RequestParam String pno,@RequestParam String cins,@RequestParam String fisno) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> paket = new ArrayList<>();
			response.put("mesaj", "" );
			if (cins.toString().equals("CIKIS") )
			{
				paket = keresteService.paket_oku(pno, "C");
				if(paket.size() >0) {
					if (! paket.get(0).get("Cikis_Evrak").toString().equals("")   && ! paket.get(0).get("Cikis_Evrak").toString().equals(fisno))
						response.put("mesaj", paket.get(0).get("Evrak_No")  + " Nolu Evrakta Cikis Yapilmis.." );
					else
						response.put("paket", paket);
				}
				else {
					response.put("mesaj","");
					response.put("paket", paket);
				}
			}
			else
			{
				paket = keresteService.paket_oku(pno, "G");
				if(paket.size() >0) {
					if (! paket.get(0).get("Evrak_No").toString().equals(fisno)) {
						response.put("mesaj", paket.get(0).get("Evrak_No")  + " Nolu Evrakta Giris Yapilmis.." );
					}
				}
			}
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kereste/anadepo")
	@ResponseBody
	public Map<String, Object>  anadepo() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> anaKodlari = keresteService.ker_kod_degisken_oku("ANA_GRUP", "AGID_Y", "ANA_GRUP_DEGISKEN") ;
			Map<String, Object> anaDeger = new HashMap<>();
			anaDeger.put("ANA_GRUP", ""); 
			anaKodlari.add(0, anaDeger);
			response.put("anaKodlari", (anaKodlari != null) ? anaKodlari : new ArrayList<>());

			List<Map<String, Object>> depoKodlari = keresteService.ker_kod_degisken_oku("DEPO", "DPID_Y", "DEPO_DEGISKEN") ;
			Map<String, Object> depoDeger = new HashMap<>();
			depoDeger.put("DEPO", ""); 
			depoKodlari.add(0, depoDeger);
			response.put("depoKodlari", (depoKodlari != null) ? depoKodlari : new ArrayList<>());

			List<Map<String, Object>> oz1Kodlari = keresteService.ker_kod_degisken_oku("OZEL_KOD_1", "OZ1ID_Y", "OZ_KOD_1_DEGISKEN") ;
			Map<String, Object> oz1Deger = new HashMap<>();
			oz1Deger.put("OZEL_KOD_1", ""); 
			oz1Kodlari.add(0, oz1Deger);
			response.put("oz1Kodlari", (oz1Kodlari != null) ? oz1Kodlari : new ArrayList<>());

			response.put("errorMessage","");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}