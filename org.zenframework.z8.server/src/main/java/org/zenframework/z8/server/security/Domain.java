package org.zenframework.z8.server.security;

import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class Domain {
	private String address;
	private guid user;
	private boolean isOwner;

	static private Domain system;
	
	static public Domain system() {
		if(system == null)
			system = new Domain(Domains.DefaultDomain.get(), Users.System, true);
		return system;
	}
	
	public Domain(string address, guid user, bool owner) {
		this(address.get(), user, owner.get());
	}

	public Domain(String address, guid user, boolean isOwner) {
		this.address = address;
		this.user = user;
		this.isOwner = isOwner;
	}

	public String getAddress() {
		return address;
	}

	public IUser getSystemUser() {
		return user.isNull() ? system().getSystemUser() : User.read(user, Database.getDefault());
	}

	public boolean isOwner() {
		return isOwner;
	}
}
