package com.hamit.obs.config;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        String username = request.getParameter("username");
        String ip       = request.getRemoteAddr();
        String ua       = request.getHeader("User-Agent");
        String uri      = request.getRequestURI();
        log.warn("LOGIN FAIL - user={} ip={} ua={} uri={} reason={}",
                 username, ip, ua, uri, exception.getMessage());
        response.sendRedirect("/login?error=true");
    }
}