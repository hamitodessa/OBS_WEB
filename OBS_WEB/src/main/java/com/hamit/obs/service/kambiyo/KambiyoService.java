package com.hamit.obs.service.kambiyo;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.connection.ConnectionManager;
import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.custom.yardimci.Formatlama;
import com.hamit.obs.dto.kambiyo.bordrodetayDTO;
import com.hamit.obs.dto.kambiyo.cekraporDTO;
import com.hamit.obs.dto.loglama.LoglamaDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.kambiyo.IKambiyoDatabase;
import com.hamit.obs.repository.loglama.LoglamaRepository;

@Service
public class KambiyoService {

	@Autowired
	private LoglamaRepository loglamaRepository;

	private LoglamaDTO loglamaDTO = new LoglamaDTO();

	@Autowired
	private ConnectionManager masterConnectionManager;

	private final KambiyoDatabaseContext databaseStrategyContext;
	private IKambiyoDatabase strategy;
	public KambiyoService(KambiyoDatabaseContext databaseStrategyContext) {
		this.databaseStrategyContext = databaseStrategyContext;
	}
	public void initialize() {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserSessionManager.removeUserByModul(useremail,modulTipi.KAMBIYO);
			this.strategy = databaseStrategyContext.getStrategy();
			masterConnectionManager.loadConnections(modulTipi.KAMBIYO,useremail);
			UserSessionManager.addUserSession(useremail, modulTipi.KAMBIYO, masterConnectionManager.getConnection(modulTipi.KAMBIYO, useremail));
		} else {
			throw new ServiceException("No authenticated user found in SecurityContext");
		}
	}

	public String[] conn_detail() {
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
		String[] detay = {"","",""};
		detay[0] = kambiyoConnDetails.getHangisql() ;
		detay[1] = kambiyoConnDetails.getDatabaseName() ;
		detay[2] = kambiyoConnDetails.getServerIp() ;
		return detay;
	}

	public List<Map<String, Object>> ozel_kodlar(String gir_cik){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			return strategy.ozel_kodlar(gir_cik,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public List<Map<String, Object>> banka_sube(String nerden){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			return strategy.banka_sube(nerden,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public String kambiyo_firma_adi() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			return strategy.kambiyo_firma_adi(kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public int kam_son_bordro_no_al(String cek_sen, String gir_cik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			return strategy.kam_son_bordro_no_al(cek_sen,gir_cik,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> bordroOku(String bordroNo, String cek_sen, String gir_cik){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			return strategy.bordroOku(bordroNo, cek_sen, gir_cik,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public String kam_aciklama_oku(String cek_sen,int satir,String bordroNo,String gircik){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			return strategy.kam_aciklama_oku(cek_sen, satir, bordroNo,gircik ,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void  bordro_sil(String bordroNo,String cek_sen,String gir_cik,String user) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			loglamaDTO.setEvrak(String.valueOf(bordroNo));
			loglamaDTO.setmESAJ(String.valueOf(bordroNo) + " Bordro Silme");
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, kambiyoConnDetails);
			strategy.bordro_sil(bordroNo, cek_sen,gir_cik ,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void  cek_kayit(bordrodetayDTO row,String user) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			loglamaDTO.setEvrak(String.valueOf(row.getGirisBordro()));
			loglamaDTO.setmESAJ(String.valueOf(row.getGirisBordro()) + " Nolu Giris Bordro  " + row.getCekNo() + " Nolu Cek " + " Tutar:" + Formatlama.doub_2(row.getTutar()) + " " + row.getCins());
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, kambiyoConnDetails);
			strategy.cek_kayit(row ,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void  kam_aciklama_yaz(String cek_sen, int satir, String bordroNo, String aciklama, String gircik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			strategy.kam_aciklama_yaz(cek_sen, satir,bordroNo,aciklama,gircik ,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void  kam_aciklama_sil(String cek_sen, String bordroNo, String gircik) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			strategy.kam_aciklama_sil(cek_sen, bordroNo,gircik ,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public int kam_bordro_no_al(String cins) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			return strategy.kam_bordro_no_al(cins,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> kalan_cek_liste(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			return strategy.kalan_cek_liste(kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public String cek_kontrol(String cekno) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			return strategy.cek_kontrol(cekno,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public bordrodetayDTO cek_dokum(String cekno) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			return strategy.cek_dokum(cekno,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void bordro_cikis_sil(String bordroNo, String cek_sen,String user) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			loglamaDTO.setEvrak(bordroNo);
			loglamaDTO.setmESAJ(bordroNo + " Nolu Cikis Bordro Numaralari Silme ");
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, kambiyoConnDetails);
			strategy.bordro_cikis_sil(bordroNo,cek_sen ,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void bordro_cikis_yaz(String cek_sen, String ceksencins_where, String cekno, String cmus, String cbor,
			String ctar, String ozkod) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			strategy.bordro_cikis_yaz(cek_sen, ceksencins_where, cekno,cmus,cbor,ctar,ozkod,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> cek_rapor(cekraporDTO cekraporDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			return strategy.cek_rapor(cekraporDTO,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public void kambiyo_firma_adi_kayit(String fadi) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			strategy.kambiyo_firma_adi_kayit(fadi,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public bordrodetayDTO cektakipkontrol(String cekno) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			return strategy.cektakipkontrol(cekno,kambiyoConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void kam_durum_yaz(String cekno, String ceksen_from, String ceksen_where, String durum, String ttarih) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kambiyoConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KAMBIYO);
			strategy.kam_durum_yaz(cekno,ceksen_from,ceksen_where,durum,ttarih,kambiyoConnDetails);
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