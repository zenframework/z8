package org.zenframework.z8.server.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class StringUtils {
	private static final String[] charTable = new String[65536];

	static {
		charTable['А'] = "A";
		charTable['Б'] = "B";
		charTable['В'] = "V";
		charTable['Г'] = "G";
		charTable['Д'] = "D";
		charTable['Е'] = "E";
		charTable['Ё'] = "E";
		charTable['Ж'] = "Z";
		charTable['З'] = "Z";
		charTable['И'] = "I";
		charTable['Й'] = "I";
		charTable['К'] = "K";
		charTable['Л'] = "L";
		charTable['М'] = "M";
		charTable['Н'] = "N";
		charTable['О'] = "O";
		charTable['П'] = "P";
		charTable['Р'] = "R";
		charTable['С'] = "S";
		charTable['Т'] = "T";
		charTable['У'] = "U";
		charTable['Ф'] = "F";
		charTable['Х'] = "H";
		charTable['Ц'] = "C";
		charTable['Ч'] = "C";
		charTable['Ш'] = "S";
		charTable['Щ'] = "S";
		charTable['Ъ'] = "";
		charTable['Ы'] = "Y";
		charTable['Ь'] = "";
		charTable['Э'] = "E";
		charTable['Ю'] = "U";
		charTable['Я'] = "Y";
		charTable['а'] = "a";
		charTable['б'] = "b";
		charTable['в'] = "v";
		charTable['г'] = "g";
		charTable['д'] = "d";
		charTable['е'] = "e";
		charTable['ё'] = "e";
		charTable['ж'] = "z";
		charTable['з'] = "z";
		charTable['и'] = "i";
		charTable['й'] = "i";
		charTable['к'] = "k";
		charTable['л'] = "l";
		charTable['м'] = "m";
		charTable['н'] = "n";
		charTable['о'] = "o";
		charTable['п'] = "p";
		charTable['р'] = "r";
		charTable['с'] = "s";
		charTable['т'] = "t";
		charTable['у'] = "u";
		charTable['ф'] = "f";
		charTable['х'] = "h";
		charTable['ц'] = "c";
		charTable['ч'] = "c";
		charTable['ш'] = "s";
		charTable['щ'] = "s";
		charTable['ъ'] = "";
		charTable['ы'] = "y";
		charTable['ь'] = "";
		charTable['э'] = "e";
		charTable['ю'] = "u";
		charTable['я'] = "y";
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static int indexOfAny(String str, int position, char[] searchChars) {
		if(isEmpty(str) || ArrayUtils.isEmpty(searchChars))
			return -1;

		for(int i = position; i < str.length(); i++) {
			char ch = str.charAt(i);
			for(int j = 0; j < searchChars.length; j++) {
				if(searchChars[j] == ch)
					return i;
			}
		}
		return -1;
	}

	public static int indexOfAny(String str, char[] searchChars) {
		return indexOfAny(str, 0, searchChars);
	}

	public static int indexOfAny(String str, String searchChars) {
		return indexOfAny(str, searchChars.toCharArray());
	}

	public static int indexOfAny(String str, int position, String searchChars) {
		return indexOfAny(str, position, searchChars.toCharArray());
	}

	static public String unescapeJava(String string) {
		Properties properties = new Properties();
		try {
			properties.load(new StringReader("key=" + string));
		} catch(IOException e) {
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
		for(String s : array) {
			list.add(s.trim());
		}
		return list;
	}

	private static String padding(int repeat, char padChar) {
		final char[] buf = new char[repeat];

		for(int i = 0; i < buf.length; i++)
			buf[i] = padChar;

		return new String(buf);
	}

	static public byte[] charsToBytes(char[] chars) {
		if(chars == null)
			return null;

		byte[] bytes = new byte[chars.length * 2];

		for(int i = 0; i < chars.length; i++) {
			int ch = chars[i];
			bytes[i * 2] = (byte)(ch >>> 8);
			bytes[i * 2 + 1] = (byte)ch;
		}

		return bytes;
	}

	static public char[] bytesToChars(byte[] bytes) {
		if(bytes.length % 2 != 0)
			throw new RuntimeException("StringUtils.bytesToChars: wrong byte array length");

		char[] chars = new char[bytes.length / 2];

		for(int i = 0; i < bytes.length; i += 2) {
			int byte1 = bytes[i];
			int byte2 = bytes[i + 1];
			chars[i / 2] = (char)((byte1 << 8) + byte2);
		}

		return chars;
	}

	public static String translit(String value) {
		return translit(value, 0);
	}

	public static String translit(String value, int maxLength) {
		char chars[] = value.toCharArray();
		StringBuffer sb = new StringBuffer(maxLength != 0 ? maxLength : value.length());

		for(int i = 0; i < chars.length; i++) {
			char ch = chars[i];
			String replacement = charTable[ch];
			sb.append(replacement != null ? replacement : ch);

			if(sb.length() == maxLength)
				break;
		}
		return sb.toString();
	}
	
	public static boolean containsIgnoreCase(Collection<String> c, String str) {
		if (str == null)
			return c.contains(str);
		str = str.toLowerCase();
		for (String e : c)
			if (e != null && e.toLowerCase().equals(str))
				return true;
		return false;
	}

	public static String concat(String separator, String... elements) {
		StringBuilder str = new StringBuilder();
		for (String el : elements)
			str.append(el).append(separator);
		if (str.length() > 0)
			str.setLength(str.length() - separator.length());
		return str.toString();
	}

}