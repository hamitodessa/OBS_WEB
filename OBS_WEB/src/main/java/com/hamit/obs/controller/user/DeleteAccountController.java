package com.hamit.obs.controller.user;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.user.UserService;

@Controller
public class DeleteAccountController {

	@Autowired
	private UserService userService;
	
	@GetMapping("user/deleteaccount")
	public String user_detailss() {
		return "user/deleteaccount";
	}

	@GetMapping("user/accountdelete")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> raporSil() {
		try {
			User user = userService.getCurrentUser();
			userService.deleteUser(user);
			return ResponseEntity.ok(Map.of("errorMessage", ""));
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errorMessage", "Beklenmeyen bir hata oluştu: " + e.getMessage()));
		}
	}
}
