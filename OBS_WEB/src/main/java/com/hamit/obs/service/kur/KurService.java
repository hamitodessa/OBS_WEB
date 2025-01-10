package com.hamit.obs.service.kur;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.connection.ConnectionManager;
import com.hamit.obs.dto.kur.kurgirisDTO;
import com.hamit.obs.dto.kur.kurraporDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.kur.IKurDatabase;

@Service
public class KurService {
	
	@Autowired
	private ConnectionManager masterConnectionManager;
	
	public ConnectionDetails kurConnDetails ;
	
	private final KurDatabaseContext databaseStrategyContext;
	private IKurDatabase strategy;
	
	public KurService(KurDatabaseContext databaseStrategyContext) {
		this.databaseStrategyContext = databaseStrategyContext;
	}
	public void initialize() {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			this.strategy = databaseStrategyContext.getStrategy();
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			masterConnectionManager.loadConnections("Kur",useremail);
			kurConnDetails = masterConnectionManager.getConnection("Kur", useremail);
		} else {
			throw new ServiceException("No authenticated user found in SecurityContext");
		}
	}

	public List<Map<String, Object>> kur_liste(String tarih){
		try {
			return strategy.kur_liste(tarih,kurConnDetails);
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
	public void kur_sil (String tarih,String kur_turu) {
		try {
			strategy.kur_sil(tarih,kur_turu,kurConnDetails);
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
	
	public boolean kur_kayit(kurgirisDTO kurgirisDTO) {
		try {
			return strategy.kur_kayit(kurgirisDTO,kurConnDetails);
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
	
	public List<Map<String, Object>> kur_rapor(kurraporDTO kurraporDTO) {
		try {
			
			return strategy.kur_rapor(kurraporDTO,kurConnDetails);
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
	
	public List<Map<String, Object>> kur_oku(kurgirisDTO kurgirisDTO) {
		try {
			return strategy.kur_oku(kurgirisDTO,kurConnDetails);
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
