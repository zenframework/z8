package org.zenframework.z8.compiler.core;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;

public interface ISource {
    CompilationUnit getCompilationUnit();

    Project getProject();

    IPosition getPosition();

    IPosition getSourceRange();

    IToken getFirstToken();
}
