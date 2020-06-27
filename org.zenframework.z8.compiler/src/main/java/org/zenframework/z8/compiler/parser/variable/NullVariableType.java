package org.zenframework.z8.compiler.parser.variable;

import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.expressions.QualifiedName;
import org.zenframework.z8.compiler.parser.type.TypeCast;

public class NullVariableType extends VariableType {
	public NullVariableType(IToken token) {
		super(new QualifiedName(token));
	}

	@Override
	public boolean isNull() {
		return true;
	}

	@Override
	public ITypeCast getCastTo(IType context, IVariableType candidate) {
		return new TypeCast(this, candidate, 0);
	}
}
