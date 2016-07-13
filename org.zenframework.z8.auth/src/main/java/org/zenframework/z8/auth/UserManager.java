package org.zenframework.z8.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.security.IUser;

public class UserManager
{
	private Map<String, IUser> users = Collections
	        .synchronizedMap(new HashMap<String, IUser>());

	public UserManager() {
	}

	public void add(IUser user) {
		users.put(user.name().toLowerCase(), user);
	}

	public IUser get(String login) {
		return users.get(login.toLowerCase());
	}
}
