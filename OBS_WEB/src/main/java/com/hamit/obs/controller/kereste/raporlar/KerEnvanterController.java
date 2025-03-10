package com.hamit.obs.controller.kereste.raporlar;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KerEnvanterController {

	@GetMapping("/kereste/envanter")
	public String fatrapor() {
		return "kereste/envanter";
	}
}
