package com.hamit.obs.controller.stok.raporlar;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ImalatGrupController {

	
	@GetMapping("/stok/imagrprapor")
	public String imagrprapor() {
		return "stok/raporlar/imagrprapor";
	}
}
