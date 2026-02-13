package com.hamit.obs.service.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.gunluk.GunlukMsSQL;
import com.hamit.obs.repository.gunluk.GunlukMySQL;
import com.hamit.obs.repository.gunluk.GunlukPgSQL;
import com.hamit.obs.repository.gunluk.IGunlukDatabase;

@Service
public class GunlukDatabaseContext {
	private final Map<String, IGunlukDatabase> strategies = new HashMap<>();

	public GunlukDatabaseContext(GunlukMySQL mySQL, GunlukMsSQL msSQL, GunlukPgSQL pgSQL) {
		strategies.put(sqlTipi.MYSQL.getValue(), mySQL);
		strategies.put(sqlTipi.MSSQL.getValue(), msSQL);
		strategies.put(sqlTipi.PGSQL.getValue(), pgSQL);
	}

	public IGunlukDatabase getStrategy(sqlTipi tip) {
	    if (tip == null) throw new ServiceException("SQL tipi null");
	    IGunlukDatabase strategy = strategies.get(tip.getValue());
	    if (strategy == null) throw new ServiceException("Strateji bulunamadı: " + tip.getValue());
	    return strategy;
	}
}
