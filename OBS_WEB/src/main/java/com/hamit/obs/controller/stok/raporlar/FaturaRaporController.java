package com.hamit.obs.controller.stok.raporlar;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.dto.stok.raporlar.fatraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.fatura.FaturaService;

@Controller
public class FaturaRaporController {

	@Autowired
	private RaporOlustur raporOlustur;
	
	@Autowired
	private FaturaService faturaService;

	@GetMapping("/stok/fatrapor")
	public String fatrapor() {
		return "stok/raporlar/fatrapor";
	}

	@PostMapping("stok/fatrapdoldur")
	@ResponseBody
	public Map<String, Object> fatrapdoldur(@RequestBody fatraporDTO fatraporDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			String turuString[] =  grup_cevir(fatraporDTO.getAnagrp(),fatraporDTO.getAltgrp(),fatraporDTO.getDepo(),fatraporDTO.getTuru());
			fatraporDTO.setAnagrp(turuString[0]);
			fatraporDTO.setAltgrp(turuString[1]);
			fatraporDTO.setDepo(turuString[2]);
			fatraporDTO.setTuru(turuString[3]);
			List<Map<String, Object>> fat_listele = new ArrayList<>();
			Pageable pageable = PageRequest.of(fatraporDTO.getPage(), fatraporDTO.getPageSize());
			if (fatraporDTO.getGruplama().equals("fno"))
				fat_listele = faturaService.fat_rapor(fatraporDTO,pageable);
			else if (fatraporDTO.getGruplama().equals("fkodu"))
			{
				String hangiadres[] = hangiadres(fatraporDTO.getCaradr());
				fatraporDTO.setBir(hangiadres[0]);
				fatraporDTO.setIki(hangiadres[1]);
				fatraporDTO.setUc(hangiadres[2]);
				fat_listele = faturaService.fat_rapor_cari_kod(fatraporDTO,pageable);
			}
			else
			{
				String hangiadres[] = hangiadres(fatraporDTO.getCaradr());
				fatraporDTO.setBir(hangiadres[0]);
				fatraporDTO.setIki(hangiadres[1]);
				fatraporDTO.setUc(hangiadres[2]);
				fat_listele = faturaService.fat_rapor_fat_tar(fatraporDTO,pageable);
			}
			response.put("data", (fat_listele != null) ? fat_listele : new ArrayList<>());
			response.put("raporturu",fatraporDTO.getGruplama());
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("stok/fatdoldursize")
	@ResponseBody
	public Map<String, Object> fatdoldursize(@RequestBody fatraporDTO fatraporDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			String turuString[] =  grup_cevir(fatraporDTO.getAnagrp(),fatraporDTO.getAltgrp(),fatraporDTO.getDepo(),fatraporDTO.getTuru());
			fatraporDTO.setAnagrp(turuString[0]);
			fatraporDTO.setAltgrp(turuString[1]);
			fatraporDTO.setDepo(turuString[2]);
			fatraporDTO.setTuru(turuString[3]);
			double fatdetaysize = faturaService.fat_raporsize(fatraporDTO);
			response.put("totalRecords", fatdetaysize);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("stok/fatdetay")
	@ResponseBody
	public Map<String, Object> fatdetay(@RequestParam String evrakNo,@RequestParam String gircik) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> fatdetay = faturaService.fat_detay_rapor(evrakNo,gircik);
			response.put("data", (fatdetay != null) ? fatdetay : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("stok/fatrap_download")
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
			String fileName = "Fatura_Rapor.xlsx";
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

	
	private String[] grup_cevir(String ana,String alt,String dpo,String turu)
	{
		String deger[] = {"","","",""};
		String qwq1 = "", qwq2="", qwq3="",tur = "";
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
		//***********************DEPO
		if (dpo.equals(""))
			qwq3 = " Like  '%' " ;
		else if  (dpo.equals("Bos Olanlar"))
			qwq3 = " = '' " ;
		else
		{
			String dpos = faturaService.urun_kod_degisken_ara("DPID_Y", "DEPO", "DEPO_DEGISKEN", dpo);
			qwq3 = "=" + dpos;
		}
		deger[2] = qwq3; 
		//***********************TUR
		if (turu.equals("GIREN"))
			tur = "G" ;
		else if (turu.equals("CIKAN"))
			tur = "C" ;
		else if (turu.equals(""))
			tur = "" ;
		deger[3] = tur; 
		return deger;
	}
	
	private String[] hangiadres(String hangiadres) {
		String deger[] = {"","","",""};
		
		String qw1 = "", qw2="", qw3="",c_yer;
		
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails faturaConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
		ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
		ConnectionDetails adrConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.ADRES);
		
		if (hangiadres.equals("Cari_Firma"))
		{
			if(faturaConnDetails.getHangisql().equals("MS SQL"))
			{
				c_yer = "OK_Car" + cariConnDetails.getDatabaseName() + "" ;
				qw1 = " ,(SELECT   UNVAN FROM " + c_yer + ".dbo.HESAP WHERE HESAP.HESAP = FATURA.Cari_Firma  ) as Unvan " ;
				qw2 = " ,(SELECT   VERGI_NO FROM " + c_yer + ".dbo.HESAP_DETAY WHERE HESAP_DETAY.D_HESAP = FATURA.Cari_Firma  ) as Vergi_No " ;
				qw3 = "Fatura_No,Gir_Cik,Tarih, Cari_Firma" ;
				deger[0] = qw1;
				deger[1] = qw2;
				deger[2] = qw3;
			}
			else if(faturaConnDetails.getHangisql().equals("MY SQL"))
			{
				c_yer = "OK_Car" + cariConnDetails.getDatabaseName() + "" ;
				qw1 = " ,(SELECT   UNVAN FROM " + c_yer + ".HESAP WHERE HESAP.HESAP = FATURA.Cari_Firma  ) as Unvan " ;
				qw2 = " ,(SELECT   VERGI_NO FROM " + c_yer + ".HESAP_DETAY WHERE HESAP_DETAY.D_HESAP = FATURA.Cari_Firma  ) as Vergi_No " ;
				qw3 = "Fatura_No,Gir_Cik,Tarih, Cari_Firma" ;
				deger[0] = qw1;
				deger[1] = qw2;
				deger[2] = qw3;
			}
			if(faturaConnDetails.getHangisql().equals("PG SQL") )
				{
					String carServer = "dbname = ok_car" + cariConnDetails.getDatabaseName() + " port = " + Global_Yardimci.ipCevir(cariConnDetails.getServerIp())[1] + " host = localhost user = " + cariConnDetails.getUsername() +" password = " + cariConnDetails.getPassword() +"" ; 
					qw1 = ",(SELECT \"UNVAN\" FROM  dblink ('" + carServer + "', "  
							+ " 'SELECT \"UNVAN\" ,\"HESAP\" FROM \"HESAP\"  ') "  
							+" AS adr(\"UNVAN\" character varying,\"HESAP\" character varying) "
							+" WHERE \"HESAP\" = \"FATURA\".\"Cari_Firma\"  LIMIT 1) as \"Unvan\"  " ;
					qw2 = ",(SELECT \"VERGI_NO\" FROM  dblink ('" + carServer + "', "  
							+ " 'SELECT \"VERGI_NO\" ,\"D_HESAP\" FROM \"HESAP_DETAY\"  ') "  
							+" AS adr(\"VERGI_NO\" character varying,\"D_HESAP\" character varying) "
							+" WHERE \"D_HESAP\" = \"FATURA\".\"Cari_Firma\" ) as \"Vergi_No\"  " ;
					qw3 = " \"Fatura_No\",\"Gir_Cik\",\"Tarih\", \"Cari_Firma\" " ;
					deger[0] = qw1;
					deger[1] = qw2;
					deger[2] = qw3;
				}
		}
		else
		{
			if(faturaConnDetails.getHangisql().equals("MS SQL"))
			{
				c_yer = "OK_Adr" + adrConnDetails.getDatabaseName() + "" ;
				qw1 = " ,(SELECT   Adi FROM " + c_yer + ".dbo.Adres WHERE Adres.M_Kodu = FATURA.Adres_Firma  ) as Unvan " ;
				qw2 = " ,(SELECT   Vergi_No FROM " + c_yer + ".dbo.Adres WHERE Adres.M_Kodu = FATURA.Adres_Firma  ) as Vergi_No " ;
				qw3 = "Fatura_No,Gir_Cik,Tarih, Adres_Firma" ;
				deger[0] = qw1;
				deger[1] = qw2;
				deger[2] = qw3;
			}
			else if(faturaConnDetails.getHangisql().equals("MY SQL"))
			{
				c_yer = "OK_Adr" + adrConnDetails.getDatabaseName() + "" ;
				qw1 = " ,(SELECT   Adi FROM " + c_yer + ".Adres WHERE Adres.M_Kodu = FATURA.Adres_Firma  ) as Unvan " ;
				qw2 = " ,(SELECT   Vergi_No FROM " + c_yer + ".Adres WHERE Adres.M_Kodu = FATURA.Adres_Firma  ) as Vergi_No " ;
				qw3 = "Fatura_No,Gir_Cik,Tarih, Adres_Firma" ;
				deger[0] = qw1;
				deger[1] = qw2;
				deger[2] = qw3;
			}
			if(faturaConnDetails.getHangisql().equals("PG SQL") )
			{
				String adrServer = "dbname = ok_adr" + adrConnDetails.getDatabaseName() + " port = " + Global_Yardimci.ipCevir(adrConnDetails.getServerIp())[1] + " host = localhost user = " + adrConnDetails.getUsername() +" password = " + adrConnDetails.getPassword() +"" ; 
				qw1 = ",(SELECT \"ADI\" FROM  dblink ('"+ adrServer + "', "  
						+ " 'SELECT \"ADI\" ,\"M_KODU\" FROM \"ADRES\"  ') "  
						+" AS adr(\"ADI\" character varying,\"M_KODU\" character varying) "
						+" WHERE \"M_KODU\" = \"FATURA\".\"Adres_Firma\" ) as \"Unvan\"  " ;
				qw2 = ",(SELECT \"VERGI_NO\" FROM  dblink ('"+ adrServer + "', "  
						+ " 'SELECT \"VERGI_NO\" ,\"M_KODU\" FROM \"ADRES\"  ') "  
						+" AS adr(\"VERGI_NO\" character varying,\"M_KODU\" character varying) "
						+" WHERE \"M_KODU\" = \"FATURA\".\"Adres_Firma\" ) as \"Vergi_No\"  " ;
				qw3 = " \"Fatura_No\",\"Gir_Cik\",\"Tarih\", \"Adres_Firma\" " ;
				deger[0] = qw1;
				deger[1] = qw2;
				deger[2] = qw3;
			}
		}
		return deger;
	}
}