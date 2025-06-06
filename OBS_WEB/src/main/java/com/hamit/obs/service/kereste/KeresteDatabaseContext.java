package com.hamit.obs.service.kereste;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.kereste.IKeresteDatabase;
import com.hamit.obs.repository.kereste.KeresteMsSQL;
import com.hamit.obs.repository.kereste.KeresteMySQL;
import com.hamit.obs.repository.kereste.KerestePgSQL;
import com.hamit.obs.service.user.UserDetailsService;

@Service
public class KeresteDatabaseContext {
	private final Map<String, IKeresteDatabase> strategies = new HashMap<>();

	@Autowired
	private UserDetailsService userDetailsService;

	public KeresteDatabaseContext(KeresteMySQL mySQL, KeresteMsSQL msSQL, KerestePgSQL pgSQL) {
		strategies.put(sqlTipi.MYSQL.getValue(), mySQL);
		strategies.put(sqlTipi.MSSQL.getValue(), msSQL);
		strategies.put(sqlTipi.PGSQL.getValue(), pgSQL);
	}

	public IKeresteDatabase getStrategy() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
	        String config = userDetailsService.findHangiSQLByUserId("Kereste", useremail);
	        if (config == null || config.isEmpty())
	        	throw new ServiceException("Kullanıcıya ait SQL konfigürasyonu bulunamadı.");
	        IKeresteDatabase strategy = strategies.get(config);
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