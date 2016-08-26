package org.zenframework.z8.compiler.parser.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.ITypeCastSet;

public class TypeCastSet implements ITypeCastSet {
	private IMethod context;
	private List<ITypeCast> typeCasts;

	public static ITypeCastSet[] findBestCast(ITypeCastSet[] candidates) {
		class TypeCastSetComparator implements Comparator<ITypeCastSet> {
			@Override
			public int compare(ITypeCastSet left, ITypeCastSet right) {
				return left.getWeight() - right.getWeight();
			}

		}

		if(candidates.length == 0)
			return new ITypeCastSet[0];

		if(candidates.length == 1)
			return candidates;

		List<ITypeCastSet> list = Arrays.asList(candidates);
		Collections.sort(list, new TypeCastSetComparator());

		ITypeCastSet first = list.get(0);
		ITypeCastSet second = list.get(1);

		return first.equals(second) ? new ITypeCastSet[] { first, second } : new ITypeCastSet[] { first };
	}

	public TypeCastSet() {
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
	public int getWeight() {
		int weight = 0;

		for(ITypeCast typeCast : typeCasts)
			weight += typeCast.getWeight();

		return weight;
	}

	@Override
	public boolean equals(Object object) {
		ITypeCastSet other = (ITypeCastSet)object;
		return getWeight() == other.getWeight();
	}

	@Override
	public ITypeCast[] get() {
		if(typeCasts == null)
			return new ITypeCast[0];
		return typeCasts.toArray(new ITypeCast[typeCasts.size()]);
	}

	@Override
	public void add(ITypeCast typeCast) {
		if(typeCasts == null)
			typeCasts = new ArrayList<ITypeCast>();
		typeCasts.add(typeCast);
	}

	@Override
	public void getCode(CodeGenerator codeGenerator, ILanguageElement[] elements) {
		ITypeCast[] typeCasts = get();

		IMethod method = getContext();

		if(method != null) {
			codeGenerator.append(method.getJavaName());
			codeGenerator.append('(');
		}

		for(int i = 0; i < elements.length; i++) {
			if(i != 0)
				codeGenerator.append(", ");
			typeCasts[i].getCode(codeGenerator, elements[i], true);
		}

		if(method != null)
			codeGenerator.append(')');
	}
}
