package org.zenframework.z8.server.base.table.system;

import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.guid;

public class Domain {

	private final guid id;
	private final String address;
	private final IUser systemUser;

	public Domain(guid id, String address, IUser systemUser) {
		this.id = id;
		this.address = address;
		this.systemUser = systemUser;
	}

	public guid getId() {
		return id;
	}

	public String getAddress() {
		return address;
	}

	public IUser getSystemUser() {
		return systemUser;
	}

}
