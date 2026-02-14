package com.hamit.obs.repository.gunluk;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.dto.gunluk.gunlukBilgiDTO;
import com.hamit.obs.exception.ServiceException;

@Component
public class GunlukMsSQL  implements IGunlukDatabase {

	@Override
	public String gun_firma_adi(ConnectionDetails gunlukConnDetails) {
		String firmaIsmi = "";
		String query = "SELECT FIRMA_ADI FROM OZEL";
		try (Connection connection = DriverManager.getConnection(gunlukConnDetails.getJdbcUrl(), gunlukConnDetails.getUsername(), gunlukConnDetails.getPassword());
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
	public List<Map<String, Object>> gorev_sayi(Date start,Date end,
			ConnectionDetails gunlukConnDetails) {
		List<Map<String, Object>> resultList = new ArrayList<>();
	    String sql = """
	        SELECT TARIH as tarih, SAAT as saat, COUNT(*) as adet
	        FROM GUNLUK
	        WHERE TARIH >= ? AND TARIH <= ?
	        GROUP BY TARIH, SAAT
	        """;
	    try (Connection connection = DriverManager.getConnection(
	                gunlukConnDetails.getJdbcUrl(),
	                gunlukConnDetails.getUsername(),
	                gunlukConnDetails.getPassword());
	         PreparedStatement ps = connection.prepareStatement(sql)) {
	        java.sql.Date s = new java.sql.Date(start.getTime());
	        java.sql.Date e = new java.sql.Date(end.getTime());

	        ps.setDate(1, s);
	        ps.setDate(2, e);

	        try (ResultSet rs = ps.executeQuery()) {
	            resultList = ResultSetConverter.convertToList(rs);
	        }

	    } catch (Exception ex) {
	        throw new ServiceException("Günlük görev sayısı okuma hatası.", ex);
	    }
		return resultList;
	}

	@Override
	public void gorev_kayit(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gorev_sil(int id, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Map<String, Object>> gorev_oku(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> isim_oku(ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> gorev_oku_tarih(String tarih,String saat, ConnectionDetails gunlukConnDetails) {
		List<Map<String, Object>> resultList = new ArrayList<>();
		String sql = "SELECT TARIH as tarih,SAAT as saat,ISIM as isim,GOREV as gorev,YER as yer,MESAJ as mesaj,GID as gid" +
				" FROM GUNLUK WITH (INDEX (IDX_GUNLUK))" +
				" WHERE TARIH = ? AND SAAT >= ? AND SAAT <=  ? " +
				" GROUP BY TARIH,SAAT,ISIM,GOREV,YER,MESAJ,GID ORDER BY ISIM";
		
	    try (Connection connection = DriverManager.getConnection(
	                gunlukConnDetails.getJdbcUrl(),
	                gunlukConnDetails.getUsername(),
	                gunlukConnDetails.getPassword());
	         PreparedStatement ps = connection.prepareStatement(sql)) {
	        ps.setDate(1, java.sql.Date.valueOf(tarih)); 
	        ps.setString(2, saat);
	        ps.setString(3, saat);
	        try (ResultSet rs = ps.executeQuery()) {
	            resultList = ResultSetConverter.convertToList(rs);
	        }

	    } catch (Exception ex) {
	        throw new ServiceException("Günlük görev sayısı okuma hatası.", ex);
	    }
		return resultList;
	}

	@Override
	public List<Map<String, Object>> gID_oku(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> hazir_gorevler(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void gorev_tek_sil(int id, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gunluk_farkli_kayit(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Map<String, Object>> gorev_oku_aylik_grup(String ay, ConnectionDetails gunlukConnDetails) {
		// ay: "2026-02"
	    List<Map<String, Object>> resultList = new ArrayList<>();

	    String sql =
	        "SELECT CONVERT(varchar(10), TARIH, 23) AS tarih, COUNT(1) AS adet " +
	        "FROM GUNLUK WITH (INDEX (IDX_GUNLUK)) " +
	        "WHERE TARIH >= ? AND TARIH < ? " +
	        "GROUP BY CONVERT(varchar(10), TARIH, 23) " +
	        "ORDER BY CONVERT(varchar(10), TARIH, 23)";

	    // start = 2026-02-01, end = 2026-03-01
	    java.sql.Date start;
	    java.sql.Date end;
	    try {
	        java.time.LocalDate s = java.time.LocalDate.parse(ay + "-01"); // "2026-02-01"
	        java.time.LocalDate e = s.plusMonths(1);
	        start = java.sql.Date.valueOf(s);
	        end   = java.sql.Date.valueOf(e);
	    } catch (Exception ex) {
	        throw new ServiceException("Ay formatı hatalı. Beklenen: YYYY-MM (örn 2026-02)", ex);
	    }

	    try (Connection connection = DriverManager.getConnection(
	            gunlukConnDetails.getJdbcUrl(),
	            gunlukConnDetails.getUsername(),
	            gunlukConnDetails.getPassword());
	         PreparedStatement ps = connection.prepareStatement(sql)) {

	        ps.setDate(1, start);
	        ps.setDate(2, end);

	        try (ResultSet rs = ps.executeQuery()) {
	            resultList = ResultSetConverter.convertToList(rs);
	        }

	    } catch (Exception ex) {
	        throw new ServiceException("Günlük aylık görev sayısı okuma hatası.", ex);
	    }

	    return resultList;
	}

	@Override
	public List<Map<String, Object>> gorev_oku_sonraki(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void gun_firma_adi_kayit(String fadi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Map<String, Object>> gorev_oku_yillik_pivot(gunlukBilgiDTO gbilgi,
			ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> gorev_oku_sonraki_yil(gunlukBilgiDTO gbilgi, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int gunluk_gid_kontrol(int gid, int kont, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void gorev_tablo_gidsil(int gid, ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Map<String, Object>> gorev_liste(ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> yer_oku(ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> gorev_oku(ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int gidnoal(ConnectionDetails gunlukConnDetails) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Map<String, Object>> gorev_oku_gun(String tarih, ConnectionDetails gunlukConnDetails) {
		List<Map<String, Object>> resultList = new ArrayList<>();

	    String sql =
	        "SELECT " +
	        "  CONVERT(varchar(10), TARIH, 23) AS tarih, " +
	        "  SAAT AS saat, " +
	        "  ISIM AS isim, " +
	        "  GOREV AS gorev, " +
	        "  YER AS yer, " +
	        "  MESAJ AS mesaj, " +
	        "  GID AS gid " +
	        "FROM GUNLUK WITH (INDEX (IDX_GUNLUK)) " +
	        "WHERE TARIH >= ? AND TARIH < DATEADD(day, 1, ?) " +
	        "ORDER BY SAAT, GID";

	    try (Connection connection = DriverManager.getConnection(
	            gunlukConnDetails.getJdbcUrl(),
	            gunlukConnDetails.getUsername(),
	            gunlukConnDetails.getPassword());
	         PreparedStatement ps = connection.prepareStatement(sql)) {

	        java.sql.Date date = java.sql.Date.valueOf(tarih);

	        ps.setDate(1, date);
	        ps.setDate(2, date);

	        try (ResultSet rs = ps.executeQuery()) {
	            resultList = ResultSetConverter.convertToList(rs);
	        }

	    } catch (Exception ex) {
	        throw new ServiceException("Günlük görev okuma hatası.", ex);
	    }

	    return resultList;
	}

	}
