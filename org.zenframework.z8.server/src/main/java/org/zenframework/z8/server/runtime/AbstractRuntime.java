package org.zenframework.z8.server.runtime;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.Procedure;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;

public abstract class AbstractRuntime implements IRuntime {
	protected Map<String, Table.CLASS<? extends Table>> tableClasses = new HashMap<String, Table.CLASS<? extends Table>>();
	protected Map<String, Table.CLASS<? extends Table>> tableNames = new HashMap<String, Table.CLASS<? extends Table>>();
	protected Map<guid, Table.CLASS<? extends Table>> tableKeys = new HashMap<guid, Table.CLASS<? extends Table>>();

	protected Map<String, OBJECT.CLASS<? extends OBJECT>> entryClasses = new HashMap<String, OBJECT.CLASS<? extends OBJECT>>();
	protected Map<guid, OBJECT.CLASS<? extends OBJECT>> entryKeys = new HashMap<guid, OBJECT.CLASS<? extends OBJECT>>();

	protected Map<String, OBJECT.CLASS<? extends OBJECT>> requestClasses = new HashMap<String, OBJECT.CLASS<? extends OBJECT>>();
	protected Map<guid, OBJECT.CLASS<? extends OBJECT>> requestKeys = new HashMap<guid, OBJECT.CLASS<? extends OBJECT>>();

	protected Map<String, Procedure.CLASS<? extends Procedure>> jobClasses = new HashMap<String, Procedure.CLASS<? extends Procedure>>();
	protected Map<guid, Procedure.CLASS<? extends Procedure>> jobKeys = new HashMap<guid, Procedure.CLASS<? extends Procedure>>();

	protected Map<String, OBJECT.CLASS<? extends OBJECT>> systemTools = new HashMap<String, OBJECT.CLASS<? extends OBJECT>>();

	protected URL url;

	protected AbstractRuntime() {
		Trace.logEvent("Runtime '" + getClass().getCanonicalName() + "' loaded");
	}

	@Override
	public Collection<Table.CLASS<? extends Table>> tables() {
		return tableClasses.values();
	}

	@Override
	public Collection<guid> tableKeys() {
		return tableKeys.keySet();
	}

	@Override
	public Collection<OBJECT.CLASS<? extends OBJECT>> requests() {
		return requestKeys.values();
	}

	@Override
	public Collection<guid> requestKeys() {
		return requestKeys.keySet();
	}

	@Override
	public Collection<OBJECT.CLASS<? extends OBJECT>> entries() {
		return entryKeys.values();
	}

	@Override
	public Collection<guid> entryKeys() {
		return entryKeys.keySet();
	}

	@Override
	public Collection<Procedure.CLASS<? extends Procedure>> jobs() {
		return jobKeys.values();
	}

	@Override
	public Collection<guid> jobKeys() {
		return jobKeys.keySet();
	}

	@Override
	public Collection<OBJECT.CLASS<? extends OBJECT>> systemTools() {
		return systemTools.values();
	}

	@Override
	public Table.CLASS<? extends Table> getTable(String className) {
		return tableClasses.get(className);
	}

	@Override
	public Table.CLASS<? extends Table> getTableByName(String name) {
		return tableNames.get(name);
	}

	@Override
	public Table.CLASS<? extends Table> getTableByKey(guid key) {
		return tableKeys.get(key);
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getRequestByKey(guid key) {
		return requestKeys.get(key);
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getRequest(String name) {
		return requestClasses.get(name);
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getEntry(String name) {
		return entryClasses.get(name);
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getEntryByKey(guid key) {
		return entryKeys.get(key);
	}

	@Override
	public Procedure.CLASS<? extends Procedure> getJob(String name) {
		return jobClasses.get(name);
	}

	@Override
	public Procedure.CLASS<? extends Procedure> getJobByKey(guid key) {
		return jobKeys.get(key);
	}

	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		return getClass().getClassLoader().loadClass(className);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && getClass().equals(obj.getClass());
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	@SuppressWarnings("unchecked")
	protected void addTable(Table.CLASS<? extends Table> cls) {
		for(Table.CLASS<? extends Table> table : tableClasses.values().toArray(new Table.CLASS[tableClasses.size()])) {
			if(table.getClass().isAssignableFrom(cls.getClass()) && table.name().equals(cls.name())) {
				tableClasses.remove(table.classId());
				tableNames.remove(table.name());
				tableKeys.remove(table.key());
			} else if(cls.getClass().isAssignableFrom(table.getClass()))
				return;
		}
		tableClasses.put(cls.classId(), cls);
		tableNames.put(cls.name(), cls);
		tableKeys.put(cls.key(), cls);
	}

	protected void addRequest(OBJECT.CLASS<? extends OBJECT> cls) {
		if(!requestKeys.containsKey(cls.classIdKey())) {
			requestClasses.put(cls.classId(), cls);
			requestKeys.put(cls.classIdKey(), cls);
		}
	}

	protected void addEntry(OBJECT.CLASS<? extends OBJECT> cls) {
		if(!entryKeys.containsKey(cls.classIdKey())) {
			entryClasses.put(cls.classId(), cls);
			entryKeys.put(cls.classIdKey(), cls);
		}
	}

	protected void addJob(Procedure.CLASS<? extends Procedure> cls) {
		if(!jobKeys.containsKey(cls.classIdKey())) {
			jobClasses.put(cls.classId(), cls);
			jobKeys.put(cls.classIdKey(), cls);
		}
	}

	protected void addSystemTool(OBJECT.CLASS<? extends OBJECT> cls) {
		if(!systemTools.containsKey(cls.classId()))
			systemTools.put(cls.classId(), cls);
	}

	protected void mergeRuntime(IRuntime runtime) {
		for(Table.CLASS<? extends Table> table : runtime.tables())
			addTable(table);

		for(Procedure.CLASS<? extends Procedure> job : runtime.jobs())
			addJob(job);

		for(OBJECT.CLASS<? extends OBJECT> entry : runtime.entries())
			addEntry(entry);

		for(OBJECT.CLASS<? extends OBJECT> request : runtime.requests())
			addRequest(request);

		for(OBJECT.CLASS<? extends OBJECT> systemTool : runtime.systemTools())
			addSystemTool(systemTool);
	}

}
