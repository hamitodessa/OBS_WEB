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

import com.hamit.obs.dto.user.UserUpdateDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.user.UserService;

@Controller
public class UserChangeNameController {

	@Autowired
	private UserService userService;

	@GetMapping("user/user_update")
	public Model showUpdateForm(Model model) {
		User user = userService.getCurrentUser();
		UserUpdateDTO userDTO = new UserUpdateDTO();
		userDTO.setFirstName(user.getFirstName());
		userDTO.setLastName(user.getLastName());
		model.addAttribute("user", userDTO);
		return model;
	}

	@PostMapping("user/user_update")
	@ResponseBody
	public Map<String, String> updateUser(@ModelAttribute("user") UserUpdateDTO userDTO, MultipartFile image) {
		Map<String, String> response = new HashMap<>();
		try {
			User user = userService.getCurrentUser();
			user.setFirstName(userDTO.getFirstName());
			user.setLastName(userDTO.getLastName());
			if ( !image.isEmpty())
			{
				byte[] resimBytes = image.getBytes();
				user.setImage(resimBytes);
			}
			userService.saveUser(user);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}