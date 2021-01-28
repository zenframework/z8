package org.zenframework.z8.compiler.cmd;

import org.eclipse.core.runtime.IPath;

public class DummyProject extends DummyContainer {
	private final String name;

	public DummyProject(String name, DummyContainer parent, IPath path) {
		super(parent, path, true);
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
