package org.zenframework.z8.webserver;

import java.security.Principal;

import javax.security.auth.Subject;

import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

public abstract class AbstractLoginService extends AbstractLifeCycle implements LoginService {

	protected IdentityService identityService;
	protected String name;

	public AbstractLoginService(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean validate(UserIdentity user) {
		return false;
	}

	@Override
	public IdentityService getIdentityService() {
		return identityService;
	}

	@Override
	public void setIdentityService(IdentityService service) {
		identityService = service;
	}

	@Override
	public void logout(UserIdentity user) {}

	protected UserIdentity newUserIdentity(Principal principal) {
		Subject subject = new Subject();
		subject.getPrincipals().add(principal);

		return identityService.newUserIdentity(subject, principal, new String[] { name });
	}

}
