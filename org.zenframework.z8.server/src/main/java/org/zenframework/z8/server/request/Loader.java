package org.zenframework.z8.server.request;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class Loader {
    private static Object mutex = new Object();
    private static Map<String, CLASS<?>> loadedClasses = Collections.synchronizedMap(new HashMap<String, CLASS<?>>());

    static public CLASS<?> loadClass(String className) {
        synchronized(mutex) {
            CLASS<?> cls = Loader.loadedClasses.get(className);

            if(cls == null) {
                cls = doLoadClass(className);
            }

            return cls;
        }
    }

    static public OBJECT getInstance(String className) {
        CLASS<?> objectClass = loadClass(className);
        return (OBJECT)objectClass.newInstance();
    }

    static private void addToCache(CLASS<?> cls) {
        String className = cls.classId();

        if(!loadedClasses.containsKey(className)) {
            cls.setContainer(null);
            loadedClasses.put(className, cls);
        }
    }

    static private CLASS<?> loadClass(CLASS<?> cls, String innerClassName) {
        CLASS<?> innerClass = loadedClasses.get(cls.classId() + '.' + innerClassName);

        if(innerClass == null) {
            innerClass = doLoad(cls, innerClassName);
        }

        return innerClass;
    }

    private static String internalClassPrefix = "__";

    /*
     *  any className has a form of 'outerClassName[.__number]', where
     *  outerClassName is the canonical name of the top level class
     *  __number is the name of the inner class
     */
    static private String getOuterClassName(String className) {
        int index = className.indexOf(internalClassPrefix);

        if(index != -1) {
            return className.substring(0, index - 1);
        }

        return className;
    }

    static private String[] getInnerClassNames(String className) {
        int index = className.indexOf(internalClassPrefix);

        if(index != -1) {
            return className.substring(index).split("\\.");
        }

        return new String[0];
    }

    static private CLASS<?> doLoadClass(String className) {
        String outerClassName = getOuterClassName(className);
        String[] innerClassNames = getInnerClassNames(className);

        if(innerClassNames.length == 0) {
            return doLoad(outerClassName);
        }

        CLASS<?> cls = loadClass(outerClassName);

        for(String name : innerClassNames) {
            cls = loadClass(cls, name);
        }

        return cls;
    }

    static private CLASS<?> doLoad(String className) {
        try {
            return doLoad(Class.forName(className));
        }
        catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static private CLASS<?> doLoad(CLASS<?> cls, String innerClassName) {
        Class<?> innerClass = getInnerClass(cls.getJavaClass(), innerClassName);
        return doLoad(innerClass);
    }

    static private Class<?> getInnerClass(Class<?> cls, String innerClassName) {
        for(Class<?> innerClass : cls.getClasses()) {
            if(innerClass.getSimpleName().equals(innerClassName)) {
                return innerClass;
            }
        }
        return null;
    }

    static private CLASS<?> doLoad(Class<?> cls) {
        try {
            for(Class<?> innerClass : cls.getClasses()) {
                if(innerClass.getSimpleName().equals("CLASS")) {
                    OBJECT container = new OBJECT();
                    CLASS<?> result = (CLASS<?>)innerClass.getDeclaredConstructor(IObject.class).newInstance(container);
                    addToCache(result);
                    return result;
                }
            }
            throw new RuntimeException("Class not found: " + cls.getCanonicalName());
        }
        catch(Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
