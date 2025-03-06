package org.zenframework.z8.server.json.parser;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class JsonPath {

	private static final Pattern PATH_PATTERN = Pattern.compile("((^|/)\\w+)|(\\[\\d+\\])");

	private final String path;
	private final Object[] parts;

	public JsonPath(Object... parts) {
		this.parts = parts != null ? parts : new Object[0];
		StringBuilder path = new StringBuilder();
		for (Object part : this.parts) {
			if (part instanceof Integer)
				path.append('[').append(part).append(']');
			else
				path.append('/').append(part);
		}
		this.path = path.toString();
	}

	public JsonPath(String path) {
		this.path = path != null ? path.trim() : "";
		List<Object> parts = new LinkedList<Object>();
		Scanner scanner = new Scanner(this.path);
		for (String part = scanner.findWithinHorizon(PATH_PATTERN, 0); part != null; part = scanner.findWithinHorizon(PATH_PATTERN, 0)) {
			if (part.startsWith("/"))
				parts.add(part.substring(1));
			else if (part.startsWith("["))
				parts.add(Integer.parseInt(part.substring(1, part.length() - 1)));
			else
				parts.add(part);
		}
		this.parts = parts.toArray();
		scanner.close();
	}

	public JsonPath parent() {
		if (parts.length == 0)
			return null;
		Object[] parentParts = new Object[parts.length - 1];
		System.arraycopy(parts, 0, parentParts, 0, parentParts.length);
		return new JsonPath(parentParts);
	}

	public JsonPath child(Object child) {
		Object[] childParts = new Object[parts.length + 1];
		System.arraycopy(parts, 0, childParts, 0, parts.length);
		childParts[childParts.length - 1] = child;
		return new JsonPath(childParts);
	}

	public int ordinal(int part) {
		return (Integer) parts[part];
	}

	public String name(int part) {
		return (String) parts[part];
	}

	public int length() {
		return parts.length;
	}

	public Object evaluate(Object json) {
		return evaluate(json, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T evaluate(Object json, T defaultValue) {
		return (T) evaluate(json, defaultValue, false);
	}

	@SuppressWarnings("unchecked")
	public Object evaluate(Object json, Object defaultValue, boolean strict) {
		if (path.isEmpty())
			return json != null ? json : defaultValue;

		json = JsonUtils.unwrap(json);

		for (int i = 0; i < parts.length; i++) {
			if (json == null)
				return defaultValue;
			Object part = parts[i];
			if (part instanceof Integer) {
				if (!(json instanceof List)) {
					if (strict)
						throw new RuntimeException("Can't evaluate '" + path + "': '" + new JsonPath(Arrays.copyOf(parts, i + 1)) + "' is not JSON array");
					else
						return defaultValue;
				}
				json = ((List<Object>) json).get((Integer) part);
			} else {
				if (!(json instanceof Map)) {
					if (strict)
						throw new RuntimeException("Can't evaluate '" + path + "': '" + new JsonPath(Arrays.copyOf(parts, i + 1)) + "' is not JSON object");
					else
						return defaultValue;
				}
				json = ((Map<String, Object>) json).get((String) part);
			}
		}

		return json != null ? json : defaultValue;
	}

	@Override
	public String toString() {
		return path;
	}

}
