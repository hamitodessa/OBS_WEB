package com.hamit.obs.controller.cari;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.custom.yardimci.Formatlama;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.dto.cari.hesapplaniDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.user.UserService;


@Controller
public class CariController {

	@Autowired
	private CariService cariservice;
	
	@Autowired
	private UserService userService;

	@PostMapping("cari/hesapadi")
	@ResponseBody
	public Map<String, String> hesapadiOgren(@RequestParam String hesapkodu) {
		Map<String, String> response = new HashMap<>();
		try {
			String[] hesAdiString = cariservice.hesap_adi_oku(hesapkodu);
			if (hesAdiString == null || hesAdiString.length < 2) {
				throw new ServiceException("Hesap bilgisi bulunamadı.");
			}
			response.put("hesapAdi", hesAdiString[0]);
			response.put("hesapCinsi", hesAdiString[1]);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("hesapAdi", ""); 
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@GetMapping("cari/getBaslik")
	@ResponseBody
	public Map<String, String> getBaslik() {
		Map<String, String> response = new HashMap<>();
		try {
			response.put("baslik", cariservice.cari_firma_adi());
			response.put("errorMessage","");
		} catch (ServiceException e) {
			response.put("baslik", ""); 
			response.put("errorMessage", e.getMessage()); // Hata mesajı
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@GetMapping("cari/ornekhesapplani")
	public Map<String, String> ornhsppln() {
		Map<String, String> response = new HashMap<>();
		try {
			response.put("kayitadedi", "Dosyadaki Kayit Sayisi :  " + Formatlama.doub_0(cariservice.hesap_plani_kayit_adedi()));
			response.put("errorMessage","");
		} catch (ServiceException e) {
			response.put("kayitadedi", ""); 
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@GetMapping("cari/ornekhesapplanikayit")
	@ResponseBody
	public Map<String, String> ornhspplnkayit() {
		Map<String, String> response = new HashMap<>();
		User user = userService.getCurrentUser();
		String usrString = user.getFirstName().length() > 15 
				? user.getFirstName().substring(0, 15) 
						: user.getFirstName();
		try (InputStream stream = new ClassPathResource("static/hesapplani/Hesap_Plani_Ornek.txt").getInputStream();
				InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_16);
				BufferedReader bReader = new BufferedReader(streamReader);
				Scanner sc = new Scanner(bReader)) {
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] token = line.split("\t");
				hesapplaniDTO dto = new hesapplaniDTO();
				dto.setKodu(token[0].trim());
				dto.setAdi(token[1].trim());
				dto.setHcins(token[2].trim());
				dto.setKarton(token[3].trim());
				dto.setUsr(usrString);
				cariservice.hpln_kayit(dto);
				hesapplaniDTO detayDTO = new hesapplaniDTO();
				detayDTO.setKodu(token[0].trim());
				cariservice.hpln_detay_kayit(detayDTO);
			}
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", "Servis Hatası: " + e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@GetMapping("cari/carikoddegis")
	public String carikoddegis() {
		return "/cari/carikoddegis";
	}

	@PostMapping("cari/koddegiskaydet")
	@ResponseBody
	public Map<String, Object> sorgula(@RequestParam String eskiKod,@RequestParam String yeniKod) {
		Map<String, Object> response = new HashMap<>();
		try {
			String usrString = Global_Yardimci.user_log(userService.getCurrentUser().getEmail());
			cariservice.cari_kod_degis_hesap(eskiKod, yeniKod,usrString);
			cariservice.cari_kod_degis_satirlar(eskiKod, yeniKod,usrString);
			cariservice.cari_kod_degis_tahsilat(eskiKod, yeniKod,usrString);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

}