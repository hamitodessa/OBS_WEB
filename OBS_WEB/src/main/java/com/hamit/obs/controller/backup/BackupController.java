package com.hamit.obs.controller.backup;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

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
	public ResponseEntity<Map<String, Object>> emirliste(
	        @RequestParam String server,
	        @RequestParam String key,
	        @RequestParam String user) {
	    String modul = "emirliste";
	    String fullUrl = "https://" + server + "/loglar?key=" + URLEncoder.encode(key, StandardCharsets.UTF_8)
	            + "&emir=" + modul
	            + "&user=" + URLEncoder.encode(user, StandardCharsets.UTF_8);
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
	                .timeout(Duration.ofSeconds(20))
	                .GET()
	                .build();

	        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
	        String body = response.body();

	        Map<String, Object> out = new LinkedHashMap<>();
	        out.put("ok", response.statusCode() >= 200 && response.statusCode() < 300);
	        out.put("status", response.statusCode());
	        out.put("data", body);
	        out.put("message", null);

	        if (!(boolean) out.get("ok")) {
	            out.put("message", body);
	        }
	        return ResponseEntity.ok(out);
	    } catch (Exception e) {
	        Map<String, Object> out = new LinkedHashMap<>();
	        out.put("ok", false);
	        out.put("status", 500);
	        out.put("data", null);
	        out.put("message", e.getMessage());
	        return ResponseEntity.ok(out);
	    }
	}
	@GetMapping("/backup/logliste")
	public ResponseEntity<Map<String, Object>> logliste(
	        @RequestParam String server,
	        @RequestParam String key,
	        @RequestParam String emir,
	        @RequestParam String start,
	        @RequestParam String end,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "500") int limit,
	        @RequestParam String user) {

	    String url = String.format(
	            "https://%s/loglar?key=%s&emir=%s&start=%s&end=%s&page=%d&limit=%d&user=%s",
	            server,
	            URLEncoder.encode(key, StandardCharsets.UTF_8),
	            URLEncoder.encode(emir, StandardCharsets.UTF_8),
	            URLEncoder.encode(start, StandardCharsets.UTF_8),
	            URLEncoder.encode(end, StandardCharsets.UTF_8),
	            page,
	            limit,
	            URLEncoder.encode(user, StandardCharsets.UTF_8)
	    );
	    Map<String, Object> out = new LinkedHashMap<>();
	    out.put("ok", false);
	    out.put("status", 500);
	    out.put("message", null);
	    out.put("data", null);
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
	                .uri(URI.create(url))
	                .timeout(Duration.ofSeconds(30))
	                .GET()
	                .build();

	        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
	        int status = response.statusCode();
	        String body = response.body();
	        boolean ok = status >= 200 && status < 300;
	        out.put("ok", ok);
	        out.put("status", status);
	        if (ok) {
	            out.put("data", body);
	            out.put("message", null);
	        } else {
	            out.put("data", null);
	            out.put("message", body);
	        }
	        return ResponseEntity.ok(out);
	    } catch (Exception e) {
	        out.put("ok", false);
	        out.put("status", 500);
	        out.put("message", e.getMessage());
	        out.put("data", null);
	        return ResponseEntity.ok(out);
	    }
	}
}