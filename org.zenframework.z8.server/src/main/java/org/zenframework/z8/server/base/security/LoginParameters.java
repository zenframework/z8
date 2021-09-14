package org.zenframework.z8.server.base.security;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class LoginParameters extends OBJECT {
	public static class CLASS<T extends LoginParameters> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(LoginParameters.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new LoginParameters(container);
		}
	}

	private org.zenframework.z8.server.security.LoginParameters loginParameters;

	public LoginParameters(IObject container) {
		super(container);
	}

	protected void setLoginParameters(org.zenframework.z8.server.security.LoginParameters loginParameters) {
		this.loginParameters = loginParameters;
	}

	public guid z8_userId() {
		return loginParameters != null ? loginParameters.getId() : null;
	}

	public string z8_login() {
		return loginParameters != null ? new string(loginParameters.getLogin()) : null;
	}

	public string z8_address() {
		return loginParameters != null ? new string(loginParameters.getAddress()) : null;
	}

	public string z8_schema() {
		return loginParameters != null ? new string(loginParameters.getSchema()) : null;
	}

	public static LoginParameters.CLASS<LoginParameters> newInstance(org.zenframework.z8.server.security.LoginParameters loginParameters) {
		LoginParameters.CLASS<LoginParameters> instance = new LoginParameters.CLASS<LoginParameters>(null);
		instance.get().setLoginParameters(loginParameters);
		return instance;
	}

}
