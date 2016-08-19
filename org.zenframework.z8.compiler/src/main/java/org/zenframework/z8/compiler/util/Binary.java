package org.zenframework.z8.compiler.util;

public class Binary {
	private byte[] bytes;

	public Binary(byte[] bytes) {
		this.bytes = bytes;
	}

	public int size() {
		return bytes.length;
	}

	public byte[] getBytes() {
		return bytes;
	}

	@Override
	public String toString() {
		if(bytes.length == 0) {
			return null;
		}

		String str = "new byte[] {";

		for(int i = 0; i < bytes.length; i++) {
			str += (i == 0 ? "" : ", ") + Byte.toString(bytes[i]);
		}

		str += "}";

		return str;
	}

	public String toShortString() {
		String str = "{";

		for(int i = 0; i < bytes.length; i++) {
			str += (i == 0 ? "" : ", ") + Byte.toString(bytes[i]);
		}

		str += "}";

		return str;
	}
}
