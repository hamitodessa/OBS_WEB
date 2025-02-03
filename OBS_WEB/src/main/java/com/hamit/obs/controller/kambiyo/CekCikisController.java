package com.hamit.obs.controller.kambiyo;

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

import com.hamit.obs.custom.yardimci.Global_Yardimci;
import com.hamit.obs.custom.yardimci.Tarih_Cevir;
import com.hamit.obs.dto.cari.dekontDTO;
import com.hamit.obs.dto.kambiyo.bordroPrinter;
import com.hamit.obs.dto.kambiyo.bordrodetayDTO;
import com.hamit.obs.dto.kambiyo.bordrokayitDTO;
import com.hamit.obs.dto.kambiyo.girisbordroDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.reports.RaporOlustur;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.kambiyo.KambiyoService;
import com.hamit.obs.service.user.UserService;

@Controller
public class CekCikisController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private CariService cariservice;
	
	@Autowired 
	KambiyoService kambiyoService;
	
	@Autowired
	private RaporOlustur raporOlustur;
	
	@GetMapping("kambiyo/cekcikis")
	public Model register(Model model) {
		try {
			model.addAttribute("hesapKodlari", (cariservice.hesap_kodlari() != null) ? cariservice.hesap_kodlari() : new ArrayList<>());
			model.addAttribute("ozelKodlar", (kambiyoService.ozel_kodlar("Cikis_Ozel_Kod") != null) ? kambiyoService.ozel_kodlar("Cikis_Ozel_Kod") : new ArrayList<>());
			LocalDate today = LocalDate.now(); 
			model.addAttribute("evrakTarih", today); 
			model.addAttribute("doviz", userService.getCurrentUser().getCalisandvzcins()); 
			model.addAttribute("errorMessage", "");
		} catch (ServiceException e) {
			model.addAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Hata: " + e.getMessage());
		}
		return model;
	}

	@GetMapping("kambiyo/kamgetCekListe")
	@ResponseBody
	public Map<String, Object> cekliste() {
	    Map<String, Object> response = new HashMap<>();
	    try {
	        List<Map<String, Object>> cekListe = kambiyoService.kalan_cek_liste();
	        response.put("cekListe", (cekListe != null) ? cekListe : new ArrayList<>());
	        response.put("errorMessage", "");
	    } catch (ServiceException e) {
	        response.put("errorMessage", e.getMessage());
	    } catch (Exception e) {
	        response.put("errorMessage", "Hata: " + e.getMessage());
	    }
	    return response;
	}	

	@GetMapping("kambiyo/csonbordroNo")
	@ResponseBody
	public girisbordroDTO kam_son_bordro_no_al() {
		girisbordroDTO response = new girisbordroDTO();
		try {
			int evrakNo = kambiyoService.kam_son_bordro_no_al("CEK","Cikis_Bordro");
			int kj = 10 - Integer.toString(evrakNo).length();
			String str_ = "0".repeat(kj) + evrakNo;
			response.setBordroNo(str_);
			response.setErrorMessage("");
		} catch (ServiceException e) {
			response.setBordroNo("0");
			response.setErrorMessage(e.getMessage());
		} catch (Exception e) {
			response.setBordroNo("0");
			response.setErrorMessage("Hata: " + e.getMessage()); 
		}
		return response;
	}

	@PostMapping("kambiyo/cbordroOku")
	@ResponseBody
	public Map<String, Object> bordroOku(@RequestParam String bordroNo) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> bordroOku = kambiyoService.bordroOku(bordroNo,"CEK","Cikis_Bordro");
			response.put("data", (bordroOku != null) ? bordroOku : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kambiyo/ckamgetAciklama")
	@ResponseBody
	public Map<String, Object> kam_aciklama_oku(@RequestParam String bordroNo) {
		Map<String, Object> response = new HashMap<>();
		try {
			String  aciklama1 = kambiyoService.kam_aciklama_oku("CEK",1,bordroNo,"C");
			String  aciklama2 = kambiyoService.kam_aciklama_oku("CEK",2,bordroNo,"C");
			response.put("aciklama1", (aciklama1 != null) ? aciklama1 : "" );
			response.put("aciklama2", (aciklama2 != null) ? aciklama2 : "" );
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}	
	
	@PostMapping("kambiyo/cbordroKayit")
	@ResponseBody
	public ResponseEntity<?> cbordroKayit(@RequestBody bordrokayitDTO bordrokayitDTO ) {
		try {
			girisbordroDTO dto = bordrokayitDTO.getGirisbordroDTO();
			List<bordrodetayDTO> tableData = bordrokayitDTO.getTableData();
			String userrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			kambiyoService.bordro_cikis_sil(dto.getBordroNo(), "CEK",userrString);
			for (bordrodetayDTO row : tableData) {
				kambiyoService.bordro_cikis_yaz("CEK","Cek_No",
						row.getCekNo(),
						row.getCikisMusteri(),
						row.getCikisBordro(),
						row.getCikisTarihi(),
						row.getCikisOzelKod());
			}
			kambiyoService.kam_aciklama_sil("CEK", dto.getBordroNo(), "C");
			kambiyoService.kam_aciklama_yaz("CEK", 1, dto.getBordroNo(), dto.getAciklama1(), "C");
			kambiyoService.kam_aciklama_yaz("CEK", 2, dto.getBordroNo(), dto.getAciklama2(), "C");
			return ResponseEntity.ok(Map.of("errorMessage", ""));
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", "Veriler kaydedilirken hata oluştu."));
		}
	}

	@PostMapping("kambiyo/cbordroYoket")
	public ResponseEntity<Map<String, String>> bordroYoket(@RequestParam String bordroNo) {
		try {
			String userrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			kambiyoService.bordro_cikis_sil(bordroNo, "CEK",userrString);
			kambiyoService.kam_aciklama_sil("CEK", bordroNo, "C");
			return ResponseEntity.ok(Map.of("errorMessage", ""));
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", "Beklenmeyen bir hata oluştu: " + e.getMessage()));
		}
	}
	
	@GetMapping("kambiyo/cyeniBordro")
	@ResponseBody
	public Map<String, Object> kam_bordro_no_al() {
		Map<String, Object> response = new HashMap<>();
		try {
			int evrakNo = kambiyoService.kam_bordro_no_al("CEK_C");
			int kj = 10 - Integer.toString(evrakNo).length();
			String str_ = "0".repeat(kj) + evrakNo;
			response.put("bordroNo", str_);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("kambiyo/cbordro_download")
	public ResponseEntity<byte[]> downloadReport(@RequestBody bordroPrinter bordroPrinter ) {
		ByteArrayDataSource dataSource = null ;
		try {
			dataSource =  raporOlustur.cekbordroCikis(bordroPrinter);
			byte[] fileContent = dataSource.getInputStream().readAllBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			String fileName = "Cek_Bordro.pdf";
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
	
	@PostMapping("kambiyo/ccekkontrol")
	@ResponseBody
	public ResponseEntity<bordrodetayDTO> ccekkontrol(@RequestParam String cekNo) {
		bordrodetayDTO bordrodetayDTO = new bordrodetayDTO();
		try {
			bordrodetayDTO = kambiyoService.cek_dokum(cekNo);
			bordrodetayDTO.setErrorMessage(""); 
			return ResponseEntity.ok(bordrodetayDTO); 
		} catch (ServiceException e) {
			bordrodetayDTO.setErrorMessage(e.getMessage());
		} catch (Exception e) {
			bordrodetayDTO.setErrorMessage("Hata: " + e.getMessage());
		}
		return ResponseEntity.badRequest().body(bordrodetayDTO); // Hata yanıtı	}
	}
	
	@PostMapping("kambiyo/cbordroCariKayit")
	@ResponseBody
	public ResponseEntity<?> cbordroCariKayit(@RequestBody bordrokayitDTO bordrokayitDTO ) {
		try {
			girisbordroDTO dto = bordrokayitDTO.getGirisbordroDTO();
			List<bordrodetayDTO> tableData = bordrokayitDTO.getTableData();
			String userrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			for (bordrodetayDTO row : tableData) {
				String aciklama = row.getCikisBordro() +  "'Bordro ile " + row.getCekNo() + " Nolu " + row.getVade() + " Vadeli Çek" ;
				dekontDTO dekontDTO = new dekontDTO();
				
				dekontDTO.setTar(Tarih_Cevir.dateFormaterSaatli(row.getCikisTarihi()));
				dekontDTO.setFisNo(cariservice.yenifisno());
				
				dekontDTO.setBhes(row.getCikisMusteri());
				dekontDTO.setBcins("");
				dekontDTO.setBkur(1);
				dekontDTO.setBorc(row.getTutar());
				
				dekontDTO.setAhes(dto.getHesapKodu());
				dekontDTO.setAcins("");
				dekontDTO.setAkur(1);
				dekontDTO.setAlacak(row.getTutar());
				
				dekontDTO.setIzahat(aciklama);
				dekontDTO.setKod("");
				dekontDTO.setUser(userrString);
				cariservice.cari_dekont_kaydet(dekontDTO);
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
}