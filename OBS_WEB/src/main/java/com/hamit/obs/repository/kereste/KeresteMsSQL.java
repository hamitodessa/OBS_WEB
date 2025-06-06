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
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.enums.modulbaslikTipi;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.dto.kereste.kerestedetayDTO;
import com.hamit.obs.dto.kereste.kerestedetayraporDTO;
import com.hamit.obs.dto.kereste.kergrupraporDTO;
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

	@Override
	public String son_no_al(String cins, ConnectionDetails keresteConnDetails) {
		String result = "" ;
		String sql ;
		if (cins.equals("G"))
			sql = "SELECT max(Evrak_No)  as NO FROM KERESTE  ";
		else
			sql = "SELECT max(Cikis_Evrak)  as NO FROM KERESTE  ";

		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				result = resultSet.getString("NO");
			}
		} catch (SQLException e) {
			throw new ServiceException("Firma adı okunamadı", e);
		}
		return result;
	}

	@Override
	public int evrak_no_al(String cins, ConnectionDetails keresteConnDetails) {
		String sql ;
		int E_NUMBER = 0 ;
		if (cins.equals("G"))
			sql = "SELECT max(Evrak_No + 1) AS NO  FROM KERESTE  ";
		else
			sql = "SELECT max(Cikis_Evrak + 1) AS NO  FROM KERESTE  ";

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
			sql = "SELECT top 250  [Evrak_No] ,[Barkod] ,[Kodu],[Paket_No],[Konsimento] ,[Miktar],[Tarih],[Kdv] ,[Doviz] ,[Fiat]  ,[Tutar] ,[Kur]  ,[Cari_Firma],[Adres_Firma]  ,[Iskonto] ,[Tevkifat], "
					+ " ISNULL((Select ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = KERESTE.Ana_Grup ) , '') AS Ana_Grup   , " 
					+ " ISNULL((Select ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = KERESTE.Alt_Grup ) , '') AS Alt_Grup , " 
					+ " ISNULL((Select MENSEI FROM MENSEI_DEGISKEN WHERE MENSEI_DEGISKEN.MEID_Y = KERESTE.Mensei ) , '') AS Mensei, " 
					+ " ISNULL((Select DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = KERESTE.Depo ) , '') AS Depo  , " 
					+ " ISNULL((SELECT OZEL_KOD_1 FROM OZ_KOD_1_DEGISKEN WHERE OZ_KOD_1_DEGISKEN.OZ1ID_Y = KERESTE.Ozel_Kod),'') Ozel_Kod  , " 
					+ " [Izahat]  , " 
					+ " ISNULL((SELECT UNVAN FROM NAKLIYECI WHERE NAKLIYECI.NAKID_Y = KERESTE.Nakliyeci ),'') Nakliyeci , " 
					+ " [USER] "
					+ "	,[Cikis_Evrak]  ,FORMAT(CTarih, 'yyyy-MM-dd HH:mm:ss.fff') AS CTarih ,[CKdv] ,[CDoviz]  ,[CFiat] ,[CTutar] ,[CKur] ,[CCari_Firma] ,[CAdres_Firma] ,[CIskonto]  ,[CTevkifat] "
					+ "	,[CAna_Grup]    ,[CAlt_Grup]  ,CDepo  ,[COzel_Kod]   ,[CIzahat]  ,[CNakliyeci]  ,[CUSER],[CSatir]" 
					+ " FROM KERESTE   " 
					+ " WHERE Evrak_No  = N'" + eno + "' Order by  Satir " ; 
		}
		else {
			sql = "SELECT   [Evrak_No] ,[Barkod] ,[Kodu],[Paket_No],[Konsimento] ,[Miktar],[Tarih],[Kdv] ,[Doviz] ,[Fiat]  ,[Tutar] ,[Kur]  ,[Cari_Firma],[Adres_Firma]  ,[Iskonto] ,[Tevkifat], "
					+ " ISNULL((Select ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = KERESTE.Ana_Grup ) , '') AS Ana_Grup   , " 
					+ " ISNULL((Select ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = KERESTE.Alt_Grup ) , '') AS Alt_Grup , " 
					+ " ISNULL((Select MENSEI FROM MENSEI_DEGISKEN WHERE MENSEI_DEGISKEN.MEID_Y = KERESTE.Mensei ) , '') AS Mensei, " 
					+ " ISNULL((Select DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = KERESTE.Depo ) , '') AS Depo  ,[Ozel_Kod] ,[Izahat]  ," 
					+ " ISNULL((SELECT UNVAN FROM NAKLIYECI WHERE NAKLIYECI.NAKID_Y = KERESTE.CNakliyeci ),'') Nakliyeci , " 
					+ " [USER] "
					+ "	,[Cikis_Evrak]  ,[CTarih]   ,[CKdv] ,[CDoviz]  ,[CFiat] ,[CTutar] ,[CKur] ,[CCari_Firma] ,[CAdres_Firma] ,[CIskonto]  ,[CTevkifat] "
					+ "	,[CAna_Grup]    ,[CAlt_Grup]  ,ISNULL((Select DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = KERESTE.CDepo ) , '') AS CDepo  ,[COzel_Kod]   ,[CIzahat]  ,[CNakliyeci]  ,[CUSER],Satir" 
					+ " FROM KERESTE   " 
					+ " WHERE Cikis_Evrak  = N'" + eno + "' ORDER BY CSatir " ;
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
				" FROM ACIKLAMA  WITH (INDEX (IX_ACIKLAMA))" +
				" WHERE EVRAK_NO = N'" + evrno + "'" +
				" AND SATIR = '" + satir + "'" +
				" AND EVRAK_CINS = '" + evrcins + "'" +
				" AND Gir_Cik = '" + gircik + "'";
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
				" FROM DPN  " +
				" WHERE Evrak_No = N'" + ino + "'" +
				" AND DPN.Tip = N'" + cins + "'" +
				" AND Gir_Cik = '" + gircik + "'";
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
			dURUMString= " AND Cikis_Evrak = ''";
		String sql = "SELECT   [Evrak_No] ,[Barkod] ,[Kodu],[Paket_No],[Konsimento] ,[Miktar],[Cikis_Evrak]  ," 
				+ " FORMAT(CTarih, 'yyyy-MM-dd HH:mm:ss.fff') AS CTarih   ,"
				+ " [CKdv] ,[CDoviz]  ,[CFiat] ,[CTutar] ,[CKur], " 
				+ " [CCari_Firma] ,[CAdres_Firma] ,[CIskonto]  ,[CTevkifat],[CAna_Grup]    ,[CAlt_Grup] , "
				+ " ISNULL((Select DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = KERESTE.CDepo ) , '') AS CDepo  ," 
				+ " [COzel_Kod]   ,[CIzahat]  ,[CNakliyeci]  ,[CUSER],Satir" 
				+ " FROM KERESTE   " 
				+ " WHERE Paket_No = N'" + token[0] + "' AND Konsimento = N'" + token[1] + "' "
				+ dURUMString + "  ORDER BY Satir" ;
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
		String sql  ="INSERT INTO KERESTE (Evrak_No,Barkod,Kodu,Paket_No,Konsimento,Miktar,Tarih,Kdv,Doviz,Fiat,Tutar,Kur,Cari_Firma,Adres_Firma,Iskonto " + //15
				" ,Tevkifat,Ana_Grup,Alt_Grup,Depo,Ozel_Kod,Izahat,Nakliyeci,[USER],Cikis_Evrak,CTarih,CKdv,CDoviz,CFiat,CTutar,CKur,CCari_Firma,CAdres_Firma " + //17
				" ,CIskonto,CTevkifat,CAna_Grup,CAlt_Grup,CDepo,COzel_Kod,CIzahat,CNakliyeci,CUSER,Mensei,Satir,CSatir) " + //9
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
			throw new ServiceException("Urun kayit Hata:" + e.getMessage());
		}
	}

	@Override
	public void ker_giris_sil(String eno, ConnectionDetails keresteConnDetails) {
		String sql =  " DELETE " +
				" FROM KERESTE " +
				" WHERE Evrak_No  = ? " ;
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
				" FROM DPN" +
				" WHERE Evrak_No = N'" + ino + "'" +
				" AND Tip = N'" + cins + "'" +
				" AND Gir_Cik = '" + gircik + "'";
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
		String sql ="INSERT INTO DPN (Evrak_No,Tip,Bir,Iki,Uc,Gir_Cik,[USER]) " +
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
				" FROM ACIKLAMA " +
				" WHERE EVRAK_CINS = N'" + evrcins + "'" +
				" AND EVRAK_NO = N'" + evrno + "'" +
				" AND Gir_Cik = N'" + cins + "'";
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
		String sql = "INSERT INTO ACIKLAMA (EVRAK_CINS,SATIR,EVRAK_NO,ACIKLAMA,Gir_Cik) " +
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
		String sql =  "SELECT  DISTINCT CONCAT(Paket_No, '-', Konsimento) AS Paket_No FROM KERESTE   " +
				" WHERE Cikis_Evrak = '' " +
				" ORDER by " + sira;
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS ker_barkod_kod_oku", e);
		}
		return resultList; 
	}

	@Override
	public void ker_cikis_sil(String eno, ConnectionDetails keresteConnDetails) {
		String sql =  "UPDATE KERESTE SET Cikis_Evrak = '', CTarih = '1900.01.01',CKdv = 0,CDoviz ='',CFiat=0,CTutar=0,CKur=0,CCari_Firma='',CAdres_Firma='' ," 
				+ " CIskonto=0,CTevkifat=0,CAna_Grup=0,CAlt_Grup=0,CDepo=0,COzel_Kod=0,CIzahat='',CNakliyeci=0,CUSER='' ,CSatir=0"
				+ " WHERE Cikis_Evrak  = ? " ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, eno);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("ker_cikis_sil", e);
		}
	}

	@Override
	public void ker_cikis_kaydet(kerestedetayDTO kerestedetayDTO, ConnectionDetails keresteConnDetails) {
		String[] token = kerestedetayDTO.getPaketno().toString().split("-");
		String sql = "UPDATE KERESTE SET " 
				+ " Cikis_Evrak = '"+ kerestedetayDTO.getCevrak() +"', CTarih = '"+ kerestedetayDTO.getCtarih() + "', " 
				+ " CKdv = "+ kerestedetayDTO.getCkdv() + ",CDoviz ='"+ kerestedetayDTO.getCdoviz() + "', "  
				+ " CFiat="+ kerestedetayDTO.getCfiat() + ",Ctutar="+ kerestedetayDTO.getCtutar() + ", " 
				+ " CKur="+ kerestedetayDTO.getCkur() + ",CCari_Firma = '"+ kerestedetayDTO.getCcarifirma() +"' ," 
				+ " CAdres_Firma='"+ kerestedetayDTO.getCadresfirma() +"' ," 
				+ " CIskonto="+ kerestedetayDTO.getCiskonto() + ",CTevkifat ="+ kerestedetayDTO.getCtevkifat() +" ,"
				+ " CAna_Grup="+ kerestedetayDTO.getCanagrup() + ",CAlt_Grup="+ kerestedetayDTO.getCaltgrup() + ", " 
				+ " CDepo="+ kerestedetayDTO.getCdepo() + ",COzel_Kod="+ kerestedetayDTO.getCozelkod() +" ," 
				+ " CIzahat='"+ kerestedetayDTO.getCizahat() +"',CNakliyeci="+ kerestedetayDTO.getCnakliyeci() + ", " 
				+ " CUSER='"+ kerestedetayDTO.getCuser() +"',"
				+ " CSatir="+ kerestedetayDTO.getCsatir() +""
				+ " WHERE Paket_No  ='" + token[0] + "' AND Konsimento = '"+ token[1] +"' "
				+ " AND Satir = "+ kerestedetayDTO.getSatir() + "" ;
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
		String sql = "SELECT * FROM KOD_ACIKLAMA   ORDER BY KOD ";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS kod_pln", e);
		}
		return resultList; 
	}

	@Override
	public void kod_kayit(String kodu, String aciklama, ConnectionDetails keresteConnDetails) {

		String sql = "INSERT INTO KOD_ACIKLAMA (KOD,ACIKLAMA) " +
				" VALUES (?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, kodu);
			preparedStatement.setString(2, aciklama);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("kod_kayit", e);
		}
	}

	@Override
	public void kod_sil(String kod, ConnectionDetails keresteConnDetails) {
		String sql = "DELETE FROM KOD_ACIKLAMA WHERE KOD = ? " ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, kod);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("kod_sil", e);
		}
	}

	@Override
	public List<Map<String, Object>> kons_pln(ConnectionDetails keresteConnDetails) {
		String sql = "SELECT * FROM KONS_ACIKLAMA   ORDER BY KONS ";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS kons_pln.", e);
		}
		return resultList; 
	}

	@Override
	public void kons_kayit(String kons, String aciklama, int paket_no, ConnectionDetails keresteConnDetails) {
		String sqlKons = "INSERT INTO KONS_ACIKLAMA (KONS, ACIKLAMA) VALUES (?, ?)";
		String sqlPaket = "INSERT INTO PAKET_NO (Pak_No, Konsimento) VALUES (?, ?)";
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword())) {
			connection.setAutoCommit(false);
			try (PreparedStatement psKons = connection.prepareStatement(sqlKons);
					PreparedStatement psPaket = connection.prepareStatement(sqlPaket)) {
				psKons.setString(1, kons);
				psKons.setString(2, aciklama);
				psKons.executeUpdate();

				psPaket.setInt(1, paket_no);
				psPaket.setString(2, kons);
				psPaket.executeUpdate();

				connection.commit();
			} catch (SQLException e) {
				connection.rollback();
				throw new ServiceException("Kayıt işlemi sırasında hata oluştu", e);
			}
		} catch (SQLException e) {
			throw new ServiceException("Veritabanı bağlantı hatası", e);
		}
	}

	@Override
	public int kons_sil(String kons, ConnectionDetails keresteConnDetails) {
		int result = 0;
		String sqlDeleteKons = "DELETE FROM KONS_ACIKLAMA WHERE KONS = ?";
		String sqlSelectPakNo = "SELECT Pak_No FROM PAKET_NO WHERE Konsimento = ?";
		String sqlDeletePaket = "DELETE FROM PAKET_NO WHERE Konsimento = ?";

		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(), 
				keresteConnDetails.getUsername(), 
				keresteConnDetails.getPassword())) {
			try (PreparedStatement psDeleteKons = connection.prepareStatement(sqlDeleteKons)) {
				psDeleteKons.setString(1, kons);
				psDeleteKons.executeUpdate();
			}
			try (PreparedStatement psSelect = connection.prepareStatement(sqlSelectPakNo)) {
				psSelect.setString(1, kons);
				try (ResultSet rs = psSelect.executeQuery()) {
					if (rs.next()) {
						result = rs.getInt("Pak_No");
					}
				}
			}
			try (PreparedStatement psDeletePaket = connection.prepareStatement(sqlDeletePaket)) {
				psDeletePaket.setString(1, kons);
				psDeletePaket.executeUpdate();
			}
		} catch (SQLException e) {
			throw new ServiceException("kons_sil", e);
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> urun_detay(String pakno, String kons, String kodu, String evrak,
			ConnectionDetails keresteConnDetails) {
		String[] token = kodu.toString().split("-");
		StringBuilder kODU = new StringBuilder();
		if (! token[0].equals("00"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 1, 2) = '" + token[0] + "'  AND" );
		if (! token[1].equals("000"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 4, 3) = '" + token[1] + "' AND"  ) ;
		if (! token[2].equals("0000"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 8, 4) = '" + token[2] + "' AND" );
		if (! token[3].equals("0000"))
			kODU.append( " SUBSTRING(KERESTE.Kodu, 13, 4) = '" + token[3] + "'  AND"  );
		String evrakString = "" ;
		if (evrak.toString().equals(""))
			evrakString = " AND Evrak_No like '" + evrak + "%'" ;
		String sql =  " SELECT [Evrak_No] "
				+ " ,[Barkod] "
				+ " ,[Kodu] "
				+ " ,[Paket_No] "
				+ " ,[Konsimento] "
				+ " ,[Miktar] "
				+ " ,(((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)  as m3"
				+ " ,[Tarih] "
				+ " ,[Kdv] "
				+ " ,[Doviz] "
				+ " ,[Fiat] "
				+ " ,[Tutar] "
				+ " ,[Kur] "
				+ " ,[Cari_Firma] "
				+ " ,[Adres_Firma] "
				+ " ,[Iskonto] "
				+ " ,[Tevkifat] "
				+ " ,ISNULL((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = KERESTE.Ana_Grup),'') Ana_Grup "
				+ " ,ISNULL((SELECT ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = KERESTE.Alt_Grup),'') AS Alt_Grup "
				+ " ,ISNULL((SELECT MENSEI FROM MENSEI_DEGISKEN WHERE MENSEI_DEGISKEN.MEID_Y = KERESTE.Mensei),'') AS Mensei "
				+ " ,(SELECT DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = KERESTE.Depo ) as Depo  " 
				+ " ,ISNULL((SELECT OZEL_KOD_1 FROM OZ_KOD_1_DEGISKEN WHERE OZ_KOD_1_DEGISKEN.OZ1ID_Y = KERESTE.Ozel_Kod),'') Ozel_Kod "
				+ " ,[Izahat] "
				+ " ,(SELECT UNVAN FROM NAKLIYECI WHERE NAKLIYECI.NAKID_Y = KERESTE.Nakliyeci ) as Nakliyeci  " 
				+ " ,[USER] "
				+ " ,[Cikis_Evrak] "
				+ " ,ISNULL(CASE WHEN CONVERT(DATE, CTarih) = '1900-01-01' THEN '' ELSE CONVERT(CHAR(10), CTarih, 104) END, '') AS CTarih "
				+ " ,[CKdv] "
				+ " ,[CDoviz] "
				+ " ,[CFiat] "
				+ " ,[CTutar] "
				+ " ,[CKur] "
				+ " ,[CCari_Firma] "
				+ " ,[CAdres_Firma] "
				+ " ,[CIskonto] "
				+ " ,[CTevkifat] "
				+ " ,ISNULL((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = KERESTE.CAna_Grup),'') AS C_Ana_Grup "
				+ "	,ISNULL((SELECT ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = KERESTE.CAlt_Grup),'') AS C_Alt_Grup "
				+ " ,ISNULL((SELECT DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = KERESTE.CDepo),'') AS C_Depo "
				+ " ,ISNULL((SELECT OZEL_KOD_1 FROM OZ_KOD_1_DEGISKEN WHERE OZ_KOD_1_DEGISKEN.OZ1ID_Y = KERESTE.COzel_Kod),'') COzel_Kod "
				+ " ,[CIzahat] "
				+ " ,(SELECT UNVAN FROM NAKLIYECI WHERE NAKLIYECI.NAKID_Y = KERESTE.CNakliyeci ) as C_Nakliyeci  " 
				+ " ,[CUSER] ,Satir" 
				+ " FROM KERESTE    " 
				+ " WHERE " 
				+ kODU 
				+ " Paket_No like N'" + pakno + "%' AND " 
				+ " Konsimento like N'" + kons + "%'" 
				+ " " + evrakString + " "; 
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
	public String kod_adi(String kod, ConnectionDetails keresteConnDetails) {
		String kodadi = "";
		String sql = "SELECT ACIKLAMA FROM KOD_ACIKLAMA  WHERE KOD = ? " ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, kod);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				kodadi = resultSet.getString("ACIKLAMA");
			}
		} catch (SQLException e) {
			throw new ServiceException("kod_oku", e);
		}
		return kodadi;
	}

	@Override
	public String kons_adi(String kons, ConnectionDetails keresteConnDetails) {
		String konsadi = "";
		String sql = "SELECT ACIKLAMA FROM KONS_ACIKLAMA  WHERE KONS = ? " ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, kons);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				konsadi = resultSet.getString("ACIKLAMA");
			}
		} catch (SQLException e) {
			throw new ServiceException("kons_oku", e);
		}
		return konsadi;
	}

	@Override
	public void ker_kod_degis(String paket_No, String kon, String yenikod, int satir,
			ConnectionDetails keresteConnDetails) {
		String sql = "UPDATE KERESTE  " 
				+ " SET  Kodu = CONCAT( ? , SUBSTRING (Kodu, 3,14))" 
				+ " WHERE  Paket_No = ? AND Konsimento = ? AND " 
				+ " Satir = ?" ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, yenikod);
			preparedStatement.setString(2, paket_No);
			preparedStatement.setString(3, kon);
			preparedStatement.setInt(4, satir);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("Ürün değişken güncelleme başarısız", e);
		}
	}

	@Override
	public void ker_kons_degis(String kons, String yenikons, int satir, ConnectionDetails keresteConnDetails) {
		String sql = "UPDATE KERESTE  " 
				+ " SET  Konsimento = ? " 
				+ " WHERE  Konsimento = ? AND  Satir = ? ";

		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(),
				keresteConnDetails.getUsername(),
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, yenikons);
			preparedStatement.setString(2, kons);
			preparedStatement.setInt(3, satir);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("Ürün değişken güncelleme başarısız", e);
		}
	}

	@Override
	public List<Map<String, Object>> stok_rapor(kerestedetayraporDTO kerestedetayraporDTO, Pageable pageable, ConnectionDetails keresteConnDetails) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		int page = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();
		int offset = page * pageSize;

		String[] token = kerestedetayraporDTO.getUkodu1().toString().split("-");
		StringBuilder kODU = new StringBuilder();
		if (! token[0].equals("00"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 1, 2) >= '" + token[0] + "'  AND" );
		if (! token[1].equals("000"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 4, 3) >= '" + token[1] + "' AND"  ) ;
		if (! token[2].equals("0000"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 8, 4) >= '" + token[2] + "' AND" );
		if (! token[3].equals("0000"))
			kODU.append( " SUBSTRING(KERESTE.Kodu, 13, 4) >= '" + token[3] + "'  AND"  );
		token = kerestedetayraporDTO.getUkodu2().toString().split("-");
		if (! token[0].equals("ZZ"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 1, 2) <= '" + token[0] + "'  AND" );
		if (! token[1].equals("999"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 4, 3) <= '" + token[1] + "' AND"  ) ;
		if (! token[2].equals("9999"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 8, 4) <= '" + token[2] + "' AND" );
		if (! token[3].equals("9999"))
			kODU.append( " SUBSTRING(KERESTE.Kodu, 13, 4) <= '" + token[3] + "'  AND"  );

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT [Evrak_No], [Barkod], [Kodu], [Paket_No], [Konsimento], [Miktar], ")
		.append("(((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3)) * ")
		.append("CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * ")
		.append("CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4))) * Miktar) / 1000000000) AS m3, ")
		.append("[Tarih], [Kdv], [Doviz], [Fiat], [Tutar], [Kur], [Cari_Firma], [Adres_Firma], ")
		.append("[Iskonto], [Tevkifat], ")
		.append("ISNULL((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = KERESTE.Ana_Grup), '') AS Ana_Grup, ")
		.append("ISNULL((SELECT ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = KERESTE.Alt_Grup), '') AS Alt_Grup, ")
		.append("ISNULL((SELECT MENSEI FROM MENSEI_DEGISKEN WHERE MENSEI_DEGISKEN.MEID_Y = KERESTE.Mensei), '') AS Mensei, ")
		.append("ISNULL((SELECT DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = KERESTE.Depo), '') AS Depo, ")
		.append("ISNULL((SELECT OZEL_KOD_1 FROM OZ_KOD_1_DEGISKEN WHERE OZ_KOD_1_DEGISKEN.OZ1ID_Y = KERESTE.Ozel_Kod),'') Ozel_Kod, ")
		.append("[Izahat], ")
		.append("ISNULL((SELECT UNVAN FROM NAKLIYECI WHERE NAKLIYECI.NAKID_Y = KERESTE.Nakliyeci), '') AS Nakliyeci, ")
		.append("[USER], [Cikis_Evrak], ")
		.append("ISNULL(CASE WHEN CONVERT(DATE, CTarih) = '1900-01-01' THEN '' ELSE CONVERT(CHAR(10), CTarih, 104) END, '') AS CTarih, ")
		.append("[CKdv],[CDoviz],[CFiat],[CTutar],[CKur],[CCari_Firma],[CAdres_Firma],[CIskonto],[CTevkifat],")
		.append("ISNULL((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = KERESTE.CAna_Grup),'') AS C_Ana_Grup, ")
		.append("ISNULL((SELECT ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = KERESTE.CAlt_Grup),'') AS C_Alt_Grup, ")
		.append("ISNULL((SELECT DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = KERESTE.CDepo),'') AS C_Depo, ")
		.append("ISNULL((SELECT OZEL_KOD_1 FROM OZ_KOD_1_DEGISKEN WHERE OZ_KOD_1_DEGISKEN.OZ1ID_Y = KERESTE.COzel_Kod),'') COzel_Kod, ")
		.append("[CIzahat] ," )
		.append("ISNULL((SELECT UNVAN FROM NAKLIYECI WHERE NAKLIYECI.NAKID_Y = KERESTE.CNakliyeci),'' ) as C_Nakliyeci, ") 
		.append("[CUSER] ") 
		.append("FROM KERESTE WHERE 1=1 ")

		.append(" AND Tarih BETWEEN '").append(kerestedetayraporDTO.getGtar1()).append("' AND '").append(kerestedetayraporDTO.getGtar2()).append(" 23:59:59.998' AND ")
		.append(kODU.toString())
		.append(" Paket_No BETWEEN N'").append(kerestedetayraporDTO.getPak1()).append("' AND N'").append(kerestedetayraporDTO.getPak2()).append("' ")
		.append(" AND Cari_Firma BETWEEN N'").append(kerestedetayraporDTO.getGfirma1()).append("' AND N'").append(kerestedetayraporDTO.getGfirma2()).append("' ")
		.append(" AND Evrak_No BETWEEN N'").append(kerestedetayraporDTO.getEvr1()).append("' AND N'").append(kerestedetayraporDTO.getEvr2()).append("' ")
		.append(" AND Konsimento BETWEEN N'").append(kerestedetayraporDTO.getKons1()).append("' AND N'").append(kerestedetayraporDTO.getKons2()).append("' ")
		.append(" AND Cikis_Evrak BETWEEN N'").append(kerestedetayraporDTO.getCevr1()).append("' AND N'").append(kerestedetayraporDTO.getCevr2()).append("' ")

		.append(" AND Ana_Grup " + kerestedetayraporDTO.getGana()  + " AND" )
		.append(" Alt_Grup " + kerestedetayraporDTO.getGalt()  + " AND" ) 
		.append(" Depo " + kerestedetayraporDTO.getGdepo()  + " AND" )
		.append(" Ozel_Kod " + kerestedetayraporDTO.getGozkod() + " AND" )

		.append(" CAna_Grup " + kerestedetayraporDTO.getCana()  + " AND" )
		.append(" CAlt_Grup " + kerestedetayraporDTO.getCalt()  + " AND" )
		.append(" CDepo " + kerestedetayraporDTO.getCdepo()  + " AND " )
		.append(" COzel_Kod " + kerestedetayraporDTO.getCozkod() ) 
		.append(" ORDER BY Tarih DESC OFFSET ").append(offset).append(" ROWS FETCH NEXT ").append(pageSize).append(" ROWS ONLY");

		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(), 
				keresteConnDetails.getUsername(), 
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 

		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList;
	}

	@Override
	public double stok_raporsize(kerestedetayraporDTO kerestedetayraporDTO, ConnectionDetails keresteConnDetails) {
		double result = 0 ;

		String[] token = kerestedetayraporDTO.getUkodu1().toString().split("-");
		StringBuilder kODU = new StringBuilder();
		if (! token[0].equals("00"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 1, 2) >= '" + token[0] + "'  AND" );
		if (! token[1].equals("000"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 4, 3) >= '" + token[1] + "' AND"  ) ;
		if (! token[2].equals("0000"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 8, 4) >= '" + token[2] + "' AND" );
		if (! token[3].equals("0000"))
			kODU.append( " SUBSTRING(KERESTE.Kodu, 13, 4) >= '" + token[3] + "'  AND"  );
		token = kerestedetayraporDTO.getUkodu2().toString().split("-");
		if (! token[0].equals("ZZ"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 1, 2) <= '" + token[0] + "'  AND" );
		if (! token[1].equals("999"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 4, 3) <= '" + token[1] + "' AND"  ) ;
		if (! token[2].equals("9999"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 8, 4) <= '" + token[2] + "' AND" );
		if (! token[3].equals("9999"))
			kODU.append( " SUBSTRING(KERESTE.Kodu, 13, 4) <= '" + token[3] + "'  AND"  );


		StringBuilder sql = new StringBuilder();
		sql.append("SELECT count(Evrak_No) as satir ") 
		.append("FROM KERESTE ")
		.append(" WHERE Tarih BETWEEN '").append(kerestedetayraporDTO.getGtar1()).append("' AND '").append(kerestedetayraporDTO.getGtar2()).append(" 23:59:59.998' AND ")
		.append(kODU.toString())
		.append(" Paket_No BETWEEN N'").append(kerestedetayraporDTO.getPak1()).append("' AND N'").append(kerestedetayraporDTO.getPak2()).append("' ")
		.append(" AND Cari_Firma BETWEEN N'").append(kerestedetayraporDTO.getGfirma1()).append("' AND N'").append(kerestedetayraporDTO.getGfirma2()).append("' ")
		.append(" AND Evrak_No BETWEEN N'").append(kerestedetayraporDTO.getEvr1()).append("' AND N'").append(kerestedetayraporDTO.getEvr2()).append("' ")
		.append(" AND Konsimento BETWEEN N'").append(kerestedetayraporDTO.getKons1()).append("' AND N'").append(kerestedetayraporDTO.getKons2()).append("' ")
		.append(" AND Cikis_Evrak BETWEEN N'").append(kerestedetayraporDTO.getCevr1()).append("' AND N'").append(kerestedetayraporDTO.getCevr2()).append("' ")

		.append(" AND Ana_Grup " + kerestedetayraporDTO.getGana()  + " AND" )
		.append(" Alt_Grup " + kerestedetayraporDTO.getGalt()  + " AND" ) 
		.append(" Depo " + kerestedetayraporDTO.getGdepo()  + " AND" )
		.append(" Ozel_Kod " + kerestedetayraporDTO.getGozkod() + " AND" )

		.append(" CAna_Grup " + kerestedetayraporDTO.getCana()  + " AND" )
		.append(" CAlt_Grup " + kerestedetayraporDTO.getCalt()  + " AND" )
		.append(" CDepo " + kerestedetayraporDTO.getCdepo()  + " AND " )
		.append(" COzel_Kod " + kerestedetayraporDTO.getCozkod() ); 

		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(), 
				keresteConnDetails.getUsername(), 
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				result  = resultSet.getInt("satir");
			} 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> baslik_bak(String baslik, String ordr, String jkj, String k1, String k2, String f1,
			String f2, String t1, String t2, String dURUM, String e1, String e2, ConnectionDetails keresteConnDetails) {
		String[] token = k1.toString().split("-");
		String ilks ,ilkk,ilkb,ilkg;
		ilks = token[0];
		ilkk = token[1];
		ilkb = token[2];
		ilkg = token[3];
		token = k2.toString().split("-");
		String sons,sonk,sonb,song;
		sons = token[0];
		sonk = token[1];
		sonb = token[2];
		song = token[3];
		String qweString = "" ;
		if(dURUM.equals("C"))
			qweString = " Cikis_Evrak " ;
		else
			qweString = " Evrak_No " ;
		String tARIH = "" ;
		if(! t1.equals("1900-01-01") || ! t2.equals("2100-12-31"))
			tARIH = " AND " + dURUM + "Tarih BETWEEN '" + t1 + "'  AND  '"  + t2 + " 23:59:59.998'" ;
		String sql =   "SELECT "+ baslik + "  FROM KERESTE   " +
				" WHERE   " + jkj +
				" SUBSTRING(KERESTE.Kodu, 1, 2) >= '"+ilks +"' AND SUBSTRING(KERESTE.Kodu, 1, 2) <= '"+ sons +"' AND" +
				" SUBSTRING(KERESTE.Kodu, 4, 3) >= '"+ilkk +"' AND SUBSTRING(KERESTE.Kodu, 4, 3) <= '"+ sonk +"' AND" +
				" SUBSTRING(KERESTE.Kodu, 8, 4) >= '"+ilkb +"' AND SUBSTRING(KERESTE.Kodu, 8, 4) <= '"+ sonb +"' AND" +
				" SUBSTRING(KERESTE.Kodu, 13, 4) >= '"+ilkg +"' AND SUBSTRING(KERESTE.Kodu, 13, 4) <= '"+ song +"' " +
				" AND " + dURUM + "Cari_Firma between N'" + f1 + "' AND N'" + f2 + "'" +
				" AND " + qweString  + " between N'" + e1 + "' AND N'" + e2 + "'" +
				tARIH +
				" " + ordr + " ";
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
	public List<Map<String, Object>> grp_rapor(String gruplama, String sstr_2, String sstr_4, String kur_dos,
			String qwq6, String qwq7, String qwq8, String k1, String k2, String s1, String s2, String jkj, String t1,
			String t2, String sstr_5, String sstr_1, String orderBY, String dURUM, String ko1, String ko2, String dpo,
			String grup, String e1, String e2, String[][] ozelgrp,Set<String> sabitkolonlar, ConnectionDetails keresteConnDetails) {

		String[] token = k1.toString().split("-");
		StringBuilder kODU = new StringBuilder();
		if (! token[0].equals("00"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 1, 2) >= '" + token[0] + "'  AND" );
		if (! token[1].equals("000"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 4, 3) >= '" + token[1] + "' AND"  ) ;
		if (! token[2].equals("0000"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 8, 4) >= '" + token[2] + "' AND" );
		if (! token[3].equals("0000"))
			kODU.append( " SUBSTRING(KERESTE.Kodu, 13, 4) >= '" + token[3] + "' AND"  );
		token = k2.toString().split("-");
		if (! token[0].equals("ZZ"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 1, 2) <= '" + token[0] + "' AND" );
		if (! token[1].equals("999"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 4, 3) <= '" + token[1] + "' AND"  ) ;
		if (! token[2].equals("9999"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 8, 4) <= '" + token[2] + "' AND" );
		if (! token[3].equals("9999"))
			kODU.append( " SUBSTRING(KERESTE.Kodu, 13, 4) <= '" + token[3] + "'  AND"  );
		if(qwq6.equals(" Like  '%' "))
			qwq6 =  " " ;
		else
			qwq6 = dURUM + "Ana_Grup " + qwq6 ;
		if(qwq7.equals(" Like  '%' "))
			qwq7 =  " " ;
		else
			qwq7 = " AND "+ dURUM + "Alt_Grup " + qwq7 ;
		if(qwq8.equals(" Like  '%' "))
			qwq8 =  " " ;
		else
			qwq8 = " AND "+ dURUM + "Ozel_Kod " + qwq8 ;
		if(dpo.equals(" Like  '%' "))
			dpo =  " " ;
		else
			dpo = " AND "+ dURUM + "Depo " + dpo + " AND ";
		String qweString = "" ;
		if(dURUM.equals("C"))
			qweString = " Cikis_Evrak " ;
		else
			qweString = " Evrak_No " ;
		String sql =   "SELECT * " +
				" FROM (SELECT " + gruplama + " ," + sstr_2 + " as degisken," + sstr_4 +
				" FROM KERESTE " + kur_dos + 
				" WHERE " + jkj + " " +
				qwq6 + " " + qwq7 + " " + qwq8 + " " + dpo +
				kODU + " " +
				dURUM + "Cari_Firma between N'" + s1 + "' AND N'" + s2 + "'" +
				" AND " + qweString  + " between N'" + e1 + "' AND N'" + e2 + "'" +
				" AND Konsimento between N'" + ko1 + "' AND N'" + ko2 + "'" +
				" AND KERESTE."+ dURUM + "Tarih BETWEEN '" +t1 + "'" + " AND  '" + t2 + " 23:59:59.998'" +
				" ) AS s " +
				" PIVOT" +
				" (" +
				" SUM(" + sstr_5 + ")" +
				" FOR degisken" +
				" IN (" + sstr_1 + ")" +
				" )" +
				" AS p" +
				" ORDER BY " + orderBY ;
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToListPIVOT(resultSet,sabitkolonlar); 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> fat_rapor(kerestedetayraporDTO kerestedetayraporDTO,
			ConnectionDetails keresteConnDetails,ConnectionDetails cariConnDetails) {
		String[] token = kerestedetayraporDTO.getUkodu1().toString().split("-");
		String ilks ,ilkk,ilkb,ilkg;
		ilks = token[0];
		ilkk = token[1];
		ilkb = token[2];
		ilkg = token[3];
		token = kerestedetayraporDTO.getUkodu2().toString().split("-");
		String sons,sonk,sonb,song;
		sons = token[0];
		sonk = token[1];
		sonb = token[2];
		song = token[3];
		String hANGI = "" ;
		String eVRAKNO = "" ;
		String aLsAT = "" ;
		String dURUM = "" ;
		if (kerestedetayraporDTO.getGircik().equals("G"))
		{
			hANGI = "" ;
			eVRAKNO = "Evrak_No" ;
			aLsAT = "Alis" ;
			dURUM =   " Cikis_Evrak between N'" + kerestedetayraporDTO.getCevr1() + "' AND N'" + kerestedetayraporDTO.getCevr2() + "' AND" ;
		}
		else 
		{
			hANGI = "C" ;
			eVRAKNO = "Cikis_Evrak" ;
			aLsAT = "Satis";
			dURUM =   " Cikis_Evrak <> '' AND" ;
		}
		String sql =   " SELECT " + eVRAKNO + " as Fatura_No   ,'" + aLsAT + "' as Hareket,FORMAT(" + hANGI + "Tarih, 'yyyy-MM-dd') as Tarih ,(SELECT   UNVAN FROM  [" + modulbaslikTipi.OK_Car.name() + cariConnDetails.getDatabaseName() + "].[dbo].[HESAP] WHERE HESAP.HESAP = KERESTE." + hANGI + "Cari_Firma  ) as Unvan  ," + hANGI + "Adres_Firma as Adres_Firma," + hANGI + "Doviz as Doviz, " 
				+" SUM( (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000))  as m3 ," 
				+" SUM(((((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Fiat ) as Tutar, " 
				+" SUM(((((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Fiat  ) - SUM(((((((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Fiat  ) * " + hANGI + "Iskonto)/100) as Iskontolu_Tutar ," 
				+" SUM((((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) - (("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Iskonto)/100) * "+ hANGI+"Kdv)/100)  AS Kdv_Tutar ," 
				+" SUM((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) - (("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Iskonto)/100 +   ((("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) - (("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Iskonto) / 100) * "+ hANGI+"Kdv ) / 100)    as Toplam_Tutar " 				
				+" FROM KERESTE   " 
				+" WHERE " 
				+" Tarih BETWEEN '" + kerestedetayraporDTO.getGtar1() + "'" + " AND  '" + kerestedetayraporDTO.getGtar2() + " 23:59:59.998' AND" 
				+" SUBSTRING(KERESTE.Kodu, 1, 2) >= '"+ilks +"' AND SUBSTRING(KERESTE.Kodu, 1, 2) <= '"+ sons +"' AND" 
				+" SUBSTRING(KERESTE.Kodu, 4, 3) >= '"+ilkk +"' AND SUBSTRING(KERESTE.Kodu, 4, 3) <= '"+ sonk +"' AND" 
				+" SUBSTRING(KERESTE.Kodu, 8, 4) >= '"+ilkb +"' AND SUBSTRING(KERESTE.Kodu, 8, 4) <= '"+ sonb +"' AND" 
				+" SUBSTRING(KERESTE.Kodu, 13, 4) >= '"+ilkg +"' AND SUBSTRING(KERESTE.Kodu, 13, 4) <= '"+ song +"' AND " 
				+" Paket_No between N'" + kerestedetayraporDTO.getPak1() + "' AND N'" + kerestedetayraporDTO.getPak2() + "' AND " 
				+" Cari_Firma between N'" + kerestedetayraporDTO.getGfirma1() + "' AND N'" + kerestedetayraporDTO.getGfirma2() + "' AND" 
				+" Evrak_No between N'" + kerestedetayraporDTO.getEvr1() + "' AND N'" + kerestedetayraporDTO.getEvr2() + "' AND" 
				+" Konsimento between N'" + kerestedetayraporDTO.getKons1() + "' AND N'" + kerestedetayraporDTO.getKons2() + "' AND" 
				+" Ana_Grup " + kerestedetayraporDTO.getGana()  + " AND" 
				+" Alt_Grup " + kerestedetayraporDTO.getGalt()  + " AND" 
				+" Depo " + kerestedetayraporDTO.getGdepo()  + " AND" 
				+" Ozel_Kod " + kerestedetayraporDTO.getGozkod() + " AND" 
				+" CTarih BETWEEN '" + kerestedetayraporDTO.getCtar1() + "'" + " AND  '" + kerestedetayraporDTO.getCtar2() + " 23:59:59.998' AND" 
				+" CCari_Firma between N'" + kerestedetayraporDTO.getCfirma1() + "' AND N'" + kerestedetayraporDTO.getCfirma2() + "' AND" 
				+" " + dURUM
				+" CAna_Grup " + kerestedetayraporDTO.getCana()  + " AND" 
				+" CAlt_Grup " + kerestedetayraporDTO.getCalt()  + " AND" 
				+" CDepo " + kerestedetayraporDTO.getCdepo()  + " AND " 
				+" COzel_Kod " + kerestedetayraporDTO.getCozkod() 
				+" GROUP BY " + eVRAKNO + ", " + hANGI + "Tarih," + hANGI + "Cari_Firma," + hANGI + "Adres_Firma," + hANGI + "Doviz  " 
				+" ORDER BY  " + eVRAKNO 
				+" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
		int page = kerestedetayraporDTO.getPage();
		int pageSize = kerestedetayraporDTO.getPageSize();
		int offset = page * pageSize;
		
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(keresteConnDetails.getJdbcUrl(), keresteConnDetails.getUsername(), keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, offset);
			preparedStatement.setInt(2, pageSize);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public double fat_raporsize(kerestedetayraporDTO kerestedetayraporDTO ,ConnectionDetails keresteConnDetails) {
		double result = 0 ;
		String[] token = kerestedetayraporDTO.getUkodu1().toString().split("-");
		String ilks ,ilkk,ilkb,ilkg;
		ilks = token[0];
		ilkk = token[1];
		ilkb = token[2];
		ilkg = token[3];
		token = kerestedetayraporDTO.getUkodu2().toString().split("-");
		String sons,sonk,sonb,song;
		sons = token[0];
		sonk = token[1];
		sonb = token[2];
		song = token[3];
		String hANGI = "" ;
		String eVRAKNO = "" ;
		String dURUM = "" ;
		if (kerestedetayraporDTO.getGircik().equals("G"))
		{
			hANGI = "" ;
			eVRAKNO = "Evrak_No" ;
			dURUM =   " Cikis_Evrak between N'" + kerestedetayraporDTO.getCevr1() + "' AND N'" + kerestedetayraporDTO.getCevr2() + "' AND" ;
		}
		else 
		{
			hANGI = "C" ;
			eVRAKNO = "Cikis_Evrak" ;
			dURUM =   " Cikis_Evrak <> '' AND" ;
		}
		String sql = "SELECT COUNT(*) AS satir" +
				 " FROM (" + 
				" SELECT Fatura_No " +
				" FROM FATURA WITH (INDEX (IX_FATURA)) " +
				" Tarih BETWEEN '" + kerestedetayraporDTO.getGtar1() + "'" + " AND  '" + kerestedetayraporDTO.getGtar2() + " 23:59:59.998' AND" +
				" SUBSTRING(KERESTE.Kodu, 1, 2) >= '"+ilks +"' AND SUBSTRING(KERESTE.Kodu, 1, 2) <= '"+ sons +"' AND" +
				" SUBSTRING(KERESTE.Kodu, 4, 3) >= '"+ilkk +"' AND SUBSTRING(KERESTE.Kodu, 4, 3) <= '"+ sonk +"' AND" +
				" SUBSTRING(KERESTE.Kodu, 8, 4) >= '"+ilkb +"' AND SUBSTRING(KERESTE.Kodu, 8, 4) <= '"+ sonb +"' AND" +
				" SUBSTRING(KERESTE.Kodu, 13, 4) >= '"+ilkg +"' AND SUBSTRING(KERESTE.Kodu, 13, 4) <= '"+ song +"' AND " + 
				" Paket_No between N'" + kerestedetayraporDTO.getPak1() + "' AND N'" + kerestedetayraporDTO.getPak2() + "' AND " + 
				" Cari_Firma between N'" + kerestedetayraporDTO.getGfirma1() + "' AND N'" + kerestedetayraporDTO.getGfirma2() + "' AND" + 
				" Evrak_No between N'" + kerestedetayraporDTO.getEvr1() + "' AND N'" + kerestedetayraporDTO.getEvr2() + "' AND" +
				" Konsimento between N'" + kerestedetayraporDTO.getKons1() + "' AND N'" + kerestedetayraporDTO.getKons2() + "' AND" + 
				" Ana_Grup " + kerestedetayraporDTO.getGana()  + " AND"  +
				" Alt_Grup " + kerestedetayraporDTO.getGalt()  + " AND" +
				" Depo " + kerestedetayraporDTO.getGdepo()  + " AND" +
				" Ozel_Kod " + kerestedetayraporDTO.getGozkod() + " AND" + 
				" CTarih BETWEEN '" + kerestedetayraporDTO.getCtar1() + "'" + " AND  '" + kerestedetayraporDTO.getCtar2() + " 23:59:59.998' AND" + 
				" CCari_Firma between N'" + kerestedetayraporDTO.getCfirma1() + "' AND N'" + kerestedetayraporDTO.getCfirma2() + "' AND" +
				" " + dURUM +
				" CAna_Grup " + kerestedetayraporDTO.getCana()  + " AND" + 
				" CAlt_Grup " + kerestedetayraporDTO.getCalt()  + " AND" +
				" CDepo " + kerestedetayraporDTO.getCdepo()  + " AND " +
				" COzel_Kod " + kerestedetayraporDTO.getCozkod() +
				" GROUP BY " + eVRAKNO + ", " + hANGI + "Tarih," + hANGI + "Cari_Firma," + hANGI + "Adres_Firma," + hANGI + "Doviz  " + 
				" ORDER BY  " + eVRAKNO ;
		try (Connection connection = DriverManager.getConnection(
				keresteConnDetails.getJdbcUrl(), 
				keresteConnDetails.getUsername(), 
				keresteConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				result  = resultSet.getInt("satir");
			} 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return result;
	}
	
	@Override
	public List<Map<String, Object>> fat_detay_rapor(String fno, String turu, ConnectionDetails keresteConnDetails) {
		String fAT_TUR = "" ;
		String hANGI = "" ;
		String eVRAKNO = "" ;
		if(turu.equals("G"))
		{
			fAT_TUR = " Evrak_No = '" + fno + "' " ;
			hANGI = "" ;
			eVRAKNO = "Evrak_No" ;
		}
		else {
			fAT_TUR = " Cikis_Evrak = '" + fno + "' " ;
			hANGI = "C" ;
			eVRAKNO = "Cikis_Evrak" ;
		}
		String sql =  " SELECT [" + eVRAKNO + "] as Fatura_No "
				+ " ,[Barkod] "
				+ " ,[Kodu] "
				+ " ,[Paket_No] "
				+ " ,[Miktar] "
				+ " ,(((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)  as m3"
				+ " ,[" + hANGI + "Kdv] as Kdv "
				+ " ,[" + hANGI + "Doviz] as Doviz "
				+ " ,[" + hANGI + "Fiat] as Fiat "
				+ " ,[" + hANGI + "Tutar] as Tutar "
				+ " ,[" + hANGI + "Kur] as Kur "
				+ " ,[" + hANGI + "Cari_Firma] as Cari_Firma "
				+ " ,[" + hANGI + "Adres_Firma] as Adres_Firma "
				+ " ,[" + hANGI + "Iskonto] as Iskonto "
				+ " ,[" + hANGI + "Tevkifat] as Tevkifat "
				+ " ,ISNULL((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = KERESTE." + hANGI + "Ana_Grup),'') AS Ana_Grup "
				+ "	,ISNULL((SELECT ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = KERESTE." + hANGI + "Alt_Grup),'') AS Alt_Grup "
				+ " ,ISNULL((SELECT MENSEI FROM MENSEI_DEGISKEN WHERE MENSEI_DEGISKEN.MEID_Y = KERESTE.Mensei),'') AS Mensei "
				+ " ,ISNULL((SELECT DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = KERESTE." + hANGI + "Depo),'') AS Depo "
				+ " ,ISNULL((SELECT OZEL_KOD_1 FROM OZ_KOD_1_DEGISKEN WHERE OZ_KOD_1_DEGISKEN.OZ1ID_Y = KERESTE."+ hANGI +"Ozel_Kod),'') Ozel_Kod "
				+ " ,[" + hANGI + "Izahat]  as Izahat "
				+ " ,ISNULL((SELECT UNVAN FROM NAKLIYECI WHERE NAKLIYECI.NAKID_Y = KERESTE." + hANGI + "Nakliyeci),'' ) as Nakliyeci  " 
				+ " ,[USER] " 
				+ " FROM KERESTE    " 
				+ " WHERE "  + fAT_TUR  + " ORDER BY " + hANGI + "Satir" ; 
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
	public List<Map<String, Object>> fat_rapor_fat_tar(kerestedetayraporDTO kerestedetayraporDTO, ConnectionDetails keresteConnDetails) {
		
		String[] token = kerestedetayraporDTO.getUkodu1().toString().split("-");
		String ilks ,ilkk,ilkb,ilkg;
		ilks = token[0];
		ilkk = token[1];
		ilkb = token[2];
		ilkg = token[3];
		token = kerestedetayraporDTO.getUkodu2().toString().split("-");
		String sons,sonk,sonb,song;
		sons = token[0];
		sonk = token[1];
		sonb = token[2];
		song = token[3];
		String hANGI = "" ;
		String eVRAKNO = "" ;
		String aLsAT = "" ;
		String dURUM = "" ;
		if (kerestedetayraporDTO.getGircik().equals("G"))
		{
			hANGI = "" ;
			eVRAKNO = "Evrak_No" ;
			aLsAT = "Alis" ;
		    dURUM =   " Cikis_Evrak between N'" + kerestedetayraporDTO.getCevr1() + "' AND N'" + kerestedetayraporDTO.getCevr2() + "' AND" ;
		}
		else {
			hANGI = "C" ;
			eVRAKNO = "Cikis_Evrak" ;
			aLsAT = "Satis";
			dURUM =   " Cikis_Evrak <> '' AND" ;
		}
		String sql =  " SELECT " + eVRAKNO + " as Fatura_No,'" + aLsAT + "' as Hareket,FORMAT(" + hANGI + "Tarih, 'yyyy-MM-dd')  as Tarih" 
				+" " + kerestedetayraporDTO.getBir() + "" 
				+" " + kerestedetayraporDTO.getIki() + " , " 
				+" SUM( (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000))  as m3 " 
				+" ,SUM(" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) as Tutar " 
				+" ,SUM((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) - (("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Iskonto)/100) as Iskontolu_Tutar  " 
				+" ,SUM((((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) - (("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Iskonto)/100) * "+ hANGI+"Kdv)/100)  AS Kdv_Tutar " 
				+" ,SUM((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) - (("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Iskonto)/100 +   ((("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) - (("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Iskonto) / 100) * "+ hANGI+"Kdv ) / 100)    as Toplam_Tutar " 
				+" FROM KERESTE   " 
				+" WHERE " 
				+" Tarih BETWEEN '" + kerestedetayraporDTO.getGtar1() + "'" + " AND  '" + kerestedetayraporDTO.getGtar2() + " 23:59:59.998' AND" 
				+" SUBSTRING(KERESTE.Kodu, 1, 2) >= '"+ilks +"' AND SUBSTRING(KERESTE.Kodu, 1, 2) <= '"+ sons +"' AND" 
				+" SUBSTRING(KERESTE.Kodu, 4, 3) >= '"+ilkk +"' AND SUBSTRING(KERESTE.Kodu, 4, 3) <= '"+ sonk +"' AND" 
				+" SUBSTRING(KERESTE.Kodu, 8, 4) >= '"+ilkb +"' AND SUBSTRING(KERESTE.Kodu, 8, 4) <= '"+ sonb +"' AND" 
				+" SUBSTRING(KERESTE.Kodu, 13, 4) >= '"+ilkg +"' AND SUBSTRING(KERESTE.Kodu, 13, 4) <= '"+ song +"' AND " 
				+" Paket_No between N'" + kerestedetayraporDTO.getPak1() + "' AND N'" + kerestedetayraporDTO.getPak2() + "' AND " 
				+" Cari_Firma between N'" + kerestedetayraporDTO.getGfirma1() + "' AND N'" + kerestedetayraporDTO.getGfirma2() + "' AND" 
				+" Evrak_No between N'" + kerestedetayraporDTO.getEvr1() + "' AND N'" + kerestedetayraporDTO.getEvr2() + "' AND" 
				+" Konsimento between N'" + kerestedetayraporDTO.getKons1() + "' AND N'" + kerestedetayraporDTO.getKons2() + "' AND" 
				+" Ana_Grup " + kerestedetayraporDTO.getGana()  + " AND" 
				+" Alt_Grup " + kerestedetayraporDTO.getGalt()  + " AND" 
				+" Depo " + kerestedetayraporDTO.getGdepo()  + " AND" 
				+" Ozel_Kod " + kerestedetayraporDTO.getGozkod() + " AND" 
				+" CTarih BETWEEN '" + kerestedetayraporDTO.getCtar1() + "'" + " AND  '" + kerestedetayraporDTO.getCtar2() + " 23:59:59.998' AND" 
				+" CCari_Firma between N'" + kerestedetayraporDTO.getCfirma1() + "' AND N'" + kerestedetayraporDTO.getCfirma2() + "' AND" 
				+" " + dURUM
				+" CAna_Grup " + kerestedetayraporDTO.getCana()  + " AND" 
				+" CAlt_Grup " + kerestedetayraporDTO.getCalt()  + " AND" 
				+" CDepo " + kerestedetayraporDTO.getCdepo()  + " AND " 
				+" COzel_Kod " + kerestedetayraporDTO.getCozkod() 
				+" GROUP BY " + kerestedetayraporDTO.getUc() + "" 
				+" ORDER BY  " + kerestedetayraporDTO.getUc() + "";
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
	public List<Map<String, Object>> fat_rapor_cari_kod(kerestedetayraporDTO kerestedetayraporDTO,
			ConnectionDetails keresteConnDetails) {
		String[] token = kerestedetayraporDTO.getUkodu1().toString().split("-");
		String ilks ,ilkk,ilkb,ilkg;
		ilks = token[0];
		ilkk = token[1];
		ilkb = token[2];
		ilkg = token[3];
		token = kerestedetayraporDTO.getUkodu2().toString().split("-");
		String sons,sonk,sonb,song;
		sons = token[0];
		sonk = token[1];
		sonb = token[2];
		song = token[3];
		String hANGI = "" ;
		String aLsAT = "" ;
		String dURUM = "" ;
		if (kerestedetayraporDTO.getGircik().equals("G"))
		{
			hANGI = "" ;
			aLsAT = "Alis" ;
		    dURUM =   " Cikis_Evrak between N'" + kerestedetayraporDTO.getCevr1() + "' AND N'" + kerestedetayraporDTO.getCevr2() + "' AND" ;
		}
		else {
			hANGI = "C" ;
			aLsAT = "Satis";
			dURUM =   " Cikis_Evrak <> '' AND" ;
		}
		String sql =  " SELECT  " + kerestedetayraporDTO.getIki() + " as Firma_Kodu,'"+ aLsAT +"' as Hareket " 
				+"  " + kerestedetayraporDTO.getBir() + " ," 
				+" SUM( (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000))  as m3 ," 
				+" SUM(" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) as Tutar ," 
				+" SUM((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) - (("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Iskonto)/100) as Iskontolu_Tutar  ," 
				+" SUM((((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) - (("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Iskonto)/100) * "+ hANGI+"Kdv)/100)  AS Kdv_Tutar ," 
				+" SUM((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) - (("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Iskonto)/100 +   ((("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) - (("+ hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * "+ hANGI+"Iskonto) / 100) * "+ hANGI+"Kdv ) / 100)    as Toplam_Tutar " 				
				+" FROM KERESTE   " 
				+" WHERE " 
				+" Tarih BETWEEN '" + kerestedetayraporDTO.getGtar1() + "'" + " AND  '" + kerestedetayraporDTO.getGtar2() + " 23:59:59.998' AND" 
				+" SUBSTRING(KERESTE.Kodu, 1, 2) >= '"+ilks +"' AND SUBSTRING(KERESTE.Kodu, 1, 2) <= '"+ sons +"' AND" 
				+" SUBSTRING(KERESTE.Kodu, 4, 3) >= '"+ilkk +"' AND SUBSTRING(KERESTE.Kodu, 4, 3) <= '"+ sonk +"' AND" 
				+" SUBSTRING(KERESTE.Kodu, 8, 4) >= '"+ilkb +"' AND SUBSTRING(KERESTE.Kodu, 8, 4) <= '"+ sonb +"' AND" 
				+" SUBSTRING(KERESTE.Kodu, 13, 4) >= '"+ilkg +"' AND SUBSTRING(KERESTE.Kodu, 13, 4) <= '"+ song +"' AND " 
				+" Paket_No between N'" + kerestedetayraporDTO.getPak1() + "' AND N'" + kerestedetayraporDTO.getPak2() + "' AND " 
				+" Cari_Firma between N'" + kerestedetayraporDTO.getGfirma1() + "' AND N'" + kerestedetayraporDTO.getGfirma2() + "' AND" 
				+" Evrak_No between N'" + kerestedetayraporDTO.getEvr1() + "' AND N'" + kerestedetayraporDTO.getEvr2() + "' AND" 
				+" Konsimento between N'" + kerestedetayraporDTO.getKons1() + "' AND N'" + kerestedetayraporDTO.getKons2() + "' AND" 
				+" Ana_Grup " + kerestedetayraporDTO.getGana()  + " AND" 
				+" Alt_Grup " + kerestedetayraporDTO.getGalt()  + " AND" 
				+" Depo " + kerestedetayraporDTO.getGdepo()  + " AND" 
				+" Ozel_Kod " + kerestedetayraporDTO.getGozkod() + " AND" 
				+" CTarih BETWEEN '" + kerestedetayraporDTO.getCtar1() + "'" + " AND  '" + kerestedetayraporDTO.getCtar2() + " 23:59:59.998' AND" 
				+" CCari_Firma between N'" + kerestedetayraporDTO.getCfirma1() + "' AND N'" + kerestedetayraporDTO.getCfirma2() + "' AND" 
				+" " + dURUM
				+" CAna_Grup " + kerestedetayraporDTO.getCana()  + " AND" 
				+" CAlt_Grup " + kerestedetayraporDTO.getCalt()  + " AND" 
				+" CDepo " + kerestedetayraporDTO.getCdepo()  + " AND " 
				+" COzel_Kod " + kerestedetayraporDTO.getCozkod() 
				+" GROUP BY " + kerestedetayraporDTO.getIki() + "" 
				+" ORDER BY  " + kerestedetayraporDTO.getIki() + "";
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
	public List<Map<String, Object>> envanter(kerestedetayraporDTO kerestedetayraporDTO, String[] gruplama,ConnectionDetails keresteConnDetails)
			{
		String[] token = kerestedetayraporDTO.getUkodu1().toString().split("-");
		StringBuilder kODU = new StringBuilder();
		if (! token[0].equals("00"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 1, 2) >= '" + token[0] + "'  AND" );
		if (! token[1].equals("000"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 4, 3) >= '" + token[1] + "' AND"  ) ;
		if (! token[2].equals("0000"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 8, 4) >= '" + token[2] + "' AND" );
		if (! token[3].equals("0000"))
			kODU.append( " SUBSTRING(KERESTE.Kodu, 13, 4) >= '" + token[3] + "'  AND"  );
		token = kerestedetayraporDTO.getUkodu2().toString().split("-");
		if (! token[0].equals("ZZ"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 1, 2) <= '" + token[0] + "'  AND" );
		if (! token[1].equals("999"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 4, 3) <= '" + token[1] + "' AND"  ) ;
		if (! token[2].equals("9999"))
			kODU.append(" SUBSTRING(KERESTE.Kodu, 8, 4) <= '" + token[2] + "' AND" );
		if (! token[3].equals("9999"))
			kODU.append( " SUBSTRING(KERESTE.Kodu, 13, 4) <= '" + token[3] + "'  AND"  );
		String sql =  "SELECT " + gruplama[0]
				+ ",SUM(Miktar) as Giris_Miktar "
				+ ",sum(((CONVERT(INT,SUBSTRING(KERESTE.Kodu,4,3)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu,8,4)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000) as Giris_m3 "
				+ ",SUM(((((CONVERT(INT,SUBSTRING(KERESTE.Kodu,4,3)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu,8,4)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * Fiat  ) - SUM(((((((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * Fiat  ) * Iskonto)/100)  as Giris_Tutar "
				+ ",SUM(iif( Cikis_Evrak <> '' ,Miktar,0)) as Cikis_Miktar "
				+ ",SUM(iif( Cikis_Evrak <> '' ,((CONVERT(INT,SUBSTRING(KERESTE.Kodu,4,3)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu,8,4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  * Miktar)/1000000000) , 0 )  )  as Cikis_m3 "
				+ ",SUM(iif( Cikis_Evrak <> '' ,CTutar,0)) as Cikis_Tutar "
				+ ",sum(((CONVERT(INT,SUBSTRING(KERESTE.Kodu,4,3)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu,8,4)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000) -   "
				+ " SUM(iif( Cikis_Evrak <> '' ,((CONVERT(INT,SUBSTRING(KERESTE.Kodu,4,3)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu,8,4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  * Miktar)/1000000000) , 0 )  )  "
				+ " as Stok_M3 ," 
				+ " ((SUM(((((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * Fiat  ) - SUM(((((((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * Fiat  ) * Iskonto)/100)) /(sum(((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000))) as Ort_Fiat , "
				+ " ((SUM(((((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * Fiat  ) - SUM(((((((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) * Fiat  ) * Iskonto)/100)) /(sum(((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000))) * " 
				+ " (sum(((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000) -  SUM(iif( Cikis_Evrak <> '' , ((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  * Miktar)/1000000000) , 0 ) ) )"
				+ "  as Stok_Tutar"
				+ "  FROM KERESTE "
				+ " WHERE " 
				+ " Tarih BETWEEN '" + kerestedetayraporDTO.getGtar1() + "'" + " AND  '" + kerestedetayraporDTO.getGtar2() + " 23:59:59.998' AND" 
				+ kODU
				+ " Paket_No between N'" + kerestedetayraporDTO.getPak1() + "' AND N'" + kerestedetayraporDTO.getPak2() + "' AND " 
				+ " Cari_Firma between N'" + kerestedetayraporDTO.getGfirma1() + "' AND N'" + kerestedetayraporDTO.getGfirma2() + "' AND" 
				+ " Evrak_No between N'" + kerestedetayraporDTO.getEvr1() + "' AND N'" + kerestedetayraporDTO.getEvr2() + "' AND" 
				+ " Konsimento between N'" + kerestedetayraporDTO.getKons1() + "' AND N'" + kerestedetayraporDTO.getKons2() + "' AND" 
				+ " Ana_Grup " + kerestedetayraporDTO.getGana()  + " AND" 
				+ " Alt_Grup " + kerestedetayraporDTO.getGalt()  + " AND" 
				+ " Depo " + kerestedetayraporDTO.getGdepo()  + " AND" 
				+ " Ozel_Kod " + kerestedetayraporDTO.getGozkod() + " AND" 
				+ " CTarih BETWEEN '" + kerestedetayraporDTO.getCtar1() + "'" + " AND  '" + kerestedetayraporDTO.getCtar2() + " 23:59:59.998' AND" 
				+ " CCari_Firma between N'" + kerestedetayraporDTO.getCfirma1() + "' AND N'" + kerestedetayraporDTO.getCfirma2() + "' AND" 
				+ " Cikis_Evrak between N'" + kerestedetayraporDTO.getCevr1()+ "' AND N'" + kerestedetayraporDTO.getCevr2() + "' AND" 
				+ " CAna_Grup " + kerestedetayraporDTO.getCana()  + " AND" 
				+ " CAlt_Grup " + kerestedetayraporDTO.getCalt()  + " AND" 
				+ " CDepo " + kerestedetayraporDTO.getCdepo()  + " AND " 
				+ " COzel_Kod " + kerestedetayraporDTO.getCozkod() 
				+ " GROUP BY "+ gruplama[1] +"  ORDER BY " + gruplama[1]  ;
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
	public List<Map<String, Object>> ort_diger_kodu(kergrupraporDTO kergrupraporDTO, String yu, String iu,
			ConnectionDetails keresteConnDetails,ConnectionDetails kurConnDetails) {
		String str1 = "" ;
		String hANGI = "" ;
		if (kergrupraporDTO.getTuru().equals("GIREN"))
			hANGI= "" ;
		else if (kergrupraporDTO.getTuru().equals("CIKAN"))
			hANGI= "C" ;
		else
			hANGI= "" ;
		if(kergrupraporDTO.isDvzcevirchc())
		{
			if (!keresteConnDetails.getSqlTipi().equals(kurConnDetails.getSqlTipi())) {
				throw new ServiceException("Kereste ve Kur Dosyası farklı SQL sunucularında yer alıyor.");
			}
			if (keresteConnDetails.getServerIp().equals(kurConnDetails.getServerIp())) {
				str1=  "OK_Kur" + kurConnDetails.getDatabaseName() + ".dbo.KURLAR";
			}
		}
		String[] token = kergrupraporDTO.getUkod1().toString().split("-");
		String ilks ,ilkk,ilkb,ilkg;
		ilks = token[0];
		ilkk = token[1];
		ilkb = token[2];
		ilkg = token[3];
		token = kergrupraporDTO.getUkod2().toString().split("-");
		String sons,sonk,sonb,song;
		sons = token[0];
		sonk = token[1];
		sonb = token[2];
		song = token[3];
		String dURUM = "" ;
		if (hANGI.equals("") )
		{
			hANGI = "" ;
			dURUM =    " " ;
		}
		else {
			hANGI = "C" ;
			dURUM =   " Cikis_Evrak <> '' AND" ;
		}
		String sql = "" ,kurc = "" ;
		kurc = kergrupraporDTO.getDvzturu();
		if(kergrupraporDTO.isDvzcevirchc())
		{
			sql =  "SELECT  " + yu + "," +
					" SUM((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu,4,3)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000) - (   (KERESTE."+hANGI+"Tutar * Kereste."+hANGI+"Iskonto)/100)    ) ) As Tutar,  " +
					" SUM((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu,4,3)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000) - (   (KERESTE."+hANGI+"Tutar * Kereste."+hANGI+"Iskonto)/100)    )  / kurlar." + kurc + ") as " + kergrupraporDTO.getDoviz() +"_Tutar , " +
					" SUM(KERESTE.Miktar)  As Miktar, " +
					" SUM(((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4,3)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu,8,4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000) As m3, " +
					" SUM(Kereste." + hANGI + "Tutar - ((KERESTE." + hANGI + "Tutar * Kereste."+hANGI+"Iskonto)/100)) /  iif(( sum(((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)  ) = 0,1, " +
					" SUM(((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4,3)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu, 8,4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) As m3_Ort_Fiat , " +
					" (SUM((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu,4,3)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000) - (   (" + hANGI+"Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000) * Kereste."+hANGI+"Iskonto)/100)) / kurlar."+ kurc +") / NULLIF(SUM(((CONVERT(INT,SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000),0))  As m3_Ort_Fiat_"+ kergrupraporDTO.getDoviz() +" " +
					" FROM KERESTE left join " + str1 + " on Kurlar.Tarih = convert(varchar(10), KERESTE." + hANGI + "Tarih, 120) and kurlar.Kur = '" + kergrupraporDTO.getDoviz() + "'  " +
					" WHERE " +
					" " + dURUM +
					" KERESTE." + hANGI + "Tarih BETWEEN '" + kergrupraporDTO.getTar1() + "'" + " AND  '" + kergrupraporDTO.getTar2() + " 23:59:59.998' AND" +
					" SUBSTRING(KERESTE.Kodu, 1, 2) >= '" + ilks + "' AND SUBSTRING(KERESTE.Kodu, 1, 2) <= '"+ sons + "' AND" +
					" SUBSTRING(KERESTE.Kodu, 4, 3) >= '" + ilkk + "' AND SUBSTRING(KERESTE.Kodu, 4, 3) <= '"+ sonk + "' AND" +
					" SUBSTRING(KERESTE.Kodu, 8, 4) >= '" + ilkb + "' AND SUBSTRING(KERESTE.Kodu, 8, 4) <= '"+ sonb + "' AND" +
					" SUBSTRING(KERESTE.Kodu, 13, 4) >= '" + ilkg + "' AND SUBSTRING(KERESTE.Kodu, 13, 4) <= '"+ song + "' AND " + 
					" " + hANGI + "Cari_Firma between N'" + kergrupraporDTO.getCkod1() + "' AND N'" + kergrupraporDTO.getCkod2() + "' AND" +
					" Konsimento between N'" + kergrupraporDTO.getKons1() + "' AND N'" + kergrupraporDTO.getKons2() + "'" +
					" GROUP BY  " + iu ;
		}
		else 
		{
			kurc="" ;
			sql =  "SELECT  " + yu + "," +
					" SUM((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3))  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000) - (   (KERESTE."+hANGI+"Tutar * Kereste."+hANGI+"Iskonto)/100)    ) ) As Tutar,  " +
					" SUM((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3))  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000) - (   (KERESTE."+hANGI+"Tutar * Kereste."+hANGI+"Iskonto)/100)    ) ) as _Tutar , " +
					" SUM(KERESTE.Miktar)  As Miktar, " +
					" SUM(((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3))  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000) As m3, " +
					" SUM(KERESTE." + hANGI + "Tutar - ((KERESTE." + hANGI + "Tutar * Kereste." + hANGI + "Iskonto)/100)) / IIF(( sum(((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)  ) = 0,1, " +
					" SUM(((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3))  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000)) As m3_Ort_Fiat , " +
					" (SUM(" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3)) * CONVERT(INT,SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4))) * Miktar)/1000000000) - ((" + hANGI + "Fiat * (((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3) )  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * "  +
					" CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4))) * Miktar)/1000000000) * Kereste." + hANGI + "Iskonto)/100)) / NULLIF(SUM(((CONVERT(INT, SUBSTRING(KERESTE.Kodu, 4, 3))  *  CONVERT(INT, SUBSTRING(KERESTE.Kodu, 8, 4)) * CONVERT(INT, SUBSTRING(KERESTE.Kodu, 13, 4) )  ) * Miktar)/1000000000),0))  As m3_Ort_Fiat_" + kergrupraporDTO.getDoviz() +" " +
					" FROM KERESTE   " +
					" WHERE    " +
					" " + dURUM +
					" KERESTE." + hANGI + "Tarih BETWEEN '" + kergrupraporDTO.getTar1() + "'" + " AND  '" + kergrupraporDTO.getTar2() + " 23:59:59.998' AND" +
					" SUBSTRING(KERESTE.Kodu, 1, 2) >= '" + ilks + "' AND SUBSTRING(KERESTE.Kodu, 1, 2) <= '"+ sons + "' AND" +
					" SUBSTRING(KERESTE.Kodu, 4, 3) >= '" + ilkk + "' AND SUBSTRING(KERESTE.Kodu, 4, 3) <= '"+ sonk + "' AND" +
					" SUBSTRING(KERESTE.Kodu, 8, 4) >= '" + ilkb + "' AND SUBSTRING(KERESTE.Kodu, 8, 4) <= '"+ sonb + "' AND" +
					" SUBSTRING(KERESTE.Kodu, 13, 4) >= '" + ilkg + "' AND SUBSTRING(KERESTE.Kodu, 13, 4) <= '"+ song + "' AND " + 
					" " + hANGI + "Cari_Firma between N'" + kergrupraporDTO.getCkod1() + "' AND N'" + kergrupraporDTO.getCkod2() + "' AND" +
					" Konsimento between N'" + kergrupraporDTO.getKons1() + "' AND N'" + kergrupraporDTO.getKons2() + "'" +
					" GROUP BY  " + iu ;
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
}