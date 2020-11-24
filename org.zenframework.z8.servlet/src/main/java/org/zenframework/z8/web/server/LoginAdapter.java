package org.zenframework.z8.web.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.web.servlet.Servlet;

public class LoginAdapter extends Adapter {

	static private final String AdapterPath = "/login";
	static private final String Forward = "forward";

	public LoginAdapter(Servlet servlet) {
		super(servlet);
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().endsWith(AdapterPath);
	}
	
	@Override
	protected void service(ISession session, Map<String, String> parameters, List<file> files, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String forward = parameters.get(Forward);
		response.sendRedirect(forward != null ? forward : "/");
	}

}
