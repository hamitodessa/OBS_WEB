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
public class SecurityConfig  {

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
				//.defaultSuccessUrl("/index", true)
				.permitAll()
				)
		.logout(logout -> logout
				.logoutUrl("/logout") // Çıkış işlemi
				.logoutSuccessUrl("/login") // Çıkış sonrası yönlendirme
				.invalidateHttpSession(true) // Oturum sonlandır
				.deleteCookies("JSESSIONID") // Çerezleri sil
				.permitAll()
				)
		.sessionManagement(session -> session
				.invalidSessionUrl("/session-expired") // Oturum süresi dolmuşsa yönlendirme
				)
		.userDetailsService(customUserDetails); // Kullanıcı detayları servisi
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}