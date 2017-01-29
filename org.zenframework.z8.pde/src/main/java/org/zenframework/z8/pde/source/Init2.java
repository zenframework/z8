package org.zenframework.z8.pde.source;

import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IType;

public class Init2 {
	public IType context;
	public List<String> prefix;
	public ILanguageElement element;
	public List<String> postfix;

	public Init2(IType context, List<String> prefix, ILanguageElement element, List<String> postfix) {
		this.context = context;
		this.element = element;
		this.prefix = prefix;
		this.postfix = postfix;
		if(prefix == null)
			this.prefix = new ArrayList<String>();
		if(postfix == null)
			this.postfix = new ArrayList<String>();
	}

	public Init2(IType context, ILanguageElement element) {
		this(context, null, element, null);
	}

	public Init2(Init2 init, ILanguageElement element) {
		context = init.context;
		prefix = new ArrayList<String>();
		prefix.addAll(init.prefix);
		postfix = new ArrayList<String>();
		postfix.addAll(init.postfix);
		this.element = element;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Init2) {
			Init2 other = (Init2)obj;
			return other.context.equals(context) && other.element.equals(element) && other.prefix.equals(prefix) && other.postfix.equals(postfix);
		}
		return false;
	}

	@Override
	public int hashCode() {
		InitHelper h = InitHelper.createHelper(this);
		if(h == null)
			return element.hashCode();
		return h.hashCode();
	}
}
