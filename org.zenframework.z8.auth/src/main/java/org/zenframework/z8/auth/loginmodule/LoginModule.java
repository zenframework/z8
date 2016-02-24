package org.zenframework.z8.auth.loginmodule;

import java.security.Principal;

import javax.security.auth.login.LoginException;

import org.zenframework.z8.auth.AuthorityCenter;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;

public class LoginModule extends LoginModuleBase
{
	public LoginModule() {
	}

	@Override
    public boolean login() throws LoginException {
		assert (subject.getPrincipals().size() == 1);

		for (Principal p : subject.getPrincipals()) {
			Z8Principal principal = (Z8Principal) p;

			Connection connection = ConnectionManager.get();
			
			try {
			    IUser user = User.load(principal.getName(), principal.getPassword(), false, AuthorityCenter.database());
	            AuthorityCenter.getUserManager().add(user);
	            loggedIn = true;
	            return true;
			} finally {
			    connection.release();
			}

		}

		return super.login();
	}
}
