package org.zenframework.z8.compiler.parser.type.members;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IInitializer;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IStatement;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.statements.CompoundStatement;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.parser.variable.Variable;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.util.Set;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public abstract class AbstractMethod extends LanguageElement implements IMethod {
	private IVariableType variableType;
	private String signature;
	private CompoundStatement body;

	@SuppressWarnings("unused")
	private IToken leftBrace;
	private IToken rightBrace;

	private IToken staticToken;
	private IToken accessToken;
	private IToken virtualToken;
	private boolean isVirtual;

	private Variable[] parameters;

	private List<Set<IVariable>> localScopes;

	public AbstractMethod(Variable[] parameters, IToken leftBrace, IToken rightBrace) {
		this.parameters = parameters == null ? new Variable[0] : parameters;
		this.leftBrace = leftBrace;
		this.rightBrace = rightBrace;
	}

	public AbstractMethod(IVariableType variableType, Variable[] parameters, IToken leftBrace, IToken rightBrace) {
		this(parameters, leftBrace, rightBrace);
		this.variableType = variableType;
	}

	protected void setVariableType(IVariableType variableType) {
		this.variableType = variableType;
	}

	@Override
	abstract public IPosition getNamePosition();

	@Override
	public IPosition getSourceRange() {
		if(body != null) {
			return getPosition().union(body.getSourceRange());
		}
		return getPosition();
	}

	@Override
	public IPosition getPosition() {
		return variableType.getSourceRange().union(rightBrace.getPosition());
	}

	@Override
	public IToken getFirstToken() {
		return getFirstToken(super.getFirstToken(), getFirstToken(accessToken, getFirstToken(virtualToken, getFirstToken(staticToken, variableType.getFirstToken()))));
	}

	@Override
	public IInitializer getInitializer() {
		return null;
	}

	public void setBody(CompoundStatement body) {
		this.body = body;
	}

	@Override
	public int hashCode() {
		return getSignature().hashCode();
	}

	@Override
	public IVariableType getVariableType() {
		return variableType;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public boolean isNative() {
		IType type = getDeclaringType();

		if(type.isNative()) {
			return true;
		}

		String signature = getSignature();

		type = type.getBaseType();

		while(type != null) {
			if(type.isNative() && type.getMethod(signature) != null) {
				return true;
			}
			type = type.getBaseType();
		}

		return false;
	}

	@Override
	public int getParametersCount() {
		return parameters.length;
	}

	@Override
	public IVariable[] getParameters() {
		return parameters;
	}

	@Override
	public IVariableType[] getParameterTypes() {
		IVariableType[] types = new IVariableType[getParametersCount()];

		for(int i = 0; i < parameters.length; i++) {
			types[i] = parameters[i].getVariableType();
		}

		return types;
	}

	@Override
	public String[] getParameterNames() {
		String[] types = new String[getParametersCount()];

		for(int i = 0; i < parameters.length; i++) {
			types[i] = parameters[i].getName();
		}

		return types;
	}

	@Override
	public String getUserName() {
		String name = getName() + "(";

		if(parameters != null) {
			boolean comma = false;
			for(IVariable parameter : parameters) {
				name += (comma ? ", " : "") + parameter.getVariableType().getSignature() + " " + parameter.getName();
				comma = true;
			}
		}

		return name + ")";
	}

	@Override
	public String getSignature() {
		if(signature == null) {
			signature = getName() + "(";

			if(parameters != null) {
				boolean comma = false;
				for(IVariable parameter : parameters) {
					signature += (comma ? ", " : "") + parameter.getSignature();
					comma = true;
				}
			}

			signature += ")";
		}
		return signature;
	}

	@Override
	public boolean isStatic() {
		return staticToken != null;
	}

	@Override
	public boolean isPublic() {
		return accessToken == null || accessToken.getId() == IToken.PUBLIC;
	}

	@Override
	public boolean isProtected() {
		return accessToken != null && accessToken.getId() == IToken.PROTECTED;
	}

	@Override
	public boolean isPrivate() {
		return accessToken != null && accessToken.getId() == IToken.PRIVATE;
	}

	@Override
	public boolean isVirtual() {
		return isVirtual;
	}

	public void setStatic(IToken token) {
		this.staticToken = token;
	}

	public void setAccess(IToken token) {
		this.accessToken = token;
	}

	public void setVirtual(IToken token) {
		this.virtualToken = token;
	}

	@Override
	public void openLocalScope() {
		if(localScopes == null) {
			localScopes = new ArrayList<Set<IVariable>>();
		}

		localScopes.add(0, new Set<IVariable>());
	}

	@Override
	public void closeLocalScope() {
		localScopes.remove(0);

		if(localScopes.size() == 0) {
			localScopes = null;
		}
	}

	@Override
	public void addLocalVariable(IVariable variable) {
		localScopes.get(0).add(variable);
	}

	@Override
	public IVariable findLocalVariable(String name) {
		if(localScopes != null) {
			for(Set<IVariable> scope : localScopes) {
				IVariable variable = scope.get(name);

				if(variable != null) {
					return variable;
				}
			}
		}
		return null;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {

		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		VariableType variableType = (VariableType)getVariableType();

		if(!variableType.resolveTypes(compilationUnit, declaringType))
			return false;

		if(parameters != null) {
			for(IVariable e : parameters) {
				Variable parameter = (Variable)e;
				parameter.resolveTypes(compilationUnit, declaringType);
			}
		}

		if(body == null && !declaringType.isNative()) {
			setFatalError(getPosition(), "This method requires a body instead of a semicolon");
			return false;
		}

		if(body != null) {
			return body.resolveTypes(compilationUnit, declaringType);
		}

		return true;
	}

	@Override
	public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveStructure(compilationUnit, declaringType))
			return false;

		VariableType variableType = (VariableType)getVariableType();

		if(!variableType.resolveStructure(compilationUnit, declaringType))
			return false;

		IMethod method = declaringType.findMethod(getSignature());

		if(method != null) {
			if(method.getDeclaringType() == declaringType) {
				setFatalError(method.getPosition(), "Duplicate method " + getSignature() + " in type " + declaringType.getUserName());
				setFatalError(getPosition(), "Duplicate method " + getSignature() + " in type " + declaringType.getUserName());
				return false;
			}

			if(!variableType.compare(method.getVariableType())) {
				setFatalError(getPosition(), "The return type is incompatible with " + method.getDeclaringType().getUserName() + "." + method.getSignature());
				return false;
			}

			if(!method.isStatic() && isStatic()) {
				if(method.isVirtual()) {
					setError(getPosition(), "The method " + method.getVariableType().getSignature() + " " + getSignature() + " in type " + method.getDeclaringType().getUserName() + " is virtual and cannot be overriden with a static method");
				} else {
					setError(getPosition(), "The method " + method.getVariableType().getSignature() + " " + getSignature() + " in type " + method.getDeclaringType().getUserName() + " is not static and cannot be overriden with a static method");
				}
			} else if(method.isStatic() && !isStatic()) {
				setError(getPosition(), "The method " + method.getVariableType().getSignature() + " " + getSignature() + " in type " + method.getDeclaringType().getUserName() + " is static and cannot be overriden with a non-static method");
			} else if(!method.isStatic() && !isStatic()) {
				isVirtual = method.isVirtual();

				if(!method.isVirtual()) {
					setError(getPosition(), "The method " + method.getVariableType().getSignature() + " " + getSignature() + " in type " + method.getDeclaringType().getUserName() + " is not virtual and cannot be overriden");
				}
			}
		} else {
			isVirtual = virtualToken != null;
		}

		declaringType.addMethod(this);

		if(method != null) {
			compilationUnit.addHyperlink(getNamePosition(), method.getCompilationUnit(), method.getNamePosition());
		}

		if(isStatic() && declaringType.getContainerType() != null) {
			setError(staticToken.getPosition(), "The modifier static cannot be used inside a nested type");
		}

		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		VariableType variableType = (VariableType)getVariableType();

		if(!variableType.checkSemantics(compilationUnit, declaringType, this, null, null))
			return false;

		if(body == null) {
			return true;
		}

		body.setAutoOpenScope(false);

		openLocalScope();

		if(parameters != null) {
			for(Variable parameter : parameters) {
				if(parameter.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null)) {
					addLocalVariable(parameter);
				}
			}
		}

		boolean result = body.checkSemantics(compilationUnit, declaringType, this, null, null);

		closeLocalScope();

		IStatement statement = (IStatement)body;

		IType voidType = Primary.resolveType(compilationUnit, Primary.Void);

		if(!statement.returnsOnAllControlPaths() && variableType.getType() != voidType) {
			setError(getPosition(), "Method must return value of type " + variableType.getSignature());
		}

		return result;
	}

	@Override
	public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveNestedTypes(compilationUnit, declaringType))
			return false;

		VariableType variableType = (VariableType)getVariableType();

		if(!variableType.resolveNestedTypes(compilationUnit, declaringType))
			return false;

		if(body != null) {
			return body.resolveNestedTypes(compilationUnit, declaringType);
		}
		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		if(body == null)
			throw new UnsupportedOperationException();

		body.getClassCode(codeGenerator);

		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append((staticToken != null ? "static " : "") + "public ");

		VariableType variableType = (VariableType)getVariableType();

		variableType.getCode(codeGenerator);

		codeGenerator.append(" " + getJavaName() + "(");

		if(parameters != null) {
			boolean comma = false;
			for(IVariable parameter : parameters) {
				Variable variable = (Variable)parameter;
				codeGenerator.append(comma ? ", " : "");
				variable.getCode(codeGenerator);
				comma = true;
			}
		}

		codeGenerator.append(")");
		codeGenerator.breakLine();
		codeGenerator.indent();
		body.getCode(codeGenerator);
	}

	@Override
	public ILanguageElement getBody() {
		return body;
	}

	@Override
	public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
		variableType.replaceTypeName(parent, type, newTypeName);

		for(Variable parameter : parameters) {
			parameter.replaceTypeName(parent, type, newTypeName);
		}

		if(body != null) {
			body.replaceTypeName(parent, type, newTypeName);
		}
	}
}
