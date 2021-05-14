package org.zenframework.z8.server.router;

import java.util.List;

public interface IUrlMatcher {
	public boolean matches(String url);

	public UrlMatch match(String url);

	public String getPattern();

	public List<String> getParameterNames();
}
