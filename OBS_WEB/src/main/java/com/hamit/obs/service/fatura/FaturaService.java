package com.hamit.obs.service.fatura;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.connection.ConnectionManager;
import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.dto.loglama.LoglamaDTO;
import com.hamit.obs.dto.stok.urunDTO;
import com.hamit.obs.dto.stok.raporlar.envanterDTO;
import com.hamit.obs.dto.stok.raporlar.fatraporDTO;
import com.hamit.obs.dto.stok.raporlar.grupraporDTO;
import com.hamit.obs.dto.stok.raporlar.imaraporDTO;
import com.hamit.obs.dto.stok.raporlar.stokdetayDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.fatura.IFaturaDatabase;
import com.hamit.obs.repository.loglama.LoglamaRepository;
import com.hamit.obs.service.context.FaturaDatabaseContext;

@Service
public class FaturaService {

	@Autowired
	private ConnectionManager masterConnectionManager;
	
	private LoglamaDTO loglamaDTO = new LoglamaDTO();
	
	@Autowired
	private LoglamaRepository loglamaRepository;
	
	private final FaturaDatabaseContext databaseStrategyContext;
	private IFaturaDatabase strategy;
	
	public FaturaService(FaturaDatabaseContext databaseStrategyContext) {
		this.databaseStrategyContext = databaseStrategyContext;
	}
	public void initialize() {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserSessionManager.removeUserByModul(useremail,modulTipi.FATURA);
			this.strategy = databaseStrategyContext.getStrategy();
			masterConnectionManager.loadConnections(modulTipi.FATURA,useremail);
			UserSessionManager.addUserSession(useremail, modulTipi.FATURA, masterConnectionManager.getConnection(modulTipi.FATURA, useremail));
		} else {
			throw new ServiceException("No authenticated user found in SecurityContext");
		}
	}
	
	public String[] conn_detail() {
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails faturaConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
		String[] detay = {"","",""};
		detay[0] = faturaConnDetails.getSqlTipi().getValue() ;
		detay[1] = faturaConnDetails.getDatabaseName() ;
		detay[2] = faturaConnDetails.getServerIp() ;
		return detay;
	}
	
	public String fat_firma_adi() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.fat_firma_adi(fatConnDetails) ;
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public List<Map<String, Object>> urun_kodlari(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.urun_kodlari(fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public urunDTO stk_urun(String sira, String arama){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.stk_urun(sira,arama, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public String urun_kod_degisken_ara(String fieldd,String sno,String nerden,String arama) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.urun_kod_degisken_ara(fieldd,sno,nerden,arama, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> stk_kod_degisken_oku(String fieldd, String sno, String nerden){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.stk_kod_degisken_oku(fieldd,sno,nerden,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> stk_kod_alt_grup_degisken_oku (int sno){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.stk_kod_alt_grup_degisken_oku(sno,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public String ur_kod_bak(String kodu) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.ur_kod_bak(kodu,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void stk_ur_sil(String kodu) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			strategy.stk_ur_sil(kodu,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void stk_ur_kayit(urunDTO urunDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			strategy.stk_ur_kayit(urunDTO,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void stk_firma_adi_kayit(String fadi) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			
			loglamaDTO.setEvrak("");
			loglamaDTO.setmESAJ("Firma Ismi :" + fadi);
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, fatConnDetails);
			
			strategy.stk_firma_adi_kayit(fadi,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public String uret_son_bordro_no_al() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.uret_son_bordro_no_al(fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> stok_oku(String eno, String cins){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.stok_oku(eno,cins,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public String aciklama_oku(String evrcins, int satir, String evrno, String gircik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.aciklama_oku(evrcins, satir,evrno,gircik,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public urunDTO urun_adi_oku (String kodu,String kodbarcode) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.urun_adi_oku(kodu, kodbarcode,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public double son_imalat_fiati_oku(String kodu) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.son_imalat_fiati_oku(kodu,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public String uret_ilk_tarih(String baslangic, String tar, String ukodu) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.uret_ilk_tarih(baslangic,tar,ukodu,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public double gir_ort_fiati_oku(String kodu, String ilkt, String tarih) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.gir_ort_fiati_oku(kodu,ilkt,tarih,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public int uretim_fisno_al() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.uretim_fisno_al(fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> recete_oku(String rno){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.recete_oku(rno,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void stok_sil(String eno, String ecins, String cins,String mesajlog) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			
			loglamaDTO.setEvrak(eno);
			loglamaDTO.setmESAJ(mesajlog);
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, fatConnDetails);
			
			strategy.stok_sil(eno,ecins,cins,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void stk_kaydet(String evrno, String evrcins, String tarih, int depo, String urnkodu, double miktar,
			double fiat, double tutar, double kdvlitut, String hareket, String izah, int anagrp, int altgrp, double kur,
			String b1, String doviz, String hspkodu, String usr,String mesajlog) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			
			loglamaDTO.setEvrak(evrno);
			loglamaDTO.setmESAJ(mesajlog);
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, fatConnDetails);
			
			strategy.stk_kaydet(evrno,evrcins,tarih,depo,urnkodu,miktar,fiat,tutar,kdvlitut,hareket,izah,anagrp,altgrp,kur,
					b1,doviz,hspkodu,usr,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void aciklama_yaz(String evrcins, int satir, String evrno, String aciklama, String gircik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			
			loglamaDTO.setEvrak(evrno);
			loglamaDTO.setmESAJ("Fatura Aciklama Yaz : " + evrcins + " - " + aciklama );
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, fatConnDetails);
			
			strategy.aciklama_yaz(evrcins,satir,evrno,aciklama,gircik,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void aciklama_sil(String evrcins,String evrno,String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			
			loglamaDTO.setEvrak(evrno);
			loglamaDTO.setmESAJ("Fatura Aciklama Sil " + evrcins + " - " + cins);
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, fatConnDetails);
			
			strategy.aciklama_sil(evrcins,evrno,cins,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> urun_arama(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.urun_arama(fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void urun_degisken_eski(String fieldd ,String degisken_adi ,String nerden ,String sno ,int ID ) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			strategy.urun_degisken_eski(fieldd,degisken_adi,nerden,sno,ID,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void urun_degisken_alt_grup_eski(String alt_grup ,int ana_grup ,int  ID) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			strategy.urun_degisken_alt_grup_eski(alt_grup,ana_grup,ID,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void urun_degisken_kayit(String fieldd  ,String nerden,String degisken_adi,String sira) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			strategy.urun_degisken_kayit(fieldd,nerden,degisken_adi,sira,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void  urun_degisken_alt_grup_kayit (String alt_grup , int ana_grup) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			strategy.urun_degisken_alt_grup_kayit(alt_grup,ana_grup,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public boolean alt_grup_kontrol(int anagrp,int altgrp) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.alt_grup_kontrol(anagrp,altgrp,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void urun_degisken_alt_grup_sil(int id) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			
			loglamaDTO.setEvrak("");
			loglamaDTO.setmESAJ("Alt Grup Silme:" + id);
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, fatConnDetails);
			
			strategy.urun_degisken_alt_grup_sil(id,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void urun_kod_degisken_sil(String hangi_Y,String nerden,int sira) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			loglamaDTO.setEvrak("");
			loglamaDTO.setmESAJ(nerden + " Silme:" + sira);
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, fatConnDetails);
			
			strategy.urun_kod_degisken_sil(hangi_Y,nerden,sira,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public double son_satis_fiati_oku(String kodu,String muskodu,String gircik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.son_satis_fiati_oku(kodu,muskodu,gircik,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> fatura_oku(String fno,String cins){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.fatura_oku(fno,cins,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> irsaliye_oku(String irsno, String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails = UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.irsaliye_oku(irsno, cins, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public String[] dipnot_oku(String ino,String cins ,String gircik){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.dipnot_oku(ino,cins,gircik,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public String son_no_al(String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.son_no_al(cins,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public String son_irsno_al(String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails = UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.son_irsno_al(cins, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public int fatura_no_al(String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.fatura_no_al(cins,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public int irsaliye_no_al(String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails = UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.irsaliye_no_al(cins, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void fat_giris_sil(String fno,String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			
			loglamaDTO.setEvrak(fno);
			loglamaDTO.setmESAJ(fno + " Nolu Giris Fatura Silindi");
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, fatConnDetails);
			
			strategy.fat_giris_sil(fno,cins,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void irs_giris_sil(String fno, String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails = UserSessionManager.getUserSession(useremail, modulTipi.FATURA);

			loglamaDTO.setEvrak(fno);
			loglamaDTO.setmESAJ(fno + " Nolu Giris Irsaliye Silindi");
			loglamaDTO.setUser(
					Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, fatConnDetails);

			strategy.irs_giris_sil(fno, cins, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void dipnot_sil(String ino,String cins,String gircik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			loglamaDTO.setEvrak(ino);
			loglamaDTO.setmESAJ("Fatura Dip Not Sil "  + ino);
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, fatConnDetails);
			
			strategy.dipnot_sil(ino,cins,gircik,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> fat_oz_kod (String cins){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.fat_oz_kod(cins,fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void fat_kaydet(String fatno ,String kodu ,int depo ,double  fiat ,double tevkifat  
			, double miktar ,String gircik ,double tutar,double iskonto ,double kdv  
			, String tarih, String izah,String doviz,String  adrfirma ,String carfirma  
			, String ozkod ,double kur ,String cins,int  anagrp,int  altgrp ,String usr) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			loglamaDTO.setEvrak(fatno);
			loglamaDTO.setmESAJ(gircik + " Fatura Kayit " +  kodu + " Mik=" + miktar + " Tut=" + tutar);
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, fatConnDetails);
			strategy.fat_kaydet(fatno, kodu, depo, fiat, tevkifat, miktar, gircik, tutar, iskonto, kdv, tarih, izah, doviz, adrfirma, carfirma, ozkod, kur, cins, anagrp, altgrp, usr, fatConnDetails);		
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void irs_kaydet(String irsno, String kodu, int depo, double fiat, double tevkifat, double miktar,
			String gircik, double tutar, double iskonto, double kdv, String tarih, String izah, String doviz,
			String adrfirma, String carfirma, String ozkod, double kur, String cins, int anagrp, int altgrp,
			String usr, String fatno, String sevktarih) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails = UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			loglamaDTO.setEvrak(fatno);
			loglamaDTO.setmESAJ(gircik + " Irsaliye Kayit " + kodu + " Mik=" + miktar + " Tut=" + tutar);
			loglamaDTO.setUser(
					Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, fatConnDetails);
			strategy.irs_kaydet(irsno, kodu, depo, fiat, tevkifat, miktar, gircik, tutar, iskonto, kdv, tarih, izah,
					doviz, adrfirma, carfirma, ozkod, kur, cins, anagrp, altgrp, usr, fatno, sevktarih, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void dipnot_yaz(String eno,String bir,String iki,String uc,String tip,String gircik,String usr) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			loglamaDTO.setEvrak(eno);
			loglamaDTO.setmESAJ("Fatura Dip Not Yaz : "  + bir);
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, fatConnDetails);
			strategy.dipnot_yaz(eno, bir, iki, uc, tip, gircik, usr, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public String recete_son_bordro_no_al() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.recete_son_bordro_no_al(fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public int recete_no_al() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.recete_no_al(fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void rec_sil(String rno) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			strategy.rec_sil(rno,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void kod_recete_yaz(String ukodu,String rec) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			strategy.kod_recete_yaz(ukodu,rec,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void recete_kayit(String recno,boolean durum,String tur,String kodu ,double miktar ,int anagrp,int altgrup ,String usr) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			strategy.recete_kayit(recno, durum, tur, kodu, miktar, anagrp, altgrup, usr, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public String zayi_son_bordro_no_al() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.zayi_son_bordro_no_al(fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public int zayi_fisno_al() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.zayi_fisno_al(fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> zayi_oku(String eno,String cins){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.zayi_oku(eno,cins,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> fat_rapor(fatraporDTO fatraporDTO,Pageable pageable){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.fat_rapor(fatraporDTO,pageable,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> irs_rapor(fatraporDTO fatraporDTO, Pageable pageable) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails = UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.irs_rapor(fatraporDTO, pageable, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public double fat_raporsize(fatraporDTO fatraporDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.fat_raporsize(fatraporDTO,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public double irs_raporsize(fatraporDTO fatraporDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails = UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.irs_raporsize(fatraporDTO, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> fat_detay_rapor(String fno , String turu){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.fat_detay_rapor(fno,turu,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> irs_detay_rapor(String fno, String turu) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails = UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.irs_detay_rapor(fno, turu, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> fat_rapor_fat_tar(fatraporDTO fatraporDTO,Pageable pageable){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.fat_rapor_fat_tar(fatraporDTO,pageable ,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> fat_rapor_cari_kod(fatraporDTO fatraporDTO,Pageable pageable){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.fat_rapor_cari_kod(fatraporDTO,pageable ,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> imalat_rapor(imaraporDTO imaraporDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.imalat_rapor(imaraporDTO,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> envanter_rapor(envanterDTO envanterDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.envanter_rapor(envanterDTO,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> envanter_rapor_fifo(envanterDTO envanterDTO ){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.envanter_rapor_fifo(envanterDTO,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> envanter_rapor_fifo_2(envanterDTO envanterDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.envanter_rapor_fifo_2(envanterDTO,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public double envanter_rapor_lifo(envanterDTO envanterDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.envanter_rapor_lifo(envanterDTO,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> envanter_rapor_u_kodu_oncekitarih(envanterDTO envanterDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.envanter_rapor_u_kodu_oncekitarih(envanterDTO,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> envanter_rapor_u_kodu(envanterDTO envanterDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.envanter_rapor_u_kodu(envanterDTO,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> envanter_rapor_ana_grup_alt_grup(envanterDTO envanterDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.envanter_rapor_ana_grup_alt_grup(envanterDTO,fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> baslik_bak(String baslik,String ordr,String jkj,String ch1,String k1,String k2,String f1,String f2,String t1,String t2){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.baslik_bak(baslik, ordr, jkj, ch1, k1, k2, f1, f2, t1, t2, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> grp_urn_kodlu(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][], Set<String> sabitKolonlar){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.grp_urn_kodlu(grupraporDTO, sstr_2, sstr_4, kur_dos, jkj, ch1, jkj1, sstr_5, sstr_1, ozelgrp,sabitKolonlar, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> grp_urn_kodlu_yil(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][], Set<String> sabitKolonlar){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.grp_urn_kodlu_yil(grupraporDTO, sstr_2, sstr_4, kur_dos, jkj, ch1, jkj1, sstr_5, sstr_1, ozelgrp,sabitKolonlar, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> grp_mus_kodlu(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][], Set<String> sabitKolonlar){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			ConnectionDetails cariConnDetails = UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.grp_mus_kodlu(grupraporDTO, sstr_2, sstr_4, kur_dos, jkj, ch1, jkj1, sstr_5, sstr_1, ozelgrp,sabitKolonlar, fatConnDetails,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> grp_mus_kodlu_yil(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][], Set<String> sabitKolonlar){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			ConnectionDetails cariConnDetails = UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.grp_mus_kodlu_yil(grupraporDTO, sstr_2, sstr_4, kur_dos, jkj, ch1, jkj1, sstr_5, sstr_1, ozelgrp,sabitKolonlar, fatConnDetails,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> grp_yil_ay(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][], Set<String> sabitKolonlar){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.grp_yil_ay(grupraporDTO, sstr_2, sstr_4, kur_dos, jkj, ch1, jkj1, sstr_5, sstr_1, ozelgrp,sabitKolonlar, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> grp_yil(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][], Set<String> sabitKolonlar){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.grp_yil(grupraporDTO, sstr_2, sstr_4, kur_dos, jkj, ch1, jkj1, sstr_5, sstr_1, ozelgrp,sabitKolonlar, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> grp_ana_grup(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][], Set<String> sabitKolonlar){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.grp_ana_grup(grupraporDTO, sstr_2, sstr_4, kur_dos, jkj, ch1, jkj1, sstr_5, sstr_1, ozelgrp,sabitKolonlar, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> grp_ana_grup_yil(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][], Set<String> sabitKolonlar){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.grp_ana_grup_yil(grupraporDTO, sstr_2, sstr_4, kur_dos, jkj, ch1, jkj1, sstr_5, sstr_1, ozelgrp,sabitKolonlar, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> ima_baslik_bak(String bas ,String jkj,String ch1,String qwq6,
			String qwq7,String qwq8,String qwq9,String k1,String k2,String t1,String t2,String ordrr){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.ima_baslik_bak(bas, jkj, ch1, qwq6, qwq7, qwq8, qwq9, k1, k2, t1, t2, ordrr, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> ima_alt_kod(String slct,String sstr_5,String sstr_2,String sstr_4,String jkj,String ch1,String qwq6,
			String qwq7,String qwq8,String qwq9,String s1 ,String s2,String k1,String k2,String t1,String t2,
			String sstr_1,String ordrr,String sstr_55,String ozelgrp[][], Set<String> sabitKolonlar){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.ima_alt_kod(slct, sstr_5, sstr_2, sstr_4, jkj, ch1, qwq6, qwq7, qwq8, qwq9, s1, s2, k1, k2, t1, t2, sstr_1, ordrr, sstr_55, ozelgrp,sabitKolonlar, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> stok_rapor(stokdetayDTO stokdetayDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails fatConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.FATURA);
			return strategy.stok_rapor(stokdetayDTO, fatConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	private String errorMessages(ServiceException e) {
		String originalMessage = e.getMessage();
		Throwable cause = e.getCause();
		String detailedMessage = originalMessage;
		if (cause != null) {
			detailedMessage += " - " + cause.getMessage();
		}
		return detailedMessage;
	}
}