package org.zenframework.z8.server.engine;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.types.encoding;

public interface IDatabase {
	public Object getLock();

	public boolean isExternal();

	public DatabaseVendor vendor();

	public String schema();
	public String password();
	public String user();
	public String connection();
	public String driver();
	public encoding charset();

	public String tableName(String name);
	public boolean tableExists(String name);
	public boolean fieldExists(String table, String field);

	public void renameTable(String tableName, String newTableName);
	public void renameField(String tableName, String fieldName, String newFieldName);

	public void dropTable(String tableName);

	public boolean isSystemInstalled();
	public boolean isLatestVersion();
}
