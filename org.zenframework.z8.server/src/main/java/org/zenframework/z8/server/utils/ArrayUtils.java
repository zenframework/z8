package org.zenframework.z8.server.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ArrayUtils {
    static public <Type> int indexOf(Type[] objects, Type object) {
        if(objects == null) {
            return -1;
        }

        for(int index = 0; index < objects.length; index++) {
            if(objects[index] == object) {
                return index;
            }
        }
        return -1;
    }

    static public <Type> Type[] prepend(Type[] objects, Type object) {
        return insert(objects, object, 0);
    }

    static public <Type> Type[] append(Type[] objects, Type object) {
        return insert(objects, object, objects.length);
    }

    static public <Type> Type[] insert(Type[] objects, Type object, int position) {
        assert (objects != null);
        assert (position <= objects.length);

        int index = Arrays.binarySearch(objects, object);

        if(index < 0) {
            List<Type> list = new ArrayList<Type>(Arrays.asList(objects));
            list.add(position, object);
            return list.toArray(objects);
        }

        return objects;
    }

    static public <Type> Collection<Type> collection(Type element) {
        Collection<Type> result = new ArrayList<Type>();
        result.add(element);
        return result;
    }
}
