package com.hamit.obs.createnewDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.hamit.obs.dto.server.serverBilgiDTO;

public class createMSSQL {

	public boolean serverKontrol(serverBilgiDTO sbilgi) {
		boolean result = false;
		String connectionString =  "jdbc:sqlserver://" + sbilgi.getUser_ip() + ";trustServerCertificate=true;";
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
		String connectionString =  "jdbc:sqlserver://" + sbilgi.getUser_ip() + ";trustServerCertificate=true;";
		String query = "SELECT * FROM sys.databases WHERE name = ?";
		try (Connection conn = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
				PreparedStatement stmt = conn.prepareStatement(query)) {
			DriverManager.setLoginTimeout(5);
			stmt.setString(1, sbilgi.getUser_modul_baslik() + sbilgi.getUser_prog_kodu());
			try (ResultSet rs = stmt.executeQuery()) {
				result = rs.isBeforeFirst();
			}
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	public boolean tableKontrolS(serverBilgiDTO sbilgi, int beklenenTabloSayisi) {
		boolean result = false;
		String connectionString = "jdbc:sqlserver://" + sbilgi.getUser_ip() + ";trustServerCertificate=true;";
		String query = "SELECT COUNT(TABLE_NAME) AS SAYI " +
				"FROM [" + sbilgi.getUser_modul() + sbilgi.getUser_prog_kodu() + "].INFORMATION_SCHEMA.TABLES";
		try (Connection conn = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
				PreparedStatement stmt = conn.prepareStatement(query);
				ResultSet rs = stmt.executeQuery()) {
			DriverManager.setLoginTimeout(5);
			if (rs.next())
				result = rs.getInt("SAYI") == beklenenTabloSayisi;
		} catch (SQLException e) {
			result = false;
		}
		return result;
	}

	public boolean dosyaOlustur(serverBilgiDTO sbilgi) {
		boolean result = false;
		String veritabaniAdi = sbilgi.getUser_modul_baslik() + sbilgi.getUser_prog_kodu();
		String connectionString =  "jdbc:sqlserver://" + sbilgi.getUser_ip() + ";trustServerCertificate=true;";
		String createDatabaseQuery = "CREATE DATABASE [" + veritabaniAdi + "]";
		try (Connection initialConnection = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
				Statement stmt = initialConnection.createStatement()) {
			stmt.executeUpdate(createDatabaseQuery);
			String databaseConnectionString = "jdbc:sqlserver://" + sbilgi.getUser_ip() + ";database=" + veritabaniAdi+ ";trustServerCertificate=true;";
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
		sql = "CREATE TABLE [dbo].[HESAP]("
				+ " [HESAP] [nvarchar](12) NOT NULL,"
				+ " [UNVAN] [nvarchar](50) NULL, "
				+ " [KARTON] [nvarchar](5) NULL,"
				+ " [HESAP_CINSI] [nvarchar](3) NULL,"
				+ " [USER] [nvarchar](15) NULL,"
				+ " CONSTRAINT [IX_HESAP] PRIMARY KEY CLUSTERED ([HESAP] ASC)WITH (PAD_INDEX = OFF,"
				+ " STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]"
				+ ") ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[HESAP_DETAY]( "
				+ " [D_HESAP] [nvarchar](12) NOT NULL,"
				+ " [YETKILI] [nvarchar](30) NULL,"
				+ " [TC_KIMLIK] [nvarchar](15) NULL,"
				+ " [ADRES_1] [nvarchar](35) NULL,"
				+ " [ADRES_2] [nvarchar](35) NULL,"
				+ " [SEMT] [nvarchar](15) NULL,"
				+ " [SEHIR] [nvarchar](15) NULL,"
				+ " [VERGI_DAIRESI] [nvarchar](25) NULL,"
				+ " [VERGI_NO] [nvarchar](15) NULL,"
				+ " [FAX] [nvarchar](25) NULL,"
				+ " [TEL_1] [nvarchar](25) NULL,"
				+ " [TEL_2] [nvarchar](25) NULL,"
				+ " [TEL_3] [nvarchar](25) NULL,"
				+ " [OZEL_KOD_1] [nvarchar](15) NULL,"
				+ " [OZEL_KOD_2] [nvarchar](15) NULL,"
				+ " [OZEL_KOD_3] [nvarchar](15) NULL,"
				+ " [ACIKLAMA] [nvarchar](30) NULL,"
				+ " [WEB] [nvarchar](50) NULL,"
				+ " [E_MAIL] [nvarchar](30) NULL,"
				+ " [SMS_GONDER] [bit] NULL,"
				+ " [RESIM] [image] NULL,"
				+ " CONSTRAINT [D_HESAP] PRIMARY KEY CLUSTERED(	[D_HESAP] ASC)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,"
				+ "  ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY] ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[SATIRLAR]( "
				+ " [SID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [HESAP] [nvarchar](12) NOT NULL,"
				+ " [TARIH] [datetime] NULL,"
				+ " [H] [nvarchar](1) NULL,"
				+ " [EVRAK] [int] NOT NULL,"
				+ " [CINS] [nvarchar](2) NULL,"
				+ " [KUR] [float] NULL,"
				+ " [BORC] [float] NULL,"
				+ " [ALACAK] [float] NULL,"
				+ " [KOD] [nvarchar](5) NULL,"
				+ " [USER] [nvarchar](15) NULL,"
				+ " CONSTRAINT [IX_SID] PRIMARY KEY CLUSTERED(	[SID] ASC)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, "
				+ " IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE NONCLUSTERED INDEX [IX_SATIRLAR] "
				+"	ON [dbo].[SATIRLAR] ([HESAP],[TARIH])"
				+"	INCLUDE ([EVRAK])";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[IZAHAT](	"
				+ " [EVRAK] [int] NOT NULL,	"
				+ " [IZAHAT] [nvarchar](100) NULL,"
				+ " CONSTRAINT [IX_EVRAK] PRIMARY KEY CLUSTERED ([EVRAK] ASC)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF,"
				+ " IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE FULLTEXT CATALOG [IZAHAT] WITH ACCENT_SENSITIVITY = ON AS DEFAULT";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE EVRAK_NO(EID int identity(1,1) CONSTRAINT PKeyEID PRIMARY KEY,EVRAK integer )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[OZEL]("
				+ " [OZID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [YONETICI] [nvarchar](25) NULL,"
				+ " [YON_SIFRE] [nvarchar](15) NULL,"
				+ " [FIRMA_ADI] [nvarchar](50) NULL,"
				+ " CONSTRAINT [PKeyOZID] PRIMARY KEY CLUSTERED (	[OZID] ASC)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF,"
				+ " IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[YETKILER]( "
				+ " [YETID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [KULLANICI] [nvarchar](25) NULL,"
				+ " [KARTON] [nvarchar](5) NULL,"
				+ " [TAM_YETKI] [bit] NULL,"
				+ " [GORUNTU] [bit] NULL,"
				+ " CONSTRAINT [PK_YETID] PRIMARY KEY CLUSTERED "
				+ " ([YETID] ASC)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON,"
				+ " ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql= "CREATE TABLE [dbo].[ANA_GRUP_DEGISKEN]( "
				+ " [ANA_GRUP] [nvarchar](25) NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " CONSTRAINT [IX_ANA_GRUP] PRIMARY KEY CLUSTERED " 
				+ " ([ANA_GRUP] ASC) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, " 
				+ " ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[ALT_GRUP_DEGISKEN]( "
				+ "[ANA_GRUP] [int] NOT NULL, "
				+ "[ALT_GRUP] [nvarchar](25) NOT NULL, "
				+ "[USER] [nvarchar](15) NOT NULL) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE TAH_EVRAK(CINS nvarchar(3),NO integer )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[TAH_AYARLAR]( " +
				" [LOGO] [image] NULL," +
				" [FIR_ISMI] [nvarchar](50) NULL, " +
				" [ADR_1] [nvarchar](50) NULL," +
				" [ADR_2] [nvarchar](50) NULL," +
				" [VD_VN] [nvarchar](60) NULL," +
				" [MAIL] [nvarchar](60) NULL," +
				" [DIGER] [nvarchar](50) NULL, " + 
				" [KASE] [image] NULL" +
				" ) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[TAH_DETAY](" +
				" [EVRAK] [nvarchar](15) NOT NULL," +
				" [TARIH] [datetime] NULL," +
				" [C_HES] [nvarchar](12) NULL," +
				" [A_HES] [nvarchar](12) NULL," +
				" [CINS] [SMALLINT] NOT NULL," +
				" [TUTAR] [float] NULL," +
				" [TUR] [SMALLINT] NOT NULL," +
				" [ACIKLAMA] [nvarchar](50) NULL," +
				" [DVZ_CINS] [nvarchar](3) NULL," +
				" [POS_BANKA] [nvarchar](40) NULL" +
				" ) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE TAH_CEK ( " + 
				" EVRAK nvarchar(15)," + 
				" CINS SMALLINT, " + 
				" BANKA nvarchar(40)," + 
				" SUBE nvarchar(40) ," + 
				" SERI nvarchar(20), " + 
				" HESAP nvarchar(20)," + 
				" BORCLU nvarchar(40)," + 
				" TARIH date," + 
				" TUTAR float) "  ;
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO TAH_EVRAK (CINS, NO) VALUES (?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "GIR"); 
			stmt.setInt(2, 0); 
			stmt.executeUpdate();
		}
		sql = "INSERT INTO TAH_EVRAK(CINS,NO) VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "CIK"); 
			stmt.setInt(2, 0);   
			stmt.executeUpdate();
		}
		sql = "INSERT INTO EVRAK_NO (EVRAK) VALUES (?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, 0);
			stmt.executeUpdate();
		}
		sql = "INSERT INTO OZEL (YONETICI, YON_SIFRE, FIRMA_ADI) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, user_name); 
			stmt.setString(2, "12345");
			stmt.setString(3, firmaAdi);
			stmt.executeUpdate();
		}
	}
	public void createTableKur(Connection connection) throws SQLException {
		String sql = null;
		sql = "CREATE TABLE KURLAR( id int identity(1,1) CONSTRAINT PKeyid PRIMARY KEY ," 
				+ " Kur nvarchar(3),"
				+ " Tarih date ,"
				+ " MA float," 
				+ " MS float," 
				+ " SA float," 
				+ " SS float," 
				+ " BA float," 
				+ " BS float," 
				+ " INDEX IX_KUR NONCLUSTERED (Kur,Tarih ASC) INCLUDE([MA],[MS]) )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
	}
	public void createTableAdres(Connection connection, String firmaAdi , String user_name) throws SQLException {
		String sql = null;
		sql = "CREATE TABLE [dbo].[Adres]( "
				+ " [ID] [int] IDENTITY(1,1) NOT NULL ,"
				+ " [M_Kodu] [nvarchar](12)  NULL, "
				+ " [Adi] [nvarchar](50) NULL,"
				+ " [Adres_1] [nvarchar] (50) NULL,"
				+ " [Adres_2] [nvarchar](50) NULL,"
				+ " [Semt] [nvarchar](25) NULL,"
				+ " [Sehir] [nvarchar](25) NULL,"
				+ " [Posta_Kodu] [nvarchar](10) NULL,"
				+ " [Vergi_Dairesi] [nvarchar](25) NULL,"
				+ " [Vergi_No] [nvarchar](15) NULL,"
				+ " [Fax] [nvarchar](25) NULL,"
				+ " [Tel_1] [nvarchar](25) NULL,"
				+ " [Tel_2] [nvarchar](25) NULL,"
				+ " [Tel_3] [nvarchar](25) NULL,"
				+ " [Ozel] [nvarchar](30) NULL,"
				+ " [Yetkili] [nvarchar](30) NULL,"
				+ " [E_Mail] [nvarchar](50) NULL,"
				+ " [Not_1] [nvarchar](30) NULL,"
				+ " [Not_2] [nvarchar](30) NULL,"
				+ " [Not_3] [nvarchar](30) NULL,"
				+ " [Aciklama] [nvarchar](50) NULL,"
				+ " [Sms_Gonder] [bit] NULL,"
				+ " [Mail_Gonder] [bit] NULL,"
				+ " [Ozel_Kod_1] [nvarchar](15) NULL,"
				+ " [Ozel_Kod_2] [nvarchar](15) NULL,"
				+ " [Web] [nvarchar](50) NULL,"
				+ " [USER] [nvarchar](15) NULL,"
				+ " [Resim] [image] NULL,"
				+ " CONSTRAINT [PKeyID] PRIMARY KEY CLUSTERED (	[ID] ASC "
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, "
				+ " ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE OZEL(OZID int identity(1,1) CONSTRAINT PKeyOZID PRIMARY KEY,YONETICI nvarchar(25), YON_SIFRE nvarchar(15) , FIRMA_ADI nvarchar(50))";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE YETKILER(YETID int identity(1,1) CONSTRAINT PKeyYETID PRIMARY KEY,KULLANICI nvarchar(25), HESAP nvarchar(12), TAM_YETKI bit, GORUNTU bit )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO OZEL (YONETICI, YON_SIFRE, FIRMA_ADI) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, user_name);
			stmt.setString(2, "12345");  
			stmt.setString(3, firmaAdi);
			stmt.executeUpdate();
		}
	}

