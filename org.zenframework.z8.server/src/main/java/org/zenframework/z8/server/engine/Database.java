package org.zenframework.z8.server.engine;

import java.sql.SQLException;
import java.util.Properties;

import org.zenframework.z8.server.base.table.system.Settings;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.db.BasicSelect;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.encoding;

public class Database {

	private String schema = null;
	private String user = null;
	private String password = null;
	private String connection = null;
	private String driver = null;
	private encoding charset = encoding.UTF8;

	private boolean isSystemInstalled = false;
	private boolean isLatestVersion = false;

	private DatabaseVendor vendor = DatabaseVendor.SqlServer;

	public Database() {
	}

	public Database(Properties prop) {
		setSchema(prop.getProperty("application.database.schema"));
		setUser(prop.getProperty("application.database.user"));
		setPassword(prop.getProperty("application.database.password"));
		setConnection(prop.getProperty("application.database.connection"));
		setDriver(prop.getProperty("application.database.driver"));
		setCharset(encoding.fromString(prop.getProperty("application.database.charset")));
	}

	public DatabaseVendor vendor() {
		return vendor;
	}

	public String schema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String password() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String user() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String connection() {
		return connection;
	}

	public void setConnection(String connection) {
		this.connection = connection;
	}

	public String driver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
		this.vendor = DatabaseVendor.fromString(driver);
	}

	public encoding charset() {
		return charset;
	}

	public void setCharset(encoding charset) {
		this.charset = charset;
	}

	public String tableName(String name) {
		return vendor.quote(schema()) + "." + vendor.quote(name);
	}

	public boolean tableExists(String name) {
		String sql = null;

		DatabaseVendor vendor = vendor();

		if(vendor == DatabaseVendor.SqlServer) {
			sql = "SELECT COUNT(TABLE_NAME) FROM INFORMATION_SCHEMA.COLUMNS " + "WHERE TABLE_NAME = '" + name + "' AND " + "TABLE_CATALOG = '" + schema() + "'";
		} else if(vendor == DatabaseVendor.Postgres) {
			sql = "SELECT COUNT(table_name) FROM information_schema.tables " + "WHERE table_name = '" + name + "' AND " + "table_schema = '" + schema() + "'";
		} else if(vendor == DatabaseVendor.Oracle) {
			sql = "SELECT COUNT(TABLE_NAME) FROM ALL_TAB_COLUMNS " + "WHERE TABLE_NAME = '" + name + "' AND " + "OWNER = '" + schema() + "'";
		}

		Cursor cursor = null;

		try {
			cursor = BasicSelect.cursor(sql);
			return cursor.next() && cursor.getInteger(1).get() != 0;
		} catch(SQLException e) {
			Trace.logError(e);
			throw new RuntimeException(e);
		} finally {
			if(cursor != null)
				cursor.close();
		}
	}

	public boolean isSystemInstalled() {
		if(isSystemInstalled)
			return isSystemInstalled;

		try {
			isSystemInstalled = tableExists(Users.TableName);
		} catch(Throwable e) {
			Trace.logEvent(e);
		} finally {
			ConnectionManager.release(this);
		}

		return isSystemInstalled;
	}

	public boolean isLatestVersion() {
		if(isLatestVersion)
			return isLatestVersion;

		if(!tableExists(Settings.TableName))
			return false;

		String version = Runtime.version();
		String currentVersion = Settings.version();

		return (isLatestVersion = version.equals(currentVersion));
	}
}
