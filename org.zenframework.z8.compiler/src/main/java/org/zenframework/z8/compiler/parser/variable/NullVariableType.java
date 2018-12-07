package org.zenframework.z8.compiler.parser.variable;

import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.parser.expressions.QualifiedName;

public class NullVariableType extends VariableType {
	public NullVariableType(IToken token) {
		super(new QualifiedName(token));
	}

	@Override
	public boolean isNull() {
		return true;
	}
}
