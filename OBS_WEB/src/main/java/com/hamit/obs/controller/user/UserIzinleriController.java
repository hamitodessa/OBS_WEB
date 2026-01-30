package com.hamit.obs.controller.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.custom.yardimci.TextSifreleme;
import com.hamit.obs.dto.user.UserRowDTO;
import com.hamit.obs.dto.user.User_DetailsDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.RolEnum;
import com.hamit.obs.model.user.Role;
import com.hamit.obs.model.user.User;
import com.hamit.obs.model.user.User_Details;
import com.hamit.obs.service.adres.AdresService;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.fatura.FaturaService;
import com.hamit.obs.service.forum.ForumService;
import com.hamit.obs.service.kambiyo.KambiyoService;
import com.hamit.obs.service.kereste.KeresteService;
import com.hamit.obs.service.kur.KurService;
import com.hamit.obs.service.user.UserDetailsService;
import com.hamit.obs.service.user.UserListService;
import com.hamit.obs.service.user.UserService;

@Controller
public class UserIzinleriController {


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
	private FaturaService faturaService;
	@Autowired
	private KeresteService keresteService;
	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private UserListService userListService;
	@Autowired
	private ForumService forumService;

	@GetMapping("user/userizinler")
	public String user_detailss() {
	    User user = userService.getCurrentUser();
	    Set<Role> roles = user.getRoles();
	    boolean isAdmin = roles.stream()
                      .anyMatch(role -> role.getName() == RolEnum.ADMIN);
	    if (isAdmin) {
	        return "user/userizinleri";
	    } else {
	        return "/wellcome";
	    }
	}

	@GetMapping("/user/adminbaglihesapoku")
	@ResponseBody
	public Map<String, Object> adminbaglihesapoku() {
		Map<String, Object> response = new HashMap<>();
		try {
			//String email = userService.getCurrentUser().getEmail();
			String email = SecurityContextHolder.getContext().getAuthentication().getName();
			List<User> userList = userService.findByUserAdminHesap(email);
			if (userList.isEmpty()) {
				response.put("success", false);
				response.put("data", Collections.emptyList());
				response.put("errorMessage", "Bağlı hesap bulunamadı.");
				return response;
			}
			List<String> adminHesapList = userList.stream()
					.map(User::getEmail)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			response.put("success", true);
			response.put("data", adminHesapList );
			response.put("errorMessage", "");
		} catch (Exception e) {
			response.put("success", false);
			response.put("data", Collections.emptyList());
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("user/izinlerioku")
	@ResponseBody
	public Map<String, Object> sorgula(@RequestBody Map<String, String> params) {
		Map<String, Object> response = new HashMap<>();
		try {
			//String email = userService.getCurrentUser().getEmail();
			String email = SecurityContextHolder.getContext().getAuthentication().getName();
			List<User_Details> userDetailsList ;
			if(params.get("hesap").equals("ADMIN"))
				userDetailsList = userDetailsService.findByUserModulAndEmail(params.get("modul"), email,"ADMIN");
			else
				userDetailsList = userDetailsService.findByUserModulAndEmail(params.get("modul"), params.get("hesap"),"ADMIN");
			userDetailsList.forEach(userDetails -> userDetails.setUser(null));
			List<User_DetailsDTO> userDetailsDTOList = userDetailsList.stream().map(userDetails -> {
				User_DetailsDTO dto = new User_DetailsDTO();
				BeanUtils.copyProperties(userDetails, dto);
				dto.setUser_pwd_server(TextSifreleme.decrypt(userDetails.getUser_pwd_server()));
				return dto;
			}).collect(Collectors.toList());
			response.put("success", true);
			response.put("data", userDetailsDTOList );
			response.put("errorMessage", "");
		} catch (Exception e) {
			response.put("success", false);
			response.put("data", Collections.emptyList());
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("user/userizinsave")
	@ResponseBody
	public Map<String, Object> userizinsave(@RequestBody User_Details userDetails) {
		Map<String, Object> response = new HashMap<>();
		try {
			User user = userService.findUserByUsername(userDetails.getEmail());
			if(userDetails.getCalisanmi())
				userDetailsService.updateUserDetailsCalisanmiNulle(userDetails.getUser_modul(), userDetails.getEmail());
			String sifre = userDetails.getUser_pwd_server();
			if (sifre == null) {
				User_Details existingDetails = userDetailsService.getUserDetailsById(userDetails.getId());
				sifre = existingDetails.getUser_pwd_server(); 
			}
			else
				sifre = TextSifreleme.encrypt(userDetails.getUser_pwd_server());
			userDetails.setUser_pwd_server(sifre);
			userDetails.setUser(user);
			userDetailsService.saveUserDetails(userDetails);
			response.put("errorMessage", ""); 
			modulTipi modultip = modulTipi.fromDbValue(userDetails.getUser_modul());
			switch (modultip) {
			case CARI_HESAP -> cariService.initialize();
			case KUR        -> kurService.initialize();
			case ADRES      -> adresService.initialize();
			case KAMBIYO    -> kambiyoService.initialize();
			case FATURA     -> faturaService.initialize();
			case KERESTE    -> keresteService.initialize();
			default -> throw new IllegalArgumentException("Unexpected value: " + modultip);
		}
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("user/izinlerdelete")
	@ResponseBody
	public Map<String, Object> deleteUserDetails(@RequestBody Map<String, Object> params) {
		Map<String, Object> response = new HashMap<>();
		try {
			Long id = Long.valueOf(params.get("id").toString());
			User user = userService.findUserByUsername(params.get("hesap").toString());
			User_Details userdetailsToRemove = user.getUserDetails().stream()
					.filter(details -> details.getId().equals(id))
					.findFirst()
					.orElseThrow(() -> new RuntimeException("User Details not found"));
			user.getUserDetails().remove(userdetailsToRemove);
			userService.saveUser(user);	
			response.put("errorMessage", ""); 
			modulTipi modultip = modulTipi.fromDbValue(userdetailsToRemove.getUser_modul());
			switch (modultip) {
			case CARI_HESAP -> cariService.initialize();
			case KUR        -> kurService.initialize();
			case ADRES      -> adresService.initialize();
			case KAMBIYO    -> kambiyoService.initialize();
			case FATURA     -> faturaService.initialize();
			case KERESTE    -> keresteService.initialize();
			default -> throw new IllegalArgumentException("Unexpected value: " + modultip);
		}
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@GetMapping("user/userlist")
	public String user_list() {
	        return "user/userlist";
    
	}
	
	@GetMapping("user/list")
	public ResponseEntity<List<UserRowDTO>> list() {
		
		return ResponseEntity.ok(userListService.listUsers());
	}

	@PostMapping("user/userSil")
	public ResponseEntity<?> userSil(@RequestParam Long id) {
		try {
			
			User user = userService.findById(id);
			forumService.mesajsayiDeleteUser(user.getEmail());
			userListService.deleteUser(id);
			UserSessionManager.removeUserSessionsByUsername(user.getEmail());
			return ResponseEntity.ok(Map.of("errorMessage", ""));
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errorMessage", "Beklenmeyen bir hata oluştu: " + e.getMessage()));
		}
	}
}