package com.hamit.obs.dto.adres;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ETIKET_ISIM {
	private String adi ;
	private String adres1;
	private String adres2;

	private String semt;
	private String sehir;
	private String telefon;

	public ETIKET_ISIM(String adi, String adres1, String adres2, String semt, String sehir,String telefon) {
		this.adi = adi;
		this.adres1 = adres1;
		this.adres2 = adres2;
		this.semt = semt;
		this.sehir = sehir;
		this.telefon = telefon;
	}
}