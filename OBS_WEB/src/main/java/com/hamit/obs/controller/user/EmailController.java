package com.hamit.obs.controller.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.custom.yardimci.TextSifreleme;
import com.hamit.obs.dto.email.EmailAyarlarDTO;
import com.hamit.obs.model.user.Email_Details;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.user.EmailService;
import com.hamit.obs.service.user.UserService;

@Controller
public class EmailController {

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserService userService;

	@GetMapping("user/emailsettings")
	public String emailSettingsPage(Model model) {
	    model.addAttribute("user", new EmailAyarlarDTO()); // boş gelsin
	    return "user/emailsettings";
	}

	@GetMapping("user/emailsettings_data")
	@ResponseBody
	public EmailAyarlarDTO emailSettingsData() {
	    String useremail = SecurityContextHolder.getContext().getAuthentication().getName();

	    EmailAyarlarDTO dto = new EmailAyarlarDTO();
	    Email_Details email = emailService.findByEmail(useremail);

	    if (email != null) {
	        dto.setId(email.getId());
	        dto.setHesap(email.getHesap());
	        dto.setHost(email.getHost());
	        dto.setPort(email.getPort());
	        try { dto.setSifre(TextSifreleme.decrypt(email.getSifre())); } catch (Exception ignored) {}
	        dto.setGon_mail(email.getGon_mail());
	        dto.setGon_isim(email.getGon_isim());
	        dto.setBssl(email.getBssl());
	        dto.setBtsl(email.getBtsl());
	    } else {
	        dto.setHesap(""); dto.setHost(""); dto.setPort(""); dto.setSifre("");
	        dto.setGon_mail(""); dto.setGon_isim("");
	        dto.setBssl(false); dto.setBtsl(false);
	    }
	    return dto;
	}


	@PostMapping("user/emailsettings_save")
	@ResponseBody
	public Map<String, String> emailSettingsSave(@ModelAttribute EmailAyarlarDTO emailDTO) {
	    Map<String, String> res = new HashMap<>();
	    try {
	        User user = userService.getCurrentUser();

	        Email_Details emailDetails = user.getEmailDetails();
	        if (emailDetails == null) emailDetails = new Email_Details();

	        emailDetails.setEmail(user.getEmail());
	        emailDetails.setBssl(emailDTO.isBssl());
	        emailDetails.setBtsl(emailDTO.isBtsl());
	        emailDetails.setGon_isim(emailDTO.getGon_isim());
	        emailDetails.setGon_mail(emailDTO.getGon_mail());
	        emailDetails.setHesap(emailDTO.getHesap());
	        emailDetails.setHost(emailDTO.getHost());
	        emailDetails.setPort(emailDTO.getPort());
	        emailDetails.setSifre(TextSifreleme.encrypt(emailDTO.getSifre()));

	        emailDetails.setUser(user);
	        user.setEmailDetails(emailDetails);
	        userService.saveUser(user);

	        res.put("errorMessage", "");
	        res.put("message", "Kayıt yapıldı.");
	    } catch (Exception e) {
	        res.put("errorMessage", e.getMessage() != null ? e.getMessage() : "Beklenmeyen hata.");
	    }
	    return res;
	}

}