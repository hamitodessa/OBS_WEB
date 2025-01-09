package com.hamit.obs.controller.user;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.Gonderilmis_Mailler;
import com.hamit.obs.service.user.GidenRaporService;

@Controller
public class GidenRaporlarController {

	@Autowired
	private GidenRaporService gidenRaporService;

	@GetMapping("user/gidenraporlar")
	public Model gidenRaporListelePage(Model model) {
		try {
			String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			List<Gonderilmis_Mailler> raporlar = gidenRaporService.gidenRaporListele(currentEmail);
			model.addAttribute("raporlar", raporlar);
			return model;
		} catch (ServiceException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return model;
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Hata: " + e.getMessage());
			return model;
		}	
	}

	@PostMapping("user/raporSil")
	public ResponseEntity<?> raporSil(@RequestParam Long raporId) {
		try {
			gidenRaporService.deletebyId(raporId);
			return ResponseEntity.ok(Map.of("errorMessage", ""));
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errorMessage", "Beklenmeyen bir hata oluştu: " + e.getMessage()));
		}
	}
}