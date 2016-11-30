package org.zenframework.z8.server.base.model;

import org.zenframework.z8.server.request.INamedObject;

public class NamedObject implements INamedObject {
	protected String id;
	protected String name;

	public NamedObject() {
	}

	public NamedObject(String id) {
		this(id, null);
	}

	public NamedObject(String id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String displayName() {
		return name;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof INamedObject ? id().equals(((INamedObject)object).id()) : false;
	}

	@Override
	public int compareTo(INamedObject object) {
		return id().hashCode() - object.id().hashCode();
	}
}
