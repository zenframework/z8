package org.zenframework.z8.compiler.core;

public interface IMember extends IVariable, ILanguageElement {
    boolean isStatic();

    boolean isPublic();

    boolean isPrivate();

    boolean isProtected();

    @Override
    IType getDeclaringType();

    IInitializer getInitializer();
}
