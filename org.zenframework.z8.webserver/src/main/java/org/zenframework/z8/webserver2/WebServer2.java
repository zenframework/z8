package org.zenframework.z8.webserver2;

import java.rmi.RemoteException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IWebServer;
import org.zenframework.z8.server.engine.RmiServer;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;

public class WebServer2 extends RmiServer implements IWebServer {

	private static final String ID = guid.create().toString();

	protected Server server;
	protected ServletContextHandler context;

	public WebServer2() throws RemoteException {
		super(ServerConfig.webServerPort());
		configureServer();
	}

	/**
	 * The method is an extension point to configure jetty server
	 */
	protected void configureServer() {
		server = new Server();

		ServerConnector connector = new ServerConnector(server);
		connector.setPort(ServerConfig.webServerHttpPort());
		server.addConnector(connector);

		context = new Z8ContextHandler();
		context.setResourceBase(ServerConfig.webServerWebapp().getAbsolutePath());
		context.setContextPath("/");

		server.setHandler(context);

		// Specify the Session ID Manager
		//SessionIdManager idmanager = new DefaultSessionIdManager(server);
		//server.setSessionIdManager(idmanager);
	}

	public void start() {
		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException("WebServer start failed", e);
		}
	}

	public void stop() throws RemoteException {
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
		new WebServer2().start();
	}

}
