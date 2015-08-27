package org.zenframework.z8.server.security;

import java.io.Serializable;
import java.util.Map;

import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public interface IUser extends Serializable {
    public guid id();

    public String name();

    public String password();

    public String description();

    public boolean blocked();

    public String phone();

    public String email();

    public String settings();

    public void setSettings(String settings);

    public SecurityGroup securityGroup();

    public Component[] components();
    public void setComponents(Component[] components);

    public void save(Database database);

    public Map<String, IForm> forms();
    public Map<string, primary> parameters();
}
