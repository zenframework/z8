package org.zenframework.z8.compiler.error;

import org.eclipse.core.resources.IResource;

import org.zenframework.z8.compiler.core.IPosition;

public class BuildWarning extends BuildMessage {
    public BuildWarning(IResource resource, IPosition position, String description) {
        super(resource, position, description, null);
    }

    public BuildWarning(IResource resource, String description) {
        this(resource, null, description);
    }
}
