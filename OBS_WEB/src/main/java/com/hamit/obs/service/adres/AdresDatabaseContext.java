package com.hamit.obs.service.adres;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.adres.AdresMsSQL;
import com.hamit.obs.repository.adres.AdresMySQL;
import com.hamit.obs.repository.adres.AdresPgSQL;
import com.hamit.obs.repository.adres.IAdresDatabase;
import com.hamit.obs.service.user.UserDetailsService;
import com.hamit.obs.service.user.UserService;

@Service
public class AdresDatabaseContext {
	private final Map<String, IAdresDatabase> strategies = new HashMap<>();

	@Autowired
	private UserService userService;

	@Autowired
	private UserDetailsService userDetailsService;

	public AdresDatabaseContext(AdresMySQL mySQL, AdresMsSQL msSQL, AdresPgSQL pgSQL) {
		strategies.put("MY SQL", mySQL);
		strategies.put("MS SQL", msSQL);
		strategies.put("PG SQL", pgSQL);
	}

	public IAdresDatabase getStrategy() {
		try {
			String config = userDetailsService.findHangiSQLByUserId("Adres", userService.getCurrentUser().getEmail());
			if (config == null || config.isEmpty()) {
				throw new ServiceException("Kullanıcıya ait SQL konfigürasyonu bulunamadı.");
			}
			IAdresDatabase strategy = strategies.get(config);
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