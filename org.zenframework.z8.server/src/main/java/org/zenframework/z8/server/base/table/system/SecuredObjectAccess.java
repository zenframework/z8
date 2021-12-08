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

		public final static String Read = "SecuredObjectAccess.read";
		public final static String Write = "SecuredObjectAccess.write";
		public final static String Create = "SecuredObjectAccess.create";
		public final static String Copy = "SecuredObjectAccess.copy";
		public final static String Destroy = "SecuredObjectAccess.destroy";

		public final static String Execute = "SecuredObjectAccess.execute";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);

		public final static String Read = Resources.get(strings.Read);
		public final static String Write = Resources.get(strings.Write);
		public final static String Create = Resources.get(strings.Create);
		public final static String Copy = Resources.get(strings.Copy);
		public final static String Destroy = Resources.get(strings.Destroy);

		public final static String Execute = Resources.get(strings.Execute);
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
		addStaticRecord(Access.Read, SecuredObjects.Database, displayNames.Read);
		addStaticRecord(Access.Write, SecuredObjects.Database, displayNames.Write);
		addStaticRecord(Access.Create, SecuredObjects.Database, displayNames.Create);
		addStaticRecord(Access.Copy, SecuredObjects.Database, displayNames.Copy);
		addStaticRecord(Access.Destroy, SecuredObjects.Database, displayNames.Destroy);

		addStaticRecord(Access.Execute, SecuredObjects.Request, displayNames.Execute);
	}

	private void addStaticRecord(guid id, guid securedObjectId, String name) {
		LinkedHashMap<IField, primary> record = new LinkedHashMap<IField, primary>();
		record.put(this.securedObjectId.get(), new guid(securedObjectId));
		record.put(this.name.get(), new string(name));
		record.put(lock.get(), RecordLock.Full);
		addRecord(id, record);
	}
}
