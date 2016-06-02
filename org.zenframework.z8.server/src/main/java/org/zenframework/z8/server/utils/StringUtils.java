package org.zenframework.z8.server.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class StringUtils {

	static public String unescapeJava(String string) {
		Properties properties = new Properties();
		try {
			properties.load(new StringReader("key=" + string));
		} catch (IOException e) {
			return string;
		}
		return properties.getProperty("key");
	}

	public static String padLeft(String str, int size, char padChar) {
		int pads = size - str.length();
		return pads > 0 ? padding(pads, padChar).concat(str) : str;
	}

	public static String padRight(String str, int size, char padChar) {
		int pads = size - str.length();
		return pads > 0 ? str.concat(padding(pads, padChar)) : str;
	}

	public static List<String> asList(String str, String delimeter) {
		String[] array = str.split(delimeter);
		List<String> list = new ArrayList<String>(array.length);
		for (String s : array) {
			list.add(s.trim());
		}
		return list;
	}

	private static String padding(int repeat, char padChar) {
		final char[] buf = new char[repeat];

		for (int i = 0; i < buf.length; i++)
			buf[i] = padChar;

		return new String(buf);
	}

}
