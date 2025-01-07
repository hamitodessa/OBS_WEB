package com.hamit.obs.service.user;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hamit.obs.model.user.User;
import com.hamit.obs.model.user.User_Details;
import com.hamit.obs.repository.user.RoleRepository;
import com.hamit.obs.repository.user.UserRepository;
import com.hamit.obs.custom.yardimci.PasswordGenerator;
import com.hamit.obs.model.user.Email_Details;
import com.hamit.obs.model.user.Etiket_Ayarlari;
import com.hamit.obs.model.user.RolEnum;
import com.hamit.obs.model.user.Role;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


@Service
public class UserService {

	
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public void registerUser(User user, RolEnum role) throws Exception {
	    if (userRepository.findByEmail(user.getEmail()) != null) {
	        throw new Exception("Bu email adresiyle kayıtlı bir kullanıcı zaten var.");
	    }
	    user.setPassword(passwordEncoder.encode(user.getPassword()));
	    Role userRole = roleRepository.findByName(role);
	    if (userRole == null) {
	        userRole = new Role();
	        userRole.setName(role);
	        roleRepository.save(userRole);
	    }
	    Set<Role> roles = new HashSet<>();
	    roles.add(userRole);
	    user.setRoles(roles);
	    userRepository.save(user);
	}
	
	public User getCurrentUser() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			String username = ((UserDetails) principal).getUsername();
			return findUserByUsername(username);
		} else {
			return null; 
		}
	}	 
	
	public List<User> findByUserAdminHesap(String email){
		return userRepository.findByUserAdminHesap(email);
		
	}
	public User findUserByUsername(String username) {
		return userRepository.findByEmail(username);
	}

	public void saveUser(User user) {
		userRepository.save(user);
	}
	
	public void deleteUser(User user) {
		userRepository.delete(user);
	}
	
	public boolean sendPasswordByEmail(String email) {
		User user = findUserByUsername(email);
		if (user != null) {
			try {
				String randomPassword =  PasswordGenerator.generateRandomPassword();
				user.setPassword(passwordEncoder.encode(randomPassword));
				saveUser(user);
				MimeBodyPart messagePart = null ;
				Properties props = System.getProperties();
				props.put("mail.smtp.starttls.enable", false);
				props.put("mail.smtp.host", "mail.okumus.gen.tr");
				props.put("mail.smtp.user", "info@okumus.gen.tr");
				props.put("mail.smtp.password", "oOk271972");
				props.put("mail.smtp.port", 587);
				props.put("mail.smtp.auth", "true");
				props.put("mail.smtp.ssl.protocols", "TLSv1.2");
				Session session = Session.getDefaultInstance(props,new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication("info@okumus.gen.tr","oOk271972");
					}
				});
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress("info@okumus.gen.tr","Obs Sistem"));
				message.setRecipient(RecipientType.TO, new InternetAddress(user.getEmail().trim()));
				messagePart = new MimeBodyPart();
				messagePart.setText("Hesabınızın Gecici şifresi: " + randomPassword,"UTF-8");
				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messagePart);
				message.setSubject("Hesap Şifreniz", "UTF-8");
				message.setContent(multipart);
				message.setSentDate(new Date());
				Transport.send(message);
				message = null;
				session = null;
				return true;
			} catch (Exception e) {
				throw new RuntimeException("Bir hata oluştu. E-posta gönderilemedi.");
			}
		}
		else {
			return false;
		}
	}
	
	@Transactional
	public User duplicateUser(User currentUser, String newEmail) {
		// Yeni kullanıcı oluştur
		User newUser = new User();
		newUser.setEmail(newEmail);
		newUser.setFirstName(currentUser.getFirstName());
		newUser.setLastName(currentUser.getLastName());
		newUser.setPassword(currentUser.getPassword()); // Şifreyi olduğu gibi mi kullanıyorsun?
		newUser.setImage(null);
		newUser.setAdmin_hesap(currentUser.getAdmin_hesap());
		newUser.setRoles(new HashSet<>(currentUser.getRoles()));

		Email_Details emailDetail = currentUser.getEmailDetails();
			Email_Details newEmailDetail = new Email_Details();
			newEmailDetail.setBssl(emailDetail.getBssl());
			newEmailDetail.setBtsl(emailDetail.getBtsl());
			newEmailDetail.setEmail(newEmail);
			newEmailDetail.setGon_isim(emailDetail.getGon_isim());
			newEmailDetail.setGon_mail(emailDetail.getGon_mail());
			newEmailDetail.setHesap(emailDetail.getHesap());
			newEmailDetail.setHost(emailDetail.getHost());
			newEmailDetail.setPort(emailDetail.getPort());
			newEmailDetail.setSifre(emailDetail.getSifre());
			newEmailDetail.setUser(newUser);
		    newUser.setEmailDetails(newEmailDetail);
		

		for (User_Details userDetail : currentUser.getUserDetails()) {
			User_Details newUserDetail = new User_Details();
			newUserDetail.setCalisanmi(userDetail.getCalisanmi());
			newUserDetail.setEmail(newEmail);
			newUserDetail.setHangi_sql(userDetail.getHangi_sql());
			newUserDetail.setIzinlimi(userDetail.getIzinlimi());
			newUserDetail.setLog(userDetail.getLog());
			newUserDetail.setUser_ip(userDetail.getUser_ip());
			newUserDetail.setUser_modul(userDetail.getUser_modul());
			newUserDetail.setUser_modul(userDetail.getUser_modul());
			newUserDetail.setUser_prog_kodu(userDetail.getUser_prog_kodu());
			newUserDetail.setUser_pwd_server(userDetail.getUser_pwd_server());
			newUserDetail.setUser_server(userDetail.getUser_server());
			newUserDetail.setUser(newUser);
			newUser.getUserDetails().add(newUserDetail);
		}

		Etiket_Ayarlari etiketAyari = currentUser.getEtiketAyarlari();
			Etiket_Ayarlari newEtiketAyari = new Etiket_Ayarlari();
			newEtiketAyari.setAltbosluk(etiketAyari.getAltbosluk());
			newEtiketAyari.setDikeyarabosluk(etiketAyari.getDikeyarabosluk());
			newEtiketAyari.setGenislik(etiketAyari.getGenislik());
			newEtiketAyari.setUstbosluk(etiketAyari.getUstbosluk());
			newEtiketAyari.setSagbosluk(etiketAyari.getSagbosluk());
			newEtiketAyari.setSolbosluk(etiketAyari.getSolbosluk());
			newEtiketAyari.setYataydikey(etiketAyari.getYataydikey());
			newEtiketAyari.setYukseklik(etiketAyari.getYukseklik());
			newEtiketAyari.setUser(newUser);
			newUser.setEtiketAyarlari(newEtiketAyari);
		
		return userRepository.save(newUser);
	}
}