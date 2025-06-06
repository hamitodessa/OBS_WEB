package com.hamit.obs.service.fatura;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.fatura.FaturaMsSQL;
import com.hamit.obs.repository.fatura.FaturaMySQL;
import com.hamit.obs.repository.fatura.FaturaPgSQL;
import com.hamit.obs.repository.fatura.IFaturaDatabase;
import com.hamit.obs.service.user.UserDetailsService;

@Service
public class FaturaDatabaseContext {
	private final Map<String, IFaturaDatabase> strategies = new HashMap<>();

	@Autowired
	private UserDetailsService userDetailsService;

	public FaturaDatabaseContext(FaturaMySQL mySQL, FaturaMsSQL msSQL, FaturaPgSQL pgSQL) {
		strategies.put(sqlTipi.MYSQL.getValue(), mySQL);
		strategies.put(sqlTipi.MSSQL.getValue(), msSQL);
		strategies.put(sqlTipi.PGSQL.getValue(), pgSQL);
	}

	public IFaturaDatabase getStrategy() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
	        String config = userDetailsService.findHangiSQLByUserId("Fatura", useremail);
	        if (config == null || config.isEmpty())
	        	throw new ServiceException("Kullanıcıya ait SQL konfigürasyonu bulunamadı.");
	        IFaturaDatabase strategy = strategies.get(config);
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