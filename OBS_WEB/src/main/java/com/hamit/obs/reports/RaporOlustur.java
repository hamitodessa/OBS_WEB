package com.hamit.obs.reports;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.hamit.obs.custom.degiskenler.UygulamaSabitleri;
import com.hamit.obs.custom.yardimci.Formatlama;
import com.hamit.obs.custom.yardimci.Tarih_Cevir;
import com.hamit.obs.custom.yardimci.sayiyiYaziyaCevir;
import com.hamit.obs.dto.adres.ETIKET_ISIM;
import com.hamit.obs.dto.cari.dvzcevirmeDTO;
import com.hamit.obs.dto.cari.mizanDTO;
import com.hamit.obs.dto.cari.tahsilatDTO;
import com.hamit.obs.dto.cari.tahsilatTableRowDTO;
import com.hamit.obs.dto.kambiyo.bordroPrinter;
import com.hamit.obs.dto.kereste.keresteyazdirDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.model.user.Etiket_Ayarlari;
import com.hamit.obs.model.user.User;
import com.hamit.obs.service.adres.AdresService;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.kambiyo.KambiyoService;
import com.hamit.obs.service.user.EtiketAyarService;
import com.hamit.obs.service.user.UserService;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.PrintOrderEnum;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@Component
public class RaporOlustur {

	@Autowired
	private CariService cariService;

	@Autowired
	private AdresService adresService;

	@Autowired 
	KambiyoService kambiyoService;

	@Autowired
	private UserService userService;

	@Autowired
	private EtiketAyarService etiketAyarService ;
	
	@Autowired
	private ExcellToDataSource excellToDataSource;

