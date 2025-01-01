package com.hamit.obs.dto.cari;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class tahayarDTO {

	private String adi;
	private String ad1 ;
	private String ad2 ;
	private String vdvn ; 
	private String mail ;
	private String diger  ;
	
	private byte[] imagelogo;
	private MultipartFile resimlogo;
	private MultipartFile resimgosterlogo ;
	private String base64Resimkase;
	
	private byte[] imagekase;
	private MultipartFile resimkase;
	private MultipartFile resimgosterkase ;
	private String base64Resimlogo;

	
	private String usr;
	private String errorMessage;
}
