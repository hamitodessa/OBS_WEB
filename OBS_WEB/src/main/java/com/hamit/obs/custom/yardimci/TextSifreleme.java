package com.hamit.obs.custom.yardimci;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
public class TextSifreleme {
	private static final String ALGORITHM = "AES";

	private static final String SECRET_KEY = "1234567890123456";

	public static String encrypt(String data)  {
		try {
			SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encryptedData = cipher.doFinal(data.getBytes());
			return Base64.getEncoder().encodeToString(encryptedData);
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public static String decrypt(String encryptedData)  {
		try {
			SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] decodedData = Base64.getDecoder().decode(encryptedData);
			byte[] originalData = cipher.doFinal(decodedData);
			return new String(originalData);
		} catch (Exception e) {
			return e.getMessage();
		}
	}
}