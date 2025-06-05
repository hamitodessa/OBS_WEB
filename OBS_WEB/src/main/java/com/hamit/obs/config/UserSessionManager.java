package com.hamit.obs.config;
import java.util.HashMap;
import java.util.Map;

import com.hamit.obs.connection.ConnectionDetails;
import com.hamit.obs.custom.enums.modulTipi;

public class UserSessionManager {
	private static Map<String, Map<String, ConnectionDetails>> userSessions = new HashMap<>();

	public static void addUserSession(String username, modulTipi modulname, ConnectionDetails connectionDetails) {
		userSessions.putIfAbsent(username, new HashMap<>());
		userSessions.get(username).put(modulname.getDbValue(), connectionDetails);
	}

	public static ConnectionDetails getUserSession(String username, modulTipi modulname) {
		return userSessions.getOrDefault(username, new HashMap<>()).get(modulname.getDbValue());
	}

	public static void removeUserByModul(String username, modulTipi modulname) {
		Map<String, ConnectionDetails> userModules = userSessions.get(username);
		if (userModules != null) {
			userModules.remove(modulname.getDbValue());
			if (userModules.isEmpty())
				userSessions.remove(username);
		}
	}

	public static void removeUserSessionsByUsername(String username) {
		userSessions.remove(username);
	}

	public static void clearAllSessions() {
		userSessions.clear();
	}
}