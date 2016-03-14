package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;

import org.zenframework.z8.server.base.simple.Activator;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.AbstractRuntime;
import org.zenframework.z8.server.runtime.IRuntime;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.StringUtils;

public class Runtime extends AbstractRuntime {

    private static IRuntime runtime;

    static public IRuntime instance() {
        if(runtime == null)
            runtime = new Runtime();
        return runtime;
    }

    private static final String Z8RuntimePath = "META-INF/z8.runtime";
    private static final String Z8BlRuntimePath = "META-INF/z8_bl.runtime";

    public Runtime() {
        this(null);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Runtime(ClassLoader classLoader) {
        if(classLoader == null)        
            classLoader =  getClass().getClassLoader();
        
        // Load base runtime-class
        mergeWith(new ServerRuntime());
        try {
            // Load other modules runtime-classes
            Enumeration<URL> resources = classLoader.getResources(Z8RuntimePath);
            while (resources.hasMoreElements()) {
                loadRuntime(resources.nextElement(), classLoader);
            }
            resources = classLoader.getResources(Z8BlRuntimePath);
            while (resources.hasMoreElements()) {
                loadRuntime(resources.nextElement(), classLoader);
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't load " + Z8RuntimePath + " resources", e);
        }
        // Run activators
        Collection<Activator.CLASS<? extends Activator>> activators = (Collection) activators();
        for (Activator.CLASS<? extends Activator> activator : activators) {
            try {
                activator.get().onInitialize();
            } catch(Throwable e) {
                Trace.logError("Plugin initialization error.", e);
            }
        }
    }

    private void loadRuntime(URL resource, ClassLoader classLoader) {
        try {
            String className = IOUtils.readText(resource);
            IRuntime runtime = (IRuntime) classLoader.loadClass(className).newInstance();
            mergeWith(runtime);
            Trace.logEvent("Runtime class '" + className + "' loaded");
        } catch (Throwable e) {
            Trace.logError("Can't load runtime-class from resource " + resource, e);
        }
    }

    public static String version() {
        int controlSum = 0;

        for(Table.CLASS<? extends Table> cls: instance().tables()) {
            Table table = cls.newInstance();
            controlSum += table.controlSum();
        }
        
        String version = StringUtils.padLeft("" + Math.abs(controlSum), 10, '0');
        return version.substring(0, 1) + "." + version.substring(1, 4) + "." + version.substring(4, 7) + "." + version.substring(7);
    }
}
