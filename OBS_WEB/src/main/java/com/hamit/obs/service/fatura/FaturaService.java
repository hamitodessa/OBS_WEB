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
}
