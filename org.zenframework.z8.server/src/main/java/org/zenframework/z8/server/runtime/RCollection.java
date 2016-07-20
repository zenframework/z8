package org.zenframework.z8.server.runtime;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class RCollection<TYPE> extends ArrayList<TYPE> {
	private static final long serialVersionUID = -6377746293839490960L;

	private final boolean uniqueElements;

	public RCollection() {
		this(10, false);
	}

	public RCollection(boolean uniqueElements) {
		this(10, uniqueElements);
	}

	public RCollection(int initialCapacity, boolean uniqueElements) {
		super(initialCapacity);
		this.uniqueElements = uniqueElements;
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

	public TYPE get(integer index) {
		return get(index.getInt());
	}

	@Override
	public boolean add(TYPE element) {
		return uniqueElements && contains(element) ? true : super.add(element);
	}

	@Override
	public void add(int index, TYPE element) {
		if(uniqueElements && contains(element))
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
			if(!uniqueElements || !contains(item)) {
				super.add(index, item);
				index++;
			}
		}

		return true;
	}

	private <T extends TYPE> boolean doAddAll(int index, T[] items) {
		for(T item : items) {
			if(!uniqueElements || !contains(item)) {
				super.add(index, item);
				index++;
			}
		}

		return true;
	}

	public void remove(Collection<? extends TYPE> elements) {
		for(TYPE element : elements)
			remove(element);
	}

	public void remove(TYPE[] elements) {
		for(TYPE element : elements)
			remove(element);
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
}
