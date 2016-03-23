package org.zenframework.z8.oda.driver;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class RuntimeLoader extends URLClassLoader {
    static Map<String, Runtime> runtimes = new HashMap<String, Runtime>();
    static Map<String, ClassLoader> loaders = new HashMap<String, ClassLoader>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static public CLASS loadClass(String name, File path) throws Throwable {
        ClassLoader classLoader = getClassLoader(path);
        Class cls = classLoader.loadClass(name.replaceAll(".__", "\\$__") + "$CLASS");
        return (CLASS)cls.getDeclaredConstructor(IObject.class).newInstance(new OBJECT());
    }
    
    static public Runtime getRuntime(File path) throws Throwable {
        Runtime runtime = runtimes.get(path.toString());
        
        if(runtime != null)
            return runtime;

        ClassLoader classLoader = getClassLoader(path);
        
        runtime = new Runtime(classLoader);

        runtimes.put(path.toString(), runtime);

        return runtime;
    }

    static public ClassLoader getClassLoader(File path) throws Throwable {
        ClassLoader classLoader = loaders.get(path.toString());
        
        if(classLoader == null) {
            classLoader = new RuntimeLoader(new File(path, "lib"), Runtime.class.getClassLoader());
            loaders.put(path.toString(), classLoader);
        }
        
        return classLoader;
    }
    
    private RuntimeLoader(File path, ClassLoader parent) throws Throwable {
        super(getJars(path), parent);
    }

    private RuntimeLoader(String path, ClassLoader parent) throws Throwable {
        super(getJars(new File(path)), parent);
    }
    
    static private URL[] getJars(File dir) throws Throwable {
        FilenameFilter filter = new FilenameFilter() {
            @Override
			public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar");
            }
        };
        
        File[] files = dir.listFiles(filter);
        
        List<URL> urls = new ArrayList<URL>();
        
        for(File file: files)
            urls.add(file.toURI().toURL());
        
        return urls.toArray(new URL[0]);
    }
}
