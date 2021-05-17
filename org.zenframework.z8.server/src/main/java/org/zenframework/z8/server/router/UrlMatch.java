package org.zenframework.z8.server.router;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class UrlMatch {
	private Map<String, String> parameters = new HashMap<String, String>();

	public UrlMatch(Map<String, String> parameters) {
		super();

		if (parameters != null) {
			this.parameters.putAll(parameters);
		}
	}

	public String get(String name) {
		return parameters.get(name);
	}

	public Set<Entry<String, String>> parameterSet() {
		return Collections.unmodifiableSet(parameters.entrySet());
	}

	/**
	 * @param parameter
	 * @param value
	 */
	public void addParameter(String parameter, String value) {
		if (!this.parameters.containsKey(parameter)) {
			this.parameters.put(parameter, value);
		}
	}

	/**
	 * @param parameter
	 */
	public void removeParameter(String parameter) {
		if (this.parameters.containsKey(parameter)) {
			this.parameters.remove(parameter);
		}
	}
}
