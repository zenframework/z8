package org.zenframework.z8.server.engine;

import org.zenframework.z8.server.runtime.ComplexRuntime;
import org.zenframework.z8.server.runtime.IRuntime;
import org.zenframework.z8.server.runtime.Runtimes;
import org.zenframework.z8.server.runtime.ServerRuntime;

public class Runtime extends ComplexRuntime {

	static private Runtime runtime;
	static private ModelGraph modelGraph;
	static private Version version;

	private Runtime() {
		addRuntime(new ServerRuntime());
		for (IRuntime runtime : Runtimes.loadRuntimes(getClass().getClassLoader()))
			addRuntime(runtime);
	}

	static public synchronized Runtime instance() {
		if(runtime == null)
			runtime = new Runtime();
		return runtime;
	}

	static public ModelGraph modelGraph() {
		if(modelGraph == null)
			modelGraph = ModelGraph.newModelGraph(instance().tables());
		return modelGraph;
	}

	static public Version version() {
		if(version == null)
			version = Version.getVersion(instance());

		return version;
	}
}
