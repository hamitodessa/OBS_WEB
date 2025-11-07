package com.hamit.obs.mailcenter;

import com.hamit.obs.model.user.Email_Details;

public class MailCfgFactory {

	private MailCfgFactory() {}

    public static MailCenter fromEmailDetails(Email_Details ed, java.util.function.Function<String,String> decryptor) {
        if (ed == null) throw new IllegalArgumentException("Email_Details null");

        MailCenter.Cfg c = new MailCenter.Cfg();
        c.host     = trim(ed.getHost());
        c.port     = safePort(ed.getPort(), ed.getBssl() ? 465 : 587);
        c.user     = trim(ed.getHesap());
        c.pass     = decryptor.apply(ed.getSifre()); // TextSifreleme.decrypt(...)
        c.fromAddr = c.user;                         // güvenli varsayılan
        c.fromName = trim(ed.getGon_isim());
        c.starttls = Boolean.TRUE.equals(ed.getBtsl());
        c.ssl      = Boolean.TRUE.equals(ed.getBssl());

        // ikisi aynı anda true ise SSL’e öncelik ver
        if (c.ssl && c.starttls) {
            c.starttls = false;
            if (c.port == 587) c.port = 465;
        }
        return new MailCenter(c);
    }

    private static String trim(String s){ return s == null ? null : s.trim(); }
    private static int safePort(String s, int def){
        try { int p = Integer.parseInt(trim(s)); return p > 0 ? p : def; }
        catch (Exception ignore){ return def; }
    }
}
