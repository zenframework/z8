package org.zenframework.z8.compiler.parser.type.members;

import org.zenframework.z8.compiler.content.HyperlinkKind;
import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IInitializer;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.grammar.lexer.Lexer;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class EnumElement extends LanguageElement implements IMember {
	private IToken nameToken;

	public EnumElement(IToken nameToken) {
		this.nameToken = nameToken;
	}

	@Override
	public IPosition getSourceRange() {
		return nameToken.getPosition();
	}

	@Override
	public IToken getFirstToken() {
		return nameToken;
	}

	@Override
	public IInitializer getInitializer() {
		return null;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public String getName() {
		return nameToken.getRawText();
	}

	@Override
	public String getJavaName() {
		return getName();
	}

	@Override
	public String getUserName() {
		return getName();
	}

	@Override
	public String getSignature() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IVariableType getVariableType() {
		return new VariableType(getCompilationUnit(), getDeclaringType());
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public boolean isPublic() {
		return true;
	}

	@Override
	public boolean isProtected() {
		return false;
	}

	@Override
	public boolean isPrivate() {
		return false;
	}

	@Override
	public boolean isFinal() {
		return true;
	}

	@Override
	public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveStructure(compilationUnit, declaringType))
			return false;

		String name = nameToken.getRawText();
		IPosition position = nameToken.getPosition();

		if(!Lexer.checkIdentifier(name)) {
			setFatalError(position, "Syntax error on token '" + name + "'. " + name + " is a reserved keyword.");
			return false;
		}

		IMember member = declaringType.findMember(name);

		if(member != null)
			setError(position, name + ": redefinition of enumerator");
		else
			declaringType.addMember(this);

		compilationUnit.addHyperlink(position, compilationUnit, position, HyperlinkKind.Enum);
		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		codeGenerator.indent();
		codeGenerator.append(getJavaName() + ",");
		codeGenerator.breakLine();
	}
}
