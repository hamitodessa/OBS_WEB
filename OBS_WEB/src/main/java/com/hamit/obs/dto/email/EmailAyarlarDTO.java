package com.hamit.obs.dto.email;

import lombok.Data;

@Data
public class EmailAyarlarDTO {

	private Long id;
	private String email;
	private String hesap;
	private String host;
	private String port;
	private String sifre;
	private String gon_mail;
	private String gon_isim;
	private boolean bssl;
	private boolean btsl;
}