package com.hamit.obs.controller.stok;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hamit.obs.exception.ServiceException;

@Controller
public class FaturaController {

	@GetMapping("stok/fatura")
	public Model cekkontrol(Model model) {
		try {
			LocalDate today = LocalDate.now(); 
			model.addAttribute("gunlukTarih", today); 
			model.addAttribute("errorMessage", "");
		} catch (ServiceException e) {
			model.addAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Hata: " + e.getMessage());
		}
		return model;
	}

}
