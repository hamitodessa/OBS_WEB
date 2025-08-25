package com.hamit.obs.controller.stok;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import com.hamit.obs.custom.yardimci.Tarih_Cevir;
import com.hamit.obs.dto.cari.dekontDTO;
import com.hamit.obs.dto.stok.faturaDTO;
import com.hamit.obs.dto.stok.faturadetayDTO;
import com.hamit.obs.dto.stok.faturakayitDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.fatura.FaturaService;
import com.hamit.obs.service.user.UserService;

@Controller
public class IrsaliyeController {

	@Autowired
	private FaturaService faturaService;

	@Autowired
	private CariService cariservice;

	@Autowired
	private UserService userService;

	@GetMapping("stok/irsaliye")
	public Model irsaliye(Model model) {
		try {
			List<Map<String, Object>> anaKodlari = faturaService.stk_kod_degisken_oku("ANA_GRUP", "AGID_Y",
					"ANA_GRUP_DEGISKEN");
			Map<String, Object> anaDeger = new HashMap<>();
			anaDeger.put("ANA_GRUP", "");
			anaKodlari.add(0, anaDeger);
			model.addAttribute("anaKodlari", (anaKodlari != null) ? anaKodlari : new ArrayList<>());

			List<Map<String, Object>> ozelKodlari = faturaService.fat_oz_kod("C");
			Map<String, Object> ozelDeger = new HashMap<>();
			ozelDeger.put("Ozel_Kod", "");
			ozelKodlari.add(0, ozelDeger);
			model.addAttribute("ozelKodlari", (ozelKodlari != null) ? ozelKodlari : new ArrayList<>());
			model.addAttribute("doviz", userService.getCurrentUser().getCalisandvzcins());
			LocalDate today = LocalDate.now();
			model.addAttribute("fisTarih", today);
			model.addAttribute("errorMessage", "");
		} catch (ServiceException e) {
			model.addAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Hata: " + e.getMessage());
		}
		return model;
	}

