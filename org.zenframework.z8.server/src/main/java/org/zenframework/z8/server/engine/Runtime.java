package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;

import org.zenframework.z8.server.base.simple.Runnable;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.AbstractRuntime;
import org.zenframework.z8.server.runtime.IRuntime;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.utils.IOUtils;

public class Runtime extends AbstractRuntime {

    private static IRuntime runtime;

    static {
        runtime = new Runtime();
    }

    static public IRuntime instance() {
        return runtime;
    }

    static public void set(IRuntime runtime) {
        Runtime.runtime = runtime;
    }

    private static final String Z8RuntimePath = "META-INF/z8.runtime";
    private static final String Z8BlRuntimePath = "META-INF/z8_bl.runtime";

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Runtime() {
        // Load base runtime-class
        mergeWith(new ServerRuntime());
        try {
            // Load other modules runtime-classes
            Enumeration<URL> resources = getClass().getClassLoader().getResources(Z8RuntimePath);
            while (resources.hasMoreElements()) {
                loadRuntime(resources.nextElement());
            }
            resources = getClass().getClassLoader().getResources(Z8BlRuntimePath);
            while (resources.hasMoreElements()) {
                loadRuntime(resources.nextElement());
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't load " + Z8RuntimePath + " resources", e);
        }
        // Run activators
        Collection<Runnable.CLASS<? extends Runnable>> activators = (Collection) activators();
        for (Runnable.CLASS<? extends Runnable> activator : activators) {
            activator.get().run();
        }
    }

    private void loadRuntime(URL resource) {
        try {
            String className = IOUtils.readText(resource);
            IRuntime runtime = (IRuntime) Class.forName(className).newInstance();
            mergeWith(runtime);
            Trace.logEvent("Runtime class '" + className + "' loaded");
        } catch (Exception e) {
            Trace.logError("Can't load runtime-class from resource " + resource, e);
        }
    }

}
