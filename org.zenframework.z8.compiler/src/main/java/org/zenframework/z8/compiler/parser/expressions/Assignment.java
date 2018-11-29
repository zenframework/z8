package org.zenframework.z8.compiler.parser.expressions;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.OperatorToken;
import org.zenframework.z8.compiler.parser.variable.LeftHandValue;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Assignment extends Initialization {
	public Assignment(ILanguageElement left, OperatorToken operatorToken, ILanguageElement right) {
		super(left, operatorToken, right);
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		ILanguageElement left = getLeftElement();
		if(!(left.getVariable() instanceof LeftHandValue)) {
			setError(left.getPosition(), "The left-hand side of an assignment must be a variable");
			return false;
		}

		return true;
	}
}
