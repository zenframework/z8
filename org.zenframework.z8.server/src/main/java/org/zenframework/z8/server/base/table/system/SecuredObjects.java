package org.zenframework.z8.server.base.table.system;

import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class SecuredObjects extends Table {
	final static public String TableName = "System Secured Objects";

	static public guid Database = new guid("36667015-C370-4153-BB38-2F4C7C25A416");
	static public guid Request = new guid("CB201467-F8DE-4C0F-9B08-13C3FACF01E1");

	static public class fieldNames {
		public final static String ClassId = "Class";
	}

	static public class strings {
		public final static String Title = "SecuredObjects.title";
		public final static String Database = "SecuredObjects.database";
		public final static String Request = "SecuredObjects.request";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Database = Resources.get(strings.Database);
		public final static String Request = Resources.get(strings.Request);
	}
	
	public static class CLASS<T extends SecuredObjects> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(SecuredObjects.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new SecuredObjects(container);
		}
	}

	static public SecuredObjects newInstance() {
		return (SecuredObjects)Runtime.instance().getTableByName(SecuredObjects.TableName).newInstance();
	}

	public SecuredObjects(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();
	}

	@Override
	public void constructor2() {
		super.constructor2();

		name.get().length = new integer(256);
	}

	@Override
	public void initStaticRecords() {
		addStaticRecord(Database, displayNames.Database);
		addStaticRecord(Request, displayNames.Request);
	}

	private void addStaticRecord(guid id, String name) {
		LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
		record.put(this.name.get(), new string(name));
		record.put(lock.get(), RecordLock.Full);
		addRecord(id, record);
	}
}
