package org.zenframework.z8.webserver.trusted;

import java.security.Principal;

import javax.security.auth.Subject;

import org.eclipse.jetty.server.UserIdentity;

public class TrustedUserIdentity implements UserIdentity {

	private final String name;

	public TrustedUserIdentity(String name) {
		this.name = name;
	}

	@Override
	public Subject getSubject() {
		return new Subject();
	}

	@Override
	public Principal getUserPrincipal() {
		return new Principal() {
			@Override
			public String getName() {
				return name;
			}
		};
	}

	@Override
	public boolean isUserInRole(String role, Scope scope) {
		return true;
	}

}
