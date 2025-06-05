package com.hamit.obs.createnewDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.hamit.obs.custom.enums.modulTipi;
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
		String connectionString =  "jdbc:postgresql://" + sbilgi.getUser_ip() + "/" + sbilgi.getSuperviser() ;
		String query = "SELECT datname FROM pg_database WHERE datname = ?";  
		try (Connection conn = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
				PreparedStatement stmt = conn.prepareStatement(query)) {
			DriverManager.setLoginTimeout(5);
			stmt.setString(1, sbilgi.getUser_modul_baslik().toLowerCase() + sbilgi.getUser_prog_kodu());
			try (ResultSet rs = stmt.executeQuery()) {
				rs.next();
				result = rs.getRow() > 0;
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
				modulTipi modultip = modulTipi.fromDbValue(sbilgi.getUser_modul());
				switch (modultip) {
				case CARI_HESAP -> createTableCari(databaseConnection, sbilgi.getFirma_adi(), sbilgi.getUser_name());
				case KUR        -> createTableKur(databaseConnection);
				case ADRES      -> createTableAdres(databaseConnection, sbilgi.getFirma_adi(), sbilgi.getUser_name());
				case KAMBIYO    -> createTableKambiyo(databaseConnection, sbilgi.getFirma_adi(), sbilgi.getUser_name());
				case FATURA     -> createTableFatura(databaseConnection, sbilgi.getFirma_adi(), sbilgi.getUser_name());
				case KERESTE    -> createTableKereste(databaseConnection, sbilgi.getFirma_adi(), sbilgi.getUser_name());
				default -> throw new IllegalArgumentException("Unexpected value: " + modultip);
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
	public void createTableFatura(Connection connection, String firmaAdi , String user_name) throws SQLException {
		String sql = null;
		sql = "CREATE TABLE  \"DPN\" ( "
				+ "  \"DID\"  SERIAL  PRIMARY KEY ,"
				+ "  \"Evrak_No\"   character varying (10) NOT NULL,"
				+ "  \"Tip\"   character varying (1) NULL,"
				+ "  \"Bir\"   character varying (40) NULL,"
				+ "  \"Iki\"   character varying (40) NULL,"
				+ "  \"Uc\"   character varying (40) NULL,"
				+ "  \"Gir_Cik\"   character varying (1) NULL,"
				+ "  \"USER\"   character varying (15) NOT NULL )  ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE INDEX  \"IX_DPN\"  ON  \"DPN\" ( "
				+ " \"Evrak_No\" , "
				+ " \"Gir_Cik\" )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"GDY\"( "
				+ "  \"GID\" SERIAL  PRIMARY KEY, "
				+ "  \"Isim\" character varying(50) NULL, "
				+ "  \"Adres\" character varying(50) NULL, "
				+ "  \"Semt\" character varying(50) NULL, "
				+ "  \"Sehir\" character varying(50) NULL, "
				+ "  \"USER\" character varying(15) NOT NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql= "CREATE TABLE \"FATURA\"( "
				+ "\"Fatura_No\" character varying(10) NOT NULL,"
				+ " \"Kodu\" character varying(12) NULL,"
				+ " \"Tarih\" timestamp NULL,"
				+ " \"Kdv\" DOUBLE PRECISION NULL,"
				+ " \"Doviz\" character varying(3) NULL,"
				+ " \"Miktar\" DOUBLE PRECISION NULL,"
				+ " \"Fiat\" DOUBLE PRECISION NULL,"
				+ " \"Tutar\" DOUBLE PRECISION NULL,"
				+ " \"Kur\" DOUBLE PRECISION NULL,"
				+ " \"Cari_Firma\" character varying(12) NULL,"
				+ " \"Iskonto\" DOUBLE PRECISION NULL,"
				+ " \"Tevkifat\" DOUBLE PRECISION NULL,"
				+ " \"Ana_Grup\" int NULL,"
				+ " \"Alt_Grup\" int NULL,"
				+ " \"Depo\" int NULL,"
				+ " \"Adres_Firma\" character varying(12) NULL,"
				+ " \"Ozel_Kod\" character varying(10) NULL,"
				+ " \"Gir_Cik\" character varying(1) NULL,"
				+ " \"Izahat\" character varying(40) NULL,"
				+ " \"Cins\" character varying(1) NULL,"
				+ " \"USER\" character varying(15) NOT NULL);"
				+ " CREATE INDEX \"IX_FATURA\" ON \"FATURA\" (\"Fatura_No\",\"Kodu\",\"Tarih\",\"Cari_Firma\",\"Gir_Cik\"); ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"IRSALIYE\"( "
				+ " \"Irsaliye_No\" character varying(10) NOT NULL,"
				+ " \"Kodu\" character varying(12) NULL,"
				+ " \"Tarih\" timestamp NULL,"
				+ " \"Kdv\" DOUBLE PRECISION NULL,"
				+ " \"Doviz\" character varying(3) NULL,"
				+ " \"Kur\" DOUBLE PRECISION NULL,"
				+ " \"Miktar\" DOUBLE PRECISION NULL,"
				+ " \"Fiat\" DOUBLE PRECISION NULL,"
				+ " \"Tutar\" DOUBLE PRECISION NULL,"
				+ " \"Firma\" character varying(12) NULL,"
				+ " \"Iskonto\" DOUBLE PRECISION NULL,"
				+ " \"Fatura_No\" character varying(10) NULL,"
				+ " \"Sevk_Tarihi\" \"date\" NULL,"
				+ " \"Ana_Grup\" int NULL,"
				+ " \"Alt_Grup\" int NULL,"
				+ " \"Depo\" int NULL,"
				+ " \"Cari_Hesap_Kodu\" character varying(12) NULL,"
				+ " \"Ozel_Kod\" character varying(10) NULL,"
				+ " \"Hareket\" character varying(1) NULL,"
				+ " \"Izahat\" character varying(40) NULL,"
				+ " \"Cins\" character varying(1) NULL,"
				+ " \"USER\" character varying(15) NOT NULL);"
				+ " CREATE INDEX \"IX_IRSALIYE\" ON \"IRSALIYE\" (\"Irsaliye_No\",\"Kodu\",\"Tarih\",\"Firma\",\"Hareket\")";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"MAL\"( "
				+ " \"Kodu\" character varying(12) NOT NULL PRIMARY KEY,"
				+ " \"Adi\" character varying(40) NULL,"
				+ " \"Birim\" character varying(5) NULL,"
				+ " \"Kusurat\" int NULL,"
				+ " \"Resim\" BYTEA NULL,"
				+ " \"Sinif\" character varying(5) NULL,"
				+ " \"Ana_Grup\" int NULL,"
				+ " \"Alt_Grup\" int NULL,"
				+ " \"Aciklama_1\" character varying(25) NULL,"
				+ " \"Aciklama_2\" character varying(25) NULL,"
				+ " \"Ozel_Kod_1\" int NULL,"
				+ " \"Ozel_Kod_2\" int NULL,"
				+ " \"Ozel_Kod_3\" int NULL,"
				+ " \"Kdv\" DOUBLE PRECISION NULL,"
				+ " \"Barkod\" character varying(20) NULL,"
				+ " \"Mensei\" int NULL,"
				+ " \"Agirlik\" DOUBLE PRECISION NULL,"
				+ " \"Depo\" int NULL,"
				+ " \"Fiat\" DOUBLE PRECISION NULL,"
				+ " \"Fiat_2\" DOUBLE PRECISION NULL,"
				+ " \"Fiat_3\" DOUBLE PRECISION NULL,"
				+ " \"Recete\" character varying(10) NULL,"
				+ " \"USER\" character varying(15) NOT NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE  INDEX \"IX_MAL\" ON \"MAL\"( "
				+ " \"Adi\" ASC,"
				+ " \"Ana_Grup\" ,"
				+ " \"Alt_Grup\")";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"RECETE\"( "
				+ " \"Recete_No\" character varying(10) NOT NULL,"
				+ " \"Ana_Grup\" int NULL,"
				+ " \"Alt_Grup\" int NULL,"
				+ " \"Durum\" BOOLEAN NULL,"
				+ " \"Tur\" character varying(7) NULL,"
				+ " \"Kodu\" character varying(10) NULL,"
				+ " \"Miktar\" DOUBLE PRECISION NULL,"
				+ " \"USER\" character varying(15) NOT NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE INDEX \"IX_RECETE\" ON \"RECETE\"( "
				+ " \"Recete_No\" ,"
				+ " \"Kodu\" )";
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
		sql = "CREATE TABLE \"STOK\"("
				+ " \"Evrak_No\" character varying(10) NOT NULL,"
				+ " \"Evrak_Cins\" character varying(3) NULL,"
				+ " \"Tarih\" timestamp NULL,"
				+ " \"Depo\" int NULL,"
				+ " \"Urun_Kodu\" character varying(12) NULL,"
				+ " \"Miktar\" DOUBLE PRECISION NULL,"
				+ " \"Fiat\" DOUBLE PRECISION NULL,"
				+ " \"Tutar\" DOUBLE PRECISION NULL,"
				+ " \"Ana_Grup\" int NULL,"
				+ " \"Alt_Grup\" int NULL,"
				+ " \"Hareket\" character varying(1) NULL,"
				+ " \"Izahat\" character varying(40) NULL,"
				+ " \"Hesap_Kodu\" character varying(12) NULL,"
				+ " \"Kur\" DOUBLE PRECISION NULL,"
				+ " \"Doviz\" character varying(3) NULL,"
				+ " \"Kdvli_Tutar\" DOUBLE PRECISION NULL,"
				+ " \"B1\" character varying(15) NULL,"
				+ " \"USER\" character varying(40) NOT NULL);"
				+ " CREATE INDEX \"IX_STOK\" ON \"STOK\" (\"Urun_Kodu\",\"Tarih\",\"Hareket\");";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"MENSEI_DEGISKEN\" ("
				+ " \"MEID\" SERIAL PRIMARY KEY ,"
				+ " \"MEID_Y\"   int   NOT NULL,"   
				+ " \"MENSEI\"   character varying (25) NOT NULL,"
				+ " \"USER\"   character varying (15) NOT NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  \"ANA_GRUP_DEGISKEN\" ("
				+ " \"AGID\" SERIAL PRIMARY KEY ,"
				+ " \"AGID_Y\"   int  NOT NULL,"  
				+ " \"ANA_GRUP\"   character varying (25) NOT NULL,"
				+ " \"USER\"   character varying (15) NOT NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"ALT_GRUP_DEGISKEN\" ("
				+ "  \"ALID\"   SERIAL PRIMARY KEY ,"
				+ "  \"ALID_Y\"   int  NOT NULL,"  
				+ "  \"ANA_GRUP\"   int  NOT NULL,"
				+ "  \"ALT_GRUP\"   character varying (25) NOT NULL,"
				+ "  \"USER\"   character varying (15) NOT NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"ACIKLAMA\" ("
				+ "  \"ACID\"    SERIAL PRIMARY KEY ,"
				+ "  \"EVRAK_CINS\"   character varying (3) NULL,"
				+ "  \"SATIR\"   int  NULL,"
				+ "  \"EVRAK_NO\"   character varying (10) NULL,"
				+ "  \"ACIKLAMA\"   character varying (50) NULL,"
				+ "  \"Gir_Cik\"   character varying (1) NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE INDEX \"IX_ACIKLAMA\" ON \"ACIKLAMA\"( "
				+ " \"EVRAK_CINS\" ,"
				+ " \"EVRAK_NO\" ,"
				+ " \"Gir_Cik\" )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"DEPO_DEGISKEN\"("
				+ " \"DPID\" SERIAL PRIMARY KEY ,"
				+ " \"DPID_Y\" int  NOT NULL,"   
				+ " \"DEPO\" character varying(25) NOT NULL,"
				+ " \"USER\" character varying(15) NOT NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"OZ_KOD_1_DEGISKEN\"("
				+ " \"OZ1ID\" SERIAL PRIMARY KEY,"
				+ " \"OZ1ID_Y\" int  NOT NULL,"  
				+ " \"OZEL_KOD_1\" character varying(25) NOT NULL,"
				+ " \"USER\" character varying(15) NOT NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"OZ_KOD_2_DEGISKEN\"("
				+ " \"OZ2ID\" SERIAL PRIMARY KEY,"
				+ " \"OZ2ID_Y\" int NOT NULL,"   
				+ " \"OZEL_KOD_2\" character varying(25) NOT NULL,"
				+ " \"USER\" character varying(15) NOT NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"DEPOEVRAK\"("
				+ " \"E_No\" int NOT NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"URET_EVRAK\"("
				+ " \"E_No\" int NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"ZAYI_EVRAK\"("
				+ " \"E_No\" int NULL)";
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
		sql = "CREATE TABLE \"IRS_EVRAK_FORMAT\" ("
				+ "  \"SAT_SUT\"   character varying (5) NULL,"
				+ "  \"TARIH\"   DOUBLE PRECISION  NULL,"
				+ "  \"SEVK_TARIH\"   DOUBLE PRECISION  NULL,"
				+ "  \"FIRMA_KODU\"   DOUBLE PRECISION  NULL,"
				+ "  \"FIRMA_UNVANI\"   DOUBLE PRECISION  NULL,"
				+ "  \"VERGI_DAIRESI\"   DOUBLE PRECISION  NULL,"
				+ "  \"VERGI_NO\"   DOUBLE PRECISION  NULL,"
				+ "  \"GIDECEGI_YER\"   DOUBLE PRECISION  NULL,"
				+ "  \"NOT_1\"   DOUBLE PRECISION  NULL,"
				+ "  \"NOT_2\"   DOUBLE PRECISION  NULL,"
				+ "  \"NOT_3\"   DOUBLE PRECISION  NULL,"
				+ "  \"BASLIK_BOLUM\"   DOUBLE PRECISION  NULL,"
				+ "  \"BARKOD\"   DOUBLE PRECISION  NULL,"
				+ "  \"URUN_KODU\"   DOUBLE PRECISION  NULL,"
				+ "  \"URUN_ADI\"   DOUBLE PRECISION  NULL,"
				+ "  \"DEPO\"   DOUBLE PRECISION  NULL,"
				+ "  \"SIMGE\"   DOUBLE PRECISION  NULL,"
				+ "  \"BIRIM_FIAT\"   DOUBLE PRECISION  NULL,"
				+ "  \"ISKONTO\"   DOUBLE PRECISION  NULL,"
				+ "  \"MIKTAR\"   DOUBLE PRECISION  NULL,"
				+ "  \"K_D_V\"   DOUBLE PRECISION  NULL,"
				+ "  \"TUTAR\"   DOUBLE PRECISION  NULL,"
				+ "  \"TUTAR_TOPLAM\"   DOUBLE PRECISION  NULL,"
				+ "  \"ISKONTO_TOPLAMI\"   DOUBLE PRECISION  NULL,"
				+ "  \"BAKIYE\"   DOUBLE PRECISION  NULL,"
				+ "  \"K_D_V_TOPLAMI\"   DOUBLE PRECISION  NULL,"
				+ "  \"BELGE_TOPLAMI\"   DOUBLE PRECISION  NULL,"
				+ "  \"YAZI_ILE\"   DOUBLE PRECISION  NULL,"
				+ "  \"ALT_BOLUM\"   DOUBLE PRECISION  NULL,"
				+ "  \"N1\"   DOUBLE PRECISION  NULL,"
				+ "  \"N2\"   DOUBLE PRECISION  NULL,"
				+ "  \"N3\"   DOUBLE PRECISION  NULL,"
				+ "  \"N4\"   DOUBLE PRECISION  NULL,"
				+ "  \"N5\"   DOUBLE PRECISION  NULL,"
				+ "  \"N6\"   DOUBLE PRECISION  NULL,"
				+ "  \"N7\"   DOUBLE PRECISION  NULL,"
				+ "  \"N8\"   DOUBLE PRECISION  NULL,"
				+ "  \"N9\"   DOUBLE PRECISION  NULL,"
				+ "  \"N10\"   DOUBLE PRECISION  NULL,"
				+ "  \"USER\"   character varying (15) NOT NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  \"IRS_EVRAK_FORMAT\"(\"SAT_SUT\" ,\"TARIH\",\"SEVK_TARIH\",\"FIRMA_KODU\",\"FIRMA_UNVANI\",\"VERGI_DAIRESI\",\"VERGI_NO\",\"GIDECEGI_YER\",\"NOT_1\",\"NOT_2\",\"NOT_3\",\"BASLIK_BOLUM\",\"BARKOD\",\"URUN_KODU\",\"URUN_ADI\",\"DEPO\",\"SIMGE\",\"BIRIM_FIAT\",\"ISKONTO\",\"MIKTAR\",\"K_D_V\",\"TUTAR\",\"TUTAR_TOPLAM\",\"ISKONTO_TOPLAMI\",\"BAKIYE\",\"K_D_V_TOPLAMI\",\"BELGE_TOPLAMI\",\"YAZI_ILE\",\"ALT_BOLUM\",\"N1\",\"N2\",\"N3\",\"N4\",\"N5\",\"N6\",\"N7\",\"N8\",\"N9\",\"N10\",\"USER\") VALUES ('SATIR','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  \"IRS_EVRAK_FORMAT\"(\"SAT_SUT\" ,\"TARIH\",\"SEVK_TARIH\",\"FIRMA_KODU\",\"FIRMA_UNVANI\",\"VERGI_DAIRESI\",\"VERGI_NO\",\"GIDECEGI_YER\",\"NOT_1\",\"NOT_2\",\"NOT_3\",\"BASLIK_BOLUM\",\"BARKOD\",\"URUN_KODU\",\"URUN_ADI\",\"DEPO\",\"SIMGE\",\"BIRIM_FIAT\",\"ISKONTO\",\"MIKTAR\",\"K_D_V\",\"TUTAR\",\"TUTAR_TOPLAM\",\"ISKONTO_TOPLAMI\",\"BAKIYE\",\"K_D_V_TOPLAMI\",\"BELGE_TOPLAMI\",\"YAZI_ILE\",\"ALT_BOLUM\",\"N1\",\"N2\",\"N3\",\"N4\",\"N5\",\"N6\",\"N7\",\"N8\",\"N9\",\"N10\",\"USER\") VALUES ('SUTUN','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"FAT_EVRAK_FORMAT\" ( "
				+ "  \"SAT_SUT\"   character varying (5) NULL,"
				+ "  \"TARIH\"   DOUBLE PRECISION  NULL,"
				+ "  \"FIRMA_KODU\"   DOUBLE PRECISION  NULL,"
				+ "  \"FIRMA_UNVANI\"   DOUBLE PRECISION  NULL,"
				+ "  \"VERGI_DAIRESI\"   DOUBLE PRECISION  NULL,"
				+ "  \"VERGI_NO\"   DOUBLE PRECISION  NULL,"
				+ "  \"GIDECEGI_YER\"   DOUBLE PRECISION  NULL,"
				+ "  \"NOT_1\"   DOUBLE PRECISION  NULL,"
				+ "  \"NOT_2\"   DOUBLE PRECISION  NULL,"
				+ "  \"NOT_3\"   DOUBLE PRECISION  NULL,"
				+ "  \"BASLIK_BOLUM\"   DOUBLE PRECISION  NULL,"
				+ "  \"BARKOD\"   DOUBLE PRECISION  NULL,"
				+ "  \"URUN_KODU\"   DOUBLE PRECISION  NULL,"
				+ "  \"URUN_ADI\"   DOUBLE PRECISION  NULL,"
				+ "  \"DEPO\"   DOUBLE PRECISION  NULL,"
				+ "  \"IZAHAT\"   DOUBLE PRECISION  NULL,"
				+ "  \"SIMGE\"   DOUBLE PRECISION  NULL,"
				+ "  \"BIRIM_FIAT\"   DOUBLE PRECISION  NULL,"
				+ "  \"ISKONTO\"   DOUBLE PRECISION  NULL,"
				+ "  \"MIKTAR\"   DOUBLE PRECISION  NULL,"
				+ "  \"K_D_V\"   DOUBLE PRECISION  NULL,"
				+ "  \"TUTAR\"   DOUBLE PRECISION  NULL,"
				+ "  \"TUTAR_TOPLAM\"   DOUBLE PRECISION  NULL,"
				+ "  \"ISKONTO_TOPLAMI\"   DOUBLE PRECISION  NULL,"
				+ "  \"BAKIYE\"   DOUBLE PRECISION  NULL,"
				+ "  \"K_D_V_TOPLAMI\"   DOUBLE PRECISION  NULL,"
				+ "  \"BELGE_TOPLAMI\"   DOUBLE PRECISION  NULL,"
				+ "  \"TEVKIFAT_ORANI\"   DOUBLE PRECISION  NULL,"
				+ "  \"AL_TAR_TEV_ED_KDV\"   DOUBLE PRECISION  NULL,"
				+ "  \"TEV_DAH_TOP_TUTAR\"   DOUBLE PRECISION  NULL,"
				+ "  \"BEYAN_ED_KDV\"   DOUBLE PRECISION  NULL,"
				+ "  \"TEV_HAR_TOP_TUT\"   DOUBLE PRECISION  NULL,"
				+ "  \"YAZI_ILE\"   DOUBLE PRECISION  NULL,"
				+ "  \"TEV_KASESI\"   DOUBLE PRECISION  NULL,"
				+ "  \"ALT_BOLUM\"   DOUBLE PRECISION  NULL,"
				+ "  \"N1\"   DOUBLE PRECISION  NULL,"
				+ "  \"N2\"   DOUBLE PRECISION  NULL,"
				+ "  \"N3\"   DOUBLE PRECISION  NULL,"
				+ "  \"N4\"   DOUBLE PRECISION  NULL,"
				+ "  \"N5\"   DOUBLE PRECISION  NULL,"
				+ "  \"N6\"   DOUBLE PRECISION  NULL,"
				+ "  \"N7\"   DOUBLE PRECISION  NULL,"
				+ "  \"N8\"   DOUBLE PRECISION  NULL,"
				+ "  \"N9\"   DOUBLE PRECISION  NULL,"
				+ "  \"N10\"   DOUBLE PRECISION  NULL,"
				+ "  \"USER\"   character varying (15) NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  \"FAT_EVRAK_FORMAT\"(\"SAT_SUT\",\"TARIH\",\"FIRMA_KODU\",\"FIRMA_UNVANI\",\"VERGI_DAIRESI\" ,\"VERGI_NO\" ,\"GIDECEGI_YER\" ,\"NOT_1\" ,\"NOT_2\" ,\"NOT_3\",\"BASLIK_BOLUM\",\"BARKOD\",\"URUN_KODU\" ,\"URUN_ADI\" , \"DEPO\" ,\"IZAHAT\",\"SIMGE\" ,\"BIRIM_FIAT\" ,\"ISKONTO\" ,\"MIKTAR\",\"K_D_V\" ,\"TUTAR\" ,\"TUTAR_TOPLAM\" ,\"ISKONTO_TOPLAMI\"  ,\"BAKIYE\" ,\"K_D_V_TOPLAMI\" ,\"BELGE_TOPLAMI\" , \"YAZI_ILE\",\"TEVKIFAT_ORANI\" ,\"AL_TAR_TEV_ED_KDV\" ,\"TEV_DAH_TOP_TUTAR\" , \"BEYAN_ED_KDV\" ,\"TEV_HAR_TOP_TUT\",\"TEV_KASESI\",\"ALT_BOLUM\",\"N1\",\"N2\",\"N3\",\"N4\",\"N5\",\"N6\",\"N7\",\"N8\",\"N9\",\"N10\",\"USER\") VALUES " + " ('SATIR','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  \"FAT_EVRAK_FORMAT\"(\"SAT_SUT\",\"TARIH\",\"FIRMA_KODU\",\"FIRMA_UNVANI\",\"VERGI_DAIRESI\" ,\"VERGI_NO\" ,\"GIDECEGI_YER\" ,\"NOT_1\" ,\"NOT_2\" ,\"NOT_3\",\"BASLIK_BOLUM\",\"BARKOD\",\"URUN_KODU\" ,\"URUN_ADI\" , \"DEPO\" ,\"IZAHAT\",\"SIMGE\" ,\"BIRIM_FIAT\" ,\"ISKONTO\" ,\"MIKTAR\",\"K_D_V\" ,\"TUTAR\" ,\"TUTAR_TOPLAM\" ,\"ISKONTO_TOPLAMI\"  ,\"BAKIYE\" ,\"K_D_V_TOPLAMI\" ,\"BELGE_TOPLAMI\" , \"YAZI_ILE\",\"TEVKIFAT_ORANI\" ,\"AL_TAR_TEV_ED_KDV\" ,\"TEV_DAH_TOP_TUTAR\" , \"BEYAN_ED_KDV\" ,\"TEV_HAR_TOP_TUT\",\"TEV_KASESI\",\"ALT_BOLUM\",\"N1\",\"N2\",\"N3\",\"N4\",\"N5\",\"N6\",\"N7\",\"N8\",\"N9\",\"N10\",\"USER\") VALUES " + " ('SUTUN','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		// ***************EVRAK NO YAZ ************
		sql = "INSERT INTO  \"DEPOEVRAK\"(\"E_No\") VALUES ('0')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  \"URET_EVRAK\"(\"E_No\") VALUES ('0')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  \"ZAYI_EVRAK\"(\"E_No\") VALUES ('0')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		// ***************SENET CIKIS EVRAK NO YAZ ************
		sql = "INSERT INTO  \"EVRAK\"(\"EVRAK\",\"NO\") VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "SEN_C");
			stmt.setInt(2, 0);
			stmt.executeUpdate();
		}
	}
	public void createTableKereste(Connection connection, String firmaAdi , String user_name) throws SQLException {
		String sql = null;
		sql = "CREATE TABLE \"PAKET_NO\"( "
				+ "  \"Pak_No\"   int  NOT NULL,"
				+ "  \"Konsimento\"   character varying (15) NOT NULL PRIMARY KEY) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  \"DPN\" ( "
				+ "  \"DID\"  SERIAL NOT NULL PRIMARY KEY ,"
				+ "  \"Evrak_No\"   character varying (10) NOT NULL,"
				+ "  \"Tip\"   character varying (1) NULL,"
				+ "  \"Bir\"   character varying (40) NULL,"
				+ "  \"Iki\"   character varying (40) NULL,"
				+ "  \"Uc\"   character varying (40) NULL,"
				+ "  \"Gir_Cik\"   character varying (1) NULL,"
				+ "  \"USER\"   character varying (15) NOT NULL )  ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE INDEX  \"IX_DPN\"  ON  \"DPN\" ( "
				+ " \"Evrak_No\" , "
				+ " \"Gir_Cik\" )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  \"NAKLIYECI\" ("
				+ "  \"NAKID\" SERIAL NOT NULL PRIMARY KEY,"
				+ "  \"NAKID_Y\" int   NOT NULL,"  
				+ "  \"UNVAN\" character varying (50) NOT NULL,"
				+ "  \"USER\" character varying (15) NOT NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql= "CREATE TABLE \"KERESTE\"( "
				+ " \"ID\" SERIAL PRIMARY KEY,"
				+ " \"Evrak_No\" character varying(10) NOT NULL,"
				+ " \"Barkod\" character varying(20) NULL,"
				+ " \"Kodu\" character varying(16) NOT NULL,"
				+ " \"Paket_No\" character varying (10) NULL,"
				+ " \"Konsimento\" character varying(15) NULL,"
				+ " \"Miktar\" DOUBLE PRECISION NULL,"
				+ " \"Tarih\" TIMESTAMP NULL,"
				+ " \"Kdv\" DOUBLE PRECISION NULL,"
				+ " \"Doviz\" character varying(3) NULL,"
				+ " \"Fiat\" DOUBLE PRECISION NULL,"
				+ " \"Tutar\" DOUBLE PRECISION NULL,"
				+ " \"Kur\" DOUBLE PRECISION NULL,"
				+ " \"Cari_Firma\" character varying(12) NULL,"
				+ " \"Adres_Firma\" character varying(12) NULL,"
				+ " \"Iskonto\" DOUBLE PRECISION NULL,"
				+ " \"Tevkifat\" DOUBLE PRECISION NULL,"
				+ " \"Ana_Grup\" int NULL,"
				+ " \"Alt_Grup\" int NULL,"
				+ " \"Mensei\" int NULL,"
				+ " \"Depo\" int NULL,"
				+ " \"Ozel_Kod\" int NULL,"
				+ " \"Izahat\" character varying(40) NULL,"
				+ " \"Nakliyeci\" int NULL,"
				+ " \"USER\" character varying(15) NOT NULL,"
				+ " \"Cikis_Evrak\" character varying(10) NULL,"
				+ " \"CTarih\" TIMESTAMP NULL,"
				+ " \"CKdv\" DOUBLE PRECISION NULL,"
				+ " \"CDoviz\" character varying(3) NULL,"
				+ " \"CFiat\" DOUBLE PRECISION NULL,"
				+ " \"CTutar\" DOUBLE PRECISION NULL,"
				+ " \"CKur\" DOUBLE PRECISION NULL,"
				+ " \"CCari_Firma\" character varying(12) NULL,"
				+ " \"CAdres_Firma\" character varying(12) NULL,"
				+ " \"CIskonto\" DOUBLE PRECISION NULL,"
				+ " \"CTevkifat\" DOUBLE PRECISION NULL,"
				+ " \"CAna_Grup\" int NULL,"
				+ " \"CAlt_Grup\" int NULL,"
				+ " \"CDepo\" int NULL,"
				+ " \"COzel_Kod\" int NULL,"
				+ " \"CIzahat\" character varying(40) NULL,"
				+ " \"CNakliyeci\" int NULL,"
				+ " \"CUSER\" character varying(15)  NULL,"
				+ " \"Satir\" int NOT NULL,"
				+ " \"CSatir\" int NOT NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE INDEX \"IX_KERESTE\" ON \"KERESTE\" (\"Kodu\",\"Paket_No\",\"Evrak_No\",\"Konsimento\",\"Cari_Firma\",\"Cikis_Evrak\" )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE INDEX \"IX_GRP_II\" ON \"KERESTE\" (\"Konsimento\",\"Tarih\",\"Cari_Firma\",\"Kodu\",\"Paket_No\",\"Miktar\",\"Ana_Grup\",\"Alt_Grup\",\"Depo\",\"Ozel_Kod\",\"Cikis_Evrak\")";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE INDEX \"IX_GRP_I\" ON \"KERESTE\" (\"Konsimento\",\"Tarih\",\"Cari_Firma\",\"Kodu\",\"Miktar\",\"Ana_Grup\",\"Alt_Grup\",\"Depo\",\"Ozel_Kod\",\"Cikis_Evrak\")";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  \"OZEL\" ("
				+ " \"OZID\" SERIAL PRIMARY KEY,"
				+ " \"YONETICI\" character varying (25) NULL,"
				+ " \"YON_SIFRE\" character varying (15) NULL,"
				+ " \"FIRMA_ADI\" character varying (50) NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"MENSEI_DEGISKEN\" ("
				+ " \"MEID\" SERIAL PRIMARY KEY ,"
				+ " \"MEID_Y\" int   NOT NULL,"   
				+ " \"MENSEI\" character varying (25) NOT NULL,"
				+ " \"USER\" character varying (15) NOT NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  \"ANA_GRUP_DEGISKEN\" ("
				+ " \"AGID\" SERIAL PRIMARY KEY ,"
				+ " \"AGID_Y\"   int  NOT NULL,"  
				+ " \"ANA_GRUP\"   character varying (25) NOT NULL,"
				+ " \"USER\"   character varying (15) NOT NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"ALT_GRUP_DEGISKEN\" ("
				+ "  \"ALID\"   SERIAL PRIMARY KEY ,"
				+ "  \"ALID_Y\"   int  NOT NULL,"  
				+ "  \"ANA_GRUP\"   int  NOT NULL,"
				+ "  \"ALT_GRUP\"   character varying (25) NOT NULL,"
				+ "  \"USER\"   character varying (15) NOT NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"ACIKLAMA\" ("
				+ "  \"ACID\"    SERIAL PRIMARY KEY ,"
				+ "  \"EVRAK_CINS\"   character varying (3) NULL,"
				+ "  \"SATIR\"   int  NULL,"
				+ "  \"EVRAK_NO\"   character varying (10) NULL,"
				+ "  \"ACIKLAMA\"   character varying (50) NULL,"
				+ "  \"Gir_Cik\"   character varying (1) NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"KOD_ACIKLAMA\" ("
				+ "  \"KOD\"   character varying (2) NOT NULL PRIMARY KEY,"
				+ "  \"ACIKLAMA\"   character varying (50) NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"KONS_ACIKLAMA\" ("
				+ "  \"KONS\"   character varying (15) NOT NULL PRIMARY KEY,"
				+ "  \"ACIKLAMA\"   character varying (50) NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE INDEX  \"IX_ACIKLAMA\" ON \"ACIKLAMA\" (\"EVRAK_CINS\" ,\"EVRAK_NO\",\"Gir_Cik\")";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"DEPO_DEGISKEN\" ("
				+ "  \"DPID\"  SERIAL PRIMARY KEY,"
				+ "  \"DPID_Y\"   int   NOT NULL,"   
				+ "  \"DEPO\"   character varying (25) NOT NULL,"
				+ "  \"USER\"   character varying (15) NOT NULL)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"OZ_KOD_1_DEGISKEN\" ("
				+ "  \"OZ1ID\"  SERIAL PRIMARY KEY,"
				+ "  \"OZ1ID_Y\"   int   NOT NULL,"  
				+ "  \"OZEL_KOD_1\"   character varying (25) NOT NULL,"
				+ "  \"USER\"   character varying (15) NOT NULL)";
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
		sql = "CREATE TABLE \"IRS_EVRAK_FORMAT\" ("
				+ "  \"SAT_SUT\"   character varying (5) NULL,"
				+ "  \"TARIH\"   DOUBLE PRECISION  NULL,"
				+ "  \"SEVK_TARIH\"   DOUBLE PRECISION  NULL,"
				+ "  \"FIRMA_KODU\"   DOUBLE PRECISION  NULL,"
				+ "  \"FIRMA_UNVANI\"   DOUBLE PRECISION  NULL,"
				+ "  \"VERGI_DAIRESI\"   DOUBLE PRECISION  NULL,"
				+ "  \"VERGI_NO\"   DOUBLE PRECISION  NULL,"
				+ "  \"GIDECEGI_YER\"   DOUBLE PRECISION  NULL,"
				+ "  \"NOT_1\"   DOUBLE PRECISION  NULL,"
				+ "  \"NOT_2\"   DOUBLE PRECISION  NULL,"
				+ "  \"NOT_3\"   DOUBLE PRECISION  NULL,"
				+ "  \"BASLIK_BOLUM\"   DOUBLE PRECISION  NULL,"
				+ "  \"BARKOD\"   DOUBLE PRECISION  NULL,"
				+ "  \"URUN_KODU\"   DOUBLE PRECISION  NULL,"
				+ "  \"URUN_ADI\"   DOUBLE PRECISION  NULL,"
				+ "  \"DEPO\"   DOUBLE PRECISION  NULL,"
				+ "  \"SIMGE\"   DOUBLE PRECISION  NULL,"
				+ "  \"BIRIM_FIAT\"   DOUBLE PRECISION  NULL,"
				+ "  \"ISKONTO\"   DOUBLE PRECISION  NULL,"
				+ "  \"MIKTAR\"   DOUBLE PRECISION  NULL,"
				+ "  \"K_D_V\"   DOUBLE PRECISION  NULL,"
				+ "  \"TUTAR\"   DOUBLE PRECISION  NULL,"
				+ "  \"TUTAR_TOPLAM\"   DOUBLE PRECISION  NULL,"
				+ "  \"ISKONTO_TOPLAMI\"   DOUBLE PRECISION  NULL,"
				+ "  \"BAKIYE\"   DOUBLE PRECISION  NULL,"
				+ "  \"K_D_V_TOPLAMI\"   DOUBLE PRECISION  NULL,"
				+ "  \"BELGE_TOPLAMI\"   DOUBLE PRECISION  NULL,"
				+ "  \"YAZI_ILE\"   DOUBLE PRECISION  NULL,"
				+ "  \"ALT_BOLUM\"   DOUBLE PRECISION  NULL,"
				+ "  \"N1\"   DOUBLE PRECISION  NULL,"
				+ "  \"N2\"   DOUBLE PRECISION  NULL,"
				+ "  \"N3\"   DOUBLE PRECISION  NULL,"
				+ "  \"N4\"   DOUBLE PRECISION  NULL,"
				+ "  \"N5\"   DOUBLE PRECISION  NULL,"
				+ "  \"N6\"   DOUBLE PRECISION  NULL,"
				+ "  \"N7\"   DOUBLE PRECISION  NULL,"
				+ "  \"N8\"   DOUBLE PRECISION  NULL,"
				+ "  \"N9\"   DOUBLE PRECISION  NULL,"
				+ "  \"N10\"   DOUBLE PRECISION  NULL,"
				+ "  \"USER\"   character varying (15) NOT NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  \"IRS_EVRAK_FORMAT\"(\"SAT_SUT\" ,\"TARIH\",\"SEVK_TARIH\",\"FIRMA_KODU\",\"FIRMA_UNVANI\",\"VERGI_DAIRESI\",\"VERGI_NO\",\"GIDECEGI_YER\",\"NOT_1\",\"NOT_2\",\"NOT_3\",\"BASLIK_BOLUM\",\"BARKOD\",\"URUN_KODU\",\"URUN_ADI\",\"DEPO\",\"SIMGE\",\"BIRIM_FIAT\",\"ISKONTO\",\"MIKTAR\",\"K_D_V\",\"TUTAR\",\"TUTAR_TOPLAM\",\"ISKONTO_TOPLAMI\",\"BAKIYE\",\"K_D_V_TOPLAMI\",\"BELGE_TOPLAMI\",\"YAZI_ILE\",\"ALT_BOLUM\",\"N1\",\"N2\",\"N3\",\"N4\",\"N5\",\"N6\",\"N7\",\"N8\",\"N9\",\"N10\",\"USER\") VALUES ('SATIR','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  \"IRS_EVRAK_FORMAT\"(\"SAT_SUT\" ,\"TARIH\",\"SEVK_TARIH\",\"FIRMA_KODU\",\"FIRMA_UNVANI\",\"VERGI_DAIRESI\",\"VERGI_NO\",\"GIDECEGI_YER\",\"NOT_1\",\"NOT_2\",\"NOT_3\",\"BASLIK_BOLUM\",\"BARKOD\",\"URUN_KODU\",\"URUN_ADI\",\"DEPO\",\"SIMGE\",\"BIRIM_FIAT\",\"ISKONTO\",\"MIKTAR\",\"K_D_V\",\"TUTAR\",\"TUTAR_TOPLAM\",\"ISKONTO_TOPLAMI\",\"BAKIYE\",\"K_D_V_TOPLAMI\",\"BELGE_TOPLAMI\",\"YAZI_ILE\",\"ALT_BOLUM\",\"N1\",\"N2\",\"N3\",\"N4\",\"N5\",\"N6\",\"N7\",\"N8\",\"N9\",\"N10\",\"USER\") VALUES ('SUTUN','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE \"FAT_EVRAK_FORMAT\" ( "
				+ "  \"SAT_SUT\"   character varying (5) NULL,"
				+ "  \"TARIH\"   DOUBLE PRECISION  NULL,"
				+ "  \"FIRMA_KODU\"   DOUBLE PRECISION  NULL,"
				+ "  \"FIRMA_UNVANI\"   DOUBLE PRECISION  NULL,"
				+ "  \"VERGI_DAIRESI\"   DOUBLE PRECISION  NULL,"
				+ "  \"VERGI_NO\"   DOUBLE PRECISION  NULL,"
				+ "  \"GIDECEGI_YER\"   DOUBLE PRECISION  NULL,"
				+ "  \"NOT_1\"   DOUBLE PRECISION  NULL,"
				+ "  \"NOT_2\"   DOUBLE PRECISION  NULL,"
				+ "  \"NOT_3\"   DOUBLE PRECISION  NULL,"
				+ "  \"BASLIK_BOLUM\"   DOUBLE PRECISION  NULL,"
				+ "  \"BARKOD\"   DOUBLE PRECISION  NULL,"
				+ "  \"URUN_KODU\"   DOUBLE PRECISION  NULL,"
				+ "  \"URUN_ADI\"   DOUBLE PRECISION  NULL,"
				+ "  \"DEPO\"   DOUBLE PRECISION  NULL,"
				+ "  \"IZAHAT\"   DOUBLE PRECISION  NULL,"
				+ "  \"SIMGE\"   DOUBLE PRECISION  NULL,"
				+ "  \"BIRIM_FIAT\"   DOUBLE PRECISION  NULL,"
				+ "  \"ISKONTO\"   DOUBLE PRECISION  NULL,"
				+ "  \"MIKTAR\"   DOUBLE PRECISION  NULL,"
				+ "  \"K_D_V\"   DOUBLE PRECISION  NULL,"
				+ "  \"TUTAR\"   DOUBLE PRECISION  NULL,"
				+ "  \"TUTAR_TOPLAM\"   DOUBLE PRECISION  NULL,"
				+ "  \"ISKONTO_TOPLAMI\"   DOUBLE PRECISION  NULL,"
				+ "  \"BAKIYE\"   DOUBLE PRECISION  NULL,"
				+ "  \"K_D_V_TOPLAMI\"   DOUBLE PRECISION  NULL,"
				+ "  \"BELGE_TOPLAMI\"   DOUBLE PRECISION  NULL,"
				+ "  \"TEVKIFAT_ORANI\"   DOUBLE PRECISION  NULL,"
				+ "  \"AL_TAR_TEV_ED_KDV\"   DOUBLE PRECISION  NULL,"
				+ "  \"TEV_DAH_TOP_TUTAR\"   DOUBLE PRECISION  NULL,"
				+ "  \"BEYAN_ED_KDV\"   DOUBLE PRECISION  NULL,"
				+ "  \"TEV_HAR_TOP_TUT\"   DOUBLE PRECISION  NULL,"
				+ "  \"YAZI_ILE\"   DOUBLE PRECISION  NULL,"
				+ "  \"TEV_KASESI\"   DOUBLE PRECISION  NULL,"
				+ "  \"ALT_BOLUM\"   DOUBLE PRECISION  NULL,"
				+ "  \"N1\"   DOUBLE PRECISION  NULL,"
				+ "  \"N2\"   DOUBLE PRECISION  NULL,"
				+ "  \"N3\"   DOUBLE PRECISION  NULL,"
				+ "  \"N4\"   DOUBLE PRECISION  NULL,"
				+ "  \"N5\"   DOUBLE PRECISION  NULL,"
				+ "  \"N6\"   DOUBLE PRECISION  NULL,"
				+ "  \"N7\"   DOUBLE PRECISION  NULL,"
				+ "  \"N8\"   DOUBLE PRECISION  NULL,"
				+ "  \"N9\"   DOUBLE PRECISION  NULL,"
				+ "  \"N10\"   DOUBLE PRECISION  NULL,"
				+ "  \"USER\"   character varying (15) NULL) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  \"FAT_EVRAK_FORMAT\"(\"SAT_SUT\",\"TARIH\",\"FIRMA_KODU\",\"FIRMA_UNVANI\",\"VERGI_DAIRESI\" ,\"VERGI_NO\" ,\"GIDECEGI_YER\" ,\"NOT_1\" ,\"NOT_2\" ,\"NOT_3\",\"BASLIK_BOLUM\",\"BARKOD\",\"URUN_KODU\" ,\"URUN_ADI\" , \"DEPO\" ,\"IZAHAT\",\"SIMGE\" ,\"BIRIM_FIAT\" ,\"ISKONTO\" ,\"MIKTAR\",\"K_D_V\" ,\"TUTAR\" ,\"TUTAR_TOPLAM\" ,\"ISKONTO_TOPLAMI\"  ,\"BAKIYE\" ,\"K_D_V_TOPLAMI\" ,\"BELGE_TOPLAMI\" , \"YAZI_ILE\",\"TEVKIFAT_ORANI\" ,\"AL_TAR_TEV_ED_KDV\" ,\"TEV_DAH_TOP_TUTAR\" , \"BEYAN_ED_KDV\" ,\"TEV_HAR_TOP_TUT\",\"TEV_KASESI\",\"ALT_BOLUM\",\"N1\",\"N2\",\"N3\",\"N4\",\"N5\",\"N6\",\"N7\",\"N8\",\"N9\",\"N10\",\"USER\") VALUES " + " ('SATIR','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  \"FAT_EVRAK_FORMAT\"(\"SAT_SUT\",\"TARIH\",\"FIRMA_KODU\",\"FIRMA_UNVANI\",\"VERGI_DAIRESI\" ,\"VERGI_NO\" ,\"GIDECEGI_YER\" ,\"NOT_1\" ,\"NOT_2\" ,\"NOT_3\",\"BASLIK_BOLUM\",\"BARKOD\",\"URUN_KODU\" ,\"URUN_ADI\" , \"DEPO\" ,\"IZAHAT\",\"SIMGE\" ,\"BIRIM_FIAT\" ,\"ISKONTO\" ,\"MIKTAR\",\"K_D_V\" ,\"TUTAR\" ,\"TUTAR_TOPLAM\" ,\"ISKONTO_TOPLAMI\"  ,\"BAKIYE\" ,\"K_D_V_TOPLAMI\" ,\"BELGE_TOPLAMI\" , \"YAZI_ILE\",\"TEVKIFAT_ORANI\" ,\"AL_TAR_TEV_ED_KDV\" ,\"TEV_DAH_TOP_TUTAR\" , \"BEYAN_ED_KDV\" ,\"TEV_HAR_TOP_TUT\",\"TEV_KASESI\",\"ALT_BOLUM\",\"N1\",\"N2\",\"N3\",\"N4\",\"N5\",\"N6\",\"N7\",\"N8\",\"N9\",\"N10\",\"USER\") VALUES " + " ('SUTUN','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		// ***************OZEL NO YAZ *************************
		sql = "INSERT INTO \"OZEL\" (\"YONETICI\",\"YON_SIFRE\",\"FIRMA_ADI\") VALUES (?,? ,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, user_name);
			stmt.setString(2, "12345");
			stmt.setString(3, firmaAdi);
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
	
	public void job_sil_S(String jobName,serverBilgiDTO sbilgi) throws SQLException {
		try {
			Statement stmt = null;
			String connectionString =  "jdbc:postgresql://" + sbilgi.getUser_ip() + "/" + sbilgi.getSuperviser() ;
			Connection conn = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
			stmt = conn.createStatement();
			String sql = "DELETE FROM pgagent.pga_job WHERE jobname = '" + jobName + "_JOB'";
			stmt.execute(sql);
			stmt.close();
			conn.close();
		} catch (Exception e)
		{
			throw new SQLException("Log tablosu veya index oluşturulurken hata oluştu.", e);
		}
	}
	public void job_olustur_S(String jobName, String dosya, String indexISIM, serverBilgiDTO sbilgi) throws SQLException {
		try {
			Class.forName("org.postgresql.Driver");
			Statement stmt = null;
			String connectionString =  "jdbc:postgresql://" + sbilgi.getUser_ip() + "/" + sbilgi.getSuperviser() ;
			Connection conn = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
			stmt = conn.createStatement();
			Date date = Calendar.getInstance().getTime();  
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
			String tarih = dateFormat.format(date);  
			String sql = "DO $$ " +
					" DECLARE " +
					" jid integer;" +
					" scid integer;" +
					" BEGIN " +
					" INSERT INTO pgagent.pga_job( " +
					" jobjclid, jobname, jobdesc, jobhostagent, jobenabled " +
					" ) VALUES ( " + 
					" 1::integer, '" + jobName + "_JOB'::text, ''::text, ''::text, true " +
					" ) RETURNING jobid INTO jid; " +
					" INSERT INTO pgagent.pga_jobstep ( " +
					" jstjobid, jstname, jstenabled, jstkind," +
					" jstconnstr, jstdbname, jstonerror, "+
					" jstcode, jstdesc " +
					" ) VALUES ( " +
					" jid, '" + jobName + "_STEP'::text, true, 's'::character(1), " +
					"    ''::text, '" + dosya + "'::name, 'f'::character(1), " +
					"    '" + indexISIM + "'::text, ''::text " +
					" ); " +
					" INSERT INTO pgagent.pga_schedule( " +
					" jscjobid, jscname, jscdesc, jscenabled, " +
					" jscstart,     jscminutes, jschours, jscweekdays, jscmonthdays, jscmonths " +
					" ) VALUES ( " +
					"  jid, '" + jobName + "_SCHEDULER'::text, ''::text, true, " +
					"    ' " + tarih + "'::timestamp with time zone, " + 
					"    '{t,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f}'::bool[]::boolean[], "+
					"    '{t,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f}'::bool[]::boolean[], " +
					"    '{t,f,f,f,f,f,f}'::bool[]::boolean[], " +
					"    '{f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f}'::bool[]::boolean[], " +
					"    '{f,f,f,f,f,f,f,f,f,f,f,f}'::bool[]::boolean[] ) RETURNING jscid INTO scid; " +
					" END " +
					"  $$ ; " ;
			stmt.execute(sql);
			stmt.close();
			conn.close();
		}
		catch (Exception e)
		{  
			throw new SQLException("Log tablosu veya index oluşturulurken hata oluştu.", e);
		}  
	}

}