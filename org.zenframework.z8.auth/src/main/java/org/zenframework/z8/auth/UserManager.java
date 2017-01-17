package org.zenframework.z8.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.security.IAccount;
import org.zenframework.z8.server.security.IUser;

public class UserManager {
	private Map<String, IUser> users = Collections.synchronizedMap(new HashMap<String, IUser>());
	private Map<String, IAccount> accounts = Collections.synchronizedMap(new HashMap<String, IAccount>());

	public UserManager() {
	}

	public void add(IUser user) {
		users.put(user.login().toLowerCase(), user);
	}

	public void add(IAccount account) {
		accounts.put(account.login().toLowerCase(), account);
	}

	public IUser user(String login) {
		return users.get(login.toLowerCase());
	}

	public IAccount account(String login) {
		return accounts.get(login.toLowerCase());
	}
}
