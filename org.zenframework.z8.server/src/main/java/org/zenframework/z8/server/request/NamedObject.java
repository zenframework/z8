package org.zenframework.z8.server.request;

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
}
