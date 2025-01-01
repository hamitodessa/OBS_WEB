package com.hamit.obs.config;
import org.springframework.security.web.session.InvalidSessionStrategy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomInvalidSessionStrategy implements InvalidSessionStrategy {
	private final String redirectUrl;

	public CustomInvalidSessionStrategy(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	@Override
	public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(redirectUrl); 
	}
}