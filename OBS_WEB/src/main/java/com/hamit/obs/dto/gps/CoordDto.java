package com.hamit.obs.dto.gps;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CoordDto {
	public String time;
	public double lat;
	public double lng;

	public CoordDto(String time, double lat, double lng) {
		this.time = time;
		this.lat = lat;
		this.lng = lng;
	}

	public LocalDateTime getTimeAsLocalDateTime() {
		try {
			String trimmed = time.contains(".") ? time.substring(0, time.indexOf(".")) : time;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			return LocalDateTime.parse(trimmed, formatter);
		} catch (Exception e) {
			return LocalDateTime.MIN;
		}
	}
}