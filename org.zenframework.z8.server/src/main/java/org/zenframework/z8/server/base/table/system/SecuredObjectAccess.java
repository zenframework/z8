package org.zenframework.z8.server.base.table.system;

import java.util.LinkedHashMap;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.Access;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class SecuredObjectAccess extends Table {
	final static public String TableName = "System Secured Obect Access";

	static public class fieldNames {
		public final static String SecuredObject = "Secured object";
	}

	static public class strings {
		public final static String Title = "SecuredObjectAccess.title";
		public final static String Name = "SecuredObjectAccess.name";

		public final static String TableRead = "SecuredObjectAccess.tableRead";
		public final static String TableWrite = "SecuredObjectAccess.tableWrite";
		public final static String TableCreate = "SecuredObjectAccess.tableCreate";
		public final static String TableCopy = "SecuredObjectAccess.tableCopy";
		public final static String TableDestroy = "SecuredObjectAccess.tableDestroy";

		public final static String FieldRead = "SecuredObjectAccess.fieldRead";
		public final static String FieldWrite = "SecuredObjectAccess.fieldWrite";

		public final static String RequestExecute = "SecuredObjectAccess.requestExecute";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);

		public final static String TableRead = Resources.get(strings.TableRead);
		public final static String TableWrite = Resources.get(strings.TableWrite);
		public final static String TableCreate = Resources.get(strings.TableCreate);
		public final static String TableCopy = Resources.get(strings.TableCopy);
		public final static String TableDestroy = Resources.get(strings.TableDestroy);

		public final static String FieldRead = Resources.get(strings.FieldRead);
		public final static String FieldWrite = Resources.get(strings.FieldWrite);

		public final static String RequestExecute = Resources.get(strings.RequestExecute);
	}

	public static class CLASS<T extends SecuredObjectAccess> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(SecuredObjectAccess.class);
			setName(SecuredObjectAccess.TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new SecuredObjectAccess(container);
		}
	}

	static public SecuredObjectAccess newInstance() {
		return (SecuredObjectAccess)Runtime.instance().getTableByName(SecuredObjectAccess.TableName).newInstance();
	}

	public SecuredObjects.CLASS<SecuredObjects> securedObject = new SecuredObjects.CLASS<SecuredObjects>(this);

	public Link.CLASS<Link> securedObjectId = new Link.CLASS<Link>(this);

	public SecuredObjectAccess(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(securedObject);
		objects.add(securedObjectId);
	}

	@Override
	public void constructor1() {
		securedObjectId.get(IClass.Constructor1).operatorAssign(securedObject);
	}
	
	@Override
	public void constructor2() {
		super.constructor2();

		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(256);

		securedObject.setIndex("securedObject");

		securedObjectId.setName(fieldNames.SecuredObject);
		securedObjectId.setIndex("securedObjectId");
	}

	@Override
	public void initStaticRecords() {
		addStaticRecord(Access.TableRead, SecuredObjects.Table, displayNames.TableRead);
		addStaticRecord(Access.TableWrite, SecuredObjects.Table, displayNames.TableWrite);
		addStaticRecord(Access.TableCreate, SecuredObjects.Table, displayNames.TableCreate);
		addStaticRecord(Access.TableCopy, SecuredObjects.Table, displayNames.TableCopy);
		addStaticRecord(Access.TableDestroy, SecuredObjects.Table, displayNames.TableDestroy);

		addStaticRecord(Access.FieldRead, SecuredObjects.Field, displayNames.FieldRead);
		addStaticRecord(Access.FieldWrite, SecuredObjects.Field, displayNames.FieldWrite);

		addStaticRecord(Access.RequestExecute, SecuredObjects.Request, displayNames.RequestExecute);
	}

	private void addStaticRecord(guid id, guid securedObjectId, String name) {
		LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
		record.put(this.securedObjectId.get(), new guid(securedObjectId));
		record.put(this.name.get(), new string(name));
		record.put(lock.get(), RecordLock.Full);
		addRecord(id, record);
	}
}
