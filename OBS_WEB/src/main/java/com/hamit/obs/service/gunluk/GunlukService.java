package com.hamit.obs.service.gunluk;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.gunluk.IGunlukDatabase;
import com.hamit.obs.service.context.GunlukDatabaseContext;

@Service
public class GunlukService {

	private final GunlukDatabaseContext databaseStrategyContext;
	private IGunlukDatabase strategy;
	public GunlukService(GunlukDatabaseContext databaseStrategyContext) {
		this.databaseStrategyContext = databaseStrategyContext;
	}
	
	public void initialize() {
	    var auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth == null)
	        throw new ServiceException("No authenticated user found in SecurityContext");
	    String email = auth.getName();
	    ConnectionDetails cd =
	            UserSessionManager.getUserSession(email, modulTipi.GUNLUK);
	    if (cd == null)
	        throw new ServiceException("Gunluk bağlantısı bulunamadı: " + email);
	    this.strategy = databaseStrategyContext.getStrategy(cd.getSqlTipi());
	}

	public String[] conn_detail() {
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails gunlukConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.GUNLUK);
		String[] detay = {"","",""};
		detay[0] = gunlukConnDetails.getSqlTipi().getValue() ;
		detay[1] = gunlukConnDetails.getDatabaseName() ;
		detay[2] = gunlukConnDetails.getServerIp() ;
		return detay;
	}
	
	public String gunluk_firma_adi() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails gunlukConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.GUNLUK);
			return strategy.gun_firma_adi(gunlukConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> gorevsayi(Date start,Date end) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails gunlukConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.GUNLUK);
			return strategy.gorev_sayi(start,end, gunlukConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> gorev_oku_tarih(String tarih,String saat) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails gunlukConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.GUNLUK);
			return strategy.gorev_oku_tarih(tarih,saat, gunlukConnDetails);
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
