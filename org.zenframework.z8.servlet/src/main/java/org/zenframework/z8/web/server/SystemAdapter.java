package org.zenframework.z8.web.server;

import org.zenframework.z8.web.servlet.Servlet;

import javax.servlet.http.HttpServletRequest;

public class SystemAdapter extends Adapter {

	static private final String AdapterPath = "/request.json";

	public SystemAdapter(Servlet servlet) {
		super(servlet);
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getPathInfo().endsWith(AdapterPath);
	}
}
