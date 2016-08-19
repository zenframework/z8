package org.zenframework.z8.compiler.core;

public interface ITypeCastSet {
	IMethod getContext();

	void setContext(IMethod context);

	int getWeight();

	ITypeCast[] get();

	void add(ITypeCast typeCast);

	void getCode(CodeGenerator codeGenerator, ILanguageElement[] elements);
}
