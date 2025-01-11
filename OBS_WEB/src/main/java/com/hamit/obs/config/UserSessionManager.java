package com.hamit.obs.config;
import java.util.HashMap;
import java.util.Map;

import com.hamit.obs.connection.ConnectionDetails;

public class UserSessionManager {
	private static Map<String, Map<String, ConnectionDetails>> userSessions = new HashMap<>();

	public static void addUserSession(String username, String modulname, ConnectionDetails connectionDetails) {
		userSessions.putIfAbsent(username, new HashMap<>());
		userSessions.get(username).put(modulname, connectionDetails);
	}

	public static ConnectionDetails getUserSession(String username, String modulname) {
		return userSessions.getOrDefault(username, new HashMap<>()).get(modulname);
	}

	public static void removeUserByModul(String username, String modulname) {
		Map<String, ConnectionDetails> userModules = userSessions.get(username);
		if (userModules != null) {
			userModules.remove(modulname);
			if (userModules.isEmpty()) {
				userSessions.remove(username);
			}
		}
	}

	public static void removeUserSessionsByUsername(String username) {
		userSessions.remove(username);
	}

	public static void clearAllSessions() {
		userSessions.clear();
	}
}