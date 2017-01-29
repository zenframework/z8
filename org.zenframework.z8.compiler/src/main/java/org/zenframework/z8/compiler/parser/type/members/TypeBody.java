package org.zenframework.z8.compiler.parser.type.members;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IInitializer;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.BuiltinNative;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class TypeBody extends LanguageElement {
	private IToken leftBrace;
	private IToken rightBrace;
	private IPosition bottomMemberSourceRange;

	private List<ILanguageElement> members;
	private List<IInitializer> initializers;

	private Operator assignOperator;

	public TypeBody(IToken leftBrace) {
		this.leftBrace = leftBrace;
	}

	public void setLeftBrace(IToken leftBrace) {
		this.leftBrace = leftBrace;
	}

	public void setRightBrace(IToken rightBrace) {
		this.rightBrace = rightBrace;
	}

	@Override
	public IPosition getSourceRange() {
		if(rightBrace != null)
			return leftBrace.getPosition().union(rightBrace.getPosition());
		else if(bottomMemberSourceRange != null)
			return leftBrace.getPosition().union(bottomMemberSourceRange);
		else
			return leftBrace.getPosition();
	}

	@Override
	public IToken getFirstToken() {
		return leftBrace;
	}

	public ILanguageElement[] getMembers() {
		if(members == null)
			return new ILanguageElement[0];
		return members.toArray(new ILanguageElement[members.size()]);
	}

	public void addMember(ILanguageElement member) {
		if(members == null)
			members = new ArrayList<ILanguageElement>();

		members.add(member);

		bottomMemberSourceRange = member.getSourceRange();
	}

	private void rearrangeMembers() {
		if(members == null)
			return;

		List<ILanguageElement> declarators = new ArrayList<ILanguageElement>();

		int index = 0;

		while(index < members.size()) {
			if(members.get(index) instanceof Member)
				declarators.add(members.remove(index));
			else
				index++;
		}

		members.addAll(0, declarators);
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		rearrangeMembers();

		ILanguageElement[] members = getMembers();

		for(ILanguageElement member : members)
			member.resolveTypes(compilationUnit, declaringType);

		return true;
	}

	@Override
	public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveStructure(compilationUnit, declaringType))
			return false;

		ILanguageElement[] members = getMembers();

		for(ILanguageElement member : members)
			member.resolveStructure(compilationUnit, declaringType);

		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		ILanguageElement[] members = getMembers();

		for(ILanguageElement member : members) {
			member.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);

			if(member instanceof Operator) {
				Operator operator = (Operator)member;

				IVariableType[] parameters = operator.getParameterTypes();

				if(operator.isKindOf(IToken.ASSIGN) && parameters.length == 1 && parameters[0].compare(new VariableType(compilationUnit, declaringType)))
					assignOperator = operator;
			}
		}

		if(!declaringType.isNative() && declaringType.extendsPrimary() && assignOperator == null) {
			String name = declaringType.getUserName();
			setError(getDeclaringType().getPosition(), "The type " + name + " extends a primary type and must implement operator=(" + name + ")");
		}

		return true;
	}

	@Override
	public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveNestedTypes(compilationUnit, declaringType))
			return false;

		ILanguageElement[] members = getMembers();

		for(ILanguageElement member : members)
			member.resolveNestedTypes(compilationUnit, declaringType);

		initializers = new ArrayList<IInitializer>();
		getReferences(getDeclaringType(), initializers);
		arrangeReferences(initializers);

		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		ILanguageElement[] members = getMembers();

		for(ILanguageElement member : members) {
			if(!(member instanceof IMethod))
				member.getCode(codeGenerator);
		}

		if(getDeclaringType().getContainerType() == null)
			getStaticBlock(codeGenerator);

		if(getDeclaringType().getContainerType() == null)
			getStaticConstructor(codeGenerator);

		if(getDeclaringType().extendsPrimary())
			getPrimaryTypeConstructor(codeGenerator);
		else
			getReferenceTypeConstructor(codeGenerator);

		for(ILanguageElement member : members) {
			if(member instanceof IMethod)
				member.getCode(codeGenerator);
		}
	}

	@Override
	public void getStaticBlock(CodeGenerator codeGenerator) {
		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("static {");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		ILanguageElement[] members = getMembers();

		for(ILanguageElement member : members)
			member.getStaticBlock(codeGenerator);

		codeGenerator.indent();
		codeGenerator.append("staticConstructor();");
		codeGenerator.breakLine();

		codeGenerator.decrementIndent();

		codeGenerator.indent();
		codeGenerator.append("}");
		codeGenerator.breakLine();
	}

	@Override
	public void getStaticConstructor(CodeGenerator codeGenerator) {
		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("public static void staticConstructor() {");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		ILanguageElement[] members = getMembers();

		for(ILanguageElement member : members)
			member.getStaticConstructor(codeGenerator);

		codeGenerator.decrementIndent();

		codeGenerator.indent();
		codeGenerator.append("}");
		codeGenerator.breakLine();
	}

	private void getReferenceTypeConstructor(CodeGenerator codeGenerator) {
		getConstructor(codeGenerator);
		getConstructor1(codeGenerator);
		getConstructor2(codeGenerator);
	}

	private void getPrimaryTypeConstructor(CodeGenerator codeGenerator) {
		codeGenerator.indent();
		codeGenerator.append("public " + getDeclaringType().getJavaName() + "() {");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		getConstructorBody(codeGenerator);
		getConstructor1Body(codeGenerator);
		getConstructor2Body(codeGenerator);

		codeGenerator.decrementIndent();

		codeGenerator.indent();
		codeGenerator.append("}");
		codeGenerator.breakLine();
		codeGenerator.breakLine();

		codeGenerator.indent();
		codeGenerator.append("public " + getDeclaringType().getJavaName() + "(" + getDeclaringType().getJavaName() + " value) {");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		codeGenerator.indent();
		codeGenerator.append("this();");
		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("operatorAssign(value);");
		codeGenerator.breakLine();

		codeGenerator.decrementIndent();

		codeGenerator.indent();
		codeGenerator.append("}");
		codeGenerator.breakLine();
	}

	@Override
	public void getConstructor(CodeGenerator codeGenerator) {
		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("public " + getDeclaringType().getJavaName() + "(" + BuiltinNative.IObject + " container) {");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		codeGenerator.indent();
		codeGenerator.append("super(container);");
		codeGenerator.breakLine();

		getConstructorBody(codeGenerator);

		codeGenerator.decrementIndent();

		codeGenerator.indent();
		codeGenerator.append("}");
		codeGenerator.breakLine();
	}

	private void getConstructorBody(CodeGenerator codeGenerator) {
		ILanguageElement[] members = getMembers();

		for(ILanguageElement member : members)
			member.getConstructor(codeGenerator);
	}

	private void getReferences(IType type, List<IInitializer> references) {
		if(type.getBaseType() != null)
			getReferences(type.getBaseType(), references);

		IInitializer[] initializers = type.getInitializers();

		for(IInitializer initializer : initializers) {
			ILanguageElement rightElement = initializer.getRightElement();

			if(rightElement == null)
				continue;

			IToken operator = initializer.getOperator();

			if(operator == null || operator.getId() != IToken.ASSIGN || !rightElement.isQualifiedName() && !rightElement.isOperatorNew())
				continue;

			String leftName = initializer.getLeftName();

			for(int index = references.size() - 1; index >= 0; index--) {
				IInitializer reference = references.get(index);

				String name = references.get(index).getLeftName();

				if(initializer instanceof IType && !(reference instanceof IType) || reference instanceof IType && !(initializer instanceof IType))
					continue;

				if(name.equals(leftName) || name.startsWith(leftName + "."))
					references.remove(index);
			}
		}

		for(IInitializer initializer : initializers) {
			if(initializer.getVariableType().isReference() && initializer.getRightElement() != null)
				references.add(initializer);
		}
	}

	class InitializerComparator implements Comparator<IInitializer> {
		@Override
		public int compare(IInitializer left, IInitializer right) {
			return left.getLeftName().compareTo(right.getLeftName());
		}
	}

	private void arrangeReferences(List<IInitializer> initializers) {
		Collections.sort(initializers, new InitializerComparator());

		Vector<Boolean> checked = new Vector<Boolean>();
		checked.setSize(initializers.size());

		while(true) {
			int index = 0;
			for(; index < initializers.size(); index++) {
				if(checked.get(index) == null)
					break;
			}

			if(index == initializers.size())
				return;

			String rightName = initializers.get(index).getRightName();

			for(int i = index + 1; i < initializers.size(); i++) {
				String leftName = initializers.get(i).getLeftName();

				if(leftName.equals(rightName) || leftName.startsWith(rightName + '.') || rightName.startsWith(leftName + '.')) {
					IInitializer initializer = initializers.remove(i);
					initializers.add(index, initializer);

					Boolean check = checked.remove(i);
					checked.insertElementAt(check, index);

					index++;
				}
			}

			checked.set(index, true);
		}
	}

	@Override
	public void getConstructor1(CodeGenerator codeGenerator) {
		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("public void constructor1() {");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		getConstructor1Body(codeGenerator);

		codeGenerator.decrementIndent();

		codeGenerator.indent();
		codeGenerator.append("}");
		codeGenerator.breakLine();
	}

	private void getConstructor1Body(CodeGenerator codeGenerator) {
		getDeclaringType().setConstructionStage(BuiltinNative.Constructor1);

		for(IInitializer initializer : initializers) {
			initializer.getDeclaringType().setConstructionStage(BuiltinNative.Constructor1);

			ILanguageElement element = (ILanguageElement)initializer;
			element.getConstructor1(codeGenerator);
		}
	}

	@Override
	public void getConstructor2(CodeGenerator codeGenerator) {
		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("public void constructor2() {");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		codeGenerator.indent();
		codeGenerator.append("super.constructor2();");
		codeGenerator.breakLine();

		getConstructor2Body(codeGenerator);

		codeGenerator.decrementIndent();

		codeGenerator.indent();
		codeGenerator.append("}");
		codeGenerator.breakLine();
	}

	private void getConstructor2Body(CodeGenerator codeGenerator) {
		getDeclaringType().setConstructionStage(BuiltinNative.Constructor2);

		ILanguageElement[] elements = getMembers();

		for(ILanguageElement element : elements) {
			element.getConstructor2(codeGenerator);
		}

		IType declaringType = getDeclaringType();

		IMember[] autoArrays = declaringType.getAutoArrays();
		IMember[] members = declaringType.getMembers();

		for(IMember member : members) {
			IVariableType memberType = new VariableType(member.getVariableType());

			for(IMember array : autoArrays) {
				IVariableType arrayElementType = new VariableType(array.getVariableType());
				arrayElementType.removeRightKey();

				ITypeCast typeCast = memberType.getCastTo(arrayElementType);

				if(typeCast != null && !typeCast.hasOperator()) {
					codeGenerator.indent();
					codeGenerator.append(array.getName() + ".add(" + member.getJavaName() + ");");
					codeGenerator.breakLine();
				}

			}
		}
	}
}
