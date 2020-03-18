package org.zenframework.z8.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IWebServer;
import org.zenframework.z8.server.engine.RmiServer;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.web.servlet.Servlet;

public class WebServer extends RmiServer implements IWebServer {

	private static final Log LOG = LogFactory.getLog(WebServer.class);

	private static final String CLASSPATH_WEBAPP = "web";
	private static final String WELCOME_FILE = "index.html";

	private static final String ID = guid.create().toString();

	private static final String PREFIX_SERVLET_CONFIG = "z8.servlet.";
	
	private static final String PROP_WEB_SERVER_PORT = "z8.webserver.port";
	private static final String PROP_WEB_SERVER_WEBAPP = "z8.webserver.webapp";
	private static final String PROP_WEB_SERVER_MAPPINGS = "z8.webserver.content.map";
	private static final String PROP_WEB_SERVER_URL_PATTERNS = "z8.webserver.urlPatterns";

	private static final int DEFAULT_WEB_SERVER_PORT = 80;
	private static final String DEFAULT_WEB_SERVER_WEBAPP = "..";

	private static final Collection<UrlPattern> urlPatterns = new LinkedList<UrlPattern>(Arrays.asList(new UrlPattern("*.json"), new UrlPattern("/storage/*"), new UrlPattern("/files/*"), new UrlPattern("/reports/*")));

	private Server server;
	private ContextHandler context;
	private Servlet servlet;
	private File webapp;
	private Properties mappings;

	public WebServer() throws RemoteException {
		super(ServerConfig.webServerPort());
	}

	@Override
	public void start() {
		try {

			final Map<String, String> initParameters = new HashMap<String, String>();
			for (String name : System.getProperties().stringPropertyNames())
				if (name.startsWith(PREFIX_SERVLET_CONFIG))
					initParameters.put(name.substring(PREFIX_SERVLET_CONFIG.length()), System.getProperty(name));

			webapp = new File(System.getProperty(PROP_WEB_SERVER_WEBAPP, DEFAULT_WEB_SERVER_WEBAPP));
			mappings = getMappings(System.getProperty(PROP_WEB_SERVER_MAPPINGS));

			urlPatterns.addAll(getUrlPatterns(System.getProperty(PROP_WEB_SERVER_URL_PATTERNS)));

			context = new ContextHandler("/");
			context.setResourceBase(webapp.getAbsolutePath());

			servlet = new Servlet();
			servlet.init(new ServletConfig() {

				@Override
				public String getServletName() {
					return "Z8 Servlet";
				}

				@Override
				public ServletContext getServletContext() {
					return context.getServletContext();
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
			});

			server = new Server(Integer.getInteger(PROP_WEB_SERVER_PORT, DEFAULT_WEB_SERVER_PORT));
			server.setHandler(context);

			// Specify the Session ID Manager
			SessionIdManager idmanager = new HashSessionIdManager();
			server.setSessionIdManager(idmanager);

			// Create the SessionHandler (wrapper) to handle the sessions
			SessionHandler sessions = new SessionHandler(new HashSessionManager());
			context.setHandler(sessions);

			// Put handler inside of SessionHandler
			sessions.setHandler(new AbstractHandler() {

				@Override
				public void handle(String target, Request baseRequest, HttpServletRequest request,
						HttpServletResponse response) throws IOException, ServletException {
					
					String path = URLDecoder.decode(baseRequest.getRequestURI(), "UTF-8");
					baseRequest.setServletPath(path);
					LOG.debug("REQUEST: " + path);
					response.setCharacterEncoding("UTF-8");
					response.setContentType(getContentType(path));
					
					if (isSystemRequest(path)) {
						// Z8 request
						servlet.service(request, response);
						
					} else if (path.contains("..") || path.startsWith("/WEB-INF")) {
						// Access denied
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal path " + path);
						
					} else {
						InputStream in = null;
						
						// 1. Find source
						File file = new File(webapp, path);
						// Try file
						if (file.exists()) {
							if (file.isDirectory()) {
								if (!path.endsWith("/")) {
									response.sendRedirect(path + '/');
									return;
								} else {
									file = new File(file, WELCOME_FILE);
								}
							}
							if (file.exists())
								in = new FileInputStream(file);
						}
						
						if (in == null) {
							// Try classpath resource
							ClassLoader classLoader = WebServer.class.getClassLoader();
							path = FilenameUtils.concat(CLASSPATH_WEBAPP, path.isEmpty() ? path : path.substring(1));
							URL resource = classLoader.getResource(path);
							if (resource != null) {
								in = resource.openStream();
								if (in.available() == 0) {
									// Is directory
									IOUtils.closeQuietly(in);
									resource = classLoader.getResource(FilenameUtils.concat(path, WELCOME_FILE));
									in = resource == null ? null : resource.openStream();
								}
							}
						}
						
						if (in == null) {
							response.sendError(HttpServletResponse.SC_NOT_FOUND, "File " + path + " not found");
						} else {
							// 2. Send source
							OutputStream out = response.getOutputStream();
							try {
								IOUtils.copy(in, out);
							} finally {
								IOUtils.closeQuietly(in);
								IOUtils.closeQuietly(out);
							}
						}
					}
					
					baseRequest.setHandled(true);
				}

			});

			server.start();

		} catch (Exception e) {
			LOG.error("Couldn't start web server", e);
		}
	}

	@Override
	public void stop() throws RemoteException {
		super.stop();
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				LOG.error("Couldn't stop web server", e);
			}
		}
		if (servlet != null) {
			servlet.destroy();
		}
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
			reader = new InputStreamReader(
					WebServer.class.getClassLoader().getResourceAsStream("webserver/mappings.properties"));
			mappings.load(reader);
		} catch (IOException e) {
			LOG.warn("Couldn't load mappings from classpath webserver/mappings.properties" + path, e);
		} finally {
			IOUtils.closeQuietly(reader);
			reader = null;
		}
		if (path != null) {
			try {
				reader = new FileReader(path);
				mappings.load(reader);
			} catch (IOException e) {
				LOG.warn("Couldn't load mappings from " + path, e);
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
}
