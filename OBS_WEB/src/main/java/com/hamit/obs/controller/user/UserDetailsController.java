package com.hamit.obs.controller.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.custom.yardimci.TextSifreleme;
import com.hamit.obs.dto.user.User_DetailsDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.RolEnum;
import com.hamit.obs.model.user.Role;
import com.hamit.obs.model.user.User;
import com.hamit.obs.model.user.User_Details;
import com.hamit.obs.service.adres.AdresService;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.kambiyo.KambiyoService;
import com.hamit.obs.service.kur.KurService;
import com.hamit.obs.service.user.UserDetailsService;
import com.hamit.obs.service.user.UserService;

@Controller
public class UserDetailsController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private CariService cariService ;
	@Autowired
    private KurService kurService;
	@Autowired
    private AdresService adresService;
	@Autowired
    private KambiyoService kambiyoService;

	@Autowired
	private UserDetailsService userDetailsService;

	@GetMapping("user/userdetails")
	public String user_detailss() {
		return "user/userdetails";
	}

	@PostMapping("user/detailoku")
	@ResponseBody
	public Map<String, Object> detailoku(@RequestBody Map<String, String> params) {
		Map<String, Object> response = new HashMap<>();
		try {
			String email = userService.getCurrentUser().getEmail();
			User user = userService.getCurrentUser();
			Set<Role> roles = user.getRoles();
			String rolName = roles.stream()
					.map(role -> role.getName().name())
					.findFirst()
					.orElse(""); 
			List<User_Details> userDetailsList = userDetailsService.findByUserModulAndEmail(params.get("modul"), email, rolName);
			userDetailsList.forEach(userDetails -> userDetails.setUser(null));
			List<User_DetailsDTO> userDetailsDTOList = userDetailsList.stream().map(userDetails -> {
				User_DetailsDTO dto = new User_DetailsDTO();
				BeanUtils.copyProperties(userDetails, dto);
				dto.setUser_pwd_server(TextSifreleme.decrypt(userDetails.getUser_pwd_server())); // Şifre çözme
				return dto;
			}).collect(Collectors.toList());
			response.put("success", true);
			response.put("roleName", rolName);
			response.put("data", userDetailsDTOList);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("role", "");
			response.put("data", Collections.emptyList());
			response.put("errorMessage", "Hata: " + e.getMessage());
		} catch (Exception e) {
			response.put("success", false);
			response.put("role", "");
			response.put("data", Collections.emptyList());
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("user/user_details_save")
	@ResponseBody
	public Map<String, Object> saveUserDetails(@RequestBody User_Details userDetails) {
		Map<String, Object> response = new HashMap<>();
		try {
			boolean drm = false;
			User userr = userService.getCurrentUser();
			Set<Role> roles = userr.getRoles();
			boolean isAdmin = roles.stream()
					.anyMatch(role -> role.getName() == RolEnum.ADMIN);
			if (!isAdmin) {
				List<User_Details> userDetailsList = userDetailsService.izinlimiKontrol(
						userDetails.getUser_modul(),
						userService.getCurrentUser().getEmail()
						);
				for (User_Details user_DetailsKontrol : userDetailsList) {
					if (user_DetailsKontrol.getUser_prog_kodu().equals(userDetails.getUser_prog_kodu())
							&& user_DetailsKontrol.getHangi_sql().equals(userDetails.getHangi_sql())
							&& user_DetailsKontrol.getUser_ip().equals(userDetails.getUser_ip())
							&& user_DetailsKontrol.getIzinlimi()) {
						drm = true;
						break; 
					}
				}
			} else {
				drm = true;
			}
			if (drm) {
				User user = userService.getCurrentUser();
				userDetails.setEmail(user.getEmail());
				if (userDetails.getCalisanmi()) {
					userDetailsService.updateUserDetailsCalisanmiNulle(userDetails.getUser_modul(), user.getEmail());
				}
				String sifre = userDetails.getUser_pwd_server();
				if (sifre == null) {
					User_Details existingDetails = userDetailsService.getUserDetailsById(userDetails.getId());
					sifre = existingDetails.getUser_pwd_server();
				} else {
					sifre = TextSifreleme.encrypt(userDetails.getUser_pwd_server());
				}
				userDetails.setUser_pwd_server(sifre);
				userDetails.setUser(user);
				userDetailsService.saveUserDetails(userDetails);
				response.put("errorMessage", "");
				if(userDetails.getUser_modul().equals("Cari Hesap"))
					cariService.initialize();
				else if(userDetails.getUser_modul().equals("Kur"))
					kurService.initialize();
				else if(userDetails.getUser_modul().equals("Adres"))
					adresService.initialize();
				else if(userDetails.getUser_modul().equals("Kambiyo"))
					kambiyoService.initialize();
			} else {
				response.put("errorMessage", "Bu işlemi yapmaya yetkiniz yok...Adminden Yetki Aliniz");
			}
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("user/delete_user_details")
	@ResponseBody
	public Map<String, Object> deleteUserDetails(@RequestBody Map<String, Long> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			Long id = request.get("id");
			User user = userService.getCurrentUser();
			User_Details userdetailsToRemove = user.getUserDetails().stream()
					.filter(details -> details.getId().equals(id))
					.findFirst()
					.orElseThrow(() -> new RuntimeException("User Details not found"));
			user.getUserDetails().remove(userdetailsToRemove);
			userService.saveUser(user);	
			response.put("errorMessage", ""); 
			if(userdetailsToRemove.getUser_modul().equals("Cari Hesap"))
				cariService.initialize();
			else if(userdetailsToRemove.getUser_modul().equals("Kur"))
				kurService.initialize();
			else if(userdetailsToRemove.getUser_modul().equals("Adres"))
				adresService.initialize();
			else if(userdetailsToRemove.getUser_modul().equals("Kambiyo"))
				kambiyoService.initialize();
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}