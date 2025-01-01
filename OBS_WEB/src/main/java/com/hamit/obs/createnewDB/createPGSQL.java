package com.hamit.obs.createnewDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.hamit.obs.dto.server.serverBilgiDTO;


public class createPGSQL {

	public boolean serverKontrol(serverBilgiDTO sbilgi) {
		boolean result = false;
		String connectionString =  "jdbc:postgresql://" + sbilgi.getUser_ip() + "/" + sbilgi.getSuperviser() ;
		try (Connection conn = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server())) {
			DriverManager.setLoginTimeout(5);
			result = true; 
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	public boolean dosyaKontrol(serverBilgiDTO sbilgi) {
		boolean result = false;
		String connectionString =  "jdbc:postgresql://" + sbilgi.getUser_ip() + "/" + sbilgi.getSuperviser();
		String query = "SELECT datname FROM pg_database WHERE datname = ?";  
		try (Connection conn = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
				PreparedStatement stmt = conn.prepareStatement(query)) {
			DriverManager.setLoginTimeout(5);
			stmt.setString(1, sbilgi.getUser_modul_baslik().toLowerCase() + sbilgi.getUser_prog_kodu());
			try (ResultSet rs = stmt.executeQuery()) {
				rs.next();
				int count=0;
				count = rs.getRow();
				result = count > 0 ;
			}
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	public boolean tableKontrolS(serverBilgiDTO sbilgi, int beklenenTabloSayisi) {
		boolean result = false;
		String connectionString = "jdbc:postgresql://" + sbilgi.getUser_ip() + "/" + sbilgi.getSuperviser();
		String query = "SELECT COUNT(*) AS sayi " 
				+ " FROM information_schema.tables "
				+ " WHERE table_catalog = '" + sbilgi.getUser_modul_baslik().toLowerCase() + sbilgi.getUser_prog_kodu() + "' AND table_schema = 'public';";
		String query2 = "CREATE EXTENSION IF NOT EXISTS dblink;";
		String query3 = "CREATE EXTENSION IF NOT EXISTS tablefunc;";

		try (Connection conn = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
				PreparedStatement stmt = conn.prepareStatement(query);
				PreparedStatement stmt2 = conn.prepareStatement(query2);
				PreparedStatement stmt3 = conn.prepareStatement(query3);
				ResultSet rs = stmt.executeQuery()) {
			DriverManager.setLoginTimeout(5);
			if (rs.next()) {
				result = rs.getInt("sayi") == beklenenTabloSayisi;
			}
			stmt2.executeUpdate();
			stmt3.executeUpdate();
		} catch (SQLException e) {
			result = false;
		}
		return result;
	}

	public boolean dosyaOlustur(serverBilgiDTO sbilgi) {
		boolean result = false;
		String veritabaniAdi = sbilgi.getUser_modul_baslik().toLowerCase() + sbilgi.getUser_prog_kodu();
		String connectionString =  "jdbc:postgresql://" +  sbilgi.getUser_ip() + "/" + sbilgi.getSuperviser() ;
		String createDatabaseQuery = "CREATE DATABASE " + veritabaniAdi +
				" WITH " +
				" OWNER = postgres " +
				" ENCODING = 'UTF8' " +
				" LC_COLLATE = 'tr_TR.UTF-8' " +
				" LC_CTYPE = 'tr_TR.UTF-8' " +
				" LOCALE_PROVIDER = 'libc' " +
				" TABLESPACE = pg_default " +
				" CONNECTION LIMIT = -1 " +
				" IS_TEMPLATE = False " +
				" TEMPLATE template0 ";
		try (Connection initialConnection = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
				Statement stmt = initialConnection.createStatement()) {
			stmt.executeUpdate(createDatabaseQuery);
			String databaseConnectionString = "jdbc:postgresql://" + sbilgi.getUser_ip() + "/" + veritabaniAdi;
			try (Connection databaseConnection = DriverManager.getConnection(databaseConnectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server())) {
				switch (sbilgi.getUser_modul()) {
				case "Cari Hesap": {
					createTableCari(databaseConnection, sbilgi.getFirma_adi() , sbilgi.getUser_name());
					break;
				}
				case "Kur": {
					createTableKur(databaseConnection);
					break;
				}
				case "Adres": {
					createTableAdres(databaseConnection, sbilgi.getFirma_adi() , sbilgi.getUser_name());
					break;
				}
				case "Kambiyo": {
					createTableKambiyo(databaseConnection, sbilgi.getFirma_adi() , sbilgi.getUser_name());
					break;
				}
				}

			}
			sifirdan_LOG(sbilgi);
			result = true;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	public void createTableCari(Connection connection, String firmaAdi , String user_name) throws Exception {
		String sql = null;
		sql = "CREATE TABLE \"HESAP\" ("
				+ " \"HESAP\" CHARACTER VARYING (12) NOT NULL,"
				+ " \"UNVAN\" CHARACTER VARYING (50) NULL,"
				+ " \"KARTON\" CHARACTER VARYING (5) NULL,"
				+ " \"HESAP_CINSI\" CHARACTER VARYING (3) NULL,"
				+ " \"USER\" CHARACTER VARYING (15) NULL,"
				+ " CONSTRAINT HESAP_pkey PRIMARY KEY (\"HESAP\"))";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE INDEX IF NOT EXISTS IX_HESAP ON \"HESAP\" (\"HESAP\") " ;
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}

		sql = "CREATE TABLE \"HESAP_DETAY\" ("
				+ " \"D_HESAP\" CHARACTER VARYING (12) NOT NULL PRIMARY KEY,"
				+ " \"YETKILI\" CHARACTER VARYING (30) NULL,"
				+ " \"TC_KIMLIK\" CHARACTER VARYING (15) NULL,"
				+ " \"ADRES_1\" CHARACTER VARYING (35) NULL,"
				+ " \"ADRES_2\" CHARACTER VARYING (35) NULL,"
				+ " \"SEMT\" CHARACTER VARYING (15) NULL,"
				+ " \"SEHIR\" CHARACTER VARYING (15) NULL,"
				+ " \"VERGI_DAIRESI\" CHARACTER VARYING (25) NULL,"
				+ " \"VERGI_NO\" CHARACTER VARYING (15) NULL,"
				+ " \"FAX\" CHARACTER VARYING (25) NULL,"
				+ " \"TEL_1\" CHARACTER VARYING (25) NULL,"
				+ " \"TEL_2\" CHARACTER VARYING (25) NULL,"
				+ " \"TEL_3\" CHARACTER VARYING (25) NULL,"
				+ " \"OZEL_KOD_1\" CHARACTER VARYING (15) NULL,"
				+ " \"OZEL_KOD_2\" CHARACTER VARYING (15) NULL,"
				+ " \"OZEL_KOD_3\" CHARACTER VARYING (15) NULL,"
				+ " \"ACIKLAMA\" CHARACTER VARYING (30) NULL,"
				+ " \"WEB\" CHARACTER VARYING (50) NULL,"
				+ " \"E_MAIL\" CHARACTER VARYING (30) NULL,"
				+ " \"SMS_GONDER\" BOOLEAN,"
				+ " \"RESIM\" BYTEA NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"SATIRLAR\" ("
				+ "  \"SID\" SERIAL PRIMARY KEY,"
				+ "  \"HESAP\" CHARACTER VARYING (12) NOT NULL,"
				+ "  \"TARIH\" TIMESTAMP NULL,"
				+ "  \"H\" CHARACTER VARYING (1) NULL,"
				+ "  \"EVRAK\" INT NOT NULL,"
				+ "  \"CINS\" CHARACTER VARYING (2) NULL,"
				+ "  \"KUR\" DOUBLE PRECISION NULL,"
				+ "  \"BORC\" DOUBLE PRECISION NULL,"
				+ "  \"ALACAK\" DOUBLE PRECISION NULL,"
				+ "  \"KOD\" CHARACTER VARYING (5) NULL,"
				+ "  \"USER\" CHARACTER VARYING (15) NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE INDEX  \"IX_SATIRLAR\"  "
				+ "	ON \"SATIRLAR\" (\"HESAP\",\"TARIH\",\"EVRAK\")";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  \"IZAHAT\" ("
				+ " \"EVRAK\" INT NOT NULL PRIMARY KEY,"
				+ " \"IZAHAT\" CHARACTER VARYING (100) NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"EVRAK_NO\" (\"EID\" SERIAL PRIMARY KEY,\"EVRAK\" int)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"OZEL\" ("
				+ " \"OZID\" SERIAL PRIMARY KEY,"
				+ " \"YONETICI\" CHARACTER VARYING (25) NULL,"
				+ " \"YON_SIFRE\" CHARACTER VARYING (15) NULL,"
				+ " \"FIRMA_ADI\" CHARACTER VARYING (50) NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"YETKILER\" ("
				+ " \"YETID\" SERIAL PRIMARY KEY,"
				+ " \"KULLANICI\" CHARACTER VARYING (25) NULL,"
				+ " \"KARTON\" CHARACTER VARYING (5) NULL,"
				+ " \"TAM_YETKI\" BOOLEAN NULL,"
				+ " \"GORUNTU\" BOOLEAN NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql= "CREATE TABLE \"ANA_GRUP_DEGISKEN\" ( "
				+ " \"ANA_GRUP\" CHARACTER VARYING (25) NOT NULL PRIMARY KEY,"
				+ " \"USER\" CHARACTER VARYING (15) NOT NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"ALT_GRUP_DEGISKEN\" ( "
				+ " \"ANA_GRUP\" int  NOT NULL,"
				+ " \"ALT_GRUP\" CHARACTER VARYING  (25) NOT NULL,"
				+ " \"USER\" CHARACTER VARYING (15) NOT NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		//TAHSIL FISI
		sql = "CREATE TABLE \"TAH_EVRAK\" (\"CINS\" CHARACTER VARYING (3),\"NO\" INT )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"TAH_AYARLAR\" ( " +
				"  \"LOGO\" BYTEA NULL," +
				"  \"FIR_ISMI\" CHARACTER VARYING (50) NULL, " +
				"  \"ADR_1\" CHARACTER VARYING (50) NULL," +
				"  \"ADR_2\" CHARACTER VARYING (50) NULL," +
				"  \"VD_VN\" CHARACTER VARYING (60) NULL," +
				"  \"MAIL\" CHARACTER VARYING  (60) NULL," +
				"  \"DIGER\" CHARACTER VARYING (50) NULL," + 
				"  \"KASE\" BYTEA NULL ) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"TAH_DETAY\" (" +
				"  \"EVRAK\" CHARACTER VARYING (15) NOT NULL," +
				"  \"TARIH\" TIMESTAMP  NULL," +
				"  \"C_HES\" CHARACTER VARYING (12) NULL," +
				"  \"A_HES\" CHARACTER VARYING (12) NULL," +
				"  \"CINS\" SMALLINT NOT NULL," +
				"  \"TUTAR\" DOUBLE PRECISION," +
				"  \"TUR\" SMALLINT NOT NULL," +
				"  \"ACIKLAMA\" CHARACTER VARYING (50) NULL," +
				"  \"DVZ_CINS\" CHARACTER VARYING (3) NULL," +
				"  \"POS_BANKA\" CHARACTER VARYING (40) NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE INDEX \"IX_TAH_DETAY\" ON \"TAH_DETAY\" (\"EVRAK\")";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"TAH_CEK\" (" + 
				" \"EVRAK\" CHARACTER VARYING (15)," + 
				" \"CINS\" SMALLINT," + 
				" \"BANKA\" CHARACTER VARYING (40)," + 
				" \"SUBE\" CHARACTER VARYING (40)," + 
				" \"SERI\" CHARACTER VARYING (20)," + 
				" \"HESAP\" CHARACTER VARYING (20)," + 
				" \"BORCLU\" CHARACTER VARYING (40)," + 
				" \"TARIH\" DATE," + 
				" \"TUTAR\" DOUBLE PRECISION ) "  ;
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO \"TAH_EVRAK\" (\"CINS\",\"NO\") VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "GIR"); 
			stmt.setInt(2, 0); 
			stmt.executeUpdate();
		}
		sql = "INSERT INTO \"TAH_EVRAK\" (\"CINS\",\"NO\") VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "CIK"); 
			stmt.setInt(2, 0);   
			stmt.executeUpdate();
		}
		// ***************EVRAK NO YAZ ************
		sql = "INSERT INTO \"EVRAK_NO\" (\"EVRAK\") VALUES (?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, 0); 
			stmt.executeUpdate();
		}
		// ***************OZEL NO YAZ ************
		sql = "INSERT INTO \"OZEL\" (\"YONETICI\",\"YON_SIFRE\",\"FIRMA_ADI\") VALUES (?,? ,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, user_name);
			stmt.setString(2, "12345");
			stmt.setString(3, firmaAdi);
			stmt.executeUpdate();
		}
	}

