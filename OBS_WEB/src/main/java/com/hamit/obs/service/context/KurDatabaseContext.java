package com.hamit.obs.service.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.kur.IKurDatabase;
import com.hamit.obs.repository.kur.KurMS;
import com.hamit.obs.repository.kur.KurMY;
import com.hamit.obs.repository.kur.KurPG;

@Service
public class KurDatabaseContext {
	private final Map<String, IKurDatabase> strategies = new HashMap<>();

	public KurDatabaseContext(KurMY mySQL, KurMS msSQL, KurPG pgSQL) {
		strategies.put(sqlTipi.MYSQL.getValue(), mySQL);
		strategies.put(sqlTipi.MSSQL.getValue(), msSQL);
		strategies.put(sqlTipi.PGSQL.getValue(), pgSQL);
	}

	public IKurDatabase getStrategy(sqlTipi tip) {
	    if (tip == null) throw new ServiceException("SQL tipi null");
	    IKurDatabase strategy = strategies.get(tip.getValue());
	    if (strategy == null) throw new ServiceException("Strateji bulunamadı: " + tip.getValue());
	    return strategy;
	}
}