package org.zenframework.z8.server.security;

import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class Domain {
	private String name;
	private guid user;
	private boolean isOwner;

	static private Domain system;
	
	static public Domain system() {
		if(system == null)
			system = new Domain(Domains.DefaultDomain.get(), Users.System, true);
		return system;
	}
	
	public Domain(string name, guid user, bool owner) {
		this(name.get(), user, owner.get());
	}

	public Domain(String name, guid user, boolean isOwner) {
		this.name = name;
		this.user = user;
		this.isOwner = isOwner;
	}

	public String getName() {
		return name;
	}

	public IUser getSystemUser() {
		return user.equals(guid.Null) ? system().getSystemUser() : User.load(user);
	}

	public boolean isOwner() {
		return isOwner;
	}
}
