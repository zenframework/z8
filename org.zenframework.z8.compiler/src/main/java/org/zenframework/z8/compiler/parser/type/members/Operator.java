package org.zenframework.z8.compiler.parser.type.members;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.KeywordToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.OperatorToken;
import org.zenframework.z8.compiler.parser.variable.Variable;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Operator extends AbstractMethod {
	private KeywordToken operatorKeyword;
	private OperatorToken operatorToken;

	public Operator(IVariableType type, KeywordToken operatorKeyword, OperatorToken operatorToken, Variable[] parameters, IToken leftBrace, IToken rightBrace) {
		super(type, parameters, leftBrace, rightBrace);

		this.operatorKeyword = operatorKeyword;
		this.operatorToken = operatorToken;
	}

	public Operator(IVariableType type, OperatorToken operatorToken, Variable[] parameters) {
		super(type, parameters, null, null);
		this.operatorToken = operatorToken;
	}

	@Override
	public IPosition getNamePosition() {
		return operatorKeyword.getPosition().union(operatorToken.getPosition());
	}

	public boolean isKindOf(int operator) {
		return operatorToken.getId() == operator;
	}

	@Override
	public String getName() {
		return operatorToken.getName();
	}

	@Override
	public String getJavaName() {
		return operatorToken.getJavaName();
	}

	@Override
	public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
		if(getParametersCount() > 1)
			setError(getPosition(), "Operator can not have more then 1 parameter");

		return super.resolveStructure(compilationUnit, declaringType);
	}
}
