package com.hamit.obs.service.kereste;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.connection.ConnectionManager;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.dto.kereste.kerestedetayDTO;
import com.hamit.obs.dto.loglama.LoglamaDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.kereste.IKeresteDatabase;
import com.hamit.obs.repository.loglama.LoglamaRepository;

@Service
public class KeresteService {

	@Autowired
	private ConnectionManager masterConnectionManager;
	
	private LoglamaDTO loglamaDTO = new LoglamaDTO();
	
	@Autowired
	private LoglamaRepository loglamaRepository;
	
	private final KeresteDatabaseContext databaseStrategyContext;
	private IKeresteDatabase strategy;
	
	public KeresteService(KeresteDatabaseContext databaseStrategyContext) {
		this.databaseStrategyContext = databaseStrategyContext;
	}
	public void initialize() {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserSessionManager.removeUserByModul(useremail,"Kereste");
			this.strategy = databaseStrategyContext.getStrategy();
			masterConnectionManager.loadConnections("Kereste",useremail);
			UserSessionManager.addUserSession(useremail, "Kereste", masterConnectionManager.getConnection("Fatura", useremail));
		} else {
			throw new ServiceException("No authenticated user found in SecurityContext");
		}
	}
	
	public String[] conn_detail() {
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
		String[] detay = {"","",""};
		detay[0] = keresteConnDetails.getHangisql() ;
		detay[1] = keresteConnDetails.getDatabaseName() ;
		detay[2] = keresteConnDetails.getServerIp() ;
		return detay;
	}
	
	public String ker_firma_adi() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.ker_firma_adi(keresteConnDetails) ;
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> ker_kod_degisken_oku(String fieldd, String sno, String nerden){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.ker_kod_degisken_oku(fieldd,sno,nerden,keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public String urun_kod_degisken_ara(String fieldd, String sno, String nerden, String arama) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.urun_kod_degisken_ara(fieldd, sno, nerden, arama, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> ker_kod_alt_grup_degisken_oku(int sno){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.ker_kod_alt_grup_degisken_oku(sno,keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void urun_degisken_eski(String fieldd ,String degisken_adi ,String nerden ,String sno ,int ID ) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.urun_degisken_eski(fieldd, degisken_adi, nerden, sno, ID, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void urun_degisken_alt_grup_eski(String alt_grup ,int ana_grup ,int  ID) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.urun_degisken_alt_grup_eski(alt_grup, ana_grup, ID, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void urun_degisken_kayit(String fieldd, String nerden, String degisken_adi, String sira) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.urun_degisken_kayit(fieldd, nerden, degisken_adi, sira, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void urun_degisken_alt_grup_kayit(String alt_grup, int ana_grup) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.urun_degisken_alt_grup_kayit(alt_grup, ana_grup, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void urun_degisken_alt_grup_sil(int id) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.urun_degisken_alt_grup_sil(id, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void urun_kod_degisken_sil(String hangi_Y, String nerden, int sira) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.urun_kod_degisken_sil(hangi_Y, nerden, sira, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public boolean alt_grup_kontrol(int anagrp, int altgrp) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.alt_grup_kontrol(anagrp, altgrp, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void ker_firma_adi_kayit(String fadi) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.ker_firma_adi_kayit(fadi, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public String son_no_al(String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.son_no_al(cins, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public int evrak_no_al(String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.evrak_no_al(cins, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> ker_oku(String eno, String cins){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.ker_oku(eno,cins, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public String aciklama_oku(String evrcins, int satir, String evrno, String gircik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.aciklama_oku(evrcins, satir, evrno, gircik, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public String[] dipnot_oku(String ino, String cins, String gircik){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.dipnot_oku(ino, cins, gircik, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> paket_oku(String pno,String nerden){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.paket_oku(pno, nerden, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void ker_kaydet(kerestedetayDTO kerestedetayDTO,String mesajlog) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			
			loglamaDTO.setEvrak(kerestedetayDTO.getFisno());
			loglamaDTO.setmESAJ(mesajlog);
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, keresteConnDetails);
			
			strategy.ker_kaydet(kerestedetayDTO,keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void ker_giris_sil(String eno,String mesajlog) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			
			loglamaDTO.setEvrak(eno);
			loglamaDTO.setmESAJ(mesajlog);
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, keresteConnDetails);
			
			strategy.ker_giris_sil(eno, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void dipnot_sil(String ino, String cins, String gircik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.dipnot_sil(ino, cins, gircik, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void dipnot_yaz(String eno, String bir, String iki, String uc, String tip, String gircik, String usr) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.dipnot_yaz(eno, bir, iki, uc, tip, gircik, usr, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void aciklama_sil(String evrcins, String evrno, String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.aciklama_sil(evrcins, evrno, cins, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void aciklama_yaz(String evrcins, int satir, String evrno, String aciklama, String gircik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.aciklama_yaz(evrcins, satir, evrno, aciklama, gircik, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> ker_barkod_kod_oku(String sira){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.ker_barkod_kod_oku(sira, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void ker_cikis_sil(String eno,String mesajlog) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			
			loglamaDTO.setEvrak(eno);
			loglamaDTO.setmESAJ(mesajlog);
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, keresteConnDetails);
			
			strategy.ker_cikis_sil(eno, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public void ker_cikis_kaydet(kerestedetayDTO kerestedetayDTO,String mesajlog) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			
			loglamaDTO.setEvrak(kerestedetayDTO.getFisno());
			loglamaDTO.setmESAJ(mesajlog);
			loglamaDTO.setUser(Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName()));
			loglamaRepository.log_kaydet(loglamaDTO, keresteConnDetails);
			
			strategy.ker_cikis_kaydet(kerestedetayDTO, keresteConnDetails);
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
