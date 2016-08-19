package org.zenframework.z8.compiler.parser.expressions;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Super extends LanguageElement {
	private IVariableType variableType;
	private IToken superToken;

	public Super(IToken token) {
		superToken = token;
	}

	@Override
	public IPosition getSourceRange() {
		return superToken.getPosition();
	}

	@Override
	public IToken getFirstToken() {
		return superToken;
	}

	@Override
	public IVariableType getVariableType() {
		return variableType;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		IType type = declaringType.getBaseType();

		if(type == null) {
			setError(superToken.getPosition(), "type " + declaringType.getUserName() + " has no base; keyword super cannot be used");
			return false;
		}

		variableType = new VariableType(getCompilationUnit(), type);

		compilationUnit.addHyperlink(superToken.getPosition(), type);
		compilationUnit.addContentProposal(superToken.getPosition(), variableType);

		if(getStaticContext()) {
			setError(getPosition(), "cannot use super in a static context");
		}

		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		codeGenerator.append("super");
	}
}
