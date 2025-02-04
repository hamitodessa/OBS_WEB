package com.hamit.obs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.hamit.obs.service.user.CustomUserDetails;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final CustomUserDetails customUserDetails;
	public SecurityConfig(CustomUserDetails customUserDetails) {
		this.customUserDetails = customUserDetails;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, LoginSuccessHandler successHandler) throws Exception {
		http.csrf(csrf -> csrf.disable())
		.authorizeHttpRequests(request -> request
				.requestMatchers(
						"/css/**", "/images/**","/style/**", "/user/send_password", "/user/register"
						).permitAll()
				.anyRequest().authenticated() 
				)
		.formLogin(form -> form
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.successHandler(successHandler)
				.failureUrl("/login?error=true")
				.permitAll()
				)
		.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
				.addLogoutHandler((request, response, authentication) -> {
		            if (authentication != null) {
		                String username = authentication.getName();
		                UserSessionManager.removeUserSessionsByUsername(username);
		            }
		        })
				.permitAll()
				)
		.sessionManagement(session -> session
				.invalidSessionUrl("/session-expired")
				)
		.userDetailsService(customUserDetails);
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}