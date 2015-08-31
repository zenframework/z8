package org.zenframework.z8.compiler.core;

public interface IMethod extends IMember {
    boolean isVirtual();

    boolean isNative();

    void openLocalScope();

    void closeLocalScope();

    void addLocalVariable(IVariable variable);

    IVariable findLocalVariable(String name);

    int getParametersCount();

    IVariable[] getParameters();

    IVariableType[] getParameterTypes();

    String[] getParameterNames();

    IPosition getNamePosition();

    ILanguageElement getBody();
}