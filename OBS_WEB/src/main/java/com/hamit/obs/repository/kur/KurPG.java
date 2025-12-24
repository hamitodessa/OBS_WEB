package com.hamit.obs.repository.kur;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.dto.kur.kurgirisDTO;
import com.hamit.obs.dto.kur.kurraporDTO;
import com.hamit.obs.exception.ServiceException;

@Component
public class KurPG implements  IKurDatabase{

	@Override
	public List<Map<String, Object>> kur_liste(String tarih, ConnectionDetails kurConnDetails) {
		List<Map<String, Object>> resultList = new ArrayList<>();
		String sql = "SELECT \"KUR\" as \"Kur\",\"MA\",\"MS\",\"SA\",\"SS\",\"BA\",\"BS\"" +
				" FROM \"KURLAR\"" +
				" WHERE \"TARIH\" = ? ORDER BY \"KUR\"";
		try (Connection connection = DriverManager.getConnection(kurConnDetails.getJdbcUrl(), kurConnDetails.getUsername(), kurConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setDate(1, Date.valueOf(LocalDate.parse(tarih)));
			try (ResultSet rs = preparedStatement.executeQuery()) {
				resultList = ResultSetConverter.convertToList(rs);
			}
		} catch (Exception e) {
			throw new ServiceException("PG KurService genel hatası.", e);
		}
		return resultList;
	}

	@Override
	public void kur_sil(String tarih, String kurTuru, ConnectionDetails kurConnDetails) {
		String sql = "DELETE" +
				" FROM \"KURLAR\"" +
				" WHERE \"TARIH\" = ? AND \"KUR\" = ?" ;
		try (Connection connection = DriverManager.getConnection(
				kurConnDetails.getJdbcUrl(), kurConnDetails.getUsername(), kurConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setDate(1, Date.valueOf(tarih));
			stmt.setString(2, kurTuru);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("Kurlar tablosundan veri silinirken bir hata oluştu.", e);
		}
	}

	@Override
	public boolean kur_kayit(kurgirisDTO kurgirisDTO, ConnectionDetails kurConnDetails) {
		String sql  = "INSERT INTO \"KURLAR\" (\"TARIH\",\"KUR\",\"MA\",\"MS\",\"SA\",\"SS\",\"BA\",\"BS\") " +
				" VALUES (?,?,?,?,?,?,?,?)" ;
		boolean drm = false;
		try (Connection connection = DriverManager.getConnection(
				kurConnDetails.getJdbcUrl(), kurConnDetails.getUsername(), kurConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setDate(1, Date.valueOf(kurgirisDTO.getTar()));
			stmt.setString(2, kurgirisDTO.getDvz_turu());
			stmt.setDouble(3, kurgirisDTO.getMa());
			stmt.setDouble(4, kurgirisDTO.getMs());
			stmt.setDouble(5, kurgirisDTO.getSa());
			stmt.setDouble(6, kurgirisDTO.getSs());
			stmt.setDouble(7, kurgirisDTO.getBa());
			stmt.setDouble(8, kurgirisDTO.getBs());
			stmt.executeUpdate();
			drm = true;
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
		return drm;
	}

	@Override
	public List<Map<String, Object>> kur_rapor(kurraporDTO kurraporDTO, ConnectionDetails kurConnDetails) {
		List<Map<String, Object>> resultList = new ArrayList<>();
		String sql = "SELECT \"TARIH\" as \"Tarih\",\"KUR\" as \"Kur\",\"MA\",\"MS\",\"BA\",\"BS\",\"SA\",\"SS\"" +
				" FROM \"KURLAR\"" +
				" WHERE \"TARIH\" >= ? AND \"TARIH\" < ? " +
				" AND \"KUR\" >= ? AND \"KUR\" < ? ORDER BY \"TARIH\" DESC,\"KUR\"";
		try (Connection connection = DriverManager.getConnection(kurConnDetails.getJdbcUrl(), kurConnDetails.getUsername(), kurConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			Timestamp[] ts = Global_Yardimci.rangeDayT2plusDay(kurraporDTO.getStartDate(), kurraporDTO.getEndDate());
			int p = 1;
			preparedStatement.setTimestamp(p++, ts[0]);            
			preparedStatement.setTimestamp(p++, ts[1]);            
			preparedStatement.setString(p++, kurraporDTO.getCins1());    
			preparedStatement.setString(p++, kurraporDTO.getCins2());   
			try (ResultSet rs = preparedStatement.executeQuery()) {
	            resultList = ResultSetConverter.convertToList(rs);
	        }
		} catch (Exception e) {
			throw new ServiceException("PG KurService genel hatası.", e);
		}
		return resultList;
	}

	@Override
	public List<Map<String, Object>> kur_oku(kurgirisDTO kurgirisDTO, ConnectionDetails kurConnDetails) {
		List<Map<String, Object>> resultList = new ArrayList<>();
		String sql = "SELECT \"KUR\",\"MA\",\"MS\",\"SA\",\"SS\",\"BA\",\"BS\",\"TARIH\"" +
				" FROM \"KURLAR\"" +
				" WHERE \"TARIH\" >= ? AND \"TARIH\" < ? AND \"KUR\" = ?";
		try (Connection connection = DriverManager.getConnection(kurConnDetails.getJdbcUrl(), kurConnDetails.getUsername(), kurConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			Timestamp[] ts = Global_Yardimci.rangeDayT2plusDay(kurgirisDTO.getTar(), kurgirisDTO.getTar());
	        int p = 1;
	        preparedStatement.setTimestamp(p++, ts[0]);           
	        preparedStatement.setTimestamp(p++, ts[1]);           
	        preparedStatement.setString(p++, kurgirisDTO.getDvz_turu());
			try (ResultSet rs = preparedStatement.executeQuery()) {
	            resultList = ResultSetConverter.convertToList(rs);
	        }
		} catch (Exception e) {
			throw new ServiceException("PG KurService genel hatası.", e);
		}
		return resultList;
	}
}