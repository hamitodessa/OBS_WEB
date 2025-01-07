// Updated Entity Files for PostgreSQL Compatibility

package com.hamit.obs.model.user;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

import jakarta.validation.constraints.Email;

@Entity
@Table(name = "giden_rapor")
@Data
public class Gonderilmis_Mailler {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "giden_rapor_seq")
	@SequenceGenerator(name = "giden_rapor_seq", sequenceName = "giden_rapor_sequence", allocationSize = 1)
	private Long id;

	@Column
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
