package com.hamit.obs.custom.yardimci;

import java.time.LocalDate;

public class Global_Yardimci {

	public static String[] ipCevir(String ip) {
	    String[] result = {"", "", ""}; // IP, Port, User
	    if (ip.contains(":")) {
	        String[] ipPortAndUser = ip.split(":", 2); // IP ve sonrası
	        result[0] = ipPortAndUser[0]; // IP kısmı
	        if (ipPortAndUser.length > 1) {
	            String[] portAndUser = ipPortAndUser[1].split("/", 2); // Port ve User
	            result[1] = portAndUser[0]; // Port kısmı
	            result[2] = portAndUser.length > 1 ? portAndUser[1] : ""; // Kullanıcı varsa al
	        }
	    }
	    return result;
	}

	public static String user_log(String email) {
		String[] parts = email.split("@");
		String user_log = parts[0].length() > 15 
			    ? parts[0].substring(0, 15) 
			    : parts[0];
		return user_log;
	}
	
	public static boolean validCheck(String value)
	{
		boolean result ;
		if(value.trim().length() == 0)
			result = false;
		else
			result = true;
		return result ;
	}
	
	public static LocalDate toLocalDateSafe(String s) {
		String t = s == null ? "" : s.trim();
		if (t.length() >= 10) t = t.substring(0, 10); 
		return LocalDate.parse(t);
	}

}