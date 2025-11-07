package com.hamit.obs.reports;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hamit.obs.custom.yardimci.ResultSetConverter;
import com.hamit.obs.custom.yardimci.TextSifreleme;
import com.hamit.obs.dto.cari.dvzcevirmeDTO;
import com.hamit.obs.dto.cari.mizanDTO;
import com.hamit.obs.dto.kambiyo.bordroPrinter;
import com.hamit.obs.dto.kereste.keresteyazdirDTO;
import com.hamit.obs.dto.user.RaporEmailDegiskenler;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.mailcenter.MailCenter;
import com.hamit.obs.mailcenter.MailCfgFactory;
import com.hamit.obs.model.user.Email_Details;
import com.hamit.obs.model.user.Gonderilmis_Mailler;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.user.EmailService;
import com.hamit.obs.service.user.GidenRaporService;
import com.hamit.obs.service.user.UserService;

import jakarta.mail.util.ByteArrayDataSource;

@Component
public class RaporEmailGonderme {

	@Autowired
	private UserService userService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private GidenRaporService gidenRaporService;

	@Autowired
	private RaporOlustur raporOlustur;

	private RaporEmailDegiskenler raporEmailDegiskenler;
	
	@Autowired
	private ExcellToDataSource excellToDataSource;

	public boolean EmailGonderme(RaporEmailDegiskenler raporEmailDegiskenler) {

		this.raporEmailDegiskenler = raporEmailDegiskenler;
		boolean durum = false ;
		try {
			String nerden = raporEmailDegiskenler.getNerden(); 
			if (nerden.equals("fatrapor") || nerden.equals("irsrapor") || nerden.equals("imarapor")
					|| nerden.equals("envanter") || nerden.equals("stok")
					 || nerden.equals("stokdetay")  || nerden.equals("tahrap")|| nerden.equals("cekrap") || nerden.equals("kerdetay")
					 || nerden.equals("kerenvanter") || nerden.equals("kerortfiat"))
				gonder_excell();
			else if(nerden.equals("gruprapor") || nerden.equals("imagruprapor") || nerden.equals("kergruprapor"))
				gonder_excell_grup();
			else if(nerden.equals("kercikis"))
				gonder_excell_kercikis();
			else if(nerden.equals("kergiris"))
				gonder_excell_kergiris();
			else if(nerden.equals("duz"))
				gonder_duz();
			else
				gonder_jasper();
			durum = true ;
		} catch (Exception e) {
			throw new ServiceException( e.getMessage());
		}
		return durum ;
	}
	private void gonder_excell() {
		try {
			String raporAdi = raporEmailDegiskenler.getNerden();
			List<Map<String, String>> tableData = raporEmailDegiskenler.getExceList();
			ByteArrayDataSource ds = excellToDataSource.export_excell(tableData);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm");
			String zaman = dtf.format(LocalDateTime.now());
			String rapor_dos_adi = raporAdi + zaman + ".xlsx";
			son_gonderme(ds, rapor_dos_adi);
		} catch (Exception ex) {
			throw new ServiceException(ex.getMessage());
		}
	}
	private void gonder_excell_grup() {
		try {
			String raporAdi = raporEmailDegiskenler.getNerden();
			String[] values = raporEmailDegiskenler.getDegerler().split(",");
			List<String> header = Arrays.asList(raporEmailDegiskenler.getBaslik().split(","));  
			String tableString = raporEmailDegiskenler.getTableString();
			List<Map<String, String>> tableData = ResultSetConverter.parseTableData(tableString, header);
			ByteArrayDataSource ds = excellToDataSource.export_excell_grp(tableData,Integer.valueOf(values[0]));
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm");
			String zaman = dtf.format(LocalDateTime.now());
			String rapor_dos_adi = raporAdi + zaman + ".xlsx";
			son_gonderme(ds, rapor_dos_adi);
		} catch (Exception ex) {
			throw new ServiceException(ex.getMessage());
		}
	}
	
	private void gonder_excell_kercikis() {
		try {
			String raporAdi = raporEmailDegiskenler.getNerden();
			keresteyazdirDTO keresteyazdirDTO = raporEmailDegiskenler.getKeresteyazdirDTO();
			ByteArrayDataSource ds = excellToDataSource.export_excell_kercikis(keresteyazdirDTO);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm");
			String zaman = dtf.format(LocalDateTime.now());
			String rapor_dos_adi = raporAdi + zaman + ".xlsx";
			son_gonderme(ds, rapor_dos_adi);
		} catch (Exception ex) {
			throw new ServiceException(ex.getMessage());
		}
	}
	
