package org.zenframework.z8.justintime.runtime;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.ComplexRuntime;
import org.zenframework.z8.server.runtime.IRuntime;

public class DynamicRuntime extends ComplexRuntime {

	private static class DynamicClassLoader extends URLClassLoader {

		private final Workspace workspace;

		DynamicClassLoader(Workspace workspace) {
			super(new URL[] { toURL(workspace.getJavaClasses()) }, workspace.getClass().getClassLoader());
			this.workspace = workspace;
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			Trace.logEvent("Schema '" + workspace.getSchema() + "' dynamic code unloaded");
		}

	}

	private static DynamicRuntime instance;

	private final Map<String, IRuntime> dynamics = new HashMap<String, IRuntime>();

	public DynamicRuntime() {
		for (Workspace workspace : Workspace.workspaces())
			loadDynamic(workspace);

		if (instance != null)
			throw new RuntimeException("DynamicRuntime reinitialized");

		instance = this;
	}

	@Override
	protected List<IRuntime> runtimes() {
		String schema = ServerConfig.database().schema();
		List<IRuntime> runtimes = new LinkedList<IRuntime>(super.runtimes());
		synchronized (dynamics) {
			IRuntime runtime = dynamics.get(schema);
			if (runtime != null)
				runtimes.add(runtime);
		}
		return runtimes;
	}

	@Override
	protected void addRuntime(IRuntime runtime) {
		String schema = Workspace.getSchema(runtime.getUrl());
		synchronized (dynamics) {
			dynamics.put(schema, runtime);
			Trace.logEvent("Schema '" + schema + "' dynamic code loaded");
		}
	}

	public void loadDynamic() {
		loadDynamic(Workspace.workspace(ServerConfig.database().schema()));
	}

	public void loadDynamic(Workspace workspace) {
		try {
			ClassLoader cl = new DynamicClassLoader(workspace);
			loadRuntimes(cl, workspace.getJavaClasses());
		} catch (Exception e) {
			Trace.logError(e);
		}
	}

	public void unloadDynamic() {
		String schema = ServerConfig.database().schema();
		try {
			Scheduler.stop();
			synchronized (dynamics) {
				dynamics.remove(schema);
			}
			Runtime.getRuntime().gc();
		} finally {
			Scheduler.start();
		}
	}

	public static DynamicRuntime instance() {
		if (instance == null)
			throw new RuntimeException("DynamicRuntime not initialized");
		return instance;
	}

	private static URL toURL(File file) {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

}
