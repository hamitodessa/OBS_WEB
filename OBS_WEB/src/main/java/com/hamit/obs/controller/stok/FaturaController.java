package com.hamit.obs.controller.stok;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.stok.urunDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.fatura.FaturaService;

@Controller
public class FaturaController {

	@Autowired
	private FaturaService faturaService;
	
	@GetMapping("stok/fatura")
	public Model cekkontrol(Model model) {
		try {
			List<Map<String, Object>> anaKodlari = faturaService.stk_kod_degisken_oku("ANA_GRUP", "AGID_Y", "ANA_GRUP_DEGISKEN") ;
			Map<String, Object> anaDeger = new HashMap<>();
			anaDeger.put("ANA_GRUP", ""); 
			anaKodlari.add(0, anaDeger);
			model.addAttribute("anaKodlari", (anaKodlari != null) ? anaKodlari : new ArrayList<>());

			List<Map<String, Object>> depoKodlari = faturaService.stk_kod_degisken_oku("DEPO", "DPID_Y", "DEPO_DEGISKEN") ;
			Map<String, Object> depoDeger = new HashMap<>();
			depoDeger.put("DEPO", ""); 
			depoKodlari.add(0, depoDeger);
			model.addAttribute("depoKodlari", (depoKodlari != null) ? depoKodlari : new ArrayList<>());
			LocalDate today = LocalDate.now(); 
			model.addAttribute("fisTarih", today); 
			model.addAttribute("errorMessage", "");
		} catch (ServiceException e) {
			model.addAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("errorMessage", "Hata: " + e.getMessage());
		}
		return model;
	}

	@PostMapping("stok/urunoku")
	@ResponseBody
	public Map<String, Object> urunoku(@RequestParam String ukodu, @RequestParam String barkod, @RequestParam String fiatlama,String gircik,String ckod) {
		Map<String, Object> response = new HashMap<>();
		try {
			urunDTO urunDTO = new urunDTO();
			if(! ukodu.equals(""))
				urunDTO =  faturaService.stk_urun("Kodu",ukodu);
			if(! barkod.equals(""))
				urunDTO =  faturaService.stk_urun("Barkod",ukodu);
			if (urunDTO.getKodu().equals("")) {
				throw new ServiceException("Bu Kodda Urun Yok");
			}
			if (urunDTO.getImage() != null) {
	            String base64Image = Base64.getEncoder().encodeToString(urunDTO.getImage());
	            urunDTO.setBase64Resim(base64Image);
	            urunDTO.setImage(null) ;
	        }
			response.put("dto",urunDTO);
			if (! fiatlama.equals(""))
			{
				if ( fiatlama.equals("fiat1"))
					response.put("fiat", urunDTO.getFiat1());
				else  if (fiatlama.equals("fiat2"))
					response.put("fiat", urunDTO.getFiat2());
				else  if (fiatlama.equals("fiat3"))
					response.put("fiat", urunDTO.getFiat3());
				else  if (fiatlama.equals("sonsatis"))
					response.put("fiat", faturaService.son_satis_fiati_oku(ukodu,ckod, gircik.equals("SATIS") ? "C" : "G"));
				}else {
				response.put("fiat",0);
			}
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/fatOku")
	@ResponseBody
	public Map<String, Object> uretimOku(@RequestParam String fisno,@RequestParam String cins) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> fatura =new ArrayList<>();
			if (cins.toString().equals("SATIS") )
			{
				fatura = faturaService.fatura_oku(fisno.trim(), "C");
				response.put("a1",faturaService.aciklama_oku("FAT", 1, fisno.trim(), "C"));
				response.put("a2",faturaService.aciklama_oku("FAT", 2, fisno.trim(), "C"));
				response.put("dipnot",faturaService.dipnot_oku(fisno.trim(), "F", "C"));
			}
			else
			{
				fatura = faturaService.fatura_oku(fisno.trim(), "G");
				response.put("a1",faturaService.aciklama_oku("FAT", 1, fisno.trim(), "G"));
				response.put("a2",faturaService.aciklama_oku("FAT", 2, fisno.trim(), "G"));
				response.put("dipnot",faturaService.dipnot_oku(fisno.trim(), "F", "G"));
			}
			response.put("data", (fatura != null) ? fatura : new ArrayList<>());
			
			
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

}
