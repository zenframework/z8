package org.zenframework.z8.compiler.parser.type.members;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.KeywordToken;
import org.zenframework.z8.compiler.parser.variable.Variable;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class CastOperator extends AbstractMethod {
	private KeywordToken operatorKeyword;
	private IPosition position;

	public CastOperator(KeywordToken operatorKeyword, VariableType type, Variable[] parameters, IToken leftBrace, IToken rightBrace) {
		super(type, parameters, leftBrace, rightBrace);
		this.operatorKeyword = operatorKeyword;
		position = this.operatorKeyword.getPosition().union(rightBrace.getPosition());
	}

	@Override
	public IPosition getPosition() {
		return position;
	}

	@Override
	public IPosition getNamePosition() {
		return operatorKeyword.getPosition().union(getVariableType().getPosition());
	}

	@Override
	public IToken getFirstToken() {
		return operatorKeyword;
	}

	@Override
	public String getName() {
		return getVariableType().getSignature();
	}

	@Override
	public String getJavaName() {
		return getName();
	}

	@Override
	public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveStructure(compilationUnit, declaringType))
			return false;

		if(getParametersCount() == 0)
			getDeclaringType().addTypeCastOperator(this);
		else
			setError(getPosition(), "Type cast operator must not have parameter(s)");

		return true;
	}
}
