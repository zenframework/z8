package org.zenframework.z8.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.web.servlet.Servlet;

public class WebServerHandler extends AbstractHandler {

	protected static final String CLASSPATH_WEBAPP = "web";
	protected static final String WELCOME_FILE = "index.html";
	protected static final String RESOURCE_CACHE = "webcache";

	protected final Collection<UrlPattern> urlPatterns = new LinkedList<UrlPattern>(Arrays.asList(new UrlPattern("*.json"), new UrlPattern("/storage/*"), new UrlPattern("/files/*"), new UrlPattern("/reports/*")));
	protected final Properties mappings = new Properties();

	protected File resourceCache;
	protected File webapp;
	protected Servlet z8Servlet;

	public WebServerHandler(ContextHandler context) {
		urlPatterns.addAll(getUrlPatterns(ServerConfig.webServerUrlPatterns()));
		mappings.putAll(getMappings(ServerConfig.webServerMappings()));

		resourceCache = Folders.Base;
		webapp = ServerConfig.webServerWebapp();

		z8Servlet = new Servlet();
		try {
			z8Servlet.init(getZ8ServletConfig(context.getServletContext(), ServerConfig.webServerServletParams()));
		} catch (ServletException e) {
			throw new RuntimeException("Z8 Servlet init failed", e);
		}
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

		if (isSystemRequest(path))
			// Z8 request
			z8Servlet.service(request, response);
		else if (path.contains("..") || path.startsWith("/WEB-INF"))
			// Access denied
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal path " + path);
		else
			// Files or other resources
			handleResource(path, response);

		baseRequest.setHandled(true);
	}

	@Override
	public void destroy() {
		super.destroy();
		z8Servlet.destroy();
	}

	protected boolean isSystemRequest(String path) {
		for (UrlPattern pattern : urlPatterns)
			if (pattern.matches(path))
				return true;
		return false;
	}

	protected String getContentType(String path) {
		return mappings.getProperty(FilenameUtils.getExtension(path), "text/html;charset=UTF-8");
	}

	@SuppressWarnings("resource")
	protected void handleResource(String path, HttpServletResponse response) throws IOException {
		File file = getFile(path);

		if (file != null && file.exists()) {
			if (file.isDirectory()) {
				if (!path.endsWith("/")) {
					response.sendRedirect(path + '/');
					return;
				} else {
					file = getFile(path + WELCOME_FILE);
				}
			}
			if (file.exists()) {
				copy(new FileInputStream(file), response.getOutputStream());
				return;
			}
		}

		response.sendError(HttpServletResponse.SC_NOT_FOUND, "File " + path + " not found");
	}

	protected File getFile(String path) throws IOException {
		File file = new File(webapp, path);
		if (file.exists())
			return file;

		file = new File(resourceCache, path);
		if (file.exists())
			return file;

		URL alternate = getAlternateResource(path);
		if (alternate != null) {
			InputStream in = alternate.openStream();
			if (in.available() == 0) {
				// Is directory
				IOUtils.closeQuietly(in);
				file.mkdirs();
			} else {
				// Cache alternate to file
				file.getParentFile().mkdirs();
				IOUtils.copy(alternate.openStream(), new FileOutputStream(file));
			}
			return file;
		}

		return null;
	}

	protected URL getAlternateResource(String path) throws IOException {
		ClassLoader classLoader = WebServer.class.getClassLoader();
		path = FilenameUtils.concat(CLASSPATH_WEBAPP, path.isEmpty() ? path : path.substring(1));
		return classLoader.getResource(path);
	}

	protected static void copy(InputStream in, OutputStream out) throws IOException {
		IOUtils.copy(in, out);
	}

	protected static Collection<UrlPattern> getUrlPatterns(String str) {
		if (str == null || str.isEmpty())
			return Collections.emptyList();
		Collection<UrlPattern> patterns = new LinkedList<UrlPattern>();
		String[] parts = str.split("\\,");
		for (String part : parts)
			patterns.add(new UrlPattern(part.trim()));
		return patterns;
	}

	protected static Properties getMappings(String path) {
		Properties mappings = new Properties();
		Reader reader = null;
		try {
			reader = new InputStreamReader(
					WebServer.class.getClassLoader().getResourceAsStream("webserver/mappings.properties"));
			mappings.load(reader);
		} catch (IOException e) {
			Trace.logError("Couldn't load mappings from classpath webserver/mappings.properties" + path, e);
		} finally {
			IOUtils.closeQuietly(reader);
			reader = null;
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

	protected static ServletConfig getZ8ServletConfig(final ServletContext context, final Map<String, String> initParameters) {
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

}
