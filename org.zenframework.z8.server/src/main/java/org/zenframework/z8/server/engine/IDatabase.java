package org.zenframework.z8.server.engine;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.types.encoding;

public interface IDatabase {
	class Info {
		private final int id;
		private final String schema;
		private final String user;
		private final String connection;
		private final String driver;
		private final String charset;

		public Info(int id, String schema, String user, String connection, String driver, String charset) {
			this.id = id;
			this.schema = schema;
			this.user = user;
			this.connection = connection;
			this.driver = driver;
			this.charset = charset;
		}

		public int getId() {
			return id;
		}

		public String getSchema() {
			return schema;
		}

		public String getConnection() {
			return connection;
		}

		public String getDriver() {
			return driver;
		}

		public String getCharset() {
			return charset;
		}

		public String getUser() {
			return user;
		}
	}

	public Object getLock();

	public boolean isExternal();

	public DatabaseVendor vendor();

	public int id();
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

	public Info getInfo();
}
