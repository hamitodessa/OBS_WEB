package com.hamit.obs.controller.email;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.user.RaporEmailDegiskenler;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.Email_Details;
import com.hamit.obs.reports.RaporEmailGonderme;
import com.hamit.obs.service.user.EmailService;
import com.hamit.obs.service.user.GidenRaporService;

@Controller
public class SendEmailController {

	@Autowired
	private EmailService emailService; 

	@Autowired
	private RaporEmailGonderme raporEmailGonderme;
	
	@Autowired
	private GidenRaporService gidenRaporService;


	@GetMapping("/send_email")
	public String mailGondermeSayfa(@RequestParam String degerler, Model model) {
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
			model.addAttribute("degerler", degerler);
			model.addAttribute("alici", alicioku);
			model.addAttribute("hesap", email_Details.getHesap());
			model.addAttribute("isim", email_Details.getGon_isim());
			model.addAttribute("nerden", degerler.substring(degerler.lastIndexOf(",") + 1));
		} catch (ServiceException e) {
			model.addAttribute("error", e.getMessage());
		} catch (Exception e) {
			model.addAttribute("error", "Beklenmeyen bir hata oluştu: " + e.getMessage());
		}
		return "email/sendemail";
	}

	@PostMapping("send_email_gonder")
	@ResponseBody
	public Map<String, String> sendEmail(@RequestBody RaporEmailDegiskenler raporEmailDegiskenler) {
		Map<String, String> response = new HashMap<>();
		try {
			boolean durum = raporEmailGonderme.EmailGonderme(raporEmailDegiskenler);
			if (durum) {
				response.put("success", "E-posta başarıyla gönderildi.");
				response.put("errorMessage", "");
			} else {
				response.put("errorMessage", "E-posta gonderilemedi");
			}
		} catch (ServiceException e) {
			response.put("success", "");
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("success", "");
			response.put("errorMessage", e.getMessage());
		}
		return response;
	}
}