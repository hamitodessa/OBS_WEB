package com.hamit.obs.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.user.UserService;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PasswordController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/user/user_pwd_change")
    public String showChangePasswordForm() {
        return "user/user_pwd_change"; 
    }

    @PostMapping("user/user_pwd_change")
    @ResponseBody
    public Map<String, String> changePassword(@RequestParam String oldPassword,
    		@RequestParam String newPassword) {
    	Map<String, String> response = new HashMap<>();
    	try {
    		User user = userService.getCurrentUser();

    		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
    			response.put("errorMessage", "Eski şifre yanlış!");
    			return response;
    		}
    		user.setPassword(passwordEncoder.encode(newPassword));
    		userService.saveUser(user);
    		response.put("errorMessage", "");
    	} catch (ServiceException e) {
    		response.put("errorMessage", "Hata: " + e.getMessage());
    	} catch (Exception e) {
    		response.put("errorMessage", "Hata: " + e.getMessage());
    	}
    	return response;
    }
}