package org.zenframework.z8.server.crypto;

import org.zenframework.z8.server.types.string;

/**
 * @deprecated
 * Use {@link Digest#md5(String)} instead
 */

@Deprecated
public class MD5 {

	public static String hex(String value) {
		return Digest.md5(value);
	}

	public static string z8_hex(string value) {
		return Digest.z8_md5(value);
	}

}
