package org.zenframework.z8.server.router;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class Route implements IRoute {
	private String name;

	private String path = "/";

	private String[] methods;

	private Map<String, String> options;

	private String action;

	private UrlPattern pattern;

	public Route(String name, String path, String action, String[] methods, Map<String, String> options) {
		this.path = path;
		this.name = name;
		this.action = action;
		this.methods = methods;
		this.options = options;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getPath() {
		return this.path;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String[] getMethods() {
		return this.methods;
	}

	@Override
	public void setMethods(String[] methods) {
		this.methods = methods;
	}

	@Override
	public boolean hasMethod(String method) {
		return Arrays.stream(this.methods).anyMatch(method::equals);
	}

	@Override
	public Map<String, String> getOptions() {
		return this.options;
	}

	@Override
	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	@Override
	public void addOption(String key, String value) {
		this.options.put(key, value);
	}

	@Override
	public boolean hasOption(String key) {
		return this.options.containsKey(key);
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the pattern
	 */
	public UrlPattern getPattern() {
		return pattern;
	}

	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(UrlPattern pattern) {
		this.pattern = pattern;
	}
	
	/**
	 * @param name
	 * @param value
	 */
	public void setOption(String name, String value) {
		this.options.put(name, value);
	}
	
	/**
	 * @param name
	 */
	public void removeOption(String name) {
		if (this.options.containsKey(name)) {
			this.options.remove(name);
		}
	}

	/**
	 * @param request
	 * @return
	 */
	public UrlMatch match(HttpServletRequest request) {
		UrlMatch match = this.getPattern().match(request.getPathInfo());
		boolean allowedRequestMethod = Arrays.asList(this.methods).contains(request.getMethod());

		if (match != null && allowedRequestMethod) {
			match.addParameter("request", this.getOptions().get("class"));
			match.addParameter("action", this.getAction());

			return match;

		}

		return null;
	}
}
