package org.zenframework.z8.webserver;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;
import java.rmi.RemoteException;
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
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlets.gzip.GzipHandler;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IWebServer;
import org.zenframework.z8.server.engine.RmiServer;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.web.servlet.Servlet;

public class WebServer extends RmiServer implements IWebServer {
	private static final String ID = guid.create().toString();

	private static final Collection<UrlPattern> urlPatterns = new LinkedList<UrlPattern>(Arrays.asList(new UrlPattern("*.json"), new UrlPattern("/storage/*"), new UrlPattern("/files/*"), new UrlPattern("/reports/*")));

	private Server server;
	private ContextHandler context;
	private HttpServlet requestServlet;
	private WebResourceHandler resourceHandler;
	private Map<String, String> servletParameters;
	private File webapp;
	private Properties mappings;

	public WebServer() throws RemoteException {
		super(ServerConfig.webServerPort());
	}

	@Override
	public void start() {
		servletParameters = ServerConfig.webServerServletParams();

		webapp = ServerConfig.webServerWebapp();
		mappings = getMappings(ServerConfig.webServerMappings());

		urlPatterns.addAll(getUrlPatterns(ServerConfig.webServerUrlPatterns()));

		context = new ContextHandler("/");
		context.setResourceBase(webapp.getAbsolutePath());

		requestServlet = new Servlet();
		try {
			requestServlet.init(getZ8ServletConfig(context.getServletContext(), servletParameters));
		} catch (ServletException e) {
			throw new RuntimeException("Z8 Servlet init failed", e);
		}

		resourceHandler = new WebResourceHandler();
		resourceHandler.init(Folders.Base, webapp);

		server = new Server(ServerConfig.webServerHttpPort());
		server.setHandler(context);

		// Specify the Session ID Manager
		SessionIdManager idmanager = new HashSessionIdManager();
		server.setSessionIdManager(idmanager);

		// Create the SessionHandler (wrapper) to handle the sessions
		SessionHandler sessions = new SessionHandler(new HashSessionManager());
		context.setHandler(sessions);

		Handler z8Handler = new AbstractHandler() {

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
					requestServlet.service(request, response);
				else if (path.contains("..") || path.startsWith("/WEB-INF"))
					// Access denied
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal path " + path);
				else
					// Files or other resources
					resourceHandler.handle(path, response);

				baseRequest.setHandled(true);
			}

		};

		GzipHandler gzipHandler = new GzipHandler();
		gzipHandler.setHandler(z8Handler);
		gzipHandler.addIncludedMimeTypes(ServerConfig.webServerGzipMimeTypes());
		gzipHandler.addIncludedMethods(ServerConfig.webServerGzipMethods());
		gzipHandler.addIncludedPaths(ServerConfig.webServerGzipPaths());

		// Put handler inside of SessionHandler
		sessions.setHandler(gzipHandler);

		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException("WebServer start failed", e);
		}
	}

	@Override
	public void stop() throws RemoteException {
		super.stop();
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				Trace.logError("Couldn't stop web server", e);
			}
		}
		if (requestServlet != null)
			requestServlet.destroy();
	}

	@Override
	public String id() throws RemoteException {
		return ID;
	}

	@Override
	public void probe() throws RemoteException {}

	private String getContentType(String path) {
		return mappings.getProperty(FilenameUtils.getExtension(path), "text/html;charset=UTF-8");
	}

	public static void launch(ServerConfig config) throws Exception {
		new WebServer().start();
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
				} catch (IOException e1) {} finally {
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

	private Collection<UrlPattern> getUrlPatterns(String str) {
		if (str == null || str.isEmpty())
			return Collections.emptyList();
		Collection<UrlPattern> patterns = new LinkedList<UrlPattern>();
		String[] parts = str.split("\\,");
		for (String part : parts)
			patterns.add(new UrlPattern(part.trim()));
		return patterns;
	}

	private static boolean isSystemRequest(String path) {
		for (UrlPattern pattern : urlPatterns)
			if (pattern.matches(path))
				return true;
		return false;
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

}
