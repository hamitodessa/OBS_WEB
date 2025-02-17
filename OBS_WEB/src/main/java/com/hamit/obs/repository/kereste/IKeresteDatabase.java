package com.hamit.obs.repository.kereste;

import java.util.List;
import java.util.Map;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.dto.kereste.kerestedetayDTO;

public interface IKeresteDatabase {

	String ker_firma_adi(ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> ker_kod_degisken_oku(String fieldd, String sno, String nerden,ConnectionDetails keresteConnDetails);
	String urun_kod_degisken_ara(String fieldd,String sno,String nerden,String arama,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> ker_kod_alt_grup_degisken_oku (int sno,ConnectionDetails keresteConnDetails);
	void urun_degisken_eski(String fieldd ,String degisken_adi ,String nerden ,String sno ,int ID ,ConnectionDetails keresteConnDetails);
	void urun_degisken_alt_grup_eski(String alt_grup ,int ana_grup ,int  ID,ConnectionDetails keresteConnDetails);
	void urun_degisken_kayit(String fieldd  ,String nerden,String degisken_adi,String sira,ConnectionDetails keresteConnDetails);
	void urun_degisken_alt_grup_kayit (String alt_grup , int ana_grup,ConnectionDetails keresteConnDetails);
	void urun_degisken_alt_grup_sil(int id,ConnectionDetails keresteConnDetails);
	void urun_kod_degisken_sil(String hangi_Y,String nerden,int sira,ConnectionDetails keresteConnDetails);
	boolean alt_grup_kontrol(int anagrp,int altgrp,ConnectionDetails keresteConnDetails);
	void ker_firma_adi_kayit(String fadi,ConnectionDetails keresteConnDetails);
	String son_no_al(String cins,ConnectionDetails keresteConnDetails);
	int evrak_no_al(String cins,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> ker_oku(String eno, String cins,ConnectionDetails keresteConnDetails);
	String aciklama_oku(String evrcins, int satir, String evrno, String gircik,ConnectionDetails keresteConnDetails);
	String[] dipnot_oku(String ino, String cins, String gircik,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> paket_oku(String pno,String nerden,ConnectionDetails keresteConnDetails);
	void ker_kaydet(kerestedetayDTO kerestedetayDTO,ConnectionDetails keresteConnDetails);
	void ker_giris_sil(String eno,ConnectionDetails keresteConnDetails);
	void dipnot_sil(String ino, String cins, String gircik,ConnectionDetails keresteConnDetails);
	void dipnot_yaz(String eno, String bir, String iki, String uc, String tip, String gircik, String usr,ConnectionDetails keresteConnDetails);
	void aciklama_sil(String evrcins, String evrno, String cins,ConnectionDetails keresteConnDetails);
	void aciklama_yaz(String evrcins, int satir, String evrno, String aciklama, String gircik,ConnectionDetails keresteConnDetails);

}
