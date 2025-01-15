package com.hamit.obs.repository.fatura;

import java.util.List;
import java.util.Map;

import com.hamit.obs.connection.ConnectionDetails;

public interface IFaturaDatabase {

	String fat_firma_adi(ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> urun_kodlari(ConnectionDetails faturaConnDetails);
}
