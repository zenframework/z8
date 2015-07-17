package org.zenframework.z8.compiler.file;

import org.eclipse.core.runtime.IPath;

public class FileException extends Exception {
    private static final long serialVersionUID = -6836702405917659626L;

    private IPath path;

    public FileException(IPath path, String message) {
        super(message);
        this.path = path;
    }

    public IPath getPath() {
        return path;
    }
}