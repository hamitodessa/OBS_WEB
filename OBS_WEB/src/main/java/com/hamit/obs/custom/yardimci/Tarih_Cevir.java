package com.hamit.obs.custom.yardimci;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class Tarih_Cevir {

	public static String tarihEksi1(String tarih)
	{
		SimpleDateFormat datefmt = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH); 
		Date datey;
		LocalDate localDate = null;
		Calendar cal = Calendar.getInstance();
		try {
			Date date = datefmt.parse(tarih);
			cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DAY_OF_MONTH, -1); 
			datey = cal.getTime();
			localDate = datey.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		} catch (Exception e) {
		}
		return localDate.toString() ;
	}
	public static String tarihTers(String tarih) {
		LocalDate localDate = LocalDate.parse(tarih, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		return localDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
	}

	public static String tarihTersSaatliden(String tarih) {
		LocalDate localDate = LocalDate.parse(tarih, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
		return localDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
	}

	public static String dateFormaterSaatli(String tarih) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		Date date = null;
		String convertedDate = null;
		date = dateFormat.parse(tarih);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
		convertedDate = simpleDateFormat.format(date);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.sss", Locale.ENGLISH);
		String str = sdf.format(new Date());
		convertedDate = convertedDate.substring(0, 10) +  " " + str ;
		return convertedDate;
	}
}