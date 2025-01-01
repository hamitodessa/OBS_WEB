package com.hamit.obs.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Entity
@Table(name = "email_details")
@Data
public class Email_Details {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Email
	@NotBlank
	@Column(unique = true, nullable = false, length = 50)
	private String email;

	@Column(length = 50)
	private String hesap;
	
	@Column(length = 50)
	private String host;
	
	@Column(length = 20)
	private String port;
	
	@Column(length = 255)
	private String sifre;
	
	@Column(length = 50)
	private String gon_mail;
	
	@Column(length = 50)
	private String gon_isim;
	
	@Column
	private Boolean  bssl;
	
	@Column
	private Boolean  btsl;
	
	@OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
