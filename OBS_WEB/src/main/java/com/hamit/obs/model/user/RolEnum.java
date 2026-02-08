package com.hamit.obs.model.user;

public enum  RolEnum {

	ADMIN,
	USER,
	MANAGER,
	GORUNTULEME;

	public static boolean isValidRole(String roleName) {
		for (RolEnum rol : RolEnum.values()) {
			if (rol.name().equals(roleName)) {
				return true;
			}
		}
		return false;
	}
}