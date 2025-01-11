package com.hamit.obs.service.cari;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.connection.ConnectionManager;
import com.hamit.obs.dto.cari.dekontDTO;
import com.hamit.obs.dto.cari.dvzcevirmeDTO;
import com.hamit.obs.dto.cari.hesapplaniDTO;
import com.hamit.obs.dto.cari.mizanDTO;
import com.hamit.obs.dto.cari.tahayarDTO;
import com.hamit.obs.dto.cari.tahrapDTO;
import com.hamit.obs.dto.cari.tahsilatDTO;
import com.hamit.obs.dto.cari.tahsilatTableRowDTO;
import com.hamit.obs.dto.loglama.LoglamaDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.repository.cari.ICariDatabase;
import com.hamit.obs.repository.loglama.LoglamaRepository;
import com.hamit.obs.service.adres.AdresService;
import com.hamit.obs.service.kur.KurService;

@Service
public class CariService {
	
	@Autowired
	private LoglamaRepository loglamaRepository;

	@Autowired
	private ConnectionManager masterConnectionManager;
	
	@Autowired
	private AdresService adresService;
	
	@Autowired
	private KurService kurService;
	
	private LoglamaDTO loglamaDTO = new LoglamaDTO();
	private final CariDatabaseContext databaseStrategyContext;
	private ICariDatabase strategy;
	
