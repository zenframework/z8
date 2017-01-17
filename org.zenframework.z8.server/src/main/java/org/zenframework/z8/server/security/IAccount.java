package org.zenframework.z8.server.security;

import java.io.Serializable;

import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.types.guid;

public interface IAccount extends RmiSerializable, Serializable {
	public guid id();

	public String login();
	public String password();

	public String firstName();
	public String lastName();

	public boolean banned();

	public IUser user();
}
