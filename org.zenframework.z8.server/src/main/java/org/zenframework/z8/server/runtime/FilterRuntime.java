package org.zenframework.z8.server.runtime;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.zenframework.z8.server.base.Executable;
import org.zenframework.z8.server.base.security.SecurityLog;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.Table.CLASS;
import org.zenframework.z8.server.types.guid;

public abstract class FilterRuntime implements IRuntime {
	@Override
	public Collection<CLASS<? extends Table>> tables() {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.tables() : Collections.emptyList();
	}

	@Override
	public Collection<guid> tableKeys() {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.tableKeys() : Collections.emptyList();
	}

	@Override
	public Collection<OBJECT.CLASS<? extends OBJECT>> requests() {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.requests() : Collections.emptyList();
	}

	@Override
	public Collection<guid> requestKeys() {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.requestKeys() : Collections.emptyList();
	}

	@Override
	public Collection<OBJECT.CLASS<? extends OBJECT>> entries() {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.entries() : Collections.emptyList();
	}

	@Override
	public Collection<guid> entryKeys() {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.entryKeys() : Collections.emptyList();
	}

	@Override
	public Collection<Executable.CLASS<? extends Executable>> jobs() {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.jobs() : Collections.emptyList();
	}

	@Override
	public Collection<guid> jobKeys() {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.jobKeys() : Collections.emptyList();
	}

	@Override
	public Collection<Executable.CLASS<? extends Executable>> executables() {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.executables() : Collections.emptyList();
	}

	@Override
	public Collection<OBJECT.CLASS<? extends OBJECT>> named() {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.named() : Collections.emptyList();
	}

	@Override
	public Collection<OBJECT.CLASS<? extends OBJECT>> systemTools() {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.systemTools() : Collections.emptyList();
	}

	@Override
	public SecurityLog.CLASS<? extends SecurityLog> securityLog() {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.securityLog() : null;
	}

	@Override
	public CLASS<? extends Table> getTable(String className) {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.getTable(className) : null;
	}

	@Override
	public CLASS<? extends Table> getTableByName(String name) {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.getTableByName(name) : null;
	}

	@Override
	public CLASS<? extends Table> getTableByKey(guid key) {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.getTableByKey(key) : null;
	}

	@Override
	public Executable.CLASS<? extends Executable> getExecutableByName(String name) {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.getExecutableByName(name) : null;
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getNamed(String name) {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.getNamed(name) : null;
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getRequest(String className) {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.getRequest(className) : null;
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getRequestByKey(guid key) {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.getRequestByKey(key) : null;
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getEntry(String className) {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.getEntry(className) : null;
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getEntryByKey(guid key) {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.getEntryByKey(key) : null;
	}

	@Override
	public Executable.CLASS<? extends Executable> getJob(String className) {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.getJob(className) : null;
	}

	@Override
	public Executable.CLASS<? extends Executable> getJobByKey(guid key) {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.getJobByKey(key) : null;
	}

	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.loadClass(className) : null;
	}

	@Override
	public List<OBJECT> getControlObjects() {
		IRuntime runtime = runtime();
		return runtime != null ? runtime.getControlObjects() : Collections.emptyList();
	}

	protected abstract IRuntime runtime();
}