	public void createTableKambiyo(Connection connection, String firmaAdi , String user_name) throws SQLException {
		String sql =null;
		sql = "CREATE TABLE CEK(Cek_No  nvarchar(10) CONSTRAINT PKeyCID PRIMARY KEY ,Vade date,Giris_Bordro  nvarchar(10),Cikis_Bordro  nvarchar(10) ,Giris_Tarihi date , Cikis_Tarihi date , Giris_Musteri nvarchar(12),Cikis_Musteri nvarchar(12),Banka nvarchar(25),Sube nvarchar(25),Tutar float ,Cins nvarchar(3), Durum nvarchar(1),T_Tarih date , Seri_No nvarchar(15),Ilk_Borclu nvarchar(30),Cek_Hesap_No nvarchar(15),Giris_Ozel_Kod nvarchar(15) ,Cikis_Ozel_Kod nvarchar(15),[USER] nvarchar(15))";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE NONCLUSTERED INDEX [IX_CEK] "
				+"	ON [dbo].[CEK] ([Cek_No],[Vade])"
				+"	INCLUDE ([Giris_Bordro],[Cikis_Bordro])";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE SENET(Senet_No  nvarchar(10)CONSTRAINT PKeyIID PRIMARY KEY,Vade date,Giris_Bordro  nvarchar(10),Cikis_Bordro  nvarchar(10) ,Giris_Tarihi date , Cikis_Tarihi date , Giris_Musteri nvarchar(12),Cikis_Musteri nvarchar(12),Tutar float ,Cins nvarchar(3), Durum nvarchar(1),T_Tarih date , Ilk_Borclu nvarchar(30),Sehir nvarchar(15),Giris_Ozel_Kod nvarchar(15) ,Cikis_Ozel_Kod nvarchar(15),[USER] nvarchar(15))";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE EVRAK(EVRAK nvarchar(5),NO integer )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE ACIKLAMA(ACID int IDENTITY(1,1) CONSTRAINT PKeyACID PRIMARY KEY,EVRAK_CINS nvarchar(3) ,SATIR int ,EVRAK_NO nvarchar(10) ,ACIKLAMA nvarchar(50) ,Gir_Cik nvarchar(1))";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE OZEL(OZID int identity(1,1) CONSTRAINT PKeyOZID PRIMARY KEY,YONETICI nvarchar(25), YON_SIFRE nvarchar(15) , FIRMA_ADI nvarchar(50))";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE YETKILER(YETID int identity(1,1) CONSTRAINT PKeyYETID PRIMARY KEY,KULLANICI nvarchar(25), HESAP nvarchar(12), TAM_YETKI bit, GORUNTU bit )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		// ***************OZEL NO YAZ *************************
		sql = "INSERT INTO OZEL (YONETICI, YON_SIFRE, FIRMA_ADI) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, user_name);
			stmt.setString(2, "12345");
			stmt.setString(3, firmaAdi);
			stmt.executeUpdate();
		}
		// ***************CEK GIRIS EVRAK NO YAZ **************
		sql = "INSERT INTO  EVRAK(EVRAK,NO) VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "CEK_G");  
			stmt.setInt(2, 0); 
			stmt.executeUpdate();
		}
		// ***************CEK CIKIS EVRAK NO YAZ **************
		sql = "INSERT INTO  EVRAK(EVRAK,NO) VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "CEK_C");  
			stmt.setInt(2, 0); 
			stmt.executeUpdate();
		}
		// ***************SENET GIRIS EVRAK NO YAZ ************
		sql = "INSERT INTO  EVRAK(EVRAK,NO) VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "SEN_G");  
			stmt.setInt(2, 0); 
			stmt.executeUpdate();
		}
		// ***************SENET CIKIS EVRAK NO YAZ ************
		sql = "INSERT INTO  EVRAK(EVRAK,NO) VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "SEN_C");  
			stmt.setInt(2, 0); 
			stmt.executeUpdate();
		}
	}

	public void sifirdan_LOG(serverBilgiDTO sbilgi) {
		String databaseName = sbilgi.getUser_modul_baslik() + sbilgi.getUser_prog_kodu() + "_LOG";
		String connectionUrl = "jdbc:sqlserver://" + sbilgi.getUser_ip() + ";trustServerCertificate=true;";
		String createDatabaseSql =  "CREATE DATABASE [" + databaseName + "]";

		sbilgi.setUser_prog_kodu(sbilgi.getUser_prog_kodu() +  "_LOG");
		if (dosyaKontrol(sbilgi)) {
			return;
		}
		try (Connection initialConnection = DriverManager.getConnection(connectionUrl, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
				Statement stmt = initialConnection.createStatement()) {
			stmt.executeUpdate(createDatabaseSql);

			String logDatabaseUrl =  "jdbc:sqlserver://" + sbilgi.getUser_ip() + ";database=" + databaseName+ ";trustServerCertificate=true;";
			try (Connection logConnection = DriverManager.getConnection(logDatabaseUrl, sbilgi.getUser_server(), sbilgi.getUser_pwd_server())) {

				createTableLog(logConnection);
			}
		} catch (Exception e) {
			throw new RuntimeException("LOG veritabanı oluşturulamadı veya tablolar oluşturulamadı.", e);
		}
	}

	private void createTableLog(Connection connection) throws SQLException  {
		String createTableSql = "CREATE TABLE [dbo].[LOGLAMA]("
				+ "	[TARIH] [datetime] NOT NULL,"
				+ "	[MESAJ] [nchar](100) NOT NULL,"
				+ "	[EVRAK] [nchar](15) NOT NULL,"
				+ "	[USER_NAME] [nchar](25) NULL) ON [PRIMARY]";
		String createIndexSql = "CREATE NONCLUSTERED INDEX [IX_LOGLAMA] ON [dbo].[LOGLAMA](" +
				"   [TARIH] ASC, " +
				"   [EVRAK] ASC, " +
				"   [USER_NAME] ASC" +
				"   ) WITH (" +
				"   PAD_INDEX = OFF, " +
				"   STATISTICS_NORECOMPUTE = OFF, " +
				"   SORT_IN_TEMPDB = OFF, " +
				"   DROP_EXISTING = OFF, " +
				"   ONLINE = OFF, " +
				"   ALLOW_ROW_LOCKS = ON, " +
				"   ALLOW_PAGE_LOCKS = ON" +
				")";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(createTableSql);
			stmt.executeUpdate(createIndexSql);
		} catch (SQLException e) {
			throw new SQLException("Log tablosu veya index oluşturulurken hata oluştu.", e);
		}
	}
}