package com.hamit.obs.controller.kereste;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.custom.yardimci.Formatlama;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.custom.yardimci.Tarih_Cevir;
import com.hamit.obs.dto.cari.dekontDTO;
import com.hamit.obs.dto.kereste.keresteDTO;
import com.hamit.obs.dto.kereste.kerestedetayDTO;
import com.hamit.obs.dto.kereste.kerestekayitDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.kereste.KeresteService;
import com.hamit.obs.service.user.UserService;

@Controller
public class GirisController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private KeresteService keresteService;
	
	@Autowired
	private CariService cariservice;
	
	@GetMapping("kereste/giris")
	public Model fatura(Model model) {
		try {
			List<Map<String, Object>> anaKodlari = keresteService.ker_kod_degisken_oku("ANA_GRUP", "AGID_Y", "ANA_GRUP_DEGISKEN") ;
			Map<String, Object> anaDeger = new HashMap<>();
			anaDeger.put("ANA_GRUP", ""); 
			anaKodlari.add(0, anaDeger);
			model.addAttribute("anaKodlari", (anaKodlari != null) ? anaKodlari : new ArrayList<>());

			List<Map<String, Object>> depoKodlari = keresteService.ker_kod_degisken_oku("DEPO", "DPID_Y", "DEPO_DEGISKEN") ;
			Map<String, Object> depoDeger = new HashMap<>();
			depoDeger.put("DEPO", ""); 
			depoKodlari.add(0, depoDeger);
			model.addAttribute("depoKodlari", (depoKodlari != null) ? depoKodlari : new ArrayList<>());
			
			List<Map<String, Object>> menseiKodlari = keresteService.ker_kod_degisken_oku("MENSEI", "MEID_Y", "MENSEI_DEGISKEN") ;
			Map<String, Object> menseiDeger = new HashMap<>();
			menseiDeger.put("MENSEI", ""); 
			menseiKodlari.add(0, menseiDeger);
			model.addAttribute("menseiKodlari", (menseiKodlari != null) ? menseiKodlari : new ArrayList<>());

			List<Map<String, Object>> nakKodlari = keresteService.ker_kod_degisken_oku("UNVAN", "NAKID_Y", "NAKLIYECI") ;
			Map<String, Object> nakDeger = new HashMap<>();
			nakDeger.put("UNVAN", ""); 
			nakKodlari.add(0, nakDeger);
			model.addAttribute("nakKodlari", (nakKodlari != null) ? nakKodlari : new ArrayList<>());

			List<Map<String, Object>> oz1Kodlari = keresteService.ker_kod_degisken_oku("OZEL_KOD_1", "OZ1ID_Y", "OZ_KOD_1_DEGISKEN") ;
			Map<String, Object> oz1Deger = new HashMap<>();
			oz1Deger.put("OZEL_KOD_1", ""); 
			oz1Kodlari.add(0, oz1Deger);
			model.addAttribute("oz1Kodlari", (oz1Kodlari != null) ? oz1Kodlari : new ArrayList<>());

			model.addAttribute("doviz", userService.getCurrentUser().getCalisandvzcins()); 
			LocalDate today = LocalDate.now(); 
			model.addAttribute("fisTarih", today); 
			model.addAttribute("errorMessage", "");
		} catch (ServiceException e) {
			model.addAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Hata: " + e.getMessage());
		}
		return model;
	}
	
	@PostMapping("kereste/kerOku")
	@ResponseBody
	public Map<String, Object> kerOku(@RequestParam String fisno,@RequestParam String cins) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> kereste =new ArrayList<>();
			if (cins.toString().equals("CIKIS") )
			{
				kereste = keresteService.ker_oku(fisno.trim(), "C");
				response.put("a1",keresteService.aciklama_oku("KER", 1, fisno.trim(), "C"));
				response.put("a2",keresteService.aciklama_oku("KER", 2, fisno.trim(), "C"));
				response.put("dipnot",keresteService.dipnot_oku(fisno.trim(), "K", "C"));
			}
			else
			{
				kereste = keresteService.ker_oku(fisno.trim(), "G");
				response.put("a1",keresteService.aciklama_oku("KER", 1, fisno.trim(), "G"));
				response.put("a2",keresteService.aciklama_oku("KER", 2, fisno.trim(), "G"));
				response.put("dipnot",keresteService.dipnot_oku(fisno.trim(), "K", "G"));
			}
			response.put("data", (kereste != null) ? kereste : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("kereste/girKayit")
	@ResponseBody
	public  Map<String, Object>  kerKayit(@RequestBody kerestekayitDTO kerestekayitDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			keresteDTO dto = kerestekayitDTO.getKeresteDTO();
			List<kerestedetayDTO> tableData = kerestekayitDTO.getTableData();
			
			String mesajlog = dto.getFisno().trim() + " Nolu Giris Silindi" ;
			keresteService.ker_giris_sil(dto.getFisno().trim(),mesajlog);
			int degisken[] = degiskenler(dto) ; 
			String userrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());

			int index = 0;
			for (kerestedetayDTO row : tableData) {
				mesajlog = "Kereste Kayit" +  row.getUkodu() + " Mik=" + row.getMiktar() + " Tut=" + row.getTutar();

				row.setFisno(dto.getFisno());
				row.setCarikod(dto.getCarikod());
				row.setAdreskod(dto.getAdreskod());
				row.setTarih(Tarih_Cevir.dateFormaterSaatli(dto.getTarih()));
				
				row.setAnagrup(degisken[0]);
				row.setAltgrup(degisken[1]);
				row.setNakliyeci(degisken[2]);
				row.setOzelkod(degisken[3]);
				row.setDepo(degisken[4]);
				row.setMensei(degisken[5]);
				
				row.setDvzcins(dto.getDvzcins());
				row.setKur(dto.getKur());
				row.setTevoran(dto.getTevoran());
				row.setUser(userrString);
				row.setSatir(index);
				keresteService.ker_kaydet(row, mesajlog);
			    index++;
			}
			keresteService.dipnot_sil(dto.getFisno(), "K", "G");
			keresteService.dipnot_yaz(dto.getFisno(), dto.getNot1(),dto.getNot2(),dto.getNot3(),"K", "G",userrString);
			
			keresteService.aciklama_sil("KER", dto.getFisno().trim(), "G");
			
			keresteService.aciklama_yaz("KER", 1, dto.getFisno().trim(), dto.getAcik1().trim(), "G");
			keresteService.aciklama_yaz("KER", 2, dto.getFisno().trim(), dto.getAcik2().trim(), "G");
					
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("kereste/fisYoket")
	@ResponseBody
	public ResponseEntity<Map<String, String>> evrakSil(@RequestParam String fisno) {
		Map<String, String> response = new HashMap<>();
		try {
			String mesajlog = fisno.trim() + " Nolu Giris Silindi" ;
			keresteService.ker_giris_sil(fisno.trim(),mesajlog);
			keresteService.dipnot_sil(fisno.trim(), "K", "G");
			keresteService.aciklama_sil("KER", fisno.trim(), "G");
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage",  e.getMessage());
		}
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("kereste/kergcariKayit")
	@ResponseBody
	public ResponseEntity<?> kergcariKayit(@RequestBody keresteDTO keresteDTO ) {
		try {
			keresteDTO dto = keresteDTO;
			String[] hesapIsmi = {"",""};
			hesapIsmi = cariservice.hesap_adi_oku(dto.getKarsihesapkodu());
			if (hesapIsmi[0].equals("") ) {  
				 throw new ServiceException("Girilen Alacakli Hesap Kodunda  bir  hesaba rastlanmadi!!!!");
			} 
			double sdf =  dto.getMiktar();
			String aciklama = "" ;
			String userrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
				aciklama = dto.getFisno() + "'Evrak ile " + Formatlama.doub_0(sdf) +  " m3 Urun Girisi" ;
				dekontDTO dekontDTO = new dekontDTO();
				dekontDTO.setTar(Tarih_Cevir.dateFormaterSaatli(dto.getTarih()));
				dekontDTO.setFisNo(cariservice.yenifisno());
				dekontDTO.setBhes(dto.getKarsihesapkodu());
				dekontDTO.setBcins("");
				dekontDTO.setBkur(1);
				dekontDTO.setBorc(dto.getTutar());
				dekontDTO.setAhes(dto.getCarikod());
				dekontDTO.setAcins("");
				dekontDTO.setAkur(1);
				dekontDTO.setAlacak(dto.getTutar());
				dekontDTO.setIzahat(aciklama);
				dekontDTO.setKod("Alış");
				dekontDTO.setUser(userrString);
				cariservice.cari_dekont_kaydet(dekontDTO);
			
			return ResponseEntity.ok(Map.of("errorMessage", ""));
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", "Veriler kaydedilirken hata oluştu."));
		}
	}


	private int[] degiskenler(keresteDTO dto) throws ClassNotFoundException, SQLException
	{
		int degisken[] = {0,0,0,0,0,0} ;
		//*************anagrp
		if(! dto.getAnagrup().equals("")) {  
			String anas = keresteService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", dto.getAnagrup());
			degisken[0]  = Integer.parseInt(anas);
		}
		//*************alt grp
		if(! dto.getAltgrup().equals("")) {
			String alts = keresteService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", dto.getAltgrup());
			degisken[1]  = Integer.parseInt(alts);
		}
		//*************nakliyeci
		if(! dto.getNakliyeci().equals("")) {
			String naks = keresteService.urun_kod_degisken_ara("NAKID_Y", "UNVAN", "NAKLIYECI", dto.getNakliyeci());
			degisken[2]  = Integer.parseInt(naks); 
		}
		//*************oz kod
		if(! dto.getOzelkod().equals("")) {
			String ozks = keresteService.urun_kod_degisken_ara("OZ1ID_Y", "OZEL_KOD_1", "OZ_KOD_1_DEGISKEN", dto.getNakliyeci());
			degisken[3] = Integer.parseInt(ozks);
		}
		//*****Depo
		if( ! dto.getDepo().equals("")) {
			String dpos = keresteService.urun_kod_degisken_ara("DPID_Y", "DEPO", "DEPO_DEGISKEN",  dto.getDepo());
			degisken[4] = Integer.parseInt(dpos);
		}
		//*****Mensei
		if(! dto.getMensei().equals("")) {
			String mens = keresteService.urun_kod_degisken_ara("MEID_Y", "MENSEI", "MENSEI_DEGISKEN", dto.getMensei());
			degisken[5]  = Integer.parseInt(mens);
		}
		return degisken;
	}
}
