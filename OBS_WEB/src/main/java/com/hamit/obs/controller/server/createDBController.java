package com.hamit.obs.controller.server;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.dto.server.serverBilgiDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.server.ServerService;
import com.hamit.obs.service.user.UserService;

@Controller
public class createDBController {

	@Autowired
	private ServerService serverService ;

	@Autowired
	private UserService userService;
	
	@GetMapping("/user/createdb")
	public String createdb() {
		return "user/createdb";
	}

	@PostMapping("server/serverkontrol")
	@ResponseBody
	public Map<String, String> serverKontrol(@RequestBody serverBilgiDTO serverBilgiDTO) {
		Map<String, String> response = new HashMap<>();
		boolean result = false;
		try {
			result = serverService.serverKontrol(serverBilgiDTO);
			String serverDurum = result ? "true" : "false";
			response.put("serverDurum", serverDurum);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("serverDurum", "false");
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("serverDurum", "false");
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("server/dosyakontrol")
	@ResponseBody
	public Map<String, String> dosyakontrol(@RequestBody serverBilgiDTO serverBilgiDTO) {
		Map<String, String> response = new HashMap<>();
		boolean result = false;
		String drm  = "false" ;
		try {
			switch (serverBilgiDTO.getUser_modul()) {
			case "Cari Hesap": {
				serverBilgiDTO.setUser_modul_baslik("OK_Car");
				break;
			}
			case "Kur": {
				serverBilgiDTO.setUser_modul_baslik("OK_Kur");
				break;
			}
			case "Adres": {
				serverBilgiDTO.setUser_modul_baslik("OK_Adr");
				break;
			}
			case "Kambiyo": {
				serverBilgiDTO.setUser_modul_baslik("OK_Kam");
				break;
			}
			case "Fatura": {
				serverBilgiDTO.setUser_modul_baslik("OK_Fat");
				break;
			}
			}
			result = serverService.dosyakontrol(serverBilgiDTO);
			drm = result != false ? "true" : "false" ;
			response.put("dosyaDurum",drm ); 
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("dosyaDurum",drm ); 
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("dosyaDurum",drm ); 
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("server/dosyaolustur")
	@ResponseBody
	public Map<String, String> dosyaolustur(@RequestBody serverBilgiDTO serverBilgiDTO) {
		Map<String, String> response = new HashMap<>();
		boolean result = false;
		String drm  = "false" ;
		String usrString =  Global_Yardimci.user_log(userService.getCurrentUser().getEmail());
		try {
			switch (serverBilgiDTO.getUser_modul()) {
			case "Cari Hesap": {
				serverBilgiDTO.setUser_name(usrString);
				serverBilgiDTO.setUser_modul_baslik("OK_Car");
				break;
			}
			case "Kur": {
				serverBilgiDTO.setUser_name(usrString);
				serverBilgiDTO.setUser_modul_baslik("OK_Kur");
				break;
			}
			case "Adres": {
				serverBilgiDTO.setUser_name(usrString);
				serverBilgiDTO.setUser_modul_baslik("OK_Adres");
				break;
			}
			case "Kambiyo": {
				serverBilgiDTO.setUser_name(usrString);
				serverBilgiDTO.setUser_modul_baslik("OK_Kam");
				break;
			}
			case "Fatura": {
				serverBilgiDTO.setUser_name(usrString);
				serverBilgiDTO.setUser_modul_baslik("OK_Fat");
				break;
			}
			}
			result = serverService.dosyaolustur(serverBilgiDTO);
			
			drm = result != false ? "true" : "false" ;			response.put("olustuDurum",drm ); 
			response.put("errorMessage", ""); 
			boolean idxdurum = index_JOB(serverBilgiDTO.getUser_modul(),serverBilgiDTO.getHangi_sql(),serverBilgiDTO);
			response.put("indexolustuDurum",idxdurum != false ? "true" : "false");
		} catch (ServiceException e) {
			response.put("olustuDurum",drm ); 
			response.put("indexolustuDurum","false");
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("olustuDurum",drm ); 
			response.put("indexolustuDurum","false");
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	private boolean index_JOB(String modul , String hangiSQL,serverBilgiDTO sbilgi)
	{
		boolean result = false;
		try {
			StringBuilder stb = new StringBuilder();
			switch(modul) {
			case "Cari Hesap":
				String hangi = hangiSQL  ;
				if( hangi.equals("MS SQL")) 
				{
					stb.append(" ALTER INDEX [IX_SATIRLAR] ON [dbo].[SATIRLAR] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_SID] ON [dbo].[SATIRLAR] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_EVRAK] ON [dbo].[IZAHAT] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_HESAP] ON [dbo].[HESAP] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IDX_HESAP] ON [dbo].[HESAP] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [D_HESAP] ON [dbo].[HESAP_DETAY] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					serverService.job_sil_S("OK_Car" +  sbilgi.getUser_prog_kodu() + "_Index","",sbilgi);
					serverService.job_olustur_S("OK_Car" +  sbilgi.getUser_prog_kodu() + "_Index","OK_Car" +  sbilgi.getUser_prog_kodu() , stb.toString(),sbilgi );
					serverService.job_baslat_S("OK_Car" +  sbilgi.getUser_prog_kodu() + "_Index",sbilgi);
				}
				else if( hangi.equals("MY SQL")) 
				{ 
					stb.append(" OPTIMIZE TABLE OK_Car" +  sbilgi.getUser_prog_kodu() + ".satirlar,") ; 
					stb.append("  OK_Car" +  sbilgi.getUser_prog_kodu() + ".izahat,") ; 
					stb.append("  OK_Car" +  sbilgi.getUser_prog_kodu() + ".hesap,") ; 
					stb.append("  OK_Car" +  sbilgi.getUser_prog_kodu() + ".hesap_detay;") ; 
					serverService.job_sil_S("OK_Car" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_car" + sbilgi.getUser_prog_kodu()  , sbilgi); //"/ok_car019"
					serverService.job_olustur_S("OK_Car" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_car" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);
				}
				else if( hangi.equals("PG SQL")) 
				{ 
					serverService.job_sil_S("CARI_" + sbilgi.getUser_prog_kodu() ,"", sbilgi); 
					stb.append("REINDEX TABLE \"SATIRLAR\";REINDEX TABLE \"HESAP\"; ") ; 
					stb.append("REINDEX TABLE \"HESAP_DETAY\";REINDEX TABLE \"IZAHAT\"; ") ; 
					String jobString = "SELECT dblink(''dbname = ok_car" +  sbilgi.getUser_prog_kodu() + " port = " + Global_Yardimci.ipCevir(sbilgi.getUser_server())[1] + " host = localhost user = " + sbilgi.getUser_server() + " password = " + sbilgi.getUser_pwd_server() + "''," + 
							"''" + stb.toString() + "'') " ;
					String dosyaString = sbilgi.getSuperviser().equals("") ? sbilgi.getSuperviser() : sbilgi.getSuperviser() ;
					serverService.job_olustur_S("CARI_" +  sbilgi.getUser_prog_kodu() ,dosyaString , jobString ,sbilgi);
				}
				break;
			case "Fatura":
				hangi = hangiSQL  ;
				if( hangi.equals("MS SQL")) 
				{
					stb.append(" ALTER INDEX [IX_FATURA] ON [dbo].[FATURA] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_Kodu] ON [dbo].[MAL] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_GRUP] ON [dbo].[STOK] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_Cikan] ON [dbo].[STOK] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_Giren] ON [dbo].[STOK] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_STOK] ON [dbo].[STOK] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_RECETE] ON [dbo].[RECETE] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					serverService.job_sil_S("OK_Fat" +  sbilgi.getUser_prog_kodu() + "_Index", "",sbilgi);
					serverService.job_olustur_S("OK_Fat" +  sbilgi.getUser_prog_kodu() + "_Index","OK_Fat" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);
					serverService.job_baslat_S("OK_Fat" +  sbilgi.getUser_prog_kodu() + "_Index", sbilgi);
				}
				else if( hangi.equals("MY SQL")) 
				{
					stb.append(" OPTIMIZE TABLE OK_Fat" +  sbilgi.getUser_prog_kodu() + ".fatura,") ; 
					stb.append("  OK_Fat" +  sbilgi.getUser_prog_kodu() + ".mal,") ; 
					stb.append("  OK_Fat" +  sbilgi.getUser_prog_kodu() + ".stok,") ; 
					stb.append("  OK_Fat" +  sbilgi.getUser_prog_kodu() + ".recete;") ; 
					serverService.job_sil_S("OK_Fat" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_fat" + sbilgi.getUser_prog_kodu()  , sbilgi);
					serverService.job_olustur_S("OK_Fat" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_fat" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);	
				}
				else if( hangi.equals("PG SQL")) 
				{ 
					serverService.job_sil_S("STOK_" + sbilgi.getUser_prog_kodu() ,""   , sbilgi); 
					stb.append("REINDEX TABLE \"DPN\";REINDEX TABLE \"FATURA\"; ") ; 
					stb.append("REINDEX TABLE \"IRSALIYE\";REINDEX TABLE \"MAL\"; ") ; 
					stb.append("REINDEX TABLE \"RECETE\";REINDEX TABLE \"STOK\"; ") ; 
					String jobString = "SELECT dblink(''dbname = ok_fat" +  sbilgi.getUser_prog_kodu() + " port = " + Global_Yardimci.ipCevir(sbilgi.getUser_server())[1] + " host = localhost user = " + sbilgi.getUser_server() + " password = " + sbilgi.getUser_pwd_server() + "''," + 
							"''" + stb.toString() + "'') " ;
					String dosyaString = sbilgi.getSuperviser().equals("") ? sbilgi.getSuperviser() : sbilgi.getSuperviser() ;
					serverService.job_olustur_S("STOK_" +  sbilgi.getUser_prog_kodu() ,dosyaString , jobString ,sbilgi);
				}
				break;
			case "Adres":
				hangi = hangiSQL  ;
				if( hangi.equals("MS SQL")) 
				{
					stb.append(" ALTER INDEX [IX_SATIRLAR] ON [dbo].[Adres] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					serverService.job_sil_S("OK_Adr" +  sbilgi.getUser_prog_kodu() + "_Index","", sbilgi);
					serverService.job_olustur_S("OK_Adr" +  sbilgi.getUser_prog_kodu() + "_Index","OK_Adr" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);
					serverService.job_baslat_S("OK_Adr" +  sbilgi.getUser_prog_kodu() + "_Index", sbilgi);
				}
				else if( hangi.equals("MY SQL")) 
				{
					stb.append(" OPTIMIZE TABLE OK_Adr" +  sbilgi.getUser_prog_kodu() + ".adres;") ; 
					serverService.job_sil_S("OK_Adr" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_adr" + sbilgi.getUser_prog_kodu()  , sbilgi); 
					serverService.job_olustur_S("OK_Adr" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_adr" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);
				}
				else if( hangi.equals("PG SQL")) 
				{ 
					serverService.job_sil_S("ADRES_" + sbilgi.getUser_prog_kodu() ,""   , sbilgi); 
					stb.append("REINDEX TABLE \"ADRES\" ") ; 
					String jobString = "SELECT dblink(''dbname = ok_adr" +  sbilgi.getUser_prog_kodu() + " port = " + Global_Yardimci.ipCevir(sbilgi.getUser_server())[1] + " host = localhost user = " + sbilgi.getUser_server() + " password = " + sbilgi.getUser_pwd_server() + "''," + 
							"''" + stb.toString() + "'') " ;
					String dosyaString = sbilgi.getSuperviser().equals("") ? sbilgi.getSuperviser() : sbilgi.getSuperviser() ;
					serverService.job_olustur_S("ADRES_" +  sbilgi.getUser_prog_kodu() ,dosyaString , jobString ,sbilgi);
				}
				break;
			case "Kur":
				hangi = hangiSQL  ;
				if( hangi.equals("MS SQL")) 
				{
					stb.append(" ALTER INDEX [IX_KUR] ON [dbo].[Kurlar] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					serverService.job_sil_S("OK_Kur" +  sbilgi.getUser_prog_kodu() + "_Index","", sbilgi);
					serverService.job_olustur_S("OK_Kur" +  sbilgi.getUser_prog_kodu() + "_Index","OK_Kur" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);
					serverService.job_baslat_S("OK_Kur" +  sbilgi.getUser_prog_kodu() + "_Index", sbilgi);
				}
				else if( hangi.equals("MY SQL")) 
				{
					stb.append(" OPTIMIZE TABLE OK_Kur" +  sbilgi.getUser_prog_kodu() + ".kurlar;") ; 
					serverService.job_sil_S("OK_Kur" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_kur" + sbilgi.getUser_prog_kodu()  , sbilgi); 
					serverService.job_olustur_S("OK_Kur" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_kur" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);	
				}
				else if( hangi.equals("PG SQL")) 
				{ 
					serverService.job_sil_S("KUR_" + sbilgi.getUser_prog_kodu() ,""   , sbilgi); 
					stb.append("REINDEX TABLE \"KURLAR\" ") ; 
					String jobString = "SELECT dblink(''dbname = ok_kur" +  sbilgi.getUser_prog_kodu() + " port = " + Global_Yardimci.ipCevir(sbilgi.getUser_server())[1] + " host = localhost user = " + sbilgi.getUser_server() + " password = " + sbilgi.getUser_pwd_server() + "''," + 
							"''" + stb.toString() + "'') " ;
					String dosyaString = sbilgi.getSuperviser().equals("") ? sbilgi.getSuperviser() : sbilgi.getSuperviser() ;
					serverService.job_olustur_S("KUR_" +  sbilgi.getUser_prog_kodu() ,dosyaString, jobString ,sbilgi);
				}
				break;
			case "Kambiyo":
				hangi = hangiSQL  ;
				if( hangi.equals("MS SQL")) 
				{
					stb.append(" ALTER INDEX [IX_CEK] ON [dbo].[CEK] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					serverService.job_sil_S("OK_Kam" +  sbilgi.getUser_prog_kodu() + "_Index","", sbilgi);
					serverService.job_olustur_S("OK_Kam" +  sbilgi.getUser_prog_kodu() + "_Index","OK_Kam" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);
					serverService.job_baslat_S("OK_Kam" +  sbilgi.getUser_prog_kodu() + "_Index", sbilgi);
				}
				else if( hangi.equals("MY SQL")) 
				{
					stb.append(" OPTIMIZE TABLE OK_Kam" +  sbilgi.getUser_prog_kodu() + ".cek;") ; 
					serverService.job_sil_S("OK_Kam" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_kam" + sbilgi.getUser_prog_kodu()  , sbilgi); 
					serverService.job_olustur_S("OK_Kam" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_kam" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);	
				}
				else if( hangi.equals("PG SQL")) 
				{ 
					serverService.job_sil_S("KAMBIYO_" + sbilgi.getUser_prog_kodu() ,""   , sbilgi); 
					stb.append("REINDEX TABLE \"CEK\" ") ; 
					String jobString = "SELECT dblink(''dbname = ok_kam" +  sbilgi.getUser_prog_kodu() + " port = " + Global_Yardimci.ipCevir(sbilgi.getUser_server())[1] + " host = localhost user = " + sbilgi.getUser_server() + " password = " + sbilgi.getUser_pwd_server() + "''," + 
							"''" + stb.toString() + "'') " ;
					String dosyaString = sbilgi.getSuperviser().equals("") ? sbilgi.getSuperviser() : sbilgi.getSuperviser() ;
					serverService.job_olustur_S("KAMBIYO_" +  sbilgi.getUser_prog_kodu() ,dosyaString , jobString ,sbilgi);
				}
				break;
			case "Sms":
				hangi = hangiSQL  ;
				if( hangi.equals("MS SQL")) 
				{
					stb.append(" ALTER INDEX [IDX_SMS_HESAP] ON [dbo].[SMS_HESAP] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_HESAP] ON [dbo].[SMS_HESAP] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IDX_SMS_BILGILERI] ON [dbo].[SMS_BILGILERI] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_SID] ON [dbo].[SMS_BILGILERI] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IDX_MAIL_HESAP] ON [dbo].[MAIL_HESAP] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_MAIL] ON [dbo].[MAIL_HESAP] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IDX_MAIL_BILGILERI] ON [dbo].[MAIL_BILGILERI] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_MID] ON [dbo].[MAIL_BILGILERI] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					serverService.job_sil_S("OK_Sms" +  sbilgi.getUser_prog_kodu() + "_Index","", sbilgi);
					serverService.job_olustur_S("OK_Sms" +  sbilgi.getUser_prog_kodu() + "_Index","OK_Sms" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);
					serverService.job_baslat_S("OK_Sms" +  sbilgi.getUser_prog_kodu() + "_Index", sbilgi);
				}
				else if( hangi.equals("MY SQL")) 
				{
					stb.append(" OPTIMIZE TABLE OK_Sms" +  sbilgi.getUser_prog_kodu() + ".sms_hesap,") ; 
					stb.append("  OK_Sms" +  sbilgi.getUser_prog_kodu() + ".sms_bilgileri,") ; 
					stb.append("  OK_Sms" +  sbilgi.getUser_prog_kodu() + ".mail_hesap,") ; 
					stb.append("  OK_Sms" +  sbilgi.getUser_prog_kodu() + ".mail_bilgileri;") ; 
					serverService.job_sil_S("OK_Sms" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_sms" + sbilgi.getUser_prog_kodu()  , sbilgi);
					serverService.job_olustur_S("OK_Sms" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_sms" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);	
				}
				else if( hangi.equals("PG SQL")) 
				{ 
					serverService.job_sil_S("SMS_" + sbilgi.getUser_prog_kodu() ,""   , sbilgi); 
					stb.append("REINDEX TABLE \"MAIL_BILGILERI\" ;") ; 
					stb.append("REINDEX TABLE \"MAIL_HESAP\" ;") ; 
					stb.append("REINDEX TABLE \"SMS_BILGILERI\" ;") ; 
					stb.append("REINDEX TABLE \"SMS_HESAP\" ;") ; 
					String jobString = "SELECT dblink(''dbname = ok_sms" +  sbilgi.getUser_prog_kodu() + " port = " + Global_Yardimci.ipCevir(sbilgi.getUser_server())[1] + " host = localhost user = " + sbilgi.getUser_server() + " password = " + sbilgi.getUser_pwd_server() + "''," + 
							"''" + stb.toString() + "'') " ;
					String dosyaString = sbilgi.getSuperviser().equals("") ? sbilgi.getSuperviser() : sbilgi.getSuperviser() ;
					serverService.job_olustur_S("SMS_" +  sbilgi.getUser_prog_kodu() ,dosyaString , jobString ,sbilgi);
				}
				break;
			case "Gunluk":
				hangi = hangiSQL  ;
				if( hangi.equals("MS SQL")) 
				{
					stb.append(" ALTER INDEX [IDX_GUNLUK] ON [dbo].[GUNLUK] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_GOREV] ON [dbo].[GOREV] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					serverService.job_sil_S("OK_Gun" +  sbilgi.getUser_prog_kodu() + "_Index","", sbilgi);
					serverService.job_olustur_S("OK_Gun" +  sbilgi.getUser_prog_kodu() + "_Index","OK_Gun" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);
					serverService.job_baslat_S("OK_Gun" +  sbilgi.getUser_prog_kodu() + "_Index", sbilgi);
				}
				else if( hangi.equals("MY SQL")) 
				{
					stb.append(" OPTIMIZE TABLE OK_Gun" +  sbilgi.getUser_prog_kodu() + ".gunluk,") ; 
					stb.append("  OK_Gun" +  sbilgi.getUser_prog_kodu() + ".gorev;") ; 
					serverService.job_sil_S("OK_Gun" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_gun" + sbilgi.getUser_prog_kodu()  , sbilgi);
					serverService.job_olustur_S("OK_Gun" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_gun" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);	
				}
				else if( hangi.equals("PG SQL")) 
				{ 
					serverService.job_sil_S("GUNLUK_" + sbilgi.getUser_prog_kodu() ,""   , sbilgi); 
					stb.append("REINDEX TABLE \"GUNLUK\" ;") ; 
					stb.append("REINDEX TABLE \"GOREV\" ;") ; 
					String jobString = "SELECT dblink(''dbname = ok_gun" +  sbilgi.getUser_prog_kodu() + " port = " + Global_Yardimci.ipCevir(sbilgi.getUser_server())[1] + " host = localhost user = " + sbilgi.getUser_server() + " password = " + sbilgi.getUser_pwd_server() + "''," + 
							"''" + stb.toString() + "'') " ;
					String dosyaString = sbilgi.getSuperviser().equals("") ? sbilgi.getSuperviser() : sbilgi.getSuperviser() ;
					serverService.job_olustur_S("GUNLUK_" +  sbilgi.getUser_prog_kodu() ,dosyaString , jobString ,sbilgi);
				}
				break;
			case "Kereste":
				hangi = hangiSQL  ;
				if( hangi.equals("MS SQL")) 
				{
					stb.append(" ALTER INDEX [IX_GRP_I] ON [dbo].[KERESTE] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_GRP_II] ON [dbo].[KERESTE] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_KERESTE] ON [dbo].[KERESTE] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [PID] ON [dbo].[KERESTE] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					stb.append(" ALTER INDEX [IX_ACIKLAMA] ON [dbo].[ACIKLAMA] REBUILD PARTITION = ALL WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)") ; 
					serverService.job_sil_S("OK_Ker" +  sbilgi.getUser_prog_kodu() + "_Index","", sbilgi);
					serverService.job_olustur_S("OK_Ker" +  sbilgi.getUser_prog_kodu() + "_Index","OK_Ker" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);
					serverService.job_baslat_S("OK_Ker" +  sbilgi.getUser_prog_kodu() + "_Index", sbilgi);
				}
				else if( hangi.equals("MY SQL")) 
				{
					stb.append(" OPTIMIZE TABLE OK_Ker" +  sbilgi.getUser_prog_kodu() + ".kereste,") ; 
					stb.append("  OK_Ker" +  sbilgi.getUser_prog_kodu() + ".aciklama;") ; 
					serverService.job_sil_S("OK_Ker" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_ker" + sbilgi.getUser_prog_kodu()  , sbilgi);
					serverService.job_olustur_S("OK_Ker" +  sbilgi.getUser_prog_kodu() + "_Index","/ok_ker" +  sbilgi.getUser_prog_kodu() , stb.toString() ,sbilgi);	
				}
				else if( hangi.equals("PG SQL")) 
				{ 
					serverService.job_sil_S("KERESTE_" + sbilgi.getUser_prog_kodu() ,""   , sbilgi); 
					stb.append("REINDEX TABLE \"KERESTE\" ;") ; 
					stb.append("REINDEX TABLE \"DPN\" ;") ; 
					String jobString = "SELECT dblink(''dbname = ok_ker" +  sbilgi.getUser_prog_kodu() + " port = " + Global_Yardimci.ipCevir(sbilgi.getUser_server())[1] + " host = localhost user = " + sbilgi.getUser_server() + " password = " + sbilgi.getUser_pwd_server() + "''," + 
							"''" + stb.toString() + "'') " ;
					String dosyaString = sbilgi.getSuperviser().equals("") ? sbilgi.getSuperviser() : sbilgi.getSuperviser() ;
					serverService.job_olustur_S("KERESTE_" +  sbilgi.getUser_prog_kodu() ,dosyaString , jobString ,sbilgi);
				}
				break;
			}
			result = true;
		} catch (Exception ex) 
		{
			result = false;
		}
		return result;
	}

}