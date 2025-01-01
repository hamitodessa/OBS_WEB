package com.hamit.obs.model.user;


import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Entity
@Table(name = "giden_rapor")
@Data
public class Gonderilmis_Mailler {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Date tarih;
	
	@Email
	@Column(nullable = false, length = 50)
	private String user_email;
	
	@Column(length = 50)
	private String konu;
	
	@Column(length = 50)
	private String rapor;
	
	@Email
	@Column(length = 50)
	private String alici;
	
	@Column(length = 100)
	private String aciklama;
	
	@Email
	@Column(length = 50)
	private String gonderen;
	
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
