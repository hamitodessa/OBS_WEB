package com.hamit.obs.createnewDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.hamit.obs.dto.server.serverBilgiDTO;

public class createMYSQL {

	public boolean serverKontrol(serverBilgiDTO sbilgi) {
		boolean result = false;
		String connectionString =  "jdbc:mysql://" + sbilgi.getUser_ip() ;
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
		String connectionString =  "jdbc:mysql://" + sbilgi.getUser_ip() ;
		String query = "SHOW DATABASES WHERE `Database` = '" + sbilgi.getUser_modul_baslik().toLowerCase() + sbilgi.getUser_prog_kodu() + "';";
		try (Connection conn = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
				PreparedStatement stmt = conn.prepareStatement(query)) {
			DriverManager.setLoginTimeout(5);

			try (ResultSet rs = stmt.executeQuery()) {
				if(rs.isBeforeFirst())
				{
					rs.next();
					result = rs.getRow() > 0;
				}
			}
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	public boolean tableKontrolS(serverBilgiDTO sbilgi, int beklenenTabloSayisi) {
		boolean result = false;
		String connectionString = "jdbc:mysql://" + sbilgi.getUser_ip() ;
		String query = "SELECT COUNT(TABLE_NAME) as SAYI"
				+ " FROM INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = '" + sbilgi.getUser_modul_baslik().toLowerCase() + sbilgi.getUser_prog_kodu() +"';";

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
		String veritabaniAdi = sbilgi.getUser_modul_baslik().toLowerCase() + sbilgi.getUser_prog_kodu();
		String connectionString =  "jdbc:mysql://" + sbilgi.getUser_ip() ;
		String createDatabaseQuery = "CREATE DATABASE " + veritabaniAdi ;
		try (Connection initialConnection = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
				Statement stmt = initialConnection.createStatement()) {
			stmt.executeUpdate(createDatabaseQuery);
			String databaseConnectionString = "jdbc:mysql://" + sbilgi.getUser_ip() + "/" + veritabaniAdi;
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
				case "Fatura": {
					createTableFatura(databaseConnection, sbilgi.getFirma_adi() , sbilgi.getUser_name());
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
		sql = "CREATE TABLE `HESAP` ("
				+ " `HESAP` VARCHAR(12) NOT NULL,"
				+ " `UNVAN` varchar(50) DEFAULT NULL,"
				+ " `KARTON` VARCHAR(5) NULL,"
				+ " `HESAP_CINSI` VARCHAR(3) NULL,"
				+ " `USER` VARCHAR(15) NULL,"
				+ " PRIMARY KEY (`HESAP`),"
				+ " UNIQUE INDEX `HESAP_UNIQUE` (`HESAP` ASC) INVISIBLE,"
				+ " INDEX `IX_HESAP` (`HESAP` ASC) VISIBLE) " 
				+ " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `HESAP_DETAY` ( "
				+ " `D_HESAP` VARCHAR(12) NOT NULL,"
				+ " `YETKILI` VARCHAR(30)  NULL,"
				+ " `TC_KIMLIK` VARCHAR(15)  NULL,"
				+ " `ADRES_1` VARCHAR(35)  NULL,"
				+ " `ADRES_2` VARCHAR(35)  NULL,"
				+ " `SEMT` VARCHAR(15)  NULL,"
				+ " `SEHIR` VARCHAR(15)  NULL,"
				+ " `VERGI_DAIRESI` VARCHAR(25)  NULL,"
				+ " `VERGI_NO` VARCHAR(15)  NULL,"
				+ " `FAX` VARCHAR(25)  NULL,"
				+ " `TEL_1` VARCHAR(25)  NULL,"
				+ " `TEL_2` VARCHAR(25)  NULL,"
				+ " `TEL_3` VARCHAR(25)  NULL,"
				+ " `OZEL_KOD_1` VARCHAR(15)  NULL,"
				+ " `OZEL_KOD_2` VARCHAR(15)  NULL,"
				+ " `OZEL_KOD_3` VARCHAR(15)  NULL,"
				+ " `ACIKLAMA` VARCHAR(30)  NULL,"
				+ " `WEB` VARCHAR(50)  NULL,"
				+ " `E_MAIL` VARCHAR(30)  NULL,"
				+ " `SMS_GONDER` TINYINT NULL,"
				+ " `RESIM` MEDIUMBLOB NULL,"
				+ "  PRIMARY KEY (`D_HESAP`),"
				+ "  UNIQUE INDEX `D_HESAP_UNIQUE` (`D_HESAP` ASC) VISIBLE,"
				+ "  INDEX `IX_DHESAP` (`D_HESAP` ASC) VISIBLE)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `SATIRLAR` ("
				+ " `SID` MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY ,"
				+ " `HESAP` VARCHAR(12) NOT NULL,"
				+ " `TARIH` DATETIME NULL,"
				+ " `H` VARCHAR(1) NULL,"
				+ " `EVRAK` INT NOT NULL,"
				+ " `CINS` VARCHAR(2) NULL,"
				+ " `KUR` DOUBLE NULL,"
				+ " `BORC` DOUBLE NULL,"
				+ " `ALACAK` DOUBLE NULL,"
				+ " `KOD` VARCHAR(5)  NULL,"
				+ " `USER` VARCHAR(15) NULL,"
				+ " UNIQUE INDEX `SID_UNIQUE` (`SID` ASC) VISIBLE,"
				+ " INDEX `IX_SATIRLAR` (`HESAP` ASC, `TARIH` ASC, `EVRAK` ASC) VISIBLE ,"
				+ " INDEX `IXS_HESAP` (`HESAP` ASC  ) VISIBLE)"
				+ " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `IZAHAT`(`EVRAK` int NOT NULL,`IZAHAT` VARCHAR(100) NULL,"
				+ " PRIMARY KEY (`EVRAK`),"
				+ " UNIQUE INDEX `EVRAK_UNIQUE` (`EVRAK` ASC) VISIBLE,"
				+ " FULLTEXT IZ_FULL (`IZAHAT`) ,"
				+ " INDEX `IX_IZAHAT` ( `EVRAK` ASC) VISIBLE)"
				+ " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `EVRAK_NO` (EID INTEGER AUTO_INCREMENT PRIMARY KEY ,`EVRAK` integer ) ;";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `OZEL` ("
				+ " `OZID` INTEGER AUTO_INCREMENT PRIMARY KEY,"
				+ " `YONETICI` VARCHAR(25) NULL,"
				+ " `YON_SIFRE` VARCHAR(15) NULL,"
				+ " `FIRMA_ADI` VARCHAR(50) NULL)"
				+ " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `YETKILER`( "
				+ " `YETID` INTEGER AUTO_INCREMENT PRIMARY KEY,"
				+ " `KULLANICI` VARCHAR(25) NULL,"
				+ " `KARTON` VARCHAR(5) NULL,"
				+ " `TAM_YETKI` TINYINT NULL,"
				+ " `GORUNTU` TINYINT NULL)"
				+ " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql= "CREATE TABLE `ANA_GRUP_DEGISKEN` ("
				+ " `ANA_GRUP` VARCHAR(25) NOT NULL,"
				+ " `USER` VARCHAR(15) NULL,"
				+ " PRIMARY KEY (`ANA_GRUP`))"
				+ " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `ALT_GRUP_DEGISKEN`( "
				+ " `ANA_GRUP` int NOT NULL, "
				+ " `ALT_GRUP` VARCHAR(25) NOT NULL, "
				+ " `USER` VARCHAR(15) NULL)"
				+ " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		//TAHSIL FISI
		sql = "CREATE TABLE `TAH_EVRAK`(`CINS` VARCHAR(3),`NO` integer );";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `TAH_AYARLAR`( " +
				" `LOGO` MEDIUMBLOB NULL," +
				" `FIR_ISMI` VARCHAR(50) NULL, " +
				" `ADR_1` VARCHAR(50) NULL," +
				" `ADR_2` VARCHAR(50) NULL," +
				" `VD_VN` VARCHAR(60) NULL," +
				" `MAIL` VARCHAR(60) NULL," +
				" `DIGER` VARCHAR(50) NULL, " + 
				" `KASE` MEDIUMBLOB NULL)" +
				" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `TAH_DETAY`(" +
				" `EVRAK` VARCHAR(15) NOT NULL," +
				" `TARIH` datetime NULL," +
				" `C_HES` VARCHAR(12) NULL," +
				" `A_HES` VARCHAR(12) NULL," +
				" `CINS` smallint NOT NULL," +
				" `TUTAR` double NULL," +
				" `TUR` SMALLINT NOT NULL," +
				" `ACIKLAMA` VARCHAR(50) NULL," +
				" `DVZ_CINS` VARCHAR(3) NULL," +
				" `POS_BANKA` VARCHAR(40) NULL)" +
				" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `TAH_CEK` (" + 
				" `EVRAK` VARCHAR(15),`CINS` SMALLINT, `BANKA` VARCHAR(40)  , " + 
				" `SUBE` VARCHAR(40) ,`SERI` VARCHAR(20),`HESAP` VARCHAR(20)," + 
				" `BORCLU` VARCHAR(40), " + 
				" `TARIH` DATE," + 
				" `TUTAR` double) " +
				"  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE PROCEDURE return_evrak (artino int)"
				+ " BEGIN"
				+ " UPDATE EVRAK_NO SET EVRAK = EVRAK + artino WHERE EID = 1;"
				+ " SELECT EVRAK FROM EVRAK_NO WHERE EID = 1;"
				+ " END";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE PROCEDURE return_tah_evrak (artino int,cins VARCHAR(10))"
				+ " BEGIN"
				+ " UPDATE TAH_EVRAK SET NO = NO + artino WHERE CINS = cins;"
				+ " SELECT NO FROM TAH_EVRAK WHERE CINS = cins;"
				+ " END";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}

		sql = "INSERT INTO `TAH_EVRAK`(`CINS`,`NO`) VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "GIR");  // 1. Parametre -> CINS
			stmt.setInt(2, 0);         // 2. Parametre -> NO
			stmt.executeUpdate();
		}

		sql = "INSERT INTO TAH_EVRAK(CINS,NO) VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "CIK");  // 1. Parametre -> CINS
			stmt.setInt(2, 0);         // 2. Parametre -> NO
			stmt.executeUpdate();
		}

		// ***************EVRAK NO YAZ ************
		sql = "INSERT INTO `EVRAK_NO` (`EVRAK`) VALUES (?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, 0);  // 1. Parametre -> EVRAK (int olarak)
			stmt.executeUpdate();
		}
		// ***************OZEL NO YAZ ************
		sql = "INSERT INTO `OZEL` (`YONETICI`,`YON_SIFRE`,`FIRMA_ADI`) VALUES (?,?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, user_name);  // 1. Parametre -> user_name
			stmt.setString(2, "12345");    // 2. Parametre -> Sabit değer "12345"
			stmt.setString(3, firmaAdi);   // 3. Parametre -> firmaAdi
			stmt.executeUpdate();
		}
	}

	public void createTableKur(Connection connection) throws SQLException {
		String sql = null;
		sql = "CREATE TABLE `KURLAR`( id int AUTO_INCREMENT PRIMARY KEY ," 
				+ " `Kur` varchar(3),"
				+ " `Tarih` date ,"
				+ " `MA` DOUBLE," 
				+ " `MS` DOUBLE," 
				+ " `SA` DOUBLE," 
				+ " `SS` DOUBLE," 
				+ " `BA` DOUBLE," 
				+ " `BS` DOUBLE," 
				+ "  INDEX `IX_KUR` (`Kur` ASC,Tarih ASC) VISIBLE);" ;
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
	}

	public void createTableAdres(Connection connection, String firmaAdi , String user_name) throws SQLException {
		String sql = null;
		sql = "CREATE TABLE `Adres`( "
				+ " `ID` INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL ,"
				+ " `M_Kodu`  VARCHAR (12)  NOT NULL, "
				+ " `Adi`  VARCHAR (50) NULL,"
				+ " `Adres_1`  VARCHAR (50)  NULL,"
				+ " `Adres_2`  VARCHAR (50)  NULL,"
				+ " `Semt`  VARCHAR (25)  NULL,"
				+ " `Sehir`  VARCHAR (25)  NULL,"
				+ " `Posta_Kodu`  VARCHAR (10)  NULL,"
				+ " `Vergi_Dairesi`  VARCHAR (25)  NULL,"
				+ " `Vergi_No`  VARCHAR (15)  NULL,"
				+ " `Fax`  VARCHAR (25)  NULL,"
				+ " `Tel_1`  VARCHAR (25)  NULL,"
				+ " `Tel_2`  VARCHAR (25)  NULL,"
				+ " `Tel_3`  VARCHAR (25)  NULL,"
				+ " `Ozel`  VARCHAR (30)  NULL,"
				+ " `Yetkili`  VARCHAR (30)  NULL,"
				+ " `E_Mail`  VARCHAR (50)  NULL,"
				+ " `Not_1`  VARCHAR (30)  NULL,"
				+ " `Not_2`  VARCHAR (30)  NULL,"
				+ " `Not_3`  VARCHAR (30)  NULL,"
				+ " `Aciklama`  VARCHAR (50)  NULL,"
				+ " `Sms_Gonder` TINYINT NULL,"
				+ " `Mail_Gonder` TINYINT NULL,"
				+ " `Ozel_Kod_1`  VARCHAR (15)  NULL,"
				+ " `Ozel_Kod_2`  VARCHAR (15)  NULL,"
				+ " `Web`  VARCHAR (50)  NULL,"
				+ " `USER`  VARCHAR (15)  NULL,"
				+ " `Resim` MEDIUMBLOB NULL,"
				+ "  UNIQUE INDEX `M_Kodu_UNIQUE` (`M_Kodu` ASC) VISIBLE,"
				+ "  INDEX `IX_Adres` (`M_Kodu` ASC , `Adi` ASC) VISIBLE)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `OZEL` ("
				+ "  `OZID` INTEGER AUTO_INCREMENT PRIMARY KEY,"
				+ "  `YONETICI` VARCHAR(25) NULL,"
				+ "  `YON_SIFRE` VARCHAR(15) NULL,"
				+ "  `FIRMA_ADI` VARCHAR(50) NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `YETKILER`( "
				+ " `YETID` INTEGER AUTO_INCREMENT PRIMARY KEY,"
				+ "`KULLANICI` VARCHAR(25) NULL,"
				+ "`KARTON` VARCHAR(5) NULL,"
				+ "`TAM_YETKI` TINYINT NULL,"
				+ "`GORUNTU` TINYINT NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		// ***************OZEL NO YAZ ************
		sql = "INSERT INTO `OZEL` (`YONETICI`,`YON_SIFRE`,`FIRMA_ADI`) VALUES (?,?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, user_name);  // 1. Parametre -> user_name
			stmt.setString(2, "12345");    // 2. Parametre -> Sabit değer "12345"
			stmt.setString(3, firmaAdi);   // 3. Parametre -> firmaAdi
			stmt.executeUpdate();
		}
	}

	public void createTableKambiyo(Connection connection, String firmaAdi , String user_name) throws SQLException {
		String sql =null;
		sql = "CREATE TABLE `CEK`(`Cek_No`  nvarchar(10)  PRIMARY KEY  ,`Vade` DATE , " + 
				" `Giris_Bordro`  varchar(10) , " + 
				" `Cikis_Bordro`  varchar(10)  ," +
				" `Giris_Tarihi` DATE , `Cikis_Tarihi` DATE , " + 
				" `Giris_Musteri` varchar(12)  , " + 
				" `Cikis_Musteri` varchar(12) , " +
				" `Banka` varchar(25) , " + 
				" `Sube` varchar(25) , " +
				" `Tutar` DOUBLE ,`Cins` varchar(3) ," + 
				" `Durum` nvarchar(1),`T_Tarih` DATE , `Seri_No` nvarchar(15), " + 
				" `Ilk_Borclu` varchar(30) , " + 
				" `Cek_Hesap_No` varchar(15) , " +
				" `Giris_Ozel_Kod` varchar(15)  , " + 
				" `Cikis_Ozel_Kod` varchar(15) , " + 
				" `USER` nvarchar(15))"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `SENET`(`Senet_No`  nvarchar(10)  PRIMARY KEY,`Vade` DATE,`Giris_Bordro`  nvarchar(10),`Cikis_Bordro`  nvarchar(10) ," 
				+ " `Giris_Tarihi` DATE , `Cikis_Tarihi` DATE , `Giris_Musteri` nvarchar(12),`Cikis_Musteri` nvarchar(12),`Tutar` DOUBLE ,`Cins` nvarchar(3), `Durum` nvarchar(1), "
				+ " `T_Tarih` DATE , `Ilk_Borclu` nvarchar(30),`Sehir` nvarchar(15),`Giris_Ozel_Kod` nvarchar(15) ,`Cikis_Ozel_Kod` nvarchar(15),`USER` nvarchar(15))"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `EVRAK`(`EVRAK` varchar(5) ,`NO` integer )";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `ACIKLAMA`(`ACID`  INTEGER AUTO_INCREMENT PRIMARY KEY,`EVRAK_CINS` nvarchar(3) ,`SATIR` int ,`EVRAK_NO` nvarchar(10) ,`ACIKLAMA` varchar(50)  ,`Gir_Cik` nvarchar(1))"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `OZEL` ("
				+ "  `OZID` INTEGER AUTO_INCREMENT PRIMARY KEY,"
				+ "  `YONETICI` VARCHAR(25)  NULL,"
				+ "  `YON_SIFRE` VARCHAR(15)  NULL,"
				+ "  `FIRMA_ADI` VARCHAR(50)  NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `YETKILER`( "
				+ " `YETID` INTEGER AUTO_INCREMENT PRIMARY KEY,"
				+ "`KULLANICI` varchar(25)  NULL,"
				+ "`KARTON` varchar(5)  NULL,"
				+ "`TAM_YETKI` TINYINT NULL,"
				+ "`GORUNTU` TINYINT NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		// ***************OZEL NO YAZ *************************
		sql = "INSERT INTO `OZEL` (`YONETICI`,`YON_SIFRE`,`FIRMA_ADI`) VALUES (?,?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, user_name); 
			stmt.setString(2, "12345"); 
			stmt.setString(3, firmaAdi); 
			stmt.executeUpdate();
		}
		// ***************CEK GIRIS EVRAK NO YAZ **************
		sql = "INSERT INTO  `EVRAK`(`EVRAK`,`NO`) VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "CEK_G");
			stmt.setInt(2, 0);  
			stmt.executeUpdate();
		}
		// ***************CEK CIKIS EVRAK NO YAZ **************
		sql = "INSERT INTO  `EVRAK`(`EVRAK`,`NO`) VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "CEK_C");
			stmt.setInt(2, 0);  
			stmt.executeUpdate();
		}
		// ***************SENET GIRIS EVRAK NO YAZ ************
		sql = "INSERT INTO  `EVRAK`(`EVRAK`,`NO`) VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "SEN_G");
			stmt.setInt(2, 0);  
			stmt.executeUpdate();
		}
		// ***************SENET CIKIS EVRAK NO YAZ ************
		sql = "INSERT INTO  `EVRAK`(`EVRAK`,`NO`) VALUES (?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, "SEN_C");
			stmt.setInt(2, 0);  
			stmt.executeUpdate();
		}
		sql = "CREATE PROCEDURE return_evrak (artino int,cins VARCHAR(10))"
				+ " BEGIN"
				+ " UPDATE EVRAK SET NO = NO + artino WHERE EVRAK = cins;"
				+ " SELECT NO FROM EVRAK WHERE EVRAK = cins;"
				+ " END " ;
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
	}
	public void createTableFatura(Connection connection, String firmaAdi , String user_name) throws SQLException {
		String sql = null;
		sql = "CREATE TABLE `DPN`( "
				+ " `DID`   INTEGER AUTO_INCREMENT PRIMARY KEY  NOT NULL,"
				+ "  `Evrak_No`  nvarchar (10) NOT NULL,"
				+ "  `Tip`  nvarchar (1) NULL,"
				+ "  `Bir`  nvarchar (40) NULL,"
				+ "  `Iki`  nvarchar (40) NULL,"
				+ "  `Uc`  nvarchar (40) NULL,"
				+ " `Gir_Cik`  nvarchar (1) NULL,"
				+ " `USER`  nvarchar (15) NOT NULL,"
				+ "  INDEX `IX_DPN` (  `Evrak_No` ASC,  `Gir_Cik` ASC ) VISIBLE)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `GDY`( "
				+ "  `GID`   INTEGER AUTO_INCREMENT PRIMARY KEY  NOT NULL,"
				+ "  `Isim`  varchar (50)  NULL, "
				+ "  `Adres`  varchar (50)  NULL, "
				+ "  `Semt`  varchar (50)  NULL, "
				+ "  `Sehir` varchar (50)  NULL, "
				+ "  `USER`  varchar (15)  NOT NULL, "
				+ "  INDEX `IX_GDY` (  `Isim` ASC,  `USER` ASC ) VISIBLE)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql= "CREATE TABLE `FATURA`( "
				+ "`Fatura_No`  nvarchar (10) NOT NULL,"
				+ " `Kodu`  varchar (12)  NULL,"
				+ " `Tarih` datetime NULL,"
				+ " `Kdv`  DOUBLE  NULL,"
				+ " `Doviz`  nvarchar (3) NULL,"
				+ " `Miktar`  DOUBLE  NULL,"
				+ " `Fiat`  DOUBLE  NULL,"
				+ " `Tutar`  DOUBLE  NULL,"
				+ " `Kur`  DOUBLE  NULL,"
				+ " `Cari_Firma`  varchar (12)  NULL,"
				+ " `Iskonto`  DOUBLE  NULL,"
				+ " `Tevkifat`  DOUBLE  NULL,"
				+ " `Ana_Grup`  int  NULL,"
				+ " `Alt_Grup`  int  NULL,"
				+ " `Depo`  int  NULL,"
				+ " `Adres_Firma`  varchar (12)  NULL,"
				+ " `Ozel_Kod`  varchar (10)  NULL,"
				+ " `Gir_Cik`  nvarchar (1) NULL,"
				+ " `Izahat`  varchar (40)  NULL,"
				+ " `Cins`  nvarchar (1) NULL,"
				+ " `USER`  varchar (15)  NOT NULL,"
				+ " INDEX `IX_FATURA`   (`Fatura_No`,`Kodu`,`Tarih`,`Cari_Firma`,`Gir_Cik`) VISIBLE)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `IRSALIYE`( "
				+ " `Irsaliye_No`  nvarchar (10) NOT NULL,"
				+ " `Kodu`  varchar (12)  NULL,"
				+ " `Tarih` datetime NULL,"
				+ " `Kdv`  DOUBLE  NULL,"
				+ " `Doviz`  nvarchar (3) NULL,"
				+ " `Kur`  DOUBLE  NULL,"
				+ " `Miktar`  DOUBLE  NULL,"
				+ " `Fiat`  DOUBLE  NULL,"
				+ " `Tutar`  DOUBLE  NULL,"
				+ " `Firma`  varchar (12)  NULL,"
				+ " `Iskonto`  DOUBLE  NULL,"
				+ " `Fatura_No`  varchar (10)  NULL,"
				+ " `Sevk_Tarihi` date NULL,"
				+ " `Ana_Grup`  int  NULL,"
				+ " `Alt_Grup`  int  NULL,"
				+ " `Depo`  int  NULL,"
				+ " `Cari_Hesap_Kodu` varchar (12)  NULL,"
				+ " `Ozel_Kod`  varchar (10)  NULL,"
				+ " `Hareket`  nvarchar (1) NULL,"
				+ " `Izahat`  varchar (40)  NULL,"
				+ " `Cins`  nvarchar (1) NULL,"
				+ " `USER`  varchar (15)  NOT NULL,"
				+ " INDEX `IX_IRSALIYE`  (`Irsaliye_No`,`Kodu`,`Tarih`,`Firma`,`Hareket`) VISIBLE)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `MAL`( "
				+ " `Kodu`  varchar (12)  PRIMARY KEY  NOT NULL,"
				+ " `Adi`  varchar (40)  NULL,"
				+ " `Birim`  nvarchar (5) NULL,"
				+ " `Kusurat`  int  NULL,"
				+ " `Resim` MEDIUMBLOB  NULL,"
				+ " `Sinif`  varchar (5)  NULL,"
				+ " `Ana_Grup`  int  NULL,"
				+ " `Alt_Grup`  int  NULL,"
				+ " `Aciklama_1`  varchar (25)  NULL,"
				+ " `Aciklama_2`  varchar (25)  NULL,"
				+ " `Ozel_Kod_1`  int  NULL,"
				+ " `Ozel_Kod_2`  int  NULL,"
				+ " `Ozel_Kod_3`  int  NULL,"
				+ " `KDV`  DOUBLE  NULL,"
				+ " `Barkod`  varchar (20)  NULL,"
				+ " `Mensei`  int  NULL,"
				+ " `Agirlik`  DOUBLE  NULL,"
				+ " `Depo`  int  NULL,"
				+ " `Fiat`  DOUBLE  NULL,"
				+ " `Fiat_2`  DOUBLE  NULL,"
				+ " `Fiat_3`  DOUBLE  NULL,"
				+ " `Recete`  varchar (10)  NULL,"
				+ " `USER`  varchar (15)  NOT NULL,"
				+ " INDEX `IX_MAL`  ( `Adi` ASC, `Ana_Grup` ASC,`Alt_Grup` ASC)  VISIBLE)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `RECETE`( "
				+ " `Recete_No`  varchar (10)  NOT NULL,"
				+ " `Ana_Grup`  int  NULL,"
				+ " `Alt_Grup`  int  NULL,"
				+ " `Durum` TINYINT NULL,"
				+ " `Tur`  varchar (7)  NULL,"
				+ " `Kodu`  varchar (10)  NULL,"
				+ " `Miktar`  DOUBLE  NULL,"
				+ " `USER`  varchar (15)  NOT NULL) "
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `OZEL` ("
				+ "  `YONETICI` VARCHAR(25)  NULL,"
				+ "  `YON_SIFRE` VARCHAR(15)  NULL,"
				+ "  `FIRMA_ADI` VARCHAR(50)  NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `STOK`("
				+ " `Evrak_No`  varchar (10)  NOT NULL,"
				+ " `Evrak_Cins`  nvarchar (3) NULL,"
				+ " `Tarih` DATETIME NULL,"
				+ " `Depo`  int  NULL,"
				+ " `Urun_Kodu`  varchar (12)  NULL,"
				+ " `Miktar`  DOUBLE  NULL,"
				+ " `Fiat`  DOUBLE  NULL,"
				+ " `Tutar`  DOUBLE  NULL,"
				+ " `Ana_Grup`  int  NULL,"
				+ " `Alt_Grup`  int  NULL,"
				+ " `Hareket`  nvarchar (1) NULL,"
				+ " `Izahat`  varchar (40)  NULL,"
				+ " `Hesap_Kodu`  varchar (12)  NULL,"
				+ " `Kur` DOUBLE NULL,"
				+ " `Doviz`  varchar (3)  NULL,"
				+ " `Kdvli_Tutar`  DOUBLE  NULL,"
				+ " `B1`  varchar (15)  NULL,"
				+ " `USER`  varchar (15)  NOT NULL,"
				+ " INDEX IX_STOK  (`Urun_Kodu`,`Tarih`,`Hareket`) VISIBLE )"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `MENSEI_DEGISKEN`("
				+ " `MEID` INTEGER AUTO_INCREMENT PRIMARY KEY  NOT NULL,"
				+ " `MEID_Y`  int   NOT NULL,"   
				+ " `MENSEI`  varchar (25)  NOT NULL,"
				+ " `USER`  varchar (15)  NOT NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  `ANA_GRUP_DEGISKEN`("
				+ " `AGID`  INTEGER AUTO_INCREMENT PRIMARY KEY  NOT NULL,"
				+ " `AGID_Y`  int  NOT NULL,"  
				+ " `ANA_GRUP`  varchar (25)  NOT NULL,"
				+ " `USER`  varchar (15)  NOT NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  `ALT_GRUP_DEGISKEN`("
				+ " `ALID` INTEGER AUTO_INCREMENT PRIMARY KEY  NOT NULL,"
				+ " `ALID_Y`  int  NOT NULL,"  
				+ " `ANA_GRUP`  int  NOT NULL,"
				+ " `ALT_GRUP`  varchar (25)  NOT NULL,"
				+ " `USER`  varchar (15)  NOT NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  `ACIKLAMA`("
				+ " `ACID`  INTEGER AUTO_INCREMENT PRIMARY KEY  NOT NULL,"
				+ " `EVRAK_CINS`  nvarchar (3) NULL,"
				+ " `SATIR`  int  NULL,"
				+ " `EVRAK_NO`  varchar (10)  NULL,"
				+ " `ACIKLAMA`  varchar (50)  NULL,"
				+ " `Gir_Cik`  varchar (1)  NULL,"
				+ " INDEX IX_ACIKLAMA  (`EVRAK_CINS` ASC, `EVRAK_NO` ASC, `Gir_Cik` ASC)  VISIBLE )"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  `DEPO_DEGISKEN`("
				+ " `DPID` INTEGER AUTO_INCREMENT PRIMARY KEY  NOT NULL,"
				+ " `DPID_Y`  int   NOT NULL,"   
				+ " `DEPO`  varchar (25)  NOT NULL,"
				+ " `USER`  varchar (15)  NOT NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE  `OZ_KOD_1_DEGISKEN`("
				+ " `OZ1ID`  INTEGER AUTO_INCREMENT PRIMARY KEY  NOT NULL,"
				+ " `OZ1ID_Y`  int   NOT NULL,"  
				+ " `OZEL_KOD_1`  varchar (25)  NOT NULL,"
				+ " `USER`  varchar (15)  NOT NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `OZ_KOD_2_DEGISKEN`("
				+ " `OZ2ID`   INTEGER AUTO_INCREMENT PRIMARY KEY  NOT NULL,"
				+ " `OZ2ID_Y`  int  NOT NULL,"   
				+ " `OZEL_KOD_2`  varchar (25)  NOT NULL,"
				+ " `USER`  varchar (15)  NOT NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `DEPOEVRAK`("
				+ " `E_No`  int  PRIMARY KEY  NOT NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `URET_EVRAK`("
				+ " `E_No`  int PRIMARY KEY  NOT NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `ZAYI_EVRAK`("
				+ " `E_No`  int PRIMARY KEY  NOT NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `YETKILER`("
				+ " `KULLANICI`  varchar (25)  NULL,"
				+ " `HESAP`  varchar (12)  NULL,"
				+ " `TAM_YETKI` TINYINT NULL,"
				+ " `GORUNTU` TINYINT NULL,"
				+ " `LEVEL`  int  NOT NULL,"
				+ " `USER`  varchar (15)  NOT NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `IRS_EVRAK_FORMAT`("
				+ " `SAT_SUT` nchar(5) NULL,"
				+ " `TARIH`  DOUBLE  NULL,"
				+ " `SEVK_TARIH`  DOUBLE  NULL,"
				+ " `FIRMA_KODU`  DOUBLE  NULL,"
				+ " `FIRMA_UNVANI`  DOUBLE  NULL,"
				+ " `VERGI_DAIRESI`  DOUBLE  NULL,"
				+ " `VERGI_NO`  DOUBLE  NULL,"
				+ " `GIDECEGI_YER`  DOUBLE  NULL,"
				+ " `NOT_1`  DOUBLE  NULL,"
				+ " `NOT_2`  DOUBLE  NULL,"
				+ " `NOT_3`  DOUBLE  NULL,"
				+ " `BASLIK_BOLUM`  DOUBLE  NULL,"
				+ " `BARKOD`  DOUBLE  NULL,"
				+ " `URUN_KODU`  DOUBLE  NULL,"
				+ " `URUN_ADI`  DOUBLE  NULL,"
				+ " `DEPO`  DOUBLE  NULL,"
				+ " `SIMGE`  DOUBLE  NULL,"
				+ " `BIRIM_FIAT`  DOUBLE  NULL,"
				+ " `ISKONTO`  DOUBLE  NULL,"
				+ " `MIKTAR`  DOUBLE  NULL,"
				+ " `K_D_V`  DOUBLE  NULL,"
				+ " `TUTAR`  DOUBLE  NULL,"
				+ " `TUTAR_TOPLAM`  DOUBLE  NULL,"
				+ " `ISKONTO_TOPLAMI`  DOUBLE  NULL,"
				+ " `BAKIYE`  DOUBLE  NULL,"
				+ " `K_D_V_TOPLAMI`  DOUBLE  NULL,"
				+ " `BELGE_TOPLAMI`  DOUBLE  NULL,"
				+ " `YAZI_ILE`  DOUBLE  NULL,"
				+ " `ALT_BOLUM`  DOUBLE  NULL,"
				+ " `N1`  DOUBLE  NULL,"
				+ " `N2`  DOUBLE  NULL,"
				+ " `N3`  DOUBLE  NULL,"
				+ " `N4`  DOUBLE  NULL,"
				+ " `N5`  DOUBLE  NULL,"
				+ " `N6`  DOUBLE  NULL,"
				+ " `N7`  DOUBLE  NULL,"
				+ " `N8`  DOUBLE  NULL,"
				+ " `N9`  DOUBLE  NULL,"
				+ " `N10`  DOUBLE  NULL,"
				+ " `USER`  nvarchar (15) NOT NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  IRS_EVRAK_FORMAT(SAT_SUT ,TARIH,SEVK_TARIH,FIRMA_KODU,FIRMA_UNVANI,VERGI_DAIRESI ,VERGI_NO  ,GIDECEGI_YER,NOT_1 ,NOT_2 ,NOT_3,BASLIK_BOLUM,BARKOD,URUN_KODU ,URUN_ADI , DEPO,SIMGE ,BIRIM_FIAT ,ISKONTO ,MIKTAR,K_D_V ,TUTAR ,TUTAR_TOPLAM ,ISKONTO_TOPLAMI  ,BAKIYE ,K_D_V_TOPLAMI ,BELGE_TOPLAMI , YAZI_ILE,ALT_BOLUM, N1 ,N2 ,N3 ,N4 ,N5 ,N6 ,N7 ,N8 ,N9 ,N10,`USER` ) VALUES ('SATIR','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  IRS_EVRAK_FORMAT(SAT_SUT ,TARIH,SEVK_TARIH,FIRMA_KODU,FIRMA_UNVANI,VERGI_DAIRESI ,VERGI_NO  ,GIDECEGI_YER,NOT_1 ,NOT_2 ,NOT_3,BASLIK_BOLUM,BARKOD,URUN_KODU ,URUN_ADI , DEPO,SIMGE ,BIRIM_FIAT ,ISKONTO ,MIKTAR,K_D_V ,TUTAR ,TUTAR_TOPLAM ,ISKONTO_TOPLAMI  ,BAKIYE ,K_D_V_TOPLAMI ,BELGE_TOPLAMI , YAZI_ILE,ALT_BOLUM, N1 ,N2 ,N3 ,N4 ,N5 ,N6 ,N7 ,N8 ,N9 ,N10,`USER` ) VALUES ('SUTUN','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "CREATE TABLE `FAT_EVRAK_FORMAT`( "
				+ " `SAT_SUT` nchar (5) NULL,"
				+ " `TARIH`  DOUBLE  NULL,"
				+ " `FIRMA_KODU`  DOUBLE  NULL,"
				+ " `FIRMA_UNVANI`  DOUBLE  NULL,"
				+ " `VERGI_DAIRESI`  DOUBLE  NULL,"
				+ " `VERGI_NO`  DOUBLE  NULL,"
				+ " `GIDECEGI_YER`  DOUBLE  NULL,"
				+ " `NOT_1`  DOUBLE  NULL,"
				+ " `NOT_2`  DOUBLE  NULL,"
				+ " `NOT_3`  DOUBLE  NULL,"
				+ " `BASLIK_BOLUM`  DOUBLE  NULL,"
				+ " `BARKOD`  DOUBLE  NULL,"
				+ " `URUN_KODU`  DOUBLE  NULL,"
				+ " `URUN_ADI`  DOUBLE  NULL,"
				+ " `DEPO`  DOUBLE  NULL,"
				+ " `IZAHAT`  DOUBLE  NULL,"
				+ " `SIMGE`  DOUBLE  NULL,"
				+ " `BIRIM_FIAT`  DOUBLE  NULL,"
				+ " `ISKONTO`  DOUBLE  NULL,"
				+ " `MIKTAR`  DOUBLE  NULL,"
				+ " `K_D_V`  DOUBLE  NULL,"
				+ " `TUTAR`  DOUBLE  NULL,"
				+ " `TUTAR_TOPLAM`  DOUBLE  NULL,"
				+ " `ISKONTO_TOPLAMI`  DOUBLE  NULL,"
				+ " `BAKIYE`  DOUBLE  NULL,"
				+ " `K_D_V_TOPLAMI`  DOUBLE  NULL,"
				+ " `BELGE_TOPLAMI`  DOUBLE  NULL,"
				+ " `TEVKIFAT_ORANI`  DOUBLE  NULL,"
				+ " `AL_TAR_TEV_ED_KDV`  DOUBLE  NULL,"
				+ " `TEV_DAH_TOP_TUTAR`  DOUBLE  NULL,"
				+ " `BEYAN_ED_KDV`  DOUBLE  NULL,"
				+ " `TEV_HAR_TOP_TUT`  DOUBLE  NULL,"
				+ " `YAZI_ILE`  DOUBLE  NULL,"
				+ " `TEV_KASESI`  DOUBLE  NULL,"
				+ " `ALT_BOLUM`  DOUBLE  NULL,"
				+ " `N1`  DOUBLE  NULL,"
				+ " `N2`  DOUBLE  NULL,"
				+ " `N3`  DOUBLE  NULL,"
				+ " `N4`  DOUBLE  NULL,"
				+ " `N5`  DOUBLE  NULL,"
				+ " `N6`  DOUBLE  NULL,"
				+ " `N7`  DOUBLE  NULL,"
				+ " `N8`  DOUBLE  NULL,"
				+ " `N9`  DOUBLE  NULL,"
				+ " `N10`  DOUBLE  NULL,"
				+ " `USER`  nvarchar (15) NULL)"
				+ "  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  FAT_EVRAK_FORMAT(SAT_SUT,TARIH,FIRMA_KODU,FIRMA_UNVANI,VERGI_DAIRESI ,VERGI_NO ,GIDECEGI_YER ,NOT_1 ,NOT_2 ,NOT_3,BASLIK_BOLUM,BARKOD,URUN_KODU ,URUN_ADI , DEPO ,IZAHAT,SIMGE ,BIRIM_FIAT ,ISKONTO ,MIKTAR,K_D_V ,TUTAR ,TUTAR_TOPLAM ,ISKONTO_TOPLAMI  ,BAKIYE ,K_D_V_TOPLAMI ,BELGE_TOPLAMI , YAZI_ILE,TEVKIFAT_ORANI ,AL_TAR_TEV_ED_KDV ,TEV_DAH_TOP_TUTAR , BEYAN_Ed_KDV ,TEV_HAR_TOP_TUT,TEV_KASESI,ALT_BOLUM,N1 ,N2 ,N3 ,N4 ,N5 ,N6 ,N7 ,N8 ,N9 ,N10,USER ) VALUES " + " ('SATIR','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
		sql = "INSERT INTO  FAT_EVRAK_FORMAT(SAT_SUT,TARIH,FIRMA_KODU,FIRMA_UNVANI,VERGI_DAIRESI ,VERGI_NO ,GIDECEGI_YER ,NOT_1 ,NOT_2 ,NOT_3,BASLIK_BOLUM,BARKOD,URUN_KODU ,URUN_ADI , DEPO ,IZAHAT,SIMGE ,BIRIM_FIAT ,ISKONTO ,MIKTAR,K_D_V ,TUTAR ,TUTAR_TOPLAM ,ISKONTO_TOPLAMI  ,BAKIYE ,K_D_V_TOPLAMI ,BELGE_TOPLAMI , YAZI_ILE,TEVKIFAT_ORANI ,AL_TAR_TEV_ED_KDV ,TEV_DAH_TOP_TUTAR , BEYAN_Ed_KDV ,TEV_HAR_TOP_TUT,TEV_KASESI,ALT_BOLUM,N1 ,N2 ,N3 ,N4 ,N5 ,N6 ,N7 ,N8 ,N9 ,N10,USER) VALUES " + " ('SUTUN','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','Admin')";
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
		sql = "INSERT INTO `OZEL` (`YONETICI`,`YON_SIFRE`,`FIRMA_ADI`) VALUES (?,?,?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, user_name); 
			stmt.setString(2, "12345"); 
			stmt.setString(3, firmaAdi); 
			stmt.executeUpdate();
		}
	}
	public void sifirdan_LOG(serverBilgiDTO sbilgi) {
		String databaseName = sbilgi.getUser_modul_baslik().toLowerCase() + sbilgi.getUser_prog_kodu() + "_log";
		String connectionUrl = "jdbc:mysql://" + sbilgi.getUser_ip() ;
		String createDatabaseSql =  "CREATE DATABASE " + databaseName ;

		sbilgi.setUser_prog_kodu(sbilgi.getUser_prog_kodu() +  "_log");
		if (dosyaKontrol(sbilgi)) {
			return;
		}
		try (Connection initialConnection = DriverManager.getConnection(connectionUrl, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
				Statement stmt = initialConnection.createStatement()) {
			stmt.executeUpdate(createDatabaseSql);

			String logDatabaseUrl =  "jdbc:mysql://" + sbilgi.getUser_ip() + "/" + databaseName;
			try (Connection logConnection = DriverManager.getConnection(logDatabaseUrl, sbilgi.getUser_server(), sbilgi.getUser_pwd_server())) {

				createTableLog(logConnection);
			}
		} catch (Exception e) {
			throw new RuntimeException("LOG veritabanı oluşturulamadı veya tablolar oluşturulamadı.", e);
		}
	}

	private void createTableLog(Connection connection) throws SQLException  {
		String sql = "CREATE TABLE `LOGLAMA` ("
				+ " `TARIH` DATETIME NOT NULL,"
				+ " `MESAJ` VARCHAR(100) NULL,"
				+ " `EVRAK` VARCHAR(15) NULL,"
				+ " `USER_NAME` VARCHAR(15) NULL,"
				+ " INDEX `IX_LOGLAMA` (`TARIH` ASC,`USER_NAME` ASC) VISIBLE)"
				+ " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_turkish_ci; ";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			throw new SQLException("Log tablosu veya index oluşturulurken hata oluştu.", e);
		}
	}

	public void job_sil_S(String jobName,serverBilgiDTO sbilgi) throws SQLException  {
		try {
			Statement stmt = null;
			String connectionString =  "jdbc:mysql://" + sbilgi.getUser_ip() ;
			Connection conn = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
			stmt = conn.createStatement();
			stmt.execute("DROP EVENT IF EXISTS " + jobName + ";");
			stmt.close();
			conn.close();
		} catch (Exception e)
		{
			throw new SQLException("Log tablosu veya index oluşturulurken hata oluştu.", e);
		}
	}

	public void job_olustur_S(String jobName, String dosya,String indexISIM, serverBilgiDTO sbilgi) throws SQLException  {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Statement stmt = null;
			String connectionString =  "jdbc:mysql://" + sbilgi.getUser_ip() ;
			Connection conn = DriverManager.getConnection(connectionString, sbilgi.getUser_server(), sbilgi.getUser_pwd_server());
			stmt = conn.createStatement();
			String sql = "CREATE EVENT IF NOT EXISTS " + jobName
					+ " ON SCHEDULE EVERY '1' DAY"
					+ "	STARTS CONCAT(DATE(NOW()+INTERVAL 1 DAY),' 00:00:00')"
					+ "	DO"
					+ indexISIM  ;
			stmt.execute(sql);
			stmt.close();
			conn.close();
		} catch (Exception e)
		{
			throw new SQLException("Log tablosu veya index oluşturulurken hata oluştu.", e);
		}
	}
}