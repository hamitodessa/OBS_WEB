package com.hamit.obs.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hamit.obs.model.user.RolEnum;
import com.hamit.obs.model.user.Role;


public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(RolEnum name); // Enum kullanımı
    
}