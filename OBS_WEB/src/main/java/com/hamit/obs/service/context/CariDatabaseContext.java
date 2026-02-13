package com.hamit.obs.service.context;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.cari.CariMsSQL;
import com.hamit.obs.repository.cari.CariMySQL;
import com.hamit.obs.repository.cari.CariPgSQL;
import com.hamit.obs.repository.cari.ICariDatabase;

@Service
public class CariDatabaseContext {
	private final Map<String, ICariDatabase> strategies = new HashMap<>();

	public CariDatabaseContext(CariMySQL mySQL, CariMsSQL msSQL, CariPgSQL pgSQL) {
		strategies.put(sqlTipi.MYSQL.getValue(), mySQL);
		strategies.put(sqlTipi.MSSQL.getValue(), msSQL);
		strategies.put(sqlTipi.PGSQL.getValue(), pgSQL);
	}

	public ICariDatabase getStrategy(sqlTipi tip) {
	    if (tip == null) throw new ServiceException("SQL tipi null");
	    ICariDatabase strategy = strategies.get(tip.getValue());
	    if (strategy == null) throw new ServiceException("Strateji bulunamadı: " + tip.getValue());
	    return strategy;
	}
}