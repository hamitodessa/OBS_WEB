package com.hamit.obs.controller.kereste;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KeresteGrupRaporController {

	@GetMapping("/kereste/grprapor")
	public String grprapor() {
		return "kereste/gruprapor";
	}
	
}
