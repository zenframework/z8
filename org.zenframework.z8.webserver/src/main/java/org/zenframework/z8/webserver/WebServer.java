package org.zenframework.z8.webserver;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IWebServer;
import org.zenframework.z8.server.engine.RmiServer;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.webserver.spnego.MultiSpnegoLoginService;
import org.zenframework.z8.webserver.spnego.SpnegoAuthenticator;
import org.zenframework.z8.webserver.spnego.SpnegoLoginService;
import org.zenframework.z8.webserver.trusted.TrustedAuthenticator;
import org.zenframework.z8.webserver.trusted.TrustedLoginService;

/**
 * {@link WebServer launch(org.zenframework.z8.server.config.ServerConfig)} method is the entrypoint,
 * do not forget to override it in descendants
 */
public class WebServer extends RmiServer implements IWebServer {
	private static final String Id = guid.create().toString();

	private static final String Spnego = "SPNEGO";
	private static final String Multispnego = "MULTISPNEGO";
	private static final String Trusted = "TRUSTED";
	private static final String SAML = "SAML";
	private static final String OAuth = "OAuth";

	private String AddressPattern = "\\s*(?<value>.+?)@(?<key>.+?)\\s*(;|$)";

	protected Server server;
	protected ContextHandler context;
	protected Z8Handler z8Handler;

	static private WebServer instance;

	public WebServer() throws RemoteException {
		super(ServerConfig.webServerPort());
		configureServer();
		instance = this;
	}

	static public WebServer getInstance() {
		return instance;
	}

	/**
	 * The method is an extension point to configure jetty server
	 */
	protected void configureServer() {
		server = new Server();

		ServerConnector connector = new ServerConnector(server);
		connector.setPort(ServerConfig.webServerHttpPort());
		server.addConnector(connector);

		HttpConfiguration connectorHttpConfiguration = connector.getConnectionFactory(HttpConnectionFactory.class).getHttpConfiguration();
		connectorHttpConfiguration.setRequestHeaderSize(ServerConfig.webServerRequestHeaderSize());
		connectorHttpConfiguration.setResponseHeaderSize(ServerConfig.webServerResponseHeaderSize());

		context = new ContextHandler("/");
		context.setResourceBase(ServerConfig.webServerWebapp().getAbsolutePath());
		context.getServletContext();

		server.setHandler(context);

		// Specify the Session ID Manager
		SessionIdManager idmanager = new DefaultSessionIdManager(server);
		server.setSessionIdManager(idmanager);

		// Create the SessionHandler (wrapper) to handle the sessions
		SessionHandler sessions = new SessionHandler() {
			/**
			 * The method additionally extracts from the session
			 * {@link Authentication} object to put it in the request
			 */
			@Override
			public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
				// TODO Проверить, нужно ли
				Authentication auth = (Authentication)baseRequest.getSession().getAttribute("authentication");
				if(auth != null)
					baseRequest.setAuthentication(auth);
				super.doHandle(target, baseRequest, request, response);
			}
		};
		context.setHandler(sessions);

		GzipHandler gzipHandler = new GzipHandler();
		gzipHandler.setHandler(z8Handler = new Z8Handler(context));
		gzipHandler.addIncludedMimeTypes(ServerConfig.webServerGzipMimeTypes());
		gzipHandler.addIncludedMethods(ServerConfig.webServerGzipMethods());
		gzipHandler.addIncludedPaths(ServerConfig.webServerGzipPaths());

		// Wrap with security handler
		HandlerWrapper securityHandler = getSecurityHandler();
		if(securityHandler != null) {
			securityHandler.setHandler(gzipHandler);
			sessions.setHandler(securityHandler);
		} else {
			sessions.setHandler(gzipHandler);
		}

		context.setErrorHandler(new Z8ErrorHandler());
	}

	@Override
	public void start() {
		try {
			super.start();
			server.start();
		} catch(Exception e) {
			throw new RuntimeException("WebServer start failed", e);
		}
	}

	@Override
	public void stop() throws RemoteException {
		super.stop();
		if(server != null) {
			try {
				server.stop();
			} catch(Exception e) {
				Trace.logError("Couldn't stop web server", e);
			}
		}
	}

	@Override
	public String id() throws RemoteException {
		return Id;
	}

	@Override
	public void probe() throws RemoteException {
	}

	public Z8Handler getZ8Handler() {
		return z8Handler;
	}

	public Map<String, String> getTrustedUsersAddresses() {
		Map<String, String> map = new HashMap<String, String>();

		Matcher matcher = Pattern.compile(AddressPattern).matcher(ServerConfig.trustedUsersAddresses());
		while(matcher.find())
			map.put(matcher.group("key"), matcher.group("value"));

		return Collections.unmodifiableMap(map);
	}

	protected SecurityHandler getSecurityHandler() {
		String ssoAuthenticator = ServerConfig.webServerSsoAuthenticator();

		if(ssoAuthenticator.isEmpty() || ssoAuthenticator.equalsIgnoreCase(SAML) || ssoAuthenticator.equalsIgnoreCase(OAuth))
			return null;

		String domainRealm = ServerConfig.webServerSsoDomainRealm();

		Constraint constraint = new Constraint();
		constraint.setName(Constraint.__SPNEGO_AUTH);
		constraint.setRoles(new String[] { domainRealm });
		constraint.setAuthenticate(true);

		ConstraintMapping cm = new ConstraintMapping();
		cm.setConstraint(constraint);
		cm.setPathSpec("/sso");

		LoginService loginService;
		Authenticator authenticator;

		if(ssoAuthenticator.equalsIgnoreCase(Spnego)) {
			loginService = new SpnegoLoginService(domainRealm);
			authenticator = new SpnegoAuthenticator();
		} else if(ssoAuthenticator.equalsIgnoreCase(Multispnego)) {
			loginService = new MultiSpnegoLoginService(domainRealm, ServerConfig.spnegoMultiConfig());
			authenticator = new SpnegoAuthenticator();
		} else if(ssoAuthenticator.equalsIgnoreCase(Trusted)) {
			loginService = new TrustedLoginService(domainRealm, getTrustedUsersAddresses());
			authenticator = new TrustedAuthenticator();
		} else {
			throw new RuntimeException("Unknown SSO authenticator: " + ssoAuthenticator + ". Supported authenticators: " +
					Arrays.asList(Spnego, Multispnego, Trusted, SAML, OAuth));
		}

		ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
		securityHandler.setAuthenticator(authenticator);
		securityHandler.setLoginService(loginService);
		securityHandler.setConstraintMappings(new ConstraintMapping[] { cm });
		securityHandler.setRealmName(domainRealm);

		return securityHandler;
	}
}
