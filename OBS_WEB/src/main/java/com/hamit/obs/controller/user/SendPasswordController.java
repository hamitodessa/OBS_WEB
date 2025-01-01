package com.hamit.obs.controller.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.hamit.obs.service.user.UserService;

@Controller
public class SendPasswordController {

	
	@Autowired
	private UserService userService;

	@GetMapping("user/send_password")
	public String register() {
		return "user/send_password"; 
	}
	
	@PostMapping("user/send_password")
	@ResponseBody
	public ResponseEntity<Map<String, String>> sendPassword(@RequestParam String email) {
	    Map<String, String> response = new HashMap<>();
	    try {
	        boolean result = userService.sendPasswordByEmail(email);
	        if (result) {
	            response.put("message", "Şifreniz e-posta adresinize gönderildi.");
	            return ResponseEntity.ok(response);
	        } else {
	            response.put("error", "Bu e-posta adresine kayıtlı bir kullanıcı bulunamadı.");
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	        }
	    } catch (RuntimeException e) {
	        response.put("error", "Bir hata oluştu: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}	
}