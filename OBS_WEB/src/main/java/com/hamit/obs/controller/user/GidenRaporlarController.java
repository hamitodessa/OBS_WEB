package com.hamit.obs.controller.user;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.Gonderilmis_Mailler;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.user.GidenRaporService;
import com.hamit.obs.service.user.UserService;

@Controller
public class GidenRaporlarController {

	@Autowired
	private GidenRaporService gidenRaporService;

	@Autowired
	private UserService userService;

	@GetMapping("user/gidenraporlar")
	public Model gidenRaporListelePage(Model model) {
		try {
			User user = userService.getCurrentUser();
			List<Gonderilmis_Mailler> raporlar = gidenRaporService.gidenRaporListele(user.getEmail());
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
			User user = userService.getCurrentUser();
			Gonderilmis_Mailler mailToRemove = user.getGonderilmisMailler().stream()
			        .filter(mail -> mail.getId().equals(raporId))
			        .findFirst()
			        .orElseThrow(() -> new RuntimeException("Mail not found"));
			if (!mailToRemove.getUser().equals(user)) {
		        throw new ServiceException("Bunu silme yetkiniz yok.");
		    }
			user.getGonderilmisMailler().remove(mailToRemove);
			userService.saveUser(user);
			return ResponseEntity.ok(Map.of("errorMessage", ""));
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errorMessage", "Beklenmeyen bir hata oluştu: " + e.getMessage()));
		}
	}
}