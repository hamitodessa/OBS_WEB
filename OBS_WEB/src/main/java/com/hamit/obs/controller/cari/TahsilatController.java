package com.hamit.obs.controller.cari;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.custom.yardimci.Formatlama;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.dto.cari.dekontDTO;
import com.hamit.obs.dto.cari.tahsilatDTO;
import com.hamit.obs.dto.cari.tahsilatKayitDTO;
import com.hamit.obs.dto.cari.tahsilatTableRowDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.cari.CariService;

@Controller
public class TahsilatController {

	@Autowired
	private CariService cariservice;

	@Autowired
	private RaporOlustur raporOlustur;


	@GetMapping("cari/tahsilat")
	public Model register(Model model) {
		try {
			model.addAttribute("hesapKodlari", (cariservice.hesap_kodlari() != null) ? cariservice.hesap_kodlari() : new ArrayList<>());
			model.addAttribute("nameBanks", (cariservice.banka_sube("Banka") != null) ? cariservice.banka_sube("Banka") : new ArrayList<>());
			LocalDate today = LocalDate.now(); 
			model.addAttribute("evrakTarih", today); 
			model.addAttribute("errorMessage", "");
		} catch (ServiceException e) {
			model.addAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Hata: " + e.getMessage());
		}
		return model;
	}

