package org.zenframework.z8.web.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.web.servlet.Servlet;

/**
 * User authorization through kerberos protocol
 */
public class SingleSignOnAdapter extends Adapter {
	static public final String AdapterPath = "/sso_auth";

	public SingleSignOnAdapter(Servlet servlet) {
		super(servlet);
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().equals(AdapterPath);
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		HttpSession httpSession = request.getSession();

		String principalName = (String) httpSession.getAttribute("userPrincipalName");
		if (principalName == null) {
			httpSession.invalidate();
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		String login = principalName.contains("@") ? principalName.split("@")[0] : principalName;
		Session session;
		try {
			session = ServerConfig.authorityCenter().trustedLogin(getLoginParameters(login, request), true);
		} catch (AccessDeniedException e) {
			httpSession.invalidate();
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		} catch (Throwable e) {
			httpSession.invalidate();
			Trace.logError(e.getMessage(), e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		if (useContainerSession)
			httpSession.setAttribute(Json.session.get(), session.getId());

		request.getRequestDispatcher("/index.html").forward(request, response);
	}
}
