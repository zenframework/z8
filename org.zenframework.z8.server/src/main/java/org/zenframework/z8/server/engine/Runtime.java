package org.zenframework.z8.server.engine;

import java.util.LinkedList;
import java.util.List;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.SystemTools;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.ComplexRuntime;
import org.zenframework.z8.server.runtime.IRuntime;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.utils.StringUtils;

public class Runtime extends ComplexRuntime {

	static private Runtime runtime;
	static private ModelGraph modelGraph;
	static private String version;

	private Runtime() {
		addRuntime(new ServerRuntime());
		loadRuntimes(getClass().getClassLoader());
	}

	static public Runtime instance() {
		if(runtime == null)
			runtime = new Runtime();
		return runtime;
	}

	static public ModelGraph modelGraph() {
		if(modelGraph == null)
			modelGraph = ModelGraph.newModelGraph(instance().tables());
		return modelGraph;
	}

	static public String version() {
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
