package com.hamit.obs.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.hamit.obs.service.adres.AdresService;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.kambiyo.KambiyoService;
import com.hamit.obs.service.kur.KurService;

import java.io.IOException;

@Component
@AllArgsConstructor

public class LoginSuccessHandler implements AuthenticationSuccessHandler {
	
	@Autowired
	private CariService cariService ;
	
	@Autowired
	private KurService kurService;
	
	@Autowired
	private AdresService adresService;
	
	@Autowired
	private KambiyoService kambiyoService;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserSessionManager.removeUserSessionsByUsername(useremail);
			cariService.initialize();
			kurService.initialize();
			adresService.initialize();
			kambiyoService.initialize();
			response.sendRedirect("/index");
		} catch (Exception e) {
			response.sendRedirect("/index?trigger=userdetails");
		} 
	}
}