package com.hamit.obs.service.adres;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.connection.ConnectionManager;
import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.dto.adres.adresDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.adres.IAdresDatabase;
import com.hamit.obs.service.context.AdresDatabaseContext;

@Service
public class AdresService {

	@Autowired
	private ConnectionManager masterConnectionManager;

	private final AdresDatabaseContext databaseStrategyContext;
	private IAdresDatabase strategy;
	public AdresService(AdresDatabaseContext databaseStrategyContext) {
		this.databaseStrategyContext = databaseStrategyContext;
	}

	public void initialize() {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserSessionManager.removeUserByModul(useremail,modulTipi.ADRES);
			this.strategy = databaseStrategyContext.getStrategy();
			masterConnectionManager.loadConnections(modulTipi.ADRES,useremail);
			UserSessionManager.addUserSession(useremail, modulTipi.ADRES, masterConnectionManager.getConnection(modulTipi.ADRES, useremail));
		} else {
			throw new ServiceException("No authenticated user found in SecurityContext");
		}
	}

	public String[] conn_detail() {
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails adresConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.ADRES);
		String[] detay = {"","",""};
		detay[0] = adresConnDetails.getSqlTipi().getValue() ;
		detay[1] = adresConnDetails.getDatabaseName() ;
		detay[2] = adresConnDetails.getServerIp() ;
		return detay;
	}
	public ConnectionDetails conn_details() {
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails adresConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.ADRES);
		return adresConnDetails;
	}

	public List<Map<String, Object>> hesap_kodlari(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails adresConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.ADRES);
			return strategy.hesap_kodlari(adresConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public adresDTO hsp_pln(String hesapisim) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails adresConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.ADRES);
			return strategy.hsp_pln(hesapisim, adresConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public String adres_firma_adi() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails adresConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.ADRES);
			return strategy.adres_firma_adi(adresConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void adres_kayit(adresDTO adresDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails adresConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.ADRES);
			strategy.adres_kayit(adresDTO,adresConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void adres_sil(int id) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails adresConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.ADRES);
			strategy.adres_sil(id,adresConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public String kod_ismi(String kodu) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails adresConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.ADRES);
			return strategy.kod_ismi(kodu,adresConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public String[] adr_etiket_arama_kod(String kodu) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails adresConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.ADRES);
			return strategy.adr_etiket_arama_kod(kodu,adresConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public void adres_firma_adi_kayit(String fadi) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails adresConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.ADRES);;
			strategy.adres_firma_adi_kayit(fadi,adresConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> adr_etiket(String siralama){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails adresConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.ADRES);
			return strategy.adr_etiket(siralama,adresConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> adr_hpl(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails adresConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.ADRES);
			return strategy.adr_hpl(adresConnDetails);
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