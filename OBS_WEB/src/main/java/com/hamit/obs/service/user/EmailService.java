package com.hamit.obs.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hamit.obs.model.user.Email_Details;
import com.hamit.obs.repository.user.IEmailRepository;

@Service
public class EmailService {

	@Autowired
	private IEmailRepository emailRepository;
	
	public  Email_Details findByEmail(String email) {
		return emailRepository.findByEmail(email);
	}
}