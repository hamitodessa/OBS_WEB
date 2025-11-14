package com.hamit.obs.repository.cari;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.enums.modulbaslikTipi;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.dto.cari.dekontDTO;
import com.hamit.obs.dto.cari.dvzcevirmeDTO;
import com.hamit.obs.dto.cari.hesapplaniDTO;
import com.hamit.obs.dto.cari.mizanDTO;
import com.hamit.obs.dto.cari.tahayarDTO;
import com.hamit.obs.dto.cari.tahrapDTO;
import com.hamit.obs.dto.cari.tahsilatDTO;
import com.hamit.obs.dto.cari.tahsilatTableRowDTO;
import com.hamit.obs.exception.ServiceException;

@Component
public class CariMsSQL implements ICariDatabase{

	@Override
	public String[] hesap_adi_oku(String hesap,  ConnectionDetails cariConnDetails) {
		String[] firmaIsmi = {"",""};
		String query = "SELECT HESAP,HESAP_CINSI,KARTON,UNVAN FROM HESAP " + 
				" WHERE HESAP = ? ";
		try (Connection connection =  DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setNString(1, hesap);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					firmaIsmi[0] = resultSet.getString("UNVAN");
					firmaIsmi[1] = resultSet.getString("HESAP_CINSI");
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Hesap adı okunamadı", e); 
		}
		return firmaIsmi;
	}

