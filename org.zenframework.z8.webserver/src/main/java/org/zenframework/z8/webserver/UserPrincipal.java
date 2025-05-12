package org.zenframework.z8.webserver;

import java.security.Principal;

public class UserPrincipal implements Principal {

	private final String name;

	public UserPrincipal(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

}