	public ByteArrayDataSource cari_mizan(mizanDTO mizanDTO) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("Firma", cariService.cari_firma_adi());
			parameters.put("Periyot", "Periyot :" + Tarih_Cevir.tarihTers(mizanDTO.getStartDate()) + " - " + Tarih_Cevir.tarihTers(mizanDTO.getEndDate()));
			List<Map<String, Object>> mizanData = cariService.mizan(mizanDTO);
			JasperPrint jp = prepareJasperPrint("MIZAN.jrxml", parameters, mizanData,UygulamaSabitleri.CariRaporYeri);
			return exportRapor(jp, mizanDTO.getFormat());
		} catch (Exception e) {
			throw new ServiceException( e.getMessage());
		}
	}

	public ByteArrayDataSource cariozel_mizan(mizanDTO mizanDTO) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("Firma", cariService.cari_firma_adi());
			parameters.put("Periyot", "Periyot :" + Tarih_Cevir.tarihTers(mizanDTO.getStartDate()) + " - " + Tarih_Cevir.tarihTers(mizanDTO.getEndDate()));
			List<Map<String, Object>> mizanData = cariService.ozel_mizan(mizanDTO);
			JasperPrint jp = prepareJasperPrint("MIZAN_OZEL.jrxml", parameters, mizanData,UygulamaSabitleri.CariRaporYeri);
			return exportRapor(jp, mizanDTO.getFormat());
		} catch (Exception e) {
			throw new ServiceException("Cari Ozel Mizan Olusturma", e);
		}
	}

	public ByteArrayDataSource cari_ekstre(String hesap, String t1, String t2, String uzanti) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("kOD", hesap);
			parameters.put("uNVAN", cariService.hesap_adi_oku(hesap)[0]);
			parameters.put("pERIYOT", "Periyot :" + Tarih_Cevir.tarihTers(t1) + " - " + Tarih_Cevir.tarihTers(t2));
			Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
			List<Map<String, Object>> ekstreData = cariService.ekstre(hesap, t1, t2,pageable);
			JasperPrint jp = prepareJasperPrint("CAR_EKSTRE.jrxml", parameters, ekstreData,UygulamaSabitleri.CariRaporYeri);
			return exportRapor(jp, uzanti);
		} catch (Exception e) {
			throw new ServiceException("Cari Rapor Olusturma", e);
		}
	}

	public ByteArrayDataSource dvzcevirme(dvzcevirmeDTO dvzcevirmeDTO) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("Kod", dvzcevirmeDTO.getHesapKodu() + " / " + cariService.hesap_adi_oku(dvzcevirmeDTO.getHesapKodu())[0]);
			parameters.put("Kur", dvzcevirmeDTO.getDvz_tur() + " / " + dvzcevirmeDTO.getDvz_cins());
			dvzcevirmeDTO.setPage(0);
			dvzcevirmeDTO.setPageSize(Integer.MAX_VALUE);
			List<Map<String, Object>> dvzcev = cariService.dvzcevirme(dvzcevirmeDTO);
			JasperPrint jp = prepareJasperPrint("DVZ_CEVIRME.jrxml", parameters, dvzcev,UygulamaSabitleri.CariRaporYeri);
			return exportRapor(jp, dvzcevirmeDTO.getFormat());
		} catch (Exception e) {
			throw new ServiceException("Cari Rapor Olusturma", e);
		}
	}

	public ByteArrayDataSource tahsilat(tahsilatDTO tahsilatDTO) {
		try {
			List<Map<String, Object>> tah_ayar_bilgi = cariService.tah_ayar_oku();
			String[] adrbilgi =  adresService.adr_etiket_arama_kod(tahsilatDTO.getAdresheskod());
			ClassPathResource resource = new ClassPathResource(UygulamaSabitleri.CariRaporYeri + "/TAHSIL.jrxml");
			InputStream jrxmlInput = resource.getInputStream();
			JasperDesign jasper = JRXmlLoader.load(jrxmlInput);
			JasperReport jr = JasperCompileManager.compileReport(jasper);
			Map<String, Object> parameters = new HashMap<>();
			String cinString = "" ;
			if(tahsilatDTO.getTah_ted() == 0)
				cinString = "TAHSİLAT";
			else
				cinString = "TEDİYE";
			if(tahsilatDTO.getTur() == 0)
				parameters.put( "Cins","NAKİT " + cinString + " MAKBUZU");
			else if(tahsilatDTO.getTur() == 2)
				parameters.put( "Cins","KREDİ KARTI " + cinString + " MAKBUZU");
			parameters.put("Tarih",Tarih_Cevir.tarihTersSaatliden(tahsilatDTO.getTahTarih()));
			if(! tahsilatDTO.isFisnoyazdir())
			{
				for(JRElement element :jr.getPageHeader().getElements())
					if(element.getKey() != null)
					{
						if(element.getKey().toString().equals("evrlbl") || element.getKey().toString().equals("evrtext"))
							element.setWidth(0);
					}
			}
			else
				parameters.put("Evrak", tahsilatDTO.getFisNo());
			parameters.put("Unvan", adrbilgi[0]);
			parameters.put("Adr1", adrbilgi[1]);
			parameters.put("Adr2", adrbilgi[2]);
			parameters.put("Semt", adrbilgi[4]);
			double aqw = tahsilatDTO.getTutar();
			if(tahsilatDTO.getTur() == 0)
				parameters.put("Aciklama","Nakit yapılan " + Formatlama.doub_2(aqw) + " " + tahsilatDTO.getDvz_cins() + " Tutarındaki tahsilat  ");
			else if(tahsilatDTO.getTur() == 2)
				parameters.put("Aciklama","Kredi Kartınızdan yapılan " + Formatlama.doub_2(aqw) + " " + tahsilatDTO.getDvz_cins() + " Tutarındaki tahsilat  ");
			if(tahsilatDTO.getTah_ted() == 0)
				parameters.put("Borc_Alacak","Cari Hesabınıza Mahsuben ALACAK olarak Kaydedilmiştir.");
			else
				parameters.put("Borc_Alacak","Cari Hesabınıza Mahsuben BORÇ olarak Kaydedilmiştir.");
			String qwe = String.format("%.2f", aqw);
			String cnt  = "" ;
			if (tahsilatDTO.getDvz_cins().equals("TL"))
				cnt = "KURUŞ" ;
			else
				cnt = "Cent" ;
			String yaziylat = sayiyiYaziyaCevir.yaziyaCevir(qwe, 2, tahsilatDTO.getDvz_cins(), cnt , "#", null, null, null,"");
			parameters.put("yaziile", yaziylat);
			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(tah_ayar_bilgi);
			JasperPrint jp = JasperFillManager.fillReport(jr, parameters, dataSource);
			return exportRapor(jp, "pdf");
		} catch (Exception e) {
			throw new ServiceException("Tahsilat Rapor Olusturma", e);
		}
	}

	public ByteArrayDataSource tahsilat_cek(tahsilatDTO tahsilatDTO,List<tahsilatTableRowDTO> tableData) {
		try {
			List<Map<String, Object>> resultList = cariService.tah_cek_kayit_aktar(tahsilatDTO.getFisNo(),tahsilatDTO.getTah_ted());
			String[] adrbilgi =  adresService.adr_etiket_arama_kod(tahsilatDTO.getAdresheskod());
			ClassPathResource resource = new ClassPathResource(UygulamaSabitleri.CariRaporYeri + "/CEK_TAHSIL.jrxml");
			InputStream jrxmlInput = resource.getInputStream();
			JasperDesign jasper = JRXmlLoader.load(jrxmlInput);
			JasperReport jr = JasperCompileManager.compileReport(jasper);
			Map<String, Object> parameters = new HashMap<>();
			String cinString = "" ;
			if(tahsilatDTO.getTah_ted() == 0)
				cinString = "TAHSİLAT";
			else
				cinString = "TEDİYE";
			if(tahsilatDTO.getTur() == 1)
				parameters.put("Cins", "ÇEK " + cinString + " MAKBUZU");
			else if(tahsilatDTO.getTur() == 2)
				parameters.put( "Cins","KREDİ KARTI " + cinString + " MAKBUZU");
			parameters.put("Tarih",Tarih_Cevir.tarihTersSaatliden(tahsilatDTO.getTahTarih()));
			if(! tahsilatDTO.isFisnoyazdir())
			{
				for(JRElement element :jr.getPageHeader().getElements())
					if(element.getKey() != null)
					{
						if(element.getKey().toString().equals("evrlbl") || element.getKey().toString().equals("evrtext"))
							element.setWidth(0);
					}
			}
			else
				parameters.put("Evrak", tahsilatDTO.getFisNo());
			parameters.put("Unvan", adrbilgi[0]);
			parameters.put("Adr1",  adrbilgi[1]);
			parameters.put("Adr2",  adrbilgi[2]);
			parameters.put("Semt", adrbilgi[4]);
			double aqw = tahsilatDTO.getTutar();
			parameters.put("Aciklama", "Aşağıda Dökümü Yapılan  " + Formatlama.doub_2(aqw) + " " + tahsilatDTO.getDvz_cins() + 
					" Tutarındaki " + Formatlama.doub_0(resultList.size()) + " Adet Çek ");
			if(tahsilatDTO.getTah_ted() == 0)
				parameters.put("Borc_Alacak","Cari Hesabınıza Mahsuben ALACAK olarak Kaydedilmiştir.");
			else
				parameters.put("Borc_Alacak","Cari Hesabınıza Mahsuben BORÇ olarak Kaydedilmiştir.");
			String qwe = String.format("%.2f", aqw);
			String cnt  = "" ;
			if (tahsilatDTO.getDvz_cins().equals("TL"))
				cnt = "KURUŞ" ;
			else
				cnt = "Cent" ;
			String yaziylat = sayiyiYaziyaCevir.yaziyaCevir(qwe, 2, tahsilatDTO.getDvz_cins(), cnt , "#", null, null, null,"");
			parameters.put("yaziile", yaziylat);
			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(resultList);
			JasperPrint jp = JasperFillManager.fillReport(jr, parameters, dataSource);
			return exportRapor(jp, "pdf");
		} catch (Exception e) {
			throw new ServiceException("Tahsilat Rapor Olusturma", e);
		}
	}

	public ByteArrayDataSource cekbordroGiris(bordroPrinter bordroPrinter) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("Kodu",bordroPrinter.getGirisMusteri() );
			parameters.put("Unvan", bordroPrinter.getUnvan());
			parameters.put("Bordro_No", bordroPrinter.getGirisBordro());
			parameters.put("Durum", "GİRİŞ");
			parameters.put("BORC_ALACAK", "ALACAK");
			parameters.put("Tarih",Tarih_Cevir.tarihTers(bordroPrinter.getGirisTarihi()));
			List<Map<String, Object>> bordrooku = kambiyoService.bordroOku(bordroPrinter.getGirisBordro(),"CEK","Giris_Bordro");
			String adt = Integer.toString(bordrooku.size());
			String adetle =sayiyiYaziyaCevir.yaziyaCevir(adt, 2, "", "" , "#", null, null, null,"SAYIILE");
			parameters.put("Adet",bordrooku.size() + "  (" + adetle + ")");
			String qwe = Double.toString(bordroPrinter.getTutar());
			String cnt  = "" ;
			if (bordroPrinter.getDvzcins().equals("TL"))
				cnt = "KURUŞ" ;
			else
				cnt = "Cent" ;
			String yaziyla= sayiyiYaziyaCevir.yaziyaCevir(qwe, 2, bordroPrinter.getDvzcins(), cnt , "#", null, null, null,"");
			parameters.put("Yazi",yaziyla);
			JasperPrint jp = prepareJasperPrint("BORDRO.jrxml", parameters, bordrooku,UygulamaSabitleri.KambiyoRaporYeri);
			return exportRapor(jp, "pdf");
		} catch (Exception e) {
			throw new ServiceException("Bordro Rapor Olusturma", e);
		}
	}

	public ByteArrayDataSource cekbordroCikis(bordroPrinter bordroPrinter) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("Kodu",bordroPrinter.getCikisMusteri() );
			parameters.put("Unvan", bordroPrinter.getUnvan());
			parameters.put("Bordro_No", bordroPrinter.getCikisBordro());
			parameters.put("Durum", "ÇIKIŞ");
			parameters.put("BORC_ALACAK", "BORÇ");
			parameters.put("Tarih",Tarih_Cevir.tarihTers(bordroPrinter.getCikisTarihi()));
			List<Map<String, Object>> bordrooku = kambiyoService.bordroOku(bordroPrinter.getCikisBordro(),"CEK","Cikis_Bordro");
			String adt = Integer.toString(bordrooku.size());
			String adetle =sayiyiYaziyaCevir.yaziyaCevir(adt, 2, "", "" , "#", null, null, null,"SAYIILE");
			parameters.put("Adet",bordrooku.size() + "  (" + adetle + ")");
			String qwe = Double.toString(bordroPrinter.getTutar());
			String cnt  = "" ;
			if (bordroPrinter.getDvzcins().equals("TL"))
				cnt = "KURUŞ" ;
			else
				cnt = "Cent" ;
			String yaziyla= sayiyiYaziyaCevir.yaziyaCevir(qwe, 2, bordroPrinter.getDvzcins(), cnt , "#", null, null, null,"");
			parameters.put("Yazi",yaziyla);
			JasperPrint jp = prepareJasperPrint("BORDRO.jrxml", parameters, bordrooku,UygulamaSabitleri.KambiyoRaporYeri);
			return exportRapor(jp, "pdf");
		} catch (Exception e) {
			throw new ServiceException("Bordro Rapor Olusturma", e);
		}
	}

	public ByteArrayDataSource etiket(List<Map<String, String>> selectedRows) {
		try {
			ClassPathResource resource = new ClassPathResource(UygulamaSabitleri.AdresRaporYeri + "/ETIKET.jrxml");
			InputStream jrxmlInput = resource.getInputStream();
			JasperDesign jasper = JRXmlLoader.load(jrxmlInput);
			User user = userService.getCurrentUser();
			Etiket_Ayarlari eayar =  etiketAyarService.findByUserId(user.getId());
			jasper.setColumnWidth(eayar.getGenislik());
			jasper.setColumnSpacing(eayar.getDikeyarabosluk());
			jasper.setLeftMargin(eayar.getSolbosluk());
			jasper.setRightMargin(eayar.getSagbosluk());
			jasper.setTopMargin(eayar.getUstbosluk());
			jasper.setBottomMargin(eayar.getAltbosluk());
			if(eayar.getYataydikey() == 0)
				jasper.setPrintOrder(PrintOrderEnum.HORIZONTAL);
			else
				jasper.setPrintOrder(PrintOrderEnum.VERTICAL);
			JRDesignSection designSection = (JRDesignSection) jasper.getDetailSection();
			JRBand[] bands =  jasper.getDetailSection().getBands();
			JRDesignBand qweBand = (JRDesignBand) bands[0].clone();
			qweBand.setHeight(eayar.getYukseklik());
			designSection.removeBand(bands[0]);
			designSection.addBand(qweBand);
			JasperReport jr = JasperCompileManager.compileReport(jasper);
			List<ETIKET_ISIM> etISIM = new ArrayList<ETIKET_ISIM>();
			for (Map<String, String> row : selectedRows) {
				ETIKET_ISIM ets1  = new ETIKET_ISIM(row.get("Adi"),row.get("Adres_1"),row.get("Adres_2")
						,row.get("Semt"),row.get("Sehir"),row.get("Tel_1"));
				etISIM.add(ets1);
			}
			JRBeanCollectionDataSource qazBe = new JRBeanCollectionDataSource(etISIM);
			JasperPrint jp = JasperFillManager.fillReport(jr,null, qazBe);
			return exportRapor(jp, "pdf");
		} catch (Exception e) {
			throw new ServiceException( e.getMessage());
		}
	}

	public ByteArrayDataSource fatrap(List<Map<String, String>> tableData)  throws Exception {
		return excellToDataSource.export_excell(tableData);
	}

	public ByteArrayDataSource envanter(List<Map<String, String>> tableData)  throws Exception {
		return excellToDataSource.export_excell(tableData);
	}

	public ByteArrayDataSource imarap(List<Map<String, String>> tableData)  throws Exception {
		return excellToDataSource.export_excell(tableData);
	}

	public ByteArrayDataSource stokrap(List<Map<String, String>> tableData)  throws Exception {
		return excellToDataSource.export_excell(tableData);
	}
	
	public ByteArrayDataSource tahrap(List<Map<String, String>> tableData)  throws Exception {
		return excellToDataSource.export_excell(tableData);
	}
	
	public ByteArrayDataSource cekrap(List<Map<String, String>> tableData)  throws Exception {
		return excellToDataSource.export_excell(tableData);
	}
	
	public ByteArrayDataSource grprap(List<Map<String, String>> tableData,int sabitkolon)  throws Exception {
		return excellToDataSource.export_excell_grp(tableData,sabitkolon);
	}
	
	public ByteArrayDataSource imagrprap(List<Map<String, String>> tableData,int sabitkolon)  throws Exception {
		return excellToDataSource.export_excell_grp(tableData,sabitkolon);
	}
	
	public ByteArrayDataSource stokdetayrap(List<Map<String, String>> tableData)  throws Exception {
		return excellToDataSource.export_excell(tableData);
	}
	
	public ByteArrayDataSource kerdetay(List<Map<String, String>> tableData)  throws Exception {
		return excellToDataSource.export_excell(tableData);
	}
	
	public ByteArrayDataSource kereste_cikis(keresteyazdirDTO keresteyazdirDTO)  throws Exception {
		return excellToDataSource.export_excell_kercikis(keresteyazdirDTO);
	}

	private JasperPrint prepareJasperPrint(String jrxmlPath, Map<String, Object> parameters, List<Map<String, Object>> data , String raporyeri) throws Exception {
		ClassPathResource resource = new ClassPathResource(raporyeri + "/" + jrxmlPath);
		InputStream jrxmlInput = resource.getInputStream();
		JasperDesign jasper = JRXmlLoader.load(jrxmlInput);
		JasperReport jr = JasperCompileManager.compileReport(jasper);
		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);
		return JasperFillManager.fillReport(jr, parameters, dataSource);
	}

	private ByteArrayDataSource exportRapor(JasperPrint jp, String format) throws Exception {
		RaportToDataSource raporttoDatasource = new RaportToDataSource();
		switch (format.toLowerCase()) {
		case "pdf":
			return raporttoDatasource.export_pdf(jp);
		case "xlsx":
			return raporttoDatasource.export_xls(jp);
		case "word":
			return raporttoDatasource.export_docx(jp);
		case "xml":
			return raporttoDatasource.export_xml(jp);
		default:
			throw new IllegalArgumentException("Unsupported file format: " + format);
		}
	}
}