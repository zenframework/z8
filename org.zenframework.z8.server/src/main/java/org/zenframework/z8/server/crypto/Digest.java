package org.zenframework.z8.server.crypto;

import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.StringUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MessageDigest algorithms:
 * MD5
 * SHA-1
 * SHA-256
 */
public class Digest {

	public static string z8_md5(string value) {
		return new string(md5(value.get()));
	}

	public static String md5(String value) {
		return digest(value, "MD5");
	}

	public static string z8_sha256(byte[] bytes) {
		return new string(sha256(bytes));
	}

	public static String sha256(byte[] value) {
		return digest(value, "SHA-256");
	}

	public static string z8_sha1(string value) {
		return new string(sha1(value.get()));
	}

	public static String sha1(String value) {
		return digest(value, "SHA-1");
	}

	private static String digest(String value, String algorithm) {
		return digest(value.getBytes(), algorithm);
	}

	private static String digest(byte[] bytes, String algorithm) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(algorithm);
			messageDigest.reset();
			messageDigest.update(bytes);
			byte[] digest = messageDigest.digest();
			return StringUtils.padLeft(new BigInteger(1, digest).toString(16), 32, '0');
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
