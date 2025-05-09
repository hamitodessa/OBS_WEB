package com.hamit.obs.repository.kereste;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Pageable;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.dto.kereste.kerestedetayDTO;
import com.hamit.obs.dto.kereste.kerestedetayraporDTO;
import com.hamit.obs.dto.kereste.kergrupraporDTO;

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
	List<Map<String, Object>> ker_barkod_kod_oku(String sira,ConnectionDetails keresteConnDetails);
	void ker_cikis_sil(String eno,ConnectionDetails keresteConnDetails);
	void ker_cikis_kaydet(kerestedetayDTO kerestedetayDTO,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> kod_pln(ConnectionDetails keresteConnDetails);
	void kod_kayit(String kodu, String aciklama,ConnectionDetails keresteConnDetails);
	void kod_sil(String kod,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> kons_pln(ConnectionDetails keresteConnDetails);
	void kons_kayit(String kons, String aciklama,int paket_no,ConnectionDetails keresteConnDetails);
	int kons_sil(String kons,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> urun_detay(String pakno,String kons, String kodu,String evrak,ConnectionDetails keresteConnDetails);
	String kod_adi(String kod,ConnectionDetails keresteConnDetails);
	String kons_adi(String kons,ConnectionDetails keresteConnDetails);
	void ker_kod_degis(String paket_No, String kon, String yenikod,int satir,ConnectionDetails keresteConnDetails);
	void ker_kons_degis(String kons, String yenikons, int satir,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> stok_rapor(kerestedetayraporDTO kerestedetayraporDTO,Pageable pageable,  ConnectionDetails keresteConnDetails);
	double stok_raporsize(kerestedetayraporDTO kerestedetayraporDTO ,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> baslik_bak(String baslik, String ordr, String jkj, String k1, String k2, String f1,
			String f2, String t1, String t2,String dURUM,String e1, String e2,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> grp_rapor(String gruplama,String sstr_2, String sstr_4, String kur_dos, String qwq6,
			String qwq7, String qwq8, String k1, String k2, String s1, String s2, String jkj,
			String t1, String t2, String sstr_5, String sstr_1,String orderBY,String dURUM,String ko1, String ko2,String dpo,String grup,
			String e1 , String e2,String ozelgrp[][],Set<String> sabitkolonlar,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> fat_rapor(kerestedetayraporDTO kerestedetayraporDTO,ConnectionDetails keresteConnDetails,ConnectionDetails cariConnDetails );
	double fat_raporsize(kerestedetayraporDTO kerestedetayraporDTO,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> fat_detay_rapor(String fno , String turu,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> fat_rapor_fat_tar(kerestedetayraporDTO kerestedetayraporDTO,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> fat_rapor_cari_kod(kerestedetayraporDTO kerestedetayraporDTO,ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> envanter(kerestedetayraporDTO kerestedetayraporDTO,String gruplama[],ConnectionDetails keresteConnDetails);
	List<Map<String, Object>> ort_diger_kodu(kergrupraporDTO kergrupraporDTO ,  String yu, String iu,ConnectionDetails keresteConnDetails,ConnectionDetails kurConnDetails);
}
