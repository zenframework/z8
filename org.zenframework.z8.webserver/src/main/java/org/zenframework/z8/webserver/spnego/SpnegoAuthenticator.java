package org.zenframework.z8.webserver.spnego;

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
import org.eclipse.jetty.util.security.Constraint;

public class SpnegoAuthenticator extends LoginAuthenticator {
	@Override
	public String getAuthMethod() {
		return Constraint.__SPNEGO_AUTH;
	}

	@Override
	public Authentication validateRequest(ServletRequest request, ServletResponse response, boolean mandatory) throws ServerAuthException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;

		String header = req.getHeader(HttpHeader.AUTHORIZATION.asString());
		String authScheme = getAuthSchemeFromHeader(header);

		if(!mandatory)
			return new DeferredAuthentication(this);

		// The client has responded to the challenge we sent previously
		if(header != null && isAuthSchemeNegotiate(authScheme)) {
			UserIdentity user = login(null, header.substring(10), request);

			if(user != null)
				return new UserAuthentication(getAuthMethod(), user);
		}

		// A challenge should be sent if any of the following cases are true:
		// 1. There was no Authorization header provided
		// 2. There was an Authorization header for a type other than Negotiate
		try {
			if(DeferredAuthentication.isDeferred(res))
				return Authentication.UNAUTHENTICATED;

			res.setHeader(HttpHeader.WWW_AUTHENTICATE.asString(), HttpHeader.NEGOTIATE.asString());
			res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return Authentication.SEND_CONTINUE;
		} catch(IOException ioe) {
			throw new ServerAuthException(ioe);
		}
	}

	protected String getAuthSchemeFromHeader(String header) {
		if(header == null || (header = header.trim()).isEmpty())
			return "";

		int index = header.indexOf(' ');
		return index != -1 ? header.substring(0, index) : header;
	}

	protected boolean isAuthSchemeNegotiate(String authScheme) {
		return HttpHeader.NEGOTIATE.asString().equalsIgnoreCase(authScheme);
	}

	@Override
	public boolean secureResponse(ServletRequest request, ServletResponse response, boolean mandatory, User validatedUser) throws ServerAuthException {
		return true;
	}
}