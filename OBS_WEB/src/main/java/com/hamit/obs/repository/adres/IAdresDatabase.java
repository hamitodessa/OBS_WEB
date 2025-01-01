package com.hamit.obs.repository.adres;

import java.util.List;
import java.util.Map;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.dto.adres.adresDTO;

public interface IAdresDatabase {

	List<Map<String, Object>> hesap_kodlari(ConnectionDetails adresConnDetails);
	public adresDTO hsp_pln(String hesap, ConnectionDetails adresConnDetails);
	String adres_firma_adi(ConnectionDetails adresConnDetails);
	void adres_kayit(adresDTO adresDTO, ConnectionDetails adresConnDetails);
	void adres_sil(int id, ConnectionDetails adresConnDetails);
	String kod_ismi(String kodu, ConnectionDetails adresConnDetails);
	String[] adr_etiket_arama_kod(String kodu, ConnectionDetails adresConnDetails);
	void adres_firma_adi_kayit(String fadi,ConnectionDetails adresConnDetails);
	List<Map<String, Object>> adr_etiket(String siralama,ConnectionDetails adresConnDetails);
	
}
