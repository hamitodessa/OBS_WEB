package com.hamit.obs.repository.kur;

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
import com.hamit.obs.dto.kur.kurgirisDTO;
import com.hamit.obs.dto.kur.kurraporDTO;
import com.hamit.obs.exception.ServiceException;

@Component
public class KurMY implements  IKurDatabase{

	@Override
	public List<Map<String, Object>> kur_liste(String tarih, ConnectionDetails kurConnDetails) {
		List<Map<String, Object>> resultList = new ArrayList<>();
		String sql = "SELECT Kur,MA,MS,SA,SS,BA,BS" +
				" FROM KURLAR" +
				" WHERE Tarih ='" + tarih + "' ORDER BY Kur";
		try (Connection connection = DriverManager.getConnection(kurConnDetails.getJdbcUrl(), kurConnDetails.getUsername(), kurConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException("MS KurService genel hatası.", e);
		}
		return resultList;
	}

	@Override
	public void kur_sil(String tarih, String kurTuru, ConnectionDetails kurConnDetails) {
		String sql = "DELETE FROM Kurlar WHERE Tarih = ? AND Kur = ?";
	    try (Connection connection = DriverManager.getConnection(
	            kurConnDetails.getJdbcUrl(), kurConnDetails.getUsername(), kurConnDetails.getPassword());
	         PreparedStatement stmt = connection.prepareStatement(sql)) {
	    	stmt.setDate(1, java.sql.Date.valueOf(tarih));
	        stmt.setString(2, kurTuru);
	        stmt.executeUpdate();
	    } catch (SQLException e) {
	        throw new ServiceException("Kurlar tablosundan veri silinirken bir hata oluştu.", e);
	    }
	}

	@Override
	public boolean kur_kayit(kurgirisDTO kurgirisDTO, ConnectionDetails kurConnDetails) {
		String sql  = "INSERT INTO Kurlar (Tarih,Kur,MA,MS,SA,SS,BA,BS) " +
				" VALUES (?,?,?,?,?,?,?,?)" ;
		boolean drm = false;
		try (Connection connection = DriverManager.getConnection(
				kurConnDetails.getJdbcUrl(), kurConnDetails.getUsername(), kurConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setDate(1, java.sql.Date.valueOf(kurgirisDTO.getTar()));
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
		String sql = "SELECT Tarih,Kur,MA,MS,BA,BS,SA,SS" +
				" FROM Kurlar" +
				" WHERE Tarih BETWEEN '" + kurraporDTO.getStartDate() + "' AND '" + kurraporDTO.getEndDate() + "'" +
				" AND Kur BETWEEN '" + kurraporDTO.getCins1() + "' AND '" + kurraporDTO.getCins2() + "' ORDER BY Tarih DESC,Kur";
		try (Connection connection = DriverManager.getConnection(kurConnDetails.getJdbcUrl(), kurConnDetails.getUsername(), kurConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException("MS KurService genel hatası.", e);
		}
		return resultList;
	}

	@Override
	public List<Map<String, Object>> kur_oku(kurgirisDTO kurgirisDTO, ConnectionDetails kurConnDetails) {
		List<Map<String, Object>> resultList = new ArrayList<>();
		String sql = "SELECT Kur,MA,MS,SA,SS,BA,BS,Tarih" +
				" FROM Kurlar" +
				" WHERE Tarih ='" + kurgirisDTO.getTar() + "' AND Kur = N'" + kurgirisDTO.getDvz_turu() + "'";
		try (Connection connection = DriverManager.getConnection(kurConnDetails.getJdbcUrl(), kurConnDetails.getUsername(), kurConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException("MS KurService genel hatası.", e);
		}
		return resultList;
	}
}