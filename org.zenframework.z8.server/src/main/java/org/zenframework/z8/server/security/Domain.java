package org.zenframework.z8.server.security;

import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class Domain {
	private String name;
	private guid user;

	static private Domain system;
	
	static public Domain system() {
		if(system == null)
			system = new Domain(Domains.DefaultDomain.get(), Users.System);
		return system;
	}
	
	public Domain(string name, guid user) {
		this(name.get(), user);
	}

	public Domain(String name, guid user) {
		this.name = name;
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public IUser getSystemUser() {
		return User.load(user);
	}
}