	public void createTableKur(Connection connection) throws SQLException {
		String sql = null;
		sql = "CREATE TABLE \"KURLAR\"( \"ID\" SERIAL PRIMARY KEY ," 
				+ " \"KUR\" character varying(3),"
				+ " \"TARIH\" DATE ,"
				+ " \"MA\" DOUBLE PRECISION," 
				+ " \"MS\" DOUBLE PRECISION," 
				+ " \"SA\" DOUBLE PRECISION," 
				+ " \"SS\" DOUBLE PRECISION," 
				+ " \"BA\" DOUBLE PRECISION," 
				+ " \"BS\" DOUBLE PRECISION)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE INDEX  IX_KUR  "
				+ "	ON \"KURLAR\" (\"KUR\" ,\"TARIH\")";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
	}

	public void createTableAdres(Connection connection, String firmaAdi , String user_name) throws SQLException {
		String sql = null;
		sql = "CREATE TABLE \"ADRES\"( "
				+ " \"ID\" SERIAL  NOT NULL PRIMARY KEY ,"
				+ " \"M_KODU\" character varying(12)  NULL, "
				+ " \"ADI\" character varying(50) NULL,"
				+ " \"ADRES_1\" character varying (50) NULL,"
				+ " \"ADRES_2\" character varying(50) NULL,"
				+ " \"SEMT\" character varying(25) NULL,"
				+ " \"SEHIR\" character varying(25) NULL,"
				+ " \"POSTA_KODU\" character varying(10) NULL,"
				+ " \"VERGI_DAIRESI\" character varying(25) NULL,"
				+ " \"VERGI_NO\" character varying(15) NULL,"
				+ " \"FAX\" character varying(25) NULL,"
				+ " \"TEL_1\" character varying(25) NULL,"
				+ " \"TEL_2\" character varying(25) NULL,"
				+ " \"TEL_3\" character varying(25) NULL,"
				+ " \"OZEL\" character varying(30) NULL,"
				+ " \"YETKILI\" character varying(30) NULL,"
				+ " \"E_MAIL\" character varying(50) NULL,"
				+ " \"NOT_1\" character varying(30) NULL,"
				+ " \"NOT_2\" character varying(30) NULL,"
				+ " \"NOT_3\" character varying(30) NULL,"
				+ " \"ACIKLAMA\" character varying(50) NULL,"
				+ " \"SMS_GONDER\" BOOLEAN NULL,"
				+ " \"MAIL_GONDER\" BOOLEAN NULL,"
				+ " \"OZEL_KOD_1\" character varying(15) NULL,"
				+ " \"OZEL_KOD_2\" character varying(15) NULL,"
				+ " \"WEB\" character varying(50) NULL,"
				+ " \"USER\" character varying(15) NULL,"
				+ " \"RESIM\" BYTEA NULL )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  \"OZEL\" ("
				+ " \"OZID\"   SERIAL PRIMARY KEY,"
				+ " \"YONETICI\"   character varying (25) NULL,"
				+ " \"YON_SIFRE\"   character varying (15) NULL,"
				+ " \"FIRMA_ADI\"   character varying (50) NULL) ";		
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  \"YETKILER\" ( "
				+ " \"YETID\"  SERIAL PRIMARY KEY,"
				+ " \"KULLANICI\"   character varying (25) NULL,"
				+ " \"KARTON\"   character varying (5) NULL,"
				+ " \"TAM_YETKI\"  BOOLEAN  NULL,"
				+ " \"GORUNTU\"  BOOLEAN  NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		// ***************OZEL NO YAZ ************
		sql = "INSERT INTO \"OZEL\" (\"YONETICI\",\"YON_SIFRE\",\"FIRMA_ADI\") VALUES (?,? ,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, user_name);  // 1. Parametre -> user_name
			stmt.setString(2, "12345");    // 2. Parametre -> Sabit değer "12345"
			stmt.setString(3, firmaAdi);   // 3. Parametre -> firmaAdi
			stmt.executeUpdate();
		}
	}
	
	public void createTableKambiyo(Connection connection, String firmaAdi , String user_name) throws SQLException {
		String sql =null;
		sql = "CREATE TABLE \"CEK\"( " + 
				"\"Cek_No\"  character varying(10) PRIMARY KEY ," + 
				"\"Vade\" date,\"Giris_Bordro\"  character varying(10),"+ 
				"\"Cikis_Bordro\"  character varying(10) ," + 
				"\"Giris_Tarihi\" date , " + 
				"\"Cikis_Tarihi\" date , " + 
				"\"Giris_Musteri\" character varying(12)," + 
				"\"Cikis_Musteri\" character varying(12)," + 
				"\"Banka\" character varying(25)," + 
				"\"Sube\" character varying(25)," + 
				"\"Tutar\" float ," + 
				"\"Cins\" character varying(3), " + 
				"\"Durum\" character varying(1), " + 
				"\"T_Tarih\" date , " + 
				"\"Seri_No\" character varying(15)," + 
				"\"Ilk_Borclu\" character varying(30)," + 
				"\"Cek_Hesap_No\" character varying(15)," + 
				"\"Giris_Ozel_Kod\" character varying(15) ," + 
				"\"Cikis_Ozel_Kod\" character varying(15)," + 
				"\"USER\" character varying(15))";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE INDEX  \"IX_CEK\" ON \"CEK\" (\"Cek_No\", \"Vade\" , \"Giris_Bordro\" ,\"Cikis_Bordro\") " ;
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"SENET\" (\"Senet_No\"  character varying(10) PRIMARY KEY, " + 
				"\"Vade\" date,\"Giris_Bordro\"  character varying(10)," + 
				"\"Cikis_Bordro\"  character varying(10) ,"+ 
				"\"Giris_Tarihi\" date , " + 
				"\"Cikis_Tarihi\" date , " + 
				"\"Giris_Musteri\" character varying(12)," + 
				"\"Cikis_Musteri\" character varying(12)," + 
				"\"Tutar\" float ," + 
				"\"Cins\" character varying(3), " + 
				"\"Durum\" character varying(1)," + 
				"\"T_Tarih\" date , " + 
				"\"Ilk_Borclu\" character varying(30)," + 
				"\"Sehir\" character varying(15)," + 
				"\"Giris_Ozel_Kod\" character varying(15) , " + 
				"\"Cikis_Ozel_Kod\" character varying(15)," + 
				"\"USER\" character varying(15))";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"EVRAK\" (\"EVRAK\" character varying (5),\"NO\" integer )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"ACIKLAMA\"(\"ACID\" SERIAL  PRIMARY KEY, " + 
				"\"EVRAK_CINS\" character varying(3) ," + 
				"\"SATIR\" integer ,\"EVRAK_NO\" character varying(10) ," + 
				"\"ACIKLAMA\" character varying(50) ,\"Gir_Cik\" character varying(1))";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  \"OZEL\" ("
				+ " \"OZID\"   SERIAL PRIMARY KEY,"
				+ " \"YONETICI\"   character varying (25) NULL,"
				+ " \"YON_SIFRE\"   character varying (15) NULL,"
				+ " \"FIRMA_ADI\"   character varying (50) NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  \"YETKILER\" ( "
				+ " \"YETID\"  SERIAL PRIMARY KEY,"
				+ " \"KULLANICI\"   character varying (25) NULL,"
				+ " \"KARTON\"   character varying (5) NULL,"
				+ " \"TAM_YETKI\"  BOOLEAN  NULL,"
				+ " \"GORUNTU\"  BOOLEAN  NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		// ***************OZEL NO YAZ *************************
		sql = "INSERT INTO \"OZEL\" (\"YONETICI\",\"YON_SIFRE\",\"FIRMA_ADI\") VALUES (?,? ,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, user_name);  // 1. Parametre -> user_name
			stmt.setString(2, "12345");    // 2. Parametre -> Sabit değer "12345"
			stmt.setString(3, firmaAdi);   // 3. Parametre -> firmaAdi
			stmt.executeUpdate();
		}
		// ***************CEK GIRIS EVRAK NO YAZ **************
		sql = "INSERT INTO  \"EVRAK\"(\"EVRAK\",\"NO\") VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "CEK_G");
			stmt.setInt(2, 0);
			stmt.executeUpdate();
		}
		// ***************CEK CIKIS EVRAK NO YAZ **************
		sql = "INSERT INTO  \"EVRAK\"(\"EVRAK\",\"NO\") VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "CEK_C");
			stmt.setInt(2, 0);
			stmt.executeUpdate();
		}
		// ***************SENET GIRIS EVRAK NO YAZ ************
		sql = "INSERT INTO  \"EVRAK\"(\"EVRAK\",\"NO\") VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "SEN_G");
			stmt.setInt(2, 0);
			stmt.executeUpdate();
		}
		// ***************SENET CIKIS EVRAK NO YAZ ************
		sql = "INSERT INTO  \"EVRAK\"(\"EVRAK\",\"NO\") VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "SEN_C");
			stmt.setInt(2, 0);
			stmt.executeUpdate();
		}
	}
	
	public void sifirdan_LOG(serverBilgiDTO sbilgi) {
		String databaseName = sbilgi.getUser_modul_baslik().toLowerCase() + sbilgi.getUser_prog_kodu() + "_log";
		sbilgi.setUser_prog_kodu(databaseName);
		if (dosyaKontrol(sbilgi)) {
			return;
		}
		String connectionUrl = "jdbc:postgresql://" + sbilgi.getUser_ip() + "/" + sbilgi.getSuperviser() ;
		String createDatabaseSql = "CREATE DATABASE " + databaseName +
				" WITH " +
				" OWNER = postgres " +
				" ENCODING = 'UTF8' " +
				" LC_COLLATE = 'tr_TR.UTF-8' " +
				" LC_CTYPE = 'tr_TR.UTF-8' " +
				" LOCALE_PROVIDER = 'libc' " +
				" TABLESPACE = pg_default " +
				" CONNECTION LIMIT = -1 " +
				" IS_TEMPLATE = False " +
				" TEMPLATE template0 ";
		try (Connection initialConnection = DriverManager.getConnection(connectionUrl, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
				Statement stmt = initialConnection.createStatement()) {
			stmt.executeUpdate(createDatabaseSql);
			String logDatabaseUrl =  "jdbc:postgresql://" + sbilgi.getUser_ip() + "/" +  databaseName;
			try (Connection logConnection = DriverManager.getConnection(logDatabaseUrl, sbilgi.getUser_server(), sbilgi.getUser_pwd_server())) {
				createTableLog(logConnection);
			}
		} catch (Exception e) {
			throw new RuntimeException("LOG veritabanı oluşturulamadı veya tablolar oluşturulamadı.", e);
		}
	}

	private void createTableLog(Connection connection) throws SQLException  {
		String createTableSql = "CREATE TABLE \"LOGLAMA\"("
				+ "	\"TARIH\" TIMESTAMP NOT NULL,"
				+ "	\"MESAJ\" CHARACTER VARYING (100) NOT NULL,"
				+ "	\"EVRAK\" CHARACTER VARYING (15) NOT NULL,"
				+ "	\"USER_NAME\" nchar(15) NULL)";

		String createIndexSql = "CREATE INDEX \"IX_LOGLAMA\" ON \"LOGLAMA\" (\"TARIH\",\"EVRAK\",\"USER_NAME\");";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(createTableSql);
			stmt.executeUpdate(createIndexSql);
		} catch (SQLException e) {
			throw new SQLException("Log tablosu veya index oluşturulurken hata oluştu.", e);
		}
	}
}