package org.zenframework.z8.webserver;

public class UrlPattern {

	private static enum Compare {
		StartsWith, EndsWith, Equals
	}

	private final Compare compare;
	private final String pattern, value;

	public UrlPattern(String pattern) {
		this.pattern = pattern;
		if (pattern.length() > 0 && pattern.charAt(0) == '*') {
			compare = Compare.EndsWith;
			value = pattern.substring(1);
		} else if (pattern.length() > 0 && pattern.charAt(pattern.length() - 1) == '*') {
			compare = Compare.StartsWith;
			value = pattern.substring(0, pattern.length() - 1);
		} else {
			compare = Compare.Equals;
			value = pattern;
		}
	}

	public boolean matches(String url) {
		return compare == Compare.StartsWith && url.startsWith(value) || compare == Compare.EndsWith && url.endsWith(value) || url.equals(value);
	}

	@Override
	public int hashCode() {
		return pattern.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof UrlPattern && ((UrlPattern) obj).pattern.equals(pattern);
	}

	@Override
	public String toString() {
		return "UrlPattern[" + pattern + ']';
	}

}
