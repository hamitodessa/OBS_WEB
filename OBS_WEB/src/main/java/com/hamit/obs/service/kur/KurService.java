package com.hamit.obs.service.kur;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.connection.ConnectionManager;
import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.dto.kur.kurgirisDTO;
import com.hamit.obs.dto.kur.kurraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.kur.IKurDatabase;
import com.hamit.obs.service.context.KurDatabaseContext;

@Service
public class KurService {

	@Autowired
	private ConnectionManager masterConnectionManager;

	private final KurDatabaseContext databaseStrategyContext;
	private IKurDatabase strategy;

	public KurService(KurDatabaseContext databaseStrategyContext) {
		this.databaseStrategyContext = databaseStrategyContext;
	}
	public void initialize() {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserSessionManager.removeUserByModul(useremail,modulTipi.KUR);
			this.strategy = databaseStrategyContext.getStrategy();
			masterConnectionManager.loadConnections(modulTipi.KUR,useremail);
			UserSessionManager.addUserSession(useremail, modulTipi.KUR, masterConnectionManager.getConnection(modulTipi.KUR, useremail));
		} else {
			throw new ServiceException("No authenticated user found in SecurityContext");
		}
	}

	public String[] conn_detail() {
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails kurConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KUR);
		String[] detay = {"","",""};
		detay[0] = kurConnDetails.getSqlTipi().getValue() ;
		detay[1] = kurConnDetails.getDatabaseName() ;
		detay[2] = kurConnDetails.getServerIp() ;
		return detay;
	}

	public ConnectionDetails conn_details() {
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails kurConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KUR);
		return kurConnDetails;
	}

	public List<Map<String, Object>> kur_liste(String tarih){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kurConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KUR);
			return strategy.kur_liste(tarih,kurConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public void kur_sil (String tarih,String kur_turu) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kurConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KUR);
			strategy.kur_sil(tarih,kur_turu,kurConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public boolean kur_kayit(kurgirisDTO kurgirisDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kurConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KUR);
			return strategy.kur_kayit(kurgirisDTO,kurConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> kur_rapor(kurraporDTO kurraporDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kurConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KUR);
			return strategy.kur_rapor(kurraporDTO,kurConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> kur_oku(kurgirisDTO kurgirisDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails kurConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.KUR);
			return strategy.kur_oku(kurgirisDTO,kurConnDetails);
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