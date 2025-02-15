package com.hamit.obs.repository.kereste;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.exception.ServiceException;

@Component
public class KerestePgSQL implements IKeresteDatabase {

	@Override
	public String ker_firma_adi(ConnectionDetails keresteConnDetails) {
		String firmaIsmi = "";
		String query = "SELECT \"FIRMA_ADI\" FROM \"OZEL\"";
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next())
				firmaIsmi = resultSet.getString("FIRMA_ADI");
		} catch (SQLException e) {
			throw new ServiceException("Firma adı okunamadı", e);
		}
		return firmaIsmi;

	}

	@Override
	public List<Map<String, Object>> ker_kod_degisken_oku(String fieldd, String sno, String nerden,
			ConnectionDetails keresteConnDetails) {
		String sql =  "SELECT \"" + sno + "\"  AS \"KOD\" , \"" + fieldd + "\" FROM \"" + nerden + "\"" +
				" ORDER BY \"" + fieldd + "\"";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException("PG stkService genel hatası.", e);
		}
		return resultList; 

	}

	@Override
	public String urun_kod_degisken_ara(String fieldd, String sno, String nerden, String arama,
			ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> ker_kod_alt_grup_degisken_oku(int sno, ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void urun_degisken_eski(String fieldd, String degisken_adi, String nerden, String sno, int ID,
			ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void urun_degisken_alt_grup_eski(String alt_grup, int ana_grup, int ID,
			ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void urun_degisken_kayit(String fieldd, String nerden, String degisken_adi, String sira,
			ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void urun_degisken_alt_grup_kayit(String alt_grup, int ana_grup, ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void urun_degisken_alt_grup_sil(int id, ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void urun_kod_degisken_sil(String hangi_Y, String nerden, int sira, ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean alt_grup_kontrol(int anagrp, int altgrp, ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void ker_firma_adi_kayit(String fadi, ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String son_no_al(String cins, ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int evrak_no_al(String cins, ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Map<String, Object>> ker_oku(String eno, String cins, ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String aciklama_oku(String evrcins, int satir, String evrno, String gircik,
			ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> dipnot_oku(String ino, String cins, String gircik,
			ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

}
