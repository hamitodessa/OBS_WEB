package com.hamit.obs.service.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.kereste.IKeresteDatabase;
import com.hamit.obs.repository.kereste.KeresteMsSQL;
import com.hamit.obs.repository.kereste.KeresteMySQL;
import com.hamit.obs.repository.kereste.KerestePgSQL;

@Service
public class KeresteDatabaseContext {
	private final Map<String, IKeresteDatabase> strategies = new HashMap<>();

	public KeresteDatabaseContext(KeresteMySQL mySQL, KeresteMsSQL msSQL, KerestePgSQL pgSQL) {
		strategies.put(sqlTipi.MYSQL.getValue(), mySQL);
		strategies.put(sqlTipi.MSSQL.getValue(), msSQL);
		strategies.put(sqlTipi.PGSQL.getValue(), pgSQL);
	}

	public IKeresteDatabase getStrategy(sqlTipi tip) {
	    if (tip == null) throw new ServiceException("SQL tipi null");
	    IKeresteDatabase strategy = strategies.get(tip.getValue());
	    if (strategy == null) throw new ServiceException("Strateji bulunamadı: " + tip.getValue());
	    return strategy;
	}
}