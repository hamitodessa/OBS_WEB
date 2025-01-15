package com.hamit.obs.custom.yardimci;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Global_Yardimci {

	public static String[] ipCevir(String ip) {
	    String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
	    Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
	    Matcher matcher = pattern.matcher(ip);
	    String[] result = {"", "", ""};
	    
	    if (matcher.find()) {
	        result[0] = matcher.group();
	        String remaining = ip.substring(matcher.group().length() + 1); // Port ve User kısmı
	        String[] parts = remaining.split("/", 2); // ':' ve '/' ile bölme
	        String[] portAndUser = parts[0].split(":");
	        result[1] = portAndUser[1]; // Port
	        result[2] = parts[1]; // User
	    } else if (ip.contains("localhost")) {
	        String[] parts = ip.split(":|/"); // Hem ':' hem '/' ile böl
	        result[0] = parts[0]; // localhost
	        result[1] = parts[1]; // Port
	        result[2] = parts[2]; // User
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
}