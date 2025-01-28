package com.hamit.obs.controller.stok;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.custom.yardimci.KusurYuvarla;
import com.hamit.obs.custom.yardimci.Tarih_Cevir;
import com.hamit.obs.dto.stok.zaiDTO;
import com.hamit.obs.dto.stok.zaidetayDTO;
import com.hamit.obs.dto.stok.zaikayitDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.fatura.FaturaService;

@Controller
public class ZayiController {

	@Autowired
	private FaturaService faturaService;
	
	@GetMapping("stok/zayi")
	public Model zayi(Model model) {
		try {
			List<Map<String, Object>> anaKodlari = faturaService.stk_kod_degisken_oku("ANA_GRUP", "AGID_Y", "ANA_GRUP_DEGISKEN") ;
			Map<String, Object> anaDeger = new HashMap<>();
			anaDeger.put("ANA_GRUP", ""); 
			anaKodlari.add(0, anaDeger);
			model.addAttribute("anaKodlari", (anaKodlari != null) ? anaKodlari : new ArrayList<>());

//			List<Map<String, Object>> depoKodlari = faturaService.stk_kod_degisken_oku("DEPO", "DPID_Y", "DEPO_DEGISKEN") ;
//			Map<String, Object> depoDeger = new HashMap<>();
//			depoDeger.put("DEPO", ""); 
//			depoKodlari.add(0, depoDeger);
//			model.addAttribute("depoKodlari", (depoKodlari != null) ? depoKodlari : new ArrayList<>());
			
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

	@PostMapping("stok/zaisonfis")
	@ResponseBody
	public Map<String, String> zaisonfis() {
		Map<String, String> response = new HashMap<>();
		try {
			response.put("fisno", faturaService.zayi_son_bordro_no_al());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@GetMapping("stok/zaiyenifis")
	@ResponseBody
	public Map<String, Object> zaiyenifis() {
		Map<String, Object> response = new HashMap<>();
		try {
			int evrakNo = faturaService.zayi_fisno_al();
			int kj = 10 - Integer.toString(evrakNo).length();
			String str_ = "0".repeat(kj) + evrakNo;
			response.put("fisno", str_);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage",  e.getMessage());
		}
		return response;
	}
	
	@PostMapping("stok/zaiOku")
	@ResponseBody
	public Map<String, Object> zaiOku(@RequestParam String fisno) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> zayi = faturaService.zayi_oku(fisno,"ZAI");
			response.put("data", (zayi != null) ? zayi : new ArrayList<>());
			response.put("aciklama1",faturaService.aciklama_oku("ZAI", 1, fisno, "C"));
			response.put("aciklama2",faturaService.aciklama_oku("ZAI", 2, fisno, "C"));
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
	
	@PostMapping("stok/zaiYoket")
	@ResponseBody
	public ResponseEntity<Map<String, String>> zaiYoket(@RequestParam String fisno) {
		Map<String, String> response = new HashMap<>();
		try {
			String mesajlog = "Zayi Stok Silme";
			faturaService.stok_sil(fisno.trim().trim(), "ZAI", "C",mesajlog);
			faturaService.aciklama_sil("ZAI", fisno, "C");
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage",  e.getMessage());
		}
		return ResponseEntity.ok(response);
	}

	@PostMapping("stok/zaiKayit")
	@ResponseBody
	public  Map<String, Object>  zaiKayit(@RequestBody zaikayitDTO zaikayitDTO ) {
		Map<String, Object> response = new HashMap<>();
		try {
			zaiDTO dto = zaikayitDTO.getZaiDTO();
			List<zaidetayDTO> tableData = zaikayitDTO.getTableData();
			String tarih = Tarih_Cevir.dateFormaterSaatli(dto.getTarih());
			String mesajlog = "Zai Stok Silme" ;
			faturaService.stok_sil(dto.getFisno(), "ZAI", "C",mesajlog);
			faturaService.aciklama_sil("ZAI", dto.getFisno().trim(), "C");
					
			String userrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			int dpo = 0 ;
			int ana = 0 ;
			int alt = 0;
			for (zaidetayDTO row : tableData) {
				dpo = 0 ;
				ana = 0 ;
				alt = 0;
				if( ! row.getDepo().equals("")) {
					String dpos = faturaService.urun_kod_degisken_ara("DPID_Y", "DEPO", "DEPO_DEGISKEN",  row.getDepo());
					dpo = Integer.parseInt(dpos);
				}
				if(! dto.getAnagrup().equals("")) {
					String anas = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", dto.getAnagrup());
					ana = Integer.parseInt(anas);
				}
				if(! dto.getAltgrup().equals("")) {
					String alts = faturaService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", dto.getAltgrup());
					alt = Integer.parseInt(alts);
				}
				String izahat = row.getIzahat() ;
				if (izahat.equals(""))
					izahat = dto.getFisno() + " Nolu Zayiat Fisi..." ;
				
				mesajlog = "Zayiat Stok Kayit  Kod:" + row.getUkodu()  + " Miktar:" + row.getMiktar() + " Fiat:" + row.getFiat() ;
				faturaService.stk_kaydet(dto.getFisno(),"ZAI",tarih, dpo, row.getUkodu(),
						row.getMiktar() *-1, row.getFiat(), KusurYuvarla.round(row.getTutar() *-1,2), KusurYuvarla.round(row.getTutar() *-1,2), 
						"C",izahat, ana, alt, 0, "", "", "", userrString,mesajlog);
			}
			faturaService.aciklama_yaz("ZAI", 1, dto.getFisno().trim(), dto.getAcik1(), "C");
			faturaService.aciklama_yaz("ZAI", 2, dto.getFisno().trim(), dto.getAcik2(), "C");
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

}
