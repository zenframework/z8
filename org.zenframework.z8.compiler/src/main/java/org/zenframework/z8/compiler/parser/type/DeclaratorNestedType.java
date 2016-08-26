package org.zenframework.z8.compiler.parser.type;

import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.statements.Declarator;
import org.zenframework.z8.compiler.parser.type.members.TypeBody;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class DeclaratorNestedType extends AbstractType {
	private TypeBody body;
	private Declarator declarator;

	public DeclaratorNestedType(IToken finalToken, VariableType type, IToken name, TypeBody body) {
		super();

		this.body = body;
		this.declarator = new Declarator(finalToken, type, name, null, null);
	}

	@Override
	public IPosition getSourceRange() {
		return declarator.getSourceRange().union(body.getSourceRange());
	}

	@Override
	public IPosition getPosition() {
		return declarator.getPosition();
	}

	@Override
	public IToken getFirstToken() {
		return declarator.getFirstToken();
	}

	@Override
	public IVariableType getVariableType() {
		return declarator.getVariableType();
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		setUserName(declarator.getName());
		setJavaName(compilationUnit.createUniqueName());

		if(!declarator.resolveTypes(compilationUnit, declaringType))
			return false;

		IVariableType variableType = declarator.getVariableType();

		if(!variableType.isReference()) {
			setFatalError(getPosition(), "The type " + variableType.getSignature() + " cannot have a subtype; a supertype must be a type");
			return false;
		}

		if(body != null)
			body.resolveTypes(compilationUnit, this);

		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(!declarator.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		IVariableType variableType = declarator.getVariableType();

		setBaseType(variableType.getType());
		setContainerType(declaringType);
		declaringType.addNestedType(this);

		return true;
	}

	@Override
	public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveNestedTypes(compilationUnit, declaringType))
			return false;

		if(!declarator.resolveNestedTypes(compilationUnit, declaringType))
			return false;

		if(body != null) {
			body.resolveStructure(compilationUnit, this);
			body.checkSemantics(compilationUnit, this, null, null, null);
			body.resolveNestedTypes(compilationUnit, this);
		}

		return true;
	}

	@Override
	public void getClassCode(CodeGenerator codeGenerator) {
		codeGenerator.indent();
		codeGenerator.append("public static class " + getJavaName() + " extends " + getBaseType().getJavaName());
		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("{");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		if(!extendsPrimary())
			generateClassCode(codeGenerator);

		body.getCode(codeGenerator);

		codeGenerator.decrementIndent();

		codeGenerator.indent();
		codeGenerator.append("};");
		codeGenerator.breakLine();
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		declarator.getCode(codeGenerator, true);

		IVariableType type = super.getVariableType();

		codeGenerator.append(" = " + type.getJavaNew(getStaticContext()) + ";");
		codeGenerator.breakLine();

		IAttribute[] attributes = getAttributes();

		for(IAttribute attribute : attributes) {
			codeGenerator.indent();
			codeGenerator.append(declarator.getName() + "." + "setAttribute(" + '"' + attribute.getName() + '"' + ", ");
			attribute.getCode(codeGenerator);
			codeGenerator.append(");");
			codeGenerator.breakLine();
		}
	}

	@Override
	public TypeBody getTypeBody() {
		return body;
	}

	@Override
	public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
		super.replaceTypeName(parent, type, newTypeName);

		declarator.replaceTypeName(parent, type, newTypeName);

		if(body != null)
			body.replaceTypeName(parent, type, newTypeName);
	}
}
