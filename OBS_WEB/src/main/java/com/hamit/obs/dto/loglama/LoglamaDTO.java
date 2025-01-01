package com.hamit.obs.dto.loglama;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoglamaDTO {
	
	private String modul;
	private String mesaj;
	private String evrak;
	private String user;
	public void setmESAJ(String mESAJ) {
		this.mesaj = mESAJ.replace("\n"," "); 
	}
}