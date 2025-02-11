// Updated Entity Files for PostgreSQL Compatibility

package com.hamit.obs.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.Data;


@Entity
@Table(name = "user_details")
@Data
public class User_Details {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_details_seq")
	@SequenceGenerator(name = "user_details_seq", sequenceName = "user_details_sequence", allocationSize = 1)
	private Long id;

	@Email
	@Column(nullable = false, length = 50)
	private String email;

	@Column(nullable = false, length = 10)
	private String user_prog_kodu;
	
	@Column(nullable = false, length = 50)
	private String user_server;
	
	@Column(nullable = false, length = 50)
	private String user_pwd_server;
	
	@Column(nullable = false, length = 50)
	private String user_ip;
	
	@Column(nullable = false, length = 20)
	private String user_modul;
	
	@Column
	private Boolean izinlimi;
	
	@Column
	private Boolean calisanmi;
	
	@Column(nullable = false, length = 10)
	private String hangi_sql;
	
	@Column
	private Boolean log;
	
	@Column(nullable = true, length = 30)
	private String superviser;

	@ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
