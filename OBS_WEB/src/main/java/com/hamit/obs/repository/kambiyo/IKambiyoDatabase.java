package com.hamit.obs.repository.kambiyo;

import java.util.List;
import java.util.Map;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.dto.kambiyo.bordrodetayDTO;
import com.hamit.obs.dto.kambiyo.cekraporDTO;

public interface IKambiyoDatabase {
	
	List<Map<String, Object>> ozel_kodlar(String gir_cik, ConnectionDetails kambiyoConnDetails);
	List<Map<String, Object>> banka_sube(String nerden ,ConnectionDetails kambiyoConnDetails);
	String kambiyo_firma_adi(ConnectionDetails kambiyoConnDetails);
	int kam_son_bordro_no_al(String cek_sen,String gir_cik,ConnectionDetails kambiyoConnDetails);
	List<Map<String, Object>> bordroOku(String bordroNo,String cek_sen,String gir_cik,ConnectionDetails kambiyoConnDetails) ;
	String kam_aciklama_oku(String cek_sen,int satir,String bordroNo,String gircik,ConnectionDetails kambiyoConnDetails);
	void bordro_sil(String bordroNo,String cek_sen,String gir_cik,ConnectionDetails kambiyoConnDetails) ;
	void cek_kayit(bordrodetayDTO bordrodetayDTO,ConnectionDetails kambiyoConnDetails) ;
	void kam_aciklama_yaz(String cek_sen,int satir,String bordroNo,String aciklama,String gircik,ConnectionDetails kambiyoConnDetails);
	void kam_aciklama_sil(String cek_sen,String bordroNo,String gircik,ConnectionDetails kambiyoConnDetails);
	int kam_bordro_no_al(String cins,ConnectionDetails kambiyoConnDetails);
	List<Map<String, Object>> kalan_cek_liste(ConnectionDetails kambiyoConnDetails);
	String cek_kontrol(String cekno,ConnectionDetails kambiyoConnDetails);
	bordrodetayDTO cek_dokum(String cekno,ConnectionDetails kambiyoConnDetails);
	void bordro_cikis_sil(String bordroNo,String cek_sen,ConnectionDetails kambiyoConnDetails);
	void bordro_cikis_yaz(String cek_sen,String ceksencins_where,String cekno,String cmus ,
			String cbor,String ctar,String ozkod,ConnectionDetails kambiyoConnDetails);
	List<Map<String, Object>> cek_rapor(cekraporDTO cekraporDTO,ConnectionDetails kambiyoConnDetails) ;
	void kambiyo_firma_adi_kayit(String fadi,ConnectionDetails kambiyoConnDetails);
	bordrodetayDTO cektakipkontrol(String cekno,ConnectionDetails kambiyoConnDetails);
	void kam_durum_yaz(String cekno,String ceksen_from,String ceksen_where,String durum,String ttarih,ConnectionDetails kambiyoConnDetails);
}
