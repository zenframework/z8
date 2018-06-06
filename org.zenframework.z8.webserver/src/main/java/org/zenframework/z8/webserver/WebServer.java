package org.zenframework.z8.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.rmi.RemoteException;
import java.util.Enumeration;
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
import org.zenframework.z8.server.engine.IServer;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.web.servlet.Servlet;

public class WebServer implements IServer {

	private static final Log LOG = LogFactory.getLog(WebServer.class);

	private static final String ID = guid.create().toString();

	private static final String PROP_WEB_SERVER_PORT = "web.server.port";
	private static final String PROP_WEB_SERVER_WEBAPP = "web.server.webapp";
	private static final String PROP_WEB_SERVER_MAPPINGS = "web.server.content.map";

	private static final int DEFAULT_WEB_SERVER_PORT = 80;
	private static final String DEFAULT_WEB_SERVER_WEBAPP = "..";

	private Server server;
	private ContextHandler context;
	private Servlet servlet;
	private File webapp;
	private Properties mappings;

	@Override
	public void start() {
		try {

			webapp = new File(System.getProperty(PROP_WEB_SERVER_WEBAPP, DEFAULT_WEB_SERVER_WEBAPP));
			mappings = getMappings(System.getProperty(PROP_WEB_SERVER_MAPPINGS));

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
					return new Enumeration<String>() {
						@Override
						public boolean hasMoreElements() {
							return false;
						}

						@Override
						public String nextElement() {
							return null;
						}
					};
				}

				@Override
				public String getInitParameter(String name) {
					return null;
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
					String path = baseRequest.getRequestURI();
					baseRequest.setServletPath(path);
					LOG.debug("REQUEST: " + path);
					response.setCharacterEncoding("UTF-8");
					response.setContentType(getContentType(path));
					if (path.endsWith(".json") || path.startsWith("/storage/") || path.startsWith("/files/")
							|| path.startsWith("/table/") || path.startsWith("/reports/")) {
						servlet.service(request, response);
					} else {
						if (path.contains("..") || path.startsWith("/WEB-INF"))
							throw new ServletException("Illegal path " + path);
						File file = new File(webapp, path);
						if (file.isDirectory())
							file = new File(file, "index.html");
						InputStream in = new FileInputStream(file);
						OutputStream out = response.getOutputStream();
						try {
							IOUtils.copy(in, out);
						} finally {
							IOUtils.closeQuietly(in);
							IOUtils.closeQuietly(out);
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
	public void stop() {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				LOG.error("Couldn't stop web server", e);
			}
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

}
