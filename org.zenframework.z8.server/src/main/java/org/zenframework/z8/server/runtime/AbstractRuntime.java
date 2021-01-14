package org.zenframework.z8.server.runtime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.Executable;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.types.guid;

public abstract class AbstractRuntime implements IRuntime {
	private Map<String, Table.CLASS<? extends Table>> tableClasses = new HashMap<String, Table.CLASS<? extends Table>>();
	private Map<String, Table.CLASS<? extends Table>> tableNames = new HashMap<String, Table.CLASS<? extends Table>>();
	private Map<guid, Table.CLASS<? extends Table>> tableKeys = new HashMap<guid, Table.CLASS<? extends Table>>();

	private Map<String, Executable.CLASS<? extends Executable>> executableClasses = new HashMap<String, Executable.CLASS<? extends Executable>>();
	private Map<String, Executable.CLASS<? extends Executable>> executableNames = new HashMap<String, Executable.CLASS<? extends Executable>>();
	private Map<guid, Executable.CLASS<? extends Executable>> executableKeys = new HashMap<guid, Executable.CLASS<? extends Executable>>();

	private Map<guid, OBJECT.CLASS<? extends OBJECT>> entryKeys = new HashMap<guid, OBJECT.CLASS<? extends OBJECT>>();
	private Map<guid, OBJECT.CLASS<? extends OBJECT>> requestKeys = new HashMap<guid, OBJECT.CLASS<? extends OBJECT>>();
	private Map<guid, Executable.CLASS<? extends Executable>> jobKeys = new HashMap<guid, Executable.CLASS<? extends Executable>>();

	@Override
	public Collection<Table.CLASS<? extends Table>> tables() {
		return tableClasses.values();
	}

	@Override
	public Collection<guid> tableKeys() {
		return tableKeys.keySet();
	}

	@Override
	public Collection<Executable.CLASS<? extends Executable>> executables() {
		return executableKeys.values();
	}

	@Override
	public Collection<guid> executableKeys() {
		return executableKeys.keySet();
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
	public Collection<Executable.CLASS<? extends Executable>> jobs() {
		return jobKeys.values();
	}

	@Override
	public Collection<guid> jobKeys() {
		return jobKeys.keySet();
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
	public Executable.CLASS<? extends Executable> getJob(String name) {
		return AbstractRuntime.<Executable.CLASS<? extends Executable>> get(name, jobs());
	}

	@Override
	public Executable.CLASS<? extends Executable> getJobByKey(guid key) {
		return jobKeys.get(key);
	}

	@Override
	public Executable.CLASS<? extends Executable> getExecutable(String className) {
		return executableClasses.get(className);
	}

	@Override
	public Executable.CLASS<? extends Executable> getExecutableByName(String name) {
		return executableNames.get(name);
	}

	@Override
	public Executable.CLASS<? extends Executable> getExecutableByKey(guid key) {
		return executableKeys.get(key);
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

	@SuppressWarnings("unchecked")
	protected void addExecutable(Executable.CLASS<? extends Executable> cls) {
		for(Map.Entry<String, Executable.CLASS<? extends Executable>> entry : executableClasses.entrySet().toArray(new Map.Entry[0])) {
			Executable.CLASS<? extends Executable> executable = entry.getValue();
			if(executable.getClass().isAssignableFrom(cls.getClass()) && executable.name().equals(cls.name())) {
				executableClasses.remove(executable.classId());
				executableNames.remove(executable.name());
				executableKeys.remove(executable.key());
			} else if(cls.getClass().isAssignableFrom(executable.getClass()))
				return;
		}
		executableClasses.put(cls.classId(), cls);
		executableNames.put(cls.name(), cls);
		executableKeys.put(cls.key(), cls);
	}

	protected void addRequest(OBJECT.CLASS<? extends OBJECT> cls) {
		if(!requestKeys.containsKey(cls.classIdKey()))
			requestKeys.put(cls.classIdKey(), cls);
	}

	protected void addEntry(OBJECT.CLASS<? extends OBJECT> cls) {
		if(!entryKeys.containsKey(cls.classIdKey()))
			entryKeys.put(cls.classIdKey(), cls);
	}

	protected void addJob(Executable.CLASS<? extends Executable> cls) {
		if(!jobKeys.containsKey(cls.classIdKey()))
			jobKeys.put(cls.classIdKey(), cls);
	}

	protected void mergeWith(IRuntime runtime) {
		for(Table.CLASS<? extends Table> table : runtime.tables())
			addTable(table);

		for(Executable.CLASS<? extends Executable> job : runtime.jobs())
			addJob(job);

		for(Executable.CLASS<? extends Executable> executable : runtime.executables())
			addExecutable(executable);

		for(OBJECT.CLASS<? extends OBJECT> entry : runtime.entries())
			addEntry(entry);

		for(OBJECT.CLASS<? extends OBJECT> request : runtime.requests())
			addRequest(request);
	}
}
