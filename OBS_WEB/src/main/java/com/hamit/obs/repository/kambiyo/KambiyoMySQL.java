package com.hamit.obs.repository.kambiyo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.dto.kambiyo.bordrodetayDTO;
import com.hamit.obs.dto.kambiyo.cekraporDTO;
import com.hamit.obs.exception.ServiceException;

@Component
public class KambiyoMySQL implements IKambiyoDatabase{

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
				" FROM " + cek_sen + " WHERE " + gir_cik + " = N'" + bordroNo + "'" +
				" ORDER BY Vade ";
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
	public String kam_aciklama_oku(String cek_sen, int satir, String bordroNo, String gircik,
			ConnectionDetails kambiyoConnDetails) {
		String aciklama = "";
		String sql = "SELECT * " +
				" FROM ACIKLAMA " +
				" WHERE EVRAK_NO = N'" + bordroNo + "'" +
				" AND SATIR = '" + satir + "'" +
				" AND EVRAK_CINS = '" + cek_sen + "'" +
				" AND Gir_Cik = '" + gircik + "'";
		try (Connection connection =  DriverManager.getConnection(kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					aciklama = resultSet.getString("ACIKLAMA");
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
				" WHERE " + gir_cik +"  ='" + bordroNo + "'" ;
		try (Connection connection = DriverManager.getConnection(
				kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement deleteStmt = connection.prepareStatement(sql)) {
			deleteStmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
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
		String query = "UPDATE EVRAK SET NO = NO + 1 OUTPUT INSERTED.NO WHERE EVRAK = '" + cins + "'";
		try (Connection connection =  DriverManager.getConnection(kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query)) {
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
		String sql = " SELECT  Cek_No " +
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
				" FROM cek WHERE Cek_No = BINARY '" + cekno + "' ";
		try (Connection connection =  DriverManager.getConnection(kambiyoConnDetails.getJdbcUrl(), kambiyoConnDetails.getUsername(), kambiyoConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
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
		String sql = "SELECT * FROM CEK WHERE Cek_No = BINARY ? ";
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
			deleteStmt.setString(3, "1900-01-01"); // Tarih formatı standart ISO 8601 formatında
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
		String qweString = "" ;
		if(cekraporDTO.getHangi_tur().equals("C"))
			qweString = " AND Cikis_Bordro <> '' " ;
		else if(cekraporDTO.getHangi_tur().equals("M"))
			qweString = " AND Cikis_Bordro = '' " ;
		else if(cekraporDTO.getHangi_tur().equals("G"))
			qweString = " AND Giris_Bordro <> '' " ;

		String sql = " SELECT  Cek_No, Vade, Giris_Bordro,Giris_Tarihi, " +
				" Giris_Musteri, Banka, Sube, Cins, Tutar,  CASE Durum  WHEN '1' THEN 'Iade'  WHEN '2' THEN 'Protesto' WHEN '3' THEN 'Tahsil' END as Durum, " +
				" IF(T_Tarih = '1900.01.01', '',DATE_FORMAT(T_Tarih,  '%d.%m.%Y') ) as T_Tarih, " +
				" Giris_Ozel_Kod ,Cikis_Bordro , " + 
				" IF(Cikis_Tarihi = '1900.01.01', '',DATE_FORMAT(Cikis_Tarihi,  '%d.%m.%Y') ) as Cikis_Tarihi," +
				" Cikis_Musteri, Cikis_Ozel_Kod,CEK.USER " +
				" FROM CEK " +
				" WHERE Cek_No >='" + cekraporDTO.getCekno1() + "' AND Cek_No <='" + cekraporDTO.getCekno2() + "'" +
				" AND Vade >='" + cekraporDTO.getVade1() + "' AND Vade <='" + cekraporDTO.getVade2() + "'" +
				" AND Giris_Bordro >='" + cekraporDTO.getGbor1() + "' AND Giris_Bordro <='" + cekraporDTO.getGbor2() + "'" +
				" AND Giris_Tarihi >='" + cekraporDTO.getGtar1() + "' AND Giris_Tarihi <='" + cekraporDTO.getGtar2() + "'" +
				" AND Cikis_Bordro >='" + cekraporDTO.getCbor1() + "' AND Cikis_Bordro <='" + cekraporDTO.getCbor2() + "'" +
				" AND Cikis_Tarihi >='" + cekraporDTO.getCtar1() + "' AND Cikis_Tarihi <='" + cekraporDTO.getCtar2() + "'" +
				" AND Giris_Musteri >='" + cekraporDTO.getGhes1() + "' AND Giris_Musteri <='" + cekraporDTO.getGhes2() + "'" +
				" AND Cikis_Musteri >='" + cekraporDTO.getChes1() + "' AND Cikis_Musteri <='" + cekraporDTO.getChes2() + "'" +
				" AND Durum >='" + cekraporDTO.getDurum1() + "' AND Durum <='" + cekraporDTO.getDurum2() + "'" +
				" AND T_Tarih >='" + cekraporDTO.getTtar1() + "'  AND T_Tarih <='" + cekraporDTO.getTtar2() + "'" +
				" AND Cins >='" + cekraporDTO.getCins1() + "' AND Cins <='" + cekraporDTO.getCins2() + "'" +
				" AND Giris_Ozel_Kod  LIKE '" + cekraporDTO.getGozel() + "'" +
				" AND Cikis_Ozel_Kod  LIKE '" + cekraporDTO.getCozel() + "'" +
				qweString +
				" ORDER BY Cek_No ";
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
	public void kambiyo_firma_adi_kayit(String fadi, ConnectionDetails kambiyoConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public bordrodetayDTO cektakipkontrol(String cekno, ConnectionDetails kambiyoConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void kam_durum_yaz(String cekno, String ceksen_from, String ceksen_where, String durum, String ttarih,
			ConnectionDetails kambiyoConnDetails) {
		// TODO Auto-generated method stub
		
	}
}