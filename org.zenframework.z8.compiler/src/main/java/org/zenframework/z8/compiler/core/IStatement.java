package org.zenframework.z8.compiler.core;

public interface IStatement {
    boolean returnsOnAllControlPaths();

    boolean breaksControlFlow();
}
