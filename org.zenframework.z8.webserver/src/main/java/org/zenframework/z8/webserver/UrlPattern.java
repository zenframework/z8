package org.zenframework.z8.webserver;

public class UrlPattern {

	private static enum Compare {
		StartsWith, EndsWith, Equals
	}

	private final Compare compare;
	private final String pattern;

	public UrlPattern(String pattern) {
		if (pattern.length() > 0 && pattern.charAt(0) == '*') {
			compare = Compare.EndsWith;
			this.pattern = pattern.substring(1);
		} else if (pattern.length() > 0 && pattern.charAt(pattern.length() - 1) == '*') {
			compare = Compare.StartsWith;
			this.pattern = pattern.substring(0, pattern.length() - 1);
		} else {
			compare = Compare.Equals;
			this.pattern = pattern;
		}
	}

	public boolean matches(String url) {
		return compare == Compare.StartsWith && url.startsWith(pattern) || compare == Compare.EndsWith && url.endsWith(pattern) || pattern.equals(url);
	}

}
