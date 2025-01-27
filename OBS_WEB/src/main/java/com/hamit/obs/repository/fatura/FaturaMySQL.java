package com.hamit.obs.repository.fatura;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.custom.yardimci.Tarih_Cevir;
import com.hamit.obs.dto.stok.urunDTO;
import com.hamit.obs.exception.ServiceException;


@Component
public class FaturaMySQL implements IFaturaDatabase {

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
				" Ozel_Kod_1 ,Ozel_Kod_2 ,Barkod,Mensei,Agirlik,Resim,Fiat,Fiat_2,Fiat_3,Recete,USER" +
				" FROM MAL USE INDEX (IX_MAL)  WHERE Kodu = '" + arama + "'  ORDER by " + sira ;
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
		String query = "SELECT Adi FROM MAL  USE INDEX (IX_MAL)  WHERE Kodu = N'" + kodu + "'" ;
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

	@Override
	public void stk_ur_sil(String kodu, ConnectionDetails faturaConnDetails) {
		String sql = " DELETE  FROM MAL WHERE Kodu= ?";
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, kodu);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Evrak yok etme sırasında bir hata oluştu", e);
		}
	}

	@Override
	public void stk_ur_kayit(urunDTO urunDTO, ConnectionDetails faturaConnDetails) {
		String sql  = "INSERT INTO MAL (Kodu,Adi,Birim,Kusurat,Sinif,Ana_Grup,Alt_Grup,Aciklama_1,Aciklama_2,Ozel_Kod_1 " +
				" ,Ozel_Kod_2,Barkod,Mensei,Agirlik,Fiat,Fiat_2,Fiat_3,Recete,Kdv,Resim,Depo , Ozel_Kod_3,[USER]) " +
				" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1,urunDTO.getKodu());
			stmt.setString(2,urunDTO.getAdi());
			stmt.setString(3,urunDTO.getBirim());
			stmt.setDouble(4,urunDTO.getKusurat());
			stmt.setString(5,urunDTO.getSinif());
			stmt.setInt(6, Integer.parseInt(urunDTO.getAnagrup()));
			stmt.setInt(7,Integer.parseInt(urunDTO.getAltgrup()));
			stmt.setString(8,urunDTO.getAciklama1());
			stmt.setString(9,urunDTO.getAciklama2());
			stmt.setInt(10,Integer.parseInt(urunDTO.getOzelkod1()));
			stmt.setInt(11,Integer.parseInt(urunDTO.getOzelkod2()));
			stmt.setString(12,urunDTO.getBarkod());
			stmt.setInt(13,Integer.parseInt(urunDTO.getMensei()));
			stmt.setDouble(14,urunDTO.getAgirlik());
			stmt.setDouble(15,urunDTO.getFiat1());
			stmt.setDouble(16,urunDTO.getFiat2());
			stmt.setDouble(17,urunDTO.getFiat3());
			stmt.setString(18,urunDTO.getRecete());
			stmt.setDouble(19,0.0);
			stmt.setBytes(20,urunDTO.getImage());
			stmt.setInt(21, 0);
			stmt.setInt(22, 0);
			stmt.setString(23, urunDTO.getUsr());
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Urun kayit Hata:" + e.getMessage());
		}
	}

	@Override
	public void stk_firma_adi_kayit(String fadi, ConnectionDetails faturaConnDetails) {
		String sql = "UPDATE OZEL SET FIRMA_ADI = ? " ;
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1,fadi);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException( e.getMessage());
		}

	}

	@Override
	public String uret_son_bordro_no_al(ConnectionDetails faturaConnDetails) {
		String E_NUMBER = "" ;
		String query = "SELECT max(Evrak_No )  as NO FROM STOK  where Evrak_Cins = 'URE' LIMIT 1 ";
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				E_NUMBER = resultSet.getString("NO") == null ? "0" :resultSet.getString("NO") ;
			}
		} catch (SQLException e) {
			throw new ServiceException("Firma adı okunamadı", e);
		}
		return E_NUMBER;

	}

	@Override
	public List<Map<String, Object>> stok_oku(String eno, String cins, ConnectionDetails faturaConnDetails) {
		String sql = "SELECT Evrak_No ,Evrak_Cins,Tarih,Urun_Kodu,Miktar,Fiat ,Tutar, Hareket , " +
				" (SELECT DEPO from DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = STOK.Depo ) as Depo , " +
				" (SELECT ANA_GRUP from ANA_GRUP_DEGISKEN WHERE AGID_Y = STOK.Ana_Grup ) as Ana_Grup , " +
				" (SELECT ALT_GRUP from ALT_GRUP_DEGISKEN WHERE ALID_Y = STOK.Alt_Grup ) as Alt_Grup , " +
				" (SELECT Adi FROM MAL  WHERE MAL.Kodu = STOK.Urun_Kodu ) as Adi , " +
				" (SELECT Birim FROM MAL  WHERE MAL.Kodu = STOK.Urun_Kodu ) as Birim , " +
				" (SELECT Barkod FROM MAL  WHERE MAL.Kodu = STOK.Urun_Kodu ) as Barkod , " +
				" Izahat ,Doviz" +
				" FROM STOK  " +
				" WHERE Evrak_No  =N'" + eno + "'" +
				" AND Evrak_Cins = '" + cins + "'";
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
	public String aciklama_oku(String evrcins, int satir, String evrno, String gircik,
			ConnectionDetails faturaConnDetails) {
		String aciklama = "" ;
		String query = "SELECT * " +
				" FROM ACIKLAMA " +
				" WHERE EVRAK_NO = N'" + evrno + "'" +
				" AND SATIR = '" + satir + "'" +
				" AND EVRAK_CINS = '" + evrcins + "'" +
				" AND Gir_Cik = '" + gircik + "'";
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				aciklama = resultSet.getString("ACIKLAMA");
			}
		} catch (SQLException e) {
			throw new ServiceException("Firma adı okunamadı", e);
		}
		return aciklama;
	}

	@Override
	public urunDTO urun_adi_oku(String kodu, String kodbarcode, ConnectionDetails faturaConnDetails) {
		String sql = "SELECT Kodu,Adi,Birim,Kusurat,Barkod,Depo,Fiat,Fiat_2,Fiat_3,Agirlik, Sinif,Recete," + 
				" (SELECT  ANA_GRUP FROM ANA_GRUP_DEGISKEN    WHERE AGID_Y = MAL.Ana_Grup) AS Ana_Grup ,  " + 
				" (SELECT  ALT_GRUP FROM ALT_GRUP_DEGISKEN    WHERE ALID_Y = MAL.Alt_Grup) AS Alt_Grup,Resim " + 
				"FROM MAL USE INDEX (IX_MAL)  WHERE " + kodbarcode + " = N'" + kodu + "'";
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
				urdto.setBarkod(rs.getString("Barkod"));
				urdto.setSinif(rs.getString("Sinif"));
				urdto.setAnagrup(rs.getString("Ana_Grup"));
				urdto.setAltgrup(rs.getString("Alt_Grup"));
				urdto.setAgirlik(rs.getDouble("Agirlik"));
				urdto.setFiat1(rs.getDouble("Fiat"));
				urdto.setFiat2(rs.getDouble("Fiat_2"));
				urdto.setFiat3(rs.getDouble("Fiat_3"));
				urdto.setRecete(rs.getString("Recete"));
			}
		} catch (Exception e) {
			throw new ServiceException("Urun okunamadı", e);
		}
		return urdto;
	}

	@Override
	public double son_imalat_fiati_oku(String kodu, ConnectionDetails faturaConnDetails) {
		double fiat=0 ;
		String query = "SELECT  Fiat " +
				" FROM STOK USE INDEX (IX_STOK) " +
				" WHERE Evrak_Cins = 'URE' and Hareket ='C'  " +
				" AND Urun_Kodu = N'" + kodu + "' " +
				" ORDER BY  Tarih DESC LIMIT 1";
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				fiat = resultSet.getDouble("Fiat");
			}
		} catch (SQLException e) {
			throw new ServiceException("son ima fiat", e);
		}
		return fiat;
	}

	@Override
	public String uret_ilk_tarih(String baslangic, String tar, String ukodu, ConnectionDetails faturaConnDetails) {
		String result_tar = "1900-01-01" ;
		String query = "SELECT Urun_Kodu ,  Evrak_Cins,Tarih ,Miktar , " +
				" SUM(Miktar) OVER(ORDER BY Tarih  ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) as Miktar_Bakiye " +
				" FROM STOK USE INDEX (IX_STOK)  WHERE  STOK.Tarih >= '" + baslangic + "'  AND STOK.Tarih < '" + tar + " 23:59:59.998'" +
				" And STOK.Urun_Kodu = N'" + ukodu + "' AND Evrak_Cins <> 'DPO'" +
				" Order by Tarih";
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				Statement preparedStatement = connection.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet resultSet = preparedStatement.executeQuery(query)) {
			if (resultSet.next()) {
				resultSet.last();
				int kayit_sayi = resultSet.getRow();
				double  dbbl  = 0;
				for (int i = kayit_sayi - 1; i >= 0; i--)
				{
					dbbl = resultSet.getDouble("Miktar_Bakiye");
					if (dbbl == 0 )
					{
						Timestamp timestamp = resultSet.getTimestamp("Tarih");
						result_tar = timestamp.toString();
						break ;
					}
					resultSet.previous();
				}
			}
		} catch (SQLException e) {
			throw new ServiceException("uret ilk tar", e);
		}
		return result_tar;
	}

	@Override
	public double gir_ort_fiati_oku(String kodu, String ilkt, String tarih, ConnectionDetails faturaConnDetails) {
		double fiat=0 ;
		String query = "SELECT  IFNULL( SUM(Tutar) / SUM(Miktar),0) as Ortalama " +
				" FROM STOK  USE INDEX (IX_STOK) " +
				" WHERE  Urun_Kodu = N'" + kodu + "' " +
				" AND Hareket = 'G' AND Tarih > '" + ilkt + "' AND  Tarih < '" + tarih + " 23:59:59.998'";
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				fiat = resultSet.getDouble("Ortalama");
			}
		} catch (SQLException e) {
			throw new ServiceException("Firma adı okunamadı", e);
		}
		return fiat;
	}

	@Override
	public int uretim_fisno_al(ConnectionDetails faturaConnDetails) {
		int eNumber = 0;
		String selectSql = "SELECT E_No FROM URET_EVRAK FOR UPDATE";
		String updateSql = "UPDATE URET_EVRAK SET E_No = ?";
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(), 
				faturaConnDetails.getUsername(), 
				faturaConnDetails.getPassword())) {
			connection.setAutoCommit(false);
			try (PreparedStatement selectStmt = connection.prepareStatement(selectSql);
					ResultSet resultSet = selectStmt.executeQuery()) {
				if (resultSet.next()) {
					eNumber = resultSet.getInt("E_No");
					eNumber++;
					try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
						updateStmt.setInt(1, eNumber);
						updateStmt.executeUpdate();
					}
				}
				connection.commit();
			} catch (Exception e) {
				connection.rollback();
				throw new ServiceException("Yeni Evrak No Alınamadı", e);
			}
		} catch (Exception e) {
			throw new ServiceException("Veritabanı bağlantı hatası", e);
		}
		return eNumber;
	}

	@Override
	public List<Map<String, Object>> recete_oku(String rno, ConnectionDetails faturaConnDetails) {
		String sql = "SELECT Recete_No,Durum,Tur,Recete.Kodu,MAL.Adi,MAL.Birim ,Miktar , " +
				" (SELECT ANA_GRUP from ANA_GRUP_DEGISKEN WHERE AGID_Y = RECETE.Ana_Grup ) as Ana_Grup , " +
				" (SELECT ALT_GRUP from ALT_GRUP_DEGISKEN WHERE ALID_Y = RECETE.Alt_Grup ) as Alt_Grup , " +
				" MAL.Kusurat ,RECETE. USER  " +
				" FROM RECETE  , MAL USE INDEX (IX_MAL) "+
				" Where RECETE.KODU = MAL.Kodu " +
				" AND Recete_No = N'" + rno + "' ORDER BY Tur ";
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
	public void stok_sil(String eno, String ecins, String cins, ConnectionDetails faturaConnDetails) {
		String sql = "DELETE FROM STOK " +
				"WHERE Evrak_No = ? " +
				"AND Evrak_Cins = ? " +
				"AND Hareket = ?";
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(),
				faturaConnDetails.getUsername(),
				faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, eno);
			preparedStatement.setString(2, ecins);
			preparedStatement.setString(3, cins);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("stok sil", e);
		}
	}

	@Override
	public void stk_kaydet(String evrno, String evrcins, String tarih, int depo, String urnkodu, double miktar,
			double fiat, double tutar, double kdvlitut, String hareket, String izah, int anagrp, int altgrp, double kur,
			String b1, String doviz, String hspkodu, String usr, ConnectionDetails faturaConnDetails) {
		String sql  = "INSERT INTO STOK (Evrak_No,Evrak_Cins,Tarih,Depo,Urun_Kodu,Miktar,Fiat,Tutar,Kdvli_Tutar,Hareket,Izahat " +
				" ,Ana_Grup,Alt_Grup,Kur,B1,Doviz,Hesap_Kodu, USER ) " +
				" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, evrno);
			stmt.setString(2, evrcins);
			stmt.setTimestamp(3, Timestamp.valueOf(Tarih_Cevir.dateFormaterSaatli(tarih)));
			stmt.setInt(4, depo);
			stmt.setString(5, urnkodu);
			stmt.setDouble(6, miktar);
			stmt.setDouble(7, fiat);
			stmt.setDouble(8, tutar);
			stmt.setDouble(9, kdvlitut);
			stmt.setString(10, hareket);
			stmt.setString(11, izah);
			stmt.setInt(12, anagrp);
			stmt.setInt(13, altgrp);
			stmt.setDouble(14, kur);
			stmt.setString(15, b1);
			stmt.setString(16, doviz);
			stmt.setString(17, hspkodu);
			stmt.setString(18, usr);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Urun kayit Hata:" + e.getMessage());
		}
	}

	@Override
	public void aciklama_yaz(String evrcins, int satir, String evrno, String aciklama, String gircik,
			ConnectionDetails faturaConnDetails) {
		String sql  = "INSERT INTO ACIKLAMA (EVRAK_CINS,SATIR,EVRAK_NO,ACIKLAMA,Gir_Cik) " +
				" VALUES (?,?,?,?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, evrcins);
			stmt.setInt(2, satir);
			stmt.setString(3, evrno);
			stmt.setString(4, aciklama);
			stmt.setString(5, gircik);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Urun kayit Hata:" + e.getMessage());
		}
	}

	@Override
	public void aciklama_sil(String evrcins, String evrno, String cins, ConnectionDetails faturaConnDetails) {
		String sql = " DELETE " +
				" FROM ACIKLAMA " +
				" WHERE EVRAK_CINS = ?" +
				" AND EVRAK_NO = ? " +
				" AND Gir_Cik = ? ";
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(),
				faturaConnDetails.getUsername(),
				faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, evrcins);
			preparedStatement.setString(2, evrno);
			preparedStatement.setString(3, cins);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("aciklama sil", e);
		}	
	}

	@Override
	public List<Map<String, Object>> urun_arama(ConnectionDetails faturaConnDetails) {
		String sql = " SELECT  MAL.Barkod, MAL.Kodu, MAL.Adi, mal.Sinif,mal.Birim,mal.agirlik as Agirlik,  " +
				" IFNULL((SELECT MENSEI FROM MENSEI_DEGISKEN WHERE MENSEI_DEGISKEN.MEID_Y = MAL.Mensei),'') AS Mensei , " +
				" IFNULL((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = MAL.Ana_Grup),'') AS Ana_Grup, " +
				" IFNULL((SELECT ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = MAL.Alt_Grup),'') AS Alt_Grup, " +
				" IFNULL((SELECT OZEL_KOD_1 FROM OZ_KOD_1_DEGISKEN WHERE OZ_KOD_1_DEGISKEN.OZ1ID = MAL.Ozel_Kod_1),'') as Ozel_Kod_1, " +
				" IFNULL((SELECT OZEL_KOD_2 FROM OZ_KOD_2_DEGISKEN WHERE OZ_KOD_2_DEGISKEN.OZ2ID = MAL.Ozel_Kod_2),'') as Ozel_Kod_2, " +
				" mal.Aciklama_1,mal.Aciklama_2 " +
				" FROM MAL USE INDEX (IX_MAL) ORDER BY MAL.Kodu ";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 

	}

	@Override
	public void urun_degisken_eski(String fieldd, String degiskenAdi, String nerden, String sno, int id,
	        ConnectionDetails faturaConnDetails) {
	    String sql = "UPDATE " + nerden + " SET " + fieldd + " = ? WHERE " + sno + " = ?";
	    try (Connection connection = DriverManager.getConnection(
	            faturaConnDetails.getJdbcUrl(),
	            faturaConnDetails.getUsername(),
	            faturaConnDetails.getPassword());
	         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
	        preparedStatement.setString(1, degiskenAdi);
	        preparedStatement.setInt(2, id);
	        preparedStatement.executeUpdate();
	    } catch (SQLException e) {
	        throw new ServiceException("Ürün değişken güncelleme başarısız", e);
	    }
	}

	@Override
	public void urun_degisken_alt_grup_eski(String alt_grup, int ana_grup, int id,
			ConnectionDetails faturaConnDetails) {
		    String sql = "UPDATE ALT_GRUP_DEGISKEN SET ALT_GRUP = ?, ANA_GRUP = ? WHERE ALID_Y = ?";
		    try (Connection connection = DriverManager.getConnection(
		            faturaConnDetails.getJdbcUrl(),
		            faturaConnDetails.getUsername(),
		            faturaConnDetails.getPassword());
		         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
		        preparedStatement.setString(1, alt_grup);
		        preparedStatement.setInt(2, ana_grup);
		        preparedStatement.setInt(3, id);
		        preparedStatement.executeUpdate();
		    } catch (SQLException e) {
		        throw new ServiceException("Ürün değişken alt grup güncelleme başarısız", e);
		    }
		}

	@Override
	public void urun_degisken_kayit(String fieldd, String nerden, String degisken_adi, String sira,
			ConnectionDetails faturaConnDetails) {
		int maks = 0;
		String sql = "SELECT MAX(" + fieldd + ") AS maks FROM " + nerden;
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(),
				faturaConnDetails.getUsername(),
				faturaConnDetails.getPassword());
				PreparedStatement selectStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = selectStatement.executeQuery();
			if (resultSet.next()) {
				maks = resultSet.getInt("maks");
			}
			sql = "INSERT INTO " + nerden + " (" + fieldd + ", " + degisken_adi + ", USER) VALUES (?, ?, ?)";
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
	public void urun_degisken_alt_grup_kayit(String alt_grup, int ana_grup, ConnectionDetails faturaConnDetails) {
		int maks = 0;
		String sql = "SELECT max(ALID_Y) AS ALID_Y FROM ALT_GRUP_DEGISKEN " ;
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(),
				faturaConnDetails.getUsername(),
				faturaConnDetails.getPassword());
				PreparedStatement selectStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = selectStatement.executeQuery();
			if (resultSet.next()) {
				int count=0;
				count = resultSet.getRow();
				maks = (count != 0) ? resultSet.getInt("ALID_Y") : 0;
			}
			sql = "INSERT INTO ALT_GRUP_DEGISKEN (ALID_Y,ALT_GRUP,ANA_GRUP,USER) " +
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
	public boolean alt_grup_kontrol(int anagrp, int altgrp, ConnectionDetails faturaConnDetails) {
		   boolean result = true;
		    String[] sqlQueries = {
		        "SELECT * FROM MAL WHERE Ana_Grup = ? AND Alt_Grup = ?",
		        "SELECT * FROM FATURA WHERE Ana_Grup = ? AND Alt_Grup = ?",
		        "SELECT * FROM IRSALIYE WHERE Ana_Grup = ? AND Alt_Grup = ?",
		        "SELECT * FROM RECETE WHERE Ana_Grup = ? AND Alt_Grup = ?",
		        "SELECT * FROM STOK WHERE Ana_Grup = ? AND Alt_Grup = ?"
		    };
		    try (Connection connection = DriverManager.getConnection(
		            faturaConnDetails.getJdbcUrl(),
		            faturaConnDetails.getUsername(),
		            faturaConnDetails.getPassword())) {
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
		    }		return result;
	}

	@Override
	public void urun_degisken_alt_grup_sil(int id, ConnectionDetails faturaConnDetails) {
		String sql = "DELETE FROM ALT_GRUP_DEGISKEN WHERE ALID_Y = ? ";
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(),
				faturaConnDetails.getUsername(),
				faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, id);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("stok sil", e);
		}
	}

	@Override
	public void urun_kod_degisken_sil(String hangi_Y, String nerden, int sira, ConnectionDetails faturaConnDetails) {
		String sql = "DELETE  FROM " + nerden  + " WHERE " + hangi_Y + " = ? ";
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(),
				faturaConnDetails.getUsername(),
				faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			System.out.println(sql);
			preparedStatement.setInt(1, sira);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("stok sil", e);
		}
	}

	@Override
		public double son_satis_fiati_oku(String kodu, String muskodu, String gircik, ConnectionDetails faturaConnDetails) {
			double fiat = 0.0;
			String sql = "SELECT  Fiat " +
					" FROM FATURA USE INDEX (IX_FATURA) " +
					" WHERE  Cari_Firma = N'" + muskodu + "'" +
					" AND  Kodu = N'" + kodu + "'" +
					" AND Gir_Cik = '" + gircik + "'" +
					" ORDER BY  Tarih desc   LIMIT 1 ";
			try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
					PreparedStatement preparedStatement = connection.prepareStatement(sql);
					ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					fiat  = resultSet.getDouble("Fiat");
				}
			} catch (Exception e) {
				throw new ServiceException("Firma adı okunamadı", e);
			}
			return fiat;
		}

		@Override
		public List<Map<String, Object>> fatura_oku(String fno, String cins, ConnectionDetails faturaConnDetails) {
			String sql = "SELECT  Fatura_No ,FATURA.Kodu,Tarih ,FATURA.Kdv ,Doviz,ABS(Miktar) as Miktar ,FATURA.Fiat,Cari_Firma,Iskonto, " + 
					" Tevkifat, " + 
					" IFNULL((Select ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = FATURA.Ana_Grup ) , '') AS Ana_Grup, " +
					" IFNULL((Select ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = FATURA.Alt_Grup ) , '') AS Alt_Grup, " +
					" IFNULL((Select DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = FATURA.DEPO ) , '') AS Depo ,Adres_Firma ," +
					" Ozel_Kod ,Gir_Cik ,MAL.Barkod ,Birim ,Izahat,MAL.Adi,Tutar,Kur, " +
					" IFNULL((Select ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = MAL.Ana_Grup ) , '') AS Ur_AnaGrup, " +
					" IFNULL((Select ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = MAL.Alt_Grup ) , '') AS Ur_AltGrup, " +
					" Resim" +
					" FROM Fatura USE INDEX (IX_FATURA), MAL USE INDEX (IX_MAL) " +
					" WHERE Fatura.Kodu = MAL.Kodu " +
					" AND Fatura_No = N'" + fno + "'" +
					" AND Gir_Cik = '" + cins + "'";
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
		public String[] dipnot_oku(String ino, String cins, String gircik,
				ConnectionDetails faturaConnDetails) {
			String[] dipnot = {"","",""};
			String sql = "SELECT * " +
					" FROM DPN " +
					" WHERE Evrak_NO = N'" + ino + "'" +
					" AND DPN.Tip = N'" + cins + "'" +
					" AND Gir_Cik = '" + gircik + "'";
			try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
					PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						dipnot[0] = resultSet.getString("Bir");
						dipnot[1] = resultSet.getString("Iki");
						dipnot[2] = resultSet.getString("Uc");
					}
				}
			} catch (Exception e) {
				throw new ServiceException("MS stkService genel hatası.", e);
			}
			return dipnot; 
		}

		@Override
		public String son_no_al(String cins, ConnectionDetails faturaConnDetails) {
			String son_no = "";
			String query = "SELECT max(Fatura_No)  as NO FROM FATURA WHERE Gir_Cik = '" + cins + "' ";
			try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
					PreparedStatement preparedStatement = connection.prepareStatement(query);
					ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					son_no = resultSet.getString("NO");
				}
			} catch (SQLException e) {
				throw new ServiceException("Firma adı okunamadı", e);
			}
			return son_no;
		}

		@Override
		public int fatura_no_al(String cins, ConnectionDetails faturaConnDetails) {
			int E_NUMBER = 0;
			String sql = "SELECT max(Fatura_No + 1) AS NO  FROM FATURA WHERE Gir_Cik = '" + cins + "' ";
			try (Connection connection =  DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
					PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (!resultSet.isBeforeFirst() ) {  
						E_NUMBER = 0 ;
					}
					else
					{
						resultSet.next();
						E_NUMBER = resultSet.getInt("NO");
					}
				}
			} catch (Exception e) {
				throw new ServiceException("Fatura Numaralarinda onceden harf ve rakkam kullanildigindan otomatik numara verilemez...."); 
			}
			return E_NUMBER;

		}

		@Override
		public void fat_giris_sil(String fno, String cins, ConnectionDetails faturaConnDetails) {
		    String sqlFatura = "DELETE FROM FATURA WHERE Fatura_No = ? AND Gir_Cik = ?";
		    String sqlStok = "DELETE FROM STOK WHERE Evrak_No = ? AND Hareket = ? AND Evrak_Cins = 'FAT'";
		    try (Connection connection = DriverManager.getConnection(
		            faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword())) {
		        try (PreparedStatement stmt = connection.prepareStatement(sqlFatura)) {
		            stmt.setString(1, fno);
		            stmt.setString(2, cins);
		            stmt.executeUpdate();
		        }
		        try (PreparedStatement stmt = connection.prepareStatement(sqlStok)) {
		            stmt.setString(1, fno);
		            stmt.setString(2, cins);
		            stmt.executeUpdate();
		        }
		    } catch (Exception e) {
		        throw new ServiceException("Evrak yok etme sırasında bir hata oluştu", e);
		    }
		}

		@Override
		public void dipnot_sil(String ino, String cins, String gircik, ConnectionDetails faturaConnDetails) {
		    String sql = "DELETE FROM DPN WHERE Evrak_NO = ? AND Tip = ? AND Gir_Cik = ?";
		    try (Connection connection = DriverManager.getConnection(
		            faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
		         PreparedStatement stmt = connection.prepareStatement(sql)) {
		        stmt.setString(1, ino);
		        stmt.setString(2, cins);
		        stmt.setString(3, gircik);
		        stmt.executeUpdate();
		    } catch (Exception e) {
		        throw new ServiceException("Dipnot silme sırasında bir hata oluştu.", e);
		    }
		}

		@Override
		public List<Map<String, Object>> fat_oz_kod(String cins, ConnectionDetails faturaConnDetails) {
			String sql = "SELECT DISTINCT  Ozel_Kod  " + 
					"  FROM FATURA WHERE Gir_Cik = '" + cins + "'";
			List<Map<String, Object>> resultList = new ArrayList<>();
			try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
					PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				ResultSet resultSet = preparedStatement.executeQuery();
				resultList = ResultSetConverter.convertToList(resultSet); 
			} catch (Exception e) {
				throw new ServiceException("MS stkService genel hatası.", e);
			}
			return resultList; 
		}

		@Override
		public void fat_kaydet(String fatno, String kodu, int depo, double fiat, double tevkifat, double miktar,
				String gircik, double tutar, double iskonto, double kdv, String tarih, String izah, String doviz,
				String adrfirma, String carfirma, String ozkod, double kur, String cins, int anagrp, int altgrp, String usr,
				ConnectionDetails faturaConnDetails) {
			String sql  = "INSERT INTO FATURA (Fatura_No,Kodu,Depo,Fiat,Tevkifat,Miktar,Gir_Cik,Tutar,Iskonto,Kdv,Tarih,Izahat " +
					" ,Doviz,Adres_Firma,Cari_Firma,Ozel_Kod,Kur,Cins,Ana_Grup,Alt_Grup,USER) " +
					" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
			try (Connection connection = DriverManager.getConnection(
					faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
					PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1,fatno);
				stmt.setString(2, kodu);
				stmt.setInt(3,depo);
				stmt.setDouble(4, fiat);
				stmt.setDouble(5, tevkifat);
				stmt.setDouble(6, miktar);
				stmt.setString(7, gircik);
				stmt.setDouble(8, tutar);
				stmt.setDouble(9, iskonto);
				stmt.setDouble(10, kdv);
				stmt.setTimestamp(11, Timestamp.valueOf(Tarih_Cevir.dateFormaterSaatli(tarih)));
				stmt.setString(12, izah);
				stmt.setString(13, doviz);
				stmt.setString(14, adrfirma);
				stmt.setString(15, carfirma);
				stmt.setString(16, ozkod);
				stmt.setDouble(17, kur);
				stmt.setString(18, cins);
				stmt.setInt(19,anagrp);
				stmt.setInt(20,altgrp);
				stmt.setString(21,usr);
				stmt.executeUpdate();
				stmt.close();
			} catch (Exception e) {
				throw new ServiceException("Urun kayit Hata:" + e.getMessage());
			}

		}

		@Override
		public void dipnot_yaz(String eno, String bir, String iki, String uc, String tip, String gircik, String usr,
				ConnectionDetails faturaConnDetails) {
			String sql  = "INSERT INTO DPN (Evrak_No,Tip,Bir,Iki,Uc,Gir_Cik,USER) " +
					" VALUES (?,?,?,?,?,?,?)" ;
			try (Connection connection = DriverManager.getConnection(
					faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
					PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1, eno);
				stmt.setString(2, tip);
				stmt.setString(3, bir);
				stmt.setString(4, iki);
				stmt.setString(5, uc);
				stmt.setString(6,gircik);
				stmt.setString(7, usr);
				stmt.executeUpdate();
				stmt.close();
			} catch (Exception e) {
				throw new ServiceException("Urun kayit Hata:" + e.getMessage());
			}
		}

		@Override
		public String recete_son_bordro_no_al(ConnectionDetails faturaConnDetails) {
			String E_NUMBER = "" ;
			String sql = "SELECT MAX(Recete_No) as NO  FROM RECETE   LIMIT 1";
			try (Connection connection =  DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
					PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.isBeforeFirst() ) {  
						resultSet.next();
						E_NUMBER = resultSet.getString("NO");
					}
				}
			} catch (Exception e) {
				throw new ServiceException("REceto no alma.",e); 
			}
			return E_NUMBER;

		}

		@Override
		public int recete_no_al(ConnectionDetails faturaConnDetails) {
			int E_NUMBER = 0 ;
			String sql = "SELECT max(Recete_No + 1) AS NO  FROM Recete  ";
			try (Connection connection =  DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
					PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.isBeforeFirst() ) {  
						resultSet.next();
						E_NUMBER = resultSet.getInt("NO");
					}
				}
			} catch (Exception e) {
				throw new ServiceException("REceto no alma.",e); 
			}
			return E_NUMBER;

		}

		@Override
		public void rec_sil(String rno, ConnectionDetails faturaConnDetails) {
			String sql = " DELETE FROM RECETE WHERE Recete_No = ?";
			try (Connection connection = DriverManager.getConnection(
					faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
					PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1, rno);
				stmt.executeUpdate();
			} catch (Exception e) {
				throw new ServiceException("Evrak yok etme sırasında bir hata oluştu", e);
			}
		}

		@Override
		public void kod_recete_yaz(String ukodu, String rec, ConnectionDetails faturaConnDetails) {
			String sql = "UPDATE MAL SET Recete = N'" + rec + "' WHERE Kodu = N'" + ukodu + "'";
			try (Connection connection = DriverManager.getConnection(
					faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
					PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.executeUpdate();
			} catch (Exception e) {
				throw new ServiceException("Evrak yok etme sırasında bir hata oluştu", e);
			}
		}

		@Override
		public void recete_kayit(String recno, boolean durum, String tur, String kodu, double miktar, int anagrp,
				int altgrup, String usr, ConnectionDetails faturaConnDetails) {
			String sql = "INSERT INTO RECETE (Recete_No,Durum,Tur,Kodu,Miktar,Ana_Grup,Alt_Grup,USER) " +
					" VALUES (?,?,?,?,?,?,?,?)" ;
			try (Connection connection = DriverManager.getConnection(
					faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
					PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1, recno);
				stmt.setBoolean(2, durum);
				stmt.setString(3, tur);
				stmt.setString(4, kodu);
				stmt.setDouble(5, miktar);
				stmt.setInt(6, anagrp);
				stmt.setInt(7, altgrup);
				stmt.setString(8, usr);
				stmt.executeUpdate();
			} catch (Exception e) {
				throw new ServiceException("Evrak yok etme sırasında bir hata oluştu", e);
			}
		}

		@Override
		public String zayi_son_bordro_no_al(ConnectionDetails faturaConnDetails) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int zayi_fisno_al(ConnectionDetails faturaConnDetails) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public List<Map<String, Object>> zayi_oku(String eno, String cins, ConnectionDetails faturaConnDetails) {
			// TODO Auto-generated method stub
			return null;
		}
	}
