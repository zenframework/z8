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
	private final boolean strict = false;

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
	}

	@SuppressWarnings("unchecked")
	public Object evaluate(Object json) {
		if (path.isEmpty())
			return json;

		for (int i = 0; i < parts.length; i++) {
			if (json == null)
				return null;
			Object part = parts[i];
			if (part instanceof Integer) {
				if (!(json instanceof List)) {
					if (strict)
						throw new RuntimeException("Can't evaluate '" + path + "': '" + new JsonPath(Arrays.copyOf(parts, i + 1)) + "' is not JSON array");
					else
						return null;
				}
				json = ((List<Object>) json).get((Integer) part);
			} else {
				if (!(json instanceof Map)) {
					if (strict)
						throw new RuntimeException("Can't evaluate '" + path + "': '" + new JsonPath(Arrays.copyOf(parts, i + 1)) + "' is not JSON object");
					else
						return null;
				}
				json = ((Map<String, Object>) json).get((String) part);
			}
		}

		return json;
	}

	@Override
	public String toString() {
		return path;
	}

}
