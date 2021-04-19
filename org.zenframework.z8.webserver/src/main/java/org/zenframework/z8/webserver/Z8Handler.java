package org.zenframework.z8.webserver;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.web.servlet.DefaultServlet;
import org.zenframework.z8.web.servlet.Servlet;

public class Z8Handler extends AbstractHandler {

	private static final Collection<UrlPattern> urlPatterns = new LinkedList<UrlPattern>(
			Arrays.asList(
					new UrlPattern("/apidoc"),
					new UrlPattern("/logout"),
					new UrlPattern("/sso_auth"),
					new UrlPattern("*.json"),
					new UrlPattern("/storage/*"),
					new UrlPattern("/files/*"),
					new UrlPattern("/reports/*")));

	private class LifeCycleListener extends AbstractLifeCycleListener {
		@Override
		public void lifeCycleStopped(LifeCycle event) {
			if (z8Servlet != null)
				z8Servlet.destroy();
			if (defaultServlet != null)
				defaultServlet.destroy();
		}
	}

	private final Properties mappings;
	private HttpServlet z8Servlet;
	private HttpServlet defaultServlet;

	public Z8Handler(ContextHandler context) {
		mappings = getMappings(ServerConfig.webServerMappings());
		urlPatterns.addAll(getUrlPatterns(ServerConfig.webServerUrlPatterns()));

		z8Servlet = newZ8Servlet();
		defaultServlet = newDefaultServlet();

		try {
			z8Servlet.init(getServletConfig("Z8 Servlet", context.getServletContext(), ServerConfig.webServerServletParams()));
			defaultServlet.init(getServletConfig("Default Servlet", context.getServletContext(), Collections.emptyMap()));
		} catch (ServletException e) {
			throw new RuntimeException("Z8 Servlets initialization failed", e);
		}

		addLifeCycleListener(new LifeCycleListener());
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,
					   HttpServletResponse response) throws IOException, ServletException {

		String path = URLDecoder.decode(baseRequest.getRequestURI(), "UTF-8");
		baseRequest.setServletPath(path);
/*
				LOG.debug("REQUEST: " + path);
*/
		response.setCharacterEncoding("UTF-8");
		response.setContentType(getContentType(path));

		if (Z8Handler.isSystemRequest(path))
			// Z8 request
			z8Servlet.service(request, response);
		else
			// Files or other resources
			defaultServlet.service(request, response);

		baseRequest.setHandled(true);
	}

	protected HttpServlet newZ8Servlet() {
		return new Servlet();
	}

	protected HttpServlet newDefaultServlet() {
		return new DefaultServlet();
	}

	private String getContentType(String path) {
		return mappings.getProperty(FilenameUtils.getExtension(path), "text/html;charset=UTF-8");
	}

	private static Properties getMappings(String path) {
		Properties mappings = new Properties();
		Reader reader = null;

		try {
			Enumeration<URL> resources = WebServer.class.getClassLoader().getResources("webserver/mappings.properties");
			while (resources.hasMoreElements()) {
				try {
					reader = new InputStreamReader(resources.nextElement().openStream());
					mappings.load(reader);
				} catch (IOException e1) {
				} finally {
					IOUtils.closeQuietly(reader);
				}
			}
		} catch (IOException e) {
			Trace.logError("Couldn't load mappings from classpath webserver/mappings.properties" + path, e);
		}

		if (path != null) {
			try {
				reader = new FileReader(path);
				mappings.load(reader);
			} catch (IOException e) {
				Trace.logError("Couldn't load mappings from " + path, e);
			} finally {
				IOUtils.closeQuietly(reader);
			}
		}
		return mappings;
	}

	private static boolean isSystemRequest(String path) {
		for (UrlPattern pattern : urlPatterns)
			if (pattern.matches(path))
				return true;
		return false;
	}

	private static Collection<UrlPattern> getUrlPatterns(String[] strs) {
		if (strs == null || strs.length == 0)
			return Collections.emptyList();
		Collection<UrlPattern> patterns = new LinkedList<UrlPattern>();
		for (String str : strs)
			patterns.add(new UrlPattern(str.trim()));
		return patterns;
	}

	private static ServletConfig getServletConfig(final String name, final ServletContext context, final Map<String, String> initParameters) {
		return new ServletConfig() {

			@Override
			public String getServletName() {
				return name;
			}

			@Override
			public ServletContext getServletContext() {
				return context;
			}

			@Override
			public Enumeration<String> getInitParameterNames() {
				final Iterator<String> names = initParameters.keySet().iterator();
				return new Enumeration<String>() {
					@Override
					public boolean hasMoreElements() {
						return names.hasNext();
					}

					@Override
					public String nextElement() {
						return names.next();
					}
				};
			}

			@Override
			public String getInitParameter(String name) {
				return initParameters.get(name);
			}
		};
	}

	public HttpServlet getRequestServlet() {
		return requestServlet;
	}
}
