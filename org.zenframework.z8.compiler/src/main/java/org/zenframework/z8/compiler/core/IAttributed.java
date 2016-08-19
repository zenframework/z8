package org.zenframework.z8.compiler.core;

public interface IAttributed {
	IAttribute[] getAttributes();

	void setAttributes(IAttribute[] attributes);

	IAttribute getAttribute(String name);
}
