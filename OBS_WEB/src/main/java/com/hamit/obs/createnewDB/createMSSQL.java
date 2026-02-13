package com.hamit.obs.createnewDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.hamit.obs.custom.enums.modulTipi;
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
			String databaseConnectionString = "jdbc:sqlserver://" + sbilgi.getUser_ip() + ";databaseName=" + veritabaniAdi+ ";trustServerCertificate=true;";
			try (Connection databaseConnection = DriverManager.getConnection(databaseConnectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server())) {
				modulTipi modultip = modulTipi.fromDbValue(sbilgi.getUser_modul());
				switch (modultip) {
				case CARI_HESAP -> createTableCari(databaseConnection, sbilgi.getFirma_adi(), sbilgi.getUser_name());
				case KUR        -> createTableKur(databaseConnection);
				case ADRES      -> createTableAdres(databaseConnection, sbilgi.getFirma_adi(), sbilgi.getUser_name());
				case KAMBIYO    -> createTableKambiyo(databaseConnection, sbilgi.getFirma_adi(), sbilgi.getUser_name());
				case FATURA     -> createTableFatura(databaseConnection, sbilgi.getFirma_adi(), sbilgi.getUser_name());
				case KERESTE    -> createTableKereste(databaseConnection, sbilgi.getFirma_adi(), sbilgi.getUser_name());
				case GUNLUK    -> createTableGunluk(databaseConnection, sbilgi.getFirma_adi(), sbilgi.getUser_name());
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
				+ "	ON [dbo].[SATIRLAR] ([HESAP],[TARIH])"
				+ "	INCLUDE ([EVRAK])";
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
		sql = "CREATE NONCLUSTERED INDEX [IX_ADRES]  ON [dbo].[Adres] ([M_Kodu],[Adi])";
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
	
	public void createTableFatura(Connection connection, String firmaAdi , String user_name) throws SQLException {
		String sql = null;
		sql = "CREATE TABLE [dbo].[DPN]( "
				+ " [DID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [Evrak_No] [nvarchar](10) NOT NULL,"
				+ " [Tip] [nvarchar](1) NULL,"
				+ " [Bir] [nvarchar](40) NULL,"
				+ " [Iki] [nvarchar](40) NULL,"
				+ " [Uc] [nvarchar](40) NULL,"
				+ " [Gir_Cik] [nvarchar](1) NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " CONSTRAINT [PKeyDID] PRIMARY KEY CLUSTERED ("
				+ " [DID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ " = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE NONCLUSTERED INDEX [IX_DPN] ON [dbo].[DPN]( "
				+ " [Evrak_No] ASC, "
				+ " [Gir_Cik] ASC "
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, "
				+ " ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[GDY]( "
				+ "  [GID] [int] IDENTITY(1,1) NOT NULL, "
				+ "  [Isim] [nvarchar](50) NULL, "
				+ "  [Adres] [nvarchar](50) NULL, "
				+ "  [Semt] [nvarchar](50) NULL, "
				+ "  [Sehir] [nvarchar](50) NULL, "
				+ "  [USER] [nvarchar](15) NOT NULL, "
				+ "  CONSTRAINT [PKeyGID] PRIMARY KEY CLUSTERED ( "
				+ "  [GID] ASC "
				+ "  )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS "
				+ "  = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql= "CREATE TABLE [dbo].[FATURA]( "
				+ " [Fatura_No] [nvarchar](10) NOT NULL,"
				+ " [Kodu] [nvarchar](12) NULL,"
				+ " [Tarih] [datetime] NULL,"
				+ " [Kdv] [float] NULL,"
				+ " [Doviz] [nvarchar](3) NULL,"
				+ " [Miktar] [float] NULL,"
				+ " [Fiat] [float] NULL,"
				+ " [Tutar] [float] NULL,"
				+ " [Kur] [float] NULL,"
				+ " [Cari_Firma] [nvarchar](12) NULL,"
				+ " [Iskonto] [float] NULL,"
				+ " [Tevkifat] [float] NULL,"
				+ " [Ana_Grup] [int] NULL,"
				+ " [Alt_Grup] [int] NULL,"
				+ " [Depo] [int] NULL,"
				+ " [Adres_Firma] [nvarchar](12) NULL,"
				+ " [Ozel_Kod] [nvarchar](10) NULL,"
				+ " [Gir_Cik] [nvarchar](1) NULL,"
				+ " [Izahat] [nvarchar](40) NULL,"
				+ " [Cins] [nvarchar](1) NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " INDEX IX_FATURA NONCLUSTERED (Fatura_No,Kodu,Tarih,Cari_Firma,Gir_Cik)) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[IRSALIYE]( "
				+ " [Irsaliye_No] [nvarchar](10) NOT NULL,"
				+ " [Kodu] [nvarchar](12) NULL,"
				+ " [Tarih] [datetime] NULL,"
				+ " [Kdv] [float] NULL,"
				+ " [Doviz] [nvarchar](3) NULL,"
				+ " [Kur] [float] NULL,"
				+ " [Miktar] [float] NULL,"
				+ " [Fiat] [float] NULL,"
				+ " [Tutar] [float] NULL,"
				+ " [Firma] [nvarchar](12) NULL,"
				+ " [Iskonto] [float] NULL,"
				+ " [Fatura_No] [nvarchar](10) NULL,"
				+ " [Sevk_Tarihi] [date] NULL,"
				+ " [Ana_Grup] [int] NULL,"
				+ " [Alt_Grup] [int] NULL,"
				+ " [Depo] [int] NULL,"
				+ " [Cari_Hesap_Kodu] [nvarchar](12) NULL,"
				+ " [Ozel_Kod] [nvarchar](10) NULL,"
				+ " [Hareket] [nvarchar](1) NULL,"
				+ " [Izahat] [nvarchar](40) NULL,"
				+ " [Cins] [nvarchar](1) NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " INDEX IX_IRSALIYE NONCLUSTERED (Irsaliye_No,Kodu,Tarih,Firma,Hareket) )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[MAL]( "
				+ " [Kodu] [nvarchar](12) NOT NULL,"
				+ " [Adi] [nvarchar](40) NULL,"
				+ " [Birim] [nvarchar](5) NULL,"
				+ " [Kusurat] [int] NULL,"
				+ " [Resim] [image] NULL,"
				+ " [Sinif] [nvarchar](5) NULL,"
				+ " [Ana_Grup] [int] NULL,"
				+ " [Alt_Grup] [int] NULL,"
				+ " [Aciklama_1] [nvarchar](25) NULL,"
				+ " [Aciklama_2] [nvarchar](25) NULL,"
				+ " [Ozel_Kod_1] [int] NULL,"
				+ " [Ozel_Kod_2] [int] NULL,"
				+ " [Ozel_Kod_3] [int] NULL,"
				+ " [KDV] [float] NULL,"
				+ " [Barkod] [nvarchar](20) NULL,"
				+ " [Mensei] [int] NULL,"
				+ " [Agirlik] [float] NULL,"
				+ " [Depo] [int] NULL,"
				+ " [Fiat] [float] NULL,"
				+ " [Fiat_2] [float] NULL,"
				+ " [Fiat_3] [float] NULL,"
				+ " [Recete] [nvarchar](10) NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " CONSTRAINT [IX_Kodu] PRIMARY KEY CLUSTERED ("
				+ " [Kodu] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ " = ON) ON [PRIMARY]) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE NONCLUSTERED INDEX [IX_MAL] ON [dbo].[MAL]( "
				+ " [Adi] ASC,"
				+ " [Ana_Grup] ASC,"
				+ " [Alt_Grup] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, "
				+ " ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[RECETE]( "
				+ " [Recete_No] [nvarchar](10) NOT NULL,"
				+ " [Ana_Grup] [int] NULL,"
				+ " [Alt_Grup] [int] NULL,"
				+ " [Durum] [bit] NULL,"
				+ " [Tur] [nvarchar](7) NULL,"
				+ " [Kodu] [nvarchar](10) NULL,"
				+ " [Miktar] [float] NULL,"
				+ " [USER] [nvarchar](15) NOT NULL) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE NONCLUSTERED INDEX [IX_RECETE] ON [dbo].[RECETE]( "
				+ " [Recete_No] ASC,"
				+ " [Kodu] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, "
				+ " ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[OZEL]("
				+ " [OZID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [YONETICI] [nvarchar](25) NULL,"
				+ " [YON_SIFRE] [nvarchar](15) NULL,"
				+ " [FIRMA_ADI] [nvarchar](50) NULL,"
				+ " CONSTRAINT [PKeyOZID] PRIMARY KEY CLUSTERED ([OZID] ASC)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF,"
				+ " IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[STOK]("
				+ " [Evrak_No] [nvarchar](10) NOT NULL,"
				+ " [Evrak_Cins] [nvarchar](3) NULL,"
				+ " [Tarih] [datetime] NULL,"
				+ " [Depo] [int] NULL,"
				+ " [Urun_Kodu] [nvarchar](12) NULL,"
				+ " [Miktar] [float] NULL,"
				+ " [Fiat] [float] NULL,"
				+ " [Tutar] [float] NULL,"
				+ " [Ana_Grup] [int] NULL,"
				+ " [Alt_Grup] [int] NULL,"
				+ " [Hareket] [nvarchar](1) NULL,"
				+ " [Izahat] [nvarchar](40) NULL,"
				+ " [Hesap_Kodu] [nvarchar](12) NULL,"
				+ " [Kur] [FLOAT] NULL,"
				+ " [Doviz] [nvarchar](3) NULL,"
				+ " [Kdvli_Tutar] [float] NULL,"
				+ " [B1] [nvarchar](15) NULL,"
				+ " [USER] [nvarchar](40) NOT NULL,"
				+ " INDEX IX_STOK NONCLUSTERED (Urun_Kodu,Tarih,Hareket))";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE NONCLUSTERED INDEX [IX_Cikan] ON [dbo].[STOK]( "
				+ " [Urun_Kodu] ASC,"
				+ " [Tarih] ASC)"
				+ " INCLUDE ([Miktar]) "
				+ " WHERE ([Hareket]='C')"
				+ " WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF,"
				+ " ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE NONCLUSTERED INDEX [IX_Giren] ON [dbo].[STOK]( "
				+ " [Urun_Kodu] ASC,"
				+ " [Tarih] ASC)"
				+ " INCLUDE ( 	[Fiat]) "
				+ " WHERE ([Hareket]='G')"
				+ " WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF,"
				+ " ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 100)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[MENSEI_DEGISKEN]("
				+ " [MEID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [MEID_Y] [int]  NOT NULL,"   
				+ " [MENSEI] [nvarchar](25) NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " CONSTRAINT [PKeyMEID] PRIMARY KEY CLUSTERED ("
				+ " [MEID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ " = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[ANA_GRUP_DEGISKEN]("
				+ " [AGID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [AGID_Y] [int] NOT NULL,"  
				+ " [ANA_GRUP] [nvarchar](25) NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ "  CONSTRAINT [PKeyAGID] PRIMARY KEY CLUSTERED ("
				+ " [AGID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ " = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[ALT_GRUP_DEGISKEN]("
				+ " [ALID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [ALID_Y] [int] NOT NULL,"  
				+ " [ANA_GRUP] [int] NOT NULL,"
				+ " [ALT_GRUP] [nvarchar](25) NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " CONSTRAINT [PKeyALID] PRIMARY KEY CLUSTERED ("
				+ " [ALID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ " = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[ACIKLAMA]("
				+ " [ACID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [EVRAK_CINS] [nvarchar](3) NULL,"
				+ " [SATIR] [int] NULL,"
				+ " [EVRAK_NO] [nvarchar](10) NULL,"
				+ " [ACIKLAMA] [nvarchar](50) NULL,"
				+ " [Gir_Cik] [nvarchar](1) NULL,"
				+ " CONSTRAINT [PKeyACID] PRIMARY KEY CLUSTERED ("
				+ " [ACID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ " = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE NONCLUSTERED INDEX [IX_ACIKLAMA] ON [dbo].[ACIKLAMA]( "
				+ " [EVRAK_CINS] ASC,"
				+ " [EVRAK_NO] ASC,"
				+ " [Gir_Cik] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, "
				+ " ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[DEPO_DEGISKEN]("
				+ " [DPID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [DPID_Y] [int]  NOT NULL,"   
				+ " [DEPO] [nvarchar](25) NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " CONSTRAINT [PKeyDPID] PRIMARY KEY CLUSTERED ("
				+ " [DPID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ "  = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[OZ_KOD_1_DEGISKEN]("
				+ " [OZ1ID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [OZ1ID_Y] [int]  NOT NULL,"  
				+ " [OZEL_KOD_1] [nvarchar](25) NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " CONSTRAINT [PKeyOZ1ID] PRIMARY KEY CLUSTERED ("
				+ " [OZ1ID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ " = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[OZ_KOD_2_DEGISKEN]("
				+ " [OZ2ID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [OZ2ID_Y] [int] NOT NULL,"   
				+ " [OZEL_KOD_2] [nvarchar](25) NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " CONSTRAINT [PKeyOZ2ID] PRIMARY KEY CLUSTERED ("
				+ " [OZ2ID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS "
				+ " = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[DEPOEVRAK]("
				+ " [E_No] [int] NOT NULL"
				+ " ) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[URET_EVRAK]("
				+ " [E_No] [int] NULL"
				+ " ) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[ZAYI_EVRAK]("
				+ " [E_No] [int] NULL"
				+ " ) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[YETKILER]("
				+ " [KULLANICI] [nvarchar](25) NULL,"
				+ " [HESAP] [nvarchar](12) NULL,"
				+ " [TAM_YETKI] [bit] NULL,"
				+ " [GORUNTU] [bit] NULL,"
				+ " [LEVEL] [int] NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL"
				+ " ) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[IRS_EVRAK_FORMAT]("
				+ " [SAT_SUT] [nchar](5) NULL,"
				+ " [TARIH] [float] NULL,"
				+ " [SEVK_TARIH] [float] NULL,"
				+ " [FIRMA_KODU] [float] NULL,"
				+ " [FIRMA_UNVANI] [float] NULL,"
				+ " [VERGI_DAIRESI] [float] NULL,"
				+ " [VERGI_NO] [float] NULL,"
				+ " [GIDECEGI_YER] [float] NULL,"
				+ " [NOT_1] [float] NULL,"
				+ " [NOT_2] [float] NULL,"
				+ " [NOT_3] [float] NULL,"
				+ " [BASLIK_BOLUM] [float] NULL,"
				+ " [BARKOD] [float] NULL,"
				+ " [URUN_KODU] [float] NULL,"
				+ " [URUN_ADI] [float] NULL,"
				+ " [DEPO] [float] NULL,"
				+ " [SIMGE] [float] NULL,"
				+ " [BIRIM_FIAT] [float] NULL,"
				+ " [ISKONTO] [float] NULL,"
				+ " [MIKTAR] [float] NULL,"
				+ " [K_D_V] [float] NULL,"
				+ " [TUTAR] [float] NULL,"
				+ " [TUTAR_TOPLAM] [float] NULL,"
				+ " [ISKONTO_TOPLAMI] [float] NULL,"
				+ " [BAKIYE] [float] NULL,"
				+ " [K_D_V_TOPLAMI] [float] NULL,"
				+ " [BELGE_TOPLAMI] [float] NULL,"
				+ " [YAZI_ILE] [float] NULL,"
				+ " [ALT_BOLUM] [float] NULL,"
				+ " [N1] [float] NULL,"
				+ " [N2] [float] NULL,"
				+ " [N3] [float] NULL,"
				+ " [N4] [float] NULL,"
				+ " [N5] [float] NULL,"
				+ " [N6] [float] NULL,"
				+ " [N7] [float] NULL,"
				+ " [N8] [float] NULL,"
				+ " [N9] [float] NULL,"
				+ " [N10] [float] NULL,"
				+ " [USER] [nvarchar](15) NOT NULL"
				+ " ) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  IRS_EVRAK_FORMAT(SAT_SUT ,TARIH,SEVK_TARIH,FIRMA_KODU,FIRMA_UNVANI,VERGI_DAIRESI ,VERGI_NO  ,GIDECEGI_YER,NOT_1 ,NOT_2 ,NOT_3,BASLIK_BOLUM,BARKOD,URUN_KODU ,URUN_ADI , DEPO,SIMGE ,BIRIM_FIAT ,ISKONTO ,MIKTAR,K_D_V ,TUTAR ,TUTAR_TOPLAM ,ISKONTO_TOPLAMI  ,BAKIYE ,K_D_V_TOPLAMI ,BELGE_TOPLAMI , YAZI_ILE,ALT_BOLUM, N1 ,N2 ,N3 ,N4 ,N5 ,N6 ,N7 ,N8 ,N9 ,N10,[USER] ) VALUES ('SATIR','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  IRS_EVRAK_FORMAT(SAT_SUT ,TARIH,SEVK_TARIH,FIRMA_KODU,FIRMA_UNVANI,VERGI_DAIRESI ,VERGI_NO  ,GIDECEGI_YER,NOT_1 ,NOT_2 ,NOT_3,BASLIK_BOLUM,BARKOD,URUN_KODU ,URUN_ADI , DEPO,SIMGE ,BIRIM_FIAT ,ISKONTO ,MIKTAR,K_D_V ,TUTAR ,TUTAR_TOPLAM ,ISKONTO_TOPLAMI  ,BAKIYE ,K_D_V_TOPLAMI ,BELGE_TOPLAMI , YAZI_ILE,ALT_BOLUM, N1 ,N2 ,N3 ,N4 ,N5 ,N6 ,N7 ,N8 ,N9 ,N10,[USER] ) VALUES ('SUTUN','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[FAT_EVRAK_FORMAT]( "
				+ " [SAT_SUT] [nchar](5) NULL,"
				+ " [TARIH] [float] NULL,"
				+ " [FIRMA_KODU] [float] NULL,"
				+ " [FIRMA_UNVANI] [float] NULL,"
				+ " [VERGI_DAIRESI] [float] NULL,"
				+ " [VERGI_NO] [float] NULL,"
				+ " [GIDECEGI_YER] [float] NULL,"
				+ " [NOT_1] [float] NULL,"
				+ " [NOT_2] [float] NULL,"
				+ " [NOT_3] [float] NULL,"
				+ " [BASLIK_BOLUM] [float] NULL,"
				+ " [BARKOD] [float] NULL,"
				+ " [URUN_KODU] [float] NULL,"
				+ " [URUN_ADI] [float] NULL,"
				+ " [DEPO] [float] NULL,"
				+ " [IZAHAT] [float] NULL,"
				+ " [SIMGE] [float] NULL,"
				+ " [BIRIM_FIAT] [float] NULL,"
				+ " [ISKONTO] [float] NULL,"
				+ " [MIKTAR] [float] NULL,"
				+ " [K_D_V] [float] NULL,"
				+ " [TUTAR] [float] NULL,"
				+ " [TUTAR_TOPLAM] [float] NULL,"
				+ " [ISKONTO_TOPLAMI] [float] NULL,"
				+ " [BAKIYE] [float] NULL,"
				+ " [K_D_V_TOPLAMI] [float] NULL,"
				+ " [BELGE_TOPLAMI] [float] NULL,"
				+ " [TEVKIFAT_ORANI] [float] NULL,"
				+ " [AL_TAR_TEV_ED_KDV] [float] NULL,"
				+ " [TEV_DAH_TOP_TUTAR] [float] NULL,"
				+ " [BEYAN_ED_KDV] [float] NULL,"
				+ " [TEV_HAR_TOP_TUT] [float] NULL,"
				+ " [YAZI_ILE] [float] NULL,"
				+ " [TEV_KASESI] [float] NULL,"
				+ " [ALT_BOLUM] [float] NULL,"
				+ " [N1] [float] NULL,"
				+ " [N2] [float] NULL,"
				+ " [N3] [float] NULL,"
				+ " [N4] [float] NULL,"
				+ " [N5] [float] NULL,"
				+ " [N6] [float] NULL,"
				+ " [N7] [float] NULL,"
				+ " [N8] [float] NULL,"
				+ " [N9] [float] NULL,"
				+ " [N10] [float] NULL,"
				+ " [USER] [nvarchar](15) NULL"
				+ "  ) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  FAT_EVRAK_FORMAT(SAT_SUT,TARIH,FIRMA_KODU,FIRMA_UNVANI,VERGI_DAIRESI ,VERGI_NO ,GIDECEGI_YER ,NOT_1 ,NOT_2 ,NOT_3,BASLIK_BOLUM,BARKOD,URUN_KODU ,URUN_ADI , DEPO ,IZAHAT,SIMGE ,BIRIM_FIAT ,ISKONTO ,MIKTAR,K_D_V ,TUTAR ,TUTAR_TOPLAM ,ISKONTO_TOPLAMI  ,BAKIYE ,K_D_V_TOPLAMI ,BELGE_TOPLAMI , YAZI_ILE,TEVKIFAT_ORANI ,AL_TAR_TEV_ED_KDV ,TEV_DAH_TOP_TUTAR , BEYAN_Ed_KDV ,TEV_HAR_TOP_TUT,TEV_KASESI,ALT_BOLUM,N1 ,N2 ,N3 ,N4 ,N5 ,N6 ,N7 ,N8 ,N9 ,N10,[USER] ) VALUES " + " ('SATIR','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  FAT_EVRAK_FORMAT(SAT_SUT,TARIH,FIRMA_KODU,FIRMA_UNVANI,VERGI_DAIRESI ,VERGI_NO ,GIDECEGI_YER ,NOT_1 ,NOT_2 ,NOT_3,BASLIK_BOLUM,BARKOD,URUN_KODU ,URUN_ADI , DEPO ,IZAHAT,SIMGE ,BIRIM_FIAT ,ISKONTO ,MIKTAR,K_D_V ,TUTAR ,TUTAR_TOPLAM ,ISKONTO_TOPLAMI  ,BAKIYE ,K_D_V_TOPLAMI ,BELGE_TOPLAMI , YAZI_ILE,TEVKIFAT_ORANI ,AL_TAR_TEV_ED_KDV ,TEV_DAH_TOP_TUTAR , BEYAN_Ed_KDV ,TEV_HAR_TOP_TUT,TEV_KASESI,ALT_BOLUM,N1 ,N2 ,N3 ,N4 ,N5 ,N6 ,N7 ,N8 ,N9 ,N10,[USER]) VALUES " + " ('SUTUN','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		// ***************EVRAK NO YAZ ************
		sql = "INSERT INTO  DEPOEVRAK(E_No) VALUES ('0')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  URET_EVRAK(E_No) VALUES ('0')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  ZAYI_EVRAK(E_No) VALUES ('0')";
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
	}
	public void createTableKereste(Connection connection, String firmaAdi , String user_name) throws SQLException {
		String sql = null;
		sql = "CREATE TABLE [dbo].[PAKET_NO]( "
				+ "  [Pak_No] [int] NOT NULL,"
				+ "  [Konsimento] [nvarchar](15) NOT NULL,"
				+ "  CONSTRAINT [KONSID] PRIMARY KEY CLUSTERED ("
				+ "  [Konsimento] ASC"
				+ "  )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ "  = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[DPN]( "
				+ " [DID] [int] IDENTITY(1,1) NOT NULL,"
				+ "  [Evrak_No] [nvarchar](10) NOT NULL,"
				+ "  [Tip] [nvarchar](1) NULL,"
				+ "  [Bir] [nvarchar](40) NULL,"
				+ "  [Iki] [nvarchar](40) NULL,"
				+ "  [Uc] [nvarchar](40) NULL,"
				+ "  [Gir_Cik] [nvarchar](1) NULL,"
				+ "  [USER] [nvarchar](15) NOT NULL,"
				+ "  CONSTRAINT [PKeyDID] PRIMARY KEY CLUSTERED ("
				+ "  [DID] ASC"
				+ "  )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ "  = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE NONCLUSTERED INDEX [IX_DPN] ON [dbo].[DPN]( "
				+ "   [Evrak_No] ASC, "
				+ "  [Gir_Cik] ASC "
				+ "  )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, "
				+ "  ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[NAKLIYECI]("
				+ " [NAKID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [NAKID_Y] [int]  NOT NULL,"  
				+ " [UNVAN] [nvarchar](50) NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " CONSTRAINT [PKeyNAKID] PRIMARY KEY CLUSTERED ("
				+ " [NAKID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ " = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql= "CREATE TABLE [dbo].[KERESTE]( "
				+ " [ID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [Evrak_No] [nvarchar](10) NOT NULL,"
				+ " [Barkod] [nvarchar](20) NULL,"
				+ " [Kodu] [nvarchar](16) NOT NULL,"
				+ " [Paket_No] [nvarchar] (10) NULL,"
				+ " [Konsimento] [nvarchar](15) NULL,"
				+ " [Miktar] [float] NULL,"
				+ " [Tarih] [datetime] NULL,"
				+ " [Kdv] [float] NULL,"
				+ " [Doviz] [nvarchar](3) NULL,"
				+ " [Fiat] [float] NULL,"
				+ " [Tutar] [float] NULL,"
				+ " [Kur] [float] NULL,"
				+ " [Cari_Firma] [nvarchar](12) NULL,"
				+ " [Adres_Firma] [nvarchar](12) NULL,"
				+ " [Iskonto] [float] NULL,"
				+ " [Tevkifat] [float] NULL,"
				+ " [Ana_Grup] [int] NULL,"
				+ " [Alt_Grup] [int] NULL,"
				+ " [Mensei] [int] NULL,"
				+ " [Depo] [int] NULL,"
				+ " [Ozel_Kod] [int] NULL,"
				+ " [Izahat] [nvarchar](40) NULL,"
				+ " [Nakliyeci] [int] NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " [Cikis_Evrak] [nvarchar](10) NULL,"
				+ " [CTarih] [datetime] NULL,"
				+ " [CKdv] [float] NULL,"
				+ " [CDoviz] [nvarchar](3) NULL,"
				+ " [CFiat] [float] NULL,"
				+ " [CTutar] [float] NULL,"
				+ " [CKur] [float] NULL,"
				+ " [CCari_Firma] [nvarchar](12) NULL,"
				+ " [CAdres_Firma] [nvarchar](12) NULL,"
				+ " [CIskonto] [float] NULL,"
				+ " [CTevkifat] [float] NULL,"
				+ " [CAna_Grup] [int] NULL,"
				+ " [CAlt_Grup] [int] NULL,"
				+ " [CDepo] [int] NULL,"
				+ " [COzel_Kod] [int] NULL,"
				+ " [CIzahat] [nvarchar](40) NULL,"
				+ " [CNakliyeci] [int] NULL,"
				+ " [CUSER] [nvarchar](15)  NULL,"
				+ " [Satir] [int] NOT NULL,"
				+ " [CSatir] [int] NOT NULL,"
				+ " CONSTRAINT [PID] PRIMARY KEY CLUSTERED ( [ID] ASC ) ,"
				+ " INDEX IX_KERESTE NONCLUSTERED (Kodu,Paket_No) INCLUDE([Evrak_No],[Konsimento],[Cari_Firma],[Cikis_Evrak]) ,"
				+ " INDEX IX_GRP_II NONCLUSTERED (Konsimento,Tarih,Cari_Firma) INCLUDE ([Kodu],[Paket_No],[Miktar],[Ana_Grup],[Alt_Grup],[Depo],[Ozel_Kod],[Cikis_Evrak]),"
				+ " INDEX IX_GRP_I NONCLUSTERED  (Konsimento,Tarih,Cari_Firma) INCLUDE (Kodu,Miktar,Ana_Grup,Alt_Grup,Depo,Ozel_Kod,Cikis_Evrak) ) ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[OZEL]("
				+ "[OZID] [int] IDENTITY(1,1) NOT NULL,"
				+ "[YONETICI] [nvarchar](25) NULL,"
				+ "[YON_SIFRE] [nvarchar](15) NULL,"
				+ "[FIRMA_ADI] [nvarchar](50) NULL,"
				+ " CONSTRAINT [PKeyOZID] PRIMARY KEY CLUSTERED ([OZID] ASC)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF,"
				+ "IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[MENSEI_DEGISKEN]("
				+ " [MEID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [MEID_Y] [int]  NOT NULL,"   
				+ " [MENSEI] [nvarchar](25) NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " CONSTRAINT [PKeyMEID] PRIMARY KEY CLUSTERED ("
				+ " [MEID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ " = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[ANA_GRUP_DEGISKEN]("
				+ " [AGID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [AGID_Y] [int] NOT NULL,"  
				+ " [ANA_GRUP] [nvarchar](25) NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ "  CONSTRAINT [PKeyAGID] PRIMARY KEY CLUSTERED ("
				+ " [AGID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ " = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[ALT_GRUP_DEGISKEN]("
				+ " [ALID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [ALID_Y] [int] NOT NULL,"  
				+ " [ANA_GRUP] [int] NOT NULL,"
				+ " [ALT_GRUP] [nvarchar](25) NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " CONSTRAINT [PKeyALID] PRIMARY KEY CLUSTERED ("
				+ " [ALID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ " = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[ACIKLAMA]("
				+ " [ACID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [EVRAK_CINS] [nvarchar](3) NULL,"
				+ " [SATIR] [int] NULL,"
				+ " [EVRAK_NO] [nvarchar](10) NULL,"
				+ " [ACIKLAMA] [nvarchar](50) NULL,"
				+ " [Gir_Cik] [nvarchar](1) NULL,"
				+ " CONSTRAINT [PKeyACID] PRIMARY KEY CLUSTERED ("
				+ " [ACID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ " = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[KOD_ACIKLAMA]("
				+ " [KOD] [nvarchar](2) NOT NULL,"
				+ " [ACIKLAMA] [nvarchar](50) NULL,"
				+ " CONSTRAINT [KOD] PRIMARY KEY CLUSTERED ([KOD] ASC)WITH (PAD_INDEX = OFF,"
				+ " STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]"
				+ ") ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[KONS_ACIKLAMA]("
				+ " [KONS] [nvarchar](15) NOT NULL,"
				+ " [ACIKLAMA] [nvarchar](50) NULL,"
				+ " CONSTRAINT [KONS] PRIMARY KEY CLUSTERED ([KONS] ASC)WITH (PAD_INDEX = OFF, "
				+ " STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS= ON) ON [PRIMARY]" 
				+ ") ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE NONCLUSTERED INDEX [IX_ACIKLAMA] ON [dbo].[ACIKLAMA]( "
				+ " [EVRAK_CINS] ASC,"
				+ " [EVRAK_NO] ASC,"
				+ " [Gir_Cik] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, "
				+ " ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[DEPO_DEGISKEN]("
				+ " [DPID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [DPID_Y] [int]  NOT NULL,"   
				+ " [DEPO] [nvarchar](25) NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " CONSTRAINT [PKeyDPID] PRIMARY KEY CLUSTERED ("
				+ " [DPID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ "  = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[OZ_KOD_1_DEGISKEN]("
				+ " [OZ1ID] [int] IDENTITY(1,1) NOT NULL,"
				+ " [OZ1ID_Y] [int]  NOT NULL,"  
				+ " [OZEL_KOD_1] [nvarchar](25) NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL,"
				+ " CONSTRAINT [PKeyOZ1ID] PRIMARY KEY CLUSTERED ("
				+ " [OZ1ID] ASC"
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS"
				+ " = ON) ON [PRIMARY]) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[YETKILER]("
				+ " [KULLANICI] [nvarchar](25) NULL,"
				+ " [HESAP] [nvarchar](12) NULL,"
				+ " [TAM_YETKI] [bit] NULL,"
				+ " [GORUNTU] [bit] NULL,"
				+ " [LEVEL] [int] NOT NULL,"
				+ " [USER] [nvarchar](15) NOT NULL"
				+ " ) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[IRS_EVRAK_FORMAT]("
				+ " [SAT_SUT] [nchar](5) NULL,"
				+ " [TARIH] [float] NULL,"
				+ " [SEVK_TARIH] [float] NULL,"
				+ " [FIRMA_KODU] [float] NULL,"
				+ " [FIRMA_UNVANI] [float] NULL,"
				+ " [VERGI_DAIRESI] [float] NULL,"
				+ " [VERGI_NO] [float] NULL,"
				+ " [GIDECEGI_YER] [float] NULL,"
				+ " [NOT_1] [float] NULL,"
				+ " [NOT_2] [float] NULL,"
				+ " [NOT_3] [float] NULL,"
				+ " [BASLIK_BOLUM] [float] NULL,"
				+ " [BARKOD] [float] NULL,"
				+ " [URUN_KODU] [float] NULL,"
				+ " [URUN_ADI] [float] NULL,"
				+ " [DEPO] [float] NULL,"
				+ " [SIMGE] [float] NULL,"
				+ " [BIRIM_FIAT] [float] NULL,"
				+ " [ISKONTO] [float] NULL,"
				+ " [MIKTAR] [float] NULL,"
				+ " [K_D_V] [float] NULL,"
				+ " [TUTAR] [float] NULL,"
				+ " [TUTAR_TOPLAM] [float] NULL,"
				+ " [ISKONTO_TOPLAMI] [float] NULL,"
				+ " [BAKIYE] [float] NULL,"
				+ " [K_D_V_TOPLAMI] [float] NULL,"
				+ " [BELGE_TOPLAMI] [float] NULL,"
				+ " [YAZI_ILE] [float] NULL,"
				+ " [ALT_BOLUM] [float] NULL,"
				+ " [N1] [float] NULL,"
				+ " [N2] [float] NULL,"
				+ " [N3] [float] NULL,"
				+ " [N4] [float] NULL,"
				+ " [N5] [float] NULL,"
				+ " [N6] [float] NULL,"
				+ " [N7] [float] NULL,"
				+ " [N8] [float] NULL,"
				+ " [N9] [float] NULL,"
				+ " [N10] [float] NULL,"
				+ " [USER] [nvarchar](15) NOT NULL"
				+ " ) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  IRS_EVRAK_FORMAT(SAT_SUT ,TARIH,SEVK_TARIH,FIRMA_KODU,FIRMA_UNVANI,VERGI_DAIRESI ,VERGI_NO  ,GIDECEGI_YER,NOT_1 ,NOT_2 ,NOT_3,BASLIK_BOLUM,BARKOD,URUN_KODU ,URUN_ADI , DEPO,SIMGE ,BIRIM_FIAT ,ISKONTO ,MIKTAR,K_D_V ,TUTAR ,TUTAR_TOPLAM ,ISKONTO_TOPLAMI  ,BAKIYE ,K_D_V_TOPLAMI ,BELGE_TOPLAMI , YAZI_ILE,ALT_BOLUM, N1 ,N2 ,N3 ,N4 ,N5 ,N6 ,N7 ,N8 ,N9 ,N10,[USER] ) VALUES ('SATIR','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  IRS_EVRAK_FORMAT(SAT_SUT ,TARIH,SEVK_TARIH,FIRMA_KODU,FIRMA_UNVANI,VERGI_DAIRESI ,VERGI_NO  ,GIDECEGI_YER,NOT_1 ,NOT_2 ,NOT_3,BASLIK_BOLUM,BARKOD,URUN_KODU ,URUN_ADI , DEPO,SIMGE ,BIRIM_FIAT ,ISKONTO ,MIKTAR,K_D_V ,TUTAR ,TUTAR_TOPLAM ,ISKONTO_TOPLAMI  ,BAKIYE ,K_D_V_TOPLAMI ,BELGE_TOPLAMI , YAZI_ILE,ALT_BOLUM, N1 ,N2 ,N3 ,N4 ,N5 ,N6 ,N7 ,N8 ,N9 ,N10,[USER] ) VALUES ('SUTUN','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE [dbo].[FAT_EVRAK_FORMAT]( "
				+ " [SAT_SUT] [nchar](5) NULL,"
				+ " [TARIH] [float] NULL,"
				+ " [FIRMA_KODU] [float] NULL,"
				+ " [FIRMA_UNVANI] [float] NULL,"
				+ " [VERGI_DAIRESI] [float] NULL,"
				+ " [VERGI_NO] [float] NULL,"
				+ " [GIDECEGI_YER] [float] NULL,"
				+ " [NOT_1] [float] NULL,"
				+ " [NOT_2] [float] NULL,"
				+ " [NOT_3] [float] NULL,"
				+ " [BASLIK_BOLUM] [float] NULL,"
				+ " [BARKOD] [float] NULL,"
				+ " [URUN_KODU] [float] NULL,"
				+ " [URUN_ADI] [float] NULL,"
				+ " [DEPO] [float] NULL,"
				+ " [IZAHAT] [float] NULL,"
				+ " [SIMGE] [float] NULL,"
				+ " [BIRIM_FIAT] [float] NULL,"
				+ " [ISKONTO] [float] NULL,"
				+ " [MIKTAR] [float] NULL,"
				+ " [K_D_V] [float] NULL,"
				+ " [TUTAR] [float] NULL,"
				+ " [TUTAR_TOPLAM] [float] NULL,"
				+ " [ISKONTO_TOPLAMI] [float] NULL,"
				+ " [BAKIYE] [float] NULL,"
				+ " [K_D_V_TOPLAMI] [float] NULL,"
				+ " [BELGE_TOPLAMI] [float] NULL,"
				+ " [TEVKIFAT_ORANI] [float] NULL,"
				+ " [AL_TAR_TEV_ED_KDV] [float] NULL,"
				+ " [TEV_DAH_TOP_TUTAR] [float] NULL,"
				+ " [BEYAN_ED_KDV] [float] NULL,"
				+ " [TEV_HAR_TOP_TUT] [float] NULL,"
				+ " [YAZI_ILE] [float] NULL,"
				+ " [TEV_KASESI] [float] NULL,"
				+ " [ALT_BOLUM] [float] NULL,"
				+ " [N1] [float] NULL,"
				+ " [N2] [float] NULL,"
				+ " [N3] [float] NULL,"
				+ " [N4] [float] NULL,"
				+ " [N5] [float] NULL,"
				+ " [N6] [float] NULL,"
				+ " [N7] [float] NULL,"
				+ " [N8] [float] NULL,"
				+ " [N9] [float] NULL,"
				+ " [N10] [float] NULL,"
				+ " [USER] [nvarchar](15) NULL"
				+ "  ) ON [PRIMARY]";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  FAT_EVRAK_FORMAT(SAT_SUT,TARIH,FIRMA_KODU,FIRMA_UNVANI,VERGI_DAIRESI ,VERGI_NO ,GIDECEGI_YER ,NOT_1 ,NOT_2 ,NOT_3,BASLIK_BOLUM,BARKOD,URUN_KODU ,URUN_ADI , DEPO ,IZAHAT,SIMGE ,BIRIM_FIAT ,ISKONTO ,MIKTAR,K_D_V ,TUTAR ,TUTAR_TOPLAM ,ISKONTO_TOPLAMI  ,BAKIYE ,K_D_V_TOPLAMI ,BELGE_TOPLAMI , YAZI_ILE,TEVKIFAT_ORANI ,AL_TAR_TEV_ED_KDV ,TEV_DAH_TOP_TUTAR , BEYAN_ED_KDV ,TEV_HAR_TOP_TUT,TEV_KASESI,ALT_BOLUM,N1 ,N2 ,N3 ,N4 ,N5 ,N6 ,N7 ,N8 ,N9 ,N10,[USER] ) VALUES " + " ('SATIR','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  FAT_EVRAK_FORMAT(SAT_SUT,TARIH,FIRMA_KODU,FIRMA_UNVANI,VERGI_DAIRESI ,VERGI_NO ,GIDECEGI_YER ,NOT_1 ,NOT_2 ,NOT_3,BASLIK_BOLUM,BARKOD,URUN_KODU ,URUN_ADI , DEPO ,IZAHAT,SIMGE ,BIRIM_FIAT ,ISKONTO ,MIKTAR,K_D_V ,TUTAR ,TUTAR_TOPLAM ,ISKONTO_TOPLAMI  ,BAKIYE ,K_D_V_TOPLAMI ,BELGE_TOPLAMI , YAZI_ILE,TEVKIFAT_ORANI ,AL_TAR_TEV_ED_KDV ,TEV_DAH_TOP_TUTAR , BEYAN_ED_KDV ,TEV_HAR_TOP_TUT,TEV_KASESI,ALT_BOLUM,N1 ,N2 ,N3 ,N4 ,N5 ,N6 ,N7 ,N8 ,N9 ,N10,[USER]) VALUES " + " ('SUTUN','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
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
	}
	
	public void createTableGunluk(Connection connection, String firmaAdi , String user_name) throws SQLException {
		String sql =null;
		sql = "CREATE TABLE GOREV ([GID] [int] IDENTITY(1,1) NOT NULL,BASL_TARIH DATE,BASL_SAAT nvarchar(5),BIT_TARIH DATE,BIT_SAAT nvarchar(5),TEKRARLA bit,ISIM nvarchar(30),GOREV nvarchar(30),YER nvarchar(30),MESAJ nvarchar(100),SECENEK nvarchar(10),DEGER int,[USER] nvarchar(15) NULL,[GID_NO] int)" ;  
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE NONCLUSTERED INDEX [IX_GOREV] ON [dbo].[GOREV]([ISIM] ASC,[GOREV] ASC"
				+ ")WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)";

		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE GUNLUK ([GRVID] [int] IDENTITY(1,1) NOT NULL,[GID] [int],TARIH DATE,SAAT nvarchar(5),ISIM nvarchar(30),GOREV nvarchar(30),YER nvarchar(30),MESAJ nvarchar(100),[USER] nvarchar(15) NULL)" ;  
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE NONCLUSTERED INDEX [IDX_GUNLUK] ON [dbo].[GUNLUK](	[TARIH] ASC "
				+ " )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)";
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
		sql = "CREATE TABLE EVRAK_NO(EID int identity(1,1) CONSTRAINT PKeyEID PRIMARY KEY,EVRAK integer )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  EVRAK_NO(EVRAK) VALUES ('0')";
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
	}
	public void sifirdan_LOG(serverBilgiDTO sbilgi) {
		String databaseName = sbilgi.getUser_modul_baslik() + sbilgi.getUser_prog_kodu() + "_LOG";
		String connectionUrl = "jdbc:sqlserver://" + sbilgi.getUser_ip() + ";trustServerCertificate=true;";
		String createDatabaseSql =  "CREATE DATABASE [" + databaseName + "]";
		sbilgi.setUser_prog_kodu(sbilgi.getUser_prog_kodu() +  "_LOG");
		if (dosyaKontrol(sbilgi))
			return;
		try (Connection initialConnection = DriverManager.getConnection(connectionUrl, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
				Statement stmt = initialConnection.createStatement()) {
			stmt.executeUpdate(createDatabaseSql);
			String logDatabaseUrl =  "jdbc:sqlserver://" + sbilgi.getUser_ip() + ";databaseName=" + databaseName+ ";trustServerCertificate=true;";
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
	
	public void job_sil_S(String jobName,serverBilgiDTO sbilgi) throws SQLException  {
		try {
			String connectionString =  "jdbc:sqlserver://" + sbilgi.getUser_ip() + ";trustServerCertificate=true;";
			Connection connection = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
			PreparedStatement stmt = connection.prepareStatement("SELECT job_id,[name] FROM msdb.dbo.sysjobs where name= N'" + jobName + "'");
			ResultSet rs = stmt.executeQuery();
			if (rs.isBeforeFirst() ) {  
				rs.next();
				PreparedStatement stmtt = connection.prepareStatement(" EXEC msdb.dbo.sp_delete_job '" + rs.getString("job_id") + "'");
				stmtt.execute();
				stmtt.close();
			}
			stmt.close();
			connection.close();
		}
		catch (Exception e)
		{  
			throw new SQLException("Log tablosu veya index oluşturulurken hata oluştu.", e);
		}  
	}
	public void job_baslat_S(String jobName, serverBilgiDTO sbilgi) throws SQLException  {
		try {
			String connectionString =  "jdbc:sqlserver://" + sbilgi.getUser_ip() + ";trustServerCertificate=true;";
			Connection connection = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
			if(msAgentKontrol_S(connection))
			{
				PreparedStatement stmt = connection.prepareStatement("USE msdb  EXEC sp_start_job  N'" + jobName + "'");
				stmt.execute();
				stmt.close();
				connection.close();
			}
		}
		catch (Exception e)
		{  
			throw new SQLException("Log tablosu veya index oluşturulurken hata oluştu.", e);
		}  
	}
	
	public void job_olustur_S(String jobName, String dosya,String indexISIM , serverBilgiDTO sbilgi) throws SQLException {
		try {
			String connectionString =  "jdbc:sqlserver://" + sbilgi.getUser_ip() + ";trustServerCertificate=true;";
			Connection connection = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
			String sql = " USE [msdb]   "
					+ " BEGIN TRANSACTION "
					+ " DECLARE @ReturnCode INT "
					+ " SELECT @ReturnCode = 0 "
					+ " IF NOT EXISTS (SELECT name FROM msdb.dbo.syscategories WHERE name=N'Database Maintenance' AND category_class=1) "
					+ " BEGIN "
					+ " EXEC @ReturnCode = msdb.dbo.sp_add_category @class=N'JOB', @type=N'LOCAL', @name=N'Database Maintenance' "
					+ " IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback "
					+ " END" 
					+ " DECLARE @jobId BINARY(16) "
					+ " EXEC @ReturnCode =  msdb.dbo.sp_add_job @job_name=N'" + jobName + "', "
					+ " @enabled=1, "
					+ " @notify_level_eventlog=0, "
					+ " @notify_level_email=0, "
					+ " @notify_level_netsend=0, "
					+ " @notify_level_page=0, "
					+ " @delete_level=0, "
					+ " @description=N'No description available.', "
					+ " @category_name=N'Database Maintenance', "
					+ " @job_id = @jobId OUTPUT"	
					+ " IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback"
					+ " EXEC @ReturnCode = msdb.dbo.sp_add_jobstep @job_id=@jobId, @step_name=N'" + dosya + "_Indexle', "
					+ " @step_id=1, "
					+ " @cmdexec_success_code=0, "
					+ " @on_success_action=1, "
					+ " @on_success_step_id=0, "
					+ " @on_fail_action=2, "
					+ " @on_fail_step_id=0, "
					+ " @retry_attempts=0, "
					+ " @retry_interval=0, "
					+ " @os_run_priority=0, @subsystem=N'TSQL', "
					+ " @command=N'USE [" + dosya + "] "
					+ indexISIM 
					+ " ', "
					+ " @database_name=N'" + dosya + "', "
					+ " @flags=0 "
					+ " IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback"
					+ " EXEC @ReturnCode = msdb.dbo.sp_update_job @job_id = @jobId, @start_step_id = 1"
					+ " IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback"
					+ " EXEC @ReturnCode = msdb.dbo.sp_add_jobschedule @job_id=@jobId, @name=N'" + dosya + "_index', "
					+ " @enabled=1, "
					+ " @freq_type=4, "
					+ " @freq_interval=1, "
					+ " @freq_subday_type=1, "
					+ " @freq_subday_interval=0, "
					+ " @freq_relative_interval=0, "
					+ " @freq_recurrence_factor=0, "
					+ " @active_start_date=20231214, "
					+ " @active_end_date=99991231, "
					+ " @active_start_time=0, "
					+ " @active_end_time=235959 "
					+ " IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback"
					+ " EXEC @ReturnCode = msdb.dbo.sp_add_jobserver @job_id = @jobId"
					+ " IF (@@ERROR <> 0 OR @ReturnCode <> 0) GOTO QuitWithRollback"
					+ " COMMIT TRANSACTION"
					+ " GOTO EndSave"
					+ " QuitWithRollback:"
					+ " IF (@@TRANCOUNT > 0) ROLLBACK TRANSACTION"
					+ " EndSave:" ;
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.execute();
			stmt.close();
			connection.close();
		}
		catch (Exception e)
		{  
			throw new SQLException("Log tablosu veya index oluşturulurken hata oluştu.", e);
		}  
	}
	public boolean msAgentKontrol_S( Connection connection) throws SQLException  {
		boolean result = false;
		try {
			String sql = "DECLARE @agent NVARCHAR(512) " +
					" SELECT @agent = COALESCE(N'SQLAgent$' + CONVERT(SYSNAME, SERVERPROPERTY('InstanceName')),  N'SQLServerAgent') " + 
					" EXEC master.dbo.xp_servicecontrol 'QueryState', @agent" ;
			PreparedStatement stmt = connection.prepareStatement(sql);
			ResultSet rss = stmt.executeQuery();
			while(rss.next())
			{
				if(rss.getString("Current Service State").equals("Stopped."))
					result = false;
				else
					result = true;
			}
		}
		catch (Exception e)
		{  
			throw new SQLException("Log tablosu veya index oluşturulurken hata oluştu.", e);
		}  
		return result;	
	}
}