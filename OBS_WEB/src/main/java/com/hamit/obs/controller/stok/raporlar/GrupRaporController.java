package com.hamit.obs.controller.stok.raporlar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.fatura.FaturaService;
import com.hamit.obs.service.user.UserService;

@Controller
public class GrupRaporController {

	@Autowired
	private FaturaService faturaService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RaporOlustur raporOlustur;
	
	@GetMapping("/stok/grprapor")
	public String stokrapor() {
		return "stok/raporlar/gruprapor";
	}
}
