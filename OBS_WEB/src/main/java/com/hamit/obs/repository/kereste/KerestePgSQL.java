package com.hamit.obs.repository.kereste;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.dto.kereste.kerestedetayDTO;
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
		String sql =   "SELECT  \"" + fieldd + "\" FROM \"" + nerden + "\" WHERE \"" + sno + "\" = '" + arama + "'";
		String deger = "" ;
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
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
		String sql =   "SELECT \"ALID_Y\" , \"ALT_GRUP\" FROM \"ALT_GRUP_DEGISKEN\"   " +
				" WHERE \"ANA_GRUP\" = '" + sno + "' ORDER BY \"ALT_GRUP\"";
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
		String sql =  "UPDATE \"" + nerden + "\"  SET \"" + fieldd + "\"  = ?  WHERE \"" + sno + "\"  = ? ";
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
		String sql = "UPDATE \"ALT_GRUP_DEGISKEN\"  SET \"ALT_GRUP\"  = ? , \"ANA_GRUP\"  = ?  WHERE \"ALID_Y\" = ? ";
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
		String sql =   "SELECT max(\"" + fieldd + "\")  as maks  FROM \"" + nerden+ "\"" ; 
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement selectStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = selectStatement.executeQuery();
			if (resultSet.next()) {
				maks = resultSet.getInt("maks");
			}
			sql = "INSERT INTO \"" + nerden + "\" (\"" + fieldd + "\",\"" + degisken_adi + "\",\"USER\") " +
					" VALUES (?,?,?)" ;
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
		String sql =   "SELECT max(\"ALID_Y\")  AS \"ALID_Y\"  FROM \"ALT_GRUP_DEGISKEN\"   " ;
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
			sql = "INSERT INTO \"ALT_GRUP_DEGISKEN\" (\"ALID_Y\",\"ALT_GRUP\",\"ANA_GRUP\",\"USER\") " +
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
		String sql = "DELETE \"ALT_GRUP_DEGISKEN\"    WHERE \"ALID_Y\" = ? ";
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
		String sql = "DELETE \"" + nerden  + "\"    WHERE \"" + hangi_Y +"\" = ? ";
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
				"SELECT * FROM \"MAL\" WHERE \"Ana_Grup\" = ? AND \"Alt_Grup\" = ?",
				"SELECT * FROM \"FATURA\" WHERE \"Ana_Grup\" = ? AND \"Alt_Grup\" = ?",
				"SELECT * FROM \"IRSALIYE\" WHERE \"Ana_Grup\" = ? AND \"Alt_Grup\" = ?",
				"SELECT * FROM \"RECETE\" WHERE \"Ana_Grup\" = ? AND \"Alt_Grup\" = ?",
				"SELECT * FROM \"STOK\" WHERE \"Ana_Grup\" = ? AND \"Alt_Grup\" = ?"
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
		String sql = "UPDATE \"OZEL\" SET \"FIRMA_ADI\" = ? " ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1,fadi);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}

	@Override
	public String son_no_al(String cins, ConnectionDetails keresteConnDetails) {
		String result = "" ;
		String sql ;
		if (cins.equals("G"))
			sql = "SELECT max(\"Evrak_No\")  as \"NO\" FROM \"KERESTE\"  ";
		else
			sql = "SELECT max(\"Cikis_Evrak\")  as \"NO\" FROM \"KERESTE\"  ";
		
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				result = resultSet.getString("NO");
			}
		} catch (SQLException e) {
			throw new ServiceException("Son Fis", e);
		}
		return result;

	}

	@Override
	public int evrak_no_al(String cins, ConnectionDetails keresteConnDetails) {
		String sql ;
		int E_NUMBER = 0 ;
		
		if (cins.equals("G"))
			sql = "SELECT CASE WHEN max(\"Evrak_No\") = '' THEN '1' ELSE max(\"Evrak_No\")::int + 1 END AS \"NO\" FROM  \"KERESTE\" ";
		else
			sql = "SELECT CASE WHEN max(\"Cikis_Evrak\") = '' THEN '1' ELSE max(\"Cikis_Evrak\")::int + 1 END AS \"NO\" FROM \"KERESTE\" ";
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				E_NUMBER = resultSet.getInt("NO");
			}
		} catch (SQLException e) {
			throw new ServiceException("Firma adı okunamadı", e);
		}
		return E_NUMBER;

	}

	@Override
	public List<Map<String, Object>> ker_oku(String eno, String cins, ConnectionDetails keresteConnDetails) {
		String sql = "";
		if (cins.equals("G")) {
			sql = "SELECT   \"Evrak_No\" ,\"Barkod\" ,\"Kodu\",\"Paket_No\",\"Konsimento\" ,\"Miktar\",\"Tarih\",\"Kdv\" ,\"Doviz\" ,\"Fiat\"  ,\"Tutar\" ,\"Kur\"  ,\"Cari_Firma\",\"Adres_Firma\"  ,\"Iskonto\" ,\"Tevkifat\" , "
					+ " COALESCE((SELECT \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID_Y\" = \"KERESTE\".\"CAna_Grup\"),'') AS \"C_Ana_Grup\" ,"
					+ "	COALESCE((SELECT \"ALT_GRUP\" FROM \"ALT_GRUP_DEGISKEN\" WHERE \"ALT_GRUP_DEGISKEN\".\"ALID_Y\" = \"KERESTE\".\"CAlt_Grup\"),'') AS \"C_Alt_Grup\" ,"
					+ " COALESCE((SELECT \"DEPO\" FROM \"DEPO_DEGISKEN\" WHERE \"DEPO_DEGISKEN\".\"DPID_Y\" = \"KERESTE\".\"CDepo\"),'') AS \"C_Depo\" ,"
					+ " COALESCE((SELECT \"OZEL_KOD_1\" FROM \"OZ_KOD_1_DEGISKEN\" WHERE \"OZ_KOD_1_DEGISKEN\".\"OZ1ID_Y\" = \"KERESTE\".\"COzel_Kod\"),'') \"COzel_Kod\" ,"
					+ " COALESCE((SELECT \"MENSEI\" FROM \"MENSEI_DEGISKEN\" WHERE \"MENSEI_DEGISKEN\".\"MEID_Y\" = \"KERESTE\".\"Mensei\"),'') AS \"_Mensei\" ,"
					+ " COALESCE((SELECT \"UNVAN\" FROM \"NAKLIYECI\" WHERE \"NAKLIYECI\".\"NAKID_Y\" = \"KERESTE\".\"CNakliyeci\" ),'') as \"C_Nakliyeci\" , " 
					+ " \"Izahat\"  , \"USER\" ,"
					+ "	\"Cikis_Evrak\"  ,TO_CHAR(\"CTarih\",'dd.MM.yyyy HH:mm:ss ms') AS \"CTarih\"  ,\"CKdv\" ,\"CDoviz\"  ,\"CFiat\" ,\"CTutar\" ,\"CKur\" ,\"CCari_Firma\" ,\"CAdres_Firma\" ,\"CIskonto\"  ,\"CTevkifat\" "
					+ "	,\"CAna_Grup\"    ,\"CAlt_Grup\"  ,\"CDepo\"  ,\"COzel_Kod\"   ,\"CIzahat\"  ,\"CNakliyeci\"  ,\"CUSER\",\"CSatir\"" 
					+ " FROM \"KERESTE\"   " 
					+ " WHERE \"Evrak_No\"  = '" + eno + "' Order by  \"Satir\" Limit 250 " ; 
		}
		else {
			sql = "SELECT   \"Evrak_No\" ,\"Barkod\" ,\"Kodu\",\"Paket_No\",\"Konsimento\" ,\"Miktar\",\"Tarih\",\"Kdv\" ,\"Doviz\" ,\"Fiat\"  ,\"Tutar\" ,\"Kur\"  ,\"Cari_Firma\",\"Adres_Firma\"  ,\"Iskonto\" ,\"Tevkifat\" , "
					+ " COALESCE((SELECT \"ANA_GRUP\" FROM \"ANA_GRUP_DEGISKEN\" WHERE \"ANA_GRUP_DEGISKEN\".\"AGID_Y\" = \"KERESTE\".\"CAna_Grup\"),'') AS \"C_Ana_Grup\" ,"
					+ "	COALESCE((SELECT \"ALT_GRUP\" FROM \"ALT_GRUP_DEGISKEN\" WHERE \"ALT_GRUP_DEGISKEN\".\"ALID_Y\" = \"KERESTE\".\"CAlt_Grup\"),'') AS \"C_Alt_Grup\" ,"
					+ " COALESCE((SELECT \"DEPO\" FROM \"DEPO_DEGISKEN\" WHERE \"DEPO_DEGISKEN\".\"DPID_Y\" = \"KERESTE\".\"CDepo\"),'') AS \"C_Depo\" ,"
					+ " COALESCE((SELECT \"OZEL_KOD_1\" FROM \"OZ_KOD_1_DEGISKEN\" WHERE \"OZ_KOD_1_DEGISKEN\".\"OZ1ID_Y\" = \"KERESTE\".\"COzel_Kod\"),'') \"COzel_Kod\" ,"
					+ " COALESCE((SELECT \"MENSEI\" FROM \"MENSEI_DEGISKEN\" WHERE \"MENSEI_DEGISKEN\".\"MEID_Y\" = \"KERESTE\".\"Mensei\"),'') AS \"_Mensei\" ,"
					+ " COALESCE((SELECT \"UNVAN\" FROM \"NAKLIYECI\" WHERE \"NAKLIYECI\".\"NAKID_Y\" = \"KERESTE\".\"CNakliyeci\" ),'') as \"C_Nakliyeci\" , " 
					+ " \"Izahat\"   ,\"USER\" ,"
					+ "	\"Cikis_Evrak\"  ,\"CTarih\"   ,\"CKdv\" ,\"CDoviz\"  ,\"CFiat\" ,\"CTutar\" ,\"CKur\" ,\"CCari_Firma\" ,\"CAdres_Firma\" ,\"CIskonto\"  ,\"CTevkifat\" "
					+ "	,\"CAna_Grup\"    ,\"CAlt_Grup\"  ,COALESCE((Select \"DEPO\" FROM \"DEPO_DEGISKEN\" WHERE \"DEPO_DEGISKEN\".\"DPID_Y\" = \"KERESTE\".\"CDepo\" ) , '') AS \"CDepo\"  ,\"COzel_Kod\"   ,\"CIzahat\"  ,\"CNakliyeci\"  ,\"CUSER\",\"Satir\"" 
					+ " FROM \"KERESTE\"   " 
					+ " WHERE \"Cikis_Evrak\"  = '" + eno + "' ORDER BY \"CSatir\"  Limit 250" ;
		}
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
	public String aciklama_oku(String evrcins, int satir, String evrno, String gircik,
			ConnectionDetails keresteConnDetails) {
		String result = "";
		String sql =     "SELECT * " +
				" FROM \"ACIKLAMA\"  " +
				" WHERE \"EVRAK_NO\" = '" + evrno + "'" +
				" AND \"SATIR\" = '" + satir + "'" +
				" AND \"EVRAK_CINS\" = '" + evrcins + "'" +
				" AND \"Gir_Cik\" = '" + gircik + "'";
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				result = resultSet.getString("ACIKLAMA");
			}
		} catch (SQLException e) {
			throw new ServiceException("Firma adı okunamadı", e);
		}
		return result;
	}

	@Override
	public String[] dipnot_oku(String ino, String cins, String gircik,
			ConnectionDetails keresteConnDetails) {
		String[] dipnot = {"","",""};
		String sql =  "SELECT * " +
				" FROM \"DPN\"  " +
				" WHERE \"Evrak_No\" = '" + ino + "'" +
				" AND \"Tip\" = '" + cins + "'" +
				" AND \"Gir_Cik\" = '" + gircik + "'";
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				dipnot[0] = resultSet.getString("Bir");
				dipnot[1] = resultSet.getString("Iki");
				dipnot[2] = resultSet.getString("Uc");
			} 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return dipnot; 
	}

	@Override
	public List<Map<String, Object>> paket_oku(String pno, String nerden, ConnectionDetails keresteConnDetails) {
		String[] token = pno.toString().split("-");
		String dURUMString= "";
		if(nerden.equals("C"))
			dURUMString= " AND \"Cikis_Evrak\" = ''";
		String sql = "SELECT   \"Evrak_No\" ,\"Barkod\" ,\"Kodu\",\"Paket_No\",\"Konsimento\" ,\"Miktar\",\"Cikis_Evrak\"  ," 
				+ " TO_CHAR(\"CTarih\",'dd.MM.yyyy HH:mm:ss ms') AS \"CTarih\"  ,"
				+ " \"CKdv\" ,\"CDoviz\"  ,\"CFiat\" ,\"CTutar\" ,\"CKur\", " 
				+ " \"CCari_Firma\" ,\"CAdres_Firma\" ,\"CIskonto\"  ,\"CTevkifat\",\"CAna_Grup\"    ,\"CAlt_Grup\" , "
				+ " COALESCE((Select \"DEPO\" FROM \"DEPO_DEGISKEN\" WHERE \"DEPO_DEGISKEN\".\"DPID_Y\" = \"KERESTE\".\"CDepo\" ) , '') AS \"CDepo\"  ," 
				+ " \"COzel_Kod\"   ,\"CIzahat\"  ,\"CNakliyeci\"  ,\"CUSER\",\"Satir\"" 
				+ " FROM \"KERESTE\"   " 
				+ " WHERE \"Paket_No\" = '" + token[0] + "' AND \"Konsimento\" = '"+ token[1] + "' "
				+ dURUMString + "  ORDER BY \"Satir\"" ;
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
	public void ker_kaydet(kerestedetayDTO kerestedetayDTO, ConnectionDetails keresteConnDetails) {
		String sql  ="INSERT INTO \"KERESTE\" (\"Evrak_No\",\"Barkod\",\"Kodu\",\"Paket_No\",\"Konsimento\",\"Miktar\",\"Tarih\",\"Kdv\",\"Doviz\",\"Fiat\",\"Tutar\",\"Kur\",\"Cari_Firma\",\"Adres_Firma\",\"Iskonto\" " + //15
				" ,\"Tevkifat\",\"Ana_Grup\",\"Alt_Grup\",\"Depo\",\"Ozel_Kod\",\"Izahat\",\"Nakliyeci\",\"USER\",\"Cikis_Evrak\",\"CTarih\",\"CKdv\",\"CDoviz\",\"CFiat\",\"CTutar\",\"CKur\",\"CCari_Firma\",\"CAdres_Firma\" " + //17
				" ,\"CIskonto\",\"CTevkifat\",\"CAna_Grup\",\"CAlt_Grup\",\"CDepo\",\"COzel_Kod\",\"CIzahat\",\"CNakliyeci\",\"CUSER\",\"Mensei\",\"Satir\",\"CSatir\") " + 
				" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;

		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1,kerestedetayDTO.getFisno());
			stmt.setString(2, kerestedetayDTO.getBarkod());
			stmt.setString(3,kerestedetayDTO.getUkodu());
			stmt.setString(4,kerestedetayDTO.getPaketno());
			stmt.setString(5,kerestedetayDTO.getKonsimento());
			stmt.setDouble(6, kerestedetayDTO.getMiktar());
			stmt.setTimestamp(7,Timestamp.valueOf(kerestedetayDTO.getTarih()));
			stmt.setDouble(8, kerestedetayDTO.getKdv());
			stmt.setString(9,kerestedetayDTO.getDvzcins());
			stmt.setDouble(10, kerestedetayDTO.getFiat());
			stmt.setDouble(11, kerestedetayDTO.getTutar());
			stmt.setDouble(12, kerestedetayDTO.getKur());
			stmt.setString(13,kerestedetayDTO.getCarikod());
			stmt.setString(14,kerestedetayDTO.getAdreskod());
			stmt.setDouble(15, kerestedetayDTO.getIskonto());
			stmt.setDouble(16, kerestedetayDTO.getTevoran());
			stmt.setInt(17, kerestedetayDTO.getAnagrup());
			stmt.setInt(18, kerestedetayDTO.getAltgrup());
			stmt.setInt(19, kerestedetayDTO.getDepo());
			stmt.setInt(20,kerestedetayDTO.getOzelkod());
			stmt.setString(21,kerestedetayDTO.getIzahat());
			stmt.setInt(22, kerestedetayDTO.getNakliyeci());
			stmt.setString(23,  kerestedetayDTO.getUser());
			stmt.setString(24,kerestedetayDTO.getCevrak());
			stmt.setTimestamp(25,Timestamp.valueOf(kerestedetayDTO.getCtarih()));
			stmt.setDouble(26, kerestedetayDTO.getCkdv());
			stmt.setString(27,kerestedetayDTO.getCdoviz());
			stmt.setDouble(28, kerestedetayDTO.getCfiat());
			stmt.setDouble(29, kerestedetayDTO.getCtutar());
			stmt.setDouble(30, kerestedetayDTO.getCkur());
			stmt.setString(31, kerestedetayDTO.getCcarifirma());
			stmt.setString(32, kerestedetayDTO.getCadresfirma());
			stmt.setDouble(33, kerestedetayDTO.getCiskonto());
			stmt.setDouble(34, kerestedetayDTO.getCtevkifat());
			stmt.setInt(35, kerestedetayDTO.getCanagrup());
			stmt.setInt(36, kerestedetayDTO.getCaltgrup());
			stmt.setInt(37, kerestedetayDTO.getCdepo());
			stmt.setInt(38, kerestedetayDTO.getCozelkod());
			stmt.setString(39, kerestedetayDTO.getCizahat());
			stmt.setInt(40, kerestedetayDTO.getCnakliyeci());
			stmt.setString(41,  kerestedetayDTO.getCuser());
			stmt.setInt(42,kerestedetayDTO.getMensei());
			stmt.setInt(43, kerestedetayDTO.getSatir());
			stmt.setInt(44, kerestedetayDTO.getCsatir());
			stmt.executeUpdate();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("Urun kayit Hata:" + e.getMessage());
		}
	}

	@Override
	public void ker_giris_sil(String eno, ConnectionDetails keresteConnDetails) {
		String sql =  " DELETE " +
				" FROM \"KERESTE\" " +
				" WHERE \"Evrak_No\"  = ? " ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, eno);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("stok sil", e);
		}
	}

	@Override
	public void dipnot_sil(String ino, String cins, String gircik, ConnectionDetails keresteConnDetails) {
		String sql = " DELETE " +
				" FROM \"DPN\" " +
				" WHERE \"Evrak_No\" = '" + ino + "'" +
				" AND \"Tip\" = '" + cins + "'" +
				" AND \"Gir_Cik\" = '" + gircik + "'";
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("stok sil", e);
		}
	}

	@Override
	public void dipnot_yaz(String eno, String bir, String iki, String uc, String tip, String gircik, String usr,
			ConnectionDetails keresteConnDetails) {
		String sql  ="INSERT INTO \"DPN\" (\"Evrak_No\",\"Tip\",\"Bir\",\"Iki\",\"Uc\",\"Gir_Cik\",\"USER\") " +
				" VALUES (?,?,?,?,?,?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, eno);
			preparedStatement.setString(2, tip);
			preparedStatement.setString(3, bir);
			preparedStatement.setString(4, iki);
			preparedStatement.setString(5, uc);
			preparedStatement.setString(6,gircik);
			preparedStatement.setString(7, usr);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("stok sil", e);
		}
	}

	@Override
	public void aciklama_sil(String evrcins, String evrno, String cins, ConnectionDetails keresteConnDetails) {
		String sql = " DELETE " +
				" FROM \"ACIKLAMA\" " +
				" WHERE \"EVRAK_CINS\" = '" + evrcins + "'" +
				" AND \"EVRAK_NO\" = '" + evrno + "'" +
				" AND \"Gir_Cik\" = '" + cins + "'";
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("stok sil", e);
		}
	}

	@Override
	public void aciklama_yaz(String evrcins, int satir, String evrno, String aciklama, String gircik,
			ConnectionDetails keresteConnDetails) {
		String sql  ="INSERT INTO \"ACIKLAMA\" (\"EVRAK_CINS\",\"SATIR\",\"EVRAK_NO\",\"ACIKLAMA\",\"Gir_Cik\") " +
				" VALUES (?,?,?,?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, evrcins);
			preparedStatement.setInt(2, satir);
			preparedStatement.setString(3, evrno);
			preparedStatement.setString(4, aciklama);
			preparedStatement.setString(5, gircik);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("stok sil", e);
		}
	}

	@Override
	public List<Map<String, Object>> ker_barkod_kod_oku(String sira, ConnectionDetails keresteConnDetails) {
		String sql =  "SELECT  DISTINCT CONCAT(\"Paket_No\", '-', \"Konsimento\") AS \"Paket_No\" FROM \"KERESTE\"   " +
				" WHERE \"Cikis_Evrak\" = '' " +
				" ORDER by \"" + sira + "\"";
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
	public void ker_cikis_sil(String eno, ConnectionDetails keresteConnDetails) {
		String sql = "UPDATE \"KERESTE\" SET \"Cikis_Evrak\" = '', \"CTarih\" = '1900-01-01 00:00:00',\"CKdv\" = 0,\"CDoviz\" ='',\"CFiat\"=0,\"CTutar\"=0,\"CKur\"=0,\"CCari_Firma\"='',\"CAdres_Firma\"='' ," 
				+ " \"CIskonto\"=0,\"CTevkifat\"=0,\"CAna_Grup\"=0,\"CAlt_Grup\"=0,\"CDepo\"=0,\"COzel_Kod\"=0,\"CIzahat\"='',\"CNakliyeci\"=0,\"CUSER\"='' ,\"CSatir\"=0"
				+ " WHERE \"Cikis_Evrak\"  = ? " ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, eno);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("stok sil", e);
		}
	}

	@Override
	public void ker_cikis_kaydet(kerestedetayDTO kerestedetayDTO, ConnectionDetails keresteConnDetails) {
		String[] token = kerestedetayDTO.getPaketno().toString().split("-");
		String sql = "UPDATE \"KERESTE\" SET " 
				+ " \"Cikis_Evrak\" = '"+ kerestedetayDTO.getCevrak() +"', \"CTarih\" = '" + kerestedetayDTO.getCtarih() + "', " 
				+ " \"CKdv\" = "+ kerestedetayDTO.getCkdv() + ",\"CDoviz\" = '"+ kerestedetayDTO.getCdoviz() + "', "  
				+ " \"CFiat\" = "+ kerestedetayDTO.getCfiat() + ",\"Ctutar\" = "+ kerestedetayDTO.getCtutar() + ", " 
				+ " \"CKur\" = "+ kerestedetayDTO.getCkur() + ",\"CCari_Firma\" = '"+ kerestedetayDTO.getCcarifirma() +"' ," 
				+ " \"CAdres_Firma\" = '"+ kerestedetayDTO.getCadresfirma() + "' ," 
				+ " \"CIskonto="+ kerestedetayDTO.getCiskonto() + ",\"CTevkifat\" = "+ kerestedetayDTO.getCtevkifat() +" ,"
				+ " \"CAna_Grup="+ kerestedetayDTO.getCanagrup() + ",\"CAlt_Grup\" = "+ kerestedetayDTO.getCaltgrup() + ", " 
				+ " \"CDepo="+ kerestedetayDTO.getCdepo() + ",\"COzel_Kod\" = "+ kerestedetayDTO.getCozelkod() +" ," 
				+ " \"CIzahat='"+ kerestedetayDTO.getCizahat() +"',\"CNakliyeci\" = "+ kerestedetayDTO.getCnakliyeci() + ", " 
				+ " \"CUSER='"+ kerestedetayDTO.getCuser() +"',"
				+ " \"CSatir="+ kerestedetayDTO.getCsatir() +""
				+ " WHERE \"Paket_No\"  ='" + token[0] + "' AND \"Konsimento\" = '"+ token[1] +"' "
				+ " AND \"Satir\" = "+ kerestedetayDTO.getSatir() + "" ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}

	@Override
	public List<Map<String, Object>> kod_pln(ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void kod_kayit(String kodu, String aciklama, ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void kod_sil(String kod, ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Map<String, Object>> kons_pln(ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void kons_kayit(String kodu, String aciklama, int paket_no, ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int kons_sil(String kod, ConnectionDetails keresteConnDetails) {
		// TODO Auto-generated method stub
		return 0;
	}

}
