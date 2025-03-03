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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.dto.kereste.kerestedetayraporDTO;
import com.hamit.obs.dto.stok.raporlar.fatraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.kereste.KeresteService;

@Controller
public class KerFatRaporController {

	@Autowired
	private RaporOlustur raporOlustur;
	
	@Autowired
	private KeresteService keresteService;
	
	@GetMapping("/kereste/fatrapor")
	public String fatrapor() {
		return "kereste/fatrapor";
	}
	
	@PostMapping("kereste/fatrapdoldur")
	@ResponseBody
	public Map<String, Object> fatrapdoldur(@RequestBody kerestedetayraporDTO kerestedetayraporDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			String turuString[] = grup_cevir(kerestedetayraporDTO.getGana(),kerestedetayraporDTO.getGalt(),kerestedetayraporDTO.getGdepo(),kerestedetayraporDTO.getGozkod(),
					kerestedetayraporDTO.getCana(),kerestedetayraporDTO.getCalt(),kerestedetayraporDTO.getCdepo(),kerestedetayraporDTO.getCozkod());
			kerestedetayraporDTO.setGana(turuString[0]);
			kerestedetayraporDTO.setGalt(turuString[1]);
			kerestedetayraporDTO.setGdepo(turuString[2]);
			kerestedetayraporDTO.setGozkod(turuString[3]);
			kerestedetayraporDTO.setCana(turuString[4]);
			kerestedetayraporDTO.setCalt(turuString[5]);
			kerestedetayraporDTO.setCdepo(turuString[6]);
			kerestedetayraporDTO.setCozkod(turuString[7]);
			
			
			if (kerestedetayraporDTO.getGircik().equals("GIREN"))
				kerestedetayraporDTO.setGircik("G") ;
			else
				kerestedetayraporDTO.setGircik("C") ;
			
			List<Map<String, Object>> fat_listele = new ArrayList<>();
			if (kerestedetayraporDTO.getGruplama().equals("fno"))
				fat_listele = keresteService.fat_rapor(kerestedetayraporDTO);
			else if (kerestedetayraporDTO.getGruplama().equals("fkodu"))
			{
				String hangiadres[] = hangiadres(kerestedetayraporDTO.getCaradr());
				kerestedetayraporDTO.setBir(hangiadres[0]);
				kerestedetayraporDTO.setIki(hangiadres[1]);
				kerestedetayraporDTO.setUc(hangiadres[2]);
//				fat_listele = keresteService.fat_rapor_fat_tar(kerestedetayraporDTO);
			}
			else
			{
				String hangiadres[] = hangiadres(kerestedetayraporDTO.getCaradr());
				kerestedetayraporDTO.setBir(hangiadres[0]);
				kerestedetayraporDTO.setIki(hangiadres[1]);
				kerestedetayraporDTO.setUc(hangiadres[2]);
//				fat_listele = keresteService.fat_rapor_cari_kod(kerestedetayraporDTO);
			}
			response.put("data", (fat_listele != null) ? fat_listele : new ArrayList<>());
			response.put("raporturu",kerestedetayraporDTO.getGruplama());
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
	
	@PostMapping("kereste/fatdetay")
	@ResponseBody
	public Map<String, Object> fatdetay(@RequestParam String evrakNo,@RequestParam String gircik) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> fatdetay = keresteService.fat_detay_rapor(evrakNo,gircik);
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
	
	@PostMapping("kereste/fatrap_download")
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

	
	private String[] grup_cevir(String ana,String alt,String dpo,String ozkod,String cana,String calt,String cdpo,String cozkod)
	{
		String deger[] = {"","","","","","","",""};
		String qwq1 = "", qwq2="", qwq3="",qwq4 = "",qwq5 = "", qwq6="",qwq7 = "",qwq8 = "";
		//***********************ANA GRUP
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
		//***********************ALT GRUP
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
		//***********************DEPO
		if (dpo.equals(""))
			qwq3 = " Like  '%' " ;
		else if  (dpo.equals("Bos Olanlar"))
			qwq3 = " = '' " ;
		else
		{
			String dpos = keresteService.urun_kod_degisken_ara("DPID_Y", "DEPO", "DEPO_DEGISKEN", dpo);
			qwq3 = "=" + dpos;
		}
		deger[2] = qwq3; 

		//***********************OZKOD
		if (ozkod.equals(""))
			qwq4 = " Like  '%' " ;
		else if  (ozkod.equals("Bos Olanlar"))
			qwq4 = " = '' " ;
		else
		{
			String anas = keresteService.urun_kod_degisken_ara("OZ1ID_Y", "OZEL_KOD_1", "OZ_KOD_1_DEGISKEN", ozkod);
			qwq4 = "=" + anas;
		}
		deger[3] = qwq4; 
		//***********************cANA GRUP
		if (cana.equals(""))
			qwq5 = " Like  '%' " ;
		else if  (ana.equals("Bos Olanlar"))
			qwq5 = " = '' " ;
		else
		{
			String canas = keresteService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", cana);
			qwq5 = "=" + canas;
		}
		deger[4] = qwq5; 
		//***********************cALT GRUP
		if (calt.equals(""))
			qwq6 = " Like  '%' " ;
		else if  (calt.equals("Bos Olanlar"))
			qwq6 = " = '' " ;
		else
		{
			String calts = keresteService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", calt);
			qwq6 ="=" + calts;
		}
		deger[5] = qwq6; 
		//***********************cDEPO
		if (cdpo.equals(""))
			qwq7 = " Like  '%' " ;
		else if  (cdpo.equals("Bos Olanlar"))
			qwq7 = " = '' " ;
		else
		{
			String cdpos = keresteService.urun_kod_degisken_ara("DPID_Y", "DEPO", "DEPO_DEGISKEN", cdpo);
			qwq7 = "=" + cdpos;
		}
		deger[6] = qwq7; 

		//***********************cOZKOD
		if (cozkod.equals(""))
			qwq8 = " Like  '%' " ;
		else if  (cozkod.equals("Bos Olanlar"))
			qwq8 = " = '' " ;
		else
		{
			String cozs = keresteService.urun_kod_degisken_ara("OZ1ID_Y", "OZEL_KOD_1", "OZ_KOD_1_DEGISKEN", cozkod);
			qwq8 = "=" + cozs;
		}
		deger[7] = qwq8; 
		return deger;
	}	
	private String[] hangiadres(String hangiadres) {
		String deger[] = {"","","",""};
		
		String qw1 = "", qw2="", qw3="",c_yer;
		
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails faturaConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
		ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
		ConnectionDetails adrConnDetails =  UserSessionManager.getUserSession(useremail, "Adres");
		
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