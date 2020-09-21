package org.zenframework.z8.webserver;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SpnegoLoginService;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IWebServer;
import org.zenframework.z8.server.engine.RmiServer;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.web.server.SingleSignOnAdapter;
import org.zenframework.z8.web.servlet.Servlet;
import org.zenframework.z8.webserver.spnego.CustomSpnegoAuthenticator;

public class WebServer extends RmiServer implements IWebServer {
	private static final String ID = guid.create().toString();

	private static final Collection<UrlPattern> urlPatterns = new LinkedList<UrlPattern>(
			Arrays.asList(
					new UrlPattern("/logout"),
					new UrlPattern("/sso_auth"),
					new UrlPattern("*.json"),
					new UrlPattern("/storage/*"),
					new UrlPattern("/files/*"),
					new UrlPattern("/reports/*")
			));

	private Server server;
	private org.eclipse.jetty.server.handler.ContextHandler context;
	private HttpServlet requestServlet;
	private WebResourceHandler resourceHandler;
	private Map<String, String> initParameters;
	private File webapp;
	private Properties mappings;

	public WebServer() throws RemoteException {
		super(ServerConfig.webServerPort());
	}

	@Override
	public void start() {
		initParameters = ServerConfig.webServerServletParams();
		webapp = ServerConfig.webServerWebapp();
		mappings = getMappings(ServerConfig.webServerMappings());

		urlPatterns.addAll(getUrlPatterns(ServerConfig.webServerUrlPatterns()));

		context = new ContextHandler("/");
		context.setResourceBase(webapp.getAbsolutePath());

		requestServlet = new Servlet();
		try {
			requestServlet.init(getZ8ServletConfig(context.getServletContext(), initParameters));
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

		final String domainRealm = ServerConfig.domainRealm();
		final String spnegoPropertiesPath = ServerConfig.spnegoPropertiesPath();
		Constraint constraint = new Constraint();
		constraint.setName(Constraint.__SPNEGO_AUTH);
		constraint.setRoles(new String[]{domainRealm});
		constraint.setAuthenticate(true);

		ConstraintMapping cm = new ConstraintMapping();
		cm.setConstraint(constraint);
		cm.setPathSpec(SingleSignOnAdapter.AdapterPath);

		SpnegoLoginService loginService = new SpnegoLoginService(domainRealm, spnegoPropertiesPath);

		ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
		securityHandler.setAuthenticator(new CustomSpnegoAuthenticator());
		securityHandler.setLoginService(loginService);
		securityHandler.setConstraintMappings(new ConstraintMapping[]{cm});
		securityHandler.setRealmName(domainRealm);

		SessionHandler sessions = new SessionHandler(new HashSessionManager()) {
			/**
			 * The method additionally extracts from the session {@link Authentication} object
			 * to put it in the request
			 */
			@Override
			public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
				Authentication auth = (Authentication) baseRequest.getSession().getAttribute("authentication");
				if (auth != null) {
					baseRequest.setAuthentication(auth);
				}
				super.doHandle(target, baseRequest, request,response);
			}
		};
		context.setHandler(sessions);

		sessions.setHandler(securityHandler);

		// Put handler inside of SessionHandler
		securityHandler.setHandler(new AbstractHandler() {

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

		});

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
