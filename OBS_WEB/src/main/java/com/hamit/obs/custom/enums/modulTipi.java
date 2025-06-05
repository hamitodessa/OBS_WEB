package com.hamit.obs.custom.enums;

public enum modulTipi {

	CARI_HESAP("Cari Hesap"),
    KUR("Kur"),
    ADRES("Adres"),
    KAMBIYO("Kambiyo"),
    FATURA("Fatura"),
    KERESTE("Kereste"),
    SMS("Sms"),
    GUNLUK("Gunluk");

    private final String dbValue;

    modulTipi(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static modulTipi fromDbValue(String value) {
        for (modulTipi tip : values()) {
            if (tip.getDbValue().equalsIgnoreCase(value)) {
                return tip;
            }
        }
        throw new IllegalArgumentException("Bilinmeyen modul tipi: " + value);
    }
}
