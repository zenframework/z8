package org.zenframework.z8.server.request;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class Loader {

	/*
	 * any className has a form of 'outerClassName[.__number]', where
	 * outerClassName is the canonical name of the top level class __number is
	 * the name of the inner class
	 */
	private static final String innerClassPrefix = "__";

	private static final Map<String, WeakReference<CLASS<?>>> loadedClasses = new HashMap<String, WeakReference<CLASS<?>>>();

	static public CLASS<?> loadClass(String className) {
		synchronized(loadedClasses) {
			WeakReference<CLASS<?>> reference = Loader.loadedClasses.get(className);
			CLASS<?> cls = reference != null ? reference.get() : null;
			if (cls != null)
				return cls;
		}

		className = className.replaceAll("." + innerClassPrefix, "\\$" + innerClassPrefix) + "$CLASS";

		try {
			CLASS<?> result = (CLASS<?>) Runtime.instance().loadClass(className).getDeclaredConstructor(IObject.class)
					.newInstance((OBJECT) null);

			synchronized(loadedClasses) {
				if(!loadedClasses.containsKey(className))
					loadedClasses.put(className, new WeakReference<CLASS<?>>(result));
			}

			return result;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	static public OBJECT getInstance(String className) {
		CLASS<?> objectClass = loadClass(className);
		return (OBJECT)objectClass.newInstance();
	}

}
