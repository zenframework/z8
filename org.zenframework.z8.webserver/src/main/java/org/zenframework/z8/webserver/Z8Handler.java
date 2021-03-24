package org.zenframework.z8.webserver;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.web.servlet.Servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

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
			if (requestServlet != null)
				requestServlet.destroy();
		}
	}

	private final Properties mappings;
	private HttpServlet requestServlet;
	private WebResourceHandler resourceHandler;

	public Z8Handler(ContextHandler context) {
		mappings = Z8Handler.getMappings(ServerConfig.webServerMappings());
		urlPatterns.addAll(Z8Handler.getUrlPatterns(ServerConfig.webServerUrlPatterns()));
		requestServlet = new Servlet();
		try {
			requestServlet.init(
					Z8Handler.getZ8ServletConfig(context.getServletContext(), ServerConfig.webServerServletParams()));
		} catch (ServletException e) {
			throw new RuntimeException("Z8 Servlet init failed", e);
		}
		resourceHandler = getWebResourceHandler();
		resourceHandler.init(Folders.Base, ServerConfig.webServerWebapp(), ServerConfig.language());

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
			requestServlet.service(request, response);
		else if (path.contains("..") || path.startsWith("/WEB-INF"))
			// Access denied
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal path " + path);
		else
			// Files or other resources
			resourceHandler.handle(path, response);

		baseRequest.setHandled(true);
	}

	protected WebResourceHandler getWebResourceHandler() {
		return new WebResourceHandler();
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

	private static Collection<UrlPattern> getUrlPatterns(String str) {
		if (str == null || str.isEmpty())
			return Collections.emptyList();
		Collection<UrlPattern> patterns = new LinkedList<UrlPattern>();
		String[] parts = str.split("\\,");
		for (String part : parts)
			patterns.add(new UrlPattern(part.trim()));
		return patterns;
	}

	private static ServletConfig getZ8ServletConfig(final ServletContext context, final Map<String, String> initParameters) {
		return new ServletConfig() {

			@Override
			public String getServletName() {
				return "Z8 Servlet";
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
