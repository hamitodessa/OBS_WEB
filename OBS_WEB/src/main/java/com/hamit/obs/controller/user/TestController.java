package com.hamit.obs.controller.user;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test/roles")
    public Map<String, Object> testRoles() {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            out.put("authIsNull", auth == null);
            if (auth == null) return out;

            out.put("name", auth.getName());
            out.put("authenticated", auth.isAuthenticated());
            out.put("authorities", auth.getAuthorities()); // JSON'a basar
            out.put("class", auth.getClass().getName());
            return out;

        } catch (Exception e) {
            out.put("ERROR", e.getClass().getName());
            out.put("message", e.getMessage());
            return out;
        }
    }
}
