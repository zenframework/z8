package org.zenframework.z8.web.server;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.web.servlet.Servlet;

public class SiteAdapter extends SystemAdapter {
	static private final String Site = "site";
	static private final String AdapterPath = "/site.json";

	private final Set<String> siteRequests = new HashSet<String>();

	public SiteAdapter(Servlet servlet) {
		super(servlet);

		String requests = servlet.getInitParameter(Site);
		if(requests != null) {
			for(String siteRequest : requests.split(",[\\n|\\r|\\s]*"))
				siteRequests.add(siteRequest);
		}
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().endsWith(AdapterPath);
	}

	protected ISession login(String login, String password) throws IOException, ServletException {
		return ServerConfig.authorityCenter().siteLogin(login, password);
	}

	protected ISession authorize(String session, String server, String request) throws IOException, ServletException {
		return session != null || siteRequests.contains(request) ? ServerConfig.authorityCenter().siteServer(session, server) : null;
	}
}
