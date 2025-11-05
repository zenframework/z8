package org.zenframework.z8.server.reports.poi;

public class Util {

	private Util() {}

	public static String columnToString(int n) {
		if (n == 0)
			return "A";

		StringBuilder str = new StringBuilder(10);

		while (n >= 0) {
			str.insert(0, (char) ('A' + n % 26));
			n = n / 26 - 1;
		}

		return str.toString();
	}

	public static int columnToInt(String column) {
		int n = 0;

		for (int i = 0; i < column.length(); i++)
			n = n * 26 + (column.charAt(i) - 'A' + 1);

		return n - 1;
	}
}
