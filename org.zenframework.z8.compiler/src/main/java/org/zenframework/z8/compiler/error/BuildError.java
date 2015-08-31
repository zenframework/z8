package org.zenframework.z8.compiler.error;

import org.eclipse.core.resources.IResource;

import org.zenframework.z8.compiler.core.IPosition;

public class BuildError extends BuildMessage {
    public BuildError(IResource resource, IPosition position, String description, Throwable throwable) {
        super(resource, position, description, throwable);
    }
}
