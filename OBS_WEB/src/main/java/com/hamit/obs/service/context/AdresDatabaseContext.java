package com.hamit.obs.service.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.adres.AdresMsSQL;
import com.hamit.obs.repository.adres.AdresMySQL;
import com.hamit.obs.repository.adres.AdresPgSQL;
import com.hamit.obs.repository.adres.IAdresDatabase;

@Service
public class AdresDatabaseContext {
	private final Map<String, IAdresDatabase> strategies = new HashMap<>();


	public AdresDatabaseContext(AdresMySQL mySQL, AdresMsSQL msSQL, AdresPgSQL pgSQL) {
		strategies.put(sqlTipi.MYSQL.getValue(), mySQL);
		strategies.put(sqlTipi.MSSQL.getValue(), msSQL);
		strategies.put(sqlTipi.PGSQL.getValue(), pgSQL);
	}

	public IAdresDatabase getStrategy(sqlTipi tip) {
	    if (tip == null) throw new ServiceException("SQL tipi null");
	    IAdresDatabase strategy = strategies.get(tip.getValue());
	    if (strategy == null) throw new ServiceException("Strateji bulunamadı: " + tip.getValue());
	    return strategy;
	}
}