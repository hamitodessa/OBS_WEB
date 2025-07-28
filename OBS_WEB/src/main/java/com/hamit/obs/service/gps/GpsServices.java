package com.hamit.obs.service.gps;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Service;

import com.hamit.obs.dto.gps.CoordDto;

@Service
public class GpsServices {


	public List<String> listGpsTxtFiles(String userEmail) throws Exception {
		FTPClient ftp = new FTPClient();
		ftp.connect("78.189.76.247");
		ftp.login("hamitadmin", "SDFks9hfji3#DEd");
		ftp.enterLocalPassiveMode();
		ftp.changeWorkingDirectory("GPS");
		FTPFile[] files = ftp.listFiles();
		List<String> list = new ArrayList<>();
		String filePrefix = userEmail.replaceAll("[^a-zA-Z0-9@._-]", "_");
		for (FTPFile file : files) {
			String name = file.getName();
			if (name.startsWith(filePrefix) && name.endsWith("_log.txt")) {
				list.add(name);
			}
		}
		ftp.logout();
		ftp.disconnect();
		return list;
	}

	public List<CoordDto> readCoordsFromFtp(String fileName, LocalDate start, LocalDate end) throws Exception {
		FTPClient ftp = new FTPClient();
		ftp.connect("78.189.76.247");
		ftp.login("hamitadmin", "SDFks9hfji3#DEd");
		ftp.enterLocalPassiveMode();
		ftp.changeWorkingDirectory("GPS");
		InputStream inputStream = ftp.retrieveFileStream(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		List<CoordDto> list = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split(",");
			if (parts.length >= 3) {
				try {
					String raw = parts[0];
					String trimmed = raw.contains(".") ? raw.substring(0, raw.indexOf(".")) : raw;
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					LocalDateTime time = LocalDateTime.parse(trimmed, formatter);
					if (!time.toLocalDate().isBefore(start) && !time.toLocalDate().isAfter(end)) {
						double lat = Double.parseDouble(parts[1]);
						double lng = Double.parseDouble(parts[2]);
						list.add(new CoordDto(parts[0], lat, lng));
					}
				} catch (Exception e) {
					System.err.println("Satır işlenemedi: " + line);
				}
			}
		}
		ftp.completePendingCommand();
		ftp.logout();
		ftp.disconnect();
		list.sort(Comparator.comparing(CoordDto::getTimeAsLocalDateTime));
		return list;
	}
}