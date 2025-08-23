package com.hamit.obs.repository.adres;

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
import com.hamit.obs.dto.adres.adresDTO;
import com.hamit.obs.exception.ServiceException;

@Component
public class AdresMySQL implements IAdresDatabase{

	@Override
	public List<Map<String, Object>> hesap_kodlari(ConnectionDetails adresConnDetails) {
		String sql = "SELECT M_Kodu FROM Adres ORDER BY M_Kodu";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException("Hesap kodlari genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public adresDTO hsp_pln(String hesap, ConnectionDetails adresConnDetails) {
		String sql = "SELECT  * " + 
				" FROM Adres " + 
				" WHERE M_Kodu = '" + hesap + "'";
		adresDTO hsdto = new adresDTO();
		try (Connection connection = DriverManager.getConnection(adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				hsdto.setKodu(rs.getString("M_Kodu"));
				hsdto.setUnvan(rs.getString("Adi"));
				hsdto.setAd1(rs.getString("Adres_1"));
				hsdto.setSemt(rs.getString("Semt"));
				hsdto.setAd2(rs.getString("Adres_2"));
				hsdto.setSeh(rs.getString("Sehir"));
				hsdto.setPkodu(rs.getString("Posta_Kodu")); 
				hsdto.setSmsgon(rs.getBoolean("Sms_Gonder"));
				hsdto.setMailgon(rs.getBoolean("Mail_Gonder"));
				hsdto.setVd(rs.getString("Vergi_Dairesi"));
				hsdto.setVn(rs.getString("Vergi_No"));
				hsdto.setT1(rs.getString("Tel_1"));
				hsdto.setT2(rs.getString("Tel_2"));
				hsdto.setT3(rs.getString("Tel_3"));
				hsdto.setFx(rs.getString("Fax"));
				hsdto.setO1(rs.getString("Ozel_Kod_1"));
				hsdto.setO2(rs.getString("Ozel_Kod_2"));
				hsdto.setWeb(rs.getString("Web"));
				hsdto.setOzel(rs.getString("Ozel"));
				hsdto.setMail(rs.getString("E_Mail"));
				hsdto.setAcik(rs.getString("Aciklama"));
				hsdto.setNot1(rs.getString("Not_1"));
				hsdto.setNot2(rs.getString("Not_2"));
				hsdto.setNot3(rs.getString("Not_3"));
				hsdto.setYetkili(rs.getString("Yetkili"));		
				hsdto.setImage(rs.getBytes("Resim"));
				hsdto.setId(rs.getInt("ID"));
			}
		} catch (Exception e) {
			throw new ServiceException("Adres okunamadı", e);
		}
		return hsdto;
	}

	@Override
	public String adres_firma_adi(ConnectionDetails adresConnDetails) {
		String firmaIsmi = "";
		String query = "SELECT FIRMA_ADI FROM OZEL";
		try (Connection connection = DriverManager.getConnection(adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
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
	public void adres_kayit(adresDTO adresDTO, ConnectionDetails adresConnDetails) {
		String sql  = "INSERT INTO Adres (M_Kodu,Adi,Adres_1,Adres_2,Semt,Sehir,Posta_Kodu,Vergi_Dairesi,Vergi_No,Fax,Tel_1" +
				" ,Tel_2,Tel_3,Ozel,Yetkili,E_Mail,Not_1,Not_2,Not_3,Aciklama,Sms_Gonder,Mail_Gonder,Ozel_Kod_1,Ozel_Kod_2,Web,[USER],Resim) " +
				" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1,adresDTO.getKodu());
			stmt.setString(2,adresDTO.getUnvan());
			stmt.setString(3,adresDTO.getAd1());
			stmt.setString(4,adresDTO.getAd2());
			stmt.setString(5,adresDTO.getSemt());
			stmt.setString(6,adresDTO.getSeh());
			stmt.setString(7,adresDTO.getPkodu());
			stmt.setString(8,adresDTO.getVd());
			stmt.setString(9,adresDTO.getVn());
			stmt.setString(10,adresDTO.getFx());
			stmt.setString(11,adresDTO.getT1());
			stmt.setString(12,adresDTO.getT2());
			stmt.setString(13,adresDTO.getT3());
			stmt.setString(14,adresDTO.getOzel());
			stmt.setString(15,adresDTO.getYetkili());
			stmt.setString(16,adresDTO.getMail());
			stmt.setString(17,adresDTO.getNot1());
			stmt.setString(18,adresDTO.getNot2());
			stmt.setString(19,adresDTO.getNot3());
			stmt.setString(20,adresDTO.getAcik());
			stmt.setBoolean(21,adresDTO.isSmsgon());
			stmt.setBoolean(22,adresDTO.isMailgon());
			stmt.setString(23,adresDTO.getO1());
			stmt.setString(24,adresDTO.getO2());
			stmt.setString(25,adresDTO.getWeb());
			stmt.setString(26,adresDTO.getUsr());
			stmt.setBytes(27,adresDTO.getImage());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("Adres kayit Hata:" + e.getMessage());
		}
	}

	@Override
	public void adres_sil(int id, ConnectionDetails adresConnDetails) {
		String sql = "DELETE " +
				" FROM Adres WHERE ID = '" + id + "'"  ;
		try (Connection connection = DriverManager.getConnection(
				adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
				PreparedStatement deleteStmt = connection.prepareStatement(sql)) {
			deleteStmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Silme sırasında bir hata oluştu", e);
		}
	}

	@Override
	public String kod_ismi(String kodu, ConnectionDetails adresConnDetails) {
		String kodIsmi = null;
		String query = "SELECT Adi  FROM Adres WHERE M_Kodu = N'" + kodu + "'";
		try (Connection connection = DriverManager.getConnection(adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				kodIsmi = resultSet.getString("Adi");
			}
		} catch (SQLException e) {
			throw new ServiceException(e.getMessage());
		}
		return kodIsmi;
	}

	@Override
	public String[] adr_etiket_arama_kod(String kodu, ConnectionDetails adresConnDetails) {
		String[] kodIsmi = {"","","","","",""};
		String query = "SELECT Adi,Adres_1,Adres_2,Tel_1,Semt,Sehir FROM Adres"
				+ " WHERE M_Kodu Like N'" + kodu + "%'";
		try (Connection connection = DriverManager.getConnection(adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				kodIsmi[0] = resultSet.getString("Adi");
				kodIsmi[1] = resultSet.getString("Adres_1");
				kodIsmi[2] = resultSet.getString("Adres_2");
				kodIsmi[3] = resultSet.getString("Tel_1");
				kodIsmi[4] = resultSet.getString("Semt");
				kodIsmi[5] = resultSet.getString("Sehir");
			}
		} catch (SQLException e) {
			throw new ServiceException(e.getMessage());
		}
		return kodIsmi;
	}

	@Override
	public void adres_firma_adi_kayit(String fadi, ConnectionDetails adresConnDetails) {
		String sql = "UPDATE OZEL SET FIRMA_ADI = ? " ;
		try (Connection connection = DriverManager.getConnection(
				adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1,fadi);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	@Override
	public List<Map<String, Object>> adr_etiket(String siralama, ConnectionDetails adresConnDetails) {
		String sql =  "SELECT CAST(0 AS UNSIGNED) as cbox,Adi,Adres_1,Adres_2,Tel_1,Semt,Sehir FROM Adres ORDER BY " + siralama + "";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> adr_hpl(ConnectionDetails adresConnDetails) {
		String sql = "SELECT M_Kodu,Adi  FROM Adres  ORDER BY M_Kodu";
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException("MY adrService genel hatası.", e);
		}
		return resultList; 
	}
}
