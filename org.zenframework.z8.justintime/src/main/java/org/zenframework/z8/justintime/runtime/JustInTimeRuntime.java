package org.zenframework.z8.justintime.runtime;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import org.zenframework.z8.justintime.table.Source;
import org.zenframework.z8.justintime.table.SourcesView;
import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.table.system.SystemTools;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.AbstractRuntime;
import org.zenframework.z8.server.runtime.ComplexRuntime;
import org.zenframework.z8.server.runtime.IRuntime;

public class JustInTimeRuntime extends ComplexRuntime {

	private static class InternalRuntime extends AbstractRuntime {

		InternalRuntime() {
			addTable(new Source.CLASS<Source>(null));
		}

	}

	private static class JustInTimeClassLoader extends URLClassLoader {

		JustInTimeClassLoader(Workspace workspace) {
			super(new URL[] { toURL(workspace.getJavaClasses()) });
		}

	}

	private static JustInTimeRuntime instance;

	private final List<IRuntime> dynamicRuntimes = new LinkedList<IRuntime>();

	public JustInTimeRuntime() {
		super.addRuntime(new InternalRuntime());

		loadDynamic();

		if (instance != null)
			throw new RuntimeException("JustInTimeRuntime reinitialized");

		instance = this;

		SystemTools.addExtension(SourcesView.CLASS.class);
	}

	@Override
	protected List<IRuntime> runtimes() {
		List<IRuntime> runtimes = new LinkedList<IRuntime>(super.runtimes());
		synchronized (dynamicRuntimes) {
			runtimes.addAll(dynamicRuntimes);
		}
		return runtimes;
	}

	@Override
	protected void addRuntime(IRuntime runtime) {
		synchronized (dynamicRuntimes) {
			dynamicRuntimes.add(runtime);
		}
	}

	public void loadDynamic() {
		for (Workspace workspace : Workspace.workspaces()) {
			try {
				ClassLoader cl = new JustInTimeClassLoader(workspace);
				loadRuntimes(cl, workspace.getJavaClasses());
			} catch (Exception e) {
				Trace.logError(e);
			}
		}
	}

	public void unloadDynamic() {
		IDatabase database = ApplicationServer.getDatabase();
		try {
			Scheduler.suspend(database);
			synchronized (dynamicRuntimes) {
				dynamicRuntimes.clear();
			}
			Runtime.getRuntime().gc();
		} finally {
			Scheduler.resume(database);
		}
	}

	public static JustInTimeRuntime instance() {
		if (instance == null)
			throw new RuntimeException("JustInTimeRuntime not initialized");
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
