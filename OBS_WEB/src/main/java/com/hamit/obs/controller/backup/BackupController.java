package com.hamit.obs.controller.backup;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BackupController {

	@GetMapping("/backup/backuptakip")
	public String backuptakip() {
		return "backup/backuptakip";
	}
	
	@GetMapping("/backup/lograpor")
	public String lograpor() {
		return "backup/lograpor";
	}
}