package com.hamit.obs.repository.fatura;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Pageable;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.dto.stok.urunDTO;
import com.hamit.obs.dto.stok.raporlar.envanterDTO;
import com.hamit.obs.dto.stok.raporlar.fatraporDTO;
import com.hamit.obs.dto.stok.raporlar.grupraporDTO;
import com.hamit.obs.dto.stok.raporlar.imaraporDTO;
import com.hamit.obs.dto.stok.raporlar.stokdetayDTO;

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
	void urun_degisken_alt_grup_kayit (String alt_grup , int ana_grup,ConnectionDetails faturaConnDetails);
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
	List<Map<String, Object>> fat_rapor(fatraporDTO fatraporDTO,Pageable pageable,ConnectionDetails faturaConnDetails);
	double fat_raporsize(fatraporDTO fatraporDTO ,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> fat_detay_rapor(String fno , String turu,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> fat_rapor_fat_tar(fatraporDTO fatraporDTO,Pageable pageable,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> fat_rapor_cari_kod(fatraporDTO fatraporDTO,Pageable pageable,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> imalat_rapor(imaraporDTO imaraporDTO,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> envanter_rapor(envanterDTO envanterDTO , ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> envanter_rapor_fifo(envanterDTO envanterDTO, ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> envanter_rapor_fifo_2(envanterDTO envanterDTO, ConnectionDetails faturaConnDetails);
	double envanter_rapor_lifo(envanterDTO envanterDTO, ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> envanter_rapor_u_kodu_oncekitarih(envanterDTO envanterDTO, ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> envanter_rapor_u_kodu(envanterDTO envanterDTO, ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> envanter_rapor_ana_grup_alt_grup(envanterDTO envanterDTO, ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> baslik_bak(String baslik,String ordr,String jkj,String ch1,String k1,String k2,String f1,String f2,String t1,String t2, ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> grp_urn_kodlu(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][],Set<String> sabitkolonlar,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> grp_urn_kodlu_yil(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][],Set<String> sabitkolonlar,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> grp_mus_kodlu(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][],Set<String> sabitkolonlar,ConnectionDetails faturaConnDetails, ConnectionDetails cariConnDetails);
	List<Map<String, Object>> grp_mus_kodlu_yil(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][],Set<String> sabitkolonlar,ConnectionDetails faturaConnDetails, ConnectionDetails cariConnDetails);
	List<Map<String, Object>> grp_yil_ay(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][],Set<String> sabitkolonlar,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> grp_yil(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][],Set<String> sabitkolonlar,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> grp_ana_grup(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][],Set<String> sabitkolonlar,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> grp_ana_grup_yil(grupraporDTO grupraporDTO,String sstr_2,String sstr_4,String kur_dos,String jkj,String ch1,String jkj1,
			String sstr_5,String sstr_1,String ozelgrp[][],Set<String> sabitkolonlar,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> ima_baslik_bak(String bas ,String jkj,String ch1,String qwq6,
			String qwq7,String qwq8,String qwq9,String k1,String k2,String t1,String t2,String ordrr,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> ima_alt_kod(String slct,String sstr_5,String sstr_2,String sstr_4,String jkj,String ch1,String qwq6,
			String qwq7,String qwq8,String qwq9,String s1 ,String s2,String k1,String k2,String t1,String t2,
			String sstr_1,String ordrr,String sstr_55,String ozelgrp[][],Set<String> sabitkolonlar,ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> stok_rapor(stokdetayDTO stokdetayDTO,ConnectionDetails faturaConnDetails);

	List<Map<String, Object>> irsaliye_oku(String irsno, String cins, ConnectionDetails faturaConnDetails);

	void irs_giris_sil(String fno, String cins, ConnectionDetails faturaConnDetails);
	int irsaliye_no_al(String cins, ConnectionDetails faturaConnDetails);
	String son_irsno_al(String cins, ConnectionDetails faturaConnDetails);
	List<Map<String, Object>> irs_rapor(fatraporDTO fatraporDTO, Pageable pageable,
			ConnectionDetails faturaConnDetails);

	void irs_kaydet(String irsno, String kodu, int depo, double fiat, double tevkifat, double miktar, String gircik,
			double tutar, double iskonto, double kdv, String tarih, String izah, String doviz, String adrfirma,
			String carfirma, String ozkod, double kur, String cins, int anagrp, int altgrp, String usr,
			String fatno, String sevktarih, ConnectionDetails faturaConnDetails);
	double irs_raporsize(fatraporDTO fatraporDTO, ConnectionDetails faturaConnDetails);

	List<Map<String, Object>> irs_detay_rapor(String fno, String turu, ConnectionDetails faturaConnDetails);
}
