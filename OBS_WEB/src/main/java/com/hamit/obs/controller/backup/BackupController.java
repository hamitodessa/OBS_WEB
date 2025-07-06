package com.hamit.obs.controller.backup;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.cert.X509Certificate;

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
			TrustManager[] trustAllCerts = new TrustManager[]{
					new X509TrustManager() {
						public void checkClientTrusted(X509Certificate[] certs, String authType) {}
						public void checkServerTrusted(X509Certificate[] certs, String authType) {}
						public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
					}
			};
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(10))
					.sslContext(sslContext)
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

	@GetMapping("/backup/logliste")
	public ResponseEntity<String> logliste(
			@RequestParam String server,
			@RequestParam String key,
			@RequestParam String emir,
			@RequestParam String start,
			@RequestParam String end,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam String user) {
		String url = String.format("https://%s/loglar?key=%s&emir=%s&start=%s&end=%s&page=%d&user=%s",
			    server,
			    URLEncoder.encode(key, StandardCharsets.UTF_8),
			    URLEncoder.encode(emir, StandardCharsets.UTF_8),
			    URLEncoder.encode(start, StandardCharsets.UTF_8),
			    URLEncoder.encode(end, StandardCharsets.UTF_8),
			    page, 
			    URLEncoder.encode(user, StandardCharsets.UTF_8)
			);		try {
			TrustManager[] trustAllCerts = new TrustManager[]{
					new X509TrustManager() {
						public void checkClientTrusted(X509Certificate[] certs, String authType) {}
						public void checkServerTrusted(X509Certificate[] certs, String authType) {}
						public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
					}
			};
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(10))
					.sslContext(sslContext)
					.build();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
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