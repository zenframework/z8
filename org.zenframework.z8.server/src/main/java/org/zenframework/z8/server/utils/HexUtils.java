package org.zenframework.z8.server.utils;

import org.zenframework.z8.server.types.string;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MessageDigest algorithms:
 * MD5
 * SHA-1
 * SHA-256
 */
public class HexUtils {

	public static String hex(String value) {
		return hexWithAlgorithm(value, "MD5");
	}

	public static String sha256hex(String value) {
		return hexWithAlgorithm(value, "SHA-256");
	}

	public static String sha1hex(String value) {
		return hexWithAlgorithm(value, "SHA-1");
	}

	private static String hexWithAlgorithm(String value, String algorithm) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(algorithm);
			messageDigest.reset();
			messageDigest.update(value.getBytes());
			byte[] digest = messageDigest.digest();
			return StringUtils.padLeft(new BigInteger(1, digest).toString(16), 32, '0');
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static string z8_hex(string value) {
		return new string(hex(value.get()));
	}

	public static string z8_sha256hex(string value) {
		return new string(sha256hex(value.get()));
	}

	public static string z8_sha1hex(string value) {
		return new string(sha1hex(value.get()));
	}
}
