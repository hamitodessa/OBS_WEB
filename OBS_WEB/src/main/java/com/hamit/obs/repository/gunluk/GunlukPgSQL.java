package com.hamit.obs.repository.gunluk;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.dto.gunluk.gunlukBilgiDTO;

@Component
public class GunlukPgSQL implements IGunlukDatabase{

	@Override
	public String gun_firma_adi(ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> gorev_sayi(Date start,Date end, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void gorev_kayit(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gorev_sil(int id, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Map<String, Object>> gorev_oku(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> isim_oku(ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> gorev_oku_tarih(String tarih,String saat, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> gID_oku(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> hazir_gorevler(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void gorev_tek_sil(int id, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gunluk_farkli_kayit(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Map<String, Object>> gorev_oku_aylik_grup(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> gorev_oku_sonraki(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void gun_firma_adi_kayit(String fadi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Map<String, Object>> gorev_oku_yillik_pivot(gunlukBilgiDTO gbilgi,
			ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> gorev_oku_sonraki_yil(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int gunluk_gid_kontrol(int gid, int kont, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void gorev_tablo_gidsil(int gid, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Map<String, Object>> gorev_liste(ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> yer_oku(ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> gorev_oku(ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int gidnoal(ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return 0;
	}

	}
