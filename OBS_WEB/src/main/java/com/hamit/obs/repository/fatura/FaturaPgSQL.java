package com.hamit.obs.repository.fatura;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.dto.stok.urunDTO;

@Component
public class FaturaPgSQL implements IFaturaDatabase {

	@Override
	public String fat_firma_adi(ConnectionDetails faturaConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> urun_kodlari(ConnectionDetails faturaConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public urunDTO stk_urun(String sira, String arama, ConnectionDetails faturaConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String urun_kod_degisken_ara(String fieldd, String sno, String nerden, String arama,
			ConnectionDetails faturaConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> stk_kod_degisken_oku(String fieldd, String sno, String nerden,
			ConnectionDetails faturaConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> stk_kod_alt_grup_degisken_oku(int sno, ConnectionDetails faturaConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String ur_kod_bak(String kodu, ConnectionDetails faturaConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

}
