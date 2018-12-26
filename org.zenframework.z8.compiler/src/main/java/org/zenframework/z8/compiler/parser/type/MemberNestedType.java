package org.zenframework.z8.compiler.parser.type;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IInitializer;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.expressions.QualifiedName;
import org.zenframework.z8.compiler.parser.type.members.TypeBody;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class MemberNestedType extends AbstractType implements IInitializer {
	private IToken classToken;

	private QualifiedName name;
	private TypeBody body;

	public MemberNestedType(QualifiedName name, IToken classToken, TypeBody body) {
		super();

		this.name = name;
		this.classToken = classToken;
		this.body = body;
	}

	@Override
	public IPosition getSourceRange() {
		if(body != null)
			return name.getSourceRange().union(body.getSourceRange());
		return name.getSourceRange();
	}

	@Override
	public IToken getFirstToken() {
		return getFirstToken(super.getFirstToken(), name.getFirstToken());
	}

	@Override
	public IToken getNameToken() {
		return classToken;
	}

	@Override
	public IPosition getPosition() {
		return name.getPosition();
	}

	@Override
	public String getLeftName() {
		return name.toString();
	}

	@Override
	public String getRightName() {
		return null;
	}

	@Override
	public ILanguageElement getLeftElement() {
		return name;
	}

	@Override
	public ILanguageElement getRightElement() {
		return null;
	}

	@Override
	public IToken getOperator() {
		return null;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		setUserName(name.toString());
		setJavaName(compilationUnit.createUniqueName());

		return body != null ? body.resolveTypes(compilationUnit, this) : true;
	}

	@Override
	public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveStructure(compilationUnit, declaringType))
			return false;
		return body != null ? body.resolveStructure(compilationUnit, this) : true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(!name.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(name.getVariableType().isStatic()) {
			setError(name.getPosition(), name.toString() + ": undeclared identifier");
			return false;
		}

		IType baseType = name.getVariableType().getType();
		setBaseType(baseType);

		if(baseType != null)
			compilationUnit.addHyperlink(classToken.getPosition(), baseType);

		String userName = name.toString();

		boolean result = true;

		if(declaringType.getNestedType(userName) != null) {
			setError(name.getPosition(), "second local class definition found");
			result = false;
		}

		setContainerType(declaringType);
		declaringType.addNestedType(this);
		declaringType.addInitializer(this);

		IVariableType variableType = name.getVariableType();

		if(variableType.isArray() || variableType.isEnum()) {
			setError(getPosition(), "Variables of array or enum type cannot be initialized in this manner");
			result = false;
		}

		return (body != null ? body.checkSemantics(compilationUnit, this, null, null, null) : true) && result;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		// local class context
		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("public static class " + getJavaName() + " extends " + getBaseType().getNestedJavaName());
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
	public void getConstructor(CodeGenerator codeGenerator) {
		codeGenerator.indent();
		name.getCode(codeGenerator);
		codeGenerator.append(" = " + getVariableType().getJavaNew(getStaticContext()));
		codeGenerator.append(';');
		codeGenerator.breakLine();
	}

	@Override
	public TypeBody getTypeBody() {
		return body;
	}
}
