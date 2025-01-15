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
public class AdresPgSQL implements IAdresDatabase {

	@Override
	public List<Map<String, Object>> hesap_kodlari(ConnectionDetails adresConnDetails) {
		String sql = "SELECT \"M_KODU\" FROM \"ADRES\" ORDER BY \"M_KODU\"";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException("Hesap Kodlari hatası.", e);
		}
		return resultList; 
	}

	@Override
	public adresDTO hsp_pln(String hesap, ConnectionDetails adresConnDetails) {
		String sql = "SELECT  * " + 
				" FROM \"ADRES\" " + 
				" WHERE \"M_KODU\" = '" + hesap + "'";
		adresDTO hsdto = new adresDTO();
		try (Connection connection = DriverManager.getConnection(adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				hsdto.setKodu(rs.getString("M_KODU"));
				hsdto.setUnvan(rs.getString("ADI"));
				hsdto.setAd1(rs.getString("ADRES_1"));
				hsdto.setSemt(rs.getString("SEMT"));
				hsdto.setAd2(rs.getString("ADRES_2"));
				hsdto.setSeh(rs.getString("SEHIR"));
				hsdto.setPkodu(rs.getString("POSTA_KODU")); 
				hsdto.setSmsgon(rs.getBoolean("SMS_GONDER"));
				hsdto.setMailgon(rs.getBoolean("MAIL_GONDER"));
				hsdto.setVd(rs.getString("VERGI_DAIRESI"));
				hsdto.setVn(rs.getString("VERGI_NO"));
				hsdto.setT1(rs.getString("TEL_1"));
				hsdto.setT2(rs.getString("TEL_2"));
				hsdto.setT3(rs.getString("TEL_3"));
				hsdto.setFx(rs.getString("FAX"));
				hsdto.setO1(rs.getString("OZEL_KOD_1"));
				hsdto.setO2(rs.getString("OZEL_KOD_2"));
				hsdto.setWeb(rs.getString("WEB"));
				hsdto.setOzel(rs.getString("OZEL"));
				hsdto.setMail(rs.getString("E_MAIL"));
				hsdto.setAcik(rs.getString("ACIKLAMA"));
				hsdto.setNot1(rs.getString("NOT_1"));
				hsdto.setNot2(rs.getString("NOT_2"));
				hsdto.setNot3(rs.getString("NOT_3"));
				hsdto.setYetkili(rs.getString("YETKILI"));		
				hsdto.setImage(rs.getBytes("RESIM"));
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
		String query = "SELECT \"FIRMA_ADI\" FROM \"OZEL\"";
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
		String sql  = "INSERT INTO \"ADRES\" (\"M_KODU\",\"ADI\",\"ADRES_1\",\"ADRES_2\",\"SEMT\",\"SEHIR\",\"POSTA_KODU\",\"VERGI_DAIRESI\",\"VERGI_NO\",\"FAX\",\"TEL_1\"" +
				" ,\"TEL_2\",\"TEL_3\",\"OZEL\",\"YETKILI\",\"E_MAIL\",\"NOT_1\",\"NOT_2\",\"NOT_3\",\"ACIKLAMA\",\"SMS_GONDER\",\"MAIL_GONDER\",\"OZEL_KOD_1\",\"OZEL_KOD_2\",\"WEB\",\"USER\",\"RESIM\") " +
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
		String query = "SELECT \"ADI\" FROM \"ADRES\" WHERE \"M_KODU\" = '" + kodu + "'";
		try (Connection connection = DriverManager.getConnection(adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				kodIsmi = resultSet.getString("ADI");
			}
		} catch (SQLException e) {
			throw new ServiceException( e.getMessage());
		}
		return kodIsmi;
	}

	@Override
	public String[] adr_etiket_arama_kod(String kodu, ConnectionDetails adresConnDetails) {
		String[] kodIsmi = {"","","","","",""};
		String sql = "SELECT \"ADI\",\"ADRES_1\",\"ADRES_2\", \"TEL_1\",\"SEMT\",\"SEHIR\"  FROM \"ADRES\" "
				+ " WHERE \"M_KODU\" Like '" + kodu + "%'";
		try (Connection connection = DriverManager.getConnection(adresConnDetails.getJdbcUrl(), adresConnDetails.getUsername(), adresConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				kodIsmi[0] = resultSet.getString("ADI");
				kodIsmi[1] = resultSet.getString("ADRES_1");
				kodIsmi[2] = resultSet.getString("ADRES_2");
				kodIsmi[3] = resultSet.getString("TEL_1");
				kodIsmi[4] = resultSet.getString("SEMT");
				kodIsmi[5] = resultSet.getString("SEHIR");
			}
		} catch (SQLException e) {
			throw new ServiceException("Adres okunamadı", e);
		}
		return kodIsmi;
	}

	@Override
	public void adres_firma_adi_kayit(String fadi, ConnectionDetails adresConnDetails) {
		String sql = "UPDATE \"OZEL\" SET \"FIRMA_ADI\" = ? " ;
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
		String sql = "SELECT  CAST(0 as bit) as cbox ,\"ADI\",\"ADRES_1\",\"ADRES_2\",\"TEL_1\",\"SEMT\",\"SEHIR\" FROM \"ADRES\" ORDER BY \"" + siralama.toUpperCase() +"\"";
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
}
