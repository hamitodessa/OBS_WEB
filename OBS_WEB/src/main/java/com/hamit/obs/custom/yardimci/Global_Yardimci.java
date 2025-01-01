package com.hamit.obs.custom.yardimci;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Global_Yardimci {

	public static String[] ipCevir(String ip) {
		String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(ip);
		String[] iphoStrings = {"",""};
		if (matcher.find()) {
			iphoStrings[0] = matcher.group();
			iphoStrings[1] = ip.substring(matcher.group().length()+1,ip.length());
		} else if (ip.contains("localhost")){
			String[] parts = ip.split(":");
			iphoStrings[0] = parts[0];
			iphoStrings[1] = parts[1];
		} else {
			iphoStrings[0] = "";
			iphoStrings[1] = "";
		}
		return iphoStrings;
	}
	
	public static String user_log(String email) {
		String[] parts = email.split("@");
		String user_log = parts[0].length() > 15 
			    ? parts[0].substring(0, 15) 
			    : parts[0];
		return user_log;
	}
}