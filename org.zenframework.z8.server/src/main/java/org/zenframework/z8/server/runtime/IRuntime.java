package org.zenframework.z8.server.runtime;

import java.net.URL;
import java.util.Collection;

import org.zenframework.z8.server.base.Executable;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.types.guid;

public interface IRuntime {
	Collection<Table.CLASS<? extends Table>> tables();
	Collection<guid> tableKeys();

	Collection<OBJECT.CLASS<? extends OBJECT>> requests();
	Collection<guid> requestKeys();

	Collection<OBJECT.CLASS<? extends OBJECT>> entries();
	Collection<guid> entryKeys();

	Collection<Executable.CLASS<? extends Executable>> jobs();
	Collection<guid> jobKeys();

	Collection<Executable.CLASS<? extends Executable>> executables();
	Collection<guid> executableKeys();

	Collection<OBJECT.CLASS<? extends OBJECT>> systemTools();

	Table.CLASS<? extends Table> getTable(String className);
	Table.CLASS<? extends Table> getTableByName(String name);
	Table.CLASS<? extends Table> getTableByKey(guid key);

	Executable.CLASS<? extends Executable> getExecutable(String className);
	Executable.CLASS<? extends Executable> getExecutableByName(String name);
	Executable.CLASS<? extends Executable> getExecutableByKey(guid key);

	OBJECT.CLASS<? extends OBJECT> getRequest(String className);
	OBJECT.CLASS<? extends OBJECT> getRequestByKey(guid key);

	OBJECT.CLASS<? extends OBJECT> getEntry(String className);
	OBJECT.CLASS<? extends OBJECT> getEntryByKey(guid key);

	Executable.CLASS<? extends Executable> getJob(String className);
	Executable.CLASS<? extends Executable> getJobByKey(guid key);

	Class<?> loadClass(String className) throws ClassNotFoundException;

	URL getUrl();
}
