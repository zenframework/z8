package org.zenframework.z8.server.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zenframework.z8.server.base.Procedure;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.IOUtils;

public class ComplexRuntime extends AbstractRuntime {

	private final List<IRuntime> runtimes = new LinkedList<IRuntime>();

	static private final String[] Z8RuntimePaths = { "META-INF/z8.runtime", "META-INF/z8_bl.runtime" };

	public void loadRuntimes(ClassLoader classLoader) {
		for (String path : Z8RuntimePaths) {
			try {
				Enumeration<URL> resources = classLoader.getResources(path);
				while(resources.hasMoreElements())
					loadRuntimes(classLoader, resources.nextElement());
			} catch(IOException e) {
				throw new RuntimeException("Can't load " + path + " resources", e);
			}
		}
	}

	public void loadRuntimes(ClassLoader classLoader, File folder) {
		for (String path : Z8RuntimePaths) {
			try {
				File file = new File(folder, path);
				if (file.exists())
					loadRuntimes(classLoader, file.toURI().toURL());
			} catch(IOException e) {
				throw new RuntimeException("Can't load " + path + " resources", e);
			}
		}
	}

	public void loadRuntimes(ClassLoader classLoader, URL resource) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(resource.openStream()));
			String className;
			while ((className = reader.readLine()) != null) {
				AbstractRuntime runtime = (AbstractRuntime) classLoader.loadClass(className.trim()).newInstance();
				runtime.setUrl(resource);
				addRuntime(runtime);
			}
		} catch (Throwable e) {
			Trace.logError("Can't load runtime-class from resource " + resource, e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	protected void addRuntime(IRuntime runtime) {
		if (runtime == null)
			return;

		if (!(runtime instanceof ComplexRuntime)) {
			mergeRuntime(runtime);
		} else if (!runtimes.contains(runtime)) {
			runtimes.add(runtime);
		} else {
			Trace.logEvent("Runtime '" + runtime.getClass().getCanonicalName() + "' skipped (loaded already)");
		}
	}

	protected List<IRuntime> runtimes() {
		return runtimes;
	}

	@Override
	public Collection<Table.CLASS<? extends Table>> tables() {
		return collectTables().values();
	}

	@Override
	public Collection<guid> tableKeys() {
		return collectTables().keySet();
	}

	@Override
	public Collection<OBJECT.CLASS<? extends OBJECT>> requests() {
		Set<OBJECT.CLASS<? extends OBJECT>> requests = new HashSet<OBJECT.CLASS<? extends OBJECT>>(super.requests());
		for (IRuntime runtime : runtimes())
			requests.addAll(runtime.requests());
		return requests;
	}

	@Override
	public Collection<guid> requestKeys() {
		Set<guid> keys = new HashSet<guid>(super.requestKeys());
		for (IRuntime runtime : runtimes())
			keys.addAll(runtime.requestKeys());
		return keys;
	}

	@Override
	public Collection<OBJECT.CLASS<? extends OBJECT>> entries() {
		Set<OBJECT.CLASS<? extends OBJECT>> entries = new HashSet<OBJECT.CLASS<? extends OBJECT>>(super.entries());
		for (IRuntime runtime : runtimes())
			entries.addAll(runtime.entries());
		return entries;
	}

	@Override
	public Collection<guid> entryKeys() {
		Set<guid> keys = new HashSet<guid>(super.entryKeys());
		for (IRuntime runtime : runtimes())
			keys.addAll(runtime.entryKeys());
		return keys;
	}

	@Override
	public Collection<Procedure.CLASS<? extends Procedure>> jobs() {
		Set<Procedure.CLASS<? extends Procedure>> jobs = new HashSet<Procedure.CLASS<? extends Procedure>>(super.jobs());
		for (IRuntime runtime : runtimes())
			jobs.addAll(runtime.jobs());
		return jobs;
	}

	@Override
	public Collection<guid> jobKeys() {
		Set<guid> keys = new HashSet<guid>(super.jobKeys());
		for (IRuntime runtime : runtimes())
			keys.addAll(runtime.jobKeys());
		return keys;
	}

	@Override
	public Collection<OBJECT.CLASS<? extends OBJECT>> systemTools() {
		Set<OBJECT.CLASS<? extends OBJECT>> systemTools = new HashSet<OBJECT.CLASS<? extends OBJECT>>(super.systemTools());
		for (IRuntime runtime : runtimes())
			systemTools.addAll(runtime.systemTools());
		return systemTools;
	}

	@Override
	public Table.CLASS<? extends Table> getTable(String className) {
		Table.CLASS<? extends Table> table = super.getTable(className);
		if (table != null)
			return table;
		for (IRuntime runtime : runtimes())
			if ((table = runtime.getTable(className)) != null)
				return table;
		return null;
	}

	@Override
	public Table.CLASS<? extends Table> getTableByName(String name) {
		Table.CLASS<? extends Table> table = super.getTableByName(name);
		for (IRuntime runtime : runtimes()) {
			Table.CLASS<? extends Table> candidate = runtime.getTableByName(name);
			if (table == null || candidate != null && table.getClass().isAssignableFrom(candidate.getClass()))
				table = candidate;
		}
		return table;
	}

	@Override
	public Table.CLASS<? extends Table> getTableByKey(guid key) {
		Table.CLASS<? extends Table> table = super.getTableByKey(key);
		if (table != null)
			return table;
		for (IRuntime runtime : runtimes())
			if ((table = runtime.getTableByKey(key)) != null)
				return table;
		return null;
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getRequest(String className) {
		OBJECT.CLASS<? extends OBJECT> request = super.getRequest(className);
		if (request != null)
			return request;
		for (IRuntime runtime : runtimes())
			if ((request = runtime.getRequest(className)) != null)
				return request;
		return null;
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getRequestByKey(guid key) {
		OBJECT.CLASS<? extends OBJECT> request = super.getRequestByKey(key);
		if (request != null)
			return request;
		for (IRuntime runtime : runtimes())
			if ((request = runtime.getRequestByKey(key)) != null)
				return request;
		return null;
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getEntry(String className) {
		OBJECT.CLASS<? extends OBJECT> entry = super.getEntry(className);
		if (entry != null)
			return entry;
		for (IRuntime runtime : runtimes())
			if ((entry = runtime.getEntry(className)) != null)
				return entry;
		return null;
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getEntryByKey(guid key) {
		OBJECT.CLASS<? extends OBJECT> entry = super.getEntryByKey(key);
		if (entry != null)
			return entry;
		for (IRuntime runtime : runtimes())
			if ((entry = runtime.getEntryByKey(key)) != null)
				return entry;
		return null;
	}

	@Override
	public Procedure.CLASS<? extends Procedure> getJob(String className) {
		Procedure.CLASS<? extends Procedure> job = super.getJob(className);
		if (job != null)
			return job;
		for (IRuntime runtime : runtimes())
			if ((job = runtime.getJob(className)) != null)
				return job;
		return null;
	}

	@Override
	public Procedure.CLASS<? extends Procedure> getJobByKey(guid key) {
		Procedure.CLASS<? extends Procedure> job = super.getJobByKey(key);
		if (job != null)
			return job;
		for (IRuntime runtime : runtimes())
			if ((job = runtime.getJobByKey(key)) != null)
				return job;
		return null;
	}

	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		try {
			return super.loadClass(className);
		} catch (ClassNotFoundException e) {
			for (IRuntime runtime : runtimes()) {
				try {
					return runtime.loadClass(className);
				} catch (ClassNotFoundException e1) {}
			}
			throw new ClassNotFoundException(className);
		}
	}

	private Map<guid, Table.CLASS<? extends Table>> collectTables() {
		Map<guid, Table.CLASS<? extends Table>> tables = new HashMap<guid, Table.CLASS<? extends Table>>(tableKeys);
		for (IRuntime runtime : runtimes()) {
			for (Table.CLASS<? extends Table> candidate : runtime.tables()) {
				boolean addCandidate = true;
				Iterator<Map.Entry<guid, Table.CLASS<? extends Table>>> it = tables.entrySet().iterator();
				while (it.hasNext()) {
					Table.CLASS<? extends Table> table = it.next().getValue();
					if (table.getClass().isAssignableFrom(candidate.getClass()) && table.name().equals(candidate.name())) {
						it.remove();
					} else if (candidate.getClass().isAssignableFrom(table.getClass())) {
						addCandidate = false;
						break;
					}
				}
				if (addCandidate)
					tables.put(candidate.key(), candidate);
			}
		}
		return tables;
	}

}
