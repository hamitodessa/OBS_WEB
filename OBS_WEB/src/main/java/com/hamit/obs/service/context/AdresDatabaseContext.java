package com.hamit.obs.service.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.adres.AdresMsSQL;
import com.hamit.obs.repository.adres.AdresMySQL;
import com.hamit.obs.repository.adres.AdresPgSQL;
import com.hamit.obs.repository.adres.IAdresDatabase;
import com.hamit.obs.service.user.UserDetailsService;

@Service
public class AdresDatabaseContext {
	private final Map<String, IAdresDatabase> strategies = new HashMap<>();

	@Autowired
	private UserDetailsService userDetailsService;

	public AdresDatabaseContext(AdresMySQL mySQL, AdresMsSQL msSQL, AdresPgSQL pgSQL) {
		strategies.put(sqlTipi.MYSQL.getValue(), mySQL);
		strategies.put(sqlTipi.MSSQL.getValue(), msSQL);
		strategies.put(sqlTipi.PGSQL.getValue(), pgSQL);
	}

	public IAdresDatabase getStrategy() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			String config = userDetailsService.findHangiSQLByUserId("Adres", useremail);
			if (config == null || config.isEmpty())
				throw new ServiceException("Kullanıcıya ait SQL konfigürasyonu bulunamadı.");
			IAdresDatabase strategy = strategies.get(config);
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