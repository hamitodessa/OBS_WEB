package com.hamit.obs.service.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hamit.obs.model.user.Gonderilmis_Mailler;
import com.hamit.obs.repository.user.IGidenRaporRepository;

@Service
public class GidenRaporService {

	@Autowired
	IGidenRaporRepository gidenRaporRepository;

	public Gonderilmis_Mailler savegonderilmisMailler(Gonderilmis_Mailler gonderilmisMailler) {
		return gidenRaporRepository.save(gonderilmisMailler);
	}

	public List<Gonderilmis_Mailler> gidenRaporListele(String user_email){
		return gidenRaporRepository.gidenRaporListele(user_email);
	}

	public void deletebyId(Long id) {
		gidenRaporRepository.deleteById(id);
	}
}