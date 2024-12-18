package org.zenframework.z8.web.server;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.types.guid;

public class AcceptAdapter extends Adapter {
	static private final String AdapterPath = "/accept";

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().equals(AdapterPath);
	}

	@Override
	protected ISession authorize(HttpServletRequest request, Map<String, String> parameters) throws IOException {
		String localDomain = parameters.get("dstDomain");
		String remoteDomain = parameters.get("srcDomain");
		String login = parameters.get("login");
		guid token = new guid(parameters.get("token"));
		String[] localDomains = ServerConfig.applicationServer().domains();
		
		if(localDomain == null
		|| remoteDomain == null
		|| !contains(localDomains, localDomain)
		|| contains(localDomains, remoteDomain))
			throw new AccessDeniedException();
		
		IApplicationServer remoteServer = ServerConfig.interconnectionCenter().connect(remoteDomain);
		if(remoteServer == null)
			throw new AccessDeniedException();

		if(!remoteServer.authorityCenter().checkRemoteToken(token, remoteDomain, login))
			throw new AccessDeniedException();
		
		return ServerConfig.authorityCenter().trustedLogin(getLoginParameters(login, request, true), true);
	}
	
	private boolean contains(String[] strings, String value) {
		for(String string : strings)
			if(string.equals(value))
				return true;
		
		return false;
	}
}
