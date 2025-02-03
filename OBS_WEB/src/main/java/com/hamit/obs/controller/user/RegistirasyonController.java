package com.hamit.obs.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hamit.obs.model.user.RolEnum;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.forum.ForumService;
import com.hamit.obs.service.user.UserService;


@Controller
public class RegistirasyonController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private ForumService forumService;

	@GetMapping("user/register")
	public String register() {
		return "user/register";
	}

	@PostMapping("user/register")
	public String registerUser(@RequestParam String email,
			@RequestParam String password,
			@RequestParam String firstName,
			@RequestParam String lastName,
			@RequestParam MultipartFile image,
			@RequestParam String role,
			@RequestParam String adminemail,
			@RequestParam String calisandvzcinsi,
			RedirectAttributes redirectAttrs) {
		try {
			RolEnum userRole;
			userRole = RolEnum.valueOf(role.toUpperCase());
			User user = new User();
			user.setEmail(email);
			user.setPassword(password);
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setCalisandvzcins(calisandvzcinsi);

			if (userRole == RolEnum.USER) {
				user.setAdmin_hesap(adminemail);
			} else {
				user.setAdmin_hesap("");
			}
			if (!image.isEmpty()) {
				byte[] resimBytes = image.getBytes();
				user.setImage(resimBytes);
			} else {
				user.setImage(null);
			}
			userService.registerUser(user, userRole);
			forumService.mesajsayiSaveUser(user.getEmail());
			redirectAttrs.addFlashAttribute("success", "Kayıt başarılı! Lütfen giriş yapın.");
			return "redirect:/login";
		} catch (Exception e) {
			redirectAttrs.addFlashAttribute("error", e.getMessage());
			return "redirect:/user/register";
		}
	}
}