	private void gonder_excell_kergiris() {
		try {
			String raporAdi = raporEmailDegiskenler.getNerden();
			keresteyazdirDTO keresteyazdirDTO = raporEmailDegiskenler.getKeresteyazdirDTO();
			ByteArrayDataSource ds = excellToDataSource.export_excell_kergiris(keresteyazdirDTO);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm");
			String zaman = dtf.format(LocalDateTime.now());
			String rapor_dos_adi = raporAdi + zaman + ".xlsx";
			son_gonderme(ds, rapor_dos_adi);
		} catch (Exception ex) {
			throw new ServiceException(ex.getMessage());
		}
	}
	
	private void gonder_duz() {
		try {
			son_gonderme(null, "");
		} catch (Exception ex) {
			throw new ServiceException(ex.getMessage());
		}
	}
	
	private void gonder_jasper() {
		try {
			String raporAdi = getRaporAdi(raporEmailDegiskenler.getNerden());
			ByteArrayDataSource ds = getDataSource(raporEmailDegiskenler.getNerden(), raporEmailDegiskenler.getDegerler(), raporEmailDegiskenler.getFormat());
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm");
			String zaman = dtf.format(LocalDateTime.now());
			String rapor_dos_adi = raporAdi + zaman + getDosyaUzantisi(raporEmailDegiskenler.getFormat());
			son_gonderme(ds, rapor_dos_adi);
		} catch (Exception ex) {
			throw new ServiceException(ex.getMessage());
		}
	}

	private String getRaporAdi(String nerden) {
		return switch (nerden) {
		case "cariekstre" -> "Cari_Ekstre_";
		case "carimizan" -> "Cari_Mizan_";
		case "cariozelmizan" -> "Cari_Ozel_Mizan_";
		case "dvzcevir" -> "Cari_Dovize_Cevirme_";
		case "cgbordro" -> "Giris_Bordro_";
		case "ccbordro" -> "Cikis_Bordro_";
		default ->
			throw new IllegalArgumentException("Unsupported report type: " + nerden);
		};
	}

	private ByteArrayDataSource getDataSource(String nerden, String degerler, String format) throws Exception {
		return switch (nerden) {
		case "cariekstre" -> cari_ekstre(degerler, format);
		case "carimizan" -> cari_mizan(degerler, format);
		case "cariozelmizan" -> cari_mizan(degerler, format);
		case "dvzcevir" -> cari_dvzcevir(degerler, format);
		case "cgbordro" -> kam_gbordro(degerler, format);
		case "ccbordro" -> kam_cbordro(degerler, format);
		default ->
			throw new IllegalArgumentException("Unsupported report type: " + nerden);
		};
	}

	private String getDosyaUzantisi(String format) {
		return switch (format) {
		case "pdf" -> ".pdf";
		case "xlsx" -> ".xlsx";
		case "WORD" -> ".docx";
		case "XML" -> ".xml";
		default ->
			throw new IllegalArgumentException("Unsupported file format: " + format);
		};
	}

	public ByteArrayDataSource cari_ekstre(String degerler,String format) throws Exception{
		String[] values = degerler.split(",");
		ByteArrayDataSource ds = raporOlustur.cari_ekstre(values[0], values[1], values[2], format);
		return ds ;
	}
	public ByteArrayDataSource cari_dvzcevir(String degerler,String format) throws Exception{
		String[] values = degerler.split(",");
		dvzcevirmeDTO dvzcevirmeDTO = new dvzcevirmeDTO();
		dvzcevirmeDTO.setHesapKodu(values[0]);
		dvzcevirmeDTO.setStartDate(values[1]);
		dvzcevirmeDTO.setEndDate(values[2]);
		dvzcevirmeDTO.setDvz_tur(values[3]);
		dvzcevirmeDTO.setDvz_cins(values[4]);
		dvzcevirmeDTO.setFormat(format);
		ByteArrayDataSource ds = raporOlustur.dvzcevirme(dvzcevirmeDTO);
		return ds ;
	}
	
	public ByteArrayDataSource cari_mizan(String degerler,String format) {
		String[] values = degerler.split(",");
		mizanDTO mizanDTO = new mizanDTO();
		mizanDTO.setHkodu1(values[0]);
		mizanDTO.setHkodu2(values[1]);
		mizanDTO.setStartDate(values[2]);
		mizanDTO.setEndDate(values[3]);
		mizanDTO.setCins1(values[4]);
		mizanDTO.setCins2(values[5]);
		mizanDTO.setKarton1(values[6]);
		mizanDTO.setKarton2(values[7]);
		mizanDTO.setHangi_tur(values[8]);
		mizanDTO.setFormat(format);
		ByteArrayDataSource ds = raporOlustur.cari_mizan(mizanDTO);
		return ds ;
	}

