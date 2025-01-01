package com.hamit.obs.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hamit.obs.model.user.Etiket_Ayarlari;

@Repository
public interface IEtiketAyarRepository extends JpaRepository<Etiket_Ayarlari, Long >{
	Etiket_Ayarlari findByUserId(Long userId);
}