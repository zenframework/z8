package org.zenframework.z8.server.router;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class Router implements IRouter {
	String prefix = "";

	RouterList routes;
	UrlMatch currentMatch;

	public Router(String prefix) {
		this.routes = new RouterList();
		this.prefix = prefix.replaceAll("[\r\n\\/]+$", "");

		Collection<Route> routes = this.routes.getRoutes().values();
		generateRouteMatches(routes);
	}

	@Override
	public Map<String, Route> getHttpEndpoints() {
		return this.routes.getRoutes();
	}

	public String getPrefix() {
		return this.prefix;
	}

	/**
	 * @param prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * @param routes
	 */
	public void generateRouteMatches(Collection<Route> routes) {
		for (Route route : routes) {
			String path = route.getPath();
			path = this.prefix + path;
			UrlPattern pattern = new UrlPattern(path);

			route.setPath(path);
			route.setPattern(pattern);
		}
	}

	/**
	 * @param path
	 * @return
	 */
	public UrlMatch match(HttpServletRequest request) {
		for (Route route : this.routes.getRoutes().values()) {
			UrlMatch match = route.match(request);
			
			if (match != null) {
				return match;
			}
		}

		return null;
	}
}
