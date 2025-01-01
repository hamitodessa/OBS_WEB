package com.hamit.obs.dto.cari;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class hesapplaniDTO {

	private String kodu;
	private String adi;
	private String karton;
	private String hcins;
	private String yetkili ;
	private String ad1 ;
	private String ad2 ;
	private String semt;
	private String seh  ; 
	private String vd ; 
	private String vn ; 
	private String t1 ;
	private String t2 ;
	private String t3 ;
	private String fx ;
	private String o1 ;
	private String o2 ;
	private String o3 ; 
	private String web ;
	private String mail ;
	private String kim  ;
	private String acik ;
	private boolean sms; 
	private byte[] image;
	private MultipartFile resim;
	private MultipartFile resimGoster ;
	
	private String base64Resim;
	private String usr;
	private String errorMessage;
}
