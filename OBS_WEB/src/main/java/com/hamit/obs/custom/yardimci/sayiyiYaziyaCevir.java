package com.hamit.obs.custom.yardimci;

public class sayiyiYaziyaCevir {

	public static String yaziyaCevir(String sayi, int kurusbasamak,	String parabirimi, String parakurus, String diyez, String[] bb1, String[] bb2, String[] bb3,String nerden) 
	{
		String[] b1 = {"", "BİR", "İKİ", "ÜÇ",  "DÖRT", "BEŞ", "ALTI", "YEDİ", "SEKİZ", "DOKUZ"};
		String[] b2 = {"", "ON", "YİRMİ", "OTUZ", "KIRK", "ELLİ", "ALTMIŞ", "YETMİŞ", "SEKSEN", "DOKSAN"};
		String[] b3 = {"", "YÜZ", "BİN", "MİLYON", "MİLYAR", "TRİLYON", "TRİLYAR"};
		if (bb1 != null)
			b1 = bb1;// farklı dil kullanımı yada farklı yazım biçimi için
		if (bb2 != null)
			b2 = bb2; // farklı dil kullanımı
		if (bb3 != null)
			b3 = bb3;// farklı dil kullanımı

		String say1, say2 = ""; // say1 virgül öncesi, say2 kuruş bölümü
		String sonuc = "";

		sayi = sayi.replace(",", "."); //virgül noktaya çevrilir

		if (sayi.indexOf(".") > 0) { // nokta varsa (kuruş)
			say1 = sayi.substring(0, sayi.indexOf(".")); // virgül öncesi
			say2 = sayi.substring(sayi.indexOf("."), sayi.length()); // virgül sonrası, kuruş
		} 
		else
			say1 = sayi; // kuruş yok

		char[] rk = say1.toCharArray(); // rakamlara ayırma

		String son;
		int w = 1; // işlenen basamak
		int sonaekle = 0; // binler on binler yüzbinler vs. için sona bin (milyon,trilyon...) eklenecek mi?
		int kac = rk.length; // kaç rakam var?
		int sonint; // işlenen basamağın rakamsal değeri
		int uclubasamak = 0; // hangi basamakta (birler onlar yüzler gibi)
		int artan = 0; // binler milyonlar milyarlar gibi artışları yapar
		String gecici;
		if (kac > 0) { // virgül öncesinde rakam var mı?
			for (int i = 0; i < kac; i++) {
				son = String.valueOf(rk[kac - 1 - i]); // son karakterden başlayarak çözümleme yapılır.
				sonint = Integer.parseInt(son); // işlenen rakam
				if (w == 1)
					sonuc = b1[sonint] + sonuc;// birinci basamak bulunuyor
				else if (w == 2)
					sonuc = b2[sonint] + sonuc;// ikinci basamak
				else if (w == 3) { // 3. basamak
					if (sonint == 1)
						sonuc = b3[1] + sonuc;
					else if (sonint > 1)
						sonuc = b1[sonint] + b3[1] + sonuc;
					uclubasamak++;
				}
				if (w > 3) { // 3. basamaktan sonraki işlemler
					if (uclubasamak == 1) {
						if (sonint > 0) {
							sonuc = b1[sonint] + b3[2 + artan] + sonuc;
							if (artan == 0) { // birbin yazmasını engelle
								if (kac - 1 == i)
									sonuc = sonuc.replace(b1[1] + b3[2], b3[2]);// 11000 yazılışını düzeltme
							}
							sonaekle = 1; // sona bin eklendi
						} else
							sonaekle = 0;
						uclubasamak++;
					} else if (uclubasamak == 2)
						{
						if (sonint > 0) {
							if (sonaekle > 0) {
								sonuc = b2[sonint] + sonuc;
								sonaekle++;
							} else {
								sonuc = b2[sonint] + b3[2 + artan] + sonuc;
								sonaekle++;
							}
						}
						uclubasamak++;
					} else if (uclubasamak == 3) {
						if (sonint > 0) {
							if (sonint == 1)
								gecici = b3[1];
							else
								gecici = b1[sonint] + b3[1];
							if (sonaekle == 0)
								gecici = gecici + b3[2 + artan];
							sonuc = gecici + sonuc;
						}
						uclubasamak = 1;
						artan++;
					}
				}
				w++; // işlenen basamak
			}
		} // if(kac>0)
		if ("".equals(sonuc))
			parabirimi = "";// virgül öncesi sayı yoksa para birimi yazma
		say2 = say2.replace(".", "");
		String kurus = "";
		if (!"".equals(say2)) { // kuruş hanesi varsa
			if (kurusbasamak > 3)
				kurusbasamak = 3;// 3 basamakla sınırlı
			if (say2.length() > kurusbasamak)
				say2 = say2.substring(0, kurusbasamak);// belirlenen basamak kadar rakam yazılır
			char[] kurusrk = say2.toCharArray(); // rakamlara ayırma
			kac = kurusrk.length; // kaç rakam var?
			w = 1;
			for (int i = 0; i < kac; i++) { // kuruş hesabı
				son = String.valueOf(kurusrk[kac - 1 - i]); // son karakterden başlayarak çözümleme yapılır.
				sonint = Integer.parseInt(son); // işlenen rakam
				if (w == 1) { // birinci basamak
					if (kurusbasamak > 0)
						kurus = b1[sonint] + kurus;
				} else if (w == 2) { // ikinci basamak
					if (kurusbasamak > 1)
						kurus = b2[sonint] + kurus;
				} else if (w == 3) { // 3. basamak
					if (kurusbasamak > 2) {
						if (sonint == 1)
							kurus = b3[1] + kurus;// 'biryüz' ü engeller
						else if (sonint > 1)
							kurus = b1[sonint] + b3[1] + kurus;
					}
				}
				w++;
			}
			if ("".equals(kurus))
				parakurus = "";// virgül öncesi sayı yoksa para birimi yazma
			else
				kurus = kurus + " "; // + "'DİR.";
			kurus = kurus + parakurus + (kurus.equals("") ? "'DİR." : "'DUR." ) ; // kuruş hanesine 'kuruş' kelimesi ekler
		}
		if(! nerden.equals("SAYIILE"))
			sonuc = diyez + sonuc + " " + parabirimi + " " + kurus + diyez;
		return sonuc;
	}
}