	public CariService(CariDatabaseContext databaseStrategyContext) {
		this.databaseStrategyContext = databaseStrategyContext;
	}
	public void initialize() {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();

			UserSessionManager.removeUserByModul(useremail,"Cari Hesap");
			this.strategy = databaseStrategyContext.getStrategy();
			masterConnectionManager.loadConnections("Cari Hesap",useremail);
			
			UserSessionManager.addUserSession(useremail, "Cari Hesap", masterConnectionManager.getConnection("Cari Hesap", useremail));
			//return masterConnectionManager.getConnection("Cari Hesap", useremail);
		} else {
			throw new ServiceException("No authenticated user found in SecurityContext");
		}
	}
	
	public String[] conn_detail() {
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
		String[] detay = {"","",""};
		detay[0] = cariConnDetails.getHangisql() ;
		detay[1] = cariConnDetails.getDatabaseName() ;
		detay[2] = cariConnDetails.getServerIp() ;
		return detay;
	}
	
	public String[] hesap_adi_oku(String hesap) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.hesap_adi_oku(hesap,cariConnDetails);
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
	public List<Map<String, Object>> ekstre(String hesap, String t1, String t2){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.ekstre(hesap, t1, t2,cariConnDetails);
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
	public List<Map<String, Object>> hesap_kodlari(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.hesap_kodlari(cariConnDetails);
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
	public List<Map<String, Object>> hp_pln(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.hp_pln(cariConnDetails);
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

	public int sonfisNo() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.cari_sonfisno(cariConnDetails);
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

	public boolean cari_dekont_kaydet(dekontDTO dBilgi){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			loglamaDTO.setEvrak(String.valueOf(dBilgi.getFisNo()));
			String mesaj = "A. Hes:" + dBilgi.getAhes().toString().trim() + " Tut:" + dBilgi.getAlacak() +
					" B. Hes:"+ dBilgi.getBhes().toString().trim() + " Tut:" + dBilgi.getBorc();
			String mesaj1 = dBilgi.getIzahat().trim();
			if( mesaj.length() + mesaj1.length() <= 95)
				mesaj = mesaj + " Msj:" + mesaj1 ;
			else
				mesaj = mesaj + " Msj:" + mesaj1.substring(0, 95  -(mesaj.length())) ;
			loglamaDTO.setmESAJ(mesaj);
			loglamaDTO.setUser(dBilgi.getUser());
			loglamaRepository.log_kaydet(loglamaDTO, cariConnDetails);
			return strategy.cari_dekont_kaydet(dBilgi,cariConnDetails);
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
	public List<dekontDTO> fiskon(int fisNo){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.fiskon(fisNo,cariConnDetails);
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
	public int yenifisno() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.yenifisno(cariConnDetails);
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
	public void evrak_yoket(int evrakno,String user) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			loglamaDTO.setEvrak(String.valueOf(evrakno));
			loglamaDTO.setmESAJ(String.valueOf(evrakno) + " Evrak Silme");
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, cariConnDetails);
			strategy.evrak_yoket(evrakno, cariConnDetails);
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
	
	public List<Map<String, Object>> mizan(mizanDTO mizanDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.mizan(mizanDTO,cariConnDetails);
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
	
	public String cari_firma_adi() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.cari_firma_adi(cariConnDetails) ;
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
	public void hsp_sil(String hesap)
	{
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			strategy.hsp_sil(hesap,cariConnDetails);
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
	
	public void hpln_kayit(hesapplaniDTO hesapplaniDTO)
	{
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			strategy.hpln_kayit(hesapplaniDTO,cariConnDetails);
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
	
	public void hpln_detay_kayit(hesapplaniDTO hesapplaniDTO)
	{
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			strategy.hpln_detay_kayit(hesapplaniDTO,cariConnDetails);
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
	public hesapplaniDTO hsp_pln(String hesap){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.hsp_pln(hesap,cariConnDetails);
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
	
	public List<Map<String, Object>> ozel_mizan(mizanDTO mizanDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.ozel_mizan(mizanDTO,cariConnDetails);
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

	public List<Map<String, Object>> dvzcevirme(dvzcevirmeDTO dvzcevirmeDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.dvzcevirme(dvzcevirmeDTO,cariConnDetails,kurService.conn_details());
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
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.banka_sube(nerden,cariConnDetails);
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
	
	public tahsilatDTO tahfiskon(String fisNo,Integer tah_ted){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.tahfiskon(fisNo,tah_ted,cariConnDetails);
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
	
	public List<Map<String, Object>> tah_cek_doldur(String fisNo,Integer tah_ted){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.tah_cek_doldur(fisNo,tah_ted,cariConnDetails);
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
	
	public int cari_tahsonfisno(Integer tah_ted) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.cari_tahsonfisno(tah_ted,cariConnDetails);
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

	public int cari_tah_fisno_al(String tah_ted) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.cari_tah_fisno_al(tah_ted,cariConnDetails);
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
	
	public void tah_kayit(tahsilatDTO tahsilatDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			strategy.tah_kayit(tahsilatDTO,cariConnDetails);
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
	
	public void tah_cek_sil(tahsilatDTO tahsilatDTO,String user) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			loglamaDTO.setEvrak(String.valueOf(tahsilatDTO.getFisNo()));
			loglamaDTO.setmESAJ(String.valueOf(tahsilatDTO.getFisNo()) + " Tahsilat Evrak Silme");
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, cariConnDetails);
			strategy.tah_cek_sil(tahsilatDTO,cariConnDetails);
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
	
	public void tah_cek_kayit(tahsilatTableRowDTO tahsilatTableRowDTO, String fisno, Integer tah_ted) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			strategy.tah_cek_kayit(tahsilatTableRowDTO,fisno,tah_ted,cariConnDetails);
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
	
	public void tah_sil(String fisno, Integer tah_ted,String user)
	{
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			loglamaDTO.setEvrak(String.valueOf(fisno));
			loglamaDTO.setmESAJ(String.valueOf(fisno) + " Tahsilat Evrak Silme");
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, cariConnDetails);
			strategy.tah_sil(fisno,tah_ted,cariConnDetails);
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
	
	public List<Map<String, Object>> tah_listele(tahrapDTO tahrapDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.tah_listele(tahrapDTO,cariConnDetails,adresService.conn_details());
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

	public tahayarDTO tahayaroku(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.tahayaroku(cariConnDetails);
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

	public void tahayar_kayit(tahayarDTO tahayarDTO)
	{
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			strategy.tahayar_kayit(tahayarDTO,cariConnDetails);
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

	public List<Map<String, Object>> tah_ayar_oku(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.tah_ayar_oku(cariConnDetails);
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
	
	public List<Map<String, Object>> tah_cek_kayit_aktar(String fisno, Integer tah_ted){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.tah_cek_kayit_aktar(fisno, tah_ted,cariConnDetails);
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
	
	public void cari_firma_adi_kayit(String fadi) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			strategy.cari_firma_adi_kayit(fadi,cariConnDetails);
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
	public List<Map<String, Object>> hsppln_liste(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.hsppln_liste(cariConnDetails);
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
	
	public int hesap_plani_kayit_adedi() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.hesap_plani_kayit_adedi(cariConnDetails);
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
	
	public void cari_kod_degis_hesap(String eskikod, String yenikod,String user) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			loglamaDTO.setEvrak("");
			loglamaDTO.setmESAJ("Kod Degistirme   Eski Kod:" + eskikod + " Yeni Kod:"+yenikod);
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, cariConnDetails);
			strategy.cari_kod_degis_hesap(eskikod,yenikod,cariConnDetails);
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
	
	public void cari_kod_degis_satirlar(String eskikod, String yenikod,String user) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			loglamaDTO.setEvrak("");
			loglamaDTO.setmESAJ("Kod Degistirme   Eski Kod:" + eskikod + " Yeni Kod:"+yenikod);
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, cariConnDetails);
			strategy.cari_kod_degis_satirlar(eskikod,yenikod,cariConnDetails);
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
	
	public void cari_kod_degis_tahsilat(String eskikod, String yenikod,String user) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			loglamaDTO.setEvrak("");
			loglamaDTO.setmESAJ("Kod Degistirme   Eski Kod:" + eskikod + " Yeni Kod:"+yenikod);
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, cariConnDetails);
			strategy.cari_kod_degis_tahsilat(eskikod,yenikod,cariConnDetails);
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
	public List<Map<String, Object>> kasa_kontrol(String hesap, String t1){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.kasa_kontrol(hesap,t1,cariConnDetails);
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
	public List<Map<String, Object>> kasa_mizan(String kod, String ilktarih, String sontarih){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, "Cari Hesap");
			return strategy.kasa_mizan(kod,ilktarih,sontarih ,cariConnDetails);
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