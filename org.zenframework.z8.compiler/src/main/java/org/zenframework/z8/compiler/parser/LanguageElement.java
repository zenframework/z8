package org.zenframework.z8.compiler.parser;

import org.eclipse.core.runtime.IPath;
import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.grammar.lexer.Position;
import org.zenframework.z8.compiler.parser.variable.Variable;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;

public abstract class LanguageElement implements ILanguageElement {
	private IAttribute[] attributes = new IAttribute[0];

	private CompilationUnit compilationUnit;
	private IType declaringType;
	private IMethod declaringMethod;
	private boolean staticContext;

	private ILanguageElement parent;

	private boolean hasError;
	private boolean hasFatalError;

	public LanguageElement() {
	}

	@Override
	public ILanguageElement getParent() {
		return parent;
	}

	@Override
	public void setParent(ILanguageElement parent) {
		this.parent = parent;
	}

	@Override
	public IAttribute[] getAttributes() {
		return attributes;
	}

	@Override
	public IAttribute getAttribute(String name) {
		for(IAttribute attribute : attributes) {
			if(attribute.getName().equals(name))
				return attribute;
		}
		return null;
	}

	@Override
	public void setAttributes(IAttribute[] attributes) {
		this.attributes = attributes;

		for(IAttribute attribute : this.attributes)
			attribute.setParent(this);
	}

	@Override
	public Project getProject() {
		return getCompilationUnit().getProject();
	}

	@Override
	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	protected void setCompilationUnit(CompilationUnit compilationUnit) {
		if(this.compilationUnit == null)
			this.compilationUnit = compilationUnit;
	}

	@Override
	public IPosition getSourceRange() {
		IAttribute[] attributes = getAttributes();

		if(attributes.length == 0)
			return null;

		if(attributes.length == 1)
			return attributes[0].getSourceRange();

		return attributes[0].getSourceRange().union(attributes[attributes.length - 1].getSourceRange());
	}

	@Override
	public IPosition getPosition() {
		return getSourceRange();
	}

	@Override
	public IToken getFirstToken() {
		return getFirstToken(attributes);
	}

	public static IToken getFirstToken(IAttribute[] attributes) {
		if(attributes.length == 0)
			return null;

		IToken token = attributes[0].getFirstToken();

		if(attributes.length == 1)
			return token;

		for(int i = 1; i < attributes.length; i++)
			token = getFirstToken(token, attributes[i].getFirstToken());

		return token;
	}

	public static IToken getFirstToken(IToken first, IToken second) {
		if(first != null && second != null)
			return first.getPosition().getOffset() < second.getPosition().getOffset() ? first : second;
		else if(first != null)
			return first;
		else if(second != null)
			return second;

		return null;
	}

	@Override
	public ILanguageElement getElementAt(int offset) {
		return getElementAt(offset, 0);
	}

	@Override
	public ILanguageElement getElementAt(int offset, int length) {
		IPosition position = new Position();
		position.setOffset(offset);
		position.setLength(length);
		return getElementAt(position);
	}

	@Override
	public ILanguageElement getElementAt(IPosition position) {
		return getElementAt(position);
	}

	@Override
	public boolean isQualifiedName() {
		return false;
	}

	@Override
	public boolean isOperatorNew() {
		return false;
	}

	@Override
	public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
	}

	@Override
	public void replaceImport(TextEdit parent, IPath oldImport, IPath newImport) {
	}

	@Override
	public IType getDeclaringType() {
		return declaringType;
	}

	protected void setDeclaringType(IType declaringType) {
		if(this.declaringType == null)
			this.declaringType = declaringType;
	}

	@Override
	public IMethod getDeclaringMethod() {
		return declaringMethod;
	}

	protected void setDeclaringMethod(IMethod declaringMethod) {
		if(this.declaringMethod == null)
			this.declaringMethod = declaringMethod;
	}

	@Override
	public boolean getStaticContext() {
		if(!staticContext) {
			IMethod method = getDeclaringMethod();
			return method != null ? method.isStatic() : false;
		}

		return true;
	}

	@Override
	public void setStaticContext(boolean context) {
		staticContext = context;
	}

	protected void initialize(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod) {
		setCompilationUnit(compilationUnit);
		setDeclaringType(declaringType);
		setDeclaringMethod(declaringMethod);
	}

	@Override
	public boolean hasError() {
		return hasError;
	}

	@Override
	public boolean hasFatalError() {
		return hasFatalError;
	}

	@Override
	public void setError(IPosition position, String message) {
		hasError = true;
		getCompilationUnit().error(position, message);
	}

	public void setWarning(IPosition position, String message) {
		hasError = true;
		getCompilationUnit().warning(position, message);
	}

	@Override
	public void setFatalError(IPosition position, String message) {
		hasFatalError = true;
		setError(position, message);
	}

	@Override
	public IVariable getVariable() {
		return new Variable(getVariableType());
	}

	@Override
	public IVariableType getVariableType() {
		return null;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		initialize(compilationUnit, declaringType, null);

		if(!hasFatalError()) {
			for(IAttribute attribute : getAttributes())
				attribute.resolveTypes(compilationUnit, declaringType);
		}
		return !hasFatalError();
	}

	@Override
	public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
		initialize(compilationUnit, declaringType, null);

		if(!hasFatalError()) {
			for(IAttribute attribute : getAttributes())
				attribute.resolveStructure(compilationUnit, declaringType);
		}

		return !hasFatalError();
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		initialize(compilationUnit, declaringType, declaringMethod);

		if(!hasFatalError()) {
			for(IAttribute attribute : getAttributes())
				attribute.checkSemantics(compilationUnit, declaringType, declaringMethod, leftHandValue, context);
		}
		return !hasFatalError();
	}

	@Override
	public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
		initialize(compilationUnit, declaringType, null);

		if(!hasFatalError()) {
			for(IAttribute attribute : getAttributes())
				attribute.resolveNestedTypes(compilationUnit, declaringType);
		}

		return !hasFatalError();
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
	}

	@Override
	public void getClassCode(CodeGenerator codeGenerator) {
	}

	@Override
	public void getStaticBlock(CodeGenerator codeGenerator) {
	}

	@Override
	public void getStaticConstructor(CodeGenerator codeGenerator) {
	}

	@Override
	public void getConstructor(CodeGenerator codeGenerator) {
	}

	@Override
	public void getConstructor1(CodeGenerator codeGenerator) {
	}

	@Override
	public void getConstructor2(CodeGenerator codeGenerator) {
	}

}