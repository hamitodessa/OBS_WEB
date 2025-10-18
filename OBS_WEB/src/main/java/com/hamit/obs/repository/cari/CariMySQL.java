package com.hamit.obs.repository.cari;

import java.sql.CallableStatement;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.enums.modulbaslikTipi;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.custom.yardimci.Tarih_Cevir;
import com.hamit.obs.dto.cari.dekontDTO;
import com.hamit.obs.dto.cari.dvzcevirmeDTO;
import com.hamit.obs.dto.cari.hesapplaniDTO;
import com.hamit.obs.dto.cari.mizanDTO;
import com.hamit.obs.dto.cari.tahayarDTO;
import com.hamit.obs.dto.cari.tahrapDTO;
import com.hamit.obs.dto.cari.tahsilatDTO;
import com.hamit.obs.dto.cari.tahsilatTableRowDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.user.UserService;

@Component
public class CariMySQL implements ICariDatabase{

	@Autowired
	private UserService userService;

	@Override
	public String[] hesap_adi_oku(String hesap,  ConnectionDetails cariConnDetails) {
		String[] firmaIsmi = {"",""};
		String query = "SELECT HESAP,HESAP_CINSI,KARTON,UNVAN FROM HESAP " + 
				" WHERE HESAP = N'" + hesap + "'";
		try (Connection connection =  DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
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
		int offset = page * pageSize;
		String tARIH = "";
		boolean hasDate = !("1900-01-01".equals(t1) && "2100-12-31".equals(t2));
		if (hasDate)
			tARIH = " AND TARIH >= ? AND TARIH < ? ";

		String sql = "SELECT SID,TARIH,SATIRLAR.EVRAK ," + 
				" IFNULL(IZAHAT.IZAHAT,'') AS IZAHAT,KOD,KUR,BORC,ALACAK," + 
				" SUM(ALACAK-BORC) OVER(ORDER BY TARIH ROWS BETWEEN UNBOUNDED PRECEDING And CURRENT ROW) AS BAKIYE,USER" + 
				" FROM SATIRLAR USE INDEX (IX_SATIRLAR)" + 
				" LEFT JOIN IZAHAT USE INDEX (IX_IZAHAT)" + 
				" ON SATIRLAR.EVRAK = IZAHAT.EVRAK " + 
				" WHERE HESAP = ? " + tARIH + 
				" ORDER BY TARIH" +
				" LIMIT " + pageSize + " OFFSET " + offset + " ";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, hesap);
			if (hasDate) {
				LocalDate startDate = Global_Yardimci.toLocalDateSafe(t1);
				LocalDate endDate = Global_Yardimci.toLocalDateSafe(t2);
				Timestamp ts1 = Timestamp.valueOf(startDate.atStartOfDay());
				Timestamp ts2 = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());
				preparedStatement.setTimestamp(2, ts1);
				preparedStatement.setTimestamp(3, ts2);
			}
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				resultList = ResultSetConverter.convertToList(resultSet);
			}
			if (!t1.equals("1900-01-01")) {
				Map<String, Object> newRow = new HashMap<>();
				double borc = 0;
				double alacak = 0;
				double bakiye =0;
				if(offset == 0) {
					List<Map<String, Object>> mizanList = ekstre_mizan(hesap,"1900-01-01",Tarih_Cevir.tarihEksi1(t1) + " 23:59:59.000","   ", "ZZZ","     ", "ZZZZZ",cariConnDetails);
					borc = (double) mizanList.get(0).getOrDefault("ISLEM", 0.0);
					alacak = (double) mizanList.get(0).getOrDefault("ISLEM2", 0.0);
					bakiye = alacak - borc;
					newRow.put("TARIH", Tarih_Cevir.stringtoDate(t1));
					newRow.put("EVRAK", 0);
					newRow.put("IZAHAT", "Devir");
					newRow.put("KOD", "");
					newRow.put("KUR", 0.0);
					newRow.put("BORC", borc);
					newRow.put("ALACAK", alacak);
					newRow.put("BAKIYE", bakiye);
					newRow.put("USER", "");
					resultList.add(0, newRow);
					borc = 0.00;
					alacak = 0.00;
					bakiye = 0.00;
				}
				else {
					List<Map<String, Object>> komplelist = eski_bakiye(hesap,resultList.get(0).get("TARIH").toString(),cariConnDetails);
					if (komplelist != null && !komplelist.isEmpty()) {
						for (int i = komplelist.size() - 1; i > 0; i--) {
							if (komplelist.get(i).get("SID").toString().equals(resultList.get(0).get("SID").toString())) {
								bakiye = (double) komplelist.get(i - 1).get("BAKIYE");
								komplelist.clear();
								komplelist = null;
								break;
							}
						}
					}
				}
				for (Map<String, Object> row : resultList) {
					borc = (double) row.getOrDefault("BORC", 0.0);
					alacak = (double) row.getOrDefault("ALACAK", 0.0);
					bakiye += alacak - borc;
					row.put("BAKIYE", bakiye);
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Ekstre okunamadı", e);
		}
		return resultList;
	}

	@Override
	public List<Map<String, Object>> eski_bakiye(String hesap,String t2,ConnectionDetails cariConnDetails){
		LocalDate endDate = Global_Yardimci.toLocalDateSafe(t2);
		Timestamp ts2 = Timestamp.valueOf(endDate.atStartOfDay());
		String sql = "SELECT SID,TARIH,BORC, ALACAK, " +
				" SUM(ALACAK - BORC) OVER(ORDER BY TARIH ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS BAKIYE" +
				" FROM SATIRLAR  " +
				" WHERE HESAP = ? " +
				" AND TARIH < ? " + 
				" ORDER BY TARIH";
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, hesap);
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
		LocalDate startDate = Global_Yardimci.toLocalDateSafe(t1);
		LocalDate endDate = Global_Yardimci.toLocalDateSafe(t2);
		Timestamp ts1 = Timestamp.valueOf(startDate.atStartOfDay());
		Timestamp ts2 = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());
		boolean hasDate = !("1900-01-01".equals(t1) && "2100-12-31".equals(t2));
		if (hasDate)
			tARIH = " AND TARIH >= ? AND TARIH < ? ";
		String sql = "SELECT COUNT(TARIH) as satir " +
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
			preparedStatement.setString(1, hesap);
			if (hasDate) {
				preparedStatement.setTimestamp(2, ts1);
				preparedStatement.setTimestamp(3, ts2);
			}
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next())
				result  = resultSet.getInt("satir");
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
		String sql = "SELECT SATIRLAR.HESAP,HESAP.UNVAN,HESAP.HESAP_CINSI,SUM(SATIRLAR.BORC) AS ISLEM,SUM(SATIRLAR.ALACAK) AS ISLEM2,SUM(SATIRLAR.ALACAK - SATIRLAR.BORC) AS BAKIYE" +
				" FROM SATIRLAR LEFT JOIN" +
				" HESAP ON SATIRLAR.HESAP = HESAP.HESAP " +
				" WHERE SATIRLAR.HESAP = ? " + 
				" AND SATIRLAR.TARIH >= ? AND SATIRLAR.TARIH < ? " + 
				" AND HESAP.HESAP_CINSI BETWEEN ?  AND  ?  " + 
				" AND HESAP.KARTON BETWEEN ? AND ? " +
				" GROUP BY SATIRLAR.HESAP,HESAP.UNVAN,HESAP.HESAP_CINSI" +
				" ORDER BY SATIRLAR.HESAP";
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			int i = 1;
			preparedStatement.setString(i++, kod);
			preparedStatement.setTimestamp(i++, Timestamp.valueOf(start.atStartOfDay()));
			preparedStatement.setTimestamp(i++, Timestamp.valueOf(end.atStartOfDay())); 
			preparedStatement.setNString(i++, ilkhcins);
			preparedStatement.setString(i++, sonhcins);
			preparedStatement.setString(i++, ilkkar);
			preparedStatement.setString(i++, sonkar);
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
			throw new ServiceException("MY CariService genel hatası.", e);
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
			throw new ServiceException("MY CariService genel hatası.", e);
		}
		return resultList; 
	}

	@Override
	public int cari_sonfisno(ConnectionDetails cariConnDetails) {
		int evrakNo = 0;
		String query = "SELECT MAX(EVRAK) AS MAX_NO FROM SATIRLAR";
		try (Connection connection =  DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next())
					evrakNo = resultSet.getInt("MAX_NO");
			}
		} catch (Exception e) {
			throw new ServiceException("Hesap adı okunamadı", e); 
		}
		return evrakNo;
	}

	@Override
	public boolean cari_dekont_kaydet(dekontDTO dBilgi, ConnectionDetails cariConnDetails) {
		String sql = "INSERT INTO SATIRLAR (HESAP,TARIH,H,EVRAK,CINS,KUR,BORC,ALACAK,KOD,USER) " +
				"VALUES (?,?,?,?,?,?,?,?,?,?)";
		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			addSatir(stmt, dBilgi.getBhes(), dBilgi.getTar(), "B", dBilgi.getFisNo(),
					dBilgi.getBcins(), dBilgi.getBkur(),
					Math.round(dBilgi.getBorc() * 100) / 100.0, 0, dBilgi.getKod(), dBilgi.getUser());
			addSatir(stmt, dBilgi.getAhes(), dBilgi.getTar(), "A", dBilgi.getFisNo(),
					dBilgi.getAcins(), dBilgi.getAkur(),
					0, Math.round(dBilgi.getAlacak() * 100) / 100.0, dBilgi.getKod(), dBilgi.getUser());
			stmt.executeBatch();
			addIzahat(connection, dBilgi.getFisNo(), dBilgi.getIzahat());
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
		return true;		
	}

	private void addSatir(PreparedStatement stmt, String hesap, String tarih, String h, int evrak,
			String cins, double kur, double borc, double alacak, String kod, String user) throws SQLException {
		stmt.setString(1, hesap);
		stmt.setTimestamp(2, Timestamp.valueOf(tarih));
		stmt.setString(3, h);
		stmt.setInt(4, evrak);
		stmt.setString(5, cins);
		stmt.setDouble(6, kur);
		stmt.setDouble(7, borc);
		stmt.setDouble(8, alacak);
		stmt.setString(9, kod);
		stmt.setString(10, user);
		stmt.addBatch();
	}

	private void addIzahat(Connection connection, int evrak, String izahat) throws SQLException {
		String sql = "INSERT INTO IZAHAT (EVRAK,IZAHAT) VALUES (?,?)";
		try (PreparedStatement stmt3 = connection.prepareStatement(sql)) {
			stmt3.setInt(1, evrak);
			stmt3.setString(2, izahat);
			stmt3.executeUpdate();
		}
	}

	@Override
	public List<dekontDTO> fiskon(int fisNo, ConnectionDetails cariConnDetails) {
		String sql = "SELECT HESAP,TARIH,H,SATIRLAR.EVRAK,CINS, KUR,BORC,ALACAK,IFNULL(IZAHAT,'') AS IZAHAT,KOD,USER" +
				" FROM SATIRLAR LEFT JOIN IZAHAT ON SATIRLAR.EVRAK = IZAHAT.EVRAK" +
				" WHERE SATIRLAR.EVRAK = '" + fisNo + "'" +
				" ORDER BY H DESC";
		List<dekontDTO> dekontDTO =  new ArrayList<>(); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
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
			resultSet.close();
		} catch (Exception e) {
			throw new ServiceException("MY CariService Evrak Okuma", e);
		}
		return dekontDTO; 
	}

	@Override
	public int yenifisno(ConnectionDetails cariConnDetails) {
		int evrakNo = 0;
		String sql = "{ CALL return_evrak(?) }";
		try (Connection connection =  DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				CallableStatement preparedStatement = connection.prepareCall(sql,
						ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setInt(1, 1);
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
						ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setString(1, mizanDTO.getHkodu1());
			preparedStatement.setString(2, mizanDTO.getHkodu2());
			preparedStatement.setString(3, mizanDTO.getCins1());
			preparedStatement.setString(4, mizanDTO.getCins2());
			preparedStatement.setString(5, mizanDTO.getKarton1());
			preparedStatement.setString(6, mizanDTO.getKarton2());
			int paramIndex = 7;
			if (hasDate) {
				LocalDate startDate = Global_Yardimci.toLocalDateSafe(mizanDTO.getStartDate());
				LocalDate endDate = Global_Yardimci.toLocalDateSafe(mizanDTO.getEndDate());
				Timestamp ts1 = Timestamp.valueOf(startDate.atStartOfDay());
				Timestamp ts2 = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());
				preparedStatement.setTimestamp(paramIndex++, ts1);
				preparedStatement.setTimestamp(paramIndex, ts2);
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
				psDetay.setString(1, hesap);
				psDetay.executeUpdate();
				psHesap.setString(1, hesap);
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
		String sql = "INSERT INTO HESAP (HESAP,UNVAN,KARTON,HESAP_CINSI,USER) " +
				" VALUES (?,?,?,?,?)" ;
		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1,hesapplaniDTO.getKodu());
			stmt.setString(2, hesapplaniDTO.getAdi());
			stmt.setString(3, hesapplaniDTO.getKarton());
			stmt.setString(4, hesapplaniDTO.getHcins());
			stmt.setString(5, hesapplaniDTO.getUsr());
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
			stmt.setString(1, hesapplaniDTO.getKodu());
			stmt.setString(2,hesapplaniDTO.getYetkili());
			stmt.setString(3, hesapplaniDTO.getAd1());
			stmt.setString(4, hesapplaniDTO.getAd2());
			stmt.setString(5, hesapplaniDTO.getSemt());
			stmt.setString(6, hesapplaniDTO.getSeh());
			stmt.setString(7, hesapplaniDTO.getVd());
			stmt.setString(8, hesapplaniDTO.getVn());
			stmt.setString(9, hesapplaniDTO.getT1());
			stmt.setString(10, hesapplaniDTO.getT2());
			stmt.setString(11, hesapplaniDTO.getT3());
			stmt.setString(12, hesapplaniDTO.getFx());
			stmt.setString(13, hesapplaniDTO.getO1());
			stmt.setString(14, hesapplaniDTO.getO2());
			stmt.setString(15, hesapplaniDTO.getO3());
			stmt.setString(16, hesapplaniDTO.getWeb());
			stmt.setString(17, hesapplaniDTO.getMail());
			stmt.setString(18, hesapplaniDTO.getKim());
			stmt.setString(19, hesapplaniDTO.getAcik());
			stmt.setBoolean(20, hesapplaniDTO.isSms());
			stmt.setBytes(21, hesapplaniDTO.getImage());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException("Veritabanı hatası: " + e.getErrorCode(), e);
		}
	}

	@Override
	public hesapplaniDTO hsp_pln(String hesap, ConnectionDetails cariConnDetails) {
		String sql = "SELECT HESAP,UNVAN,HESAP_CINSI,KARTON,YETKILI,ADRES_1,ADRES_2,SEMT,SEHIR,VERGI_DAIRESI," + 
				" VERGI_NO,FAX,TEL_1,TEL_2,TEL_3,OZEL_KOD_1,OZEL_KOD_2,OZEL_KOD_3,ACIKLAMA,TC_KIMLIK,WEB," + 
				" E_MAIL,SMS_GONDER,RESIM,USER" + 
				" FROM HESAP USE INDEX (IX_HESAP) LEFT OUTER JOIN HESAP_DETAY USE INDEX (IX_DHESAP) ON" + 
				" HESAP.HESAP = HESAP_DETAY.D_HESAP WHERE HESAP.HESAP = '" + hesap + "' ORDER BY HESAP";
		hesapplaniDTO hsdto = new hesapplaniDTO();
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
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
		LocalDate start = Global_Yardimci.toLocalDateSafe(mizanDTO.getStartDate());
		LocalDate end   = Global_Yardimci.toLocalDateSafe(mizanDTO.getEndDate());
		Timestamp tsStart = Timestamp.valueOf(start.atStartOfDay());
		Timestamp tsEnd   = Timestamp.valueOf(end.atStartOfDay());

		String havingCondition = "";
		if (mizanDTO.getHangi_tur().equals("Borclu Hesaplar")) {
			havingCondition = " HAVING BAKIYE < 0 ";
		} else if (mizanDTO.getHangi_tur().equals("Alacakli Hesaplar")) {
			havingCondition = " HAVING BAKIYE > 0 ";
		} else if (mizanDTO.getHangi_tur().equals("Bakiyesi 0 Olanlar")) {
			havingCondition = " HAVING BAKIYE = 0 ";
		} else if (mizanDTO.getHangi_tur().equals("Bakiyesi 0 Olmayanlar")) {
			havingCondition = " HAVING BAKIYE <> 0 ";
		}

		String sql =  "SELECT s.HESAP, h.UNVAN, h.HESAP_CINSI, " +
				"COALESCE(ROUND(ozet.ONCEKI_BAKIYE, 2), 0) AS ONCEKI_BAKIYE, " +
				"COALESCE(ROUND(donem.BORC, 2), 0) AS BORC, " +
				"COALESCE(ROUND(donem.ALACAK, 2), 0) AS ALACAK, " +
				"ROUND(COALESCE(donem.ALACAK, 0) - COALESCE(donem.BORC, 0), 2) AS BAK_KVARTAL, " +
				"ROUND(COALESCE(ozet.ONCEKI_BAKIYE, 0) + COALESCE(donem.ALACAK, 0) - COALESCE(donem.BORC, 0), 2) AS BAKIYE " +
				"FROM SATIRLAR s USE INDEX (IXS_HESAP) " +
				"LEFT JOIN HESAP h ON h.HESAP = s.HESAP " +
				"LEFT JOIN (" +
				"  SELECT HESAP, SUM(ALACAK) - SUM(BORC) AS ONCEKI_BAKIYE " +
				"  FROM SATIRLAR USE INDEX (IXS_HESAP) WHERE TARIH < ? GROUP BY HESAP " +
				") AS ozet ON ozet.HESAP = s.HESAP " +
				"LEFT JOIN (" +
				"  SELECT HESAP, SUM(BORC) AS BORC, SUM(ALACAK) AS ALACAK " +
				"  FROM SATIRLAR USE INDEX (IXS_HESAP) WHERE TARIH >= ? AND TARIH < ? GROUP BY HESAP " +
				") AS donem ON donem.HESAP = s.HESAP " +
				"WHERE s.HESAP > ? AND s.HESAP < ? " +
				"AND h.HESAP_CINSI BETWEEN ? AND ? " +
				"AND h.KARTON BETWEEN ? AND ? " +
				"GROUP BY s.HESAP, h.UNVAN, h.HESAP_CINSI, ozet.ONCEKI_BAKIYE, donem.BORC, donem.ALACAK " +
				havingCondition + " " +
				"ORDER BY s.HESAP ASC";

		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setTimestamp(1, tsStart);
			preparedStatement.setTimestamp(2, tsStart);
			preparedStatement.setTimestamp(3, tsEnd);
			preparedStatement.setString(4, mizanDTO.getHkodu1());
			preparedStatement.setString(5, mizanDTO.getHkodu2());
			preparedStatement.setString(6, mizanDTO.getCins1());
			preparedStatement.setString(7, mizanDTO.getCins2());
			preparedStatement.setString(8, mizanDTO.getKarton1());
			preparedStatement.setString(9, mizanDTO.getKarton2());
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				resultList = ResultSetConverter.convertToList(resultSet);
			}
		} catch (SQLException e) {
			throw new ServiceException("Mizan okunamadı", e);
		}
		return resultList;
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
				str1 = modulbaslikTipi.OK_Kur.name().toLowerCase() + kurConnectionDetails.getDatabaseName() + ".kurlar as k";
			} else {
				User user = userService.getCurrentUser();
				String usrString = user.getFirstName().length() > 15 
						? user.getFirstName().substring(0, 15) 
								: user.getFirstName();
				create_federated(cariConnDetails, kurConnectionDetails, usrString);
				str1 = "obs_federated." + usrString + "_kurlar_federated AS k USE INDEX (IX_KUR)";
			}

			LocalDate startDate = Global_Yardimci.toLocalDateSafe(dvzcevirmeDTO.getStartDate());
			LocalDate endDate = Global_Yardimci.toLocalDateSafe(dvzcevirmeDTO.getEndDate());
			Timestamp ts1 = Timestamp.valueOf(startDate.atStartOfDay());
			Timestamp ts2 = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());
			String tarihFilter = "";
			boolean hasDate = !("1900-01-01".equals(dvzcevirmeDTO.getStartDate()) && "2100-12-31".equals(dvzcevirmeDTO.getEndDate()));
			if (hasDate)
				tarihFilter = " AND s.TARIH  >= ?  AND s.TARIH < ? ";

			String islem = "/";
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DATE(s.TARIH) as TARIH, s.EVRAK, IFNULL(I.IZAHAT, '') AS IZAHAT, ")
			.append("IFNULL(IF(k.")
			.append(dvzcevirmeDTO.getDvz_cins())
			.append(" = 0, 1, k.")
			.append(dvzcevirmeDTO.getDvz_cins())
			.append("), 1) AS CEV_KUR, ")
			.append("((s.ALACAK - s.BORC) ").append(islem)
			.append(" IFNULL(NULLIF(k.").append(dvzcevirmeDTO.getDvz_cins()).append(", 0), 1)) AS DOVIZ_TUTAR, ")
			.append("SUM((s.ALACAK - s.BORC) ").append(islem)
			.append(" IFNULL(NULLIF(k.").append(dvzcevirmeDTO.getDvz_cins()).append(", 0), 1)) OVER(ORDER BY s.TARIH ")
			.append("ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS DOVIZ_BAKIYE, ")
			.append("SUM(s.ALACAK - s.BORC) OVER(ORDER BY s.TARIH ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS BAKIYE, ")
			.append("s.KUR, BORC, ALACAK, s.USER ")
			.append("FROM SATIRLAR AS s USE INDEX (IX_SATIRLAR) ")
			.append("LEFT OUTER JOIN IZAHAT AS I USE INDEX (IX_IZAHAT) ON s.EVRAK = I.EVRAK ")
			.append("LEFT OUTER JOIN ")
			.append(str1).append(" ON DATE(k.TARIH) = DATE(s.TARIH) ")
			.append("WHERE s.HESAP = ? AND (k.KUR IS NULL OR k.KUR = ?) ");

			if (hasDate)
				sql.append( tarihFilter );

			sql.append("ORDER BY s.TARIH");
			sql.append(" LIMIT " + pageSize + " OFFSET " + offset + " ");
			try (Connection connection = DriverManager.getConnection(
					cariConnDetails.getJdbcUrl(), 
					cariConnDetails.getUsername(), 
					cariConnDetails.getPassword());
					PreparedStatement stmt = connection.prepareStatement(sql.toString(),
							ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
				stmt.setString(1, dvzcevirmeDTO.getHesapKodu());
				stmt.setString(2, dvzcevirmeDTO.getDvz_tur());
				if (hasDate) {
					stmt.setTimestamp(3, ts1);
					stmt.setTimestamp(4, ts2);
				}
				try (ResultSet rss = stmt.executeQuery()) {
					resultList = ResultSetConverter.convertToList(rss);
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Bilinmeyen bir hata oluştu: " + e.getMessage(), e);
		}
		return resultList;
	}

	@Override
	public double dvz_raporsize(dvzcevirmeDTO dvzcevirmeDTO, ConnectionDetails cariConnDetails,ConnectionDetails kurConnectionDetails) {
		long count = 0;
		try {
			LocalDate startDate = Global_Yardimci.toLocalDateSafe(dvzcevirmeDTO.getStartDate());
			LocalDate endDate   = Global_Yardimci.toLocalDateSafe(dvzcevirmeDTO.getEndDate());
			Timestamp ts1 = Timestamp.valueOf(startDate.atStartOfDay());
			Timestamp ts2 = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()); // [start, end+1)
			boolean hasDate = !("1900-01-01".equals(dvzcevirmeDTO.getStartDate())
					&& "2100-12-31".equals(dvzcevirmeDTO.getEndDate()));
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT COUNT(*) AS satir ")
			.append("FROM SATIRLAR ")
			.append("WHERE HESAP = ? ");
			if (hasDate) {
				sql.append("AND TARIH >= ? AND TARIH < ? ");
			}
			try (Connection con = DriverManager.getConnection(
					cariConnDetails.getJdbcUrl(),
					cariConnDetails.getUsername(),
					cariConnDetails.getPassword());
					PreparedStatement stmt = con.prepareStatement(sql.toString())) {
				int idx = 1;
				stmt.setString(idx++, dvzcevirmeDTO.getHesapKodu());
				if (hasDate) {
					stmt.setTimestamp(idx++, ts1);
					stmt.setTimestamp(idx++, ts2);
				}
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						count = rs.getLong("satir");
					}
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Dvz Cevirme (rapor size) sırasında hata: " + e.getMessage(), e);
		}
		return (double) count;	
	}

	private  void create_federated(ConnectionDetails cariConnDetails, 
			ConnectionDetails kurConnectionDetails,String usrString )   {
		try {
			Connection con = null;
			String cumle = "";

			cumle = "jdbc:mysql://" + cariConnDetails.getServerIp();
			con = DriverManager.getConnection(cumle,cariConnDetails.getUsername(),cariConnDetails.getPassword());
			Statement stmt = null;
			String sql =null;
			sql = "CREATE DATABASE IF NOT EXISTS obs_federated" ;
			stmt = con.createStatement();  
			stmt.execute(sql);
			stmt.close();
			String kurserstr = "jdbc:mysql://" + kurConnectionDetails.getServerIp() ;
			sql =  " CREATE TABLE IF NOT EXISTS " + usrString + "_kurlar_federated(" 
					+ " Kur varchar(3),Tarih DATE ,"
					+ " MA DOUBLE,MS DOUBLE,SA DOUBLE,SS DOUBLE,BA DOUBLE,BS DOUBLE," 
					+ " INDEX IX_KUR (Tarih ASC) VISIBLE)" 
					+ " ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci" 
					+ " CONNECTION='mysql://" + kurConnectionDetails.getUsername() + ":" 
					+ kurConnectionDetails.getPassword() + "@" + kurserstr + "/KURLAR'; " ;

			cumle = cumle + "/obs_federated" ;
			con = DriverManager.getConnection(cumle,cariConnDetails.getUsername(),cariConnDetails.getPassword());
			stmt = con.createStatement();
			stmt.executeUpdate("DROP TABLE IF EXISTS " + usrString + "_kurlar_federated;");
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (Exception e) {
			throw new ServiceException("Bilinmeyen bir hata oluştu: " + e.getMessage(), e);
		}
	}

	@Override
	public List<Map<String, Object>> banka_sube(String nerden, ConnectionDetails cariConnDetails) {
		String sql = "SELECT DISTINCT " + nerden +  " AS " +  nerden.toUpperCase() +
				" FROM TAH_CEK" +
				" ORDER BY " + nerden;
		List<Map<String, Object>> resultList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql, 
						ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			try (ResultSet rs = preparedStatement.executeQuery()) {
				resultList = ResultSetConverter.convertToList(rs);
			}
		} catch (Exception e) {
			throw new ServiceException("MY CariService genel hatası.", e);
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
						ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			preparedStatement.setNString(1, fisNo);
			preparedStatement.setInt(2, tah_ted);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
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
			}
		} catch (Exception e) {
			throw new ServiceException("MY CariService Evrak Okuma", e);
		}
		return dto; 
	}

	@Override
	public List<Map<String, Object>> tah_cek_doldur(String fisNo, Integer tah_ted, ConnectionDetails cariConnDetails) {
		String sql = "SELECT * FROM TAH_CEK WHERE EVRAK = ? AND CINS = ? ";
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
		String query = "SELECT MAX(CONVERT(EVRAK,UNSIGNED)) AS MAX_NO FROM TAH_DETAY WHERE CINS ='" + tah_ted + "'";
		try (Connection connection =  DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next())
					evrakNo = resultSet.getInt("MAX_NO");
			}
		} catch (Exception e) {
			throw new ServiceException("Hesap adı okunamadı", e); 
		}
		return evrakNo;
	}

	@Override
	public int cari_tah_fisno_al(String tah_ted, ConnectionDetails cariConnDetails) {
		int evrakNo = 0;
		String query = "CALL return_tah_evrak(?,?);";
		try (Connection connection =  DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			preparedStatement.setInt(1, 1);
			preparedStatement.setString(2, tah_ted);
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
			stmt.setString(1, fisno);
			stmt.setInt(2, tah_ted);
			stmt.setString(3, tahsilatTableRowDTO.getBanka());
			stmt.setString(4, tahsilatTableRowDTO.getSube());
			stmt.setString(5, tahsilatTableRowDTO.getSeri() );
			stmt.setString(6, tahsilatTableRowDTO.getHesap() );
			stmt.setString(7, tahsilatTableRowDTO.getBorclu());
			stmt.setDate(8, java.sql.Date.valueOf(tahsilatTableRowDTO.getTarih()));
			stmt.setDouble(9, tahsilatTableRowDTO.getTutar());
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}

	@Override
	public void tah_sil(String fisno, Integer tah_ted, ConnectionDetails cariConnDetails) {
		String sql1 = "DELETE FROM TAH_CEK WHERE EVRAK = '"+ fisno + "' AND CINS = '" + tah_ted + "'" ;
		String sql = "DELETE FROM TAH_DETAY WHERE EVRAK = '" + fisno + "' AND CINS = '" + tah_ted + "'" ;
		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement deleteStmt = connection.prepareStatement(sql1);
				PreparedStatement insertStmt = connection.prepareStatement(sql)) {
			deleteStmt.executeUpdate();
			insertStmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
		}
	}

	@Override
	public List<Map<String, Object>> tah_listele(tahrapDTO tahrapDTO, ConnectionDetails cariConnDetails,ConnectionDetails adresConnectionDetails) {
		List<Map<String, Object>> resultList = new ArrayList<>();
		try {
			String cinString = "" , turString="" ,posString = "" ;
			if(tahrapDTO.getTah_ted() !=0)
				cinString = " CINS = '" + (tahrapDTO.getTah_ted() - 1) + "' AND";
			if(tahrapDTO.getHangi_tur() != 0)
				turString = " TUR = '" + (tahrapDTO.getHangi_tur() - 1) + "' AND";
			if(! tahrapDTO.getPos().equals(""))
				posString = " POS_BANKA = '" + tahrapDTO.getPos() + "' AND";

			if (!cariConnDetails.getSqlTipi().equals(adresConnectionDetails.getSqlTipi())) {
				throw new ServiceException("Cari Dosya ve Adres Dosyası farklı SQL sunucularında yer alıyor.");
			}
			String str1 = "";
			if (cariConnDetails.getServerIp().equals(adresConnectionDetails.getServerIp())) {
				str1=  modulbaslikTipi.OK_Adr.name() + adresConnectionDetails.getDatabaseName() + ".Adres as adr ";
			}
			else
			{  
				User user = userService.getCurrentUser();
				String usrString = user.getFirstName().length() > 15 
						? user.getFirstName().substring(0, 15) 
								: user.getFirstName();
				create_federated(cariConnDetails, adresConnectionDetails, usrString);
				str1 = "obs_federated." + usrString + "_adres_federated";
			}
			String sql = "SELECT EVRAK,TARIH,C_HES as CARI_HESAP," + 
					" (SELECT UNVAN FROM HESAP WHERE HESAP = C_HES ) AS UNVAN,"+ 
					" A_HES as ADRES_HESAP," + 
					" IFNULL(Adi,'') AS ADRES_UNVAN," +
					" CASE CINS WHEN '0' THEN 'Tahsilat' WHEN '1' THEN 'Tediye' END as CINS," +
					" CASE TUR  WHEN '0' THEN 'Nakit' WHEN '1' THEN 'Cek' WHEN '2' THEN 'Kredi Kartı' END as TUR,POS_BANKA," +
					" DVZ_CINS,TUTAR" +
					" FROM TAH_DETAY left join " + str1 + " on adr.M_Kodu = A_HES" + 
					" WHERE " + cinString  + turString  + posString +
					" TARIH >= ? AND TARIH <  ? " + 
					" AND EVRAK >= ? AND EVRAK <= ? " + 
					" AND C_HES >= ? AND C_HES <= ? " + 
					" ORDER BY TARIH,EVRAK" ;
			LocalDate start = Global_Yardimci.toLocalDateSafe(tahrapDTO.getStartDate());
			LocalDate end   = Global_Yardimci.toLocalDateSafe(tahrapDTO.getEndDate());
			Timestamp tsStart = Timestamp.valueOf(start.atStartOfDay());
			Timestamp tsEnd   = Timestamp.valueOf(end.plusDays(1).atStartOfDay());

			try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
					PreparedStatement preparedStatement = connection.prepareStatement(sql,
							ResultSet.TYPE_FORWARD_ONLY,
							ResultSet.CONCUR_READ_ONLY)) {
				preparedStatement.setTimestamp(1, tsStart);
				preparedStatement.setTimestamp(2, tsEnd);
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
			if (rs.next()) {
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
		String sql = "INSERT INTO TAH_AYARLAR (FIR_ISMI,ADR_1,ADR_2,VD_VN,MAIL,DIGER,LOGO,KASE)" +
				" VALUES (?,?,?,?,?,?,?,?)" ;
		String deletesql=  "DELETE FROM TAH_AYARLAR " ;
		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement deleteStmt = connection.prepareStatement(deletesql);
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1,tahayarDTO.getAdi());
			stmt.setString(2, tahayarDTO.getAd1());
			stmt.setString(3, tahayarDTO.getAd2());
			stmt.setString(4, tahayarDTO.getVdvn());
			stmt.setString(5, tahayarDTO.getMail());
			stmt.setString(6, tahayarDTO.getDiger());
			stmt.setBytes(7, tahayarDTO.getImagelogo());
			stmt.setBytes(8, tahayarDTO.getImagekase());
			deleteStmt.executeUpdate();
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Kayıt sırasında bir hata oluştu", e);
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
			throw new ServiceException( e.getMessage());
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
				+ " WHERE TC.EVRAK = '" +fisno + "' AND TC.CINS = '" + tah_ted + "'";
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException( e.getMessage());
		}
		return resultList; 
	}

	@Override
	public void cari_firma_adi_kayit(String fadi, ConnectionDetails cariConnDetails) {
		String sql = "UPDATE OZEL SET FIRMA_ADI = ? " ;
		try (Connection connection = DriverManager.getConnection(
				cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1,fadi);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException( e.getMessage());
		}
	}

	@Override
	public List<Map<String, Object>> hsppln_liste(ConnectionDetails cariConnDetails) {
		String sql = "SELECT  HESAP,UNVAN,HESAP_CINSI,KARTON,YETKILI,ADRES_1,ADRES_2,SEMT,SEHIR,VERGI_DAIRESI," + 
				" VERGI_NO,FAX,TEL_1,TEL_2,TEL_3,OZEL_KOD_1,OZEL_KOD_2,OZEL_KOD_3,ACIKLAMA,TC_KIMLIK,WEB," + 
				" E_MAIL,SMS_GONDER,RESIM ,USER " + 
				" FROM HESAP LEFT OUTER JOIN HESAP_DETAY ON" + 
				" HESAP.HESAP = HESAP_DETAY.D_HESAP ORDER BY HESAP";
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultList = ResultSetConverter.convertToList(resultSet); 
		} catch (Exception e) {
			throw new ServiceException( e.getMessage());
		}
		return resultList;
	}

	@Override
	public int hesap_plani_kayit_adedi(ConnectionDetails cariConnDetails) {
		int kayitSayi = 0;
		String query = "SELECT COUNT(*) AS SAYI FROM HESAP";
		try (Connection connection =  DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(query,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next())
					kayitSayi = resultSet.getInt("SAYI");
			}
		} catch (Exception e) {
			throw new ServiceException( e.getMessage());
		}
		return kayitSayi;
	}

	@Override
	public void cari_kod_degis_hesap(String eskikod, String yenikod, ConnectionDetails cariConnDetails) {
		String SQL_UPD_HESAP = "UPDATE HESAP SET HESAP = ? WHERE HESAP = ?";
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
					ps1.setString(1, yenikod);
					ps1.setString(2, eskikod);
					ps1.executeUpdate();
					ps2.setString(1, yenikod);
					ps2.setString(2, eskikod);
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
			stmt.setString(1,yenikod);
			stmt.setString(2,eskikod);
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
			stmt.setString(1,yenikod);
			stmt.setString(2,eskikod);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	@Override
	public List<Map<String, Object>> kasa_kontrol(String hesap, String t1, ConnectionDetails cariConnDetails) {
		String sql =  "SELECT SATIRLAR.EVRAK,IZAHAT,KOD,BORC,ALACAK,[USER]" +
				" FROM SATIRLAR,IZAHAT" +
				" WHERE SATIRLAR.EVRAK = IZAHAT.EVRAK and HESAP = ? " +
				" AND TARIH >= AND TARIH < " +
				" ORDER BY SATIRLAR.EVRAK";
		LocalDate start = Global_Yardimci.toLocalDateSafe(t1);
		Timestamp tsStart = Timestamp.valueOf(start.atStartOfDay());
		Timestamp tsEnd   = Timestamp.valueOf(start.plusDays(1).atStartOfDay());
		List<Map<String, Object>> resultList = new ArrayList<>(); 
		try (Connection connection = DriverManager.getConnection(cariConnDetails.getJdbcUrl(), cariConnDetails.getUsername(), cariConnDetails.getPassword());
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
			int i = 1;
			preparedStatement.setString(i++, hesap);
			preparedStatement.setTimestamp(i++, tsStart);
			preparedStatement.setTimestamp(i++, tsEnd);
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
		String sql =  "SELECT SATIRLAR.HESAP,HESAP.UNVAN,HESAP.HESAP_CINSI, SUM(SATIRLAR.BORC) AS islem, SUM(SATIRLAR.ALACAK) AS islem2, SUM(SATIRLAR.ALACAK - SATIRLAR.BORC) AS bakiye" +
				" FROM SATIRLAR LEFT JOIN" +
				" HESAP ON SATIRLAR.HESAP = HESAP.HESAP" +
				" WHERE SATIRLAR.HESAP = ? " + 
				" AND SATIRLAR.TARIH >= ? AND SATIRLAR.TARIH < ? " +
				" GROUP BY SATIRLAR.HESAP,HESAP.UNVAN,HESAP.HESAP_CINSI" +
				" ORDER BY SATIRLAR.HESAP";
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