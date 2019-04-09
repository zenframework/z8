package org.zenframework.z8.compiler.parser.statements;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IStatement;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.BuiltinNative;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class ForEachStatement extends LanguageElement implements IStatement {
	private IToken forToken;

	private Declarator declarator;
	private ILanguageElement array;
	private ILanguageElement statement;

	public ForEachStatement(IToken forToken, Declarator declarator, ILanguageElement expression, ILanguageElement statement) {
		this.forToken = forToken;
		this.declarator = declarator;
		this.array = expression;
		this.statement = statement;

		this.statement.setParent(this);
	}

	@Override
	public IPosition getSourceRange() {
		return forToken.getPosition().union(statement.getSourceRange());
	}

	@Override
	public IToken getFirstToken() {
		return forToken;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		declarator.resolveTypes(compilationUnit, declaringType);
		array.resolveTypes(compilationUnit, declaringType);
		statement.resolveTypes(compilationUnit, declaringType);

		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		declaringMethod.openLocalScope();

		declarator.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
		boolean success = array.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
		statement.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);

		declaringMethod.closeLocalScope();

		if(!success)
			return false;

		IVariableType arrayType = array.getVariableType();

		if(!arrayType.isArray()) {
			setError(array.getPosition(), "The type of the expression must be an array type but it resolved to " + arrayType.getSignature());
			return false;
		}

		IVariableType left = declarator.getVariableType();
		left.addRightKey(null);
		ITypeCast typeCast = arrayType.getCastTo(left);
		left.removeRightKey();

		if(typeCast == null) {
			setError(array.getPosition(), "Type mismatch: cannot convert from " + arrayType.getSignature() + " to " + left.getSignature());
			return false;
		}

		return true;
	}

	@Override
	public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
		return super.resolveNestedTypes(compilationUnit, declaringType) && statement.resolveNestedTypes(compilationUnit, declaringType);
	}

	@Override
	public boolean returnsOnAllControlPaths() {
		((IStatement)statement).returnsOnAllControlPaths();
		return false;
	}

	@Override
	public boolean breaksControlFlow() {
		return false;
	}

	@Override
	public void getClassCode(CodeGenerator codeGenerator) {
		declarator.getClassCode(codeGenerator);
		statement.getClassCode(codeGenerator);
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		codeGenerator.getCompilationUnit().setLineNumbers(getSourceRange().getLine(), codeGenerator.getCurrentLine());

		codeGenerator.append("for(");
		declarator.getCode(codeGenerator, true);
		codeGenerator.append(" : ");

		IVariableType variableType = declarator.getVariableType();

		if(!variableType.isArray())
			codeGenerator.append('(' + BuiltinNative.Array + '<' +  variableType.getDeclaringJavaName() + '>' + ')');

		array.getCode(codeGenerator);
		codeGenerator.append(")");
		codeGenerator.breakLine();

		boolean braces = statement instanceof CompoundStatement;

		if(!braces)
			codeGenerator.incrementIndent();

		codeGenerator.indent();
		statement.getCode(codeGenerator);

		if(!braces)
			codeGenerator.decrementIndent();
	}
}
