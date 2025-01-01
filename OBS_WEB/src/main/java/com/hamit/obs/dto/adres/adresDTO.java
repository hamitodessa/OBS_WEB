package com.hamit.obs.dto.adres;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class adresDTO {

	private String kodu;
	private String unvan;
	private String ad1 ;
	private String semt;
	private String ad2 ;
	private String seh  ; 
	private String pkodu  ; 
	private boolean smsgon; 
	private boolean mailgon; 
	private String vd ; 
	private String vn ; 
	private String t1 ;
	private String t2 ;
	private String t3 ;
	private String fx ;
	private String o1 ;
	private String o2 ;
	private String web ;
	private String ozel ; 
	private String mail ;
	private String acik ;
	private String not1 ;
	private String not2 ;
	private String not3 ;
	private String yetkili  ;
	
	private int id;
	private byte[] image;
	private MultipartFile resim;
	private MultipartFile resimGoster ;
	
	private String base64Resim;
	private String usr;
	private String errorMessage;

}
