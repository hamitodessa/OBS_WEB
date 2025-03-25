package com.hamit.obs.controller.email;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.Email_Details;
import com.hamit.obs.service.user.EmailService;
import com.hamit.obs.service.user.GidenRaporService;

@Controller
public class DuzMailGonderController {

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private GidenRaporService gidenRaporService;
	
	@GetMapping("/user/mailgonderme")
	public String mailGondermeSayfa(Model model) {
		try {
			String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			if (currentEmail == null) {
				throw new ServiceException("Kullanıcı oturumu bulunamadı.");
			}
			Email_Details email_Details = emailService.findByEmail(currentEmail);
			if (email_Details == null) {
				throw new ServiceException("Kullanıcıya ait e-posta bilgisi bulunamadı.");
			}
			List<String> alicioku = gidenRaporService.alicioku(currentEmail);
			model.addAttribute("degerler", "");
			model.addAttribute("alici", alicioku);
			model.addAttribute("hesap", email_Details.getHesap());
			model.addAttribute("isim", email_Details.getGon_isim());
			model.addAttribute("nerden", "duz");
		} catch (ServiceException e) {
			model.addAttribute("error", e.getMessage());
		} catch (Exception e) {
			model.addAttribute("error", "Beklenmeyen bir hata oluştu: " + e.getMessage());
		}
		return "email/sendemail";
	}
}