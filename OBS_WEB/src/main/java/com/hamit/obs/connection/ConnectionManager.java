package com.hamit.obs.connection;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.custom.enums.modulbaslikTipi;
import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.custom.yardimci.TextSifreleme;
import com.hamit.obs.model.user.User_Details;
import com.hamit.obs.service.user.UserDetailsService;

@Component
public class ConnectionManager {
	private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

	@Autowired
	private UserDetailsService userDetailsService;

	public void loadAllConnections(String userEmail) {
		List<User_Details> list = userDetailsService.user_Details_All(userEmail);
		if (list.isEmpty()) {
			log.warn("Bağlantı bilgisi bulunamadı: " + userEmail);
			throw new RuntimeException("Bağlantı bilgisi bulunamadı: " + userEmail);
		}
		for (User_Details details : list) {
			String modul = details.getUser_modul();
			modulTipi mt = modulTipi.fromDbValue(modul); 
			String dosbaslangic = switch (mt) {
			case CARI_HESAP -> modulbaslikTipi.OK_Car.name();
			case KUR        -> modulbaslikTipi.OK_Kur.name();
			case ADRES      -> modulbaslikTipi.OK_Adr.name();
			case KAMBIYO    -> modulbaslikTipi.OK_Kam.name();
			case FATURA     -> modulbaslikTipi.OK_Fat.name();
			case KERESTE    -> modulbaslikTipi.OK_Ker.name();
			case GUNLUK    -> modulbaslikTipi.OK_Gun.name();
			default -> throw new IllegalArgumentException("Unexpected modul: " + mt);
			};
			String jdbcUrl    = generateJdbcUrl(details, modul, dosbaslangic);
			String jdbcUrlLog = generateJdbcUrlLog(details, dosbaslangic);
			UserSessionManager.addUserSession(userEmail,mt,
					new ConnectionDetails(
							details.getUser_ip(),
							details.getUser_prog_kodu(),
							details.getUser_server(),
							TextSifreleme.decrypt(details.getUser_pwd_server()),
							sqlTipi.fromString(details.getHangi_sql()),
							details.getLog(),jdbcUrl,jdbcUrlLog));
		}
	}

	private String generateJdbcUrl(User_Details details,String modul,String dosbaslangic) {
		String cnnString="" ;
		if(details.getHangi_sql().equals(sqlTipi.MSSQL.getValue()))
			cnnString = "jdbc:sqlserver://" + details.getUser_ip() + ";databaseName=" + dosbaslangic + details.getUser_prog_kodu() + ";trustServerCertificate=true;";
		else if(details.getHangi_sql().equals(sqlTipi.MYSQL.getValue()))
			cnnString = "jdbc:mysql://" + details.getUser_ip() +  "/" + dosbaslangic.toLowerCase() + details.getUser_prog_kodu();
		else if(details.getHangi_sql().equals(sqlTipi.PGSQL.getValue()))
			cnnString = "jdbc:postgresql://" + details.getUser_ip() +  "/" + dosbaslangic.toLowerCase() + details.getUser_prog_kodu();
		return cnnString ;
	}

	private String generateJdbcUrlLog(User_Details details,String dosbaslangic) {
		String cnnString="" ;
		if(details.getHangi_sql().equals(sqlTipi.MSSQL.getValue()))
			cnnString = "jdbc:sqlserver://" + details.getUser_ip() + ";databaseName=" + dosbaslangic + details.getUser_prog_kodu() + "_LOG" +	";trustServerCertificate=true;";
		else if(details.getHangi_sql().equals(sqlTipi.MYSQL.getValue()))
			cnnString = "jdbc:mysql://" + details.getUser_ip() +  "/" + dosbaslangic.toLowerCase() + details.getUser_prog_kodu()  + "_LOG";
		else if(details.getHangi_sql().equals(sqlTipi.PGSQL.getValue()))
			cnnString = "jdbc:postgresql://" + details.getUser_ip() +  "/" + dosbaslangic.toLowerCase() + details.getUser_prog_kodu()  + "_log";
		return cnnString ;
	}

}