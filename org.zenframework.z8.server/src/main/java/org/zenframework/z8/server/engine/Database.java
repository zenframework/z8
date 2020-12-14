package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.zenframework.z8.server.base.table.system.Settings;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.db.BasicSelect;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.encoding;

public class Database implements RmiSerializable, Serializable {
	private static final long serialVersionUID = -3409230943645338455L;

	private String schema = null;
	private String user = null;
	private String password = null;
	private String connection = null;
	private String driver = null;
	private encoding charset = encoding.UTF8;

	private boolean isSystemInstalled = false;
	private boolean isLatestVersion = false;

	private DatabaseVendor vendor = DatabaseVendor.SqlServer;

	static private Map<Database, Object> locks = new HashMap<Database, Object>();

	public Database() {
	}

	public Database(Properties properties) {
		setSchema(properties.getProperty("application.database.schema"));
		setUser(properties.getProperty("application.database.user"));
		setPassword(properties.getProperty("application.database.password"));
		setConnection(properties.getProperty("application.database.connection"));
		setDriver(properties.getProperty("application.database.driver"));
		setCharset(encoding.fromString(properties.getProperty("application.database.charset")));
	}

	public Database(String json) {
		this(new JsonObject(json));
	}

	public Database(JsonObject json) {
		setSchema(json.getString("schema"));
		setUser(json.getString("user"));
		setPassword(json.getString("password"));
		setConnection(json.getString("connection"));
		setDriver(json.getString("driver"));
		setCharset(encoding.fromString(json.getString("charset")));
	}

	@Override
	public int hashCode() {
		return (driver() + connection() + schema()).hashCode();
	}

	public Object getLock() {
		Object lock = locks.get(this);
		if(lock == null) {
			lock = new Object();
			locks.put(this, lock);
		}
		return lock;
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
		} else if(vendor == DatabaseVendor.H2) {
			sql = "SELECT COUNT(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES " + "WHERE TABLE_NAME = '" + name + "' AND " + "	TABLE_SCHEMA = '" + schema() + "'";
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
			ConnectionManager.release();
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

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		RmiIO.writeString(out, schema);
		RmiIO.writeString(out, user);
		RmiIO.writeString(out, password);
		RmiIO.writeString(out, connection);
		RmiIO.writeString(out, driver);
		RmiIO.writeString(out, charset.toString());

		RmiIO.writeBoolean(out, isSystemInstalled);
		RmiIO.writeBoolean(out, isLatestVersion);

		RmiIO.writeString(out, vendor.toString());
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		schema = RmiIO.readString(in);
		user = RmiIO.readString(in);
		password = RmiIO.readString(in);
		connection = RmiIO.readString(in);
		driver = RmiIO.readString(in);
		charset = encoding.fromString(RmiIO.readString(in));

		isSystemInstalled = RmiIO.readBoolean(in);
		isLatestVersion = RmiIO.readBoolean(in);

		charset = encoding.fromString(RmiIO.readString(in));
		vendor = DatabaseVendor.fromString(RmiIO.readString(in));
	}
}
