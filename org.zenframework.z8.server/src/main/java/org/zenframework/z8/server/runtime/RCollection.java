package org.zenframework.z8.server.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class RCollection<TYPE> extends ArrayList<TYPE> {
    private static final long serialVersionUID = -6377746293839490960L;

    private final boolean uniqueElements;
    private final Set<Object> hashes;

    public RCollection() {
        this(10, false);
    }

    public RCollection(boolean uniqueElements) {
        this(10, uniqueElements);
    }

    public RCollection(int initialCapacity, boolean uniqueElements) {
        super(initialCapacity);
        this.uniqueElements = uniqueElements;
        hashes = uniqueElements ? new HashSet<Object>() : null;
    }

    public RCollection(TYPE[] array) {
        this(array.length, false);
        for(TYPE element : array) {
            add(element);
        }
    }

    public RCollection(Collection<TYPE> collection) {
        this(collection.size(), false);
        addAll(collection);
    }

    @Override
    public TYPE get(int index) {
        try {
            return super.get(index);
        }
        catch(IndexOutOfBoundsException e) {
            throw new RuntimeException(e);
        }
    }

    public TYPE get(integer index) {
        return get(index.getInt());
    }

    @Override
    public boolean contains(Object object) {
        return uniqueElements ? hashes.contains(object) : super.contains(object);
    }

    @Override
    public boolean add(TYPE element) {
        if(uniqueElements) {
            if(contains(element)) {
                return true;
            }

            hashes.add(element);
        }

        return super.add(element);
    }

    @Override
    public void add(int index, TYPE element) {
        if(uniqueElements) {
            if(contains(element)) {
                return;
            }

            hashes.add(element);
        }

        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends TYPE> items) {
        return addAll(this.size(), items);
    }

    @Override
    public boolean addAll(int index, Collection<? extends TYPE> items) {
        return addAllOfType(index, items);
    }
    
    private <T extends TYPE> boolean addAllOfType(int index, Collection<T> items) {
        Collection<T> itemsToAdd = items;

        if(uniqueElements) {
            itemsToAdd = new LinkedHashSet<T>();

            for(T item : items) {
                if(!contains(item)) {
                    itemsToAdd.add(item);
                    hashes.add(item);
                }
            }
        }

        return super.addAll(index, itemsToAdd);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object object) {
        TYPE element = (TYPE)object;

        if(uniqueElements) {
            hashes.remove(object);
        }

        return super.remove(element);
    }

    public void remove(Collection<? extends TYPE> elements) {
        for(TYPE element : elements) {
            remove(element);
        }
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
        addAll((RCollection<TYPE>)array);
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

    public bool z8_isSubsetOf(RCollection<? extends TYPE> elements) {
        for(TYPE element : this) {
            if(!elements.contains(element)) {
                return new bool(false);
            }
        }

        return new bool(true);
    }

    public integer z8_indexOf(TYPE element) {
        return new integer(indexOf(element));
    }

    @Override
    public void clear() {
        if(hashes != null)
            hashes.clear();
        super.clear();
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

    public void z8_addAll(RCollection<? extends TYPE> elements) {
        addAll(elements);
    }

    public void z8_addAll(integer index, RCollection<? extends TYPE> elements) {
        addAll(index.getInt(), elements);
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
}
