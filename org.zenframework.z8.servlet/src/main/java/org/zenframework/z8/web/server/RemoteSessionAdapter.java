package org.zenframework.z8.web.server;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.security.IUser;

public class RemoteSessionAdapter extends Adapter {
	static public final String AdapterPath = "/remote";
	static public final String DomainParametr = "domain";

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().equals(AdapterPath);
	}
	
	@Override
	protected ISession authorize(HttpServletRequest request, Map<String, String> parameters) throws IOException {
		ISession currentSession = super.authorize(request, parameters);
		String remoteDomain = parameters.get(DomainParametr);
		
		if(remoteDomain == null || Domains.newInstance().isOwner(remoteDomain))
			return currentSession;
		
		IApplicationServer remoteServer = ServerConfig.interconnectionCenter().connect(remoteDomain);
		
		if(remoteServer == null)
			throw new AccessDeniedException();

		IUser user = currentSession.user();
		return remoteServer.authorityCenter().trustedLogin(getLoginParameters(user.login(), request, true), true);
	}

}
