package com.hamit.obs.repository.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hamit.obs.model.user.Gonderilmis_Mailler;

@Repository
public interface IGidenRaporRepository extends JpaRepository<Gonderilmis_Mailler, Long >{
	
   @Query("SELECT u FROM Gonderilmis_Mailler u WHERE u.user_email = :user_email")
   List<Gonderilmis_Mailler> gidenRaporListele(@Param("user_email") String user_email);
   
   @Query("SELECT DISTINCT alici FROM Gonderilmis_Mailler u WHERE u.user_email = :user_email")
   List<String> alicioku(@Param("user_email") String user_email);
}