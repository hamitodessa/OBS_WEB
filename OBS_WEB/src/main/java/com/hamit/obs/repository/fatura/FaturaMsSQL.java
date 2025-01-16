package com.hamit.obs.repository.fatura;

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
import com.hamit.obs.dto.stok.urunDTO;
import com.hamit.obs.exception.ServiceException;

@Component
public class FaturaMsSQL implements IFaturaDatabase {

	@Override
	public String fat_firma_adi(ConnectionDetails faturaConnDetails) {
		String firmaIsmi = "";
		String query = "SELECT FIRMA_ADI FROM OZEL";
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
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
	public List<Map<String, Object>> urun_kodlari(ConnectionDetails faturaConnDetails) {
		String sql = "SELECT Kodu FROM MAL ORDER BY Kodu";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
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
	public urunDTO stk_urun(String sira, String arama, ConnectionDetails faturaConnDetails) {
		String sql = "SELECT Kodu,Adi,Birim,Kusurat,Sinif,Ana_Grup,Alt_Grup,Aciklama_1,Aciklama_2 ," +
				" Ozel_Kod_1 ,Ozel_Kod_2 ,Barkod,Mensei,Agirlik,Resim,Fiat,Fiat_2,Fiat_3,Recete,[USER]" +
				" FROM MAL WITH (INDEX (IX_MAL))  WHERE Kodu = '" + arama + "'  ORDER by " + sira ;
		System.out.println(sql);
		urunDTO urdto = new urunDTO();
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				urdto.setKodu(rs.getString("Kodu"));
				urdto.setAdi(rs.getString("Adi"));
				urdto.setBirim(rs.getString("Birim"));
				urdto.setKusurat(rs.getInt("Kusurat"));
				urdto.setSinif(rs.getString("Sinif"));
				urdto.setAnagrup(urun_kod_degisken_ara("ANA_GRUP",  "AGID_Y","ANA_GRUP_DEGISKEN",String.valueOf(rs.getInt("Ana_Grup")),faturaConnDetails));
				urdto.setAltgrup(urun_kod_degisken_ara("ALT_GRUP","ALID_Y",  "ALT_GRUP_DEGISKEN",String.valueOf(rs.getInt("Alt_Grup")),faturaConnDetails));
				urdto.setAciklama1(rs.getString("Aciklama_1"));
				urdto.setAciklama2(rs.getString("Aciklama_2"));
				urdto.setOzelkod1(urun_kod_degisken_ara("OZEL_KOD_1","OZ1ID",  "OZ_KOD_1_DEGISKEN",String.valueOf(rs.getInt("Ozel_Kod_1")),faturaConnDetails));
				urdto.setOzelkod2(urun_kod_degisken_ara("OZEL_KOD_2","OZ2ID",  "OZ_KOD_2_DEGISKEN",String.valueOf(rs.getInt("Ozel_Kod_2")),faturaConnDetails));
				urdto.setKdv(0);
				urdto.setBarkod(rs.getString("Barkod"));
				urdto.setMensei(urun_kod_degisken_ara("MENSEI","MEID_Y",  "MENSEI_DEGISKEN",String.valueOf(rs.getInt("Mensei")),faturaConnDetails));
				urdto.setAgirlik(rs.getDouble("Agirlik"));
				urdto.setFiat1(rs.getDouble("Fiat"));
				urdto.setFiat2(rs.getDouble("Fiat_2"));
				urdto.setFiat3(rs.getDouble("Fiat_3"));
				urdto.setRecete(rs.getString("Recete"));
				urdto.setImage(rs.getBytes("Resim"));
			}
		} catch (Exception e) {
			throw new ServiceException("Urun okunamadı", e);
		}
		return urdto;
	}

	@Override
	public String urun_kod_degisken_ara(String fieldd, String sno, String nerden, String arama,
			ConnectionDetails faturaConnDetails) {
		String query = "SELECT  " + fieldd + " FROM " + nerden + " WHERE " + sno + " = N'" + arama + "'";
		String deger = "" ;
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
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
	public List<Map<String, Object>> stk_kod_degisken_oku(String fieldd, String sno, String nerden,
			ConnectionDetails faturaConnDetails) {
		String sql =  "SELECT " + sno + "  AS KOD , " + fieldd + " FROM " + nerden + "" +
				" ORDER BY " + fieldd + "";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
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
	public List<Map<String, Object>> stk_kod_alt_grup_degisken_oku(int sno, ConnectionDetails faturaConnDetails) {

			String sql =  "SELECT ALID_Y , ALT_GRUP FROM ALT_GRUP_DEGISKEN   " +
					" WHERE ANA_GRUP = N'" + sno + "' ORDER BY ALT_GRUP";
			List<Map<String, Object>> resultList = new ArrayList<>();
			try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
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
	public String ur_kod_bak(String kodu, ConnectionDetails faturaConnDetails) {
		String firmaIsmi = "";
		String query = "SELECT Adi FROM MAL  WITH (INDEX (IX_MAL))  WHERE Kodu = N'" + kodu + "'" ;
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				firmaIsmi = resultSet.getString("Adi");
			}
		} catch (SQLException e) {
			throw new ServiceException("Urun okunamadı", e);
		}
		return firmaIsmi;

	}
}
