package com.hamit.obs.reports;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hamit.obs.custom.yardimci.TextSifreleme;
import com.hamit.obs.dto.cari.mizanDTO;
import com.hamit.obs.dto.kambiyo.bordroPrinter;
import com.hamit.obs.dto.user.RaporEmailDegiskenler;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.Email_Details;
import com.hamit.obs.model.user.Gonderilmis_Mailler;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.user.EmailService;
import com.hamit.obs.service.user.UserService;
@Component
public class RaporEmailGonderme {

	@Autowired
	private UserService userService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private RaporOlustur raporOlustur;

	private RaporEmailDegiskenler raporEmailDegiskenler;

	public boolean EmailGonderme(RaporEmailDegiskenler raporEmailDegiskenler) {

		this.raporEmailDegiskenler = raporEmailDegiskenler;
		boolean durum = false ;
		try {
			gonder_jasper();
			durum = true ;
		} catch (Exception e) {
			throw new ServiceException("Mail Gonderme ", e);
		}
		return durum ;
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
			throw new ServiceException("Mail Gönderme Hatası", ex);
		}
	}

	private String getRaporAdi(String nerden) {
		switch (nerden) {
		case "cariekstre":
			return "Cari_Ekstre_";
		case "carimizan":
			return "Cari_Mizan_";
		case "cariozelmizan":
			return "Cari_Ozel_Mizan_";
		case "cgbordro":
			return "Giris_Bordro_";
		case "ccbordro":
			return "Cikis_Bordro_";
		default:
			throw new IllegalArgumentException("Unsupported report type: " + nerden);
		}
	}

	private ByteArrayDataSource getDataSource(String nerden, String degerler, String format) throws Exception {
		switch (nerden) {
		case "cariekstre":
			return cari_ekstre(degerler, format);
		case "carimizan":
			return cari_mizan(degerler, format);
		case "cariozelmizan":
			return cari_mizan(degerler, format);
		case "cgbordro":
			return kam_gbordro(degerler, format);
		case "ccbordro":
			return kam_cbordro(degerler, format);
		default:
			throw new IllegalArgumentException("Unsupported report type: " + nerden);
		}
	}

	private String getDosyaUzantisi(String format) {
		switch (format) {
		case "pdf":
			return ".pdf";
		case "xlsx":
			return ".xlsx";
		case "WORD":
			return ".docx";
		case "XML":
			return ".xml";
		default:
			throw new IllegalArgumentException("Unsupported file format: " + format);
		}
	}

	public ByteArrayDataSource cari_ekstre(String degerler,String format) throws Exception{
		String[] values = degerler.split(",");
		ByteArrayDataSource ds = raporOlustur.cari_ekstre(values[0], values[1], values[2], format);
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
			MimeBodyPart messagePart = null ;
			String[] to = { raporEmailDegiskenler.getToo()  };
			Properties props = System.getProperties();
			Email_Details email_Details = emailService.findByEmail(user.getEmail());
			props.put("mail.smtp.starttls.enable", email_Details.getBtsl());
			if (email_Details.getBssl())
			{
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");   
				//props.put("mail.smtp.startsls.enable", SSL);
			}
			String sifre = TextSifreleme.decrypt(email_Details.getSifre());
			props.put("mail.smtp.host",email_Details.getHost());
			props.put("mail.smtp.user", email_Details.getHesap());
			props.put("mail.smtp.password",sifre);
			props.put("mail.smtp.port", email_Details.getPort());
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.ssl.protocols", "TLSv1.2");
			Session session = Session.getDefaultInstance(props,new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(email_Details.getHesap(), sifre);
				}
			});
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email_Details.getHesap(),email_Details.getGon_isim()));
			InternetAddress[] toAddress = new InternetAddress[to.length];
			for (int i = 0; i < to.length; i++)
				toAddress[i] = new InternetAddress(to[i].toString().trim());
			for (int i = 0; i < toAddress.length; i++)
				message.setRecipient(RecipientType.TO,  toAddress[i]);
			if(! raporEmailDegiskenler.getCcc().toString().equals(""))
				message.setRecipient(RecipientType.CC,new InternetAddress( raporEmailDegiskenler.getCcc()));
			messagePart = new MimeBodyPart();
			messagePart.setText(raporEmailDegiskenler.getAciklama().toString().trim(), "UTF-8", "plain");
			MimeBodyPart attachment = new MimeBodyPart();
			Multipart multipart = new MimeMultipart();
			attachment.setDataHandler(new DataHandler(ds));
			attachment.setFileName(rapdosadi);
			multipart.addBodyPart(attachment);
			multipart.addBodyPart(messagePart);
			message.setSubject(raporEmailDegiskenler.getKonu().toString().trim(), "UTF-8");
			message.setContent(multipart);
			message.setSentDate(new Date());
			Transport.send(message);
			message= null;
			session = null;

			Gonderilmis_Mailler gonderilmis_Mailler = new Gonderilmis_Mailler();
			gonderilmis_Mailler.setTarih(new Date());
			gonderilmis_Mailler.setAciklama(raporEmailDegiskenler.getAciklama().toString().trim());
			gonderilmis_Mailler.setAlici(raporEmailDegiskenler.getToo().toString().trim());
			gonderilmis_Mailler.setUser_email(userService.getCurrentUser().getEmail());
			gonderilmis_Mailler.setGonderen(raporEmailDegiskenler.getHesap().toString().trim());
			gonderilmis_Mailler.setKonu(raporEmailDegiskenler.getKonu().toString().trim());
			gonderilmis_Mailler.setRapor(rapdosadi);
			gonderilmis_Mailler.setUser(user);
			user.getGonderilmisMailler().add(gonderilmis_Mailler);
			userService.saveUser(user);
			if(! raporEmailDegiskenler.getCcc().toString().equals(""))
			{
				Gonderilmis_Mailler ikinciGonderilmisMailler = new Gonderilmis_Mailler();
				ikinciGonderilmisMailler.setTarih(new Date());
				ikinciGonderilmisMailler.setAciklama(raporEmailDegiskenler.getAciklama().toString().trim());
				ikinciGonderilmisMailler.setAlici(raporEmailDegiskenler.getCcc().toString().trim());
				ikinciGonderilmisMailler.setUser_email(userService.getCurrentUser().getEmail());
				ikinciGonderilmisMailler.setGonderen(raporEmailDegiskenler.getHesap().toString().trim());
				ikinciGonderilmisMailler.setKonu(raporEmailDegiskenler.getKonu().toString().trim());
				ikinciGonderilmisMailler.setRapor(rapdosadi);
				ikinciGonderilmisMailler.setUser(user);
				user.getGonderilmisMailler().add(ikinciGonderilmisMailler);
				userService.saveUser(user);
			}
		} catch (Exception ex) 
		{
			throw new ServiceException("Mail Gonderme ", ex);
		}
	}
}