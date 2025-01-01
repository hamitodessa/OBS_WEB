package com.hamit.obs.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hamit.obs.model.user.Email_Details;

@Repository
public interface IEmailRepository extends JpaRepository<Email_Details, Long >{
	
	 Email_Details findByEmail(String email);
}