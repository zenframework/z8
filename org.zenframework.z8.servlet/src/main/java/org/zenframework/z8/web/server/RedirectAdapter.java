package org.zenframework.z8.web.server;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.server.crypto.Crypto;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.file;

public class RedirectAdapter extends Adapter {
	static private final String AdapterPath = "/redirect";

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().equals(AdapterPath);
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response, Map<String, String> parameters, List<file> files, ISession session) throws IOException {
		String remoteDomain = parameters.get("domain");
		String url = parameters.get("url");
		String login = parameters.get("login");
		
		if(remoteDomain == null)
			throw new RuntimeException("Domain is null");
		if(url == null)
			throw new RuntimeException("Url is null");
		if(login == null || login.isEmpty())
			login = session.user().login();

		JsonObject json = new JsonObject();
		json.put(Json.domain.get(), remoteDomain);
		json.put(Json.login.get(), login);
		json.put(Json.time.get(), System.currentTimeMillis());
		String encrypt = Crypto.Default.encrypt(json.toString());
		String urlJson = URLEncoder.encode(encrypt, StandardCharsets.UTF_8.name());

		if(response != null)
			response.sendRedirect(url + (url.endsWith("/") ? "" : "/") + "accept?"
									  + "json=" + urlJson);
	}
}
