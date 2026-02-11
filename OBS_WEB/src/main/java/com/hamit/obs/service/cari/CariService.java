package com.hamit.obs.service.cari;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hamit.obs.config.UserSessionManager;
import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.enums.RoleUtil;
import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.custom.yardimci.Global_Yardimci;
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
import com.hamit.obs.model.user.RolEnum;
import com.hamit.obs.repository.cari.ICariDatabase;
import com.hamit.obs.repository.loglama.LoglamaRepository;
import com.hamit.obs.service.adres.AdresService;
import com.hamit.obs.service.context.CariDatabaseContext;
import com.hamit.obs.service.kur.KurService;

@Service
public class CariService {

	@Autowired
	private LoglamaRepository loglamaRepository;

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
	    var auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth == null)
	        throw new ServiceException("No authenticated user found in SecurityContext");
	    String email = auth.getName();
	    ConnectionDetails cd = UserSessionManager.getUserSession(email, modulTipi.CARI_HESAP);
	    if (cd == null)
	        throw new ServiceException("CARI bağlantısı bulunamadı: " + email);
	    this.strategy = databaseStrategyContext.getStrategy(cd.getSqlTipi());
	}

	
	public String[] conn_detail() {
		String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
		ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
		String[] detay = {"","",""};
		detay[0] = cariConnDetails.getSqlTipi().getValue() ;
		detay[1] = cariConnDetails.getDatabaseName() ;
		detay[2] = cariConnDetails.getServerIp() ;
		return detay;
	}

	public String[] hesap_adi_oku(String hesap) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.hesap_adi_oku(hesap,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> ekstre(String hesap, String t1, String t2,Pageable pageable){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.ekstre(hesap, t1, t2,pageable,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public double eks_raporsize(String hesap , String t1 ,String t2) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.eks_raporsize(hesap, t1, t2,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public List<Map<String, Object>> hesap_kodlari(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.hesap_kodlari(cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public List<Map<String, Object>> hp_pln(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.hp_pln(cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public int sonfisNo() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.cari_sonfisno(cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public boolean cari_dekont_kaydet(dekontDTO dBilgi){
		try {
			if (! RoleUtil.durumRole(RolEnum.ADMIN))
				throw new ServiceException("Yetkiniz Yok");

			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			
			String usrString = Global_Yardimci.user_log(SecurityContextHolder.getContext().getAuthentication().getName());
			evrak_yoket(dBilgi.getFisNo(),usrString);
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
			throw new ServiceException(errorMessages(e));
		}
	}
	public List<dekontDTO> fiskon(int fisNo){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.fiskon(fisNo,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public int yenifisno() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.yenifisno(cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public void evrak_yoket(int evrakno,String user) {
		//RolEnum rolEnum = RoleUtil.resolveRolEnum(SecurityContextHolder.getContext().getAuthentication());
		//String rolAdi = rolEnum != null ? rolEnum.name() : null;
		// System.out.println("Rol Adi=" +rolAdi);
		if (! RoleUtil.durumRole(RolEnum.ADMIN))
			throw new ServiceException("Yetkiniz Yok");

		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			loglamaDTO.setEvrak(String.valueOf(evrakno));
			loglamaDTO.setmESAJ(String.valueOf(evrakno) + " Evrak Silme");
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, cariConnDetails);
			strategy.evrak_yoket(evrakno, cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> mizan(mizanDTO mizanDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.mizan(mizanDTO,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public String cari_firma_adi() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.cari_firma_adi(cariConnDetails) ;
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public void hsp_sil(String hesap)
	{
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			strategy.hsp_sil(hesap,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void hpln_kayit(hesapplaniDTO hesapplaniDTO)
	{
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			strategy.hpln_kayit(hesapplaniDTO,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void hpln_detay_kayit(hesapplaniDTO hesapplaniDTO)
	{
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			strategy.hpln_detay_kayit(hesapplaniDTO,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public hesapplaniDTO hsp_pln(String hesap){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.hsp_pln(hesap,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> ozel_mizan(mizanDTO mizanDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.ozel_mizan(mizanDTO,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> dvzcevirme(dvzcevirmeDTO dvzcevirmeDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.dvzcevirme(dvzcevirmeDTO,cariConnDetails,kurService.conn_details());
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	
	public double dvz_raporsize(dvzcevirmeDTO dvzcevirmeDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.dvz_raporsize(dvzcevirmeDTO,cariConnDetails,kurService.conn_details());
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}


	public List<Map<String, Object>> banka_sube(String nerden){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.banka_sube(nerden,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public tahsilatDTO tahfiskon(String fisNo,Integer tah_ted){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.tahfiskon(fisNo,tah_ted,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> tah_cek_doldur(String fisNo,Integer tah_ted){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.tah_cek_doldur(fisNo,tah_ted,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public int cari_tahsonfisno(Integer tah_ted) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.cari_tahsonfisno(tah_ted,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public int cari_tah_fisno_al(String tah_ted) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.cari_tah_fisno_al(tah_ted,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void tah_kayit(tahsilatDTO tahsilatDTO) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			strategy.tah_kayit(tahsilatDTO,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void tah_cek_sil(tahsilatDTO tahsilatDTO,String user) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			loglamaDTO.setEvrak(String.valueOf(tahsilatDTO.getFisNo()));
			loglamaDTO.setmESAJ(String.valueOf(tahsilatDTO.getFisNo()) + " Tahsilat Evrak Silme");
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, cariConnDetails);
			strategy.tah_cek_sil(tahsilatDTO,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void tah_cek_kayit(tahsilatTableRowDTO tahsilatTableRowDTO, String fisno, Integer tah_ted) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			strategy.tah_cek_kayit(tahsilatTableRowDTO,fisno,tah_ted,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void tah_sil(String fisno, Integer tah_ted,String user)
	{
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			loglamaDTO.setEvrak(String.valueOf(fisno));
			loglamaDTO.setmESAJ(String.valueOf(fisno) + " Tahsilat Evrak Silme");
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, cariConnDetails);
			strategy.tah_sil(fisno,tah_ted,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> tah_listele(tahrapDTO tahrapDTO){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.tah_listele(tahrapDTO,cariConnDetails,adresService.conn_details());
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public tahayarDTO tahayaroku(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.tahayaroku(cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void tahayar_kayit(tahayarDTO tahayarDTO)
	{
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			strategy.tahayar_kayit(tahayarDTO,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> tah_ayar_oku(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.tah_ayar_oku(cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public List<Map<String, Object>> tah_cek_kayit_aktar(String fisno, Integer tah_ted){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.tah_cek_kayit_aktar(fisno, tah_ted,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void cari_firma_adi_kayit(String fadi) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			strategy.cari_firma_adi_kayit(fadi,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public List<Map<String, Object>> hsppln_liste(){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.hsppln_liste(cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public int hesap_plani_kayit_adedi() {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.hesap_plani_kayit_adedi(cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void cari_kod_degis_hesap(String eskikod, String yenikod,String user) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			loglamaDTO.setEvrak("");
			loglamaDTO.setmESAJ("Kod Degistirme   Eski Kod:" + eskikod + " Yeni Kod:"+yenikod);
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, cariConnDetails);
			strategy.cari_kod_degis_hesap(eskikod,yenikod,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void cari_kod_degis_satirlar(String eskikod, String yenikod,String user) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			loglamaDTO.setEvrak("");
			loglamaDTO.setmESAJ("Kod Degistirme   Eski Kod:" + eskikod + " Yeni Kod:"+yenikod);
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, cariConnDetails);
			strategy.cari_kod_degis_satirlar(eskikod,yenikod,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}

	public void cari_kod_degis_tahsilat(String eskikod, String yenikod,String user) {
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			loglamaDTO.setEvrak("");
			loglamaDTO.setmESAJ("Kod Degistirme   Eski Kod:" + eskikod + " Yeni Kod:"+yenikod);
			loglamaDTO.setUser(user);
			loglamaRepository.log_kaydet(loglamaDTO, cariConnDetails);
			strategy.cari_kod_degis_tahsilat(eskikod,yenikod,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public List<Map<String, Object>> kasa_kontrol(String hesap, String t1){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.kasa_kontrol(hesap,t1,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	public List<Map<String, Object>> kasa_mizan(String kod, String ilktarih, String sontarih){
		try {
			String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
			ConnectionDetails cariConnDetails =  UserSessionManager.getUserSession(useremail, modulTipi.CARI_HESAP);
			return strategy.kasa_mizan(kod,ilktarih,sontarih ,cariConnDetails);
		} catch (ServiceException e) {
			throw new ServiceException(errorMessages(e));
		}
	}
	

	private String errorMessages(ServiceException e) {
		String originalMessage = e.getMessage();
		Throwable cause = e.getCause();
		String detailedMessage = originalMessage;
		if (cause != null) {
			detailedMessage += " - " + cause.getMessage();
		}
		return detailedMessage;
	}
}