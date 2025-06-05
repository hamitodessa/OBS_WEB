package com.hamit.obs.service.user;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.connection.ConnectionManager;
import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.loglama.LoglamaRepository;

@Service
public class LogRaporService {

	@Autowired
	private LoglamaRepository loglamaRepository;

	@Autowired
	private ConnectionManager masterConnectionManager;

	@Autowired
	private UserService userService;

	ConnectionDetails connConnDetails ;

	public List<Map<String, Object>> lograpor(String startDate,String endDate,String aciklama,modulTipi modul){
		try {
			String useremail = userService.getCurrentUser().getEmail();
			masterConnectionManager.loadConnections(modul,useremail);
			connConnDetails = masterConnectionManager.getConnection(modul, useremail);
			String usrString = Global_Yardimci.user_log(userService.getCurrentUser().getEmail());
			return loglamaRepository.logRapor(usrString ,startDate,endDate,aciklama ,connConnDetails);
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