package com.hamit.obs.controller.gps;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.gps.CoordDto;
import com.hamit.obs.service.gps.GpsServices;

@Controller
public class GpsController {

	@Autowired
	private GpsServices gpsServices;

	@GetMapping("/gps/gps")
	public String gpsrapor() {
		return "gps/gps";
	}

	@GetMapping("/gps/files")
	@ResponseBody
	public ResponseEntity<List<String>> getFiles() throws Exception {
		return ResponseEntity.ok(gpsServices.listGpsTxtFiles());
	}

	@GetMapping("/gps/data")
	public ResponseEntity<List<CoordDto>> getData(@RequestParam String file, @RequestParam String start,
			@RequestParam String end) throws Exception {
		return ResponseEntity.ok(gpsServices.readCoordsFromFtp(file, LocalDate.parse(start), LocalDate.parse(end)));
	}
}
