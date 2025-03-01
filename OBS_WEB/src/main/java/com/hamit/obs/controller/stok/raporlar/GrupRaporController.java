package com.hamit.obs.controller.stok.raporlar;

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
import com.hamit.obs.dto.stok.raporlar.grupraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.fatura.FaturaService;


@Controller
public class GrupRaporController {

	@Autowired
	private RaporOlustur raporOlustur;

	@Autowired
	private FaturaService faturaService;

	@GetMapping("/stok/grprapor")
	public String grprapor() {
		return "stok/raporlar/gruprapor";
	}

	@PostMapping("stok/grpdoldur")
	@ResponseBody
	public Map<String, Object> grpdoldur(@RequestBody grupraporDTO grupraporDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			String turuString[] =  grup_cevir(grupraporDTO.getUranagrp(),grupraporDTO.getUraltgrp(),grupraporDTO.getUrozkod());
			grupraporDTO.setUranagrp(turuString[0]);
			grupraporDTO.setUraltgrp(turuString[1]);
			grupraporDTO.setUrozkod(turuString[2]);

			String[] baslikbakStrings = {"",""};
			String[] deg_cevirString = {"","","","","",""};
			String ozelgrp[][] = new String[7][2];
			
			if (grupraporDTO.getGruplama().equals("Urun Kodu"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				if (! baslikbakStrings[0].equals(""))
				{
					if(fatConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"MAL\".\"Kodu\""; 
						ozelgrp[0][1] = "Urun_Kodu"; 
						ozelgrp[1][0] = "\"MAL\".\"Adi\""; 
						ozelgrp[1][1] = "Urun_Adi";
						ozelgrp[2][0] = "\"Birim\"" ; 
						ozelgrp[2][1] = "Birim";
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Urun_Kodu", "Urun_Adi", "Birim")); // ✅ Sabit kolonları ekle
					List<Map<String, Object>> grup = faturaService.grp_urn_kodlu(
							grupraporDTO,baslikbakStrings[1],deg_cevirString[3], 
							deg_cevirString[5], deg_cevirString[0], deg_cevirString[2],deg_cevirString[1],
							deg_cevirString[4], baslikbakStrings[0],ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(grupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else
						response.put("format",3);
					response.put("baslik","Urun_Kodu, Urun_Adi , Birim , " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",3);
				}
			}
			else if (grupraporDTO.getGruplama().equals("Urun Kodu-Yil"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				if (! baslikbakStrings[0].equals(""))
				{
					if(fatConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"MAL\".\"Kodu\""; 
						ozelgrp[0][1] = "Urun_Kodu"; 
						ozelgrp[1][0] = "\"MAL\".\"Adi\""; 
						ozelgrp[1][1] = "Urun_Adi";
						ozelgrp[2][0] = "\"Birim\"" ; 
						ozelgrp[2][1] = "Birim";
						ozelgrp[3][0] = "TO_CHAR(\"STOK\".\"Tarih\",'YYYY')" ; 
						ozelgrp[3][1] = "Yil";
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Urun_Kodu", "Urun_Adi", "Birim","Yil"));

					List<Map<String, Object>> grup = faturaService.grp_urn_kodlu_yil(
							grupraporDTO,baslikbakStrings[1],deg_cevirString[3], 
							deg_cevirString[5], deg_cevirString[0], deg_cevirString[2],deg_cevirString[1],
							deg_cevirString[4], baslikbakStrings[0],ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(grupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else
						response.put("format",3);
					response.put("baslik","Urun_Kodu,Urun_Adi,Birim,Yil, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",4);
				}
			}
			else if (grupraporDTO.getGruplama().equals("Hesap Kodu"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				if (! baslikbakStrings[0].equals(""))
				{
					if(fatConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"Hesap_Kodu\""; 
						ozelgrp[0][1] = "Musteri_Kodu"; 
						String carServer = "dbname = ok_car" + cariConnDetails.getDatabaseName() + " port = " + Global_Yardimci.ipCevir(cariConnDetails.getServerIp())[1] + " host = localhost user = " + cariConnDetails.getUsername() +" password = " + cariConnDetails.getPassword() +"" ; 
						String adrString ="(SELECT \"UNVAN\" FROM  dblink ('"+ carServer + "', " + 
								" 'SELECT \"UNVAN\" ,\"HESAP\" FROM \"HESAP\" ') " + 
								" AS adr(\"UNVAN\" character varying,\"HESAP\" character varying) "+
								" WHERE \"HESAP\" = \"STOK\".\"Hesap_Kodu\"  )";
						ozelgrp[1][0] = adrString; 
						ozelgrp[1][1] = "Unvan"; 
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Hesap_Kodu", "Unvan"));
					List<Map<String, Object>> grup = faturaService.grp_mus_kodlu(
							grupraporDTO,baslikbakStrings[1],deg_cevirString[3], 
							deg_cevirString[5], deg_cevirString[0], deg_cevirString[2],deg_cevirString[1],
							deg_cevirString[4], baslikbakStrings[0],ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(grupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else
						response.put("format",3);
					response.put("baslik","Hesap_Kodu,Unvan, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",2);
				}
			}
			else if (grupraporDTO.getGruplama().equals("Hesap Kodu-Yil"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				if (! baslikbakStrings[0].equals(""))
				{
					if(fatConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = "\"Hesap_Kodu\""; 
						ozelgrp[0][1] = "Musteri_Kodu"; 
						String carServer = "dbname = ok_car" + cariConnDetails.getDatabaseName() + " port = " + Global_Yardimci.ipCevir(cariConnDetails.getServerIp())[1] + " host = localhost user = " + cariConnDetails.getUsername() +" password = " + cariConnDetails.getPassword() +"" ; 
						String adrString ="(SELECT \"UNVAN\" FROM  dblink ('"+ carServer + "', " + 
								" 'SELECT \"UNVAN\" ,\"HESAP\" FROM \"HESAP\" ') " + 
								" AS adr(\"UNVAN\" character varying,\"HESAP\" character varying) "+
								" WHERE \"HESAP\" = \"STOK\".\"Hesap_Kodu\"  )";
						ozelgrp[1][0] = adrString; 
						ozelgrp[1][1] = "Unvan"; 
						ozelgrp[2][0] = "TO_CHAR(\"STOK\".\"Tarih\",'YYYY')" ; 
						ozelgrp[2][1] = "Yil";
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Musteri_Kodu","Unvan","Yil"));
					List<Map<String, Object>> grup = faturaService.grp_mus_kodlu_yil(
							grupraporDTO,baslikbakStrings[1],deg_cevirString[3], 
							deg_cevirString[5], deg_cevirString[0], deg_cevirString[2],deg_cevirString[1],
							deg_cevirString[4], baslikbakStrings[0],ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(grupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else
						response.put("format",3);
					response.put("baslik","Musteri_Kodu,Unvan,Yil, " + baslikbakStrings[0] + ",TOPLAM");    
					response.put("sabitkolonsayisi",3);
				}
			}
			else if (grupraporDTO.getGruplama().equals("Yil_Ay"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				if (! baslikbakStrings[0].equals(""))
				{
					if(fatConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = " TO_CHAR(\"STOK\".\"Tarih\",'YYYY')" ;
						ozelgrp[0][1] = "Yil"; 
						ozelgrp[1][0] = "TO_CHAR(\"STOK\".\"Tarih\",'MM')"; 
						ozelgrp[1][1] = "Ay"; 
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Yil","Ay"));
					List<Map<String, Object>> grup = faturaService.grp_yil_ay(
							grupraporDTO,baslikbakStrings[1],deg_cevirString[3], 
							deg_cevirString[5], deg_cevirString[0], deg_cevirString[2],deg_cevirString[1],
							deg_cevirString[4], baslikbakStrings[0],ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(grupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else
						response.put("format",3);
					response.put("baslik","Yil,Ay, " + baslikbakStrings[0] + ",TOPLAM");   
					response.put("sabitkolonsayisi",2);
				}
			}
			else if (grupraporDTO.getGruplama().equals("Yil"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				if (! baslikbakStrings[0].equals(""))
				{
					if(fatConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = " TO_CHAR(\"STOK\".\"Tarih\",'YYYY')" ;
						ozelgrp[0][1] = "Yil"; 
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Yil"));
					List<Map<String, Object>> grup = faturaService.grp_yil(
							grupraporDTO,baslikbakStrings[1],deg_cevirString[3], 
							deg_cevirString[5], deg_cevirString[0], deg_cevirString[2],deg_cevirString[1],
							deg_cevirString[4], baslikbakStrings[0],ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(grupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else
						response.put("format",3);
					response.put("baslik","Yil, " + baslikbakStrings[0] + ",TOPLAM");  
					response.put("sabitkolonsayisi",1);
				}
			}
			else if (grupraporDTO.getGruplama().equals("Urun_Ana_Grup"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				if (! baslikbakStrings[0].equals(""))
				{
					if(fatConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = " (SELECT DISTINCT  \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID_Y\" = \"MAL\".\"Ana_Grup\" )" ;
						ozelgrp[0][1] = "Ana_Grup"; 
						ozelgrp[1][0] = " (SELECT DISTINCT  \"ALT_GRUP\" FROM \"ALT_GRUP_DEGISKEN\" WHERE \"ALT_GRUP_DEGISKEN\".\"ALID_Y\" = \"MAL\".\"Alt_Grup\" )" ;
						ozelgrp[1][1] = "Alt_Grup"; 
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Ana_Grup","Alt_Grup"));
					List<Map<String, Object>> grup = faturaService.grp_ana_grup(
							grupraporDTO,baslikbakStrings[1],deg_cevirString[3], 
							deg_cevirString[5], deg_cevirString[0], deg_cevirString[2],deg_cevirString[1],
							deg_cevirString[4], baslikbakStrings[0],ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(grupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else
						response.put("format",3);
					response.put("baslik","Ana_Grup,Alt_Grup, " + baslikbakStrings[0] + ",TOPLAM");    
					response.put("sabitkolonsayisi",2);
				}
			}
			else if (grupraporDTO.getGruplama().equals("Urun_Ana_Grup_Yil"))
			{
				baslikbakStrings = baslik_bak(grupraporDTO);
				deg_cevirString = deg_cevir(grupraporDTO);
				if (! baslikbakStrings[0].equals(""))
				{
					if(fatConnDetails.getHangisql().equals("PG SQL"))
					{
						ozelgrp = new String[7][2];
						ozelgrp[0][0] = " (SELECT DISTINCT  \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID_Y\" = \"MAL\".\"Ana_Grup\" )" ;
						ozelgrp[0][1] = "Ana_Grup"; 
						ozelgrp[1][0] = " (SELECT DISTINCT  \"ALT_GRUP\" FROM \"ALT_GRUP_DEGISKEN\" WHERE \"ALT_GRUP_DEGISKEN\".\"ALID_Y\" = \"MAL\".\"Alt_Grup\" )" ;
						ozelgrp[1][1] = "Alt_Grup"; 
						ozelgrp[2][0] = " TO_CHAR(\"STOK\".\"Tarih\",'YYYY')" ;
						ozelgrp[2][1] = "Yil"; 
					}
					Set<String> sabitKolonlar = new HashSet<>(Arrays.asList("Ana_Grup","Alt_Grup","Yil"));
					List<Map<String, Object>> grup = faturaService.grp_ana_grup_yil(
							grupraporDTO,baslikbakStrings[1],deg_cevirString[3], 
							deg_cevirString[5], deg_cevirString[0], deg_cevirString[2],deg_cevirString[1],
							deg_cevirString[4], baslikbakStrings[0],ozelgrp,sabitKolonlar);
					response.put("data", (grup != null) ? grup : new ArrayList<>());
					if(grupraporDTO.getBirim().equals("Tutar"))
						response.put("format",2);
					else
						response.put("format",3);
					response.put("baslik","Ana_Grup,Alt_Grup,Yil, " + baslikbakStrings[0] + ",TOPLAM");    
					response.put("sabitkolonsayisi",3);
				}
			}
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@PostMapping("stok/grp_download")
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

	private String[] baslik_bak(grupraporDTO grupraporDTO)
	{
		String[] baslikbakStrings = {"",""};
		try {
			List<Map<String, Object>> baslik = new ArrayList<>();
			String jkj  = "" ;
			String ch1 = "" ;
			String sstr_2 = "" ;
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			if(fatConnDetails.getHangisql().equals("PG SQL"))
			{
				if (grupraporDTO.getTuru().equals("CIKAN"))
					jkj = " \"STOK\".\"Hareket\" = 'C'  " ;
				else
					jkj = " \"STOK\".\"Hareket\" = 'G'  " ;
				ch1 = " \"Evrak_Cins\" = 'FAT' " ;
			}
			else {
				if (grupraporDTO.getTuru().equals("CIKAN"))
					jkj = " STOK.Hareket = 'C' " ;
				else
					jkj = " STOK.Hareket = 'G' " ;
				ch1 = " Evrak_Cins = 'FAT' " ;
			}

			if (grupraporDTO.getStunlar().equals("Yil"))
			{
				if(fatConnDetails.getHangisql().equals("MS SQL"))
				{
					baslik = faturaService.baslik_bak("DISTINCT datepart(yyyy,STOK.Tarih)","order by datepart(yyyy,STOK.Tarih)",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 = " datepart(yyyy,STOK.Tarih)" ;
				}
				else if(fatConnDetails.getHangisql().equals("MY SQL")) {
					baslik = faturaService.baslik_bak("DISTINCT YEAR(STOK.Tarih)", "order by YEAR(STOK.Tarih)",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 = " YEAR(STOK.Tarih)" ;
				}
				else if(fatConnDetails.getHangisql().equals("PG SQL"))
				{
					baslik = faturaService.baslik_bak("DISTINCT TO_CHAR(\"STOK\".\"Tarih\",'YYYY') ","order by TO_CHAR(\"STOK\".\"Tarih\",'YYYY')",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 =" TO_CHAR(\"STOK\".\"Tarih\",'YYYY')" ;
				}
			}
			else if (grupraporDTO.getStunlar().equals("Ay"))
			{
				if(fatConnDetails.getHangisql().equals("MS SQL"))
				{
					baslik = faturaService.baslik_bak("DISTINCT datepart(mm,STOK.Tarih)", "order by datepart(mm,STOK.Tarih)",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 = "datepart(mm,STOK.Tarih)" ;
				}
				else if(fatConnDetails.getHangisql().equals("MY SQL"))
				{
					baslik = faturaService.baslik_bak("DISTINCT MONTH(STOK.Tarih)", "order by MONTH(STOK.Tarih)",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 = "MONTH(STOK.Tarih)" ;
				}
				else if(fatConnDetails.getHangisql().equals("PG SQL"))
				{
					baslik = faturaService.baslik_bak("DISTINCT TO_CHAR(\"STOK\".\"Tarih\",'MM') ","order by TO_CHAR(\"STOK\".\"Tarih\",'MM')",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 =" TO_CHAR(\"STOK\".\"Tarih\",'MM')" ;
				}
			}
			else if (grupraporDTO.getStunlar().equals("Gun"))
			{
				if(fatConnDetails.getHangisql().equals("MS SQL"))
				{
					baslik = faturaService.baslik_bak("DISTINCT datepart(dd,STOK.Tarih)", "order by datepart(dd,STOK.Tarih)",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 = "datepart(dd,STOK.Tarih)" ;
				}
				else  if(fatConnDetails.getHangisql().equals("MY SQL"))
				{
					baslik = faturaService.baslik_bak("DISTINCT DAY(STOK.Tarih)", "order by DAY(STOK.Tarih)",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 = "DAY(STOK.Tarih)" ;
				}
				else if(fatConnDetails.getHangisql().equals("PG SQL"))
				{
					baslik = faturaService.baslik_bak("DISTINCT TO_CHAR(\"STOK\".\"Tarih\",'DD') ","order by TO_CHAR(\"STOK\".\"Tarih\",'DD')",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 =" TO_CHAR(\"STOK\".\"Tarih\",'DD')" ;
				}
			}
			else if (grupraporDTO.getStunlar().equals("Ana Grup"))
			{
				String ifnul = "";
				if(fatConnDetails.getHangisql().equals("MS SQL"))
					ifnul = "ISNULL";
				else if(fatConnDetails.getHangisql().equals("MY SQL"))
					ifnul = "IFNULL";
				if(fatConnDetails.getHangisql().equals("PG SQL"))
				{
					ifnul = "COALESCE";
					baslik = faturaService.baslik_bak("DISTINCT " + ifnul + "((SELECT \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID\" = \"STOK\".\"Ana_Grup\"),'---')  as \"ANA_GRUP\" ", "order by  \"ANA_GRUP\" ",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 =  ifnul + "((SELECT \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID\" = \"STOK\".\"Ana_Grup\"),'---')  " ;
				}
				else {
					baslik = faturaService.baslik_bak("DISTINCT " + ifnul + "((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID = STOK.Ana_Grup),'---') as Ana_Grup", "order by Ana_Grup",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 =  ifnul + "((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID = STOK.Ana_Grup),'---')  " ;
				}
			}
			else if (grupraporDTO.getStunlar().equals("Urun Ana Grup"))
			{
				String ifnul = "";
				if(fatConnDetails.getHangisql().equals("MS SQL"))
					ifnul = "ISNULL";
				else if(fatConnDetails.getHangisql().equals("MY SQL"))
					ifnul = "IFNULL";
				if(fatConnDetails.getHangisql().equals("PG SQL"))
				{
					ifnul = "COALESCE";
					baslik = faturaService.baslik_bak("DISTINCT "+ ifnul +"((SELECT \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID\" = \"MAL\".\"Ana_Grup\"),'---')  as \"ANA_GRUP\" ", "order by  \"ANA_GRUP\" ",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 =  ifnul + "((SELECT \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID\" = \"MAL\".\"Ana_Grup\"),'---')  " ;
				}
				else {
					baslik = faturaService.baslik_bak("DISTINCT "+ ifnul +"((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID = MAL.Ana_Grup),'---') as Ana_Grup", "order by Ana_Grup",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 =  ifnul + "((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID = MAL.Ana_Grup),'---')  " ;
				}
			}
			else if (grupraporDTO.getStunlar().equals("Hesap Kodu"))
			{
				if(fatConnDetails.getHangisql().equals("PG SQL"))
				{
					baslik = faturaService.baslik_bak("DISTINCT Case when \"Hesap_Kodu\" = '' then '---'	else \"Hesap_Kodu\" end as \"Hesap_Kodu\"", "order by \"Hesap_Kodu\"",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());

					sstr_2 = "Case when \"Hesap_Kodu\" = '' then '---'	else \"Hesap_Kodu\" end" ;
				}
				else {
					baslik = faturaService.baslik_bak("DISTINCT CASE WHEN  Hesap_Kodu ='' THEN '---' else  Hesap_Kodu END  as Hesap_Kodu  ", "order by Hesap_Kodu",jkj,ch1,
							grupraporDTO.getUkod1(),grupraporDTO.getUkod2() ,
							grupraporDTO.getCkod1(),grupraporDTO.getCkod2() ,
							grupraporDTO.getTar1(),grupraporDTO.getTar2());
					sstr_2 = "  CASE WHEN  Hesap_Kodu ='' THEN '---' else  Hesap_Kodu END " ;
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

	private String[] grup_cevir(String urana,String uralt, String oz1)
	{
		String deger[] = {"","",""};
		String qwq1 = "", qwq2="", qwq3="";
		//***********************ANA GRUP
		if (urana.equals(""))
			qwq1 = " Like  '%' " ;
		else if  (urana.equals("Bos Olanlar"))
			qwq1 = " = '' " ;
		else
		{
			String anas = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", urana);
			qwq1 = "=" + anas;
		}
		deger[0] = qwq1; 
		//***********************ALT GRUP
		if (uralt.equals(""))
			qwq2 = " Like  '%' " ;
		else if  (uralt.equals("Bos Olanlar"))
			qwq2 = " = '' " ;
		else
		{
			String alts = faturaService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", uralt);
			qwq2 ="=" + alts;
		}
		deger[1] = qwq2; 
		//***********************oz1
		if (oz1.equals(""))
			qwq3 = " Like  '%' " ;
		else if  (oz1.equals("Bos Olanlar"))
			qwq3 = " = '' " ;
		else
		{
			String ozs1 = faturaService.urun_kod_degisken_ara("OZ1ID_Y", "OZEL_KOD_1", "OZ_KOD_1_DEGISKEN", oz1);
			qwq3 = "=" + ozs1;
		}
		deger[2] = qwq3; 

		return deger;
	}

	private String[] deg_cevir(grupraporDTO grupraporDTO )
	{
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
		ConnectionDetails kurConnDetails =  UserSessionManager.getUserSession(useremail, "Kur");

		String[] deg_cevirString = {"","","","","",""};
		String harekString = "" ;
		String jkj = "" ,jkj1 = "" ,ch1 = "",sstr_4 = "" , sstr_5 = "" , kur_dos = "" ;

		if (grupraporDTO.getTuru().equals("CIKAN"))
			harekString = "C";
		else
			harekString = "G";
		if (grupraporDTO.isIstenenaychc())
		{
			if(fatConnDetails.getHangisql().equals("MS SQL"))
				jkj = " datepart(mm,STOK.Tarih) like '" + grupraporDTO.getIstenenay() + "'  AND STOK.Hareket = '" + harekString + "' " ;
			else if(fatConnDetails.getHangisql().equals("MY SQL"))
				jkj = " MONTH(STOK.Tarih) like '" + grupraporDTO.getIstenenay() + "'  AND STOK.Hareket = '" + harekString + "' " ;
			else if(fatConnDetails.getHangisql().equals("PG SQL"))
			{
				String ay = grupraporDTO.getIstenenay() ;
				if(grupraporDTO.getIstenenay().length() == 1)
					ay = "0" + grupraporDTO.getIstenenay();
				jkj = " TO_CHAR(\"STOK\".\"Tarih\",'MM')::text like '" + ay + "'  AND \"STOK\".\"Hareket\" = '" + harekString + "' " ;
			}
		}
		else
		{
			if(fatConnDetails.getHangisql().equals("PG SQL"))
				jkj = " \"STOK\".\"Hareket\" = '" + harekString + "' " ;
			else
				jkj = " STOK.Hareket = '" + harekString + "' " ;
		}
		if(fatConnDetails.getHangisql().equals("PG SQL"))
		{
			jkj1 = " \"STOK\".\"Hareket\" = '" + harekString + "' " ;
			ch1 = " \"Evrak_Cins\" = 'FAT' " ;
		}
		else
		{
			jkj1 = " Fatura.Gir_Cik = '" + harekString + "' " ;
			ch1 = " Evrak_Cins = 'FAT' " ;
		}
		if (grupraporDTO.getBirim().equals("Tutar"))
		{
			if (grupraporDTO.isDvzcevirchc())
			{
				if(fatConnDetails.getHangisql().equals("MS SQL"))
					sstr_4 = " ABS(STOK.Tutar / iif(k." + grupraporDTO.getDvzturu() + " = 0 ,1, k." + grupraporDTO.getDvzturu() + ")) as Tutar ";
				else if (fatConnDetails.getHangisql().equals("MY SQL"))
					sstr_4 = " ABS(STOK.Tutar / IF(k." + grupraporDTO.getDvzturu() + " = 0 ,1, k." + grupraporDTO.getDvzturu() + ")) as Tutar ";
				else if(fatConnDetails.getHangisql().equals("PG SQL"))
				{
					String kurcString = grupraporDTO.getDvzturu();
					sstr_4 = "SUM(ABS(\"STOK\".\"Tutar\" / tr.\"" + kurcString + "\"))::DOUBLE PRECISION ";
				}
			}
			else
			{
				if(fatConnDetails.getHangisql().equals("PG SQL"))
					sstr_4 = " SUM(ABS(\"STOK\".\"Tutar\")) ";
				else
					sstr_4 = " ABS(STOK.Tutar) as Tutar";
			}
			sstr_5 = "Tutar";
		}
		else  if (grupraporDTO.getBirim().equals("Miktar"))
		{
			sstr_4 = " ABS(STOK.Miktar) as Miktar";
			sstr_5 = "Miktar";
			if(fatConnDetails.getHangisql().equals("PG SQL"))
			{
				sstr_4 =  " CASE WHEN SUM(ABS(\"STOK\".\"Miktar\")) = 0 THEN null else SUM(ABS(\"STOK\".\"Miktar\")) end " ;
				sstr_5 = "\"Miktar\"" ;
			}
		}
		else  if (grupraporDTO.getBirim().equals("Agirlik"))
		{
			sstr_4 = " (ABS(STOK.Miktar) * MAL.Agirlik)  as Agirlik";
			sstr_5 = "Agirlik";
			if(fatConnDetails.getHangisql().equals("PG SQL"))
			{
				sstr_4 = " CASE WHEN  SUM(ABS(\"STOK\".\"Miktar\" * \"MAL\".\"Agirlik\")) = 0 THEN null ELSE  SUM(ABS(\"STOK\".\"Miktar\" * \"MAL\".\"Agirlik\")) end" ;    //" SUM(ABS(\"STOK\".\"Miktar\" * \"MAL\".\"Agirlik\"))"
				sstr_5 = "\"Agirlik\"" ;
			}
		}
		if (grupraporDTO.isDvzcevirchc())
		{
			if (grupraporDTO.getBirim().equals("Tutar"))
			{
				if(fatConnDetails.getHangisql().equals("MS SQL"))
					kur_dos = "  left outer join OK_Kur" + kurConnDetails.getDatabaseName() + ".dbo.kurlar k on k.Tarih = convert(varchar(10), STOK.Tarih, 120) and (k.kur IS NULL OR k.KUR ='" + grupraporDTO.getDoviz() + "') ";
				else if(fatConnDetails.getHangisql().equals("MY SQL"))
					kur_dos = "  left outer join ok_kur" + kurConnDetails.getDatabaseName() + ".kurlar k on k.Tarih = DATE( STOK.Tarih) and  k.kur ='" + grupraporDTO.getDoviz() + "' ";
				else if(fatConnDetails.getHangisql().equals("PG SQL"))
				{
					String kurServer = "" ; 
					String[] ipogren = Global_Yardimci.ipCevir(kurConnDetails.getServerIp());
					if (fatConnDetails.getServerIp().equals(kurConnDetails.getServerIp()))
						kurServer = "dbname = ok_kur" + kurConnDetails.getDatabaseName() + " port = " + ipogren[1] + " host = localhost user = " + kurConnDetails.getUsername() + " password = " + kurConnDetails.getPassword() +"" ; 
					else
						kurServer = "dbname = ok_kur" + kurConnDetails.getDatabaseName() + " port = " + ipogren[1] + " host = " +   ipogren[0] + " user = " + kurConnDetails.getUsername() + " password = " + kurConnDetails.getPassword() +"" ; 
					String kurcString = grupraporDTO.getDvzturu();
					String kurcesitString = grupraporDTO.getDoviz();
					kur_dos = " left join  (SELECT * FROM  dblink ('" + kurServer + "'," + 
							" 'SELECT \"TARIH\", \"" + kurcString + "\",\"KUR\" FROM \"KURLAR\" WHERE \"KUR\"= ''" + kurcesitString +"''')  AS kur " +
							" (\"TARIH\" timestamp,\"" + kurcString + "\" DOUBLE PRECISION,\"KUR\" character varying) " +
							" ) as tr on DATE(\"STOK\".\"Tarih\") = DATE(tr.\"TARIH\") " ;
				}
			}
			else
				kur_dos = "";
		}
		else
			kur_dos = "" ;
		deg_cevirString[0] = jkj ;
		deg_cevirString[1] = jkj1 ;
		deg_cevirString[2] = ch1 ;
		deg_cevirString[3] = sstr_4 ;
		deg_cevirString[4] = sstr_5 ;
		deg_cevirString[5] = kur_dos ;
		return deg_cevirString;
	}
}