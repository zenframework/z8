package org.zenframework.z8.server.request;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class Loader {
	private static Object mutex = new Object();
	private static Map<String, CLASS<?>> loadedClasses = new HashMap<String, CLASS<?>>();

	static public CLASS<?> loadClass(String className) {
		CLASS<?> cls = null;

		synchronized(mutex) {
			cls = Loader.loadedClasses.get(className);
		}

		return cls != null ? cls : doLoadClass(className);
	}

	static public OBJECT getInstance(String className) {
		CLASS<?> objectClass = loadClass(className);
		return (OBJECT)objectClass.newInstance();
	}

	static private void addToCache(CLASS<?> cls) {
		String className = cls.classId();

		if(!loadedClasses.containsKey(className)) {
			synchronized(mutex) {
				loadedClasses.put(className, cls);
			}
		}
	}

	/*
	 * any className has a form of 'outerClassName[.__number]', where
	 * outerClassName is the canonical name of the top level class __number is
	 * the name of the inner class
	 */
	private static String innerClassPrefix = "__";

	static private CLASS<?> doLoadClass(String className) {
		className = className.replaceAll("." + innerClassPrefix, "\\$" + innerClassPrefix) + "$CLASS";

		try {
			Class<?> cls = Class.forName(className);
			CLASS<?> result = (CLASS<?>)cls.getDeclaredConstructor(IObject.class).newInstance((OBJECT)null);
			addToCache(result);
			return result;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
