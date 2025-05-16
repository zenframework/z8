package org.zenframework.z8.webserver.trusted;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.eclipse.jetty.server.UserIdentity;
import org.zenframework.z8.webserver.AbstractLoginService;
import org.zenframework.z8.webserver.UserPrincipal;

public class TrustedLoginService extends AbstractLoginService {

	private final Map<String, String> users = new HashMap<String, String>();

	public TrustedLoginService(String name, Map<String, String> users) {
		super(name);
		this.users.putAll(users);
	}

	@Override
	public UserIdentity login(String username, Object credentials, ServletRequest request) {
		username = users.get(request.getLocalName());
		return username != null ? newUserIdentity(new UserPrincipal(username)) : null;
	}
}
