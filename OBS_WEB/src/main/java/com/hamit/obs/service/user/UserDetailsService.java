package com.hamit.obs.service.user;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import com.hamit.obs.model.user.User_Details;
import com.hamit.obs.repository.user.IUserDetailsRepository;

@Service
public class UserDetailsService {

	@Autowired
    private IUserDetailsRepository userDetailsRepository;

	public List<User_Details> findByUserModulAndEmail(String userModul, String email,String role) {
		if(role.equals("ADMIN"))
			 return userDetailsRepository.findByUserModulAndEmailAdmin(userModul, email);
		else
			 return userDetailsRepository.findByUserModulAndEmail(userModul, email);
    }
	public void updateUserDetailsCalisanmiNulle(String userModul, String email) {
        userDetailsRepository.updateUserDetailsCalisanmiNulle(userModul, email,false);
	}
	
	public List<User_Details> user_Details_Modul(String userModul, String email){
		return userDetailsRepository.user_Details_Modul(userModul, email);
	}
	public String findHangiSQLByUserId(@Param("userModul") String userModul, @Param("email") String email) {
		return userDetailsRepository.findHangiSQLByUserId(userModul, email);
	}

    public User_Details saveUserDetails(User_Details userDetails) {
        return userDetailsRepository.save(userDetails);
    }

    public void deleteUserDetails(Long id) {
        userDetailsRepository.deleteById(id);
    }
    
    public User_Details getUserDetailsById(Long id) {
    	return userDetailsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User details not found for id: " + id));
    }
    
    public List<User_Details> izinlimiKontrol(String userModul, String email){
    		return userDetailsRepository.izinlimiKontrol(userModul, email);
    }
}