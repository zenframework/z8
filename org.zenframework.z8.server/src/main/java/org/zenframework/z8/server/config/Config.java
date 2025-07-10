package org.zenframework.z8.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.zenframework.z8.server.json.parser.JsonObject;

@SuppressWarnings("serial")
public class Config extends Properties {
	public Config() {
	}

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

		if(value == null || value.trim().isEmpty())
			return defaultValue;

		String[] values = value.split("\\s*[,;]\\s*");

		String[] result = new String[values.length];

		for(int i = 0; i < values.length; i++)
			result[i] = values[i].trim();

		return result;
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject();

		for(String property : stringPropertyNames())
			json.put(property, getProperty(property));

		return json;
	}
}