	@Override
	public List<Map<String, Object>> ekstre(String hesap, String t1, String t2,Pageable pageable, ConnectionDetails cariConnDetails) {
		int page = pageable.getPageNumber();
	    int pageSize = pageable.getPageSize();
	    if (page < 0) page = 0;
	    int offset = page * pageSize;
	    int startRn = offset + 1;
	    int endRn   = offset + pageSize;
	    boolean hasDate = !("1900-01-01".equals(t1) && "2100-12-31".equals(t2));
	    final String orderKey = "S.TARIH, S.EVRAK, S.H, S.SID";
	    String sql;
	    if (hasDate) {
	        sql =
	            "WITH R AS ( " +
	            "  SELECT " +
	            "    S.SID, S.TARIH, S.EVRAK, ISNULL(I.IZAHAT,'') AS IZAHAT, " +
	            "    S.KOD, S.KUR, S.BORC, S.ALACAK, S.[USER], " +
	            "    SUM(S.ALACAK - S.BORC) OVER (ORDER BY " + orderKey + " " +
	            "        ROWS UNBOUNDED PRECEDING) AS RUNNING, " +
	            "    (SELECT ISNULL(SUM(ALACAK - BORC),0)  " +
	            "       FROM SATIRLAR " +
	            "      WHERE HESAP = ? AND TARIH < ?) AS DEVREDEN, " +
	            "    ROW_NUMBER() OVER (ORDER BY " + orderKey + ") AS rn " +
	            "  FROM SATIRLAR S " +
	            "  LEFT JOIN IZAHAT I ON S.EVRAK = I.EVRAK " +
	            "  WHERE S.HESAP = ? AND S.TARIH >= ? AND S.TARIH < ? " +
	            ") " +
	            "SELECT " +
	            "  SID, TARIH, EVRAK, IZAHAT, KOD, KUR, BORC, ALACAK, " +
	            "  DEVREDEN + RUNNING AS BAKIYE, [USER] " +
	            "FROM R " +
	            "WHERE rn BETWEEN ? AND ? " +
	            "ORDER BY rn;";
	    } else {
	        sql =
	            "WITH R AS ( " +
	            "  SELECT " +
	            "    S.SID, S.TARIH, S.EVRAK, ISNULL(I.IZAHAT,'') AS IZAHAT, " +
	            "    S.KOD, S.KUR, S.BORC, S.ALACAK, S.[USER], " +
	            "    SUM(S.ALACAK - S.BORC) OVER (ORDER BY " + orderKey + " " +
	            "        ROWS UNBOUNDED PRECEDING) AS BAKIYE, " +
	            "    ROW_NUMBER() OVER (ORDER BY " + orderKey + ") AS rn " +
	            "  FROM SATIRLAR S " +
	            "  LEFT JOIN IZAHAT I ON S.EVRAK = I.EVRAK " +
	            "  WHERE S.HESAP = ? " +
	            ") " +
	            "SELECT " +
	            "  SID, TARIH, EVRAK, IZAHAT, KOD, KUR, BORC, ALACAK, BAKIYE, [USER] " +
	            "FROM R " +
	            "WHERE rn BETWEEN ? AND ? " +
	            "ORDER BY rn;";
	    }

	    List<Map<String, Object>> resultList = new ArrayList<>();
	    try (Connection connection = DriverManager.getConnection(
	                cariConnDetails.getJdbcUrl(),
	                cariConnDetails.getUsername(),
	                cariConnDetails.getPassword());
	         PreparedStatement ps = connection.prepareStatement(
	                sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

	        int p = 1;
	        if (hasDate) {
	            ps.setNString(p++, hesap);
	            Timestamp[] ts = Global_Yardimci.rangeDayT2plusDay(t1, t2);
	            ps.setTimestamp(p++, ts[0]);   // TARIH < t1

	            ps.setNString(p++, hesap);
	            ps.setTimestamp(p++, ts[0]);   // TARIH >= t1
	            ps.setTimestamp(p++, ts[1]);   // TARIH < t2(+1)

	            ps.setInt(p++, startRn);
	            ps.setInt(p++, endRn);
	        } else {
	            ps.setNString(p++, hesap);
	            ps.setInt(p++, startRn);
	            ps.setInt(p++, endRn);
	        }
	        try (ResultSet rs = ps.executeQuery()) {
	            resultList = ResultSetConverter.convertToList(rs);
	        }
	    } catch (Exception e) {
	        throw new ServiceException("Ekstre okunamadı", e);
	    }
	    return resultList;
	}

	@Override
	public List<Map<String, Object>> eski_bakiye(String hesap,String t2, ConnectionDetails cariConnDetails){
		LocalDate endDate = Global_Yardimci.toLocalDateSafe(t2);
		Timestamp ts2 = Timestamp.valueOf(endDate.atStartOfDay());
		String sql = "SELECT SID,TARIH, BORC, ALACAK, " +
				" SUM(ALACAK - BORC) OVER(ORDER BY TARIH ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS BAKIYE" +
				" FROM SATIRLAR  " +
				" WHERE HESAP = ? " +
				" AND SATIRLAR.TARIH < ? " + 
				" ORDER BY TARIH";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setNString(1, hesap);
			preparedStatement.setTimestamp(2, ts2);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				resultList = ResultSetConverter.convertToList(resultSet);
			}
		} catch (Exception e) {
			throw new ServiceException("Ekstre okunamadı", e);
		}
		return resultList;
	}

	@Override
	public double eks_raporsize(String hesap, String t1, String t2, ConnectionDetails cariConnDetails) {
		double result = 0 ;
		String tARIH = "";
		Timestamp ts[] = Global_Yardimci.rangeDayT2plusDay(t1, t2);
		boolean hasDate = !("1900-01-01".equals(t1) && "2100-12-31".equals(t2));
		if (hasDate)
			tARIH = " AND TARIH >= ? AND TARIH < ? ";
		String sql = "SELECT COUNT(*)  " +
				" FROM SATIRLAR " +
				" WHERE HESAP = ? " +
				tARIH ;
		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(), 
				cariConnDetails.getUsername(), 
				cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setNString(1, hesap);
			if (hasDate) {
				preparedStatement.setTimestamp(2, ts[0]);
				preparedStatement.setTimestamp(3, ts[1]);
			}
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next())
				result  = resultSet.getInt(1);
		} catch (Exception e) {
			throw new ServiceException("MS stkService genel hatası.", e);
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> ekstre_mizan(String kod, String ilktarih, String sontarih, String ilkhcins,
			String sonhcins, String ilkkar, String sonkar, ConnectionDetails cariConnDetails) {
		LocalDate start = Global_Yardimci.toLocalDateSafe(ilktarih);
		LocalDate end = Global_Yardimci.toLocalDateSafe(sontarih);
		String sql = "SELECT SATIRLAR.HESAP,HESAP.UNVAN,HESAP.HESAP_CINSI,SUM(SATIRLAR.BORC) AS ISLEM,SUM(SATIRLAR.ALACAK) AS ISLEM2,SUM(SATIRLAR.ALACAK - SATIRLAR.BORC) AS BAKIYE" 
				+ " FROM SATIRLAR LEFT JOIN" 
				+ " HESAP ON SATIRLAR.HESAP = HESAP.HESAP " 
				+ " WHERE SATIRLAR.HESAP = ? " 
				+ " AND SATIRLAR.TARIH >= ? AND SATIRLAR.TARIH <  ? " 
				+ " AND HESAP.HESAP_CINSI BETWEEN ? AND ? " 
				+ " AND HESAP.KARTON BETWEEN ? AND ? "
				+ " GROUP BY SATIRLAR.HESAP,HESAP.UNVAN,HESAP.HESAP_CINSI" 
				+ " ORDER BY SATIRLAR.HESAP";
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			int i = 1;
			preparedStatement.setNString(i++, kod);
			preparedStatement.setTimestamp(i++, Timestamp.valueOf(start.atStartOfDay()));
			preparedStatement.setTimestamp(i++, Timestamp.valueOf(end.atStartOfDay())); 
			preparedStatement.setNString(i++, ilkhcins);
			preparedStatement.setNString(i++, sonhcins);
			preparedStatement.setNString(i++, ilkkar);
			preparedStatement.setNString(i++, sonkar);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("veritabani okuma", e); 
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> hesap_kodlari(ConnectionDetails cariConnDetails) {
		String sql = "SELECT HESAP FROM HESAP ORDER BY HESAP";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS CariService genel hatası.", e);
		}
		return resultList; 
	}
	@Override
	public List<Map<String, Object>> hp_pln(ConnectionDetails cariConnDetails) {
		String sql = "SELECT * FROM HESAP ORDER BY HESAP";
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("MS CariService genel hatası.", e);
		}
		return resultList; 
	}
	@Override
	public int cari_sonfisno(ConnectionDetails cariConnDetails) {
		int evrakNo = 0;
		String query = "SELECT MAX(EVRAK)  FROM SATIRLAR";
		try (Connection connection =  DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next())
					evrakNo = resultSet.getInt(1);
			}
		} catch (Exception e) {
			throw new ServiceException("Hesap adı okunamadı", e); 
		}
		return evrakNo;
	}
	@Override
	public boolean cari_dekont_kaydet(dekontDTO dBilgi, ConnectionDetails cariConnDetails) {
		final String sqlSatir = "INSERT INTO SATIRLAR (HESAP,TARIH,H,EVRAK,CINS,KUR,BORC,ALACAK,KOD,[USER]) " +
				"VALUES (?,?,?,?,?,?,?,?,?,?)";
		final String sqlIzahat =
				"INSERT INTO IZAHAT (EVRAK, IZAHAT) VALUES (?, ?)";
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
			try (Statement s = connection.createStatement()) { s.execute("SET XACT_ABORT ON"); }
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			connection.setAutoCommit(false);
			try (PreparedStatement psSatir = connection.prepareStatement(sqlSatir);
					PreparedStatement psIzahat   = connection.prepareStatement(sqlIzahat)) {
				addSatir(psSatir, dBilgi.getBhes(), dBilgi.getTar(), "B", dBilgi.getFisNo(),
						dBilgi.getBcins(), dBilgi.getBkur(),
						Math.round(dBilgi.getBorc() * 100) / 100.0, 0, dBilgi.getKod(), dBilgi.getUser());
				psSatir.addBatch();
				addSatir(psSatir, dBilgi.getAhes(), dBilgi.getTar(), "A", dBilgi.getFisNo(),
						dBilgi.getAcins(), dBilgi.getAkur(),
						0, Math.round(dBilgi.getAlacak() * 100) / 100.0, dBilgi.getKod(), dBilgi.getUser());
				psSatir.addBatch();
				psSatir.executeBatch();
				psIzahat.setInt(1, dBilgi.getFisNo());
				psIzahat.setNString(2, dBilgi.getIzahat());
				psIzahat.executeUpdate();
			}
			connection.commit();
			return true;
		} catch (Exception e) {
			if (connection != null) try { connection.rollback(); } catch (SQLException ignore) {}
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		} finally {
			if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
		}
	}

	private void addSatir(PreparedStatement stmt, String hesap, String tarih, String h, int evrak,
			String cins, double kur, double borc, double alacak, String kod, String user) throws SQLException {
		stmt.setNString(1, hesap);
		stmt.setTimestamp(2, Timestamp.valueOf(tarih));
		stmt.setNString(3, h);
		stmt.setInt(4, evrak);
		stmt.setNString(5, cins);
		stmt.setDouble(6, kur);
		stmt.setDouble(7, borc);
		stmt.setDouble(8, alacak);
		stmt.setNString(9, kod);
		stmt.setNString(10, user);
	}

	@Override
	public List<dekontDTO> fiskon(int fisNo, ConnectionDetails cariConnDetails) {
		String sql = "SELECT HESAP,TARIH,H,SATIRLAR.EVRAK,CINS, KUR,BORC,ALACAK,ISNULL(IZAHAT,'') AS IZAHAT,KOD,[USER]" +
				" FROM SATIRLAR LEFT JOIN IZAHAT ON SATIRLAR.EVRAK = IZAHAT.EVRAK" +
				" WHERE SATIRLAR.EVRAK = ? " +
				" ORDER BY H DESC";
		List<dekontDTO> dekontDTO =  new ArrayList<>(); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setInt(1, fisNo);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.isBeforeFirst()) {
				resultSet.next();
				dekontDTO dto = new dekontDTO();
				dto.setFisNo(fisNo);
				Date tarih = resultSet.getDate("TARIH");
				if (tarih != null)
					dto.setTar(dateFormat.format(tarih));
				dto.setBhes(resultSet.getString("HESAP"));
				dto.setBcins(resultSet.getString("CINS"));
				dto.setBkur(resultSet.getDouble("KUR"));
				dto.setBorc(resultSet.getDouble("BORC"));
				dto.setIzahat(resultSet.getString("IZAHAT"));
				dto.setKod(resultSet.getString("KOD"));
				dto.setUser(resultSet.getString("USER"));
				dekontDTO.add(dto);

				resultSet.next();
				dto = new dekontDTO();

				dto.setTar(dateFormat.format(tarih));
				dto.setAhes(resultSet.getString("HESAP")); 
				dto.setAcins(resultSet.getString("CINS"));
				dto.setAkur(resultSet.getDouble("KUR"));
				dto.setAlacak(resultSet.getDouble("ALACAK"));

				dto.setIzahat(resultSet.getString("IZAHAT"));
				dto.setKod(resultSet.getString("KOD"));
				dto.setUser(resultSet.getString("USER"));
				dekontDTO.add(dto);
			}
		} catch (Exception e) {
			throw new ServiceException("MS CariService Evrak Okuma", e);
		}
		return dekontDTO; 
	}

	@Override
	public int yenifisno(ConnectionDetails cariConnDetails) {
		int evrakNo = 0;
		String query = "UPDATE EVRAK_NO WITH (ROWLOCK, UPDLOCK) " +
				"SET EVRAK = EVRAK + 1 " +
				"OUTPUT INSERTED.EVRAK " +
				"WHERE EID = 1;";
		try (Connection connection =  DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next())
					evrakNo = resultSet.getInt(1);
			}
		} catch (Exception e) {
			throw new ServiceException("Yeni Evrak No Alinamadi", e); 
		}
		return evrakNo;
	}

	@Override
	public void evrak_yoket(int evrakno,ConnectionDetails cariConnDetails) {
		String delSatirlar = "DELETE FROM SATIRLAR WHERE EVRAK = ?";
		String delIzahat = "DELETE FROM IZAHAT WHERE EVRAK = ?";
		Connection con = null;
		try {
			con = DriverManager.getConnection(
					cariConnDetails.getJdbcUrl(),
					cariConnDetails.getUsername(),
					cariConnDetails.getPassword());
			try (Statement s = con.createStatement()) {
				s.execute("SET XACT_ABORT ON");
			}
			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			con.setAutoCommit(false);
			try (PreparedStatement psIza = con.prepareStatement(delIzahat);
					PreparedStatement psSat = con.prepareStatement(delSatirlar)) {
				psIza.setInt(1, evrakno);
				psIza.executeUpdate();
				psSat.setInt(1, evrakno);
				psSat.executeUpdate();
				con.commit();
			}
		} catch (Exception e) {
			if (con != null) try { con.rollback(); } catch (SQLException ignore) {}
			throw new ServiceException("Evrak yok etme sırasında bir hata oluştu", e);
		} finally {
			if (con != null) try { con.close(); } catch (SQLException ignore) {}
		}
	}

	@Override
	public List<Map<String, Object>> mizan(mizanDTO mizanDTO, ConnectionDetails cariConnDetails) {
		List<Map<String, Object>> resultList = new ArrayList<>();
		StringBuilder sqlBuilder = new StringBuilder();
		String havingClause = "";
		havingClause = switch (mizanDTO.getHangi_tur()) {
		case "borcluhesaplar" -> " HAVING ROUND(SUM(SATIRLAR.ALACAK - SATIRLAR.BORC),2) < 0 ";
		case "alacaklihesaplar" -> " HAVING ROUND(SUM(SATIRLAR.ALACAK - SATIRLAR.BORC),2) > 0 ";
		case "sifirolanlar" -> " HAVING ROUND(SUM(SATIRLAR.ALACAK - SATIRLAR.BORC),2) = 0 ";
		case "sifirolmayanlar" -> " HAVING ROUND(SUM(SATIRLAR.ALACAK - SATIRLAR.BORC),2) <> 0 ";
		default -> ""; 
		};
		sqlBuilder.append("SELECT SATIRLAR.HESAP, HESAP.UNVAN, HESAP.HESAP_CINSI AS H_CINSI, ")
		.append("SUM(SATIRLAR.BORC) AS BORC, SUM(SATIRLAR.ALACAK) AS ALACAK, ")
		.append("SUM(SATIRLAR.ALACAK) - SUM(SATIRLAR.BORC) AS BAKIYE ")
		.append("FROM SATIRLAR, HESAP ")
		.append("WHERE SATIRLAR.HESAP = HESAP.HESAP ")
		.append("AND SATIRLAR.HESAP BETWEEN ? AND ? ")
		.append("AND HESAP.HESAP_CINSI BETWEEN ? AND ? ")
		.append("AND HESAP.KARTON BETWEEN ? AND ? ");
		boolean hasDate = !("1900-01-01".equals(mizanDTO.getStartDate()) && "2100-12-31".equals(mizanDTO.getEndDate()));
		if (hasDate)
			sqlBuilder.append(" AND TARIH >= ? AND TARIH < ? ");
		sqlBuilder.append("GROUP BY SATIRLAR.HESAP, HESAP.UNVAN, HESAP.HESAP_CINSI ")
		.append(havingClause)
		.append("ORDER BY SATIRLAR.HESAP ASC ");
		String sql = sqlBuilder.toString();
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setNString(1, mizanDTO.getHkodu1());
			preparedStatement.setNString(2, mizanDTO.getHkodu2());
			preparedStatement.setNString(3, mizanDTO.getCins1());
			preparedStatement.setNString(4, mizanDTO.getCins2());
			preparedStatement.setNString(5, mizanDTO.getKarton1());
			preparedStatement.setNString(6, mizanDTO.getKarton2());
			int paramIndex = 7;
			if (hasDate) {
				Timestamp ts[] = Global_Yardimci.rangeDayT2plusDay(mizanDTO.getStartDate(), mizanDTO.getEndDate());
				preparedStatement.setTimestamp(paramIndex++, ts[0]);
				preparedStatement.setTimestamp(paramIndex, ts[1]);
			}
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				resultList = ResultSetConverter.convertToList(resultSet);
			}
		} catch (SQLException e) {
			throw new ServiceException("Mizan okunamadı", e);
		}
		return resultList;
	}
	@Override
	public String cari_firma_adi(ConnectionDetails cariConnDetails) {
		String firmaIsmi = "";
		String query = "SELECT FIRMA_ADI FROM OZEL";
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next())
				firmaIsmi = resultSet.getString("FIRMA_ADI");
		} catch (SQLException e) {
			throw new ServiceException("Firma adı okunamadı", e);
		}
		return firmaIsmi;
	}

	@Override
	public void hsp_sil(String hesap,ConnectionDetails cariConnDetails) {
		String queryHesap = "DELETE FROM HESAP WHERE HESAP = ?";
		String queryHesapDetay = "DELETE FROM HESAP_DETAY WHERE D_HESAP = ?";
		Connection con = null;
		boolean oldAuto = true;
		try {
			con = DriverManager.getConnection(
					cariConnDetails.getJdbcUrl(),
					cariConnDetails.getUsername(),
					cariConnDetails.getPassword());

			oldAuto = con.getAutoCommit();
			con.setAutoCommit(false);
			try (PreparedStatement psDetay = con.prepareStatement(queryHesapDetay);
					PreparedStatement psHesap = con.prepareStatement(queryHesap)) {
				psDetay.setNString(1, hesap);
				psDetay.executeUpdate();
				psHesap.setNString(1, hesap);
				psHesap.executeUpdate();
			}
			con.commit();
		} catch (Exception e) {
			if (con != null) {
				try { con.rollback(); } catch (Exception ignore) {}
			}
			throw new ServiceException("Hesap silme sırasında bir hata oluştu", e);
		} finally {
			if (con != null) {
				try { con.setAutoCommit(oldAuto); } catch (Exception ignore) {}
				try { con.close(); } catch (Exception ignore) {}
			}
		}	
	}

	@Override
	public void hpln_kayit(hesapplaniDTO hesapplaniDTO, ConnectionDetails cariConnDetails) {
		String sql = "INSERT INTO HESAP (HESAP,UNVAN,KARTON,HESAP_CINSI,[USER]) " +
				" VALUES (?,?,?,?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setNString(1,hesapplaniDTO.getKodu());
			stmt.setNString(2, hesapplaniDTO.getAdi());
			stmt.setNString(3, hesapplaniDTO.getKarton());
			stmt.setNString(4, hesapplaniDTO.getHcins());
			stmt.setNString(5, hesapplaniDTO.getUsr());
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}
	@Override
	public void hpln_detay_kayit(hesapplaniDTO hesapplaniDTO, ConnectionDetails cariConnDetails) {
		String sql = "INSERT INTO HESAP_DETAY (D_HESAP,YETKILI,ADRES_1,ADRES_2,SEMT,SEHIR,VERGI_DAIRESI,VERGI_NO,TEL_1,TEL_2, " + 
				" TEL_3,FAX,OZEL_KOD_1,OZEL_KOD_2,OZEL_KOD_3,WEB,E_MAIL,TC_KIMLIK,ACIKLAMA,SMS_GONDER,RESIM)" +
				" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setNString(1, hesapplaniDTO.getKodu());
			stmt.setNString(2,hesapplaniDTO.getYetkili());
			stmt.setNString(3, hesapplaniDTO.getAd1());
			stmt.setNString(4, hesapplaniDTO.getAd2());
			stmt.setNString(5, hesapplaniDTO.getSemt());
			stmt.setNString(6, hesapplaniDTO.getSeh());
			stmt.setNString(7, hesapplaniDTO.getVd());
			stmt.setNString(8, hesapplaniDTO.getVn());
			stmt.setNString(9, hesapplaniDTO.getT1());
			stmt.setNString(10, hesapplaniDTO.getT2());
			stmt.setNString(11, hesapplaniDTO.getT3());
			stmt.setNString(12, hesapplaniDTO.getFx());
			stmt.setNString(13, hesapplaniDTO.getO1());
			stmt.setNString(14, hesapplaniDTO.getO2());
			stmt.setNString(15, hesapplaniDTO.getO3());
			stmt.setNString(16, hesapplaniDTO.getWeb());
			stmt.setNString(17, hesapplaniDTO.getMail());
			stmt.setNString(18, hesapplaniDTO.getKim());
			stmt.setNString(19, hesapplaniDTO.getAcik());
			stmt.setBoolean(20, hesapplaniDTO.isSms());
			stmt.setBytes(21, hesapplaniDTO.getImage());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("Veritabanı hatası: " + e.getErrorCode(), e);
		}
	}
	@Override
	public hesapplaniDTO hsp_pln(String hesap, ConnectionDetails cariConnDetails) {
		String sql = "SELECT  [HESAP],[UNVAN],[HESAP_CINSI],[KARTON],[YETKILI],[ADRES_1],[ADRES_2],[SEMT],[SEHIR],[VERGI_DAIRESI]," + 
				" [VERGI_NO],[FAX],[TEL_1],[TEL_2],[TEL_3],[OZEL_KOD_1],[OZEL_KOD_2],[OZEL_KOD_3],[ACIKLAMA],[TC_KIMLIK],[WEB]," + 
				" [E_MAIL],[SMS_GONDER],[RESIM] ,[USER] " + 
				" FROM [HESAP] LEFT OUTER JOIN [HESAP_DETAY] WITH (INDEX (D_HESAP)) ON" + 
				" HESAP.HESAP = HESAP_DETAY.D_HESAP  WHERE HESAP.HESAP = ? ORDER BY HESAP";
		hesapplaniDTO hsdto = new hesapplaniDTO();
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setNString(1, hesap);
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				hsdto.setKodu(rs.getString("HESAP"));
				hsdto.setAdi(rs.getString("UNVAN"));
				hsdto.setKarton(rs.getString("KARTON"));
				hsdto.setHcins(rs.getString("HESAP_CINSI"));
				hsdto.setYetkili(rs.getString("YETKILI"));		       
				hsdto.setAd1(rs.getString("ADRES_1"));
				hsdto.setAd2(rs.getString("ADRES_2"));
				hsdto.setSemt(rs.getString("SEMT"));
				hsdto.setSeh(rs.getString("SEHIR"));
				hsdto.setVd(rs.getString("VERGI_DAIRESI"));
				hsdto.setVn(rs.getString("VERGI_NO"));
				hsdto.setT1(rs.getString("TEL_1"));
				hsdto.setT2(rs.getString("TEL_2"));
				hsdto.setT3(rs.getString("TEL_3"));
				hsdto.setFx(rs.getString("FAX"));
				hsdto.setO1(rs.getString("OZEL_KOD_1"));
				hsdto.setO2(rs.getString("OZEL_KOD_2"));
				hsdto.setO3(rs.getString("OZEL_KOD_3"));
				hsdto.setWeb(rs.getString("WEB"));
				hsdto.setMail(rs.getString("E_MAIL"));
				hsdto.setKim(rs.getString("TC_KIMLIK"));
				hsdto.setAcik(rs.getString("ACIKLAMA"));
				hsdto.setSms(rs.getBoolean("SMS_GONDER"));
				hsdto.setImage(rs.getBytes("RESIM"));
			}
		} catch (Exception e) {
			throw new ServiceException("Ekstre okunamadı", e);
		}
		return hsdto;
	}
	@Override
	public List<Map<String, Object>> ozel_mizan(mizanDTO mizanDTO, ConnectionDetails cariConnDetails) {
		List<Map<String, Object>> resultList = new ArrayList<>();
		Timestamp ts[] = Global_Yardimci.rangeDayT2plusDay(mizanDTO.getStartDate(), mizanDTO.getEndDate());
		String bakiyeFilter;
		switch (mizanDTO.getHangi_tur()) {
		case "borcluhesaplar" -> bakiyeFilter = " WHERE X.BAKIYE < -? ";
		case "alacaklihesaplar" -> bakiyeFilter = " WHERE X.BAKIYE >  ? ";
		case "sifirolanlar" -> bakiyeFilter = " WHERE ABS(X.BAKIYE) <  ? ";
		case "sifirolmayanlar" -> bakiyeFilter = " WHERE ABS(X.BAKIYE) >= ? ";
		default -> bakiyeFilter = "";
		};
		final String sql =
				"WITH H AS ( " +
						"  SELECT HESAP, UNVAN, HESAP_CINSI " +
						"  FROM HESAP " +
						"  WHERE HESAP > ? AND HESAP < ? " +
						"    AND HESAP_CINSI BETWEEN ? AND ? " +
						"    AND KARTON      BETWEEN ? AND ? " +
						"), " +
						"ozet AS ( " +
						"  SELECT S.HESAP, " +
						"         SUM(S.ALACAK - S.BORC) AS ONCEKI_BAKIYE " +
						"  FROM SATIRLAR S WITH (INDEX(IX_SATIRLAR)) " +
						"  JOIN H ON H.HESAP = S.HESAP " +
						"  WHERE S.TARIH < ? " +
						"  GROUP BY S.HESAP " +
						"), " +
						"donem AS ( " +
						"  SELECT S.HESAP, " +
						"         SUM(S.BORC) AS BORC, " +
						"         SUM(S.ALACAK) AS ALACAK " +
						"  FROM SATIRLAR S WITH (INDEX(IX_SATIRLAR)) " +
						"  JOIN H ON H.HESAP = S.HESAP " +
						"  WHERE S.TARIH >= ? AND S.TARIH < DATEADD(DAY,1, ?) " +
						"  GROUP BY S.HESAP " +
						") " +
						"SELECT * FROM ( " +
						"  SELECT H.HESAP, H.UNVAN, H.HESAP_CINSI, " +
						"         ISNULL(ozet.ONCEKI_BAKIYE,0) AS ONCEKI_BAKIYE, " +
						"         ISNULL(donem.BORC,0)         AS BORC, " +
						"         ISNULL(donem.ALACAK,0)       AS ALACAK, " +
						"         ISNULL(donem.ALACAK,0) - ISNULL(donem.BORC,0) AS BAK_KVARTAL, " +
						"        ISNULL(ozet.ONCEKI_BAKIYE,0) +(ISNULL(donem.ALACAK,0) - ISNULL(donem.BORC,0)) AS BAKIYE " +
						"  FROM H " +
						"  LEFT JOIN ozet  ON ozet.HESAP  = H.HESAP " +
						"  LEFT JOIN donem ON donem.HESAP = H.HESAP " +
						") X " + bakiyeFilter +
						"ORDER BY X.HESAP " +
						"OPTION (RECOMPILE, OPTIMIZE FOR UNKNOWN);";

		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(),
				cariConnDetails.getUsername(),
				cariConnDetails.getPassword());
				PreparedStatement ps = connection.prepareStatement(
						sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			connection.setReadOnly(true);
			int i = 1;
			ps.setNString(i++, mizanDTO.getHkodu1());
			ps.setNString(i++, mizanDTO.getHkodu2());
			ps.setNString(i++, mizanDTO.getCins1());
			ps.setNString(i++, mizanDTO.getCins2());
			ps.setNString(i++, mizanDTO.getKarton1());
			ps.setNString(i++, mizanDTO.getKarton2());
			ps.setTimestamp(i++, ts[0]);
			ps.setTimestamp(i++, ts[0]);
			ps.setTimestamp(i++, ts[1]);
			double eps = 0.01;
			if (bakiyeFilter != null && !bakiyeFilter.isEmpty()) {
				ps.setDouble(i++, eps); 
			}
			try (ResultSet rs = ps.executeQuery()) {
				resultList = ResultSetConverter.convertToList(rs);
			}
			return resultList;
		} catch (SQLException e) {
			throw new ServiceException("Mizan sorgusu çalıştırılırken hata oluştu", e);
		}
	}

	@Override
	public List<Map<String, Object>> dvzcevirme(dvzcevirmeDTO dvzcevirmeDTO, 
			ConnectionDetails cariConnDetails, 
			ConnectionDetails kurConnectionDetails) {
		int page = dvzcevirmeDTO.getPage();
		int pageSize = dvzcevirmeDTO.getPageSize();
		int offset = page * pageSize;

		List<Map<String, Object>> resultList = new ArrayList<>();
		try {
			if (!cariConnDetails.getSqlTipi().equals(kurConnectionDetails.getSqlTipi())) {
				throw new ServiceException("Cari Dosya ve Kur Dosyası farklı SQL sunucularında yer alıyor.");
			}
			String str1 = "";
			if (cariConnDetails.getServerIp().equals(kurConnectionDetails.getServerIp())) {
				str1 = modulbaslikTipi.OK_Kur.name() + kurConnectionDetails.getDatabaseName() + ".dbo.KURLAR";
			} else {
				try (Connection connection = DriverManager.getConnection(
						cariConnDetails.getJdbcUrl(), 
						cariConnDetails.getUsername(), 
						cariConnDetails.getPassword());
						PreparedStatement stmt = connection.prepareStatement(
								"SELECT CAST(value AS INT) as Config_Value FROM [master].sys.configurations WHERE name = 'ad hoc distributed queries'");
						ResultSet rs = stmt.executeQuery()) {
					rs.next();
					if (rs.getInt("Config_Value") == 0) {
						try (PreparedStatement enableStmt = connection.prepareStatement(
								"EXEC sp_configure 'show advanced options', 1; RECONFIGURE; " +
								"EXEC sp_configure 'ad hoc distributed queries', 1; RECONFIGURE;")) {
							enableStmt.execute();
						}
					}
				}
				str1 = "OPENROWSET('MSOLEDBSQL', '" + kurConnectionDetails.getServerIp().replace(":", ",") + "'; '" 
						+ kurConnectionDetails.getUsername() + "'; '" 
						+ kurConnectionDetails.getPassword() + "', 'SELECT * FROM [OK_Kur" 
						+ kurConnectionDetails.getDatabaseName() + "].[dbo].[KURLAR]')";
			}
			Timestamp ts[] = Global_Yardimci.rangeDayT2plusDay(dvzcevirmeDTO.getStartDate(), dvzcevirmeDTO.getEndDate());
			String tarihFilter = "";
			boolean hasDate = !("1900-01-01".equals(dvzcevirmeDTO.getStartDate()) && "2100-12-31".equals(dvzcevirmeDTO.getEndDate()));
			if (hasDate)
				tarihFilter = " AND s.TARIH  >= ?  AND s.TARIH < ? ";
			String sql = "SELECT s.TARIH, s.EVRAK, I.IZAHAT, " +
					" ISNULL(IIF(k." + dvzcevirmeDTO.getDvz_cins() + " = 0, 1, k." + dvzcevirmeDTO.getDvz_cins() + "), 1) as CEV_KUR, " +
					" ((s.ALACAK - s.BORC) / ISNULL(NULLIF(k." + dvzcevirmeDTO.getDvz_cins() + ", 0), 1)) as DOVIZ_TUTAR, " +
					" SUM((s.ALACAK - s.BORC) / ISNULL(NULLIF(k." + dvzcevirmeDTO.getDvz_cins() + ", 0), 1)) OVER (ORDER BY s.TARIH " +
					" ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) as DOVIZ_BAKIYE, " +
					" SUM(s.ALACAK - s.BORC) OVER (ORDER BY s.TARIH ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) as BAKIYE, " +
					" s.KUR, BORC, ALACAK, s.[USER] " +
					" FROM (SATIRLAR as s LEFT OUTER JOIN IZAHAT as I ON s.EVRAK = I.EVRAK) " +
					" LEFT OUTER JOIN " + str1 + " as k " +
					" ON CONVERT(VARCHAR(25), s.TARIH, 101) = CONVERT(VARCHAR(25), k.Tarih, 101) " +
					" WHERE s.HESAP = ? " + tarihFilter +
					" AND (k.Kur IS NULL OR k.Kur = '" + dvzcevirmeDTO.getDvz_tur() + "') " +
					" ORDER BY s.TARIH" +
					" OFFSET " + offset + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
			try (Connection connection = DriverManager.getConnection(
					cariConnDetails.getJdbcUrl(),
					cariConnDetails.getUsername(),
					cariConnDetails.getPassword());
					PreparedStatement stmt = connection.prepareStatement(sql,
							ResultSet.TYPE_FORWARD_ONLY,
							ResultSet.CONCUR_READ_ONLY)) {

				stmt.setNString(1, dvzcevirmeDTO.getHesapKodu());
				if (hasDate) {
					stmt.setTimestamp(2, ts[0]);
					stmt.setTimestamp(3, ts[1]);
				}
				try (ResultSet rs = stmt.executeQuery()) {
					resultList = ResultSetConverter.convertToList(rs);
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Dvz Cevirme: " + e.getMessage(), e);
		}
		return resultList;
	}

	@Override
	public double dvz_raporsize(dvzcevirmeDTO dvzcevirmeDTO, ConnectionDetails cariConnDetails,ConnectionDetails kurConnectionDetails) {
		double result = 0 ;
		try {
			String tarihFilter = "";
			Timestamp ts[] = Global_Yardimci.rangeDayT2plusDay(dvzcevirmeDTO.getStartDate(), dvzcevirmeDTO.getEndDate());

			boolean hasDate = !("1900-01-01".equals(dvzcevirmeDTO.getStartDate())
					&& "2100-12-31".equals(dvzcevirmeDTO.getEndDate()));
			if (hasDate) {
				tarihFilter = " AND TARIH >= ?  AND TARIH < ?";
			}
			String sql = "SELECT COUNT(*)  " +
					" FROM SATIRLAR " +
					" WHERE HESAP = ?  " + tarihFilter ;
			try (Connection connection = DriverManager.getConnection(
					cariConnDetails.getJdbcUrl(),
					cariConnDetails.getUsername(),
					cariConnDetails.getPassword());
					PreparedStatement stmt = connection.prepareStatement(sql,
							ResultSet.TYPE_FORWARD_ONLY,
							ResultSet.CONCUR_READ_ONLY)) {
				stmt.setNString(1, dvzcevirmeDTO.getHesapKodu());
				if (hasDate) {
					stmt.setTimestamp(2, ts[0]);
					stmt.setTimestamp(3, ts[1]);
				}
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next())
						result = rs.getInt(1);
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Dvz Cevirme: " + e.getMessage(), e);
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> banka_sube(String nerden, ConnectionDetails cariConnDetails) {
		String sql = "SELECT DISTINCT " + nerden +  " AS " +  nerden.toUpperCase() +
				" FROM TAH_CEK" +
				" ORDER BY " + nerden;
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {

			try (ResultSet rs = preparedStatement.executeQuery()) {
				resultList = ResultSetConverter.convertToList(rs);
			}
		} catch (Exception e) {
			throw new ServiceException("Banka sube okuma", e);
		}
		return resultList;
	}

	@Override
	public tahsilatDTO tahfiskon(String fisNo,Integer tah_ted, ConnectionDetails cariConnDetails) {
		String sql = "SELECT * FROM TAH_DETAY WHERE EVRAK = ? AND CINS = ? ";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		tahsilatDTO dto = new tahsilatDTO();
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setNString(1, fisNo); 
			preparedStatement.setInt(2, tah_ted);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.isBeforeFirst()) {
				resultSet.next();
				dto.setFisNo(resultSet.getString("EVRAK"));
				Date tarih = resultSet.getDate("TARIH");
				if (tarih != null)
					dto.setTahTarih(dateFormat.format(tarih));
				dto.setTcheskod(resultSet.getString("C_HES"));
				dto.setAdresheskod(resultSet.getString("A_HES"));
				dto.setTur(resultSet.getInt("TUR"));
				dto.setTah_ted(resultSet.getInt("CINS"));
				dto.setDvz_cins(resultSet.getString("DVZ_CINS"));
				dto.setTutar(resultSet.getDouble("TUTAR"));
				dto.setPosBanka(resultSet.getString("POS_BANKA"));
			}
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException("Tahsilat Evrak Okuma", e);
		}
		return dto; 
	}

	@Override
	public List<Map<String, Object>> tah_cek_doldur(String fisNo, Integer tah_ted, ConnectionDetails cariConnDetails) {
		String sql = "SELECT * FROM TAH_CEK WHERE EVRAK = ? AND CINS = ?";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setNString(1, fisNo);
			preparedStatement.setInt(2, tah_ted);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException("Tahsilat cek doldur", e);
		}
		return resultList; 
	}

	@Override
	public int cari_tahsonfisno(Integer tah_ted,ConnectionDetails cariConnDetails) {
		int evrakNo = 0;
		String sql = "SELECT MAX(TRY_CONVERT(int, EVRAK)) AS MAX_NO " +
				"FROM TAH_DETAY " +
				"WHERE CINS = ? AND TRY_CONVERT(int, EVRAK) IS NOT NULL";
		try (Connection connection =  DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setInt(1, tah_ted);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next())
					evrakNo = resultSet.getInt("MAX_NO");
			}
		} catch (Exception e) {
			throw new ServiceException("Tahsilat son fisno okunamadı", e); 
		}
		return evrakNo;
	}

	@Override
	public int cari_tah_fisno_al(String tah_ted, ConnectionDetails cariConnDetails) {
		int evrakNo = 0;
		String query = "UPDATE TAH_EVRAK SET NO = NO + 1 OUTPUT INSERTED.NO WHERE CINS = ? ";
		try (Connection connection =  DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			preparedStatement.setNString(1, tah_ted);   
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next())
					evrakNo = resultSet.getInt("NO");
			}
		} catch (Exception e) {
			throw new ServiceException("Tahsilat fisno alma  okunamadı", e); 
		}
		return evrakNo;
	}

	@Override
	public void tah_kayit(tahsilatDTO tahsilatDTO, ConnectionDetails cariConnDetails) {
		String sqlDel = "DELETE FROM TAH_DETAY WHERE EVRAK = ? AND CINS = ? ";
		String sqlIns = "INSERT INTO TAH_DETAY (EVRAK, TARIH, C_HES, A_HES, CINS, TUTAR, TUR, ACIKLAMA, DVZ_CINS, POS_BANKA)" +
				" VALUES (?,?,?,?,?,?,?,?,?,?)";
		Connection con = null;
		boolean oldAuto = true;
		try {
			con = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
			oldAuto = con.getAutoCommit();
			con.setAutoCommit(false);
			try (PreparedStatement psDel = con.prepareStatement(sqlDel);
					PreparedStatement psIns = con.prepareStatement(sqlIns)) {
				psDel.setString(1, tahsilatDTO.getFisNo());
				psDel.setInt(2, tahsilatDTO.getTah_ted());
				psDel.executeUpdate();
				int i = 1;
				psIns.setString(i++, tahsilatDTO.getFisNo());
				psIns.setTimestamp(i++, Timestamp.valueOf(tahsilatDTO.getTahTarih()));
				psIns.setString(i++, tahsilatDTO.getTcheskod());
				psIns.setString(i++, tahsilatDTO.getAdresheskod());
				psIns.setInt(i++, tahsilatDTO.getTah_ted());
				psIns.setDouble(i++, tahsilatDTO.getTutar());
				psIns.setInt(i++, tahsilatDTO.getTur());
				psIns.setString(i++, "");
				psIns.setString(i++, tahsilatDTO.getDvz_cins());
				psIns.setString(i++, tahsilatDTO.getPosBanka());
				psIns.executeUpdate();
			}
			con.commit();
		} catch (Exception e) {
			if (con != null) try { con.rollback(); } catch (Exception ignore) {}
			throw new ServiceException("Tahsilat Kayıt sırasında bir hata oluştu", e);
		} finally {
			if (con != null) {
				try { con.setAutoCommit(oldAuto); } catch (Exception ignore) {}
				try { con.close(); } catch (Exception ignore) {}
			}
		}	
	}

	@Override
	public void tah_cek_sil(tahsilatDTO tahsilatDTO, ConnectionDetails cariConnDetails) {
		String sql = "DELETE FROM TAH_CEK WHERE EVRAK = ? AND CINS = ? " ;
		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement deleteStmt = connection.prepareStatement(sql)) {
			deleteStmt.setNString(1, tahsilatDTO.getFisNo()); 
			deleteStmt.setInt(2, tahsilatDTO.getTah_ted());
			deleteStmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Tahsilat cek silme sırasında bir hata oluştu", e);
		}
	}

	@Override
	public void tah_cek_kayit(tahsilatTableRowDTO tahsilatTableRowDTO, String fisno, Integer tah_ted,
			ConnectionDetails cariConnDetails) {
		String sql  = "INSERT INTO TAH_CEK (EVRAK,CINS,BANKA,SUBE,SERI,HESAP,BORCLU,TARIH,TUTAR)" +
				" VALUES (?,?,?,?,?,?,?,?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setNString(1, fisno);
			stmt.setInt(2, tah_ted);
			stmt.setNString(3, tahsilatTableRowDTO.getBanka());
			stmt.setNString(4, tahsilatTableRowDTO.getSube());
			stmt.setNString(5, tahsilatTableRowDTO.getSeri() );
			stmt.setNString(6, tahsilatTableRowDTO.getHesap() );
			stmt.setNString(7, tahsilatTableRowDTO.getBorclu());
			stmt.setDate(8, java.sql.Date.valueOf(tahsilatTableRowDTO.getTarih()));
			stmt.setDouble(9, tahsilatTableRowDTO.getTutar());
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Tahsilat Kayıt sırasında bir hata oluştu", e);
		}
	}

	@Override
	public void tah_sil(String fisno, Integer tah_ted, ConnectionDetails cariConnDetails) {
		String sqlCek = "DELETE FROM TAH_CEK WHERE EVRAK = ? AND CINS = ? " ;
		String sqlDetay = "DELETE FROM TAH_DETAY WHERE EVRAK = ? AND CINS = ? " ;
		try (Connection con = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(),
				cariConnDetails.getUsername(),
				cariConnDetails.getPassword())) {
			boolean oldAuto = con.getAutoCommit();
			con.setAutoCommit(false);
			try (PreparedStatement psDetay = con.prepareStatement(sqlDetay);
					PreparedStatement psCek   = con.prepareStatement(sqlCek)) {
				psDetay.setNString(1, fisno);
				psDetay.setInt(2, tah_ted);
				psDetay.executeUpdate();
				psCek.setNString(1, fisno);
				psCek.setInt(2, tah_ted);
				psCek.executeUpdate();
				con.commit();
			} catch (Exception ex) {
				try { con.rollback(); } catch (SQLException ignore) {}
				throw new ServiceException("Evrak silme sırasında hata oluştu", ex);
			} finally {
				try { con.setAutoCommit(oldAuto); } catch (SQLException ignore) {}
			}
		} catch (SQLException e) {
			throw new ServiceException("Bağlantı hatası", e);
		}
	}

	@Override
	public List<Map<String, Object>> tah_listele(tahrapDTO tahrapDTO, ConnectionDetails cariConnDetails,ConnectionDetails adresConnectionDetails) {
		List<Map<String, Object>> resultList = new ArrayList<>();
		try {
			if (!cariConnDetails.getSqlTipi().equals(adresConnectionDetails.getSqlTipi())) {
				throw new ServiceException("Cari Dosya ve Adres Dosyası farklı SQL sunucularında yer alıyor.");
			}
			String str1 = "";
			if (cariConnDetails.getServerIp().equals(adresConnectionDetails.getServerIp())) {
				str1=  modulbaslikTipi.OK_Adr.name() + adresConnectionDetails.getDatabaseName() + ".dbo.Adres as adr ";
			} else {
				try (Connection connection = DriverManager.getConnection(
						cariConnDetails.getJdbcUrl(), 
						cariConnDetails.getUsername(), 
						cariConnDetails.getPassword());
						PreparedStatement stmt = connection.prepareStatement(
								"SELECT CAST(value AS INT) as Config_Value FROM [master].sys.configurations WHERE name = 'ad hoc distributed queries'");
						ResultSet rs = stmt.executeQuery()) {
					rs.next();
					if (rs.getInt("Config_Value") == 0) {
						try (PreparedStatement enableStmt = connection.prepareStatement(
								"EXEC sp_configure 'show advanced options', 1; RECONFIGURE; " +
								"EXEC sp_configure 'ad hoc distributed queries', 1; RECONFIGURE;")) {
							enableStmt.execute();
						}
					}
				}
				str1 = "OPENROWSET('MSOLEDBSQL', '" + adresConnectionDetails.getServerIp().replace(":", ",") + "'; '" 
						+ adresConnectionDetails.getUsername() + "'; '" 
						+ adresConnectionDetails.getPassword() + "', 'SELECT * FROM [OK_Adr" 
						+ adresConnectionDetails.getDatabaseName() + "].[dbo].[Adres]') as adr";
			}
			String cinString = "" , turString="" ,posString = "" ;
			if(tahrapDTO.getTah_ted() !=0)
				cinString = " CINS = '" + (tahrapDTO.getTah_ted() - 1) + "' AND";
			if(tahrapDTO.getHangi_tur() != 0)
				turString = " TUR = '" + (tahrapDTO.getHangi_tur() -1) + "' AND";
			if(! tahrapDTO.getPos().equals(""))
				posString = " POS_BANKA = '" + tahrapDTO.getPos() + "' AND";
			String sql = "SELECT EVRAK,TARIH,C_HES as CARI_HESAP," + 
					" (SELECT UNVAN FROM HESAP WHERE HESAP = C_HES ) AS UNVAN,"+ 
					" A_HES as ADRES_HESAP," + 
					" ISNULL(Adi,'') AS ADRES_UNVAN," +
					" CASE CINS WHEN '0' THEN 'Tahsilat' WHEN '1' THEN 'Tediye' END as CINS," +
					" CASE TUR  WHEN '0' THEN 'Nakit' WHEN '1' THEN 'Cek' WHEN '2' THEN 'Kredi Kartı' END as TUR,POS_BANKA," +
					" DVZ_CINS,TUTAR" +
					" FROM TAH_DETAY left join " + str1 + " on adr.M_Kodu = A_HES" + 
					" WHERE " + cinString  + turString  + posString +
					" TARIH >= ? AND TARIH < ? " + 
					" AND EVRAK >= ? AND EVRAK <= ? " + 
					" AND C_HES >= ? AND C_HES <= ? " + 
					" ORDER BY TARIH,EVRAK" ;
			Timestamp ts[] = Global_Yardimci.rangeDayT2plusDay(tahrapDTO.getStartDate(), tahrapDTO.getEndDate());

			try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
					PreparedStatement preparedStatement = connection.prepareStatement(sql,
							ResultSet.TYPE_FORWARD_ONLY,
							ResultSet.CONCUR_READ_ONLY)) {
				preparedStatement.setTimestamp(1, ts[0]);
				preparedStatement.setTimestamp(2, ts[1]);
				preparedStatement.setNString(3, tahrapDTO.getEvrak1() );
				preparedStatement.setNString(4,  tahrapDTO.getEvrak2());
				preparedStatement.setNString(5, tahrapDTO.getHkodu1() );
				preparedStatement.setNString(6, tahrapDTO.getHkodu2());
				ResultSet resultSet = preparedStatement.executeQuery();
				resultList = ResultSetConverter.convertToList(resultSet); 
				resultSet.close();
			}
		} catch (Exception e) {
			throw new ServiceException("Tahsilat Listeleme okunamadi", e);
		}
		return resultList; 
	}

	@Override
	public tahayarDTO tahayaroku(ConnectionDetails cariConnDetails) {
		String sql = "SELECT * FROM TAH_AYARLAR";
		tahayarDTO tahayarDTO = new tahayarDTO();
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				tahayarDTO.setAdi(rs.getString("FIR_ISMI"));
				tahayarDTO.setAd1(rs.getString("ADR_1"));
				tahayarDTO.setAd2(rs.getString("ADR_2"));
				tahayarDTO.setVdvn(rs.getString("VD_VN"));
				tahayarDTO.setMail(rs.getString("MAIL"));
				tahayarDTO.setDiger(rs.getString("DIGER"));
				tahayarDTO.setImagelogo(rs.getBytes("LOGO"));
				tahayarDTO.setImagekase(rs.getBytes("KASE"));
			}
		} catch (Exception e) {
			throw new ServiceException("Tahsilat ayar okunamadı", e);
		}
		return tahayarDTO;
	}

	@Override
	public void tahayar_kayit(tahayarDTO tahayarDTO, ConnectionDetails cariConnDetails) {
		String SQL_INSERT = "INSERT INTO TAH_AYARLAR (FIR_ISMI,ADR_1,ADR_2,VD_VN,MAIL,DIGER,LOGO,KASE)" +
				" VALUES (?,?,?,?,?,?,?,?)" ;
		String SQL_DELETE=  "DELETE FROM TAH_AYARLAR " ;
		try (Connection con = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(),
				cariConnDetails.getUsername(),
				cariConnDetails.getPassword())) {
			boolean old = con.getAutoCommit();
			con.setAutoCommit(false);
			try (PreparedStatement psDel = con.prepareStatement(SQL_DELETE);
					PreparedStatement psIns = con.prepareStatement(SQL_INSERT)) {
				psDel.executeUpdate();
				int i = 1;
				psIns.setNString(i++, tahayarDTO.getAdi());
				psIns.setNString(i++, tahayarDTO.getAd1());
				psIns.setNString(i++, tahayarDTO.getAd2());
				psIns.setNString(i++, tahayarDTO.getVdvn());
				psIns.setNString(i++, tahayarDTO.getMail());
				psIns.setNString(i++, tahayarDTO.getDiger());
				psIns.setBytes(i++, tahayarDTO.getImagelogo());
				psIns.setBytes(i++, tahayarDTO.getImagekase());
				psIns.executeUpdate();
				con.commit();
			} catch (Exception ex) {
				try { con.rollback(); } catch (Exception ignore) {}
				throw new ServiceException("TAH_AYARLAR kaydı sırasında hata", ex);
			} finally {
				try { con.setAutoCommit(old); } catch (Exception ignore) {}
			}
		} catch (SQLException e) {
			throw new ServiceException("Bağlantı hatası", e);
		}
	}

	@Override
	public List<Map<String, Object>> tah_ayar_oku(ConnectionDetails cariConnDetails) {
		String sql = "SELECT * FROM TAH_AYARLAR";
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("Tahsilat okuma", e); 
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> tah_cek_kayit_aktar(String fisno, Integer tah_ted,
			ConnectionDetails cariConnDetails) {
		String sql = "SELECT TA.LOGO, TA.FIR_ISMI,TA.ADR_1,TA.ADR_2,TA.VD_VN, TA.MAIL,TA.DIGER, " 
				+ " TA.KASE,TC.BANKA,TC.SUBE,TC.SERI,TC.HESAP,TC.BORCLU,TC.TARIH,TC.TUTAR "
				+ " FROM TAH_AYARLAR TA "
				+ " CROSS JOIN TAH_CEK TC "
				+ " WHERE TC.EVRAK = ? AND TC.CINS = ? ";
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setNString(1, fisno); 
			preparedStatement.setInt(2, tah_ted); 
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("Cek tahsilat okuma", e); 
		}
		return resultList; 
	}

	@Override
	public void cari_firma_adi_kayit(String fadi, ConnectionDetails cariConnDetails) {
		String sql = "UPDATE OZEL SET FIRMA_ADI = ? " ;
		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setNString(1,fadi);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}

	@Override
	public List<Map<String, Object>> hsppln_liste(ConnectionDetails cariConnDetails) {
		String sql = "SELECT  [HESAP],[UNVAN],[HESAP_CINSI],[KARTON],[YETKILI],[ADRES_1],[ADRES_2],[SEMT],[SEHIR],[VERGI_DAIRESI]," + 
				" [VERGI_NO],[FAX],[TEL_1],[TEL_2],[TEL_3],[OZEL_KOD_1],[OZEL_KOD_2],[OZEL_KOD_3],[ACIKLAMA],[TC_KIMLIK],[WEB]," + 
				" [E_MAIL],[SMS_GONDER],[RESIM] ,[USER] " + 
				" FROM [HESAP] LEFT OUTER JOIN [HESAP_DETAY] WITH (INDEX (D_HESAP)) ON" + 
				" HESAP.HESAP = HESAP_DETAY.D_HESAP ORDER BY HESAP";
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException("Ekstre okunamadı", e);
		}
		return resultList;
	}

	@Override
	public int hesap_plani_kayit_adedi(ConnectionDetails cariConnDetails) {
		int kayitSayi = 0;
		String query = "SELECT COUNT(*) FROM HESAP";
		try (Connection connection =  DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next())
					kayitSayi = resultSet.getInt(1);
			}
		} catch (Exception e) {
			throw new ServiceException("Yeni Evrak No Alinamadi", e); 
		}
		return kayitSayi;
	}

	@Override
	public void cari_kod_degis_hesap(String eskikod, String yenikod, ConnectionDetails cariConnDetails) {
		String SQL_UPD_HESAP = "UPDATE HESAP SET HESAP = ? WHERE HESAP = ? ";
		String SQL_UPD_HESAP_DETAY = "UPDATE HESAP_DETAY SET D_HESAP = ? WHERE D_HESAP = ?";
		try (Connection con = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(),
				cariConnDetails.getUsername(),
				cariConnDetails.getPassword())) {
			boolean oldAuto = con.getAutoCommit();
			con.setAutoCommit(false);
			try {
				try (PreparedStatement ps1 = con.prepareStatement(SQL_UPD_HESAP);
						PreparedStatement ps2 = con.prepareStatement(SQL_UPD_HESAP_DETAY)) {
					ps1.setNString(1, yenikod);
					ps1.setNString(2, eskikod);
					ps1.executeUpdate();
					ps2.setNString(1, yenikod);
					ps2.setNString(2, eskikod);
					ps2.executeUpdate();
				}
				con.commit();
			} catch (Exception ex) {
				try { con.rollback(); } catch (Exception ignore) {}
				throw new ServiceException("Hesap kodu değiştirme sırasında hata", ex);
			} finally {
				try { con.setAutoCommit(oldAuto); } catch (Exception ignore) {}
			}
		} catch (SQLException e) {
			throw new ServiceException("Bağlantı hatası", e);
		}
	}

	@Override
	public void cari_kod_degis_satirlar(String eskikod, String yenikod, ConnectionDetails cariConnDetails) {
		String sql = "UPDATE SATIRLAR SET HESAP = ? WHERE HESAP = ?";
		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setNString(1,yenikod);
			stmt.setNString(2,eskikod);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	@Override
	public void cari_kod_degis_tahsilat(String eskikod, String yenikod, ConnectionDetails cariConnDetails) {
		String sql = "UPDATE TAH_DETAY SET C_HES = ? WHERE C_HES = ? ";
		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setNString(1,yenikod);
			stmt.setNString(2,eskikod);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	@Override
	public List<Map<String, Object>> kasa_kontrol(String hesap, String t1, ConnectionDetails cariConnDetails) {
		String sql =  "SELECT S.EVRAK, I.IZAHAT AS IZAHAT, S.KOD, S.BORC, S.ALACAK, S.[USER] " +
				"FROM SATIRLAR S " +
				"JOIN IZAHAT I ON S.EVRAK = I.EVRAK " +
				"WHERE S.HESAP = ? " +
				"AND S.TARIH >= ? " +
				"AND S.TARIH < ? " +
				"ORDER BY S.EVRAK";
		Timestamp ts[] = Global_Yardimci.rangeDayT2plusDay(t1, t1);

		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			int i = 1;
			preparedStatement.setNString(i++, hesap);
			preparedStatement.setTimestamp(i++, ts[0]);
			preparedStatement.setTimestamp(i++, ts[1]);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return resultList; 
	}

	@Override
	public List<Map<String, Object>> kasa_mizan(String kod, String ilktarih, String sontarih,
			ConnectionDetails cariConnDetails) {
		String sql =  "SELECT S.HESAP, H.UNVAN, H.HESAP_CINSI, " 
				+ " SUM(S.BORC) AS islem, "
				+ " SUM(S.ALACAK) AS islem2, "
				+ " SUM(S.ALACAK - S.BORC) AS bakiye " 
				+ " FROM SATIRLAR S "
				+ " LEFT JOIN HESAP H ON S.HESAP = H.HESAP " 
				+ " WHERE S.HESAP = ? " 
				+ " AND S.TARIH >= ? "
				+ " AND S.TARIH <  ? "
				+ " GROUP BY S.HESAP, H.UNVAN, H.HESAP_CINSI " 
				+ " ORDER BY S.HESAP";
		LocalDate start = Global_Yardimci.toLocalDateSafe(ilktarih);
		LocalDate end = Global_Yardimci.toLocalDateSafe(sontarih);
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			int i = 1;
			preparedStatement.setNString(i++, kod);
			preparedStatement.setTimestamp(i++, Timestamp.valueOf(start.atStartOfDay()));
			preparedStatement.setTimestamp(i++, Timestamp.valueOf(end.atStartOfDay()));
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return resultList; 
	}
}