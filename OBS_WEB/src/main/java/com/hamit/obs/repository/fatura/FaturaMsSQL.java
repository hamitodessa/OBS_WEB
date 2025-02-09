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
import com.hamit.obs.dto.stok.urunDTO;
import com.hamit.obs.dto.stok.raporlar.envanterDTO;
import com.hamit.obs.dto.stok.raporlar.fatraporDTO;
import com.hamit.obs.dto.stok.raporlar.grupraporDTO;
import com.hamit.obs.dto.stok.raporlar.imaraporDTO;
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

	@Override
	public void stk_ur_sil(String kodu, ConnectionDetails faturaConnDetails) {
		String sql = " DELETE FROM MAL WHERE Kodu= ?";
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
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}

	@Override
	public String uret_son_bordro_no_al(ConnectionDetails faturaConnDetails) {
		String E_NUMBER = "" ;
		String query = "SELECT max(Evrak_No) AS NO FROM STOK WHERE Evrak_Cins = 'URE' OPTION (FAST 1)";
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
				"FROM MAL WITH (INDEX (IX_MAL))  WHERE " + kodbarcode + " = N'" + kodu + "'";
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
		String query = "SELECT TOP 1 Fiat " +
				" FROM STOK WITH (INDEX (IX_STOK)) " +
				" WHERE Evrak_Cins = 'URE' and Hareket ='C'  " +
				" AND Urun_Kodu = N'" + kodu + "' " +
				" ORDER BY  Tarih DESC OPTION (FAST 1)";
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
				" FROM STOK WITH (INDEX (IX_STOK))  WHERE  STOK.Tarih >= '" + baslangic + "'  AND STOK.Tarih < '" + tar + " 23:59:59.998'" +
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
		String query = "SELECT  ISNULL( SUM(Tutar) / SUM(Miktar),0) as Ortalama " +
				" FROM STOK  WITH (INDEX (IX_STOK)) " +
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
		int evrakNo = 0;
		String sql = "UPDATE URET_EVRAK SET E_No = E_No + 1 OUTPUT INSERTED.E_No ;";
		try (Connection connection =  DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					evrakNo = resultSet.getInt("E_No");
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Yeni Evrak No Alinamadi", e); 
		}
		return evrakNo;
	}

	@Override
	public List<Map<String, Object>> recete_oku(String rno, ConnectionDetails faturaConnDetails) {
		String sql = "SELECT Recete_No,Durum,Tur,Recete.Kodu,MAL.Adi,MAL.Birim ,Miktar , " +
				" (SELECT ANA_GRUP from ANA_GRUP_DEGISKEN WHERE AGID_Y = RECETE.Ana_Grup ) as Ana_Grup , " +
				" (SELECT ALT_GRUP from ALT_GRUP_DEGISKEN WHERE ALID_Y = RECETE.Alt_Grup ) as Alt_Grup , " +
				" MAL.Kusurat ,RECETE.[USER] " +
				" FROM RECETE  , MAL WITH (INDEX (IX_MAL)) "+
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
				" ,Ana_Grup,Alt_Grup,Kur,B1,Doviz,Hesap_Kodu,[USER]) " +
				" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, evrno);
			stmt.setString(2, evrcins);
			stmt.setTimestamp(3, Timestamp.valueOf(tarih));
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
				" ISNULL((SELECT MENSEI FROM MENSEI_DEGISKEN WHERE MENSEI_DEGISKEN.MEID_Y = MAL.Mensei),'') AS Mensei , " +
				" ISNULL((SELECT ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = MAL.Ana_Grup),'') AS Ana_Grup, " +
				" ISNULL((SELECT ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = MAL.Alt_Grup),'') AS Alt_Grup, " +
				" ISNULL((SELECT OZEL_KOD_1 FROM OZ_KOD_1_DEGISKEN WHERE OZ_KOD_1_DEGISKEN.OZ1ID = MAL.Ozel_Kod_1),'') as Ozel_Kod_1, " +
				" ISNULL((SELECT OZEL_KOD_2 FROM OZ_KOD_2_DEGISKEN WHERE OZ_KOD_2_DEGISKEN.OZ2ID = MAL.Ozel_Kod_2),'') as Ozel_Kod_2, " +
				" mal.Aciklama_1,mal.Aciklama_2 " +
				" FROM MAL WITH (INDEX (IX_MAL)) ORDER BY MAL.Kodu ";
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
	public void urun_degisken_alt_grup_kayit(String alt_grup, int ana_grup, ConnectionDetails faturaConnDetails) {
		int maks = 0;
		String sql = "SELECT max(ALID_Y)  AS ALID_Y  FROM ALT_GRUP_DEGISKEN   " ;
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
		}		
		return result;
	}

	@Override
	public void urun_degisken_alt_grup_sil(int id, ConnectionDetails faturaConnDetails) {
		String sql = "DELETE FROM ALT_GRUP_DEGISKEN  WHERE ALID_Y = ? ";
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
		String sql = "DELETE FROM " + nerden  + " WHERE " + hangi_Y + " = ?";
		try (Connection connection = DriverManager.getConnection(
				faturaConnDetails.getJdbcUrl(),
				faturaConnDetails.getUsername(),
				faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, sira);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("stok sil", e);
		}
	}

	@Override
	public double son_satis_fiati_oku(String kodu, String muskodu, String gircik, ConnectionDetails faturaConnDetails) {
		double fiat = 0.0;
		String sql = "SELECT TOP 1  Fiat " +
				" FROM FATURA WITH (INDEX (IX_FATURA)) " +
				" WHERE  Cari_Firma = N'" + muskodu + "'" +
				" AND  Kodu = N'" + kodu + "'" +
				" AND Gir_Cik = '" + gircik + "'" +
				" ORDER BY  Tarih desc  OPTION (FAST 1)";
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
				" ISNULL((Select ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = FATURA.Ana_Grup ) , '') AS Ana_Grup, " +
				" ISNULL((Select ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = FATURA.Alt_Grup ) , '') AS Alt_Grup, " +
				" ISNULL((Select DEPO FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = FATURA.DEPO ) , '') AS Depo ,Adres_Firma ," +
				" Ozel_Kod ,Gir_Cik ,MAL.Barkod ,Birim ,Izahat,MAL.Adi,Tutar,Kur, " +
				" ISNULL((Select ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = MAL.Ana_Grup ) , '') AS Ur_AnaGrup, " +
				" ISNULL((Select ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = MAL.Alt_Grup ) , '') AS Ur_AltGrup, " +
				" Resim" +
				" FROM Fatura WITH (INDEX (IX_FATURA)), MAL WITH (INDEX (IX_MAL)) " +
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
		String query ="SELECT max(Fatura_No)  as NO FROM FATURA WHERE Gir_Cik = '" + cins + "' ";
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
				" ,Doviz,Adres_Firma,Cari_Firma,Ozel_Kod,Kur,Cins,Ana_Grup,Alt_Grup,[USER]) " +
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
			stmt.setTimestamp(11, Timestamp.valueOf(tarih));
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
		String sql  = "INSERT INTO DPN (Evrak_No,Tip,Bir,Iki,Uc,Gir_Cik,[USER]) " +
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
		String sql = "SELECT MAX(Recete_No) as NO  FROM RECETE   OPTION (FAST 1) ";
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
		String sql = "INSERT INTO RECETE (Recete_No,Durum,Tur,Kodu,Miktar,Ana_Grup,Alt_Grup,[USER]) " +
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
		String E_NUMBER = "" ;
		String sql =  "SELECT max(Evrak_No )  as NO FROM STOK  where Evrak_Cins = 'ZAI' OPTION (FAST 1) ";
		try (Connection connection =  DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.isBeforeFirst() ) {  
					resultSet.next();
					E_NUMBER = resultSet.getString("NO") == null ? "0" :resultSet.getString("NO") ;
				}
			}
		} catch (Exception e) {
			throw new ServiceException("REceto no alma.",e); 
		}
		return E_NUMBER;
	}

	@Override
	public int zayi_fisno_al(ConnectionDetails faturaConnDetails) {
		int evrakNo = 0;
		String sql = "UPDATE ZAYI_EVRAK SET E_No = E_No + 1 OUTPUT INSERTED.E_No ;";
		try (Connection connection =  DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					evrakNo = resultSet.getInt("E_No");
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Yeni Evrak No Alinamadi", e); 
		}
		return evrakNo;
	}

	@Override
	public List<Map<String, Object>> zayi_oku(String eno, String cins, ConnectionDetails faturaConnDetails) {
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
				" AND Evrak_Cins = '" + cins + "' AND Hareket ='C'";
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
	public List<Map<String, Object>> fat_rapor(fatraporDTO fatraporDTO, ConnectionDetails faturaConnDetails) {
		String sql = " SELECT Fatura_No ,IIF(Gir_Cik = 'C','Satis','Alis') as Hareket,Tarih ,Cari_Firma ,Adres_Firma,Doviz , sum(Miktar) as Miktar ,sum(Fiat * Miktar) as Tutar, " +
				" SUM(Fiat * Miktar) - sum(((Fiat * Miktar) * Iskonto)/100) as Iskontolu_Tutar,count(Fatura_No) as fatsayi " +
				" FROM FATURA WITH (INDEX (IX_FATURA)) " +
				" WHERE FATURA.Fatura_No >= N'" + fatraporDTO.getFatno1() + "' AND  FATURA.Fatura_No <= N'" + fatraporDTO.getFatno2() + "'" +
				" AND FATURA.Tarih >= '" + fatraporDTO.getTar1() + "' AND  FATURA.Tarih <= '" + fatraporDTO.getTar2() + " 23:59:59.998'" +
				" AND FATURA.Cari_Firma >= N'" + fatraporDTO.getCkod1() + "' AND  FATURA.Cari_Firma <= N'" + fatraporDTO.getCkod2() + "' " +
				" AND FATURA.Adres_Firma >= N'" + fatraporDTO.getAdr1() + "' AND  FATURA.Adres_Firma <= N'" + fatraporDTO.getAdr2() + "' " +
				" AND FATURA.Kodu >= N'" + fatraporDTO.getUkod1() + "' AND FATURA.Kodu <= N'" + fatraporDTO.getUkod2() + "' " +
				" AND FATURA.Doviz >= N'" + fatraporDTO.getDvz1() + "' AND FATURA.Doviz <= N'" + fatraporDTO.getDvz2() + "' " +
				" AND FATURA.Tevkifat >= '" + fatraporDTO.getTev1() + "' AND FATURA.Tevkifat <= '" + fatraporDTO.getTev2() + "' " +
				" AND FATURA.Ozel_Kod >= N'" + fatraporDTO.getOkod1() + "' AND FATURA.Ozel_Kod <= N'" + fatraporDTO.getOkod2() + "' " +
				" AND FATURA.Ana_Grup " + fatraporDTO.getAnagrp() +
				" AND FATURA.Alt_Grup " + fatraporDTO.getAltgrp() +
				" AND FATURA.Depo " + fatraporDTO.getDepo() +
				" AND FATURA.Gir_Cik Like '" + fatraporDTO.getTuru() + "%'" +
				" GROUP BY Fatura_No,Gir_Cik,Tarih ,Cari_Firma,Adres_Firma,Doviz  " +
				" ORDER BY  Fatura_No";
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
	public List<Map<String, Object>> fat_detay_rapor(String fno, String turu, ConnectionDetails faturaConnDetails) {
		String sql = "SELECT  Fatura.Kodu, " +
				" (SELECT Adi FROM MAL WHERE FATURA.Kodu = MAL.KODU ) AS Adi , " +
				"  Miktar , " +
				" (SELECT Birim FROM MAL WHERE FATURA.KODU = MAL.KODU ) AS Birim , " +
				" Fatura.Fiat ,Doviz,Fatura.Fiat * Miktar as Tutar , " +
				" Iskonto , " +
				" ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100 as Iskonto_Tutar , " +
				" (Fatura.Fiat * [Miktar]) - ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100 as Iskontolu_Tutar , " +
				" Fatura.Kdv , " +
				" (((Fatura.Fiat * [Miktar]) - ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100) * Fatura.kdv ) / 100  AS Kdv_Tutar , " +
				" Tevkifat , " +
				" (((((Fatura.Fiat * [Miktar]) - ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100) * Fatura.kdv ) / 100)/ 10 ) * [Tevkifat] as Tev_Edilen_KDV , " +
				" ((Fatura.Fiat * [Miktar]) - ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100) + ((((Fatura.Fiat * [Miktar]) - ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100) * Fatura.kdv ) / 100) as Tev_Dah_Top_Tutar , " +
				" ((((Fatura.Fiat * [Miktar]) - ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100) * Fatura.kdv ) / 100) - ((((((Fatura.Fiat * [Miktar]) - ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100) * Fatura.kdv ) / 100)/ 10 ) * [Tevkifat] ) as Beyan_Edilen_KDV , " +
				" (((Fatura.Fiat * [Miktar]) - ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100) + ((((Fatura.Fiat * [Miktar]) - ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100) * Fatura.kdv ) / 100)) - ((((((Fatura.Fiat * [Miktar]) - ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100) * Fatura.kdv ) / 100)/ 10 ) * [Tevkifat]) as Tev_Har_Top_Tutar , " +
				" ISNULL((SELECT Ana_Grup FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = MAL.Ana_Grup),'') AS Ana_Grup  , " +
				" ISNULL((SELECT Alt_Grup FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = MAL.Alt_Grup),'') AS Alt_Grup  , " +
				" ISNULL((SELECT Depo FROM DEPO_DEGISKEN WHERE DEPO_DEGISKEN.DPID_Y = FATURA.Depo),'') AS Depo , " +
				" Ozel_Kod ,Izahat, IIF(Gir_Cik = 'C','Satis','Alis') as Hareket ,FATURA.[USER]" +
				" FROM FATURA,MAL " +
				" WHERE FATURA.Fatura_No = '" + fno + "' " + 
				" AND FATURA.Gir_Cik Like '" + turu + "%'" +
				" AND FATURA .Kodu = mal.Kodu " ;
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
	public List<Map<String, Object>> fat_rapor_fat_tar(fatraporDTO fatraporDTO, ConnectionDetails faturaConnDetails) {
		String sql = " SELECT Fatura_No,IIF(Gir_Cik = 'C','Satis','Alis') as Hareket,Tarih " +
				" " + fatraporDTO.getBir() + "" +
				" " + fatraporDTO.getIki() + "" +
				" , sum( Miktar) as Miktar " +
				" ,sum([Fiat] * [Miktar]) as Tutar " +
				" ,sum((Fiat * Miktar) - ((Fiat * Miktar) * Iskonto)/100) as Iskontolu_Tutar  " +
				" ,sum((((Fiat * Miktar) - ((Fiat * Miktar) * Iskonto)/100) * Fatura.kdv)/100)  AS Kdv_Tutar " +
				" ,sum((Fiat * Miktar) - ((Fiat * Miktar) * Iskonto)/100 +   (((Fatura.Fiat * [Miktar]) - ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100) * Fatura.kdv ) / 100)    as Toplam_Tutar " +
				" FROM FATURA WITH (INDEX (IX_FATURA)) " +
				" WHERE FATURA.Fatura_No >= N'" + fatraporDTO.getFatno1() + "' AND  FATURA.Fatura_No <= N'" + fatraporDTO.getFatno2() + "'" +
				" AND FATURA.Tarih >= '" + fatraporDTO.getTar1() + "' AND  FATURA.Tarih <= '" + fatraporDTO.getTar2() + " 23:59:59.998'" +
				" AND FATURA.Cari_Firma >= N'" + fatraporDTO.getCkod1() + "' AND  FATURA.Cari_Firma <= N'" + fatraporDTO.getCkod2() + "' " +
				" AND FATURA.Adres_Firma >= N'" + fatraporDTO.getAdr1() + "' AND  FATURA.Adres_Firma <= N'" + fatraporDTO.getAdr2() + "' " +
				" AND FATURA.Kodu >= N'" + fatraporDTO.getUkod1() + "' AND FATURA.Kodu <= N'" + fatraporDTO.getUkod2() + "' " +
				" AND FATURA.Doviz >= N'" + fatraporDTO.getDvz1() + "' AND FATURA.Doviz <= N'" + fatraporDTO.getDvz2() + "' " +
				" AND FATURA.Tevkifat >= '" + fatraporDTO.getTev1() + "' AND FATURA.Tevkifat <= '" + fatraporDTO.getTev2() + "' " +
				" AND FATURA.Ozel_Kod >= N'" + fatraporDTO.getOkod1() + "' AND FATURA.Ozel_Kod <= N'" + fatraporDTO.getOkod2() + "' " +
				" AND FATURA.Ana_Grup " + fatraporDTO.getAnagrp() +
				" AND FATURA.Alt_Grup " + fatraporDTO.getAltgrp() +
				" AND FATURA.Depo " + fatraporDTO.getDepo() +
				" AND FATURA.Gir_Cik Like '" + fatraporDTO.getTuru() + "%'" +
				" GROUP BY " + fatraporDTO.getUc() + "" +
				" ORDER BY  " + fatraporDTO.getUc() + "";
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
	public List<Map<String, Object>> fat_rapor_cari_kod(fatraporDTO fatraporDTO, ConnectionDetails faturaConnDetails) {
		String sql = " SELECT  " + fatraporDTO.getUc() + " ,IIF(Gir_Cik = 'C','Satis','Alis') as Hareket " +
				"  " + fatraporDTO.getBir() + " " +
				" ,sum([Miktar]) as Miktar " +
				" ,sum([Fiat] * [Miktar]) as Tutar " +
				" ,SUM(Fiat * Miktar) - sum(((Fiat * Miktar) * Iskonto)/100) as Iskontolu_Tutar " +
				" ,sum((((Fatura.Fiat * [Miktar]) - ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100) * Fatura.kdv ) / 100)  AS Kdv_Tutar " +
				" ,SUM(Fiat * Miktar) - sum(((Fiat * Miktar) * Iskonto)/100) +   sum((((Fatura.Fiat * [Miktar]) - ((Fatura.Fiat * [Miktar]) * [Iskonto]) / 100) * Fatura.kdv ) / 100)    as Toplam_Tutar" +
				" FROM FATURA WITH (INDEX (IX_FATURA)) " +
				" WHERE FATURA.Fatura_No >= N'" + fatraporDTO.getFatno1() + "' AND  FATURA.Fatura_No <= N'" + fatraporDTO.getFatno2() + "'" +
				" AND FATURA.Tarih >= '" + fatraporDTO.getTar1() + "' AND  FATURA.Tarih <= '" + fatraporDTO.getTar2() + " 23:59:59.998'" +
				" AND FATURA.Cari_Firma >= N'" + fatraporDTO.getCkod1() + "' AND  FATURA.Cari_Firma <= N'" + fatraporDTO.getCkod2() + "' " +
				" AND FATURA.Adres_Firma >= N'" + fatraporDTO.getAdr1() + "' AND  FATURA.Adres_Firma <= N'" + fatraporDTO.getAdr2() + "' " +
				" AND FATURA.Kodu >= N'" + fatraporDTO.getUkod1() + "' AND FATURA.Kodu <= N'" + fatraporDTO.getUkod2() + "' " +
				" AND FATURA.Doviz >= N'" + fatraporDTO.getDvz1() + "' AND FATURA.Doviz <= N'" + fatraporDTO.getDvz2() + "' " +
				" AND FATURA.Tevkifat >= '" + fatraporDTO.getTev1() + "' AND FATURA.Tevkifat <= '" + fatraporDTO.getTev2() + "' " +
				" AND FATURA.Ozel_Kod >= N'" + fatraporDTO.getOkod1() + "' AND FATURA.Ozel_Kod <= N'" + fatraporDTO.getOkod2() + "' " +
				" AND FATURA.Ana_Grup " + fatraporDTO.getAnagrp() +
				" AND FATURA.Alt_Grup " + fatraporDTO.getAltgrp() +
				" AND FATURA.Depo " + fatraporDTO.getDepo() +
				" AND FATURA.Gir_Cik Like '" + fatraporDTO.getTuru() + "%'" +
				" GROUP BY " + fatraporDTO.getUc() + "" +
				" ORDER BY  " + fatraporDTO.getUc() + "";
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
	public List<Map<String, Object>> imalat_rapor(imaraporDTO imaraporDTO, ConnectionDetails faturaConnDetails) {
		String sql = "SELECT Evrak_No, Tarih,Urun_Kodu, Adi , " +
				" Miktar,  Birim ,(Miktar * Mal.Agirlik) as Agirlik ," +
				" (SELECT DEPO from DEPO_DEGISKEN WHERE DPID_Y = STOK.DEPO ) as Depo , " +
				" (SELECT ANA_GRUP from ANA_GRUP_DEGISKEN WHERE AGID_Y = STOK.Ana_Grup ) as Ana_Grup , " +
				" (SELECT ALT_GRUP from ALT_GRUP_DEGISKEN WHERE ALID_Y = STOK.Alt_Grup ) as Alt_Grup  , " +
				" Barkod  , " +
				" Recete ,STOK.[USER]" +
				" FROM STOK WITH (INDEX (IX_STOK)) ,MAL WITH (INDEX (IX_MAL))" +
				" WHERE Evrak_Cins = 'URE' " +
				" AND MAL.Ana_Grup " + imaraporDTO.getUranagrp() +
				" AND MAL.Alt_Grup " + imaraporDTO.getUraltgrp() +
				" AND Hareket = 'G' " +
				" AND Stok.Urun_Kodu = MAL.Kodu  " +
				" AND STOK.Evrak_No >= '" + imaraporDTO.getEvrno1() + "' AND  STOK.Evrak_No <= '" + imaraporDTO.getEvrno2() + "'" +
				" AND Tarih >= '" + imaraporDTO.getTar1() + "' AND  Tarih <= '" + imaraporDTO.getTar2() + " 23:59:59.998'" +
				" AND STOK.Urun_Kodu >= N'" + imaraporDTO.getUkod1() + "' AND  STOK.Urun_Kodu <= N'" + imaraporDTO.getUkod2() + "' " +
				" AND Recete >= N'" + imaraporDTO.getRec1() + "' AND Recete <= N'" + imaraporDTO.getRec2() + "' " +
				" AND Barkod >= N'" + imaraporDTO.getBkod1() + "' AND Barkod <= N'" + imaraporDTO.getBkod2() + "' " +
				" AND STOK.Ana_Grup " + imaraporDTO.getAnagrp() +
				" AND STOK.Alt_Grup " + imaraporDTO.getAltgrp() +
				" AND STOK.Depo " + imaraporDTO.getDepo() +
				" ORDER BY Evrak_No ";
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
	public List<Map<String, Object>> envanter_rapor(envanterDTO envanterDTO,
			ConnectionDetails faturaConnDetails) {

		String calisanpara= envanterDTO.getDoviz() ;
		String wee  = "" ;
		if (envanterDTO.isDepohardahil())
			wee = " Like '%' " ;
		else
			wee = " <> 'DPO' ";
		String ure1 = "";
		if (envanterDTO.isUretfisdahil() )
			ure1 = " Like '%' " ;
		else
			ure1 = " <> 'URE' " ;
		String sql =    " SELECT mal.Kodu As Kodu ,mal.Adi as Adi, mal.Birim as Simge, " +
				"case when mal.Kusurat = 0 then Format( ISNULL((SELECT sum(Miktar) FROM STOK d " +
				" WHERE  d.Urun_Kodu=mal.kodu AND d.Hareket='G' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.999'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo() +
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ) ,0),'N0') " + // N0
				" when mal.Kusurat = 1 then Format( ISNULL((SELECT sum(Miktar) FROM STOK d " +
				" WHERE  d.Urun_Kodu=mal.kodu And d.Hareket='G' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.999'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ) ,0),'N1') " + // N1
				" when mal.Kusurat = 2 then Format( ISNULL((SELECT sum(Miktar) FROM STOK d " +
				" WHERE  d.Urun_Kodu=mal.kodu And d.Hareket='G' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.999'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ) ,0),'N2') " + // N2
				" ELSE Format( ISNULL((SELECT sum(Miktar) FROM STOK d " +
				" WHERE  d.Urun_Kodu=mal.kodu And d.Hareket='G' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.999'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ) ,0),'N3') " +
				" End  As Giris_Miktari  , " +
				" ISNULL((Select  sum(Tutar) " +
				" FROM STOK d " +
				" WHERE  d.Urun_Kodu=mal.kodu And  d.Hareket='G' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.999'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0) as Giris_Tutar, " +
				" CASE WHEN kusurat = 0 THEN format(ISNULL((SELECT sum(abs(miktar)) FROM STOK d " +
				" WHERE  d.Urun_Kodu=mal.kodu AND  d.Hareket ='C' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.999'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0),'N0') " +
				" WHEN kusurat = 1 THEN format(ISNULL((SELECT sum(abs(miktar)) FROM STOK d " +
				" WHERE  d.Urun_Kodu=mal.kodu And  d.Hareket ='C' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.999'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0),'N1') " +
				" WHEN kusurat = 2 THEN format(ISNULL((SELECT sum(abs(miktar)) FROM STOK d " +
				" WHERE  d.Urun_Kodu=mal.kodu And  d.Hareket ='C' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.999'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0),'N2') " +
				" ELSE format(ISNULL((SELECT sum(abs(miktar)) FROM STOK d " +
				" WHERE  d.Urun_Kodu=mal.kodu And  d.Hareket ='C' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0),'N3') " +
				" End As Cikis_Miktari , " +
				" ISNULL((Select sum(iif(d.Doviz = '" + calisanpara + "',abs(Tutar),abs(Tutar)* d.Kur))  " + 
				" FROM STOK d " +  
				" WHERE  d.Urun_Kodu=mal.kodu And  d.Hareket='C' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0) as Cikis_Tutar, " +
				" ISNULL((SELECT sum(abs(miktar)) FROM STOK d " +
				" WHERE  d.Urun_Kodu=mal.kodu AND  d.Hareket ='C' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0) *  " +
				" ISNULL((SELECT  sum(Tutar)/ sum(iif(miktar=0 ,1,miktar)) " +
				" FROM STOK d " +
				" WHERE  d.Urun_Kodu=mal.kodu AND  d.Hareket='G' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0) as Cikis_Maliyet, " +
				"" + 
				"  CASE WHEN kusurat = 0 THEN format( ISNULL((SELECT ROUND(SUM(CASE when Hareket = 'G' then miktar else 0 END - CASE when Hareket= 'C' then abs(miktar) else 0 END),3) " +
				" FROM STOK d WHERE  d.Urun_Kodu=mal.kodu " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0),'N0') " +
				" WHEN kusurat = 1 THEN format( ISNULL((SELECT ROUND(SUM(CASE when Hareket = 'G' then miktar else 0 END - CASE when Hareket= 'C' then abs(miktar) else 0 END),3) " +
				" FROM STOK d WHERE  d.Urun_Kodu=mal.kodu " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0),'N1') " +
				" WHEN kusurat = 2 THEN format( ISNULL((SELECT ROUND(SUM(CASE when Hareket = 'G' then miktar else 0 END - CASE when Hareket= 'C' then abs(miktar) else 0 END),3) " +
				" FROM STOK d WHERE  d.Urun_Kodu=mal.kodu " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0),'N2') " +
				" ELSE format( ISNULL((SELECT ROUND(SUM(CASE when Hareket = 'G' then miktar else 0 END - CASE when Hareket= 'C' then abs(miktar) else 0 END),3) " +
				" FROM STOK d WHERE  d.Urun_Kodu=mal.kodu " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0),'N3') " +
				" End  As Stok_Miktari, " +
				" ISNULL((Select  sum(Tutar)/ sum(iif(miktar=0 ,1,miktar)) " +
				" FROM STOK d " +
				" WHERE  d.Urun_Kodu=mal.kodu And  d.Hareket='G' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0) as Maliyet, " +
				" ISNULL(((ISNULL((SELECT sum(miktar) FROM STOK d " +
				"  WHERE  d.Urun_Kodu=mal.kodu AND  d.Hareket='G' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ) ,0) )  - ( ISNULL((SELECT sum(abs(miktar)) FROM STOK d  " +
				" WHERE  d.Urun_Kodu=mal.kodu AND  d.Hareket='C' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" ),0) ) ) * ((SELECT  sum(Tutar)/ sum(iif(miktar=0 ,1,miktar)) " +
				" FROM STOK d " +
				" WHERE  d.Urun_Kodu=mal.kodu AND  d.Hareket='G' " +
				" AND d.Tarih >= '" + envanterDTO.getTar1() + "' AND  d.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.999'" +
				" AND d.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND d.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND d.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND d.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND d.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND d.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND d.Depo " + envanterDTO.getDepo()+
				" AND d.Evrak_Cins " + wee +
				" AND d.Evrak_Cins " + ure1 +
				" )),0) AS Tutar " +
				
				" FROM MAL WITH (INDEX (IX_MAL)) ,STOK WITH (INDEX (IX_STOK)) " +
				" WHERE   Kodu >= N'" + envanterDTO.getUkod1() + "' AND  Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND Stok.Urun_Kodu = MAL.Kodu " +
				" AND MAL.Ana_Grup " + envanterDTO.getUranagrp() +
				" AND MAL.Alt_Grup " + envanterDTO.getUraltgrp() +
				" AND Stok.Tarih >= '" + envanterDTO.getTar1() + "' AND  Stok.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.999'" +
				" AND Stok.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND Stok.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND Stok.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND Stok.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND Stok.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND Stok.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND Stok.Depo " + envanterDTO.getDepo()+
				" AND stok.Evrak_Cins " + wee +
				" AND stok.Evrak_Cins " + ure1 +
				" GROUP BY mal.kodu,mal.barkod,mal.adi,mal.birim,mal.kusurat " +
				" ORDER BY mal.kodu ";		
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
	public List<Map<String, Object>> envanter_rapor_fifo(envanterDTO envanterDTO,
			ConnectionDetails faturaConnDetails) {

		String calisanpara= envanterDTO.getDoviz() ;
		String wee  = "" ;
		if (envanterDTO.isDepohardahil())
			wee = " Like '%' " ;
		else
			wee = " <> 'DPO' ";
		String ure1 = "";
		if (envanterDTO.isUretfisdahil() )
			ure1 = " Like '%' " ;
		else
			ure1 = " <> 'URE' " ;
		String sql = "WITH cteStockSum AS (   " +
				" SELECT " +
				"     stk.[Urun_Kodu]," +  
				"     mal.[Adi], " +
				"     (SELECT [Birim] FROM [MAL] WHERE [Kodu] = stk.[Urun_Kodu]) AS [Simge]," + 
				"     (SELECT SUM([Miktar]) FROM [STOK] WHERE [Hareket] = 'G' AND [Urun_Kodu] = stk.[Urun_Kodu]) AS [Giris_Miktari]," + 
				"     (SELECT SUM([Tutar]) FROM [STOK] WHERE [Hareket] = 'G' AND [Urun_Kodu] = stk.[Urun_Kodu]) AS [Giris_Tutari], " +
				"     COALESCE((SELECT SUM(ABS([Miktar])) FROM [STOK] WHERE [Hareket] = 'C' AND [Urun_Kodu] = stk.[Urun_Kodu]), 0) AS [Cikis_Miktari]," + 
				"     COALESCE((SELECT SUM(CASE WHEN [Doviz] = '" + calisanpara + "' THEN [Tutar] ELSE ([Tutar] * [Kur]) END)" +
				"               FROM [STOK] " +
				"               WHERE [Hareket] = 'C' AND [Urun_Kodu] = stk.[Urun_Kodu]), 0) AS [Cikis_Tutari]," + 
				"     COALESCE((" +
				"         SELECT cum_sold_cost FROM (" +
				"             SELECT " +
				"                 tneg.[Urun_Kodu]," +
				"                 tneg.[Tarih]," +
				"                 tpos.prev_total_cost + ((tneg.cum_sold - tpos.prev_bought) / " + 
				"                 (tpos.qty_bought - tpos.prev_bought)) * (tpos.total_cost - tpos.prev_total_cost) AS cum_sold_cost " +  
				"             FROM ( " +
				"                 SELECT " + 
				"                     [Urun_Kodu]," + 
				"                     [Tarih], " +
				"                     ABS([Miktar]) AS qty_sold," +
				"                     SUM(ABS([Miktar])) OVER (PARTITION BY [Urun_Kodu] ORDER BY [Tarih]) AS cum_sold" +  
				"                 FROM [STOK] " +
				"                 WHERE [STOK].[Urun_Kodu] = stk.[Urun_Kodu] AND [Miktar] < 0 " +
				"             ) tneg " +
				"             LEFT JOIN (" +
				"                 SELECT " +
				"                     [Urun_Kodu]," + 
				"                     SUM([Miktar]) OVER (PARTITION BY [Urun_Kodu] ORDER BY [Tarih]) AS qty_bought," +
				"                     COALESCE(SUM([Miktar]) OVER (PARTITION BY [Urun_Kodu] ORDER BY [Tarih] ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING), 0) AS prev_bought," + 
				"                     [Miktar] * [Fiat] AS cost," +
				"                     SUM([Miktar] * [Fiat]) OVER (PARTITION BY [Urun_Kodu] ORDER BY [Tarih]) AS total_cost," +
				"                     COALESCE(SUM([Miktar] * [Fiat]) OVER (PARTITION BY [Urun_Kodu] ORDER BY [Tarih] ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING), 0) AS prev_total_cost" +  
				"                 FROM [STOK]  " +
				"                 WHERE [STOK].[Urun_Kodu] = stk.[Urun_Kodu] AND [Miktar] > 0 " +
				"             ) tpos " +
				"             ON tneg.cum_sold BETWEEN tpos.prev_bought AND tpos.qty_bought " + 
				"             AND tneg.[Urun_Kodu] = tpos.[Urun_Kodu] " +
				"         ) t " +
				"         ORDER BY [Tarih] DESC " + 
				"         OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY " +
				"     ), 0) AS [Cikis_Maliyet], " +
				"     SUM([Miktar]) AS [Stok_Miktari] " +
				" FROM [STOK] stk " +
				" JOIN [MAL] mal ON stk.[Urun_Kodu] = mal.[Kodu] " +
				" WHERE " +
				" stk . Tarih  BETWEEN  '" + envanterDTO.getTar1() + "' AND   '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND  Urun_Kodu  = N'" + envanterDTO.getUkod1() + "'" +
				" AND  Evrak_No  >= '" + envanterDTO.getEvrno1() + "' AND  Evrak_No  <= '" + envanterDTO.getEvrno2() + "' " +
				" AND  stk . Ana_Grup   " + envanterDTO.getAnagrp() +
				" AND  MAL . Ana_Grup    " + envanterDTO.getUranagrp() + " " +
				" AND  MAL . Alt_Grup    " + envanterDTO.getUraltgrp() + " " +
				" AND  stk . Alt_Grup   " + envanterDTO.getAltgrp() +
				" AND  stk . Depo   " + envanterDTO.getDepo() +
				" AND  Evrak_Cins  " + wee +
				" AND  Evrak_Cins   " + ure1 +
				" GROUP BY stk.[Urun_Kodu], mal.[Adi] " +
				" )," +
				" cteReverseInSum AS ( " +
				" SELECT " +
				"     s.[Urun_Kodu]," + 
				"     s.[Tarih], " +
				"     SUM(s.[Miktar]) OVER (PARTITION BY [Urun_Kodu] ORDER BY s.[Tarih] ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING) AS RollingStock, " +
				"     s.[Miktar] AS ThisStock " +
				" FROM [STOK] s " +
				" WHERE s.[Miktar] > 0 " +
				" ), " +
				" cteWithLastTranDate AS ( " +
				" SELECT DISTINCT " +
				"     w.[Urun_Kodu], " +
				"     w.[Adi], " +
				"     w.[Simge], " +
				"     w.[Giris_Miktari]," +
				"     w.[Giris_Tutari]," +
				"     w.[Cikis_Miktari]," +
				"     w.[Cikis_Tutari]," +
				"     w.[Cikis_Maliyet]," +
				"     w.[Stok_Miktari]," +
				"     LAST_VALUE(z.[Tarih]) OVER (PARTITION BY w.[Urun_Kodu] ORDER BY z.[Tarih] ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING) AS [Tarih] " +
				" FROM cteStockSum w " +
				" JOIN cteReverseInSum z ON w.[Urun_Kodu] = z.[Urun_Kodu] AND z.RollingStock >= w.[Stok_Miktari] " +
				" )" +
				" SELECT " + 
				" y.[Urun_Kodu], " +
				" e.[Adi]," +
				" e.[Simge]," +
				" e.[Giris_Miktari], " +
				" e.[Giris_Tutari]," +
				" e.[Cikis_Miktari], " +
				" e.[Cikis_Tutari]," +
				" e.[Cikis_Maliyet]," +
				" e.[Stok_Miktari]," +
				" Price.[Fiat] AS [Maliyet]," +
				" SUM(CASE WHEN e.[Tarih] = y.[Tarih] THEN e.[Stok_Miktari] - (y.RollingStock - y.ThisStock) ELSE y.ThisStock END * Price.[Fiat]) AS [Tutar] " +
				" FROM cteReverseInSum y " +
				" JOIN cteWithLastTranDate e ON e.[Urun_Kodu] = y.[Urun_Kodu]" +
				" CROSS APPLY ( " +
				"  SELECT TOP 1 p.[Fiat] " + 
				"  FROM [STOK] p " +
				"  WHERE p.[Urun_Kodu] = e.[Urun_Kodu] AND p.[Tarih] <= e.[Tarih] AND p.[Miktar] > 0 " +
				"  ORDER BY p.[Tarih] DESC  " +
				" ) AS Price " +
				" WHERE y.[Tarih] >= e.[Tarih]" +
				" GROUP BY y.[Urun_Kodu], e.[Adi], e.[Simge], e.[Giris_Miktari], e.[Giris_Tutari], e.[Cikis_Miktari], e.[Cikis_Tutari], e.[Cikis_Maliyet], Price.[Fiat], e.[Stok_Miktari] " +
				" ORDER BY y.[Urun_Kodu];" ;
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
	public List<Map<String, Object>> envanter_rapor_fifo_2(envanterDTO envanterDTO,
			ConnectionDetails faturaConnDetails) {
		String calisanpara= envanterDTO.getDoviz() ;
		String wee  = "" ;
		if (envanterDTO.isDepohardahil())
			wee = " Like '%' " ;
		else
			wee = " <> 'DPO' ";
		String ure1 = "";
		if (envanterDTO.isUretfisdahil() )
			ure1 = " Like '%' " ;
		else
			ure1 = " <> 'URE' " ;
		String sql = " SELECT Urun_Kodu ,Evrak_No , " +
				" iif(STOK.Evrak_Cins= 'URE','',Hesap_Kodu), " +
				" Evrak_Cins,Tarih ,Miktar ,  Birim , STOK.Fiat , " +
				" SUM(Miktar) OVER(ORDER BY Tarih  ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) as Miktar_Bakiye , " +
				" Tutar ,Doviz," +
				" iif(Doviz = '" + calisanpara + "',Tutar,(Tutar * Kur)) as " + calisanpara + "_Tutar ," +
				" SUM(iif(Doviz = '" + calisanpara + "',Tutar,(Tutar * Kur))) OVER(ORDER BY Tarih  ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) as Tutar_Bakiye , " +
				" stok.[USER],MAL.Kusurat " +
				" FROM STOK ,MAL " +
				" WHERE  mal.kodu = stok.Urun_Kodu " +
				" AND MAL.Ana_Grup " + envanterDTO.getUranagrp() +
				" AND MAL.Alt_Grup " + envanterDTO.getUraltgrp() +
				" AND STOK.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND  STOK.Evrak_No <= '" + envanterDTO.getEvrno2() + "'" +
				" AND STOK.Tarih BETWEEN '" + envanterDTO.getTar1() + "' AND  '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND STOK.Urun_Kodu = N'" + envanterDTO.getUkod1() + "'" +
				" AND STOK.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND STOK.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND STOK.Depo " + envanterDTO.getDepo() +
				" AND Evrak_Cins " + wee +
				" AND Evrak_Cins " + ure1 +
				" Order by Tarih " ;
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
	public double envanter_rapor_lifo(envanterDTO envanterDTO, ConnectionDetails faturaConnDetails) {

		double maliyet = 0 ;
		String wee  = "" ;
		if (envanterDTO.isDepohardahil())
			wee = " Like '%' " ;
		else
			wee = " <> 'DPO' ";
		String ure1 = "";
		if (envanterDTO.isUretfisdahil() )
			ure1 = " Like '%' " ;
		else
			ure1 = " <> 'URE' " ;
		String sql = "WITH InventoryRanked AS (			" +
				" SELECT " +
				"    STOK.Urun_Kodu, " +
				"    STOK.Hareket," +
				"    STOK.Miktar," +
				"    STOK.Fiat," +
				"    STOK.Tarih," +
				"    ROW_NUMBER() OVER (PARTITION BY STOK.Urun_Kodu ORDER BY STOK.Tarih DESC) AS rn " +
				" FROM " +
				"    STOK " +
				"    INNER JOIN MAL ON MAL.Kodu = STOK.Urun_Kodu " +
				" WHERE  " +
				"   STOK.Tarih  BETWEEN  '" + envanterDTO.getTar1() + "' AND   '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" 	AND  Urun_Kodu  = N'" + envanterDTO.getUkod1() + "'" +
				" 	AND  Evrak_No  >= '" + envanterDTO.getEvrno1() + "' AND  Evrak_No  <= '" + envanterDTO.getEvrno2() + "' " +
				" 	AND  STOK . Ana_Grup  " + envanterDTO.getAnagrp() +
				" 	AND  MAL . Ana_Grup   " + envanterDTO.getUranagrp() + " " +
				" 	AND  MAL . Alt_Grup   " + envanterDTO.getUraltgrp() + " " +
				" 	AND  STOK . Alt_Grup  " + envanterDTO.getAltgrp() +
				" 	AND  STOK . Depo  " + envanterDTO.getDepo() +
				" 	AND  Evrak_Cins  " + wee +
				" 	AND  Evrak_Cins  " + ure1 +
				" 	), " +
				" StockCalculation AS ( " +
				" SELECT " +
				"    Urun_Kodu," +
				"    Hareket," +
				"    Miktar," +
				"    Fiat," +
				"    Tarih," +
				"    ROW_NUMBER() OVER (PARTITION BY Urun_Kodu ORDER BY Tarih DESC) AS qwe, " +
				"    SUM(CASE WHEN Hareket = 'G' THEN Miktar ELSE Miktar END) " +
				"        OVER (PARTITION BY Urun_Kodu ORDER BY rn) AS kalan_stok," +
				"    CASE " +
				"        WHEN Hareket = 'G' THEN Miktar * Fiat " +
				"        ELSE Miktar * ( " +
				"            SELECT TOP 1 Fiat " + 
				"            FROM STOK AS inv " +
				"            WHERE inv.Urun_Kodu = InventoryRanked.Urun_Kodu " + 
				"            AND Hareket = 'G' " +
				"            ORDER BY inv.Tarih DESC " +
				"        ) " +
				"    END AS transaction_value " +
				" FROM InventoryRanked " +
				" ) " +
				" SELECT TOP 1 " +
				" Urun_Kodu, " +
				" SUM(transaction_value) OVER (PARTITION BY Urun_Kodu ORDER BY qwe) / kalan_stok AS maliyet " +
				" FROM StockCalculation " +
				" ORDER BY Tarih desc;" ;
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				maliyet = resultSet.getDouble("maliyet");   
			}
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return maliyet; 
	}

	@Override
	public List<Map<String, Object>> envanter_rapor_u_kodu_oncekitarih(envanterDTO envanterDTO,
			ConnectionDetails faturaConnDetails) {
		String wee  = "" ;
		if (envanterDTO.isDepohardahil())
			wee = " Like '%' " ;
		else
			wee = " <> 'DPO' ";
		String ure1 = "";
		if (envanterDTO.isUretfisdahil() )
			ure1 = " Like '%' " ;
		else
			ure1 = " <> 'URE' " ;
		String sql =   " SELECT s.Urun_Kodu as Kodu  , mal.Adi ,Mal.Birim as Simge, " + 
				" ISNULL(((SELECT SUM(iif( Hareket = 'G' , miktar , 0 ) * mal.Agirlik )   FROM [STOK] WITH (INDEX (IX_STOK)) ,[MAL] WITH (INDEX (IX_MAL)) " +
				" where stok.Urun_Kodu = s.Urun_Kodu and  mal.Kodu = stok.Urun_Kodu " +
				" AND Stok.Tarih < '" + envanterDTO.getTar1() + "' " +
				" Group by STOK.Urun_Kodu, Mal.Adi ,Mal.Birim " +
				" ) - " +
				" (SELECT SUM(iif( Hareket = 'C' , abs(miktar) , 0 ) * mal.Agirlik )   FROM [STOK] WITH (INDEX (IX_STOK)) ,[MAL] WITH (INDEX (IX_MAL))  " +
				" where stok.Urun_Kodu = s.Urun_Kodu and  mal.Kodu = stok.Urun_Kodu " +
				" AND Stok.Tarih < '" + envanterDTO.getTar1() + "' " +
				" Group by STOK.Urun_Kodu, Mal.Adi ,Mal.Birim " +
				" )),0) as Onceki_Bakiye ," +  // Onceki_Bakiye
				" ISNULL((SELECT SUM(iif( Hareket = 'G' , miktar , 0 ) * mal.Agirlik )   FROM [STOK] WITH (INDEX (IX_STOK)) ,[MAL] WITH (INDEX (IX_MAL)) " +
				" where stok.Urun_Kodu = s.Urun_Kodu and  mal.Kodu = stok.Urun_Kodu " +
				" AND Stok.Tarih >= '" + envanterDTO.getTar1() + "' AND  Stok.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" Group by STOK.Urun_Kodu, Mal.Adi ,Mal.Birim ),0) as Periyot_Giris_Agirlik," + // Periyot_Giris_Agirlik
				" ISNULL((SELECT SUM(iif( Hareket = 'C' ,abs( miktar) , 0 ) * mal.Agirlik )   FROM [STOK] WITH (INDEX (IX_STOK)) ,[MAL] WITH (INDEX (IX_MAL)) " +
				" where stok.Urun_Kodu = s.Urun_Kodu and  mal.Kodu = stok.Urun_Kodu " +
				" AND Stok.Tarih >= '" + envanterDTO.getTar1() + "' AND  Stok.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" Group by STOK.Urun_Kodu, Mal.Adi ,Mal.Birim ),0) as Periyot_Cikis_Agirlik, " + // Periyot_Cikis_Agirlik
				//
				" ISNULL((SELECT SUM(iif( Hareket = 'G' , miktar , 0 ) * mal.Agirlik )   FROM [STOK] WITH (INDEX (IX_STOK)) ,[MAL] WITH (INDEX (IX_MAL)) " +
				" where stok.Urun_Kodu = s.Urun_Kodu and  mal.Kodu = stok.Urun_Kodu " +
				" And   Kodu >= N'" + envanterDTO.getUkod1() + "' AND  Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND Stok.Tarih >= '" + envanterDTO.getTar1() + "' AND  Stok.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" Group by STOK.Urun_Kodu, Mal.Adi ,Mal.Birim ) - " +
				" (SELECT SUM(iif( Hareket = 'C' ,abs( miktar) , 0 ) * mal.Agirlik )   FROM [STOK] WITH (INDEX (IX_STOK)) ,[MAL] WITH (INDEX (IX_MAL)) " +
				" where stok.Urun_Kodu = s.Urun_Kodu and  mal.Kodu = stok.Urun_Kodu " +
				" AND Stok.Tarih >= '" + envanterDTO.getTar1() + "' AND  Stok.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" Group by STOK.Urun_Kodu, Mal.Adi ,Mal.Birim ),0)  as Periyot_Stok_Agirlik , " +
				//
				//" SUM(iif( Hareket = 'G' ,miktar, 0 )* mal.Agirlik) -  SUM(iif(Hareket = 'C',abs( miktar), 0 )* mal.Agirlik)     as Periyot_Stok_Agirlik , " + //Periyot_Stok_Agirlik
				" ISNULL(((SELECT SUM(iif( Hareket = 'G' , miktar , 0 ) * mal.Agirlik )   " +
				" FROM [STOK] WITH (INDEX (IX_STOK)) ,[MAL] WITH (INDEX (IX_MAL)) "+ 
				" where stok.Urun_Kodu = s.Urun_Kodu and  mal.Kodu = stok.Urun_Kodu  " +
				" AND Stok.Tarih < '" + envanterDTO.getTar1() + "'   Group by STOK.Urun_Kodu, Mal.Adi ,Mal.Birim  ) -  (SELECT SUM(iif( Hareket = 'C' , abs(miktar) , 0 ) * mal.Agirlik )   "+
				" FROM [STOK] WITH (INDEX (IX_STOK)) ,[MAL] WITH (INDEX (IX_MAL))   where stok.Urun_Kodu = s.Urun_Kodu and  mal.Kodu = stok.Urun_Kodu "+
				" AND Stok.Tarih < '" + envanterDTO.getTar1() + "'   Group by STOK.Urun_Kodu, Mal.Adi ,Mal.Birim  ) +  " +
				" ISNULL((SELECT SUM(iif( Hareket = 'G' , miktar , 0 ) * mal.Agirlik )   FROM [STOK] WITH (INDEX (IX_STOK)) ,[MAL] WITH (INDEX (IX_MAL)) " + 
				" where stok.Urun_Kodu = s.Urun_Kodu and  mal.Kodu = stok.Urun_Kodu  AND " + 
				" Stok.Tarih >= '" + envanterDTO.getTar1() + "' AND  Stok.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'  " +
				" Group by STOK.Urun_Kodu, Mal.Adi ,Mal.Birim ) -  " +
				" (SELECT SUM(iif( Hareket = 'C' ,abs( miktar) , 0 ) * mal.Agirlik )   FROM [STOK] WITH (INDEX (IX_STOK)) ,[MAL] WITH (INDEX (IX_MAL)) " + 
				" where stok.Urun_Kodu = s.Urun_Kodu and  mal.Kodu = stok.Urun_Kodu  AND " + 
				" Stok.Tarih >= '" + envanterDTO.getTar1() + "' AND  Stok.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998' "+ 
				" Group by STOK.Urun_Kodu, Mal.Adi ,Mal.Birim ) " +
				" ,0)),0) as BAKIYE " +  
				//" SUM(iif( Hareket = 'G' ,miktar, 0 )* mal.Agirlik) -  SUM(iif(Hareket = 'C',abs( miktar), 0 )* mal.Agirlik)),0) as BAKIYE " +
				" From [STOK] s WITH (INDEX (IX_STOK)) ,[MAL] WITH (INDEX (IX_MAL)) " +
				" where mal.Kodu = s.Urun_Kodu " +
				" And   Kodu >= N'" + envanterDTO.getUkod1() + "' AND  Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND Mal.Ana_Grup " + envanterDTO.getUranagrp() +
				" AND Mal.Alt_Grup " + envanterDTO.getUraltgrp() +
				" AND s.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND s.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND s.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND s.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND s.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND s.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND s.Depo " + envanterDTO.getDepo() +
				" AND s.Evrak_Cins " + wee +
				" AND s.Evrak_Cins " + ure1 +
				" Group by s.Urun_Kodu, Mal.Adi ,Mal.Birim " +
				" ORDER by s.Urun_Kodu, Mal.Adi ,Mal.Birim ";
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
	public List<Map<String, Object>> envanter_rapor_u_kodu(envanterDTO envanterDTO,
			ConnectionDetails faturaConnDetails) {
		String wee  = "" ;
		if (envanterDTO.isDepohardahil())
			wee = " Like '%' " ;
		else
			wee = " <> 'DPO' ";
		String ure1 = "";
		if (envanterDTO.isUretfisdahil() )
			ure1 = " Like '%' " ;
		else
			ure1 = " <> 'URE' " ;
		String sql =   " SELECT STOK.Urun_Kodu as Kodu  , mal.Adi ,Mal.Birim as Simge, SUM(iif( Hareket = 'G' ,miktar, 0 )) as Giris_Miktari," +
				" SUM(iif( Hareket = 'G' , miktar , 0 ) * mal.Agirlik )  as Giris_Agirlik," +
				" SUM(iif(Hareket = 'C', abs(miktar), 0 )) as Cikis_Miktari, " +
				" SUM(iif(Hareket = 'C', abs(miktar), 0 ) * MAL.Agirlik)  as Cikis_Agirlik, " +
				" SUM(iif( Hareket = 'G' ,miktar, 0 )) -  SUM(iif(Hareket = 'C',abs( miktar), 0 )) as Stok_Miktari," +
				" SUM(iif( Hareket = 'G' ,miktar, 0 )* mal.Agirlik) -  SUM(iif(Hareket = 'C',abs( miktar), 0 )* mal.Agirlik)     as Stok_Agirlik  " +
				" From [STOK] WITH (INDEX (IX_STOK)) ,[MAL] WITH (INDEX (IX_MAL)) " +
				" where mal.Kodu = stok.Urun_Kodu " +
				" And   Kodu >= N'" + envanterDTO.getUkod1() + "' AND  Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND Mal.Ana_Grup " + envanterDTO.getUranagrp() +
				" AND Mal.Alt_Grup " + envanterDTO.getUraltgrp() +
				" AND Stok.Tarih >= '" + envanterDTO.getTar1() + "' AND  Stok.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND Stok.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND Stok.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND Stok.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND Stok.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND Stok.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND Stok.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND Stok.Depo " + envanterDTO.getDepo() +
				" AND Stok.Evrak_Cins " + wee +
				" AND Stok.Evrak_Cins " + ure1 +
				" Group by STOK.Urun_Kodu, Mal.Adi ,Mal.Birim " +
				" ORDER by STOK.Urun_Kodu, Mal.Adi ,Mal.Birim ";
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
	public List<Map<String, Object>> envanter_rapor_ana_grup_alt_grup(envanterDTO envanterDTO,
			ConnectionDetails faturaConnDetails) {
		String wee  = "" ;
		if (envanterDTO.isDepohardahil())
			wee = " Like '%' " ;
		else
			wee = " <> 'DPO' ";
		String ure1 = "";
		if (envanterDTO.isUretfisdahil() )
			ure1 = " Like '%' " ;
		else
			ure1 = " <> 'URE' " ;
		String sql =   " SELECT anaDegisken.ANA_GRUP ,  altDegisken.ALT_GRUP, " +
				" SUM(t.Giris_Miktar) as Giris_Miktar,  " +
				" SUM(t.Giris_Agirlik ) as Giris_Agirlik,  " +
				" SUM(t.Cikis_Miktar) as Cikis_Miktar, " +
				" SUM(t.Cikis_Agirlik ) as Cikis_Agirlik, " +
				" SUM(t.Giris_Miktar - abs(t.Cikis_Miktar)) as Stok_Miktar, " +
				" SUM(t.Stok_Agirlik) as Stok_Agirlik " +
				" FROM " +
				" ((SELECT m.KODU,  " +
				" SUM(CASE WHEN s.Hareket = 'G' THEN s.miktar ELSE 0 END) as Giris_Miktar, " +
				" SUM(CASE WHEN s.Hareket = 'G' THEN s.miktar ELSE 0 END) * m.Agirlik  as Giris_Agirlik, " +
				" SUM(CASE WHEN s.Hareket = 'C' THEN abs(s.miktar) ELSE 0 END) as Cikis_Miktar, " +
				" SUM(CASE WHEN s.Hareket = 'C' THEN abs(s.miktar) ELSE 0 END) * m.Agirlik  as Cikis_Agirlik, " +
				" (SUM(CASE WHEN s.Hareket = 'G' THEN s.miktar ELSE 0 END) -  SUM(CASE WHEN s.Hareket = 'C' THEN abs(s.miktar) ELSE 0 END)) * m.Agirlik as Stok_Agirlik, " +
				" m.Ana_Grup,   m.Alt_Grup " +
				" From MAL m LEFT OUTER JOIN STOK s ON m.Kodu = s.Urun_Kodu " +
				" WHERE   Kodu >= N'" + envanterDTO.getUkod1() + "' AND  Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND m.Ana_Grup " + envanterDTO.getUranagrp() +
				" AND m.Alt_Grup " + envanterDTO.getUraltgrp() +
				" AND s.Tarih >= '" + envanterDTO.getTar1() + "' AND  s.Tarih <= '" + envanterDTO.getTar2() + " 23:59:59.998'" +
				" AND s.Urun_Kodu >= N'" + envanterDTO.getUkod1() + "' AND s.Urun_Kodu <= N'" + envanterDTO.getUkod2() + "' " +
				" AND s.Evrak_No >= '" + envanterDTO.getEvrno1() + "' AND s.Evrak_No <= '" + envanterDTO.getEvrno2() + "' " +
				" AND s.Ana_Grup " + envanterDTO.getAnagrp() +
				" AND s.Alt_Grup " + envanterDTO.getAltgrp() +
				" AND s.Depo " + envanterDTO.getDepo() +
				" AND s.Evrak_Cins " + wee +
				" AND s.Evrak_Cins " + ure1 +
				" Group by m.Kodu, m.Agirlik,m.Ana_Grup, m.Alt_Grup " +
				" ) as t  " +
				" LEFT OUTER JOIN ANA_GRUP_DEGISKEN anaDegisken on t.Ana_Grup = anaDegisken.AGID_Y) " +
				" LEFT OUTER JOIN ALT_GRUP_DEGISKEN altDegisken on t.Alt_Grup = altDegisken.ALID_Y " +
				" Group BY anaDegisken.ANA_GRUP, altDegisken.ALT_GRUP " +
				" ORDER BY ANA_GRUP,ALT_GRUP ";
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
	public List<Map<String, Object>> baslik_bak(String baslik, String ordr, String jkj, String ch1, String k1,
			String k2, String f1, String f2, String t1, String t2, ConnectionDetails faturaConnDetails) {
		String sql =   "SELECT " + baslik + "  FROM STOK  " +
				" WHERE  " +  jkj +
				" AND " + ch1 +
				" AND Urun_Kodu between N'" + k1 + "' and N'" + k2 + "'" +
				" AND Hesap_Kodu between N'" + f1 + "' and N'" + f2 + "'" +
				" AND Tarih BETWEEN '" + t1 + "'" +
				" AND  '"  + t2 + " 23:59:59.998'" +
				" " + ordr + " ";
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
	public List<Map<String, Object>> grp_urn_kodlu(grupraporDTO grupraporDTO, String sstr_2, String sstr_4,
			String kur_dos, String jkj, String ch1, String jkj1, String sstr_5, String sstr_1,
			String[][] ozelgrp, ConnectionDetails faturaConnDetails) {
		String sql =   "SELECT * " +
				" FROM  (SELECT MAL.Kodu as Urun_Kodu, Adi as Urun_Adi , Birim ," + sstr_2 + " as  degisken , " + sstr_4 +
				" FROM STOK " + kur_dos + ",MAL " +
				" WHERE   " + jkj +
				"  AND " + ch1 +
				" AND MAL.Ana_Grup " + grupraporDTO.getUranagrp() +
				" AND MAL.Alt_Grup " + grupraporDTO.getUraltgrp() +
				" AND Mal.Ozel_Kod_1 " + grupraporDTO.getUrozkod() +
				" AND STOK.Urun_Kodu = MAL.Kodu " +
				" AND  MAL.Sinif BETWEEN N'" + grupraporDTO.getSinif1() + "' and N'" + grupraporDTO.getSinif2() + "'" +
				" AND Urun_Kodu between N'" + grupraporDTO.getUkod1() + "' and N'" + grupraporDTO.getUkod2() + "'" +
				" AND Hesap_Kodu BETWEEN N'" + grupraporDTO.getCkod1() + "' and N'" + grupraporDTO.getCkod2() + "'" +
				" AND  STOK.Tarih BETWEEN '" + grupraporDTO.getTar1() + "'" +
				" AND  '" + grupraporDTO.getTar2() + " 23:59:59.998'" +
				"  ) as s  " +
				" PIVOT " +
				" ( " +
				" SUM(" + sstr_5 + ") " +
				" FOR degisken " +
				" IN ( " + sstr_1 + ") " +
				"    ) " +
				" AS p" +
				" ORDER BY Urun_Kodu ";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToListPIVOT(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> grp_urn_kodlu_yil(grupraporDTO grupraporDTO, String sstr_2, String sstr_4,
			String kur_dos, String jkj, String ch1, String jkj1, String sstr_5, String sstr_1, String[][] ozelgrp,
			ConnectionDetails faturaConnDetails) {
		String sql =   "SELECT * " +
				" FROM  (SELECT MAL.Kodu as Urun_Kodu, Adi as Urun_Adi , Birim ," +
				" DATEPART(yyyy,STOK.Tarih) as Yil  , " + sstr_2 + " as  degisken , " + sstr_4 +
				"  FROM STOK " + kur_dos + ",MAL " +
				" WHERE " + jkj +
				"  AND " + ch1 +
				" AND MAL.Ana_Grup " + grupraporDTO.getUranagrp() +
				" AND MAL.Alt_Grup " + grupraporDTO.getUraltgrp() +
				" AND Mal.Ozel_Kod_1 " + grupraporDTO.getUrozkod() +
				" AND STOK.Urun_Kodu = MAL.Kodu " +
				" AND  MAL.Sinif BETWEEN N'" + grupraporDTO.getSinif1() + "' and N'" + grupraporDTO.getSinif2() + "'" +
				" AND Urun_Kodu between N'" + grupraporDTO.getUkod1() + "' and N'" + grupraporDTO.getUkod2() + "'" +
				" AND Hesap_Kodu BETWEEN N'" + grupraporDTO.getCkod1() + "' and N'" + grupraporDTO.getCkod2() + "'" +
				" AND  STOK.Tarih BETWEEN '" + grupraporDTO.getTar1() + "'" +
				" AND  '" + grupraporDTO.getTar2() + " 23:59:59.998'" +
				"  ) as s  " +
				" PIVOT " +
				" ( " +
				" SUM(" + sstr_5 + ") " +
				" FOR degisken " +
				" IN ( " + sstr_1 + ") " +
				"    ) " +
				" AS p" +
				" ORDER BY Urun_Kodu, Yil ";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToListPIVOT(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> grp_mus_kodlu(grupraporDTO grupraporDTO, String sstr_2, String sstr_4,
			String kur_dos, String jkj, String ch1, String jkj1, String sstr_5, String sstr_1, String[][] ozelgrp,
			ConnectionDetails faturaConnDetails, ConnectionDetails cariConnDetails) {
		String sql =   "SELECT * " +
				" FROM  (SELECT Hesap_Kodu  , " + 
				"  (SELECT DISTINCT  UNVAN FROM [OK_Car" +  cariConnDetails.getDatabaseName() + "].[dbo].[HESAP] WHERE hesap.hesap = STOK.Hesap_Kodu  ) as Unvan , " +
				sstr_2 + " as  degisken , " + sstr_4 +
				"  FROM STOK " + kur_dos + ",MAL " +
				" WHERE " + jkj +
				"  AND " + ch1 +
				" AND MAL.Ana_Grup " + grupraporDTO.getUranagrp() +
				" AND MAL.Alt_Grup " + grupraporDTO.getUraltgrp() +
				" AND Mal.Ozel_Kod_1 " + grupraporDTO.getUrozkod() +
				" AND STOK.Urun_Kodu = MAL.Kodu " +
				" AND  MAL.Sinif BETWEEN N'" + grupraporDTO.getSinif1() + "' and N'" + grupraporDTO.getSinif2() + "'" +
				" AND Urun_Kodu between N'" + grupraporDTO.getUkod1() + "' and N'" + grupraporDTO.getUkod2() + "'" +
				" AND Hesap_Kodu BETWEEN N'" + grupraporDTO.getCkod1() + "' and N'" + grupraporDTO.getCkod2() + "'" +
				" AND  STOK.Tarih BETWEEN '" + grupraporDTO.getTar1() + "'" +
				" AND  '" + grupraporDTO.getTar2() + " 23:59:59.998'" +
				"  ) as s  " +
				" PIVOT " +
				" ( " +
				" SUM(" + sstr_5 + ") " +
				" FOR degisken " +
				" IN ( " + sstr_1 + ") " +
				"    ) " +
				" AS p" +
				" ORDER BY Hesap_Kodu ";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToListPIVOT(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> grp_mus_kodlu_yil(grupraporDTO grupraporDTO, String sstr_2, String sstr_4,
			String kur_dos, String jkj, String ch1, String jkj1, String sstr_5, String sstr_1, String[][] ozelgrp,
			ConnectionDetails faturaConnDetails, ConnectionDetails cariConnDetails) {
		String sql =   "SELECT * " +
				" FROM (SELECT  Hesap_Kodu as Musteri_Kodu  ," +
				"  (SELECT DISTINCT  UNVAN FROM [OK_Car" +  cariConnDetails.getDatabaseName() + "].[dbo].[HESAP] WHERE hesap.hesap = STOK.Hesap_Kodu  ) as Unvan  " +
				" ,datepart(yyyy,STOK.Tarih) as Yil  , " + sstr_2 + " as  degisken , " + sstr_4 +
				"  FROM STOK " + kur_dos + ",MAL " +
				" WHERE " + jkj +
				"  AND " + ch1 +
				" AND MAL.Ana_Grup " + grupraporDTO.getUranagrp() +
				" AND MAL.Alt_Grup " + grupraporDTO.getUraltgrp() +
				" AND Mal.Ozel_Kod_1 " + grupraporDTO.getUrozkod() +
				" AND STOK.Urun_Kodu = MAL.Kodu " +
				" AND  MAL.Sinif BETWEEN N'" + grupraporDTO.getSinif1() + "' and N'" + grupraporDTO.getSinif2() + "'" +
				" AND Urun_Kodu between N'" + grupraporDTO.getUkod1() + "' and N'" + grupraporDTO.getUkod2() + "'" +
				" AND Hesap_Kodu BETWEEN N'" + grupraporDTO.getCkod1() + "' and N'" + grupraporDTO.getCkod2() + "'" +
				" AND  STOK.Tarih BETWEEN '" + grupraporDTO.getTar1() + "'" +
				" AND  '" + grupraporDTO.getTar2() + " 23:59:59.998'" +
				"  ) as s  " +
				" PIVOT " +
				" ( " +
				" SUM(" + sstr_5 + ") " +
				" FOR degisken " +
				" IN ( " + sstr_1 + ") " +
				"    ) " +
				" AS p" +
				" ORDER BY Musteri_Kodu, Yil ";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToListPIVOT(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> grp_yil_ay(grupraporDTO grupraporDTO, String sstr_2, String sstr_4, String kur_dos,
			String jkj, String ch1, String jkj1, String sstr_5, String sstr_1, String[][] ozelgrp,
			ConnectionDetails faturaConnDetails) {
		String sql =   "SELECT * " +
				" FROM  (SELECT datepart(yy,STOK.Tarih) as Yil , datepart(mm,STOK.Tarih) as Ay  " +
				" ,  " + sstr_2 + " as  degisken , " + sstr_4 +
				"  FROM STOK " + kur_dos + ",MAL " +
				" WHERE " + jkj +
				"  AND " + ch1 +
				" AND MAL.Ana_Grup " + grupraporDTO.getUranagrp() +
				" AND MAL.Alt_Grup " + grupraporDTO.getUraltgrp() +
				" AND Mal.Ozel_Kod_1 " + grupraporDTO.getUrozkod() +
				" AND STOK.Urun_Kodu = MAL.Kodu " +
				" AND  MAL.Sinif BETWEEN N'" + grupraporDTO.getSinif1() + "' and N'" + grupraporDTO.getSinif2() + "'" +
				" AND Urun_Kodu between N'" + grupraporDTO.getUkod1() + "' and N'" + grupraporDTO.getUkod2() + "'" +
				" AND Hesap_Kodu BETWEEN N'" + grupraporDTO.getCkod1() + "' and N'" + grupraporDTO.getCkod2() + "'" +
				" AND  STOK.Tarih BETWEEN '" + grupraporDTO.getTar1() + "'" +
				" AND  '" + grupraporDTO.getTar2() + " 23:59:59.998'" +
				"  ) as s  " +
				" PIVOT " +
				" ( " +
				" SUM(" + sstr_5 + ") " +
				" FOR degisken " +
				" IN ( " + sstr_1 + ") " +
				"    ) " +
				" AS p" +
				" ORDER BY Yil,Ay ";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToListPIVOT(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> grp_yil(grupraporDTO grupraporDTO, String sstr_2, String sstr_4, String kur_dos,
			String jkj, String ch1, String jkj1, String sstr_5, String sstr_1, String[][] ozelgrp,
			ConnectionDetails faturaConnDetails) {
		String sql =   "SELECT * " +
				" FROM  (SELECT  datepart(yyyy,STOK.Tarih) as Yil , " + sstr_2 + " as  degisken , " + sstr_4 +
				"  FROM STOK " + kur_dos + ",MAL " +
				" WHERE " + jkj +
				"  AND " + ch1 +
				" AND MAL.Ana_Grup " + grupraporDTO.getUranagrp() +
				" AND MAL.Alt_Grup " + grupraporDTO.getUraltgrp() +
				" AND Mal.Ozel_Kod_1 " + grupraporDTO.getUrozkod() +
				" AND STOK.Urun_Kodu = MAL.Kodu " +
				" AND  MAL.Sinif BETWEEN N'" + grupraporDTO.getSinif1() + "' and N'" + grupraporDTO.getSinif2() + "'" +
				" AND Urun_Kodu between N'" + grupraporDTO.getUkod1() + "' and N'" + grupraporDTO.getUkod2() + "'" +
				" AND Hesap_Kodu BETWEEN N'" + grupraporDTO.getCkod1() + "' and N'" + grupraporDTO.getCkod2() + "'" +
				" AND  STOK.Tarih BETWEEN '" + grupraporDTO.getTar1() + "'" +
				" AND  '" + grupraporDTO.getTar2() + " 23:59:59.998'" +
				"  ) as s  " +
				" PIVOT " +
				" ( " +
				" SUM(" + sstr_5 + ") " +
				" FOR degisken " +
				" IN ( " + sstr_1 + ") " +
				"    ) " +
				" AS p" +
				" ORDER BY Yil ";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToListPIVOT(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> grp_ana_grup(grupraporDTO grupraporDTO, String sstr_2, String sstr_4,
			String kur_dos, String jkj, String ch1, String jkj1, String sstr_5, String sstr_1, String[][] ozelgrp,
			ConnectionDetails faturaConnDetails) {
		String sql =   "SELECT * " +
				" FROM  (SELECT  (SELECT DISTINCT  ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = MAL.Ana_Grup ) as Ana_Grup  " +
				" ,(SELECT DISTINCT  ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = MAL.Alt_Grup ) as Alt_Grup, " +
				" " + sstr_2 + " as  degisken , " + sstr_4 +
				"  FROM STOK " + kur_dos + ",MAL " +
				" WHERE " + jkj +
				"  AND " + ch1 +
				" AND MAL.Ana_Grup " + grupraporDTO.getUranagrp() +
				" AND MAL.Alt_Grup " + grupraporDTO.getUraltgrp() +
				" AND Mal.Ozel_Kod_1 " + grupraporDTO.getUrozkod() +
				" AND STOK.Urun_Kodu = MAL.Kodu " +
				" AND  MAL.Sinif BETWEEN N'" + grupraporDTO.getSinif1() + "' and N'" + grupraporDTO.getSinif2() + "'" +
				" AND Urun_Kodu between N'" + grupraporDTO.getUkod1() + "' and N'" + grupraporDTO.getUkod2() + "'" +
				" AND Hesap_Kodu BETWEEN N'" + grupraporDTO.getCkod1() + "' and N'" + grupraporDTO.getCkod2() + "'" +
				" AND  STOK.Tarih BETWEEN '" + grupraporDTO.getTar1() + "'" +
				" AND  '" + grupraporDTO.getTar2() + " 23:59:59.998'" +
				"  ) as s  " +
				" PIVOT " +
				" ( " +
				" SUM(" + sstr_5 + ") " +
				" FOR degisken " +
				" IN ( " + sstr_1 + ") " +
				"    ) " +
				" AS p" +
				" ORDER BY Ana_Grup ,Alt_Grup";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToListPIVOT(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> grp_ana_grup_yil(grupraporDTO grupraporDTO, String sstr_2, String sstr_4,
			String kur_dos, String jkj, String ch1, String jkj1, String sstr_5, String sstr_1, String[][] ozelgrp,
			ConnectionDetails faturaConnDetails) {
		String sql =   "SELECT * " +
				" FROM  (SELECT  (SELECT DISTINCT  ANA_GRUP FROM ANA_GRUP_DEGISKEN WHERE ANA_GRUP_DEGISKEN.AGID_Y = MAL.Ana_Grup ) as Ana_Grup  " +
				" ,(SELECT DISTINCT  ALT_GRUP FROM ALT_GRUP_DEGISKEN WHERE ALT_GRUP_DEGISKEN.ALID_Y = MAL.Alt_Grup ) as Alt_Grup, " +
				" datepart(yyyy,STOK.Tarih) as Yil  , " +
				" " + sstr_2 + " as  degisken , " + sstr_4 +
				"  FROM STOK " + kur_dos + ",MAL " +
				" WHERE " + jkj +
				"  AND " + ch1 +
				" AND MAL.Ana_Grup " + grupraporDTO.getUranagrp() +
				" AND MAL.Alt_Grup " + grupraporDTO.getUraltgrp() +
				" AND Mal.Ozel_Kod_1 " + grupraporDTO.getUrozkod() +
				" AND STOK.Urun_Kodu = MAL.Kodu " +
				" AND  MAL.Sinif BETWEEN N'" + grupraporDTO.getSinif1() + "' and N'" + grupraporDTO.getSinif2() + "'" +
				" AND Urun_Kodu between N'" + grupraporDTO.getUkod1() + "' and N'" + grupraporDTO.getUkod2() + "'" +
				" AND Hesap_Kodu BETWEEN N'" + grupraporDTO.getCkod1() + "' and N'" + grupraporDTO.getCkod2() + "'" +
				" AND  STOK.Tarih BETWEEN '" + grupraporDTO.getTar1() + "'" +
				" AND  '" + grupraporDTO.getTar2() + " 23:59:59.998'" +
				"  ) as s  " +
				" PIVOT " +
				" ( " +
				" SUM(" + sstr_5 + ") " +
				" FOR degisken " +
				" IN ( " + sstr_1 + ") " +
				"    ) " +
				" AS p" +
				" ORDER BY Ana_Grup ,Alt_Grup, Yil";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToListPIVOT(resultSet);  
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> ima_baslik_bak(String bas, String jkj, String ch1, String qwq6, String qwq7,
			String qwq8, String qwq9, String k1, String k2, String t1, String t2, String ordrr,
			ConnectionDetails faturaConnDetails) {
		String sql =  "SELECT "+ bas + "  from STOK ,MAL " +
				" WHERE   " + jkj +
				" AND " + ch1 +
				" AND STOK.Urun_Kodu = MAL.Kodu " +
				" AND MAL.Ana_Grup " + qwq6 +
				" AND MAL.Alt_Grup " + qwq7 +
				" AND STOK.Ana_Grup " + qwq8 +
				" AND STOK.Alt_Grup " + qwq9 +
				" AND Urun_Kodu between N'" + k1 + "' and N'" + k2 + "'" +
				" AND  STOK.Tarih BETWEEN '" + t1 + "'" +
				" AND  '" + t2 + " 23:59:59.998'" +
				"" + ordrr + " ";
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
	public List<Map<String, Object>> ima_alt_kod(String slct, String sstr_5, String sstr_2, String sstr_4, String jkj,
			String ch1, String qwq6, String qwq7, String qwq8, String qwq9, String s1, String s2, String k1, String k2,
			String t1, String t2, String sstr_1, String ordrr, String sstr_55, String[][] ozelgrp,
			ConnectionDetails faturaConnDetails) {
		String sql = "SELECT * " +
				" FROM  (SELECT " + slct + sstr_5  + sstr_2 + " as  degisken , " + sstr_4 +
				" FROM STOK,MAL " +
				" WHERE   " + jkj +
				" AND " + ch1 +
				" AND MAL.Ana_Grup " + qwq6 +
				" AND MAL.Alt_Grup " + qwq7 +
				" AND STOK.Ana_Grup " + qwq8 +
				" AND STOK.Alt_Grup " + qwq9 +
				" AND STOK.Urun_Kodu = MAL.Kodu " +
				" AND  MAL.Sinif BETWEEN N'" + s1 + "' and N'" + s2 + "'" +
				" AND Urun_Kodu between N'" + k1 + "' and N'" + k2 + "'" +
				" AND  STOK.Tarih BETWEEN '" + t1 + "'" +
				" AND  '" + t2 + " 23:59:59.998'" +
				"  ) as s  " +
				" PIVOT " +
				" ( " +
				" SUM(" + sstr_55 + ") " +
				" FOR degisken " +
				" IN ( " + sstr_1 + ") " +
				"    ) " +
				" AS p" +
				" ORDER BY  " + ordrr + " ";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(faturaConnDetails.getJdbcUrl(), faturaConnDetails.getUsername(), faturaConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToListPIVOT(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return resultList; 
	}
}