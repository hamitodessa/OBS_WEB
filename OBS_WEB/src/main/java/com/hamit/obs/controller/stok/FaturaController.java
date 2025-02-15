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
import com.hamit.obs.custom.yardimci.KusurYuvarla;
import com.hamit.obs.custom.yardimci.Tarih_Cevir;
import com.hamit.obs.dto.cari.dekontDTO;
import com.hamit.obs.dto.stok.faturaDTO;
import com.hamit.obs.dto.stok.faturadetayDTO;
import com.hamit.obs.dto.stok.faturakayitDTO;
import com.hamit.obs.dto.stok.urunDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.fatura.FaturaService;
import com.hamit.obs.service.user.UserService;

@Controller
public class FaturaController {

	@Autowired
	private FaturaService faturaService;
	
	@Autowired
	private CariService cariservice;
	
	@Autowired
	private UserService userService;
	
	@GetMapping("stok/fatura")
	public Model fatura(Model model) {
		try {
			List<Map<String, Object>> anaKodlari = faturaService.stk_kod_degisken_oku("ANA_GRUP", "AGID_Y", "ANA_GRUP_DEGISKEN") ;
			Map<String, Object> anaDeger = new HashMap<>();
			anaDeger.put("ANA_GRUP", ""); 
			anaKodlari.add(0, anaDeger);
			model.addAttribute("anaKodlari", (anaKodlari != null) ? anaKodlari : new ArrayList<>());

			List<Map<String, Object>> ozelKodlari = faturaService.fat_oz_kod("C") ;
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

	@PostMapping("stok/urunoku")
	@ResponseBody
	public Map<String, Object> urunoku(@RequestParam String ukodu, @RequestParam String barkod, @RequestParam String fiatlama,String gircik,String ckod) {
		Map<String, Object> response = new HashMap<>();
		try {
			urunDTO urunDTO = new urunDTO();
			if(! ukodu.equals(""))
				urunDTO =  faturaService.stk_urun("Kodu",ukodu);
			if(! barkod.equals(""))
				urunDTO =  faturaService.stk_urun("Barkod",ukodu);
			if (urunDTO.getKodu() == null || urunDTO.getKodu().isEmpty()) {
				throw new ServiceException("Bu Kodda Urun Yok");
			}
			if (urunDTO.getImage() != null) {
	            String base64Image = Base64.getEncoder().encodeToString(urunDTO.getImage());
	            urunDTO.setBase64Resim(base64Image);
	            urunDTO.setImage(null) ;
	        }
			response.put("dto",urunDTO);
			if (! fiatlama.equals(""))
			{
				if ( fiatlama.equals("fiat1"))
					response.put("fiat", urunDTO.getFiat1());
				else  if (fiatlama.equals("fiat2"))
					response.put("fiat", urunDTO.getFiat2());
				else  if (fiatlama.equals("fiat3"))
					response.put("fiat", urunDTO.getFiat3());
				else  if (fiatlama.equals("sonsatis"))
					response.put("fiat", faturaService.son_satis_fiati_oku(ukodu,ckod, gircik.equals("SATIS") ? "C" : "G"));
				}else {
				response.put("fiat",0);
			}
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/fatOku")
	@ResponseBody
	public Map<String, Object> fatOku(@RequestParam String fisno,@RequestParam String cins) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> fatura =new ArrayList<>();
			if (cins.toString().equals("SATIS") )
			{
				fatura = faturaService.fatura_oku(fisno.trim(), "C");
				response.put("a1",faturaService.aciklama_oku("FAT", 1, fisno.trim(), "C"));
				response.put("a2",faturaService.aciklama_oku("FAT", 2, fisno.trim(), "C"));
				response.put("dipnot",faturaService.dipnot_oku(fisno.trim(), "F", "C"));
			}
			else
			{
				fatura = faturaService.fatura_oku(fisno.trim(), "G");
				response.put("a1",faturaService.aciklama_oku("FAT", 1, fisno.trim(), "G"));
				response.put("a2",faturaService.aciklama_oku("FAT", 2, fisno.trim(), "G"));
				response.put("dipnot",faturaService.dipnot_oku(fisno.trim(), "F", "G"));
			}
			
			fatura = fatura.stream().map(item -> {
			    if (item.containsKey("Resim") && item.get("Resim") instanceof byte[]) {
			        String base64Image = Base64.getEncoder().encodeToString((byte[]) item.get("Resim"));
			        item.put("base64Resim", base64Image); // Base64 string olarak ekliyoruz
			        item.remove("Resim"); // Orijinal byte[] alanını kaldırıyoruz (isteğe bağlı)
			    }
			    return item;
			}).collect(Collectors.toList());
			
			response.put("data", (fatura != null) ? fatura : new ArrayList<>());
			
			
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("stok/sonfatfis")
	@ResponseBody
	public Map<String, String> sonfatfis(@RequestParam String cins) {
		Map<String, String> response = new HashMap<>();
		try {
			if (cins.toString().equals("SATIS"))
				response.put("fisno", faturaService.son_no_al("C"));
			else
				response.put("fisno", faturaService.son_no_al("G"));
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/yenifis")
	@ResponseBody
	public Map<String, String> yenifis(@RequestParam String cins) {
		Map<String, String> response = new HashMap<>();
		try {
			int sno = 0 ;
			if (cins.toString().equals("SATIS"))
				sno =  faturaService.fatura_no_al("C");
			else
				sno =  faturaService.fatura_no_al("G");
			int kj = 10 - Integer.toString(sno).length();
			StringBuilder strBuilder = new StringBuilder();
			for (int i = 0; i < kj; i++) {
			    strBuilder.append("0");
			}
			strBuilder.append(sno);
			String str_ = strBuilder.toString();
			response.put("fisno", str_.equals("0000000000") ? "0000000001":str_);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("stok/fatYoket")
	@ResponseBody
	public ResponseEntity<Map<String, String>> evrakSil(@RequestParam String fisno,@RequestParam String cins) {
		Map<String, String> response = new HashMap<>();
		try {
			if (cins.toString().equals("SATIS")) {
				faturaService.fat_giris_sil(fisno.trim(), "C");
				faturaService.dipnot_sil(fisno.trim(), "F", "C");
				faturaService.aciklama_sil("FAT",fisno.trim(), "C");
			}
			else {
				faturaService.fat_giris_sil(fisno.trim(), "G");
				faturaService.dipnot_sil(fisno.trim(), "F", "G");
				faturaService.aciklama_sil("FAT", fisno.trim(), "G");
			}
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage",  e.getMessage());
		}
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("stok/fatKayit")
	@ResponseBody
	public  Map<String, Object>  fatKayit(@RequestBody faturakayitDTO faturakayitDTO ) {
		Map<String, Object> response = new HashMap<>();
		try {
			faturaDTO dto = faturakayitDTO.getFaturaDTO();
			List<faturadetayDTO> tableData = faturakayitDTO.getTableData();
			String tarih = Tarih_Cevir.dateFormaterSaatli(dto.getTarih());
			String mesajlog = "";
			if (dto.getFatcins().toString().equals("SATIS")) {
				faturaService.fat_giris_sil(dto.getFisno().trim(), "C");
				mesajlog = dto.getFisno().trim() + " Nolu Cikis Fatura Silindi" ;
			}
			else {
				mesajlog = dto.getFisno().trim() + " Nolu Giris Fatura Silindi" ;
				faturaService.fat_giris_sil(dto.getFisno().trim(), "G");
			}
			String userrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			int dpo = 0 ;
			int ana = 0 ;
			int alt = 0;
			String gircik, izahat ;
			double  miktar ;
			for (faturadetayDTO row : tableData) {
				dpo = 0 ;
				ana = 0 ;
				alt = 0;
				miktar = 0;
				if( ! row.getDepo().equals("")) {
					String dpos = faturaService.urun_kod_degisken_ara("DPID_Y", "DEPO", "DEPO_DEGISKEN",  row.getDepo());
					dpo = Integer.parseInt(dpos);
				}
				if(! dto.getAnagrup().equals("")) {
					String anas = faturaService.urun_kod_degisken_ara("AGID_Y", "ANA_GRUP", "ANA_GRUP_DEGISKEN", dto.getAnagrup());
					ana = Integer.parseInt(anas);
				}
				if(! dto.getAltgrup().equals("")) {
					String alts = faturaService.urun_kod_degisken_ara("ALID_Y", "ALT_GRUP", "ALT_GRUP_DEGISKEN", dto.getAltgrup());
					alt = Integer.parseInt(alts);
				}
				if (dto.getFatcins().toString().equals("SATIS") )
				{
					miktar = row.getMiktar();
					miktar = miktar * -1;
					gircik = "C" ;
				}
				else
				{
					miktar = row.getMiktar();
					gircik = "G";
				}
				
				double kur =0;
				double tutar = row.getTutar();
				izahat =  row.getIzahat();
				kur = dto.getKur();
				double tevk = dto.getTevoran()  ;
				double fiat =0 ;
				fiat = row.getFiat();
				double isk = 0 ;
				isk = row.getIskonto();
				double kdv = 0 ; 
				kdv = row.getKdv();
				faturaService.fat_kaydet(dto.getFisno().trim(), row.getUkodu().trim(), dpo,fiat , tevk,
						miktar, gircik, tutar ,isk,kdv,
						tarih, izahat, dto.getDvzcins(),dto.getAdreskod().trim(), dto.getCarikod().trim(), 
						dto.getOzelkod(), kur, "", ana, alt, userrString);
			}
			
			if (dto.getFatcins().toString().equals("SATIS")) {
				faturaService.dipnot_sil(dto.getFisno().trim(), "F", "C");
				faturaService.dipnot_yaz(dto.getFisno().trim(), 
						dto.getNot1().trim(),dto.getNot2().trim(),dto.getNot3().trim(), "F", "C",userrString);
			
				faturaService.aciklama_sil("FAT", dto.getFisno().trim(), "C");
				faturaService.aciklama_yaz("FAT", 1,  dto.getFisno().trim(),  
						dto.getAcik1().trim(), "C");
				faturaService.aciklama_yaz("FAT", 2, dto.getFisno().trim(), 
						dto.getAcik2().trim(), "C");
				
			}
			else {
				faturaService.dipnot_sil(dto.getFisno().trim(), "F", "G");
				faturaService.dipnot_yaz(dto.getFisno().trim(), 
						dto.getNot1().trim(),dto.getNot2().trim(),dto.getNot3().trim(), "F", "G",userrString);

				faturaService.aciklama_sil("FAT", dto.getFisno().trim(), "G");
				faturaService.aciklama_yaz("FAT", 1,  dto.getFisno().trim(),  
						dto.getAcik1().trim(), "G");
				faturaService.aciklama_yaz("FAT", 2, dto.getFisno().trim(), 
						dto.getAcik2().trim(), "G");

			}
			if (dto.getFatcins().toString().equals("SATIS")) {
				mesajlog = dto.getFisno().trim() + " Nolu Cikis Fatura Silindi" ;
				faturaService.stok_sil( dto.getFisno().trim(), "FAT", "C",mesajlog);
			}
			else {
				mesajlog = dto.getFisno().trim() + " Nolu Giris Fatura Silindi" ;
				faturaService.stok_sil( dto.getFisno().trim(), "FAT", "G",mesajlog);
			}
			double tutar,kdvlitut ;
			String  har, izah ;
			for (faturadetayDTO row : tableData) {
				har = "";
				izah = "";
				miktar = 0 ;
				tutar = 0;
				kdvlitut = 0 ;
				if (dto.getFatcins().toString().equals("SATIS")) {
					miktar = row.getMiktar();
					miktar = miktar * -1;
					tutar =  row.getTutar();
					tutar = tutar - ((tutar * row.getIskonto()) / 100);
					tutar =  tutar * -1;
					kdvlitut = sat_toplam(row.getTutar(), row.getIskonto(),row.getKdv(),dto.getTevoran() );
					kdvlitut =  kdvlitut * -1;
					har = "C";
					izah = row.getIzahat() + " Nolu Satis Faturasi...";
				}
				else {
					miktar =  row.getMiktar();
					tutar =  row.getTutar();
					tutar = tutar - ((tutar * row.getIskonto()) / 100);
					kdvlitut = sat_toplam(row.getTutar(), row.getIskonto(),row.getKdv(),dto.getTevoran() );           
					har = "G" ;
					izah = row.getIzahat() + " Nolu Giris Faturasi...";
				}
				mesajlog = "Fatura Stok Kayit  H:"+ har + "   Kod:" + row.getUkodu().trim() + " Miktar:" + miktar + " Fiat:" + row.getFiat() ;

				faturaService.stk_kaydet(dto.getFisno().trim(), "FAT", tarih, dpo, row.getUkodu().trim(), miktar, row.getFiat()
						,KusurYuvarla.round(tutar,2),KusurYuvarla.round(kdvlitut,2) , har, izah, ana, alt, dto.getKur(), "",dto.getDvzcins(), dto.getCarikod().trim(),userrString,mesajlog);	
			}
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	private static double sat_toplam(double tutar,double isk ,double kdv ,double tev )
	{
		double double_0, double_1, double_2;
		double_1 = (tutar * isk) / 100  ; //' iskonto
		double_2 = ((tutar - (tutar * isk) / 100) * kdv) / 100 ; //' kdv
		//'**********Tevkif Islemi **********************************************************
		double_0 = ( tutar -  double_1) + ( double_2 - ( double_2 / 10) *  tev);
		return double_0;
	}
	
	@PostMapping("stok/fatcariKayit")
	@ResponseBody
	public ResponseEntity<?> fatcariKayit(@RequestBody faturaDTO faturaDTO ) {
		try {
			faturaDTO dto = faturaDTO;
			String[] hesapIsmi = {"",""};
			hesapIsmi = cariservice.hesap_adi_oku(dto.getKarsihesapkodu());
			if (hesapIsmi[0].equals("") ) {  
				 throw new ServiceException("Girilen Alacakli Hesap Kodunda  bir  hesaba rastlanmadi!!!!");
			} 
			
			double sdf =  dto.getMiktar();
			String aciklama = "" ;
			String userrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			if (dto.getFatcins().toString().equals("SATIS")) {
				aciklama = dto.getFisno() + "'Fatura ile " + Formatlama.doub_0(sdf) +  " Urun Satisi" ;
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
			}
			else {
				aciklama = dto.getFisno() + "'Fatura ile " + Formatlama.doub_0(sdf) +  " Urun Girisi" ;
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
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("errorMessage", "Veriler kaydedilirken hata oluştu."));
		}
	}
}