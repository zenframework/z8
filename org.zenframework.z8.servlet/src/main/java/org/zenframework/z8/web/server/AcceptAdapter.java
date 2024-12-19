package org.zenframework.z8.web.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.crypto.Crypto;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.file;

public class AcceptAdapter extends Adapter {
	static private final String AdapterPath = "/accept";

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().equals(AdapterPath);
	}

	@Override
	protected ISession authorize(HttpServletRequest request, Map<String, String> parameters) throws IOException {
		String jsonString = parameters.get("json");
		if(jsonString == null)
			throw new AccessDeniedException();
		JsonObject json = new JsonObject(Crypto.Default.decrypt(jsonString));
		
		if(!json.containsKey(Json.domain.get())
		|| !json.containsKey(Json.login.get())
		|| !json.containsKey(Json.time.get()))
			throw new AccessDeniedException();
		
		long jsonTime = json.getLong(Json.time.get());
		long currentTime = System.currentTimeMillis();
		if(currentTime - jsonTime >= 7000)
			throw new AccessDeniedException();
		
		String domain = json.getString(Json.domain.get());
		String login = json.getString(Json.login.get());
		String[] localDomains = ServerConfig.applicationServer().domains();
		
		if(domain == null || !contains(localDomains, domain))
			throw new AccessDeniedException();

		return ServerConfig.authorityCenter().trustedLogin(getLoginParameters(login, request, true), true);
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response, Map<String, String> parameters, List<file> files, ISession session) throws IOException {
		response.sendRedirect("/");
	}
	
	private boolean contains(String[] strings, String value) {
		for(String string : strings)
			if(string.equals(value))
				return true;
		
		return false;
	}
}
