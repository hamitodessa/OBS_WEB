package com.hamit.obs.repository.fatura;

import java.util.List;
import java.util.Map;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.dto.stok.urunDTO;
import com.hamit.obs.dto.stok.raporlar.fatraporDTO;

public interface IFaturaDatabase {

	String fat_firma_adi(ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> urun_kodlari(ConnectionDetails faturaConnDetails);
	urunDTO stk_urun(String sira,String arama,ConnectionDetails faturaConnDetails);
	String urun_kod_degisken_ara(String fieldd,String sno,String nerden,String arama,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> stk_kod_degisken_oku(String fieldd,String sno,String nerden,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> stk_kod_alt_grup_degisken_oku (int sno,ConnectionDetails faturaConnDetails);
	String ur_kod_bak(String kodu,ConnectionDetails faturaConnDetails);
	void stk_ur_sil(String kodu,ConnectionDetails faturaConnDetails);
	void stk_ur_kayit(urunDTO urunDTO,ConnectionDetails faturaConnDetails);
	void stk_firma_adi_kayit(String fadi,ConnectionDetails faturaConnDetails);
	String uret_son_bordro_no_al(ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> stok_oku(String eno, String cins,ConnectionDetails faturaConnDetails);
	String aciklama_oku(String evrcins, int satir, String evrno, String gircik,ConnectionDetails faturaConnDetails);
	urunDTO urun_adi_oku (String kodu,String kodbarcode,ConnectionDetails faturaConnDetails);
	double son_imalat_fiati_oku(String kodu,ConnectionDetails faturaConnDetails);
	String uret_ilk_tarih(String baslangic, String tar, String ukodu,ConnectionDetails faturaConnDetails);
	double gir_ort_fiati_oku(String kodu, String ilkt, String tarih,ConnectionDetails faturaConnDetails);
	int uretim_fisno_al(ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> recete_oku(String rno,ConnectionDetails faturaConnDetails);
	void stok_sil(String eno, String ecins, String cins,ConnectionDetails faturaConnDetails);
	void stk_kaydet(String evrno, String evrcins, String tarih, int depo, String urnkodu, double miktar,
			double fiat, double tutar, double kdvlitut, String hareket, String izah, int anagrp, int altgrp, double kur,
			String b1, String doviz, String hspkodu, String usr,ConnectionDetails faturaConnDetails);
	void aciklama_yaz(String evrcins, int satir, String evrno, String aciklama, String gircik,ConnectionDetails faturaConnDetails);
	void aciklama_sil(String evrcins,String evrno,String cins,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> urun_arama(ConnectionDetails faturaConnDetails);
	void urun_degisken_eski(String fieldd ,String degisken_adi ,String nerden ,String sno ,int ID ,ConnectionDetails faturaConnDetails);
	void urun_degisken_alt_grup_eski(String alt_grup ,int ana_grup ,int  ID,ConnectionDetails faturaConnDetails);
	void urun_degisken_kayit(String fieldd  ,String nerden,String degisken_adi,String sira,ConnectionDetails faturaConnDetails);
	void  urun_degisken_alt_grup_kayit (String alt_grup , int ana_grup,ConnectionDetails faturaConnDetails);
	boolean alt_grup_kontrol(int anagrp,int altgrp,ConnectionDetails faturaConnDetails);
	void urun_degisken_alt_grup_sil(int id,ConnectionDetails faturaConnDetails);
	void urun_kod_degisken_sil(String hangi_Y,String nerden,int sira,ConnectionDetails faturaConnDetails);
	double son_satis_fiati_oku(String kodu,String muskodu,String gircik,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> fatura_oku(String fno,String cins,ConnectionDetails faturaConnDetails);
	String[] dipnot_oku(String ino,String cins ,String gircik,ConnectionDetails faturaConnDetails);
	String son_no_al(String cins,ConnectionDetails faturaConnDetails);
	int fatura_no_al(String cins,ConnectionDetails faturaConnDetails);
	void fat_giris_sil(String fno,String cins,ConnectionDetails faturaConnDetails);
	void dipnot_sil(String ino,String cins,String gircik,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> fat_oz_kod (String cins,ConnectionDetails faturaConnDetails);
	void fat_kaydet(String fatno ,String kodu ,int depo ,double  fiat ,double tevkifat  
			, double miktar ,String gircik ,double tutar,double iskonto ,double kdv  
			, String tarih, String izah,String doviz,String  adrfirma ,String carfirma  
			, String ozkod ,double kur ,String cins,int  anagrp,int  altgrp ,String usr,ConnectionDetails faturaConnDetails);
	void dipnot_yaz(String eno,String bir,String iki,String uc,String tip,String gircik,String usr,ConnectionDetails faturaConnDetails);
	String recete_son_bordro_no_al(ConnectionDetails faturaConnDetails);
	int recete_no_al(ConnectionDetails faturaConnDetails);
	void rec_sil(String rno,ConnectionDetails faturaConnDetails);
	void kod_recete_yaz(String ukodu,String rec,ConnectionDetails faturaConnDetails);
	void recete_kayit(String recno,boolean durum,String tur,String kodu ,double miktar ,int anagrp,int altgrup ,String usr,ConnectionDetails faturaConnDetails);
	String zayi_son_bordro_no_al(ConnectionDetails faturaConnDetails);
	int zayi_fisno_al(ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> zayi_oku(String eno,String cins,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> fat_rapor(fatraporDTO fatraporDTO,ConnectionDetails faturaConnDetails);
}
