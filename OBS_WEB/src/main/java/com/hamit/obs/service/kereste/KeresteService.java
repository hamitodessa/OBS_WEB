package com.hamit.obs.service.kereste;

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
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.dto.kereste.kerestedetayDTO;
import com.hamit.obs.dto.kereste.kerestedetayraporDTO;
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
			UserSessionManager.addUserSession(useremail, "Kereste", masterConnectionManager.getConnection("Kereste", useremail));
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

	public List<Map<String, Object>> kod_pln(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.kod_pln(keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void kod_kayit(String kodu, String aciklama) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.kod_kayit(kodu,aciklama,keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void kod_sil(String kod) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.kod_sil(kod,keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> kons_pln(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.kons_pln(keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void kons_kayit(String kons, String aciklama,int paket_no) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.kons_kayit(kons,aciklama,paket_no,keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public int kons_sil(String kons) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.kons_sil(kons,keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> urun_detay(String pakno,String kons, String kodu,String evrak){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.urun_detay(pakno, kons, kodu, evrak, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public String kod_adi(String kod) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.kod_adi(kod, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public String kons_adi(String kons) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.kons_adi(kons, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void ker_kod_degis(String paket_No, String kon, String yenikod,int satir) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.ker_kod_degis(paket_No, kon, yenikod, satir, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void ker_kons_degis(String kons, String yenikons, int satir) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			strategy.ker_kons_degis(kons, yenikons, satir, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> stok_rapor(kerestedetayraporDTO kerestedetayraporDTO,Pageable pageable){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.stok_rapor(kerestedetayraporDTO,pageable, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public double stok_raporsize(kerestedetayraporDTO kerestedetayraporDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.stok_raporsize(kerestedetayraporDTO, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> baslik_bak(String baslik, String ordr, String jkj, String k1, String k2, String f1,
			String f2, String t1, String t2,String dURUM,String e1, String e2){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.baslik_bak(baslik, ordr, jkj, k1, k2, f1, f2, t1, t2, dURUM, e1, e2, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> grp_rapor(String gruplama,String sstr_2, String sstr_4, String kur_dos, String qwq6,
			String qwq7, String qwq8, String k1, String k2, String s1, String s2, String jkj,
			String t1, String t2, String sstr_5, String sstr_1,String orderBY,String dURUM,String ko1, String ko2,String dpo,String grup,
			String e1 , String e2,String ozelgrp[][],Set<String> sabitkolonlar){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.grp_rapor(gruplama, sstr_2, sstr_4, kur_dos, qwq6, qwq7, qwq8, k1, k2, s1, s2, jkj, t1, t2, sstr_5, sstr_1, orderBY, dURUM, ko1, ko2, dpo, grup, e1, e2, ozelgrp,sabitkolonlar, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> fat_rapor(kerestedetayraporDTO kerestedetayraporDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.fat_rapor(kerestedetayraporDTO, keresteConnDetails,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public double fat_raporsize(kerestedetayraporDTO kerestedetayraporDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.fat_raporsize(kerestedetayraporDTO, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> fat_detay_rapor(String fno , String turu){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.fat_detay_rapor(fno,turu, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> fat_rapor_fat_tar(kerestedetayraporDTO kerestedetayraporDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.fat_rapor_fat_tar(kerestedetayraporDTO, keresteConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> fat_rapor_cari_kod(kerestedetayraporDTO kerestedetayraporDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails keresteConnDetails =  UserSessionManager.getUserSession(useremail, "Kereste");
			return strategy.fat_rapor_cari_kod(kerestedetayraporDTO, keresteConnDetails);
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