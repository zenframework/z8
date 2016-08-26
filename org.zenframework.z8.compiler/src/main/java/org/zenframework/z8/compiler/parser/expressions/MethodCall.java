package org.zenframework.z8.compiler.parser.expressions;

import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IJavaTypeCast;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.ITypeCastSet;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.parser.type.TypeCastSet;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class MethodCall extends LanguageElement implements IJavaTypeCast {
	ILanguageElement context;

	private IToken nameToken;
	private IToken leftBrace;
	private ILanguageElement[] arguments;
	private IToken rightBrace;

	private IVariableType variableType;
	private ITypeCastSet typeCastSet;

	private boolean javaCastPending = true;

	public MethodCall(ILanguageElement context, IToken nameToken, ILanguageElement[] arguments, IToken leftBrace, IToken rightBrace) {
		this.context = context;

		this.nameToken = nameToken;
		this.arguments = arguments;

		this.leftBrace = leftBrace;
		this.rightBrace = rightBrace;
	}

	@Override
	public IPosition getSourceRange() {
		if(rightBrace != null)
			return nameToken.getPosition().union(rightBrace.getPosition());
		else if(arguments.length > 0)
			return nameToken.getPosition().union(arguments[arguments.length - 1].getSourceRange());
		else
			return nameToken.getPosition().union(leftBrace.getPosition());
	}

	@Override
	public IToken getFirstToken() {
		return nameToken;
	}

	@Override
	public IVariableType getVariableType() {
		return variableType;
	}

	private String makeSignatureList(IVariableType[] variableTypes) {
		StringBuffer buffer = new StringBuffer();

		buffer.append('(');

		for(int i = 0; i < variableTypes.length; i++) {
			if(i != 0)
				buffer.append(", ");
			buffer.append(variableTypes[i].getSignature());
		}

		return buffer.toString() + ')';
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		if(context != null && !context.resolveTypes(compilationUnit, declaringType))
			return false;

		boolean result = true;

		for(ILanguageElement argument : arguments)
			result &= argument.resolveTypes(compilationUnit, declaringType);

		return result;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType contextType) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(context != null && !context.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		IVariableType[] argumentTypes = new IVariableType[arguments.length];

		boolean parametersChecked = true;

		for(int i = 0; i < arguments.length; i++)
			parametersChecked &= arguments[i].checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);

		if(!parametersChecked)
			return false;

		for(int i = 0; i < arguments.length; i++)
			argumentTypes[i] = arguments[i].getVariableType();

		contextType = context != null ? context.getVariableType() : new VariableType(getCompilationUnit(), declaringType);

		IMethod[] methods = contextType.getMatchingMethods(nameToken.getRawText());

		if(methods.length == 0) {
			setError(nameToken.getPosition(), "The method " + nameToken.getRawText() + makeSignatureList(argumentTypes) + " is undefined for the type " + contextType.getSignature());
			return false;
		}

		List<ITypeCastSet> availableCasts = new ArrayList<ITypeCastSet>();

		for(IMethod method : methods) {
			IVariableType[] parameters = method.getParameterTypes();

			if(parameters.length != argumentTypes.length)
				continue;

			ITypeCastSet candidates = new TypeCastSet();
			candidates.setContext(method);

			boolean argumentsCast = true;

			for(int i = 0; i < parameters.length; i++) {
				ITypeCast typeCast = argumentTypes[i].getCastTo(parameters[i]);

				if(typeCast == null) {
					argumentsCast = false;
					break;
				}

				candidates.add(typeCast);
			}

			if(argumentsCast)
				availableCasts.add(candidates);
		}

		ITypeCastSet[] result = TypeCastSet.findBestCast(availableCasts.toArray(new ITypeCastSet[availableCasts.size()]));

		if(result.length == 0) {
			setError(getPosition(), "The method " + methods[0].getSignature() + " in the type " + contextType.getSignature() + " is not applicable for the arguments " + makeSignatureList(argumentTypes));
			addHyperlink(methods[0]);
			return false;
		} else if(result.length != 1) {
			setError(getPosition(), "The method " + result[0].getContext().getSignature() + " is ambiguous for the type " + contextType.getSignature());
			addHyperlink(methods[0]);
			return false;
		}

		IMethod method = result[0].getContext();
		addHyperlink(method);

		if(contextType.isStatic() && !method.isStatic())
			setError(getPosition(), "Cannot make a static reference to the non-static method " + method.getSignature());

		if(context != null && !contextType.isStatic() && method.isStatic())
			setWarning(getPosition(), "The static method " + contextType.getSignature() + '.' + method.getSignature() + " should be accessed in a static way");

		if(context == null && getStaticContext() && !method.isStatic())
			setError(getPosition(), "Cannot make a static reference to the non-static method " + method.getSignature() + " from " + method.getDeclaringType().getUserName());

		IType methodDeclaringType = method.getDeclaringType();

		if(methodDeclaringType != null && methodDeclaringType != declaringType) {
			if(method.isPrivate() && !methodDeclaringType.hasPrivateAccess(declaringType) || method.isProtected() && !methodDeclaringType.hasProtectedAccess(declaringType))
				setError(getPosition(), "The method " + method.getSignature() + " from the type " + methodDeclaringType.getUserName() + " is not visible");
		}

		typeCastSet = result[0];

		variableType = method.getVariableType();

		return true;
	}

	protected void addHyperlink(IMethod method) {
		CompilationUnit compilationUnit = method.getCompilationUnit();

		if(compilationUnit != null)
			getCompilationUnit().addHyperlink(nameToken.getPosition(), compilationUnit, method.getNamePosition());
	}

	@Override
	public void setCastPending(boolean castPending) {
		javaCastPending = castPending;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		IVariableType variableType = getVariableType();

		if(context != null) {
			boolean needCast = javaCastPending && context.getVariableType().isArray() && !variableType.isArray() && !variableType.getType().getUserName().equals(Primary.Void);

			if(needCast) {
				codeGenerator.getCompilationUnit().importType(variableType.getType());
				codeGenerator.append("((" + variableType.getDeclaringJavaName() + ")");
			}

			boolean needGet = !(context instanceof Super) && !context.getVariableType().isStatic() && context.getVariableType().isReference();

			context.getCode(codeGenerator);
			codeGenerator.append(".");

			if(needGet)
				codeGenerator.append("get(" + getDeclaringType().getConstructionStage() + ").");

			typeCastSet.getCode(codeGenerator, arguments);

			if(needCast)
				codeGenerator.append(")");
		} else
			typeCastSet.getCode(codeGenerator, arguments);
	}

	public String getSignature() {
		List<IVariableType> types = new ArrayList<IVariableType>();

		for(ILanguageElement argument : arguments)
			types.add(argument.getVariableType());

		return nameToken.getRawText() + makeSignatureList(types.toArray(new IVariableType[0]));
	}

	public ILanguageElement getContext() {
		return context;
	}

	public ILanguageElement[] getArguments() {
		return arguments;
	}
}
