package com.hamit.obs.service.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.kambiyo.IKambiyoDatabase;
import com.hamit.obs.repository.kambiyo.KambiyoMsSQL;
import com.hamit.obs.repository.kambiyo.KambiyoMySQL;
import com.hamit.obs.repository.kambiyo.KambiyoPgSQL;
import com.hamit.obs.service.user.UserDetailsService;

@Service
public class KambiyoDatabaseContext {
	private final Map<String, IKambiyoDatabase> strategies = new HashMap<>();

	@Autowired
	private UserDetailsService userDetailsService;

	public KambiyoDatabaseContext(KambiyoMySQL mySQL, KambiyoMsSQL msSQL, KambiyoPgSQL pgSQL) {
		strategies.put(sqlTipi.MYSQL.getValue(), mySQL);
		strategies.put(sqlTipi.MSSQL.getValue(), msSQL);
		strategies.put(sqlTipi.PGSQL.getValue(), pgSQL);
	}

	public IKambiyoDatabase getStrategy() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
	        String config = userDetailsService.findHangiSQLByUserId("Kambiyo", useremail);
	        if (config == null || config.isEmpty())
	        	throw new ServiceException("Kullanıcıya ait SQL konfigürasyonu bulunamadı.");
	        IKambiyoDatabase strategy = strategies.get(config);
	        if (strategy == null)
	        	throw new ServiceException("Belirtilen konfigürasyona uygun strateji bulunamadı: " + config);
	        return strategy;
	    } catch (ServiceException e) {
	        throw e;
	    } catch (Exception e) {
	        throw new ServiceException("Strateji alma sırasında beklenmeyen bir hata oluştu.", e);
	    }
	}
}