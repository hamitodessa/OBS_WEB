package com.hamit.obs.service.cari;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.cari.CariMsSQL;
import com.hamit.obs.repository.cari.CariMySQL;
import com.hamit.obs.repository.cari.CariPgSQL;
import com.hamit.obs.repository.cari.ICariDatabase;
import com.hamit.obs.service.user.UserDetailsService;

@Service
public class CariDatabaseContext {
	private final Map<String, ICariDatabase> strategies = new HashMap<>();

	@Autowired
	private UserDetailsService userDetailsService;

	public CariDatabaseContext(CariMySQL mySQL, CariMsSQL msSQL, CariPgSQL pgSQL) {
		strategies.put("MY SQL", mySQL);
		strategies.put("MS SQL", msSQL);
		strategies.put("PG SQL", pgSQL);
	}

	public ICariDatabase getStrategy() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
	        String config = userDetailsService.findHangiSQLByUserId(modulTipi.CARI_HESAP.getDbValue(), useremail);
	        if (config == null || config.isEmpty())
	        	throw new ServiceException("Kullanıcıya ait SQL konfigürasyonu bulunamadı.");
	        ICariDatabase strategy = strategies.get(config);
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