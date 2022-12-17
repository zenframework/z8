package org.zenframework.z8.web.server;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutAdapter extends Adapter {

	static private final String AdapterPath = "/logout";

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().endsWith(AdapterPath);
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();

		if (session != null)
			session.invalidate();

		response.sendRedirect("/");
	}

}
