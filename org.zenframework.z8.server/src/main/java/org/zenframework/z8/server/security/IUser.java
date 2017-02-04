package org.zenframework.z8.server.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public interface IUser extends RmiSerializable, Serializable {
	public guid id();

	public String login();
	public String password();

	public String name();
	public String firstName();
	public String middleName();
	public String lastName();
	public String fullName();

	public String description();

	public boolean banned();

	public String phone();

	public String email();

	public String settings();
	public void setSettings(String settings);

	public Collection<IRole> roles();
	public IPrivileges privileges();

	public boolean isAdministrator();

	public Collection<Entry> entries();
	public void setEntries(Collection<Entry> entries);

	public Map<string, primary> parameters();
}
