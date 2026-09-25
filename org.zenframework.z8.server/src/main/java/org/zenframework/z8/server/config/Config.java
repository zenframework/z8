package org.zenframework.z8.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.StringUtils;

@SuppressWarnings("serial")
public class Config extends Properties {

	public Config() {}

	public Config(String path) {
		this(new File(path));
	}

	public Config(File file) {
		try {
			load(new FileInputStream(file));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void load(InputStream is) {
		try {
			super.load(is);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return value != null && !value.isEmpty() ? value : defaultValue;
	}

	public boolean getProperty(String key, boolean defaultValue) {
		String value = getProperty(key);
		return value != null && !value.isEmpty() ? Boolean.parseBoolean(value) : defaultValue;
	}

	public int getProperty(String key, int defaultValue) {
		try {
			return Integer.parseInt(getProperty(key));
		} catch(NumberFormatException e) {
			return defaultValue;
		}
	}

	public String[] getProperty(String key, String[] defaultValue) {
		String value = getProperty(key);

		if (value == null || value.trim().isEmpty())
			return defaultValue;

		return value.trim().split("\\s*[,;]\\s*");
	}

	public int[] getProperty(String key, int[] defaultValue) {
		String value = getProperty(key);

		if (value == null || value.trim().isEmpty())
			return defaultValue;

		String[] values = value.trim().split("\\s*[,;]\\s*");

		int[] result = new int[values.length];

		try {
			for(int i = 0; i < values.length; i++)
				result[i] = Integer.parseInt(values[i].trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}

		return result;
	}

	public List<String> getList(String key, String defaultValue, String delimiter) {
		return StringUtils.asList(getProperty(key, defaultValue), delimiter);
	}

	public Map<String, String> getMap(String key, String defaultValue, String pattern) {
		String value = getProperty(key, defaultValue);

		if (value == null)
			return Collections.emptyMap();

		Map<String, String> map = new HashMap<String, String>();
		Matcher matcher = Pattern.compile(pattern).matcher(value);
		while (matcher.find())
			map.put(matcher.group("key"), matcher.group("value"));

		return Collections.unmodifiableMap(map);
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject();

		for (String property : stringPropertyNames())
			json.put(property, getProperty(property));

		return json;
	}

	protected Map<String, String> filterParameters(String prefix) {
		Map<String, String> filtered = new HashMap<String, String>();
		for (String name : stringPropertyNames())
			if (name.startsWith(prefix))
				filtered.put(name.substring(prefix.length()), getProperty(name));
		return filtered;
	}

}
