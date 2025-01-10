package com.hamit.obs.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
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
	private final CariService cariService ;
	private final KurService kurService;
	private final AdresService adresService;
	private final KambiyoService kambiyoService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		try {
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