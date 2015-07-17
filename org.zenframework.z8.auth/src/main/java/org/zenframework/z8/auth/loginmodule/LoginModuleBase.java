package org.zenframework.z8.auth.loginmodule;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.zenframework.z8.server.resources.Resources;

public class LoginModuleBase implements LoginModule
{
	protected Subject subject;
	protected CallbackHandler callbackHandler;

	protected boolean loggedIn = false;

	@Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
	        Map<String, ?> sharedState, Map<String, ?> options) {
		this.subject = subject;
		this.callbackHandler = callbackHandler;
	}

	@Override
    public boolean login() throws LoginException {
		throw new LoginException(Resources.get("Exception.accessDenied"));
	}

	@Override
    public boolean commit() throws LoginException {
		return loggedIn;
	}

	@Override
    public boolean abort() throws LoginException {
		if (loggedIn) {
			logout();
		}
		return loggedIn;
	}

	@Override
    public boolean logout() throws LoginException {
		subject.getPrincipals().clear();
		loggedIn = false;
		return true;
	}
}
