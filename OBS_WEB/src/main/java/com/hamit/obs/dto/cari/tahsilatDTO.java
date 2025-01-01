package com.hamit.obs.dto.cari;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class tahsilatDTO {

	private String fisNo;
    
    private String tahTarih ;
    private String tcheskod ;
	private String adresheskod;
	
	private int tah_ted;
	private int tur;
	
	private double tutar;
	
	
	private String divposbilgi;
	private String dvz_cins;
	private String posBanka;

	
	private String user;
	private String errorMessage;
	
	private boolean fisnoyazdir;
	private String borc_alacak;
	
}
