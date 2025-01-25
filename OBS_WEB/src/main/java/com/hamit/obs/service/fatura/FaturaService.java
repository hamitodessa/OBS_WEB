package com.hamit.obs.service.fatura;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.connection.ConnectionManager;
import com.hamit.obs.dto.stok.urunDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.fatura.IFaturaDatabase;

@Service
public class FaturaService {

	@Autowired
	private ConnectionManager masterConnectionManager;
	
	
	private final FaturaDatabaseContext databaseStrategyContext;
	private IFaturaDatabase strategy;
	
	public FaturaService(FaturaDatabaseContext databaseStrategyContext) {
		this.databaseStrategyContext = databaseStrategyContext;
	}
	public void initialize() {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserSessionManager.removeUserByModul(useremail,"Fatura");
			this.strategy = databaseStrategyContext.getStrategy();
			masterConnectionManager.loadConnections("Fatura",useremail);
			UserSessionManager.addUserSession(useremail, "Fatura", masterConnectionManager.getConnection("Fatura", useremail));
		} else {
			throw new ServiceException("No authenticated user found in SecurityContext");
		}
	}
	
	public String[] conn_detail() {
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails faturaConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
		String[] detay = {"","",""};
		detay[0] = faturaConnDetails.getHangisql() ;
		detay[1] = faturaConnDetails.getDatabaseName() ;
		detay[2] = faturaConnDetails.getServerIp() ;
		return detay;
	}
	
	public String fat_firma_adi() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.fat_firma_adi(fatConnDetails) ;
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	public List<Map<String, Object>> urun_kodlari(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.urun_kodlari(fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public urunDTO stk_urun(String sira, String arama){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.stk_urun(sira,arama, fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public String urun_kod_degisken_ara(String fieldd,String sno,String nerden,String arama) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.urun_kod_degisken_ara(fieldd,sno,nerden,arama, fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public List<Map<String, Object>> stk_kod_degisken_oku(String fieldd, String sno, String nerden){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.stk_kod_degisken_oku(fieldd,sno,nerden,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public List<Map<String, Object>> stk_kod_alt_grup_degisken_oku (int sno){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.stk_kod_alt_grup_degisken_oku(sno,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	public String ur_kod_bak(String kodu) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.ur_kod_bak(kodu,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void stk_ur_sil(String kodu) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.stk_ur_sil(kodu,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void stk_ur_kayit(urunDTO urunDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.stk_ur_kayit(urunDTO,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void stk_firma_adi_kayit(String fadi) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.stk_firma_adi_kayit(fadi,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	public String uret_son_bordro_no_al() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.uret_son_bordro_no_al(fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public List<Map<String, Object>> stok_oku(String eno, String cins){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.stok_oku(eno,cins,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	public String aciklama_oku(String evrcins, int satir, String evrno, String gircik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.aciklama_oku(evrcins, satir,evrno,gircik,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public urunDTO urun_adi_oku (String kodu,String kodbarcode) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.urun_adi_oku(kodu, kodbarcode,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public double son_imalat_fiati_oku(String kodu) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.son_imalat_fiati_oku(kodu,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public String uret_ilk_tarih(String baslangic, String tar, String ukodu) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.uret_ilk_tarih(baslangic,tar,ukodu,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	public double gir_ort_fiati_oku(String kodu, String ilkt, String tarih) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.gir_ort_fiati_oku(kodu,ilkt,tarih,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public int uretim_fisno_al() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.uretim_fisno_al(fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public List<Map<String, Object>> recete_oku(String rno){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.recete_oku(rno,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void stok_sil(String eno, String ecins, String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.stok_sil(eno,ecins,cins,fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void stk_kaydet(String evrno, String evrcins, String tarih, int depo, String urnkodu, double miktar,
			double fiat, double tutar, double kdvlitut, String hareket, String izah, int anagrp, int altgrp, double kur,
			String b1, String doviz, String hspkodu, String usr) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.stk_kaydet(evrno,evrcins,tarih,depo,urnkodu,miktar,fiat,tutar,kdvlitut,hareket,izah,anagrp,altgrp,kur,
					b1,doviz,hspkodu,usr,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void aciklama_yaz(String evrcins, int satir, String evrno, String aciklama, String gircik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.aciklama_yaz(evrcins,satir,evrno,aciklama,gircik,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void aciklama_sil(String evrcins,String evrno,String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.aciklama_sil(evrcins,evrno,cins,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public List<Map<String, Object>> urun_arama(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.urun_arama(fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void urun_degisken_eski(String fieldd ,String degisken_adi ,String nerden ,String sno ,int ID ) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.urun_degisken_eski(fieldd,degisken_adi,nerden,sno,ID,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void urun_degisken_alt_grup_eski(String alt_grup ,int ana_grup ,int  ID) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.urun_degisken_alt_grup_eski(alt_grup,ana_grup,ID,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void urun_degisken_kayit(String fieldd  ,String nerden,String degisken_adi,String sira) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.urun_degisken_kayit(fieldd,nerden,degisken_adi,sira,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void  urun_degisken_alt_grup_kayit (String alt_grup , int ana_grup) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.urun_degisken_alt_grup_kayit(alt_grup,ana_grup,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public boolean alt_grup_kontrol(int anagrp,int altgrp) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.alt_grup_kontrol(anagrp,altgrp,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void urun_degisken_alt_grup_sil(int id) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.urun_degisken_alt_grup_sil(id,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void urun_kod_degisken_sil(String hangi_Y,String nerden,int sira) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.urun_kod_degisken_sil(hangi_Y,nerden,sira,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	public double son_satis_fiati_oku(String kodu,String muskodu,String gircik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.son_satis_fiati_oku(kodu,muskodu,gircik,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public List<Map<String, Object>> fatura_oku(String fno,String cins){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.fatura_oku(fno,cins,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public String[] dipnot_oku(String ino,String cins ,String gircik){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.dipnot_oku(ino,cins,gircik,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public String son_no_al(String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.son_no_al(cins,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public int fatura_no_al(String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.fatura_no_al(cins,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void fat_giris_sil(String fno,String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.fat_giris_sil(fno,cins,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void dipnot_sil(String ino,String cins,String gircik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.dipnot_sil(ino,cins,gircik,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public List<Map<String, Object>> fat_oz_kod (String cins){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			return strategy.fat_oz_kod(cins,fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void fat_kaydet(String fatno ,String kodu ,int depo ,double  fiat ,double tevkifat  
			, double miktar ,String gircik ,double tutar,double iskonto ,double kdv  
			, String tarih, String izah,String doviz,String  adrfirma ,String carfirma  
			, String ozkod ,double kur ,String cins,int  anagrp,int  altgrp ,String usr) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.fat_kaydet(fatno, kodu, depo, fiat, tevkifat, miktar, gircik, tutar, iskonto, kdv, tarih, izah, doviz, adrfirma, carfirma, ozkod, kur, cins, anagrp, altgrp, usr, fatConnDetails);		
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void dipnot_yaz(String eno,String bir,String iki,String uc,String tip,String gircik,String usr) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, "Fatura");
			strategy.dipnot_yaz(eno, bir, iki, uc, tip, gircik, usr, fatConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}

	}
}
