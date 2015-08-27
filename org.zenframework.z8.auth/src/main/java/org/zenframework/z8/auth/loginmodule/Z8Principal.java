package org.zenframework.z8.auth.loginmodule;

import java.security.Principal;

public class Z8Principal implements Principal
{
	private String login;
	private String password;

	public Z8Principal(String login, String password)
	{
		this.login = login;
		this.password = password;
	}

	@Override
    public String toString()
	{
		return "Z8 user: " + getName();
	}

	@Override
    public String getName()
	{
		return login;
	}

	public String getPassword()
	{
		return password;
	}
}