	@PostMapping("stok/irsOku")
	@ResponseBody
	public Map<String, Object> irsOku(@RequestParam String fisno, @RequestParam String cins) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> irsaliye = new ArrayList<>();
			if (cins.toString().equals("SATIS")) {
				irsaliye = faturaService.irsaliye_oku(fisno.trim(), "C");
				response.put("a1", faturaService.aciklama_oku("IRS", 1, fisno.trim(), "C"));
				response.put("a2", faturaService.aciklama_oku("IRS", 2, fisno.trim(), "C"));
				response.put("dipnot", faturaService.dipnot_oku(fisno.trim(), "I", "C"));
			} else {
				irsaliye = faturaService.irsaliye_oku(fisno.trim(), "G");
				response.put("a1", faturaService.aciklama_oku("IRS", 1, fisno.trim(), "G"));
				response.put("a2", faturaService.aciklama_oku("IRS", 2, fisno.trim(), "G"));
				response.put("dipnot", faturaService.dipnot_oku(fisno.trim(), "I", "G"));
			}
			irsaliye = irsaliye.stream().map(item -> {
				if (item.containsKey("Resim") && item.get("Resim") instanceof byte[]) {
					String base64Image = Base64.getEncoder().encodeToString((byte[]) item.get("Resim"));
					item.put("base64Resim", base64Image);
					item.remove("Resim");
				}
				return item;
			}).collect(Collectors.toList());
			response.put("data", (irsaliye != null) ? irsaliye : new ArrayList<>());
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/sonirsfis")
	@ResponseBody
	public Map<String, String> sonfatfis(@RequestParam String cins) {
		Map<String, String> response = new HashMap<>();
		try {
			if (cins.toString().equals("SATIS"))
				response.put("fisno", faturaService.son_irsno_al("C"));
			else
				response.put("fisno", faturaService.son_irsno_al("G"));
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/irsyenifis")
	@ResponseBody
	public Map<String, String> irsyenifis(@RequestParam String cins) {
		Map<String, String> response = new HashMap<>();
		try {
			int sno = 0;
			if (cins.toString().equals("SATIS"))
				sno = faturaService.irsaliye_no_al("C");
			else
				sno = faturaService.irsaliye_no_al("G");
			int kj = 10 - Integer.toString(sno).length();
			StringBuilder strBuilder = new StringBuilder();
			for (int i = 0; i < kj; i++)
				strBuilder.append("0");
			strBuilder.append(sno);
			String str_ = strBuilder.toString();
			response.put("fisno", str_.equals("0000000000") ? "0000000001" : str_);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/irsYoket")
	@ResponseBody
	public ResponseEntity<Map<String, String>> irsSil(@RequestParam String fisno, @RequestParam String cins) {
		Map<String, String> response = new HashMap<>();
		try {
			if (cins.toString().equals("SATIS")) {
				faturaService.irs_giris_sil(fisno.trim(), "C");
				faturaService.dipnot_sil(fisno.trim(), "I", "C");
				faturaService.aciklama_sil("IRS", fisno.trim(), "C");
			} else {
				faturaService.irs_giris_sil(fisno.trim(), "G");
				faturaService.dipnot_sil(fisno.trim(), "I", "G");
				faturaService.aciklama_sil("IRS", fisno.trim(), "G");
			}
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", e.getMessage());
		}
		return ResponseEntity.ok(response);
	}

	@PostMapping("stok/irsKayit")
	@ResponseBody
	public Map<String, Object> irsKayit(@RequestBody faturakayitDTO faturakayitDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			faturaDTO dto = faturakayitDTO.getFaturaDTO();
			List<faturadetayDTO> tableData = faturakayitDTO.getTableData();
			String tarih = Tarih_Cevir.dateFormaterSaatli(dto.getTarih());
			String mesajlog = "";
			if (dto.getFatcins().toString().equals("SATIS")) {
				faturaService.irs_giris_sil(dto.getFisno().trim(), "C");
				mesajlog = dto.getFisno().trim() + " Nolu Cikis Irsaliye Silindi";
			} else {
				mesajlog = dto.getFisno().trim() + " Nolu Giris Irsaliye Silindi";
				faturaService.irs_giris_sil(dto.getFisno().trim(), "G");
			}
			String userrString = Global_Yardimci
					.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			int dpo = 0;
			int ana = 0;
			int alt = 0;
			String gircik, izahat;
			double miktar;
			for (faturadetayDTO row : tableData) {
				dpo = 0;
				ana = 0;
				alt = 0;
				miktar = 0;
				if (!row.getDepo().equals("")) {
					String dpos = faturaService.urun_kod_degisken_ara("DPID_Y", "DEPO", "DEPO_DEGISKEN", row.getDepo());
					dpo = Integer.parseInt(dpos);
				}
				if (!dto.getAnagrup().equals("")) {
					String anas = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN",
							dto.getAnagrup());
					ana = Integer.parseInt(anas);
				}
				if (!dto.getAltgrup().equals("")) {
					String alts = faturaService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN",
							dto.getAltgrup());
					alt = Integer.parseInt(alts);
				}
				if (dto.getFatcins().toString().equals("SATIS")) {
					miktar = row.getMiktar();
					miktar = miktar * -1;
					gircik = "C";
				} else {
					miktar = row.getMiktar();
					gircik = "G";
				}
				double kur = 0;
				double tutar = row.getTutar();
				izahat = row.getIzahat();
				kur = dto.getKur();
				double tevk = dto.getTevoran();
				double fiat = 0;
				fiat = row.getFiat();
				double isk = 0;
				isk = row.getIskonto();
				double kdv = 0;
				kdv = row.getKdv();
				faturaService.irs_kaydet(dto.getFisno().trim(), row.getUkodu().trim(), dpo, fiat, tevk, miktar, gircik,
						tutar, isk, kdv, tarih, izahat, dto.getDvzcins(), dto.getAdreskod().trim(),
						dto.getCarikod().trim(), dto.getOzelkod(), kur, "", ana, alt, userrString, dto.getFatno(),
						dto.getSevktarih());
			}

			if (dto.getFatcins().toString().equals("SATIS")) {
				faturaService.dipnot_sil(dto.getFisno().trim(), "I", "C");
				faturaService.dipnot_yaz(dto.getFisno().trim(), dto.getNot1().trim(), dto.getNot2().trim(),
						dto.getNot3().trim(), "I", "C", userrString);
				faturaService.aciklama_sil("IRS", dto.getFisno().trim(), "C");
				faturaService.aciklama_yaz("IRS", 1, dto.getFisno().trim(), dto.getAcik1().trim(), "C");
				faturaService.aciklama_yaz("IRS", 2, dto.getFisno().trim(), dto.getAcik2().trim(), "C");

			} else {
				faturaService.dipnot_sil(dto.getFisno().trim(), "I", "G");
				faturaService.dipnot_yaz(dto.getFisno().trim(), dto.getNot1().trim(), dto.getNot2().trim(),
						dto.getNot3().trim(), "I", "G", userrString);
				faturaService.aciklama_sil("IRS", dto.getFisno().trim(), "G");
				faturaService.aciklama_yaz("IRS", 1, dto.getFisno().trim(), dto.getAcik1().trim(), "G");
				faturaService.aciklama_yaz("IRS", 2, dto.getFisno().trim(), dto.getAcik2().trim(), "G");
			}
			if (dto.getFatcins().toString().equals("SATIS")) {
				mesajlog = dto.getFisno().trim() + " Nolu Cikis Irsaliye Silindi";
				faturaService.stok_sil(dto.getFisno().trim(), "IRS", "C", mesajlog);
			} else {
				mesajlog = dto.getFisno().trim() + " Nolu Giris Irsaliye Silindi";
				faturaService.stok_sil(dto.getFisno().trim(), "IRS", "G", mesajlog);
			}
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/irscariKayit")
	@ResponseBody
	public ResponseEntity<?> irscariKayit(@RequestBody faturaDTO faturaDTO) {
		try {
			faturaDTO dto = faturaDTO;
			String[] hesapIsmi = { "", "" };
			hesapIsmi = cariservice.hesap_adi_oku(dto.getKarsihesapkodu());
			if (hesapIsmi[0].equals("")) {
				throw new ServiceException("Girilen Alacakli Hesap Kodunda  bir  hesaba rastlanmadi!!!!");
			}
			double sdf = dto.getMiktar();
			String aciklama = "";
			String userrString = Global_Yardimci
					.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			if (dto.getFatcins().toString().equals("SATIS")) {
				aciklama = dto.getFisno() + "'Irsaliye ile " + Formatlama.doub_0(sdf) + " Urun Satisi";
				dekontDTO dekontDTO = new dekontDTO();
				dekontDTO.setTar(Tarih_Cevir.dateFormaterSaatli(dto.getTarih()));
				dekontDTO.setFisNo(cariservice.yenifisno());
				dekontDTO.setBhes(dto.getCarikod());
				dekontDTO.setBcins("");
				dekontDTO.setBkur(1);
				dekontDTO.setBorc(dto.getTutar());
				dekontDTO.setAhes(dto.getKarsihesapkodu());
				dekontDTO.setAcins("");
				dekontDTO.setAkur(1);
				dekontDTO.setAlacak(dto.getTutar());
				dekontDTO.setIzahat(aciklama);
				dekontDTO.setKod("Satış");
				dekontDTO.setUser(userrString);
				cariservice.cari_dekont_kaydet(dekontDTO);
			} else {
				aciklama = dto.getFisno() + "'Irsaliye ile " + Formatlama.doub_0(sdf) + " Urun Girisi";
				dekontDTO dekontDTO = new dekontDTO();
				dekontDTO.setTar(Tarih_Cevir.dateFormaterSaatli(dto.getTarih()));
				dekontDTO.setFisNo(cariservice.yenifisno());
				dekontDTO.setBhes(dto.getKarsihesapkodu());
				dekontDTO.setBcins("");
				dekontDTO.setBkur(1);
				dekontDTO.setBorc(dto.getTutar());
				dekontDTO.setAhes(dto.getCarikod());
				dekontDTO.setAcins("");
				dekontDTO.setAkur(1);
				dekontDTO.setAlacak(dto.getTutar());
				dekontDTO.setIzahat(aciklama);
				dekontDTO.setKod("Alış");
				dekontDTO.setUser(userrString);
				cariservice.cari_dekont_kaydet(dekontDTO);
			}
			return ResponseEntity.ok(Map.of("errorMessage", ""));
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", "Veriler kaydedilirken hata oluştu."));
		}
	}
}