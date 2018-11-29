package org.zenframework.z8.compiler.parser.expressions;

import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.compiler.content.HyperlinkKind;
import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.variable.LeftHandValue;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class QualifiedName extends LanguageElement {
	private List<IToken> tokens;
	private List<IVariableType> variableTypes;

	private IType staticType;
	private IVariableType variableType;
	private IVariable closure;

	public QualifiedName(IToken identifier) {
		this.tokens = new ArrayList<IToken>();
		this.tokens.add(identifier);
	}

	@Override
	public boolean isQualifiedName() {
		return true;
	}

	@Override
	public IPosition getSourceRange() {
		IPosition position = tokens.get(0).getPosition();

		for(int i = 1; i < tokens.size(); i++)
			position = position.union(tokens.get(i).getPosition());

		return position;
	}

	@Override
	public IVariable getVariable() {
		return new LeftHandValue(this);
	}

	@Override
	public IVariableType getVariableType() {
		return variableType;
	}

	public IVariableType getVariableType(int index) {
		return variableTypes != null ? variableTypes.get(index) : null;
	}

	public int getTokenCount() {
		return tokens.size();
	}

	public IToken[] getTokens() {
		return tokens.toArray(new IToken[getTokenCount()]);
	}

	@Override
	public IToken getFirstToken() {
		return tokens.get(0);
	}

	public IToken getLastToken() {
		return tokens.get(getTokenCount() - 1);
	}

	public void add(IToken identifier) {
		tokens.add(identifier);
	}

	public IToken remove(int index) {
		return tokens.remove(index);
	}

	@Override
	public String toString() {
		return toString(0, -1);
	}

	public String toString(int index) {
		return toString(index, -1);
	}

	public String toString(int index, int count) {
		if(count == 0)
			return "this";

		String name = "";

		count = count == -1 ? tokens.size() : count;

		for(int i = index; i < count; i++)
			name += (i == index ? "" : ".") + tokens.get(i).getRawText();

		return name;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		staticType = compilationUnit.resolveType(toString(0, 1));

		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		int i = 0;

		IVariableType variableType = context;

		List<IVariable> variables = new ArrayList<IVariable>();
		List<IVariableType> variableTypes = new ArrayList<IVariableType>();

		HyperlinkKind hyperlinkKind = HyperlinkKind.None;

		if(variableType == null) {
			IToken nameToken = tokens.get(i);
			String name = nameToken.getRawText();

			IVariable variable = null;

			if(declaringMethod != null) {
				variable = declaringMethod.findLocalVariable(name);
				declaringMethod = declaringMethod.getDeclaringMethod();
			}

			if(variable != null)
				hyperlinkKind = HyperlinkKind.Local;

			if(variable == null && declaringType != null) {
				variable = declaringType.findMember(name);

				if(variable != null && getStaticContext() && !((IMember)variable).isStatic())
					setError(nameToken.getPosition(), "Cannot make a static reference to the non-static member " + name);

				if(variable == null) {
					if(declaringMethod != null)
						closure = variable = declaringMethod.findLocalVariable(name);
					if(variable != null)
						hyperlinkKind = HyperlinkKind.Local;
				} else
					hyperlinkKind = ((IMember)variable).isStatic() ? HyperlinkKind.StaticMember : HyperlinkKind.Member;
			}

			if(variable == null) {
				if(staticType == null) {
					setFatalError(nameToken.getPosition(), name + ": undeclared identifier");
					compilationUnit.addUnresolvedType(name);
					return false;
				}

				variableType = new VariableType(staticType, true);
				variables.add(null);
				variableTypes.add(variableType);

				compilationUnit.addContributor(staticType.getCompilationUnit());
				compilationUnit.addHyperlink(nameToken.getPosition(), staticType);
			} else {
				staticType = null;

				if(variable instanceof IMember) {
					IMember member = (IMember)variable;

					if(member.getDeclaringType() != declaringType && member.isPrivate())
						setError(nameToken.getPosition(), "The member " + member.getDeclaringType().getUserName() + '.' + name + " is not visible");
				}

				variableType = variable.getVariableType();
				variables.add(variable);
				variableTypes.add(variableType);

				compilationUnit.addHyperlink(nameToken.getPosition(), variable.getCompilationUnit(), variable.getPosition(), hyperlinkKind);
			}

			i = 1;

			compilationUnit.addContentProposal(nameToken.getPosition(), variableType);
		}

		for(; i < tokens.size(); i++) {
			IToken nameToken = tokens.get(i);
			String name = nameToken.getRawText();

			IMember member = variableType.findMember(name);

			if(member == null) {
				setFatalError(nameToken.getPosition(), name + " is not a member of " + variableType.getSignature());
				return false;
			}

			if(variableType.isStatic() && !member.isStatic()) {
				setFatalError(nameToken.getPosition(), "Cannot make a static reference to the non-static member " + name + " from " + staticType.getUserName());
				return false;
			}

			if(!variableType.isStatic() && member.isStatic())
				setWarning(nameToken.getPosition(), "The static member " + variableType.getSignature() + '.' + name + " should be accessed in a static way");

			IType memberDeclaringType = member.getDeclaringType();

			if(memberDeclaringType != declaringType) {
				if(declaringType == null) {
					if(!member.isPublic())
						setError(nameToken.getPosition(), "The member " + memberDeclaringType.getUserName() + '.' + name + " is not visible");
				} else if(member.isPrivate() && !memberDeclaringType.hasPrivateAccess(declaringType) || member.isProtected() && !memberDeclaringType.hasProtectedAccess(declaringType))
					setError(nameToken.getPosition(), "The member " + memberDeclaringType.getUserName() + '.' + name + " is not visible");
			}

			variableType = member.getVariableType();
			variableTypes.add(variableType);
			variables.add(member);

			compilationUnit.addHyperlink(nameToken.getPosition(), member.getDeclaringType().getCompilationUnit(), member.getPosition(), member.isStatic() ? HyperlinkKind.StaticMember : HyperlinkKind.Member);
			compilationUnit.addContentProposal(nameToken.getPosition(), variableType);
		}

		this.variableType = variableType;
		this.variableTypes = variableTypes;
		return true;
	}

	private boolean getClosureCode(CodeGenerator codeGenerator) {
		if(closure == null)
			return false;

		IVariableType variableType = getVariableType();

		if(!variableType.isArray())
			codeGenerator.getCompilationUnit().importType(getVariableType().getType());

		codeGenerator.append("(");
		codeGenerator.append("(" + variableTypes.get(0).getDeclaringJavaName() + ")");
		codeGenerator.append("getCLASS().getClosure()[" + closure.getClosure() + "]");
		codeGenerator.append(")");
		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		for(int i = 0; i < tokens.size(); i++) {
			if(i == 0) {
				if(getClosureCode(codeGenerator))
					continue;

				if(staticType != null) {
					codeGenerator.getCompilationUnit().importType(staticType);
					codeGenerator.append(staticType.getJavaName());
				} else
					codeGenerator.append(tokens.get(0).getRawText());
			} else {
				IVariableType variableType = variableTypes.get(i - 1);

				codeGenerator.append('.');

				if(variableType.isReference() && !variableType.isStatic())
					codeGenerator.append("get(" + getDeclaringType().getConstructionStage() + ").");

				codeGenerator.append(tokens.get(i).getRawText());
			}
		}
	}
}
