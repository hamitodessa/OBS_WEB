package com.hamit.obs.connection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.custom.enums.modulbaslikTipi;
import com.hamit.obs.custom.yardimci.TextSifreleme;
import com.hamit.obs.model.user.User_Details;
import com.hamit.obs.service.user.UserDetailsService;

@Component
public class ConnectionManager {

	private final Map<String, ConnectionDetails> userConnectionMap = new HashMap<>();

	@Autowired
	private UserDetailsService userDetailsService;

	public ConnectionDetails getConnection(modulTipi modul, String userEmail) {
		return getConnect(userEmail, modul.getDbValue());
	}

	public void loadConnections(modulTipi modul, String userEmail) {
		String baslik = switch (modul) {
			case CARI_HESAP -> modulbaslikTipi.OK_Car.name();
			case KUR        -> modulbaslikTipi.OK_Kur.name();
			case ADRES      -> modulbaslikTipi.OK_Adr.name();
			case KAMBIYO    -> modulbaslikTipi.OK_Kam.name();
			case FATURA     -> modulbaslikTipi.OK_Fat.name();
			case KERESTE    -> modulbaslikTipi.OK_Ker.name();
		default -> throw new IllegalArgumentException("Unexpected value: " + modul);
		};

		loadConnect(userEmail, modul.getDbValue(), baslik);
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
			cnnString = "jdbc:sqlserver://" + details.getUser_ip() + ";databaseName=" + dosbaslangic + details.getUser_prog_kodu() + ";trustServerCertificate=true;";
		else if(details.getHangi_sql().equals("MY SQL"))
			cnnString = "jdbc:mysql://" + details.getUser_ip() +  "/" + dosbaslangic.toLowerCase() + details.getUser_prog_kodu();
		else if(details.getHangi_sql().equals("PG SQL"))
			cnnString = "jdbc:postgresql://" + details.getUser_ip() +  "/" + dosbaslangic.toLowerCase() + details.getUser_prog_kodu();
		return cnnString ;
	}

	private String generateJdbcUrlLog(User_Details details,String dosbaslangic) {
		String cnnString="" ;
		if(details.getHangi_sql().equals("MS SQL"))
			cnnString = "jdbc:sqlserver://" + details.getUser_ip() + ";databaseName=" + dosbaslangic + details.getUser_prog_kodu() + "_LOG" +	";trustServerCertificate=true;";
		else if(details.getHangi_sql().equals("MY SQL"))
			cnnString = "jdbc:mysql://" + details.getUser_ip() +  "/" + dosbaslangic.toLowerCase() + details.getUser_prog_kodu()  + "_LOG";
		else if(details.getHangi_sql().equals("PG SQL"))
			cnnString = "jdbc:postgresql://" + details.getUser_ip() +  "/" + dosbaslangic.toLowerCase() + details.getUser_prog_kodu()  + "_log";
		return cnnString ;
	}
}