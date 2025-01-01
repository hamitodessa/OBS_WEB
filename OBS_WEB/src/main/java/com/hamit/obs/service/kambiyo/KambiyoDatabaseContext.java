package com.hamit.obs.service.kambiyo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.kambiyo.IKambiyoDatabase;
import com.hamit.obs.repository.kambiyo.KambiyoMsSQL;
import com.hamit.obs.repository.kambiyo.KambiyoMySQL;
import com.hamit.obs.repository.kambiyo.KambiyoPgSQL;
import com.hamit.obs.service.user.UserDetailsService;
import com.hamit.obs.service.user.UserService;

@Service
public class KambiyoDatabaseContext {
	private final Map<String, IKambiyoDatabase> strategies = new HashMap<>();

	@Autowired
	private UserService userService;

	@Autowired
	private UserDetailsService userDetailsService;

	public KambiyoDatabaseContext(KambiyoMySQL mySQL, KambiyoMsSQL msSQL, KambiyoPgSQL pgSQL) {
		strategies.put("MY SQL", mySQL);
		strategies.put("MS SQL", msSQL);
		strategies.put("PG SQL", pgSQL);
	}

	public IKambiyoDatabase getStrategy() {
		try {
	        String config = userDetailsService.findHangiSQLByUserId("Kambiyo", userService.getCurrentUser().getEmail());
	        if (config == null || config.isEmpty()) {
	            throw new ServiceException("Kullanıcıya ait SQL konfigürasyonu bulunamadı.");
	        }
	        IKambiyoDatabase strategy = strategies.get(config);
	        if (strategy == null) {
	            throw new ServiceException("Belirtilen konfigürasyona uygun strateji bulunamadı: " + config);
	        }
	        return strategy;
	    } catch (ServiceException e) {
	        throw e;
	    } catch (Exception e) {
	        throw new ServiceException("Strateji alma sırasında beklenmeyen bir hata oluştu.", e);
	    }
	}
}