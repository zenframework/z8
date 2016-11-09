package org.zenframework.z8.server.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayUtils {
	static public boolean isEmpty(char[] array) {
		return array == null || array.length == 0;
	}

	static public <Type> boolean contains(Type[] objects, Type object) {
		return indexOf(objects, object) != -1;
	}

	static public <Type> int indexOf(Type[] objects, Type object) {
		if(objects == null)
			return -1;

		for(int index = 0; index < objects.length; index++) {
			if(objects[index] == object || object != null && object.equals(objects[index]))
				return index;
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
		int index = Arrays.binarySearch(objects, object);

		if(index < 0) {
			List<Type> list = new ArrayList<Type>(Arrays.asList(objects));
			list.add(position, object);
			return list.toArray(objects);
		}

		return objects;
	}
}
