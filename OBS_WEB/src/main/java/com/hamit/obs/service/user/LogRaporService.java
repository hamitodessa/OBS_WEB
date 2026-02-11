package com.hamit.obs.service.user;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.loglama.LoglamaRepository;

@Service
public class LogRaporService {

	@Autowired
	private LoglamaRepository loglamaRepository;

	@Autowired
	private UserService userService;

	ConnectionDetails connConnDetails ;

	public List<Map<String, Object>> lograpor(String startDate,String endDate,String aciklama,modulTipi modul,Pageable pageable){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			
			ConnectionDetails connConnDetails =
		            UserSessionManager.getUserSession(useremail, modul);
			String usrString = Global_Yardimci.user_log(userService.getCurrentUser().getEmail());
			return loglamaRepository.logRapor(usrString ,startDate,endDate,aciklama,pageable ,connConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null)
				detailedMessage += " - " + cause.getMessage();
			throw new ServiceException(detailedMessage);
		}
	}
	
	public double logsize(String startDate,String endDate,modulTipi modul,String aciklama){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails connConnDetails =
		            UserSessionManager.getUserSession(useremail, modul);
			String usrString = Global_Yardimci.user_log(userService.getCurrentUser().getEmail());
			return loglamaRepository.log_raporsize(startDate,endDate,usrString,aciklama,connConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null)
				detailedMessage += " - " + cause.getMessage();
			throw new ServiceException(detailedMessage);
		}
	}
}