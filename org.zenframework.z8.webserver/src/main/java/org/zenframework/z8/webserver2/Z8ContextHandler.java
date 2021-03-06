package org.zenframework.z8.webserver2;

import javax.servlet.Servlet;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ArrayUtil;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.web.servlet.DefaultServlet;
import org.zenframework.z8.web.servlet.WebSocketServlet;

public class Z8ContextHandler extends ServletContextHandler {

	private static final String[] Z8_URL_PATTERNS = { "/apidoc", "/logout", "/sso_auth",
			"*.json", "/storage/*", "/files/*", "/reports/*" };

	private static final String WEBSOCKET_URL_PATTERN = "/events/*";

	public Z8ContextHandler() {
		super(ServletContextHandler.SESSIONS & ServletContextHandler.GZIP);
		init();
	}

	public ServletHolder addServlet(String className, String... pathSpecs) {
		return getServletHandler().addServletWithMapping(className, pathSpecs);
	}

	public ServletHolder addServlet(Class<? extends Servlet> servlet, String... pathSpecs) {
		return getServletHandler().addServletWithMapping(servlet, pathSpecs);
	}

	public void addServlet(ServletHolder servlet, String... pathSpecs) {
		getServletHandler().addServletWithMapping(servlet, pathSpecs);
	}

	@Override
	public Z8ServletHandler getServletHandler() {
		return (Z8ServletHandler) super.getServletHandler();
	}

	@Override
	protected ServletHandler newServletHandler() {
		return new Z8ServletHandler();
	}

	protected void init() {
		addServlet(DefaultServlet.class, "/");
		addServlet(org.zenframework.z8.web.servlet.Servlet.class, ArrayUtil.add(Z8_URL_PATTERNS, ServerConfig.webServerUrlPatterns()));
		addServlet(WebSocketServlet.class, WEBSOCKET_URL_PATTERN);
	}

}
