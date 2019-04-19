package org.zenframework.z8.server.crypto;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.StringUtils;

public class MD5 {
	public static String hex(String value) {
		MessageDigest messageDigest = null;

		try {
			messageDigest = MessageDigest.getInstance("MD5");
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
}
