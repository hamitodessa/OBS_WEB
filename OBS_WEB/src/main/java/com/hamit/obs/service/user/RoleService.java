package com.hamit.obs.service.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hamit.obs.model.user.Role;
import com.hamit.obs.repository.user.RoleRepository;

import java.util.HashSet;
import java.util.Set;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Set<Role> getAllRoles() {
        return new HashSet<>(roleRepository.findAll());
    }

    public Role findRoleById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }

    public void saveRole(Role role) {
        roleRepository.save(role);
    }
    
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
}