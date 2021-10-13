package org.zenframework.z8.webserver;

import java.rmi.RemoteException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IWebServer;
import org.zenframework.z8.server.engine.RmiServer;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;

public class WebServer extends RmiServer implements IWebServer {

	private static final String ID = guid.create().toString();

	private Server server;
	private Handler handler;

	public WebServer() throws RemoteException {
		super(ServerConfig.webServerPort());
		configure();
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
		if (handler != null)
			handler.destroy();
	}

	@Override
	public String id() throws RemoteException {
		return ID;
	}

	@Override
	public void probe() throws RemoteException {}

	protected void configure() {
		ContextHandler context = new ContextHandler("/");
		context.setResourceBase(ServerConfig.webServerWebapp().getAbsolutePath());

		// Create the SessionHandler (wrapper) to handle the sessions
		SessionHandler sessions = new SessionHandler(new HashSessionManager());
		context.setHandler(sessions);

		handler = createHandler(context);

		// Put handler inside of SessionHandler
		sessions.setHandler(handler);

		server = new Server(ServerConfig.webServerHttpPort());
		server.setHandler(context);

		// Specify the Session ID Manager
		server.setSessionIdManager(new HashSessionIdManager());
	}

	protected Handler createHandler(ContextHandler context) {
		return new WebServerHandler(context);
	}

	public static void launch(ServerConfig config) throws Exception {
		new WebServer().start();
	}

}
