package org.zenframework.z8.server.crypto;

import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
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

	public static string z8_sha256(string value) {
		return new string(sha256(value.get()));
	}

	public static String sha256(String value) {
		return digest(value, "SHA-256");
	}

	public static String sha256Binary(binary value) {
		return digest(value, "SHA-256");
	}

	public static String sha256BinaryArray(binary[] values) {
		return digest(values, "SHA-256");
	}

	public static string z8_sha1(string value) {
		return new string(sha1(value.get()));
	}

	public static String sha1(String value) {
		return digest(value, "SHA-1");
	}

	private static String digest(String value, String algorithm) {
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

	private static String digest(binary value, String algorithm) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(algorithm);
			messageDigest.reset();
			messageDigest = digestStreamReading(value, messageDigest);
			byte[] digest = messageDigest.digest();
			return StringUtils.padLeft(new BigInteger(1, digest).toString(16), 32, '0');
		} catch(NoSuchAlgorithmException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String digest(binary[] values, String algorithm) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(algorithm);
			messageDigest.reset();
			for (binary b : values) {
				digestStreamReading(b, messageDigest);
			}
			byte[] digest = messageDigest.digest();
			return StringUtils.padLeft(new BigInteger(1, digest).toString(16), 32, '0');
		} catch(NoSuchAlgorithmException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static MessageDigest digestStreamReading(binary value, MessageDigest messageDigest) throws IOException {
		InputStream is = value.get();
		byte b;
		while ((b = (byte) is.read()) != -1) {
			messageDigest.update(b);
		}
		return messageDigest;
	}

}
