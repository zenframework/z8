package org.zenframework.z8.server.runtime;

import java.net.URL;
import java.util.Collection;

import org.zenframework.z8.server.base.Executable;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.types.guid;

public interface IRuntime {
	public Collection<Table.CLASS<? extends Table>> tables();
	public Collection<guid> tableKeys();

	public Collection<OBJECT.CLASS<? extends OBJECT>> requests();
	public Collection<guid> requestKeys();

	public Collection<OBJECT.CLASS<? extends OBJECT>> entries();
	public Collection<guid> entryKeys();

	public Collection<Executable.CLASS<? extends Executable>> jobs();
	public Collection<guid> jobKeys();

	public Collection<Executable.CLASS<? extends Executable>> executables();
	public Collection<guid> executableKeys();

	public Collection<OBJECT.CLASS<? extends OBJECT>> systemTools();

	public Table.CLASS<? extends Table> getTable(String className);
	public Table.CLASS<? extends Table> getTableByName(String name);
	public Table.CLASS<? extends Table> getTableByKey(guid key);

	public Executable.CLASS<? extends Executable> getExecutable(String className);
	public Executable.CLASS<? extends Executable> getExecutableByName(String name);
	public Executable.CLASS<? extends Executable> getExecutableByKey(guid key);

	public OBJECT.CLASS<? extends OBJECT> getRequest(String className);
	public OBJECT.CLASS<? extends OBJECT> getRequestByKey(guid key);

	public OBJECT.CLASS<? extends OBJECT> getEntry(String className);
	public OBJECT.CLASS<? extends OBJECT> getEntryByKey(guid key);

	public Executable.CLASS<? extends Executable> getJob(String className);
	public Executable.CLASS<? extends Executable> getJobByKey(guid key);

	public Class<?> loadClass(String className) throws ClassNotFoundException;

	public URL getUrl();
}
