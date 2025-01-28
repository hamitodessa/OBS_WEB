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
			List<Map<String, Object>> fat_listele = faturaService.fat_rapor(fatraporDTO);
			String turuString = "" ;
			if(fatraporDTO.getTuru().equals("GIREN"))
				turuString = "G";
			else
				turuString = "C";
			fatraporDTO.setTuru(turuString);

			int dpo = 0 ;
			int ana = 0 ;
			int alt = 0;
			if( ! fatraporDTO.getDepo().equals("")) {
				String dpos = faturaService.urun_kod_degisken_ara("DPID_Y", "DEPO", "DEPO_DEGISKEN", fatraporDTO.getDepo());
				dpo = Integer.parseInt(dpos);
			}
			if(! fatraporDTO.getAnagrp().equals("")) {
				String anas = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", fatraporDTO.getAnagrp());
				ana = Integer.parseInt(anas);
			}
			if(! fatraporDTO.getAltgrp().equals("")) {
				String alts = faturaService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", fatraporDTO.getAltgrp());
				alt = Integer.parseInt(alts);
			}
			fatraporDTO.setAnaint(ana);
			fatraporDTO.setAltint(alt);
			fatraporDTO.setDpoint(dpo);
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

}
