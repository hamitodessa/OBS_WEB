package com.hamit.obs.controller.backup;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BackupController {

	@GetMapping("/backup/backuptakip")
	public String backuptakip() {
		return "backup/backuptakip";
	}
	
	@GetMapping("/backup/lograpor")
	public String lograpor() {
		return "backup/lograpor";
	}
	
	@GetMapping("/backup/emirliste")
	public ResponseEntity<String> emirliste(
	        @RequestParam String server,
	        @RequestParam String key,
	        @RequestParam String user) {
	    String modul = "emirliste";
	    String fullUrl = "https://" + server + "/loglar?key=" + key + "&emir=" + modul + "&user=" + user;
	    try {
	        HttpClient client = HttpClient.newBuilder()
	                .connectTimeout(Duration.ofSeconds(10))
	                .build();
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create(fullUrl))
	                .GET()
	                .build();
	        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
	        return ResponseEntity
	                .status(response.statusCode())
	                .body(response.body());
	    } catch (Exception e) {
	        return ResponseEntity
	                .status(500)
	                .body("{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
	    }
	}
}