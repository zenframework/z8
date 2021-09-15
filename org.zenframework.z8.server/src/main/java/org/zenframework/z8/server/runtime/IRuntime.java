package org.zenframework.z8.server.runtime;

import java.net.URL;
import java.util.Collection;

import org.zenframework.z8.server.base.Procedure;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.types.guid;

public interface IRuntime {
	Collection<Table.CLASS<? extends Table>> tables();
	Collection<guid> tableKeys();

	Collection<OBJECT.CLASS<? extends OBJECT>> requests();
	Collection<guid> requestKeys();

	Collection<OBJECT.CLASS<? extends OBJECT>> entries();
	Collection<guid> entryKeys();

	Collection<Procedure.CLASS<? extends Procedure>> jobs();
	Collection<guid> jobKeys();

	Collection<OBJECT.CLASS<? extends OBJECT>> systemTools();

	Table.CLASS<? extends Table> getTable(String className);
	Table.CLASS<? extends Table> getTableByName(String name);
	Table.CLASS<? extends Table> getTableByKey(guid key);

	OBJECT.CLASS<? extends OBJECT> getRequest(String className);
	OBJECT.CLASS<? extends OBJECT> getRequestByKey(guid key);

	OBJECT.CLASS<? extends OBJECT> getEntry(String className);
	OBJECT.CLASS<? extends OBJECT> getEntryByKey(guid key);

	Procedure.CLASS<? extends Procedure> getJob(String className);
	Procedure.CLASS<? extends Procedure> getJobByKey(guid key);

	Class<?> loadClass(String className) throws ClassNotFoundException;

	URL getUrl();
}
