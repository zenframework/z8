package org.zenframework.z8.server.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

	private static final String SecretKey = "#This-is-z8-secret-key!#"; // 24-bytes secret key
	private static final String CryptoCipherSpec = "TripleDES/CBC/PKCS5Padding";
	private static final String CryptoSecretKeySpec = "TripleDES";
	private static final String CryptoIv = "anc96lt1";

	public static final Crypto Default = getDefault();

	private final Cipher encryptCipher;
	private final Cipher decryptCipher;

	public Crypto(String cipherSpec, String secretKeySpec, String secretKey, String iv) {
		try {
			encryptCipher = getCipher(cipherSpec, secretKeySpec, secretKey, iv, Cipher.ENCRYPT_MODE);
			decryptCipher = getCipher(cipherSpec, secretKeySpec, secretKey, iv, Cipher.DECRYPT_MODE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Crypto(String cipherSpec, SecretKey secretKey, String iv) {
		try {
			encryptCipher = getCipher(cipherSpec, secretKey, iv, Cipher.ENCRYPT_MODE);
			decryptCipher = getCipher(cipherSpec, secretKey, iv, Cipher.DECRYPT_MODE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String encrypt(String data) {
		return Base64.getEncoder().encodeToString(encrypt(data.getBytes(StandardCharsets.UTF_8)));
	}

	public byte[] encrypt(byte[] data) {
		try {
			return encryptCipher.doFinal(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String decrypt(String data) {
		return new String(decrypt(Base64.getDecoder().decode(data.getBytes())));
	}

	public byte[] decrypt(byte[] data) {
		try {
			return decryptCipher.doFinal(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Crypto getDefault() {
		return new Crypto(CryptoCipherSpec, CryptoSecretKeySpec, SecretKey, CryptoIv);
	}

	private static Cipher getCipher(String cipherSpec, String secretKeySpec, String secretKey, String iv, int mode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		Cipher cipher = Cipher.getInstance(cipherSpec);
		cipher.init(mode, new SecretKeySpec(secretKey.getBytes(), secretKeySpec), new IvParameterSpec(iv.getBytes()));
		return cipher;
	}

	private static Cipher getCipher(String cipherSpec, SecretKey secretKey, String iv, int mode) {
		try {
			Cipher cipher = Cipher.getInstance(cipherSpec);
			cipher.init(mode, secretKey, new IvParameterSpec(iv.getBytes()));
			return cipher;
		} catch (Exception e) {
			throw new RuntimeException("Error initializing cipher", e);
		}
	}
}
