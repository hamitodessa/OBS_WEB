package com.hamit.obs.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionManager;
import com.hamit.obs.service.adres.AdresService;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.fatura.FaturaService;
import com.hamit.obs.service.kambiyo.KambiyoService;
import com.hamit.obs.service.kereste.KeresteService;
import com.hamit.obs.service.kur.KurService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor

public class LoginSuccessHandler implements AuthenticationSuccessHandler {
	private final Logger log = LoggerFactory.getLogger(LoginSuccessHandler.class);
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
	private ConnectionManager connectionManager;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		try {
			String username = authentication.getName();
			String ip       = request.getRemoteAddr();
			String ua       = request.getHeader("User-Agent");
			String uri      = request.getRequestURI();
			log.info("LOGIN OK   - user={} ip={} ua={} uri={}", username, ip, ua, uri);
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserSessionManager.removeUserSessionsByUsername(useremail);
			connectionManager.loadAllConnections(useremail);// Kullanıcının tüm bağlantılarını yükle
			
			cariService.initialize();
			kurService.initialize();
			adresService.initialize();
			kambiyoService.initialize();
			faturaService.initialize();
			keresteService.initialize();
			response.sendRedirect("/index");
		} catch (Exception e) {
			log.error("LOGIN   - user={} ",  SecurityContextHolder.getContext().getAuthentication().getName(), e);
			response.sendRedirect("/index?trigger=userdetails");
		} 
	}
}