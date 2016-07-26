package org.zenframework.z8.web.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.web.servlet.Servlet;

public class TrustedAuthAdapter extends Adapter {

	private static final String AdapterPath = "/trusted.json";
	private static final String PARAM_LOGIN = "login";

	public TrustedAuthAdapter(Servlet servlet) {
		super(servlet);
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().equals(AdapterPath) && isRequestTrusted(request);
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		ISession session = null;
		String login = new String(Base64.decodeBase64(request.getParameter(PARAM_LOGIN)), "UTF-8");
		String error = null;
		if (login != null) {
			try {
				session = ServerConfig.authorityCenter().login(login);
			} catch (Throwable e) {
				error = e.getMessage();
			}
		}
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		if (session == null) {
			response.getWriter().print("{ success:false, error:\"" + error + "\" }");
		} else {
			response.getWriter().print("{ success:true, sessionId:\"" + session.id() + "\" }");
		}
	}

	private boolean isTrustLocalOnly() {
		return ServerConfig.get("z8.servlet.trustLocalOnly", false);
	}

	private boolean isRequestTrusted(HttpServletRequest request) {
		try {
			return !isTrustLocalOnly() || InetAddress.getByName(request.getRemoteAddr()).isLoopbackAddress();
		} catch (UnknownHostException e) {
			return false;
		}
	}

}
