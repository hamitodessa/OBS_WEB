package com.hamit.obs.repository.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hamit.obs.model.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.admin_hesap) = LOWER(:email) ORDER BY LOWER(u.admin_hesap)")
    List<User> findByUserAdminHesap(@Param("email") String email);
}