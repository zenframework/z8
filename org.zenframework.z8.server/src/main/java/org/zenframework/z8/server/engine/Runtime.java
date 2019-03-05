package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.SystemTools;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.AbstractRuntime;
import org.zenframework.z8.server.runtime.IRuntime;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.StringUtils;

public class Runtime extends AbstractRuntime {

	static private IRuntime runtime;
	static private ModelGraph modelGraph;
	static private String version;

	static public IRuntime instance() {
		if(runtime == null)
			runtime = new Runtime();
		return runtime;
	}

	static public ModelGraph modelGraph() {
		if(modelGraph == null)
			modelGraph = ModelGraph.newModelGraph(instance().tables());
		return modelGraph;
	}

	private static final String Z8RuntimePath = "META-INF/z8.runtime";
	private static final String Z8BlRuntimePath = "META-INF/z8_bl.runtime";

	public Runtime() {
		this(null);
	}

	public Runtime(ClassLoader classLoader) {
		if(classLoader == null)
			classLoader = getClass().getClassLoader();

		// Load base runtime-class
		mergeWith(new ServerRuntime());

		try {
			// Load other modules runtime-classes
			Enumeration<URL> resources = classLoader.getResources(Z8RuntimePath);
			while(resources.hasMoreElements()) {
				loadRuntime(resources.nextElement(), classLoader);
			}
			resources = classLoader.getResources(Z8BlRuntimePath);
			while(resources.hasMoreElements()) {
				loadRuntime(resources.nextElement(), classLoader);
			}
		} catch(IOException e) {
			throw new RuntimeException("Can't load " + Z8RuntimePath + " resources", e);
		}
	}

	private void loadRuntime(URL resource, ClassLoader classLoader) {
		try {
			String className = IOUtils.readText(resource);
			IRuntime runtime = (IRuntime)classLoader.loadClass(className).newInstance();
			mergeWith(runtime);
			Trace.logEvent("Runtime class '" + className + "' loaded");
		} catch(Throwable e) {
			Trace.logError("Can't load runtime-class from resource " + resource, e);
		}
	}

	public static String version() {
		if(version != null)
			return version;

		int controlSum = 0;

		for(Table.CLASS<? extends Table> cls : instance().tables())
			controlSum += cls.newInstance().controlSum();

		for(OBJECT.CLASS<? extends OBJECT> cls : instance().entries()) {
			if(!cls.instanceOf(SystemTools.class))
				controlSum += cls.newInstance().controlSum();
		}

		version = StringUtils.padLeft("" + Math.abs(controlSum), 10, '0');
		version = version.substring(0, 1) + "." + version.substring(1, 4) + "." + version.substring(4, 7) + "." + version.substring(7);
		return version;
	}
}
