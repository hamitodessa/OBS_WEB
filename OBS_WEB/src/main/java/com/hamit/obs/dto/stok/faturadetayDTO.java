package com.hamit.obs.dto.stok;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class faturadetayDTO {
	private String ukodu;
	private String barkod;
	private String depo;
	private Double fiat;
	private Double iskonto;
	private Double miktar;
	private Double kdv;
	private Double tutar;
	private String izahat;
	
}
