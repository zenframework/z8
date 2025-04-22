package org.zenframework.z8.server.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.utils.IOUtils;

public class Crypto {

	private static final String SecretKey = "#This-is-Z8-secret-key!#"; // 24-bytes secret key

	private static final String KeyStoreType = "JCEKS";
	private static final String DefaultSecretKeyAlias = "Z8";

	private static final File KeyStorePath = ServerConfig.keystorePath();
	private static final String KeyStorePassword = ServerConfig.keystorePassword();

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
		String cipherSpec = ServerConfig.cryptoCipherSpec();
		String secretKeySpec = ServerConfig.cryptoSecretKeySpec();
		String iv = ServerConfig.cryptoIv();
		try {
			if(ServerConfig.keystoreUseCustom())
				return new Crypto(cipherSpec, loadSecretKey(), iv);
			return new Crypto(cipherSpec, secretKeySpec, SecretKey, iv);
		} catch (Exception e) {
			Trace.logError("Can't initialize default cipher", e);
			return null;
		}
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

	private static SecretKey loadSecretKey() {
		FileInputStream fis = null;
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStoreType);
			fis = new FileInputStream(KeyStorePath);
			keyStore.load(fis, KeyStorePassword.toCharArray());
			return (SecretKey) keyStore.getKey(DefaultSecretKeyAlias, KeyStorePassword.toCharArray());
		} catch (Exception e) {
			throw new RuntimeException("Failed to load custom secret key from keystore", e);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}
}
