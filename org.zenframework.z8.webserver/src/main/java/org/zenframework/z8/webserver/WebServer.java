package org.zenframework.z8.webserver;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SpnegoLoginService;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlets.gzip.GzipHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IWebServer;
import org.zenframework.z8.server.engine.RmiServer;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.web.server.SingleSignOnAdapter;
import org.zenframework.z8.webserver.spnego.CustomSpnegoAuthenticator;

/**
 * {@link WebServer#launch(org.zenframework.z8.server.config.ServerConfig)} method is the entrypoint,
 * do not forget to override it in descendants
 */
public class WebServer extends RmiServer implements IWebServer {
	private static final String ID = guid.create().toString();
	protected Server server;
	protected ContextHandler context;

	public WebServer() throws RemoteException {
		super(ServerConfig.webServerPort());
		configureServer();
	}

	/**
	 * The method is an extension point to configure jetty server
	 */
	protected void configureServer() {
		context = new ContextHandler("/");
		context.setResourceBase(ServerConfig.webServerWebapp().getAbsolutePath());
		context.getServletContext();

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
				super.doHandle(target, baseRequest, request, response);
			}
		};
		context.setHandler(sessions);

		GzipHandler gzipHandler = new GzipHandler();
		gzipHandler.setHandler(new Z8Handler(context));
		gzipHandler.addIncludedMimeTypes(ServerConfig.webServerGzipMimeTypes());
		gzipHandler.addIncludedMethods(ServerConfig.webServerGzipMethods());
		gzipHandler.addIncludedPaths(ServerConfig.webServerGzipPaths());

		securityHandler.setHandler(gzipHandler);
		sessions.setHandler(securityHandler);
	}

	@Override
	public void start() {
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
	}

	@Override
	public String id() throws RemoteException {
		return ID;
	}

	@Override
	public void probe() throws RemoteException {}

	public static void launch(ServerConfig config) throws Exception {
		new WebServer().start();
	}
}