	public ByteArrayDataSource ozelcari_mizan(String degerler,String format) {
		String[] values = degerler.split(",");
		mizanDTO mizanDTO = new mizanDTO();
		mizanDTO.setHkodu1(values[0]);
		mizanDTO.setHkodu2(values[1]);
		mizanDTO.setStartDate(values[2]);
		mizanDTO.setEndDate(values[3]);
		mizanDTO.setCins1(values[4]);
		mizanDTO.setCins2(values[5]);
		mizanDTO.setKarton1(values[6]);
		mizanDTO.setKarton2(values[7]);
		mizanDTO.setHangi_tur(values[8]);
		mizanDTO.setFormat(format);
		ByteArrayDataSource ds = raporOlustur.cariozel_mizan(mizanDTO);
		return ds ;
	}

	public ByteArrayDataSource kam_gbordro(String degerler,String format) {
		String[] values = degerler.split(",");
		bordroPrinter bordroPrinter = new bordroPrinter();
		bordroPrinter.setGirisBordro(values[0]);
		bordroPrinter.setGirisTarihi(values[1]);
		bordroPrinter.setGirisMusteri(values[2]);
		bordroPrinter.setUnvan(values[3]);
		bordroPrinter.setDvzcins(values[4]);
		bordroPrinter.setTutar(Double.parseDouble(values[5]));
		ByteArrayDataSource ds = raporOlustur.cekbordroGiris(bordroPrinter);
		return ds ;
	}

	public ByteArrayDataSource kam_cbordro(String degerler,String format) {
		String[] values = degerler.split(",");
		bordroPrinter bordroPrinter = new bordroPrinter();
		bordroPrinter.setCikisBordro(values[0]);
		bordroPrinter.setCikisTarihi(values[1]);
		bordroPrinter.setCikisMusteri(values[2]);
		bordroPrinter.setUnvan(values[3]);
		bordroPrinter.setDvzcins(values[4]);
		bordroPrinter.setTutar(Double.parseDouble(values[5]));
		ByteArrayDataSource ds = raporOlustur.cekbordroCikis(bordroPrinter);
		return ds ;
	}

	private void son_gonderme(ByteArrayDataSource ds,String rapdosadi) 
	{
		try {
	        User user = userService.getCurrentUser();
	        Email_Details email_Details = emailService.findByEmail(user.getEmail());
	        MailCenter mc = MailCfgFactory.fromEmailDetails(
	                email_Details,
	                TextSifreleme::decrypt
	        );
	        String to  = raporEmailDegiskenler.getToo().trim();
	        String cc  = String.valueOf(raporEmailDegiskenler.getCcc()).trim();
	        String sub = raporEmailDegiskenler.getKonu().trim();
	        String body= raporEmailDegiskenler.getAciklama().trim();

	        List<jakarta.activation.DataSource> atts = new ArrayList<>();
	        if (ds != null) {
	            ds.setName(rapdosadi); 
	            atts.add(ds);
	        }

	        mc.sendWithOptionalAttachment(to, cc, sub, body, atts);

	        Gonderilmis_Mailler gm = new Gonderilmis_Mailler();
	        gm.setTarih(new Date());
	        gm.setAciklama(body);
	        gm.setAlici(to);
	        gm.setUser_email(user.getEmail());
	        gm.setGonderen(raporEmailDegiskenler.getHesap().toString().trim());
	        gm.setKonu(sub);
	        gm.setRapor(rapdosadi);
	        gm.setUser(user);
	        gidenRaporService.savegonderilmisMailler(gm);
	        if (!cc.isEmpty()) {
	            Gonderilmis_Mailler gm2 = new Gonderilmis_Mailler();
	            gm2.setTarih(new Date());
	            gm2.setAciklama(body);
	            gm2.setAlici(cc);
	            gm2.setUser_email(user.getEmail());
	            gm2.setGonderen(raporEmailDegiskenler.getHesap().toString().trim());
	            gm2.setKonu(sub);
	            gm2.setRapor(rapdosadi);
	            gm2.setUser(user);
	            gidenRaporService.savegonderilmisMailler(gm2);
	        }
	    } catch (Exception ex) {
	        throw new ServiceException(ex.getMessage());
	    }
	}
}