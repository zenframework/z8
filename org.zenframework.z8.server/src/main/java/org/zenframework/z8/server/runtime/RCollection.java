package org.zenframework.z8.server.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class RCollection<TYPE> extends ArrayList<TYPE> {
	private static final long serialVersionUID = -6377746293839490960L;

	private HashSet<Object> set;

	public RCollection() {
		this(10, false);
	}

	public RCollection(boolean uniqueElements) {
		this(10, uniqueElements);
	}

	public RCollection(int initialCapacity, boolean uniqueElements) {
		super(initialCapacity);
		if(uniqueElements)
			set = new HashSet<Object>();
	}

	@SuppressWarnings("unchecked")
	public RCollection(Object elements) {
		this();

		if(elements instanceof Collection)
			addAll((Collection<? extends TYPE>)elements);
		else
			addAll((TYPE[])elements);
	}

	public RCollection(TYPE[] array) {
		this(array.length, false);
		addAll(array);
	}

	public RCollection(Collection<TYPE> collection) {
		this(collection.size(), false);
		addAll(collection);
	}

	public boolean contains(Object o) {
		return set != null ? set.contains(o) : super.contains(o);
	}

	public TYPE get(integer index) {
		return get(index.getInt());
	}

	@Override
	public boolean add(TYPE element) {
		return set != null && contains(element) ? true : super.add(element);
	}

	@Override
	public void add(int index, TYPE element) {
		if(set != null && contains(element))
			return;

		super.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends TYPE> items) {
		return addAll(this.size(), items);
	}

	public boolean addAll(TYPE[] items) {
		return addAll(this.size(), items);
	}

	@Override
	public boolean addAll(int index, Collection<? extends TYPE> items) {
		return doAddAll(index, items);
	}

	public boolean addAll(int index, TYPE[] items) {
		return doAddAll(index, items);
	}

	private <T extends TYPE> boolean doAddAll(int index, Collection<T> items) {
		for(T item : items) {
			if(set == null || !contains(item)) {
				super.add(index, item);
				index++;
			}
		}

		return true;
	}

	private <T extends TYPE> boolean doAddAll(int index, T[] items) {
		for(T item : items) {
			if(set == null || !contains(item)) {
				super.add(index, item);
				index++;
			}
		}

		return true;
	}

	public void remove(Collection<? extends TYPE> elements) {
		if(set != null)
			set.removeAll(elements);
		super.removeAll(elements);
	}

	public void remove(TYPE[] elements) {
		remove(new RCollection<TYPE>(elements));
	}

	@Override
	public boolean remove(Object o) {
		if(set != null)
			set.remove(o);
		return super.remove(o);
	}

	public int indexOf(Object object) {
		for(int i = 0; i < size(); i++) {
			Object element = get(i);
			if(element == object || element.equals(object))
				return i;
		}
		return -1;
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	public void operatorAssign(Object array) {
		clear();
		operatorAddAssign(array);
	}

	@SuppressWarnings("unchecked")
	public void operatorAddAssign(Object array) {
		if(array instanceof Collection)
			addAll((Collection<TYPE>)array);
		else
			addAll((TYPE[])array);
	}

	public RCollection<TYPE> operatorAdd(Object array) {
		RCollection<TYPE> result = new RCollection<TYPE>();
		result.operatorAddAssign(this);
		result.operatorAddAssign(array);
		return result;
	}

	public integer z8_size() {
		return new integer(size());
	}

	public bool z8_isEmpty() {
		return new bool(size() == 0);
	}

	public bool z8_contains(TYPE element) {
		return new bool(contains(element));
	}

	@SuppressWarnings("unchecked")
	public bool z8_isSubsetOf(Object elements) {
		if(elements instanceof Collection)
			return new bool(isSubsetOf((Collection<? extends TYPE>)elements));
		return new bool(isSubsetOf((TYPE[])elements));
	}

	public boolean isSubsetOf(Collection<? extends TYPE> elements) {
		for(TYPE element : this) {
			if(!elements.contains(element))
				return false;
		}

		return true;
	}

	public boolean isSubsetOf(TYPE[] elements) {
		return isSubsetOf(new RCollection<TYPE>(elements));
	}

	public integer z8_indexOf(TYPE element) {
		return new integer(indexOf(element));
	}

	public void z8_clear() {
		clear();
	}

	public void z8_add(TYPE element) {
		add(element);
	}

	public void z8_add(integer index, TYPE element) {
		add(index.getInt(), element);
	}

	public void z8_set(integer index, TYPE element) {
		set(index.getInt(), element);
	}

	@SuppressWarnings({ "unchecked" })
	public void z8_addAll(Object elements) {
		if(elements instanceof Collection)
			addAll((Collection<? extends TYPE>)elements);
		else
			addAll((TYPE[])elements);
	}

	@SuppressWarnings("unchecked")
	public void z8_addAll(integer index, Object elements) {
		if(elements instanceof Collection)
			addAll(index.getInt(), (Collection<? extends TYPE>)elements);
		else
			addAll(index.getInt(), (TYPE[])elements);
	}

	public TYPE z8_removeAt(integer index) {
		return remove(index.getInt());
	}

	public boolean z8_remove(TYPE element) {
		return remove(element);
	}

	public void z8_remove(RCollection<? extends TYPE> elements) {
		remove(elements);
	}

	public void z8_remove(TYPE[] elements) {
		remove(elements);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RCollection<TYPE> z8_sort() {
		Collections.sort((List<Comparable>)this);
		return this;
	}

	public string z8_join() {
		return z8_join(new string(""));
	}

	@SuppressWarnings("rawtypes")
	public string z8_join(string separator) {
		String result = "";
		boolean first = true;
		for(TYPE element : this) {
			result += first ? "" : separator.get();
			first = false;

			if(element instanceof CLASS) {
				OBJECT object = (OBJECT)((CLASS)element).get();
				result += object.z8_toString();
			} else if(element instanceof primary) {
				primary primary = (primary)element;
				result += primary.toString();
			} else
				result += element.toString();
		}

		return new string(result);
	}
}
