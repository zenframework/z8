package org.zenframework.z8.webserver.spnego;

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
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlets.gzip.GzipHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IWebServer;
import org.zenframework.z8.web.server.SingleSignOnAdapter;

/**
 * This server supports kerberos authentication
 */
public class SpnegoWebServer extends org.zenframework.z8.webserver.WebServer implements IWebServer {
	// TODO temporary until upgrade jetty up to 9.4
	protected ConstraintSecurityHandler securityHandler;

	public SpnegoWebServer() throws RemoteException {
		super();
	}

	protected void configureServer() {
		super.configureServer();
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

		securityHandler = new ConstraintSecurityHandler();
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
		gzipHandler.setHandler(getZ8Handler());
		gzipHandler.addIncludedMimeTypes(ServerConfig.webServerGzipMimeTypes());
		gzipHandler.addIncludedMethods(ServerConfig.webServerGzipMethods());
		gzipHandler.addIncludedPaths(ServerConfig.webServerGzipPaths());

		securityHandler.setHandler(gzipHandler);
		sessions.setHandler(securityHandler);
	}

	public static void launch(ServerConfig config) throws Exception {
		new SpnegoWebServer().start();
	}
}