	@GetMapping("cari/getDegiskenler")
	@ResponseBody
	public Map<String, Object> bankaIsmi() {
		Map<String, Object> response = new HashMap<>();
		try {	 
			List<Map<String, Object>> bankaIsmi = cariservice.banka_sube("Banka");
			response.put("bankaIsmi", (bankaIsmi != null) ? bankaIsmi : new ArrayList<>());
			List<Map<String, Object>> subeIsmi = cariservice.banka_sube("Sube");
			response.put("subeIsmi", (subeIsmi != null) ? subeIsmi : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("cari/tahevrakOku")
	@ResponseBody
	public ResponseEntity<tahsilatDTO> evrakOku(@RequestParam String evrakNo,@RequestParam Integer tah_ted) {
		tahsilatDTO tahsilatDTO = new tahsilatDTO();
		try {
			tahsilatDTO = cariservice.tahfiskon(evrakNo,tah_ted);
			tahsilatDTO.setErrorMessage(""); 
			return ResponseEntity.ok(tahsilatDTO); 
		} catch (ServiceException e) {
			tahsilatDTO.setErrorMessage(e.getMessage());
		} catch (Exception e) {
			tahsilatDTO.setErrorMessage("Hata: " + e.getMessage());
		}
		return ResponseEntity.badRequest().body(tahsilatDTO); // Hata yanıtı
	}

	@PostMapping("cari/tahcekdokum")
	@ResponseBody
	public Map<String, Object> sorgula(@RequestParam String evrakNo,@RequestParam Integer tah_ted) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> cekdokum = cariservice.tah_cek_doldur(evrakNo,tah_ted);
			response.put("success", true);
			response.put("data", (cekdokum != null) ? cekdokum : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("cari/tahsonfisNo")
	@ResponseBody
	public tahsilatDTO tahsonfisNo(@RequestParam Integer tah_ted) {
		tahsilatDTO response = new tahsilatDTO();
		try {
			int evrakNo = cariservice.cari_tahsonfisno(tah_ted);
			if (evrakNo == 0) {
				throw new ServiceException("Dosyada kayıt yok");
			}
			response.setFisNo(String.valueOf(evrakNo));
			response.setErrorMessage("");
		} catch (ServiceException e) {
			response.setFisNo("0");
			response.setErrorMessage(e.getMessage());
		} catch (Exception e) {
			response.setFisNo("0");
			response.setErrorMessage("Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("cari/tahyenifisNo")
	@ResponseBody
	public tahsilatDTO cari_tah_fisno_al(@RequestParam String tah_ted) {
		tahsilatDTO response = new tahsilatDTO();
		try {
			int evrakNo = cariservice.cari_tah_fisno_al(tah_ted);
			if (evrakNo == 0) {
				throw new ServiceException("Dosyada kayıt yok");
			}
			response.setFisNo(String.valueOf(evrakNo));
			response.setErrorMessage(""); 
		} catch (ServiceException e) {
			response.setFisNo("0");
			response.setErrorMessage(e.getMessage()); 
		} catch (Exception e) {
			response.setFisNo("0");
			response.setErrorMessage("Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("cari/tahsilatKayit")
	@ResponseBody
	public ResponseEntity<?> tahsilatKayit(@RequestBody tahsilatKayitDTO tahsilatKayitDTO) {
		try {
			tahsilatDTO dto = tahsilatKayitDTO.getTahsilatDTO();
			List<tahsilatTableRowDTO> tableData = tahsilatKayitDTO.getTableData();
			if (dto.getTur() != 2) {
				dto.setPosBanka("");
			}
			cariservice.tah_kayit(dto);
			String usrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			cariservice.tah_cek_sil(dto, usrString);

			if (dto.getTur() == 1 && tableData != null && !tableData.isEmpty()) {
				for (tahsilatTableRowDTO row : tableData) {
					cariservice.tah_cek_kayit(row, dto.getFisNo(), dto.getTah_ted());
				}
			}
			return ResponseEntity.ok(Map.of("errorMessage", ""));
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", "Veriler kaydedilirken hata oluştu."));
		}
	}

	@PostMapping("cari/tahfisYoket")
	public ResponseEntity<Map<String, String>> tah_sil(@RequestParam String evrakNo,@RequestParam Integer tah_ted) {
		try {
			String usrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			cariservice.tah_sil(evrakNo,tah_ted,usrString);
			return ResponseEntity.ok(Map.of("errorMessage", ""));
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", "Beklenmeyen bir hata oluştu: " + e.getMessage()));
		}
	}
	
	@PostMapping("cari/tahsilat_download")
	public ResponseEntity<byte[]> downloadReport(@RequestBody tahsilatKayitDTO tahsilatKayitDTO) {
		ByteArrayDataSource dataSource = null ;
		try {
			tahsilatDTO dto = tahsilatKayitDTO.getTahsilatDTO();
			List<tahsilatTableRowDTO> tableData = tahsilatKayitDTO.getTableData();
			if(dto.getTur() != 1)
				dataSource =  raporOlustur.tahsilat(dto);
			else
				dataSource =  raporOlustur.tahsilat_cek(dto,tableData);
			byte[] fileContent = dataSource.getInputStream().readAllBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			String fileName = "Cari_Tahsilat.pdf";
			headers.setContentDispositionFormData("attachment", fileName);
			return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage().getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Beklenmeyen bir hata oluştu.".getBytes(StandardCharsets.UTF_8));
		} finally {
			dataSource = null;
		}	
	}
	
	@PostMapping("cari/tahsilatCariKayit")
	@ResponseBody
	public ResponseEntity<?> tahsilatCariKayit(@RequestBody tahsilatKayitDTO tahsilatKayitDTO ) {
		try {
			tahsilatDTO dto = tahsilatKayitDTO.getTahsilatDTO();
			List<tahsilatTableRowDTO> tableData = tahsilatKayitDTO.getTableData();
			if (dto.getTur() != 2) {
				dto.setPosBanka("");
			}
			String bh = "",alh = "";
			if(dto.getTah_ted() == 0)
			{
				bh = dto.getBorc_alacak();
				alh = dto.getTcheskod();
			}
			else  if(dto.getTah_ted() ==1)
			{
				alh = dto.getBorc_alacak();
				bh = dto.getTcheskod();
			}

			String usrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());

			dekontDTO dekontDTO = new dekontDTO();

			dekontDTO.setTar(dto.getTahTarih());
			dekontDTO.setFisNo(cariservice.yenifisno());

			dekontDTO.setBhes(bh);
			dekontDTO.setBcins("");
			dekontDTO.setBkur(1);
			dekontDTO.setBorc(dto.getTutar());

			dekontDTO.setAhes(alh);
			dekontDTO.setAcins("");
			dekontDTO.setAkur(1);
			dekontDTO.setAlacak(dto.getTutar());

			dekontDTO.setUser(usrString);

			if(dto.getTur() == 0)
				dekontDTO.setIzahat(dto.getFisNo() + " Nolu Tah.Fisi ile Nakit");
			else if(dto.getTur() ==1)
				dekontDTO.setIzahat(dto.getFisNo() + " Nolu Tah.Fisi ile " + Formatlama.doub_0(Integer.valueOf(tableData.size())) + " Adet Cek");
			else if(dto.getTur() ==2)
			{
				dekontDTO.setIzahat(dto.getFisNo() + " Nolu Tah.Fisi ile Kredi Karti " + "Pos:" + dto.getPosBanka() + 
						"  " + 	Formatlama.doub_0(dto.getTutar()) + " " + (dto.getTah_ted() == 0 ? "Tahsilat" :"Tediye"));
			}
			if(dto.getTah_ted()==0)
				dekontDTO.setKod("Tahs.");
			else if(dto.getTah_ted()==1)
				dekontDTO.setKod("Tedi.");
			cariservice.cari_dekont_kaydet(dekontDTO);
			return ResponseEntity.ok(Map.of("errorMessage", ""));
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", "Veriler kaydedilirken hata oluştu."));
		}
	}
}