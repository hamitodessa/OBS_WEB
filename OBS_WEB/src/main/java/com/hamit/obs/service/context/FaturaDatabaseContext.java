package com.hamit.obs.service.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.fatura.FaturaMsSQL;
import com.hamit.obs.repository.fatura.FaturaMySQL;
import com.hamit.obs.repository.fatura.FaturaPgSQL;
import com.hamit.obs.repository.fatura.IFaturaDatabase;

@Service
public class FaturaDatabaseContext {
	private final Map<String, IFaturaDatabase> strategies = new HashMap<>();

	public FaturaDatabaseContext(FaturaMySQL mySQL, FaturaMsSQL msSQL, FaturaPgSQL pgSQL) {
		strategies.put(sqlTipi.MYSQL.getValue(), mySQL);
		strategies.put(sqlTipi.MSSQL.getValue(), msSQL);
		strategies.put(sqlTipi.PGSQL.getValue(), pgSQL);
	}

	public IFaturaDatabase getStrategy(sqlTipi tip) {
	    if (tip == null) throw new ServiceException("SQL tipi null");
	    IFaturaDatabase strategy = strategies.get(tip.getValue());
	    if (strategy == null) throw new ServiceException("Strateji bulunamadı: " + tip.getValue());
	    return strategy;
	}
}