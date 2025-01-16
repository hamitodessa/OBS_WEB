package com.hamit.obs.dto.stok;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class urunDTO {

	private String kodu;
	private String adi;
	private String birim;
	private int kusurat;
	private String sinif ;
	private String anagrup;
	private String altgrup;
	private String aciklama1;
	private String aciklama2;
	private String ozelkod1;
	private String ozelkod2;
	private double kdv;
	private String barkod;
	private String mensei;
	private double agirlik;
	private String depo;
	private double fiat1;
	private double fiat2;
	private double fiat3;
	private String recete;
	
	private byte[] image;
	private MultipartFile resim;
	private MultipartFile resimGoster ;
	
	private String base64Resim;
	private String usr;
	private String errorMessage;
}
