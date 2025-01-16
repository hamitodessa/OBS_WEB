package com.hamit.obs.repository.fatura;

import java.util.List;
import java.util.Map;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.dto.stok.urunDTO;

public interface IFaturaDatabase {

	String fat_firma_adi(ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> urun_kodlari(ConnectionDetails faturaConnDetails);
	urunDTO stk_urun(String sira,String arama,ConnectionDetails faturaConnDetails);
	String urun_kod_degisken_ara(String fieldd,String sno,String nerden,String arama,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> stk_kod_degisken_oku(String fieldd,String sno,String nerden,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> stk_kod_alt_grup_degisken_oku (int sno,ConnectionDetails faturaConnDetails);
	String ur_kod_bak(String kodu,ConnectionDetails faturaConnDetails);
}
