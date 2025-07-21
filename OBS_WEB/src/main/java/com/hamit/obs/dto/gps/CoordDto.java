package com.hamit.obs.dto.gps;

public class CoordDto {
	public String time;
	public double lat;
	public double lng;

	public CoordDto(String time, double lat, double lng) {
		this.time = time;
		this.lat = lat;
		this.lng = lng;
	}
}