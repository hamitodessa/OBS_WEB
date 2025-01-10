package com.hamit.obs.service.kambiyo;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.connection.ConnectionManager;
import com.hamit.obs.custom.yardimci.Formatlama;
import com.hamit.obs.dto.kambiyo.bordrodetayDTO;
import com.hamit.obs.dto.kambiyo.cekraporDTO;
import com.hamit.obs.dto.loglama.LoglamaDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.kambiyo.IKambiyoDatabase;
import com.hamit.obs.repository.loglama.LoglamaRepository;

@Service
public class KambiyoService {

	@Autowired
	private LoglamaRepository loglamaRepository;
	
	private LoglamaDTO loglamaDTO = new LoglamaDTO();
	
	@Autowired
	private ConnectionManager masterConnectionManager;
	
	private final KambiyoDatabaseContext databaseStrategyContext;
	private IKambiyoDatabase strategy;
	public KambiyoService(KambiyoDatabaseContext databaseStrategyContext) {
		this.databaseStrategyContext = databaseStrategyContext;
	}
	public ConnectionDetails  initialize() {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			this.strategy = databaseStrategyContext.getStrategy();
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			return masterConnectionManager.getConnection("Kambiyo", useremail);
		} else {
			throw new ServiceException("No authenticated user found in SecurityContext");
		}
	}
	
	public String[] conn_detail() {
		ConnectionDetails kambiyoConnDetails = initialize();
		String[] detay = {"","",""};
		detay[0] = kambiyoConnDetails.getHangisql() ;
		detay[1] = kambiyoConnDetails.getDatabaseName() ;
		detay[2] = kambiyoConnDetails.getServerIp() ;
		return detay;
	}

	public List<Map<String, Object>> ozel_kodlar(String gir_cik){
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			return strategy.ozel_kodlar(gir_cik,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	public List<Map<String, Object>> banka_sube(String nerden){
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			return strategy.banka_sube(nerden,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}

	public String kambiyo_firma_adi() {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			return strategy.kambiyo_firma_adi(kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}

	public int kam_son_bordro_no_al(String cek_sen, String gir_cik) {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			return strategy.kam_son_bordro_no_al(cek_sen,gir_cik,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}

	public List<Map<String, Object>> bordroOku(String bordroNo, String cek_sen, String gir_cik){
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			return strategy.bordroOku(bordroNo, cek_sen, gir_cik,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public String kam_aciklama_oku(String cek_sen,int satir,String bordroNo,String gircik){
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			return strategy.kam_aciklama_oku(cek_sen, satir, bordroNo,gircik ,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}

	public void  bordro_sil(String bordroNo,String cek_sen,String gir_cik,String user) {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			loglamaDTO.setEvrak(String.valueOf(bordroNo));
			loglamaDTO.setmESAJ(String.valueOf(bordroNo) + " Bordro Silme");
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, kambiyoConnDetails);
			strategy.bordro_sil(bordroNo, cek_sen,gir_cik ,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void  cek_kayit(bordrodetayDTO row,String user) {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			loglamaDTO.setEvrak(String.valueOf(row.getGirisBordro()));
			loglamaDTO.setmESAJ(String.valueOf(row.getGirisBordro()) + " Nolu Giris Bordro  " + row.getCekNo() + " Nolu Cek " + " Tutar:" + Formatlama.doub_2(row.getTutar()) + " " + row.getCins());
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, kambiyoConnDetails);
			strategy.cek_kayit(row ,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void  kam_aciklama_yaz(String cek_sen, int satir, String bordroNo, String aciklama, String gircik) {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			strategy.kam_aciklama_yaz(cek_sen, satir,bordroNo,aciklama,gircik ,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void  kam_aciklama_sil(String cek_sen, String bordroNo, String gircik) {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			strategy.kam_aciklama_sil(cek_sen, bordroNo,gircik ,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	public int kam_bordro_no_al(String cins) {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			return strategy.kam_bordro_no_al(cins,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public List<Map<String, Object>> kalan_cek_liste(){
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			return strategy.kalan_cek_liste(kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public String cek_kontrol(String cekno) {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			return strategy.cek_kontrol(cekno,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public bordrodetayDTO cek_dokum(String cekno) {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			return strategy.cek_dokum(cekno,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void bordro_cikis_sil(String bordroNo, String cek_sen,String user) {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			loglamaDTO.setEvrak(bordroNo);
			loglamaDTO.setmESAJ(bordroNo + " Nolu Cikis Bordro Numaralari Silme ");
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, kambiyoConnDetails);
			strategy.bordro_cikis_sil(bordroNo,cek_sen ,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void bordro_cikis_yaz(String cek_sen, String ceksencins_where, String cekno, String cmus, String cbor,
			String ctar, String ozkod) {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			strategy.bordro_cikis_yaz(cek_sen, ceksencins_where, cekno,cmus,cbor,ctar,ozkod,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public List<Map<String, Object>> cek_rapor(cekraporDTO cekraporDTO){
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			return strategy.cek_rapor(cekraporDTO,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	public void kambiyo_firma_adi_kayit(String fadi) {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			strategy.kambiyo_firma_adi_kayit(fadi,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	public bordrodetayDTO cektakipkontrol(String cekno) {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			return strategy.cektakipkontrol(cekno,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
	
	public void kam_durum_yaz(String cekno, String ceksen_from, String ceksen_where, String durum, String ttarih) {
		try {
			ConnectionDetails kambiyoConnDetails = initialize();
			strategy.kam_durum_yaz(cekno,ceksen_from,ceksen_where,durum,ttarih,kambiyoConnDetails);
		} catch (ServiceException e) {
			String originalMessage = e.getMessage();
			Throwable cause = e.getCause();
			String detailedMessage = originalMessage;
			if (cause != null) {
				detailedMessage += " - " + cause.getMessage();
			}
			throw new ServiceException(detailedMessage);
		}
	}
}
