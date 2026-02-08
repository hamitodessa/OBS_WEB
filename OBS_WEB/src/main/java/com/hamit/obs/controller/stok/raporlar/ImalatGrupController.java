package com.hamit.obs.controller.stok.raporlar;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.dto.stok.raporlar.grupraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.fatura.FaturaService;

import jakarta.mail.util.ByteArrayDataSource;


@Controller
public class ImalatGrupController {

	@Autowired
	private RaporOlustur raporOlustur;

	@Autowired
	private FaturaService faturaService;

	@GetMapping("/stok/imagrprapor")
	public String imagrprapor() {
		return "stok/raporlar/imagrprapor";
	}

	@PostMapping("stok/imagrpdoldur")
	@ResponseBody
	public Map<String, Object> grpdoldur(@RequestBody grupraporDTO grupraporDTO) {
		Map<String, Object> response = new HashMap<>();
		List<Map<String, Object>> imagrup = new ArrayList<>();
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);

			String[] baslikbakStrings = {"","","",""};
			String[] deg_cevirString = {"",""};
			String ozelgrp[][] = new String[7][2];
			String turuString[] =  grup_cevir(grupraporDTO.getAnagrp(),grupraporDTO.getAltgrp() ,grupraporDTO.getUranagrp(),grupraporDTO.getUraltgrp());
			if (grupraporDTO.getGruplama().equals("Urun Kodu"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
				{
					ozelgrp = new String[10][10];
					ozelgrp[0][0] = "\"MAL\".\"Kodu\""; 
					ozelgrp[0][1] = "Urun_Kodu"; 
					ozelgrp[1][0] = "\"MAL\".\"Adi\""; 
					ozelgrp[1][1] = "Urun_Adi";
					ozelgrp[2][0] = "\"Birim\"" ; 
					ozelgrp[2][1] = "Birim";
				}
				grupraporDTO.setAnagrp(turuString[0]);
				grupraporDTO.setAltgrp(turuString[1]);
				grupraporDTO.setUranagrp(turuString[2]);
				grupraporDTO.setUraltgrp(turuString[3]);
				String sstr_55 = deg_cevirString[1] ;
				deg_cevirString[1] = ",'" + deg_cevirString[1] + "' as Birim," ;
				Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Urun_Kodu", "Urun_Adi"));
				imagrup = faturaService.ima_alt_kod("MAL.Kodu as Urun_Kodu, Adi as Urun_Adi",deg_cevirString[1],
						baslikbakStrings[1],deg_cevirString[0], baslikbakStrings[2], baslikbakStrings[3],  
						grupraporDTO.getUranagrp(),grupraporDTO.getUraltgrp(),
						grupraporDTO.getAnagrp(),  grupraporDTO.getAltgrp(),
						grupraporDTO.getSinif1(), grupraporDTO.getSinif2(),
						grupraporDTO.getUkod1(),grupraporDTO.getUkod2(),
						grupraporDTO.getTar1(),
						grupraporDTO.getTar2(),
						baslikbakStrings[0],"Urun_Kodu",sstr_55,ozelgrp,sabitKolonlar);
				response.put("data", (imagrup != null) ? imagrup : new ArrayList<>());
				if(grupraporDTO.getBirim().equals("Tutar"))
					response.put("format",2);
				else
					response.put("format",3);
				response.put("baslik","Urun_Kodu, Urun_Adi , " + baslikbakStrings[0] + ",TOPLAM");   
				response.put("sabitkolonsayisi",2);
			}
			else if (grupraporDTO.getGruplama().equals("Depo"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
				{
					ozelgrp = new String[10][10];
					ozelgrp[0][0] = "(SELECT DISTINCT  \"DEPO\" FROM \"DEPO_DEGISKEN\" WHERE \"DEPO_DEGISKEN\".\"DPID_Y\" = \"STOK\".\"Depo\" )"; 
					ozelgrp[0][1] = "Depo"; 
					ozelgrp[1][0] = "\"Birim\""; 
					ozelgrp[1][1] = "Birim";
				}
				grupraporDTO.setAnagrp(turuString[0]);
				grupraporDTO.setAltgrp(turuString[1]);
				grupraporDTO.setUranagrp(turuString[2]);
				grupraporDTO.setUraltgrp(turuString[3]);
				String sstr_55 = deg_cevirString[1] ;
				deg_cevirString[1] = ",'" + deg_cevirString[1] + "' as Birim," ;
				Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Depo"));
				imagrup = faturaService.ima_alt_kod("(SELECT DISTINCT  DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = STOK.Depo ) as Depo",deg_cevirString[1],
						baslikbakStrings[1],deg_cevirString[0], baslikbakStrings[2], baslikbakStrings[3],  
						grupraporDTO.getUranagrp(),grupraporDTO.getUraltgrp(),
						grupraporDTO.getAnagrp(),  grupraporDTO.getAltgrp(),
						grupraporDTO.getSinif1(), grupraporDTO.getSinif2(),
						grupraporDTO.getUkod1(),grupraporDTO.getUkod2(),
						grupraporDTO.getTar1(),
						grupraporDTO.getTar2(),
						baslikbakStrings[0],"Depo",sstr_55,ozelgrp,sabitKolonlar);
				response.put("data", (imagrup != null) ? imagrup : new ArrayList<>());
				if(grupraporDTO.getBirim().equals("Tutar"))
					response.put("format",2);
				else
					response.put("format",3);
				response.put("baslik","Depo, " + baslikbakStrings[0] + ",TOPLAM");   
				response.put("sabitkolonsayisi",1);
			}
			else if (grupraporDTO.getGruplama().equals("Yil"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				String slctString = "";
				if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
				{
					ozelgrp = new String[10][10];
					ozelgrp[0][0] = "TO_CHAR(\"STOK\".\"Tarih\",'YYYY')"; 
					ozelgrp[0][1] = "Yil"; 
					slctString = "YEAR(STOK.Tarih) as Yil";
				}
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.MSSQL))
					slctString = "datepart(yyyy,STOK.Tarih) as Yil, ";
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.MYSQL))
					slctString = "YEAR(STOK.Tarih) as Yil";
				grupraporDTO.setAnagrp(turuString[0]);
				grupraporDTO.setAltgrp(turuString[1]);
				grupraporDTO.setUranagrp(turuString[2]);
				grupraporDTO.setUraltgrp(turuString[3]);
				String sstr_55 = deg_cevirString[1] ;
				deg_cevirString[1] = "" ;
				Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Yil"));
				imagrup = faturaService.ima_alt_kod(slctString,deg_cevirString[1],
						baslikbakStrings[1],deg_cevirString[0], baslikbakStrings[2], baslikbakStrings[3],  
						grupraporDTO.getUranagrp(),grupraporDTO.getUraltgrp(),
						grupraporDTO.getAnagrp(),  grupraporDTO.getAltgrp(),
						grupraporDTO.getSinif1(), grupraporDTO.getSinif2(),
						grupraporDTO.getUkod1(),grupraporDTO.getUkod2(),
						grupraporDTO.getTar1(),
						grupraporDTO.getTar2(),
						baslikbakStrings[0],"Yil",sstr_55,ozelgrp,sabitKolonlar);
				response.put("data", (imagrup != null) ? imagrup : new ArrayList<>());
				if(grupraporDTO.getBirim().equals("Tutar"))
					response.put("format",2);
				else
					response.put("format",3);
				response.put("baslik","Yil, " + baslikbakStrings[0] + ",TOPLAM");   
				response.put("sabitkolonsayisi",1);
			}
			else if (grupraporDTO.getGruplama().equals("Ana_Grup"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
				{
					ozelgrp = new String[10][10];
					ozelgrp[0][0] = "(SELECT DISTINCT  \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID_Y\" = \"STOK\".\"Ana_Grup\"  )"; 
					ozelgrp[0][1] = "Ana_Grup"; 
					ozelgrp[1][0] ="\"Birim\"";
					ozelgrp[1][1] = "Birim";
				}
				grupraporDTO.setAnagrp(turuString[0]);
				grupraporDTO.setAltgrp(turuString[1]);
				grupraporDTO.setUranagrp(turuString[2]);
				grupraporDTO.setUraltgrp(turuString[3]);
				String sstr_55 = deg_cevirString[1] ;
				deg_cevirString[1] = ",'" + deg_cevirString[1] + "' as Birim," ;
				Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Ana_Grup"));
				imagrup = faturaService.ima_alt_kod("(SELECT DISTINCT  ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = STOK.Ana_Grup ) as Ana_Grup  ",deg_cevirString[1],
						baslikbakStrings[1],deg_cevirString[0], baslikbakStrings[2], baslikbakStrings[3],  
						grupraporDTO.getUranagrp(),grupraporDTO.getUraltgrp(),
						grupraporDTO.getAnagrp(),  grupraporDTO.getAltgrp(),
						grupraporDTO.getSinif1(), grupraporDTO.getSinif2(),
						grupraporDTO.getUkod1(),grupraporDTO.getUkod2(),
						grupraporDTO.getTar1(),
						grupraporDTO.getTar2(),
						baslikbakStrings[0],"Ana_Grup",sstr_55,ozelgrp,sabitKolonlar);
				response.put("data", (imagrup != null) ? imagrup : new ArrayList<>());
				if(grupraporDTO.getBirim().equals("Tutar"))
					response.put("format",2);
				else
					response.put("format",3);
				response.put("baslik","Ana_Grup, " + baslikbakStrings[0] + ",TOPLAM");   
				response.put("sabitkolonsayisi",1);
			}
			else if (grupraporDTO.getGruplama().equals("Alt_Grup"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
				{
					ozelgrp = new String[10][10];
					ozelgrp[0][0] = "(SELECT DISTINCT  \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID_Y\" = \"STOK\".\"Ana_Grup\"  )"; 
					ozelgrp[0][1] = "Ana_Grup"; 
					ozelgrp[1][0] = "(SELECT DISTINCT  \"ALT_GRUP\" FROM \"ALT_GRUP_DEGISKEN\" WHERE \"ALT_GRUP_DEGISKEN\".\"ALID\" = \"STOK\".\"Alt_Grup\"  )"; 
					ozelgrp[1][1] = "Alt_Grup"; 
					ozelgrp[2][0] = "\"Birim\"";
					ozelgrp[2][1] = "Birim";
				}
				grupraporDTO.setAnagrp(turuString[0]);
				grupraporDTO.setAltgrp(turuString[1]);
				grupraporDTO.setUranagrp(turuString[2]);
				grupraporDTO.setUraltgrp(turuString[3]);
				String sstr_55 = deg_cevirString[1] ;
				deg_cevirString[1] = ",'" + deg_cevirString[1] + "' as Birim," ;
				Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Ana_Grup" , "Alt_Grup"));
				imagrup = faturaService.ima_alt_kod("(SELECT DISTINCT  ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = STOK.Ana_Grup ) as Ana_Grup , "
						+ " (SELECT DISTINCT  ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID = STOK.Alt_Grup ) as Alt_Grup " ,deg_cevirString[1],
						baslikbakStrings[1],deg_cevirString[0], baslikbakStrings[2], baslikbakStrings[3],  
						grupraporDTO.getUranagrp(),grupraporDTO.getUraltgrp(),
						grupraporDTO.getAnagrp(),  grupraporDTO.getAltgrp(),
						grupraporDTO.getSinif1(), grupraporDTO.getSinif2(),
						grupraporDTO.getUkod1(),grupraporDTO.getUkod2(),
						grupraporDTO.getTar1(),
						grupraporDTO.getTar2(),
						baslikbakStrings[0],"Ana_Grup",sstr_55,ozelgrp,sabitKolonlar);
				response.put("data", (imagrup != null) ? imagrup : new ArrayList<>());
				if(grupraporDTO.getBirim().equals("Tutar"))
					response.put("format",2);
				else
					response.put("format",3);
				response.put("baslik","Ana_Grup , Alt_Grup, " + baslikbakStrings[0] + ",TOPLAM");   
				response.put("sabitkolonsayisi",2);
			}
			else if (grupraporDTO.getGruplama().equals("Alt_Grup_Yil"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				String slctString = "";
				if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
				{
					ozelgrp = new String[10][10];
					ozelgrp[0][0] = "(SELECT DISTINCT  \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID_Y\" = \"STOK\".\"Ana_Grup\"  )"; 
					ozelgrp[0][1] = "Ana_Grup"; 
					ozelgrp[1][0] = "(SELECT DISTINCT  \"ALT_GRUP\" FROM \"ALT_GRUP_DEGISKEN\" WHERE \"ALT_GRUP_DEGISKEN\".\"ALID\" = \"STOK\".\"Alt_Grup\"  )"; 
					ozelgrp[1][1] = "Alt_Grup"; 
					ozelgrp[2][0] = "TO_CHAR(\"STOK\".\"Tarih\",'YYYY')"; 
					ozelgrp[2][1] = "Yil"; 
					ozelgrp[3][0] = "\"Birim\"";
					ozelgrp[3][1] = "Birim";
					slctString = "";
				}
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.MSSQL))
					slctString = "(SELECT DISTINCT  ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID = STOK.Ana_Grup ) as Ana_Grup  ,"  + 
							" (SELECT DISTINCT  ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID = STOK.Alt_Grup ) as Alt_Grup , "  + 
							"  datepart(yyyy,STOK.Tarih) as Yil,";
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.MYSQL))
					slctString = "(SELECT DISTINCT  ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID = STOK.Ana_Grup ) as Ana_Grup  ,"  + 
							" (SELECT DISTINCT  ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID = STOK.Alt_Grup ) as Alt_Grup , "  + 
							"  YEAR(STOK.Tarih) as Yil,";
				grupraporDTO.setAnagrp(turuString[0]);
				grupraporDTO.setAltgrp(turuString[1]);
				grupraporDTO.setUranagrp(turuString[2]);
				grupraporDTO.setUraltgrp(turuString[3]);
				String sstr_55 = deg_cevirString[1] ;
				deg_cevirString[1] = "" ;
				Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Ana_Grup" , "Alt_Grup","Yil"));
				imagrup = faturaService.ima_alt_kod(slctString,deg_cevirString[1],
						baslikbakStrings[1],deg_cevirString[0], baslikbakStrings[2], baslikbakStrings[3],  
						grupraporDTO.getUranagrp(),grupraporDTO.getUraltgrp(),
						grupraporDTO.getAnagrp(),grupraporDTO.getAltgrp(),
						grupraporDTO.getSinif1(),grupraporDTO.getSinif2(),
						grupraporDTO.getUkod1(),grupraporDTO.getUkod2(),
						grupraporDTO.getTar1(),
						grupraporDTO.getTar2(),
						baslikbakStrings[0],"Yil,Ana_Grup ,Alt_Grup",sstr_55,ozelgrp,sabitKolonlar);
				response.put("data", (imagrup != null) ? imagrup : new ArrayList<>());
				if(grupraporDTO.getBirim().equals("Tutar"))
					response.put("format",2);
				else
					response.put("format",3);
				response.put("baslik","Ana_Grup ,Alt_Grup,Yil, " + baslikbakStrings[0] + ",TOPLAM");   
				response.put("sabitkolonsayisi",3);
			}
			else if (grupraporDTO.getGruplama().equals("Alt_Grup_Yil_Ay"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				String slctString = "";
				if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
				{
					ozelgrp = new String[10][10];
					ozelgrp[0][0] = "(SELECT DISTINCT  \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID_Y\" = \"STOK\".\"Ana_Grup\"  )"; 
					ozelgrp[0][1] = "Ana_Grup"; 
					ozelgrp[1][0] = "(SELECT DISTINCT  \"ALT_GRUP\" FROM \"ALT_GRUP_DEGISKEN\" WHERE \"ALT_GRUP_DEGISKEN\".\"ALID\" = \"STOK\".\"Alt_Grup\"  )"; 
					ozelgrp[1][1] = "Alt_Grup"; 
					ozelgrp[2][0] = "TO_CHAR(\"STOK\".\"Tarih\",'YYYY / MM')"; 
					ozelgrp[2][1] = "Yil-Ay"; 
					ozelgrp[3][0] = "\"Birim\"";
					ozelgrp[3][1] = "Birim";
					slctString = "";
				}
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.MSSQL))
					slctString = "(SELECT DISTINCT  ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID = STOK.Ana_Grup ) as Ana_Grup  ,"  + 
							" (SELECT DISTINCT  ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID = STOK.Alt_Grup ) as Alt_Grup , "  + 
							" format (stok.tarih,'yyyy / MM') as Yil_Ay,";
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.MYSQL))
					slctString = "(SELECT DISTINCT  ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID = STOK.Ana_Grup ) as Ana_Grup  ,"  + 
							" (SELECT DISTINCT  ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID = STOK.Alt_Grup ) as Alt_Grup , "  + 
							" DATE_FORMAT(stok.tarih, '%Y / %m') as Yil_Ay,";
				grupraporDTO.setAnagrp(turuString[0]);
				grupraporDTO.setAltgrp(turuString[1]);
				grupraporDTO.setUranagrp(turuString[2]);
				grupraporDTO.setUraltgrp(turuString[3]);
				String sstr_55 = deg_cevirString[1] ;
				deg_cevirString[1] = "" ;
				Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Ana_Grup" , "Alt_Grup","Yil_Ay"));
				imagrup = faturaService.ima_alt_kod(slctString,deg_cevirString[1],
						baslikbakStrings[1],deg_cevirString[0], baslikbakStrings[2], baslikbakStrings[3],  
						grupraporDTO.getUranagrp(),grupraporDTO.getUraltgrp(),
						grupraporDTO.getAnagrp(),grupraporDTO.getAltgrp(),
						grupraporDTO.getSinif1(),grupraporDTO.getSinif2(),
						grupraporDTO.getUkod1(),grupraporDTO.getUkod2(),
						grupraporDTO.getTar1(),
						grupraporDTO.getTar2(),
						baslikbakStrings[0],"Yil_Ay,Ana_Grup ,Alt_Grup",sstr_55,ozelgrp,sabitKolonlar);
				response.put("data", (imagrup != null) ? imagrup : new ArrayList<>());
				if(grupraporDTO.getBirim().equals("Tutar"))
					response.put("format",2);
				else
					response.put("format",3);
				response.put("baslik","Ana_Grup ,Alt_Grup,Yil_Ay, " + baslikbakStrings[0] + ",TOPLAM");   
				response.put("sabitkolonsayisi",3);
			}
			else if (grupraporDTO.getGruplama().equals("Yil_Ay"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				String slctString = "";
				if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
				{
					ozelgrp = new String[10][10];
					ozelgrp[1][0] = " TO_CHAR(\"STOK\".\"Tarih\",'YYYY')"; 
					ozelgrp[1][1] = "Yil"; 
					ozelgrp[2][0] = " TO_CHAR(\"STOK\".\"Tarih\",'MM')"; 
					ozelgrp[2][1] = "Ay"; 
					slctString = "";
				}
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.MSSQL))
					slctString = " datepart(yyyy,STOK.Tarih) as Yil ,datepart(mm,STOK.Tarih) as Ay,";
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.MYSQL))
					slctString = "YEAR(STOK.Tarih) as Yil ,MONTH(STOK.Tarih) as Ay, ";
				grupraporDTO.setAnagrp(turuString[0]);
				grupraporDTO.setAltgrp(turuString[1]);
				grupraporDTO.setUranagrp(turuString[2]);
				grupraporDTO.setUraltgrp(turuString[3]);
				String sstr_55 = deg_cevirString[1] ;
				deg_cevirString[1] = "" ;
				Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Yil","Ay"));
				imagrup = faturaService.ima_alt_kod(slctString,deg_cevirString[1],
						baslikbakStrings[1],deg_cevirString[0], baslikbakStrings[2], baslikbakStrings[3],  
						grupraporDTO.getUranagrp(),grupraporDTO.getUraltgrp(),
						grupraporDTO.getAnagrp(),grupraporDTO.getAltgrp(),
						grupraporDTO.getSinif1(),grupraporDTO.getSinif2(),
						grupraporDTO.getUkod1(),grupraporDTO.getUkod2(),
						grupraporDTO.getTar1(),
						grupraporDTO.getTar2(),
						baslikbakStrings[0],"Yil,Ay",sstr_55,ozelgrp,sabitKolonlar);
				response.put("data", (imagrup != null) ? imagrup : new ArrayList<>());
				if(grupraporDTO.getBirim().equals("Tutar"))
					response.put("format",2);
				else
					response.put("format",3);
				response.put("baslik","Yil,Ay," + baslikbakStrings[0] + ",TOPLAM");   
				response.put("sabitkolonsayisi",2);
			}
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@PostMapping("stok/imagrp_download")
	@ResponseBody
	public ResponseEntity<byte[]> imagrpdownloadReport(@RequestBody Map<String, Object> requestBody) {
		ByteArrayDataSource dataSource ;
		try {
			List<String> header =  (List<String>) requestBody.get("headers");  
			String tableString = (String) requestBody.get("data");
			int sabitkolon = (int) requestBody.get("sabitkolon");
			List<Map<String, String>> tableData = ResultSetConverter.parseTableData(tableString, header);
			dataSource =  raporOlustur.imagrprap(tableData,sabitkolon);
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

	private String[] grup_cevir(String ana,String alt,String urana,String uralt)
	{
		String deger[] = {"","","","",""};
		String qwq1 = "", qwq2="", qwq4 = "",qwq5 = "";
		//***********************ANA GRUP
		if (ana.equals(""))
			qwq1 = " Like  '%' " ;
		else if  (ana.equals("Bos Olanlar"))
			qwq1 = " = '' " ;
		else
		{
			String anas = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", ana);
			qwq1 = "=" + anas;
		}
		deger[0] = qwq1; 
		//***********************ALT GRUP
		if (alt.equals(""))
			qwq2 = " Like  '%' " ;
		else if  (alt.equals("Bos Olanlar"))
			qwq2 = " = '' " ;
		else
		{
			String alts = faturaService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", alt);
			qwq2 ="=" + alts;
		}
		deger[1] = qwq2; 
		if (urana.equals(""))
			qwq4 = " Like  '%' " ;
		else if  (urana.equals("Bos Olanlar"))
			qwq4 = " = '' " ;
		else
		{
			String anas = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", urana);
			qwq4 = "=" + anas;
		}
		deger[2] = qwq4; 
		//*********************** URUN ALT GRUP
		if (uralt.equals(""))
			qwq5 = " Like  '%' " ;
		else if  (uralt.equals("Bos Olanlar"))
			qwq5 = " = '' " ;
		else
		{
			String alts = faturaService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", uralt);
			qwq5 ="=" + alts;
		}
		deger[3] = qwq5; 
		return deger;
	}

	private String[]  baslik_bak(grupraporDTO grupraporDTO)
	{
		String[] baslikbakStrings = {"","","",""};
		try {
			List<Map<String, Object>> baslik = new ArrayList<>();
			String turuString[] =  grup_cevir(grupraporDTO.getAnagrp(),grupraporDTO.getAltgrp() ,grupraporDTO.getUranagrp(),grupraporDTO.getUraltgrp());
			grupraporDTO.setAnagrp(turuString[0]);
			grupraporDTO.setAltgrp(turuString[1]);
			grupraporDTO.setUranagrp(turuString[2]);
			grupraporDTO.setUraltgrp(turuString[3]);
			String jkj  = "" ;
			String ch1 = "" ;
			String sstr_2 = "" ;
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
			{
				if (grupraporDTO.getTuru().equals("CIKAN"))
					jkj = " \"STOK\".\"Hareket\" = 'C'  " ;
				else
					jkj = " \"STOK\".\"Hareket\" = 'G'  " ;
				ch1 = " \"Evrak_Cins\" = 'URE' " ;
			}
			else {
				if (grupraporDTO.getTuru().equals("CIKAN"))
					jkj = " STOK.Hareket = 'C' " ;
				else
					jkj = " STOK.Hareket = 'G' " ;
				ch1 = " Evrak_Cins = 'URE' " ;
			}
			if (grupraporDTO.getStunlar().equals("Yil"))
			{
				if(fatConnDetails.getSqlTipi().equals(sqlTipi.MSSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT datepart(yyyy,STOK.Tarih)",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2(),
							"order by datepart(yyyy,STOK.Tarih)");
					sstr_2 =" datepart(yyyy,STOK.Tarih)" ;
				}
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.MYSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT YEAR(STOK.Tarih)",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2(),
							"order by YEAR(STOK.Tarih)");
					sstr_2 =" YEAR(STOK.Tarih)" ;
				}
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT TO_CHAR(\"STOK\".\"Tarih\",'YYYY')",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2(),"order by TO_CHAR(\"STOK\".\"Tarih\",'YYYY')");
					sstr_2 =" TO_CHAR(\"STOK\".\"Tarih\",'YYYY')" ;
				}
			}
			else if (grupraporDTO.getStunlar().equals("Ay"))
			{
				if(fatConnDetails.getSqlTipi().equals(sqlTipi.MSSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT datepart(mm,STOK.Tarih)",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2(),"order by datepart(mm,STOK.Tarih)");
					sstr_2 = "datepart(mm,STOK.Tarih)";
				}
				else if(fatConnDetails.getSqlTipi().equals( sqlTipi.MYSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT MONTH(STOK.Tarih)",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2(),"order by MONTH(STOK.Tarih)");
					sstr_2 = " MONTH(STOK.Tarih)";
				}
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT TO_CHAR(\"STOK\".\"Tarih\",'MM')",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2(),"order by TO_CHAR(\"STOK\".\"Tarih\",'MM')");
					sstr_2 =" TO_CHAR(\"STOK\".\"Tarih\",'MM')" ;
				}
			}
			else if (grupraporDTO.getStunlar().equals("Gun"))
			{
				if(fatConnDetails.getSqlTipi().equals(sqlTipi.MSSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT datepart(dd,STOK.Tarih)",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2(),"order by datepart(dd,STOK.Tarih)");
					sstr_2 = "datepart(dd,STOK.Tarih)";
				}
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.MYSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT DAY(STOK.Tarih)",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2()," order by DAY(STOK.Tarih)");
					sstr_2 = "DAY(STOK.Tarih)";
				}
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT TO_CHAR(\"STOK\".\"Tarih\",'DD')",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2()," order by TO_CHAR(\"STOK\".\"Tarih\",'DD')");
					sstr_2 =" TO_CHAR(\"STOK\".\"Tarih\",'DD')" ;
				}
			}
			else if (grupraporDTO.getStunlar().equals("Depo"))
			{
				if(fatConnDetails.getSqlTipi().equals(sqlTipi.MSSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT ISNULL((SELECT DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID = STOK.Depo),'---') as Depo",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2()," order by Depo ");
					sstr_2 = " ISNULL((SELECT DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y= STOK.Depo) ,'---') ";
				}
				else  if(fatConnDetails.getSqlTipi().equals(sqlTipi.MYSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT IFNULL((SELECT DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID = STOK.Depo),'---') as Depo",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2()," order by Depo ");
					sstr_2 = " IFNULL((SELECT DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y= STOK.Depo) ,'---') ";
				}
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT COALESCE((SELECT \"DEPO\" FROM \"DEPO_DEGISKEN\" WHERE \"DEPO_DEGISKEN\".\"DPID\" = \"STOK\".\"Depo\"),'---') as \"Depo\"",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2(),"order by \"Depo\"");
					sstr_2 =" COALESCE((SELECT \"DEPO\" FROM \"DEPO_DEGISKEN\" WHERE \"DEPO_DEGISKEN\".\"DPID\" = \"STOK\".\"Depo\"),'---')" ;
				}
			}
			else if (grupraporDTO.getStunlar().equals("Ana Grup"))
			{
				if(fatConnDetails.getSqlTipi().equals(sqlTipi.MSSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT ISNULL((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = STOK.Ana_Grup),'---') as Ana_Grup",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2()," order by Ana_Grup  ");
					sstr_2 = "  ISNULL((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = STOK.Ana_Grup),'---')  ";
				}
				else  if(fatConnDetails.getSqlTipi().equals(sqlTipi.MYSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT IFNULL((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = STOK.Ana_Grup),'---') as Ana_Grup",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2()," order by Ana_Grup  ");
					sstr_2 = "  IFNULL((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = STOK.Ana_Grup),'---')  ";
				}
				else if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
				{
					baslik =  faturaService.ima_baslik_bak("DISTINCT COALESCE((SELECT \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID_Y\" = \"STOK\".\"Ana_Grup\"),'---') as \"Ana_Grup\"",jkj,ch1,
							turuString[2],turuString[3],turuString[0],turuString[1],
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getTar1(),
							grupraporDTO.getTar2(),	"order by \"Ana_Grup\"");
					sstr_2 =" COALESCE((SELECT \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID_Y\" = \"STOK\".\"Ana_Grup\"),'---') " ;
				}
			}
			String sstr_1 = "";
			StringBuilder text = new StringBuilder();
			for (Map<String, Object> row : baslik)
				row.forEach((key, value) -> text.append("[").append(value).append("],"));
			sstr_1 = text.length() > 0 ? text.substring(0, text.length() - 1) : "";
			baslikbakStrings[0] = sstr_1;
			baslikbakStrings[1] = sstr_2;
			baslikbakStrings[2] = jkj;
			baslikbakStrings[3] = ch1;
		}
		catch (Exception ex)
		{
			throw new ServiceException(ex.getMessage());
		}
		return baslikbakStrings;
	}

	private String[] deg_cevir(grupraporDTO grupraporDTO )
	{
		String[] deg_cevirString = {"",""};
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
		String sstr_4 = "" , sstr_5 = "";
		if (grupraporDTO.getBirim().equals("Tutar"))
		{
			sstr_4 = " ABS(STOK.Tutar) as Tutar" ;
			sstr_5 = "Tutar" ;
			if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
			{
				sstr_4 = " CASE WHEN SUM(ABS(\"STOK\".\"Tutar\")) = 0 THEN null else SUM(ABS(\"STOK\".\"Tutar\")) end " ;
				sstr_5 = "\"Tutar\"" ;
			}
		}
		else  if (grupraporDTO.getBirim().equals("Miktar"))
		{
			sstr_4 = " ABS(STOK.Miktar) as Miktar" ;
			sstr_5 = "Miktar";
			if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
			{
				sstr_4 =  " CASE WHEN SUM(ABS(\"STOK\".\"Miktar\")) = 0 THEN null else SUM(ABS(\"STOK\".\"Miktar\")) end " ;
				sstr_5 = "\"Miktar\"" ;
			}
		}
		else  if (grupraporDTO.getBirim().equals("Agirlik"))
		{
			sstr_4 = " (ABS(STOK.Miktar) * MAL.Agirlik)  as Agirlik" ;
			sstr_5 = "Agirlik";
			if(fatConnDetails.getSqlTipi().equals(sqlTipi.PGSQL))
			{
				sstr_4 = " CASE WHEN  SUM(ABS(\"STOK\".\"Miktar\" * \"MAL\".\"Agirlik\")) = 0 THEN null ELSE  SUM(ABS(\"STOK\".\"Miktar\" * \"MAL\".\"Agirlik\")) end" ;    //" SUM(ABS(\"STOK\".\"Miktar\" * \"MAL\".\"Agirlik\"))"
				sstr_5 = "\"Agirlik\"" ;
			}
		}
		deg_cevirString[0] = sstr_4 ;
		deg_cevirString[1] = sstr_5 ;
		return deg_cevirString;
	}
}
