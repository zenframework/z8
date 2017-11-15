package org.zenframework.z8.server.base.sql;

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

	public org.zenframework.z8.server.engine.Database database;

	public Database(IObject container) {
		super(container);
	}

	static public Database.CLASS<? extends Database> z8_fromJson(string json) {
		Database.CLASS<Database> cls = new Database.CLASS<Database>(null);
		cls.get().database = new org.zenframework.z8.server.engine.Database(json.get());
		return cls;
	}
}
