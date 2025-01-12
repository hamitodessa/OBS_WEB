package com.hamit.obs.controller.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.forum.ForumService;
import com.hamit.obs.service.user.UserService;

@Controller
public class UserCopyController {

	@Autowired
    private UserService userService;
	
	@Autowired
	private ForumService forumService;
	
	@GetMapping("user/copyuser")
	public String copyuser() {
		return "user/copyuser";
	}

	@PostMapping("user/checkuser")
	@ResponseBody
	public Map<String, String> changePassword(@RequestParam String hesap) {
		Map<String, String> response = new HashMap<>();
		try {
			User user = userService.findUserByUsername(hesap);
			if (user != null) {
				response.put("errorMessage", "Bu Kullanci Mevcut");
			}else {
				response.put("errorMessage", "");
			}
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("user/savecopyuser")
	@ResponseBody
	public Map<String, String> savecopyuser(@RequestParam String hesap) {
		Map<String, String> response = new HashMap<>();
		try {
			User currentUser = userService.getCurrentUser();
			String newEmail = hesap;
			User newUser = userService.duplicateUser(currentUser, newEmail);
			forumService.mesajsayiSaveUser(newUser.getEmail());
			response.put("success", newUser.getEmail());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}