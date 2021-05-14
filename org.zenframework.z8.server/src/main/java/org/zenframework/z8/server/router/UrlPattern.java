package org.zenframework.z8.server.router;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlPattern implements IUrlMatcher {

	private static final String URL_PARAM_REGEX = "\\{(\\w*?)\\}";

	private static final String URL_PARAM_MATCH_REGEX = "\\([%\\\\w-.\\\\~!\\$&'\\\\(\\\\)\\\\*\\\\+,;=:\\\\[\\\\]@]+?\\)";

	private static final Pattern URL_PARAM_PATTERN = Pattern.compile(URL_PARAM_REGEX);

	private static final String URL_FORMAT_REGEX = "(?:\\.\\{format\\})$";

	private static final String URL_FORMAT_MATCH_REGEX = "(?:\\\\.\\([\\\\w%]+?\\))?";

	private static final String URL_QUERY_STRING_REGEX = "(?:\\?.*?)?$";

	private String urlPattern;

	private Pattern compiledUrl;

	private List<String> parameterNames = new ArrayList<String>();

	public UrlPattern(String pattern) {
		super();
		setUrlPattern(pattern);
		compile();
	}

	private String getUrlPattern() {
		return urlPattern;
	}

	public String getPattern() {
		return getUrlPattern().replaceFirst(URL_FORMAT_REGEX, "");
	}

	private void setUrlPattern(String pattern) {
		this.urlPattern = pattern;
	}

	public List<String> getParameterNames() {
		return Collections.unmodifiableList(parameterNames);
	}

	@Override
	public UrlMatch match(String url) {
		Matcher matcher = compiledUrl.matcher(url);

		if (matcher.matches()) {
			return new UrlMatch(extractParameters(matcher));
		}

		return null;
	}

	@Override
	public boolean matches(String url) {
		return (match(url) != null);
	}
	
	public void compile() {
		acquireParameterNames();
		String parsedPattern = getUrlPattern().replaceFirst(URL_FORMAT_REGEX, URL_FORMAT_MATCH_REGEX);
		parsedPattern = parsedPattern.replaceAll(URL_PARAM_REGEX, URL_PARAM_MATCH_REGEX);
		this.compiledUrl = Pattern.compile(parsedPattern + URL_QUERY_STRING_REGEX);
	}

	private void acquireParameterNames() {
		Matcher m = URL_PARAM_PATTERN.matcher(getUrlPattern());

		while (m.find()) {
			parameterNames.add(m.group(1));
		}
	}

	private Map<String, String> extractParameters(Matcher matcher) {
		Map<String, String> values = new HashMap<String, String>();

		for (int i = 0; i < matcher.groupCount(); i++) {
			String value = matcher.group(i + 1);

			if (value != null) {
				values.put(parameterNames.get(i), value);
			}
		}

		return values;
	}

}
