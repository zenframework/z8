package org.zenframework.z8.server.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

	@Override
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
		return doAddAll(index, Arrays.asList(items));
	}

	private <T extends TYPE> boolean doAddAll(int index, Iterable<T> items) {
		boolean modified = false;

		for(T item : items) {
			if(set == null || !contains(item)) {
				super.add(index++, item);
				modified = true;
			}
		}

		return modified;
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

	@Override
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

	@SuppressWarnings("unchecked")
	private RCollection<TYPE> doAddAll(Object array) {
		if(array instanceof Collection)
			addAll((Collection<TYPE>)array);
		else
			addAll((TYPE[])array);
		return this;
	}

	public RCollection<TYPE> operatorAdd(Object array) {
		return new RCollection<TYPE>().doAddAll(this).doAddAll(array);
	}

	public integer z8_size() {
		return new integer(size());
	}

	public bool z8_isEmpty() {
		return new bool(size() == 0);
	}

	public TYPE z8_first() {
		return isEmpty() ? null : get(0);
	}

	public TYPE z8_last() {
		return isEmpty() ? null : get(size() - 1);
	}

	public bool z8_contains(TYPE element) {
		return new bool(contains(element));
	}

	@SuppressWarnings("unchecked")
	public bool z8_isSubsetOf(Object elements) {
		return new bool(elements instanceof Collection ? isSubsetOf((Collection<? extends TYPE>)elements)
				: isSubsetOf((TYPE[])elements));
	}

	public boolean isSubsetOf(Collection<? extends TYPE> elements) {
		return elements.containsAll(this);
	}

	public boolean isSubsetOf(TYPE[] elements) {
		return isSubsetOf(Arrays.asList(elements));
	}

	public integer z8_indexOf(TYPE element) {
		return new integer(indexOf(element));
	}

	public void z8_clear() {
		clear();
	}

	public RCollection<TYPE> z8_add(TYPE element) {
		add(element);
		return this;
	}

	public RCollection<TYPE> z8_add(integer index, TYPE element) {
		add(index.getInt(), element);
		return this;
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

	public bool z8_remove(TYPE element) {
		return new bool(remove(element));
	}

	public void z8_remove(RCollection<? extends TYPE> elements) {
		remove(elements);
	}

	public void z8_remove(TYPE[] elements) {
		remove(elements);
	}

	public RCollection<TYPE> z8_removeAll(TYPE element) {
		remove(element);
		return this;
	}

	public RCollection<TYPE> z8_removeAll(RCollection<? extends TYPE> elements) {
		remove(elements);
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RCollection<TYPE> z8_sort() {
		Collections.sort((List<Comparable>)this);
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RCollection<TYPE> z8_reverse() {
		Collections.reverse((List<Comparable>)this);
		return this;
	}

	public RCollection<TYPE> z8_unique() {
		return new RCollection<TYPE>(new LinkedHashSet<TYPE>(this));
	}

	public string z8_join() {
		return z8_join(new string(""));
	}

	public string z8_join(string separator) {
		String result = "";
		boolean first = true;
		for(TYPE element : this) {
			result += first ? "" : separator.get();
			first = false;

			if(element instanceof CLASS) {
				OBJECT object = (OBJECT)((CLASS<?>)element).get();
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
