package com.hamit.obs.service.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.kambiyo.IKambiyoDatabase;
import com.hamit.obs.repository.kambiyo.KambiyoMsSQL;
import com.hamit.obs.repository.kambiyo.KambiyoMySQL;
import com.hamit.obs.repository.kambiyo.KambiyoPgSQL;

@Service
public class KambiyoDatabaseContext {
	private final Map<String, IKambiyoDatabase> strategies = new HashMap<>();

	public KambiyoDatabaseContext(KambiyoMySQL mySQL, KambiyoMsSQL msSQL, KambiyoPgSQL pgSQL) {
		strategies.put(sqlTipi.MYSQL.getValue(), mySQL);
		strategies.put(sqlTipi.MSSQL.getValue(), msSQL);
		strategies.put(sqlTipi.PGSQL.getValue(), pgSQL);
	}

	public IKambiyoDatabase getStrategy(sqlTipi tip) {
	    if (tip == null) throw new ServiceException("SQL tipi null");
	    IKambiyoDatabase strategy = strategies.get(tip.getValue());
	    if (strategy == null) throw new ServiceException("Strateji bulunamadı: " + tip.getValue());
	    return strategy;
	}
}