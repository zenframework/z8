package org.zenframework.z8.compiler.error;

import org.eclipse.core.resources.IResource;

import org.zenframework.z8.compiler.core.IPosition;

abstract public class BuildMessage {
	private IPosition position;
	private IResource resource;
	private String description;
	private Throwable throwable;

	public BuildMessage(IResource resource, IPosition position, String description, Throwable throwable) {
		this.resource = resource;
		this.position = position;
		this.description = description;
		this.throwable = throwable;
	}

	public IPosition getPosition() {
		return position;
	}

	public IResource getResource() {
		return resource;
	}

	public String getDescription() {
		return description;
	}

	public Throwable getException() {
		return throwable;
	}

	public String format() {
		String text = resource.getProjectRelativePath().toString();

		IPosition position = getPosition();

		if(position != null) {
			text += " (" + position.getLine() + ", " + position.getColumn() + ")";
		}

		text += ": " + getDescription();
		return text;
	}
}
