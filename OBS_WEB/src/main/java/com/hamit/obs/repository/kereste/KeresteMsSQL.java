package com.hamit.obs.repository.kereste;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.exception.ServiceException;

@Component
public class KeresteMsSQL implements IKeresteDatabase {

	@Override
	public String ker_firma_adi(ConnectionDetails keresteConnDetails) {
		String firmaIsmi = "";
		String query = "SELECT FIRMA_ADI FROM OZEL";
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				firmaIsmi = resultSet.getString("FIRMA_ADI");
			}
		} catch (SQLException e) {
			throw new ServiceException("Firma adı okunamadı", e);
		}
		return firmaIsmi;

	}

	@Override
	public List<Map<String, Object>> ker_kod_degisken_oku(String fieldd, String sno, String nerden,
			ConnectionDetails keresteConnDetails) {
		String sql =  "SELECT " + sno + "  AS KOD , " + fieldd + " FROM " + nerden + "" +
				" ORDER BY " + fieldd + "";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public String urun_kod_degisken_ara(String fieldd, String sno, String nerden, String arama,
			ConnectionDetails keresteConnDetails) {
		String query = "SELECT  " + fieldd + " FROM " + nerden + " WHERE " + sno + " = N'" + arama + "'";
		String deger = "" ;
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				deger = resultSet.getString(fieldd);
			}
		} catch (SQLException e) {
			throw new ServiceException("Firma adı okunamadı", e);
		}
		return deger;

	}

	@Override
	public List<Map<String, Object>> ker_kod_alt_grup_degisken_oku(int sno, ConnectionDetails keresteConnDetails) {
		String sql =  "SELECT ALID_Y , ALT_GRUP FROM ALT_GRUP_DEGISKEN   " +
				" WHERE ANA_GRUP = N'" + sno + "' ORDER BY ALT_GRUP";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public void urun_degisken_eski(String fieldd, String degisken_adi, String nerden, String sno, int id,
			ConnectionDetails keresteConnDetails) {
		String sql = "UPDATE " + nerden + " SET " + fieldd + " = ? WHERE " + sno + " = ?";
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, degisken_adi);
			preparedStatement.setInt(2, id);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("Ürün değişken güncelleme başarısız", e);
		}
	}

	@Override
	public void urun_degisken_alt_grup_eski(String alt_grup, int ana_grup, int ID,
			ConnectionDetails keresteConnDetails) {
		String sql = "UPDATE ALT_GRUP_DEGISKEN SET ALT_GRUP = ?, ANA_GRUP = ? WHERE ALID_Y = ?";
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, alt_grup);
			preparedStatement.setInt(2, ana_grup);
			preparedStatement.setInt(3, ID);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("Ürün değişken alt grup güncelleme başarısız", e);
		}

	}

	@Override
	public void urun_degisken_kayit(String fieldd, String nerden, String degisken_adi, String sira,
			ConnectionDetails keresteConnDetails) {
		int maks = 0;
		String sql = "SELECT MAX(" + fieldd + ") AS maks FROM " + nerden;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement selectStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = selectStatement.executeQuery();
			if (resultSet.next()) {
				maks = resultSet.getInt("maks");
			}
			sql = "INSERT INTO " + nerden + " (" + fieldd + ", " + degisken_adi + ", [USER]) VALUES (?, ?, ?)";
			try (PreparedStatement insertStatement = connection.prepareStatement(sql)) {
				insertStatement.setInt(1, maks + 1);
				insertStatement.setString(2, sira);
				String usrString = Global_Yardimci.user_log(
						SecurityContextHolder.getContext().getAuthentication().getName());
				insertStatement.setString(3, usrString);
				insertStatement.executeUpdate();
			}
		} catch (SQLException e) {
			throw new ServiceException("Ürün değişken kayıt işlemi başarısız", e);
		}
	}

	@Override
	public void urun_degisken_alt_grup_kayit(String alt_grup, int ana_grup, ConnectionDetails keresteConnDetails) {
		int maks = 0;
		String sql = "SELECT max(ALID_Y)  AS ALID_Y  FROM ALT_GRUP_DEGISKEN   " ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement selectStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = selectStatement.executeQuery();
			if (resultSet.next()) {
				int count=0;
				count = resultSet.getRow();
				maks = (count != 0) ? resultSet.getInt("ALID_Y") : 0;
			}
			sql = "INSERT INTO ALT_GRUP_DEGISKEN (ALID_Y,ALT_GRUP,ANA_GRUP,[USER]) " +
					" VALUES (?,?,?,?)" ;
			try (PreparedStatement insertStatement = connection.prepareStatement(sql)) {
				insertStatement.setInt(1,maks + 1);
				insertStatement.setString(2, alt_grup);
				insertStatement.setInt(3,ana_grup);
				String usrString = Global_Yardimci.user_log(
						SecurityContextHolder.getContext().getAuthentication().getName());
				insertStatement.setString(4, usrString);
				insertStatement.executeUpdate();
			}
		} catch (SQLException e) {
			throw new ServiceException("Ürün değişken kayıt işlemi başarısız", e);
		}
	}

	@Override
	public void urun_degisken_alt_grup_sil(int id, ConnectionDetails keresteConnDetails) {
		String sql = "DELETE FROM ALT_GRUP_DEGISKEN  WHERE ALID_Y = ? ";
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, id);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("stok sil", e);
		}
	}

	@Override
	public void urun_kod_degisken_sil(String hangi_Y, String nerden, int sira, ConnectionDetails keresteConnDetails) {
		String sql = "DELETE FROM " + nerden  + " WHERE " + hangi_Y + " = ?";
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, sira);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("stok sil", e);
		}
	}

	@Override
	public boolean alt_grup_kontrol(int anagrp, int altgrp, ConnectionDetails keresteConnDetails) {
		boolean result = true;
		String[] sqlQueries = {
				"SELECT * FROM MAL WHERE Ana_Grup = ? AND Alt_Grup = ?",
				"SELECT * FROM FATURA WHERE Ana_Grup = ? AND Alt_Grup = ?",
				"SELECT * FROM IRSALIYE WHERE Ana_Grup = ? AND Alt_Grup = ?",
				"SELECT * FROM RECETE WHERE Ana_Grup = ? AND Alt_Grup = ?",
				"SELECT * FROM STOK WHERE Ana_Grup = ? AND Alt_Grup = ?"
		};
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword())) {

			for (String sql : sqlQueries) {
				try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
					preparedStatement.setInt(1, anagrp);
					preparedStatement.setInt(2, altgrp);

					try (ResultSet resultSet = preparedStatement.executeQuery()) {
						if (!resultSet.isBeforeFirst()) {
							result = false;
							break; 
						}
					}
				}
			}
		} catch (SQLException e) {
			throw new ServiceException("Alt grup kontrolü sırasında bir hata oluştu", e);
		}		
		return result;

	}

	@Override
	public void ker_firma_adi_kayit(String fadi, ConnectionDetails keresteConnDetails) {
		String sql = "UPDATE OZEL SET FIRMA_ADI = ? " ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1,fadi);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}

	}
}
