package com.hamit.obs.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "etiket_ayar")
@Data
public class Etiket_Ayarlari {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column()
	private int altbosluk;
	
	@Column()
	private int ustbosluk;
	
	@Column()
	private int sagbosluk;
	
	@Column()
	private int solbosluk;
	
	@Column()
	private int dikeyarabosluk;

	@Column()
	private int genislik;
	
	@Column()
	private int yataydikey;
	
	@Column()
	private int yukseklik;
	
	@OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
