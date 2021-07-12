package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.system.Settings;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.BasicSelect;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.encoding;

public class Database implements IDatabase, RmiSerializable, Serializable {
	private static final long serialVersionUID = -3409230943645338455L;

	private String schema = null;
	private String user = null;
	private String password = null;
	private String connection = null;
	private String driver = null;
	private encoding charset = encoding.UTF8;

	private boolean isSystemInstalled;
	private boolean isLatestVersion;

	private boolean external = false;

	private DatabaseVendor vendor = DatabaseVendor.SqlServer;

	static private Map<IDatabase, Object> locks = new HashMap<IDatabase, Object>();
	static private Map<String, IDatabase> databases = new HashMap<String, IDatabase>();

	static public IDatabase get(String scheme) {
		Database defaultDatabase = new Database(ServerConfig.databaseSchema(), ServerConfig.databaseUser(), ServerConfig.databasePassword(),
				ServerConfig.databaseConnection(), ServerConfig.databaseDriver(), ServerConfig.databaseCharset());

		if(ServerConfig.isMultitenant())
			defaultDatabase.setSchema(scheme);

		String key = defaultDatabase.key();
		IDatabase database = databases.get(key);

		if(database == null) {
			databases.put(key, defaultDatabase);
			return defaultDatabase;
		}

		return database;
	}

	public Database() {
	}

	private Database(String schema, String user, String password, String connection, String driver, encoding charset) {
		this.schema = schema;
		this.user = user;
		this.password = password;
		this.connection = connection;
		this.driver = driver;
		this.charset = charset;
		this.vendor = DatabaseVendor.fromString(driver);
	}

	public Database(String json) {
		this(new JsonObject(json));
	}

	public Database(JsonObject json) {
		this(json.getString(Json.schema), json.getString(Json.user), json.getString(Json.password), 
				json.getString(Json.connection), json.getString(Json.driver), encoding.fromString(json.getString(Json.charset)));

		external = true;
	}

	public String key() {
		return driver + connection + schema;
	}

	@Override
	public int hashCode() {
		return key().hashCode();
	}

	public Object getLock() {
		Object lock = locks.get(this);
		if(lock == null) {
			lock = new Object();
			locks.put(this, lock);
		}
		return lock;
	}

	public boolean isExternal() {
		return external;
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

		return isSystemInstalled = tableExists(Users.TableName);
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
		RmiIO.writeLong(out, serialVersionUID);

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

		vendor = DatabaseVendor.fromString(RmiIO.readString(in));
	}
}
