package org.zenframework.z8.web.server;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.exceptions.AccessDeniedException;

public class SingleSignOnAdapter extends Adapter {
	static public final String AdapterPath = "/sso";

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().equals(AdapterPath);
	}

	@Override
	protected ISession authorize(HttpServletRequest request, Map<String, String> parameters) throws IOException {
		HttpSession httpSession = request.getSession();

		String principalName = (String) httpSession.getAttribute("userPrincipalName");

		if (principalName == null)
			throw new AccessDeniedException();

		return ServerConfig.authorityCenter().trustedLogin(getLoginParameters(getLogin(principalName), request, true), true);
	}

	private static String getLogin(String principalName) {
		return principalName.contains("@") ? principalName.split("@")[0] : principalName;
	}
}
