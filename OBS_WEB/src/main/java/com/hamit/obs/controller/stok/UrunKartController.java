package com.hamit.obs.controller.stok;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.fatura.FaturaService;

@Controller
public class UrunKartController {

	@Autowired
	private FaturaService faturaService;
	
	@GetMapping("/stok/urunkart")
	public Model urunkart(Model model) {
	    try {
	        model.addAttribute("urunKodlari", (faturaService.urun_kodlari() != null) ? faturaService.urun_kodlari() : new ArrayList<>());
	        model.addAttribute("errorMessage", "");
	    } catch (ServiceException e) {
	        model.addAttribute("errorMessage", e.getMessage());
	    } catch (Exception e) {
	        model.addAttribute("errorMessage", "Hata: " + e.getMessage());
	    }
	    return model;
	}
}
