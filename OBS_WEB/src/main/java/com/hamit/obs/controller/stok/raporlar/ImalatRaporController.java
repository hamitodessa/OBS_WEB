package com.hamit.obs.controller.stok.raporlar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.hamit.obs.service.fatura.FaturaService;

@Controller
public class ImalatRaporController {
	
	@Autowired
	private FaturaService faturaService;

	@GetMapping("/stok/imarapor")
	public String fatrapor() {
		return "stok/raporlar/imarapor";
	}

}
