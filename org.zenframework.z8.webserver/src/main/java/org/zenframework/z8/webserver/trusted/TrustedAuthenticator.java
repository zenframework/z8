package org.zenframework.z8.webserver.trusted;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.authentication.DeferredAuthentication;
import org.eclipse.jetty.security.authentication.LoginAuthenticator;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Authentication.User;
import org.eclipse.jetty.server.UserIdentity;

public class TrustedAuthenticator extends LoginAuthenticator {

	public static final String METHOD = "TRUSTED";

	@Override
	public String getAuthMethod() {
		return METHOD;
	}

	@Override
	public boolean secureResponse(ServletRequest req, ServletResponse res, boolean mandatory, User user)
			throws ServerAuthException {
		return true;
	}

	@Override
	public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory)
			throws ServerAuthException {
		if (!mandatory)
			return new DeferredAuthentication(this);

		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;

		UserIdentity user = login(null, null, request);

		if (user != null)
			return new UserAuthentication(getAuthMethod(), user);

		if (DeferredAuthentication.isDeferred(response))
			return Authentication.UNAUTHENTICATED;

		try {
			response.setHeader(HttpHeader.WWW_AUTHENTICATE.asString(), "trusted realm=\"" + _loginService.getName() + "\"");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return Authentication.SEND_CONTINUE;
		} catch (IOException e) {
			throw new ServerAuthException(e);
		}
	}

}
