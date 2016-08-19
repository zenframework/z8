package org.zenframework.z8.compiler.parser.statements;

import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IStatement;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class ForStatement extends LanguageElement implements IStatement {
	private IToken forToken;

	private ILanguageElement init;
	private ILanguageElement expression;
	private LoopCondition condition;
	private ILanguageElement statement;

	public ForStatement(IToken forToken, ILanguageElement init, ILanguageElement condition, ILanguageElement expression, ILanguageElement statement) {
		this.forToken = forToken;
		this.init = init;
		this.condition = new LoopCondition(condition);
		this.expression = expression;
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

		if(init != null)
			init.resolveTypes(compilationUnit, declaringType);

		condition.resolveTypes(compilationUnit, declaringType);
		expression.resolveTypes(compilationUnit, declaringType);
		statement.resolveTypes(compilationUnit, declaringType);

		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		declaringMethod.openLocalScope();

		if(init != null) {
			init.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
		}

		condition.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
		expression.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
		statement.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);

		declaringMethod.closeLocalScope();

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
		if(init != null) {
			init.getClassCode(codeGenerator);
		}
		statement.getClassCode(codeGenerator);
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		boolean scope = init instanceof Declarator;

		if(scope) {
			codeGenerator.append("{");
			codeGenerator.breakLine();

			codeGenerator.incrementIndent();

			codeGenerator.indent();
			init.getCode(codeGenerator);
			codeGenerator.append(";");
			codeGenerator.breakLine();

			codeGenerator.getCompilationUnit().setLineNumbers(getSourceRange().getLine(), codeGenerator.getCurrentLine());

			codeGenerator.indent();
			codeGenerator.append("for(; ");
		} else {
			codeGenerator.getCompilationUnit().setLineNumbers(getSourceRange().getLine(), codeGenerator.getCurrentLine());

			codeGenerator.append("for(");

			if(init != null) {
				init.getCode(codeGenerator);
			}
			codeGenerator.append("; ");
		}

		condition.getCode(codeGenerator);
		codeGenerator.append("; ");
		expression.getCode(codeGenerator);
		codeGenerator.append(")");
		codeGenerator.breakLine();

		boolean braces = statement instanceof CompoundStatement;

		if(!braces)
			codeGenerator.incrementIndent();

		codeGenerator.indent();
		statement.getCode(codeGenerator);

		if(!braces)
			codeGenerator.decrementIndent();

		if(scope) {
			codeGenerator.decrementIndent();
			codeGenerator.indent();
			codeGenerator.append("}");
			codeGenerator.breakLine();
		}
	}

	@Override
	public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
		if(init != null) {
			init.replaceTypeName(parent, type, newTypeName);
		}

		condition.replaceTypeName(parent, type, newTypeName);
		expression.replaceTypeName(parent, type, newTypeName);
		statement.replaceTypeName(parent, type, newTypeName);
	}

}
