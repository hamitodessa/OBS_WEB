package com.hamit.obs.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.Etiket_Ayarlari;
import com.hamit.obs.repository.user.IEtiketAyarRepository;

@Service
public class EtiketAyarService {

	@Autowired
	IEtiketAyarRepository iEtiketAyarRepository ;
	
	public Etiket_Ayarlari findByUserId(Long user_id){
		try {
			return iEtiketAyarRepository.findByUserId(user_id);
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