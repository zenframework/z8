package org.zenframework.z8.justintime.runtime;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.FilterRuntime;
import org.zenframework.z8.server.runtime.IRuntime;
import org.zenframework.z8.server.runtime.Runtimes;

public class DynamicRuntime extends FilterRuntime {
	private static class DynamicClassLoader extends URLClassLoader {

		private final Workspace workspace;

		DynamicClassLoader(Workspace workspace) {
			super(new URL[] { toURL(workspace.getJavaClasses()) });
			this.workspace = workspace;
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			Trace.logEvent("Schema '" + workspace.getSchema() + "' dynamic code unloaded");
		}

	}

	public static DynamicRuntime instance() {
		if (instance == null)
			throw new RuntimeException("DynamicRuntime not initialized");
		return instance;
	}

	private static DynamicRuntime instance;

	private final Map<String, IRuntime> dynamics = Collections.synchronizedMap(new HashMap<String, IRuntime>());

	public DynamicRuntime() {
		for (Workspace workspace : Workspace.workspaces())
			loadDynamic(workspace);

		if (instance != null)
			throw new RuntimeException("DynamicRuntime reinitialized");

		instance = this;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	protected IRuntime runtime() {
		return dynamics.get(ApplicationServer.getSchema());
	}

	public void loadDynamic() {
		loadDynamic(Workspace.workspace(ApplicationServer.getSchema()));
	}

	public void unloadDynamic() {
		IDatabase database = ApplicationServer.getDatabase();

		try {
			Scheduler.suspend(database);
			dynamics.remove(database.schema());
			Runtime.getRuntime().gc();
		} finally {
			Scheduler.resume(database);
		}
	}

	private void loadDynamic(Workspace workspace) {
		String schema = workspace.getSchema();

		if (schema == null)
			return;

		try {
			Collection<IRuntime> runtimes = Runtimes.loadRuntimes(new DynamicClassLoader(workspace), workspace.getJavaClasses());
			if (!runtimes.isEmpty()) {
				dynamics.put(schema, runtimes.iterator().next());
				Trace.logEvent("Schema '" + schema + "' dynamic code loaded");
			}
		} catch (Throwable e) {
			Trace.logError("Couldn't load schema '" + schema + "' dynamic code", e);
		}
	}

	private static URL toURL(File file) {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
