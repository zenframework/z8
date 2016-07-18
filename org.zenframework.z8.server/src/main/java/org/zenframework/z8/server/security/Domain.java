package org.zenframework.z8.server.security;

import org.zenframework.z8.server.types.string;

public class Domain {
	private final String name;
	private final String login;

	public Domain(string name, string login) {
		this(name.get(), login.get());
	}

	public Domain(String name, String login) {
		this.name = name;
		this.login = login;
	}

	public String getName() {
		return name;
	}

	public IUser getSystemUser() {
		return User.load(login);
	}
}
