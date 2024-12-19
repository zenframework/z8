package org.zenframework.z8.web.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.types.file;
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
		IAuthorityCenter remoteAutCenter = remoteServer.authorityCenter();
		if(!remoteAutCenter.checkRemoteToken(token, localDomain, login))
			throw new AccessDeniedException();

		return ServerConfig.authorityCenter().trustedLogin(getLoginParameters(login, request, true), true);
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response, Map<String, String> parameters, List<file> files, ISession session) throws IOException {
		try {
			URI uri = new URI(request.getRequestURI());
			response.sendRedirect(uri.getScheme() + "://" + uri.getAuthority());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	private boolean contains(String[] strings, String value) {
		for(String string : strings)
			if(string.equals(value))
				return true;
		
		return false;
	}
}
