package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;

import org.zenframework.z8.server.base.simple.Activator;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.db.generator.IForeignKey;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Loader;
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
            try {
                activator.get().onInitialize();
            } catch(Throwable e) {
                Trace.logError("Plugin initialization error.", e);
            }
        }
    }

    public static JsonObject getTablesStructure() {
        JsonObject structure = new JsonObject();
        for (Table.CLASS<? extends Table> tableClass : runtime.tables()) {
            fillStructure(structure, tableClass);
        }
        return structure;
    }

    @SuppressWarnings("unchecked")
    private static void fillStructure(JsonObject structure, Table.CLASS<? extends Table> tableClass) {
        if (!structure.has(tableClass.classId())) {
            JsonObject table = new JsonObject();
            structure.put(tableClass.classId(), table);
            for (IForeignKey fkey : tableClass.get().getForeignKeys()) {
                String fieldIndex = tableClass.get().getFieldByName(fkey.getFieldDescriptor().name()).getIndex();
                table.put(fieldIndex, ((Table) fkey.getReferencedTable()).classId());
                fillStructure(structure,
                        (Table.CLASS<? extends Table>) Loader.loadClass(((Table) fkey.getReferencedTable()).classId()));
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

}
