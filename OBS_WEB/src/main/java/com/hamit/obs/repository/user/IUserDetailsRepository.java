package com.hamit.obs.repository.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.hamit.obs.model.user.User_Details;

@Repository
public interface IUserDetailsRepository extends JpaRepository<User_Details, Long > {
	@Query("SELECT u FROM User_Details u WHERE LOWER(u.user_modul) = LOWER(:userModul) AND LOWER(u.email) = LOWER(:email) AND u.izinlimi = true  order by calisanmi desc ")
	List<User_Details> findByUserModulAndEmail(@Param("userModul") String userModul, @Param("email") String email);

	@Query("SELECT u FROM User_Details u WHERE LOWER(u.user_modul) = LOWER(:userModul) AND LOWER(u.email) = LOWER(:email) order by calisanmi desc ")
	List<User_Details> findByUserModulAndEmailAdmin(@Param("userModul") String userModul, @Param("email") String email);

	
	@Query("SELECT u FROM User_Details u WHERE LOWER(u.user_modul) = LOWER(:userModul) AND LOWER(u.email) = LOWER(:email) AND u.calisanmi = true ")
	List<User_Details> user_Details_Modul(@Param("userModul") String userModul, @Param("email") String email);

	@Query("SELECT u FROM User_Details u WHERE LOWER(u.user_modul) = LOWER(:userModul) AND LOWER(u.email) = LOWER(:email) ")
	List<User_Details> izinlimiKontrol(@Param("userModul") String userModul, @Param("email") String email);


	@Query("SELECT u.hangi_sql FROM User_Details u WHERE u.user_modul = :userModul AND u.email = :email AND u.calisanmi = true")
	String findHangiSQLByUserId(@Param("userModul") String userModul, @Param("email") String email);


	@Modifying
	@Transactional
	@Query("UPDATE User_Details u SET u.calisanmi = :userCalisanmi WHERE u.user_modul = :userModul and u.email = :userEmail")
	void updateUserDetailsCalisanmiNulle(@Param("userModul") String userModul, 
			@Param("userEmail") String userEmail ,@Param("userCalisanmi") boolean userCalisanmi);

}