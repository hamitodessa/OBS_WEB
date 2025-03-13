package com.hamit.obs.controller.kereste.raporlar;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.hamit.obs.dto.kereste.kergrupraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.kereste.KeresteService;

@Controller
public class KerOrtFiatController {

	@Autowired
	private RaporOlustur raporOlustur;
	
	@Autowired
	private KeresteService keresteService;
	
	@GetMapping("/kereste/ortfiat")
	public String fatrapor() {
		return "kereste/ortfiat";
	}
	
	@PostMapping("kereste/ortfiatdoldur")
	@ResponseBody
	public Map<String, Object> grpdoldur(@RequestBody kergrupraporDTO kergrupraporDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kerConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			String turuString[] =  grup_cevir(kergrupraporDTO.getAnagrp(),kergrupraporDTO.getAltgrp(),kergrupraporDTO.getOzkod());

			kergrupraporDTO.setAnagrp(turuString[0]);
			kergrupraporDTO.setAltgrp(turuString[1]);
			kergrupraporDTO.setOzkod(turuString[2]);
			String degString[] = deg_cevir(kergrupraporDTO, kerConnDetails, cariConnDetails);
			
			if(! kergrupraporDTO.isDvzcevirchc()) {
				kergrupraporDTO.setDoviz("");
			}
			List<Map<String, Object>> ortfiat = keresteService.ort_diger_kodu(kergrupraporDTO, degString[0], degString[1]);
			response.put("data", (ortfiat != null) ? ortfiat : new ArrayList<>());
			response.put("raporturu", kergrupraporDTO.getGruplama()); 
			if(kergrupraporDTO.isDvzcevirchc()) {
				response.put("dvz", kergrupraporDTO.getDoviz()); 
			}
			else {
				response.put("dvz", ""); 
			}
			
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			e.printStackTrace();
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	private String[] grup_cevir(String ana,String alt, String oz1)
	{
		String deger[] = {"","","",""};
		String qwq1 = "", qwq2="", qwq3="";
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
		return deger;
	}
	
	private String[] deg_cevir(kergrupraporDTO kergrupraporDTO, ConnectionDetails kerConnDetails,ConnectionDetails cariConnDetails)
	{
		String deger[] = {"",""};
		String hANGI = "" ;
		String yu = "" , iu = "" ;
		if (kergrupraporDTO.getTuru().equals("GIREN"))
			hANGI= "" ;
		else if (kergrupraporDTO.getTuru().equals("CIKAN"))
			hANGI= "C" ;
		else
			hANGI= "" ;
		
		if (kergrupraporDTO.getGruplama().toString().equals("Ana Grup"))
		{ 
			yu = " (SELECT DISTINCT  ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y =  KERESTE." + hANGI + "Ana_Grup ) as Ana_Grup " +
					" , (SELECT DISTINCT  ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y =  KERESTE." + hANGI + "Alt_Grup ) as Alt_Grup ";
			iu = " KERESTE."+hANGI+"Ana_Grup ,  KERESTE." + hANGI + "Alt_Grup order by KERESTE." + hANGI + "Ana_Grup ";
			if(kerConnDetails.getHangisql().equals("PG SQL"))
			{
				yu = " (SELECT DISTINCT  \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID_Y\" =  s.\"" + hANGI + "Ana_Grup\" ) as \"Ana_Grup\" " +
						" , (SELECT DISTINCT  \"ALT_GRUP\" FROM \"ALT_GRUP_DEGISKEN\" WHERE \"ALT_GRUP_DEGISKEN\".\"ALID_Y\" =  s.\"" + hANGI + "Alt_Grup\" ) as \"Alt_Grup\" ";
				iu = " s.\"" + hANGI + "Ana_Grup\" ,  s.\"" + hANGI + "Alt_Grup\" order by s.\"" + hANGI + "Ana_Grup\" ";
			}
		}
		else  if (kergrupraporDTO.getGruplama().toString().equals("Sinif"))
		{
			yu = " SUBSTRING(KERESTE.Kodu,1, 2) as Sinif, (SELECT ACIKLAMA FROM KOD_ACIKLAMA  WHERE KOD = SUBSTRING(KERESTE.Kodu,1, 2) ) as Adi ";
			iu = " SUBSTRING(KERESTE.Kodu,1, 2) order by SUBSTRING(KERESTE.Kodu,1, 2)  ";
			if(kerConnDetails.getHangisql().equals("MY SQL"))
				iu = " SUBSTRING(KERESTE.Kodu,1, 2),Adi  order by SUBSTRING(KERESTE.Kodu,1, 2)  ";
			if(kerConnDetails.getHangisql().equals("PG SQL"))
			{
				yu = " SUBSTRING(s.\"Kodu\",1, 2) as \"Sinif\", (SELECT \"ACIKLAMA\" FROM \"KOD_ACIKLAMA\"  WHERE \"KOD\" = SUBSTRING(s.\"Kodu\",1, 2) ) as \"Adi\" ";
				iu = " SUBSTRING(s.\"Kodu\",1, 2) , \"Adi\"  order by SUBSTRING(s.\"Kodu\",1, 2)  ";
			}
		}
		else  if (kergrupraporDTO.getGruplama().toString().equals("Sinif-Kal"))
		{
			yu = " SUBSTRING(KERESTE.Kodu,1, 2) as Sinif, SUBSTRING(KERESTE.Kodu, 4, 3) as Kal ";
			iu = " SUBSTRING(KERESTE.Kodu,1, 2) ,  SUBSTRING(KERESTE.Kodu, 4, 3)  order by SUBSTRING(KERESTE.Kodu,1, 2) , SUBSTRING(KERESTE.Kodu, 4, 3) ";
			if(kerConnDetails.getHangisql().equals("PG SQL"))
			{
				yu = " SUBSTRING(s.\"Kodu\",1, 2) as \"Sinif\", SUBSTRING(s.\"Kodu\", 4, 3) as \"Kal\" ";
				iu = " SUBSTRING(s.\"Kodu\",1, 2) ,  SUBSTRING(s.\"Kodu\", 4, 3)  order by SUBSTRING(s.\"Kodu\",1, 2) , SUBSTRING(s.\"Kodu\", 4, 3) ";

			}
		}
		else  if (kergrupraporDTO.getGruplama().toString().equals("Sinif-Boy"))
		{
			yu = " SUBSTRING(KERESTE.Kodu,1, 2) as Sinif, SUBSTRING(KERESTE.Kodu, 8, 4) as Boy ";
			iu = " SUBSTRING(KERESTE.Kodu,1, 2) ,  SUBSTRING(KERESTE.Kodu, 8, 4)  order by SUBSTRING(KERESTE.Kodu,1, 2) , SUBSTRING(KERESTE.Kodu, 8, 4) ";
			if(kerConnDetails.getHangisql().equals("PG SQL"))
			{
				yu = " SUBSTRING(s.\"Kodu\",1, 2) as \"Sinif\", SUBSTRING(s.\"Kodu\", 8, 4) as \"Boy\" ";
				iu = " SUBSTRING(s.\"Kodu\",1, 2) ,  SUBSTRING(s.\"Kodu\", 8, 4)  order by SUBSTRING(s.\"Kodu\",1, 2) , SUBSTRING(s.\"Kodu\", 8, 4) ";
			}
		}
		else  if (kergrupraporDTO.getGruplama().toString().equals("Sinif-Gen"))
		{
			yu = " SUBSTRING(KERESTE.Kodu,1, 2) as Sinif, SUBSTRING(KERESTE.Kodu, 13, 4) as Gen ";
			iu = " SUBSTRING(KERESTE.Kodu,1, 2) ,  SUBSTRING(KERESTE.Kodu, 13, 4)  order by SUBSTRING(KERESTE.Kodu,1, 2) , SUBSTRING(KERESTE.Kodu, 13, 4) ";
			if(kerConnDetails.getHangisql().equals("PG SQL"))
			{
				yu = " SUBSTRING(s.\"Kodu\",1, 2) as \"Sinif\", SUBSTRING(s.\"Kodu\", 13, 4) as \"Gen\" ";
				iu = " SUBSTRING(s.\"Kodu\",1, 2) ,  SUBSTRING(s.\"Kodu\", 13, 4)  order by SUBSTRING(s.\"Kodu\",1, 2) , SUBSTRING(s.\"Kodu\", 13, 4) ";

			}
		}
		else  if (kergrupraporDTO.getGruplama().toString().equals("Kodu"))
		{
			yu = " KERESTE.Kodu, (SELECT ACIKLAMA FROM KOD_ACIKLAMA  WHERE KOD = SUBSTRING(KERESTE.Kodu,1, 2) ) as Adi ";
			iu = " KERESTE.Kodu  order by KERESTE.Kodu  ";
			if(kerConnDetails.getHangisql().equals("MY SQL"))
				iu = " KERESTE.Kodu,Adi  order by SUBSTRING(KERESTE.Kodu,1, 2)  ";
			if(kerConnDetails.getHangisql().equals("PG SQL"))
			{
				yu = " s.\"Kodu\", (SELECT \"ACIKLAMA\" FROM \"KOD_ACIKLAMA\"  WHERE \"KOD\" = SUBSTRING(s.\"Kodu\",1, 2) ) as \"Adi\" ";
				iu = " s.\"Kodu\" , \"Adi\"  order by s.\"Kodu\"  ";
			}
		}
		else  if (kergrupraporDTO.getGruplama().toString().equals("Konsimento"))
		{
			yu = " KERESTE.Konsimento, (SELECT ACIKLAMA FROM KONS_ACIKLAMA  WHERE KONS = KERESTE.Konsimento ) as Aciklama ";
			iu = " KERESTE.Konsimento  order by KERESTE.Konsimento  ";
			if(kerConnDetails.getHangisql().equals("MY SQL"))
				iu = " KERESTE.Konsimento,Aciklama  order by KERESTE.Konsimento  ";
			if(kerConnDetails.getHangisql().equals("PG SQL"))
			{
				yu = " s.\"Konsimento\", (SELECT \"ACIKLAMA\" FROM \"KONS_ACIKLAMA\"  WHERE \"KONS\" = s.\"Konsimento\" ) as \"Aciklama\" ";
				iu = " s.\"Konsimento\" , \"Aciklama\"  order by s.\"Konsimento\"  ";
			}
		}
		else  if (kergrupraporDTO.getGruplama().toString().equals("Yil"))
		{
			if(kerConnDetails.getHangisql().equals("MS SQL"))
			{
				yu = " datepart(yyyy,KERESTE." + hANGI + "Tarih) as Yil ";
				iu = "  datepart(yyyy,KERESTE." + hANGI + "Tarih) order by datepart(yyyy,KERESTE." + hANGI + "Tarih)  ";
			}
			else if(kerConnDetails.getHangisql().equals("MY SQL"))
			{
				yu = " YEAR(KERESTE." + hANGI + "Tarih) as Yil "; 
				iu = "  YEAR(KERESTE." + hANGI + "Tarih) order by YEAR(KERESTE." + hANGI + "Tarih)  ";
			}
			else if(kerConnDetails.getHangisql().equals("PG SQL"))
			{
				yu = " DATE_PART('year',s.\"Tarih\") as \"Yil\" "; 
				iu = " DATE_PART('year',s.\"" + hANGI + "Tarih\") order by DATE_PART('year',s.\"" + hANGI + "Tarih\")  ";
			}
		}
		else  if (kergrupraporDTO.getGruplama().toString().equals("Yil_Ay"))
		{
			if(kerConnDetails.getHangisql().equals("MS SQL"))
			{
				yu = " datepart(yyyy,KERESTE." + hANGI + "Tarih) as Yil, datepart(mm,KERESTE." + hANGI + "Tarih) as Ay ";
				iu = "  datepart(yyyy,KERESTE." + hANGI + "Tarih) , datepart(mm,KERESTE." + hANGI + "Tarih) order by datepart(yyyy,KERESTE." + hANGI + "Tarih),datepart(mm,KERESTE."+hANGI+"Tarih)  ";
			}
			else if(kerConnDetails.getHangisql().equals("MY SQL"))
			{
				yu = " YEAR(KERESTE." + hANGI + "Tarih) as Yil, MONTH(KERESTE." + hANGI + "Tarih) as Ay ";
				iu = "  YEAR(KERESTE." + hANGI + "Tarih) , MONTH(KERESTE." + hANGI + "Tarih) order by YEAR(KERESTE." + hANGI + "Tarih),MONTH(KERESTE." + hANGI + "Tarih) ";
			}
			else if(kerConnDetails.getHangisql().equals("PG SQL"))
			{
				yu = " DATE_PART('year',s.\"" + hANGI + "Tarih\") as \"Yil\", DATE_PART('month',s.\"" + hANGI + "Tarih\") as \"Ay\" ";
				iu = " DATE_PART('year',s.\"" + hANGI + "Tarih\") , DATE_PART('monyh',s.\"" + hANGI + "Tarih\") order by DATE_PART('year',s.\"" + hANGI + "Tarih\"),DATE_PART('month',s.\"" + hANGI + "Tarih\") ";
			}
		}
		else if (kergrupraporDTO.getGruplama().toString().equals("Hesap Kodu-Ana_Alt_Grup"))
		{ 
			yu = " KERESTE." + hANGI + "Cari_Firma as Cari_Firma,(SELECT DISTINCT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y =  KERESTE." + hANGI + "Ana_Grup ) as Ana_Grup " +
					" , (SELECT DISTINCT  ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y =  KERESTE." + hANGI + "Alt_Grup ) as Alt_Grup ";
			iu = " KERESTE." + hANGI + "Cari_Firma,KERESTE." + hANGI + "Ana_Grup ,  KERESTE." + hANGI + "Alt_Grup order by KERESTE." + hANGI + "Ana_Grup ";
			if(kerConnDetails.getHangisql().equals("PG SQL"))
			{
				yu = " s.\"" + hANGI + "Cari_Firma\",(SELECT DISTINCT  \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID_Y\" = s.\"" + hANGI + "Ana_Grup\" ) as \"Ana_Grup\" " +
						" , (SELECT DISTINCT  \"ALT_GRUP\" FROM \"ALT_GRUP_DEGISKEN\" WHERE \"ALT_GRUP_DEGISKEN\".\"ALID_Y\" =  s.\"" + hANGI + "Alt_Grup\" ) as \"Alt_Grup\" ";
				iu = " s.\"" + hANGI + "Cari_Firma\",s.\"" + hANGI + "Ana_Grup\" ,  s.\"" + hANGI + "Alt_Grup\" order by s.\"" + hANGI + "Ana_Grup\" ";

			}
		}
		else if (kergrupraporDTO.getGruplama().toString().equals("Hesap Kodu"))
		{ 
			if(kerConnDetails.getHangisql().equals("MS SQL"))
			{
				yu = " KERESTE." + hANGI + "Cari_Firma as Cari_Firma,(SELECT DISTINCT UNVAN FROM OK_Car" + cariConnDetails.getDatabaseName() + ".dbo.HESAP WHERE hesap.hesap = KERESTE." + hANGI + "Cari_Firma   ) as Cari_Adi  ";
				iu = " KERESTE." + hANGI + "Cari_Firma ORDER BY KERESTE." + hANGI + "Cari_Firma";
			}
			else if(kerConnDetails.getHangisql().equals("MY SQL"))
			{
				yu = " KERESTE." + hANGI + "Cari_Firma,(SELECT DISTINCT UNVAN FROM OK_Car" + cariConnDetails.getDatabaseName() + ".HESAP WHERE hesap.hesap = KERESTE."+hANGI+"Cari_Firma   ) as Cari_Adi  ";
				iu = " KERESTE." + hANGI + "Cari_Firma ORDER BY KERESTE." + hANGI + "Cari_Firma";
			}
			if(kerConnDetails.getHangisql().equals("PG SQL"))
			{
				String carServer = "dbname = ok_car" + cariConnDetails.getDatabaseName() + " port = " + Global_Yardimci.ipCevir(cariConnDetails.getServerIp())[1] + " host = localhost user = " + cariConnDetails.getUsername() +" password = " + cariConnDetails.getPassword() +"" ; 
				yu = " s.\"" + hANGI + "Cari_Firma\","
						+ "(SELECT \"UNVAN\" FROM dblink ('"+ carServer + "',"  
						+ " 'SELECT \"UNVAN\",\"HESAP\" FROM \"HESAP\" ') "  
						+ " AS adr(\"UNVAN\" character varying,\"HESAP\" character varying) "
						+ " WHERE \"HESAP\" = s.\"" + hANGI + "Cari_Firma\") as \"Cari_Adi\" " ;
				iu = " s.\"" + hANGI + "Cari_Firma\",\"Cari_Adi\" ORDER BY s.\"" + hANGI + "Cari_Firma\" ";
			}
		}
		deger[0] = yu; 
		deger[1] = iu; 
		return deger;
	}
	
	@PostMapping("kereste/ortfiat_download")
	public ResponseEntity<byte[]> downloadReport(@RequestBody List<Map<String, String>> tableData) {
		ByteArrayDataSource dataSource ;
		try {
			dataSource =  raporOlustur.fatrap(tableData);
			if (dataSource == null) {
				throw new ServiceException("Rapor oluşturulamadı: veri bulunamadı.");
			}
			byte[] fileContent = dataSource.getInputStream().readAllBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			String fileName = "Ortalama_Fiat.xlsx";
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
