package com.hamit.obs.service.kur;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.kur.IKurDatabase;
import com.hamit.obs.repository.kur.KurMS;
import com.hamit.obs.repository.kur.KurMY;
import com.hamit.obs.repository.kur.KurPG;
import com.hamit.obs.service.user.UserDetailsService;

@Service
public class KurDatabaseContext {
	private final Map<String, IKurDatabase> strategies = new HashMap<>();

	@Autowired
	private UserDetailsService userDetailsService;

	public KurDatabaseContext(KurMY mySQL, KurMS msSQL, KurPG pgSQL) {
		strategies.put(sqlTipi.MYSQL.getValue(), mySQL);
		strategies.put(sqlTipi.MSSQL.getValue(), msSQL);
		strategies.put(sqlTipi.PGSQL.getValue(), pgSQL);
	}

	public IKurDatabase getStrategy() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
	        String config = userDetailsService.findHangiSQLByUserId("Kur", useremail);
	        if (config == null || config.isEmpty())
	        	throw new ServiceException("Kullanıcıya ait SQL konfigürasyonu bulunamadı.");
	        IKurDatabase strategy = strategies.get(config);
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