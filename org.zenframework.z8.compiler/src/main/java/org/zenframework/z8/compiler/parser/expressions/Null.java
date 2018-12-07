package org.zenframework.z8.compiler.parser.expressions;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.variable.NullVariableType;

public class Null extends LanguageElement {
	private IToken nullToken;

	public Null(IToken token) {
		nullToken = token;
	}

	@Override
	public IPosition getSourceRange() {
		return nullToken.getPosition();
	}

	@Override
	public IToken getFirstToken() {
		return nullToken;
	}

	@Override
	public IVariableType getVariableType() {
		return new NullVariableType(nullToken);
	}

	@Override
	public boolean isQualifiedName() {
		return false;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		codeGenerator.append("null");
	}
}
