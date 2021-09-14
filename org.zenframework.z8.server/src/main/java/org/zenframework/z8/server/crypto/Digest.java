package org.zenframework.z8.server.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.StringUtils;

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
		MessageDigest messageDigest = newInstance(algorithm);
		messageDigest.update(value.getBytes());
		return toString(messageDigest);
	}

	private static String digest(binary value, String algorithm) {
		MessageDigest messageDigest = newInstance(algorithm);
		try {
			messageDigest.update(IOUtils.read(value.get()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return toString(messageDigest);
	}

	private static String digest(binary[] values, String algorithm) {
		MessageDigest messageDigest = newInstance(algorithm);
		try {
			for (binary b : values)
				messageDigest.update(IOUtils.read(b.get()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return toString(messageDigest);
	}

	private static MessageDigest newInstance(String algorithm) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(algorithm);
			messageDigest.reset();
			return messageDigest;
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static String toString(MessageDigest messageDigest) {
		byte[] digest = messageDigest.digest();
		return StringUtils.padLeft(new BigInteger(1, digest).toString(16), 32, '0');
	}
}
