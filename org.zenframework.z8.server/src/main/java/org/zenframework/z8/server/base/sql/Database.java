package org.zenframework.z8.server.base.sql;

import org.zenframework.z8.server.base.json.parser.JsonObject;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.string;

public class Database extends OBJECT {
	static public string Schema = new string("schema");
	static public string User = new string("user");
	static public string Password = new string("password");
	static public string Connection = new string("connection");
	static public string Driver = new string("driver");
	static public string Charset = new string("charset");

	public static class CLASS<T extends Database> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Database.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Database(container);
		}
	}

	public IDatabase database;

	public Database(IObject container) {
		super(container);
	}

	public JsonObject.CLASS<JsonObject> z8_toJson() {
		JsonObject.CLASS<JsonObject> json = new JsonObject.CLASS<JsonObject>();
		json.get().z8_put(Schema, new string(database.schema()));
		json.get().z8_put(User, new string(database.user()));
		json.get().z8_put(Password, new string(database.password()));
		json.get().z8_put(Connection, new string(database.connection()));
		json.get().z8_put(Driver, new string(database.driver()));
		json.get().z8_put(Charset, new string(database.charset().toString()));
		return json;
	}

	static public Database.CLASS<? extends Database> z8_fromJson(string json) {
		Database.CLASS<Database> cls = new Database.CLASS<Database>(null);
		cls.get().database = new org.zenframework.z8.server.engine.Database(json.get());
		return cls;
	}

	static public Database.CLASS<? extends Database> z8_fromJson(JsonObject.CLASS<? extends JsonObject> json) {
		Database.CLASS<Database> cls = new Database.CLASS<Database>(null);
		cls.get().database = new org.zenframework.z8.server.engine.Database(json.get().getInternalObject());
		return cls;
	}

	static public Database.CLASS<? extends Database> z8_get() {
		Database.CLASS<Database> cls = new Database.CLASS<Database>(null);
		cls.get().database = ApplicationServer.getUser().database();
		return cls;
	}

	public Connection.CLASS<? extends Connection> z8_connection() {
		Connection.CLASS<Connection> cls = new Connection.CLASS<Connection>(null);
		cls.get().connection = ConnectionManager.get(database);
		return cls;
	}

	public string z8_schema() {
		return new string(database.schema());
	}
}
