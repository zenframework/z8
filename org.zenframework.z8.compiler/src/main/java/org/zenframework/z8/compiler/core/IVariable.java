package org.zenframework.z8.compiler.core;

public interface IVariable extends IAttributed, ISource {
	IVariableType getVariableType();

	String getName();

	String getJavaName();

	String getUserName();

	String getSignature();

	boolean isFinal();
}
