package org.zenframework.z8.server.utils;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.types.string;

public class PrimaryUtils {

	private PrimaryUtils() {}

	public static Map<String, String> unwrapStringMap(Map<string, string> map) {
		Map<String, String> result = new HashMap<String, String>();
		for (Map.Entry<string, string> entry : map.entrySet())
			result.put(entry.getKey().get(), entry.getValue().get());
		return result;
	}

}
