package org.zenframework.z8.web.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IAuthorityCenter;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class RedirectAdapter extends Adapter {
	static private final String AdapterPath = "/redirect";

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().equals(AdapterPath);
	}
	
	/*@Override
	protected void service(HttpServletRequest request, HttpServletResponse response, Map<String, String> parameters, List<file> files) throws IOException {
		service(request, response, parameters, files, null);
	}*/
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response, Map<String, String> parameters, List<file> files, ISession session) throws IOException {
		String remoteDomain = parameters.get("domain");
		String url = parameters.get("url");
		String[] localDomains = ServerConfig.applicationServer().domains();
		
		if(remoteDomain == null || localDomains.length < 2 || contains(localDomains, remoteDomain)) {
			super.service(request, response, parameters, files, session);
			return;
		}
		if(url == null)
			throw new AccessDeniedException();
		
		IApplicationServer remoteServer = ServerConfig.interconnectionCenter().connect(remoteDomain);
		if(remoteServer == null)
			throw new AccessDeniedException();
		IAuthorityCenter auth = ServerConfig.authorityCenter();
		IUser user = session.user();
		guid token = guid.create();
		auth.addRemoteToken(token, remoteDomain, user);
		
		if(response != null)
			//response.sendRedirect("https://ivk.ru");
			response.sendRedirect(url + (url.endsWith("/") ? "" : "/") + "accept?"
									  + "&token=" + token.toString()
									  + "&login=" + user.login()
									  + "&srcDomain=" + localDomains[1]
									  + "&dstDomain=" + remoteDomain);
	}
	
	private boolean contains(String[] strings, String value) {
		for(String string : strings)
			if(string.equals(value))
				return true;
		
		return false;
	}
}
