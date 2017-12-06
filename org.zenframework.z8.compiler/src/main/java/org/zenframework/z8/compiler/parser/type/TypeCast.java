package org.zenframework.z8.compiler.parser.type;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class TypeCast implements ITypeCast {
	private IMethod context;
	private IMethod operator;

	private IVariableType source;
	private IVariableType target;
	private int distance;

	public static ITypeCast getCastToBaseType(CompilationUnit compilationUnit, IType sourceType, IType targetType) {
		if(sourceType == null || targetType == null)
			return null;

		int distance = 0;
		IType baseType = sourceType;

		while(!targetType.equals(baseType)) {
			if(baseType == null)
				return null;

			distance++;
			baseType = baseType.getBaseType();
		}

		return new TypeCast(new VariableType(compilationUnit, sourceType), new VariableType(compilationUnit, targetType), distance);
	}

	public static ITypeCast[] findBestCast(ITypeCast[] candidates) {
		class TypeCastComparator implements Comparator<ITypeCast> {
			@Override
			public int compare(ITypeCast left, ITypeCast right) {
				return left.compare(right);
			}
		}

		if(candidates.length == 0)
			return new ITypeCast[0];

		if(candidates.length == 1)
			return candidates;

		List<ITypeCast> list = Arrays.asList(candidates);
		Collections.sort(list, new TypeCastComparator());

		ITypeCast first = list.get(0);
		ITypeCast second = list.get(1);

		return first.equals(second) ? new ITypeCast[] { first, second } : new ITypeCast[] { first };
	}

	public TypeCast() {
	}

	public TypeCast(IVariableType source, IMethod operator) {
		this.source = source;
		this.target = operator.getVariableType();
		this.operator = operator;
	}

	public TypeCast(IVariableType source, IVariableType target, int distance) {
		this.source = source;
		this.target = target;
		this.distance = distance;
	}

	@Override
	public IVariableType getSource() {
		return source;
	}

	@Override
	public IVariableType getTarget() {
		return target;
	}

	@Override
	public boolean hasOperator() {
		return operator != null;
	}

	@Override
	public boolean isBaseTypeCast() {
		return operator == null;
	}

	@Override
	public IMethod getContext() {
		return context;
	}

	@Override
	public void setContext(IMethod context) {
		this.context = context;
	}

	@Override
	public IMethod getOperator() {
		return operator;
	}

	@Override
	public void setOperator(IMethod operator) {
		this.operator = operator;
		target = this.operator.getVariableType();
	}

	@Override
	public int distanceToBaseType() {
		return distance;
	}

	@Override
	public int getWeight() {
		return (hasOperator() ? 100 : 0);
	}

	@Override
	public int compare(ITypeCast other) {
		if(hasOperator() == other.hasOperator())
			return distanceToBaseType() - other.distanceToBaseType();

		return hasOperator() ? 1 : -1;
	}

	@Override
	public int hashCode() {
		return getOperator().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		ITypeCast other = (ITypeCast)object;

		if(hasOperator() == other.hasOperator())
			return distanceToBaseType() == other.distanceToBaseType();

		return hasOperator();
	}

	@Override
	public void getCode(CodeGenerator codeGenerator, ILanguageElement element) {
		IMethod method = getContext();

		if(method != null) {
			codeGenerator.append(method.getJavaName());
			codeGenerator.append('(');
		}

		IMethod operator = getOperator();
		element.getCode(codeGenerator);

		if(operator != null) {
			codeGenerator.append('.');

			if(element.getVariableType().isReference())
				codeGenerator.append("get(" + element.getDeclaringType().getConstructionStage() + ").");
			codeGenerator.append(operator.getJavaName() + "()");
		}

		if(method != null)
			codeGenerator.append(')');

	}
}
