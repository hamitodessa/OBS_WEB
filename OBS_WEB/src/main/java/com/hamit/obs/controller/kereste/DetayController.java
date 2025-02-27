package com.hamit.obs.controller.kereste;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.hamit.obs.service.kereste.KeresteService;

@Controller
public class DetayController {

	@Autowired
	private KeresteService keresteService;

	@GetMapping("/kereste/detay")
	public String kodaciklama() {
		System.out.println("---------");
		return "kereste/detay";
	}
}
