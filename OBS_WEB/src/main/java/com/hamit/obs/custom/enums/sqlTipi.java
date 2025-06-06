package com.hamit.obs.custom.enums;


public enum sqlTipi {
	MSSQL("MS SQL"),
	MYSQL("MY SQL"),
	PGSQL("PG SQL");

	private final String value;

	sqlTipi(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static sqlTipi fromString(String text) {
		for (sqlTipi tip : sqlTipi.values()) {
			if (tip.getValue().equalsIgnoreCase(text.trim()))
				return tip;
		}
		throw new IllegalArgumentException("Geçersiz SqlTipi: " + text);
	}

	@Override
	public String toString() {
		return value;
	}
}
