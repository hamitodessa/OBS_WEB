package com.hamit.obs.service.user;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hamit.obs.model.user.User;
import com.hamit.obs.model.user.User_Shortcut;
import com.hamit.obs.repository.user.UserRepository;
import com.hamit.obs.repository.user.UserShortcutRepository;

@Service
public class UserShortcutService {

	@Autowired
	private UserShortcutRepository repo;
	@Autowired
	private UserRepository userRepo;

	public UserShortcutService(UserShortcutRepository repo, UserRepository userRepo) {
		this.repo = repo;
		this.userRepo = userRepo;
	}

	public Map<String, String> getShortcuts(Long userId) {
		// Map: SAVE -> S
		Map<String, String> m = new LinkedHashMap<>();
		for (User_Shortcut s : repo.findAllByUserId(userId)) {
			m.put(s.getActionCode(), s.getHotkey());
		}
		return m;
	}

	@Transactional
	public void upsert(Long userId, String actionCode, String hotkey) {
		String ac = normAction(actionCode);
		String hk = normHotkey(hotkey);

		if (!hk.matches("^[A-Z]$"))
			throw new IllegalArgumentException("Hotkey tek harf olmalı (A-Z).");

		User_Shortcut sc = repo.findByUserIdAndActionCode(userId, ac)
				.orElseGet(User_Shortcut::new);

		sc.setActionCode(ac);
		sc.setHotkey(hk);

		if (sc.getUser() == null) {
			User u = userRepo.findById(userId)
					.orElseThrow(() -> new RuntimeException("User bulunamadı: " + userId));
			sc.setUser(u);
		}

		repo.save(sc);
	}

	private String normAction(String s) {
		return (s == null) ? "" : s.trim().toUpperCase(Locale.ROOT);
	}

	private String normHotkey(String s) {
		return (s == null) ? "" : s.trim().toUpperCase(Locale.ROOT);
	}
}
