package org.zenframework.z8.compiler.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Set<TYPE> implements Iterable<TYPE> {
    private List<TYPE> list = new ArrayList<TYPE>();
    private Map<Integer, Integer> indices = new HashMap<Integer, Integer>();

    public Set() {}

    public Set(TYPE element) {
        add(element);
    }

    public int size() {
        return list.size();
    }

    public TYPE get(int index) {
        return 0 <= index && index < size() ? list.get(index) : null;
    }

    public TYPE get(Object name) {
        Integer index = indices.get(name.hashCode());
        return index != null ? list.get(index) : null;
    }

    public boolean contains(TYPE object) {
        return get(object) != null;
    }

    public TYPE firstElement() {
        return list.get(0);
    }

    public TYPE lastElement() {
        return list.get(list.size() - 1);
    }

    public void add(TYPE element) {
        if(element != null) {
            Integer index = indices.get(element.hashCode());

            if(index != null) {
                list.set(index, element);
            }
            else {
                indices.put(element.hashCode(), size());
                list.add(element);
            }
        }
    }

    public void add(TYPE[] elements) {
        for(TYPE element : elements) {
            add(element);
        }
    }

    private void rebuildIndex() {
        indices.clear();

        for(int index = 0; index < list.size(); index++) {
            indices.put(list.get(index).hashCode(), index);
        }
    }

    public TYPE remove(int index) {
        TYPE element = list.remove(index);
        rebuildIndex();
        return element;
    }

    public TYPE remove(TYPE element) {
        Integer index = indices.get(element.hashCode());
        return index != null ? remove(index) : null;
    }

    public TYPE removeLastElement() {
        return remove(size() - 1);
    }

    public TYPE removeFirstElement() {
        return remove(0);
    }

    @Override
    public Iterator<TYPE> iterator() {
        return list.iterator();
    }

    public TYPE[] toArray(TYPE[] array) {
        return list.toArray(array);
    }

    public void sort(Comparator<TYPE> comparator) {
        Collections.sort(list, comparator);
        rebuildIndex();
    }
}