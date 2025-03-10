package com.hamit.obs.controller.kereste.raporlar;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.dto.kereste.kergrupraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.kereste.KeresteService;

@Controller
public class KeresteGrupRaporController {

	@Autowired
	private RaporOlustur raporOlustur;


	@Autowired
	private KeresteService keresteService;

	@GetMapping("/kereste/grprapor")
	public String grprapor() {
		return "kereste/gruprapor";
	}

	@PostMapping("kereste/grpdoldur")
	@ResponseBody
	public Map<String, Object> grpdoldur(@RequestBody kergrupraporDTO kergrupraporDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kerConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			String turuString[] =  grup_cevir(kergrupraporDTO.getAnagrp(),kergrupraporDTO.getAltgrp(),kergrupraporDTO.getOzkod(),kergrupraporDTO.getDepo());

			kergrupraporDTO.setAnagrp(turuString[0]);
			kergrupraporDTO.setAltgrp(turuString[1]);
			kergrupraporDTO.setOzkod(turuString[2]);
			kergrupraporDTO.setDepo(turuString[3]);

			String[] baslikbakStrings = {"",""};
			String[] deg_cevirString = {"","","","","",""};
			String ozelgrp[][] = new String[7][2];
			String hANGI = "" ;

			if (kergrupraporDTO.getGruplama().equals("Urun Kodu"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"KERESTE\".\"Kodu\""; 
						ozelgrp[0][1] = "Kodu"; 
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Kodu")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor("Kodu",baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0]," Kodu",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo()," Kodu" ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Kodu, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",1);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Sinif"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String klmString = "" ,grpString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = " SUBSTRING(\"KERESTE\".\"Kodu\",1, 2) "; 
						ozelgrp[0][1] = " Sinif "; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						grpString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) As Sinif ";
						grpString = " SUBSTRING(KERESTE.Kodu,1, 2) ";
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Sinif")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(klmString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0]," Sinif",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Sinif, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",1);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Sinif I Hane"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String klmString = "" ,grpString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = " SUBSTRING(\"KERESTE\".\"Kodu\",1, 1) "; 
						ozelgrp[0][1] = " Sinif I- "; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 1) AS Sinif ";
						grpString = " SUBSTRING(KERESTE.Kodu,1, 1) AS Sinif ";
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 1) As Sinif ";
						grpString = " SUBSTRING(KERESTE.Kodu,1, 1) ";
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Sinif")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(klmString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0]," Sinif",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Sinif, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",1);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Sinif II Hane"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String klmString = "" ,grpString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = " SUBSTRING(\"KERESTE\".\"Kodu\",2, 1) "; 
						ozelgrp[0][1] = " Sinif -I "; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,2, 1) AS Sinif ";
						grpString = " SUBSTRING(KERESTE.Kodu,2, 1) AS Sinif ";
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,2, 1) As Sinif ";
						grpString = " SUBSTRING(KERESTE.Kodu,2, 1) ";
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Sinif")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(klmString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0]," Sinif",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Sinif, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",1);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Kalinlik"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String klmString = "" ,grpString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = " SUBSTRING(\"KERESTE\".\"Kodu\",4, 3) "; 
						ozelgrp[0][1] = " Kalinlik "; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,4, 3) AS Kalinlik ";
						grpString = " SUBSTRING(KERESTE.Kodu,4, 3) AS Kalinlik ";
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,4, 3) As Kalinlik ";
						grpString = " SUBSTRING(KERESTE.Kodu,4, 3) ";
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Kalinlik")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(klmString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0]," Kalinlik",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Kalinlik, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",1);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Genislik"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String klmString = "" ,grpString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = " SUBSTRING(\"KERESTE\".\"Kodu\",13, 4) "; 
						ozelgrp[0][1] = " Genislik "; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,13, 4) AS Genislik ";
						grpString = " SUBSTRING(KERESTE.Kodu,13, 4) AS Genislik ";
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,13, 4) As Genislik ";
						grpString = " SUBSTRING(KERESTE.Kodu,13, 4) ";
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Genislik")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(klmString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0]," Genislik",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Genislik, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",1);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Boy"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String klmString = "" ,grpString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = " SUBSTRING(\"KERESTE\".\"Kodu\",8, 4) "; 
						ozelgrp[0][1] = " Boy "; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,8, 4) AS Boy ";
						grpString = " SUBSTRING(KERESTE.Kodu,8, 4) AS Boy ";
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,8, 4) As Boy ";
						grpString = " SUBSTRING(KERESTE.Kodu,13, 4) ";
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Boy")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(klmString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0]," Boy",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Boy, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",1);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Sinif-Kal"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String klmString = "" ,grpString="" ,mlkString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",1, 2)"; 
						ozelgrp[0][1] = "Sinif"; 
						ozelgrp[1][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",4, 3)"; 
						ozelgrp[1][1] = "Kal "; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal " ;
						grpString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ,SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal ";
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal  " ;
						grpString = " SUBSTRING(KERESTE.Kodu,1, 2) ,SUBSTRING(KERESTE.Kodu, 4, 3)  ";
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Sinif,Kal")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(klmString + " , " + mlkString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0],"Sinif , Kal",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Sinif,Kal, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",2);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Sinif-Gen"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String klmString = "" ,grpString="" ,mlkString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",1, 2)"; 
						ozelgrp[0][1] = "Sinif"; 
						ozelgrp[1][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",13, 4)"; 
						ozelgrp[1][1] = "Gen "; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 13, 4) AS Gen " ;
						grpString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ,SUBSTRING(KERESTE.Kodu, 13, 4) AS Gen ";
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 13, 4) AS Gen  " ;
						grpString = " SUBSTRING(KERESTE.Kodu,1, 2) ,SUBSTRING(KERESTE.Kodu, 13, 4)  ";
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Sinif,Gen")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(klmString + " , " + mlkString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0],"Sinif , Gen" ,hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Sinif,Gen, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",2);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Sinif-Boy"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String klmString = "" ,grpString="" ,mlkString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",1, 2)"; 
						ozelgrp[0][1] = "Sinif"; 
						ozelgrp[1][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",8, 4)"; 
						ozelgrp[1][1] = "Boy "; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 8, 4) AS Boy " ;
						grpString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ,SUBSTRING(KERESTE.Kodu, 8, 4) AS Boy ";
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 8, 4) AS Gen  " ;
						grpString = " SUBSTRING(KERESTE.Kodu,1, 2) ,SUBSTRING(KERESTE.Kodu, 8, 4)  ";
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Sinif,Boy")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(klmString + " , " + mlkString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0],"Sinif , Boy" ,hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Sinif,Boy, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",2);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Sinif-Kal-Boy"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String klmString = "" ,grpString="" ,mlkString="" , blkString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",1, 2)"; 
						ozelgrp[0][1] = "Sinif"; 
						ozelgrp[1][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",4, 3)"; 
						ozelgrp[1][1] = "Kal"; 
						ozelgrp[2][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",8, 4)"; 
						ozelgrp[2][1] = "Boy"; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal " ;
						blkString = " SUBSTRING(KERESTE.Kodu, 8, 4) As Boy " ;
						grpString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif , SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal ,SUBSTRING(KERESTE.Kodu, 8, 4) As Boy ";
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal " ;
						blkString = " SUBSTRING(KERESTE.Kodu, 8, 4) As Boy " ;
						grpString = " SUBSTRING(KERESTE.Kodu,1, 2)  , SUBSTRING(KERESTE.Kodu, 4, 3),SUBSTRING(KERESTE.Kodu, 8, 4)  ";
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Sinif,Kal,Boy")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(klmString + " , " + mlkString + " , " + blkString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0],"Sinif,Kal,Boy" ,hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Sinif,Kal,Boy, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",3);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Sinif-Kal-Gen"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String klmString = "" ,grpString="" ,mlkString="" , blkString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",1, 2)"; 
						ozelgrp[0][1] = "Sinif"; 
						ozelgrp[1][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",4, 3)"; 
						ozelgrp[1][1] = "Kal"; 
						ozelgrp[2][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",8, 4)"; 
						ozelgrp[2][1] = "Gen"; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal " ;
						blkString = " SUBSTRING(KERESTE.Kodu, 13, 4)  AS Gen" ;
						grpString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ,SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal,SUBSTRING(KERESTE.Kodu, 13, 4)  AS Gen " ;
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal " ;
						blkString = " SUBSTRING(KERESTE.Kodu, 13, 4)  AS Gen" ;
						grpString = " SUBSTRING(KERESTE.Kodu,1, 2)  ,SUBSTRING(KERESTE.Kodu, 4, 3) ,SUBSTRING(KERESTE.Kodu, 13, 4)   " ;
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Sinif,Kal,Gen")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(klmString + " ," + mlkString + " ," + blkString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0],"Sinif,Kal,Gen" ,hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Sinif,Kal,Gen, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",3);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Yil"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String klmString = "" ,grpString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = " TO_CHAR(\"KERESTE\".\"" + hANGI + "Tarih\",'YYYY')" ;
						ozelgrp[0][1] = "Yil"; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " datepart(yyyy,KERESTE." + hANGI + "Tarih) AS Yil " ;
						grpString = " datepart(yyyy,KERESTE." + hANGI + "Tarih) AS Yil " ;
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " YEAR(KERESTE."+ hANGI+"Tarih)  as Yil"   ;
						grpString = " YEAR(KERESTE."+ hANGI+"Tarih) "   ;
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Yil")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(klmString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0]," Yil",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Yil, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",1);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Yil-Ay"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String mlkString = "" ,grpString="" , blkString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;

					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = " TO_CHAR(\"KERESTE\".\"" + hANGI + "Tarih\",'YYYY')" ;
						ozelgrp[0][1] = "Yil"; 
						ozelgrp[1][0] = "TO_CHAR(\"KERESTE\".\"" + hANGI + "Tarih\",'MM')"; 
						ozelgrp[1][1] = "Ay"; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						mlkString = " datepart(mm,KERESTE." + hANGI +"Tarih) as Ay " ;
						blkString = " datepart(yyyy,KERESTE." + hANGI + "Tarih) AS Yil " ;
						grpString = " datepart(yyyy,KERESTE." + hANGI + "Tarih) AS Yil , datepart(mm,KERESTE." + hANGI +"Tarih) as Ay " ;
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						mlkString = " MONTH(KERESTE."+ hANGI+"Tarih) as Ay"  ;
						blkString = " YEAR(KERESTE."+ hANGI+"Tarih) as Yil" ;
						grpString = " YEAR(KERESTE."+ hANGI+"Tarih) , MONTH(KERESTE."+ hANGI+"Tarih) " ;
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Yil,Ay")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(blkString + " , " + mlkString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0],"Yil,Ay",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Yil,Ay, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",2);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Urun Kodu-Yil"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String grpString="" , blkString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"KERESTE\".\"Kodu\""; 
						ozelgrp[0][1] = "Kodu"; 
						ozelgrp[1][0] = " TO_CHAR(\"KERESTE\".\"" + hANGI + "Tarih\",'YYYY')" ;
						ozelgrp[1][1] = "Yil"; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						blkString = " datepart(yyyy,KERESTE." + hANGI + "Tarih) AS Yil " ;
						grpString = " Kodu , datepart(yyyy,KERESTE." + hANGI + "Tarih) AS Yil " ;
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						blkString = " YEAR(KERESTE."+ hANGI+"Tarih) as Yil" ;
						grpString= " Kodu ,YEAR(KERESTE." + hANGI + "Tarih) " ;
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Kodu,Yil")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(" Kodu, " + blkString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0]," Kodu,Yil",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Kodu,Yil, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",2);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Paket-Sinif-Kal-Boy"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String grpString="" , blkString="",klmString="",mlkString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"Paket_No\""; 
						ozelgrp[0][1] = "Paket_No"; 
						ozelgrp[1][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",1, 2)"; 
						ozelgrp[1][1] = "Sinif"; 
						ozelgrp[2][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",4, 3)"; 
						ozelgrp[2][1] = "Kal"; 
						ozelgrp[3][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",8, 4)"; 
						ozelgrp[3][1] = "Boy"; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " Paket_No, SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal " ;
						blkString = " SUBSTRING(KERESTE.Kodu, 8, 4) AS Boy" ;
						grpString = " Paket_No ,SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif , SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal ,SUBSTRING(KERESTE.Kodu, 8, 4) AS Boy ";
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " Paket_No,SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal " ;
						blkString = " SUBSTRING(KERESTE.Kodu, 8, 4) AS Boy" ;
						grpString = " Paket_No, SUBSTRING(KERESTE.Kodu,1, 2)  , SUBSTRING(KERESTE.Kodu, 4, 3) ,SUBSTRING(KERESTE.Kodu, 8, 4)  ";

					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Paket_No,Sinif , Kal,Boy")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(klmString + " ," + mlkString +" ," + blkString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0],"Paket_No,Sinif , Kal,Boy",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Paket_No,Sinif , Kal,Boy, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",4);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Paket-Sinif-Kal-Gen"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String grpString="" , blkString="",klmString="",mlkString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"KERESTE\".\"Paket_No\""; 
						ozelgrp[0][1] = "Paket_No"; 
						ozelgrp[1][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",1, 2)"; 
						ozelgrp[1][1] = "Sinif"; 
						ozelgrp[2][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",4, 3)"; 
						ozelgrp[2][1] = "Kal"; 
						ozelgrp[3][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",13, 4)"; 
						ozelgrp[3][1] = "Gen"; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal " ;
						blkString = " SUBSTRING(KERESTE.Kodu, 13, 4) AS Gen " ;
						grpString = " Paket_No,SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif , SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal ,SUBSTRING(KERESTE.Kodu, 13, 4) AS Gen" ;
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal " ;
						blkString = " SUBSTRING(KERESTE.Kodu, 13, 4) AS Gen " ;
						grpString = " Paket_No,SUBSTRING(KERESTE.Kodu,1, 2)  , SUBSTRING(KERESTE.Kodu, 4, 3) ,SUBSTRING(KERESTE.Kodu, 13, 4) " ;
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Paket_No,Sinif , Kal,Gen")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(" Paket_No," + klmString + " ," + mlkString+" ," + blkString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0],"Paket_No,Sinif , Kal,Gen",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Paket_No,Sinif , Kal,Gen, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",4);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Kons-Paket-Sinif-Kal-Gen"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String grpString="" , blkString="",klmString="",mlkString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;

					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"KERESTE\".\"Konsimento\""; 
						ozelgrp[0][1] = "Konsimento"; 
						ozelgrp[1][0] = "\"KERESTE\".\"Paket_No\""; 
						ozelgrp[1][1] = "Paket_No"; 
						ozelgrp[2][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",1, 2)"; 
						ozelgrp[2][1] = "Sinif"; 
						ozelgrp[3][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",4, 3)"; 
						ozelgrp[3][1] = "Kal"; 
						ozelgrp[4][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",13, 4)"; 
						ozelgrp[4][1] = "Gen"; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal " ;
						blkString = " SUBSTRING(KERESTE.Kodu, 13, 4) AS Gen " ;
						grpString = " Konsimento,Paket_No,SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif , SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal ,SUBSTRING(KERESTE.Kodu, 13, 4) AS Gen" ;
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal " ;
						blkString = " SUBSTRING(KERESTE.Kodu, 13, 4) AS Gen " ;
						grpString = " Konsimento,Paket_No,SUBSTRING(KERESTE.Kodu,1, 2)  , SUBSTRING(KERESTE.Kodu, 4, 3) ,SUBSTRING(KERESTE.Kodu, 13, 4) " ;
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Konsimento,Paket_No,Sinif,Kal,Gen")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(" Konsimento, Paket_No," + klmString + " ," + mlkString+" ," + blkString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0]," Konsimento,Paket_No,Sinif,Kal,Gen",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Konsimento,Paket_No,Sinif,Kal,Gen, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",5);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Kons-Paket-Sinif-Kal-Boy"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String grpString="" , blkString="",klmString="",mlkString="";
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;

					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"KERESTE\".\"Konsimento\""; 
						ozelgrp[0][1] = "Konsimento"; 
						ozelgrp[1][0] = "\"KERESTE\".\"Paket_No\""; 
						ozelgrp[1][1] = "Paket_No"; 
						ozelgrp[2][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",1, 2)"; 
						ozelgrp[2][1] = "Sinif"; 
						ozelgrp[3][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",4, 3)"; 
						ozelgrp[3][1] = "Kal"; 
						ozelgrp[4][0] = "SUBSTRING(\"KERESTE\".\"Kodu\",8, 4)"; 
						ozelgrp[4][1] = "Boy"; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal " ;
						blkString = " SUBSTRING(KERESTE.Kodu, 8, 4) AS Boy " ;
						grpString = " Konsimento,Paket_No,SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif , SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal ,SUBSTRING(KERESTE.Kodu, 8, 4) AS Boy" ;
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						klmString = " SUBSTRING(KERESTE.Kodu,1, 2) AS Sinif ";
						mlkString = " SUBSTRING(KERESTE.Kodu, 4, 3) AS Kal " ;
						blkString = " SUBSTRING(KERESTE.Kodu, 8, 4) AS Boy " ;
						grpString = " Konsimento,Paket_No,SUBSTRING(KERESTE.Kodu,1, 2)  , SUBSTRING(KERESTE.Kodu, 4, 3) ,SUBSTRING(KERESTE.Kodu, 8, 4) " ;
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Konsimento,Paket_No,Sinif , Kal,Boy")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(" Konsimento, Paket_No," + klmString + " ," + mlkString+" ," + blkString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0]," Konsimento,Paket_No,Sinif , Kal,Boy ",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Konsimento,Paket_No,Sinif , Kal,Boy, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",5);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Hesap-Kodu"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String grpString="" , klmString="",mlkString="";
				String hESAP = "" ;
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
					{
						hANGI = "" ;
						hESAP = "Cari_Firma" ;
					}
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
					{
						hANGI = "C" ;
						hESAP = "CCari_Firma" ;
					}
					else if (kergrupraporDTO.getTuru().equals("STOK"))
					{
						hANGI = "" ;
						hESAP = "Cari_Firma" ;
					}
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"KERESTE\".\"" + hESAP + "\""; 
						ozelgrp[0][1] = "Hesap"; 
						String carServer = "dbname = ok_car" + cariConnDetails.getDatabaseName() + " port = " + Global_Yardimci.ipCevir(cariConnDetails.getServerIp())[1] + " host = localhost user = " + cariConnDetails.getUsername() +" password = " + cariConnDetails.getPassword() +"" ; 
						String carString ="(SELECT \"UNVAN\" FROM  dblink ('"+ carServer + "', " + 
								" 'SELECT \"UNVAN\" ,\"HESAP\" FROM \"HESAP\" ') " + 
								" AS adr(\"UNVAN\" character varying,\"HESAP\" character varying) "+
								" WHERE \"HESAP\" = \"STOK\".\"Hesap_Kodu\"  )";
						ozelgrp[1][0] = carString; 
						ozelgrp[1][1] = "Unvan"; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						String c_yer = "OK_Car" + cariConnDetails.getDatabaseName() + "" ;
						klmString = hESAP + " AS Hesap " ;
						mlkString = " (SELECT   UNVAN FROM " + c_yer + ".[dbo].[HESAP] WHERE HESAP.HESAP = KERESTE." + hESAP + "  )  " + " AS Unvan " ;
						grpString = hESAP + " AS Hesap , (SELECT   UNVAN FROM " + c_yer + ".[dbo].[HESAP] WHERE HESAP.HESAP = KERESTE." + hESAP + "  )  " + " AS Unvan " ;
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						String c_yer = "OK_Car" + cariConnDetails.getDatabaseName() + "" ;
						klmString = hESAP + " AS Hesap " ;
						mlkString = " (SELECT   UNVAN FROM " + c_yer + ".HESAP WHERE HESAP.HESAP = KERESTE." + hESAP + "  )  " + " AS Unvan " ;
						grpString = hESAP + "  , (SELECT   UNVAN FROM " + c_yer + ".HESAP WHERE HESAP.HESAP = KERESTE." + hESAP + "  )  " ;
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Hesap,Unvan")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor( klmString + " , " + mlkString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0]," Hesap , Unvan",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Hesap,Unvan, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",2);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Hesap-Kodu-Yil"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				String grpString="" , klmString="",mlkString="";
				String hESAP = "" ;
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
					{
						hANGI = "" ;
						hESAP = "Cari_Firma" ;
					}
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
					{
						hANGI = "C" ;
						hESAP = "CCari_Firma" ;
					}
					else if (kergrupraporDTO.getTuru().equals("STOK"))
					{
						hANGI = "" ;
						hESAP = "Cari_Firma" ;
					}
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"KERESTE\".\"" + hESAP + "\""; 
						ozelgrp[0][1] = "Hesap"; 
						String carServer = "dbname = ok_car" + cariConnDetails.getDatabaseName() + " port = " + Global_Yardimci.ipCevir(cariConnDetails.getServerIp())[1] + " host = localhost user = " + cariConnDetails.getUsername() +" password = " + cariConnDetails.getPassword() +"" ; 
						String carString ="(SELECT \"UNVAN\" FROM  dblink ('"+ carServer + "', " + 
								" 'SELECT \"UNVAN\" ,\"HESAP\" FROM \"HESAP\" ') " + 
								" AS adr(\"UNVAN\" character varying,\"HESAP\" character varying) "+
								" WHERE \"HESAP\" = \"STOK\".\"Hesap_Kodu\"  )";
						ozelgrp[1][0] = carString; 
						ozelgrp[1][1] = "Unvan"; 
					}
					else if(kerConnDetails.getHangisql().equals("MS SQL"))
					{
						String c_yer = "OK_Car" + cariConnDetails.getDatabaseName() + "" ;
						klmString = hESAP + " AS Hesap " ;
						mlkString = " (SELECT   UNVAN FROM " + c_yer + ".[dbo].[HESAP] WHERE HESAP.HESAP = KERESTE." + hESAP + "  )  " + " AS Unvan " ;
						grpString = hESAP + " AS Hesap , (SELECT   UNVAN FROM " + c_yer + ".[dbo].[HESAP] WHERE HESAP.HESAP = KERESTE." + hESAP + "  )  " + " AS Unvan " ;
					}
					else if(kerConnDetails.getHangisql().equals("MY SQL"))
					{
						String c_yer = "OK_Car" + cariConnDetails.getDatabaseName() + "" ;
						klmString = hESAP + " AS Hesap " ;
						mlkString = " (SELECT   UNVAN FROM " + c_yer + ".HESAP WHERE HESAP.HESAP = KERESTE." + hESAP + "  )  " + " AS Unvan " ;
						grpString = hESAP + "  , (SELECT   UNVAN FROM " + c_yer + ".HESAP WHERE HESAP.HESAP = KERESTE." + hESAP + "  )  " ;
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Hesap,Unvan")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor( klmString + " , " + mlkString,baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0]," Hesap , Unvan",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),grpString ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Hesap,Unvan, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",2);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Konsimento"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"KERESTE\".\"Konsimento\""; 
						ozelgrp[0][1] = "Konsimento"; 
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Konsimento")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor("Konsimento",baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0],"Konsimento",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),"Konsimento" ,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Konsimento, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",1);
				}
			}
			else if (kergrupraporDTO.getGruplama().equals("Paket-Konsimento"))
			{
				baslikbakStrings = baslik_bak(kergrupraporDTO);
				deg_cevirString = deg_cevir(kergrupraporDTO);
				if (! baslikbakStrings[0].equals(""))
				{
					if (kergrupraporDTO.getTuru().equals("GIREN"))
						hANGI = "" ;
					else if (kergrupraporDTO.getTuru().equals("CIKAN"))
						hANGI = "C" ;
					else if (kergrupraporDTO.getTuru().equals("STOK"))
						hANGI = "" ;
					if(kerConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"Paket_No\""; 
						ozelgrp[0][1] = "Paket_No"; 
						ozelgrp[1][0] = "\"Konsimento\""; 
						ozelgrp[1][1] = "Konsimento"; 
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Paket_No,Konsimento")); 
					List<Map<String, Object>> grup = keresteService.grp_rapor(" Paket_No,Konsimento ",baslikbakStrings[1],deg_cevirString[3], deg_cevirString[5],   kergrupraporDTO.getAnagrp(),  kergrupraporDTO.getAltgrp(),  kergrupraporDTO.getOzkod(),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2(),
							deg_cevirString[0],
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),
							deg_cevirString[4], baslikbakStrings[0],"Paket_No , Konsimento",hANGI,
							kergrupraporDTO.getKons1(),kergrupraporDTO.getKons2(),kergrupraporDTO.getDepo(),"Paket_No,Konsimento",
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2(),ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(kergrupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else if(kergrupraporDTO.getBirim().equals("Miktar"))
						response.put("format",0);
					else
						response.put("format",3);
					response.put("baslik","Paket_No,Konsimento, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",2);
				}
			}
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	private String[] deg_cevir(kergrupraporDTO kergrupraporDTO )
	{
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails kerConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
		ConnectionDetails kurConnDetails =  UserSessionManager.getUserSession(useremail, "Kur");

		String[] deg_cevirString = {"","","","","",""};
		String jkj = "" ,jkj1 = "" ,ch1 = "",sstr_4 = "" , sstr_5 = "" , kur_dos = "" ;

		String hangiFiatString = "" ;
		String hangiIskontoString = "" ;
		String hTarString = "" ;
		if (kergrupraporDTO.getTuru().equals("GIREN"))
		{
			hangiFiatString = "Fiat" ;
			hangiIskontoString = "Iskonto" ;
			hTarString = "" ;
		}
		else if (kergrupraporDTO.getTuru().equals("CIKAN"))
		{
			hangiFiatString = "CFiat" ;
			hangiIskontoString = "CIskonto" ;
			hTarString = "C" ;
		}
		else if (kergrupraporDTO.getTuru().equals("STOK"))
		{
			hangiFiatString = "Fiat" ;
			hangiIskontoString = "Iskonto" ;
			hTarString = "" ;
		}
		if (kergrupraporDTO.getBirim().equals("Tutar"))
		{
			if (kergrupraporDTO.isDvzcevirchc())
			{
				if(kerConnDetails.getHangisql().equals("MS SQL"))
					sstr_4 = " ((("+ hangiFiatString + " * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) - (("+ hangiFiatString + " * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT,SUBSTRING(KERESTE.Kodu,8,4)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu,13,4))) * Miktar)/1000000000)) * "+ hangiIskontoString + ")/100) / iif(k." + kergrupraporDTO.getDvzturu() + " = 0 ,1, k." + kergrupraporDTO.getDvzturu() + ")) as Tutar ";
				else if (kerConnDetails.getHangisql().equals("MY SQL"))
					sstr_4 = " ((("+ hangiFiatString + " * (((CONVERT( SUBSTRING(KERESTE.Kodu, 4, 3),DECIMAL )  *  CONVERT( SUBSTRING(KERESTE.Kodu, 8, 4),DECIMAL) * CONVERT(SUBSTRING(KERESTE.Kodu, 13, 4),DECIMAL )  ) * Miktar)/1000000000)) - (("+ hangiFiatString + " * (((CONVERT(SUBSTRING(KERESTE.Kodu, 4, 3),DECIMAL ) * CONVERT(SUBSTRING(KERESTE.Kodu,8,4),DECIMAL) * CONVERT(SUBSTRING(KERESTE.Kodu,13,4),DECIMAL)) * Miktar)/1000000000)) * "+ hangiIskontoString + ")/100) / IF(k." + kergrupraporDTO.getDvzturu() + " = 0 ,1, k." + kergrupraporDTO.getDvzturu() + "))  ";
				else if (kerConnDetails.getHangisql().equals("PG SQL"))
				{
					String kurcString = kergrupraporDTO.getDoviz();
					String kurString  = " / tr.\"" + kurcString + "\"" ;
					sstr_4 = "SUM((((\"KERESTE\".\""+ hangiFiatString + "\" * (((SUBSTRING(\"KERESTE\".\"Kodu\", 4, 3)::int  *  SUBSTRING(\"KERESTE\".\"Kodu\", 8, 4)::int * SUBSTRING(\"KERESTE\".\"Kodu\", 13, 4)::int ) * \"Miktar\")/1000000000)) - ((\"KERESTE\".\""+ hangiFiatString + "\" * (((SUBSTRING(\"KERESTE\".\"Kodu\",4,3)::int  * SUBSTRING(\"KERESTE\".\"Kodu\", 8, 4)::int * SUBSTRING(\"KERESTE\".\"Kodu\", 13, 4)::int ) * \"Miktar\")/1000000000)) * \"KERESTE\".\""+ hangiIskontoString + "\")/100)) " + kurString+")::DOUBLE PRECISION ";
				}
			}
			else
			{
				if(kerConnDetails.getHangisql().equals("MS SQL"))
					sstr_4 = "(("+ hangiFiatString + " * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu,8,4)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu,13,4))) * Miktar)/1000000000)) - (("+ hangiFiatString + " * (((CONVERT(INT,SUBSTRING(KERESTE.Kodu,4,3)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu,8,4)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu,13,4))) * Miktar)/1000000000)) * "+ hangiIskontoString + ")/100) as Tutar";
				else if (kerConnDetails.getHangisql().equals("MY SQL"))
					sstr_4 = "(("+ hangiFiatString + " * (((CONVERT(SUBSTRING(KERESTE.Kodu, 4, 3),DECIMAL )  *  CONVERT(SUBSTRING(KERESTE.Kodu, 8, 4),DECIMAL) * CONVERT(SUBSTRING(KERESTE.Kodu, 13, 4),DECIMAL )  ) * Miktar)/1000000000)) - (("+ hangiFiatString + " * (((CONVERT(SUBSTRING(KERESTE.Kodu, 4, 3),DECIMAL )  *  CONVERT(SUBSTRING(KERESTE.Kodu,8,4),DECIMAL) * CONVERT(SUBSTRING(KERESTE.Kodu,13,4),DECIMAL)) * Miktar)/1000000000)) * "+ hangiIskontoString + ")/100) ";
				else if (kerConnDetails.getHangisql().equals("PG SQL"))
					sstr_4 = "SUM(((\"KERESTE\".\""+ hangiFiatString + "\" * (((SUBSTRING(\"KERESTE\".\"Kodu\", 4, 3)::int  *  SUBSTRING(\"KERESTE\".\"Kodu\", 8, 4)::int * SUBSTRING(\"KERESTE\".\"Kodu\", 13, 4)::int ) * \"Miktar\")/1000000000)) - ((\"KERESTE\".\""+ hangiFiatString + "\" * (((SUBSTRING(\"KERESTE\".\"Kodu\", 4, 3)::int  * SUBSTRING(\"KERESTE\".\"Kodu\", 8, 4)::int * SUBSTRING(\"KERESTE\".\"Kodu\", 13, 4)::int ) * \"Miktar\")/1000000000)) * \"KERESTE\".\""+ hangiIskontoString + "\")/100))::DOUBLE PRECISION ";
			}
			sstr_5 = "Tutar";
		}
		else  if (kergrupraporDTO.getBirim().equals("Miktar"))
		{
			sstr_4 = " Miktar";
			sstr_5 = "Miktar";
			if (kerConnDetails.getHangisql().equals("PG SQL"))
			{
				sstr_4 = "SUM(\"Miktar\")::DOUBLE PRECISION  ";
				sstr_5 = "Miktar";
			}
		}
		else  if (kergrupraporDTO.getBirim().equals("m3"))
		{
			if(kerConnDetails.getHangisql().equals("MS SQL"))
			{
				sstr_4 = " (((CONVERT(INT,SUBSTRING(KERESTE.Kodu,4,3)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu,8,4)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu,13,4))) * Miktar)/1000000000) as m3";
				sstr_5 = "m3";
			}
			else if (kerConnDetails.getHangisql().equals("MY SQL"))
			{
				sstr_4 = " (((CONVERT(SUBSTRING(KERESTE.Kodu,4,3),DECIMAL) * CONVERT(SUBSTRING(KERESTE.Kodu,8,4),DECIMAL) * CONVERT(SUBSTRING(KERESTE.Kodu,13,4),DECIMAL)) * Miktar)/1000000000) ";
				sstr_5 = "m3";
			}
			else if (kerConnDetails.getHangisql().equals("PG SQL"))
			{
				sstr_4 = "SUM((((SUBSTRING(\"KERESTE\".\"Kodu\",4,3)::int  *  SUBSTRING(\"KERESTE\".\"Kodu\",8,4)::int * SUBSTRING(\"KERESTE\".\"Kodu\", 13, 4)::int) * \"Miktar\")/1000000000))::DOUBLE PRECISION ";
				sstr_5 = "m3";
			}
		}
		if (kergrupraporDTO.isDvzcevirchc())
		{
			if (kergrupraporDTO.getBirim().equals("Tutar"))
			{
				if(kerConnDetails.getHangisql().equals("MS SQL"))
					kur_dos = "  left outer join OK_Kur" + kurConnDetails.getDatabaseName() + ".dbo.kurlar k on k.Tarih = convert(varchar(10), KERESTE." + hTarString + "Tarih, 120) and (k.kur IS NULL OR k.KUR ='" + kergrupraporDTO.getDoviz() + "') ";
				else if(kerConnDetails.getHangisql().equals("MY SQL"))
					kur_dos = "  left outer join ok_kur" + kurConnDetails.getDatabaseName() + ".kurlar k on k.Tarih = DATE(KERESTE." + hTarString + "Tarih) and  k.kur ='" + kergrupraporDTO.getDoviz() + "' ";
				else if(kerConnDetails.getHangisql().equals("PG SQL"))
				{
					String kurServer = "" ; 
					String[] ipogren = Global_Yardimci.ipCevir(kurConnDetails.getServerIp());
					if (kerConnDetails.getServerIp().equals(kurConnDetails.getServerIp()))
						kurServer = "dbname = ok_kur" + kurConnDetails.getDatabaseName() + " port = " + ipogren[1] + " host = localhost user = " + kurConnDetails.getUsername() + " password = " + kurConnDetails.getPassword() +"" ; 
					else
						kurServer = "dbname = ok_kur" + kurConnDetails.getDatabaseName() + " port = " + ipogren[1] + " host = " +   ipogren[0] + " user = " + kurConnDetails.getUsername() + " password = " + kurConnDetails.getPassword() +"" ; 
					String kurcString = kergrupraporDTO.getDvzturu();
					String kurcesitString = kergrupraporDTO.getDoviz();
					kur_dos = " left join  (SELECT * FROM  dblink ('" + kurServer + "'," + 
							" 'SELECT \"TARIH\", \"" + kurcString + "\",\"KUR\" FROM \"KURLAR\" WHERE \"KUR\"= ''" + kurcesitString +"''')  AS kur " +
							" (\"TARIH\" timestamp,\"" + kurcString + "\" DOUBLE PRECISION,\"KUR\" character varying) " +
							" ) as tr on DATE(\"KERESTE\".\"" +hTarString + "Tarih\") = DATE(tr.\"TARIH\") " ;
				}
			}
			else
				kur_dos = "";
		}
		else
			kur_dos = "" ;
		jkj  = "" ;
		if (kergrupraporDTO.getTuru().equals("GIREN"))
			jkj =   "" ;
		else if (kergrupraporDTO.getTuru().equals("CIKAN"))
			jkj = " Cikis_Evrak <> '' AND  " ;
		else if (kergrupraporDTO.getTuru().equals("STOK"))
			jkj = " Cikis_Evrak = '' AND " ;
		deg_cevirString[0] = jkj ;
		deg_cevirString[1] = jkj1 ;
		deg_cevirString[2] = ch1 ;
		deg_cevirString[3] = sstr_4 ;
		deg_cevirString[4] = sstr_5 ;
		deg_cevirString[5] = kur_dos ;
		return deg_cevirString;
	}
	private String[] grup_cevir(String ana,String alt, String oz1,String depo)
	{
		String deger[] = {"","","",""};
		String qwq1 = "", qwq2="", qwq3="",qwq4="";
		if (ana.equals(""))
			qwq1 = " Like  '%' " ;
		else if  (ana.equals("Bos Olanlar"))
			qwq1 = " = '' " ;
		else
		{
			String anas = keresteService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", ana);
			qwq1 = "=" + anas;
		}
		deger[0] = qwq1; 
		if (alt.equals(""))
			qwq2 = " Like  '%' " ;
		else if  (alt.equals("Bos Olanlar"))
			qwq2 = " = '' " ;
		else
		{
			String alts = keresteService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", alt);
			qwq2 ="=" + alts;
		}
		deger[1] = qwq2; 
		if (oz1.equals(""))
			qwq3 = " Like  '%' " ;
		else if  (oz1.equals("Bos Olanlar"))
			qwq3 = " = '' " ;
		else
		{
			String ozs1 = keresteService.urun_kod_degisken_ara("OZ1ID_Y", "OZEL_KOD_1", "OZ_KOD_1_DEGISKEN", oz1);
			qwq3 = "=" + ozs1;
		}
		deger[2] = qwq3; 
		if (depo.equals(""))
			qwq4 = " Like  '%' " ;
		else if  (depo.equals("Bos Olanlar"))
			qwq4 = " = '' " ;
		else
		{
			String dpo1 = keresteService.urun_kod_degisken_ara("DEPO", "DPID_Y", "DEPO_DEGISKEN", depo);
			qwq4 = "=" + dpo1;
		}
		deger[3] = qwq4; 
		return deger;
	}

	private String[] baslik_bak(kergrupraporDTO kergrupraporDTO)
	{
		String[] baslikbakStrings = {"",""};
		try {
			List<Map<String, Object>> baslik = new ArrayList<>();
			String jkj  = "" ;
			String sstr_2 = "" ;
			String hANGI = "" ;
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kerConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			jkj  = "" ;
			hANGI = "" ;
			if (kergrupraporDTO.getTuru().equals("GIREN"))
			{
				jkj =   "" ;
				hANGI = "" ;
			}
			else if (kergrupraporDTO.getTuru().equals("CIKAN"))
			{
				jkj = " Cikis_Evrak <> '' AND  " ;
				hANGI = "C" ;
			}
			else if (kergrupraporDTO.getTuru().equals("STOK"))
			{
				jkj = " Cikis_Evrak = '' AND " ;
				hANGI = "" ;
			}
			if (kergrupraporDTO.getStunlar().equals("Yil"))
			{
				if(kerConnDetails.getHangisql().equals("MS SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT datepart(yyyy,KERESTE." + hANGI + "Tarih)","ORDER BY datepart(yyyy,KERESTE." + hANGI + "Tarih)",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " datepart(yyyy,KERESTE." + hANGI + "Tarih)" ;
				}
				else if(kerConnDetails.getHangisql().equals("MY SQL")) {
					baslik = keresteService.baslik_bak("DISTINCT YEAR(KERESTE." + hANGI + "Tarih)","ORDER BY YEAR(KERESTE."+ hANGI+"Tarih)",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " YEAR(KERESTE."+ hANGI+"Tarih)" ;
				}
				else if(kerConnDetails.getHangisql().equals("PG SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT TO_CHAR(\"KERESTE\".\"" + hANGI + "Tarih\",'YYYY')","ORDER BY TO_CHAR(\"KERESTE\".\"" + hANGI + "Tarih\",'YYYY')",jkj.replace("Cikis_Evrak", "\"Cikis_Evrak\""),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " TO_CHAR(\"KERESTE\".\"" + hANGI + "Tarih\",'YYYY')" ;
				}
			}
			else if (kergrupraporDTO.getStunlar().equals("Ay"))
			{
				if(kerConnDetails.getHangisql().equals("MS SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT datepart(mm,KERESTE." + hANGI + "Tarih)","ORDER BY datepart(mm,KERESTE." + hANGI + "Tarih)",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " datepart(mm,KERESTE." + hANGI + "Tarih)" ;
				}
				else if(kerConnDetails.getHangisql().equals("MY SQL")) {
					baslik = keresteService.baslik_bak("DISTINCT MONTH(KERESTE." + hANGI + "Tarih)","ORDER BY MONTH(KERESTE."+ hANGI+"Tarih)",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " MONTH(KERESTE."+ hANGI+"Tarih)" ;
				}
				else if(kerConnDetails.getHangisql().equals("PG SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT TO_CHAR(\"KERESTE\".\"" + hANGI + "Tarih\",'MM')","ORDER BY TO_CHAR(\"KERESTE\".\"" + hANGI + "Tarih\",'MM')",jkj.replace("Cikis_Evrak", "\"Cikis_Evrak\""),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " TO_CHAR(\"KERESTE\".\"" + hANGI + "Tarih\",'MM')" ;
				}
			}
			else if (kergrupraporDTO.getStunlar().equals("Gun"))
			{
				if(kerConnDetails.getHangisql().equals("MS SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT datepart(dd,KERESTE." + hANGI + "Tarih)","ORDER BY datepart(dd,KERESTE." + hANGI + "Tarih)",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " datepart(dd,KERESTE." + hANGI + "Tarih)" ;
				}
				else if(kerConnDetails.getHangisql().equals("MY SQL")) {
					baslik = keresteService.baslik_bak("DISTINCT DAY(KERESTE." + hANGI + "Tarih)","ORDER BY DAY(KERESTE."+ hANGI+"Tarih)",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " DAY(KERESTE."+ hANGI+"Tarih)" ;
				}
				else if(kerConnDetails.getHangisql().equals("PG SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT TO_CHAR(\"KERESTE\".\"" + hANGI + "Tarih\",'DD')","ORDER BY TO_CHAR(\"KERESTE\".\"" + hANGI + "Tarih\",'DD')",jkj.replace("Cikis_Evrak", "\"Cikis_Evrak\""),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " TO_CHAR(\"KERESTE\".\"" + hANGI + "Tarih\",'DD')" ;
				}
			}
			else if (kergrupraporDTO.getStunlar().equals("Kalinlik"))
			{
				if(kerConnDetails.getHangisql().equals("MS SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT CONVERT(INT,SUBSTRING(KERESTE.Kodu,4,3)) ", "ORDER BY CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) ) ",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) ) " ;
				}
				else if(kerConnDetails.getHangisql().equals("MY SQL")) {
					baslik = keresteService.baslik_bak("DISTINCT CONVERT(SUBSTRING(KERESTE.Kodu,4,3),DECIMAL)  ", "ORDER BY CONVERT(SUBSTRING(KERESTE.Kodu, 4, 3) , DECIMAL) ",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " CONVERT(SUBSTRING(KERESTE.Kodu, 4, 3) ,DECIMAL) " ;
				}
				else if(kerConnDetails.getHangisql().equals("PG SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT SUBSTRING(\"KERESTE\".\"Kodu\",4,3) ","ORDER BY SUBSTRING(\"KERESTE\".\"Kodu\", 4, 3)",jkj.replace("Cikis_Evrak", "\"Cikis_Evrak\""),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " SUBSTRING(\"KERESTE\".\"Kodu\", 4, 3) " ;
				}
			}
			else if (kergrupraporDTO.getStunlar().equals("Boy"))
			{
				if(kerConnDetails.getHangisql().equals("MS SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4) ) ", "ORDER BY CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4) ) ",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4) ) " ;
				}
				else if(kerConnDetails.getHangisql().equals("MY SQL")) {
					baslik = keresteService.baslik_bak("DISTINCT CONVERT(SUBSTRING(KERESTE.Kodu,8,4),DECIMAL) ", "ORDER BY CONVERT(SUBSTRING(KERESTE.Kodu,8,4),DECIMAL) ",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " CONVERT(SUBSTRING(KERESTE.Kodu, 8, 4) ,DECIMAL) " ;
				}
				else if(kerConnDetails.getHangisql().equals("PG SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT SUBSTRING(\"KERESTE\".\"Kodu\",8,4) ","ORDER BY SUBSTRING(\"KERESTE\".\"Kodu\", 8, 4)",jkj.replace("Cikis_Evrak", "\"Cikis_Evrak\""),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " SUBSTRING(\"KERESTE\".\"Kodu\", 8, 4) " ;
				}
			}
			else if (kergrupraporDTO.getStunlar().equals("Genislik"))
			{
				if(kerConnDetails.getHangisql().equals("MS SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) ) ", "ORDER BY CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) ) ",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) ) " ;
				}
				else if(kerConnDetails.getHangisql().equals("MY SQL")) {
					baslik = keresteService.baslik_bak("DISTINCT CONVERT(SUBSTRING(KERESTE.Kodu,13,4),DECIMAL) ", "ORDER BY CONVERT(SUBSTRING(KERESTE.Kodu,13,4),DECIMAL) ",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " CONVERT(SUBSTRING(KERESTE.Kodu, 13, 4) ,DECIMAL) " ;
				}
				else if(kerConnDetails.getHangisql().equals("PG SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT SUBSTRING(\"KERESTE\".\"Kodu\",13,4) ","ORDER BY SUBSTRING(\"KERESTE\".\"Kodu\", 13, 4)",jkj.replace("Cikis_Evrak", "\"Cikis_Evrak\""),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " SUBSTRING(\"KERESTE\".\"Kodu\", 13, 4) " ;
				}
			}
			else if (kergrupraporDTO.getStunlar().equals("Sinif"))
			{
				if(kerConnDetails.getHangisql().equals("MS SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT SUBSTRING(KERESTE.Kodu,1,2)  ","ORDER BY SUBSTRING(KERESTE.Kodu, 1, 2)  ",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = "  SUBSTRING(KERESTE.Kodu,1,2) " ;
				}
				else if(kerConnDetails.getHangisql().equals("MY SQL")) {
					baslik = keresteService.baslik_bak("DISTINCT SUBSTRING(KERESTE.Kodu,1,2) ","ORDER BY SUBSTRING(KERESTE.Kodu, 1, 2)  ",jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " SUBSTRING(KERESTE.Kodu,1,2) " ;
				}
				else if(kerConnDetails.getHangisql().equals("PG SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT SUBSTRING(\"KERESTE\".\"Kodu\",1,2) ","ORDER BY SUBSTRING(\"KERESTE\".\"Kodu\",1, 2)",jkj.replace("Cikis_Evrak", "\"Cikis_Evrak\""),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = " SUBSTRING(\"KERESTE\".\"Kodu\",1,2) " ;
				}
			}
			else if (kergrupraporDTO.getStunlar().equals("Hesap-Kodu"))
			{
				String hKODU = "" ;
				if (kergrupraporDTO.getTuru().equals("GIREN"))
					hKODU = "Cari_Firma" ;
				else if (kergrupraporDTO.getTuru().equals("CIKAN"))
					hKODU = "CCari_Firma" ;
				else if (kergrupraporDTO.getTuru().equals("STOK"))
					hKODU = "Cari_Firma" ;
				if(kerConnDetails.getHangisql().equals("MS SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT " + hKODU,"ORDER BY " + hKODU,jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = hKODU ;
				}
				else if(kerConnDetails.getHangisql().equals("MY SQL")) {
					baslik = keresteService.baslik_bak("DISTINCT " + hKODU,"ORDER BY " + hKODU,jkj,
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = hKODU ;
				}
				else if(kerConnDetails.getHangisql().equals("PG SQL"))
				{
					baslik = keresteService.baslik_bak("DISTINCT \"" + hKODU.trim() + "\" ","ORDER BY \"" + hKODU.trim() + "\" ",jkj.replace("Cikis_Evrak", "\"Cikis_Evrak\""),
							kergrupraporDTO.getUkod1(),kergrupraporDTO.getUkod2() ,
							kergrupraporDTO.getCkod1(),kergrupraporDTO.getCkod2() ,
							kergrupraporDTO.getTar1(),kergrupraporDTO.getTar2(),hANGI,
							kergrupraporDTO.getEvr1(),kergrupraporDTO.getEvr2());
					sstr_2 = "\"" + hKODU.trim() + "\" " ;
				}
			}
			String sstr_1 = "";
			StringBuilder text = new StringBuilder();
			for (Map<String, Object> row : baslik)
				row.forEach((key, value) -> text.append("[").append(value).append("],"));
			sstr_1 = text.length() > 0 ? text.substring(0, text.length() - 1) : "";
			baslikbakStrings[0] = sstr_1;
			baslikbakStrings[1] = sstr_2;
		}
		catch (Exception ex)
		{
			throw new ServiceException(ex.getMessage());
		}
		return baslikbakStrings;
	}

	@SuppressWarnings("unchecked")
	@PostMapping("kereste/grp_download")
	@ResponseBody
	public ResponseEntity<byte[]> downloadReport(@RequestBody Map<String, Object> requestBody) {
		ByteArrayDataSource dataSource ;
		try {
			List<String> header =  (List<String>) requestBody.get("headers");  
			String tableString = (String) requestBody.get("data");
			int sabitkolon = (int) requestBody.get("sabitkolon");
			List<Map<String, String>> tableData = ResultSetConverter.parseTableData(tableString, header);
			dataSource =  raporOlustur.grprap(tableData,sabitkolon);
			if (dataSource == null)
				throw new ServiceException("Rapor oluşturulamadı: veri bulunamadı.");
			byte[] fileContent = dataSource.getInputStream().readAllBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			String fileName = "Grup_Rapor.xlsx";
			headers.setContentDispositionFormData("attachment", fileName);
			return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage().getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Beklenmeyen bir hata oluştu.".getBytes(StandardCharsets.UTF_8));
		} finally {
			dataSource = null;
		}	
	}
}