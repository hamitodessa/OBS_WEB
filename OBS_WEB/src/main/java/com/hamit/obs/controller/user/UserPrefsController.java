package com.hamit.obs.controller.user;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.user.UserService;
import com.hamit.obs.service.user.UserShortcutService;

import lombok.Data;

@RestController

public class UserPrefsController {

	@Autowired
	private UserService userService;
	@Autowired
    private UserShortcutService shortcutService;

    public UserPrefsController(UserShortcutService service) {
        this.shortcutService = service;
    }

    @Data
    public static class ShortcutReq {
        private String actionCode; // SAVE
        private String hotkey;     // S
    }

     
    @GetMapping("user/shortcut")
    public Model shortcutsPage(Model model) {
        try {
            Long userId = userService.getCurrentUser().getId();

            Map<String, String> sc = shortcutService.getShortcuts(userId);

            // defaultlar (db boşsa)
            sc.putIfAbsent("SAVE", "S");
            sc.putIfAbsent("DELETE", "D");
            sc.putIfAbsent("REFRESH", "R");
            sc.putIfAbsent("NEW", "N");
            sc.putIfAbsent("SEARCH", "F");

            model.addAttribute("shortcuts", sc);
            return model; 
        } catch (ServiceException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return model;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Hata: " + e.getMessage());
            return model;
        }
    }

    
    @PostMapping("user/shortcutsave")
    public ResponseEntity<?> save(@RequestBody ShortcutReq req, Authentication auth) {
        Long userId = userService.getCurrentUser().getId();
        shortcutService.upsert(userId, req.getActionCode(), req.getHotkey());
        return ResponseEntity.ok().build();
    }
}
