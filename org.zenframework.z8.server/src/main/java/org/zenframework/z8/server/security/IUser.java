package org.zenframework.z8.server.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public interface IUser extends RmiSerializable, Serializable {
	public guid id();

	public String name();

	public String password();

	public String description();

	public boolean blocked();

	public String phone();

	public String email();

	public String settings();

	public void setSettings(String settings);

	public Collection<guid> roles();
	public boolean isAdministrator();

	public Collection<Component> components();

	public void setComponents(Collection<Component> components);

	public void save(Database database);

	public Map<string, primary> parameters();
}
