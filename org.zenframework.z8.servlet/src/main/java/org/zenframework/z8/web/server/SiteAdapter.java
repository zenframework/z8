package org.zenframework.z8.web.server;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.security.IAccount;
import org.zenframework.z8.server.types.file;
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

	protected void service(ISession session, Map<String, String> parameters, List<file> files, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String requestId = parameters.get(Json.request);

		if(requestId == null || Json.login.equals(requestId)) {
			JsonWriter writer = new JsonWriter();

			writer.startObject();
			writer.writeProperty(Json.session, session.id());
			writer.writeProperty(Json.success, true);
			writer.writeInfo();

			IAccount account = session.account();

			writer.startObject(Json.account);
			writer.writeProperty(Json.id, account.id());
			writer.writeProperty(Json.login, account.login());
			writer.writeProperty(Json.firstName, account.firstName());
			writer.writeProperty(Json.lastName, account.lastName());
			writer.finishObject();

			writer.finishObject();

			writeResponse(response, writer.toString());
		} else
			super.service(session, parameters, files, request, response);
	}

}
