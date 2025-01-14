package com.hamit.obs.custom.degiskenler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class UygulamaSabitleri {

	public static final String CariRaporYeri = "static/raporlar/CariRaporlar";
	public static final String KambiyoRaporYeri = "static/raporlar/KambiyoRaporlar";
	public static final String AdresRaporYeri = "static/raporlar/AdresRaporlar";

	public static String forumConnString = "";
	public static String FORUMUSER_STRING = "";
	public static String FORUMPWD_STRING = "";

	@Value("${spring.datasource.username}")
	private String forumUser;

	@Value("${spring.datasource.password}")
	private String forumPassword;

	@Value("${spring.datasource.url}")
	private String forumUrl;

	@PostConstruct
	private void init() {
		FORUMUSER_STRING = forumUser;
		FORUMPWD_STRING = forumPassword;
		forumConnString = forumUrl ;
	}
}