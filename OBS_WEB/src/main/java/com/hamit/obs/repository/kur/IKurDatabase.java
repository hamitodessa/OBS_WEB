package com.hamit.obs.repository.kur;

import java.util.List;
import java.util.Map;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.dto.kur.kurgirisDTO;
import com.hamit.obs.dto.kur.kurraporDTO;

public interface IKurDatabase {

	List<Map<String, Object>> kur_liste(String tarih,  ConnectionDetails kurConnDetails);
	void kur_sil (String tarih,String kur_turu,  ConnectionDetails kurConnDetails);
	boolean kur_kayit(kurgirisDTO kurgirisDTO,  ConnectionDetails kurConnDetails);
	List<Map<String, Object>> kur_rapor(kurraporDTO kurraporDTO,  ConnectionDetails kurConnDetails);
	List<Map<String, Object>> kur_oku(kurgirisDTO kurgirisDTO,  ConnectionDetails kurConnDetails);
}
