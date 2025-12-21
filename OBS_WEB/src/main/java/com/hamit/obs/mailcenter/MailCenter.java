package com.hamit.obs.mailcenter;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.util.ByteArrayDataSource;

public class MailCenter {
	
	public static final class Cfg {
        public String host;
        public int    port;
        public boolean auth = true;
        public boolean starttls;   // 587 ise true
        public boolean ssl;        // 465 ise true
        public String user;        // tam e-posta
        public String pass;        // çözülmüş (decrypt edilmiş) şifre
        public String fromAddr;    // genelde user
        public String fromName;    // görünen ad
        public int connTimeoutMs = 10000;
        public int readTimeoutMs  = 15000;
        public String tlsProtocols = "TLSv1.2";
    }

    private final Cfg cfg;
    private final Session session;

    public MailCenter(Cfg cfg) {
        this.cfg = Objects.requireNonNull(cfg, "cfg");
        Properties p = new Properties();
        p.put("mail.transport.protocol", "smtp");
        p.put("mail.smtp.host", cfg.host);
        p.put("mail.smtp.port", String.valueOf(cfg.port));
        p.put("mail.smtp.auth", String.valueOf(cfg.auth));
        p.put("mail.smtp.connectiontimeout", String.valueOf(cfg.connTimeoutMs));
        p.put("mail.smtp.timeout",          String.valueOf(cfg.readTimeoutMs));
        p.put("mail.smtp.ssl.protocols",    cfg.tlsProtocols);

        if (cfg.ssl) {
            p.put("mail.smtp.ssl.enable", "true");
            p.remove("mail.smtp.starttls.enable");
        } else {
            p.put("mail.smtp.starttls.enable", String.valueOf(cfg.starttls));
            if (cfg.starttls) p.put("mail.smtp.starttls.required", "true");
            p.remove("mail.smtp.ssl.enable");
        }

        this.session = Session.getInstance(p, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(cfg.user, cfg.pass);
            }
        });
    }

    public void sendWithOptionalAttachment(
            String to, String cc, String subject, String bodyUtf8, List<DataSource> atts
    ) throws MessagingException, UnsupportedEncodingException {

        MimeMessage m = new MimeMessage(session);
        if (cfg.fromName != null && !cfg.fromName.isBlank())
            m.setFrom(new InternetAddress(cfg.fromAddr, cfg.fromName));
        else
            m.setFrom(new InternetAddress(cfg.fromAddr));

        m.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        if (cc != null && !cc.isBlank())
            m.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
        m.setSubject(subject, "UTF-8");
        m.setSentDate(new Date());
        m.setHeader("X-Mailer", "OBS");

        MimeBodyPart text = new MimeBodyPart();
        text.setText(bodyUtf8, "UTF-8", "plain");

        Multipart mp = new MimeMultipart();
        mp.addBodyPart(text);

        if (atts != null) {
        	for (DataSource ds : atts) {
                MimeBodyPart att = new MimeBodyPart();
                att.setDataHandler(new DataHandler(ds));
                String name = ds.getName();
                if (name == null || name.isBlank()) {
                    name = "attachment";
                }
                att.setFileName(MimeUtility.encodeText(name, "UTF-8", null));
                mp.addBodyPart(att);
            }
        }

        m.setContent(mp);
        m.saveChanges();
        Transport.send(m);
    }

    public static DataSource fileDs(String path) {
        return new FileDataSource(path);
    }
    public static DataSource bytesDs(byte[] data, String mime, String fileName) {
        var ds = new ByteArrayDataSource(data, mime);
        ds.setName(fileName);
        return ds;
    }
}