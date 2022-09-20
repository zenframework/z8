package org.zenframework.z8.server.runtime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.Procedure;
import org.zenframework.z8.server.base.security.SecurityLog;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.types.guid;

public abstract class AbstractRuntime implements IRuntime {
	private Map<String, Table.CLASS<? extends Table>> tableClasses = new HashMap<String, Table.CLASS<? extends Table>>();
	private Map<String, Table.CLASS<? extends Table>> tableNames = new HashMap<String, Table.CLASS<? extends Table>>();
	private Map<guid, Table.CLASS<? extends Table>> tableKeys = new HashMap<guid, Table.CLASS<? extends Table>>();

	private Map<guid, OBJECT.CLASS<? extends OBJECT>> entryKeys = new HashMap<guid, OBJECT.CLASS<? extends OBJECT>>();
	private Map<guid, OBJECT.CLASS<? extends OBJECT>> requestKeys = new HashMap<guid, OBJECT.CLASS<? extends OBJECT>>();
	private Map<guid, Procedure.CLASS<? extends Procedure>> jobKeys = new HashMap<guid, Procedure.CLASS<? extends Procedure>>();

	protected SecurityLog.CLASS<? extends SecurityLog> securityLog = null;

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
	public SecurityLog.CLASS<? extends SecurityLog> securityLog() {
		return securityLog;
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
		return AbstractRuntime.<OBJECT.CLASS<? extends OBJECT>> get(name, requests());
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getEntry(String name) {
		return AbstractRuntime.<OBJECT.CLASS<? extends OBJECT>> get(name, entries());
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getEntryByKey(guid key) {
		return entryKeys.get(key);
	}

	@Override
	public Procedure.CLASS<? extends Procedure> getJob(String name) {
		return AbstractRuntime.<Procedure.CLASS<? extends Procedure>> get(name, jobs());
	}

	@Override
	public Procedure.CLASS<? extends Procedure> getJobByKey(guid key) {
		return jobKeys.get(key);
	}

	private static <T extends OBJECT.CLASS<? extends OBJECT>> T get(String name, Collection<T> list) {
		for(T cls : list) {
			if(name.equals(cls.classId()))
				return cls;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected void addTable(Table.CLASS<? extends Table> cls) {
		for(Map.Entry<String, Table.CLASS<? extends Table>> entry : tableClasses.entrySet().toArray(new Map.Entry[0])) {
			Table.CLASS<? extends Table> table = entry.getValue();
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
		if(!requestKeys.containsKey(cls.classIdKey()))
			requestKeys.put(cls.classIdKey(), cls);
	}

	protected void addEntry(OBJECT.CLASS<? extends OBJECT> cls) {
		if(!entryKeys.containsKey(cls.classIdKey()))
			entryKeys.put(cls.classIdKey(), cls);
	}

	protected void addJob(Procedure.CLASS<? extends Procedure> cls) {
		if(!jobKeys.containsKey(cls.classIdKey()))
			jobKeys.put(cls.classIdKey(), cls);
	}

	protected void addSecurityLog(SecurityLog.CLASS<? extends SecurityLog> cls) {
		if (cls != null && (securityLog == null || securityLog.getClass().isAssignableFrom(cls.getClass())))
			securityLog = cls;
	}

	protected void mergeWith(IRuntime runtime) {
		for(Table.CLASS<? extends Table> table : runtime.tables())
			addTable(table);

		for(Procedure.CLASS<? extends Procedure> job : runtime.jobs())
			addJob(job);

		for(OBJECT.CLASS<? extends OBJECT> entry : runtime.entries())
			addEntry(entry);

		for(OBJECT.CLASS<? extends OBJECT> request : runtime.requests())
			addRequest(request);

		addSecurityLog(runtime.securityLog());
	}
}
