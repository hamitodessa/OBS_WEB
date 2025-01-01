package com.hamit.obs.custom.yardimci;

public class sayiyiYaziyaCevir {

	public static String yaziyaCevir(String sayi, int kurusBasamak, String paraBirimi, String paraKurus, String diyez, String[] bb1, String[] bb2, String[] bb3, String nereden) {
	    String[] b1 = {"", "BİR", "İKİ", "ÜÇ", "DÖRT", "BEŞ", "ALTI", "YEDİ", "SEKİZ", "DOKUZ"};
	    String[] b2 = {"", "ON", "YİRMİ", "OTUZ", "KIRK", "ELLİ", "ALTMIŞ", "YETMİŞ", "SEKSEN", "DOKSAN"};
	    String[] b3 = {"", "YÜZ", "BİN", "MİLYON", "MİLYAR", "TRİLYON", "TRİLYAR"};

	    if (bb1 != null) b1 = bb1;
	    if (bb2 != null) b2 = bb2;
	    if (bb3 != null) b3 = bb3;

	    sayi = sayi.replace(",", ".");
	    String[] sayiParcalari = sayi.split("\\.");
	    String tamKisim = sayiParcalari[0];
	    String kurusKisim = sayiParcalari.length > 1 ? sayiParcalari[1] : "";

	    String tamYazi = yaziyaCevirHelper(tamKisim, b1, b2, b3);
	    String kurusYazi = kurusKisim.isEmpty() ? "" : kurusYaziyaCevir(kurusKisim, kurusBasamak, b1, b2, b3);

	    if (tamYazi.isEmpty()) paraBirimi = "";
	    if (kurusYazi.isEmpty()) paraKurus = "";

	    String sonuc = diyez + tamYazi + " " + paraBirimi + " " + kurusYazi + paraKurus + diyez;
	    return sonuc.trim();
	}

	private static String yaziyaCevirHelper(String sayi, String[] b1, String[] b2, String[] b3) {
	    StringBuilder sonuc = new StringBuilder();
	    int uzunluk = sayi.length();
	    int grupSayaci = 0;

	    for (int i = 0; i < uzunluk; i++) {
	        int basamak = Character.getNumericValue(sayi.charAt(uzunluk - 1 - i));
	        int pozisyon = i % 3;

	        if (pozisyon == 0 && grupSayaci > 0) {
	            sonuc.insert(0, b3[2 + grupSayaci] + " ");
	        }

	        if (pozisyon == 0 && basamak > 0) {
	            sonuc.insert(0, b1[basamak] + " ");
	        } else if (pozisyon == 1 && basamak > 0) {
	            sonuc.insert(0, b2[basamak] + " ");
	        } else if (pozisyon == 2) {
	            if (basamak == 1) {
	                sonuc.insert(0, b3[1] + " ");
	            } else if (basamak > 1) {
	                sonuc.insert(0, b1[basamak] + b3[1] + " ");
	            }
	        }

	        if (pozisyon == 2) grupSayaci++;
	    }
	    return sonuc.toString().trim();
	}

	private static String kurusYaziyaCevir(String kurus, int kurusBasamak, String[] b1, String[] b2, String[] b3) {
	    StringBuilder sonuc = new StringBuilder();
	    kurus = kurus.length() > kurusBasamak ? kurus.substring(0, kurusBasamak) : kurus;

	    for (int i = 0; i < kurus.length(); i++) {
	        int basamak = Character.getNumericValue(kurus.charAt(i));
	        if (i == 0 && kurusBasamak > 0) {
	            sonuc.append(b1[basamak]).append(" ");
	        } else if (i == 1 && kurusBasamak > 1) {
	            sonuc.append(b2[basamak]).append(" ");
	        } else if (i == 2 && kurusBasamak > 2) {
	            if (basamak == 1) {
	                sonuc.append(b3[1]).append(" ");
	            } else if (basamak > 1) {
	                sonuc.append(b1[basamak]).append(b3[1]).append(" ");
	            }
	        }
	    }
	    return sonuc.toString().trim();
	}
}
