package org.zenframework.z8.web.server;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.crypto.Crypto;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.types.encoding;

public class TokenAuthAdapter extends Adapter {

	private static final String AdapterPath = "/token_auth";

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().endsWith(AdapterPath);
	}

	@Override
	protected ISession authorize(HttpServletRequest request, Map<String, String> parameters) throws IOException {
		String token = parameters.getOrDefault("token", "");
		if (token.isEmpty())
			throw new AccessDeniedException();

		token = URLDecoder.decode(token, encoding.Default.toString());
		token = token.replace(" ", "+");
		token = Crypto.Default.decrypt(token);

		String[] splited = token.split(":", 2);

		String login = splited[0];
		String password = splited.length > 1 ? splited[1] : "";

		return ServerConfig.authorityCenter().login(getLoginParameters(login, request, false), password);
	}
}
