package org.zenframework.z8.webserver;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IWebServer;
import org.zenframework.z8.server.engine.RmiServer;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;

/**
 * {@link WebServer#launch(org.zenframework.z8.server.config.ServerConfig)} method is the entrypoint,
 * do not forget to override it in descendants
 */
public class WebServer extends RmiServer implements IWebServer {
	private static final String ID = guid.create().toString();
	protected Server server;
	protected ContextHandler context;
	protected Z8Handler z8Handler;

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

		server = new Server();

		ServerConnector connector = new ServerConnector(server);
		connector.setHost(ServerConfig.webServerHttpHost());
		connector.setPort(ServerConfig.webServerHttpPort());
		server.addConnector(connector);
		server.setHandler(context);

		// Specify the Session ID Manager
		SessionIdManager idmanager = new DefaultSessionIdManager(server);
		server.setSessionIdManager(idmanager);

		// Create the SessionHandler (wrapper) to handle the sessions
		SessionHandler sessions = new SessionHandler() {
			/**
			 * The method additionally extracts from the session {@link Authentication} object
			 * to put it in the request
			 */
			@Override
			public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
				// TODO Проверить, нужно ли
				Authentication auth = (Authentication) baseRequest.getSession().getAttribute("authentication");
				if (auth != null)
					baseRequest.setAuthentication(auth);
				super.doHandle(target, baseRequest, request, response);
			}
		};
		context.setHandler(sessions);

		GzipHandler gzipHandler = new GzipHandler();
		gzipHandler.setHandler(getZ8Handler());
		gzipHandler.addIncludedMimeTypes(ServerConfig.webServerGzipMimeTypes());
		gzipHandler.addIncludedMethods(ServerConfig.webServerGzipMethods());
		gzipHandler.addIncludedPaths(ServerConfig.webServerGzipPaths());

		// Wrap with security handler
		HandlerWrapper securityHandler = getSecurityHandler();
		if (securityHandler != null) {
			securityHandler.setHandler(gzipHandler);
			sessions.setHandler(securityHandler);
		} else {
			sessions.setHandler(gzipHandler);
		}
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

	protected synchronized Handler getZ8Handler() {
		if (z8Handler == null)
			z8Handler = new Z8Handler(context);
		return z8Handler;
	}

	protected SecurityHandler getSecurityHandler() {
		return null;
	}

	public static void launch(ServerConfig config) throws Exception {
		new WebServer().start();
	}
}
