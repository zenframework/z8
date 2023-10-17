package org.zenframework.z8.web.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.types.file;

public class LogoutAdapter extends Adapter {

	static private final String AdapterPath = "/logout";

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().endsWith(AdapterPath);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response, Map<String, String> parameters, List<file> files, ISession session) throws IOException {
		ServerConfig.authorityCenter().logout(session);

		HttpSession httpSession = request.getSession();

		if (httpSession != null)
			httpSession.invalidate();

		response.sendRedirect("/");
	}

}
