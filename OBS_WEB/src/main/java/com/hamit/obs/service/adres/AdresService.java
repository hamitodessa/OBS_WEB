package com.hamit.obs.service.adres;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.connection.ConnectionManager;
import com.hamit.obs.dto.adres.adresDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.adres.IAdresDatabase;

@Service
public class AdresService {

	@Autowired
	private ConnectionManager masterConnectionManager;
	
	private ConnectionDetails adresConnDetails ;
	
	private final AdresDatabaseContext databaseStrategyContext;
	private IAdresDatabase strategy;
	public AdresService(AdresDatabaseContext databaseStrategyContext) {
		this.databaseStrategyContext = databaseStrategyContext;
	}
	public void initialize() {
		try {
			if (SecurityContextHolder.getContext().getAuthentication() != null) {
				this.strategy = databaseStrategyContext.getStrategy();
				String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
				masterConnectionManager.loadConnections("Adres",useremail);
				adresConnDetails = masterConnectionManager.getConnection("Adres", useremail);
			} else {
				throw new ServiceException("No authenticated user found in SecurityContext");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String[] conn_detail() {
		initialize();
		String[] detay = {"","",""};
		detay[0] = adresConnDetails.getHangisql() ;
		detay[1] = adresConnDetails.getDatabaseName() ;
		detay[2] = adresConnDetails.getServerIp() ;
		return detay;
	}
	public ConnectionDetails conn_details() {
		initialize();
		return adresConnDetails;
	}


	
	public List<Map<String, Object>> hesap_kodlari(){
		try {
			initialize();
			return strategy.hesap_kodlari(adresConnDetails);
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

	public adresDTO hsp_pln(String hesap){
		try {
			initialize();
			return strategy.hsp_pln(hesap,adresConnDetails);
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

	public String adres_firma_adi() {
		try {
			initialize();
			return strategy.adres_firma_adi(adresConnDetails);
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

	public void adres_kayit(adresDTO adresDTO) {
		try {
			initialize();
			strategy.adres_kayit(adresDTO,adresConnDetails);
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
	
	public void adres_sil(int id) {
		try {
			initialize();
			strategy.adres_sil(id,adresConnDetails);
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
	
	public String kod_ismi(String kodu) {
		try {
			initialize();
			return strategy.kod_ismi(kodu,adresConnDetails);
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
	
	public String[] adr_etiket_arama_kod(String kodu) {
		try {
			initialize();
			return strategy.adr_etiket_arama_kod(kodu,adresConnDetails);
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
	public void adres_firma_adi_kayit(String fadi) {
		try {
			initialize();
			strategy.adres_firma_adi_kayit(fadi,adresConnDetails);
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
	
	public List<Map<String, Object>> adr_etiket(String siralama){
		try {
			initialize();
			return strategy.adr_etiket(siralama,adresConnDetails);
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
