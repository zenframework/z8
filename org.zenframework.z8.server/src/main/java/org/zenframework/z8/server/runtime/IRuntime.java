package org.zenframework.z8.server.runtime;

import java.util.Collection;

import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.types.guid;

public interface IRuntime {
	public Collection<Table.CLASS<? extends Table>> tables();
	public Collection<guid> tableKeys();

	public Collection<OBJECT.CLASS<? extends OBJECT>> entries();

	public Collection<Procedure.CLASS<? extends Procedure>> jobs();

	public Table.CLASS<? extends Table> getTable(String className);

	public Table.CLASS<? extends Table> getTableByName(String name);

	public Table.CLASS<? extends Table> getTableByKey(guid key);

	public OBJECT.CLASS<? extends OBJECT> getEntry(String className);

	public Procedure.CLASS<? extends Procedure> getJob(String className);
}
