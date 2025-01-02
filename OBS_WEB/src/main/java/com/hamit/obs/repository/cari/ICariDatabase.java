package com.hamit.obs.repository.cari;

import java.util.List;
import java.util.Map;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.dto.cari.dekontDTO;
import com.hamit.obs.dto.cari.dvzcevirmeDTO;
import com.hamit.obs.dto.cari.hesapplaniDTO;
import com.hamit.obs.dto.cari.mizanDTO;
import com.hamit.obs.dto.cari.tahayarDTO;
import com.hamit.obs.dto.cari.tahrapDTO;
import com.hamit.obs.dto.cari.tahsilatDTO;
import com.hamit.obs.dto.cari.tahsilatTableRowDTO;


public interface ICariDatabase {

	String[] hesap_adi_oku(String hesap, ConnectionDetails cariConnDetails) ;
	List<Map<String, Object>> ekstre(String hesap , String t1 ,String t2,ConnectionDetails cariConnDetails) ;
	List<Map<String, Object>> ekstre_mizan(String kod,String ilktarih,String sontarih,String ilkhcins,String sonhcins,String ilkkar,String sonkar,ConnectionDetails cariConnDetails) ;
	List<Map<String, Object>> hesap_kodlari(ConnectionDetails cariConnDetails);
	List<Map<String, Object>> hp_pln(ConnectionDetails cariConnDetails);
	int cari_sonfisno(ConnectionDetails cariConnDetails);
	boolean cari_dekont_kaydet(dekontDTO dBilgi,ConnectionDetails cariConnDetails);
	List<dekontDTO> fiskon(int fisNo,ConnectionDetails cariConnDetails);
	int yenifisno(ConnectionDetails cariConnDetails);
	void evrak_yoket(int evrakno,ConnectionDetails cariConnDetails);
	List<Map<String, Object>> mizan(mizanDTO mizanDTO,ConnectionDetails cariConnDetails) ;
	String cari_firma_adi(ConnectionDetails cariConnDetails);
	void hsp_sil(String hesap,ConnectionDetails cariConnDetails);
	void hpln_kayit(hesapplaniDTO hesapplaniDTO, ConnectionDetails cariConnDetails);
	void hpln_detay_kayit(hesapplaniDTO hesapplaniDTO, ConnectionDetails cariConnDetails);
	hesapplaniDTO hsp_pln(String hesap ,ConnectionDetails cariConnDetails);
	List<Map<String, Object>> ozel_mizan(mizanDTO mizanDTO,ConnectionDetails cariConnDetails) ;
	List<Map<String, Object>> dvzcevirme(dvzcevirmeDTO dvzcevirmeDTO,ConnectionDetails cariConnDetails,ConnectionDetails kurConnectionDetails) ;
	List<Map<String, Object>> banka_sube(String nerden ,ConnectionDetails cariConnDetails);
	tahsilatDTO tahfiskon(String fisNo,Integer tah_ted,ConnectionDetails cariConnDetails);
	List<Map<String, Object>> tah_cek_doldur(String fisNo,Integer tah_ted,ConnectionDetails cariConnDetails) ;
	int cari_tahsonfisno(Integer tah_ted,ConnectionDetails cariConnDetails);
	int cari_tah_fisno_al(String tah_ted,ConnectionDetails cariConnDetails);
	void tah_kayit(tahsilatDTO tahsilatDTO , ConnectionDetails cariConnDetails);
	void tah_cek_sil(tahsilatDTO tahsilatDTO , ConnectionDetails cariConnDetails);
	void tah_cek_kayit(tahsilatTableRowDTO tahsilatTableRowDTO,String fisno,Integer tah_ted , ConnectionDetails cariConnDetails);
	void tah_sil(String fisno,Integer tah_ted , ConnectionDetails cariConnDetails);
	List<Map<String, Object>> tah_listele(tahrapDTO tahrapDTO,ConnectionDetails cariConnDetails,ConnectionDetails adresConnDetails) ;
	tahayarDTO tahayaroku(ConnectionDetails cariConnDetails);
	void tahayar_kayit(tahayarDTO tahayarDTO , ConnectionDetails cariConnDetails);
	List<Map<String, Object>> tah_ayar_oku(ConnectionDetails cariConnDetails);
	List<Map<String, Object>> tah_cek_kayit_aktar(String fisno,Integer tah_ted ,ConnectionDetails cariConnDetails);
	void cari_firma_adi_kayit(String fadi,ConnectionDetails cariConnDetails);
	List<Map<String, Object>> hsppln_liste(ConnectionDetails cariConnDetails);
	int hesap_plani_kayit_adedi(ConnectionDetails cariConnDetails);
	void cari_kod_degis_hesap(String eskikod,String yenikod,ConnectionDetails cariConnDetails);
	void cari_kod_degis_satirlar(String eskikod,String yenikod,ConnectionDetails cariConnDetails);
	void cari_kod_degis_tahsilat(String eskikod,String yenikod,ConnectionDetails cariConnDetails);
	List<Map<String, Object>> kasa_kontrol(String hesap , String t1 ,ConnectionDetails cariConnDetails) ;
	List<Map<String, Object>> kasa_mizan(String kod,String ilktarih,String sontarih,ConnectionDetails cariConnDetails) ;

}