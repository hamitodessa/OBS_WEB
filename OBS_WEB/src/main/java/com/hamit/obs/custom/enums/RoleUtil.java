package com.hamit.obs.custom.enums;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hamit.obs.model.user.RolEnum;
import com.hamit.obs.model.user.User;

public final class RoleUtil {

    private RoleUtil() {}

    private static String normalize(String authority) {
        if (authority == null) return null;
        return authority.startsWith("ROLE_") ? authority.substring(5) : authority; // ROLE_ADMIN -> ADMIN
    }

    public static RolEnum resolveRolEnum(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return null;

        for (GrantedAuthority ga : auth.getAuthorities()) {
            String role = normalize(ga.getAuthority()); // ADMIN / USER / MANAGER
            if (RolEnum.isValidRole(role)) {
                return RolEnum.valueOf(role);
            }
        }
        return null;
    }

    public static boolean hasRole(Authentication auth, RolEnum rol) {
        if (auth == null || rol == null) return false;

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(RoleUtil::normalize)
                .anyMatch(r -> rol.name().equals(r));
    }
    
    public static boolean durumRole( RolEnum rol) {
        return hasRole(SecurityContextHolder.getContext().getAuthentication(), rol);
    }

    public static RolEnum resolveRolEnum(User user) {
        if (user == null || user.getRoles() == null) return null;

        if (user.getRoles().stream().anyMatch(r -> r.getName() == RolEnum.ADMIN))   return RolEnum.ADMIN;
        if (user.getRoles().stream().anyMatch(r -> r.getName() == RolEnum.MANAGER)) return RolEnum.MANAGER;
        if (user.getRoles().stream().anyMatch(r -> r.getName() == RolEnum.USER))    return RolEnum.USER;

        return null;
    }
}
