package com.hamit.obs.repository.loglama;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.dto.loglama.LoglamaDTO;
import com.hamit.obs.exception.ServiceException;

@Component
public class LoglamaRepository {

	public void log_kaydet(LoglamaDTO loglamaDTO ,  ConnectionDetails connDetails) {
		if(! connDetails.isLoglama()) return  ; 
		String sql = "INSERT INTO LOGLAMA (TARIH, EVRAK, MESAJ, USER_NAME) VALUES (?, ?, ?, ?)";
		try (Connection connection = DriverManager.getConnection(
				connDetails.getJdbcUrlLog(), connDetails.getUsername(), connDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			stmt.setString(2, loglamaDTO.getEvrak());
			stmt.setString(3, loglamaDTO.getMesaj());
			stmt.setString(4, loglamaDTO.getUser());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}
	
public List<Map<String, Object>> logRapor(String user, String startDate,String endDate,String aciklama ,ConnectionDetails connDetails){
		String sql = "" ;
		if(connDetails.getSqlTipi().equals(sqlTipi.MSSQL)) {
				sql = " SELECT FORMAT (TARIH,'dd.MM.yyyy HH:mm:ss fff') AS TARIH,MESAJ,EVRAK,USER_NAME" +
					 " FROM LOGLAMA WITH (INDEX (IX_LOGLAMA))" + 
					 " WHERE MESAJ LIKE N'%" + aciklama + "%'" + 
					 " AND TARIH BETWEEN '" + startDate + "' AND '" + endDate + " 23:59:59.998'" + 
					 " AND USER_NAME  LIKE '" + user + "%'";
		}else if(connDetails.getSqlTipi().equals(sqlTipi.MYSQL)) {
			sql = "SELECT DATE_FORMAT(TARIH,'%d.%m.%Y %H:%i:%s') AS TARIH,MESAJ,EVRAK,USER_NAME" + 
					" FROM LOGLAMA USE INDEX (IX_LOGLAMA)" + 
					" WHERE MESAJ LIKE N'%" + aciklama + "%'" + 
					" AND TARIH BETWEEN '" + startDate + "' AND '" + endDate + " 23:59:59.998'" + 
					" AND USER_NAME LIKE '" + user + "%'";
		} else if(connDetails.getSqlTipi().equals(sqlTipi.PGSQL)) {
			sql = "SELECT TO_CHAR(\"TARIH\",'dd.MM.yyyy HH:mm:ss ms') AS \"TARIH\",\"MESAJ\",\"EVRAK\",\"USER_NAME\"" +
					" FROM \"LOGLAMA\"" + 
					" WHERE \"MESAJ\"::text LIKE '%" + aciklama + "%'" +
					" AND \"TARIH\" BETWEEN '" + startDate + "' AND '" + endDate + " 23:59:59.998'" + 
					" AND \"USER_NAME\"::text LIKE '" + user + "'";
		}
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(connDetails.getJdbcUrlLog(), connDetails.getUsername(), connDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("log okuma", e); 
		}
		return resultList; 
	}
}