package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;

import org.zenframework.z8.server.base.simple.Activator;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.AbstractRuntime;
import org.zenframework.z8.server.runtime.IRuntime;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.utils.IOUtils;

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
            activator.get().onInitialize();
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

}
