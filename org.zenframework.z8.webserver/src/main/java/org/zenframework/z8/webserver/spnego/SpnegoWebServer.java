package org.zenframework.z8.webserver.spnego;

import java.rmi.RemoteException;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.SpnegoLoginService;
import org.eclipse.jetty.util.security.Constraint;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IWebServer;
import org.zenframework.z8.web.server.SingleSignOnAdapter;

/**
 * This server supports kerberos authentication
 */
public class SpnegoWebServer extends org.zenframework.z8.webserver.WebServer implements IWebServer {

	public SpnegoWebServer() throws RemoteException {
		super();
	}

	@Override
	protected SecurityHandler getSecurityHandler() {
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
		securityHandler.setConstraintMappings(new ConstraintMapping[] { cm });
		securityHandler.setRealmName(domainRealm);

		return securityHandler;
	}

	public static void launch(ServerConfig config) throws Exception {
		new SpnegoWebServer().start();
	}
}
