package com.hamit.obs.repository.kambiyo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.dto.kambiyo.bordrodetayDTO;
import com.hamit.obs.dto.kambiyo.cekraporDTO;
import com.hamit.obs.exception.ServiceException;

@Component
public class KambiyoMsSQL implements IKambiyoDatabase{

	@Override
	public List<Map<String, Object>> ozel_kodlar(String gir_cik,ConnectionDetails kambiyoConnDetails) {
		String sql = "SELECT DISTINCT " + gir_cik + " FROM CEK ORDER BY " + gir_cik;
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException("MS KambiyoService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> banka_sube(String nerden, ConnectionDetails kambiyoConnDetails) {
		String sql = "SELECT DISTINCT " + nerden +
				" FROM CEK" +
				" ORDER BY " + nerden;
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException("MS KambiyoService genel hatası.", e);
		}
		return resultList;
	}

	@Override
	public String kambiyo_firma_adi(ConnectionDetails kambiyoConnDetails) {
		String firmaIsmi = "";
		String query = "SELECT FIRMA_ADI FROM OZEL";
		try (Connection connection = DriverManager.getConnection(kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
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
	public int kam_son_bordro_no_al(String cek_sen, String gir_cik, ConnectionDetails kambiyoConnDetails) {
		int evrakNo = 0;
		String query = "SELECT  MAX(" + gir_cik + ") AS MAX_NO  FROM  " + cek_sen ;
		try (Connection connection =  DriverManager.getConnection(kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					evrakNo = resultSet.getInt("MAX_NO");
				}
			}
		} catch (Exception e) {
			throw new ServiceException("son bordro okunamadı", e); 
		}
		return evrakNo;

	}

	@Override
	public List<Map<String, Object>> bordroOku(String bordroNo, String cek_sen, String gir_cik,
			ConnectionDetails kambiyoConnDetails) {
		String sql = "SELECT * " +
				" FROM " + cek_sen + " WHERE " + gir_cik + " = ? " +
				" ORDER BY Vade ";
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(kambiyoConnDetails.getJdbcUrl(),
				kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword())){
			try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				preparedStatement.setNString(1, bordroNo);
				ResultSet resultSet = preparedStatement.executeQuery();
				resultList = ResultSetConverter.convertToList(resultSet); 
			}
		} catch (Exception e) {
			throw new ServiceException("MS CariService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public String kam_aciklama_oku(String cek_sen, int satir, String bordroNo, String gircik,
			ConnectionDetails kambiyoConnDetails) {
		String aciklama = "";
		String sql = "SELECT ACIKLAMA " +
				" FROM ACIKLAMA " +
				" WHERE EVRAK_NO = ? " +
				" AND SATIR = ? " +
				" AND EVRAK_CINS = ? " +
				" AND Gir_Cik = ? ";
		try (Connection connection = DriverManager.getConnection(
				kambiyoConnDetails.getJdbcUrl(),
				kambiyoConnDetails.getUsername(),
				kambiyoConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, bordroNo);
			preparedStatement.setInt(2, satir);
			preparedStatement.setString(3, cek_sen);
			preparedStatement.setString(4, gircik);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getString("ACIKLAMA");
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Aciklama okunamadı", e); 
		}
		return aciklama;
	}

	@Override
	public void bordro_sil(String bordroNo, String cek_sen, String gir_cik, ConnectionDetails kambiyoConnDetails) {
		String sql = " DELETE " +
				" FROM " + cek_sen + "" +
				" WHERE " + gir_cik + "  = ? " ;
		try (Connection connection = DriverManager.getConnection(
				kambiyoConnDetails.getJdbcUrl(),
				kambiyoConnDetails.getUsername(),
				kambiyoConnDetails.getPassword());
				PreparedStatement deleteStmt = connection.prepareStatement(sql)) {
			deleteStmt.setString(1, bordroNo);
			deleteStmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Bordro silme sırasında bir hata oluştu", e);
		}
	}

	@Override
	public void cek_kayit(bordrodetayDTO bordrodetayDTO, ConnectionDetails kambiyoConnDetails) {
		String sql  = "INSERT INTO CEK ( Cek_No,Vade,Giris_Bordro,Giris_Musteri,Giris_Tarihi,Giris_Ozel_Kod,Cikis_Tarihi " +
				" ,Cikis_Bordro,Cikis_Musteri,Cikis_Ozel_Kod,Banka,Sube,Tutar,Cins,Seri_No " +
				" ,Ilk_Borclu,Cek_Hesap_No,Durum,T_Tarih,[USER])" +
				"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, bordrodetayDTO.getCekNo()); 
			stmt.setString(2, bordrodetayDTO.getVade());
			stmt.setString(3,bordrodetayDTO.getGirisBordro());
			stmt.setString(4, bordrodetayDTO.getGirisMusteri());
			stmt.setString(5, bordrodetayDTO.getGirisTarihi());
			stmt.setString(6, bordrodetayDTO.getGirisOzelKod());
			stmt.setString(7, bordrodetayDTO.getCikisTarihi());
			stmt.setString(8,bordrodetayDTO.getCikisBordro());
			stmt.setString(9, bordrodetayDTO.getCikisMusteri()); 
			stmt.setString(10, bordrodetayDTO.getCikisOzelKod());
			stmt.setString(11, bordrodetayDTO.getBanka());
			stmt.setString(12, bordrodetayDTO.getSube());
			stmt.setDouble(13, bordrodetayDTO.getTutar());
			stmt.setString(14, bordrodetayDTO.getCins());
			stmt.setString(15, bordrodetayDTO.getSeriNo());
			stmt.setString(16, bordrodetayDTO.getIlkBorclu()); 
			stmt.setString(17, bordrodetayDTO.getCekHesapNo()); 
			stmt.setString(18, bordrodetayDTO.getDurum());
			stmt.setString(19, bordrodetayDTO.getTtarih()); 
			stmt.setString(20, bordrodetayDTO.getUser());
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}

	@Override
	public void kam_aciklama_yaz(String cek_sen, int satir, String bordroNo, String aciklama, String gircik,
			ConnectionDetails kambiyoConnDetails) {
		String sql  = "INSERT INTO ACIKLAMA (EVRAK_CINS,SATIR,EVRAK_NO,ACIKLAMA,Gir_Cik) " +
				"VALUES (?,?,?,?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, cek_sen); 
			stmt.setInt(2, satir);
			stmt.setString(3,bordroNo);
			stmt.setString(4, aciklama);
			stmt.setString(5, gircik);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}

	@Override
	public void kam_aciklama_sil(String cek_sen, String bordroNo, String gircik,ConnectionDetails kambiyoConnDetails) {
		String sql = " DELETE  FROM ACIKLAMA " +
				" WHERE EVRAK_CINS = N'" + cek_sen + "'" +
				" AND EVRAK_NO = N'" + bordroNo + "'" +
				" AND Gir_Cik = N'" + gircik + "'" ;
		try (Connection connection = DriverManager.getConnection(
				kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement deleteStmt = connection.prepareStatement(sql)) {
			deleteStmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Silme sırasında bir hata oluştu", e);
		}
	}

	@Override
	public int kam_bordro_no_al(String cins, ConnectionDetails kambiyoConnDetails) {
		int evrakNo = 0;
		String query = "UPDATE EVRAK SET NO = NO + 1 OUTPUT INSERTED.NO WHERE EVRAK = ? ";
		try (Connection connection =  DriverManager.getConnection(kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			preparedStatement.setNString(1, cins);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					evrakNo = resultSet.getInt("NO");
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Yeni Evrak No Alinamadi", e); 
		}
		return evrakNo;
	}

	@Override
	public List<Map<String, Object>> kalan_cek_liste(ConnectionDetails kambiyoConnDetails) {
		String sql = " SELECT Cek_No " +
				" FROM CEK " +
				" WHERE " +
				" Cikis_Bordro = ''" +
				" ORDER BY Cek_No ";
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet);
		} catch (Exception e) {
			throw new ServiceException("MS CariService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public String cek_kontrol(String cekno, ConnectionDetails kambiyoConnDetails) {
		String bordrono = "";
		String sql =  "SELECT Banka, Cek_No, Cikis_Bordro, Cikis_Musteri, Cikis_Tarihi, Cins, Durum, Giris_Bordro, Giris_Musteri, Giris_Tarihi, Ilk_Borclu, " +
				" Seri_No, Sube, T_Tarih, Tutar, Vade, Cek_Hesap_No ,Cikis_Ozel_Kod,Giris_Ozel_Kod " +
				" FROM cek WHERE Cek_No = ? COLLATE SQL_Latin1_General_Cp1_CS_AS";
		try (Connection connection =  DriverManager.getConnection(kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setNString(1, cekno);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					bordrono = resultSet.getString("Giris_Bordro");
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Aciklama okunamadı", e); 
		}
		return bordrono;
	}

	@Override
	public bordrodetayDTO cek_dokum(String cekno, ConnectionDetails kambiyoConnDetails) {
		String sql = "SELECT * FROM CEK WHERE Cek_No = ? COLLATE SQL_Latin1_General_Cp1_CS_AS";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		bordrodetayDTO dto = new bordrodetayDTO();
		try (Connection connection = DriverManager.getConnection(kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, cekno);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.isBeforeFirst()) {
					resultSet.next();
					dto.setCikisBordro(resultSet.getString("Cikis_Bordro"));
					Date tarih = resultSet.getDate("Vade");
					if (tarih != null) {
						dto.setVade(dateFormat.format(tarih));
					}
					dto.setBanka(resultSet.getString("Banka"));
					dto.setSube(resultSet.getString("Sube"));
					dto.setSeriNo(resultSet.getString("Seri_No"));
					dto.setIlkBorclu(resultSet.getString("Ilk_Borclu"));
					dto.setCekHesapNo(resultSet.getString("Cek_Hesap_No"));
					dto.setCins(resultSet.getString("Cins"));
					dto.setTutar(resultSet.getDouble("Tutar"));
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Çek Okuma Hatası" , e);
		}
		return dto; 
	}

	@Override
	public void bordro_cikis_sil(String bordroNo, String cek_sen, ConnectionDetails kambiyoConnDetails) {
		String sql = "UPDATE " + cek_sen + 
				" SET Cikis_Bordro = ?, Cikis_Musteri = ?, Cikis_Tarihi = ? " +
				"WHERE Cikis_Bordro = ?";
		try (Connection connection = DriverManager.getConnection(
				kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement deleteStmt = connection.prepareStatement(sql)) {
			deleteStmt.setString(1, "");
			deleteStmt.setString(2, "");
			deleteStmt.setString(3, "1900-01-01");
			deleteStmt.setString(4, bordroNo);
			deleteStmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}

	@Override
	public void bordro_cikis_yaz(String cek_sen, String ceksencins_where, String cekno, String cmus, String cbor,
			String ctar, String ozkod, ConnectionDetails kambiyoConnDetails) {
		String sql = "UPDATE " + cek_sen + " SET Cikis_Bordro = ?, Cikis_Musteri = ?, Cikis_Tarihi = ?, Cikis_Ozel_Kod = ? " +
				"WHERE " + ceksencins_where + " = ?";
		try (Connection connection = DriverManager.getConnection(
				kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement updateStmt = connection.prepareStatement(sql)) {
			updateStmt.setString(1, cbor);
			updateStmt.setString(2, cmus);
			updateStmt.setString(3, ctar);
			updateStmt.setString(4, ozkod);
			updateStmt.setString(5, cekno);
			updateStmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}

	@Override
	public List<Map<String, Object>> cek_rapor(cekraporDTO cekraporDTO, ConnectionDetails kambiyoConnDetails) {
		String ekstraKosul = "";
	    switch (cekraporDTO.getHangi_tur()) {
	        case "C" -> ekstraKosul = " AND Cikis_Bordro <> '' ";
	        case "M" -> ekstraKosul = " AND Cikis_Bordro = '' ";
	        case "G" -> ekstraKosul = " AND Giris_Bordro <> '' ";
	        default  -> ekstraKosul = "";
	    }
	    StringBuilder sql = new StringBuilder();
	    sql.append("""
	            SELECT  Cek_No,
	                    Vade,
	                    Giris_Bordro,
	                    Giris_Tarihi,
	                    Giris_Musteri,
	                    Banka,
	                    Sube,
	                    Cins,
	                    Tutar,
	                    CASE Durum
	                        WHEN '1' THEN 'Iade'
	                        WHEN '2' THEN 'Protesto'
	                        WHEN '3' THEN 'Tahsil'
	                    END as Durum,
	                    IIF(T_Tarih = '1900.01.01', '',
	                        RIGHT(T_Tarih,2) + '.' + SUBSTRING(CONVERT(nvarchar, T_Tarih),6,2) + '.' + LEFT(T_Tarih,4)
	                    ) as T_Tarih,
	                    Giris_Ozel_Kod,
	                    Cikis_Bordro,
	                    IIF(Cikis_Tarihi = '1900-01-01', '',
	                        RIGHT(Cikis_Tarihi,2) + '.' + SUBSTRING(CONVERT(nvarchar, Cikis_Tarihi),6,2) + '.' + LEFT(Cikis_Tarihi,4)
	                    ) as Cikis_Tarihi,
	                    Cikis_Musteri,
	                    Cikis_Ozel_Kod,
	                    CEK.[USER]
	            FROM CEK
	            WHERE 1 = 1
	              AND Cek_No      >= ?  AND Cek_No      <= ?
	              AND Vade        >= ?  AND Vade        <  ?
	              AND Giris_Bordro>= ?  AND Giris_Bordro<= ?
	              AND Giris_Tarihi>= ?  AND Giris_Tarihi<  ?
	              AND Cikis_Bordro>= ?  AND Cikis_Bordro<= ?
	              AND Cikis_Tarihi>= ?  AND Cikis_Tarihi<  ?
	              AND Giris_Musteri>= ? AND Giris_Musteri<= ?
	              AND Cikis_Musteri>= ? AND Cikis_Musteri<= ?
	              AND Durum       >= ?  AND Durum       <= ?
	              AND T_Tarih     >= ?  AND T_Tarih     <  ?
	              AND Cins        >= ?  AND Cins        <= ?
	              AND Giris_Ozel_Kod LIKE ?
	              AND Cikis_Ozel_Kod LIKE ?
	            """);
	    sql.append(ekstraKosul);
	    sql.append(" ORDER BY Cek_No ");
	    List<Map<String, Object>> resultList = new ArrayList<>();
	    try (Connection connection = DriverManager.getConnection(
	                kambiyoConnDetails.getJdbcUrl(),
	                kambiyoConnDetails.getUsername(),
	                kambiyoConnDetails.getPassword());
	         PreparedStatement ps = connection.prepareStatement(sql.toString())) {
	        Timestamp[] tsVade = Global_Yardimci.rangeDayT2plusDay(cekraporDTO.getVade1(), cekraporDTO.getVade2());
	        Timestamp[] tsGiris = Global_Yardimci.rangeDayT2plusDay(cekraporDTO.getGtar1(), cekraporDTO.getGtar2());
	        Timestamp[] tsCikis = Global_Yardimci.rangeDayT2plusDay(cekraporDTO.getCtar1(), cekraporDTO.getCtar2());
	        Timestamp[] tsTTarih = Global_Yardimci.rangeDayT2plusDay(cekraporDTO.getTtar1(), cekraporDTO.getTtar2());
	        int p = 1;
	        ps.setString(p++, cekraporDTO.getCekno1());
	        ps.setString(p++, cekraporDTO.getCekno2());

	        ps.setTimestamp(p++, tsVade[0]);
	        ps.setTimestamp(p++, tsVade[1]);

	        ps.setString(p++, cekraporDTO.getGbor1());
	        ps.setString(p++, cekraporDTO.getGbor2());

	        ps.setTimestamp(p++, tsGiris[0]);
	        ps.setTimestamp(p++, tsGiris[1]);

	        ps.setString(p++, cekraporDTO.getCbor1());
	        ps.setString(p++, cekraporDTO.getCbor2());

	        ps.setTimestamp(p++, tsCikis[0]);
	        ps.setTimestamp(p++, tsCikis[1]);

	        ps.setString(p++, cekraporDTO.getGhes1());
	        ps.setString(p++, cekraporDTO.getGhes2());

	        ps.setString(p++, cekraporDTO.getChes1());
	        ps.setString(p++, cekraporDTO.getChes2());

	        ps.setString(p++, cekraporDTO.getDurum1());
	        ps.setString(p++, cekraporDTO.getDurum2());

	        ps.setTimestamp(p++, tsTTarih[0]);
	        ps.setTimestamp(p++, tsTTarih[1]);

	        ps.setString(p++, cekraporDTO.getCins1());
	        ps.setString(p++, cekraporDTO.getCins2());

	        ps.setString(p++, cekraporDTO.getGozel()); 
	        ps.setString(p++, cekraporDTO.getCozel());

	        try (ResultSet rs = ps.executeQuery()) {
	            resultList = ResultSetConverter.convertToList(rs);
	        }
	    } catch (Exception e) {
	        throw new ServiceException("MS KambiyoService genel hatası.", e);
	    }
	    return resultList;
	}

	@Override
	public void kambiyo_firma_adi_kayit(String fadi, ConnectionDetails kambiyoConnDetails) {
		String sql = "UPDATE OZEL SET FIRMA_ADI = ? " ;
		try (Connection connection = DriverManager.getConnection(
				kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1,fadi);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}

	@Override
	public bordrodetayDTO cektakipkontrol(String cekno, ConnectionDetails kambiyoConnDetails) {
		bordrodetayDTO dto = new bordrodetayDTO();
		String sql =  "SELECT Banka, Cek_No, Cikis_Bordro, Cikis_Musteri, Cikis_Tarihi, Cins, Durum, Giris_Bordro,"
				+ " Giris_Musteri, Giris_Tarihi, Ilk_Borclu, " +
				" Seri_No, Sube, T_Tarih, Tutar, Vade, Cek_Hesap_No ,Cikis_Ozel_Kod,Giris_Ozel_Kod " +
				" FROM cek WHERE Cek_No = ? COLLATE SQL_Latin1_General_Cp1_CS_AS";
		try (Connection connection =  DriverManager.getConnection(kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setNString(1, cekno);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					dto.setBanka(resultSet.getString("Banka"));
					dto.setCekNo(resultSet.getString("Cek_No"));
					dto.setCikisBordro(resultSet.getString("Cikis_Bordro"));
					dto.setCikisMusteri(resultSet.getString("Cikis_Musteri"));
					dto.setCikisTarihi(resultSet.getString("Cikis_Tarihi"));
					dto.setCins(resultSet.getString("Cins"));
					dto.setDurum(resultSet.getString("Durum"));
					dto.setGirisBordro(resultSet.getString("Giris_Bordro"));
					dto.setGirisMusteri(resultSet.getString("Giris_Musteri"));
					dto.setGirisTarihi(resultSet.getString("Giris_Tarihi"));
					dto.setIlkBorclu(resultSet.getString("Ilk_Borclu"));
					dto.setSeriNo(resultSet.getString("Seri_No"));
					dto.setSube(resultSet.getString("Sube"));
					dto.setTtarih(resultSet.getString("T_Tarih"));
					dto.setTutar(resultSet.getDouble("Tutar"));
					dto.setVade(resultSet.getString("Vade"));
					dto.setCekHesapNo(resultSet.getString("Cek_Hesap_No"));
					dto.setCikisOzelKod(resultSet.getString("Cikis_Ozel_Kod"));
					dto.setGirisOzelKod(resultSet.getString("Giris_Ozel_Kod"));
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Aciklama okunamadı", e); 
		}
		return dto;

	}

	@Override
	public void kam_durum_yaz(String cekno, String ceksen_from, String ceksen_where, String durum, String ttarih,
			ConnectionDetails kambiyoConnDetails) {
		String sql = "UPDATE "+ ceksen_from + " SET Durum = '" + durum + "', T_Tarih = '" + ttarih + "'" + 
				" WHERE " + ceksen_where + "  =? " ;
		try (Connection connection = DriverManager.getConnection(
				kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setNString(1, cekno);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}
}