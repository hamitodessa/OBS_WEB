package com.hamit.obs.controller.stok;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FaturaRaporController {

	@GetMapping("/stok/fatrapor")
	public String ekstre() {
		return "stok/raporlar/fatrapor";
	}
}
