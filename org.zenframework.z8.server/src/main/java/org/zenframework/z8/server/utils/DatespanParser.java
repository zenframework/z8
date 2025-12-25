package org.zenframework.z8.server.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatespanParser {

	private static final Pattern PATTERN = Pattern.compile("(\\d+)\\s*(min|мин|[mмwнdдhч])");

	public static long parseToMilliseconds(String input) {
		if (input == null || input.trim().isEmpty())
			return 0;

		long totalMillis = 0;
		String normalized = input.trim().toLowerCase();

		Matcher matcher = PATTERN.matcher(normalized);

		while (matcher.find()) {
			int value = Integer.parseInt(matcher.group(1));
			String modifier = matcher.group(2);

			long millis = parseModifier(value, modifier);
			totalMillis += millis;
		}

		return totalMillis;
	}

	private static long parseModifier(int value, String modifier) {
		switch (modifier) {
		case "m":
		case "м":
			return value * 30L * 24 * 60 * 60 * 1000;

		case "w":
		case "н":
			return value * 7L * 24 * 60 * 60 * 1000;

		case "d":
		case "д":
			return value * 24L * 60 * 60 * 1000;

		case "h":
		case "ч":
			return value * 60L * 60 * 1000;
		case "min":
		case "мин":
			return value * 60L * 1000;
		default:
			return 0;
		}
	}
}