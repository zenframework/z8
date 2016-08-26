package org.zenframework.z8.compiler.cmd;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class DummyProject extends DummyResource {
	private final String name;

	public DummyProject(String name, IResource parent, IPath path) {
		super(parent, path, true);
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
