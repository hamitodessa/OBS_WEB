package com.hamit.obs.connection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hamit.obs.custom.yardimci.TextSifreleme;
import com.hamit.obs.model.user.User_Details;
import com.hamit.obs.service.user.UserDetailsService;

@Component
public class ConnectionManager {

	private final Map<String, ConnectionDetails> userConnectionMap = new HashMap<>();

	@Autowired
	private UserDetailsService userDetailsService;

	public ConnectionDetails getConnection(String modul, String userEmail) {
		switch (modul) {
		case "Cari Hesap":
			return getConnect(userEmail, "Cari Hesap");
		case "Kur":
			return getConnect(userEmail,"Kur");
		case "Adres":
			return getConnect(userEmail,"Adres");
		case "Kambiyo":
			return getConnect(userEmail,"Kambiyo");
		case "Fatura":
			return getConnect(userEmail,"Fatura");
		default:
			throw new IllegalArgumentException("Geçersiz modül adı: " + modul);
		}
	}

	public void loadConnections(String modul,String userEmail) {
		switch (modul) {
		case "Cari Hesap":
			loadConnect(userEmail,modul ,"ok_car");
			break;
		case "Kur":
			loadConnect(userEmail,modul,"ok_kur");
			break;
		case "Adres":
			loadConnect(userEmail,modul,"ok_adr");
			break;
		case "Kambiyo":
			loadConnect(userEmail,modul,"ok_kam");
			break;
		case "Fatura":
			loadConnect(userEmail,modul,"ok_fat");
			break;
		}
	}

	private ConnectionDetails getConnect(String userEmail , String modul) {
		if (!userConnectionMap.containsKey(userEmail)) {
			throw new RuntimeException(modul + " bağlantı bilgisi bulunamadı: " + userEmail);
		}
		return userConnectionMap.get(userEmail);
	}

	private void loadConnect(String userEmail, String modul,String dosbaslangic) {
		List<User_Details> userDetailsList = userDetailsService.user_Details_Modul(modul, userEmail);
		if (userDetailsList.isEmpty()) {
			throw new RuntimeException(modul + " modülü için bağlantı bilgisi bulunamadı.");
		}
		User_Details details = userDetailsList.get(0);
		String jdbcUrl = generateJdbcUrl(details,modul,dosbaslangic);
		String jdbcUrlLog = generateJdbcUrlLog(details,dosbaslangic);
		userConnectionMap.put(userEmail, new ConnectionDetails(
				details.getUser_ip(),
				details.getUser_prog_kodu(),
				details.getUser_server(),
				TextSifreleme.decrypt(details.getUser_pwd_server()) ,
				details.getHangi_sql(),
				details.getLog(),
				jdbcUrl ,jdbcUrlLog
				));
	}

	private String generateJdbcUrl(User_Details details,String modul,String dosbaslangic) {
		String cnnString="" ;
		if(details.getHangi_sql().equals("MS SQL"))
			cnnString = "jdbc:sqlserver://" + details.getUser_ip() + ";databaseName=" + dosbaslangic.toUpperCase() + details.getUser_prog_kodu() + ";trustServerCertificate=true;";
		else if(details.getHangi_sql().equals("MY SQL"))
			cnnString = "jdbc:mysql://" + details.getUser_ip() +  "/" + dosbaslangic + details.getUser_prog_kodu();
		else if(details.getHangi_sql().equals("PG SQL"))
			cnnString = "jdbc:postgresql://" + details.getUser_ip() +  "/" + dosbaslangic + details.getUser_prog_kodu();
		return cnnString ;
	}

	private String generateJdbcUrlLog(User_Details details,String dosbaslangic) {
		String cnnString="" ;
		if(details.getHangi_sql().equals("MS SQL"))
			cnnString = "jdbc:sqlserver://" + details.getUser_ip() + ";databaseName=" + dosbaslangic.toUpperCase() + details.getUser_prog_kodu() + "_LOG" +	";trustServerCertificate=true;";
		else if(details.getHangi_sql().equals("MY SQL"))
			cnnString = "jdbc:mysql://" + details.getUser_ip() +  "/" + dosbaslangic + details.getUser_prog_kodu()  + "_LOG";
		else if(details.getHangi_sql().equals("PG SQL"))
			cnnString = "jdbc:postgresql://" + details.getUser_ip() +  "/" + dosbaslangic + details.getUser_prog_kodu()  + "_log";
		return cnnString ;
	}
}