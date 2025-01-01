package com.hamit.obs.controller.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.hamit.obs.custom.yardimci.TextSifreleme;
import com.hamit.obs.dto.email.EmailAyarlarDTO;
import com.hamit.obs.exception.ServiceException;
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
	public Model getemailSettings(Model model) {
		User user = userService.getCurrentUser();
		EmailAyarlarDTO emailDTO = new EmailAyarlarDTO();
		Email_Details email = emailService.findByEmail(user.getEmail().toString());
		if(email != null) {
			emailDTO.setId(email.getId());
			emailDTO.setHesap(email.getHesap());
			emailDTO.setHost(email.getHost());
			emailDTO.setPort(email.getPort());
			try {
				emailDTO.setSifre(TextSifreleme.decrypt(email.getSifre()));
			} catch (Exception e) {
			}
			emailDTO.setGon_mail(email.getGon_mail());
			emailDTO.setGon_isim(email.getGon_isim());
			emailDTO.setBssl(email.getBssl());
			emailDTO.setBtsl(email.getBtsl());
			model.addAttribute("user", emailDTO);
		}
		else {
			emailDTO.setHesap("");
			emailDTO.setHost("");
			emailDTO.setPort("");
			emailDTO.setSifre("");
			emailDTO.setGon_mail("");
			emailDTO.setGon_isim("");
			emailDTO.setBssl(false);
			emailDTO.setBtsl(false);
			model.addAttribute("user", emailDTO);
		}
		return model;
	}

	@PostMapping("user/emailsettings_save")
	@ResponseBody
	public Map<String, String> updateUser(@ModelAttribute("user") EmailAyarlarDTO emailDTO, MultipartFile image) {
		Map<String, String> response = new HashMap<>();
		try {
			Email_Details email_Details = new Email_Details();
			User user = userService.getCurrentUser();
			email_Details.setEmail(user.getEmail());
			email_Details.setBssl(emailDTO.isBssl());
			email_Details.setBtsl(emailDTO.isBtsl());
			email_Details.setGon_isim(emailDTO.getGon_isim());
			email_Details.setGon_mail(emailDTO.getGon_mail());
			email_Details.setHesap(emailDTO.getHesap());
			email_Details.setHost(emailDTO.getHost());
			email_Details.setPort(emailDTO.getPort());
			email_Details.setSifre(TextSifreleme.encrypt(emailDTO.getSifre()));
			if (emailDTO.getId() != null) {
			    Email_Details emailDetails = user.getEmail_Details();
			    if (emailDetails != null && emailDetails.getId().equals(emailDTO.getId())) {
			        user.setEmail_Details(null);
			        userService.saveUser(user);
			    }
			}
			email_Details.setUser(user);
			user.setEmail_Details(email_Details);
			userService.saveUser(user);
			response.put("errorMessage", "");
	    } catch (ServiceException e) {
	    	response.put("errorMessage", e.getMessage());
	    } catch (Exception e) {
	    	response.put("errorMessage", e.getMessage());
	    }
		return response;
	}
}