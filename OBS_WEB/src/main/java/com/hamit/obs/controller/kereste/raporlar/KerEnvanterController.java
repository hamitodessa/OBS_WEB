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
import com.hamit.obs.dto.kereste.kerestedetayraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.kereste.KeresteService;

@Controller
public class KerEnvanterController {

	@Autowired
	private RaporOlustur raporOlustur;
	
	@Autowired
	private KeresteService keresteService;
	
	@GetMapping("/kereste/envanter")
	public String fatrapor() {
		return "kereste/envanter";
	}
	
	@PostMapping("kereste/kerenvanter")
	@ResponseBody
	public Map<String, Object> kerenvanter(@RequestBody kerestedetayraporDTO kerestedetayraporDTO ) {
		Map<String, Object> response = new HashMap<>();
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kerConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
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
			String grupString[] = {"",""} ;
			if(kerestedetayraporDTO.getGruplama().equals("Urun Kodu")) {
				grupString[0] = " Kodu " ;
				grupString[1] = " Kodu " ;
				if(kerConnDetails.getHangisql().equals("PG SQL") )
				{
					grupString[0] = " \"Kodu\" " ;
					grupString[1] = " \"Kodu\" " ;
				}
			}
			else if(kerestedetayraporDTO.getGruplama().equals("Konsimento")) {
				grupString[0] = " Konsimento , (SELECT ACIKLAMA FROM KONS_ACIKLAMA  WHERE KONS = KERESTE.Konsimento ) as Aciklama " ;
				grupString[1] = " Konsimento " ;
				if(kerConnDetails.getHangisql().equals("PG SQL") )
				{
					grupString[0] = " \"Konsimento\" , (SELECT \"ACIKLAMA\" FROM \"KONS_ACIKLAMA\"  WHERE \"KONS\" = \"KERESTE\".\"Konsimento\" ) as \"Aciklama\" " ;
					grupString[1] = " \"Konsimento\" " ;
				}
			}
			else if(kerestedetayraporDTO.getGruplama().equals("Hesap-Kodu")) {
				if(kerConnDetails.getHangisql().equals("MS SQL") )
				{
					grupString[0] = " Cari_Firma , (SELECT TOP 1  UNVAN FROM OK_Car" + kerConnDetails.getDatabaseName() + ".dbo.HESAP WHERE HESAP.HESAP = KERESTE.Cari_Firma  ) as Unvan  " ;
					grupString[1] = " Cari_Firma " ;
				}
				else if(kerConnDetails.getHangisql().equals("MY SQL") )
				{
					grupString[0] = " Cari_Firma , (SELECT  UNVAN FROM OK_Car" + kerConnDetails.getDatabaseName() + ".HESAP WHERE HESAP.HESAP = KERESTE.Cari_Firma Limit 1 ) as Unvan  " ;
					grupString[1] = " Cari_Firma " ;
				}
				else if(kerConnDetails.getHangisql().equals("PG SQL") )
				{
					String carServer = "dbname = ok_car" + cariConnDetails.getDatabaseName() + " port = " + Global_Yardimci.ipCevir(cariConnDetails.getServerIp())[1] + " host = localhost user = " + cariConnDetails.getUsername() +" password = " + cariConnDetails.getPassword() +"" ; 
					String carString ="(SELECT \"UNVAN\" FROM  dblink ('"+ carServer + "', " + 
							" 'SELECT \"UNVAN\" ,\"HESAP\" FROM \"HESAP\"') " + 
							" AS adr(\"UNVAN\" character varying,\"HESAP\" character varying) "+
							" WHERE \"HESAP\" = \"KERESTE\".\"Cari_Firma\"  Limit 1 )";
					grupString[0] = " \"Cari_Firma\" , " + carString + " as \"Unvan\"  " ;
					grupString[1] = " \"Cari_Firma\" " ;
				}
			}
			else if(kerestedetayraporDTO.getGruplama().equals("Ana_Grup-Alt_Grup")) {
				grupString[0] = "  (SELECT DISTINCT  ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = KERESTE.Ana_Grup ) as Ana_Grup , "
						+ " (SELECT DISTINCT  ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = KERESTE.Alt_Grup ) as Alt_Grup " ;
				grupString[1] = " Ana_Grup ,Alt_Grup  " ;
				if(kerConnDetails.getHangisql().equals("PG SQL"))
				{
					grupString[0] = "  (SELECT DISTINCT  \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID_Y\" = \"KERESTE\".\"Ana_Grup\" ) as \"Ana_Grup\" , "
							+ " (SELECT DISTINCT  \"ALT_GRUP\" FROM \"ALT_GRUP_DEGISKEN\" WHERE \"ALT_GRUP_DEGISKEN\".\"ALID_Y\" = \"KERESTE\".\"Alt_Grup\" ) as \"Alt_Grup\" " ;
					grupString[1] = " \"Ana_Grup\" ,\"Alt_Grup\"  " ;
				}
			}
			List<Map<String, Object>> envanter = keresteService.envanter(kerestedetayraporDTO,grupString);
			response.put("data", (envanter != null) ? envanter : new ArrayList<>());
			response.put("raporturu",kerestedetayraporDTO.getGruplama());
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	
	private String[] grup_cevir(String ana,String alt,String dpo,String ozkod,String cana,String calt,String cdpo,String cozkod)
	{
		String deger[] = {"","","","","","","",""};
		String qwq1 = "", qwq2="", qwq3="",qwq4 = "",qwq5 = "", qwq6="",qwq7 = "",qwq8 = "";
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

	@PostMapping("kereste/envanter_download")
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
			String fileName = "Envanter_Rapor.xlsx";
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
